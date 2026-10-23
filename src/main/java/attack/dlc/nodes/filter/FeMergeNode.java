package attack.dlc.nodes.filter;

import attack.dlc.attributes.filter.DefaultFilterChannel;
import attack.dlc.attributes.filter.FilterChannelKey;
import attack.dlc.nodes.AbstractSVGNode;
import attack.dlc.nodes.animation.Animate;
import attack.dlc.nodes.animation.Set;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.parser.impl.AttributeNode;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.None)
@PermittedContent(anyOf = {Animate.class, Set.class})
public final class FeMergeNode extends AbstractSVGNode {
   public static final String TAG = "feMergeNode";
   private FilterChannelKey inputChannel;

   @NotNull
   @Override
   public String tagName() {
      return "feMergeNode";
   }

   @NotNull
   public FilterChannelKey inputChannel() {
      return this.inputChannel;
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.inputChannel = attributeNode.getFilterChannelKey("in", DefaultFilterChannel.LastResult);
   }
}

