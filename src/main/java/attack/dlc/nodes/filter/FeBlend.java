package attack.dlc.nodes.filter;

import attack.dlc.attributes.filter.BlendMode;
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
public final class FeBlend extends AbstractCompositeFilterPrimitive {
   public static final String TAG = "feblend";
   private Composite composite;

   @NotNull
   @Override
   public String tagName() {
      return "feblend";
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      BlendMode blendMode = attributeNode.getEnum("mode", BlendMode.Normal);
      this.composite = BlendModeComposite.create(blendMode);
   }

   @NotNull
   @Override
   protected Composite composite() {
      return this.composite;
   }
}

