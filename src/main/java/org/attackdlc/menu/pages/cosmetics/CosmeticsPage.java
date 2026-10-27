package org.attackdlc.menu.pages.cosmetics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.attackdlc.cosmetics.CosmeticManager;
import org.attackdlc.cosmetics.CosmeticRenderer;
import org.attackdlc.menu.core.MenuPage;
import org.attackdlc.menu.ui.PageComponent;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.Textures;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.TextAlign;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;

/**
 * Страница косметики в клик GUI.
 * Слева — категории, справа — сетка карточек.
 */
public final class CosmeticsPage extends PageComponent {

    private static final float SIDEBAR_X = 24f;
    private static final float SIDEBAR_W = 140f;
    private static final float TAB_H = 28f;
    private static final float TAB_GAP = 3f;
    private static final float CONTENT_X = 176f;
    private static final float CONTENT_Y = 100f;
    private static final float CARD_W = 96f;
    private static final float CARD_H = 64f;
    private static final float CARD_GAP = 6f;
    private static final int COLUMNS = 4;

    private static final Map<String, String> CATEGORY_RU = new HashMap<>();
    static {
        CATEGORY_RU.put("wings", "Крылья");
        CATEGORY_RU.put("backpacks", "Рюкзаки");
        CATEGORY_RU.put("hats", "Шляпы");
        CATEGORY_RU.put("ears", "Уши");
        CATEGORY_RU.put("heads", "Головы");
        CATEGORY_RU.put("masks", "Маски");
        CATEGORY_RU.put("pets", "Питомцы");
        CATEGORY_RU.put("tails", "Хвосты");
        CATEGORY_RU.put("stockings", "Носки");
        CATEGORY_RU.put("paws", "Лапы");
    }

    private static final Map<String, Identifier> CATEGORY_ICONS = new HashMap<>();
    static {
        CATEGORY_ICONS.put("wings", Textures.Icons.ZAP);
        CATEGORY_ICONS.put("pets", Textures.Icons.GIFT);
        CATEGORY_ICONS.put("hats", Textures.Icons.PERSON_STANDING);
        CATEGORY_ICONS.put("ears", Textures.Icons.PERSON_STANDING);
        CATEGORY_ICONS.put("heads", Textures.Icons.PERSON_STANDING);
        CATEGORY_ICONS.put("masks", Textures.Icons.EYE);
        CATEGORY_ICONS.put("tails", Textures.Icons.ZAP);
        CATEGORY_ICONS.put("backpacks", Textures.Icons.BOXES);
        CATEGORY_ICONS.put("stockings", Textures.Icons.PERSON_STANDING);
        CATEGORY_ICONS.put("paws", Textures.Icons.PERSON_STANDING);
    }

    private String currentCategory;
    private List<CosmeticManager.Entry> entries;
    private float scroll;
    private float targetScroll;
    private final Map<String, Float> hoverAnims = new HashMap<>();
    private final Map<String, Float> selectAnims = new HashMap<>();
    private long lastFrame;
    private float frameDelta = 1f;

    @Override
    protected void onLayout() {
        if (state == null || state.displayPage() != MenuPage.COSMETICS) return;
        long now = System.currentTimeMillis();
        frameDelta = Math.min(50f, Math.max(0.5f, now - lastFrame));
        lastFrame = now;
        scroll = lerp(scroll, targetScroll, 0.045f, frameDelta);

        if (entries == null) {
            CosmeticManager.ensureLoaded();
            List<String> cats = CosmeticManager.categories();
            currentCategory = cats.isEmpty() ? "" : cats.get(0);
            entries = CosmeticManager.byCategory(currentCategory);
        }
    }

    @Override
    public void render(Minecraft mc, GuiGraphicsExtractor ctx) {
        if (state == null || state.displayPage() != MenuPage.COSMETICS) return;
        float p = progress;
        if (p < 0.01f) return;

        renderSidebar();
        renderGrid();
    }

