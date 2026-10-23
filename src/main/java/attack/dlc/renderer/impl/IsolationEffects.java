package attack.dlc.renderer.impl;

import attack.dlc.nodes.ClipPath;
import attack.dlc.nodes.Mask;
import attack.dlc.nodes.filter.Filter;
import org.jetbrains.annotations.Nullable;

class IsolationEffects {
   @Nullable
   public final Filter filter;
   @Nullable
   public final Mask mask;
   @Nullable
   public final ClipPath clipPath;

   public IsolationEffects(@Nullable Filter filter, @Nullable Mask mask, @Nullable ClipPath clipPath) {
      this.filter = filter;
      this.mask = mask;
      this.clipPath = clipPath;
   }
}

