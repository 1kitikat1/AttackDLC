package org.attackdlc.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.SamplerCache;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.attackdlc.utils.render.gui.GuiBackdrop;
import org.attackdlc.utils.render.gui.GuiPipelines;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
   @Shadow
   @Final
   private List<?> draws;
   @Unique
   private final IntList attackdlc$glassSplits = new IntArrayList();
   @Unique
   private boolean attackdlc$previousElementWasGlass;

   @Inject(method = "prepare", at = @At("HEAD"))
   private void attackdlc$resetGlassSplits(CallbackInfo ci) {
      this.attackdlc$glassSplits.clear();
      this.attackdlc$previousElementWasGlass = false;
   }

   @Inject(method = "addElementToMesh", at = @At("HEAD"))
   private void attackdlc$markGlassRuns(GuiElementRenderState elementState, CallbackInfo ci) {
      boolean glass = attackdlc$isGlassPipeline(elementState.pipeline());
      if (glass && !this.attackdlc$previousElementWasGlass) {
         this.attackdlc$glassSplits.add(this.draws.size());
      }

      this.attackdlc$previousElementWasGlass = glass;
   }

   @WrapOperation(
      method = "draw",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;II)V"
      )
   )
   private void attackdlc$captureBackdropBeforeGlassRuns(
      GuiRenderer instance,
      Supplier<String> label,
      RenderTarget mainRenderTarget,
      GpuBufferSlice dynamicTransforms,
      int startIndex,
      int endIndex,
      Operation<Void> original
   ) {
      int cursor = startIndex;

      for (int i = 0; i < this.attackdlc$glassSplits.size(); i++) {
         int split = this.attackdlc$glassSplits.getInt(i);
         if (split >= cursor && split < endIndex) {
            if (split > cursor) {
               original.call(new Object[]{instance, label, mainRenderTarget, dynamicTransforms, cursor, split});
            }

            GuiBackdrop.captureNow();
            cursor = split;
         }
      }

      if (cursor < endIndex) {
         original.call(new Object[]{instance, label, mainRenderTarget, dynamicTransforms, cursor, endIndex});
      }
   }

   @Unique
   private static boolean attackdlc$isGlassPipeline(RenderPipeline pipeline) {
      return pipeline == GuiPipelines.BLUR_RECT || pipeline == GuiPipelines.GLASS_SHADOW;
   }

   @WrapOperation(
      method = "submitBlitFromItemAtlas",
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/systems/SamplerCache;getRepeat(Lcom/mojang/blaze3d/textures/FilterMode;)Lcom/mojang/blaze3d/textures/GpuSampler;"
      )
   )
   private GpuSampler attackdlc$smoothDownscaledItems(
      SamplerCache cache, FilterMode filterMode, Operation<GpuSampler> original, @Local(argsOnly = true) GuiItemRenderState itemState
   ) {
      return attackdlc$isDownscaled(itemState)
         ? (GpuSampler)original.call(new Object[]{cache, FilterMode.LINEAR})
         : (GpuSampler)original.call(new Object[]{cache, filterMode});
   }

   @WrapOperation(
      method = "submitBlitFromItemAtlas",
      at = @At(
         value = "FIELD",
         target = "Lnet/minecraft/client/renderer/RenderPipelines;GUI_TEXTURED_PREMULTIPLIED_ALPHA:Lcom/mojang/blaze3d/pipeline/RenderPipeline;"
      )
   )
   private RenderPipeline attackdlc$bicubicDownscalePipeline(Operation<RenderPipeline> original, @Local(argsOnly = true) GuiItemRenderState itemState) {
      return attackdlc$isDownscaled(itemState)
         ? GuiPipelines.itemDownscale(Minecraft.getInstance().getWindow().getGuiScale())
         : (RenderPipeline)original.call(new Object[0]);
   }

   @Unique
   private static boolean attackdlc$isDownscaled(GuiItemRenderState itemState) {
      return false;
   }
}

