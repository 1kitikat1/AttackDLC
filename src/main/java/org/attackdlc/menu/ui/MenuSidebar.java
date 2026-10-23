package org.attackdlc.menu.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.menu.core.MenuOverlayState;
import org.attackdlc.menu.core.MenuPage;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.Textures;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.UiFontStyle;
import org.attackdlc.utils.render.gui.UiFonts;
import org.attackdlc.utils.text.NameProtectUtil;
import org.attackdlc.utils.text.StringUtil;

/**
 * Боковая панель меню — ультра-минималистичная.
 * Тонкий фон, один волосковый разделитель, плоские строки без рамок.
 */
public final class MenuSidebar extends Component {
    private static final int WIDTH = 160;
    private static final int ROW_H = 32;
    private static final int ROW_GAP = 2;
    private static final int ROW_X = 10;
    private static final int CATEGORY_H = 24;
    private static final int CATEGORY_GAP = 1;
    private static final int CATEGORY_X = 10;
    private static final int BRAND_Y = 14;
    private static final int CATEGORY_LABEL_Y = 176;
    private static final int PROFILE_Y = 560;
    private static final int PROFILE_H = 48;

    private static final MenuPage[] MAIN_PAGES = new MenuPage[]{MenuPage.NONE, MenuPage.SETTINGS, MenuPage.CONFIGURATIONS};
    private static final MenuPage[] UTILITY_PAGES = new MenuPage[]{MenuPage.SEARCH, MenuPage.FRIENDS, MenuPage.ATTACK};
    private static final Identifier[] MAIN_ICONS = new Identifier[]{Textures.Icons.BOXES, Textures.Header.SETTINGS, Textures.Header.DOCUMENT};
    private static final String[] MAIN_LABELS = new String[]{"Modules", "Settings", "Configs"};
    private static final Identifier[] UTILITY_ICONS = new Identifier[]{Textures.Header.SEARCH, Textures.Header.FRIENDS, Textures.Icons.USER_ROUND};
    private static final String[] UTILITY_LABELS = new String[]{"Search", "Friends", "Attack"};
    private static final Identifier[] CATEGORY_ICONS = new Identifier[]{
        Textures.Icons.SWORDS_DAGGER, Textures.Icons.PERSON_STANDING, Textures.Icons.EYE,
        Textures.Icons.USER_ROUND, Textures.Icons.BOXES, Textures.Icons.BRAIN
    };

    private int mouseX;
    private int mouseY;
    private MenuPage activePage = MenuPage.NONE;
    private FeatureCategory activeCategory = FeatureCategory.COMBAT;
    private String username = "";
    private float panelRadius;

    public void place(Component frame, MenuOverlayState state, Minecraft mc, int mx, int my,
                      FeatureCategory activeCat, float panelRadius) {
        this.attach(frame, frame.x(), frame.y(), (float) WIDTH, frame.height());
        this.mouseX = mx;
        this.mouseY = my;
        this.activePage = state.page();
        this.activeCategory = activeCat;
        this.username = StringUtil.abbreviate(NameProtectUtil.protect(accountNick()), 13);
        this.panelRadius = panelRadius;
    }

    private static String accountNick() {
        try {
            String name = ru.xd.Session.getName();
            if (name != null && !name.isBlank()) return name;
        } catch (Throwable ignored) {}
        return "Не авторизован";
    }

    public MenuPage pageAt(float mx, float my) {
        for (int i = 0; i < MAIN_PAGES.length; i++) {
            if (hit(mx, my, ROW_X, mainRowY(i), WIDTH - ROW_X, ROW_H)) return MAIN_PAGES[i];
        }
        for (int i = 0; i < UTILITY_PAGES.length; i++) {
            if (hit(mx, my, ROW_X, utilityRowY(i), WIDTH - ROW_X, ROW_H)) return UTILITY_PAGES[i];
        }
        return MenuPage.NONE;
    }

    public FeatureCategory categoryAt(float mx, float my) {
        FeatureCategory[] cats = FeatureCategory.values();
        for (int i = 0; i < Math.min(cats.length, CATEGORY_ICONS.length); i++) {
            if (hit(mx, my, CATEGORY_X, categoryRowY(i), WIDTH - CATEGORY_X, CATEGORY_H)) return cats[i];
        }
        return null;
    }

    public boolean isProfileAt(float mx, float my) {
        return mx >= sx(ROW_X) && mx <= sx(WIDTH - 4) && my >= sy(PROFILE_Y) && my <= sy(PROFILE_Y + PROFILE_H);
    }

