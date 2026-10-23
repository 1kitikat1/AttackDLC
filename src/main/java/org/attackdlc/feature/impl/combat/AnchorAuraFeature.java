package org.attackdlc.feature.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.game.GameTickEvent;
import org.attackdlc.event.events.lifecycle.WorldLeaveEvent;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.feature.FeatureManager;
import org.attackdlc.feature.setting.BooleanSetting;
import org.attackdlc.feature.setting.ModeSetting;
import org.attackdlc.feature.setting.NumberSetting;
import org.attackdlc.utils.FriendManager;
import org.attackdlc.utils.combat.StopWatch;
import org.attackdlc.utils.combat.aura.Angle;
import org.attackdlc.utils.combat.aura.AngleConfig;
import org.attackdlc.utils.combat.aura.AngleConnection;
import org.attackdlc.utils.combat.aura.impl.LinearConstructor;
import org.attackdlc.utils.combat.aura.util.TaskPriority;
import org.attackdlc.utils.network.PacketUtil;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

/**
 * AnchorAura — установка, зарядка и подрыв якорей возрождения вокруг цели.
 *
 * <p>Цикл (приоритет сверху): BREAK → CHARGE → PLACE.
 * Светокамень в offhand удваивает требуемые заряды.
 */
@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class AnchorAuraFeature extends Feature {

    private final NumberSetting charges         = this.register(new NumberSetting("Заряды",        1, 1,  4, 1, ""));
    private final NumberSetting targetRange     = this.register(new NumberSetting("Target Range",  8, 2, 12, 1, " blocks"));
    private final NumberSetting range           = this.register(new NumberSetting("Range",         4.5f, 1f, 6f, 0.1f, " blocks"));
    private final NumberSetting minDamage       = this.register(new NumberSetting("Min Damage",    2.0, 0f, 20f, 0.5f, " dmg"));
    private final NumberSetting maxSelfDmg      = this.register(new NumberSetting("Max Self Dmg",  6.0, 0f, 20f, 0.5f, " dmg"));
    private final NumberSetting delay           = this.register(new NumberSetting("Delay",         200f, 0f, 500f, 50f, " ms"));
    private final BooleanSetting ignoreWalls    = this.register(new BooleanSetting("Ignore Walls", false));
    private final ModeSetting rotateMode        = this.register(new ModeSetting("Rotate Mode", "Packet", "Packet", "Client", "None"));

    private final StopWatch actionTimer = new StopWatch();
    private LivingEntity currentTarget;
    private int restoreSlot = -1;

    public AnchorAuraFeature() {
        super("AnchorAura", "Respawn anchor: place, charge and detonate around target.",
                FeatureCategory.COMBAT, -1);
    }

    public static AnchorAuraFeature getEnabled() {
        return FeatureManager.INSTANCE.getEnabled(AnchorAuraFeature.class);
    }

    @Override
    protected void onEnable()  { resetState(); }
    @Override
    protected void onDisable() { resetState(); }

    @EventTarget
    public void onWorldLeave(WorldLeaveEvent event) { resetState(); }

    @EventTarget
    public void onTick(GameTickEvent event) {
        Minecraft mc        = event.getClient();
        LocalPlayer player  = mc.player;
        ClientLevel level   = mc.level;
        if (player == null || level == null || mc.gameMode == null) return;

        if (restoreSlot != -1 && player.getInventory().getSelectedSlot() != restoreSlot) {
            switchSlot(player, restoreSlot);
            restoreSlot = -1;
        }

        this.currentTarget = findTarget(player, level);
        if (this.currentTarget == null) return;
        if (!actionTimer.finished(delay.getValue())) return;
        if (isPaused(player)) return;

        if (tryBreakAnchor(mc, player, level)) return;
        if (tryChargeAnchor(mc, player, level)) return;
        if (tryPlaceAnchor(mc, player, level)) return;
    }

    // ── Target ──────────────────────────────────────────────────────────────────

    private LivingEntity findTarget(LocalPlayer player, ClientLevel level) {
        double rangeSq = targetRange.getValue() * targetRange.getValue();
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (Player p : level.players()) {
            if (p == player || !p.isAlive() || p.isCreative() || p.isSpectator()
                    || FriendManager.INSTANCE.isFriend(p.getGameProfile().name())) continue;
            double d = p.distanceToSqr(player);
            if (d <= rangeSq && d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }

    private boolean isPaused(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        return player.isUsingItem() || player.isShiftKeyDown() || mc.gui.screen() != null
                || player.containerMenu != player.inventoryMenu
                || !player.containerMenu.getCarried().isEmpty();
    }

    // ── BREAK ───────────────────────────────────────────────────────────────────

    private boolean tryBreakAnchor(Minecraft mc, LocalPlayer player, ClientLevel level) {
        List<Candidate> cands = findBreakable(player, level);
        if (cands.isEmpty()) return false;
        Candidate best = cands.get(0);

        rotateTo(player, best.pos);
        if (!isAimed(player, Vec3.atCenterOf(best.pos), 14f)) return false;
        Vec3 src = Vec3.atLowerCornerOf(best.pos).add(0.5, 0.5, 0.5);
        float selfDmg = anchorExplosionDamage(player, src, player);
        if (selfDmg > maxSelfDmg.getValue() || selfDmg >= player.getHealth() - 0.5f) return false;

        int slot = findEmptyHandSlot(player);
        if (slot < 0) return false;
        int restore = switchToHand(player, slot, mc);
        mc.gameMode.destroyBlock(best.pos);
        player.swing(InteractionHand.MAIN_HAND);
        if (restore != -1) switchSlot(player, restore);
        actionTimer.reset();
        return true;
    }

    // ── CHARGE ──────────────────────────────────────────────────────────────────

    private boolean tryChargeAnchor(Minecraft mc, LocalPlayer player, ClientLevel level) {
        int required = effectiveCharges();
        List<Candidate> cands = findChargeable(player, level, required);
        if (cands.isEmpty()) return false;
        Candidate best = cands.get(0);

        rotateTo(player, best.pos);
        if (!isAimed(player, Vec3.atCenterOf(best.pos), 14f)) return false;

        int slot = findGlowstoneSlot(player);
        if (slot < 0) return false;
        int restore = switchToHand(player, slot, mc);

        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(best.pos), Direction.UP, best.pos, false);
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        player.swing(InteractionHand.MAIN_HAND);
        if (restore != -1) switchSlot(player, restore);
        actionTimer.reset();
        return true;
    }

    // ── PLACE ───────────────────────────────────────────────────────────────────

    private boolean tryPlaceAnchor(Minecraft mc, LocalPlayer player, ClientLevel level) {
        List<Candidate> cands = findPlaceable(player, level);
        if (cands.isEmpty()) return false;
        Candidate best = cands.get(0);

        rotateTo(player, best.support);
        if (!isAimed(player, Vec3.atCenterOf(best.support), 14f)) return false;

        int slot = findAnchorSlot(player);
        if (slot < 0) return false;
        int restore = switchToHand(player, slot, mc);

        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(best.support), Direction.UP, best.support, false);
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        player.swing(InteractionHand.MAIN_HAND);
        if (restore != -1) switchSlot(player, restore);
        actionTimer.reset();
        return true;
    }

    // ── Поиск кандидатов ────────────────────────────────────────────────────────

    private List<Candidate> findBreakable(LocalPlayer player, ClientLevel level) {
        List<Candidate> r = new ArrayList<>();
        BlockPos center = currentTarget.blockPosition();
        double rngSq = range.getValue() * range.getValue();
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                for (int y = -1; y <= 2; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.getWorldBorder().isWithinBounds(pos)) continue;
                    BlockState st = level.getBlockState(pos);
                    if (!st.is(Blocks.RESPAWN_ANCHOR)) continue;
                    if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > rngSq) continue;
                    if (!ignoreWalls.getValue() && !isVisible(player, Vec3.atCenterOf(pos))) continue;
                    Vec3 src = Vec3.atLowerCornerOf(pos).add(0.5, 0.5, 0.5);
                    float dmg = anchorExplosionDamage(player, src, currentTarget);
                    if (dmg < minDamage.getValue()) continue;
                    r.add(new Candidate(pos, pos.below(), dmg, player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos))));
                }
        r.sort(Comparator.comparingDouble(c -> -c.dmg));
        return r;
    }

    private List<Candidate> findChargeable(LocalPlayer player, ClientLevel level, int required) {
        List<Candidate> r = new ArrayList<>();
        BlockPos center = currentTarget.blockPosition();
        double rngSq = range.getValue() * range.getValue();
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                for (int y = -1; y <= 2; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.getWorldBorder().isWithinBounds(pos)) continue;
                    BlockState st = level.getBlockState(pos);
                    if (!st.is(Blocks.RESPAWN_ANCHOR)) continue;
                    if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > rngSq) continue;
                    if (!ignoreWalls.getValue() && !isVisible(player, Vec3.atCenterOf(pos))) continue;
                    r.add(new Candidate(pos, pos, 0f, player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos))));
                }
        r.sort(Comparator.comparingDouble(c -> c.distSq));
        return r;
    }

    private List<Candidate> findPlaceable(LocalPlayer player, ClientLevel level) {
        List<Candidate> r = new ArrayList<>();
        BlockPos center = currentTarget.blockPosition();
        double rngSq = range.getValue() * range.getValue();
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                for (int y = -1; y <= 2; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.getWorldBorder().isWithinBounds(pos)) continue;
                    BlockState st = level.getBlockState(pos);
                    if (!st.isAir() && !st.canBeReplaced()) continue;
                    BlockState support = level.getBlockState(pos.below());
                    if (support.isAir()) continue;
                    if (st.is(Blocks.RESPAWN_ANCHOR)) continue;
                    if (!level.getBlockState(pos.above()).isAir()) continue;
                    if (!level.getBlockState(pos.above(2)).isAir()) continue;
                    AABB box = new AABB(pos).inflate(0.5);
                    boolean hasLiving = false;
                    for (var e : level.getEntities(null, box)) {
                        if (e instanceof LivingEntity le && le.isAlive()) { hasLiving = true; break; }
                    }
                    if (hasLiving) continue;
                    if (player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > rngSq) continue;
                    if (!ignoreWalls.getValue() && !isVisible(player, Vec3.atCenterOf(pos))) continue;
                    r.add(new Candidate(pos, pos.below(), 0f, player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos))));
                }
        r.sort(Comparator.comparingDouble(c -> c.distSq));
        return r;
    }

    // ── Rotation ─────────────────────────────────────────────────────────────────

    private void rotateTo(LocalPlayer player, BlockPos target) {
        Vec3 eye = player.getEyePosition();
        Vec3 center = Vec3.atCenterOf(target);
        Vec3 d = center.subtract(eye);
        if (d.lengthSqr() < 1e-8) return;
        double horiz = Math.sqrt(d.x * d.x + d.z * d.z);
        float yaw   = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
        float pitch = (float) -(Math.toDegrees(Math.atan2(d.y, horiz)));
        String rm = rotateMode.getValue();
        if ("None".equals(rm)) return;
        AngleConnection.INSTANCE.rotateTo(
                new Angle(yaw, pitch), 1,
                new AngleConfig(new LinearConstructor(), "Packet".equals(rm), "Packet".equals(rm)),
                TaskPriority.HIGH_IMPORTANCE_2, this);
    }

    private boolean isAimed(LocalPlayer player, Vec3 point, float tolerance) {
        Vec3 d = point.subtract(player.getEyePosition());
        double horiz = Math.sqrt(d.x * d.x + d.z * d.z);
        float yaw   = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
        float pitch = (float) -(Math.toDegrees(Math.atan2(d.y, horiz)));
        return Math.max(
                Math.abs(Mth.wrapDegrees(yaw - player.getYRot())),
                Math.abs(pitch - player.getXRot())) <= tolerance;
    }

    private boolean isVisible(LocalPlayer player, Vec3 point) {
        HitResult r = player.level().clip(new ClipContext(
                player.getEyePosition(), point,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                player));
        return r.getType() == HitResult.Type.MISS;
    }

    // ── Слоты ────────────────────────────────────────────────────────────────────

    private int findAnchorSlot(LocalPlayer player) {
        for (int i = 0; i < 9; i++)
            if (player.getInventory().getItem(i).is(Items.RESPAWN_ANCHOR)) return i;
        return -1;
    }

    private int findGlowstoneSlot(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.GLOWSTONE)) return -2;
        for (int i = 0; i < 9; i++)
            if (player.getInventory().getItem(i).is(Items.GLOWSTONE)) return i;
        return -1;
    }

    private int findEmptyHandSlot(LocalPlayer player) {
        if (player.getMainHandItem().isEmpty()) return player.getInventory().getSelectedSlot();
        for (int i = 0; i < 9; i++)
            if (player.getInventory().getItem(i).isEmpty()) return i;
        return -1;
    }

    private int switchToHand(LocalPlayer player, int targetSlot, Minecraft mc) {
        int current = player.getInventory().getSelectedSlot();
        if (targetSlot == current || targetSlot == -2) return -1;
        restoreSlot = current;
        player.getInventory().setSelectedSlot(targetSlot);
        PacketUtil.sendHeldItemChange(targetSlot);
        return restoreSlot;
    }

    private static void switchSlot(LocalPlayer player, int slot) {
        if (slot < 0 || slot > 8) return;
        if (player.getInventory().getSelectedSlot() != slot) {
            player.getInventory().setSelectedSlot(slot);
            PacketUtil.sendHeldItemChange(slot);
        }
    }

    // ── Урон ─────────────────────────────────────────────────────────────────────

    private float anchorExplosionDamage(LocalPlayer player, Vec3 source, LivingEntity target) {
        AABB box = target.getBoundingBox();
        Vec3 center = target.position().add(0, box.getYsize() * 0.5, 0);
        double diameter = 10.0;
        double distance = center.distanceTo(source) / diameter;
        if (distance >= 1.0) return 0f;
        double yMid = box.minY + box.getYsize() * 0.5;
        Vec3[] pts = {
                center,
                new Vec3(box.minX, yMid, box.minZ),
                new Vec3(box.maxX, yMid, box.maxZ),
                new Vec3(box.minX, yMid, box.maxZ),
                new Vec3(box.maxX, yMid, box.minZ),
        };
        int clear = 0;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return 0f;
        for (Vec3 p : pts) {
            HitResult r = level.clip(new ClipContext(
                    source, p, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target));
            if (r.getType() == HitResult.Type.MISS) clear++;
        }
        double impact = (1.0 - distance) * clear / (double) pts.length;
        float raw = (float) Math.floor((impact * impact + impact) / 2.0 * 7.0 * diameter + 1.0);
        raw = switch (Minecraft.getInstance().level.getDifficulty()) {
            case PEACEFUL -> 0;
            case EASY     -> Math.min(raw / 2f + 1, raw);
            case HARD     -> raw * 1.5f;
            default       -> raw;
        };
        float armor = target.getArmorValue();
        float tough = (float) target.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        float clamped = Mth.clamp(armor - raw / (2f + tough / 4f), armor * 0.2f, 20f);
        float dmg = raw * (1f - clamped / 25f);
        if (target.hasEffect(net.minecraft.world.effect.MobEffects.RESISTANCE)) dmg *= 0.8f;
        int blastProt = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            var enchList = target.getItemBySlot(slot).getEnchantments();
            for (Entry<Holder<Enchantment>> en : enchList.entrySet()) {
                int lvl = en.getIntValue();
                if (en.getKey().is(Enchantments.BLAST_PROTECTION)) blastProt += lvl * 2;
                else if (en.getKey().is(Enchantments.PROTECTION)) blastProt += lvl;
            }
        }
        dmg *= 1f - Math.min(20, blastProt) / 25f;
        return Math.max(0f, dmg);
    }

    private int effectiveCharges() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getOffhandItem().is(Items.GLOWSTONE))
            return (int) (charges.getValue().doubleValue() * 2);
        return (int) (charges.getValue().doubleValue());
    }

    private void resetState() {
        currentTarget = null;
        restoreSlot = -1;
        actionTimer.reset();
    }

    private record Candidate(BlockPos pos, BlockPos support, float dmg, double distSq) {}
}
