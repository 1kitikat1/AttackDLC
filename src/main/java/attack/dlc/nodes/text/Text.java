package attack.dlc.nodes.text;

import attack.dlc.nodes.Anchor;
import attack.dlc.nodes.SVGNode;
import attack.dlc.nodes.prototype.HasGeometryContext;
import attack.dlc.nodes.prototype.Renderable;
import attack.dlc.nodes.prototype.impl.HasGeometryContextImpl;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.parser.TextContent;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.impl.NodeRenderer;
import attack.dlc.renderer.output.Output;
import attack.dlc.util.AttributeUtil;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Path2D.Float;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories({Category.Graphic, Category.TextContent})
@PermittedContent(categories = {Category.Animation, Category.Descriptive, Category.TextContentChild}, anyOf = Anchor.class, charData = true)
public final class Text extends LinearTextContainer<TextLayoutGroup> implements HasGeometryContext.ByDelegate, Renderable {
   public static final String TAG = "text";
   private HasGeometryContext geometryContext;

   @NotNull
   @Override
   public String tagName() {
      return "text";
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.geometryContext = HasGeometryContextImpl.parse(attributeNode);
   }

   @NotNull
   @Override
   public HasGeometryContext geometryContextDelegate() {
      return this.geometryContext;
   }

   @Override
   public boolean isVisible(@NotNull RenderContext context) {
      return this.isVisible;
   }

   @Override
   protected boolean acceptChild(@Nullable String id, @NotNull SVGNode node) {
      return node instanceof TextSegment || node instanceof TextLayoutGroup;
   }

   @Override
   protected void doAdd(@NotNull SVGNode node) {
      if (node instanceof TextSegment) {
         this.lastLinearLayoutGroup().segments().add((TextSegment)node);
      }

      if (node instanceof TextLayoutGroup) {
         this.children.add((TextLayoutGroup)node);
      }
   }

   @Override
   public void addContent(@NotNull TextContent.Segment content) {
      if (!content.isConstant() || !content.text().isEmpty()) {
         if (!this.children.isEmpty() || !content.isConstant() || !AttributeUtil.isBlank(content.text())) {
            LinearTextLayoutGroup linearTextLayoutGroup = this.lastLinearLayoutGroup();
            linearTextLayoutGroup.segments().add(new StringTextSegment(this, linearTextLayoutGroup, linearTextLayoutGroup.segments().size(), content));
         }
      }
   }

   @NotNull
   private LinearTextLayoutGroup lastLinearLayoutGroup() {
      TextLayoutGroup lastGroup = this.children.isEmpty() ? null : this.children.get(this.children.size() - 1);
      if (!(lastGroup instanceof LinearTextLayoutGroup)) {
         lastGroup = new LinearTextLayoutGroup(this);
         this.children.add(lastGroup);
      }

      return (LinearTextLayoutGroup)lastGroup;
   }

   @Override
   public void render(@NotNull RenderContext context, @NotNull Output output) {
      Point2D start = null;

      for (TextLayoutGroup layoutGroup : this.children()) {
         RenderContext currentContext = context;
         if (layoutGroup instanceof TextContainer) {
            currentContext = NodeRenderer.setupRenderContext(layoutGroup, context);
         }

         start = layoutGroup.renderText(start, currentContext, output);
      }
   }

   @NotNull
   @Override
   protected Shape glyphShape(@NotNull RenderContext context) {
      Path2D shape = new Float();
      Point2D start = null;

      for (TextLayoutGroup layoutGroup : this.children()) {
         start = layoutGroup.appendGlyphShape(start, context, shape);
      }

      return shape;
   }

   @NotNull
   @Override
   public GlyphCursor createLocalCursor(boolean isInitial, @NotNull GlyphCursor current) {
      return !isInitial ? current : super.createLocalCursor(isInitial, current);
   }
}

