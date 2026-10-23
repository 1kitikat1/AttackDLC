package org.attackdlc.mixin.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.attackdlc.context.RotationContext;
import org.attackdlc.event.EventManager;
import org.attackdlc.event.events.input.MouseInputEvent;
import org.attackdlc.feature.impl.player.AutoBuyScreen;
import org.attackdlc.feature.impl.player.FreeLookFeature;
import org.attackdlc.menu.accounts.AccountsScreen;
import org.attackdlc.menu.core.MenuOverlay;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
   @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
   private void onMouseButton(long window, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
      if (EventManager.hasListeners(MouseInputEvent.class)) {
         MouseInputEvent event = EventManager.call(new MouseInputEvent(window, buttonInfo.button(), action, buttonInfo.modifiers()));
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
   private void blockWorldScrollWhileMenuOpen(long window, double horizontal, double vertical, CallbackInfo ci) {
      if (Minecraft.getInstance().gui.screen() instanceof AutoBuyScreen) {
         AutoBuyScreen.handleScroll(vertical);
         ci.cancel();
      } else if (Minecraft.getInstance().gui.screen() instanceof AccountsScreen accounts) {
         accounts.handleScroll(vertical);
         ci.cancel();
      } else {
         if (MenuOverlay.blocksInput()) {
            MenuOverlay.handleScroll(vertical);
            ci.cancel();
         }
      }
   }

   @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
   private void onTurnPlayer(LocalPlayer player, double yawDelta, double pitchDelta) {
      if (!FreeLookFeature.onMouseTurn(yawDelta, pitchDelta)) {
         if (!RotationContext.onMouseTurn(yawDelta, pitchDelta)) {
            player.turn(yawDelta, pitchDelta);
         }
      }
   }
}

