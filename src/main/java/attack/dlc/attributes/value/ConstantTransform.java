package attack.dlc.attributes.value;

import attack.dlc.renderer.MeasureContext;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;

public final class ConstantTransform implements TransformValue {
   @NotNull
   private final AffineTransform value;

   public ConstantTransform(@NotNull AffineTransform value) {
      this.value = value;
   }

   @NotNull
   @Override
   public AffineTransform get(@NotNull MeasureContext context) {
      return this.value;
   }
}

