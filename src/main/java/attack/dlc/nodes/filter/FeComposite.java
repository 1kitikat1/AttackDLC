package attack.dlc.nodes.filter;

import attack.dlc.nodes.animation.Animate;
import attack.dlc.nodes.animation.Set;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.parser.impl.AttributeNode;
import java.awt.Composite;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.FilterPrimitive)
@PermittedContent(anyOf = {Animate.class, Set.class})
public final class FeComposite extends AbstractCompositeFilterPrimitive {
   public static final String TAG = "fecomposite";
   private CompositeModeComposite composite;

   @NotNull
   @Override
   public String tagName() {
      return "fecomposite";
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.composite = new CompositeModeComposite(attributeNode);
   }

   @NotNull
   @Override
   protected Composite composite() {
      return this.composite.composite();
   }
}

