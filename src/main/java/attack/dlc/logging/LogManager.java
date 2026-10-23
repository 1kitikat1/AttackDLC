package attack.dlc.logging;

import org.jetbrains.annotations.NotNull;

public interface LogManager {
   @NotNull
   Logger createLogger(@NotNull String var1);
}
