package org.attackdlc.utils.render;

import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.math.MathUtil;
import sdk.api.optimize.optimize;

@optimize
public final class Theme {
    private static int accentIndex;
    private static int customAccent = -1;
    private static boolean customAccentActive;

    public static int getAccent() {
        return customAccentActive ? customAccent : Theme.Colors.ACCENTS[accentIndex];
    }

    public static int accent(int index) {
        return Theme.Colors.ACCENTS[MathUtil.clamp(index, 0, Theme.Colors.ACCENTS.length - 1)];
    }

    public static int accentCount() {
        return Theme.Colors.ACCENTS.length;
    }

    public static void setAccentIndex(int index) {
        accentIndex = MathUtil.clamp(index, 0, Theme.Colors.ACCENTS.length - 1);
        customAccent = -1;
        customAccentActive = false;
    }

    public static int accentIndex() {
        return accentIndex;
    }

    public static void setCustomAccent(int color) {
        customAccent = ColorUtil.withAlpha(color, 255);
        customAccentActive = true;
    }

    public static void clearCustomAccent() {
        customAccent = -1;
        customAccentActive = false;
    }

    public static boolean isCustomAccent() {
        return customAccentActive;
    }

    public static void saveAccentPreset(int index, int color) {
        Theme.Colors.ACCENTS[MathUtil.clamp(index, 0, Theme.Colors.ACCENTS.length - 1)] = ColorUtil.withAlpha(color, 255);
        accentIndex = MathUtil.clamp(index, 0, Theme.Colors.ACCENTS.length - 1);
        customAccentActive = false;
    }

