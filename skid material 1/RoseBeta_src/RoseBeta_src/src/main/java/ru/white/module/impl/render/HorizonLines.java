package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
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
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Horizon Lines",
   desc = "Волны-ряби из-под ног при движении",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class HorizonLines extends Module {
   public final BooleanSetting innerGlow;
   private final List<rj.tj> f675;
   private double f679;
   public final ModeSetting colorMode;
   public final BooleanSetting multiRing;
   public final SliderSetting expandSpeed;
   private static final RenderLayer RING_LAYER = RenderLayer.method_75940(
      "horizon_lines_ring",
      RenderSetup.method_75927(rj.RING_PIPELINE).method_75937().method_75929(65536).method_75938()
   );
   private float f681;
   public final SliderSetting maxRadius = new SliderSetting(this, "Макс. радиус", 2.5F, 0.8F, 5.0F, 0.1F);
   private double f678;
   private static final RenderPipeline RING_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/horizon_lines/ring"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final int SEGMENTS = 64;
   private boolean f680;
   public final SliderSetting spawnFreq;
   private double f682;
   private final BufferAllocator f676;
   public final tf customColor;
   public final SliderSetting ringsPerStep;
   private final Random f677;
   public final SliderSetting ringWidth;

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null) {
         this.f681 = this.f681 + (1.0F - this.f681) * 0.08F;
         double v2 = mc.field_1724.method_23317();
         double v4 = mc.field_1724.method_23321();
         if (!this.f680) {
            this.f678 = v2;
            this.f679 = v4;
            this.f680 = true;
         } else {
            double v6 = v2 - this.f678;
            double v8 = v4 - this.f679;
            double v10 = Math.sqrt(v6 * v6 + v8 * v8);
            if (v10 > 0.003) {
               this.f682 += v10;
               double v12 = this.spawnFreq.getValue().doubleValue();
               if (this.f682 >= v12) {
                  this.f682 -= v12;
                  this.m758(v2, mc.field_1724.method_23318(), v4);
               }
            }

            this.f678 = v2;
            this.f679 = v4;
         }

         float v17 = this.maxRadius.getValue();
         Iterator v7 = this.f675.iterator();

         while (v7.hasNext()) {
            rj.tj v18 = (rj.tj)v7.next();
            v18.update();
            if (v18.radius >= v17 * 1.1F) {

               if (v18.alpha < 0.01F) {
                  v7.remove();
               }
            }
         }
      }
   }

   private static void m760(
      VertexConsumer buf, Matrix4f mat, float cx, float cy, float cz, float outerR, float innerR, int cr, int StringSetting, int cb, int outerA, int innerA
   ) {
      for (int v12 = 0; v12 < 64; v12++) {
         float result = (float)v12 / 64.0F;

         float v13 = result * (float) (Math.PI * 2);
         float v14 = (float)(v12 + 1) / 64.0F * (float) (Math.PI * 2);
         float v15 = MathHelper.method_15362((double)v13);
         float v16 = MathHelper.method_15374((double)v13);
         float v17 = MathHelper.method_15362((double)v14);
         float v18 = MathHelper.method_15374((double)v14);
         buf.method_22918(mat, cx + v15 * innerR, cy, cz + v16 * innerR).method_1336(cr, StringSetting, cb, innerA);
         float v21 = cx + v15 * outerR;

         buf.method_22918(mat, v21, cy, cz + v16 * outerR).method_1336(cr, StringSetting, cb, outerA);
         buf.method_22918(mat, cx + v17 * outerR, cy, cz + v18 * outerR).method_1336(cr, StringSetting, cb, outerA);
         buf.method_22918(mat, cx + v17 * innerR, cy, cz + v18 * innerR).method_1336(cr, StringSetting, cb, innerA);
      }
   }

   @Override
   protected void onDisable() {
      this.f675.clear();
      
   }

   private int m759() {
      if (this.colorMode.is("Свой")) {
         return this.customColor.getValue();
      } else if (this.colorMode.is("Радуга")) {
         return Color.HSBtoRGB(this.f677.nextFloat(), 0.85F, 1.0F) | -16777216;
      } else {
         int temp1 = 120;

         int result = up.fade(this.f677.nextInt(temp1));

         return result;
      }
   }

   @Override
   protected void onEnable() {

      this.f675.clear();
      this.f680 = false;

      this.f681 = 0.0F;
      this.f682 = 0.0;
   }

   public HorizonLines() {
      this.expandSpeed = new SliderSetting(this, "Скорость расширения", 1.6F, 0.5F, 4.0F, 0.1F);
      this.ringWidth = new SliderSetting(this, "Ширина кольца", 0.06F, 0.01F, 0.2F, 0.01F);
      this.spawnFreq = new SliderSetting(this, "Частота (блоков/кольцо)", 0.4F, 0.1F, 2.0F, 0.05F);
      this.ringsPerStep = new SliderSetting(this, "Колец на шаг", 1.0F, 1.0F, 4.0F, 1.0F);
      this.colorMode = new ModeSetting(this, "Цвет", "Тема", "Радуга", "Свой");
      this.customColor = new tf(this, "Свой цвет", -16711732).setVisible(() -> {

         return this.colorMode.is("Свой");
      });
      this.innerGlow = new BooleanSetting(this, "Внутреннее свечение", true);
      this.multiRing = new BooleanSetting(this, "Двойное кольцо", false);
      this.f675 = new ArrayList<>();
      this.f676 = new BufferAllocator(65536);
      this.f677 = new Random();
      this.f680 = false;
      this.f681 = 0.0F;
      this.f682 = 0.0;
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {
      this.f675.clear();
      int temp5 = v3 + 1;

      this.f680 = false;
      this.f682 = 0.0;
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && !this.f675.isEmpty() && !(this.f681 < 0.02F)) {
         Vec3d v2 = mc.field_1773.method_19418().method_71156();
         MatrixStack v3 = e.getMatrixStack();
         Matrix4f v4 = v3.method_23760().method_23761();
         Immediate v5 = VertexConsumerProvider.method_22991(this.f676);
         VertexConsumer v6 = v5.method_73477(RING_LAYER);
         float v7 = this.ringWidth.getValue();

         for (rj.tj v9 : this.f675) {
            float v10 = v9.alpha * this.f681;
            if (!(v10 < 0.005F)) {
               float v11 = v9.radius;
               float v12 = v11 - v7;
               if (v12 <= 0.0F) {
                  v12 = 0.001F;
               }

               int v13 = v9.color;
               int v14 = v13 >> 16 & 255;
               int v15 = v13 >> 8 & 255;
               int v16 = v13 & 255;
               int v17 = (int)(v10 * 180.0F);
               int v18 = (int)(v10 * 50.0F);

               float v19 = (float)(v9.x - v2.field_1352);
               float v20 = (float)(v9.y - v2.field_1351);
               float v21 = (float)(v9.z - v2.field_1350);
               m760(v6, v4, v19, v20, v21, v11, v12, v14, v15, v16, v17, v18);
               if (this.innerGlow.getValue()) {
                  float v22 = Math.max(0.001F, v11 - v7 * 4.0F);
                  m760(v6, v4, v19, v20 + 0.001F, v21, v11 + v7 * 0.8F, v22, v14, v15, v16, (int)(v10 * 22.0F), 0);
               }
            }
         }

         v5.method_22993();
      }
   }

   private void m758(double x, double y, double z) {
      int v7 = this.ringsPerStep.getValue().intValue();

      for (int v8 = 0; v8 < v7; v8++) {

         float v9 = (this.f677.nextFloat() - 0.5F) * 0.04F;
         List result = this.f675;
         rj.tj v14 = new rj.tj;

         double temp5 = y + (double)v9;

         v14./* $VF: Unable to resugar constructor */<init>(x, temp5, z, this.m759());
         result.add(v14);
         if (this.multiRing.getValue()) {
            this.f675.add(new rj.tj(x, y + (double)v9 + 0.01F, z, this.m759()) {
               {
                  this.radius = 0.18F;
                  this.alphaTarget = 0.55F;
               }

               
            });
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private class tj {
      boolean fading;
      final double z;
      float radius = 0.05F;
      final double x;
      final double y;
      float alphaTarget;
      final int color;
      float alpha = 0.0F;

      void update() {
         float v1 = rj.this.maxRadius.getValue();
         float result = rj.this.expandSpeed.getValue() * 0.045F;

         float v2 = result;
         this.radius += v2;
         if (!this.fading) {
            this.alpha = this.alpha + (this.alphaTarget - this.alpha) * 0.18F;
         }

         if (this.radius / v1 > 0.55F && !this.fading) {
            this.fading = true;
         }

         if (this.fading) {
            this.alpha = this.alpha - this.alpha * 0.09F;
         }
      }

      tj(double x, double y, double z, int color) {
         this.alphaTarget = 1.0F;
         this.fading = false;
         this.x = x;
         this.y = y;
         this.z = z;
         this.color = color;
      }

      
   }
}
