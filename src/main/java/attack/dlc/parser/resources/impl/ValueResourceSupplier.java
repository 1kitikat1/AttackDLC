package attack.dlc.parser.resources.impl;

import attack.dlc.parser.resources.ResourceSupplier;
import attack.dlc.renderer.PlatformSupport;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public final class ValueResourceSupplier<T> implements ResourceSupplier<T> {
   private final T value;

   public ValueResourceSupplier(T value) {
      this.value = value;
   }

   @NotNull
   @Override
   public Optional<T> get(@NotNull PlatformSupport platformSupport) {
      return Optional.of(this.value);
   }

   public T get() {
      return this.value;
   }
}

