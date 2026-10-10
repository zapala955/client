package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.io.IOException;
import java.io.InputStream;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.ChatUtil;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Penguin Companion",
   desc = "Renders the gugu gaga penguin model near the player",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class PenguinCompanion extends Module {
   private float f718;
   private Vec3d f716;
   private float f717;
   private final BufferAllocator f713;
   private static final RenderPipeline MODEL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56864})
         .withLocation(Identifier.method_60655("client", "nanaly_model"))
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27379)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   private fk.hk f714;
   private static final Logger LOGGER = LoggerFactory.getLogger("client/PenguinCompanion");
   private static final String TEXTURE_PREFIX = "model_new/textures/";
   private long f720;
   private float f719;
   private static final Identifier MODEL_ID = Identifier.method_60655("client", "model_new/companion_mesh.bin");
   private static final Map<Identifier, RenderLayer> LAYERS = new HashMap<>();
   private boolean f715;
   public SliderSetting scale = new SliderSetting(this, "Scale", 2.45F, 0.35F, 4.0F, 0.05F);

   private fk.hk m794() {
      if (this.f714 == null && !this.f715) {
         this.f715 = true;

         try {
            Optional v1 = MinecraftClient.method_1551().method_1478().method_14486(MODEL_ID);
            if (v1.isEmpty()) {
               LOGGER.warn("Nanaly mesh not found: {}", MODEL_ID);
               if (mc.field_1724 != null) {
                  String v5 = String.valueOf(MODEL_ID);
                  zp.addChatMessage("§cPenguin Companion: mesh не найден §7" + v5);
               }

               return null;
            }

            try (InputStream v2 = ((Resource)v1.get()).method_14482()) {
               this.f714 = fk.hk.read(v2.readAllBytes());
               LOGGER.info("Loaded Nanaly mesh with {} groups", this.f714.groups.length);

               if (mc.field_1724 != null) {
                  int v6 = this.f714.groups.length;
                  zp.addChatMessage("§aPenguin Companion: mesh загружен §7(" + v6 + " groups)");
               }
            }
         } catch (RuntimeException | IOException v11) {
            LOGGER.error("Failed to load Nanaly mesh", v11);
            if (mc.field_1724 != null) {
               int temp1 = -1334399824;

               zp.addChatMessage(
                  StrDec.d("0QtIWsO3wepQKBFTI568xKkRMFBozQL1PJq1fKg2KOsrtKMyuWG+pyL/GSmni5PdFQAPtlcgSUtp2hwtANOAh5J+KtomsVajCOKEM+gN8Jpw", temp1)
               );
            }
         }

         return this.f714;
      } else {
         return this.f714;
      }
   }

   private Vec3d m787(LivingEntity followTarget, float tickDelta) {
      Vec3d result = followTarget.method_30950(tickDelta);

      Vec3d v3 = result;
      if (followTarget == mc.field_1724) {
         float v11 = mc.field_1724.method_36454();
         double v5 = Math.toRadians((double)v11);
         Vec3d v7 = new Vec3d(-Math.sin(v5), 0.0, Math.cos(v5));
         return v3.method_1019(v7.method_1021(1.15));
      } else {
         Vec3d v4 = v3.method_1020(mc.field_1724.method_30950(tickDelta));
         if (v4.method_37268() < 1.0E-4) {
            v4 = new Vec3d(0.0, 0.0, 1.0);
         } else {
            v4 = v4.method_1029();
            
         }

         return v3.method_1019(v4.method_1021(0.95));
      }
   }

   private static float[] m789(fk.hk model, Identifier texture, float sourceX, float sourceY, float x, float y, float z) {
      float v7 = (sourceY - model.minY) / Math.max(model.maxY - model.minY, 1.0E-4F);
      float v8 = Math.abs(sourceX);
      String v9 = texture.method_12832();
      boolean v10 = v9.contains("cloth");
      if (v10 && !(v8 < 0.16F) && !(v7 < 0.22F) && !(v7 > 0.72F)) {
         float v11 = Math.signum(sourceX);
         float v12 = m792(0.16F, 0.42F, v8);

         float v13 = m793(v7, 0.22F, 0.34F, 0.58F, 0.72F);
         float v14 = v12 * v13 * 0.72F;
         float v15 = v11 * 0.15F;
         float v16 = z + 0.08F;
         return new float[]{MathHelper.method_16439(v14, x, v15), y - 0.015F * v14, MathHelper.method_16439(v14, z, v16)};
      } else {
         return new float[]{x, y, z};
      }
   }

   private static float m792(float edge0, float edge1, float value) {
      float v3 = MathHelper.method_15363((value - edge0) / Math.max(edge1 - edge0, 1.0E-4F), 0.0F, 1.0F);
      float result = v3 * v3;

      float temp2 = 2.0F * v3;

      return result * (3.0F - temp2);
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         fk.hk v2 = this.m794();
         if (v2 != null) {
            MatrixStack v3 = e.getMatrixStack();
            Vec3d v4 = mc.field_1773.method_19418().method_71156();
            LivingEntity v5 = this.m786();
            Vec3d v6 = this.m787(v5, e.getTickDelta());
            this.m788(v6, v5);
            Vec3d v7 = this.f716;

            Vec3d v8 = v7.method_1020(v4);
            v3.method_22903();
            v3.method_22904(v8.field_1352, v8.field_1351, v8.field_1350);
            v3.method_22907(RotationAxis.field_40716.rotationDegrees(180.0F - this.f717));
            v3.method_22905(this.scale.getValue(), this.scale.getValue(), this.scale.getValue());
            Matrix4f v9 = v3.method_23760().method_23761();
            Immediate v10 = VertexConsumerProvider.method_22991(this.f713);

            for (fk.gk v14 : v2.groups) {
               VertexConsumer v15 = v10.method_73477(m795(v14.texture));

               for (byte v16 = 0; v16 < v14.f712.length; v16 += 8) {
                  float v17 = v14.f712[v16];
                  float v18 = v14.f712[v16 + 1];
                  float v19 = v14.f712[v16 + 2];
                  float v20 = v14.f712[v16 + 3];
                  float v21 = v14.f712[v16 + 4];
                  float v23 = v19 - v2.minY;
                  float v24 = -(v18 - v2.centerDepth);
                  float[] v25 = m789(v2, v14.texture, v17, v19, v17, v23, v24);

                  float[] v26 = m790(v2, v14.texture, v17, v19, v25[0], v25[1], v25[2], this.f718, this.f719);
                  v15.method_22918(v9, v26[0], v26[1], v26[2])
                     .method_1336(255, 255, 255, 255)
                     .method_22913(v20, v21);
               }
            }

            v10.method_22993();
            v3.method_22909();
         }
      }
   }

   private void m788(Vec3d desiredPos, LivingEntity lookTarget) {
      long v3 = System.nanoTime();
      float v5 = this.f720 == 0L ? 0.05F : MathHelper.method_15363((float)(v3 - this.f720) / 1.0E9F, 0.0F, 0.08F);

      this.f720 = v3;
      Vec3d v6 = this.f716;
      if (this.f716 != null && !(this.f716.method_1025(desiredPos) > 256.0)) {
         Vec3d v7 = desiredPos.method_1020(this.f716);
         double v8 = Math.sqrt(v7.field_1352 * v7.field_1352 + v7.field_1350 * v7.field_1350);
         if (v8 > 0.24) {
            double v10 = lookTarget == mc.field_1724 ? 4.6 : 6.2;
            double v12 = Math.min(v8, v10 * (double)v5);
            this.f716 = this.f716.method_1019(new Vec3d(v7.field_1352 / v8 * v12, 0.0, v7.field_1350 / v8 * v12));
         }

         this.f716 = new Vec3d(this.f716.field_1352, desiredPos.field_1351, this.f716.field_1350);
      } else {
         this.f716 = desiredPos;
      }

      double v17 = 0.0;
      if (v6 != null) {
         double v9 = this.f716.field_1352 - v6.field_1352;
         double v11 = this.f716.field_1350 - v6.field_1350;
         double v22 = v9 * v9 + v11 * v11;

         v17 = Math.sqrt(v22);
         
      }

      float v18 = v5 <= 0.0F ? 0.0F : MathHelper.method_15363((float)(v17 / (double)v5) * 0.22F, 0.0F, 1.0F);
      this.f719 = MathHelper.method_16439(0.2F, this.f719, v18);
      if (this.f719 < 0.03F) {
         this.f719 = 0.0F;
      }

      this.f718 += (float)v17 * 9.0F;
      Vec3d v19 = lookTarget.method_73189().method_1031(0.0, (double)lookTarget.method_17682() * 0.45, 0.0);
      Vec3d v20 = v19.method_1020(this.f716);
      float v21 = (float)Math.toDegrees(Math.atan2(v20.field_1352, v20.field_1350));
      this.f717 = m791(this.f717, v21, 0.2F);
   }

   private static float m793(float value, float fadeInStart, float fadeInEnd, float fadeOutStart, float fadeOutEnd) {
      float result = m792(fadeInStart, fadeInEnd, value);

      result *= 1.0F - m792(fadeOutStart, fadeOutEnd, value);

      return result;
   }

   @Override
   protected void onEnable() {
      this.f716 = null;
      this.f718 = 0.0F;
      this.f719 = 0.0F;
   }

   private static float[] m790(fk.hk model, Identifier texture, float sourceX, float sourceY, float x, float y, float z, float cycle, float amount) {
      if (amount <= 0.0F) {
         return new float[]{x, y, z};
      } else {
         float result = sourceY - model.minY;
         float temp1 = model.maxY - model.minY;

         float v9 = result / Math.max(temp1, 1.0E-4F);
         float v10 = Math.abs(sourceX);
         float v11 = sourceX >= 0.0F ? 1.0F : -1.0F;
         float v12 = MathHelper.method_15374((double)(cycle + (v11 > 0.0F ? 0.0F : (float) Math.PI)));
         float v13 = Math.max(v12, 0.0F);
         String v14 = texture.method_12832();
         if (v14.contains("cloth") && v9 < 0.2F && v10 > 0.025F && v10 < 0.22F) {
            float v19 = m793(v9, 0.0F, 0.03F, 0.16F, 0.22F);
            return new float[]{x + v11 * 0.01F * v13 * v19 * amount, y + 0.045F * v13 * v19 * amount, z + v12 * 0.07F * v19 * amount};
         } else if (v14.contains("cloth") && v9 >= 0.22F && v9 <= 0.62F && v10 > 0.16F) {
            float v18 = m793(v9, 0.22F, 0.34F, 0.54F, 0.66F) * m792(0.16F, 0.38F, v10);
            return new float[]{x + v11 * 0.012F * v12 * v18 * amount, y + 0.01F * v13 * v18 * amount, z - v12 * 0.035F * v18 * amount};
         } else if (v9 > 0.18F && v9 < 0.78F && v10 < 0.34F) {
            float v15 = m793(v9, 0.18F, 0.36F, 0.62F, 0.78F);
            float[] v20 = new float[]{x + v11 * 0.002F * v12 * v15 * amount, y, 0.0F};
            float temp4 = Math.abs(v12) * 0.01F * v15 * amount;

            v20[2] = z - temp4;
            return v20;
         } else {
            return new float[]{x, y, z};
         }
      }
   }

   private static float m791(float from, float to, float delta) {
      float result = to - from;

      float v3 = MathHelper.method_15393(result);
      return from + v3 * delta;
   }

   private LivingEntity m786() {
      fg v1 = fg.get();
      if (v1 != null) {
         boolean result = v1.isEnabled();

         if (result && fg.target != null && fg.target.method_5805()) {

            return fg.target;
         }
      }

      return mc.field_1724;
   }

   public PenguinCompanion() {
      this.f713 = new BufferAllocator(1048576);
   }

   private static RenderLayer m795(Identifier texture) {

      return LAYERS.computeIfAbsent(
         texture,
         id -> {
            String v1x = id.method_12832()
               .replace((char)(47), (char)(95))
               .replace((char)(46), (char)(95));
            StringBuilder result = new StringBuilder;

            result./* $VF: Unable to resugar constructor */<init>();

            result = result.append("nanaly_model_").append((Object)v1x);

            return RenderLayer.method_75940(
               result.toString(),
               RenderSetup.method_75927(MODEL_PIPELINE).method_75934("Sampler0", id).method_75937().method_75929(1048576).method_75938()
            );
         }
      );
   }

   @Environment(EnvType.CLIENT)
   private static record gk(Identifier texture, float[] vertices) {
      private final float[] f712;

      private gk(Identifier texture, float[] vertices) {
         this.texture = texture;
         this.f712 = vertices;
      }

      public Identifier texture() {

         return this.texture;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",fk.gk,"texture;vertices",fk.gk::texture,fk.gk::f712>(this, o);
      }

      public float[] vertices() {

         return this.f712;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static class hk {
      final float centerDepth;
      final float maxY;
      final float minY;
      final fk.gk[] groups;

      static fk.hk read(byte[] bytes) {
         ByteBuffer v1 = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
         byte[] v2 = new byte[5];
         v1.get(v2);
         if (!"NMSH1".equals(new String(v2, StandardCharsets.US_ASCII))) {
            throw new IllegalArgumentException("Bad Nanaly mesh magic");
         } else {
            int v3 = v1.getInt();
            fk.gk[] v4 = new fk.gk[v3];
            float v5 = Float.POSITIVE_INFINITY;
            float v6 = Float.NEGATIVE_INFINITY;
            float v7 = Float.POSITIVE_INFINITY;
            float v8 = Float.NEGATIVE_INFINITY;

            for (int v9 = 0; v9 < v3; v9++) {
               int v10 = Short.toUnsignedInt(v1.getShort());
               byte[] v11 = new byte[v10];
               v1.get(v11);
               String v12 = new String(v11, StandardCharsets.UTF_8);
               int v13 = v1.getInt();
               float[] v14 = new float[v13 * (8)];

               while (v15 < v14.length) {
                  v14[v15] = v1.getFloat();
                  v15++;
               }

               for (byte v20 = 0; v20 < v14.length; v20 += 8) {
                  v7 = Math.min(v7, v14[v20 + 1]);

                  v8 = Math.max(v8, v14[v20 + 1]);
                  v5 = Math.min(v5, v14[v20 + 2]);
                  v6 = Math.max(v6, v14[v20 + 2]);
               }

               Identifier v21 = Identifier.method_60655("client", "model_new/textures/" + v12);
               v4[v9] = new fk.gk(v21, v14);
            }

            return new fk.hk(v4, v5, v6, (v7 + v8) * 0.5F);
         }
      }

      hk(fk.gk[] groups, float minY, float maxY, float centerDepth) {
         this.groups = groups;
         this.minY = minY;
         this.maxY = maxY;
         this.centerDepth = centerDepth;
      }

      
   }
}
