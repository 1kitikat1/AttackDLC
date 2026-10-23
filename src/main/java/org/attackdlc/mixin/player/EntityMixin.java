package org.attackdlc.mixin.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.attackdlc.event.EventManager;
import org.attackdlc.event.events.input.PlayerVelocityStrafeEvent;
import org.attackdlc.feature.impl.combat.HitBoxesFeature;
import org.attackdlc.feature.impl.movement.NoPushFeature;
import org.attackdlc.utils.combat.aura.AngleConnection;

@Mixin(Entity.class)
public abstract class EntityMixin {
   @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void cancelEntityPush(Entity entity, CallbackInfo ci) {
      if (NoPushFeature.shouldCancelEntityPush((Entity)(Object)this, entity)) {
         ci.cancel();
      }
   }

   @Inject(method = "getPickRadius", at = @At("RETURN"), cancellable = true)
   private void expandPickRadius(CallbackInfoReturnable<Float> cir) {
      HitBoxesFeature hitBoxes = HitBoxesFeature.getEnabled();
      if (hitBoxes != null && hitBoxes.appliesTo((Entity)(Object)this)) {
         cir.setReturnValue((Float)cir.getReturnValue() + (float)hitBoxes.getHorizontalExpansion());
      }
   }

   @Redirect(
      method = "moveRelative",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"
      ),
      require = 0
   )
   private Vec3 attackdlc$fixMoveRelative(Vec3 movementInput, float speed, float yaw) {
      if (this.attackdlc$isLocalPlayer()) {
         PlayerVelocityStrafeEvent event = EventManager.call(
            new PlayerVelocityStrafeEvent(movementInput, speed, yaw, attackdlc$getInputVector(movementInput, speed, yaw))
         );
         return event.getVelocity();
      } else {
         return attackdlc$getInputVector(movementInput, speed, yaw);
      }
   }

   @ModifyVariable(method = "calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private float attackdlc$packetPitch(float pitch) {
      return this.attackdlc$isLocalPlayer() && AngleConnection.INSTANCE.shouldApplyPacketRotation() ? AngleConnection.INSTANCE.getPacketPitch() : pitch;
   }

   @ModifyVariable(method = "calculateViewVector(FF)Lnet/minecraft/world/phys/Vec3;", at = @At("HEAD"), ordinal = 1, argsOnly = true)
   private float attackdlc$packetYaw(float yaw) {
      return this.attackdlc$isLocalPlayer() && AngleConnection.INSTANCE.shouldApplyPacketRotation() ? AngleConnection.INSTANCE.getPacketYaw() : yaw;
   }

   @Unique
   private boolean attackdlc$isLocalPlayer() {
      LocalPlayer player = Minecraft.getInstance().player;
      return player != null && (Object)this == player;
   }

   @Unique
   private static Vec3 attackdlc$getInputVector(Vec3 movementInput, float speed, float yaw) {
      double length = movementInput.lengthSqr();
      if (length < 1.0E-7) {
         return Vec3.ZERO;
      }

      Vec3 scaled = (length > 1.0 ? movementInput.normalize() : movementInput).scale(speed);
      float sin = Mth.sin(yaw * (float) (Math.PI / 180.0));
      float cos = Mth.cos(yaw * (float) (Math.PI / 180.0));
      return new Vec3(scaled.x * cos - scaled.z * sin, scaled.y, scaled.z * cos + scaled.x * sin);
   }
}

