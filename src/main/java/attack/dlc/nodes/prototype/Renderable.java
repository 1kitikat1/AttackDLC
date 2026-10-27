package attack.dlc.nodes.prototype;

import attack.dlc.parser.impl.AttributeNode;
import attack.dlc.renderer.RenderContext;
import attack.dlc.renderer.output.Output;
import org.jetbrains.annotations.NotNull;

public interface Renderable {
   default boolean requiresInstantiation() {
      return false;
   }

   boolean isVisible(@NotNull RenderContext var1);

   void render(@NotNull RenderContext var1, @NotNull Output var2);

   default boolean parseIsVisible(@NotNull AttributeNode node) {
      return parseVisibility(node);
   }

   static boolean parseVisibility(@NotNull AttributeNode node) {
      return !"none".equals(node.getValue("display")) && !"hidden".equals(node.getValue("visibility")) && !"collapse".equals(node.getValue("visibility"));
   }
}
