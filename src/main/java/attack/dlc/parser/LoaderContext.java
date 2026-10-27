package attack.dlc.parser;

import attack.dlc.parser.css.CssParser;
import attack.dlc.parser.impl.MutableLoaderContext;
import attack.dlc.parser.resources.ResourceLoader;
import attack.dlc.parser.resources.ResourcePolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LoaderContext {
   @Nullable
   DomProcessor preProcessor();

   @NotNull
   CssParser cssParser();

   @NotNull
   PaintParser paintParser();

   @NotNull
   ResourceLoader resourceLoader();

   @NotNull
   ElementLoader elementLoader();

   @NotNull
   ResourcePolicy externalResourcePolicy();

   @NotNull
   DocumentLimits documentLimits();

   @NotNull
   static LoaderContext.Builder builder() {
      return MutableLoaderContext.createDefault();
   }

   @NotNull
   static LoaderContext createDefault() {
      return builder().build();
   }

   interface Builder {
      @NotNull
      LoaderContext.Builder preProcessor(@Nullable DomProcessor var1);

      @NotNull
      LoaderContext.Builder cssParser(@NotNull CssParser var1);

      @NotNull
      LoaderContext.Builder paintParser(@NotNull PaintParser var1);

      @NotNull
      LoaderContext.Builder resourceLoader(@NotNull ResourceLoader var1);

      @NotNull
      LoaderContext.Builder elementLoader(@NotNull ElementLoader var1);

      @NotNull
      LoaderContext.Builder externalResourcePolicy(@NotNull ResourcePolicy var1);

      @NotNull
      LoaderContext.Builder documentLimits(@NotNull DocumentLimits var1);

      @NotNull
      LoaderContext build();
   }
}
