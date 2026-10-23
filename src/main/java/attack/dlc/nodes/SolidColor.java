package attack.dlc.nodes;

import attack.dlc.geometry.size.Percentage;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.paint.SimplePaintSVGPaint;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.util.ColorUtil;
import java.awt.Color;
import java.awt.Paint;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.Gradient)
@PermittedContent(categories = {Category.Animation, Category.Descriptive})
public final class SolidColor extends AbstractSVGNode implements SimplePaintSVGPaint {
   public static final String TAG = "solidcolor";
   private Color color;

   @NotNull
   @Override
   public String tagName() {
      return "solidcolor";
   }

   @NotNull
   @Override
   public Paint paint() {
      return this.color;
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      Color c = attributeNode.getColor("solid-color");
      float opacity = attributeNode.getPercentage("solid-opacity", new Percentage(c.getAlpha() / 255.0F)).value();
      this.color = ColorUtil.withAlpha(c, opacity);
   }
}

