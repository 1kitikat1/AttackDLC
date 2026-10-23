package attack.dlc.nodes;

import attack.dlc.attributes.MaskType;
import attack.dlc.attributes.UnitType;
import attack.dlc.attributes.value.PercentageDimension;
import attack.dlc.geometry.size.Length;
import attack.dlc.geometry.size.Unit;
import attack.dlc.geometry.util.GeometryUtil;
import attack.dlc.nodes.container.CommonRenderableContainerNode;
import attack.dlc.nodes.filter.Filter;
import attack.dlc.nodes.prototype.Instantiator;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.nodes.text.Text;
import attack.dlc.paint.impl.MaskedPaint;
import attack.dlc.parser.PaintParser;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.impl.ElementBounds;
import attack.dlc.renderer.output.Output;
import attack.dlc.util.BlittableImage;
import attack.dlc.util.CachedSurfaceSupplier;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D.Double;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.Container)
@PermittedContent(
   categories = {Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient},
   anyOf = {Anchor.class, ClipPath.class, Filter.class, Image.class, Marker.class, Mask.class, Pattern.class, Style.class, Text.class, View.class}
)
public final class Mask extends CommonRenderableContainerNode implements Instantiator {
   private static final boolean DEBUG = false;
   public static final String TAG = "mask";
   private CachedSurfaceSupplier surfaceSupplier;
   private Length x;
   private Length y;
   private Length width;
   private Length height;
   private UnitType maskContentUnits;
   private UnitType maskUnits;
   private MaskType maskType;

   @NotNull
   @Override
   public String tagName() {
      return "mask";
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.maskContentUnits = attributeNode.getEnum("maskContentUnits", UnitType.UserSpaceOnUse);
      this.maskUnits = attributeNode.getEnum("maskUnits", UnitType.ObjectBoundingBox);
      this.maskType = attributeNode.getEnum("mask-type", MaskType.Luminance);
      this.surfaceSupplier = new CachedSurfaceSupplier(this.maskType.bufferSurface());
      this.x = attributeNode.getLength("x", PercentageDimension.WIDTH, Unit.PERCENTAGE_WIDTH.valueOf(-10.0F))
         .coercePercentageToCorrectUnit(this.maskUnits, PercentageDimension.WIDTH);
      this.y = attributeNode.getLength("y", PercentageDimension.HEIGHT, Unit.PERCENTAGE_HEIGHT.valueOf(-10.0F))
         .coercePercentageToCorrectUnit(this.maskUnits, PercentageDimension.HEIGHT);
      this.width = attributeNode.getLength("width", PercentageDimension.WIDTH, Unit.PERCENTAGE_WIDTH.valueOf(120.0F))
         .coercePercentageToCorrectUnit(this.maskUnits, PercentageDimension.WIDTH);
      this.height = attributeNode.getLength("height", PercentageDimension.HEIGHT, Unit.PERCENTAGE_HEIGHT.valueOf(120.0F))
         .coercePercentageToCorrectUnit(this.maskUnits, PercentageDimension.HEIGHT);
   }

   @NotNull
   public Paint createMaskPaint(@NotNull Output output, @NotNull RenderContext context, @NotNull ElementBounds elementBounds) {
      Double maskBounds = this.maskUnits.computeViewBounds(context.measureContext(), elementBounds.boundingBox(), this.x, this.y, this.width, this.height);
      boolean useCache = this.surfaceSupplier.useCache(output, context);
      BlittableImage blitImage = BlittableImage.create(
         this.surfaceSupplier.surfaceSupplier(useCache),
         context,
         output.clipBounds(),
         maskBounds.createIntersection(elementBounds.geometryBox()),
         elementBounds.boundingBox(),
         this.maskContentUnits
      );
      if (blitImage == null) {
         return PaintParser.DEFAULT_COLOR;
      }

      if (this.maskType == MaskType.Luminance) {
         blitImage.clearBackground(Color.BLACK);
      } else {
         Graphics2D g = blitImage.image().createGraphics();
         g.setComposite(AlphaComposite.Clear);
         g.fillRect(0, 0, blitImage.image().getWidth(), blitImage.image().getHeight());
         g.dispose();
      }

      blitImage.renderNode(output, this, this);
      Point2D offset = GeometryUtil.getLocation(blitImage.imageBoundsInDeviceSpace());
      return new MaskedPaint(
         PaintParser.DEFAULT_COLOR, blitImage.image().getRaster(), offset, this.surfaceSupplier.resourceCleaner(output, useCache), this.maskType
      );
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

