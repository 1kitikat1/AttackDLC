package attack.dlc.nodes.prototype;

import attack.dlc.nodes.SVGNode;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface Container<E> {
   @Internal
   void addChild(@Nullable String var1, @NotNull SVGNode var2);

   List<? extends @NotNull E> children();

   default <T extends E> List<@NotNull T> childrenOfType(Class<T> type) {
      return this.children().stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
   }
}