    private void renderSidebar() {
        CosmeticManager.ensureLoaded();
        List<String> categories = CosmeticManager.categories();
        float baseY = sy(CONTENT_Y);
        float p = progress;

        for (int i = 0; i < categories.size(); i++) {
            String cat = categories.get(i);
            float ryScreen = baseY + i * (TAB_H + TAB_GAP);
            float ry = designY(ryScreen);
            boolean selected = cat.equals(currentCategory);
            boolean hovered = mouseY >= ryScreen && mouseY <= ryScreen + px(TAB_H) && mouseX >= sx(SIDEBAR_X) && mouseX <= sx(SIDEBAR_X + SIDEBAR_W);

            float hover = lerp(hoverAnims.getOrDefault(cat, 0f), hovered ? 1f : 0f, 0.028f, frameDelta);
            hoverAnims.put(cat, hover);

            int bg = ColorUtil.lerp(0xFF10141B, 0xFF1B212A, hover * 0.7f);
            if (selected) bg = ColorUtil.lerp(bg, ColorUtil.withAlpha(Theme.getAccent(), 0x20), 0.8f);

            this.rect(SIDEBAR_X, ry, px(SIDEBAR_W), TAB_H, bg, 5f, p);

            if (selected) {
                Render2DUtil.rect(sx(SIDEBAR_X + 2f), sy(ry + 4f), px(2f), px(TAB_H - 8f))
                        .color(Theme.getAccent()).radius(px(1f)).draw();
            }

            String label = CATEGORY_RU.getOrDefault(cat, cat);
            int tc = selected ? Theme.Colors.PRIMARY_BRIGHT : Theme.Colors.SECONDARY;
            float textY = sy(ry + TAB_H / 2f);
            Render2DUtil.text(sx(SIDEBAR_X + 9f), UiFonts.sfProDisplay().centeredTextY(textY, px(9f)), px(9f), label)
                    .style(UiFontStyle.MEDIUM).color(this.alpha(tc, p)).draw();
        }
    }

