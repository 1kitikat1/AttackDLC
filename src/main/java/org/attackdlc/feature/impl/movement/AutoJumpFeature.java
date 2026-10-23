package org.attackdlc.feature.impl.movement;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.game.GameTickEvent;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.feature.FeatureManager;
import org.attackdlc.feature.impl.combat.AuraFeature;
import org.attackdlc.feature.setting.BooleanSetting;
import org.attackdlc.pve.AutomationResource;
import org.attackdlc.pve.PveAutomationCoordinator;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class AutoJumpFeature extends Feature {
   public final BooleanSetting aura = this.register(new BooleanSetting("Aura", true));
   public final BooleanSetting negativeEffects = this.register(new BooleanSetting("Negative Effects", true));

   public AutoJumpFeature() {
      super("AutoJump", "Automatically jumps from ground", FeatureCategory.MOVEMENT, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      LocalPlayer player = event.getClient().player;
      if (player != null && event.getClient().level != null) {
         if (!PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.MOVEMENT)
            && !PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.NAVIGATION)) {
            if (player.onGround() && !event.getClient().options.keyJump.isDown()) {
               if (!player.getAbilities().flying && !player.isFallFlying()) {
                  if (!player.isInWater() && !player.isInLava() && !player.onClimbable()) {
                     if (this.shouldJumpForAura(player) || this.shouldJumpForNegativeEffects(player)) {
                        player.jumpFromGround();
                     }
                  }
               }
            }
         }
      }
   }

   private boolean shouldJumpForAura(LocalPlayer player) {
      if (!this.aura.getValue()) {
         return false;
      }

      AuraFeature aura = FeatureManager.INSTANCE.getFeature(AuraFeature.class);
      return aura != null && aura.shouldAutoJump(player);
   }

   private boolean shouldJumpForNegativeEffects(LocalPlayer player) {
      return !this.negativeEffects.getValue() ? false : player.input.getMoveVector().lengthSquared() > 0.0F && this.hasNegativeEffects(player);
   }

   private boolean hasNegativeEffects(LocalPlayer player) {
      for (MobEffectInstance effect : player.getActiveEffects()) {
         if (((MobEffect)effect.getEffect().value()).getCategory() == MobEffectCategory.HARMFUL) {
            return true;
         }
      }

      return false;
   }
}

