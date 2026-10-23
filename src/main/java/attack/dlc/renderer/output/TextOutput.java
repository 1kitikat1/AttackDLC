package attack.dlc.renderer.output;

import attack.dlc.nodes.text.NullTextOutput;
import attack.dlc.renderer.RenderContext;
import java.awt.geom.AffineTransform;
import org.jetbrains.annotations.NotNull;

public interface TextOutput {
   void codepoint(@NotNull String var1, @NotNull AffineTransform var2, @NotNull RenderContext var3);

   @NotNull
   static TextOutput createDefault() {
      return NullTextOutput.INSTANCE;
   }

   void beginText();

   void glyphRunBreak();

   void endText();
}
