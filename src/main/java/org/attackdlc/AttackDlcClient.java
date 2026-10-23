package org.attackdlc;

import net.fabricmc.api.ModInitializer;
import org.attackdlc.command.CommandManager;
import org.attackdlc.event.EventManager;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.game.GameTickEvent;
import org.attackdlc.event.events.lifecycle.ClientStartEvent;
import org.attackdlc.event.events.lifecycle.WorldLeaveEvent;
import org.attackdlc.feature.FeatureManager;
import org.attackdlc.hud.NotificationsElement;
import org.attackdlc.license.LicenseManager;
import org.attackdlc.menu.MenuKeyHandler;
import org.attackdlc.menu.MenuOverlayRenderHandler;
import org.attackdlc.menu.i18n.UiLanguage;
import org.attackdlc.menu.pages.configs.ConfigPreviewManager;
import org.attackdlc.pve.PveAutomationCoordinator;
import org.attackdlc.pve.PvpStateTracker;
import org.attackdlc.pve.economy.AhSellFlow;
import org.attackdlc.utils.AccountSwitcher;
import org.attackdlc.utils.FriendManager;
import org.attackdlc.utils.ScreenNbtParser;
import org.attackdlc.utils.StaffManager;
import org.attackdlc.utils.blockesp.BlockEspConfig;
import org.attackdlc.utils.combat.ServerSprintTracker;
import org.attackdlc.utils.combat.ServerTickSync;
import org.attackdlc.utils.combat.aura.AngleConnection;
import org.attackdlc.utils.inventory.DropAllInventoryController;
import org.attackdlc.utils.inventory.InventoryFlowManager;
import org.attackdlc.utils.inventory.InventorySwap;
import org.attackdlc.utils.render.EntityEspStateCache;
import org.attackdlc.utils.render.Textures;
import org.attackdlc.utils.render.gui.GuiTexture;
import org.attackdlc.utils.render.world.WorldEffects;

public class AttackDlcClient implements ModInitializer {
   private final MenuKeyHandler menuKeyHandler = new MenuKeyHandler();
   private final MenuOverlayRenderHandler menuOverlayRenderHandler = new MenuOverlayRenderHandler();

   public void onInitialize() {
      UiLanguage.loadSaved();
      // проверка лицензии: без неё фичи выключаются
      org.attackdlc.license.LicenseManager.INSTANCE.setStatusListener(() ->
            NotificationsElement.notify(Textures.Icons.USER_ROUND,
                  org.attackdlc.utils.render.Theme.getAccent(),
                  "Attack DLC", org.attackdlc.license.LicenseManager.INSTANCE.status()));
      org.attackdlc.license.LicenseManager.INSTANCE.start();
      ru.xd.Session.getInstance().reload();
      FriendManager.INSTANCE.initialize();      StaffManager.INSTANCE.initialize();
      BlockEspConfig.INSTANCE.initialize();
      FeatureManager.INSTANCE.initialize();
      CommandManager.INSTANCE.initialize();
      WorldEffects.bootstrap();
      EventManager.subscribe(FeatureManager.INSTANCE);
      EventManager.subscribe(CommandManager.INSTANCE);
      EventManager.subscribe(FriendManager.INSTANCE);
      EventManager.subscribe(StaffManager.INSTANCE);
      EventManager.subscribe(new NotificationsElement.ToggleListener());
      EventManager.subscribe(this.menuKeyHandler);
      EventManager.subscribe(this.menuOverlayRenderHandler);
      EventManager.subscribe(InventorySwap.INSTANCE);
      EventManager.subscribe(DropAllInventoryController.INSTANCE);
      EventManager.subscribe(ServerTickSync.INSTANCE);
      EventManager.subscribe(ServerSprintTracker.INSTANCE);
      EventManager.subscribe(AngleConnection.INSTANCE);
      EventManager.subscribe(PveAutomationCoordinator.INSTANCE);
      EventManager.subscribe(PvpStateTracker.INSTANCE);
      EventManager.subscribe(AhSellFlow.INSTANCE);
      EventManager.subscribe(ScreenNbtParser.INSTANCE);
      EventManager.subscribe(ConfigPreviewManager.INSTANCE);
      EventManager.subscribe(this);
   }

   @EventTarget
   public void onClientStart(ClientStartEvent event) {
      AccountSwitcher.applyStartupAccount();
      GuiTexture.prewarm(Textures.all());
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      InventoryFlowManager.update();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      EntityEspStateCache.clear();
   }
}
