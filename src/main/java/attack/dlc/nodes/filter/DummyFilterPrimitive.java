package attack.dlc.nodes.filter;

import attack.dlc.nodes.animation.Animate;
import attack.dlc.nodes.animation.Set;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.renderer.RenderContext;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.FilterPrimitive)
@PermittedContent(anyOf = {Animate.class, Set.class})
public final class DummyFilterPrimitive extends AbstractFilterPrimitive {
   @NotNull
   private final String tag;

   public DummyFilterPrimitive(@NotNull String tag) {
      this.tag = tag;
   }

   @NotNull
   @Override
   public String tagName() {
      return this.tag;
   }

   @Override
   public void layoutFilter(@NotNull RenderContext context, @NotNull FilterLayoutContext filterLayoutContext) {
      this.impl().saveLayoutResult(this.impl().layoutInput(filterLayoutContext), filterLayoutContext);
   }

   @Override
   public void applyFilter(@NotNull RenderContext context, @NotNull FilterContext filterContext) {
      this.impl().noop(filterContext);
   }
}

