package org.attackdlc.license;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.attackdlc.context.RenderContext;
import org.attackdlc.event.EventManager;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.render.FinalGuiRenderEvent;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.MsdfFont;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.TextAlign;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

/**
 * Окно-заглушка: игра с клиентом недоступна.
 *
 * <p>Показывается, если аккаунт не авторизован в лаунчере: ключ не
 * активирован, срок истёк или аккаунт привязан к другому компьютеру.
 * Экран не закрывается по Esc — играть с клиентом без подписки нельзя.</p>
 *
 * <p>Отрисовка идёт через 2D-рендер клиента (шрифты MSDF, тема, акцент),
 * поэтому окно выглядит как остальной интерфейс, а не как ванильный экран.</p>
 */
public final class LicenseGateScreen extends Screen {

   private static final float DESIGN_W = 560.0F;
   private static final float DESIGN_H = 268.0F;
   private static final float BUTTON_W = 156.0F;
   private static final float BUTTON_H = 32.0F;
   private static final float BUTTON_GAP = 10.0F;
   private static final float PAD = 28.0F;

   private final Screen parent;
   private String reason = "";
   private float scale = 1.0F;
   private float panelX;
   private float panelY;
   private float panelW;
   private float panelH;
   private int mouseX = -1;
   private int mouseY = -1;

