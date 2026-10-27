package attack.dlc.renderer.impl.context;

import attack.dlc.animation.value.AnimatedColor;
import attack.dlc.attributes.Animatable;
import attack.dlc.attributes.FillRule;
import attack.dlc.attributes.Inherited;
import attack.dlc.attributes.PaintOrder;
import attack.dlc.attributes.value.PercentageValue;
import attack.dlc.geometry.size.Percentage;
import attack.dlc.nodes.prototype.Mutator;
import attack.dlc.paint.SVGPaint;
import attack.dlc.paint.impl.AwtSVGPaint;
import attack.dlc.paint.impl.PredefinedPaints;
import attack.dlc.paint.impl.RGBColor;
import attack.dlc.parser.impl.AttributeNode;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PaintContext implements Mutator<PaintContext> {
   @Nullable
   public final SVGPaint color;
   @Nullable
   public final SVGPaint fillPaint;
   @Nullable
   public final SVGPaint strokePaint;
   @NotNull
   public final PercentageValue opacity;
   @Nullable
   public final PercentageValue fillOpacity;
   @Nullable
   public final PercentageValue strokeOpacity;
   @Nullable
   public final PaintOrder paintOrder;
   @Nullable
   public final StrokeContext strokeContext;
   @Nullable
   public final FillRule fillRule;

   public PaintContext(
      @Nullable SVGPaint color,
      @Nullable SVGPaint fillPaint,
      @Nullable PercentageValue fillOpacity,
      @Nullable SVGPaint strokePaint,
      @Nullable PercentageValue strokeOpacity,
      @NotNull PercentageValue opacity,
      @Nullable PaintOrder paintOrder,
      @Nullable StrokeContext strokeContext,
      @Nullable FillRule fillRule
   ) {
      this.color = color;
      this.fillPaint = fillPaint;
      this.strokePaint = strokePaint;
      this.fillOpacity = fillOpacity;
      this.strokeOpacity = strokeOpacity;
      this.opacity = opacity;
      this.paintOrder = paintOrder;
      this.strokeContext = strokeContext != null && !strokeContext.isTrivial() ? strokeContext : null;
      this.fillRule = fillRule;
   }

   @NotNull
   public static PaintContext createDefault(@NotNull SVGPaint currentColor) {
      return new PaintContext(
         currentColor,
         PredefinedPaints.DEFAULT_PAINT,
         Percentage.ONE,
         PredefinedPaints.NONE,
         Percentage.ONE,
         Percentage.ONE,
         PaintOrder.NORMAL,
         StrokeContext.createDefault(),
         FillRule.Nonzero
      );
   }

   @NotNull
   public static PaintContext parse(@NotNull AttributeNode attributeNode) {
      return new PaintContext(
         parseColorAttribute(attributeNode),
         attributeNode.getPaint("fill", Inherited.YES, Animatable.YES),
         attributeNode.getPercentage("fill-opacity", Inherited.YES, Animatable.YES),
         attributeNode.getPaint("stroke", Inherited.YES, Animatable.YES),
         attributeNode.getPercentage("stroke-opacity", Inherited.YES, Animatable.YES),
         attributeNode.getPercentage("opacity", Percentage.ONE, Inherited.NO, Animatable.YES),
         PaintOrder.parse(attributeNode),
         StrokeContext.parse(attributeNode),
         FillRule.parse(attributeNode)
      );
   }

   @Nullable
   private static SVGPaint parseColorAttribute(@NotNull AttributeNode attributeNode) {
      Color c = attributeNode.getColor("color", null);
      if (c == null) {
         return null;
      }

      AnimatedColor animatedColor = attributeNode.getAnimatedColor("color", new RGBColor(c));
      return animatedColor != null ? animatedColor : new AwtSVGPaint(c);
   }

   @NotNull
   public PaintContext derive(@NotNull PaintContext context) {
      return new PaintContext(
         SVGPaint.derive(this.color, context.color),
         SVGPaint.derive(this.fillPaint, context.fillPaint),
         context.fillOpacity != null ? context.fillOpacity : this.fillOpacity,
         SVGPaint.derive(this.strokePaint, context.strokePaint),
         context.strokeOpacity != null ? context.strokeOpacity : this.strokeOpacity,
         this.opacity.multiply(context.opacity),
         context.paintOrder != null ? context.paintOrder : this.paintOrder,
         this.strokeContext != null ? this.strokeContext.derive(context.strokeContext) : context.strokeContext,
         context.fillRule != null && context.fillRule != FillRule.Inherit ? context.fillRule : this.fillRule
      );
   }

   @NotNull
   public PaintContext mutate(@NotNull PaintContext element) {
      return element.derive(this);
   }

   @Override
   public String toString() {
      return "PaintContext{color="
         + this.color
         + ", fillPaint="
         + this.fillPaint
         + ", strokePaint="
         + this.strokePaint
         + ", opacity="
         + this.opacity
         + ", fillOpacity="
         + this.fillOpacity
         + ", strokeOpacity="
         + this.strokeOpacity
         + ", strokeContext="
         + this.strokeContext
         + ", paintOrder="
         + this.paintOrder
         + ", fillRule="
         + this.fillRule
         + '}';
   }
}

