package attack.dlc.nodes;

import attack.dlc.nodes.container.CommonRenderableContainerNode;
import attack.dlc.nodes.filter.Filter;
import attack.dlc.nodes.prototype.ShapedContainer;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.nodes.text.Text;
import org.jetbrains.annotations.NotNull;

@ElementCategories({Category.Container, Category.Structural})
@PermittedContent(
   categories = {Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient},
   anyOf = {Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class}
)
public final class Group extends CommonRenderableContainerNode implements ShapedContainer<SVGNode> {
   public static final String TAG = "g";

   @NotNull
   @Override
   public String tagName() {
      return "g";
   }
}

