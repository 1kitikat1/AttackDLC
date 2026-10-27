package attack.dlc.geometry.path;

import attack.dlc.geometry.mesh.Bezier;
import java.awt.geom.Point2D.Float;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface BezierPathCommand {
   @NotNull
   Bezier createBezier(@NotNull Float var1);
}
