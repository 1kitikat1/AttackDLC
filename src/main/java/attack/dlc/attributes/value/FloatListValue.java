package attack.dlc.attributes.value;

import attack.dlc.renderer.MeasureContext;
import org.jetbrains.annotations.NotNull;

public interface FloatListValue {
   float @NotNull [] get(@NotNull MeasureContext var1);
}