    /** Глубокий тёмный минимализм: чёрный уголь, мягкие разделители, спокойные акценты. */
    public static final class Colors {
        // Фон — глубокий чёрно-угольный
        public static final int PANEL = ColorUtil.rgba(8, 9, 12, 248);
        public static final int BACKGROUND_PRIMARY_50 = ColorUtil.rgba(7, 8, 11, 244);
        // Рамка панели — тонкий волосок
        public static final int PANEL_BORDER = ColorUtil.rgba(255, 255, 255, 14);
        public static final int PANEL_SHADOW = ColorUtil.rgba(0, 0, 0, 130);
        public static final int PANEL_SHADOW_STRONG = ColorUtil.rgba(0, 0, 0, 170);
        public static final int POPUP_SHADOW = ColorUtil.rgba(0, 0, 0, 160);
        public static final int OVERLAY = ColorUtil.rgba(3, 4, 6, 175);
        // Карточки — почти невидимые, без рамок
        public static final int CARD = ColorUtil.rgba(255, 255, 255, 5);
        public static final int CARD_HOVER = ColorUtil.rgba(255, 255, 255, 9);
        public static final int MODULE_CARD = ColorUtil.rgba(255, 255, 255, 4);
        public static final int MODULE_CARD_HOVER = ColorUtil.rgba(255, 255, 255, 8);
        // Поверхности
        public static final int BACKGROUND_SURFACE_S = ColorUtil.rgba(255, 255, 255, 6);
        public static final int BACKGROUND_SURFACE_M = ColorUtil.rgba(255, 255, 255, 4);
        // Контролы
        public static final int CONTROL = ColorUtil.rgba(255, 255, 255, 7);
        public static final int CONTROL_ALT = ColorUtil.rgba(255, 255, 255, 7);
        public static final int CONTROL_STRONG = ColorUtil.rgba(255, 255, 255, 11);
        public static final int CONTROL_HOVER = ColorUtil.rgba(255, 255, 255, 15);
        public static final int CONTROL_IDLE = ColorUtil.rgba(255, 255, 255, 3);
        // Обводки
        public static final int OUTLINES_SMALL = ColorUtil.rgba(255, 255, 255, 8);
        public static final int OUTLINES_MEDIUM = ColorUtil.rgba(255, 255, 255, 12);
        public static final int OUTLINES_LARGE = ColorUtil.rgba(255, 255, 255, 16);
        // Системные
        public static final int SYSTEM_INFORMATION = ColorUtil.rgb(0, 145, 255);
        public static final int SYSTEM_RED = ColorUtil.rgb(237, 106, 95);
        public static final int CONTROL_SHADOW = ColorUtil.rgba(0, 0, 0, 60);
        // Текст
        public static final int TEXT_TITLE = -1;
        public static final int TEXT_TEXT = ColorUtil.rgb(226, 228, 234);
        public static final int TEXT = ColorUtil.rgb(238, 240, 244);
        public static final int PRIMARY = ColorUtil.rgb(242, 244, 248);
        public static final int PRIMARY_BRIGHT = ColorUtil.rgb(252, 252, 255);
        public static final int SECONDARY = ColorUtil.rgb(140, 144, 156);
        public static final int SECONDARY_DARK = ColorUtil.rgb(100, 104, 114);
        public static final int SECTION = ColorUtil.rgb(88, 92, 102);
        // Разделители
        public static final int DIVIDER = ColorUtil.rgba(255, 255, 255, 20);
        public static final int DIVIDER_SUBTLE = ColorUtil.rgba(255, 255, 255, 10);
        public static final int DIVIDER_HEADER = ColorUtil.rgba(255, 255, 255, 10);
        public static final int SEPARATOR = ColorUtil.rgba(255, 255, 255, 12);
        public static final int SURFACE_ACTIVE = ColorUtil.rgba(255, 255, 255, 22);
        public static final int SURFACE_HOVER = ColorUtil.rgba(255, 255, 255, 8);
        public static final int FOCUS = ColorUtil.rgba(255, 255, 255, 100);
        // Иконки
        public static final int ICON = ColorUtil.rgb(130, 134, 146);
        public static final int ICON_MUTED = ColorUtil.rgb(178, 182, 192);
        public static final int ICON_GHOST = ColorUtil.rgb(68, 72, 82);
        public static final int TEXT_GHOST = ICON_GHOST;
        // Трафик
        public static final int TRAFFIC_CLOSE = ColorUtil.rgb(237, 106, 95);
        public static final int TRAFFIC_MINIMIZE = ColorUtil.rgb(245, 191, 79);
        public static final int TRAFFIC_MAXIMIZE = ColorUtil.rgb(97, 197, 84);
        public static final int RISK = TRAFFIC_MINIMIZE;
        public static final int EXTRA_RISK = TRAFFIC_CLOSE;
        // Док
        public static final int DOCK = ColorUtil.rgba(14, 15, 20, 235);
        public static final int DOCK_BORDER = ColorUtil.rgba(255, 255, 255, 24);
        public static final int DOCK_SHADOW = ColorUtil.rgba(0, 0, 0, 85);
        // Акценты — спокойные, приглушённые
        public static final int[] ACCENTS = new int[]{
            ColorUtil.rgb(135, 160, 255),  // soft periwinkle
            ColorUtil.rgb(178, 186, 200),  // steel
            ColorUtil.rgb(190, 162, 225),  // lilac
            ColorUtil.rgb(152, 198, 182),  // sage
            ColorUtil.rgb(222, 190, 140),  // sand
            ColorUtil.rgb(216, 155, 168),  // rose
            ColorUtil.rgb(142, 185, 220),  // sky
            ColorUtil.rgb(202, 198, 186),  // bone
            ColorUtil.rgb(162, 206, 206),  // ice
            ColorUtil.rgb(194, 172, 214)   // mauve
        };

        private Colors() {
        }
    }

