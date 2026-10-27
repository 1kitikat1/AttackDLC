package org.attackdlc.menu.ui.rows;

import org.attackdlc.feature.setting.BooleanSetting;
import org.attackdlc.feature.setting.ButtonSetting;
import org.attackdlc.feature.setting.ColorSetting;
import org.attackdlc.feature.setting.InputBindSetting;
import org.attackdlc.feature.setting.ModeSetting;
import org.attackdlc.feature.setting.MultiSelectSetting;
import org.attackdlc.feature.setting.NumberSetting;
import org.attackdlc.feature.setting.Setting;
import org.attackdlc.feature.setting.TextSetting;

public final class SettingRows {
   private SettingRows() {
   }

   public static SettingRow create(String featureName, Setting<?> setting) {
      SettingRow row = switch (setting) {
         case BooleanSetting booleanSetting -> new ToggleRow(booleanSetting);
         case ButtonSetting buttonSetting -> new ButtonRow(buttonSetting);
         case NumberSetting numberSetting -> new SliderRow(numberSetting);
         case ModeSetting modeSetting -> new DropdownRow(modeSetting);
         case MultiSelectSetting multiSelectSetting -> new DropdownRow(multiSelectSetting);
         case ColorSetting colorSetting -> new ColorRow(colorSetting);
         case TextSetting textSetting -> new TextRow(textSetting);
         case InputBindSetting inputBindSetting -> new BindChipRow(inputBindSetting);
         default -> null;
      };
      return row == null ? null : row.context(featureName);
   }
}

