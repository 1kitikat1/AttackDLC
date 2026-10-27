package attack.dlc.parser.resources.impl;

import attack.dlc.SVGDocument;
import attack.dlc.parser.resources.RenderableResource;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.output.Output;
import attack.dlc.view.FloatSize;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;

public class SVGResource implements RenderableResource {
   @NotNull
   private final SVGDocument document;

   public SVGResource(@NotNull SVGDocument document) {
      this.document = document;
   }

   @NotNull
   @Override
   public FloatSize intrinsicSize(@NotNull RenderContext context) {
      return this.document.size();
   }

   @Override
   public void render(@NotNull Output output, @NotNull RenderContext context, @NotNull AffineTransform imgTransform) {
      output.applyTransform(imgTransform);
      this.document.renderWithPlatform(context.platformSupport(), output, null);
   }
}

