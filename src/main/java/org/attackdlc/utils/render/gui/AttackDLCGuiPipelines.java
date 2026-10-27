package org.attackdlc.utils.render.gui;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import sdk.api.optimize.optimize;

@optimize
public final class AttackDLCGuiPipelines {
   public static final RenderPipeline RECT = RenderPipeline.builder(new Snippet[]{RenderPipelines.GUI_SNIPPET})
      .withLocation(Identifier.parse("attackdlc:gui/rect"))
      .withVertexShader(Identifier.parse("attackdlc:core/rect"))
      .withFragmentShader(Identifier.parse("attackdlc:core/rect"))
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .withVertexBinding(0, DefaultVertexFormat.ENTITY)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withCull(false)
      .build();
   public static final RenderPipeline TEXT = RenderPipeline.builder(new Snippet[]{RenderPipelines.GUI_SNIPPET})
      .withLocation(Identifier.parse("attackdlc:gui/text"))
      .withVertexShader(Identifier.parse("attackdlc:core/text"))
      .withFragmentShader(Identifier.parse("attackdlc:core/text"))
      .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .withVertexBinding(0, DefaultVertexFormat.ENTITY)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withCull(false)
      .build();
   public static final RenderPipeline TEXTURE = RenderPipeline.builder(new Snippet[]{RenderPipelines.GUI_SNIPPET})
      .withLocation(Identifier.parse("attackdlc:gui/texture"))
      .withVertexShader(Identifier.parse("attackdlc:core/texture"))
      .withFragmentShader(Identifier.parse("attackdlc:core/texture"))
      .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .withVertexBinding(0, DefaultVertexFormat.ENTITY)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withCull(false)
      .build();
   public static final RenderPipeline GLASS_SHADOW = RenderPipeline.builder(new Snippet[]{RenderPipelines.GUI_SNIPPET})
      .withLocation(Identifier.parse("attackdlc:gui/glass_shadow"))
      .withVertexShader(Identifier.parse("attackdlc:core/rect"))
      .withFragmentShader(Identifier.parse("attackdlc:core/rect"))
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .withVertexBinding(0, DefaultVertexFormat.ENTITY)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withCull(false)
      .build();
   public static final RenderPipeline BLUR_RECT = RenderPipeline.builder(new Snippet[]{RenderPipelines.GUI_SNIPPET})
      .withLocation(Identifier.parse("attackdlc:gui/blur_rect"))
      .withVertexShader(Identifier.parse("attackdlc:core/blur_rect"))
      .withFragmentShader(Identifier.parse("attackdlc:core/blur_rect"))
      .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .withVertexBinding(0, DefaultVertexFormat.ENTITY)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withCull(false)
      .build();
   public static final RenderPipeline COLOR_GRID = RenderPipeline.builder(new Snippet[]{RenderPipelines.GUI_SNIPPET})
      .withLocation(Identifier.parse("attackdlc:gui/color_grid"))
      .withVertexShader(Identifier.parse("attackdlc:core/color_grid"))
      .withFragmentShader(Identifier.parse("attackdlc:core/color_grid"))
      .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .withVertexBinding(0, DefaultVertexFormat.ENTITY)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withCull(false)
      .build();

   private AttackDLCGuiPipelines() {
   }
}

