package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderSetup.Builder;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.gl.Jf;
import ru.white.utils.render.gl.Kf;
import ru.white.utils.render.gl.Lf;
import ru.white.utils.IMinecraft;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Trails",
   desc = "Красивый эффект шлейфа за игроком при движении",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Trails extends Module implements jf {
   private double f810;
   public final BooleanSetting dashSegs;
   public final SliderSetting waveSpeed;
   public final SliderSetting spread;
   public final BooleanSetting dashDots;
   private static final int DASH_TEX_COUNT = 21;
   public final SliderSetting flowSpeed;
   private double f816;
   private static final RenderPipeline TRAIL_LINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/trails/ribbon_line"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final long DASH_FRAME_TIME_MS = 80L;
   public final ModeSetting ribbonColorMode;
   public final SliderSetting glow;
   private double f817;
   public final SliderSetting moveLerp;
   private static final int SUBDIVISIONS = 4;
   public final ModeSetting rotateMode;
   private static final RenderLayer SPARK_LAYER;
   private static final int[] GROUP_SIZES = new int[]{
      11, 23, 32, 16, 32
   };
   private static final RenderPipeline TRAIL_FILL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/trails/ribbon_fill"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public final SliderSetting wave;
   public final tf ribbonColor;
   private static final double TWO_PI = Math.PI * 2;
   public final SliderSetting height;
   private final List<Vec3d> f799;
   private final Random f803;
   private static final Identifier BLOOM_TEX = Identifier.method_60655("client", "textures/particles/glow.png");
   private final Vector3f f805;
   public final SliderSetting coreWidth;
   public final BooleanSetting lighting;
   private float f809;
   private final List<bl.dl> f801;
   public final BooleanSetting thirdPersonOnly;
   private static final List<List<Identifier>> DASH_ANIM_GROUPS = new ArrayList<>();
   private static final double MIN_STEP_SQR = 4.0E-4;
   private final List<Vec3d> f800;
   private static final Function<Identifier, RenderLayer> CUBIC_LAYER;
   private final Vector3f f806;
   private static final RenderPipeline CUBIC_PIPELINE;
   private static final double TELEPORT_DISTANCE_SQR = 64.0;
   private static final RenderLayer TRAIL_LINE_LAYER = RenderLayer.method_75940(
      "trails_ribbon_line",
      RenderSetup.method_75927(TRAIL_LINE_PIPELINE).method_75937().method_75929(32768).method_75938()
   );
   public final ModeSetting colorMode;
   private double f811;
   private final lf f798;
   public final SliderSetting waveDensity;
   private static final int RIBBONS = 3;
   private static final RenderLayer TRAIL_FILL_LAYER = RenderLayer.method_75940(
      "trails_ribbon_fill",
      RenderSetup.method_75927(TRAIL_FILL_PIPELINE).method_75937().method_75929(131072).method_75938()
   );
   public sf previewButton = lf.button(this);
   private static final List<Identifier> DASH_TEXTURES = new ArrayList<>();
   private boolean f818;
   private static final RenderPipeline SPARK_PIPELINE;
   public final BooleanSetting motionSmooth;
   public final ModeSetting mode = new ModeSetting(this, "Режим", "Ленты", "Кубики");
   private static final float TAIL_WIDTH = 0.18F;
   private double f812;
   private float f808;
   private Vec3d f819;
   private final Quaternionf f807;
   private final List<bl.dl> f802;
   private final BufferAllocator f804;
   public final SliderSetting dashLength;
   public final SliderSetting ribbonWidth;
   private LivingEntity f814;
   public final SliderSetting length = new SliderSetting(this, "Длина", 30.0F, 6.0F, 90.0F, 1.0F).setVisible(() -> {

      return this.mode.is("Ленты");
   });
   private boolean f813;
   private double f815;

   private static List<Vec3d> m924(List<Vec3d> spine, int subdivisions) {
      int v2 = spine.size();
      if (v2 >= 2 && subdivisions >= 2) {
         ArrayList v3 = new ArrayList((v2 - 1) * subdivisions + 1);

         for (int v4 = 0; v4 < v2 - 1; v4++) {

            Vec3d v5 = (Vec3d)spine.get(Math.max(0, v4 - 1));
            Vec3d v6 = (Vec3d)spine.get(v4);
            Vec3d v7 = (Vec3d)spine.get(v4 + 1);
            Vec3d v8 = (Vec3d)spine.get(Math.min(v2 - 1, v4 + 2));

            for (int v9 = 0; v9 < subdivisions; v9++) {
               v3.add(m925(v5, v6, v7, v8, (double)v9 / (double)subdivisions));
            }
         }

         int temp2 = spine.size();

         v3.add((Vec3d)spine.get(temp2 - 1));
         return v3;
      } else {
         return spine;
      }
   }

   @Override
   protected void onEnable() {
      this.f799.clear();
      this.f800.clear();

      this.f801.clear();

      this.f802.clear();

      this.f808 = 1.0F;
      this.f813 = false;
      this.f818 = false;
      this.f819 = null;
   }

   @Override
   public lf previewSettings() {
      return this.f798;
   }

   private int m926() {
      if (this.ribbonColorMode.is("Тема")) {
         return up.client();
      } else {

         if (this.ribbonColorMode.is("Радуга")) {
            float v1 = (float)((double)(System.currentTimeMillis() % (-1468770996271691125L + 1468770996271695125L)) / 4000.0);
            return Color.HSBtoRGB(v1, 0.85F, 1.0F) | -16777216;
         } else {
            int result = this.ribbonColor.getValue();

            return result;
         }
      }
   }

   private int m934() {
      if (this.colorMode.is("Случайный")) {
         float result = (float)Math.random();

         int v3 = Color.HSBtoRGB(result, 0.8F, 1.0F);

         return v3 | -16777216;
      } else {
         return up.client();
      }
   }

   private void m931(MatrixStack matrix, float[] rot, float fCamYaw, float fCamPitch) {
      if (this.rotateMode.is("Камера")) {
         matrix.method_22907(this.f807);
      } else {
         matrix.method_22907(new Quaternionf().rotateY((float)Math.toRadians((double)rot[0])));

         matrix.method_22907(new Quaternionf().rotateX((float)Math.toRadians((double)rot[1])));
      }
   }

   private void m916() {
      if (this.f814 != null && !this.f814.method_31481()) {
         this.f800.clear();
         int v1 = Math.max(16, this.length.getValue().intValue());
         double result = this.height.getValue().doubleValue();

         double v2 = result;
         Vec3d v4 = new Vec3d(this.f814.method_23317(), this.f814.method_23318() + v2, this.f814.method_23321());
         float v5 = (float)Math.toRadians((double)this.f814.method_36454());
         Vec3d v6 = new Vec3d((double)MathHelper.method_15374((double)v5), 0.0, (double)(-MathHelper.method_15362((double)v5)));
         Vec3d v7 = new Vec3d((double)MathHelper.method_15362((double)v5), 0.0, (double)MathHelper.method_15374((double)v5));
         float v8 = (float)((double)(System.currentTimeMillis() % (7494296136414605995L + -7494296136411005995L)) / 1000.0);
         double v9 = 2.8;

         for (int v11 = 0; v11 <= v1; v11++) {
            result = (double)v11;
            double v25 = (double)v1;

            double v12 = result / v25;
            double v14 = (1.0 - v12) * v9;
            double v16 = Math.sin(v12 * Math.PI * 2.5 - (double)v8 * 2.4) * 0.35;
            double v18 = Math.cos(v12 * Math.PI * 2.0 - (double)v8 * 2.0) * 0.18;
            Vec3d v20 = v4.method_1019(v6.method_1021(v14)).method_1019(v7.method_1021(v16)).method_1031(0.0, v18, 0.0);

            this.f800.add(v20);
         }
      } else {
         this.f800.clear();
      }
   }

   @Override
   public void previewSpawn(kf c) {

      this.f814 = c.dummy();
      this.m916();
   }

   @Override
   public void previewStop() {
      this.f814 = null;

      this.f800.clear();
   }

   private void m915(LivingEntity entity, List<Vec3d> hist, int maxLen, double hOffset) {
      if (entity == null) {
         hist.clear();
      } else {
         Vec3d v6 = new Vec3d(entity.method_23317(), entity.method_23318() + hOffset, entity.method_23321());
         if (!hist.isEmpty()) {
            Vec3d v7 = (Vec3d)hist.get(hist.size() - 1);
            if (v7.method_1025(v6) > 64.0) {
               hist.clear();
            }
         }

         boolean v12;
         if (entity == mc.field_1724) {

            v12 = 1;
         } else {
            v12 = 0;
         }

         boolean v11 = (boolean)v12;
         boolean v8;
         if (hist.isEmpty()) {
            v8 = v11 && this.f819 != null && this.f819.method_1025(v6) > 4.0E-4;
         } else {
            v8 = ((Vec3d)hist.get(hist.size() - 1)).method_1025(v6) > 4.0E-4;
         }

         if (v8) {
            if (hist.isEmpty() && v11 && this.f819 != null) {
               hist.add(this.f819);
            }

            hist.add(v6);
         } else if (!hist.isEmpty()) {
            hist.remove(0);
         }

         if (v11) {
            this.f819 = v6;
         }

         while (true) {
            v12 = hist.size();

            if (v12 <= maxLen) {
               return;
            }

            hist.remove(0);
         }
      }
   }

   private static float m923(double progress) {

      float v2 = (float)MathHelper.method_15350(progress * 1.6, 0.0, 1.0);
      double result = (1.0 - progress) * 12.0;

      float v3 = (float)MathHelper.method_15350(result, 0.0, 1.0);
      return v2 * v3;
   }

   @EventTarget
   public void onTick(EventRenderModel event) {
      if (mc.field_1724 == null) {
         this.f799.clear();
         this.f800.clear();
         this.f801.clear();
         this.f819 = null;
      } else {
         this.f808 = this.f808 + (1.0F - this.f808) * 0.15F;
         this.f809 = this.f809 + (1.0F - this.f809) * 0.15F;

         if (this.mode.is("Ленты")) {
            ClientPlayerEntity v5 = mc.field_1724;
            List temp2 = this.f799;
            int temp3 = this.length.getValue().intValue();
            Float temp4 = this.height.getValue();
            int temp5 = v2 * (v2 + 1) % 2;

            if (temp5 != 0) {
               }

            this.m915(v5, temp2, temp3, temp4.doubleValue());
            if (this.f814 != null && !this.f814.method_31481()) {
               this.m916();
            }
         } else {
            this.m927();
         }
      }
   }

   @Override
   public void previewTick(kf c) {
      this.f814 = c.dummy();
      int result = v2 * (v2 + 1);

      this.m916();
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      this.f799.clear();
      this.f800.clear();
      this.f801.clear();
      this.f802.clear();
      this.f804.method_60809();

      this.f813 = false;
      this.f818 = false;
      this.f819 = null;
   }

   private void m927() {
      if (!this.f813) {
         this.f810 = mc.field_1724.method_23317();
         this.f811 = mc.field_1724.method_23318();
         this.f812 = mc.field_1724.method_23321();
         this.f813 = true;
      } else {
         long v1 = System.currentTimeMillis();
         int v3 = this.f801.size();

         for (int v4 = 0; v4 < v3; v4++) {
            bl.dl v5 = this.f801.get(v4);
            v5.cachedTimePC = Math.min(1.0F, (float)(v1 - v5.startTime) / (float)v5.base.rMTime);
            if (v5.cachedTimePC >= 1.0F && v5.alphaTarget != 0.0F) {
               v5.alphaTarget = 0.0F;
            }

            v5.alphaAnim = v5.alphaAnim + (v5.alphaTarget - v5.alphaAnim) * 0.08F;
         }

         for (int v16 = this.f801.size() - 1; v16 >= 0; v16--) {
            bl.dl v19 = this.f801.get(v16);
            if (v19.cachedTimePC >= 1.0F && v19.alphaTarget == 0.0F && v19.alphaAnim < 0.02F) {
               this.f801.remove(v16);
            }
         }

         this.f802.clear();

         for (int v20 = this.f801.size(); v17 < v20; v17++) {
            bl.dl v6 = this.f801.get(v17);
            if (v6.alphaAnim > 0.05F) {
               this.f802.add(v6);
            }
         }

         float v18 = mc.field_1773.method_19418().method_19329();
         boolean v21 = this.motionSmooth.getValue();
         int v22 = this.f802.size();

         for (int v7 = 0; v7 < v22; v7++) {
            bl.dl v8 = this.f802.get(v7);
            bl.dl v9 = v21 && v7 + 1 < v22 ? this.f802.get(v7 + 1) : null;
            if (v9 != null && v9.base.entityId != v8.base.entityId) {
               v9 = null;
            }

            v8.motionProcess(v9, v1, v18);
            
         }

         double v23 = this.f810;
         double v24 = this.f811;
         double v11 = this.f812;
         double v13 = this.moveLerp.getValue().doubleValue();
         this.f810 = MathHelper.method_16436(v13, this.f810, mc.field_1724.method_23317());
         this.f811 = MathHelper.method_16436(v13, this.f811, mc.field_1724.method_23318());
         this.f812 = MathHelper.method_16436(v13, this.f812, mc.field_1724.method_23321());
         this.m929(mc.field_1724, v23, v24, v11);
         this.m928(v13);
      }
   }

   @Override
   protected void onDisable() {
      this.f799.clear();
      this.f800.clear();
      this.f801.clear();
      this.f802.clear();

      this.f804.method_60809();
      this.f814 = null;
      this.f819 = null;
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && !(this.f808 < 0.04F)) {
         MatrixStack v2 = e.getMatrixStack();
         Vec3d v3 = mc.field_1773.method_19418().method_71156();
         Immediate result = VertexConsumerProvider.method_22991(this.f804);

         Immediate v4 = result;
         if (!this.mode.is("Ленты")) {
            this.m930(e, v4, v3);
         } else {
            boolean v5 = mc.field_1773.method_19418().method_19333();
            if (!this.thirdPersonOnly.getValue() || v5) {

               this.m917(v2, v3, v4, mc.field_1724, this.f799, e.getTickDelta());
            }

            if (this.f814 != null && !this.f814.method_31481()) {
               this.m917(v2, v3, v4, this.f814, this.f800, e.getTickDelta());
            }
         }
      }
   }

   private void m929(LivingEntity entity, double lx, double ly, double lz) {
      if (entity != null) {
         double v8 = entity.method_23317() - lx;
         double v10 = entity.method_23318() - ly;
         double v12 = entity.method_23321() - lz;
         if (!(v8 * v8 + v12 * v12 < 0.0064)) {
            double v14 = Math.sqrt(v8 * v8 + v10 * v10 + v12 * v12);
            int v16 = (int)MathHelper.method_15363((float)(v14 / 0.4F), 1.0F, 16.0F);

            for (int v17 = 0; v17 < v16; v17++) {
               float result = (float)(550 + this.f803.nextInt(300));

               int v18 = (int)(result * this.dashLength.getValue());
               this.f801.add(new bl.dl(new bl.cl(entity, 0.04F, (float)v17 / (float)v16, v18)));
            }
         }
      }
   }

   @Override
   public boolean previewNeedsDummy() {
      return true;
   }

   private static void m932(VertexConsumer buf, Matrix4f mat, float x1, float y1, float x2, float y2, int argb) {
      buf.method_22918(mat, x1, y1, 0.0F).method_22913(0.0F, 0.0F).method_39415(argb);
      buf.method_22918(mat, x1, y2, 0.0F).method_22913(0.0F, 1.0F).method_39415(argb);
      VertexConsumer result = buf.method_22918(mat, x2, y2, 0.0F);

      result.method_22913(1.0F, 1.0F).method_39415(argb);
      buf.method_22918(mat, x2, y1, 0.0F).method_22913(1.0F, 0.0F).method_39415(argb);
   }

   private static float m933(double mx, double mz) {
      double result = Math.atan2(mz, mx);

      return (float)Math.toDegrees(result) - 90.0F;
   }

   private void m928(double lr) {
      if (this.f814 == null || this.f814.method_31481()) {
         this.f818 = false;
      } else if (!this.f818) {

         this.f815 = this.f814.method_23317();
         this.f816 = this.f814.method_23318();
         this.f817 = this.f814.method_23321();
         this.f818 = true;
      } else {
         double v3 = this.f815;
         double v5 = this.f816;
         double v7 = this.f817;
         this.f815 = MathHelper.method_16436(lr, this.f815, this.f814.method_23317());
         double temp1 = MathHelper.method_16436(lr, this.f816, this.f814.method_23318());

         this.f816 = temp1;
         this.f817 = MathHelper.method_16436(lr, this.f817, this.f814.method_23321());
         this.m929(this.f814, v3, v5, v7);
      }
   }

   private void m919(Matrix4f matrix, Vec3d camPos, VertexConsumer lines, List<Vec3d> pts, float[] alphas, float[] flows) {
      int v7 = pts.size();
      if (v7 >= 2) {
         double v8 = camPos.field_1352;
         double v10 = camPos.field_1351;
         double v12 = camPos.field_1350;

         for (int v14 = 0; v14 < v7 - 1; v14++) {
            float v15 = alphas[v14];
            float v16 = alphas[v14 + 1];
            if (!(v15 <= 0.003F) || !(v16 <= 0.003F)) {
               Vec3d v17 = (Vec3d)pts.get(v14);
               Vec3d v18 = (Vec3d)pts.get(v14 + 1);
               float v19 = (float)(v17.field_1352 - v8);
               float v20 = (float)(v17.field_1351 - v10);
               float v21 = (float)(v17.field_1350 - v12);
               float v22 = (float)(v18.field_1352 - v8);
               float result = (float)(v18.field_1351 - v10);

               float v23 = result;

               float v24 = (float)(v18.field_1350 - v12);
               int v25 = up.replAlpha(-1, v15 * 0.88F * flows[v14]);

               int v26 = up.replAlpha(-1, v16 * 0.88F * flows[v14 + 1]);
               lines.method_22918(matrix, v19, v20, v21).method_39415(v25);
               lines.method_22918(matrix, v22, v23, v24).method_39415(v26);
            }
         }
      }
   }

   private List<Vec3d> m920(LivingEntity entity, List<Vec3d> hist, float tickDelta, double hOffset) {
      ArrayList v6 = new ArrayList(hist.size() + 1);
      v6.addAll(hist);
      double v7 = MathHelper.method_16436((double)tickDelta, entity.field_6038, entity.method_23317());
      double result = MathHelper.method_16436((double)tickDelta, entity.field_5971, entity.method_23318()) + hOffset;

      double v9 = result;
      double v11 = MathHelper.method_16436((double)tickDelta, entity.field_5989, entity.method_23321());
      Vec3d v13 = new Vec3d(v7, v9, v11);
      if (v6.isEmpty() || ((Vec3d)v6.get(v6.size() - 1)).method_1025(v13) > 1.0E-6) {
         v6.add(v13);
      }

      return v6;
   }

   public Trails() {
      this.ribbonWidth = new SliderSetting(this, "Ширина", 0.12F, 0.02F, 0.6F, 0.01F).setVisible(() -> {
         Boolean result = this.mode.is("Ленты");

         return result;
      });
      this.spread = new SliderSetting(this, "Разброс", 0.25F, 0.0F, 1.5F, 0.05F).setVisible(() -> this.mode.is("Ленты"));
      this.height = new SliderSetting(this, "Высота", 1.0F, 0.0F, 2.5F, 0.05F).setVisible(() -> {
         boolean result = this.mode.is("Ленты");

         return result;
      });
      this.thirdPersonOnly = new BooleanSetting(this, "Только в 3-м лице", false)
         .setVisible(() -> this.mode.is("Ленты"));
      this.ribbonColorMode = new ModeSetting(this, "Цвет лент", "Тема", "Свой", "Радуга").setVisible(() -> {

         return this.mode.is("Ленты");
      });
      this.ribbonColor = new tf(this, "Цвет лент", -9707009).setVisible(() -> {
         if (this.mode.is("Ленты")) {

            if (this.ribbonColorMode.is("Свой")) {
               return true;
            }
         }

         return false;
      });
      this.wave = new SliderSetting(this, "Волна", 0.18F, 0.0F, 1.0F, 0.01F).setVisible(() -> {

         return this.mode.is("Ленты");
      });
      this.waveSpeed = new SliderSetting(this, "Скорость волны", 1.5F, 0.0F, 6.0F, 0.1F).setVisible(() -> {

         return this.mode.is("Ленты");
      });
      this.waveDensity = new SliderSetting(this, "Плотность волны", 0.35F, 0.05F, 1.5F, 0.05F)
         .setVisible(() -> {

            return this.mode.is("Ленты");
         });
      this.glow = new SliderSetting(this, "Свечение", 1.0F, 0.0F, 2.5F, 0.05F).setVisible(() -> {
         Boolean result = this.mode.is("Ленты");

         return result;
      });
      this.coreWidth = new SliderSetting(this, "Ядро", 0.35F, 0.05F, 1.0F, 0.05F).setVisible(() -> {

         return this.mode.is("Ленты");
      });
      this.flowSpeed = new SliderSetting(this, "Скорость потока", 1.2F, 0.0F, 5.0F, 0.1F).setVisible(() -> {

         return this.mode.is("Ленты");
      });
      this.colorMode = new ModeSetting(this, "Цвет", "Клиент", "Случайный").setVisible(() -> {

         return this.mode.is("Кубики");
      });
      this.rotateMode = new ModeSetting(this, "Вращение", "Движение", "Камера").setVisible(() -> {

         return this.mode.is("Кубики");
      });
      this.dashLength = new SliderSetting(this, "Длина шлейфа", 0.75F, 0.5F, 1.5F, 0.01F).setVisible(() -> {
         boolean result = this.mode.is("Кубики");

         return result;
      });
      this.moveLerp = new SliderSetting(this, "Move Lerp", 0.2F, 0.1F, 0.5F, 0.01F).setVisible(() -> {

         return this.mode.is("Кубики");
      });
      this.motionSmooth = new BooleanSetting(this, "Сглаживание", false).setVisible(() -> {
         boolean result = this.mode.is("Кубики");

         return result;
      });
      this.dashSegs = new BooleanSetting(this, "Сегменты", false).setVisible(() -> {

         return this.mode.is("Кубики");
      });
      this.dashDots = new BooleanSetting(this, "Точки", true).setVisible(() -> {
         boolean result = this.mode.is("Кубики");
         int temp2 = v2 + 1;

         return result;
      });
      this.lighting = new BooleanSetting(this, "Свечение кубиков", true).setVisible(() -> {
         boolean result = this.mode.is("Кубики");

         return result;
      });
      this.f798 = lf.withoutInterval(this, 4.0F, 0.0F);
      this.f799 = new ArrayList<>();
      this.f800 = new ArrayList<>();
      this.f801 = new ArrayList<>();
      this.f802 = new ArrayList<>();
      this.f803 = new Random(-2848779747230423941L + 2848779748464991832L);
      this.f804 = new BufferAllocator(262144);
      this.f805 = new Vector3f();
      this.f806 = new Vector3f();
      this.f807 = new Quaternionf();
      this.f808 = 0.0F;
      this.f809 = 0.0F;
      this.f813 = false;
      this.f818 = false;
      this.f819 = null;
   }

   private static float m922(double progress) {
      double result = 0.18F + 0.82F * progress;

      return (float)result;
   }

   private void m917(MatrixStack matrices, Vec3d camPos, Immediate immediate, LivingEntity entity, List<Vec3d> hist, float tickDelta) {
      if (entity != null) {
         if (hist.size() >= 2) {
            List v7 = this.m920(entity, hist, tickDelta, this.height.getValue().doubleValue());
            if (v7.size() < 2) {
               return;
            }

            List v8 = m924(v7, 4);
            if (v8.size() < 2) {
               return;
            }

            int v9 = this.m926();
            float v10 = (float)((double)(System.currentTimeMillis() % (2126788382714005799L + -2126788382710405799L)) / 1000.0);
            float v11 = this.ribbonWidth.getValue();
            double v12 = this.spread.getValue().doubleValue();
            double v14 = this.wave.getValue().doubleValue();
            double v16 = this.waveDensity.getValue().doubleValue();
            double v18 = (double)v10 * this.waveSpeed.getValue().doubleValue();
            float v20 = this.glow.getValue();
            float v21 = this.coreWidth.getValue();
            float v22 = this.flowSpeed.getValue();
            Matrix4f v23 = matrices.method_23760().method_23761();
            ArrayList v24 = new ArrayList(3);

            for (int v25 = 0; v25 < 3; v25++) {
               double v26 = (double)(v25 - 1) * v12;
               double v28 = (double)v25 * (Math.PI * 2) / 3.0;
               ArrayList v30 = new ArrayList(v8.size());
               float[] v31 = new float[v8.size()];
               float[] v32 = new float[v8.size()];
               float[] v33 = new float[v8.size()];

               for (int v34 = 0; v34 < v8.size(); v34++) {
                  double v35 = (double)v34 / (double)(v8.size() - 1);
                  Vec3d v37 = m921(v8, v34);
                  Vec3d v38 = (Vec3d)v8.get(v34);
                  double v39 = (double)v34 * v16 + v18 + v28;
                  double v41 = v26 + Math.sin(v39) * v14;
                  double v43 = Math.cos(v39) * v14 * 0.6;
                  v30.add(v38.method_1019(v37.method_1021(v41)).method_1031(0.0, v43, 0.0));
                  v31[v34] = v11 * m922(v35);
                  v32[v34] = m923(v35) * this.f808;

                  v33[v34] = 0.82F + 0.32F * (float)Math.sin((v35 * 5.0 - (double)(v10 * v22)) * (Math.PI * 2));
               }

               v24.add(new bl.gl(v30, v31, v32, v33));
            }

            VertexConsumer v48 = immediate.method_73477(TRAIL_FILL_LAYER);

            for (bl.gl v27 : v24) {
               this.m918(v23, camPos, v48, v27.points, v27.widths, v27.alphas, v27.flows, v9, v20, v21);
            }

            immediate.method_22994(TRAIL_FILL_LAYER);
            VertexConsumer v50 = immediate.method_73477(TRAIL_LINE_LAYER);

            for (bl.gl v52 : v24) {
               this.m919(v23, camPos, v50, v52.points, v52.alphas, v52.flows);
            }

            immediate.method_22994(TRAIL_LINE_LAYER);
            return;
         }

         
      }
   }

   private static Vec3d m921(List<Vec3d> path, 

      Vec3d v2 = (Vec3d)path.get(Math.max(0, index - 1));
      Vec3d result = (Vec3d)path.get(Math.min(path.size() - 1, index + 1));

      Vec3d v3 = result;
      double v4 = v3.field_1352 - v2.field_1352;
      double v6 = v3.field_1350 - v2.field_1350;
      double v8 = Math.sqrt(v4 * v4 + v6 * v6);
      double v15;
      int v13 = (v15 = v8 - 1.0E-5) == 0.0 ? 0 : (v15 < 0.0 ? -1 : 1);

      return v13 < 0 ? new Vec3d(1.0, 0.0, 0.0) : new Vec3d(-v6 / v8, 0.0, v4 / v8);
   }

   private void m930(EventTrace e, Immediate immediate, Vec3d cam) {
      if (!this.f802.isEmpty()) {
         float v4 = e.getTickDelta();
         float v5 = this.f808;
         float v6 = this.f809;
         int v7 = this.f802.size();
         long v8 = System.currentTimeMillis();

         MatrixStack v10 = e.getMatrixStack();
         double v11 = cam.field_1352;
         double v13 = cam.field_1351;
         double v15 = cam.field_1350;
         float v17 = mc.field_1773.method_19418().method_19330();
         float v18 = mc.field_1773.method_19418().method_19329();
         Quaternionf v19 = mc.field_1773.method_19418().method_23767();
         v19.transform(this.f805.set(1.0F, 0.0F, 0.0F));
         v19.transform(this.f806.set(0.0F, 1.0F, 0.0F));
         this.f807.identity().rotateY((float)Math.toRadians((double)(-v17))).rotateX((float)Math.toRadians((double)v18));

         for (int v20 = 0; v20 < v7; v20++) {
            bl.dl v21 = this.f802.get(v20);
            Identifier v22 = v21.dashTexture.getCurrent(v8);
            if (v22 != null) {
               float v23 = v21.alphaAnim * v5;
               float v24 = 4.224F * v23 * 0.3F;
               int v25 = v21.color;
               v10.method_22903();
               v10.method_22904(v21.getRenderX(v4) - v11, v21.getRenderY(v4) - v13, v21.getRenderZ(v4) - v15);
               this.m931(v10, v21.rotate, v17, v18);
               v10.method_22905(-0.1F, -0.1F, 0.1F);
               m932(
                  immediate.method_73477(CUBIC_LAYER.apply(v22)),
                  v10.method_23760().method_23761(),
                  -v24,
                  -v24,
                  v24,
                  v24,
                  up.replAlpha(v25, v23)
               );
               v10.method_22909();
            }
         }

         if (this.lighting.getValue() && v6 > 0.01F) {
            VertexConsumer v29 = immediate.method_73477(CUBIC_LAYER.apply(BLOOM_TEX));
            float v30 = 0.5F * v6;

            for (int v31 = 0; v31 < v7; v31++) {
               bl.dl v32 = this.f802.get(v31);
               float v33 = v32.alphaAnim * v5;
               int v34 = v32.color;
               int v26 = up.overCol(v34, -1, 0.18F);
               v10.method_22903();
               v10.method_22904(v32.getRenderX(v4) - v11, v32.getRenderY(v4) - v13, v32.getRenderZ(v4) - v15);
               v10.method_22907(this.f807);
               v10.method_22905(-0.1F, -0.1F, 0.1F);
               Matrix4f v27 = v10.method_23760().method_23761();
               m932(v29, v27, -v30 * 6.0F, -v30 * 6.0F, v30 * 6.0F, v30 * 6.0F, up.swapAlpha(v26, 15.0F * v33));
               m932(v29, v27, -v30 * 2.2F, -v30 * 2.2F, v30 * 2.2F, v30 * 2.2F, up.swapAlpha(v26, 70.0F * v33));
               v10.method_22909();
            }
         }

         immediate.method_22993();
      }
   }

   static {
      for (int v0 = 1; v0 <= 21; v0++) {
         DASH_TEXTURES.add(Identifier.method_60655("client", "textures/dash_cubes/dashcubic" + v0 + ".png"));
      }

      for (int v6 = 1; v6 <= GROUP_SIZES.length; v6++) {
         ArrayList v1 = new ArrayList();

         for (int v2 = 1; v2 <= GROUP_SIZES[v6 - 1]; v2++) {
            v1.add(
               Identifier.method_60655(
                  "client", "textures/dash_cubes/group_dashs/group" + v6 + "/dashcubic" + v2 + ".png"
               )
            );
         }

         DASH_ANIM_GROUPS.add(v1);
      }

      SPARK_PIPELINE = RenderPipelines.method_67887(
         RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
            .withLocation(Identifier.method_60655("client", "pipeline/trails/spark"))
            .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.LIGHTNING)
            .build()
      );
      SPARK_LAYER = RenderLayer.method_75940(
         "trails_spark", RenderSetup.method_75927(SPARK_PIPELINE).method_75929(16384).method_75938()
      );
      CUBIC_PIPELINE = RenderPipelines.method_67887(
         RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56864})
            .withLocation(Identifier.method_60655("client", "pipeline/trails/cubic"))
            .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.LIGHTNING)
            .build()
      );
      CUBIC_LAYER = Util.method_34866(tex -> {
         String result = "trails_cubic";
         Builder temp1 = RenderSetup.method_75927(CUBIC_PIPELINE).method_75934("Sampler0", tex).method_75937();
         int temp2 = 1536;

         return RenderLayer.method_75940(result, temp1.method_75929(temp2).method_75938());
      });
   }

   private static Vec3d m925(Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, double t) {
      double v6 = t * t;
      double v8 = v6 * t;
      double v10 = -0.5 * v8 + v6 - 0.5 * t;
      double v12 = 1.5 * v8 - 2.5 * v6 + 1.0;

      double v14 = -1.5 * v8 + 2.0 * v6 + 0.5 * t;
      double result = 0.5 * v8 - 0.5 * v6;

      double v16 = result;
      Vec3d v21 = new Vec3d;
      double temp2 = p0.field_1352 * v10 + p1.field_1352 * v12 + p2.field_1352 * v14 + p3.field_1352 * v16;
      double temp3 = p0.field_1351 * v10 + p1.field_1351 * v12;

      v21./* $VF: Unable to resugar constructor */<init>(
         temp2,
         temp3 + p2.field_1351 * v14 + p3.field_1351 * v16,
         p0.field_1350 * v10 + p1.field_1350 * v12 + p2.field_1350 * v14 + p3.field_1350 * v16
      );
      return v21;
   }

   private void m918(
      Matrix4f matrix,
      Vec3d camPos,
      VertexConsumer fill,
      List<Vec3d> pts,
      float[] widths,
      float[] alphas,
      float[] flows,
      int baseColor,
      float glowVal,
      float coreWidthVal
   ) {
      int v11 = pts.size();
      if (v11 >= 2) {
         Vec3d[] v12 = new Vec3d[v11];

         for (int v13 = 0; v13 < v11; v13++) {
            Vec3d v14 = (Vec3d)pts.get(Math.max(0, v13 - 1));
            Vec3d v15 = (Vec3d)pts.get(Math.min(v11 - 1, v13 + 1));
            Vec3d v16 = v15.method_1020(v14);
            Vec3d v17 = camPos.method_1020((Vec3d)pts.get(v13));
            Vec3d v18 = v16.method_1036(v17);
            double v19 = v18.method_1033();
            if (v19 < 1.0E-6) {
               v12[v13] = new Vec3d(0.0, 1.0, 0.0);
            } else {
               v12[v13] = v18.method_1021(1.0 / v19);
            }
         }

         double v67 = camPos.field_1352;
         double v68 = camPos.field_1351;
         double v69 = camPos.field_1350;

         for (int v70 = 0; v70 < v11 - 1; v70++) {
            Vec3d v20 = (Vec3d)pts.get(v70);
            Vec3d v21 = (Vec3d)pts.get(v70 + 1);
            float v22 = alphas[v70];
            float v23 = alphas[v70 + 1];
            if (!(v22 <= 0.003F) || !(v23 <= 0.003F)) {
               float v24 = widths[v70];
               float v25 = widths[v70 + 1];
               float v26 = flows[v70];
               float v27 = flows[v70 + 1];
               Vec3d v28 = v12[v70];
               Vec3d v29 = v12[v70 + 1];
               float v30 = (float)(v20.field_1352 - v67);
               float v31 = (float)(v20.field_1351 - v68);
               float v32 = (float)(v20.field_1350 - v69);
               float v33 = (float)(v21.field_1352 - v67);
               float v34 = (float)(v21.field_1351 - v68);
               float v35 = (float)(v21.field_1350 - v69);
               float v36 = v24 * 3.0F * glowVal;
               float v37 = v25 * 3.0F * glowVal;
               float v38 = (float)(v28.field_1352 * (double)v36);
               float v39 = (float)(v28.field_1351 * (double)v36);
               float v40 = (float)(v28.field_1350 * (double)v36);
               float v41 = (float)(v29.field_1352 * (double)v37);
               float v42 = (float)(v29.field_1351 * (double)v37);
               float v43 = (float)(v29.field_1350 * (double)v37);
               int v44 = up.replAlpha(baseColor, v22 * 0.35F * v26 * glowVal);
               int v45 = up.replAlpha(baseColor, v23 * 0.35F * v27 * glowVal);
               int v46 = up.replAlpha(baseColor, 0.0F);
               fill.method_22918(matrix, v30 - v38, v31 - v39, v32 - v40).method_39415(v46);
               fill.method_22918(matrix, v30, v31, v32).method_39415(v44);
               fill.method_22918(matrix, v33, v34, v35).method_39415(v45);
               fill.method_22918(matrix, v33 - v41, v34 - v42, v35 - v43).method_39415(v46);
               fill.method_22918(matrix, v30, v31, v32).method_39415(v44);
               fill.method_22918(matrix, v30 + v38, v31 + v39, v32 + v40).method_39415(v46);
               fill.method_22918(matrix, v33 + v41, v34 + v42, v35 + v43).method_39415(v46);
               fill.method_22918(matrix, v33, v34, v35).method_39415(v45);
               float v47 = (float)(v28.field_1352 * (double)v24);
               float v48 = (float)(v28.field_1351 * (double)v24);
               float v49 = (float)(v28.field_1350 * (double)v24);
               float v50 = (float)(v29.field_1352 * (double)v25);
               float v51 = (float)(v29.field_1351 * (double)v25);
               float v52 = (float)(v29.field_1350 * (double)v25);
               int v53 = up.replAlpha(baseColor, v22 * 0.82F * v26);
               int v54 = up.replAlpha(baseColor, v23 * 0.82F * v27);
               int v55 = up.replAlpha(baseColor, 0.0F);
               float temp2 = v30 - v47;
               float temp3 = v31 - v48;

               fill.method_22918(matrix, temp2, temp3, v32 - v49).method_39415(v55);
               fill.method_22918(matrix, v30, v31, v32).method_39415(v53);
               fill.method_22918(matrix, v33, v34, v35).method_39415(v54);
               fill.method_22918(matrix, v33 - v50, v34 - v51, v35 - v52).method_39415(v55);
               fill.method_22918(matrix, v30, v31, v32).method_39415(v53);
               fill.method_22918(matrix, v30 + v47, v31 + v48, v32 + v49).method_39415(v55);
               fill.method_22918(matrix, v33 + v50, v34 + v51, v35 + v52).method_39415(v55);
               fill.method_22918(matrix, v33, v34, v35).method_39415(v54);
               float v56 = v24 * coreWidthVal;
               float v57 = v25 * coreWidthVal;
               float v58 = (float)(v28.field_1352 * (double)v56);
               float v59 = (float)(v28.field_1351 * (double)v56);
               float v60 = (float)(v28.field_1350 * (double)v56);
               float v61 = (float)(v29.field_1352 * (double)v57);
               float v62 = (float)(v29.field_1351 * (double)v57);
               float v63 = (float)(v29.field_1350 * (double)v57);
               int v64 = up.replAlpha(-1, v22 * 0.95F * v26);
               int v65 = up.replAlpha(-1, v23 * 0.95F * v27);
               fill.method_22918(matrix, v30 - v58, v31 - v59, v32 - v60).method_39415(v55);
               fill.method_22918(matrix, v30, v31, v32).method_39415(v64);
               fill.method_22918(matrix, v33, v34, v35).method_39415(v65);
               fill.method_22918(matrix, v33 - v61, v34 - v62, v35 - v63).method_39415(v55);
               fill.method_22918(matrix, v30, v31, v32).method_39415(v64);
               fill.method_22918(matrix, v30 + v58, v31 + v59, v32 + v60).method_39415(v55);
               fill.method_22918(matrix, v33 + v61, v34 + v62, v35 + v63).method_39415(v55);
               fill.method_22918(matrix, v33, v34, v35).method_39415(v65);
            }
         }
      }
   }

   @Override
   public void previewStart(kf c) {

      this.f814 = c.dummy();
      this.m916();
   }

   @Environment(EnvType.CLIENT)
   private static class cl {
      double motionY;
      double prevPosZ;
      double posY;
      final int entityId;
      double prevPosY;
      final float yaw;
      final int rMTime;
      double motionX;
      double motionZ;
      double posZ;
      double posX;
      final float prevYaw;
      double prevPosX;

      cl(LivingEntity entity, float spd, float offsetPC, int rmTime) {
         this.entityId = entity.method_5628();
         this.rMTime = rmTime;
         this.yaw = entity.method_36454();
         this.prevYaw = entity.field_5982;
         double v5 = -(entity.field_6038 - entity.method_23317());
         double v7 = -(entity.field_5971 - entity.method_23318());
         double v9 = -(entity.field_5989 - entity.method_23321());
         this.motionX = v5;
         this.motionY = v7;
         this.motionZ = v9;
         float v11 = entity.method_17682();
         float v12 = entity.method_5869() ? 2.4F : 1.0F;
         this.posX = entity.field_6038 - v5 * (double)offsetPC + -0.0875 + 0.175 * Math.random();
         this.posY = entity.field_5971 - v7 * (double)offsetPC + (double)(v11 / v12) / 3.0 + (double)(v11 / v12) / 4.0 * Math.random() * 2.7;
         this.posZ = entity.field_5989 - v9 * (double)offsetPC + -0.0875 + 0.175 * Math.random();
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.motionX *= (double)spd;
         this.motionY *= (double)spd;
         this.motionZ *= (double)spd;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private final class dl {
      final float[] rotate = new float[2];
      float cachedTimePC;
      final bl.cl base;
      final bl.fl dashTexture;
      float alphaTarget;
      float alphaAnim;
      final int color;
      final List<bl.el> sparks = new ArrayList<>();
      final long startTime = System.currentTimeMillis();

      void motionProcess(bl.dl next, long nowMs, float cameraPitch) {
         this.base.prevPosX = this.base.posX;
         this.base.prevPosY = this.base.posY;
         this.base.prevPosZ = this.base.posZ;
         this.base.motionX = (next != null ? next.base.motionX : this.base.motionX) / 1.05;

         this.base.posX = this.base.posX + 5.0 * this.base.motionX;
         this.base.motionY = (next != null ? next.base.motionY : this.base.motionY) / 1.05;

         this.base.posY = this.base.posY + 5.0 * this.base.motionY / (this.base.motionY < 0.0 ? 1.0 : 3.5);
         this.base.motionZ = (next != null ? next.base.motionZ : this.base.motionZ) / 1.05;
         this.base.posZ = this.base.posZ + 5.0 * this.base.motionZ;
         this.recomputeRotate(cameraPitch);
         int v5 = this.sparks.size();

         for (int v6 = 0; v6 < v5; v6++) {
            this.sparks.get(v6).update(nowMs);
         }

         for (int v9 = this.sparks.size() - 1; v9 >= 0; v9--) {
            if (this.sparks.get(v9).dead) {
               this.sparks.remove(v9);
            }
         }
      }

      double getRenderX(float pt) {
         double result = this.base.prevPosX + (this.base.posX - this.base.prevPosX) * (double)pt;

         return result;
      }

      dl(bl.cl base) {
         this.alphaAnim = 1.0F;
         this.alphaTarget = 1.0F;
         this.cachedTimePC = 0.0F;
         this.base = base;
         this.color = bl.this.m934();
         this.dashTexture = bl.this.new fl();
         this.recomputeRotate(hp.mc.field_1773.method_19418().method_19329());
      }

      double getRenderY(float pt) {

         double temp1 = this.base.posY - this.base.prevPosY;

         temp1 *= (double)pt;

         return this.base.prevPosY + temp1;
      }

      void recomputeRotate(float cameraPitch) {

         double result = this.base.motionX * this.base.motionX;

         double v2 = Math.sqrt(result + this.base.motionZ * this.base.motionZ);
         if (v2 < 5.0E-4) {
            this.rotate[0] = (float)(360.0 * Math.random());
            this.rotate[1] = cameraPitch;
         } else {
            float v4 = bl.m933(this.base.motionX, this.base.motionZ);
            this.rotate[0] = v4 - 60.0F - (this.base.prevYaw - this.base.yaw) * 3.0F;
            float v5 = Math.abs(v4 + 26.3F - this.base.yaw) % 360.0F;
            if (v5 > 180.0F) {
               v5 = 360.0F - v5;
            }

            this.rotate[1] = !(v5 < 10.0F) && !(v5 > 160.0F) ? cameraPitch : -90.0F;
         }
      }

      double getRenderZ(float pt) {

         return this.base.prevPosZ + (this.base.posZ - this.base.prevPosZ) * (double)pt;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static class el {
      final double cosYaw;
      final double spd;
      float cachedAlphaPC;
      final double cosPitchOffset;
      double prevY;
      double prevZ;
      boolean dead = false;
      final long startTime;
      final double sinYaw;
      double posX;
      double posY;
      double prevX;
      double posZ;

      double getRenderY(float pt) {
         int temp2 = v2 + 1;

         return this.prevY + (this.posY - this.prevY) * (double)pt;
      }

      el() {
         this.cachedAlphaPC = 1.0F;
         this.spd = Math.random() / 50.0;
         this.startTime = System.currentTimeMillis();
         double v1 = Math.random() * Math.PI * 2.0;
         double v3 = Math.toRadians(-90.0 + Math.random() * 180.0 - 90.0);
         this.sinYaw = Math.sin(v1);
         this.cosYaw = Math.cos(v1);
         this.cosPitchOffset = Math.cos(v3);
      }

      void update(long nowMs) {
         float v3 = MathHelper.method_15363((float)(nowMs - this.startTime) / 1000.0F, 0.0F, 1.0F);
         float v10;
         int temp1 = (v10 = v3 - 1.0F) == 0.0F ? 0 : (v10 < 0.0F ? -1 : 1);

         this.dead = temp1 >= 0;
         this.cachedAlphaPC = 1.0F - v3;
         this.prevX = this.posX;
         this.prevY = this.posY;
         this.prevZ = this.posZ;
         double v7 = this.posX + this.sinYaw * this.spd;

         this.posX = v7;
         this.posY = this.posY + this.cosPitchOffset * this.spd;

         this.posZ = this.posZ + this.cosYaw * this.spd;
      }

      double getRenderX(float pt) {

         return this.prevX + (this.posX - this.prevX) * (double)pt;
      }

      double getRenderZ(float pt) {

         return this.prevZ + (this.posZ - this.prevZ) * (double)pt;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private class fl {
      private final long f796 = System.currentTimeMillis();
      private final long f797;
      private final List<Identifier> f794;
      private final boolean f795;

      fl() {
         boolean v2 = bl.this.f803.nextInt(100) > 40 && !bl.DASH_ANIM_GROUPS.isEmpty();
         this.f795 = v2;
         if (v2) {
            this.f794 = bl.DASH_ANIM_GROUPS.get(bl.this.f803.nextInt(bl.DASH_ANIM_GROUPS.size()));
            this.f797 = (long)((float)(550 + bl.this.f803.nextInt(300)) * bl.this.dashLength.getValue());
         } else {
            this.f794 = bl.DASH_TEXTURES.isEmpty() ? List.of() : List.of(bl.DASH_TEXTURES.get(bl.this.f803.nextInt(bl.DASH_TEXTURES.size())));
            this.f797 = 0L;
         }
      }

      Identifier getCurrent(long nowMs) {
         if (this.f794.isEmpty()) {
            return null;
         } else if (this.f795 && this.f794.size() > 1 && this.f797 > 0L) {

            int v3 = (int)((nowMs - this.f796) / (6319398126273176674L + -6319398126273176594L) % (long)this.f794.size());
            Object result = this.f794.get(v3);

            return (Identifier)result;
         } else {
            return this.f794.get(0);
         }
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static final class gl {
      final float[] flows;
      final List<Vec3d> points;
      final float[] alphas;
      final float[] widths;

      gl(List<Vec3d> points, float[] widths, float[] alphas, float[] flows) {
         this.points = points;
         this.widths = widths;
         this.alphas = alphas;
         this.flows = flows;
      }

      
   }
}
