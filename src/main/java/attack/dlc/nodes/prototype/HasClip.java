package attack.dlc.nodes.prototype;

import attack.dlc.nodes.ClipPath;
import attack.dlc.nodes.Mask;
import org.jetbrains.annotations.Nullable;

public interface HasClip {
   @Nullable
   ClipPath clipPath();

   @Nullable
   Mask mask();
}