    private void renderGrid() {
        CosmeticManager.ensureLoaded();
        if (entries == null || entries.isEmpty()) {
            Render2DUtil.text(sx(32f), sy(160f), px(12f), "Нет косметики")
                    .style(UiFontStyle.REGULAR).color(Theme.Colors.SECONDARY_DARK).draw();
            return;
        }

        float contentW = width() - px(CONTENT_X + 16f);
        float contentH = height() - px(CONTENT_Y + 16f);
        float gridYDesign = CONTENT_Y + (1f - progress) * 7f;
        float maxScroll = Math.max(0f, getMaxScroll(contentH));
        float clampedScroll = Math.max(0f, Math.min(scroll, maxScroll));
        float p = progress;

        for (int i = 0; i < entries.size(); i++) {
            CosmeticManager.Entry entry = entries.get(i);
            int col = i % COLUMNS;
            int row = i / COLUMNS;

            float cardX = CONTENT_X + 8f + col * (CARD_W + CARD_GAP);
            float cardY = gridYDesign + 8f + row * (CARD_H + CARD_GAP) - clampedScroll;

            float cardYScreen = sy(cardY);
            float gridYScreen = sy(gridYDesign);
            float contentBScreen = sy(CONTENT_Y + contentH);

            if (cardYScreen + px(CARD_H) < gridYScreen || cardYScreen > contentBScreen) continue;

            boolean selected = CosmeticRenderer.Instance != null && CosmeticRenderer.Instance.isEquipped(entry.id());
            boolean hovered = mouseX >= sx(cardX) && mouseX <= sx(cardX + CARD_W)
                    && mouseY >= cardYScreen && mouseY <= cardYScreen + px(CARD_H);

            float hover = lerp(hoverAnims.getOrDefault(entry.id(), 0f), hovered ? 1f : 0f, 0.032f, frameDelta);
            hoverAnims.put(entry.id(), hover);

            float select = lerp(selectAnims.getOrDefault(entry.id(), selected ? 1f : 0f), selected ? 1f : 0f, 0.03f, frameDelta);
            selectAnims.put(entry.id(), select);

            int bg = ColorUtil.lerp(0xFF12161D, 0xFF1B212A, hover * 0.75f);
            if (select > 0.001f) {
                bg = ColorUtil.lerp(bg, ColorUtil.withAlpha(Theme.getAccent(), 0x1E), select);
            }

            rect(cardX, cardY, CARD_W, CARD_H, bg, 5f, p);

            if (select > 0.001f) {
                Render2DUtil.rect(sx(cardX), sy(cardY + 5f), px(2f), px(CARD_H - 10f))
                        .color(Theme.getAccent()).radius(px(1f)).draw();
                Render2DUtil.rect(sx(cardX + CARD_W - 8f), sy(cardY + 7f), px(3f), px(3f))
                        .color(Theme.getAccent()).radius(px(1.5f)).draw();
            } else {
                outline(cardX, cardY, CARD_W, CARD_H, 0xFFFFFFFF, 5f, 0.7f, p);
            }

            // Icon
            Identifier icon = CATEGORY_ICONS.getOrDefault(entry.category(), Textures.Icons.SPARKLES);
            float iconSize = px(22f);
            float iconX = sx(cardX + (CARD_W - iconSize) / 2f);
            float iconY = sy(cardY + 10f);
            Render2DUtil.texture(iconX, iconY, iconSize, iconSize, icon)
                    .color(alpha(-1, (0.5f + hover * 0.3f) * p)).draw();

            // Name
            String name = trim(entry.displayName(), CARD_W - 12f, 7f);
            int tc = select > 0.001f ? Theme.Colors.PRIMARY_BRIGHT : Theme.Colors.SECONDARY;
            float nx = sx(cardX + CARD_W / 2f);
            float ny = sy(cardY + CARD_H - 14f);
            Render2DUtil.text(nx, ny, px(7f), name)
                    .style(UiFontStyle.REGULAR).color(alpha(tc, p))
                    .align(TextAlign.CENTER).draw();
        }

        // Scrollbar
        if (maxScroll > 0.01f) {
            float trackX = sx(CONTENT_X + contentW - 4f);
            float trackY = sy(CONTENT_Y + 7f);
            float trackH = Math.max(0f, px(contentH - 14f));
            float thumbH = Math.max(22f, trackH * trackH / (trackH + px(maxScroll)));
            float thumbY = sy(trackY + (trackH - thumbH) * (clampedScroll / maxScroll));

            Render2DUtil.rect(trackX, trackY, px(2f), px(trackH))
                    .color(ColorUtil.withAlpha(-1, Math.round(0.08f * p))).radius(px(1f)).draw();
            Render2DUtil.rect(trackX, thumbY, px(2f), thumbH)
                    .color(ColorUtil.withAlpha(Theme.getAccent(), Math.round(0xBF * p))).radius(px(1f)).draw();
        }
    }

