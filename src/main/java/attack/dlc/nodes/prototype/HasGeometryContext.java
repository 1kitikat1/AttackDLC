package attack.dlc.nodes.prototype;

import attack.dlc.attributes.Coordinate;
import attack.dlc.attributes.transform.TransformBox;
import attack.dlc.attributes.value.LengthValue;
import attack.dlc.attributes.value.TransformValue;
import attack.dlc.nodes.ClipPath;
import attack.dlc.nodes.Mask;
import attack.dlc.nodes.filter.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HasGeometryContext extends Transformable, HasClip, HasFilter {
   interface ByDelegate extends HasGeometryContext {
      @NotNull
      HasGeometryContext geometryContextDelegate();

      @Nullable
      @Override
      default ClipPath clipPath() {
         return this.geometryContextDelegate().clipPath();
      }

      @Nullable
      @Override
      default Mask mask() {
         return this.geometryContextDelegate().mask();
      }

      @Nullable
      @Override
      default Filter filter() {
         return this.geometryContextDelegate().filter();
      }

      @Nullable
      @Override
      default TransformValue transform() {
         return this.geometryContextDelegate().transform();
      }

      @Override
      default TransformBox transformBox() {
         return this.geometryContextDelegate().transformBox();
      }

      @NotNull
      @Override
      default Coordinate<LengthValue> transformOrigin() {
         return this.geometryContextDelegate().transformOrigin();
      }
   }
}
