package attack.dlc.nodes;

import attack.dlc.attributes.Animatable;
import attack.dlc.attributes.Inherited;
import attack.dlc.attributes.value.LengthValue;
import attack.dlc.attributes.value.PercentageDimension;
import attack.dlc.geometry.SVGRectangle;
import attack.dlc.geometry.SVGRoundRectangle;
import attack.dlc.geometry.SVGShape;
import attack.dlc.geometry.size.Length;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.util.AttributeUtil;
import org.jetbrains.annotations.NotNull;

@ElementCategories({Category.BasicShape, Category.Graphic, Category.Shape})
@PermittedContent(categories = {Category.Animation, Category.Descriptive})
public final class Rect extends ShapeNode {
   public static final String TAG = "rect";

   @NotNull
   @Override
   public String tagName() {
      return "rect";
   }

   @NotNull
   @Override
   protected SVGShape buildShape(@NotNull AttributeNode node) {
      LengthValue x = node.getLength("x", PercentageDimension.WIDTH, Length.ZERO, Inherited.NO, Animatable.YES);
      LengthValue y = node.getLength("y", PercentageDimension.HEIGHT, Length.ZERO, Inherited.NO, Animatable.YES);
      LengthValue width = node.getLength("width", PercentageDimension.WIDTH, Length.ZERO, Inherited.NO, Animatable.YES);
      LengthValue height = node.getLength("height", PercentageDimension.HEIGHT, Length.ZERO, Inherited.NO, Animatable.YES);
      AttributeUtil.AxisPair radius = AttributeUtil.parseAxisPair(node, "rx", "ry", Length.ZERO, Inherited.NO, v -> !v.isConstantlyNonNegative() ? null : v);
      LengthValue rx = radius.xAxis();
      LengthValue ry = radius.yAxis();
      boolean isNonRounded = rx.isConstantlyZero() && ry.isConstantlyZero();
      return isNonRounded ? new SVGRectangle(x, y, width, height) : new SVGRoundRectangle(x, y, width, height, rx, ry);
   }
}

