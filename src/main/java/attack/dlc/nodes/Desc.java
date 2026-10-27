package attack.dlc.nodes;

import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.Descriptive)
@PermittedContent(any = true, charData = true)
public final class Desc extends MetaSVGNode {
   public static final String TAG = "desc";

   @NotNull
   @Override
   public String tagName() {
      return "desc";
   }
}

