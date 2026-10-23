package attack.dlc.parser.impl;

import attack.dlc.SVGDocument;
import attack.dlc.nodes.SVG;
import org.jetbrains.annotations.NotNull;

public class DocumentConstructorAccessor {
   private static DocumentConstructorAccessor.DocumentConstructor documentConstructor;

   public static void setDocumentConstructor(@NotNull DocumentConstructorAccessor.DocumentConstructor constructor) {
      if (documentConstructor != null) {
         throw new IllegalStateException("Document constructor already set");
      }

      documentConstructor = constructor;
   }

   static DocumentConstructorAccessor.DocumentConstructor constructor() {
      if (documentConstructor == null) {
         throw new IllegalStateException("Document constructor not set");
      } else {
         return documentConstructor;
      }
   }

   static {
      try {
         Class.forName(SVGDocument.class.getName());
      } catch (ClassNotFoundException e) {
         throw new IllegalStateException(e);
      }
   }

   public interface DocumentConstructor {
      @NotNull
      SVGDocument create(@NotNull SVG var1);
   }
}

