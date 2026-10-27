package org.attackdlc.menu.accounts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.attackdlc.context.MinecraftContext;
import org.attackdlc.context.RenderContext;
import org.attackdlc.event.EventManager;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.input.CharacterInputEvent;
import org.attackdlc.event.events.render.FinalGuiRenderEvent;
import org.attackdlc.event.events.screen.ScreenKeyEvent;
import org.attackdlc.event.events.screen.ScreenMouseButtonEvent;
import org.attackdlc.menu.core.MenuOverlayState;
import org.attackdlc.menu.core.MenuPage;
import org.attackdlc.menu.pages.accounts.AccountPage;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.Textures;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.TextAlign;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

/**
 * Отдельный экран менеджера аккаунтов.
 *
 * <p>Вынесен из клик-гуи: та же минималистичная тёмная панель, но без сайдбара
 * и лишних разделов. Открывается из главного меню и по клику на профиль.
 */
public final class AccountsScreen extends Screen implements MinecraftContext {
   private static final float DESIGN_WIDTH = 1024.0F;
   private static final float DESIGN_HEIGHT = 640.0F;
   private static final float PANEL_RADIUS = 12.0F;
   private static final float CLOSE_LEFT = 900.0F;
   private static final float CLOSE_RIGHT = 1000.0F;
   private static final float CLOSE_TOP = 12.0F;
   private static final float CLOSE_BOTTOM = 50.0F;
   private final Screen parent;
   private final MenuOverlayState state = new MenuOverlayState();
   private final AccountsHost host = new AccountsHost();
   private final AccountPage page = new AccountPage();
   private float mouseX = -1.0F;
   private float mouseY = -1.0F;

   public AccountsScreen(Screen parent) {
      super(Component.literal("Аккаунты"));
      this.parent = parent;
      this.page.onCloseRequested(this::closeScreen);
      this.state.open(false);
      this.state.openPage(MenuPage.ACCOUNT_SWITCHER);
   }

   @Override
   protected void init() {
      EventManager.subscribe(this);
   }

