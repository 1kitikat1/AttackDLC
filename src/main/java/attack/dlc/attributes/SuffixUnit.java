package attack.dlc.attributes;

import org.jetbrains.annotations.NotNull;

public interface SuffixUnit<T, V> {
   @NotNull
   String suffix();

   @NotNull
   SuffixUnit<T, V>[] units();

   @NotNull
   V valueOf(float var1);
}
