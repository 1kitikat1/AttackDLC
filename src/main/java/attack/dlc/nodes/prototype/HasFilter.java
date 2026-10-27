package attack.dlc.nodes.prototype;

import attack.dlc.nodes.filter.Filter;
import org.jetbrains.annotations.Nullable;

public interface HasFilter {
   @Nullable
   Filter filter();
}
