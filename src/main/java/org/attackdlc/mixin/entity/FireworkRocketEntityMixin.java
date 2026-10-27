package org.attackdlc.mixin.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.attackdlc.event.EventManager;
import org.attackdlc.event.events.game.FireworkEvent;
import org.attackdlc.mixin.accessor.FireworkRocketEntityAccessor;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {
   @Shadow
   private LivingEntity attachedToEntity;
   @Unique
   private static final Minecraft mc = Minecraft.getInstance();

   @ModifyConstant(method = "tick", constant = @Constant(doubleValue = 1.5))
   private double attackdlc$replaceSpeed(double original) {
      FireworkEvent event = new FireworkEvent(this.attachedToEntity, (float)original);
      EventManager.call(event);
      return event.getSpeed();
   }

   @Inject(method = "tick", at = @At("HEAD"))
   private void attackdlc$onTick(CallbackInfo ci) {
      if (mc.player != null && mc.level != null && mc.player.isFallFlying()) {
         if (this.attachedToEntity != mc.player) {
            if (!this.attackdlc$hasActiveOwnFirework()) {
               FireworkRocketEntity self = (FireworkRocketEntity)(Object)this;
               Vec3 fireworkPos = self.position();
               Vec3 playerPos = mc.player.position();
               double distance = fireworkPos.distanceTo(playerPos);
               if (distance < 5.0) {
                  Vec3 fireworkVel = self.getDeltaMovement();
                  if (fireworkVel.lengthSqr() < 0.01) {
                     return;
                  }

                  Vec3 toPlayer = playerPos.subtract(fireworkPos).normalize();
                  double dot = fireworkVel.normalize().dot(toPlayer);
                  if (dot > 0.4) {
                     Vec3 fireworkDir = fireworkVel.normalize();
                     Vec3 right = new Vec3(fireworkDir.x, 0.0, -fireworkDir.z).normalize();
                     double dotRight = toPlayer.dot(right);
                     if (dotRight < 0.0) {
                        right = right.scale(-1.0);
                     }

                     double urgency = 1.0 - distance / 5.0;
                     double strength = 0.25 * urgency;
                     double upward = 0.1 * urgency;
                     Vec3 currentVel = mc.player.getDeltaMovement();
                     Vec3 dodge = right.scale(strength).add(0.0, upward, 0.0);
                     mc.player.setDeltaMovement(currentVel.add(dodge));
                  }
               }
            }
         }
      }
   }

   @Unique
   private boolean attackdlc$hasActiveOwnFirework() {
      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof FireworkRocketEntity firework) {
            FireworkRocketEntityAccessor accessor = (FireworkRocketEntityAccessor)firework;
            if (accessor.getAttachedToEntity() == mc.player) {
               return true;
            }
         }
      }

      return false;
   }
}

