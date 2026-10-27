package attack.dlc.nodes;

import attack.dlc.animation.value.AnimatedFloatList;
import attack.dlc.animation.value.AnimatedPath;
import attack.dlc.attributes.Animatable;
import attack.dlc.attributes.Inherited;
import attack.dlc.attributes.value.ConstantFloatList;
import attack.dlc.attributes.value.ConstantValue;
import attack.dlc.attributes.value.FloatListValue;
import attack.dlc.geometry.AWTSVGShape;
import attack.dlc.geometry.FillRuleAwareAWTSVGShape;
import attack.dlc.geometry.SVGShape;
import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.util.PathUtil;
import java.awt.Rectangle;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPolyShape extends ShapeNode {
   @NotNull
   @Override
   protected final SVGShape buildShape(@NotNull AttributeNode attributeNode) {
      FloatListValue points = attributeNode.getFloatList("points", Inherited.NO, Animatable.YES);
      if (points instanceof AnimatedFloatList) {
         return new FillRuleAwareAWTSVGShape(new AnimatedPath((AnimatedFloatList)points, this.doClose()));
      }

      float[] pointsArray = ((ConstantFloatList)points).value();
      return pointsArray.length > 0
         ? new FillRuleAwareAWTSVGShape(new ConstantValue<>(PathUtil.setPolyLine(null, pointsArray, this.doClose())))
         : new AWTSVGShape<>(new Rectangle());
   }

   protected abstract boolean doClose();
}

