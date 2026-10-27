package org.attackdlc.feature;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.input.KeyboardInputEvent;
import org.attackdlc.event.events.input.MouseInputEvent;
import org.attackdlc.event.events.lifecycle.ShutdownEvent;
import org.attackdlc.feature.impl.combat.AimAssistantFeature;
import org.attackdlc.feature.impl.combat.AntiBotFeature;
import org.attackdlc.feature.impl.combat.AuraFeature;
import org.attackdlc.feature.impl.combat.AutoClickerFeature;
import org.attackdlc.feature.impl.combat.AutoExplosionFeature;
import org.attackdlc.feature.impl.combat.AutoMaceFeature;
import org.attackdlc.feature.impl.combat.AutoTotemFeature;
import org.attackdlc.feature.impl.combat.BowAimbotFeature;
import org.attackdlc.feature.impl.combat.CriticalsFeature;
import org.attackdlc.feature.impl.combat.CrystalAuraFeature;
import org.attackdlc.feature.impl.combat.AnchorAuraFeature;
import org.attackdlc.feature.impl.combat.HitBoxesFeature;
import org.attackdlc.feature.impl.combat.HitSoundFeature;
import org.attackdlc.feature.impl.combat.LungeBoostFeature;
import org.attackdlc.feature.impl.combat.ReachFeature;
import org.attackdlc.feature.impl.combat.ShiftTapFeature;
import org.attackdlc.feature.impl.combat.TriggerBotFeature;
import org.attackdlc.feature.impl.combat.VelocityFeature;
import org.attackdlc.feature.impl.misc.AuctionHelperFeature;
import org.attackdlc.feature.impl.misc.AutoDuelFeature;
import org.attackdlc.feature.impl.misc.AutoTpaAcceptFeature;
import org.attackdlc.feature.impl.misc.ChestStealerFeature;
import org.attackdlc.feature.impl.misc.DeathCoordsFeature;
import org.attackdlc.feature.impl.misc.ElytraHelperFeature;
import org.attackdlc.feature.impl.misc.NameProtectFeature;
import org.attackdlc.feature.impl.misc.NoDelaysFeature;
import org.attackdlc.feature.impl.misc.ServerHelperFeature;
import org.attackdlc.feature.impl.misc.UseTrackerFeature;
import org.attackdlc.feature.impl.misc.XCarryFeature;
import org.attackdlc.feature.impl.movement.AirStuckFeature;
import org.attackdlc.feature.impl.movement.AutoJumpFeature;
import org.attackdlc.feature.impl.movement.ElytraBoosterFeature;
import org.attackdlc.feature.impl.movement.ElytraMotionFeature;
import org.attackdlc.feature.impl.movement.ElytraTargetFeature;
import org.attackdlc.feature.impl.movement.FlyFeature;
import org.attackdlc.feature.impl.movement.GrimCollideFeature;
import org.attackdlc.feature.impl.movement.GrimElytraFeature;
import org.attackdlc.feature.impl.movement.GrimFlyFeature;
import org.attackdlc.feature.impl.movement.HighJumpFeature;
import org.attackdlc.feature.impl.movement.InventoryMoveFeature;
import org.attackdlc.feature.impl.movement.NoFallFeature;
import org.attackdlc.feature.impl.movement.NoPushFeature;
import org.attackdlc.feature.impl.movement.NoSlowFeature;
import org.attackdlc.feature.impl.movement.NoWebFeature;
import org.attackdlc.feature.impl.movement.ParkourFeature;
import org.attackdlc.feature.impl.movement.SafeWalkFeature;
import org.attackdlc.feature.impl.movement.SpeedFeature;
import org.attackdlc.feature.impl.movement.SprintFeature;
import org.attackdlc.feature.impl.movement.StrafeFeature;
import org.attackdlc.feature.impl.movement.TimerFeature;
import org.attackdlc.feature.impl.movement.WallClimbFeature;
import org.attackdlc.feature.impl.movement.WaterSpeedFeature;
import org.attackdlc.feature.impl.player.AutoRespawnFeature;
import org.attackdlc.feature.impl.player.AutoSwapFeature;
import org.attackdlc.feature.impl.player.AutoToolFeature;
import org.attackdlc.feature.impl.player.ClickPearlFeature;
import org.attackdlc.feature.impl.player.DiscordRPCFeature;
import org.attackdlc.feature.impl.player.FarmHelperFeature;
import org.attackdlc.feature.impl.player.FastBreakFeature;
import org.attackdlc.feature.impl.player.FreeCamFeature;
import org.attackdlc.feature.impl.player.FreeLookFeature;
import org.attackdlc.feature.impl.player.FullBrightFeature;
import org.attackdlc.feature.impl.player.ItemScrollerFeature;
import org.attackdlc.feature.impl.player.MultiActionFeature;
import org.attackdlc.feature.impl.player.NoJumpBoostFeature;
import org.attackdlc.feature.impl.player.WindJumpFeature;
import org.attackdlc.feature.impl.pve.AntiAfkFeature;
import org.attackdlc.feature.impl.pve.AppleFarmerFeature;
import org.attackdlc.feature.impl.pve.AuctionRelistFeature;
import org.attackdlc.feature.impl.pve.AutoArmorFeature;
import org.attackdlc.feature.impl.pve.AutoAuthFeature;
import org.attackdlc.feature.impl.pve.AutoCrafterFeature;
import org.attackdlc.feature.impl.pve.AutoFishFeature;
import org.attackdlc.feature.impl.pve.AutoGappleFeature;
import org.attackdlc.feature.impl.pve.AutoLeaveFeature;
import org.attackdlc.feature.impl.pve.AutoPotionFeature;
import org.attackdlc.feature.impl.pve.AutoPottBotFeature;
import org.attackdlc.feature.impl.misc.PanicFeature;
import org.attackdlc.feature.impl.pve.AutoTpLootFeature;
import org.attackdlc.feature.impl.pve.AutoUseFeature;
import org.attackdlc.feature.impl.pve.ClanInvestFeature;
import org.attackdlc.feature.impl.pve.GriefJoinerFeature;
import org.attackdlc.feature.impl.pve.MineHelperFeature;
import org.attackdlc.feature.impl.pve.PveManagerFeature;
import org.attackdlc.feature.impl.visual.ArrowsFeature;
import org.attackdlc.feature.impl.visual.AtmoDawnFogFeature;
import org.attackdlc.feature.impl.visual.BlockEspFeature;
import org.attackdlc.feature.impl.visual.BlockOutlineFeature;
import org.attackdlc.feature.impl.visual.ChamsFeature;
import org.attackdlc.feature.impl.visual.CrosshairFeature;
import org.attackdlc.feature.impl.visual.SwingAnimationFeature;
import org.attackdlc.feature.impl.visual.EntityEspFeature;
import org.attackdlc.feature.impl.visual.HitParticlesFeature;
import org.attackdlc.feature.impl.visual.HoldMyItemsFeature;
import org.attackdlc.feature.impl.visual.HudFeature;
import org.attackdlc.feature.impl.visual.ItemPhysicsFeature;
import org.attackdlc.feature.impl.visual.JumpCirclesFeature;
import org.attackdlc.feature.impl.visual.KillEffectFeature;
import org.attackdlc.feature.impl.visual.NameTagsFeature;
import org.attackdlc.feature.impl.visual.PopChamsFeature;
import org.attackdlc.feature.impl.visual.RemovalsFeature;
import org.attackdlc.feature.impl.visual.SeeInvisibleFeature;
import org.attackdlc.feature.impl.visual.ShaderHandsFeature;
import org.attackdlc.feature.impl.visual.TargetESPFeature;
import org.attackdlc.feature.impl.visual.TrajectoriesFeature;
import org.attackdlc.feature.impl.visual.ViewModelFeature;
import org.attackdlc.feature.impl.visual.WorldParticlesFeature;
import org.attackdlc.feature.impl.visual.WorldTweaksFeature;
import org.attackdlc.feature.setting.BindSetting;

