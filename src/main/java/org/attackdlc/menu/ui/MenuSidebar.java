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

public final class MenuSidebar extends Component {
    private static final int WIDTH = 160;
    private static final int ROW_H = 32;
    private static final int ROW_GAP = 2;
    private static final int ROW_X = 10;
    private static final int CATEGORY_H = 24;
    private static final int CATEGORY_GAP = 1;
    private static final int CATEGORY_X = 10;
    private static final int BRAND_Y = 14;
    private static final int CATEGORY_LABEL_Y = 48;
    private static final int NAV_START_Y = 226;
    private static final int PROFILE_Y = 548;
    private static final int PROFILE_H = 48;

    private static final MenuPage[] NAV_PAGES = new MenuPage[]{
        MenuPage.SEARCH, MenuPage.FRIENDS, MenuPage.ATTACK,
        MenuPage.SETTINGS, MenuPage.CONFIGURATIONS
    };
    private static final Identifier[] NAV_ICONS = new Identifier[]{
        Textures.Header.SEARCH, Textures.Header.FRIENDS,
        Textures.Icons.USER_ROUND, Textures.Header.SETTINGS, Textures.Header.DOCUMENT
    };
    private static final String[] NAV_LABELS = new String[]{
        "Search", "Friends", "Attack", "Settings", "Configs"
    };
    private static final Identifier[] CATEGORY_ICONS = new Identifier[]{
        Textures.Icons.SWORDS_DAGGER, Textures.Icons.PERSON_STANDING, Textures.Icons.EYE,
        Textures.Icons.USER_ROUND, Textures.Icons.BOXES, Textures.Icons.BRAIN
    };

    private int mouseX, mouseY;
    private MenuPage activePage = MenuPage.NONE;
    private FeatureCategory activeCategory = FeatureCategory.COMBAT;
    private float panelRadius;
    private int hoveredNav = -1;
    private int hoveredCat = -1;

    public void place(Component frame, MenuOverlayState state, Minecraft mc, int mx, int my,
                      FeatureCategory activeCat, float panelRadius) {
        this.attach(frame, frame.x(), frame.y(), (float) WIDTH, frame.height());
        this.mouseX = mx; this.mouseY = my;
        this.activePage = state.page();
        this.activeCategory = activeCat;
        this.panelRadius = panelRadius;
    }

    public MenuPage pageAt(float mx, float my) {
        for (int i = 0; i < NAV_PAGES.length; i++) {
            if (hit(mx, my, ROW_X, navY(i), WIDTH - ROW_X, ROW_H)) { hoveredNav = i; return NAV_PAGES[i]; }
        }
        hoveredNav = -1;
        return MenuPage.NONE;
    }

    public FeatureCategory categoryAt(float mx, float my) {
        FeatureCategory[] cats = FeatureCategory.values();
        for (int i = 0; i < Math.min(cats.length, CATEGORY_ICONS.length); i++) {
            if (hit(mx, my, CATEGORY_X, catY(i), WIDTH - CATEGORY_X, CATEGORY_H)) { hoveredCat = i; return cats[i]; }
        }
        hoveredCat = -1;
        return null;
    }

    @Override
    public void render(Minecraft mc, GuiGraphicsExtractor ctx) {
        drawBackground();
        drawBrand();
        drawCategories();
        drawHairline(376);
        drawNav();
        drawProfile();
    }

    private void drawBackground() {
        float r = panelRadius, h = height();
        Render2DUtil.rect(sx(0), sy(0), px(WIDTH), px(h))
            .color(Theme.Colors.BACKGROUND_PRIMARY_50).radius(r, 0.0F, 0.0F, r).draw();
        float bw = Math.max(0.5F, px(0.5F));
        Render2DUtil.rect(sx(WIDTH - bw), sy(0), bw, px(h))
            .color(Theme.Colors.DIVIDER_SUBTLE).draw();
    }

    private void drawBrand() {
        float logoSize = px(18f);
        Render2DUtil.texture(sx(14f), sy(BRAND_Y), logoSize, logoSize, Textures.Icons.SWORDS_FINE)
            .color(ColorUtil.multiplyAlpha(-1, 0.88F)).draw();
        float nameX = sx(38f);
        float nameY = UiFonts.sfProDisplay().centeredTextY(sy(BRAND_Y + logoSize / 2f), px(11f));
        Render2DUtil.text(nameX, nameY, px(11f), "AttackDLC")
            .style(UiFontStyle.SEMIBOLD).color(Theme.Colors.PRIMARY).draw();
    }

