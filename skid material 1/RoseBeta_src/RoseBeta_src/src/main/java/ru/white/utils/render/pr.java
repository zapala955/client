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
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.state.SkyRenderState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.module.impl.render.Atmosphere;

@Environment(EnvType.CLIENT)
public final class pr {
   private final Vector4f f1318;
   private static final float HAZE_DENSITY_SCALE = 0.02F;
   private final Vector4f f1317;
   private SkyRenderState f1319;
   private int f1310 = 0;
   private boolean f1316;
   private ByteBuffer f1312;
   private GpuTextureView f1313;
   private static pr instance;
   private int f1314 = 0;
   private static final Identifier VERTEX_SHADER = Identifier.method_60655("client", "core/atmosphere");
   private static final float DENSITY_SCALE = 0.005F;
   private GpuBuffer[] f1309;
   private GpuBuffer f1311;
   private static final int UNIFORM_RING = 16;
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[0])
         .withLocation(pr.PIPELINE_ID)
         .withVertexShader(VERTEX_SHADER)
         .withFragmentShader(pr.FRAGMENT_SHADER)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("AtmosphereUniforms", UniformType.field_60031)
         .withSampler("DepthSampler")
         .withSampler("SceneSampler")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Identifier FRAGMENT_SHADER = Identifier.method_60655("client", "core/atmosphere");
   private static final int BUFFER_SIZE = 304;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/world/atmosphere");
   private static final float NEAR_PLANE_GUARD = 0.5F;
   private static final double ORIGIN_WRAP = 65536.0;
   private GpuTexture sceneTexture;
   private int f1315 = 0;

   private void m1699(
      qi feature, SkyRenderState skyState, kt storm, Vec3d cameraPos, Matrix4f viewProjection, Matrix4f inverseViewProjection, int width, int height
   ) {
      float v9 = skyState != null ? skyState.field_63094 : 0.0F;
      int v10;
      if (feature.fogColorMode.is("Тема")) {
         v10 = up.getClientColor1(1);
      } else if (feature.fogColorMode.is("Свой")) {
         v10 = feature.fogColor.getValue();
      } else {
         v10 = -6642232;
      }

      if (feature.syncWithSky.getValue() && skyState != null) {
         v10 = m1700(v10, skyState.field_63097, feature.skySync.getValue());
         int v11 = skyState.field_63095;
         int v12 = v11 >> 24 & 255;

         float v13 = Math.min((float)v12 / 255.0F * feature.sunsetBoost.getValue(), 1.0F);
         if (v13 > 0.001F) {
            v10 = m1700(v10, v11, v13);
         }
      }

      boolean v49 = feature.isEnabled();
      boolean v50 = v49 && feature.usesFog();
      float v51 = v50 ? feature.fogDensity.getValue() * 0.005F : 0.0F;
      float v14 = v50 ? feature.ambientStrength.getValue() * (1.0F - 0.55F * v9) : 0.0F;
      if (v50 && feature.stormFog.getValue()) {
         float v15 = storm.intensity();
         v51 *= 1.0F + v15 * 1.35F;
         v14 *= 1.0F - 0.4F * v15;
      }

      float v52 = 1.0F / Math.max(feature.fogHeight.getValue(), 1.0F);
      float v16 = height > 0 ? (float)width / (float)height : 1.0F;
      MinecraftClient v17 = MinecraftClient.method_1551();
      float v18 = skyState != null
         ? skyState.field_63091
         : (
            v17.field_1687 != null
               ? ((float)(v17.field_1687.method_8532() % (2230021899200435938L + -2230021899200411938L)) / 24000.0F - 0.25F) * (float) (Math.PI * 2)
               : 0.0F
         );
      float v19 = (float)Math.cos((double)v18);
      float v20 = -((float)Math.sin((double)v18));
      boolean v21 = v19 >= 0.0F;
      float v22 = v50
         ? (MathHelper.method_15363(v19 * 6.0F, 0.0F, 1.0F) + MathHelper.method_15363(-v19 * 6.0F, 0.0F, 1.0F) * 0.3F) * (1.0F - 0.7F * storm.intensity())
         : 0.0F;
      float v23 = storm.flashStrength() * feature.flashStrength.getValue();
      int v24 = feature.boltColor.getValue();
      float v25 = (float)(v24 >> 16 & 255) / 255.0F;
      float v26 = (float)(v24 >> 8 & 255) / 255.0F;
      float v27 = (float)(v24 & 255) / 255.0F;
      float v28 = 0.0F;
      float v29 = 0.0F;
      float v30 = 0.0F;
      kt.lt v31 = storm.activeStrike();
      if (v31 != null
         && m1702(viewProjection, cameraPos, v31.topX, v31.topY, v31.topZ, this.f1317)
         && m1702(viewProjection, cameraPos, v31.bottomX, v31.bottomY, v31.bottomZ, this.f1318)) {
         v28 = kt.boltEnvelope(v31);
         v29 = v31.seed;
         double v32 = v31.bottomX - cameraPos.field_1352;
         double v34 = v31.bottomY - cameraPos.field_1351;
         double v36 = v31.bottomZ - cameraPos.field_1350;
         v30 = (float)Math.sqrt(v32 * v32 + v34 * v34 + v36 * v36);
      }

      float v53 = Math.max(feature.boltWidth.getValue(), 0.1F) / (float)Math.max(height, 1);
      float v33 = v53 * 6.0F + 0.005F;
      boolean v54 = feature.usesSnow();
      int v35 = feature.rainColor.getValue();
      float v55 = v49 && feature.usesRain() ? Math.min(storm.intensity() * feature.rainDensity.getValue() * storm.skyVisibility() * 0.7F, 0.72F) : 0.0F;
      float v37 = feature.ripples.getValue() && !v54 && v55 > 0.0F ? feature.rippleStrength.getValue() * Math.min(1.0F, v55 * 2.5F) : 0.0F;
      float v38 = MathHelper.method_15363((-v19 + 0.12F) * 3.0F, 0.0F, 1.0F);
      boolean v39 = v49 && feature.usesNightHaze();
      float v40 = feature.hazeNightOnly.getValue() ? v38 : 1.0F;
      float v41 = v39 ? feature.hazeAmount.getValue() * 0.02F * v40 : 0.0F;
      float v42 = feature.hazeGlow.getValue();
      int v43;
      if (feature.hazeColorMode.is("Тема")) {
         v43 = up.getClientColor1(1);
      } else if (feature.hazeColorMode.is("Свой")) {
         v43 = feature.hazeColor.getValue();
      } else {
         v43 = -4930336;
      }

      float v44 = feature.hazeFollowGround.getValue() ? storm.groundHeight() + feature.hazeOffset.getValue() : feature.hazeLevel.getValue();
      float v45 = (float)(cameraPos.field_1351 - (double)v44);
      float v46 = 1.0F / Math.max(feature.hazeThickness.getValue(), 0.5F);
      this.f1312.clear();
      this.f1312
         .putFloat(inverseViewProjection.m00())
         .putFloat(inverseViewProjection.m01())
         .putFloat(inverseViewProjection.m02())
         .putFloat(inverseViewProjection.m03());
      this.f1312
         .putFloat(inverseViewProjection.m10())
         .putFloat(inverseViewProjection.m11())
         .putFloat(inverseViewProjection.m12())
         .putFloat(inverseViewProjection.m13());
      this.f1312
         .putFloat(inverseViewProjection.m20())
         .putFloat(inverseViewProjection.m21())
         .putFloat(inverseViewProjection.m22())
         .putFloat(inverseViewProjection.m23());
      this.f1312
         .putFloat(inverseViewProjection.m30())
         .putFloat(inverseViewProjection.m31())
         .putFloat(inverseViewProjection.m32())
         .putFloat(inverseViewProjection.m33());
      this.f1312.putFloat((float)(v10 >> 16 & 255) / 255.0F);
      this.f1312.putFloat((float)(v10 >> 8 & 255) / 255.0F);
      this.f1312.putFloat((float)(v10 & 255) / 255.0F);
      this.f1312.putFloat(v51);
      this.f1312.putFloat(v52);
      this.f1312.putFloat(v14);
      this.f1312.putFloat(0.0F);
      this.f1312.putFloat(v16);
      this.f1312.putFloat(v50 ? feature.turbulence.getValue() : 0.0F);
      this.f1312.putFloat(feature.sunScatter.getValue());
      this.f1312.putFloat(storm.fogDrift());
      this.f1312.putFloat(storm.intensity());
      this.f1312.putFloat(v21 ? v20 : -v20);
      this.f1312.putFloat(v21 ? v19 : -v19);
      this.f1312.putFloat(0.0F);
      this.f1312.putFloat(v22);
      this.f1312.putFloat((float)m1701(cameraPos.field_1352));
      this.f1312.putFloat((float)cameraPos.field_1351);
      this.f1312.putFloat((float)m1701(cameraPos.field_1350));
      this.f1312.putFloat(v30);
      this.f1312.putFloat(storm.windX());
      this.f1312.putFloat(storm.windZ());
      this.f1312.putFloat(storm.windStrength());
      this.f1312.putFloat(0.0F);
      this.f1312.putFloat(v25 + (1.0F - v25) * 0.3F);
      this.f1312.putFloat(v26 + (1.0F - v26) * 0.3F);
      this.f1312.putFloat(v27 + (1.0F - v27) * 0.3F);
      this.f1312.putFloat(v23);
      this.f1312.putFloat(this.f1317.x);
      this.f1312.putFloat(this.f1317.y);
      this.f1312.putFloat(this.f1318.x);
      this.f1312.putFloat(this.f1318.y);
      this.f1312.putFloat(v25);
      this.f1312.putFloat(v26);
      this.f1312.putFloat(v27);
      this.f1312.putFloat(v33);
      this.f1312.putFloat(v28);
      this.f1312.putFloat(v53);
      this.f1312.putFloat(v29);
      this.f1312.putFloat(feature.boltBranches.getValue());
      this.f1312.putFloat((float)(v35 >> 16 & 255) / 255.0F);
      this.f1312.putFloat((float)(v35 >> 8 & 255) / 255.0F);
      this.f1312.putFloat((float)(v35 & 255) / 255.0F);
      this.f1312.putFloat(feature.rainOpacity.getValue());
      this.f1312.putFloat(v55);
      this.f1312.putFloat(storm.rainPhase());
      this.f1312.putFloat(v54 ? 1.0F : 0.0F);
      this.f1312.putFloat(v54 ? 1.5F : 0.25F);
      this.f1312.putFloat(v37);
      this.f1312.putFloat(storm.ripplePhase());
      this.f1312.putFloat(Math.min(v55 * 1.4F, 0.85F));
      this.f1312.putFloat(0.0F);
      float v56 = (float)(v43 >> 16 & 255) / 255.0F;

      this.f1312.putFloat(v56 * v42);
      this.f1312.putFloat((float)(v43 >> 8 & 255) / 255.0F * v42);
      this.f1312.putFloat((float)(v43 & 255) / 255.0F * v42);
      this.f1312.putFloat(v41);
      this.f1312.putFloat(v46);
      this.f1312.putFloat(v45);
      this.f1312.putFloat(feature.hazeBanks.getValue());
      this.f1312.putFloat(feature.hazeMoonScatter.getValue());
      this.f1312.flip();
   }

   public void render(qi feature, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
      MinecraftClient v5 = MinecraftClient.method_1551();
      Framebuffer v6 = v5.method_1522();
      if (v6 != null && camera != null && positionMatrix != null && projectionMatrix != null) {
         int v7 = v6.field_1482;
         int v8 = v6.field_1481;
         if (v7 > 0 && v8 > 0 && v6.method_30277() != null && v6.method_71640() != null) {
            Vec3d v9 = camera.method_71156();
            kt v10 = kt.INSTANCE;
            v10.update(feature, v9);
            boolean v11 = feature.isEnabled() && feature.usesAtmosphere();
            if (v11 || v10.hasVisibleStrike()) {
               this.m1697();
               this.m1698(v7, v8);
               if (this.sceneTexture != null && this.f1313 != null) {
                  Matrix4f v12 = new Matrix4f(projectionMatrix).mul(positionMatrix);
                  Matrix4f v13 = new Matrix4f(v12).invert();
                  this.m1699(feature, this.f1319, v10, v9, v12, v13, v7, v8);
                  GpuBuffer v14 = this.f1309[this.f1310];
                  this.f1310 = (this.f1310 + 1) % (16);
                  CommandEncoder v15 = RenderSystem.getDevice().createCommandEncoder();
                  v15.copyTextureToTexture(v6.method_30277(), this.sceneTexture, 0, 0, 0, 0, 0, v7, v8);
                  v15.writeToBuffer(v14.slice(), this.f1312);
                  GpuSampler v16 = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
                  GpuSampler v17 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
                  RenderPass v18 = v15.createRenderPass(
                     () -> "client:atmosphere_pass", v6.method_71639(), OptionalInt.empty()
                  );

                  try {
                     v18.setPipeline(PIPELINE);
                     v18.setVertexBuffer(0, this.f1311);
                     v18.setUniform("AtmosphereUniforms", v14);
                     v18.bindTexture("DepthSampler", v6.method_71640(), v16);
                     v18.bindTexture("SceneSampler", this.f1313, v17);
                     v18.draw(0, 6);
                     
                  } catch (Throwable v25) {
                     if (v18 != null) {
                        try {
                           v18.close();
                        } catch (Throwable v24) {
                           v25.addSuppressed(v24);
                        }
                     }

                     throw v25;
                  }

                  if (v18 != null) {
                     v18.close();
                  }
               }
            }
         }
      }
   }

   public void release() {
      if (this.f1313 != null) {
         this.f1313.close();
         this.f1313 = null;
      }

      if (this.sceneTexture != null) {

         this.sceneTexture.close();
         this.sceneTexture = null;
      }

      if (this.f1309 != null) {
         for (GpuBuffer v4 : this.f1309) {
            if (v4 != null) {
               v4.close();
            }
         }

         this.f1309 = null;
      }

      if (this.f1311 != null) {
         this.f1311.close();
         this.f1311 = null;
      }

      if (this.f1312 != null) {
         MemoryUtil.memFree(this.f1312);
         this.f1312 = null;
      }

      this.f1316 = false;
      kt.INSTANCE.reset();
   }

   public static pr getInstance() {
      if (instance == null) {

         instance = new pr();
      }

      return instance;
   }

   private static boolean m1702(Matrix4f viewProjection, Vec3d cameraPos, double x, double y, double z, Vector4f out) {
      float temp1 = (float)(x - cameraPos.field_1352);
      float temp2 = (float)(y - cameraPos.field_1351);

      out.set(temp1, temp2, (float)(z - cameraPos.field_1350), 1.0F);

      viewProjection.transform(out);
      if (out.w <= 0.5F) {
         return false;
      } else {
         out.x = out.x / out.w * 0.5F + 0.5F;
         out.y = out.y / out.w * 0.5F + 0.5F;
         return true;
      }
   }

   private static double m1701(double value) {
      double result = value - Math.floor(value / 65536.0) * 65536.0;

      return result;
   }

   private static int m1700(int c1, int c2, float t) {
      t = MathHelper.method_15363(t, 0.0F, 1.0F);
      int temp1 = 24;

      int v3 = c1 >> temp1 & 255;
      int v4 = c1 >> 16 & 255;
      int v5 = c1 >> 8 & 255;
      int v6 = c1 & 255;

      int v7 = c2 >> 24 & 255;
      int v8 = c2 >> 16 & 255;
      int v9 = c2 >> 8 & 255;
      int v10 = c2 & 255;
      int v11 = (int)((float)v3 + (float)(v7 - v3) * t);
      int v12 = (int)((float)v4 + (float)(v8 - v4) * t);
      float result = (float)v5;
      temp1 = v9 - v5;

      int v13 = (int)(result + (float)temp1 * t);
      int v14 = (int)((float)v6 + (float)(v10 - v6) * t);
      return v11 << 24 | v12 << 16 | v13 << 8 | v14;
   }

   private pr() {
      this.f1316 = false;
      this.f1317 = new Vector4f();
      this.f1318 = new Vector4f();
   }

   public void setSkyRenderState(SkyRenderState state) {
      this.f1319 = state;
   }

   private void m1697() {
      if (!this.f1316) {
         this.f1312 = MemoryUtil.memAlloc(304);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0).flip();
         this.f1311 = RenderSystem.getDevice().createBuffer(() -> {
            int var10001x = -1613360234;

            return StrDec.d("Vn/iQO4SPNFrlP5MGiQJ+ISYm2QZPws7vci3nztA", var10001x);
         }, 32, v1);
         MemoryUtil.memFree(v1);
         this.f1309 = new GpuBuffer[16];
         int v2 = 0;

         while (true) {
            int temp1 = 16;

            if (v2 >= temp1) {
               this.f1316 = true;
               return;
            }

            int v3 = v2;
            GpuBuffer[] result = this.f1309;
            GpuDevice v7 = RenderSystem.getDevice();

            result[v2] = v7.createBuffer(
               () -> "client:atmosphere_uniform_" + v3,
               136,
               -3925567605605908285L + 3925567605605908589L
            );
            v2++;
         }
      }
   }

   private void m1698(int width, int height) {
      if (this.sceneTexture != null && width == this.f1314) {

         if (height == this.f1315) {
            return;
         }
      }

      if (this.f1313 != null) {
         this.f1313.close();
         this.f1313 = null;
      }

      if (this.sceneTexture != null) {
         this.sceneTexture.close();
         this.sceneTexture = null;
      }

      this.sceneTexture = RenderSystem.getDevice().createTexture(() -> {
         int v0 = -918763899;
         int temp3 = v0 * (v0 + 1);

         return "client:atmosphere_scene";
      }, 5, TextureFormat.RGBA8, width, height, 1, 1);
      this.f1313 = RenderSystem.getDevice().createTextureView(this.sceneTexture);
      this.f1314 = width;
      this.f1315 = height;
   }

   
}
