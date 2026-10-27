package attack.dlc.nodes;

import attack.dlc.nodes.prototype.spec.ElementCategories;
import attack.dlc.nodes.prototype.spec.PermittedContent;
import attack.dlc.parser.TextContent;
import attack.dlc.parser.css.CssParser;
import attack.dlc.parser.css.StyleSheet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@ElementCategories({})
@PermittedContent(any = true, charData = true)
public final class Style extends MetaSVGNode {
   public static final String TAG = "style";
   private StyleSheet styleSheet;
   private final List<char @NotNull []> data = new ArrayList<>();

   public void parseStyleSheet(@NotNull CssParser cssParser) {
      this.styleSheet = cssParser.parse(this.data);
      this.data.clear();
   }

   @NotNull
   public StyleSheet styleSheet() {
      return Objects.requireNonNull(this.styleSheet);
   }

   @Override
   public void addContent(@NotNull TextContent.Segment segment) {
      if (!segment.isConstant()) {
         throw new UnsupportedOperationException("Style elements only support constant text. Encountered: " + segment);
      }

      String content = segment.text();
      this.data.add(content.toCharArray());
   }

   @NotNull
   @Override
   public String tagName() {
      return "style";
   }
}

