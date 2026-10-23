package attack.dlc.nodes;

import attack.dlc.attributes.Overflow;
import attack.dlc.attributes.UnitType;
import attack.dlc.attributes.value.PercentageDimension;
import attack.dlc.attributes.value.TransformValue;
import attack.dlc.geometry.size.Length;
import attack.dlc.nodes.container.BaseInnerViewContainer;
import attack.dlc.nodes.filter.Filter;
import attack.dlc.nodes.prototype.Instantiator;
import attack.dlc.nodes.prototype.ShapedContainer;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.nodes.text.Text;
import attack.dlc.paint.SVGPaint;
import attack.dlc.paint.impl.TransformedPaint;
import attack.dlc.parser.PaintParser;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.MeasureContext;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.output.Output;
import attack.dlc.util.BlittableImage;
import attack.dlc.util.ImageUtil;
import attack.dlc.view.FloatSize;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D.Double;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(Category.Container)
@PermittedContent(
   categories = {Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient},
   anyOf = {Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class}
)
public final class Pattern extends BaseInnerViewContainer implements SVGPaint, ShapedContainer<SVGNode>, Instantiator {
   public static final String TAG = "pattern";
   private Length x;
   private Length y;
   private Length width;
   private Length height;
   private UnitType patternUnits;
   private UnitType patternContentUnits;
   private TransformValue patternTransform;

   @NotNull
   @Override
   public String tagName() {
      return "pattern";
   }

   @NotNull
   @Override
   protected Point2D outerLocation(@NotNull MeasureContext context) {
      return new Float(0.0F, 0.0F);
   }

   @Nullable
   @Override
   protected Point2D anchorLocation(@NotNull MeasureContext context) {
      return null;
   }

   @NotNull
   @Override
   protected Overflow defaultOverflow() {
      return Overflow.Hidden;
   }

   @NotNull
   @Override
   public FloatSize size(@NotNull RenderContext context) {
      return new FloatSize(this.width.resolve(context.measureContext()), this.height.resolve(context.measureContext()));
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      Pattern template = this.parseTemplate(attributeNode);
      if (this.viewBox == null && template != null) {
         this.viewBox = template.viewBox;
      }

      this.preserveAspectRatio = template != null ? template.preserveAspectRatio : this.preserveAspectRatio;
      this.patternUnits = attributeNode.getEnum("patternUnits", template != null ? template.patternUnits : UnitType.ObjectBoundingBox);
      this.patternContentUnits = attributeNode.getEnum("patternContentUnits", template != null ? template.patternContentUnits : UnitType.UserSpaceOnUse);
      this.x = attributeNode.getLength("x", PercentageDimension.WIDTH, template != null ? template.x : Length.ZERO)
         .coercePercentageToCorrectUnit(this.patternUnits, PercentageDimension.WIDTH);
      this.y = attributeNode.getLength("y", PercentageDimension.HEIGHT, template != null ? template.y : Length.ZERO)
         .coercePercentageToCorrectUnit(this.patternUnits, PercentageDimension.HEIGHT);
      this.width = attributeNode.getLength("width", PercentageDimension.WIDTH, template != null ? template.width : Length.ZERO)
         .coerceNonNegative()
         .coercePercentageToCorrectUnit(this.patternUnits, PercentageDimension.WIDTH);
      this.height = attributeNode.getLength("height", PercentageDimension.HEIGHT, template != null ? template.height : Length.ZERO)
         .coerceNonNegative()
         .coercePercentageToCorrectUnit(this.patternUnits, PercentageDimension.HEIGHT);
      this.patternTransform = attributeNode.parseTransform("patternTransform");
      if (this.patternTransform == null && template != null) {
         this.patternTransform = template.patternTransform;
      }
   }

   @Nullable
   private Pattern parseTemplate(@NotNull AttributeNode attributeNode) {
      Pattern template = attributeNode.getElementByHref(Pattern.class, attributeNode.getHref(), AttributeNode.ElementRelation.TEMPLATE);
      return template != this ? template : null;
   }

   @Override
   public boolean isVisible(@NotNull RenderContext context) {
      return !this.width.isZero() && !this.height.isZero() && SVGPaint.super.isVisible(context);
   }

   @Override
   public void fillShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
      Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
      output.setPaint(() -> this.paintForBounds(output, context, b));
      output.fillShape(shape);
   }

   @Override
   public void drawShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
      Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
      output.setPaint(() -> this.paintForBounds(output, context, b));
      output.drawShape(shape);
   }

   @NotNull
   private Paint paintForBounds(@NotNull Output output, @NotNull RenderContext context, @NotNull Rectangle2D bounds) {
      MeasureContext measure = context.measureContext();
      Double patternBounds = this.patternUnits.computeViewBounds(measure, bounds, this.x, this.y, this.width, this.height);
      BlittableImage blittableImage = BlittableImage.create(
         ImageUtil::createCompatibleTransparentImage, context, null, patternBounds, bounds, this.patternContentUnits
      );
      if (blittableImage == null) {
         return PaintParser.DEFAULT_COLOR;
      }

      blittableImage.render(output, (out, ctx) -> {
         if (this.patternContentUnits == UnitType.UserSpaceOnUse) {
            ctx.translate(out, patternBounds.getX(), patternBounds.getY());
         }

         this.renderWithSize(new FloatSize(patternBounds), this.viewBox, ctx, out);
      });
      return this.patternTransform != null
         ? new TransformedPaint(new TexturePaint(blittableImage.image(), patternBounds), this.patternTransform.get(measure))
         : new TexturePaint(blittableImage.image(), patternBounds);
   }

   @Override
   public boolean requiresInstantiation() {
      return true;
   }

   @Override
   public boolean canInstantiate(@NotNull SVGNode node) {
      return node == this;
   }
}

