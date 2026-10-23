package attack.dlc.paint.impl;

import attack.dlc.paint.SVGPaint;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.output.Output;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class NonePaint implements SVGPaint {
   @Override
   public void fillShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
   }

   @Override
   public void drawShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
   }

   @Override
   public String toString() {
      return "SVGPaint.None";
   }
}

