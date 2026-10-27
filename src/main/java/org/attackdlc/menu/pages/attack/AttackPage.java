package org.attackdlc.menu.pages.attack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;
import org.attackdlc.license.LicenseManager;
import org.attackdlc.menu.core.MenuPage;
import org.attackdlc.menu.ui.PageComponent;
import org.attackdlc.menu.ui.controls.ButtonComponent;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.UiFontStyle;

/**
 * Страница «Аккаунт Attack»: ник, роль, подписка и привязка к ПК.
 *
 * <p>Данные берутся из лаунчера (файл входа) и проверяются на сервере:
 * если доступа нет — показываем причину и кнопку покупки ключа.</p>
 */
public final class AttackPage extends PageComponent {

   private static final float CARD_X = 32.0F;
   private static final float CARD_Y = 168.0F;
   private static final float CARD_W = 452.0F;
   private static final float CARD_H = 236.0F;
   private static final float CARD2_X = 508.0F;
   private static final float CARD2_W = 484.0F;
   private static final float ROW_Y = 208.0F;
   private static final float ROW_STEP = 30.0F;
   private static final float LABEL_X = 56.0F;
   private static final float VALUE_X = 196.0F;
   private static final float PAD = 24.0F;
   private static final float BUTTON_Y = 340.0F;
   private static final float BUTTON_W = 200.0F;
   private static final float BUTTON_H = 32.0F;

   private final ButtonComponent buyButton = new ButtonComponent("Buy key", this::openShop);
   private final ButtonComponent refreshButton = new ButtonComponent("Refresh access", this::refresh);
   private MenuPage displayedPage = MenuPage.NONE;
   private String nick = "—";
   private String role = "—";
   private String key = "—";
   private String hwid = "—";
   private String term = "—";
   private String reason = "";

   @Override
   protected void onLayout() {
      this.displayedPage = this.state.displayPage();
      this.sync();

      this.buyButton.place(this, Math.round(CARD2_X + PAD), Math.round(BUTTON_Y), Math.round(BUTTON_W), Math.round(BUTTON_H), this.mouseX, this.mouseY)
         .alpha(this.progress);
      this.refreshButton.place(this, Math.round(CARD2_X + PAD + BUTTON_W + 12.0F), Math.round(BUTTON_Y), Math.round(BUTTON_W), Math.round(BUTTON_H), this.mouseX, this.mouseY)
         .alpha(this.progress);
   }

   /** Забирает актуальные данные из менеджера лицензии. */
   private void sync() {
      String name = ru.xd.Session.getName();
      this.nick = name == null || name.isBlank() ? "—" : name;
      String accountRole = ru.xd.Session.getRole();
      this.role = accountRole == null || accountRole.isBlank() ? "—" : accountRole;
      this.key = ru.xd.Session.getActivatedKey();
      this.hwid = org.attackdlc.license.Hwid.shortForm();
      this.reason = LicenseManager.INSTANCE.status();

      int left = LicenseManager.INSTANCE.daysLeft();
      if (!LicenseManager.INSTANCE.isLicensed()) {
         this.term = "нет доступа";
      } else if ("owner".equalsIgnoreCase(this.role)) {
         this.term = "владелец · навсегда";
      } else if (left < 0) {
         this.term = "навсегда";
      } else {
         this.term = "осталось дней: " + left;
      }
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contentContains(mouseX, mouseY)) {
         return false;
      }

      if (this.buyButton.handleClick(mouseX, mouseY)) {
         return true;
      }

      if (this.refreshButton.handleClick(mouseX, mouseY)) {
         return true;
      }

