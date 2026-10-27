package org.attackdlc.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.attackdlc.context.RenderContext;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.render.FinalGuiRenderEvent;
import org.attackdlc.feature.impl.visual.HudFeature;
import org.attackdlc.menu.core.MenuOverlay;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.MsdfFont;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

/**
 * Оверлей главного меню AttackDLC.
 * <p>Когда меню открыто — стандартные панели. Когда закрыто —
 * минималистичная тёмная заставка с неоновым логотипом, пульсирующими
 * кольцами и плавными линиями по краям.
 */
public final class MenuOverlayRenderHandler {
    @EventTarget
    public void onFinalGuiRender(FinalGuiRenderEvent event) {
        Minecraft minecraft = event.getClient();
        RenderContext.enter2D(event.getGui(), event.getGuiGraphicsExtractor(), event.getDeltaTracker());

        try {
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
            Render2DUtil.beginFrame();
            Render2DUtil.setBackdropBlurScale(HudFeature.glassBlurScale());

            // Титульный экран рисует себя сам (TitleScreenMixin), а в игре оверлей
            // должен показываться только когда он действительно открыт.
            if (MenuOverlay.isOpen()) {
                MenuOverlay.render(minecraft, event.getGuiGraphicsExtractor(), screenWidth, screenHeight);
            }

            Render2DUtil.flush();
        } finally {
            RenderContext.exit2D();
        }
    }

    /** Минималистичная заставка: глубокий чёрный фон, неоновый логотип, кольца, тонкие линии. */
    private static void drawMinimalMenu(Minecraft mc, GuiGraphicsExtractor context, int screenWidth, int screenHeight) {
        float time = (float)(System.currentTimeMillis() % 3600000L) / 1000.0F;
        float cx = screenWidth / 2f;
        float cy = screenHeight * 0.42f;
        int accent = Theme.getAccent();

        // Глубокий тёмный оверлей
        Render2DUtil.rect(0, 0, screenWidth, screenHeight)
            .color(ColorUtil.rgba(4, 5, 9, 250))
            .draw();

        // Тонкая сетка-фонарь (горизонтальные/вертикальные линии)
        float gridAlpha = 12;
        float gridSize = 40f;
        float offsetY = (time * 20f) % gridSize;
        for (float y = -gridSize + offsetY; y < screenHeight + gridSize; y += gridSize) {
            Render2DUtil.rect(0, y, screenWidth, 0.5f)
                .color(ColorUtil.rgba(80, 90, 120, gridAlpha))
                .draw();
        }
        for (float x = 0; x < screenWidth + gridSize; x += gridSize) {
            Render2DUtil.rect(x, 0, 0.5f, screenHeight)
                .color(ColorUtil.rgba(80, 90, 120, gridAlpha))
                .draw();
        }

        // Пульсирующие неоновые кольца за логотипом
        float ringCount = 3f;
        for (int i = 0; i < 3; i++) {
            float phase = (time * 0.8f + i * 0.33f) % 1f;
            float radius = 60f + phase * 80f;
            float alpha = (float)(255 * (1f - phase) * (1f - phase) * 0.35f);
            if (alpha > 2) {
                Render2DUtil.rect(cx - radius, cy - radius, radius * 2f, radius * 2f)
                    .color(ColorUtil.withAlpha(accent, (int) alpha))
                    .radius(radius)
                    .draw();
            }
        }

        // Мягкое свечение за центром
        Render2DUtil.rect(cx - 100f, cy - 100f, 200f, 200f)
            .color(ColorUtil.withAlpha(accent, 18))
            .radius(100f)
            .draw();

        // Логотип "AttackDLC" — крупно, с акцентным свечением
        MsdfFont logo = UiFonts.googleSans(700);
        float logoSize = 34f;
        float logoW = logo.measureWidth("AttackDLC", logoSize, logoSize * 0.05f);
        float logoY = cy - logoSize / 2f;

        // Свечение под текстом (3 слоя)
        for (int layer = 3; layer >= 1; layer--) {
            float glowSize = logoSize * (1f + layer * 0.08f);
            float glowAlpha = 20 / layer;
            float glowOffset = layer * 1.5f;
            Render2DUtil.text(cx - logoW / 2f, logoY + glowOffset, glowSize, "AttackDLC")
                .font(logo)
                .style(UiFontStyle.SEMIBOLD)
                .color(ColorUtil.withAlpha(accent, (int) glowAlpha))
                .draw();
        }

        // Сам текст
        Render2DUtil.text(cx - logoW / 2f, logoY, logoSize, "AttackDLC")
            .font(logo)
            .style(UiFontStyle.SEMIBOLD)
            .color(ColorUtil.withAlpha(-1, 250))
            .draw();

        // Акцентная линия под логотипом
        float lineW = 180f;
        Render2DUtil.rect(cx - lineW / 2f, logoY + logoSize + 16f, lineW, 2f)
            .color(ColorUtil.withAlpha(accent, 140))
            .draw();

        // Маленькие точки по бокам линии
        Render2DUtil.rect(cx - lineW / 2f - 6f, logoY + logoSize + 15f, 4f, 4f)
            .color(ColorUtil.withAlpha(accent, 100))
            .radius(2f)
            .draw();
        Render2DUtil.rect(cx + lineW / 2f + 2f, logoY + logoSize + 15f, 4f, 4f)
            .color(ColorUtil.withAlpha(accent, 100))
            .radius(2f)
            .draw();

        // Подзаголовок
        MsdfFont subFont = UiFonts.googleSans(500);
        String subText = "Client for Minecraft 26.2";
        float subSize = 10f;
        float subW = subFont.measureWidth(subText, subSize, 0.02f);
        Render2DUtil.text(cx - subW / 2f, logoY + logoSize + 28f, subSize, subText)
            .font(subFont)
            .style(UiFontStyle.MEDIUM)
            .color(ColorUtil.rgb(140, 144, 160))
            .draw();

        // Статус снизу: версия + подсказка
        String hint = "Right Shift — open menu";
        MsdfFont hintFont = UiFonts.googleSans(400);
        float hintSize = 9f;
        float hintW = hintFont.measureWidth(hint, hintSize, 0.04f);
        Render2DUtil.text(cx - hintW / 2f, screenHeight - 40f, hintSize, hint)
            .font(hintFont)
            .style(UiFontStyle.REGULAR)
            .color(ColorUtil.rgb(80, 84, 96))
            .draw();

        // Версия в правом углу
        String version = "v1.0";
        float verSize = 8f;
        float verW = hintFont.measureWidth(version, verSize, 0.04f);
        Render2DUtil.text(screenWidth - verW - 14f, 14f, verSize, version)
            .font(hintFont)
            .style(UiFontStyle.REGULAR)
            .color(ColorUtil.rgb(60, 64, 74))
            .draw();

        // Тонкая рамка по периметру экрана
        Render2DUtil.rect(2f, 2f, screenWidth - 4f, screenHeight - 4f)
            .color(ColorUtil.rgba(80, 90, 120, 28))
            .radius(0f)
            .draw();
    }
}
