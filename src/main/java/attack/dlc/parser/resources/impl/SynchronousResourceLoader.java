package attack.dlc.parser.resources.impl;

import attack.dlc.parser.DomDocument;
import attack.dlc.parser.resources.RenderableResource;
import attack.dlc.parser.resources.ResourceLoader;
import attack.dlc.parser.resources.ResourceSupplier;
import attack.dlc.util.ResourceUtil;
import java.io.IOException;
import java.net.URI;
import org.jetbrains.annotations.NotNull;

public final class SynchronousResourceLoader implements ResourceLoader {
   @NotNull
   @Override
   public ResourceSupplier<RenderableResource> loadImage(@NotNull DomDocument document, @NotNull URI uri) throws IOException {
      return new ValueResourceSupplier<>(ResourceUtil.loadImage(document, uri));
   }
}

