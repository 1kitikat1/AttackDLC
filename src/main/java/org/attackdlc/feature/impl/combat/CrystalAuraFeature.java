package org.attackdlc.feature.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
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
 * CrystalAura — агрессивный HvH кристалл.
 * Ставит кристаллы на любой блок, сбивает все кристаллы в радиусе,
 * поддерживает мульти-плейс, предсказание движения и silent switch.
 */
@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class CrystalAuraFeature extends Feature {
    private static final float PLACE_ANGLE_TOLERANCE = 14.0F;
    private static final float BREAK_ANGLE_TOLERANCE = 14.0F;

    // ── Радусы ────────────────────────────────────────────────────────────────
    private final NumberSetting targetRange   = this.register(new NumberSetting("Target Range",  16.0, 3.0,  16.0, 0.5, " blocks"));
    private final NumberSetting placeRange    = this.register(new NumberSetting("Place Range",    5.5, 1.0,   6.0, 0.1, " blocks"));
    private final NumberSetting breakRange    = this.register(new NumberSetting("Break Range",    5.5, 2.0,   6.0, 0.1, " blocks"));
    private final NumberSetting minDamage     = this.register(new NumberSetting("Min Damage",     0.0, 0.0,  20.0, 0.5, " dmg"));
    private final NumberSetting maxSelfDmg    = this.register(new NumberSetting("Max Self Dmg",   12.0, 0.5,  20.0, 0.5, " dmg"));
    private final NumberSetting placeDelay    = this.register(new NumberSetting("Place Delay",   50.0, 0.0, 500.0, 10.0, " ms"));
    private final NumberSetting breakDelay    = this.register(new NumberSetting("Break Delay",   50.0, 0.0, 500.0, 10.0, " ms"));

    // ── Режимы ────────────────────────────────────────────────────────────────
    private final ModeSetting rotateMode   = this.register(new ModeSetting("Rotate Mode", "Packet", "Packet", "Client", "None"));
    private final ModeSetting targetPrior  = this.register(new ModeSetting("Target Priority", "Closest", "Closest", "Lowest HP", "Highest Dmg", "Random"));
    private final ModeSetting placeMode    = this.register(new ModeSetting("Place Mode", "Best", "Best", "Any", "Fastest"));

    // ── Флаги ─────────────────────────────────────────────────────────────────
    private final BooleanSetting silentSwitch  = this.register(new BooleanSetting("Silent Switch",  true));
    private final BooleanSetting multiPlace    = this.register(new BooleanSetting("Multi Place",    true));
    private final BooleanSetting predictMove   = this.register(new BooleanSetting("Predict Move",   true));
    private final BooleanSetting ignoreWalls   = this.register(new BooleanSetting("Ignore Walls",   false));
    private final BooleanSetting antiSuicide   = this.register(new BooleanSetting("Anti Suicide",   true));
    private final BooleanSetting hvhMode       = this.register(new BooleanSetting("HvH Mode",       false));

    // ── Состояние ─────────────────────────────────────────────────────────────
    private final StopWatch  placeTimer   = new StopWatch();
    private final StopWatch  breakTimer   = new StopWatch();
    private final StopWatch  swingTimer   = new StopWatch();
    private LivingEntity     currentTarget;
    private int              restoreSlot  = -1;
    private Vec3             lastPlacedPos;

    public CrystalAuraFeature() {
        super("CrystalAura", "Aggressive HvH end crystal: places & detonates crystals automatically.", FeatureCategory.COMBAT, -1);
    }

    public static CrystalAuraFeature getEnabled() {
        return FeatureManager.INSTANCE.getEnabled(CrystalAuraFeature.class);
    }

    @Override
    protected void onEnable() {
        resetState();
    }

    @Override
    protected void onDisable() {
        resetState();
    }

    @EventTarget
    public void onWorldLeave(WorldLeaveEvent event) {
        resetState();
    }

    @EventTarget
    public void onTick(GameTickEvent event) {
        Minecraft mc    = event.getClient();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.gameMode == null) return;

        // Восстанавливаем слот после silent switch
        if (restoreSlot != -1 && player.getInventory().getSelectedSlot() != restoreSlot) {
            switchSlot(player, restoreSlot);
            restoreSlot = -1;
        }

        this.currentTarget = findTarget(player, level);
        if (this.currentTarget == null) return;

        boolean hvh = hvhMode.getValue();

        // 1) Сбиваем существующие кристаллы
        if (attackBestCrystal(mc, player, hvh)) {
            return;
        }

        // 2) Ставим новые кристаллы
        tryPlaceCrystal(mc, player, level, hvh);
    }

    // ── Target finding ─────────────────────────────────────────────────────────

    private LivingEntity findTarget(LocalPlayer player, ClientLevel level) {
        double rangeSq = targetRange.getValue() * targetRange.getValue();
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        String prio = targetPrior.getValue();

        for (Player c : level.players()) {
            if (c == player || !c.isAlive() || c.isSpectator() || c.isCreative()
                    || FriendManager.INSTANCE.isFriend(c.getGameProfile().name())) {
                continue;
            }
            double distSq = c.getBoundingBox().getCenter().distanceToSqr(player.position());
            if (distSq > rangeSq) continue;

            double score;
            switch (prio) {
                case "Lowest HP"   -> score = c.getHealth();
                case "Highest Dmg" -> score = -explosionDamage(c, player.position());
                case "Random"      -> score = Math.random() * 100;
                default            -> score = distSq; // Closest
            }
            if (score < bestScore) {
                bestScore = score;
                best = c;
            }
        }
        return best;
    }

    // ── Break crystals ─────────────────────────────────────────────────────────

    private boolean attackBestCrystal(Minecraft mc, LocalPlayer player, boolean hvh) {
        if (!breakTimer.finished(breakDelay.getValue())) return false;

        LivingEntity target = currentTarget;
        if (target == null) return false;

        EndCrystal best = null;
        float bestDmg = hvh ? -1.0F : minDamage.getValue().floatValue();

        for (EndCrystal crystal : mc.level.getEntitiesOfClass(EndCrystal.class,
                player.getBoundingBox().inflate(breakRange.getValue()))) {
            if (!crystal.isAlive()) continue;
            Vec3 center = crystal.position();
            float selfDmg = explosionDamage(player, center);
            float tgtDmg  = explosionDamage(target, center);

            if (!selfDamageOk(player, selfDmg, hvh)) continue;

            // В HvH-режиме не бомбим себя ради мизерного урона по цели
            if (hvh && tgtDmg <= selfDmg) continue;

            // Урон по цели должен быть не меньше минимального (в не-HvH)
            if (!hvh && tgtDmg < minDamage.getValue().floatValue()) continue;

            // Выбираем кристалл с максимальным уроном по цели
            if (tgtDmg <= bestDmg) continue;

            best = crystal;
            bestDmg = tgtDmg;
        }
        if (best == null) return false;

        rotateTo(player, best.position());
        if (!isAimed(player, best.position(), BREAK_ANGLE_TOLERANCE)) return false;

        mc.gameMode.attack(player, best);
        swingMain(player);
        breakTimer.reset();
        return true;
    }

    // ── Place crystal ──────────────────────────────────────────────────────────

    private void tryPlaceCrystal(Minecraft mc, LocalPlayer player, ClientLevel level, boolean hvh) {
        if (!placeTimer.finished(placeDelay.getValue())) return;

        List<Placement> candidates = findAllPlacements(player, level, hvh);
        if (candidates.isEmpty()) return;

        Placement best = candidates.get(0);
        rotateTo(player, best.facePoint());
        if (!isAimed(player, best.facePoint(), PLACE_ANGLE_TOLERANCE)) return;

        int restore = switchToCrystal(mc, player);
        if (restore == -1 && !player.getMainHandItem().is(Items.END_CRYSTAL)) return;

        // Находим нужный слот для мульти-плейса (если silent switch)
        int crystalSlot = -1;
        if (silentSwitch.getValue()) {
            for (int i = 0; i < 9; i++) {
                if (player.getInventory().getItem(i).is(Items.END_CRYSTAL)) {
                    crystalSlot = i;
                    break;
                }
            }
        }

        BlockHitResult hit = new BlockHitResult(
                best.facePoint(), best.face(), best.support(), false);
        mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        swingMain(player);

        if (restore != -1) {
            switchSlot(player, restore);
        }

        lastPlacedPos = best.facePoint();
        placeTimer.reset();

        // Multi-place: ставим дополнительные кристаллы быстро
        if (multiPlace.getValue() && candidates.size() > 1) {
            for (int i = 1; i < Math.min(3, candidates.size()); i++) {
                Placement extra = candidates.get(i);

                // При silent switch перед каждым useItemOn отправляем пакет смены слота
                if (silentSwitch.getValue() && crystalSlot != -1) {
                    PacketUtil.sendHeldItemChange(crystalSlot);
                }

                BlockHitResult h = new BlockHitResult(
                        extra.facePoint(), extra.face(), extra.support(), false);
                mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, h);
                swingMain(player);
            }

            // Восстанавливаем слот после мульти-плейса при silent switch
            if (silentSwitch.getValue() && restore != -1) {
                switchSlot(player, restore);
            }
        }
    }

    /**
     * Синхронный поиск всех валидных позиций для установки кристалла.
     * Асинхронный подход с CompletableFuture и таймаутом 2мс здесь не работал:
     * задачи просто не успевали выполниться.
     */
    private List<Placement> findAllPlacements(LocalPlayer player, ClientLevel level, boolean hvh) {
        LivingEntity target = currentTarget;
        if (target == null) return List.of();

        int r = Math.max(1, (int) Math.ceil(placeRange.getValue()));
        BlockPos pPos = player.blockPosition();
        List<Placement> results = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                pPos.offset(-r, -1, -r), pPos.offset(r, 2, r))) {
            Placement p = evaluatePlacement(pos.immutable(), player, level, target, hvh);
            if (p != null) {
                results.add(p);
            }
        }

        String mode = placeMode.getValue();
        if ("Best".equals(mode)) {
            results.sort(Comparator.comparingDouble(p -> -p.damage()));
        } else if ("Fastest".equals(mode)) {
            results.sort(Comparator.comparingDouble(p -> p.distanceSq));
        }
        // "Any" — без сортировки

        return results;
    }

    private Placement evaluatePlacement(BlockPos pos, LocalPlayer player, ClientLevel level,
                                        LivingEntity target, boolean hvh) {
        if (!canPlaceCrystal(level, pos, hvh)) return null;

        Vec3 explosion = Vec3.atLowerCornerOf(pos).add(0.5, 1.0, 0.5);
        if (!ignoreWalls.getValue() && !isVisible(player, explosion)) return null;

        double dp = player.position().distanceToSqr(explosion);
        double de = player.getEyePosition().distanceToSqr(explosion);
        double pr = placeRange.getValue() * placeRange.getValue();
        if (dp > pr && de > pr) return null;

        float selfDmg = explosionDamage(player, explosion);
        if (!selfDamageOk(player, selfDmg, hvh)) return null;

        // Предсказание движения цели
        Vec3 targetPos = predictMove.getValue()
                ? target.position().add(target.getDeltaMovement().scale(0.5))
                : target.position();
        float damage = explosionDamageAt(target, explosion, targetPos);
        if (!hvh && damage < minDamage.getValue().floatValue()) return null;

        double distSq = player.position().distanceToSqr(explosion);
        return new Placement(pos.immutable(), explosion, Direction.UP, damage, distSq);
    }

    // ── Placement validity ─────────────────────────────────────────────────────

    private boolean canPlaceCrystal(ClientLevel level, BlockPos pos, boolean hvh) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && !state.canBeReplaced()) return false;

        BlockState support = level.getBlockState(pos.below());
        if (hvh) {
            // HvH: можно ставить на любой не-воздушный блок
            if (support.isAir()) return false;
        } else {
            // Обычный режим: только обсидиан, плачущий обсидиан, бедрок
            if (!support.is(Blocks.OBSIDIAN) && !support.is(Blocks.CRYING_OBSIDIAN)
                    && !support.is(Blocks.BEDROCK)) {
                return false;
            }
        }

        // Проверяем, что в позиции кристалла нет сущностей
        AABB box = new AABB(pos).inflate(0.05);
        for (var entity : level.getEntities(null, box)) {
            if (entity instanceof LivingEntity living && living.isAlive()) return false;
        }
        return true;
    }

    // ── Rotation ───────────────────────────────────────────────────────────────

    private void rotateTo(LocalPlayer player, Vec3 point) {
        double dx = point.x - player.getX();
        double dy = point.y - (player.getY() + player.getEyeHeight());
        double dz = point.z - player.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -(Math.toDegrees(Math.atan2(dy, horiz)));

        String rm = rotateMode.getValue();
        if ("None".equals(rm)) return;

        Angle angle = new Angle(yaw, pitch);
        if ("Client".equals(rm)) {
            AngleConnection.INSTANCE.rotateTo(angle, 1,
                    new AngleConfig(new LinearConstructor(), false, false),
                    TaskPriority.HIGH_IMPORTANCE_2, this);
        } else {
            AngleConnection.INSTANCE.rotateTo(angle, 1,
                    new AngleConfig(new LinearConstructor(), true, true),
                    TaskPriority.HIGH_IMPORTANCE_2, this);
        }
    }

    private boolean isAimed(LocalPlayer player, Vec3 point, float tolerance) {
        double dx = point.x - player.getX();
        double dy = point.y - (player.getY() + player.getEyeHeight());
        double dz = point.z - player.getZ();
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -(Math.toDegrees(Math.atan2(dy, horiz)));
        return Math.max(
                Math.abs(Mth.wrapDegrees(yaw - player.getYRot())),
                Math.abs(pitch - player.getXRot())) <= tolerance;
    }

    private boolean isVisible(LocalPlayer player, Vec3 point) {
        HitResult r = player.level().clip(new ClipContext(
                player.getEyePosition(), point,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player));
        return r.getType() == HitResult.Type.MISS;
    }

    // ── Item switching ────────────────────────────────────────────────────────

    private int switchToCrystal(Minecraft mc, LocalPlayer player) {
        if (player.getMainHandItem().is(Items.END_CRYSTAL)) return -1;

        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(Items.END_CRYSTAL)) {
                slot = i;
                break;
            }
        }
        if (slot == -1) return -1;

        if (silentSwitch.getValue()) {
            // Отправляем пакет смены слота, но не меняем слот на клиенте.
            // Использование предмета выполняется через mc.gameMode.useItemOn,
            // который на клиенте проверит предмет в руке. Чтобы это сработало,
            // временно меняем слот, вызываем useItemOn, затем возвращаем.
            restoreSlot = player.getInventory().getSelectedSlot();
            switchSlot(player, slot);
            return restoreSlot;
        }

        restoreSlot = player.getInventory().getSelectedSlot();
        switchSlot(player, slot);
        return restoreSlot;
    }

    private static void switchSlot(LocalPlayer player, int slot) {
        if (slot >= 0 && slot <= 8 && player.getInventory().getSelectedSlot() != slot) {
            player.getInventory().setSelectedSlot(slot);
            PacketUtil.sendHeldItemChange(slot);
        }
    }

    private static void swingMain(LocalPlayer player) {
        player.swing(InteractionHand.MAIN_HAND);
    }

    // ── Damage calculation ─────────────────────────────────────────────────────

    private boolean selfDamageOk(LocalPlayer player, float selfDmg, boolean hvh) {
        float limit = hvh
                ? Math.min(12.0F, maxSelfDmg.getValue().floatValue())
                : maxSelfDmg.getValue().floatValue();
        if (selfDmg > limit) return false;
        if (antiSuicide.getValue() && !hvh) {
            return selfDmg < player.getHealth() - 0.5F;
        }
        return true;
    }

    /**
     * Урон от взрыва по цели в её текущей позиции.
     */
    private static float explosionDamage(LivingEntity target, Vec3 explosion) {
        return explosionDamageAt(target, explosion, target.position());
    }

    /**
     * Урон от взрыва с учётом предсказанной позиции цели.
     */
    private static float explosionDamageAt(LivingEntity target, Vec3 explosion, Vec3 predictedPos) {
        AABB box = target.getBoundingBox();
        Vec3 center = predictedPos.add(0, box.getYsize() * 0.5, 0);
        double dist = center.distanceTo(explosion);
        if (dist >= 12.0) return 0.0F;

        double impact = (1.0 - dist / 12.0) * exposure(target, explosion, center);
        double raw = (impact * impact + impact) / 2.0 * 7.0 * 6.0 * 2.0 + 1.0;
        float armor = target.getArmorValue();
        float toughness = (float) target.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        float f = 2.0F + toughness / 4.0F;
        float clamped = Mth.clamp(armor - (float) raw / f, armor * 0.2F, 20.0F);
        return (float) raw * (1.0F - clamped / 25.0F);
    }

    /**
     * Более точный расчёт экспозиции: проверяем видимость до нескольких точек хитбокса.
     */
    private static float exposure(LivingEntity target, Vec3 explosion, Vec3 center) {
        AABB box = target.getBoundingBox();
        double yMid = box.minY + box.getYsize() * 0.5;
        Vec3[] points = {
                center,
                new Vec3(box.minX, yMid, box.minZ),
                new Vec3(box.maxX, yMid, box.maxZ),
                new Vec3(box.minX, yMid, box.maxZ),
                new Vec3(box.maxX, yMid, box.minZ),
                new Vec3(center.x, box.maxY - 0.1, center.z)
        };
        int visible = 0;
        for (Vec3 p : points) {
            HitResult r = target.level().clip(new ClipContext(
                    explosion, p,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    target));
            if (r.getType() == HitResult.Type.MISS) {
                visible++;
            }
        }
        return (float) visible / points.length;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void resetState() {
        currentTarget = null;
        restoreSlot = -1;
        lastPlacedPos = null;
    }

    private record Placement(BlockPos support, Vec3 facePoint, Direction face,
                             float damage, double distanceSq) {}
}