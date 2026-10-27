package org.attackdlc.utils;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;

/**
 * Временное «глушение» Baritone на время паники.
 * <p>Baritone выводит всё в чат через {@code Settings.logger}/{@code notifier}/{@code toaster},
 * а команды принимает через {@code chatControl}/{@code prefixControl}. Пока паника активна,
 * эти обработчики подменяются пустыми, а настройки выключаются — затем восстанавливаются.
 */
public final class BaritoneMute {

    private static boolean active;

    private static Consumer<Component> logger;
    private static BiConsumer<String, Boolean> notifier;
    private static BiConsumer<Component, Component> toaster;

    private static Boolean chatControl;
    private static Boolean chatControlAnyway;
    private static Boolean prefixControl;
    private static Boolean chatDebug;
    private static Boolean logAsToast;
    private static Boolean useMessageTag;
    private static Boolean desktopNotifications;
    private static String prefix;

    private BaritoneMute() {
    }

    /** Выключает весь чат-вывод и приём команд Baritone. */
    public static void mute() {
        if (active) {
            return;
        }
        try {
            Settings settings = BaritoneAPI.getSettings();

            logger = settings.logger.value;
            notifier = settings.notifier.value;
            toaster = settings.toaster.value;

            chatControl = settings.chatControl.value;
            chatControlAnyway = settings.chatControlAnyway.value;
            prefixControl = settings.prefixControl.value;
            chatDebug = settings.chatDebug.value;
            logAsToast = settings.logAsToast.value;
            useMessageTag = settings.useMessageTag.value;
            desktopNotifications = settings.desktopNotifications.value;
            prefix = settings.prefix.value;

            settings.logger.value = ignored -> {};
            settings.notifier.value = (a, b) -> {};
            settings.toaster.value = (a, b) -> {};

            settings.chatControl.value = false;
            settings.chatControlAnyway.value = false;
            settings.prefixControl.value = false;
            settings.chatDebug.value = false;
            settings.logAsToast.value = false;
            settings.useMessageTag.value = false;
            settings.desktopNotifications.value = false;

            active = true;
        } catch (Throwable ignored) {
            // Baritone недоступен — нечего глушить
        }
    }

    /** Возвращает всё как было. */
    public static void restore() {
        if (!active) {
            return;
        }
        active = false;
        try {
            Settings settings = BaritoneAPI.getSettings();

            if (logger != null) settings.logger.value = logger;
            if (notifier != null) settings.notifier.value = notifier;
            if (toaster != null) settings.toaster.value = toaster;

            if (chatControl != null) settings.chatControl.value = chatControl;
            if (chatControlAnyway != null) settings.chatControlAnyway.value = chatControlAnyway;
            if (prefixControl != null) settings.prefixControl.value = prefixControl;
            if (chatDebug != null) settings.chatDebug.value = chatDebug;
            if (logAsToast != null) settings.logAsToast.value = logAsToast;
            if (useMessageTag != null) settings.useMessageTag.value = useMessageTag;
            if (desktopNotifications != null) settings.desktopNotifications.value = desktopNotifications;
            if (prefix != null) settings.prefix.value = prefix;
        } catch (Throwable ignored) {
        }
        logger = null;
        notifier = null;
        toaster = null;
        chatControl = null;
        chatControlAnyway = null;
        prefixControl = null;
        chatDebug = null;
        logAsToast = null;
        useMessageTag = null;
        desktopNotifications = null;
        prefix = null;
    }

    public static boolean isMuted() {
        return active;
    }

    /** Префикс команд Baritone (обычно {@code #}). */
    public static String commandPrefix() {
        try {
            String value = BaritoneAPI.getSettings().prefix.value;
            return value == null || value.isEmpty() ? "#" : value;
        } catch (Throwable ignored) {
            return "#";
        }
    }
}