      return true;
   }

   public void handleScroll(int mouseX, int mouseY, double vertical) {
   }

   public boolean handleKey(int key) {
      return false;
   }

   public boolean handleCharacter(int codePoint) {
      return false;
   }

   @Override
   public void render(Minecraft minecraft, GuiGraphicsExtractor guiGraphicsExtractor) {
      if (this.displayedPage != MenuPage.ATTACK || this.progress <= 0.001F) {
         return;
      }

      this.pageHeader("Attack DLC", "Account and subscription");
      // карточка с данными аккаунта
      this.rect(CARD_X, CARD_Y, CARD_W, CARD_H, Theme.Colors.PANEL, 12.0F, this.progress);
      this.outline(CARD_X, CARD_Y, CARD_W, CARD_H, Theme.Colors.PANEL_BORDER, 12.0F, 0.75F, this.progress);

      this.text(LABEL_X, ROW_Y, 11.0F, "Ник аккаунта", Theme.Colors.SECONDARY, this.progress, UiFontStyle.MEDIUM);
      this.text(VALUE_X, ROW_Y, 12.0F, this.nick, Theme.Colors.PRIMARY, this.progress, UiFontStyle.SEMIBOLD);
      this.row(ROW_Y + ROW_STEP, "Роль", this.role);
      this.row(ROW_Y + ROW_STEP * 2.0F, "Подписка", this.term);
      this.row(ROW_Y + ROW_STEP * 3.0F, "Ключ", this.key.isBlank() ? "—" : this.key);
      this.row(ROW_Y + ROW_STEP * 4.0F, "Привязка ПК", this.hwid);

      // карточка со статусом доступа
      this.rect(CARD2_X, CARD_Y, CARD2_W, CARD_H, Theme.Colors.PANEL, 12.0F, this.progress);
      this.outline(CARD2_X, CARD_Y, CARD2_W, CARD_H, Theme.Colors.PANEL_BORDER, 12.0F, 0.75F, this.progress);

      boolean active = LicenseManager.INSTANCE.isLicensed();
      int statusColor = active ? Theme.getAccent() : Theme.Colors.SYSTEM_RED;
      this.text(CARD2_X + PAD, ROW_Y, 18.0F,
            active ? "Доступ активен" : "Доступ закрыт",
            statusColor, this.progress, UiFontStyle.SEMIBOLD);
      this.text(CARD2_X + PAD, ROW_Y + 30.0F, 12.0F,
            this.reason.isBlank() ? "—" : this.reason,
            Theme.Colors.SECONDARY, this.progress, UiFontStyle.REGULAR);
      this.text(CARD2_X + PAD, ROW_Y + 54.0F, 12.0F,
            active
               ? "Клиент работает без ограничений."
               : "Купите ключ в лаунчере и активируйте его — доступ привяжется к этому ПК.",
            Theme.Colors.SECONDARY, this.progress, UiFontStyle.REGULAR);

      // откуда взялись данные входа: какой аккаунт и как давно записан файл
      long age = ru.xd.Session.getAgeMinutes();
      String source = age < 0
            ? "Данные входа: файл лаунчера не найден"
            : "Данные входа: " + this.nick + " · файл лаунчера " + age + " мин назад";
      this.text(CARD2_X + PAD, ROW_Y + 78.0F, 11.0F, source,
            Theme.Colors.SECONDARY_DARK, this.progress, UiFontStyle.REGULAR);

      this.buyButton.render(minecraft, guiGraphicsExtractor);
      this.refreshButton.render(minecraft, guiGraphicsExtractor);
   }

   /** Строка «подпись — значение». */
   private void row(float y, String label, String value) {
      this.text(LABEL_X, y, 11.0F, label, Theme.Colors.SECONDARY, this.progress, UiFontStyle.MEDIUM);
      this.text(VALUE_X, y, 12.0F, value == null || value.isBlank() ? "—" : value,
            Theme.Colors.PRIMARY, this.progress, UiFontStyle.SEMIBOLD);
   }

   /** Открывает страницу покупки (ссылка из лаунчера). */
   private void openShop() {
      String url = ru.xd.Session.getFunpayUrl();
      if (url == null || url.isBlank()) {
         url = ru.xd.Session.getSiteUrl();
      }
      if (url == null || url.isBlank()) {
         return;
      }
      try {
         Util.getPlatform().openUri(url);
      } catch (Throwable ignored) {
      }
   }

   /** Повторно проверяет доступ (после активации ключа в лаунчере). */
   private void refresh() {
      Thread thread = new Thread(() -> LicenseManager.INSTANCE.check(), "attack-license-refresh");
      thread.setDaemon(true);
      thread.start();
   }
}
