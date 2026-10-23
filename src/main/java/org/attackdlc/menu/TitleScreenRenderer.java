package org.attackdlc.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.attackdlc.context.RenderContext;
import org.attackdlc.menu.accounts.AccountsScreen;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.Textures;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.MsdfFont;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

import java.time.LocalTime;

/**
 * Главное меню AttackDLC.
 *
 * <p>Спокойный «редакционный» минимализм: размытая панорама, один акцентный
 * цвет, крупная типографика и много воздуха. Никаких частиц, сеток и свечений.
 */
public final class TitleScreenRenderer {
    private static final String[] LABELS = {
            "Одиночная игра", "Сетевая игра", "Аккаунты", "Настройки", "Выход"
    };
    private static final String[] HINTS = {
            "локальные миры", "серверы", "менеджер аккаунтов", "параметры игры", "закрыть клиент"
    };

    // Геометрия
    private static final float ROW_H = 42f;
    private static final float ROW_GAP = 4f;
    private static final float ROW_W = 470f;
    private static final float ROW_RADIUS = 7f;
    private static final float NAV_ANIM = 0.18f;
    private static final float ENTER_OFFSET = 12f;

    // Палитра
    private static final int TEXT_PRIMARY = 0xFFF2F3F5;
    private static final int TEXT_SECONDARY = 0xFFA6ACB6;
    private static final int TEXT_MUTED = 0xFF787E88;
    private static final int HAIRLINE = 0x1AFFFFFF;

    private static float openProgress = 0f;
    private static long lastFrameTime = System.nanoTime();
    private static final float[] ROW_ANIM = new float[LABELS.length];
    private static final float[] ROW_RECT = new float[LABELS.length * 4];
    private static String toast = null;
    private static long toastUntil = 0L;

    private TitleScreenRenderer() {
    }

    public static void resetOpenAnimation() {
        openProgress = 0f;
        java.util.Arrays.fill(ROW_ANIM, 0f);
    }

    public static void render(GuiGraphicsExtractor ctx, int w, int h, double mx, double my) {
        if (org.attackdlc.feature.impl.misc.PanicFeature.isPanicked()) return;
        long now = System.nanoTime();
        float dt = Math.min((now - lastFrameTime) / 1_000_000_000f, 0.05f);
        lastFrameTime = now;

        openProgress = Math.min(1f, openProgress + dt * 3.2f);
        float eased = 1f - (1f - openProgress) * (1f - openProgress);
        float offsetY = (1f - eased) * ENTER_OFFSET;

        RenderContext.enter2D(null, ctx, null);
        try {
            Render2DUtil.beginFrame();

            drawBackdrop(ctx, w, h);
            drawBrand(ctx, w, h, offsetY);
            drawNavigation(ctx, w, h, (float) mx, (float) my - offsetY, offsetY, dt);
            drawFooter(ctx, w, h);
            drawToast(ctx, w, h);

            Render2DUtil.flush();
        } finally {
            RenderContext.exit2D();
        }
    }

    // ---------------------------------------------------------------- фон

    private static void drawBackdrop(GuiGraphicsExtractor ctx, int w, int h) {
        // Панорама без размытия: мягкое затемнение + градиент для читаемости
        Render2DUtil.rect(0, 0, w, h)
                .color(ColorUtil.rgba(8, 9, 12, 118))
                .draw();

        Render2DUtil.rect(0, 0, w, h)
                .color(ColorUtil.rgba(8, 9, 12, 186), ColorUtil.rgba(8, 9, 12, 24),
                        ColorUtil.rgba(8, 9, 12, 64), ColorUtil.rgba(8, 9, 12, 192))
                .draw();

        // Тонкая виньетка снизу
        Render2DUtil.rect(0, h * 0.55f, w, h * 0.45f)
                .color(0x00000000, 0x00000000, ColorUtil.rgba(5, 6, 8, 120), ColorUtil.rgba(5, 6, 8, 120))
                .draw();
    }

    // ------------------------------------------------------------ заголовок

