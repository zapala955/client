package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
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
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.event.Event;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Echo Form",
   desc = "Эхо-копии прошлой позы тела с dissolve-эффектом",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class EchoForm extends Module {
   private static final RenderPipeline FILL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/echo_form/fill"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private long f647;
   public final ModeSetting effectMode;
   private final BufferAllocator f650;
   private static final float[][] PARTS;
   private static final RenderLayer LINE_LAYER = RenderLayer.method_75940(
      "echo_form_lines", RenderSetup.method_75927(aj.LINE_PIPELINE).method_75929(8192).method_75938()
   );
   private static final int[][] FACES;
   public final SliderSetting echoAlpha;
   private static final float[][] PIVOTS;
   public final BooleanSetting showFill;
   private static final int[][] EDGES;
   private double f648;
   private static final RenderPipeline LINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/echo_form/lines"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final int PART_COUNT;
   private int f646;
   public final SliderSetting captureMs;
   public final SliderSetting echoCount;
   private static final double MIN_MOVE_DIST = 0.04;
   public final SliderSetting chromShift;
   public final BooleanSetting attackFlash;
   private final aj.bj[] f644;
   public final ModeSetting colorMode = new ModeSetting(this, "Цвет", "Тема", "Холод", "Жар", "Монохром");
   public final BooleanSetting showOutline;
   public final SliderSetting lifetime;
   private double f649;
   private static final RenderLayer FILL_LAYER = RenderLayer.method_75940(
      "echo_form_fill",
      RenderSetup.method_75927(FILL_PIPELINE).method_75937().method_75929(16384).method_75938()
   );
   public final SliderSetting dissolveSpeed;
   private int f645;

   private static float m702(int partIdx, int faceIdx) {
      int result = partIdx * (31);

      int v2 = m705(result + faceIdx * (7) + 10728645);
      return (float)(v2 & 65535) / 65535.0F;
   }

   private void m696(ClientPlayerEntity p, boolean flash) {
      float[] v3 = new float[PART_COUNT];
      float v4 = MathHelper.method_15363(MathHelper.method_15393(p.method_5791() - p.method_73188()), -75.0F, 75.0F);
      v3[0] = p.method_36455();
      float v5 = p.field_42108.method_48569();
      float v6 = Math.min(p.field_42108.method_48566(), 1.0F);
      double temp2 = Math.toDegrees((double)(MathHelper.method_15362((double)(v5 * 0.6662F + (float) Math.PI)) * 2.0F * v6 * 0.5F));

      v3[2] = (float)temp2;
      float v9 = MathHelper.method_15362((double)(v5 * 0.6662F));

      v3[3] = (float)Math.toDegrees((double)(v9 * 2.0F * v6 * 0.5F));
      v3[4] = (float)Math.toDegrees((double)(MathHelper.method_15362((double)(v5 * 0.6662F)) * 1.4F * v6));
      v3[5] = (float)Math.toDegrees((double)(MathHelper.method_15362((double)(v5 * 0.6662F + (float) Math.PI)) * 1.4F * v6));
      this.f644[this.f645] = new aj.bj(p.method_23317(), p.method_23318(), p.method_23321(), p.method_73188(), v4, v3, flash);
      this.f645 = (this.f645 + 1) % this.f644.length;
      if (this.f646 < this.f644.length) {
         this.f646++;
      }
   }

   private static float m707(float t) {
      t = MathHelper.method_15363(t, 0.0F, 1.0F);
      float result = t * t;

      return result * (3.0F - 2.0F * t);
   }

   @Override
   protected void onDisable() {

      this.f646 = 0;
      
   }

   private static int m705(int x) {
      x = x ^ 61 ^ x >>> 16;
      x *= 9;
      x ^= x >>> 4;
      x *= 668265261;
      x ^= x >>> 15;

      return x;
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      ClientPlayerEntity v2 = mc.field_1724;
      if (v2 != null) {
         long v3 = System.currentTimeMillis();
         long v5 = this.captureMs.getValue().longValue();
         if (v3 - this.f647 >= v5) {
            double v7 = v2.method_23317() - this.f648;
            double result = v2.method_23321();

            double v9 = result - this.f649;
            double v11 = v7 * v7 + v9 * v9;
            this.f648 = v2.method_23317();
            this.f649 = v2.method_23321();
            if (this.f648 != Double.MAX_VALUE && !(v11 < 0.0016)) {
               this.f647 = v3;

               this.m696(v2, false);
            }
         }
      }
   }

   private static float m701(float[] p, int bit, int axis) {
      float result;
      if ((bit >> axis & 1) == 0) {

         result = p[axis];
      } else {
         result = p[axis + 3];
      }

      return result;
   }

   private static float m704(float dissolve, float noiseThreshold, float softness) {
      float v3 = noiseThreshold - softness;
      if (dissolve >= noiseThreshold) {
         return 0.0F;
      } else if (dissolve <= v3) {
         return 1.0F;
      } else {
         float v5 = (dissolve - v3) / (noiseThreshold - v3);

         return 1.0F - m707(v5);
      }
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      ClientPlayerEntity v2 = mc.field_1724;
      if (v2 != null && this.f646 != 0) {
         long v3 = System.currentTimeMillis();
         long v5 = this.lifetime.getValue().longValue();
         int v7 = this.echoCount.getValue().intValue();
         float v8 = this.echoAlpha.getValue();
         boolean v9 = this.effectMode.is("Хром");
         float v10 = this.chromShift.getValue();
         Vec3d v11 = mc.field_1773.method_19418().method_71156();
         MatrixStack v12 = e.getMatrixStack();
         Immediate v13 = VertexConsumerProvider.method_22991(this.f650);

         for (int v15 = this.f646 - 1; v15 >= 0 && v14 < v7; v15--) {
            int v16 = Math.floorMod(this.f645 - 1 - v15, this.f644.length);

            aj.bj v17 = this.f644[v16];
            if (v17 != null) {
               long result = v3 - v17.captureTime;

               long v18 = result;
               if (v18 <= v5) {
                  float v20 = (float)v18 / (float)v5;
                  float v21 = MathHelper.method_15363(v20 * 6.0F, 0.0F, 1.0F);
                  float v22 = v20 > 0.3F ? 1.0F - (v20 - 0.3F) / 0.7F : 1.0F;
                  float v23 = m707(v21) * m707(v22);
                  if (!(v23 < 0.01F)) {
                     float v24 = MathHelper.method_15363(v20 * this.dissolveSpeed.getValue() * 1.4F, 0.0F, 1.0F);
                     float v25 = v8 * v23 * (v17.isFlash ? 1.6F : 1.0F);
                     v25 = Math.min(v25, 1.0F);
                     this.m698(v17, v12, v11);
                     if (v9) {
                        float v27 = v10 * (1.0F + v20 * 0.5F);
                        this.m697(v13, v17, v24, v25 * 0.85F, v14, 1, 0, 0, v27, 0.0F);
                        this.m697(v13, v17, v24, v25 * 0.85F, v14, 0, 1, 0, 0.0F, 0.0F);
                        float temp4 = v25 * 0.85F;

                        this.m697(v13, v17, v24, temp4, v14, 0, 0, 1, -v27, 0.0F);
                     } else {
                        this.m697(v13, v17, v24, v25, v14, -1, -1, -1, 0.0F, 0.0F);
                     }

                     v14++;
                  }
               }
            }
         }

         v13.method_22993();
      }
   }

   private static void m699(VertexConsumer buf, Matrix4f m, float[] p, int[] face, int r, int g, int b, int a) {
      for (int v11 : face) {
         float temp2 = m701(p, v11, 0);

         buf.method_22918(m, temp2, m701(p, v11, 1), m701(p, v11, 2)).method_1336(r, g, b, a);
      }
   }

   @Override
   protected void onEnable() {
      this.f646 = 0;
      this.f645 = 0;
      this.f648 = Double.MAX_VALUE;
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      this.f649 = Double.MAX_VALUE;
   }

   private static void m700(VertexConsumer buf, Matrix4f m, float[] p, int[] edge, int r, int g, int b, int a) {
      buf.method_22918(m, m701(p, edge[0], 0), m701(p, edge[0], 1), m701(p, edge[0], 2)).method_1336(r, g, b, a);

      buf.method_22918(m, m701(p, edge[1], 0), m701(p, edge[1], 1), m701(p, edge[1], 2)).method_1336(r, g, b, a);
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {

      this.f646 = 0;
      this.f645 = 0;
      this.f648 = Double.MAX_VALUE;

      this.f649 = Double.MAX_VALUE;
   }

   public EchoForm() {
      this.effectMode = new ModeSetting(this, "Режим", "Обычный", "Хром", "Импульс");
      this.echoCount = new SliderSetting(this, "Кол-во эхо", 6.0F, 2.0F, 12.0F, 1.0F);
      this.lifetime = new SliderSetting(this, "Время жизни (мс)", 900.0F, 200.0F, 2000.0F, 50.0F);
      this.captureMs = new SliderSetting(this, "Интервал захвата (мс)", 80.0F, 40.0F, 300.0F, 10.0F);
      this.echoAlpha = new SliderSetting(this, "Прозрачность", 0.55F, 0.05F, 1.0F, 0.05F);
      this.dissolveSpeed = new SliderSetting(this, "Скорость распада", 0.65F, 0.1F, 1.0F, 0.05F);
      this.showOutline = new BooleanSetting(this, "Контур", true);
      this.showFill = new BooleanSetting(this, "Заливка", true);
      this.attackFlash = new BooleanSetting(this, "Вспышка при атаке", true);
      this.chromShift = new SliderSetting(this, "Смещение хрома", 0.06F, 0.01F, 0.25F, 0.01F).setVisible(() -> {

         return this.effectMode.is("Хром");
      });
      this.f644 = new aj.bj[32];
      this.f645 = 0;
      this.f646 = 0;
      this.f647 = 0L;
      this.f648 = Double.MAX_VALUE;
      this.f649 = Double.MAX_VALUE;
      this.f650 = new BufferAllocator(65536);
   }

   private void m697(Immediate imm, aj.bj snap, float dissolve, float alpha, int echoIndex, int rOnly, int gOnly, int bOnly, float shiftX, float shiftY) {
      boolean v11 = this.showFill.getValue();
      boolean v12 = this.showOutline.getValue();
      if (v11 || v12) {
         for (int v13 = 0; v13 < PART_COUNT; v13++) {
            Matrix4f v14 = snap.partMatrices[v13];
            if (v14 != null) {
               Matrix4f v15 = shiftX == 0.0F && shiftY == 0.0F ? v14 : new Matrix4f(v14).translate(shiftX, shiftY, 0.0F);
               float[] v16 = PARTS[v13];
               if (v11) {
                  VertexConsumer v17 = imm.method_73477(FILL_LAYER);

                  for (int v18 = 0; v18 < FACES.length; v18++) {
                     float v19 = m702(v13, v18);
                     float v20 = m704(dissolve, v19, 0.15F) * alpha;
                     if (!(v20 < 0.004F)) {

                        int[] v21 = this.m706(echoIndex, v13, v18, v20, rOnly, gOnly, bOnly, false);
                        m699(v17, v15, v16, FACES[v18], v21[0], v21[1], v21[2], v21[3]);
                     }
                  }
               }

               if (v12) {
                  VertexConsumer v23 = imm.method_73477(LINE_LAYER);

                  for (int v24 = 0; v24 < EDGES.length; v24++) {
                     float v25 = m703(v13, v24);
                     float v26 = m704(dissolve, v25, 0.08F) * alpha * 1.4F;
                     if (!(v26 < 0.004F)) {
                        int[] v27 = this.m706(echoIndex, v13, v24, Math.min(v26, 1.0F), rOnly, gOnly, bOnly, true);
                        m700(v23, v15, v16, EDGES[v24], v27[0], v27[1], v27[2], v27[3]);
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onAttack(Event e) {
      if (this.attackFlash.getValue()) {
         ClientPlayerEntity v2 = mc.field_1724;
         if (v2 != null) {

            this.m696(v2, true);
         }
      }
   }

   private void m698(aj.bj snap, MatrixStack matrices, Vec3d cam) {
      double v4 = snap.x - cam.field_1352;
      double v6 = snap.y - cam.field_1351;
      double v8 = snap.z - cam.field_1350;
      matrices.method_22903();
      matrices.method_22904(v4, v6, v8);
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees(-snap.bodyYaw));

      for (int v10 = 0; v10 < PART_COUNT; v10++) {
         float v11 = snap.partPitchDeg[v10];
         float v12 = v10 == 0 ? snap.headYawOff : 0.0F;
         if (v11 == 0.0F && v12 == 0.0F) {
            snap.partMatrices[v10].set(matrices.method_23760().method_23761());
         } else {
            float[] v13 = PIVOTS[v10];
            matrices.method_22903();

            matrices.method_46416(v13[0], v13[1], v13[2]);
            if (v12 != 0.0F) {
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees(-v12));
            }

            if (v11 != 0.0F) {
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(v11));
            }

            matrices.method_46416(-v13[0], -v13[1], -v13[2]);
            snap.partMatrices[v10].set(matrices.method_23760().method_23761());
            matrices.method_22909();
         }
      }

      matrices.method_22909();
   }

   static {
      float[][] result = new float[6][];
      float[] temp3 = new float[6];
      temp3[0] = -0.25F;
      temp3[1] = 1.5F;
      temp3[2] = -0.25F;
      temp3[3] = 0.25F;
      temp3[4] = 2.0F;
      temp3[5] = 0.25F;
      result[0] = temp3;
      temp3 = new float[6];
      temp3[0] = -0.25F;
      temp3[1] = 0.75F;
      temp3[2] = -0.125F;
      temp3[3] = 0.25F;
      temp3[4] = 1.5F;
      temp3[5] = 0.125F;
      result[1] = temp3;
      temp3 = new float[6];
      temp3[0] = 0.25F;
      temp3[1] = 0.75F;
      temp3[2] = -0.125F;
      temp3[3] = 0.5F;
      temp3[4] = 1.5F;
      temp3[5] = 0.125F;
      result[2] = temp3;
      temp3 = new float[6];
      temp3[0] = -0.5F;
      temp3[1] = 0.75F;
      temp3[2] = -0.125F;
      temp3[3] = -0.25F;
      temp3[4] = 1.5F;
      temp3[5] = 0.125F;
      result[3] = temp3;
      temp3 = new float[6];
      temp3[0] = 0.0F;
      temp3[1] = 0.0F;
      temp3[2] = -0.125F;
      temp3[3] = 0.25F;
      temp3[4] = 0.75F;
      temp3[5] = 0.125F;
      result[4] = temp3;
      temp3 = new float[6];
      temp3[0] = -0.25F;
      temp3[1] = 0.0F;
      temp3[2] = -0.125F;
      temp3[3] = 0.0F;
      temp3[4] = 0.75F;
      temp3[5] = 0.125F;
      result[5] = temp3;
      PARTS = result;
      result = new float[6][];
      result[0] = new float[]{0.0F, 1.5F, 0.0F};
      result[1] = new float[]{0.0F, 0.75F, 0.0F};
      result[2] = new float[]{0.375F, 1.4F, 0.0F};
      result[3] = new float[]{-0.375F, 1.4F, 0.0F};
      result[4] = new float[]{0.125F, 0.75F, 0.0F};
      result[5] = new float[]{-0.125F, 0.75F, 0.0F};
      PIVOTS = result;
      PART_COUNT = PARTS.length;
      int[][] v1 = new int[6][];
      v1[0] = new int[]{0, 1, 3, 2};
      v1[1] = new int[]{4, 5, 7, 6};
      v1[2] = new int[]{0, 1, 5, 4};
      v1[3] = new int[]{2, 3, 7, 6};
      v1[4] = new int[]{0, 2, 6, 4};
      v1[5] = new int[]{1, 3, 7, 5};
      FACES = v1;
      int[][] v2 = new int[12][];
      v2[0] = new int[]{0, 1};
      v2[1] = new int[]{1, 3};
      v2[2] = new int[]{3, 2};
      v2[3] = new int[]{2, 0};
      v2[4] = new int[]{4, 5};
      v2[5] = new int[]{5, 7};
      v2[6] = new int[]{7, 6};
      v2[7] = new int[]{6, 4};
      v2[8] = new int[]{0, 4};
      v2[9] = new int[]{1, 5};
      v2[10] = new int[]{2, 6};
      v2[11] = new int[]{3, 7};
      EDGES = v2;
   }

   private int[] m706(int echoIndex, int partIdx, int elemIdx, float alpha, int rOnly, int gOnly, int bOnly, boolean isOutline) {
      long v9 = System.currentTimeMillis();
      int v11;
      if (this.colorMode.is("Тема")) {
         int result = up.getClientColor1(echoIndex * (30));

         v11 = result;
      } else if (this.colorMode.is("Холод")) {
         float v12 = 0.58F + 0.08F * (float)Math.sin((double)v9 * 0.001 + (double)echoIndex * 0.5);
         float v13 = 0.7F;
         float v14 = 0.7F + 0.3F * (float)Math.sin((double)v9 * 0.002 + (double)partIdx);
         v11 = Color.HSBtoRGB(v12, v13, v14) | -16777216;
      } else if (this.colorMode.is("Жар")) {

         float v19 = 0.02F + 0.04F * (float)Math.sin((double)v9 * 0.0015 + (double)echoIndex * 0.4);
         float v22 = 0.9F;
         float v24 = 0.75F + 0.25F * (float)Math.sin((double)v9 * 0.003 + (double)elemIdx * 0.3);
         v11 = Color.HSBtoRGB(v19, v22, v24) | -16777216;
      } else {
         int v20 = 180 + (int)(50.0 * Math.sin((double)v9 * 0.002 + (double)echoIndex * 0.8));
         v11 = v20 << 16 | v20 << 8 | v20 | -16777216;
      }

      if (isOutline) {
         v11 = up.interpolateColor(v11, -1, 0.3F);
      }

      int v21 = v11 >> 16 & 255;
      int v23 = v11 >> 8 & 255;
      int v25 = v11 & 255;
      int v15 = (int)(MathHelper.method_15363(alpha, 0.0F, 1.0F) * 255.0F);
      if (rOnly != -1) {
         v21 = rOnly > 0 ? v21 : 0;
         v23 = gOnly > 0 ? v23 : 0;
         v25 = bOnly > 0 ? v25 : 0;
      }

      return new int[]{v21, v23, v25, v15};
   }

   private static float m703(int partIdx, int edgeIdx) {
      int v2 = m705(partIdx * (53) + edgeIdx * (13) + 13755123);

      return (float)(v2 & 65535) / 65535.0F;
   }

   @Environment(EnvType.CLIENT)
   private static final class bj {
      final double x;
      final float headYawOff;
      final float bodyYaw;
      final long captureTime;
      final boolean isFlash;
      final Matrix4f[] partMatrices;
      final double z;
      final double y;
      final float[] partPitchDeg = new float[aj.PART_COUNT];

      bj(double x, double y, double z, float bodyYaw, float headYawOff, float[] pitches, boolean isFlash) {
         this.partMatrices = new Matrix4f[aj.PART_COUNT];
         this.x = x;
         this.y = y;
         this.z = z;
         this.bodyYaw = bodyYaw;
         this.headYawOff = headYawOff;
         System.arraycopy(pitches, 0, this.partPitchDeg, 0, aj.PART_COUNT);
         this.captureTime = System.currentTimeMillis();
         this.isFlash = isFlash;

         for (int v11 = 0; v11 < aj.PART_COUNT; v11++) {
            this.partMatrices[v11] = new Matrix4f();
         }
      }

      
   }
}