    @Override
    public boolean handleClick(int mx, int my) {
        if (state == null || state.displayPage() != MenuPage.COSMETICS) return false;

        CosmeticManager.ensureLoaded();
        List<String> categories = CosmeticManager.categories();

        // Sidebar clicks
        float sidebarX = sx(SIDEBAR_X);
        float sidebarY = sy(CONTENT_Y);
        for (int i = 0; i < categories.size(); i++) {
            float ry = sidebarY + i * (TAB_H + TAB_GAP);
            if (my >= ry && my <= ry + px(TAB_H) && mx >= sidebarX && mx <= sidebarX + px(SIDEBAR_W)) {
                String cat = categories.get(i);
                if (!cat.equals(currentCategory)) {
                    currentCategory = cat;
                    entries = CosmeticManager.byCategory(cat);
                    scroll = 0f;
                    targetScroll = 0f;
                    hoverAnims.clear();
                    selectAnims.clear();
                }
                return true;
            }
        }

        // Grid clicks
        float contentW = width() - px(CONTENT_X + 16f);
        float contentH = height() - px(CONTENT_Y + 16f);
        float gridYDesign = CONTENT_Y + (1f - progress) * 7f;
        float maxScroll = Math.max(0f, getMaxScroll(contentH));
        float clampedScroll = Math.max(0f, Math.min(scroll, maxScroll));
        float gridYScreen = sy(gridYDesign);
        float contentBScreen = sy(CONTENT_Y + contentH);

        if (my >= gridYScreen && my <= contentBScreen && mx >= sx(CONTENT_X) && mx <= sx(CONTENT_X + contentW)) {
            float gridXDesign = CONTENT_X + 8f;
            float gridXScreen = sx(gridXDesign);
            float gridYpScreen = gridYScreen + px(8f);
            int col = (int) ((mx - gridXScreen) / px(CARD_W + CARD_GAP));
            int row = (int) ((my - gridYpScreen + clampedScroll) / px(CARD_H + CARD_GAP));
            float localX = mx - gridXScreen - col * px(CARD_W + CARD_GAP);
            float localY = my - gridYpScreen + clampedScroll - row * px(CARD_H + CARD_GAP);

            if (col >= 0 && col < COLUMNS && localX >= 0 && localX <= px(CARD_W)
                    && localY >= 0 && localY <= px(CARD_H)) {
                int index = row * COLUMNS + col;
                if (index >= 0 && index < entries.size()) {
                    CosmeticManager.Entry entry = entries.get(index);
                    CosmeticRenderer renderer = CosmeticRenderer.Instance;
                    if (renderer != null) {
                        if (renderer.isEquipped(entry.id())) {
                            renderer.unequip(entry.id());
                        } else {
                            renderer.equip(entry.id());
                        }
                    }
                    selectAnims.clear();
                    selectAnims.put(entry.id(), 0f);
                    return true;
                }
            }
        }
        return false;
    }

    public void handleScroll(int mx, int my, double v) {
        if (state == null || state.displayPage() != MenuPage.COSMETICS) return;
        float contentW = width() - px(CONTENT_X + 16f);
        float contentH = height() - px(CONTENT_Y + 16f);
        float gridYDesign = CONTENT_Y + (1f - progress) * 7f;
        float gridYScreen = sy(gridYDesign);
        float contentBScreen = sy(CONTENT_Y + contentH);
        if (my >= gridYScreen && my <= contentBScreen && mx >= sx(CONTENT_X) && mx <= sx(CONTENT_X + contentW)) {
            targetScroll = Math.max(0f, Math.min(targetScroll - (float) v * 28f, getMaxScroll(contentH)));
        }
    }

    public boolean handleKey(int key) { return false; }
    public boolean handleCharacter(int cp) { return false; }

    private float getMaxScroll(float viewportHeight) {
        if (entries == null || entries.isEmpty()) return 0f;
        int rows = (entries.size() + COLUMNS - 1) / COLUMNS;
        float totalH = rows * (CARD_H + CARD_GAP) - CARD_GAP + 16f;
        return Math.max(0f, totalH - viewportHeight);
    }

    private static float lerp(float cur, float tgt, float speed, float delta) {
        if (Math.abs(tgt - cur) < 0.0005f) return tgt;
        float factor = 1f - (float) Math.pow(1f - Math.max(0.001f, Math.min(0.999f, speed)), delta / 16.6667f);
        float next = cur + (tgt - cur) * factor;
        return Math.abs(tgt - next) < 0.0005f ? tgt : next;
    }

    private static String trim(String text, float maxWidth, float size) {
        if (text == null) return "";
        int maxChars = Math.max(3, (int) (maxWidth / (size * 0.55f)));
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars - 1) + "\u2026";
    }
}
