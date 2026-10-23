package attack.dlc.nodes;

import attack.dlc.attributes.MarkerOrientation;
import attack.dlc.attributes.MarkerUnitType;
import attack.dlc.attributes.Overflow;
import attack.dlc.attributes.value.LengthValue;
import attack.dlc.attributes.value.PercentageDimension;
import attack.dlc.geometry.size.Length;
import attack.dlc.nodes.container.BaseInnerViewContainer;
import attack.dlc.nodes.filter.Filter;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.nodes.text.Text;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.MeasureContext;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.impl.context.RenderContextAccessor;
import attack.dlc.view.FloatSize;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.Container)
@PermittedContent(
   categories = {Category.Animation, Category.Descriptive, Category.Shape, Category.Structural, Category.Gradient},
   anyOf = {Anchor.class, ClipPath.class, Filter.class, Image.class, Mask.class, Marker.class, Pattern.class, Style.class, Text.class, View.class}
)
public final class Marker extends BaseInnerViewContainer {
   public static final String TAG = "marker";
   private Length refX;
   private Length refY;
   private MarkerOrientation orientation;
   private MarkerUnitType markerUnits;
   private Length markerHeight;
   private Length markerWidth;

   @NotNull
   @Override
   public String tagName() {
      return "marker";
   }

   @NotNull
   public MarkerOrientation orientation() {
      return this.orientation;
   }

   @NotNull
   @Override
   protected Point2D outerLocation(@NotNull MeasureContext context) {
      return new Float(0.0F, 0.0F);
   }

   @NotNull
   @Override
   protected Point2D anchorLocation(@NotNull MeasureContext context) {
      return new Float(-this.refX.resolve(context), -this.refY.resolve(context));
   }

   @NotNull
   @Override
   protected Overflow defaultOverflow() {
      return Overflow.Hidden;
   }

   @NotNull
   @Override
   public FloatSize size(@NotNull RenderContext context) {
      MeasureContext measure = context.measureContext();
      if (this.markerUnits == MarkerUnitType.StrokeWidth) {
         LengthValue strokeWidthLength = RenderContextAccessor.instance().strokeContext(context).strokeWidth;
         assert strokeWidthLength != null;
         float strokeWidth = strokeWidthLength.resolve(measure);
         return new FloatSize(this.markerWidth.raw() * strokeWidth, this.markerHeight.raw() * strokeWidth);
      } else {
         return new FloatSize(this.markerWidth.resolve(measure), this.markerHeight.resolve(measure));
      }
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.refX = attributeNode.getHorizontalReferenceLengthFromKey("refX");
      this.refY = attributeNode.getVerticalReferenceLengthFromKey("refY");
      this.orientation = MarkerOrientation.parse(attributeNode.getValue("orient"), attributeNode.parser());
      this.markerUnits = attributeNode.getEnum("markerUnits", MarkerUnitType.StrokeWidth);
      this.markerWidth = attributeNode.getLength("markerWidth", PercentageDimension.WIDTH, 3.0F);
      this.markerHeight = attributeNode.getLength("markerHeight", PercentageDimension.HEIGHT, 3.0F);
   }

   @Override
   public boolean requiresInstantiation() {
      return true;
   }

   @Override
   protected boolean inheritAttributes() {
      return false;
   }
}

