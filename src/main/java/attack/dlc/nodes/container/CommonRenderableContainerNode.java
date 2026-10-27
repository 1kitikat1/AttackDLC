package attack.dlc.nodes.container;

import attack.dlc.nodes.SVGNode;
import attack.dlc.nodes.prototype.HasContext;
import attack.dlc.nodes.prototype.HasGeometryContext;
import attack.dlc.nodes.prototype.Renderable;
import attack.dlc.nodes.prototype.impl.HasContextImpl;
import attack.dlc.nodes.prototype.impl.HasGeometryContextImpl;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.impl.NodeRenderer;
import attack.dlc.renderer.output.Output;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

public abstract class CommonRenderableContainerNode
   extends BaseContainerNode<SVGNode>
   implements Renderable,
   HasGeometryContext.ByDelegate,
   HasContext.ByDelegate {
   private final List<@NotNull SVGNode> children = new ArrayList<>();
   private boolean isVisible;
   private HasGeometryContext geometryContext;
   private HasContext context;

   @MustBeInvokedByOverriders
   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.isVisible = this.parseIsVisible(attributeNode);
      this.geometryContext = HasGeometryContextImpl.parse(attributeNode);
      this.context = HasContextImpl.parse(attributeNode);
   }

   @NotNull
   @Override
   public HasGeometryContext geometryContextDelegate() {
      return this.geometryContext;
   }

   @NotNull
   @Override
   public HasContext contextDelegate() {
      return this.context;
   }

   @Override
   protected void doAdd(@NotNull SVGNode node) {
      this.children.add(node);
   }

   @Override
   public List<? extends @NotNull SVGNode> children() {
      return this.children;
   }

   @Override
   public void render(@NotNull RenderContext context, @NotNull Output output) {
      for (SVGNode child : this.children()) {
         NodeRenderer.renderNode(child, context, output);
      }
   }

   @Override
   public boolean isVisible(@NotNull RenderContext context) {
      return this.isVisible;
   }
}

