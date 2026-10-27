package attack.dlc.animation.interpolation;

import attack.dlc.attributes.transform.TransformPart;
import attack.dlc.attributes.value.TransformValue;
import attack.dlc.renderer.MeasureContext;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;

public interface TransformInterpolator {
   @NotNull
   AffineTransform interpolate(@NotNull MeasureContext var1, @NotNull TransformValue var2, @NotNull TransformPart var3, @NotNull TransformPart var4, float var5);
}