    @Override
    public void render(Minecraft mc, GuiGraphicsExtractor ctx) {
        drawBackground();
        drawBrand();
        drawGroup(MAIN_PAGES, MAIN_ICONS, MAIN_LABELS, MenuSidebar::mainRowY, ROW_H);
        drawHairline(170);
        drawCategories();
        drawHairline(388);
        drawGroup(UTILITY_PAGES, UTILITY_ICONS, UTILITY_LABELS, MenuSidebar::utilityRowY, ROW_H);
        drawProfile();
    }

    private void drawBackground() {
        float r = panelRadius;
        float h = height();
        // Фон панели
        Render2DUtil.rect(sx(0), sy(0), px(WIDTH), px(h))
            .color(Theme.Colors.BACKGROUND_PRIMARY_50)
            .radius(r, 0.0F, 0.0F, r)
            .draw();
        // Волосковый разделитель справа
        float bw = Math.max(0.5F, px(0.5F));
        Render2DUtil.rect(sx(WIDTH - bw), sy(0), bw, px(h))
            .color(Theme.Colors.DIVIDER_SUBTLE)
            .draw();
    }

    private void drawBrand() {
        float logoSize = px(18f);
        Render2DUtil.texture(sx(14f), sy(BRAND_Y), logoSize, logoSize, Textures.Icons.SWORDS_FINE)
            .color(ColorUtil.multiplyAlpha(-1, 0.88F))
            .draw();
        float nameX = sx(38f);
        float nameY = UiFonts.sfProDisplay().centeredTextY(sy(BRAND_Y + logoSize / 2f), px(11f));
        Render2DUtil.text(nameX, nameY, px(11f), "AttackDLC")
            .style(UiFontStyle.SEMIBOLD)
            .color(Theme.Colors.PRIMARY)
            .draw();
    }

    private void drawGroup(MenuPage[] pages, Identifier[] icons, String[] labels, YMapper yMap, int rowH) {
        for (int i = 0; i < pages.length; i++) {
            float ry = yMap.y(i);
            boolean active = activePage == pages[i];
            boolean hovered = hit(mouseX, mouseY, ROW_X, ry, WIDTH - ROW_X, rowH);
            drawRow(ROW_X, ry, rowH, active, hovered);
            float iconX = 20f;
            float iconY = ry + (rowH - 14) / 2f;
            int iconColor = active ? Theme.getAccent() : (hovered ? Theme.Colors.TEXT : Theme.Colors.ICON);
            Render2DUtil.texture(sx(iconX), sy(iconY), px(14f), px(14f), icons[i])
                .color(iconColor)
                .draw();
            float textX = 38f;
            float textY = centeredTextY(sy(ry + rowH / 2f), px(11f));
            int textColor = active ? Theme.Colors.PRIMARY : (hovered ? Theme.Colors.TEXT : Theme.Colors.SECONDARY);
            Render2DUtil.text(sx(textX), textY, px(11f), labels[i])
                .style(UiFontStyle.MEDIUM)
                .color(textColor)
                .draw();
        }
    }

    private void drawCategories() {
        // Заголовок
        float labelY = sy(CATEGORY_LABEL_Y);
        Render2DUtil.text(sx(CATEGORY_X), labelY, px(8f), "CATEGORIES")
            .style(UiFontStyle.MEDIUM)
            .color(Theme.Colors.SECTION)
            .draw();
        FeatureCategory[] cats = FeatureCategory.values();
        for (int i = 0; i < Math.min(cats.length, CATEGORY_ICONS.length); i++) {
            float ry = categoryRowY(i);
            boolean active = cats[i] == activeCategory;
            boolean hovered = hit(mouseX, mouseY, CATEGORY_X, ry, WIDTH - CATEGORY_X, CATEGORY_H);
            if (active) {
                Render2DUtil.rect(sx(CATEGORY_X), sy(ry), px(WIDTH - CATEGORY_X), px(CATEGORY_H))
                    .color(ColorUtil.rgba(255, 255, 255, 10))
                    .radius(px(4f))
                    .draw();
                Render2DUtil.rect(sx(CATEGORY_X + 2f), sy(ry + 6f), px(2f), px(CATEGORY_H - 12f))
                    .color(Theme.getAccent())
                    .radius(px(1f))
                    .draw();
            } else if (hovered) {
                Render2DUtil.rect(sx(CATEGORY_X), sy(ry), px(WIDTH - CATEGORY_X), px(CATEGORY_H))
                    .color(ColorUtil.rgba(255, 255, 255, 6))
                    .radius(px(4f))
                    .draw();
            }
            float iconX = CATEGORY_X + 10f;
            float iconY = ry + 5f;
            int c = active ? Theme.Colors.TEXT : (hovered ? Theme.Colors.TEXT_TEXT : Theme.Colors.ICON);
            Render2DUtil.texture(sx(iconX), sy(iconY), px(12f), px(12f), CATEGORY_ICONS[i])
                .color(c)
                .draw();
            float tx = CATEGORY_X + 26f;
            float ty = centeredTextY(sy(ry + CATEGORY_H / 2f), px(10f));
            int tc = active ? Theme.Colors.PRIMARY : (hovered ? Theme.Colors.TEXT_TEXT : Theme.Colors.SECONDARY);
            Render2DUtil.text(sx(tx), ty, px(10f), cats[i].getDisplayName())
                .style(UiFontStyle.REGULAR)
                .color(tc)
                .draw();
        }
    }

