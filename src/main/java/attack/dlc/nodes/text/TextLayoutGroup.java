package attack.dlc.nodes.text;

import attack.dlc.geometry.size.Length;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.output.Output;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

interface TextLayoutGroup {
   @NotNull
   List<? extends TextSegment> segments();

   @NotNull
   Point2D renderText(@Nullable Point2D var1, @NotNull RenderContext var2, @NotNull Output var3);

   @NotNull
   Point2D appendGlyphShape(@Nullable Point2D var1, @NotNull RenderContext var2, @NotNull Path2D var3);

   @Nullable
   Length fixedLength();
}