    private static void drawBrand(GuiGraphicsExtractor ctx, int w, int h, float offsetY) {
        float margin = marginX(w);
        MsdfFont font = UiFonts.sfPro(700);

        float brandSize = 13f;
        float brandY = 44f + offsetY * 0.4f;
        Render2DUtil.text(margin, brandY, brandSize, "ATTACKDLC")
                .font(font)
                .style(UiFontStyle.MEDIUM)
                .color(TEXT_PRIMARY)
                .draw();

        Render2DUtil.text(margin, brandY + brandSize + 8f, 7f, "MINECRAFT 26.2  ·  FABRIC")
                .font(UiFonts.sfPro(500))
                .style(UiFontStyle.MEDIUM)
                .color(TEXT_MUTED)
                .draw();

        // Версия справа сверху
        MsdfFont small = UiFonts.sfPro(500);
        String version = "v1.0";
        float versionW = small.measureWidth(version, 8f, 8f * UiFontStyle.REGULAR.letterSpacingEm());
        Render2DUtil.text(w - margin - versionW, brandY + 2f, 8f, version)
                .font(small)
                .style(UiFontStyle.REGULAR)
                .color(TEXT_MUTED)
                .draw();

        // Имя аккаунта с короной (если вошёл) — справа, перед версией
        try {
            String nick = ru.xd.Session.getName();
            if (nick != null && !nick.isBlank()) {
                String role = ru.xd.Session.getRole();
                float nickW = font.measureWidth(nick, brandSize, brandSize * UiFontStyle.MEDIUM.letterSpacingEm());
                float nickX = w - margin - nickW - versionW - 8f;
                if (nickX > margin + 10f) {
                    Render2DUtil.text(nickX, brandY, brandSize, nick)
                            .font(font)
                            .style(UiFontStyle.MEDIUM)
                            .color(TEXT_PRIMARY)
                            .draw();
                    // Корона-иконка
                    try {
                        boolean isOwner = "owner".equalsIgnoreCase(role);
                        float crownSize = brandSize * 0.85f;
                        Render2DUtil.texture(nickX + nickW + 3f, brandY + (brandSize - crownSize) / 2f, crownSize, crownSize,
                                Textures.Icons.CROWN)
                                .color(isOwner ? Theme.getAccent() : 0xFFA6ACB6)
                                .draw();
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
    }

    // ------------------------------------------------------------ навигация

    private static void drawNavigation(GuiGraphicsExtractor ctx, int w, int h,
                                       float mx, float my, float offsetY, float dt) {
        float margin = marginX(w);
        float blockH = LABELS.length * ROW_H + (LABELS.length - 1) * ROW_GAP;
        float startY = (h - blockH) / 2f + h * 0.03f;

        // Подпись над меню
        float captionSize = 8f;
        Render2DUtil.text(margin, startY - 30f, captionSize, getGreeting())
                .font(UiFonts.sfPro(500))
                .style(UiFontStyle.REGULAR)
                .color(TEXT_SECONDARY)
                .draw();
        Render2DUtil.rect(margin, startY - 12f, ROW_W, 1f).color(HAIRLINE).draw();

        MsdfFont labelFont = UiFonts.sfPro(600);
        MsdfFont indexFont = UiFonts.sfPro(500);
        int accent = Theme.getAccent();

        for (int i = 0; i < LABELS.length; i++) {
            float rowY = startY + i * (ROW_H + ROW_GAP) + offsetY;
            float rowX = margin;

            boolean hovered = mx >= rowX && mx <= rowX + ROW_W && my >= rowY && my <= rowY + ROW_H;
            ROW_ANIM[i] = approach(ROW_ANIM[i], hovered ? 1f : 0f, Math.min(1f, dt / NAV_ANIM));

            float k = ROW_ANIM[i];

            if (k > 0.01f) {
                Render2DUtil.rect(rowX, rowY, ROW_W, ROW_H)
                        .color(ColorUtil.withAlpha(0xFFFFFF, (int) (10f * k)))
                        .radius(ROW_RADIUS)
                        .draw();
                Render2DUtil.rect(rowX, rowY + ROW_H / 2f - 9f, 2f, 18f)
                        .color(ColorUtil.withAlpha(accent, (int) (235f * k)))
                        .radius(1f)
                        .draw();
            }

            // Индекс
            String index = String.format("%02d", i + 1);
            float indexSize = 7.5f;
            Render2DUtil.text(rowX + 18f, rowY + ROW_H / 2f - indexSize / 2f + 0.5f, indexSize, index)
                    .font(indexFont)
                    .style(UiFontStyle.REGULAR)
                    .color(lerpColor(TEXT_MUTED, TEXT_SECONDARY, k))
                    .draw();

            // Название
            float labelSize = 13f;
            Render2DUtil.text(rowX + 50f, labelFont.centeredTextY(rowY + ROW_H / 2f, labelSize), labelSize, LABELS[i])
                    .font(labelFont)
                    .style(UiFontStyle.MEDIUM)
                    .color(lerpColor(0xFFD7D9DE, TEXT_PRIMARY, k))
                    .draw();

            // Короткое описание справа
            float hintSize = 8.5f;
            Render2DUtil.text(rowX + 230f, labelFont.centeredTextY(rowY + ROW_H / 2f, hintSize), hintSize, HINTS[i])
                    .font(UiFonts.sfPro(500))
                    .style(UiFontStyle.REGULAR)
                    .color(lerpColor(TEXT_MUTED, TEXT_SECONDARY, k))
                    .draw();

            // Стрелка при наведении
            if (k > 0.02f) {
                String arrow = "→";
                float arrowSize = 12f;
                float arrowW = labelFont.measureWidth(arrow, arrowSize, arrowSize * UiFontStyle.MEDIUM.letterSpacingEm());
                Render2DUtil.text(rowX + ROW_W - 18f - arrowW + k * 4f,
                                labelFont.centeredTextY(rowY + ROW_H / 2f, arrowSize), arrowSize, arrow)
                        .font(labelFont)
                        .style(UiFontStyle.MEDIUM)
                        .color(ColorUtil.withAlpha(TEXT_PRIMARY, (int) (215f * k)))
                        .draw();
            }

            int ri = i * 4;
            ROW_RECT[ri] = rowX;
            ROW_RECT[ri + 1] = rowY;
            ROW_RECT[ri + 2] = ROW_W;
            ROW_RECT[ri + 3] = ROW_H;
        }
    }

    // --------------------------------------------------------------- футер

    private static void drawFooter(GuiGraphicsExtractor ctx, int w, int h) {
        float margin = marginX(w);
        float lineY = h - 52f;
        Render2DUtil.rect(margin, lineY, w - margin * 2f, 1f)
                .color(HAIRLINE)
                .draw();

        MsdfFont font = UiFonts.sfPro(500);
        float size = 8f;

        Render2DUtil.text(margin, lineY + 16f, size, "RIGHT SHIFT  —  МЕНЮ В ИГРЕ")
                .font(font)
                .style(UiFontStyle.REGULAR)
                .color(TEXT_SECONDARY)
                .draw();

        String metrics = Minecraft.getInstance().getFps() + " FPS";
        float metricsW = font.measureWidth(metrics, size, size * UiFontStyle.REGULAR.letterSpacingEm());
        Render2DUtil.text(w - margin - metricsW, lineY + 16f, size, metrics)
                .font(font)
                .style(UiFontStyle.REGULAR)
                .color(TEXT_SECONDARY)
                .draw();
    }

    private static void drawToast(GuiGraphicsExtractor ctx, int w, int h) {
        if (toast == null || System.currentTimeMillis() > toastUntil) {
            return;
        }
        MsdfFont font = UiFonts.sfPro(500);
        float size = 8.5f;
        float textW = font.measureWidth(toast, size, size * UiFontStyle.REGULAR.letterSpacingEm());
        float boxW = textW + 26f;
        float x = (w - boxW) / 2f;
        float y = h - 86f;

        Render2DUtil.rect(x, y, boxW, 24f)
                .color(ColorUtil.rgba(14, 15, 19, 235))
                .radius(6f)
                .border(1f, HAIRLINE)
                .draw();
        Render2DUtil.text(x + 13f, y + 8f, size, toast)
                .font(font)
                .style(UiFontStyle.REGULAR)
                .color(TEXT_SECONDARY)
                .draw();
    }

    // ----------------------------------------------------------------- ввод

    /** @return true — клик поглощён (ванильные кнопки скрыты). */
    public static boolean handleClick(double mx, double my) {
        for (int i = 0; i < LABELS.length; i++) {
            int ri = i * 4;
            if (mx >= ROW_RECT[ri] && mx <= ROW_RECT[ri] + ROW_RECT[ri + 2]
                    && my >= ROW_RECT[ri + 1] && my <= ROW_RECT[ri + 1] + ROW_RECT[ri + 3]) {
                triggerAction(i);
                return true;
            }
        }
        return true;
    }

    private static void triggerAction(int index) {
        Minecraft mc = Minecraft.getInstance();
        Screen current = mc.gui.screen();
        Screen parent = current instanceof TitleScreen ? current : null;

        switch (index) {
            case 0 -> mc.gui.setScreen(new SelectWorldScreen(parent));
            case 1 -> mc.gui.setScreen(new JoinMultiplayerScreen(parent));
            case 2 -> mc.gui.setScreen(new AccountsScreen(parent));
            case 3 -> mc.gui.setScreen(new OptionsScreen(parent, mc.options, false));
            case 4 -> mc.stop();
            default -> {
            }
        }
    }

    private static void showToast(String message) {
        toast = message;
        toastUntil = System.currentTimeMillis() + 2800L;
    }

    // -------------------------------------------------------------- утилиты

    private static float marginX(int w) {
        return Math.max(28f, Math.round(w * 0.055f));
    }

    private static String getGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour >= 4 && hour < 12) return "Доброе утро";
        if (hour >= 12 && hour < 18) return "Добрый день";
        if (hour >= 18 && hour < 23) return "Добрый вечер";
        return "Доброй ночи";
    }

    private static float approach(float value, float target, float speed) {
        return value + (target - value) * Math.min(1f, speed);
    }

    private static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((int) (aa + (ba - aa) * t) << 24)
                | ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8)
                | (int) (ab + (bb - ab) * t);
    }
}
