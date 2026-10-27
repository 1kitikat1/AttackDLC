package attack.dlc.parser;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DomProcessor {
   void process(@NotNull DomElement var1);
}
