package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "World Cubes",
   desc = "Сетка по миру со свечением",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class WorldCubes extends Module {
   public SliderSetting range;
   private int f833;
   private int f834;
   private boolean f836;
   public SliderSetting lineAlphaS;
   public static final Function<Identifier, RenderLayer> ROMB_ESP = Util.method_34866(
      texture -> {
         RenderSetup v1 = RenderSetup.method_75927(ll.ROMB_ESP_PIPELINE)
            .method_75934("Sampler0", texture)
            .method_75937()
            .method_75929(1536)
            .method_75938();
         RenderLayer result = RenderLayer.method_75940("wtex", v1);

         return result;
      }
   );
   private int f830;
   public SliderSetting glowSize;
   public static final RenderPipeline ROMB_ESP_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
         .build()
   );
   public tf tintColor;
   public BooleanSetting showGlow;
   private final Animation f829;
   public SliderSetting vRange;
   private int f835;
   private static final int CELL = 12;
   private final BufferAllocator f837;
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   private static final RenderLayer LINE_LAYER = RenderLayer.method_75940(
      "world_cubes_lines",
      RenderSetup.method_75927(ll.LINE_PIPELINE).method_75929(16384).method_75938()
   );
   private int f831;
   private int f832;
   public SliderSetting glowBright;
   private static final RenderPipeline LINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "world_cubes_grid"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );

   private void m983(VertexConsumer EventReceivePacket, Matrix4f mat, Vec3d cam, int ox, int oy, int oz, int cr, int StringSetting, int cb, int alpha, int rI, int vI) {
      if (alpha > 0) {
         int v13 = ox - rI * (12);
         int v14 = ox + rI * (12);
         int v15 = oz - rI * (12);
         int v16 = oz + rI * (12);
         int v17 = oy - (vI - 1) * (12);
         int v18 = oy + vI * (12);

         for (int v19 = v13; v19 <= v14; v19 += 12) {
            for (int v20 = v15; v20 <= v16; v20 += 12) {
               float v21 = (float)((double)v19 - cam.field_1352);
               float v22 = (float)((double)v20 - cam.field_1350);
               lb.method_22918(mat, v21, (float)((double)v17 - cam.field_1351), v22).method_1336(cr, StringSetting, cb, alpha);
               lb.method_22918(mat, v21, (float)((double)v18 - cam.field_1351), v22).method_1336(cr, StringSetting, cb, alpha);
            }
         }

         for (int v26 = v17; v26 <= v18; v26 += 12) {
            float v28 = (float)((double)v26 - cam.field_1351);

            for (int v30 = v15; v30 <= v16; v30 += 12) {
               float v32 = (float)((double)v30 - cam.field_1350);
               double temp2 = (double)v13;

               lb.method_22918(mat, (float)(temp2 - cam.field_1352), v28, v32).method_1336(cr, StringSetting, cb, alpha);

               lb.method_22918(mat, (float)((double)v14 - cam.field_1352), v28, v32).method_1336(cr, StringSetting, cb, alpha);
            }
         }

         int v27 = v17;

         while (true) {

            if (v27 > v18) {
               return;
            }

            float v29 = (float)((double)v27 - cam.field_1351);

            for (int v31 = v13; v31 <= v14; v31 += 12) {
               float v33 = (float)((double)v31 - cam.field_1352);
               lb.method_22918(mat, v33, v29, (float)((double)v15 - cam.field_1350)).method_1336(cr, StringSetting, cb, alpha);
               lb.method_22918(mat, v33, v29, (float)((double)v16 - cam.field_1350)).method_1336(cr, StringSetting, cb, alpha);
            }

            v27 += 12;
         }
      }
   }

   private static int m985(double v) {
      double result = v / 12.0;

      int v5 = (int)Math.floor(result) * (12);

      return v5;
   }

   private void m984(
      Immediate immediate, RenderLayer glowLayer, Matrix4f mat, Vec3d cam, int ox, int oy, int oz, int cr, int StringSetting, int cb, int alpha, float gs, int rI, int vI
   ) {
      if (alpha > 0) {
         int v15 = ox - rI * (12);
         int v16 = ox + rI * (12);
         int v17 = oz - rI * (12);
         int v18 = oz + rI * (12);
         int v19 = oy - (vI - 1) * (12);
         int v20 = oy + vI * (12);
         Camera v21 = mc.field_1773.method_19418();
         Quaternionf v22 = v21.method_23767();
         Vector3f v23 = new Vector3f(-gs, -gs, 0.0F).rotate(v22);
         Vector3f v24 = new Vector3f(gs, -gs, 0.0F).rotate(v22);
         Vector3f v25 = new Vector3f(gs, gs, 0.0F).rotate(v22);
         Vector3f v26 = new Vector3f(-gs, gs, 0.0F).rotate(v22);
         float v27 = (float)Math.toRadians((double)v21.method_19330());
         float v28 = (float)Math.toRadians((double)v21.method_19329());
         float v29 = (float)Math.cos((double)v28);
         double result = (double)v27;

         float v30 = (float)(-Math.sin(result) * (double)v29);
         float v31 = (float)(-Math.sin((double)v28));
         float v32 = (float)(Math.cos((double)v27) * (double)v29);
         VertexConsumer v33 = immediate.method_73477(glowLayer);

         for (int v34 = v15; v34 <= v16; v34 += 12) {
            for (int v35 = v19; v35 <= v20; v35 += 12) {
               for (int v36 = v17; v36 <= v18; v36 += 12) {
                  float v37 = (float)((double)v34 - cam.field_1352);
                  float v38 = (float)((double)v35 - cam.field_1351);
                  float v39 = (float)((double)v36 - cam.field_1350);
                  if (!(v37 * v30 + v38 * v31 + v39 * v32 < -gs * 2.0F)) {

                     v33.method_22918(mat, v37 + v23.x, v38 + v23.y, v39 + v23.z)
                        .method_1336(cr, StringSetting, cb, alpha)
                        .method_22913(0.0F, 1.0F)
                        .method_22922(OverlayTexture.field_21444)
                        .method_60803(15728880)
                        .method_22914(0.0F, 0.0F, 1.0F);
                     v33.method_22918(mat, v37 + v24.x, v38 + v24.y, v39 + v24.z)
                        .method_1336(cr, StringSetting, cb, alpha)
                        .method_22913(1.0F, 1.0F)
                        .method_22922(OverlayTexture.field_21444)
                        .method_60803(15728880)
                        .method_22914(0.0F, 0.0F, 1.0F);
                     v33.method_22918(mat, v37 + v25.x, v38 + v25.y, v39 + v25.z)
                        .method_1336(cr, StringSetting, cb, alpha)
                        .method_22913(1.0F, 0.0F)
                        .method_22922(OverlayTexture.field_21444)
                        .method_60803(15728880)
                        .method_22914(0.0F, 0.0F, 1.0F);
                     v33.method_22918(mat, v37 + v26.x, v38 + v26.y, v39 + v26.z)
                        .method_1336(cr, StringSetting, cb, alpha)
                        .method_22913(0.0F, 0.0F)
                        .method_22922(OverlayTexture.field_21444)
                        .method_60803(15728880)
                        .method_22914(0.0F, 0.0F, 1.0F);
                  }
               }
            }
         }
      }
   }

   public int getColor() {
      if (this.typeColor.is("Тема")) {
         return up.getClientColor1(1);
      } else {
         int result = this.tintColor.getValue();

         return result;
      }
   }

   @Override
   protected void onEnable() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      this.f836 = true;
   }

   public WorldCubes() {
      this.tintColor = new tf(this, "Цвет", -16711681).setVisible(() -> {
         boolean result = this.typeColor.is("Свой");

         return result;
      });
      this.range = new SliderSetting(this, "Радиус (ячейки)", 4.0F, 1.0F, 30.0F, 1.0F);
      this.vRange = new SliderSetting(this, "Высота (ячейки)", 2.0F, 1.0F, 20.0F, 1.0F);
      this.lineAlphaS = new SliderSetting(this, "Прозрачность линий", 0.22F, 0.04F, 1.0F, 0.02F);
      this.showGlow = new BooleanSetting(this, "Свечение в углах", true);
      this.glowSize = new SliderSetting(this, "Размер свечения", 0.08F, 0.02F, 1.0F, 0.02F).setVisible(() -> {

         return this.showGlow.getValue();
      });
      this.glowBright = new SliderSetting(this, "Яркость свечения", 1.8F, 0.5F, 4.0F, 0.1F)
         .setVisible(() -> {
            Boolean result = this.showGlow.getValue();

            return result;
         });
      this.f829 = new Animation();
      this.f836 = true;
      this.f837 = new BufferAllocator(262144);
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int v2 = m985(mc.field_1724.method_23317());
         int v3 = m985(mc.field_1724.method_23318());
         int v4 = m985(mc.field_1724.method_23321());
         if (this.f836) {
            this.f830 = v2;
            this.f831 = v3;
            this.f832 = v4;
            this.f833 = v2;
            this.f834 = v3;
            this.f835 = v4;
            this.f829.set(1.0);
            this.f836 = false;
         } else if (v2 != this.f830 || v3 != this.f831 || v4 != this.f832) {
            this.f833 = this.f830;
            this.f834 = this.f831;
            this.f835 = this.f832;
            this.f830 = v2;
            this.f831 = v3;
            this.f832 = v4;
            this.f829.set(0.0);
            this.f829.run(1.0, 0.25, ap.QUAD_OUT);
         }

         this.f829.update();
         float v5 = this.f829.get();
         int v6 = this.getColor();
         int v7 = v6 >> 16 & 255;
         int result = v6 >> 8;

         int v8 = result & 255;
         int v9 = v6 & 255;
         float v10 = this.lineAlphaS.getValue() * 255.0F;
         int v11 = this.range.getValue().intValue();
         int v12 = this.vRange.getValue().intValue();
         Vec3d v13 = mc.field_1773.method_19418().method_71156();
         MatrixStack v14 = e.getMatrixStack();
         Matrix4f v15 = v14.method_23760().method_23761();
         Immediate v16 = VertexConsumerProvider.method_22991(this.f837);
         VertexConsumer v17 = v16.method_73477(LINE_LAYER);
         if (v5 < 0.999F) {
            int v18 = (int)(v10 * (1.0F - v5));
            this.m983(v17, v15, v13, this.f833, this.f834, this.f835, v7, v8, v9, v18, v11, v12);
         }

         int v24 = (int)(v10 * v5);

         this.m983(v17, v15, v13, this.f830, this.f831, this.f832, v7, v8, v9, v24, v11, v12);
         if (this.showGlow.getValue()) {
            float v19 = this.glowSize.getValue();
            float v20 = this.glowBright.getValue();
            int v21 = Math.min(255, (int)(15.0F * v5 * v20));
            this.m984(
               v16,
               ROMB_ESP.apply(Particles.ParticleType.BLOOM.texture()),
               v15,
               v13,
               this.f830,
               this.f831,
               this.f832,
               v7,
               v8,
               v9,
               v21,
               v19 * 2.0F,
               v11,
               v12
            );
            v21 = Math.min(255, (int)(255.0F * v5 * v20));
            this.m984(
               v16,
               ROMB_ESP.apply(Particles.ParticleType.CIRCLE.texture()),
               v15,
               v13,
               this.f830,
               this.f831,
               this.f832,
               v7,
               v8,
               v9,
               v21,
               v19,
               v11,
               v12
            );
         }

         v16.method_22993();
      }
   }

   
}
