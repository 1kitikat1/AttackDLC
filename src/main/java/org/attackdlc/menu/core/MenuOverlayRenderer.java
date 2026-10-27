package org.attackdlc.menu.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.feature.FeatureManager;
import org.attackdlc.menu.pages.ConfigPage;
import org.attackdlc.menu.pages.SettingsPage;
import org.attackdlc.menu.pages.friends.FriendPage;
import org.attackdlc.menu.pages.modules.ModulePage;
import org.attackdlc.menu.ui.Component;
import org.attackdlc.menu.ui.MenuSidebar;
import org.attackdlc.utils.math.MathUtil;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.Render2DUtil;

/**
 * Рендерер меню — чистый минимализм: одна волосковая рамка, глубокий фон,
 * мягкая тень, без анимированных шейдеров и градиентов.
 */
public final class MenuOverlayRenderer extends Component {
    private final MenuSidebar sidebar = new MenuSidebar();
    private final MenuHeader header = new MenuHeader();
    private final ContentFrame content = new ContentFrame();
    private final ModulePage modules = new ModulePage();
    private final SettingsPage settings = new SettingsPage();
    private final FriendPage friends = new FriendPage();
    private final ConfigPage configs = new ConfigPage();
    private final org.attackdlc.menu.pages.attack.AttackPage attack = new org.attackdlc.menu.pages.attack.AttackPage();
    private MenuDimensions dimensions;
    private MenuPage displayPage = MenuPage.NONE;

    public void layout(Minecraft mc, MenuOverlayState state, int sw, int sh, int mx, int my) {
        this.dimensions = MenuDimensions.resolve(mc, state);
        ensurePosition(state, sw, sh);
        float slideY = (1.0F - state.openProgress()) * Math.max(0.0F, sh - state.panelY());
        configureFrame(state.panelX(), state.panelY() + slideY, dimensions.panelWidth(), dimensions.panelHeight());
        this.displayPage = state.displayPage();
        sidebar.place(this, state, mc, mx, my, modules.selectedCategory(), dimensions.panelRadius());
        content.configureFrame(x() + px(170f), y(), width() - px(170f), height());
        modules.layout(content, state, mx, my);
        settings.layout(content, state, mx, my);
        friends.layout(content, state, mx, my);
        configs.layout(content, state, mx, my);
        attack.layout(content, state, mx, my);
        header.place(this, state, mc, mx, my, modules.selectedCategory(),
                     enabledCount(modules.selectedCategory()), totalCount(modules.selectedCategory()));
    }

    public boolean handleMouseButton(int mx, int my, int button, MenuOverlayState state) {
        if (button == 0) {
            FeatureCategory cat = sidebar.categoryAt(mx, my);
            if (cat != null) {
                if (state.page() != MenuPage.NONE) state.openPage(MenuPage.NONE);
                modules.selectCategory(cat);
                displayPage = state.displayPage();
                return true;
            }
            MenuPage page = sidebar.pageAt(mx, my);
            if (page != MenuPage.NONE) {
                state.openPage(page);
                displayPage = state.displayPage();
                return true;
            }
            if (sidebar.contains(mx, my)) return true;
            if (header.isChevronLeftAt(mx, my)) { state.goBack(); displayPage = state.displayPage(); return true; }
            if (header.isChevronRightAt(mx, my)) { state.goForward(); displayPage = state.displayPage(); return true; }
            if (header.isSearchAt(mx, my)) {
                state.openPage(state.page() == MenuPage.SEARCH ? MenuPage.NONE : MenuPage.SEARCH);
                displayPage = state.displayPage();
                return true;
            }
        }
        return switch (displayPage) {
            case SETTINGS -> settings.handleMouseButton(mx, my, button);
            case FRIENDS  -> friends.handleClick(mx, my);
            case CONFIGURATIONS -> configs.handleClick(mx, my);
            case ATTACK -> attack.handleClick(mx, my);
            default     -> modules.handleMouseButton(mx, my, button);
        };
    }

    public void drag(int mx, int my) {
        if (displayPage == MenuPage.SETTINGS) settings.drag(mx, my);
        else if (displayPage == MenuPage.NONE || displayPage == MenuPage.SEARCH) modules.drag(mx);
    }

