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
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class qr implements wr.xr {
   private boolean f1324;
   private static final int UNIFORM_RING = 64;
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(qr.PIPELINE_ID)
         .withVertexShader(qr.VERTEX_SHADER)
         .withFragmentShader(qr.FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("BlurData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private GpuBuffer[] f1320;
   private int f1321 = 0;
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/blur");
   private final ArrayList<GpuBuffer> f1325;
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/blur");
   private static final int BUFFER_SIZE = 128;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/blur");
   private ByteBuffer f1323;
   private GpuBuffer f1322;

   @Override
   public void discardBatch() {

      this.f1325.clear();
   }

   public void drawGlassBlur(
      float x, float y, float width, float height, float alpha, float[] radii, int tintColor, float distortion, float waveSize, float edgeLight, float shine
   ) {

      this.m1711(x, y, width, height, alpha, radii, tintColor, distortion, waveSize, edgeLight, shine);
   }

   private int m1708() {
      MinecraftClient v1 = MinecraftClient.method_1551();
      return v1 != null && v1.method_22683() != null ? (int)Math.ceil((double)v1.method_22683().method_4506() / 2.0) : 540;
   }

   private void m1709() {
      if (!this.f1324) {
         this.f1323 = MemoryUtil.memAlloc(128);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();
         GpuDevice temp1 = RenderSystem.getDevice();
         Supplier temp2 = () -> {

            int v0 = -663684137;
            int var10001x = v0 * (v0 + 1);

            return "client:blur_vertex";
         };

         this.f1322 = temp1.createBuffer(temp2, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1320 = new GpuBuffer[64];
         this.f1324 = true;
      }
   }

   private int m1707() {
      MinecraftClient v1 = MinecraftClient.method_1551();

      return v1 != null && v1.method_22683() != null ? (int)Math.ceil((double)v1.method_22683().method_4489() / 2.0) : 960;
   }

   @Override
   public void drawBatch(RenderPass pass) {
      if (this.f1325.isEmpty()) {
         int v6 = -1785184149;
         int result = v6 * (v6 + 1);

         
      } else if (!et.isReady()) {
         this.f1325.clear();
      } else {
         GpuSampler v2 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
         pass.setPipeline(PIPELINE);
         pass.setVertexBuffer(0, this.f1322);
         GpuTextureView temp2 = et.getBlurredView();

         pass.bindTexture("Sampler0", temp2, v2);

         for (GpuBuffer v4 : this.f1325) {
            pass.setUniform("BlurData", v4);
            pass.draw(0, 6);
         }

         this.f1325.clear();
      }
   }

   public void drawBlur(float x, float y, float width, float height, float radius, float[] radii, int color) {
      int v8 = 1951004273;

      this.m1711(x, y, width, height, radius, radii, color, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   private void m1711(
      float x, float y, float width, float height, float radius, float[] radii, int color, float distortion, float waveSize, float edgeLight, float shine
   ) {
      et.markUsed();
      if (et.isReady()) {
         MinecraftClient v12 = MinecraftClient.method_1551();
         if (v12.method_1522() != null) {
            this.m1709();
            int v13 = et.getCachedWidth();
            int v14 = et.getCachedHeight();
            int v15 = this.m1707();
            int v16 = this.m1708();
            this.m1712(x, y, width, height, (float)v15, (float)v16, v13, v14, 2.0F, radius, radii, color, distortion, waveSize, edgeLight, shine);
            GpuBuffer v17 = this.m1710();
            GpuDevice result = RenderSystem.getDevice();

            result.createCommandEncoder().writeToBuffer(v17.slice(0L, (long)this.f1323.remaining()), this.f1323);
            this.f1325.add(v17);
            if (wr.isEnabled()) {
               wr.register(this);
               if (this.f1325.size() >= 64) {
                  wr.drawImmediate(this);
               }
            } else {
               wr.flushPending();
               wr.drawImmediate(this);
            }
         }
      }
   }

   @Override
   public void uploadBatch(CommandEncoder encoder) {
   }

   private GpuBuffer m1710() {

      GpuBuffer v1 = this.f1320[this.f1321];
      if (v1 == null) {
         int v2 = this.f1321;
         v1 = RenderSystem.getDevice().createBuffer(() -> {
            StringBuilder result = new StringBuilder().append("client:blur_uniform_");

            return result.append(v2).toString();
         }, 136, 7959831864636141446L + -7959831864636141318L);
         this.f1320[this.f1321] = v1;
      }

      this.f1321 = (this.f1321 + 1) % (64);
      return v1;
   }

   public qr() {
      this.f1324 = false;
      this.f1325 = new ArrayList<>();
   }

   private void m1712(
      float x,
      float y,
      float width,
      float height,
      float screenWidth,
      float screenHeight,
      int fbWidth,
      int fbHeight,
      float guiScale,
      float blurRadius,
      float[] radii,
      int color,
      float distortion,
      float waveSize,
      float edgeLight,
      float shine
   ) {
      this.f1323.clear();
      this.f1323.putFloat(x);
      this.f1323.putFloat(y);
      this.f1323.putFloat(width);
      this.f1323.putFloat(height);
      this.f1323.putFloat(screenWidth);

      this.f1323.putFloat(screenHeight);
      this.f1323.putFloat(guiScale);
      this.f1323.putFloat(blurRadius);
      this.f1323.putFloat((float)fbWidth);
      this.f1323.putFloat((float)fbHeight);
      this.f1323.putFloat(0.0F);
      this.f1323.putFloat(0.0F);
      this.f1323.putFloat(radii[0]);
      this.f1323.putFloat(radii[1]);
      this.f1323.putFloat(radii[2]);
      this.f1323.putFloat(radii[3]);

      float v17 = (float)(color >> 24 & 255) / 255.0F;
      float v18 = (float)(color >> 16 & 255) / 255.0F;
      float v19 = (float)(color >> 8 & 255) / 255.0F;
      float v20 = (float)(color & 255) / 255.0F;
      this.f1323.putFloat(v18);
      this.f1323.putFloat(v19);
      this.f1323.putFloat(v20);
      this.f1323.putFloat(v17);
      this.f1323.putFloat(Math.max(0.0F, distortion));
      this.f1323.putFloat(Math.max(0.0F, waveSize));
      this.f1323.putFloat(Math.max(0.0F, edgeLight));
      this.f1323.putFloat(Math.max(0.0F, shine));
      this.f1323.flip();
   }

   public void flush() {
      if (!this.f1325.isEmpty()) {
         wr.drawImmediate(this);
      }
   }

   @Override
   public int batchLayer() {
      return -1;
   }

   public void close() {
      this.f1325.clear();
      if (this.f1320 != null) {
         for (GpuBuffer result : this.f1320) {
            int v5 = -1661540895;
            int temp1 = v5 * (v5 + 1) % 2;

            if (temp1 != 0) {

               }

            GpuBuffer v4 = result;
            if (v4 != null) {
               v4.close();
            }
         }

         this.f1320 = null;
      }

      this.f1321 = 0;
      if (this.f1322 != null) {
         this.f1322.close();
         this.f1322 = null;
      }

      if (this.f1323 != null) {
         MemoryUtil.memFree(this.f1323);
         this.f1323 = null;
      }

      this.f1324 = false;
   }

   
}
