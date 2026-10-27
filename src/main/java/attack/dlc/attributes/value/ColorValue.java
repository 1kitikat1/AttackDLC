package attack.dlc.attributes.value;

import attack.dlc.renderer.MeasureContext;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;

public interface ColorValue {
   @NotNull
   Color get(@NotNull MeasureContext var1);
}