   public LicenseGateScreen(Screen parent) {
      super(Component.literal("Attack DLC"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      this.reason = LicenseManager.INSTANCE.status();
      EventManager.subscribe(this);
      this.layoutPanel();
   }

   /** Панель по центру экрана, размер — пропорционально разрешению. */
   private void layoutPanel() {
      this.scale = Math.max(0.75F, Math.min(this.width / 1024.0F, this.height / 640.0F));
      this.panelW = DESIGN_W * this.scale;
      this.panelH = DESIGN_H * this.scale;
      this.panelX = (this.width - this.panelW) / 2.0F;
      this.panelY = (this.height - this.panelH) / 2.0F;
   }

   @Override
   public void onClose() {
      EventManager.unsubscribe(this);
      // экран блокирующий: закрывать нельзя
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public boolean isPauseScreen() {
      return true;
   }

   @Override
   public void tick() {
      // доступ появился — выпускаем в игру
      if (LicenseManager.INSTANCE.isLicensed()) {
         this.closeGate();
      }
   }

   @EventTarget
   public void onFinalGuiRender(FinalGuiRenderEvent event) {
      Minecraft minecraft = event.getClient();
      if (minecraft.gui.screen() != this || !event.isRenderScreen()) {
         return;
      }
      this.mouseX = (int) event.getMouseX();
      this.mouseY = (int) event.getMouseY();

      RenderContext.enter2D(event.getGui(), event.getGuiGraphicsExtractor(),
            event.getDeltaTracker());
      try {
         Render2DUtil.beginFrame();
         this.draw();
         Render2DUtil.flush();
      } finally {
         RenderContext.exit2D();
      }
   }

   /** Рисует затемнение, панель и кнопки. */
   private void draw() {
      float s = this.scale;
      int accent = Theme.getAccent();
      MsdfFont font = UiFonts.sfProDisplay();

      // затемнение игрового мира
      Render2DUtil.rect(0.0F, 0.0F, this.width, this.height)
         .color(ColorUtil.rgba(5, 6, 10, 226))
         .draw();

      // панель
      Render2DUtil.rect(this.panelX, this.panelY, this.panelW, this.panelH)
         .color(Theme.Colors.PANEL)
         .radius(14.0F * s)
         .shadow(Theme.Colors.PANEL_SHADOW, 22.0F * s)
         .draw();
      Render2DUtil.rect(this.panelX, this.panelY, this.panelW, this.panelH)
         .color(0)
         .radius(14.0F * s)
         .border(Math.max(1.0F, 0.75F * s), Theme.Colors.PANEL_BORDER)
         .draw();

      float cx = this.panelX + this.panelW / 2.0F;
      float top = this.panelY + PAD * s;

      // логотип-надпись
      Render2DUtil.text(cx, top + font.centeredTextY(0.0F, 22.0F * s), 22.0F * s, "ATTACK")
         .style(UiFontStyle.SEMIBOLD)
         .align(TextAlign.CENTER)
         .color(accent)
         .draw();
      Render2DUtil.text(cx, top + 26.0F * s + font.centeredTextY(0.0F, 15.0F * s), 15.0F * s, "Игра с клиентом недоступна")
         .style(UiFontStyle.SEMIBOLD)
         .align(TextAlign.CENTER)
         .color(Theme.Colors.PRIMARY)
         .draw();
      Render2DUtil.text(cx, top + 54.0F * s + font.centeredTextY(0.0F, 12.0F * s), 12.0F * s,
            "Приобретите подписку или войдите в аккаунт Attack")
         .style(UiFontStyle.REGULAR)
         .align(TextAlign.CENTER)
         .color(Theme.Colors.SECONDARY)
         .draw();

      // причина
      String reason = this.reason.isBlank() ? "Доступ не подтверждён" : this.reason;
      Render2DUtil.text(cx, top + 76.0F * s + font.centeredTextY(0.0F, 12.0F * s), 12.0F * s, reason)
         .style(UiFontStyle.MEDIUM)
         .align(TextAlign.CENTER)
         .color(Theme.Colors.SYSTEM_RED)
         .draw();

      // кнопки
      float buttonY = this.panelY + this.panelH - PAD * s - BUTTON_H * s;
      float totalW = BUTTON_W * 3.0F * s + BUTTON_GAP * 2.0F * s;
      float buttonX = cx - totalW / 2.0F;
      this.button(buttonX, buttonY, "Купить ключ", true, 0);
      this.button(buttonX + (BUTTON_W + BUTTON_GAP) * s, buttonY, "Проверить ещё раз", false, 1);
      this.button(buttonX + (BUTTON_W + BUTTON_GAP) * 2.0F * s, buttonY, "Выйти из игры", false, 2);
   }

   /** Рисует кнопку; primary — заливка акцентом. */
   private void button(float x, float y, String label, boolean primary, int id) {
      float s = this.scale;
      boolean hovered = this.mouseX >= x && this.mouseX <= x + BUTTON_W * s
            && this.mouseY >= y && this.mouseY <= y + BUTTON_H * s;
      int background = primary
            ? (hovered ? ColorUtil.multiplyAlpha(Theme.getAccent(), 235) : Theme.getAccent())
            : (hovered ? Theme.Colors.SURFACE_HOVER : Theme.Colors.CONTROL);
      int textColor = primary ? 0xFF0B0C10 : (hovered ? Theme.Colors.PRIMARY : Theme.Colors.SECONDARY);

      Render2DUtil.rect(x, y, BUTTON_W * s, BUTTON_H * s)
         .color(background)
         .radius(6.0F * s)
         .border(Math.max(1.0F, 0.5F * s),
               primary ? ColorUtil.rgba(255, 255, 255, 40) : Theme.Colors.OUTLINES_SMALL)
         .draw();
      Render2DUtil.text(x + BUTTON_W * s / 2.0F,
            UiFonts.sfProDisplay().centeredTextY(y + BUTTON_H * s / 2.0F, 12.0F * s),
            12.0F * s, label)
         .style(UiFontStyle.MEDIUM)
         .align(TextAlign.CENTER)
         .color(textColor)
         .draw();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      float s = this.scale;
      float cx = this.panelX + this.panelW / 2.0F;
      float buttonY = this.panelY + this.panelH - PAD * s - BUTTON_H * s;
      float totalW = BUTTON_W * 3.0F * s + BUTTON_GAP * 2.0F * s;
      float buttonX = cx - totalW / 2.0F;

      for (int id = 0; id < 3; id++) {
         float x = buttonX + (BUTTON_W + BUTTON_GAP) * id * s;
         if (event.x() >= x && event.x() <= x + BUTTON_W * s
               && event.y() >= buttonY && event.y() <= buttonY + BUTTON_H * s) {
            this.press(id);
            return true;
         }
      }
      return true;
   }

   /** Действие кнопки. */
   private void press(int id) {
      switch (id) {
         case 0 -> {
            String url = ru.xd.Session.getFunpayUrl();
            if (url == null || url.isBlank()) {
               url = ru.xd.Session.getSiteUrl();
            }
            if (url != null && !url.isBlank()) {
               try {
                  Util.getPlatform().openUri(url);
               } catch (Throwable ignored) {
               }
            }
         }
         case 1 -> {
            Thread thread = new Thread(() -> LicenseManager.INSTANCE.check(),
                  "attack-license-recheck");
            thread.setDaemon(true);
            thread.start();
         }
         default -> Minecraft.getInstance().stop();
      }
   }

   /** Убирает заглушку — доступ есть. */
   private void closeGate() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }
}
