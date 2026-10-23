package attack.dlc.nodes;

import attack.dlc.attributes.FillRule;
import attack.dlc.attributes.font.AttributeFontSpec;
import attack.dlc.attributes.font.FontParser;
import attack.dlc.attributes.value.PercentageDimension;
import attack.dlc.geometry.AWTSVGShape;
import attack.dlc.geometry.size.Length;
import attack.dlc.nodes.container.CommonInnerViewContainer;
import attack.dlc.nodes.prototype.HasContext;
import attack.dlc.nodes.prototype.HasShape;
import attack.dlc.nodes.prototype.Instantiator;
import attack.dlc.nodes.prototype.Renderable;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.MeasureContext;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.impl.NodeRenderer;
import attack.dlc.renderer.impl.context.FontRenderContext;
import attack.dlc.renderer.impl.context.PaintContext;
import attack.dlc.renderer.output.Output;
import attack.dlc.view.FloatSize;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories({Category.Graphic, Category.GraphicsReferencing, Category.Structural})
@PermittedContent(categories = {Category.Animation, Category.Descriptive})
public final class Use extends RenderableSVGNode implements HasContext, HasShape, Instantiator {
   public static final String TAG = "use";
   private Length x;
   private Length y;
   private Length width;
   private Length height;
   @Nullable
   private SVGNode referencedNode;
   private PaintContext paintContext;
   private FontRenderContext fontRenderContext;
   private AttributeFontSpec fontSpec;
   private FillRule fillRule;

   @NotNull
   @Override
   public String tagName() {
      return "use";
   }

   @Nullable
   public SVGNode referencedNode() {
      return this.referencedNode;
   }

   @Override
   public boolean isVisible(@NotNull RenderContext context) {
      return super.isVisible(context) && this.referencedNode instanceof Renderable;
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.x = attributeNode.getLength("x", PercentageDimension.WIDTH, 0.0F);
      this.y = attributeNode.getLength("y", PercentageDimension.HEIGHT, 0.0F);
      this.width = attributeNode.getLength("width", PercentageDimension.WIDTH, Length.UNSPECIFIED);
      this.height = attributeNode.getLength("height", PercentageDimension.HEIGHT, Length.UNSPECIFIED);
      String href = attributeNode.getValue("href");
      if (href == null) {
         href = attributeNode.getValue("xlink:href");
      }

      this.referencedNode = attributeNode.getElementByHref(SVGNode.class, href, AttributeNode.ElementRelation.PAINTED_CHILD);
      this.paintContext = PaintContext.parse(attributeNode);
      this.fontRenderContext = FontRenderContext.parse(attributeNode);
      this.fontSpec = FontParser.parseFontSpec(attributeNode);
      this.fillRule = FillRule.parse(attributeNode);
   }

   @NotNull
   @Override
   public Shape untransformedElementShape(@NotNull RenderContext context, HasShape.Box box) {
      return this.referencedNode instanceof HasShape
         ? ((HasShape)this.referencedNode).elementShape(NodeRenderer.createChildContext((Renderable)this.referencedNode, context, this), box)
         : AWTSVGShape.EMPTY_SHAPE;
   }

   @NotNull
   @Override
   public Rectangle2D untransformedElementBounds(@NotNull RenderContext context, HasShape.Box box) {
      return this.referencedNode instanceof HasShape
         ? ((HasShape)this.referencedNode).elementBounds(NodeRenderer.createChildContext((Renderable)this.referencedNode, context, this), box)
         : AWTSVGShape.EMPTY_SHAPE;
   }

   @NotNull
   public PaintContext paintContext() {
      return this.paintContext;
   }

   @NotNull
   @Override
   public FontRenderContext fontRenderContext() {
      return this.fontRenderContext;
   }

   @NotNull
   public AttributeFontSpec fontSpec() {
      return this.fontSpec;
   }

   @Override
   public boolean canInstantiate(@NotNull SVGNode node) {
      return node instanceof CommonInnerViewContainer;
   }

   @Override
   public void render(@NotNull RenderContext context, @NotNull Output output) {
      if (this.referencedNode != null) {
         MeasureContext measureContext = context.measureContext();
         context.translate(output, this.x.resolve(measureContext), this.y.resolve(measureContext));
         if (this.referencedNode instanceof CommonInnerViewContainer) {
            FloatSize targetViewBox = new FloatSize(Float.NaN, Float.NaN);
            if (this.width.isSpecified()) {
               targetViewBox.width = this.width.resolve(measureContext);
            }

            if (this.height.isSpecified()) {
               targetViewBox.height = this.height.resolve(measureContext);
            }

            CommonInnerViewContainer view = (CommonInnerViewContainer)this.referencedNode;
            NodeRenderer.renderWithSize(view, targetViewBox, context, output, this);
         } else {
            NodeRenderer.renderNode(this.referencedNode, context, output, this);
         }
      }
   }

   @Override
   public String toString() {
      return "Use{x="
         + this.x
         + ", y="
         + this.y
         + ", width="
         + this.width
         + ", height="
         + this.height
         + ", referencedNode="
         + (this.referencedNode != null ? this.referencedNode.id() : null)
         + ", styleContext="
         + this.paintContext
         + ", fillRule="
         + this.fillRule
         + ", fontRenderContext="
         + this.fontRenderContext
         + '}';
   }
}

