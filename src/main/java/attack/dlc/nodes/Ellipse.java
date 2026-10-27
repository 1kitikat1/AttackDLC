package attack.dlc.nodes;

import attack.dlc.attributes.Animatable;
import attack.dlc.attributes.Inherited;
import attack.dlc.attributes.value.LengthValue;
import attack.dlc.attributes.value.PercentageDimension;
import attack.dlc.geometry.SVGEllipse;
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
public final class Ellipse extends ShapeNode {
   public static final String TAG = "ellipse";

   @NotNull
   @Override
   public String tagName() {
      return "ellipse";
   }

   @NotNull
   @Override
   protected SVGShape buildShape(@NotNull AttributeNode node) {
      AttributeUtil.AxisPair radius = AttributeUtil.parseAxisPair(node, "rx", "ry", Length.ZERO, Inherited.NO, v -> !v.isConstantlyNonNegative() ? null : v);
      LengthValue rx = radius.xAxis();
      LengthValue ry = radius.yAxis();
      return new SVGEllipse(
         node.getLength("cx", PercentageDimension.WIDTH, Length.ZERO, Inherited.NO, Animatable.YES),
         node.getLength("cy", PercentageDimension.HEIGHT, Length.ZERO, Inherited.NO, Animatable.YES),
         rx,
         ry
      );
   }
}

