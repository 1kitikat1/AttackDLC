package attack.dlc.renderer.impl;

import attack.dlc.attributes.font.MeasurableFontSpec;
import attack.dlc.nodes.ClipPath;
import attack.dlc.nodes.Mask;
import attack.dlc.nodes.SVG;
import attack.dlc.nodes.SVGNode;
import attack.dlc.nodes.container.BaseInnerViewContainer;
import attack.dlc.nodes.filter.Filter;
import attack.dlc.nodes.prototype.HasClip;
import attack.dlc.nodes.prototype.HasFilter;
import attack.dlc.nodes.prototype.HasFontContext;
import attack.dlc.nodes.prototype.HasFontRenderContext;
import attack.dlc.nodes.prototype.HasPaintContext;
import attack.dlc.nodes.prototype.Instantiator;
import attack.dlc.nodes.prototype.Mutator;
import attack.dlc.nodes.prototype.Renderable;
import attack.dlc.nodes.prototype.Transformable;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.SVGRenderingHints;
import attack.dlc.renderer.impl.context.ContextElementAttributes;
import attack.dlc.renderer.impl.context.FontRenderContext;
import attack.dlc.renderer.impl.context.PaintContext;
import attack.dlc.renderer.impl.context.RenderContextAccessor;
import attack.dlc.renderer.output.Output;
import attack.dlc.view.FloatSize;
import attack.dlc.view.ViewBox;
import java.awt.geom.Rectangle2D;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NodeRenderer {
   private NodeRenderer() {
   }

   public static void renderRootSVG(@NotNull SVG svgRoot, @NotNull RenderContext context, @NotNull Output output) {
      RenderContext viewContext = svgRoot.createInnerContextForViewBox(svgRoot.size(context), svgRoot.viewBox(context), context, output);
      Info info = createRenderInfo(svgRoot, viewContext, output, null);

      try {
         if (info != null) {
            ((SVG)info.renderable()).renderWithEstablishedViewBox(info.context(), info.output());
         }
      } catch (Throwable var8) {
         if (info != null) {
            try {
               info.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (info != null) {
         info.close();
      }
   }

   public static void renderNode(@NotNull SVGNode node, @NotNull RenderContext context, @NotNull Output output) {
      renderNode(node, context, output, null);
   }

   public static void renderNode(@NotNull SVGNode node, @NotNull RenderContext context, @NotNull Output output, @Nullable Instantiator instantiator) {
      Info info = createRenderInfo(node, context, output, instantiator);

      try {
         if (info != null) {
            info.renderable().render(info.context(), info.output());
         }
      } catch (Throwable var8) {
         if (info != null) {
            try {
               info.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (info != null) {
         info.close();
      }
   }

   public static void renderWithSize(
      @NotNull BaseInnerViewContainer node,
      @NotNull FloatSize size,
      @NotNull RenderContext context,
      @NotNull Output output,
      @Nullable Instantiator instantiator
   ) {
      Info info = createRenderInfo(node, context, output, instantiator);

      try {
         if (info != null) {
            node.renderWithSize(size, node.viewBox(info.context()), info.context(), info.output());
         }
      } catch (Throwable var9) {
         if (info != null) {
            try {
               info.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (info != null) {
         info.close();
      }
   }

   @NotNull
   public static RenderContext createChildContext(@NotNull Renderable node, @NotNull RenderContext context, @Nullable Instantiator instantiator) {
      return setupRenderContext(instantiator, node, context);
   }

   @Nullable
   private static Info createRenderInfo(@NotNull SVGNode node, @NotNull RenderContext context, @NotNull Output output, @Nullable Instantiator instantiator) {
      if (!(node instanceof Renderable)) {
         return null;
      }

      Renderable renderable = (Renderable)node;
      if (!checkInstantiation(node, instantiator, renderable)) {
         return null;
      }

      if (!renderable.isVisible(context)) {
         return null;
      }

      RenderContext childContext = createChildContext(renderable, context, instantiator);
      Output childOutput = output.createChild();
      ElementBounds elementBounds = new ElementBounds(node, childContext);
      applyTransform(renderable, childOutput, childContext, elementBounds);
      Mask maskForIsolation = null;
      ClipPath clipPathForIsolation = null;
      if (renderable instanceof HasClip) {
         maskForIsolation = setupMask((HasClip)renderable, elementBounds, childOutput, childContext);
         ClipPath clipPath = setupClip((HasClip)renderable, elementBounds, childContext, childOutput);
         if (clipPath != null && !clipPath.isValid()) {
            return null;
         }

         if (useAccurateMasking(childOutput)) {
            clipPathForIsolation = clipPath;
         }
      }

      Filter filter = null;
      if (renderable instanceof HasFilter) {
         filter = setupFilter((HasFilter)renderable, childOutput);
      }

      Info info = Info.InfoWithIsolation.create(
         renderable, childContext, childOutput, elementBounds, new IsolationEffects(filter, maskForIsolation, clipPathForIsolation)
      );
      return info != null ? info : new Info(renderable, childContext, childOutput);
   }

   private static void applyTransform(
      @NotNull Renderable renderable, @NotNull Output childOutput, @NotNull RenderContext childContext, @NotNull ElementBounds elementBounds
   ) {
      if (renderable instanceof Transformable && ((Transformable)renderable).shouldTransform()) {
         ((Transformable)renderable).applyTransform(childOutput, childContext, elementBounds);
      }
   }

   private static boolean checkInstantiation(@NotNull SVGNode node, @Nullable Instantiator instantiator, @NotNull Renderable renderable) {
      boolean instantiated = renderable.requiresInstantiation();
      return !instantiated || instantiator != null && instantiator.canInstantiate(node);
   }

   @Nullable
   private static ClipPath setupClip(
      @NotNull HasClip renderable, @NotNull ElementBounds elementBounds, @NotNull RenderContext childContext, @NotNull Output childOutput
   ) {
      ClipPath childClip = renderable.clipPath();
      if (childClip == null) {
         return null;
      }

      if (!childClip.isValid()) {
         return childClip;
      }

      childClip.applyClip(childOutput, childContext, elementBounds);
      return childClip;
   }

   @Nullable
   private static Mask setupMask(HasClip renderable, ElementBounds elementBounds, Output childOutput, RenderContext childContext) {
      Mask mask = renderable.mask();
      if (mask == null) {
         return null;
      }

      Rectangle2D bounds = elementBounds.geometryBox();
      if (bounds.isEmpty()) {
         return null;
      }

      if (useAccurateMasking(childOutput)) {
         return mask;
      }

      childOutput.setPaint(() -> mask.createMaskPaint(childOutput, childContext, elementBounds));
      return null;
   }

   @Nullable
   private static Filter setupFilter(@NotNull HasFilter hasFilter, @NotNull Output childOutput) {
      Filter filter = hasFilter.filter();
      if (filter != null && (!filter.hasEffect() || !childOutput.supportsFilters())) {
         filter = null;
      }

      return filter;
   }

   private static boolean useAccurateMasking(@NotNull Output output) {
      return output.renderingHint(SVGRenderingHints.KEY_MASK_CLIP_RENDERING) == SVGRenderingHints.VALUE_MASK_CLIP_RENDERING_ACCURACY;
   }

   @NotNull
   public static RenderContext setupRenderContext(@NotNull Object node, @NotNull RenderContext context) {
      return setupRenderContext(null, node, context);
   }

   @NotNull
   private static RenderContext setupRenderContext(@Nullable Instantiator instantiator, @NotNull Object node, @NotNull RenderContext context) {
      Mutator<PaintContext> paintContext = null;
      Mutator<MeasurableFontSpec> fontSpec = null;
      FontRenderContext fontRenderContext = null;
      if (node instanceof HasPaintContext) {
         paintContext = ((HasPaintContext)node).paintContext();
      }

      if (node instanceof HasFontContext) {
         fontSpec = ((HasFontContext)node).fontSpec();
      }

      if (node instanceof HasFontRenderContext) {
         fontRenderContext = ((HasFontRenderContext)node).fontRenderContext();
      }

      ContextElementAttributes contextElementAttributes = null;
      if (instantiator != null) {
         contextElementAttributes = instantiator.createContextAttributes(context);
      }

      return RenderContextAccessor.instance().deriveForNode(context, paintContext, fontSpec, fontRenderContext, contextElementAttributes, node);
   }

   @NotNull
   public static RenderContext setupInnerViewRenderContext(@NotNull ViewBox viewBox, @NotNull RenderContext context, boolean inheritAttributes) {
      return RenderContextAccessor.instance().setupInnerViewRenderContext(viewBox, context, inheritAttributes);
   }
}

