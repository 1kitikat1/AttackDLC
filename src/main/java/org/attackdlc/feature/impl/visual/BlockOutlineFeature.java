package org.attackdlc.feature.impl.visual;

import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.feature.FeatureManager;
import org.attackdlc.feature.setting.BooleanSetting;
import org.attackdlc.feature.setting.ColorSetting;
import org.attackdlc.feature.setting.NumberSetting;
import sdk.api.optimize.optimize;

@optimize
public final class BlockOutlineFeature extends Feature {
   public final ColorSetting auroraColor = this.register(new ColorSetting("Aurora Color", -14028140).configKey("render.blockoutline.auroraColor"));
   public final ColorSetting secondaryColor = this.register(new ColorSetting("Secondary Color", -7579137).configKey("render.blockoutline.secondaryColor"));
   public final ColorSetting starColor = this.register(new ColorSetting("Star Color", -3022593).configKey("render.blockoutline.starColor"));
   public final BooleanSetting ignoreDepth = this.register(new BooleanSetting("Ignore Depth", false).configKey("render.blockoutline.ignoreDepth"));
   public final NumberSetting animationSpeed = this.register(
      new NumberSetting("Animation Speed", 15.0, 1.0, 30.0, 1.0, "").configKey("render.blockoutline.animationSpeed")
   );
   public final NumberSetting transitionSpeed = this.register(
      new NumberSetting("Transition Speed", 10.0, 1.0, 30.0, 0.5, "").configKey("render.blockoutline.transitionSpeed")
   );
   public final NumberSetting shaderSpeed = this.register(
      new NumberSetting("Shader Speed", 1.0, 0.1, 3.0, 0.05, "x").configKey("render.blockoutline.shaderSpeed")
   );
   public final NumberSetting shaderIntensity = this.register(
      new NumberSetting("Shader Intensity", 1.5, 0.1, 3.0, 0.05, "x").configKey("render.blockoutline.shaderIntensity")
   );

   public BlockOutlineFeature() {
      super("BlockOutline", "Night aurora shader over the selected block", FeatureCategory.VISUAL, -1);
   }

   public static BlockOutlineFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(BlockOutlineFeature.class);
   }

   public boolean usesShader() {
      return true;
   }
}

