package org.attackdlc.feature.impl.visual;

import java.util.Locale;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.feature.setting.ModeSetting;
import org.attackdlc.feature.setting.NumberSetting;
import sdk.api.optimize.optimize;

@optimize
public final class SwingAnimationFeature extends Feature {
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Slice", "Slice", "Spiral", "Thrust", "Spear"));
   public final NumberSetting speed = this.register(new NumberSetting("Speed", 6.0, 1.0, 20.0, 1.0, ""));

   public SwingAnimationFeature() {
      super("SwingAnimation", "Custom hand swing animation", FeatureCategory.VISUAL, -1);
   }

   public SwingAnimationFeature.Style style() {
      return SwingAnimationFeature.Style.valueOf(this.mode.getValue().toUpperCase(Locale.ROOT));
   }

   public int swingDurationTicks() {
      return this.speed.getValue().intValue();
   }

   public enum Style {
      SLICE,
      SPIRAL,
      THRUST,
      SPEAR;

      // $VF: synthetic method
      private static SwingAnimationFeature.Style[] $values() {
         return new SwingAnimationFeature.Style[]{SLICE, SPIRAL, THRUST, SPEAR};
      }
   }
}

