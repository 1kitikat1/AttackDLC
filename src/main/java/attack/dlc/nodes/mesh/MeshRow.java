package attack.dlc.nodes.mesh;

import attack.dlc.nodes.Style;
import attack.dlc.nodes.container.ContainerNode;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import org.jetbrains.annotations.NotNull;

@ElementCategories({})
@PermittedContent(categories = Category.Descriptive, anyOf = {MeshPatch.class, Style.class})
public final class MeshRow extends ContainerNode {
   public static final String TAG = "meshrow";

   @NotNull
   @Override
   public String tagName() {
      return "meshrow";
   }
}

