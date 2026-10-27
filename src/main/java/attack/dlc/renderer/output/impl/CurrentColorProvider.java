package attack.dlc.renderer.output.impl;

import attack.dlc.paint.SVGPaint;
import org.jetbrains.annotations.Nullable;

public interface CurrentColorProvider {
   @Nullable
   SVGPaint currentColor();
}
