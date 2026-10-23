package attack.dlc.parser.resources;

import attack.dlc.renderer.PlatformSupport;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface ResourceSupplier<T> {
   @NotNull
   Optional<T> get(@NotNull PlatformSupport var1);
}
