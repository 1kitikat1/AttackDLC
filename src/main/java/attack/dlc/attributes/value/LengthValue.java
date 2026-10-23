package attack.dlc.attributes.value;

import attack.dlc.animation.value.AnimatedLength;
import attack.dlc.renderer.MeasureContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LengthValue {
   @Nullable
   static LengthValue derive(@Nullable LengthValue current, @Nullable LengthValue other) {
      if (other == null) {
         return current;
      } else if (current == null) {
         return other;
      } else {
         return other instanceof AnimatedLength ? ((AnimatedLength)other).derive(current) : other;
      }
   }

   boolean isConstantlyZero();

   boolean isConstantlyNonNegative();

   float resolve(@NotNull MeasureContext var1);
}
