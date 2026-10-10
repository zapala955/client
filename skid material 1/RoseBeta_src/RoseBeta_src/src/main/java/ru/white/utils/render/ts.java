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
public class ts extends rt {
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/rect");
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/rect");
   private static final int RECT_SIZE = 224;
   private static final int UNIFORM_RING = 32;
   private static final int MAX_RECTS = 64;
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(ts.VERTEX_SHADER)
         .withFragmentShader(FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("RectData", UniformType.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/rect");
   private final int[] f1523 = new int[9];

   private int m1863(int... colors) {
      int v2 = 0;
      int v3 = 0;

      int v4 = 0;
      int v5 = 0;
      int[] v6 = colors;
      int v7 = colors.length;

      for (int v8 = 0; v8 < v7; v8++) {

         int v9 = v6[v8];
         v5 += v9 >> 24 & 255;
         v2 += v9 >> 16 & 255;
         v3 += v9 >> 8 & 255;
         v4 += v9 & 255;
      }

      int v13 = colors.length;
      int v14 = v5 / v13;

      return v14 << 24 | v2 / v13 << 16 | v3 / v13 << 8 | v4 / v13;
   }

   public ts() {
      super("rect", PIPELINE, "RectData", 64, 224, 32);
   }

   public void drawGlow(float x, float y, float width, float height, int color, float[] radii, float glowSize, float strength, float softness) {
      if (!(width <= 0.0F) && !(height <= 0.0F) && !(glowSize <= 0.0F) && !(strength <= 0.0F)) {
         MinecraftClient v10 = MinecraftClient.method_1551();
         if (v10.method_1522() != null) {
            int v12 = -1915040482;

            for (int v11 = 0; v11 < 9; v11++) {

               this.f1523[v11] = color;
            }

            float v15 = Math.max(0.5F, glowSize);
            this.m1864(x - v15, y - v15, width + v15 * 2.0F, height + v15 * 2.0F, x, y, width, height, 0.0F, radii, glowSize, strength, softness, color);
            this.endItem();
         }
      }
   }

   private void m1862(int[] colors) {
      if (colors.length == 1) {
         for (int v2 = 0; v2 < 9; v2++) {
            this.f1523[v2] = colors[0];
         }
      } else if (colors.length == 4) {
         this.f1523[0] = colors[0];
         this.f1523[1] = this.m1863(colors[0], colors[1]);
         this.f1523[2] = colors[1];
         this.f1523[3] = this.m1863(colors[0], colors[3]);
         int[] temp3 = new int[4];

         temp3[0] = colors[0];
         temp3[1] = colors[1];
         temp3[2] = colors[2];
         temp3[3] = colors[3];
         this.f1523[4] = this.m1863(temp3);
         this.f1523[5] = this.m1863(colors[1], colors[2]);
         this.f1523[6] = colors[3];
         int[] result = this.f1523;
         int temp1 = 7;
         int temp2 = this.m1863(colors[3], colors[2]);

         result[temp1] = temp2;
         this.f1523[8] = colors[2];
      } else if (colors.length >= 9) {
         System.arraycopy(colors, 0, this.f1523, 0, 9);
      } else {
         for (int v5 = 0; v5 < 9; v5++) {
            this.f1523[v5] = colors[v5 % colors.length];
         }
      }
   }

   public void drawRect(float x, float y, float width, float height, int[] colors, float[] radii) {

      this.drawRect(x, y, width, height, colors, radii, 0.0F);
   }

   private void m1864(
      float drawX,
      float drawY,
      float drawWidth,
      float drawHeight,
      float x,
      float y,
      float width,
      float height,
      float innerBlur,
      float[] radii,
      float glowSize,
      float glowStrength,
      float glowSoftness,
      int glowColor
   ) {
      ByteBuffer v15 = this.beginItem();
      v15.putFloat(drawX);
      v15.putFloat(drawY);
      v15.putFloat(drawWidth);
      v15.putFloat(drawHeight);

      v15.putFloat(radii[0]);
      v15.putFloat(radii[1]);
      v15.putFloat(radii[2]);
      v15.putFloat(radii[3]);

      for (int v16 = 0; v16 < 9; v16++) {
         int v17 = this.f1523[v16];
         float v18 = (float)(v17 >> 24 & 255) / 255.0F;
         float v19 = (float)(v17 >> 16 & 255) / 255.0F;
         float v20 = (float)(v17 >> 8 & 255) / 255.0F;
         float v21 = (float)(v17 & 255) / 255.0F;
         v15.putFloat(v19);
         v15.putFloat(v20);
         v15.putFloat(v21);
         v15.putFloat(v18);
      }

      v15.putFloat(x);

      v15.putFloat(y);
      v15.putFloat(width);
      v15.putFloat(height);
      v15.putFloat(glowSize);
      v15.putFloat(glowStrength);
      v15.putFloat(Math.max(0.05F, glowSoftness));
      v15.putFloat(innerBlur);
      v15.putFloat((float)(glowColor >> 16 & 255) / 255.0F);
      v15.putFloat((float)(glowColor >> 8 & 255) / 255.0F);
      v15.putFloat((float)(glowColor & 255) / 255.0F);
      v15.putFloat((float)(glowColor >> 24 & 255) / 255.0F);
   }

   public void drawRect(float x, float y, float width, float height, int[] colors, float[] radii, float innerBlur) {
      MinecraftClient v8 = MinecraftClient.method_1551();
      if (v8.method_1522() != null) {
         this.m1862(colors);

         this.m1864(x, y, width, height, x, y, width, height, innerBlur, radii, 0.0F, 0.0F, 1.0F, 0);
         this.endItem();
      }
   }

   @Override
   public int batchLayer() {
      return 0;
   }

   public void drawGlow(float x, float y, float width, float height, int color, float[] radii, float glowSize, float strength) {
      int v9 = -1811485227;

      this.drawGlow(x, y, width, height, color, radii, glowSize, strength, 1.0F);
   }

   
}
