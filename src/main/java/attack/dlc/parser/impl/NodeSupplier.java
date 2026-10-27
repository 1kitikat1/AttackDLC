package attack.dlc.parser.impl;

import attack.dlc.nodes.Anchor;
import attack.dlc.nodes.Circle;
import attack.dlc.nodes.ClipPath;
import attack.dlc.nodes.Defs;
import attack.dlc.nodes.Desc;
import attack.dlc.nodes.Ellipse;
import attack.dlc.nodes.Group;
import attack.dlc.nodes.Image;
import attack.dlc.nodes.Line;
import attack.dlc.nodes.LinearGradient;
import attack.dlc.nodes.Marker;
import attack.dlc.nodes.Mask;
import attack.dlc.nodes.Metadata;
import attack.dlc.nodes.Path;
import attack.dlc.nodes.Pattern;
import attack.dlc.nodes.Polygon;
import attack.dlc.nodes.Polyline;
import attack.dlc.nodes.RadialGradient;
import attack.dlc.nodes.Rect;
import attack.dlc.nodes.SVG;
import attack.dlc.nodes.SVGNode;
import attack.dlc.nodes.SolidColor;
import attack.dlc.nodes.Stop;
import attack.dlc.nodes.Style;
import attack.dlc.nodes.Symbol;
import attack.dlc.nodes.Title;
import attack.dlc.nodes.Use;
import attack.dlc.nodes.View;
import attack.dlc.nodes.animation.Animate;
import attack.dlc.nodes.animation.AnimateTransform;
import attack.dlc.nodes.animation.Set;
import attack.dlc.nodes.filter.DummyFilterPrimitive;
import attack.dlc.nodes.filter.FeBlend;
import attack.dlc.nodes.filter.FeColorMatrix;
import attack.dlc.nodes.filter.FeComponentTransfer;
import attack.dlc.nodes.filter.FeComposite;
import attack.dlc.nodes.filter.FeDisplacementMap;
import attack.dlc.nodes.filter.FeDropShadow;
import attack.dlc.nodes.filter.FeFlood;
import attack.dlc.nodes.filter.FeGaussianBlur;
import attack.dlc.nodes.filter.FeMerge;
import attack.dlc.nodes.filter.FeMergeNode;
import attack.dlc.nodes.filter.FeOffset;
import attack.dlc.nodes.filter.FeTurbulence;
import attack.dlc.nodes.filter.Filter;
import attack.dlc.nodes.filter.TransferFunctionElement;
import attack.dlc.nodes.mesh.MeshGradient;
import attack.dlc.nodes.mesh.MeshPatch;
import attack.dlc.nodes.mesh.MeshRow;
import attack.dlc.nodes.text.Text;
import attack.dlc.nodes.text.TextPath;
import attack.dlc.nodes.text.TextSpan;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NodeSupplier {
   private final Map<String, Supplier<SVGNode>> constructorMap;

   public NodeSupplier() {
      this(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
   }

   public NodeSupplier(@NotNull Map<String, Supplier<SVGNode>> mapImpl) {
      mapImpl.clear();
      this.constructorMap = mapImpl;
      this.constructorMap.put("a", () -> new Anchor());
      this.constructorMap.put("clippath", () -> new ClipPath());
      this.constructorMap.put("defs", () -> new Defs());
      this.constructorMap.put("g", () -> new Group());
      this.constructorMap.put("image", () -> new Image());
      this.constructorMap.put("marker", () -> new Marker());
      this.constructorMap.put("mask", () -> new Mask());
      this.constructorMap.put("svg", () -> new SVG());
      this.constructorMap.put("style", () -> new Style());
      this.constructorMap.put("symbol", () -> new Symbol());
      this.constructorMap.put("use", () -> new Use());
      this.constructorMap.put("view", () -> new View());
      this.populateShapeNodeConstructors();
      this.populatePaintNodeConstructors();
      this.populateTextNodeConstructors();
      this.populateFilterNodeConstructors();
      this.populateAnimationNodeConstructors();
      this.populateMetaNodeConstructors();
      this.populateDummyNodeConstructors();
   }

   @Nullable
   public SVGNode create(@NotNull String tagName) {
      Supplier<SVGNode> supplier = this.constructorMap.get(tagName);
      return supplier == null ? null : supplier.get();
   }

   private void populateShapeNodeConstructors() {
      this.constructorMap.put("circle", () -> new Circle());
      this.constructorMap.put("ellipse", () -> new Ellipse());
      this.constructorMap.put("line", () -> new Line());
      this.constructorMap.put("path", () -> new Path());
      this.constructorMap.put("polygon", () -> new Polygon());
      this.constructorMap.put("polyline", () -> new Polyline());
      this.constructorMap.put("rect", () -> new Rect());
   }

   private void populatePaintNodeConstructors() {
      this.constructorMap.put("lineargradient", () -> new LinearGradient());
      this.constructorMap.put("meshgradient", () -> new MeshGradient());
      this.constructorMap.put("meshpatch", () -> new MeshPatch());
      this.constructorMap.put("meshrow", () -> new MeshRow());
      this.constructorMap.put("pattern", () -> new Pattern());
      this.constructorMap.put("radialgradient", () -> new RadialGradient());
      this.constructorMap.put("solidcolor", () -> new SolidColor());
      this.constructorMap.put("stop", () -> new Stop());
   }

   private void populateTextNodeConstructors() {
      this.constructorMap.put("text", () -> new Text());
      this.constructorMap.put("textpath", () -> new TextPath());
      this.constructorMap.put("tspan", () -> new TextSpan());
   }

   private void populateFilterNodeConstructors() {
      this.constructorMap.put("filter", () -> new Filter());
      this.constructorMap.put("feblend", () -> new FeBlend());
      this.constructorMap.put("fecolormatrix", () -> new FeColorMatrix());
      this.constructorMap.put("fecomposite", () -> new FeComposite());
      this.constructorMap.put("fedisplacementmap", () -> new FeDisplacementMap());
      this.constructorMap.put("fedropshadow", () -> new FeDropShadow());
      this.constructorMap.put("feflood", () -> new FeFlood());
      this.constructorMap.put("fegaussianblur", () -> new FeGaussianBlur());
      this.constructorMap.put("feMerge", () -> new FeMerge());
      this.constructorMap.put("feMergeNode", () -> new FeMergeNode());
      this.constructorMap.put("feturbulence", () -> new FeTurbulence());
      this.constructorMap.put("feOffset", () -> new FeOffset());
      this.constructorMap.put("fecomponenttransfer", () -> new FeComponentTransfer());
      this.constructorMap.put("fefuncb", () -> new TransferFunctionElement.FeFuncB());
      this.constructorMap.put("fefuncg", () -> new TransferFunctionElement.FeFuncG());
      this.constructorMap.put("fefuncr", () -> new TransferFunctionElement.FeFuncR());
      this.constructorMap.put("fefunca", () -> new TransferFunctionElement.FeFuncA());
   }

   private void populateAnimationNodeConstructors() {
      this.constructorMap.put("animate", () -> new Animate());
      this.constructorMap.put("animatetransform", () -> new AnimateTransform());
      this.constructorMap.put("set", () -> new Set());
   }

   private void populateMetaNodeConstructors() {
      this.constructorMap.put("desc", () -> new Desc());
      this.constructorMap.put("metadata", () -> new Metadata());
      this.constructorMap.put("title", () -> new Title());
   }

   private void populateDummyNodeConstructors() {
      this.constructorMap.put("feConvolveMatrix", () -> new DummyFilterPrimitive("feConvolveMatrix"));
      this.constructorMap.put("feDiffuseLightning", () -> new DummyFilterPrimitive("feDiffuseLightning"));
      this.constructorMap.put("feImage", () -> new DummyFilterPrimitive("feImage"));
      this.constructorMap.put("feMorphology", () -> new DummyFilterPrimitive("feMorphology"));
      this.constructorMap.put("feSpecularLighting", () -> new DummyFilterPrimitive("feSpecularLighting"));
      this.constructorMap.put("feTile", () -> new DummyFilterPrimitive("feTile"));
   }
}

