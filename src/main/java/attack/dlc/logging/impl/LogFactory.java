package attack.dlc.logging.impl;

import attack.dlc.logging.Logger;
import org.jetbrains.annotations.NotNull;

public final class LogFactory {
   private LogFactory() {
   }

   @NotNull
   public static Logger createLogger(@NotNull Class<?> clazz) {
      return LogManagerImpl.logManager().createLogger(clazz.getName());
   }
}

