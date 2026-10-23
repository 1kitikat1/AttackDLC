package attack.dlc.attributes.value;

import attack.dlc.animation.value.AnimatedTransform;
import attack.dlc.renderer.MeasureContext;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TransformValue {
   @Nullable
   static TransformValue derive(@Nullable TransformValue current, @Nullable TransformValue other) {
      if (other == null) {
         return current;
      } else if (current == null) {
         return other;
      } else {
         return other instanceof AnimatedTransform ? ((AnimatedTransform)other).derive(current) : other;
      }
   }

   @NotNull
   AffineTransform get(@NotNull MeasureContext var1);
}
