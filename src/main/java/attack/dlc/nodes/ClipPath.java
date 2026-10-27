package attack.dlc.nodes;

import attack.dlc.attributes.MaskType;
import attack.dlc.attributes.UnitType;
import attack.dlc.geometry.util.GeometryUtil;
import attack.dlc.nodes.container.ContainerNode;
import attack.dlc.nodes.prototype.HasShape;
import attack.dlc.nodes.prototype.ShapedContainer;
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
import attack.dlc.util.ImageUtil;
import attack.dlc.util.ShapeUtil;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;

@ElementCategories({})
@PermittedContent(categories = {Category.Animation, Category.Descriptive, Category.Shape}, anyOf = {Use.class, Text.class})
public final class ClipPath extends ContainerNode implements ShapedContainer<SVGNode> {
   private static final boolean DEBUG = false;
   public static final String TAG = "clippath";
   private final CachedSurfaceSupplier surfaceSupplier = new CachedSurfaceSupplier(ImageUtil::createLuminosityBuffer);
   private boolean isValid;
   private UnitType clipPathUnits;

   @NotNull
   @Override
   public String tagName() {
      return "clippath";
   }

   public boolean isValid() {
      return this.isValid;
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.isValid = this.checkIsValid();
      this.clipPathUnits = attributeNode.getEnum("clipPathUnits", UnitType.UserSpaceOnUse);
   }

   private boolean checkIsValid() {
      for (SVGNode child : this.children()) {
         if (child instanceof Use) {
            SVGNode referenced = ((Use)child).referencedNode();
            if (referenced != null && !this.isAcceptableType(referenced)) {
               return false;
            }
         }
      }

      return true;
   }

   @NotNull
   public Shape clipShape(@NotNull RenderContext context, @NotNull ElementBounds elementBounds, boolean useSoftClip) {
      Shape shape = ShapedContainer.super.elementShape(context, HasShape.Box.BoundingBox);
      if (!useSoftClip && this.clipPathUnits == UnitType.ObjectBoundingBox) {
         shape = ShapeUtil.transformShape(shape, this.clipPathUnits.viewTransform(elementBounds.boundingBox()));
      }

      Area areaShape = new Area(shape);
      return areaShape.isRectangular() ? areaShape.getBounds2D() : areaShape;
   }

   @NotNull
   public Paint createPaintForSoftClipping(
      @NotNull Output output, @NotNull RenderContext context, @NotNull ElementBounds elementBounds, @NotNull Shape clipShape
   ) {
      Rectangle2D transformedClipBounds = GeometryUtil.containingBoundsAfterTransform(
         this.clipPathUnits.viewTransform(elementBounds.boundingBox()), clipShape.getBounds2D()
      );
      boolean useCache = this.surfaceSupplier.useCache(output, context);
      BlittableImage blitImage = BlittableImage.create(
         this.surfaceSupplier.surfaceSupplier(useCache),
         context,
         output.clipBounds(),
         transformedClipBounds.createIntersection(elementBounds.geometryBox()),
         elementBounds.boundingBox(),
         this.clipPathUnits
      );
      if (blitImage == null) {
         return PaintParser.DEFAULT_COLOR;
      }

      blitImage.clearBackground(Color.BLACK);
      blitImage.render(output, g -> {
         g.setColor(Color.WHITE);
         g.fill(clipShape);
      });
      Point2D offset = GeometryUtil.getLocation(blitImage.imageBoundsInDeviceSpace());
      return new MaskedPaint(
         PaintParser.DEFAULT_COLOR, blitImage.image().getRaster(), offset, this.surfaceSupplier.resourceCleaner(output, useCache), MaskType.Luminance
      );
   }

   public void applyClip(@NotNull Output output, @NotNull RenderContext context, @NotNull ElementBounds elementBounds) {
      if (output.isSoftClippingEnabled()) {
         Rectangle2D bounds = elementBounds.geometryBox();
         if (!bounds.isEmpty()) {
            output.setPaint(() -> {
               Shape childClipShapex = this.clipShape(context, elementBounds, true);
               return this.createPaintForSoftClipping(output, context, elementBounds, childClipShapex);
            });
         }
      } else {
         Shape childClipShape = this.clipShape(context, elementBounds, false);
         output.applyClip(childClipShape);
      }
   }

   // $VF: synthetic method
   private static void lambda$applyClip$2(Shape childClipShape, Graphics2D g) {
      g.setClip(null);
      g.setPaint(Color.MAGENTA);
      g.draw(childClipShape);
   }
}

