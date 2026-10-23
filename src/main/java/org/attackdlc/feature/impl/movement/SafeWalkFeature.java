package org.attackdlc.feature.impl.movement;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.attackdlc.context.MinecraftContext;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.input.PlayerInputEvent;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class SafeWalkFeature extends Feature implements MinecraftContext {
   public SafeWalkFeature() {
      super("SafeWalk", "Prevents walking off block edges", FeatureCategory.MOVEMENT, -1);
   }

   @EventTarget
   public void onInput(PlayerInputEvent event) {
      LocalPlayer player = this.player();
      if (player != null && this.level() != null && player.onGround()) {
         BlockPos below = player.blockPosition().below();
         if (this.level().getBlockState(below).getCollisionShape(this.level(), below).isEmpty()) {
            event.setShift(true);
         }
      }
   }
}