    public void releasePointer() { modules.releasePointer(); settings.releasePointer(); }
    public boolean isSearchOpen()   { return modules.isSearchOpen(); }
    public boolean isSearchFocused(){ return modules.isSearchFocused(); }
    public void focusSearch()       { modules.focusSearch(); }
    public boolean isCapturingBind(){ return displayPage == MenuPage.NONE && modules.isCapturingBind(); }
    public void appendSearchCodePoint(int cp) { modules.appendCodePoint(cp); }
    public void backspaceSearch() { modules.backspace(); }

    public boolean handleKey(int key) {
        if (displayPage == MenuPage.FRIENDS) return friends.handleKey(key);
        if (displayPage == MenuPage.ATTACK)  return attack.handleKey(key);
        if (displayPage == MenuPage.CONFIGURATIONS) return configs.handleKey(key);
        return (displayPage == MenuPage.NONE || displayPage == MenuPage.SEARCH) && modules.handleKey(key);
    }

    public boolean handleCharacter(int cp) {
        if (displayPage == MenuPage.FRIENDS) return friends.handleCharacter(cp);
        if (displayPage == MenuPage.ATTACK)  return attack.handleCharacter(cp);
        if (displayPage == MenuPage.CONFIGURATIONS) return configs.handleCharacter(cp);
        return displayPage == MenuPage.NONE && modules.handleCharacter(cp);
    }

    public void handleScroll(int mx, int my, double v) {
        if (header.handleScroll(mx, my, v)) return;
        switch (displayPage) {
            case SETTINGS:    settings.handleScroll(mx, my, v); break;
            case FRIENDS:     friends.handleScroll(mx, my, v); break;
            case CONFIGURATIONS: configs.handleScroll(mx, my, v); break;
            case ATTACK:      attack.handleScroll(mx, my, v); break;
            case NONE: case SEARCH: modules.handleScroll(mx, my, v); break;
            default: break;
        }
    }

    @Override
    public boolean isDragHandle(float mx, float my) {
        return contains(mx, my) && my <= y() + px(52f) && mx > x() + px(170f)
            && !header.isSearchAt(mx, my) && !header.isChevronLeftAt(mx, my) && !header.isChevronRightAt(mx, my);
    }

    @Override
    public void render(Minecraft mc, GuiGraphicsExtractor ctx) {
        drawBackground();
        switch (displayPage) {
            case SETTINGS:    settings.render(mc, ctx); break;
            case FRIENDS:     friends.render(mc, ctx); break;
            case CONFIGURATIONS: configs.render(mc, ctx); break;
            case ATTACK:      attack.render(mc, ctx); break;
            default:          modules.render(mc, ctx);
        }
        header.render(mc, ctx);
        sidebar.render(mc, ctx);
    }

    // ── Фон: плоский, без шейдера ────────────────────────────────────────────

    private void drawBackground() {
        float r = dimensions.panelRadius();
        Render2DUtil.rect(x(), y() + px(5f), width(), height())
            .color(0).radius(r).shadow(Theme.Colors.PANEL_SHADOW, px(14f)).draw();
        Render2DUtil.rect(x(), y(), width(), height())
            .color(Theme.Colors.PANEL).radius(r).draw();
        float border = Math.max(0.5F, px(0.8F));
        Render2DUtil.rect(x(), y(), width(), height())
            .color(0).radius(r).border(border, Theme.Colors.PANEL_BORDER).draw();
    }

    private static int totalCount(FeatureCategory cat) {
        return FeatureManager.INSTANCE.getFeatures(cat).size();
    }

    private static int enabledCount(FeatureCategory cat) {
        int c = 0;
        for (Feature f : FeatureManager.INSTANCE.getFeatures(cat)) if (f.isEnabled()) c++;
        return c;
    }

    private void ensurePosition(MenuOverlayState state, int sw, int sh) {
        if (!state.hasPosition()) {
            state.setPanelPosition((sw - dimensions.panelWidth()) / 2f, (sh - dimensions.panelHeight()) / 2f);
            state.markPositionInitialized();
        }
        float maxX = Math.max(0f, sw - dimensions.panelWidth());
        float maxY = Math.max(0f, sh - dimensions.panelHeight());
        state.setPanelPosition(MathUtil.clamp(state.panelX(), 0f, maxX), MathUtil.clamp(state.panelY(), 0f, maxY));
    }

    private static final class ContentFrame extends Component {
        @Override public void render(Minecraft mc, GuiGraphicsExtractor ctx) {}
    }
}
