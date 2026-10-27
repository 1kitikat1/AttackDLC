package org.attackdlc.feature.impl.misc;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.feature.FeatureManager;
import org.attackdlc.hud.NotificationsElement;
import org.attackdlc.menu.core.MenuOverlay;
import org.attackdlc.utils.render.Textures;
import org.lwjgl.glfw.GLFW;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

/**
 * Panic — экстренное скрытие клиента.
 * <p>Мгновенно сворачивает окно, закрывает все меню, отключает модули и HUD.
 * Состояние НЕ сохраняется в конфиг (supportsBinds=false prevents save).
 */
@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class PanicFeature extends Feature {

    private static volatile boolean panicked = false;
    private static final Map<String, Boolean> savedStatesStatic = new HashMap<>();
    private boolean hudWasEnabled;

    public PanicFeature() {
        super("Panic", "Instantly hide the client — minimizes window and suppresses all cheats.",
              FeatureCategory.MISC, -1);
    }

    /** Panic не сохраняется в конфиг — это экстренная кнопка. */
    @Override
    public boolean persistsEnabledState() {
        return false;
    }

    /** Состояние фичи до паники (нужно, чтобы конфиг не «запомнил» выключенные модули). */
    public static Boolean savedEnabledState(String featureName) {
        if (!panicked) {
            return null;
        }
        return savedStatesStatic.get(featureName);
    }

    /** True когда паника активна. */
    public static boolean isPanicked() {
        return panicked;
    }

    @Override
    protected void onEnable() {
        panicked = true;

        // Сохраняем состояния
        savedStatesStatic.clear();
        for (Feature f : FeatureManager.INSTANCE.getFeatures()) {
            if (f != this) {
                savedStatesStatic.put(f.getName(), f.isEnabled());
            }
        }

        // Отключаем все фичи
        for (Feature f : FeatureManager.INSTANCE.getFeatures()) {
            if (f != this && f.isEnabled()) {
                f.setEnabled(false);
            }
        }

        // Скрываем HUD
        var hud = FeatureManager.INSTANCE.getFeature("HUD");
        if (hud != null && hud.isEnabled()) {
            hudWasEnabled = true;
            hud.setEnabled(false);
        }

        // Глушим Baritone: чат, уведомления и приём команд
        org.attackdlc.utils.BaritoneMute.mute();

        // Закрываем всё что можно
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gui != null) {
            // Чистим историю чата — там могли остаться ответы .bind / команды Baritone
            try { mc.gui.hud.getChat().clearMessages(true); } catch (Throwable ignored) {}
            try { MenuOverlay.discard(mc); } catch (Exception ignored) {}
            try {
                if (mc.gui.screen() != null) {
                    mc.gui.setScreen(null);
                }
            } catch (Exception ignored) {}
            try {
                long handle = mc.getWindow().handle();
                if (handle != 0L) {
                    GLFW.glfwIconifyWindow(handle);
                }
            } catch (Exception ignored) {}
        }

        try {
            NotificationsElement.notify(
                Textures.Icons.EYE,
                0xFFFF6B4A,
                "PANIC",
                "Клиент скрыт"
            );
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDisable() {
        panicked = false;

        // Возвращаем Baritone
        org.attackdlc.utils.BaritoneMute.restore();

        // Восстанавливаем фичи
        for (Feature f : FeatureManager.INSTANCE.getFeatures()) {
            if (f != this) {
                Boolean saved = savedStatesStatic.get(f.getName());
                if (saved != null && saved && !f.isEnabled()) {
                    f.setEnabled(true);
                }
            }
        }
        savedStatesStatic.clear();

        // Возвращаем HUD
        if (hudWasEnabled) {
            hudWasEnabled = false;
            var hud = FeatureManager.INSTANCE.getFeature("HUD");
            if (hud != null && !hud.isEnabled()) {
                hud.setEnabled(true);
            }
        }

        // Разворачиваем окно
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            try {
                long handle = mc.getWindow().handle();
                if (handle != 0L) {
                    GLFW.glfwRestoreWindow(handle);
                }
            } catch (Exception ignored) {}
        }

        try {
            NotificationsElement.notify(
                Textures.Icons.EYE,
                0xFFA0A8FF,
                "Panic off",
                "Восстановлено"
            );
        } catch (Exception ignored) {}
    }
}