    private void drawProfile() {
        boolean hovered = isProfileAt(mouseX, mouseY);
        if (hovered) {
            Render2DUtil.rect(sx(CATEGORY_X), sy(PROFILE_Y), px(WIDTH - CATEGORY_X), px(PROFILE_H))
                .color(ColorUtil.rgba(255, 255, 255, 7))
                .radius(px(5f))
                .draw();
        }
        float avatarSize = px(20f);
        float avatarX = CATEGORY_X + 10f;
        float avatarY = PROFILE_Y + 14f;
        Render2DUtil.texture(sx(avatarX), sy(avatarY), avatarSize, avatarSize, Textures.Header.PROFILE_AVATAR).draw();

        // Имя + корона (корона под именем, центрирована)
        float nameX = CATEGORY_X + 38f;
        float nameCY = PROFILE_Y + 17f;
        Render2DUtil.text(sx(nameX), UiFonts.sfProDisplay().centeredTextY(sy(nameCY), px(10f)), px(10f), username)
            .style(UiFontStyle.SEMIBOLD)
            .color(Theme.Colors.PRIMARY)
            .draw();

        // Корона — по центру имени, ниже
        try {
            String role = ru.xd.Session.getRole();
            boolean isOwner = "owner".equalsIgnoreCase(role);
            float crownSize = px(10f);
            float nameW = UiFonts.sfProDisplay().measureWidth(username, px(10f), px(10f) * UiFontStyle.SEMIBOLD.letterSpacingEm());
            float crownX = sx(nameX + (nameW - crownSize) / 2f);
            float crownY = sy(nameCY + 10f);
            Render2DUtil.texture(crownX, crownY, crownSize, crownSize,
                Textures.Icons.CROWN)
                .color(isOwner ? Theme.getAccent() : Theme.Colors.SECONDARY)
                .draw();
        } catch (Throwable ignored) {}

        Render2DUtil.text(sx(nameX), UiFonts.sfProDisplay().centeredTextY(sy(nameCY + 26f), px(8f)), px(8f), "Аккаунты")
            .style(UiFontStyle.REGULAR)
            .color(Theme.Colors.SECONDARY_DARK)
            .draw();
    }

    private void drawRow(float x, float y, float h, boolean active, boolean hovered) {
        if (active) {
            Render2DUtil.rect(sx(x), sy(y), px(WIDTH - x), px(h))
                .color(ColorUtil.rgba(255, 255, 255, 14))
                .radius(px(5f))
                .draw();
            Render2DUtil.rect(sx(x + 2f), sy(y + h / 2f - px(5f)), px(2f), px(10f))
                .color(Theme.getAccent())
                .radius(px(1f))
                .draw();
        } else if (hovered) {
            Render2DUtil.rect(sx(x), sy(y), px(WIDTH - x), px(h))
                .color(ColorUtil.rgba(255, 255, 255, 6))
                .radius(px(5f))
                .draw();
        }
    }

    private void drawHairline(float y) {
        float lw = WIDTH - 32;
        Render2DUtil.rect(sx(16f), sy(y), px(lw), Math.max(0.5F, px(0.5F)))
            .color(Theme.Colors.DIVIDER_SUBTLE)
            .draw();
    }

    private static float mainRowY(int i)     { return 56f + i * (ROW_H + ROW_GAP); }
    private static float utilityRowY(int i)  { return 400f + i * (ROW_H + ROW_GAP); }
    private static float categoryRowY(int i) { return 196f + i * (CATEGORY_H + CATEGORY_GAP); }

    @FunctionalInterface
    private interface YMapper { float y(int v); }
}
