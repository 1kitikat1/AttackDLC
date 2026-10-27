package attack.dlc.geometry;

import attack.dlc.attributes.value.Value;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.impl.context.RenderContextAccessor;
import java.awt.geom.Path2D;
import org.jetbrains.annotations.NotNull;

public final class FillRuleAwareAWTSVGShape extends AWTSVGShape<Path2D> {
   public FillRuleAwareAWTSVGShape(@NotNull Value<Path2D> shape) {
      super(shape);
   }

   @NotNull
   public Path2D shape(@NotNull RenderContext context, boolean validate) {
      Path2D shape = (Path2D)super.shape(context, validate);
      shape.setWindingRule(RenderContextAccessor.instance().fillRule(context).awtWindingRule);
      return shape;
   }
}

