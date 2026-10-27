package org.attackdlc.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.attackdlc.menu.TitleScreenRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Кастомное главное меню AttackDLC.
 *
 * <p>Ванильные кнопки скрываются, вместо них рисуется собственное меню
 * ({@link TitleScreenRenderer}). Логотип и сплэш Minecraft отключаются.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    private static final List<AbstractButton> ATTACKDLC$BUTTONS = new ArrayList<>();

    @Inject(method = "init", at = @At("RETURN"))
    private void attackdlc$setupButtons(CallbackInfo ci) {
        ATTACKDLC$BUTTONS.clear();
        TitleScreen screen = (TitleScreen) (Object) this;
        for (var child : screen.children()) {
            if (child instanceof AbstractButton button) {
                button.visible = false;
                ATTACKDLC$BUTTONS.add(button);
            }
        }
        TitleScreenRenderer.resetOpenAnimation();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void attackdlc$renderMenu(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        TitleScreenRenderer.render(context, screen.width, screen.height, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void attackdlc$handleClick(MouseButtonEvent event, boolean doubleClick,
            CallbackInfoReturnable<Boolean> ci) {
        if (event.button() != 0) {
            return;
        }
        // Меню полностью перекрывает ванильные кнопки — любой клик поглощаем.
        TitleScreenRenderer.handleClick(event.x(), event.y());
        ci.setReturnValue(true);
    }

    @Redirect(method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)V"))
    private void attackdlc$hideVersion(GuiGraphicsExtractor context, net.minecraft.client.gui.Font font,
            String text, int x, int y, int color) {
    }

    @Redirect(method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LogoRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V"))
    private void attackdlc$hideLogo(LogoRenderer renderer, GuiGraphicsExtractor context, int width, float alpha) {
    }

    @Redirect(method = "extractRenderState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/SplashRenderer;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;ILnet/minecraft/client/gui/Font;F)V"))
    private void attackdlc$hideSplash(SplashRenderer splash, GuiGraphicsExtractor context, int width,
            net.minecraft.client.gui.Font font, float alpha) {
    }
}
