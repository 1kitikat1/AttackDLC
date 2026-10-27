package org.attackdlc.feature.impl.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.game.GameTickEvent;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.feature.setting.BooleanSetting;
import org.attackdlc.mixin.accessor.MultiPlayerGameModeAccessor;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class FastBreakFeature extends Feature {
   public final BooleanSetting onlyWhileMining = this.register(new BooleanSetting("Only While Mining", true));

   public FastBreakFeature() {
      super("FastBreak", "Removes block breaking delay", FeatureCategory.PLAYER, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      MultiPlayerGameMode gameMode = event.getClient().gameMode;
      if (gameMode != null && gameMode instanceof MultiPlayerGameModeAccessor accessor) {
         if (!this.onlyWhileMining.getValue() || Minecraft.getInstance().options.keyAttack.isDown()) {
            accessor.setDestroyDelay(0);
         }
      }
   }
}

