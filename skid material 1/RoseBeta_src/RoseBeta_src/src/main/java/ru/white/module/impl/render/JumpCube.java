package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
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
import ru.white.event.impl.EventPreMotion;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Jump Cube",
   desc = "Кубики вокруг игрока - прыгают и крутятся",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class JumpCube extends Module implements jf {
   public tf tintColor;
   private static final double MAX_OWNER_DIST = 6.0;
   private static final double SPAWN_MIN_DST = 0.6;
   private static final RenderLayer GLOW_LAYER_SMALL = RenderLayer.method_75940(
      "jump_cube_glow_small",
      RenderSetup.method_75927(yj.GLOW_PIPELINE).method_75934("Sampler0", yj.GLOW_TEX_SMALL).method_75937().method_75929(4096).method_75938()
   );
   public SliderSetting count;
   private static final Identifier GLOW_TEX_BIG = Identifier.method_60655(
      "client", "textures/visuals/particles_1.png"
   );
   private static final RenderPipeline FILL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "jump_cube_fill"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private final lf f695;
   private static final double SPAWN_MAX_DST = 2.6;
   private static final long LIFETIME_MS = 2000L;
   private static final RenderLayer GLOW_LAYER_BIG = RenderLayer.method_75940(
      "jump_cube_glow_big",
      RenderSetup.method_75927(yj.GLOW_PIPELINE).method_75934("Sampler0", GLOW_TEX_BIG).method_75937().method_75929(4096).method_75938()
   );
   private static final int[] JUMP_YAWS = new int[]{0, 90, 180, 270};
   private static final long RANGE_FADE_MS = 400L;
   private static final RenderLayer LINE_LAYER = RenderLayer.method_75940(
      "jump_cube_lines",
      RenderSetup.method_75927(yj.LINE_PIPELINE).method_75929(4096).method_75938()
   );
   public sf previewButton = lf.button(this);
   private static final RenderPipeline LINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "jump_cube_lines"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final int RES_PX = 16;
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   private final Random f696;
   private final BufferAllocator f699;
   private static final RenderLayer FILL_LAYER = RenderLayer.method_75940(
      "jump_cube_fill", RenderSetup.method_75927(FILL_PIPELINE).method_75929(8192).method_75938()
   );
   private static final RenderPipeline GLOW_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56864})
         .withLocation(Identifier.method_60655("client", "jump_cube_glow"))
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private final List<yj.zj> f697;
   public SliderSetting size;
   private static final Identifier GLOW_TEX_SMALL = Identifier.method_60655(
      "client", "textures/visuals/particles_2.png"
   );
   private final List<yj.zj> f698;

   @Override
   public void previewTick(kf ctx) {
      for (yj.zj v3 : this.f697) {
         v3.tryStartJump();
         
      }
   }

   private boolean m771(BlockPos pos) {
      if (mc.field_1687 == null) {
         return false;
      } else if (!mc.field_1687.method_22347(pos)) {
         return false;
      } else {
         return mc.field_1687.method_22347(pos.method_10074()) ? false : mc.field_1687.method_8335(null, new Box(pos)).isEmpty();
      }
   }

   @EventTarget
   public void onRender(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null && !this.f697.isEmpty()) {
         float v2 = e.getTickDelta();
         Vec3d v3 = mc.field_1773.method_19418().method_71156();
         MatrixStack v4 = e.getMatrixStack();
         float v5 = this.size.getValue();
         int v6 = this.typeColor.is("Тема") ? up.getClientColor1(1) : this.tintColor.getValue();
         int v7 = v6 >> 16 & 255;
         int v8 = v6 >> 8 & 255;
         int v9 = v6 & 255;
         this.f698.clear();

         for (yj.zj v11 : this.f697) {
            if (!v11.isDead()) {
               float v12 = v11.getAlpha();
               if (!(v12 < 0.01F)) {
                  double v13 = v11.spawnPos.field_1352 - v3.field_1352;
                  double v15 = v11.spawnPos.field_1351 + v11.getJumpYOffset(v2) - v3.field_1351;
                  double v17 = v11.spawnPos.field_1350 - v3.field_1350;
                  v4.method_22903();
                  v4.method_22904(v13, v15, v17);
                  if (v11.jumpTicksMax > 0) {
                     float v19 = MathHelper.method_16439(v2, (float)v11.prevJumpTicks, (float)v11.jumpTicks);
                     float v20 = 1.0F - v19 / (float)v11.jumpTicksMax;
                     v4.method_22907(RotationAxis.field_40716.rotationDegrees((float)v11.jumpYaw));
                     v4.method_22907(RotationAxis.field_40714.rotationDegrees(90.0F * v20));
                  }

                  v4.method_22905(v12, v12, v12);
                  v11.cubeMatrix.set(v4.method_23760().method_23761());
                  v4.method_22909();
                  v4.method_22903();
                  v4.method_22904(v13, v15, v17);
                  v4.method_22907(mc.field_1773.method_19418().method_23767());
                  v4.method_22905(v12, v12, v12);
                  v11.glowMatrix.set(v4.method_23760().method_23761());
                  v4.method_22909();
                  v11.cachedAlpha = v12;
                  this.f698.add(v11);
               }
            }
         }

         if (!this.f698.isEmpty()) {
            Immediate v22 = VertexConsumerProvider.method_22991(this.f699);
            VertexConsumer v23 = v22.method_73477(FILL_LAYER);

            for (yj.zj v31 : this.f698) {
               int v14 = (int)(v31.cachedAlpha * 0.2F * 255.0F);
               if (v14 > 0) {

                  m773(v23, v31.cubeMatrix, v5, v7, v8, v9, v14);
               }
            }

            v23 = v22.method_73477(LINE_LAYER);

            for (yj.zj v32 : this.f698) {
               int v35 = (int)(v32.cachedAlpha * 255.0F);
               if (v35 > 0) {
                  m774(v23, v32.cubeMatrix, v5, v7, v8, v9, v35);
               }
            }

            v23 = v22.method_73477(GLOW_LAYER_BIG);

            for (yj.zj v33 : this.f698) {
               m776(v23, v33.glowMatrix, v7, v8, v9, (int)(35.0F * v33.cachedAlpha), v5 * 5.5F);
            }

            v23 = v22.method_73477(GLOW_LAYER_SMALL);

            for (yj.zj v34 : this.f698) {
               m776(v23, v34.glowMatrix, v7, v8, v9, (int)(40.0F * v34.cachedAlpha), v5 * 1.8F);
            }

            v22.method_22993();
         }
      }
   }

   private static void m773(VertexConsumer b, Matrix4f m, float s, int r, int g, int bl, int a) {
      float v7 = s / 2.0F;
      b.method_22918(m, -v7, -v7, -v7).method_1336(r, g, bl, a);
      b.method_22918(m, v7, -v7, -v7).method_1336(r, g, bl, a);
      b.method_22918(m, v7, -v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, -v7, -v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, -v7, v7, -v7).method_1336(r, g, bl, a);
      b.method_22918(m, v7, v7, -v7).method_1336(r, g, bl, a);
      b.method_22918(m, v7, v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, -v7, v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, -v7, -v7, -v7).method_1336(r, g, bl, a);
      VertexConsumer result = b.method_22918(m, v7, -v7, -v7);

      result.method_1336(r, g, bl, a);
      b.method_22918(m, v7, v7, -v7).method_1336(r, g, bl, a);
      b.method_22918(m, -v7, v7, -v7).method_1336(r, g, bl, a);
      result = b.method_22918(m, -v7, -v7, v7);

      result.method_1336(r, g, bl, a);
      b.method_22918(m, v7, -v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, v7, v7, v7).method_1336(r, g, bl, a);

      b.method_22918(m, -v7, v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, -v7, -v7, -v7).method_1336(r, g, bl, a);
      b.method_22918(m, -v7, -v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, -v7, v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, -v7, v7, -v7).method_1336(r, g, bl, a);
      b.method_22918(m, v7, -v7, -v7).method_1336(r, g, bl, a);
      b.method_22918(m, v7, -v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, v7, v7, v7).method_1336(r, g, bl, a);
      b.method_22918(m, v7, v7, -v7).method_1336(r, g, bl, a);
   }

   @EventTarget
   public void onJump(EventPreMotion e) {
      for (yj.zj v3 : this.f697) {
         v3.tryStartJump();
      }
   }

   @Override
   protected void onDisable() {
      this.f697.clear();

      
   }

   private static void m776(VertexConsumer buf, Matrix4f m, int r, int g, int b, int alpha, float s) {
      if (alpha > 0) {
         buf.method_22918(m, -s, -s, 0.0F)
            .method_1336(r, g, b, alpha)
            .method_22913(0.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);

         buf.method_22918(m, s, -s, 0.0F)
            .method_1336(r, g, b, alpha)
            .method_22913(1.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);

         buf.method_22918(m, s, s, 0.0F)
            .method_1336(r, g, b, alpha)
            .method_22913(1.0F, 0.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         VertexConsumer v10 = buf.method_22918(m, -s, s, 0.0F);

         v10.method_1336(r, g, b, alpha)
            .method_22913(0.0F, 0.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
      }
   }

   private Vec3d m772(BlockPos pos, Vec3d playerPos) {
      double v3 = 0.0625;
      double v5 = v3 / 2.0;

      for (int v7 = 64; v7 > 0; v7--) {
         Vec3d result = new Vec3d;
         double temp2 = (double)pos.method_10263();

         result./* $VF: Unable to resugar constructor */<init>(
            temp2 + this.f696.nextDouble(), (double)pos.method_10264() + this.f696.nextDouble(), (double)pos.method_10260() + this.f696.nextDouble()
         );
         Vec3d v8 = result;
         double v9 = v8.method_1022(playerPos);
         if (v9 >= 0.6 && v9 <= 2.6) {
            double v11 = Math.floor(v8.field_1352 * 16.0) / 16.0 + v5;
            double v13 = Math.floor(v8.field_1351) + v5 - v3;
            double v15 = Math.floor(v8.field_1350 * 16.0) / 16.0 + v5;
            return new Vec3d(v11, v13, v15);
         }
      }

      return null;
   }

   @Override
   public lf previewSettings() {

      int v4 = v2 * (v2 + 1);

      return this.f695;
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         Vec3d v2 = mc.field_1724.method_73189();
         List v3 = this.m770(v2, 0.6, 2.6, 3);
         int v4 = this.count.getValue().intValue();

         for (int v5 = 0; v5 < v4; v5++) {

            if (v3.isEmpty()) {
               break;
            }

            int v6 = this.f696.nextInt(v3.size());
            Vec3d v7 = this.m772((BlockPos)v3.get(v6), v2);
            if (v7 != null) {
               List v11 = this.f697;

               yj.zj v12 = new yj.zj;

               v12./* $VF: Unable to resugar constructor */<init>(v7);
               v11.add(v12);
            }
         }

         this.f697.forEach(yj.zj::updateLogic);
         this.f697.removeIf(yj.zj::isDead);
      }
   }

   public JumpCube() {
      this.tintColor = new tf(this, "Цвет", -16711681).setVisible(() -> {
         Boolean result = this.typeColor.is("Свой");

         return result;
      });
      this.count = new SliderSetting(this, "Кубиков", 3.0F, 1.0F, 10.0F, 1.0F);
      this.size = new SliderSetting(this, "Размер", 0.18F, 0.05F, 0.4F, 0.01F);
      this.f695 = lf.none(this);
      this.f696 = new Random();
      this.f697 = new ArrayList<>();
      this.f698 = new ArrayList<>();
      this.f699 = new BufferAllocator(65536);
   }

   @Override
   public void previewStop() {

      this.f697.clear();

      
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {

      this.f697.clear();
   }

   private List<BlockPos> m770(Vec3d center, double minDst, double maxDst, int offsetDown) {
      ArrayList result = new ArrayList;

      result./* $VF: Unable to resugar constructor */<init>();
      ArrayList v7 = result;
      if (mc.field_1687 == null) {
         return v7;
      } else {
         double v8 = center.field_1352;

         double v10 = center.field_1351 + 1.0;
         double v12 = center.field_1350;
         double v14 = v8 - maxDst;

         while (true) {
            double v25 = v8 + maxDst;

            if (!(v14 < v25)) {
               return v7;
            }

            for (double v16 = v12 - maxDst; v16 < v12 + maxDst; v16++) {
               for (double v18 = v10 - (double)offsetDown; v18 < v10; v18++) {
                  BlockPos v20 = BlockPos.method_49637(v14, v18, v16);
                  if (this.m771(v20) && !v7.contains(v20)) {
                     v7.add(v20);
                  }
               }
            }

            v14++;
         }
      }
   }

   private static void m774(VertexConsumer b, Matrix4f m, float s, int r, int g, int bl, int a) {
      float v7 = s / 2.0F;
      m775(b, m, -v7, -v7, -v7, v7, -v7, -v7, r, g, bl, a);
      m775(b, m, v7, -v7, -v7, v7, -v7, v7, r, g, bl, a);
      m775(b, m, v7, -v7, v7, -v7, -v7, v7, r, g, bl, a);
      m775(b, m, -v7, -v7, v7, -v7, -v7, -v7, r, g, bl, a);
      m775(b, m, -v7, v7, -v7, v7, v7, -v7, r, g, bl, a);
      m775(b, m, v7, v7, -v7, v7, v7, v7, r, g, bl, a);
      m775(b, m, v7, v7, v7, -v7, v7, v7, r, g, bl, a);
      m775(b, m, -v7, v7, v7, -v7, v7, -v7, r, g, bl, a);
      m775(b, m, -v7, -v7, -v7, -v7, v7, -v7, r, g, bl, a);
      m775(b, m, v7, -v7, -v7, v7, v7, -v7, r, g, bl, a);
      m775(b, m, v7, -v7, v7, v7, v7, v7, r, g, bl, a);
      float temp2 = -v7;
      float temp3 = -v7;
      float temp5 = -v7;

      m775(b, m, temp2, temp3, v7, temp5, v7, v7, r, g, bl, a);
   }

   @Override
   public void previewSpawn(kf ctx) {
   }

   private static void m775(VertexConsumer b, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, int r, 

      b.method_22918(m, x1, y1, z1).method_1336(r, g, bl, a);

      b.method_22918(m, x2, y2, z2).method_1336(r, g, bl, a);
   }

   @Environment(EnvType.CLIENT)
   private class zj {
      long outOfRangeStart;
      int jumpTicksMax;
      double jumpHeight;
      float cachedAlpha;
      final Vec3d spawnPos;
      int jumpTicks;
      int prevJumpTicks;
      boolean outOfRange;
      final Matrix4f cubeMatrix;
      final Matrix4f glowMatrix;
      int jumpYaw;
      final long spawnTime = System.currentTimeMillis();

      float getTimePc() {
         long result = System.currentTimeMillis();

         return MathHelper.method_15363((float)(result - this.spawnTime) / 2000.0F, 0.0F, 1.0F);
      }

      zj(Vec3d spawnPos) {
         this.jumpTicks = 0;
         this.prevJumpTicks = 0;
         this.jumpTicksMax = 0;
         this.jumpYaw = 0;
         this.jumpHeight = 0.0;
         this.outOfRange = false;
         this.outOfRangeStart = 0L;
         this.cubeMatrix = new Matrix4f();
         this.glowMatrix = new Matrix4f();
         this.spawnPos = spawnPos;
      }

      void tryStartJump() {
         if (this.jumpTicks <= 0) {
            this.jumpTicksMax = this.jumpTicks = (int)(14.0F * (0.5F + 0.5F * yj.this.f696.nextFloat()));
            this.jumpHeight = (double)(2 + yj.this.f696.nextInt(11)) / 16.0;

            this.jumpYaw = yj.JUMP_YAWS[yj.this.f696.nextInt(yj.JUMP_YAWS.length)];
         }
      }

      float getAlpha() {
         float v1 = this.getTimePc();
         float result;
         if (v1 < 0.1F) {
            result = v1 / 0.1F;
         } else if (v1 > 0.8F) {

            result = 1.0F - (v1 - 0.8F) / 0.2F;
         } else {
            result = 1.0F;
         }

         float v2 = result;
         return MathHelper.method_15363(v2, 0.0F, 1.0F) * this.getRangeFade();
      }

      void updateLogic() {
         this.prevJumpTicks = this.jumpTicks;
         if (this.jumpTicks > 0) {
            this.jumpTicks--;
         }

         if (hp.mc.field_1724 != null) {
            Vec3d result = hp.mc.field_1724.method_73189();

            if (result.method_1022(this.spawnPos) > 6.0 && !this.outOfRange) {
               this.outOfRange = true;
               this.outOfRangeStart = System.currentTimeMillis();
            }
         }
      }

      boolean isDead() {
         if (!(this.getTimePc() >= 1.0F)) {

            if (!this.outOfRange || !(this.getRangeFade() <= 0.01F)) {
               return false;
            }
         }

         return true;
      }

      float getRangeFade() {
         if (!this.outOfRange) {
            return 1.0F;
         } else {
            float result = MathHelper.method_15363(1.0F - (float)(System.currentTimeMillis() - this.outOfRangeStart) / 400.0F, 0.0F, 1.0F);

            return result;
         }
      }

      double getJumpYOffset(float pt) {
         if (this.jumpTicksMax <= 0) {
            return 0.0;
         } else {

            float v2 = MathHelper.method_16439(pt, (float)this.prevJumpTicks, (float)this.jumpTicks) / (float)this.jumpTicksMax;
            if (v2 > 0.5F) {

               v2 = 1.0F - v2;
            }

            return (double)v2 * 2.0 * this.jumpHeight;
         }
      }

      
   }
}
