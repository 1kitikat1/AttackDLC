package attack.dlc.animation.value;

import attack.dlc.attributes.value.ConstantFloatList;
import attack.dlc.attributes.value.ConstantLengthTransform;
import attack.dlc.attributes.value.FloatListValue;
import attack.dlc.attributes.value.LengthValue;
import attack.dlc.attributes.value.PercentageValue;
import attack.dlc.attributes.value.TransformValue;
import attack.dlc.geometry.size.Length;
import attack.dlc.geometry.size.Percentage;
import org.jetbrains.annotations.NotNull;

public final class NeutralElements {
   @NotNull
   public static final LengthValue NEUTRAL_LENGTH = Length.ZERO;
   @NotNull
   public static final PercentageValue NEUTRAL_PERCENTAGE = Percentage.ONE;
   @NotNull
   public static final TransformValue NEUTRAL_TRANSFORM = ConstantLengthTransform.IDENTITY;
   @NotNull
   public static final FloatListValue NEUTRAL_FLOAT_LIST = ConstantFloatList.EMPTY;

   private NeutralElements() {
   }
}

