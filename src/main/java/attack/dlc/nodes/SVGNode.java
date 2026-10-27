package attack.dlc.nodes;

import attack.dlc.parser.TextContent;
import attack.dlc.parser.impl.AttributeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SVGNode {
   @NotNull
   String tagName();

   @Nullable
   String id();

   void build(@NotNull AttributeNode var1);

   default void addContent(@NotNull TextContent.Segment content) {
   }
}
