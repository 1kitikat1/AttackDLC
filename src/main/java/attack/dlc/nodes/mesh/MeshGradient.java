package attack.dlc.nodes.mesh;

import attack.dlc.attributes.UnitType;
import attack.dlc.attributes.value.PercentageDimension;
import attack.dlc.geometry.size.Length;
import attack.dlc.nodes.SVGNode;
import attack.dlc.nodes.animation.Animate;
import attack.dlc.nodes.animation.AnimateTransform;
import attack.dlc.nodes.animation.Set;
import attack.dlc.nodes.container.ContainerNode;
import attack.dlc.nodes.prototype.spec.Category;
import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.paint.SVGPaint;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.MeasureContext;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.output.Output;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Float;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ElementCategories(Category.Gradient)
@PermittedContent(categories = Category.Descriptive, anyOf = {MeshRow.class, Animate.class, AnimateTransform.class, Set.class})
public final class MeshGradient extends ContainerNode implements SVGPaint {
   public static final String TAG = "meshgradient";
   private Length x;
   private Length y;
   private UnitType gradientUnits;

   @NotNull
   @Override
   public String tagName() {
      return "meshgradient";
   }

   @Override
   public void build(@NotNull AttributeNode attributeNode) {
      super.build(attributeNode);
      this.x = attributeNode.getLength("x", PercentageDimension.WIDTH, 0.0F);
      this.y = attributeNode.getLength("y", PercentageDimension.HEIGHT, 0.0F);
      this.gradientUnits = attributeNode.getEnum("gradientUnits", UnitType.ObjectBoundingBox);
      MeshBuilder.buildMesh(this, new Float(this.x.raw(), this.y.raw()));
   }

   public void renderMesh(@NotNull MeasureContext measure, @NotNull Output output) {
      Output meshOutput = output.createChild();
      meshOutput.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

      for (SVGNode child : this.children()) {
         MeshRow row = (MeshRow)child;

         for (SVGNode node : row.children()) {
            MeshPatch patch = (MeshPatch)node;
            patch.renderPath(meshOutput);
         }
      }

      meshOutput.dispose();
   }

   @Override
   public void fillShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
      Output.SafeState safeState = output.safeState();
      Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
      output.applyClip(shape);
      output.translate(b.getX(), b.getY());
      this.renderMesh(context.measureContext(), output);
      safeState.restore();
   }

   @Override
   public void drawShape(@NotNull Output output, @NotNull RenderContext context, @NotNull Shape shape, @Nullable Rectangle2D bounds) {
      Output.SafeState safeState = output.safeState();
      Rectangle2D b = bounds != null ? bounds : shape.getBounds2D();
      output.applyClip(output.stroke().createStrokedShape(shape));
      output.translate(b.getX(), b.getY());
      this.renderMesh(context.measureContext(), output);
      safeState.restore();
   }
}

