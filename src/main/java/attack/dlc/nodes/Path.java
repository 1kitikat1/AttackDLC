package attack.dlc.nodes;

import attack.dlc.attributes.FillRule;
import attack.dlc.geometry.AWTSVGShape;
import attack.dlc.geometry.SVGShape;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.util.PathUtil;
import java.awt.Rectangle;
import org.jetbrains.annotations.NotNull;

@ElementCategories({Category.Graphic, Category.Shape})
@PermittedContent(categories = {Category.Animation, Category.Descriptive})
public final class Path extends ShapeNode {
   public static final String TAG = "path";

   @NotNull
   @Override
   public String tagName() {
      return "path";
   }

   @NotNull
   @Override
   protected SVGShape buildShape(@NotNull AttributeNode attributeNode) {
      String pathValue = attributeNode.getValue("d");
      return pathValue == null ? new AWTSVGShape<>(new Rectangle()) : PathUtil.parseFromPathData(pathValue, FillRule.Nonzero);
   }

   @Override
   protected boolean shouldPaintStartEndMarkersInMiddle() {
      return false;
   }
}

