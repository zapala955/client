package ru.white.utils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class pt implements wr.xr {
   private static final int BUFFER_SIZE = 256;
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/texture");
   private static final int UNIFORM_RING = 64;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/texture");
   private final ArrayList<pt.qt> f1600;
   private static final RenderPipeline GLOW_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(Identifier.method_60655("client", "pipeline/texture_glow"))
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(pt.FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("TextureData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private GpuBuffer[] f1595;
   private int f1596 = 0;
   private ByteBuffer f1598;
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(pt.FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("TextureData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private final Map<GpuTexture, GpuTextureView> f1601;
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/texture");
   private static final float FIXED_GUI_SCALE = 2.0F;
   private boolean f1599 = false;
   private GpuBuffer f1597;

   private void m1929(
      float x,
      float y,
      float w,
      float h,
      float u0,
      float v0,
      float u1,
      float v1,
      float screenWidth,
      float screenHeight,
      float guiScale,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation,
      float shearX
   ) {
      this.f1598.clear();
      this.f1598.putFloat(screenWidth);
      this.f1598.putFloat(screenHeight);
      this.f1598.putFloat(smoothness);
      this.f1598.putFloat(guiScale);
      this.f1598.putFloat(x);
      this.f1598.putFloat(y);
      this.f1598.putFloat(w);

      this.f1598.putFloat(h);
      this.f1598.putFloat(u0);
      this.f1598.putFloat(v0);
      this.f1598.putFloat(u1);
      this.f1598.putFloat(v1);
      this.f1598.putFloat(radii[0]);
      this.f1598.putFloat(radii[1]);

      this.f1598.putFloat(radii[2]);
      this.f1598.putFloat(radii[3]);
      float v17 = (float)Math.toRadians((double)rotation);
      this.f1598.putFloat(v17);

      this.f1598.putFloat(shearX);
      this.f1598.putFloat(0.0F);
      this.f1598.putFloat(0.0F);

      for (int v18 = 0; v18 < 4; v18++) {
         int v19 = v18 < colors.length ? colors[v18] : colors[colors.length - 1];
         float v20 = (float)(v19 >> 24 & 255) / 255.0F;
         float v21 = (float)(v19 >> 16 & 255) / 255.0F;
         float v22 = (float)(v19 >> 8 & 255) / 255.0F;
         float v23 = (float)(v19 & 255) / 255.0F;
         this.f1598.putFloat(v21);
         this.f1598.putFloat(v22);
         this.f1598.putFloat(v23);
         this.f1598.putFloat(v20);
      }

      this.f1598.flip();
   }

   public void drawTexture(
      Identifier textureId,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation,
      float shearX
   ) {
      int v15 = -207601142;

      this.m1928(textureId, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness, rotation, shearX, PIPELINE);
   }

   public void flush() {
      if (this.f1600.isEmpty()) {
         
      } else {
         wr.drawImmediate(this);
      }
   }

   public void close() {
      this.f1600.clear();
      Iterator v1 = this.f1601.values().iterator();

      while (true) {

         if (!v1.hasNext()) {
            this.f1601.clear();
            if (this.f1595 != null) {
               GpuBuffer[] v7 = this.f1595;
               int v8 = v7.length;

               for (int v3 = 0; v3 < v8; v3++) {

                  GpuBuffer v4 = v7[v3];
                  if (v4 != null) {
                     v4.close();
                  }
               }

               this.f1595 = null;
            }

            this.f1596 = 0;
            if (this.f1597 != null) {
               this.f1597.close();
               this.f1597 = null;
            }

            if (this.f1598 != null) {
               MemoryUtil.memFree(this.f1598);
               this.f1598 = null;
            }

            this.f1599 = false;
            return;
         }

         GpuTextureView v2 = (GpuTextureView)v1.next();
         if (!v2.isClosed()) {
            v2.close();
         }
      }
   }

   @Override
   public int batchLayer() {
      return 2;
   }

   private GpuBuffer m1927() {
      GpuBuffer v1 = this.f1595[this.f1596];
      if (v1 == null) {
         int v2 = this.f1596;
         GpuBuffer result = RenderSystem.getDevice().createBuffer(() -> {
            StringBuilder var10000x = new StringBuilder;

            var10000x./* $VF: Unable to resugar constructor */<init>();
            var10000x = var10000x.append("minecraft:texture_uniform_").append(v2);

            return var10000x.toString();
         }, 136, 4592695160320409690L + -4592695160320409434L);

         v1 = result;
         this.f1595[this.f1596] = v1;
      }

      this.f1596 = (this.f1596 + 1) % (64);
      return v1;
   }

   private void m1926() {
      if (!this.f1599) {
         this.f1598 = MemoryUtil.memAlloc(256);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         int v3 = -1148925591;

         v1.putInt(0);
         v1.flip();
         GpuDevice temp1 = RenderSystem.getDevice();
         Supplier v5 = () -> {

            String result = "minecraft:texture_dummy_vertex";
            int v0 = 1590405685;
            int var10001x = v0 * (v0 + 1) % 2;

            if (var10001x != 0) {
               }

            return result;
         };

         this.f1597 = temp1.createBuffer(v5, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1595 = new GpuBuffer[64];
         this.f1599 = true;
      }
   }

   @Override
   public void uploadBatch(CommandEncoder encoder) {
   }

   public void drawTexture(
      Identifier textureId,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation
   ) {
      this.drawTexture(textureId, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness, rotation, 0.0F);
      
   }

   public void drawGlowTexture(
      Identifier textureId,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation
   ) {
      this.drawGlowTexture(textureId, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness, rotation, 0.0F);
   }

   public pt() {
      this.f1600 = new ArrayList<>();
      this.f1601 = new HashMap<>();
   }

   private void m1928(
      Identifier textureId,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation,
      float shearX,
      RenderPipeline pipeline
   ) {
      MinecraftClient v16 = MinecraftClient.method_1551();
      if (v16.method_1522() != null) {
         AbstractTexture v17 = v16.method_1531().method_4619(textureId);
         if (v17 != null) {
            GpuTexture v18 = v17.method_68004();
            if (v18 != null) {
               this.m1926();
               Window result = v16.method_22683();

               float v19 = (float)result.method_4489() / 2.0F;
               float v20 = (float)v16.method_22683().method_4506() / 2.0F;
               this.m1929(x, y, width, height, u0, v0, u1, v1, v19, v20, 2.0F, colors, radii, smoothness, rotation, shearX);
               GpuBuffer v21 = this.m1927();
               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(v21.slice(0L, (long)this.f1598.remaining()), this.f1598);
               this.f1600.add(new pt.qt(pipeline, this.m1930(v18), v21));
               if (wr.isEnabled()) {
                  wr.register(this);
                  if (this.f1600.size() >= 64) {
                     wr.drawImmediate(this);
                  }
               } else {
                  wr.flushPending();
                  wr.drawImmediate(this);
               }
            }
         }
      }
   }

   private GpuTextureView m1930(GpuTexture gpuTexture) {

      GpuTextureView v2 = this.f1601.get(gpuTexture);
      if (v2 == null || v2.isClosed()) {
         this.f1601.entrySet().removeIf(e -> {
            if (!e.getKey().isClosed()) {
               boolean result = e.getValue().isClosed();

               if (!result) {
                  return false;
               }
            }

            if (!e.getValue().isClosed()) {
               Object v4 = e.getValue();

               ((GpuTextureView)v4).close();
            }

            return true;
         });
         v2 = RenderSystem.getDevice().createTextureView(gpuTexture);
         this.f1601.put(gpuTexture, v2);
      }

      return v2;
   }

   @Override
   public void drawBatch(RenderPass pass) {
      if (this.f1600.isEmpty()) {
         
      } else {
         GpuSampler v2 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
         RenderPipeline v3 = null;

         for (pt.qt v5 : this.f1600) {

            if (!v5.view().isClosed()) {
               if (v5.pipeline() != v3) {
                  pass.setPipeline(v5.pipeline());
                  pass.setVertexBuffer(0, this.f1597);
                  v3 = v5.pipeline();
               }

               pass.bindTexture("Sampler0", v5.view(), v2);
               pass.setUniform("TextureData", v5.uniformBuffer());
               pass.draw(0, 6);
            }
         }

         this.f1600.clear();
      }
   }

   public void drawGlowTexture(
      Identifier textureId,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int[] colors,
      float[] radii,
      float smoothness,
      float rotation,
      float shearX
   ) {
      int v15 = -122235405;

      this.m1928(textureId, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness, rotation, shearX, GLOW_PIPELINE);
   }

   @Override
   public void discardBatch() {
      this.f1600.clear();
   }

   public void drawTexture(
      Identifier textureId, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int[] colors, float[] radii, float smoothness
   ) {

      this.drawTexture(textureId, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness, 0.0F, 0.0F);
   }

   @Environment(EnvType.CLIENT)
   private static record qt(RenderPipeline pipeline, GpuTextureView view, GpuBuffer uniformBuffer) {
      private final RenderPipeline f1592;
      private final GpuBuffer f1594;
      private final GpuTextureView f1593;

      public GpuTextureView view() {
         return this.f1593;
      }

      public RenderPipeline pipeline() {

         return this.f1592;
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",pt.qt,"pipeline;view;uniformBuffer",pt.qt::f1592,pt.qt::f1593,pt.qt::f1594>(this);
         int v2 = -97466572;
         int temp2 = v2 * (v2 + 1) % 2;

         if (temp2 != 0) {
            }

         return result;
      }

      private qt(RenderPipeline pipeline, GpuTextureView view, GpuBuffer uniformBuffer) {
         this.f1592 = pipeline;
         this.f1593 = view;
         this.f1594 = uniformBuffer;
      }

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",pt.qt,"pipeline;view;uniformBuffer",pt.qt::f1592,pt.qt::f1593,pt.qt::f1594>(this);
         int v1 = 138793718;

         return result;
      }

      public GpuBuffer uniformBuffer() {
         int v1 = 680979464;
         int temp1 = v1 * (v1 + 1);

         return this.f1594;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",pt.qt,"pipeline;view;uniformBuffer",pt.qt::f1592,pt.qt::f1593,pt.qt::f1594>(this, o);
      }

      
   }
}
