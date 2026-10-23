package attack.dlc.parser.css;

import attack.dlc.parser.DomElement;
import org.jetbrains.annotations.NotNull;

public interface StyleSheet {
   void forEachMatchingRule(@NotNull DomElement var1, @NotNull StyleSheet.RuleConsumer var2);

   interface RuleConsumer {
      void applyRule(@NotNull StyleProperty var1);
   }
}
