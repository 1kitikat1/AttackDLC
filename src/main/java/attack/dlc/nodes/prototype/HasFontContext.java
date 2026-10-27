package attack.dlc.nodes.prototype;

import attack.dlc.attributes.font.MeasurableFontSpec;
import org.jetbrains.annotations.NotNull;

public interface HasFontContext {
   @NotNull
   Mutator<MeasurableFontSpec> fontSpec();
}
