package org.attackdlc.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.attackdlc.context.RotationContext;
import org.attackdlc.event.EventManager;
import org.attackdlc.event.Events;
import org.attackdlc.event.events.game.PlayerTickEvent;
import org.attackdlc.feature.impl.movement.NoPushFeature;
import org.attackdlc.feature.impl.movement.NoSlowFeature;
import org.attackdlc.utils.combat.LocalPlayerHistory;
import org.attackdlc.utils.combat.aura.AngleConnection;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Shadow
   private float yRotLast;
   @Shadow
   private float xRotLast;
   @Unique
   private double attackdlc$prevX;
   @Unique
   private double attackdlc$prevZ;
   @Unique
   private float attackdlc$prevBodyYaw;

   @Inject(method = "tick", at = @At("HEAD"))
   private void onTickPre(CallbackInfo ci) {
      if (EventManager.hasListeners(PlayerTickEvent.class)) {
         EventManager.call(Events.PLAYER_TICK.set((LocalPlayer)(Object)this, PlayerTickEvent.Phase.PRE));
      }
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void onTickPost(CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      LocalPlayerHistory.record(player);
      if (EventManager.hasListeners(PlayerTickEvent.class)) {
         EventManager.call(Events.PLAYER_TICK.set(player, PlayerTickEvent.Phase.POST));
      }
   }

   @Inject(method = "moveTowardsClosestSpace(DD)V", at = @At("HEAD"), cancellable = true)
   private void cancelClosestSpacePush(double x, double z, CallbackInfo ci) {
      if (NoPushFeature.shouldCancelClosestSpacePush((LocalPlayer)(Object)this)) {
         ci.cancel();
      }
   }

   @Redirect(method = "applyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
   private float useCameraPitchForHandBob(LocalPlayer player) {
      return this.minecraft.options.getCameraType().isFirstPerson() ? this.minecraft.gameRenderer.mainCamera().xRot() : player.getXRot();
   }

   @Redirect(method = "applyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
   private float useCameraYawForHandBob(LocalPlayer player) {
      return this.minecraft.options.getCameraType().isFirstPerson() ? this.minecraft.gameRenderer.mainCamera().yRot() : player.getYRot();
   }

   @ModifyExpressionValue(
      method = {"sendPosition", "tick"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"),
      require = 0
   )
   private float attackdlc$packetYaw(float original) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      AngleConnection controller = AngleConnection.INSTANCE;
      if (!controller.shouldApplyPacketRotation()) {
         this.attackdlc$syncBodyYawCache(player, original);
         return original;
      } else {
         float yaw = controller.getPacketYaw();
         float bodyYaw = this.attackdlc$calculateBodyYaw(
            yaw, this.attackdlc$prevBodyYaw, this.attackdlc$prevX, this.attackdlc$prevZ, player.getX(), player.getZ(), player.getAttackAnim(1.0F)
         );
         this.attackdlc$prevBodyYaw = bodyYaw;
         this.attackdlc$prevX = player.getX();
         this.attackdlc$prevZ = player.getZ();
         player.setYBodyRot(bodyYaw);
         return yaw;
      }
   }

   @ModifyExpressionValue(
      method = {"sendPosition", "tick"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"),
      require = 0
   )
   private float attackdlc$packetPitch(float original) {
      AngleConnection controller = AngleConnection.INSTANCE;
      return controller.shouldApplyPacketRotation() ? controller.getPacketPitch() : original;
   }

   @Inject(method = "sendPosition", at = @At("TAIL"))
   private void attackdlc$ensureSilentRotationPacket(CallbackInfo ci) {
      AngleConnection controller = AngleConnection.INSTANCE;
      if (controller.shouldApplyPacketRotation()) {
         LocalPlayer player = (LocalPlayer)(Object)this;
         float yaw = controller.getPacketYaw();
         float pitch = controller.getPacketPitch();
         boolean rotationChanged = Math.abs(yaw - this.yRotLast) > 0.001F || Math.abs(pitch - this.xRotLast) > 0.001F;
         if (rotationChanged) {
            player.connection.send(new Rot(yaw, pitch, player.onGround(), player.horizontalCollision));
            this.yRotLast = yaw;
            this.xRotLast = pitch;
         }
      }
   }

   @Redirect(
      method = "modifyInput",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec2;scale(F)Lnet/minecraft/world/phys/Vec2;", ordinal = 1)
   )
   private Vec2 attackdlc$cancelItemSlowdown(Vec2 vec, float multiplier) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      return NoSlowFeature.shouldCancelSlowdown(player) && player.isUsingItem() && !player.isPassenger() ? vec.scale(1.0F) : vec.scale(multiplier);
   }

   @Unique
   private void attackdlc$syncBodyYawCache(LocalPlayer player, float yaw) {
      this.attackdlc$prevBodyYaw = yaw;
      this.attackdlc$prevX = player.getX();
      this.attackdlc$prevZ = player.getZ();
   }

   @Unique
   private float attackdlc$calculateBodyYaw(float yaw, float prevBodyYaw, double prevX, double prevZ, double currentX, double currentZ, float handSwingProgress) {
      double motionX = currentX - prevX;
      double motionZ = currentZ - prevZ;
      float motionSquared = (float)(motionX * motionX + motionZ * motionZ);
      float bodyYaw = prevBodyYaw;
      if (motionSquared > 0.0025000002F) {
         float movementYaw = (float)Mth.atan2(motionZ, motionX) * (180.0F / (float)Math.PI) - 90.0F;
         float yawDiff = Mth.abs(Mth.wrapDegrees(yaw) - movementYaw);
         if (95.0F < yawDiff && yawDiff < 265.0F) {
            bodyYaw = movementYaw - 180.0F;
         } else {
            bodyYaw = movementYaw;
         }
      }

      if (handSwingProgress - 0.2F > 0.0F) {
         bodyYaw = yaw;
      }

      float deltaYaw = Mth.wrapDegrees(bodyYaw - prevBodyYaw);
      bodyYaw = prevBodyYaw + deltaYaw * 0.3F;
      float yawOffsetDiff = Mth.wrapDegrees(yaw - bodyYaw);
      float maxHeadRotation = 52.0F;
      if (Math.abs(yawOffsetDiff) > maxHeadRotation) {
         bodyYaw += yawOffsetDiff - Mth.sign(yawOffsetDiff) * maxHeadRotation;
      }

      return bodyYaw;
   }

   @Inject(method = "getViewYRot", at = @At("HEAD"), cancellable = true)
   private void onGetViewYRot(float partialTick, CallbackInfoReturnable<Float> cir) {
      if (RotationContext.isActive()) {
         cir.setReturnValue(RotationContext.getFreeYaw());
      }
   }

   @Inject(method = "getViewXRot", at = @At("HEAD"), cancellable = true)
   private void onGetViewXRot(float partialTick, CallbackInfoReturnable<Float> cir) {
      if (RotationContext.isActive()) {
         cir.setReturnValue(RotationContext.getFreePitch());
      }
   }
}

