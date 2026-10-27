package attack.dlc.nodes.prototype;

import attack.dlc.attributes.VectorEffect;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface HasVectorEffects {
   @NotNull
   Set<VectorEffect> vectorEffects();
}
