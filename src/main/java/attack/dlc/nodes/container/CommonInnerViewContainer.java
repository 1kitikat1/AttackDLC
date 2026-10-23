package attack.dlc.nodes.container;

import attack.dlc.attributes.value.PercentageDimension;
import attack.dlc.geometry.size.Length;
import attack.dlc.nodes.SVGNode;
import attack.dlc.nodes.prototype.ShapedContainer;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.MeasureContext;
import attack.dlc.renderer.RenderContext;
import attack.dlc.view.FloatSize;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CommonInnerViewContainer extends BaseInnerViewContainer implements ShapedContainer<SVGNode> {
   protected Length x;
   protected Length y;
   protected Length width;
   protected Length height;

   @NotNull
   @Override
   protected Point2D outerLocation(@NotNull MeasureContext context) {
      return new Float(this.x.resolve(context), this.y.resolve(context));
   }

   @Nullable
   @Override
   protected Point2D anchorLocation(@NotNull MeasureContext context) {
      return null;
   }

   @NotNull
   @Override
   public FloatSize size(@NotNull RenderContext context) {
      MeasureContext measure = context.measureContext();
      return new FloatSize(
         this.width.orElseIfUnspecified(measure.viewWidth()).resolve(measure), this.height.orElseIfUnspecified(measure.viewHeight()).resolve(measure)
      );
   }

   @Override
   public boolean isVisible(@NotNull RenderContext context) {
      return !this.width.isZero() && !this.height.isZero() && super.isVisible(context);
   }

   @MustBeInvokedByOverriders
   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.x = attributeNode.getLength("x", PercentageDimension.WIDTH, 0.0F);
      this.y = attributeNode.getLength("y", PercentageDimension.HEIGHT, 0.0F);
      this.width = attributeNode.getLength("width", PercentageDimension.WIDTH, Length.UNSPECIFIED);
      this.height = attributeNode.getLength("height", PercentageDimension.HEIGHT, Length.UNSPECIFIED);
   }
}

