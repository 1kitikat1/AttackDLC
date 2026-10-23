package org.attackdlc.mixin.core;

import net.minecraft.client.GameLoadCookie;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.attackdlc.context.RotationContext;
import org.attackdlc.event.EventManager;
import org.attackdlc.event.Events;
import org.attackdlc.event.events.game.AttackEvent;
import org.attackdlc.event.events.game.GameTickEvent;
import org.attackdlc.event.events.game.TickContext;
import org.attackdlc.event.events.lifecycle.ClientStartEvent;
import org.attackdlc.event.events.lifecycle.DisconnectEvent;
import org.attackdlc.event.events.lifecycle.ResourceReloadEvent;
import org.attackdlc.event.events.lifecycle.ShutdownEvent;
import org.attackdlc.event.events.lifecycle.WorldJoinEvent;
import org.attackdlc.event.events.lifecycle.WorldLeaveEvent;
import org.attackdlc.event.events.screen.ScreenOpenEvent;
import org.attackdlc.feature.impl.movement.ElytraBoosterFeature;
import org.attackdlc.feature.impl.movement.SpeedFeature;
import org.attackdlc.feature.impl.movement.TimerFeature;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
   private static final TickContext TICK_CONTEXT = new TickContext();
   @Unique
   private ClientLevel previousLevel;
   @Unique
   private boolean attackdlc$runningTimerTick;
   @Shadow
   public ClientLevel level;

   @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
   private void onClientStart(GameLoadCookie gameLoadCookie, CallbackInfo ci) {
      if (EventManager.hasListeners(ClientStartEvent.class)) {
         EventManager.call(Events.CLIENT_START.set((Minecraft)(Object)this));
      }
   }

   @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
   private void onTick(CallbackInfo ci) {
      /*
       * Нет лицензии — игра с клиентом недоступна: показываем окно
       * и не даём закрыть его, пока доступ не появится.
       */
      Minecraft client = (Minecraft)(Object)this;
      if (org.attackdlc.license.LicenseManager.INSTANCE.isChecked()
            && !org.attackdlc.license.LicenseManager.INSTANCE.isLicensed()
            && !(client.gui.screen() instanceof org.attackdlc.license.LicenseGateScreen)) {
         client.gui.setScreen(new org.attackdlc.license.LicenseGateScreen(client.gui.screen()));
      }

      if (this.attackdlc$runningTimerTick || !TimerFeature.shouldSkipBaseTick() && !SpeedFeature.shouldSkipBaseTick() && !ElytraBoosterFeature.shouldSkipBaseTick()
         )
       {
         if (EventManager.hasListeners(GameTickEvent.class)) {
            Minecraft client2 = (Minecraft)(Object)this;
            EventManager.call(Events.GAME_TICK.set(client2, TICK_CONTEXT.begin(client2)));
         }
      } else {
         ci.cancel();
      }
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void runTimerTick(CallbackInfo ci) {
      if (!this.attackdlc$runningTimerTick && (TimerFeature.consumeExtraTick() || SpeedFeature.consumeExtraTick() || ElytraBoosterFeature.consumeExtraTick())) {
         this.attackdlc$runningTimerTick = true;

         try {
            ((Minecraft)(Object)this).tick();
         } finally {
            this.attackdlc$runningTimerTick = false;
         }
      }
   }

   @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
   private void onAttack(CallbackInfoReturnable<Boolean> cir) {
      if (EventManager.hasListeners(AttackEvent.class)) {
         if (EventManager.call(Events.ATTACK.set((Minecraft)(Object)this)).isCancelled()) {
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(method = "setLevel", at = @At("HEAD"))
   private void capturePreviousLevel(ClientLevel level, CallbackInfo ci) {
      this.previousLevel = this.level;
   }

   @Inject(method = "setLevel", at = @At("TAIL"))
   private void onSetLevel(ClientLevel level, CallbackInfo ci) {
      Minecraft client = (Minecraft)(Object)this;
      ClientLevel previous = this.previousLevel;
      if (level == null) {
         RotationContext.clear();
      }

      if (previous == null && level != null && EventManager.hasListeners(WorldJoinEvent.class)) {
         EventManager.call(Events.WORLD_JOIN.set(client, level));
      }

      if (previous != null && level == null && EventManager.hasListeners(WorldLeaveEvent.class)) {
         EventManager.call(Events.WORLD_LEAVE.set(client, previous));
      }
   }

   @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"))
   private void onDisconnect(Screen screen, boolean transferring, boolean resetting, CallbackInfo ci) {
      if (EventManager.hasListeners(DisconnectEvent.class)) {
         EventManager.call(Events.DISCONNECT.set((Minecraft)(Object)this, screen, transferring, resetting));
      }
   }

   @Inject(method = "setScreenAndShow", at = @At("HEAD"), cancellable = true)
   private void onSetScreen(Screen screen, CallbackInfo ci) {
      Minecraft client = (Minecraft)(Object)this;
      if (screen != null && EventManager.hasListeners(ScreenOpenEvent.class)) {
         if (EventManager.call(Events.SCREEN_OPEN.set(client, screen)).isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "stop", at = @At("HEAD"))
   private void onShutdown(CallbackInfo ci) {
      if (EventManager.hasListeners(ShutdownEvent.class)) {
         EventManager.call(Events.SHUTDOWN.set((Minecraft)(Object)this));
      }
   }

   @Inject(method = "onResourceLoadFinished", at = @At("TAIL"))
   private void onResourceReload(GameLoadCookie gameLoadCookie, CallbackInfo ci) {
      if (EventManager.hasListeners(ResourceReloadEvent.class)) {
         EventManager.call(Events.RESOURCE_RELOAD.set((Minecraft)(Object)this));
      }
   }
}

