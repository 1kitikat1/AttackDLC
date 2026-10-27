package attack.dlc.attributes.value;

import attack.dlc.animation.value.AnimatedPercentage;
import attack.dlc.renderer.MeasureContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PercentageValue {
   @Nullable
   static PercentageValue derive(@Nullable PercentageValue current, @Nullable PercentageValue other) {
      if (other == null) {
         return current;
      } else if (current == null) {
         return other;
      } else {
         return other instanceof AnimatedPercentage ? ((AnimatedPercentage)other).derive(current) : other;
      }
   }

   float get(@NotNull MeasureContext var1);

   @NotNull
   PercentageValue multiply(@NotNull PercentageValue var1);
}
