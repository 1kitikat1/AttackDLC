package attack.dlc.nodes.prototype;

import attack.dlc.nodes.SVGNode;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.impl.context.ContextElementAttributes;
import org.jetbrains.annotations.NotNull;

public interface Instantiator {
   boolean canInstantiate(@NotNull SVGNode var1);

   @NotNull
   default ContextElementAttributes createContextAttributes(@NotNull RenderContext context) {
      return new ContextElementAttributes(context.fillPaint(), context.strokePaint());
   }
}
