package attack.dlc.nodes.animation;

import attack.dlc.nodes.MetaSVGNode;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.Animation)
@PermittedContent(categories = Category.Descriptive)
public final class Set extends MetaSVGNode {
   public static final String TAG = "set";

   @NotNull
   @Override
   public String tagName() {
      return "set";
   }
}

