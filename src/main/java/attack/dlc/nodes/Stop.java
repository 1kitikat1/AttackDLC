package attack.dlc.nodes;

import attack.dlc.geometry.path.BezierPathCommand;
import attack.dlc.geometry.path.PathParser;
import attack.dlc.geometry.size.Percentage;
import attack.dlc.nodes.animation.Animate;
import attack.dlc.nodes.animation.Set;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.parser.PaintParser;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.util.ColorUtil;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(Category.Gradient)
@PermittedContent(anyOf = {Animate.class, Set.class})
public final class Stop extends AbstractSVGNode {
   public static final String TAG = "stop";
   @NotNull
   private Color color = PaintParser.DEFAULT_COLOR;
   private Percentage offset;
   @Nullable
   private BezierPathCommand path;

   @NotNull
   @Override
   public String tagName() {
      return "stop";
   }

   @NotNull
   public Color color() {
      return this.color;
   }

   @NotNull
   public Percentage offset() {
      return this.offset;
   }

   @Nullable
   public BezierPathCommand bezierCommand() {
      return this.path;
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      Color c = attributeNode.getColor("stop-color");
      float opacity = attributeNode.getPercentage("stop-opacity", new Percentage(c.getAlpha() / 255.0F)).value();
      this.color = ColorUtil.withAlpha(c, opacity);
      this.offset = attributeNode.getPercentage("offset", Percentage.ZERO);
      String pathData = attributeNode.getValue("path");
      this.path = pathData != null ? new PathParser(pathData).parseMeshCommand() : null;
   }

   @Override
   public String toString() {
      return "Stop{color=" + this.color + ", offset=" + this.offset + '}';
   }
}