   @Override
   public void onClose() {
      EventManager.unsubscribe(this);
      mc.gui.setScreen(this.parent);
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   private void closeScreen() {
      if (mc.gui.screen() == this) {
         this.onClose();
      }
   }

   @EventTarget(priority = -1000)
   public void onFinalGuiRender(FinalGuiRenderEvent event) {
      if (mc.gui.screen() == this && event.isRenderScreen()) {
         this.mouseX = event.getMouseX();
         this.mouseY = event.getMouseY();
         RenderContext.enter2D(event.getGui(), event.getGuiGraphicsExtractor(), event.getDeltaTracker());
         Render2DUtil.beginFrame();

         try {
            this.draw(event.getGuiGraphicsExtractor());
         } finally {
            Render2DUtil.flush();
            RenderContext.exit2D();
         }
      }
   }

   private void draw(GuiGraphicsExtractor guiGraphicsExtractor) {
      this.layoutHost();
      int accent = Theme.getAccent();
      float screenWidth = this.width;
      float screenHeight = this.height;
      float panelX = this.host.x();
      float panelY = this.host.y();
      float panelWidth = this.host.width();
      float panelHeight = this.host.height();
      Render2DUtil.rect(0.0F, 0.0F, screenWidth, screenHeight).color(ColorUtil.rgba(8, 9, 12, 232)).draw();
      Render2DUtil.rect(panelX, panelY + this.host.px(6.0F), panelWidth, panelHeight)
         .color(0)
         .radius(this.host.px(PANEL_RADIUS))
         .shadow(Theme.Colors.PANEL_SHADOW, this.host.px(18.0F))
         .draw();
      Render2DUtil.rect(panelX, panelY, panelWidth, panelHeight)
         .color(Theme.Colors.PANEL)
         .radius(this.host.px(PANEL_RADIUS))
         .draw();
      Render2DUtil.rect(panelX, panelY, panelWidth, panelHeight)
         .color(0)
         .radius(this.host.px(PANEL_RADIUS))
         .border(Math.max(0.5F, this.host.px(0.75F)), Theme.Colors.PANEL_BORDER)
         .draw();
      Render2DUtil.texture(this.host.sx(28.0F), this.host.sy(21.0F), this.host.px(19.0F), this.host.px(19.0F), Textures.Icons.SWORDS_FINE)
         .color(accent)
         .draw();
      Render2DUtil.text(
            this.host.sx(55.0F),
            UiFonts.sfProDisplay().centeredTextY(this.host.sy(31.0F), this.host.px(11.5F)),
            this.host.px(11.5F),
            "AttackDLC"
         )
         .style(UiFontStyle.SEMIBOLD)
         .color(Theme.Colors.TEXT)
         .draw();
      Render2DUtil.text(
            this.host.sx(132.0F),
            UiFonts.sfProDisplay().centeredTextY(this.host.sy(31.0F), this.host.px(8.0F)),
            this.host.px(8.0F),
            "MINECRAFT 26.2  ·  FABRIC"
         )
         .style(UiFontStyle.MEDIUM)
         .color(Theme.Colors.SECTION)
         .draw();
      boolean closeHovered = this.isCloseAt(this.mouseX, this.mouseY);
      Render2DUtil.text(
            this.host.sx(996.0F),
            UiFonts.sfProDisplay().centeredTextY(this.host.sy(31.0F), this.host.px(9.5F)),
            this.host.px(9.5F),
            "Закрыть"
         )
         .style(UiFontStyle.MEDIUM)
         .align(TextAlign.RIGHT)
         .color(closeHovered ? Theme.Colors.TEXT : Theme.Colors.SECONDARY_DARK)
         .draw();
      Render2DUtil.rect(this.host.sx(24.0F), this.host.sy(62.0F), this.host.px(976.0F), Math.max(1.0F, this.host.px(0.75F)))
         .color(Theme.Colors.DIVIDER_SUBTLE)
         .draw();
      this.page.layout(this.host, this.state, (int)this.mouseX, (int)this.mouseY);
      this.page.render(mc, guiGraphicsExtractor);
   }

   private void layoutHost() {
      float scale = Math.min((this.width - 64.0F) / DESIGN_WIDTH, (this.height - 64.0F) / DESIGN_HEIGHT);
      scale = Math.max(0.42F, Math.min(1.0F, scale));
      float panelWidth = DESIGN_WIDTH * scale;
      float panelHeight = DESIGN_HEIGHT * scale;
      this.host.configureFrame((this.width - panelWidth) / 2.0F, (this.height - panelHeight) / 2.0F, panelWidth, panelHeight);
   }

   private boolean isCloseAt(float mouseX, float mouseY) {
      float x = this.host.designX(mouseX);
      float y = this.host.designY(mouseY);
      return x >= CLOSE_LEFT && x <= CLOSE_RIGHT && y >= CLOSE_TOP && y <= CLOSE_BOTTOM;
   }

   @EventTarget(priority = 2000)
   public void onScreenClick(ScreenMouseButtonEvent event) {
      if (event.getScreen() == this) {
         if (event.getAction() == ScreenMouseButtonEvent.Action.CLICK) {
            MouseButtonEvent click = event.getMouseButtonEvent();
            this.handleClick((float)click.x(), (float)click.y());
         }

         event.cancel();
      }
   }

   private void handleClick(float mouseX, float mouseY) {
      float designX = this.host.designX(mouseX);
      float designY = this.host.designY(mouseY);
      if (designX < 0.0F || designY < 0.0F || designX > DESIGN_WIDTH || designY > DESIGN_HEIGHT) {
         this.closeScreen();
         return;
      }

      if (this.isCloseAt(mouseX, mouseY)) {
         this.closeScreen();
         return;
      }

      this.page.handleClick((int)mouseX, (int)mouseY);
   }

   @EventTarget(priority = 2000)
   public void onScreenKey(ScreenKeyEvent event) {
      if (event.getScreen() == this) {
         KeyEvent key = event.getKeyEvent();
         if (key != null && event.getAction() == ScreenKeyEvent.Action.PRESS) {
            if (key.key() == 256) {
               if (!this.page.handleKey(256)) {
                  this.closeScreen();
               }
            } else {
               this.page.handleKey(key.key());
            }
         }

         event.cancel();
      }
   }

   @EventTarget(priority = 2000)
   public void onCharacterInput(CharacterInputEvent event) {
      if (mc.gui.screen() == this) {
         this.page.handleCharacter(event.getCodePoint());
         event.cancel();
      }
   }

   public void handleScroll(double vertical) {
      this.page.handleScroll((int)this.mouseX, (int)this.mouseY, vertical);
   }

   private static final class AccountsHost extends org.attackdlc.menu.ui.Component {
      @Override
      public void render(Minecraft minecraft, GuiGraphicsExtractor guiGraphicsExtractor) {
      }
   }
}
