package attack.dlc.nodes.prototype.impl;

import attack.dlc.attributes.Animatable;
import attack.dlc.attributes.Coordinate;
import attack.dlc.attributes.Inherited;
import attack.dlc.attributes.transform.TransformBox;
import attack.dlc.attributes.value.LengthValue;
import attack.dlc.attributes.value.TransformValue;
import attack.dlc.geometry.size.Length;
import attack.dlc.nodes.ClipPath;
import attack.dlc.nodes.Mask;
import attack.dlc.nodes.filter.Filter;
import attack.dlc.nodes.prototype.HasGeometryContext;
import attack.dlc.parser.impl.AttributeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HasGeometryContextImpl implements HasGeometryContext {
   @Nullable
   private final TransformValue transform;
   @NotNull
   private final Coordinate<LengthValue> transformOrigin;
   @NotNull
   private final TransformBox transformBox;
   @Nullable
   private final ClipPath clipPath;
   @Nullable
   private final Mask mask;
   @Nullable
   private final Filter filter;

   private HasGeometryContextImpl(
      @Nullable TransformValue transform,
      @NotNull Coordinate<LengthValue> transformOrigin,
      @NotNull TransformBox transformBox,
      @Nullable ClipPath clipPath,
      @Nullable Mask mask,
      @Nullable Filter filter
   ) {
      this.transform = transform;
      this.transformOrigin = transformOrigin;
      this.transformBox = transformBox;
      this.clipPath = clipPath;
      this.mask = mask;
      this.filter = filter;
   }

   @NotNull
   public static HasGeometryContext parse(@NotNull AttributeNode attributeNode) {
      return new HasGeometryContextImpl(
         attributeNode.parseTransform("transform", Inherited.NO, Animatable.YES),
         parseTransformOrigin(attributeNode, attributeNode.getStringList("transform-origin")),
         attributeNode.getEnum("transform-box", TransformBox.ViewBox),
         attributeNode.getClipPath(),
         attributeNode.getMask(),
         attributeNode.getFilter()
      );
   }

   @NotNull
   private static Coordinate<LengthValue> parseTransformOrigin(@NotNull AttributeNode attributeNode, @NotNull String[] parts) {
      if (parts.length == 0) {
         return new Coordinate<>(Length.ZERO, Length.ZERO);
      }

      String originX;
      String originY;
      if (parts.length == 1) {
         String val = parts[0];
         if (attributeNode.isVerticalKeyword(val)) {
            originX = "center";
            originY = val;
         } else {
            originX = val;
            originY = "center";
         }
      } else {
         String first = parts[0];
         String second = parts[1];
         if (!attributeNode.isVerticalKeyword(first) && !attributeNode.isHorizontalKeyword(second)) {
            originX = first;
            originY = second;
         } else {
            originX = second;
            originY = first;
         }
      }

      return new Coordinate<>(attributeNode.getHorizontalReferenceLength(originX), attributeNode.getVerticalReferenceLength(originY));
   }

   @Nullable
   @Override
   public ClipPath clipPath() {
      return this.clipPath;
   }

   @Nullable
   @Override
   public Mask mask() {
      return this.mask;
   }

   @Nullable
   @Override
   public Filter filter() {
      return this.filter;
   }

   @Nullable
   @Override
   public TransformValue transform() {
      return this.transform;
   }

   @NotNull
   @Override
   public TransformBox transformBox() {
      return this.transformBox;
   }

   @NotNull
   @Override
   public Coordinate<LengthValue> transformOrigin() {
      return this.transformOrigin;
   }
}

