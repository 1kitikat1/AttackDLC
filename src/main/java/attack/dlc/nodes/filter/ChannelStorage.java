package attack.dlc.nodes.filter;

import attack.dlc.attributes.filter.FilterChannelKey;
import attack.dlc.util.supplier.ConstantSupplier;
import attack.dlc.util.supplier.LazySupplier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public final class ChannelStorage<T> {
   @NotNull
   private final Map<Object, Supplier<T>> storage = new HashMap<>();

   public void addResult(@NotNull FilterChannelKey key, @NotNull T value) {
      this.storage.put(key.key(), new ConstantSupplier<>(value));
   }

   public void addResult(@NotNull FilterChannelKey key, @NotNull Supplier<T> value) {
      this.storage.put(key.key(), new LazySupplier<>(value));
   }

   @NotNull
   public T get(@NotNull FilterChannelKey key) {
      Supplier<T> provider = this.storage.get(key.key());
      if (provider == null) {
         throw new IllegalFilterStateException("Channel " + key + " not found.");
      } else {
         return provider.get();
      }
   }
}