    private void drawCategories() {
        Render2DUtil.text(sx(CATEGORY_X), sy(CATEGORY_LABEL_Y), px(7.5f), "CATEGORIES")
            .style(UiFontStyle.MEDIUM).color(Theme.Colors.SECTION).draw();
        FeatureCategory[] cats = FeatureCategory.values();
        for (int i = 0; i < Math.min(cats.length, CATEGORY_ICONS.length); i++) {
            float ry = catY(i);
            boolean active = cats[i] == activeCategory;
            boolean hovered = hoveredCat == i;
            if (active) {
                Render2DUtil.rect(sx(CATEGORY_X), sy(ry), px(WIDTH - CATEGORY_X), px(CATEGORY_H))
                    .color(ColorUtil.rgba(255, 255, 255, 10)).radius(px(4f)).draw();
                Render2DUtil.rect(sx(CATEGORY_X + 2f), sy(ry + 6f), px(2f), px(CATEGORY_H - 12f))
                    .color(Theme.getAccent()).radius(px(1f)).draw();
            } else if (hovered) {
                Render2DUtil.rect(sx(CATEGORY_X), sy(ry), px(WIDTH - CATEGORY_X), px(CATEGORY_H))
                    .color(ColorUtil.rgba(255, 255, 255, 6)).radius(px(4f)).draw();
            }
            Render2DUtil.texture(sx(CATEGORY_X + 10f), sy(ry + 5f), px(12f), px(12f), CATEGORY_ICONS[i])
                .color(active ? Theme.Colors.TEXT : (hovered ? Theme.Colors.TEXT_TEXT : Theme.Colors.ICON)).draw();
            float tx = CATEGORY_X + 26f;
            float ty = centeredTextY(sy(ry + CATEGORY_H / 2f), px(10f));
            int tc = active ? Theme.Colors.PRIMARY : (hovered ? Theme.Colors.TEXT_TEXT : Theme.Colors.SECONDARY);
            Render2DUtil.text(sx(tx), ty, px(10f), cats[i].getDisplayName())
                .style(UiFontStyle.REGULAR).color(tc).draw();
        }
    }

    private void drawNav() {
        for (int i = 0; i < NAV_PAGES.length; i++) {
            float ry = navY(i);
            boolean active = activePage == NAV_PAGES[i];
            boolean hovered = hoveredNav == i;
            if (active) {
                Render2DUtil.rect(sx(ROW_X), sy(ry), px(WIDTH - ROW_X), px(ROW_H))
                    .color(ColorUtil.rgba(255, 255, 255, 14)).radius(px(5f)).draw();
                Render2DUtil.rect(sx(ROW_X + 2f), sy(ry + ROW_H / 2f - px(5f)), px(2f), px(10f))
                    .color(Theme.getAccent()).radius(px(1f)).draw();
            } else if (hovered) {
                Render2DUtil.rect(sx(ROW_X), sy(ry), px(WIDTH - ROW_X), px(ROW_H))
                    .color(ColorUtil.rgba(255, 255, 255, 6)).radius(px(5f)).draw();
            }
            Render2DUtil.texture(sx(ROW_X + 10f), sy(ry + (ROW_H - 14) / 2f), px(14f), px(14f), NAV_ICONS[i])
                .color(active ? Theme.getAccent() : (hovered ? Theme.Colors.TEXT : Theme.Colors.ICON)).draw();
            float textY = centeredTextY(sy(ry + ROW_H / 2f), px(11f));
            int tc = active ? Theme.Colors.PRIMARY : (hovered ? Theme.Colors.TEXT : Theme.Colors.SECONDARY);
            Render2DUtil.text(sx(ROW_X + 30f), textY, px(11f), NAV_LABELS[i])
                .style(UiFontStyle.MEDIUM).color(tc).draw();
        }
    }

    private void drawProfile() {
        float py = sy(PROFILE_Y);
        float ph = px(PROFILE_H);
        float pw = px(WIDTH - ROW_X);
        boolean hovered = mouseY >= py && mouseY <= py + ph && mouseX >= sx(ROW_X) && mouseX <= sx(WIDTH - 4);
        if (hovered) {
            Render2DUtil.rect(sx(ROW_X), py, pw, ph)
                .color(ColorUtil.rgba(255, 255, 255, 7)).radius(px(5f)).draw();
        }
        float avSize = px(20f);
        float avX = sx(ROW_X + 10f);
        float avY = py + px(14f);
        Render2DUtil.texture(avX, avY, avSize, avSize, Textures.Header.PROFILE_AVATAR).draw();
        float nameX = sx(ROW_X + 38f);
        float nameCY = PROFILE_Y + 17f;
        try {
            String nick = ru.xd.Session.getName();
            String username = (nick != null && !nick.isBlank()) ? nick : "Player";
            Render2DUtil.text(nameX, UiFonts.sfProDisplay().centeredTextY(sy(nameCY), px(10f)), px(10f), username)
                .style(UiFontStyle.SEMIBOLD).color(Theme.Colors.PRIMARY).draw();
            String role = ru.xd.Session.getRole();
            if ("owner".equalsIgnoreCase(role)) {
                float crownSize = px(10f);
                float nameW = UiFonts.sfProDisplay().measureWidth(username, px(10f), 0f);
                Render2DUtil.texture(sx(nameX + nameW + px(4f)), sy(nameCY + 1f), crownSize, crownSize, Textures.Icons.CROWN)
                    .color(Theme.getAccent()).draw();
            }
            if (role != null && !role.isBlank()) {
                Render2DUtil.text(nameX, UiFonts.sfProDisplay().centeredTextY(sy(nameCY + 26f), px(8f)), px(8f), role)
                    .style(UiFontStyle.REGULAR).color(Theme.Colors.SECONDARY_DARK).draw();
            }
        } catch (Throwable ignored) {}
    }

    private void drawHairline(float y) {
        float lw = WIDTH - 32;
        Render2DUtil.rect(sx(16f), sy(y), px(lw), Math.max(0.5F, px(0.5F)))
            .color(Theme.Colors.DIVIDER_SUBTLE).draw();
    }

    private static float catY(int i)    { return CATEGORY_LABEL_Y + 8f + i * (CATEGORY_H + CATEGORY_GAP); }
    private static float navY(int i)    { return NAV_START_Y + i * (ROW_H + ROW_GAP); }
}
