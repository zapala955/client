package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.ls;
import ru.white.utils.render.gl.Jf;
import ru.white.utils.render.gl.Kf;
import ru.white.utils.render.gl.Lf;
import ru.white.event.impl.EventPreMotion;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Jump Circle",
   desc = "Кружочек под тобой при прижке",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class JumpCircle extends Module implements jf {
   public sf previewButton;
   private final ls f693;
   BufferAllocator allocator;
   public static final Function<Identifier, RenderLayer> ROMB_ESP = Util.method_34866(
      texture -> {

         RenderSetup v1 = RenderSetup.method_75927(vj.TEXTURED_QUADS_PIPELINE)
            .method_75934("Sampler0", texture)
            .method_75937()
            .method_75929(1536)
            .method_75938();

         return RenderLayer.method_75940("wtex", v1);
      }
   );
   public BooleanSetting waveEffect;
   private final List<vj.xj> f692;
   private final List<vj.wj> f691 = new ArrayList<>();
   public SliderSetting waveRadius;
   public SliderSetting waveStrength;
   private static final int QUAD_BUFFER_SIZE_BYTES = 1024;
   private static final RenderPipeline TEXTURED_QUADS_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56864})
         .withLocation(Identifier.method_60655("client", "pipeline/world/textured_quads"))
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public BooleanSetting glow;
   private final lf f694;
   public SliderSetting waveTime;
   public SliderSetting size_c;

   @Override
   public void previewStop() {
      this.f691.clear();

      this.f692.clear();
   }

   public JumpCircle() {
      this.f692 = new ArrayList<>();
      this.f693 = new ls();
      this.previewButton = lf.button(this);
      this.size_c = new SliderSetting(this, "Размер", 1.0F, 0.2F, 3.0F, 0.1F);
      this.glow = new BooleanSetting(this, "Свечение", true);
      this.waveEffect = new BooleanSetting(this, "Волна", true);
      this.waveStrength = new SliderSetting(this, "Сила волны", 0.6F, 0.1F, 2.0F, 0.05F).setVisible(() -> this.waveEffect.getValue());
      this.waveRadius = new SliderSetting(this, "Радиус волны", 3.0F, 0.5F, 8.0F, 0.1F).setVisible(() -> {

         return this.waveEffect.getValue();
      });
      this.waveTime = new SliderSetting(this, "Время волны", 600.0F, 150.0F, 2500.0F, 50.0F).setVisible(() -> {

         return this.waveEffect.getValue();
      });
      this.f694 = lf.of(this, 3.5F, 0.0F, 2.0F);
      this.allocator = new BufferAllocator(262144);
   }

   @Override
   public void previewSpawn(kf ctx) {
      Vec3d v2 = ctx.anchor();
      this.f691.add(new vj.wj(v2.method_1031(0.0, 0.05, 0.0)));
      boolean result = this.waveEffect.getValue();

      if (result) {
         this.f692.add(new vj.xj(v2, System.currentTimeMillis(), this.waveTime.getValue(), this.waveStrength.getValue(), this.waveRadius.getValue()));
      }
   }

   @EventTarget
   public void onRender(EventTrace e) {
      long v2 = System.currentTimeMillis();
      this.f691.removeIf(c -> {
         long result = v2 - c.f684;

         return result > -3243504337679256979L + 3243504337679259479L;
      });
      this.f692.removeIf(w -> {

         int v6x = v3 * (v3 + 1) % 2;

         if (v6x != 0) {
            }

         return w.finished(v2);
      });
      if (this.f691.isEmpty()) {

         if (this.f692.isEmpty()) {
            return;
         }
      }

      MatrixStack v4 = e.getMatrixStack();
      int v5 = up.fade(1);
      if (!this.f691.isEmpty()) {
         Immediate v6 = VertexConsumerProvider.method_22991(this.allocator);

         for (vj.wj v9 : this.f691) {
            if (v2 - v9.f684 > 2627782519598974173L + -2627782519598973373L && !v9.f687) {
               v9.f685.run(0.0, 0.5, ap.SINE_OUT);
               v9.f686.run(2.0, 0.5, ap.SINE_OUT);
               v9.f687 = true;
            }

            v9.f685.update();
            v9.f686.update();
            float v10 = (float)v9.f686.getValue();
            double v11 = v9.f683.field_1352 - mc.field_1773.method_19418().method_71156().field_1352;
            double v13 = v9.f683.field_1351 - mc.field_1773.method_19418().method_71156().field_1351;
            double v15 = v9.f683.field_1350 - mc.field_1773.method_19418().method_71156().field_1350;
            float v17 = this.size_c.getValue() * v10;
            v4.method_22903();
            v4.method_22904(v11, v13, v15);
            v4.method_22907(RotationAxis.field_40714.rotationDegrees(90.0F));
            Entry v18 = v4.method_23760();
            Matrix4f v19 = v18.method_23761();
            Matrix3f v20 = v18.method_23762();
            VertexConsumer v21 = v6.method_73477(
               ROMB_ESP.apply(Identifier.method_60655("client", "textures/visuals/jump_new.png"))
            );
            this.m763(
               v21,
               v19,
               v20,
               -v17 / 2.0F,
               -v17 / 2.0F,
               v17,
               v17,
               new int[]{up.fade(0), up.fade(90), up.fade(180), up.fade(360)},
               (int)((float)v7 * v9.f685.get())
            );
            v4.method_22909();
            if (this.glow.getValue()) {
               v4.method_22903();
               v4.method_22904(v11, v13, v15);
               this.m762(v6, v4, v17, v9.f685.get());
               v4.method_22909();
            }
         }

         v6.method_22993();
      }

      if (this.waveEffect.getValue() && !this.f692.isEmpty()) {
         this.f693.renderWaves(this.f692, v4, e.getProjectionMatrix(), v5);
      }
   }

   private void m763(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, float x, float y, float width, float height, int[] ints, int alpha) {
      Vector3f v10 = new Vector3f(0.0F, 0.0F, 1.0F);
      normalMatrix.transform(v10);
      v10.normalize();
      float v13 = x + width;
      float v14 = y + height;
      buffer.method_22918(matrix, x, y, 0.0F)
         .method_39415(up.replAlpha(ints[0], alpha))
         .method_22913(0.0F, 1.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(v10.x, v10.y, v10.z);
      VertexConsumer result = buffer.method_22918(matrix, v13, y, 0.0F)
         .method_39415(up.replAlpha(ints[1], alpha))
         .method_22913(1.0F, 1.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880);

      result.method_22914(v10.x, v10.y, v10.z);
      buffer.method_22918(matrix, v13, v14, 0.0F)
         .method_39415(up.replAlpha(ints[2], alpha))
         .method_22913(1.0F, 0.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(v10.x, v10.y, v10.z);
      result = buffer.method_22918(matrix, x, v14, 0.0F)
         .method_39415(up.replAlpha(ints[3], alpha))
         .method_22913(0.0F, 0.0F)
         .method_22922(OverlayTexture.field_21444);

      result.method_60803(15728880).method_22914(v10.x, v10.y, v10.z);
   }

   @EventTarget
   public void onJump(EventPreMotion e) {
      Vec3d v2 = mc.field_1724.method_73189();
      List result = this.f691;
      vj.wj temp1 = new vj.wj;

      temp1./* $VF: Unable to resugar constructor */<init>(v2.method_1031(0.0, 0.05, 0.0));
      result.add(temp1);
      if (this.waveEffect.getValue()) {

         this.f692.add(new vj.xj(v2, System.currentTimeMillis(), this.waveTime.getValue(), this.waveStrength.getValue(), this.waveRadius.getValue()));
      }
   }

   @Override
   public lf previewSettings() {
      return this.f694;
   }

   private void m762(Immediate immediate, MatrixStack pose, float radius, float alpha) {
      VertexConsumer v5 = immediate.method_73477(
         ROMB_ESP.apply(Identifier.method_60655("client", "textures/visuals/jump_new.png"))
      );
      float v7 = radius * 0.15F;
      float v8 = radius * 0.25F;
      Vector3f v9 = new Vector3f(0.0F, 1.0F, 0.0F);
      pose.method_23760().method_23762().transform(v9);
      v9.normalize();

      for (int v10 = 0; v10 < v6; v10++) {
         float v11 = (float)v10 / (float)v6;
         float v12 = v7 * v11;
         float v13 = alpha * (1.0F - v11) * 0.15F;
         if (!(v13 <= 0.004F)) {
            float v14 = radius + v8 * v11;
            float v15 = v14 / 2.0F;
            int v16 = (int)(v13 * 255.0F);
            Matrix4f v17 = pose.method_23760().method_23761();
            int v18 = up.fade(0);
            int v19 = up.fade(90);
            int v20 = up.fade(180);
            int v21 = up.fade(270);
            v5.method_22918(v17, -v15, v12, -v15)
               .method_1336(
                  v18 >> 16 & 255,
                  v18 >> 8 & 255,
                  v18 & 255,
                  v16
               )
               .method_22913(0.0F, 0.0F)
               .method_22922(OverlayTexture.field_21444)
               .method_60803(15728880)
               .method_22914(v9.x, v9.y, v9.z);
            v5.method_22918(v17, -v15, v12, v15)
               .method_1336(
                  v19 >> 16 & 255,
                  v19 >> 8 & 255,
                  v19 & 255,
                  v16
               )
               .method_22913(0.0F, 1.0F)
               .method_22922(OverlayTexture.field_21444)
               .method_60803(15728880)
               .method_22914(v9.x, v9.y, v9.z);
            VertexConsumer result = v5.method_22918(v17, v15, v12, v15);
            int temp1 = v20 >> 16;
            int temp2 = 255;

            result.method_1336(temp1 & temp2, v20 >> 8 & 255, v20 & 255, v16)
               .method_22913(1.0F, 1.0F)
               .method_22922(OverlayTexture.field_21444)
               .method_60803(15728880)
               .method_22914(v9.x, v9.y, v9.z);
            v5.method_22918(v17, v15, v12, -v15)
               .method_1336(
                  v21 >> 16 & 255,
                  v21 >> 8 & 255,
                  v21 & 255,
                  v16
               )
               .method_22913(1.0F, 0.0F)
               .method_22922(OverlayTexture.field_21444)
               .method_60803(15728880)
               .method_22914(v9.x, v9.y, v9.z);
         }
      }
   }

   @Override
   protected void onDisable() {

      this.f691.clear();

      this.f692.clear();
      this.f693.close();
   }

   @Environment(EnvType.CLIENT)
   private class wj {
      private final Animation f686;
      private final long f684;
      private boolean f687;
      private final Animation f685 = new Animation();
      private final Vec3d f683;

      public wj(Vec3d vector3d) {
         this.f686 = new Animation();
         this.f683 = vector3d;
         this.f684 = System.currentTimeMillis();
         this.f685.run(1.0, 0.5, ap.SINE_OUT);
         this.f686.run(1.0, 0.5, ap.SINE_OUT);
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static record xj(Vec3d position, long createdAt, float lifetimeMs, float strength, float maxRadius) {
      private final float f690;
      private final Vec3d f688;
      private final float f689;

      public xj(Vec3d position, long createdAt, float lifetimeMs, float strength, float maxRadius) {
         this.f688 = position;
         this.createdAt = createdAt;
         this.f689 = lifetimeMs;
         this.strength = strength;
         this.f690 = maxRadius;
      }

      public float maxRadius() {
         int temp2 = v1 + 1;

         return this.f690;
      }

      public Vec3d position() {

         return this.f688;
      }

      @Override
      public final int hashCode() {

         return ObjectMethods.bootstrap<"hashCode",vj.xj,"position;createdAt;lifetimeMs;strength;maxRadius",vj.xj::f688,vj.xj::createdAt,vj.xj::f689,vj.xj::strength,vj.xj::f690>(
            this
         );
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",vj.xj,"position;createdAt;lifetimeMs;strength;maxRadius",vj.xj::f688,vj.xj::createdAt,vj.xj::f689,vj.xj::strength,vj.xj::f690>(
            this, o
         );
      }

      public boolean finished(long now) {
         boolean result;
         if ((float)(now - this.createdAt) >= this.f689) {
            result = true;
         } else {
            result = false;
            int temp1 = v3 * (v3 + 1) % 2;

            if (temp1 != 0) {
               }
         }

         return result;
      }

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",vj.xj,"position;createdAt;lifetimeMs;strength;maxRadius",vj.xj::f688,vj.xj::createdAt,vj.xj::f689,vj.xj::strength,vj.xj::f690>(
            this
         );

         return result;
      }

      public float lifetimeMs() {
         return this.f689;
      }

      public float rawProgress(long now) {
         float result = Math.max(0.0F, Math.min((float)(now - this.createdAt) / this.f689, 1.0F));

         return result;
      }

      public long createdAt() {

         return this.createdAt;
      }

      public float strength() {

         return this.strength;
      }

      
   }
}
