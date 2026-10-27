package org.attackdlc.feature.impl.misc;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.game.GameTickEvent;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.utils.text.ChatUtil;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class DeathCoordsFeature extends Feature {
   private boolean reported;

   public DeathCoordsFeature() {
      super("DeathCoords", "Shows your coordinates on death", FeatureCategory.MISC, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      LocalPlayer player = event.getClient().player;
      if (player == null) {
         this.reported = false;
      } else if (!player.isDeadOrDying()) {
         this.reported = false;
      } else if (!this.reported) {
         BlockPos pos = player.getOnPos();
         ChatUtil.print("Death at x: " + pos.getX() + " y: " + pos.getY() + " z: " + pos.getZ());
         this.reported = true;
      }
   }
}

