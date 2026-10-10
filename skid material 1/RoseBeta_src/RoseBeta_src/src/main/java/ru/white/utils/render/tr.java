package ru.white.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class tr extends rt {
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/circle_progress");
   private static final int MAX_CIRCLES = 64;
   private static final int UNIFORM_RING = 32;
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/circle_progress");
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/circle_progress");
   private static final int CIRCLE_SIZE = 64;
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("CircleData", UniformType.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );

   public void draw(float centerX, float centerY, float radius, float thickness, float progress, int color) {
      if (!(radius <= 0.0F) && !(thickness <= 0.0F)) {
         MinecraftClient v7 = MinecraftClient.method_1551();
         if (v7.method_1522() != null) {
            ByteBuffer v8 = this.beginItem();
            float v9 = thickness * 0.75F + 1.0F;
            float v10 = (radius + v9) * 2.0F;
            float v11 = centerX - v10 / 2.0F;
            float v12 = centerY - v10 / 2.0F;

            v8.putFloat(v11);
            v8.putFloat(v12);
            v8.putFloat(v10);
            v8.putFloat(v10);
            v8.putFloat(radius + v9);
            v8.putFloat(radius);
            v8.putFloat(thickness);
            float v15 = Math.max(0.0F, Math.min(1.0F, progress));

            v8.putFloat(v15);
            v8.putFloat((float)(color >> 16 & 255) / 255.0F);
            v8.putFloat((float)(color >> 8 & 255) / 255.0F);
            v8.putFloat((float)(color & 255) / 255.0F);
            v8.putFloat((float)(color >> 24 & 255) / 255.0F);
            v8.putFloat(0.0F);
            v8.putFloat(0.0F);
            v8.putFloat(0.0F);
            v8.putFloat(0.0F);
            this.endItem();
         }
      }
   }

   public tr() {
      super(
         "circle_progress",
         PIPELINE,
         "CircleData",
         64,
         64,
         32
      );
   }

   @Override
   public int batchLayer() {
      return 0;
   }

   
}
