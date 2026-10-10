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
public class rs extends rt {
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(rs.PIPELINE_ID)
         .withVertexShader(rs.VERTEX_SHADER)
         .withFragmentShader(rs.FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("OutlineData", UniformType.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/outline");
   private static final int OUTLINE_SIZE = 208;
   private static final int MAX_OUTLINES = 64;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/outline");
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/outline");
   private static final int UNIFORM_RING = 32;

   public rs() {
      super("outline", PIPELINE, "OutlineData", 64, 208, 32);
   }

   @Override
   public 

      return 1;
   }

   public void drawOutline(float x, float y, float width, float height, int[] colors, float[] thicknesses, float[] radii, float smoothness) {
      MinecraftClient v9 = MinecraftClient.method_1551();
      if (v9.method_1522() != null) {
         ByteBuffer v10 = this.beginItem();
         v10.putFloat(x);
         v10.putFloat(y);
         v10.putFloat(width);
         v10.putFloat(height);
         v10.putFloat(radii[0]);
         v10.putFloat(radii[1]);
         v10.putFloat(radii[2]);
         v10.putFloat(radii[3]);

         for (int v11 = 0; v11 < 8; v11++) {

            int v12 = v11 < colors.length ? colors[v11] : colors[colors.length - 1];
            float v13 = (float)(v12 >> 24 & 255) / 255.0F;
            float v14 = (float)(v12 >> 16 & 255) / 255.0F;
            float v15 = (float)(v12 >> 8 & 255) / 255.0F;
            float v16 = (float)(v12 & 255) / 255.0F;
            v10.putFloat(v14);
            v10.putFloat(v15);
            v10.putFloat(v16);

            v10.putFloat(v13);
         }

         int v20 = 0;

         while (true) {
            int temp1 = 8;

            if (v20 >= temp1) {
               v10.putFloat(smoothness);
               v10.putFloat(0.0F);
               v10.putFloat(0.0F);
               v10.putFloat(0.0F);
               this.endItem();
               return;
            }

            float v21 = v20 < thicknesses.length ? thicknesses[v20] : thicknesses[thicknesses.length - 1];
            v10.putFloat(v21);
            v20++;
         }
      }
   }

   
}
