package org.attackdlc.feature.impl.misc;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.game.GameTickEvent;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.feature.setting.NumberSetting;
import org.attackdlc.pve.AutomationResource;
import org.attackdlc.pve.PveAutomationCoordinator;
import org.attackdlc.utils.inventory.ContainerLootService;
import org.attackdlc.utils.inventory.InventoryUtil;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class ChestStealerFeature extends Feature {
   public final NumberSetting delay = this.register(new NumberSetting("Delay", 80.0, 0.0, 1000.0, 10.0, "ms"));
   private long lastMoveAt;

   public ChestStealerFeature() {
      super("ChestStealer", "Automatically takes items from containers", FeatureCategory.MISC, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (!PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.INVENTORY) && InventoryUtil.isContainerScreenOpen()) {
         AbstractContainerMenu menu = InventoryUtil.getOpenMenu();
         if (menu != null && menu != event.getClient().player.inventoryMenu) {
            long now = System.currentTimeMillis();
            if (now - this.lastMoveAt >= this.delay.getValue().longValue()) {
               if (ContainerLootService.quickMoveFirst(menu, stack -> !stack.isEmpty())) {
                  this.lastMoveAt = now;
               }
            }
         }
      }
   }
}

