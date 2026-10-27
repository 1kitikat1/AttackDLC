package org.attackdlc.menu.pages.cosmetics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.attackdlc.menu.core.MenuPage;
import org.attackdlc.menu.ui.PageComponent;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.UiFontStyle;

/**
 * Cosmetics page — заглушка, позже будет полноценный UI.
 */
public final class CosmeticsPage extends PageComponent {

    @Override
    protected void onLayout() {}

    @Override
    public void render(Minecraft mc, GuiGraphicsExtractor ctx) {
        if (state == null || state.displayPage() != MenuPage.COSMETICS) return;
        float p = progress;
        Render2DUtil.text(sx(32f), sy(96f), px(18f), "Косметика")
                .style(UiFontStyle.SEMIBOLD)
                .color(Theme.Colors.PRIMARY_BRIGHT)
                .draw();
        Render2DUtil.text(sx(32f), sy(120f), px(11f), "Скоро: выбор скинов, мечей и аксессуаров")
                .style(UiFontStyle.REGULAR)
                .color(Theme.Colors.SECONDARY)
                .draw();
    }

    @Override
    public boolean handleClick(int mx, int my) { return false; }
    public void handleScroll(int mx, int my, double v) {}
    public boolean handleKey(int key) { return false; }
    public boolean handleCharacter(int cp) { return false; }
}
