package attack.dlc.animation.value;

import attack.dlc.attributes.value.Value;
import attack.dlc.renderer.MeasureContext;
import attack.dlc.util.PathUtil;
import java.awt.geom.Path2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AnimatedPath implements Value<Path2D> {
   @NotNull
   private final AnimatedFloatList list;
   @Nullable
   private Path2D cache;
   private final boolean closed;

   public AnimatedPath(@NotNull AnimatedFloatList list, boolean closed) {
      this.list = list;
      this.closed = closed;
   }

   @NotNull
   public Path2D get(@NotNull MeasureContext context) {
      if (this.cache == null || this.list.isDirty(context.timestamp())) {
         this.cache = PathUtil.setPolyLine(this.cache, this.list.get(context), this.closed);
      }

      return this.cache;
   }
}