public final class FeatureManager {
   public static final FeatureManager INSTANCE = new FeatureManager();
   private final Map<String, Feature> features = new LinkedHashMap<>();
   private final Map<Class<? extends Feature>, Feature> featuresByType = new ConcurrentHashMap<>();
   private final FeatureConfigStore configStore = new FeatureConfigStore();
   private final ScheduledExecutorService configWriter = Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "AttackDLC Config Writer");
      thread.setDaemon(true);
      return thread;
   });
   private final Object saveLock = new Object();
   private final Map<Feature, Set<Integer>> activeHoldBinds = new IdentityHashMap<>();
   private ScheduledFuture<?> pendingSave;
   private boolean initialized;

   private FeatureManager() {
   }

   public void initialize() {
      if (!this.initialized) {
         List.of(
               new SprintFeature(),
               new AutoJumpFeature(),
               new AirStuckFeature(),
               new SpeedFeature(),
               new ElytraMotionFeature(),
               new FlyFeature(),
               new NoFallFeature(),
               new NoWebFeature(),
               new StrafeFeature(),
               new HighJumpFeature(),
               new GrimFlyFeature(),
               new InventoryMoveFeature(),
               new NoPushFeature(),
               new NoSlowFeature(),
               new ElytraBoosterFeature(),
               new SafeWalkFeature(),
               new BlockOutlineFeature(),
               new WallClimbFeature(),
               new WaterSpeedFeature(),
               new ParkourFeature(),
               new TimerFeature(),
               new GrimCollideFeature(),
               new GrimElytraFeature(),
               new ElytraTargetFeature(),
               new AuraFeature(),
               new AntiBotFeature(),
               new CriticalsFeature(),
               new TriggerBotFeature(),
                               new CrystalAuraFeature(),
                new AnchorAuraFeature(),
                new AutoTotemFeature(),               new AutoClickerFeature(),
               new AutoMaceFeature(),
               new HitBoxesFeature(),
               new VelocityFeature(),
               new BowAimbotFeature(),
               new AutoExplosionFeature(),
               new HitSoundFeature(),
               new ReachFeature(),
               new ShiftTapFeature(),
               new AimAssistantFeature(),
               new LungeBoostFeature(),
               new HudFeature(),
               new ArrowsFeature(),
               new EntityEspFeature(),
               new NameTagsFeature(),
               new HitParticlesFeature(),
               new JumpCirclesFeature(),
               new KillEffectFeature(),
               new TrajectoriesFeature(),
               new WorldParticlesFeature(),
               new PopChamsFeature(),
               new SeeInvisibleFeature(),
               new RemovalsFeature(),
                new SwingAnimationFeature(),
               new CrosshairFeature(),

               new ViewModelFeature(),
               new ShaderHandsFeature(),
               new BlockEspFeature(),
               new TargetESPFeature(),
               new ChamsFeature(),
               new HoldMyItemsFeature(),
               new ItemPhysicsFeature(),
               new WorldTweaksFeature(),
               new AtmoDawnFogFeature(),
               new NameProtectFeature(),
               new NoDelaysFeature(),
               new DeathCoordsFeature(),
               new XCarryFeature(),
               new AuctionHelperFeature(),
               new ServerHelperFeature(),
               new UseTrackerFeature(),
               new AutoTpaAcceptFeature(),
               new AutoDuelFeature(),
               new ElytraHelperFeature(),
               new ChestStealerFeature(),
               new AutoSwapFeature(),
               new ItemScrollerFeature(),
               new AutoRespawnFeature(),
               new FarmHelperFeature(),
               new DiscordRPCFeature(),
               new FullBrightFeature(),
               new FreeCamFeature(),
               new FreeLookFeature(),
               new NoJumpBoostFeature(),
               new AutoToolFeature(),
               new ClickPearlFeature(),
               new FastBreakFeature(),
               new MultiActionFeature(),
               new WindJumpFeature(),
               PveManagerFeature.INSTANCE,
               new AutoFishFeature(),
               new AutoArmorFeature(),
               new AutoPotionFeature(),
               new AutoUseFeature(),
               new AutoGappleFeature(),
               new AntiAfkFeature(),
               new AutoLeaveFeature(),
               new AutoAuthFeature(),
               new MineHelperFeature(),
               new AutoTpLootFeature(),
               new AppleFarmerFeature(),
               new AutoCrafterFeature(),
               new AuctionRelistFeature(),
               new ClanInvestFeature(),
               new GriefJoinerFeature(),
               new AutoPottBotFeature(),
                new PanicFeature()
            )
            .forEach(this::register);
         this.configStore.load(this);
         this.initialized = true;
         Runtime.getRuntime().addShutdownHook(new Thread(this::flushPendingSave, "AttackDLC Config Flush"));
      }
   }

   public void save() {
      if (this.initialized) {
         JsonObject snapshot = this.configStore.snapshot(this);
         synchronized (this.saveLock) {
            if (this.pendingSave != null) {
               this.pendingSave.cancel(false);
            }

            this.pendingSave = this.configWriter.schedule(() -> this.configStore.save(snapshot), 2L, TimeUnit.SECONDS);
         }
      }
   }

   private void flushPendingSave() {
      synchronized (this.saveLock) {
         if (this.pendingSave == null) {
            return;
         }

         this.pendingSave.cancel(false);
         this.pendingSave = null;
      }

      this.configStore.save(this.configStore.snapshot(this));
   }

   public boolean saveConfigAs(String name) {
      return this.initialized && this.configStore.saveNamed(name, this.configStore.snapshot(this));
   }

   public boolean loadConfig(String name) {
      if (this.initialized && this.configStore.loadNamed(this, name)) {
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public boolean deleteConfig(String name) {
      return this.configStore.deleteNamed(name);
   }

   public List<String> configNames() {
      return this.configStore.listNamed();
   }

   public void register(Feature feature) {
      String key = this.normalize(feature.getName());
      if (this.features.containsKey(key)) {
         throw new IllegalArgumentException("Duplicate feature name: " + feature.getName());
      }

      feature.setStateListener(this::save);
      this.features.put(key, feature);
      this.featuresByType.put((Class<? extends Feature>)feature.getClass(), feature);
   }

   public Collection<Feature> getFeatures() {
      return Collections.unmodifiableCollection(this.features.values());
   }

   public List<Feature> getFeatures(FeatureCategory category) {
      List<Feature> result = new ArrayList<>();

      for (Feature feature : this.features.values()) {
         if (feature.getCategory() == category) {
            result.add(feature);
         }
      }

      return result;
   }

   public Feature getFeature(String name) {
      return this.features.get(this.normalize(name));
   }

   public <T extends Feature> T getFeature(Class<T> type) {
      return type.cast(this.featuresByType.get(type));
   }

   public <T extends Feature> T getEnabled(Class<T> type) {
      T feature = this.getFeature(type);
      return feature != null && feature.isEnabled() ? feature : null;
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.gui.screen() == null || mc.gui.screen() instanceof AbstractContainerScreen) {
         this.dispatchBind(BindSetting.key(event.getKey()), event.getAction());
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.gui.screen() == null || mc.gui.screen() instanceof AbstractContainerScreen) {
         this.dispatchBind(BindSetting.mouse(event.getButton()), event.getAction());
      }
   }

   @EventTarget
   public void onShutdown(ShutdownEvent event) {
      synchronized (this.saveLock) {
         if (this.pendingSave != null) {
            this.pendingSave.cancel(false);
            this.pendingSave = null;
         }
      }

      this.configStore.save(this.configStore.snapshot(this));
      this.configWriter.shutdown();
      this.initialized = false;
   }

   private String normalize(String value) {
      return value.toLowerCase(Locale.ROOT);
   }

   private void dispatchBind(int bindCode, int action) {
      if (action == 1 || action == 0) {
         for (Feature feature : this.features.values()) {
            if (feature.supportsBinds() && feature.isToggleable() && feature.getBind().matchesCode(bindCode)) {
               this.handleBindInput(feature, bindCode, action);
            }
         }
      }
   }

   private void handleBindInput(Feature feature, int bindCode, int action) {
      if (feature.getBindModeForCode(bindCode) == BindMode.HOLD) {
         Set<Integer> activeCodes = this.activeHoldBinds.computeIfAbsent(feature, ignored -> new LinkedHashSet<>());
         if (action == 1) {
            if (activeCodes.add(bindCode)) {
               feature.setEnabled(true);
            }
         } else {
            activeCodes.remove(bindCode);
            if (activeCodes.isEmpty()) {
               this.activeHoldBinds.remove(feature);
               feature.setEnabled(false);
            }
         }
      } else {
         if (action == 1) {
            feature.toggle();
         }
      }
   }
}