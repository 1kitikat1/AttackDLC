package attack.dlc.nodes;

import attack.dlc.attributes.Animatable;
import attack.dlc.attributes.Inherited;
import attack.dlc.attributes.value.PercentageDimension;
import attack.dlc.geometry.SVGCircle;
import attack.dlc.geometry.SVGShape;
import attack.dlc.geometry.size.Length;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.parser.impl.AttributeNode;
import org.jetbrains.annotations.NotNull;

@ElementCategories({Category.BasicShape, Category.Graphic, Category.Shape})
@PermittedContent(categories = {Category.Animation, Category.Descriptive})
public final class Circle extends ShapeNode {
   public static final String TAG = "circle";

   @NotNull
   @Override
   public String tagName() {
      return "circle";
   }

   @NotNull
   @Override
   protected SVGShape buildShape(@NotNull AttributeNode attributeNode) {
      return new SVGCircle(
         attributeNode.getLength("cx", PercentageDimension.WIDTH, Length.ZERO, Inherited.NO, Animatable.YES),
         attributeNode.getLength("cy", PercentageDimension.HEIGHT, Length.ZERO, Inherited.NO, Animatable.YES),
         attributeNode.getLength("r", PercentageDimension.LENGTH, Length.ZERO, Inherited.NO, Animatable.YES)
      );
   }
}

