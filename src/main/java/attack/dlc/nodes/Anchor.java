package attack.dlc.nodes;

import attack.dlc.nodes.container.CommonRenderableContainerNode;
import attack.dlc.nodes.filter.Filter;
import attack.dlc.nodes.prototype.ShapedContainer;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.nodes.text.Text;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.Container)
@PermittedContent(
   categories = {Category.Container, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient, Category.Animation},
   anyOf = {Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class}
)
public final class Anchor extends CommonRenderableContainerNode implements ShapedContainer<SVGNode> {
   public static final String TAG = "a";

   @NotNull
   @Override
   public String tagName() {
      return "a";
   }
}

