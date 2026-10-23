package attack.dlc.nodes;

import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import org.jetbrains.annotations.NotNull;

@ElementCategories({})
@PermittedContent(categories = Category.Descriptive)
public final class View extends MetaSVGNode {
   public static final String TAG = "view";

   @NotNull
   @Override
   public String tagName() {
      return "view";
   }
}