    public static final class Sizes {
        public static final int HEADER_HEIGHT = 52;
        public static final int DEFAULT_PANEL_RADIUS_INDEX = 1;
        public static final int[] PANEL_RADII = new int[]{6, 10, 18};
        public static final int MODULE_CARD_RADIUS = 7;
        public static final int MODULE_CARD_HEADER_HEIGHT = 60;
        public static final int MODULE_CARD_ROW_HEIGHT = 38;
        public static final int MODULE_CARD_PADDING = 14;
        public static final int MODULE_CARD_SWITCH_WIDTH = 34;
        public static final int MODULE_CARD_SWITCH_HEIGHT = 14;
        public static final int MODULE_CARD_ACTION_ICON = 11;
        public static final int SLIDER_WIDTH = 120;
        public static final int SLIDER_HEIGHT = 14;
        public static final int SLIDER_PADDING = 2;
        public static final int SLIDER_TRACK_HEIGHT = 10;
        public static final int SLIDER_KNOB_SIZE = 13;
        public static final float SLIDER_SHADOW_BLUR = 7.0F;
        public static final int MODULE_CARD_SLIDER_WIDTH = 120;
        public static final int MODULE_CARD_SLIDER_HEIGHT = 14;
        public static final int COLOR_PREVIEW_WIDTH = 22;
        public static final int COLOR_PREVIEW_HEIGHT = 14;
        public static final int COLOR_PREVIEW_PADDING = 2;
        public static final int COLOR_PREVIEW_RADIUS = 5;
        public static final int COLOR_BLOCK_RADIUS = 3;
        public static final int COLOR_PICKER_WIDTH = 136;
        public static final int COLOR_PICKER_HEIGHT = 116;
        public static final int COLOR_PICKER_PADDING = 5;
        public static final int COLOR_PICKER_RADIUS = 11;
        public static final int COLOR_PICKER_GRID_WIDTH = 124;
        public static final int COLOR_PICKER_GRID_HEIGHT = 104;
        public static final int COLOR_PICKER_GRID_RADIUS = 9;
        public static final int COLOR_PICKER_CELL_SIZE = 10;
        public static final int COLOR_PICKER_COLUMNS = 12;
        public static final int COLOR_PICKER_OFFSET_Y = 3;
        public static final int DROPDOWN_VALUE_WIDTH = 104;
        public static final int DROPDOWN_VALUE_HEIGHT = 22;
        public static final int DROPDOWN_GAP = 3;
        public static final int DROPDOWN_ICON_SIZE = 22;
        public static final int DROPDOWN_ICON_PADDING = 3;
        public static final int DROPDOWN_POPUP_WIDTH = 136;
        public static final int DROPDOWN_POPUP_RADIUS = 10;
        public static final int DROPDOWN_POPUP_PADDING = 5;
        public static final int DROPDOWN_POPUP_GAP = 2;
        public static final int DROPDOWN_POPUP_ITEM_HEIGHT = 30;
        public static final int DROPDOWN_POPUP_ITEM_RADIUS = 7;
        public static final int DROPDOWN_POPUP_TEXT_WIDTH = 100;
        public static final int DROPDOWN_POPUP_OFFSET_Y = 3;
        public static final int INPUT_BIND_WIDTH = 42;
        public static final int INPUT_BIND_HEIGHT = 18;
        public static final int INPUT_BIND_RADIUS = 3;
        public static final int INPUT_BIND_PADDING_X = 7;
        public static final int INPUT_BIND_TEXT_SIZE = 9;
        public static final int INPUT_WIDTH = 120;
        public static final int INPUT_HEIGHT = 22;
        public static final int INPUT_RADIUS = 3;
        public static final int INPUT_PADDING_X = 7;
        public static final int INPUT_PADDING_Y = 3;
        public static final int INPUT_TEXT_WIDTH = 104;
        public static final int INPUT_TEXT_SIZE = 9;
        public static final float HEADER_BRAND_WIDTH = 310.0F;
        public static final int HEADER_BRAND_HEIGHT = 30;
        public static final int HEADER_BRAND_RADIUS = 7;
        public static final int HEADER_CONTROLS_X = 14;
        public static final int HEADER_CONTROLS_Y = 11;
        public static final int HEADER_CONTROLS_HEIGHT = 30;
        public static final int HEADER_CONTROLS_GAP = 7;
        public static final int HEADER_TRAFFIC_SIZE = 11;
        public static final int HEADER_TRAFFIC_GAP = 9;
        public static final int HEADER_CONTROL_BUTTON_SIZE = 22;
        public static final int HEADER_CONTROL_BUTTON_PADDING = 3;
        public static final int HEADER_NAV_GROUP_WIDTH = 46;
        public static final int HEADER_ICON_SIZE = 15;
        public static final int HEADER_PROFILE_ICON_SIZE = 18;
        public static final int HEADER_DIVIDER_HEIGHT = 10;

        private Sizes() {
        }
    }
}
