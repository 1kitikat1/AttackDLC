package attack.dlc.parser.resources;

import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.output.Output;
import attack.dlc.view.FloatSize;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;

public interface RenderableResource {
   @NotNull
   FloatSize intrinsicSize(@NotNull RenderContext var1);

   void render(@NotNull Output var1, @NotNull RenderContext var2, @NotNull AffineTransform var3);
}
