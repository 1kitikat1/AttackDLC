package attack.dlc.attributes.value;

import attack.dlc.renderer.MeasureContext;
import org.jetbrains.annotations.NotNull;

public interface Value<T> {
   T get(@NotNull MeasureContext var1);
}
