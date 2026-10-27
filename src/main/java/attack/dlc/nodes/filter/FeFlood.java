package attack.dlc.nodes.filter;

import attack.dlc.attributes.Animatable;
import attack.dlc.attributes.Inherited;
import attack.dlc.attributes.filter.LayoutBounds;
import attack.dlc.attributes.value.PercentageValue;
import attack.dlc.geometry.size.FloatInsets;
import attack.dlc.geometry.size.Percentage;
import attack.dlc.nodes.animation.Animate;
import attack.dlc.nodes.animation.Set;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.paint.SVGPaint;
import attack.dlc.paint.impl.AwtSVGPaint;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.output.impl.Graphics2DOutput;
import attack.dlc.renderer.output.impl.GraphicsUtil;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.jetbrains.annotations.NotNull;

@ElementCategories(Category.FilterPrimitive)
@PermittedContent(anyOf = {Animate.class, Set.class})
public final class FeFlood extends AbstractFilterPrimitive {
   public static final String TAG = "feflood";
   private SVGPaint floodColor;
   private PercentageValue floodOpacity;

   @NotNull
   @Override
   public String tagName() {
      return "feflood";
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.floodColor = attributeNode.getPaint("flood-color", new AwtSVGPaint(Color.BLACK), Inherited.NO, Animatable.YES);
      this.floodOpacity = attributeNode.getPercentage("flood-opacity", Percentage.ONE, Inherited.NO, Animatable.YES);
   }

   @Override
   public void layoutFilter(@NotNull RenderContext context, @NotNull FilterLayoutContext filterLayoutContext) {
      LayoutBounds layoutBounds = new LayoutBounds(filterLayoutContext.filterPrimitiveRegion(context.measureContext(), this), new FloatInsets());
      this.impl().saveLayoutResult(layoutBounds, filterLayoutContext);
   }

   @Override
   public void applyFilter(@NotNull RenderContext context, @NotNull FilterContext filterContext) {
      Filter.FilterInfo info = filterContext.info();
      BufferedImage img = new BufferedImage(info.imageWidth, info.imageHeight, 2);
      float opacity = this.floodOpacity.get(context.measureContext());
      if (opacity != 0.0F) {
         Graphics2D graphics = GraphicsUtil.createGraphics(img);
         graphics.setComposite(AlphaComposite.Src.derive(opacity));
         Rectangle rect = new Rectangle(0, 0, img.getWidth(), img.getHeight());
         this.floodColor.fillShape(new Graphics2DOutput(graphics), context, rect, rect);
         graphics.dispose();
      }

      this.impl().saveResult(new ImageProducerChannel(img.getSource()), filterContext);
   }
}

