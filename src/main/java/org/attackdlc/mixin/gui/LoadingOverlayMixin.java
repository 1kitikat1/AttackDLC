package org.attackdlc.mixin.gui;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.attackdlc.context.RenderContext;
import org.attackdlc.utils.ColorUtil;
import org.attackdlc.utils.render.Theme;
import org.attackdlc.utils.render.gui.Render2DUtil;
import org.attackdlc.utils.render.gui.TextAlign;
import org.attackdlc.utils.render.gui.UiFontStyle;

@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {
   @Shadow
   @Final
   private Minecraft minecraft;
   @Shadow
   @Final
   private ReloadInstance reload;
   @Shadow
   @Final
   private Consumer<Optional<Throwable>> onFinish;
   @Shadow
   @Final
   private boolean fadeIn;
   @Shadow
   private float currentProgress;
   @Shadow
   private long fadeOutStart;
   @Shadow
   private long fadeInStart;
   @Unique
   private long lastTime = -1L;
   @Unique
   private float animTime = 0.0F;

   @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
   private void onExtractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      ci.cancel();
      if (Thread.currentThread().getPriority() != 10) {
         try {
            Thread.currentThread().setPriority(10);
         } catch (Throwable var40) {
         }
      }

      int width = this.minecraft.getWindow().getGuiScaledWidth();
      int height = this.minecraft.getWindow().getGuiScaledHeight();
      long currentTime = Util.getMillis();
      if (this.fadeIn && this.fadeInStart == -1L) {
         this.fadeInStart = currentTime;
      }

      if (RenderContext.overlayStartTime == -1L) {
         RenderContext.overlayStartTime = currentTime;
      }

      long elapsed = currentTime - RenderContext.overlayStartTime;
      boolean isStartup = this.minecraft.gui.screen() == null;
      long animStartDelay = isStartup ? 1200L : 0L;
      if (this.lastTime == -1L) {
         this.lastTime = currentTime;
      }

      float deltaTime = (float)(currentTime - this.lastTime) / 1000.0F;
      this.lastTime = currentTime;
      float progressDelta = Math.min(deltaTime, 0.03F);
      if (elapsed >= animStartDelay) {
         this.animTime += progressDelta;
      }

      float targetProgress;
      if (elapsed < animStartDelay) {
         targetProgress = 0.0F;
      } else if (!this.reload.isDone()) {
         float progressTime = Math.max(0.0F, (float)(elapsed - animStartDelay) / 1000.0F);
         float simulated = Mth.clamp(progressTime * 0.51F, 0.0F, 0.92F);
         targetProgress = Math.max(simulated, this.reload.getActualProgress() * 0.92F);
      } else {
         targetProgress = 1.0F;
      }

      float catchUpSpeed = !this.reload.isDone() ? 0.2F : 0.5F;
      if (this.currentProgress < targetProgress) {
         this.currentProgress = Math.min(this.currentProgress + catchUpSpeed * progressDelta, targetProgress);
      } else {
         this.currentProgress = Mth.clamp(this.currentProgress * 0.98F + targetProgress * 0.02F, 0.0F, 1.0F);
      }

      float fadeOutProgress = this.fadeOutStart > -1L ? (float)(currentTime - this.fadeOutStart) / 1500.0F : 0.0F;
      if (fadeOutProgress >= 1.0F) {
         this.minecraft.gui.setOverlay(null);
         RenderContext.overlayStartTime = -1L;
      } else {
         if (this.reload.isDone() && this.minecraft.gui.screen() != null) {
            this.minecraft.gui.screen().extractRenderStateWithTooltipAndSubtitles(guiGraphicsExtractor, mouseX, mouseY, partialTick);
         }

         float alpha = 1.0F;
         if (this.fadeOutStart > -1L) {
            alpha = Mth.clamp(1.0F - fadeOutProgress, 0.0F, 1.0F);
         } else if (this.fadeIn && this.fadeInStart > -1L) {
            float fadeInProgress = (float)(currentTime - this.fadeInStart) / 1000.0F;
            alpha = Mth.clamp(fadeInProgress, 0.0F, 1.0F);
         }

         int bgAlpha = Math.round(255.0F * alpha);
         int bgCol = ColorUtil.rgba(10, 11, 14, bgAlpha);
         RenderContext.enter2D(null, guiGraphicsExtractor, null);

         try {
            Render2DUtil.beginFrame();
            float centerX = width / 2.0F;
            float centerY = height / 2.0F;
            Render2DUtil.rect(0.0F, 0.0F, width, height).color(bgCol).draw();
            int accentColor = Theme.getAccent();
            float textAlpha = Math.round(255.0F * alpha);
            int brandColor = ColorUtil.rgba(232, 234, 238, textAlpha);
            int mutedColor = ColorUtil.rgba(120, 124, 134, textAlpha);
            Render2DUtil.text(centerX, centerY - 44.0F, 17.0F, "ATTACKDLC")
               .style(UiFontStyle.SEMIBOLD)
               .align(TextAlign.CENTER)
               .color(brandColor)
               .draw();
            Render2DUtil.text(centerX, centerY - 21.0F, 8.0F, "MINECRAFT 26.2  ·  FABRIC")
               .style(UiFontStyle.MEDIUM)
               .align(TextAlign.CENTER)
               .color(mutedColor)
               .draw();
            float barWidth = 240.0F;
            float barHeight = 2.0F;
            float barX = centerX - barWidth / 2.0F;
            float barY = centerY + 20.0F;
            if (this.fadeOutStart > -1L) {
               barY += 12.0F * fadeOutProgress;
            }

            int trackColor = ColorUtil.rgba(255, 255, 255, Math.round(20.0F * alpha));
            Render2DUtil.rect(barX, barY, barWidth, barHeight).color(trackColor).radius(barHeight).draw();
            int accentFadeColor = ColorUtil.withAlpha(accentColor, Math.round(255.0F * alpha));
            float fillWidth = barWidth * this.currentProgress;
            if (fillWidth > 0.0F) {
               Render2DUtil.rect(barX, barY, fillWidth, barHeight).color(accentFadeColor).radius(barHeight).draw();
            }

            String statusText;
            if (this.reload.isDone()) {
               statusText = "Почти готово";
            } else {
               statusText = "Загрузка";
               int dots = (int)(currentTime / 400L) % 4;

               for (int i = 0; i < dots; i++) {
                  statusText = statusText + ".";
               }
            }

            Render2DUtil.text(barX, barY + 12.0F, 9.0F, statusText)
               .style(UiFontStyle.REGULAR)
               .color(mutedColor)
               .draw();
            String percentText = Math.round(this.currentProgress * 100.0F) + "%";
            Render2DUtil.text(barX + barWidth, barY + 12.0F, 9.0F, percentText)
               .style(UiFontStyle.REGULAR)
               .align(TextAlign.RIGHT)
               .color(mutedColor)
               .draw();
            Render2DUtil.flush();
         } finally {
            RenderContext.exit2D();
         }
      }
   }
}

