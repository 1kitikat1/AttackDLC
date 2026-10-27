package attack.dlc.nodes.prototype;

import attack.dlc.renderer.impl.context.PaintContext;
import org.jetbrains.annotations.NotNull;

public interface HasPaintContext {
   @NotNull
   Mutator<PaintContext> paintContext();
}
