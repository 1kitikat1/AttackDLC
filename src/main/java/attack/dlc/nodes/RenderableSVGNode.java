package attack.dlc.nodes;

import attack.dlc.nodes.prototype.HasGeometryContext;
import attack.dlc.nodes.prototype.Renderable;
import attack.dlc.nodes.prototype.impl.HasGeometryContextImpl;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.RenderContext;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public abstract class RenderableSVGNode extends AbstractSVGNode implements Renderable, HasGeometryContext.ByDelegate {
   private boolean isVisible;
   private HasGeometryContext geometryContext;

   @Override
   public boolean isVisible(@NotNull RenderContext context) {
      return this.isVisible && context.rawOpacity() > 0.0F;
   }

   @NotNull
   @Override
   public HasGeometryContext geometryContextDelegate() {
      return this.geometryContext;
   }

   @MustBeInvokedByOverriders
   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.isVisible = this.parseIsVisible(attributeNode);
      this.geometryContext = HasGeometryContextImpl.parse(attributeNode);
   }
}

