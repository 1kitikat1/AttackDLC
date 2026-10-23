package org.attackdlc.hud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.combat.ServerTickSync;
import org.attackdlc.utils.render.Textures;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.MsdfFont;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

/**
 * Watermark: одна тонкая строка — бренд, ник, FPS, пинг, TPS и время.
 * Без иконок на каждый чих и без «стеклянных» бликов.
 */
public final class WatermarkElement extends HudElement {
    private static final float HEIGHT = 28.0F;
    private static final float BRAND_SIZE = 11.5F;
    private static final float TEXT_SIZE = 10.0F;
    private static final float LABEL_SIZE = 8.5F;
    private static final float PAD = 12.0F;
    private static final float GAP = 12.0F;
    private static final float ICON_SIZE = 14.0F;
    private static final float ICON_GAP = 8.0F;
    private static final float LABEL_GAP = 4.0F;
    private static final String BRAND = "AttackDLC";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);
    private static final long SESSION_REFRESH_MS = 5000L;
    private static String cachedSessionName;
    private static long cachedSessionNameAt;

    private String userText;
    private boolean showUser;
    private String fpsText;
    private String pingText;
    private String tpsText;
    private String timeText;

    public WatermarkElement() {
        super("watermark", "Watermark");
    }

    private static String sessionName(Minecraft mc) {
        long now = System.currentTimeMillis();
        if (cachedSessionName != null && now - cachedSessionNameAt < SESSION_REFRESH_MS) {
            return cachedSessionName;
        }

        String resolved = resolveSessionName(mc);
        if (!resolved.equals(cachedSessionName)) {
            cachedSessionName = resolved;
        }

        cachedSessionNameAt = now;
        return resolved;
    }

    /**
     * Ник аккаунта из лаунчера.
     *
     * <p>Только из файла входа лаунчера: ник игрока не подставляется — в
     * клиенте можно переключить игровой аккаунт, а показывать надо того,
     * кто купил подписку.</p>
     */
    private static String resolveSessionName(Minecraft mc) {
        try {
            String name = Class.forName("ru.xd.Session").getMethod("getName").invoke(null) instanceof String s ? s : null;
            if (name != null && !name.isBlank()) {
                return name;
            }
        } catch (Throwable ignored) {
        }

        return "Не авторизован";
    }

    @Override
    protected boolean preservePositionOnContentResize() {
        return true;
    }

    @Override
    protected void layout(Minecraft mc, float unit) {
        unit = this.scaledUnit(unit);
        this.userText = sessionName(mc);
        this.showUser = !BRAND.equalsIgnoreCase(this.userText);
        this.fpsText = String.valueOf(mc.getFps());
        this.pingText = String.valueOf(latency(mc));
        this.tpsText = String.valueOf(tps());
        this.timeText = LocalTime.now().format(TIME_FORMATTER);

        MsdfFont brandFont = UiFonts.sfPro(700);
        MsdfFont font = UiFonts.sfPro(600);
        MsdfFont labelFont = UiFonts.sfPro(500);

        float brandW = brandFont.measureWidth(BRAND, BRAND_SIZE * unit, BRAND_SIZE * unit * UiFontStyle.MEDIUM.letterSpacingEm());
        float userW = font.measureWidth(this.userText, TEXT_SIZE * unit, TEXT_SIZE * unit * UiFontStyle.SEMIBOLD.letterSpacingEm());
        float fpsW = this.blockWidth(font, labelFont, this.fpsText, "fps", unit);
        float pingW = this.blockWidth(font, labelFont, this.pingText, "ms", unit);
        float tpsW = this.blockWidth(font, labelFont, this.tpsText, "tps", unit);
        float timeW = labelFont.measureWidth(this.timeText, TEXT_SIZE * unit, TEXT_SIZE * unit * UiFontStyle.REGULAR.letterSpacingEm());

        float w = PAD * unit + ICON_SIZE * unit + ICON_GAP * unit + brandW;
        if (this.showUser) {
            w += GAP * unit + 1.0F + GAP * unit + userW;
        }
        w += GAP * unit + 1.0F + GAP * unit + fpsW;
        w += GAP * unit + 1.0F + GAP * unit + pingW;
        w += GAP * unit + 1.0F + GAP * unit + tpsW;
        w += GAP * unit + 1.0F + GAP * unit + timeW;
        w += PAD * unit;
        this.width = w;
        this.height = HEIGHT * unit;
    }

    @Override
    protected void draw(Minecraft mc, float unit) {
        unit = this.scaledUnit(unit);
        float alpha = this.appearAlpha();
        float rowH = HEIGHT * unit;
        float centerY = this.y + rowH / 2.0F;
        this.hudBackground(alpha, unit, 7.0F * unit);

        MsdfFont brandFont = UiFonts.sfPro(700);
        MsdfFont font = UiFonts.sfPro(600);
        MsdfFont labelFont = UiFonts.sfPro(500);
        int accent = Theme.getAccent();

        float iconY = centerY - ICON_SIZE * unit / 2.0F;
        float ox = this.x + PAD * unit;
        Render2DUtil.texture(ox, iconY, ICON_SIZE * unit, ICON_SIZE * unit, Textures.Icons.SWORDS_FINE)
                .color(ColorUtil.multiplyAlpha(accent, alpha))
                .draw();
        ox += ICON_SIZE * unit + ICON_GAP * unit;

        float brandSize = BRAND_SIZE * unit;
        Render2DUtil.text(ox, brandFont.centeredTextY(centerY, brandSize), brandSize, BRAND)
                .font(brandFont)
                .style(UiFontStyle.MEDIUM)
                .color(ColorUtil.multiplyAlpha(-1, alpha))
                .draw();
        ox += brandFont.measureWidth(BRAND, brandSize, brandSize * UiFontStyle.MEDIUM.letterSpacingEm());

        ox = this.divider(ox, centerY, 11.0F, GAP, unit, alpha);

        float textSize = TEXT_SIZE * unit;
        if (this.showUser) {
            Render2DUtil.text(ox, font.centeredTextY(centerY, textSize), textSize, this.userText)
                    .font(font)
                    .style(UiFontStyle.SEMIBOLD)
                    .color(ColorUtil.multiplyAlpha(Theme.Colors.SECONDARY, alpha))
                    .draw();
            ox += font.measureWidth(this.userText, textSize, textSize * UiFontStyle.SEMIBOLD.letterSpacingEm());
            ox = this.divider(ox, centerY, 11.0F, GAP, unit, alpha);
        }

        ox = this.drawBlock(ox, centerY, font, labelFont, this.fpsText, "fps", unit, alpha);

        ox = this.divider(ox, centerY, 11.0F, GAP, unit, alpha);
        ox = this.drawBlock(ox, centerY, font, labelFont, this.pingText, "ms", unit, alpha);

        ox = this.divider(ox, centerY, 11.0F, GAP, unit, alpha);
        ox = this.drawBlock(ox, centerY, font, labelFont, this.tpsText, "tps", unit, alpha);

        ox = this.divider(ox, centerY, 11.0F, GAP, unit, alpha);
        float timeSize = TEXT_SIZE * unit;
        Render2DUtil.text(ox, labelFont.centeredTextY(centerY, timeSize), timeSize, this.timeText)
                .font(labelFont)
                .style(UiFontStyle.REGULAR)
                .color(ColorUtil.multiplyAlpha(Theme.Colors.SECONDARY_DARK, alpha))
                .draw();
    }

    private float drawBlock(float ox, float centerY, MsdfFont font, MsdfFont labelFont,
                            String value, String label, float unit, float alpha) {
        float textSize = TEXT_SIZE * unit;
        float letterSpacing = textSize * UiFontStyle.SEMIBOLD.letterSpacingEm();
        Render2DUtil.text(ox, font.centeredTextY(centerY, textSize), textSize, value)
                .font(font)
                .style(UiFontStyle.SEMIBOLD)
                .color(ColorUtil.multiplyAlpha(-1, alpha))
                .draw();
        ox += font.measureWidth(value, textSize, letterSpacing) + LABEL_GAP * unit;
        float labelSize = LABEL_SIZE * unit;
        Render2DUtil.text(ox, labelFont.centeredTextY(centerY, labelSize), labelSize, label)
                .font(labelFont)
                .style(UiFontStyle.REGULAR)
                .color(ColorUtil.multiplyAlpha(Theme.Colors.SECONDARY_DARK, alpha))
                .draw();
        ox += labelFont.measureWidth(label, labelSize, labelSize * UiFontStyle.REGULAR.letterSpacingEm());
        return ox;
    }

    private float blockWidth(MsdfFont font, MsdfFont labelFont, String value, String label, float unit) {
        float textSize = TEXT_SIZE * unit;
        float labelSize = LABEL_SIZE * unit;
        return font.measureWidth(value, textSize, textSize * UiFontStyle.SEMIBOLD.letterSpacingEm())
                + LABEL_GAP * unit
                + labelFont.measureWidth(label, labelSize, labelSize * UiFontStyle.REGULAR.letterSpacingEm());
    }

    private static int tps() {
        float value = ServerTickSync.INSTANCE.effectiveTps();
        return Math.round(value);
    }

    private static int latency(Minecraft mc) {
        if (mc.getConnection() != null && mc.player != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            return info != null ? info.getLatency() : 0;
        } else {
            return 0;
        }
    }

    private float scaledUnit(float unit) {
        return unit / 1.175F;
    }
}
