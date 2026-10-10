package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap.Type;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.IMinecraft;
import ru.white.utils.PacketUtil;
import ru.white.event.impl.EventMove;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Svetoch",
   desc = "Летающие светящиеся кубы с физикой",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Svetoch extends Module {
   private static final Identifier GLOW_TEXTURE_BIG = Identifier.method_60655(
      "client", "textures/particles/glow.png"
   );
   private static final double CULL_DIST_SQ = 9216.0;
   private static final int[][] EDGES;
   private long f785;
   private static final int[][] FACES;
   public tf tintColor;
   private static final RenderLayer FILL_LAYER = RenderLayer.method_75940(
      "svetoch_fill", RenderSetup.method_75927(vk.FILL_PIPELINE).method_75929(8192).method_75938()
   );
   private static final long LIFETIME = 1500L;
   private static final RenderLayer GLOW_LAYER_SMALL = RenderLayer.method_75940(
      "svetoch_glow_small",
      RenderSetup.method_75927(vk.GLOW_PIPELINE)
         .method_75934("Sampler0", vk.GLOW_TEXTURE_SMALL)
         .method_75937()
         .method_75929(4096)
         .method_75938()
   );
   private static final RenderPipeline GLOW_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56864})
         .withLocation(Identifier.method_60655("client", "svetoch_glow"))
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public SliderSetting cubeSize;
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   private final BufferAllocator f784;
   public SliderSetting count;
   private static final float[][] CORNERS;
   private static final RenderLayer LINE_LAYER = RenderLayer.method_75940(
      "svetoch_lines", RenderSetup.method_75927(vk.LINE_PIPELINE).method_75929(8192).method_75938()
   );
   private static final Identifier GLOW_TEXTURE_SMALL = Identifier.method_60655(
      "client", "textures/visuals/particles_2.png"
   );
   private static final RenderPipeline FILL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "svetoch_fill"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private final List<vk.wk> f783;
   private static final RenderPipeline LINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "svetoch_lines"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderLayer GLOW_LAYER_BIG = RenderLayer.method_75940(
      "svetoch_glow_big",
      RenderSetup.method_75927(GLOW_PIPELINE).method_75934("Sampler0", GLOW_TEXTURE_BIG).method_75937().method_75929(4096).method_75938()
   );
   private final List<vk.wk> f782;

   static {
      float[][] result = new float[8][];
      result[0] = new float[]{-1.0F, -1.0F, -1.0F};
      result[1] = new float[]{1.0F, -1.0F, -1.0F};
      result[2] = new float[]{1.0F, -1.0F, 1.0F};
      result[3] = new float[]{-1.0F, -1.0F, 1.0F};
      result[4] = new float[]{-1.0F, 1.0F, -1.0F};
      result[5] = new float[]{1.0F, 1.0F, -1.0F};
      result[6] = new float[]{1.0F, 1.0F, 1.0F};
      result[7] = new float[]{-1.0F, 1.0F, 1.0F};
      CORNERS = result;
      int[][] v0 = new int[6][];
      v0[0] = new int[]{0, 1, 2, 3};
      v0[1] = new int[]{4, 5, 6, 7};
      v0[2] = new int[]{0, 1, 5, 4};
      v0[3] = new int[]{2, 3, 7, 6};
      v0[4] = new int[]{1, 2, 6, 5};
      v0[5] = new int[]{3, 0, 4, 7};
      FACES = v0;
      int[][] v1 = new int[12][];
      v1[0] = new int[]{0, 1};
      v1[1] = new int[]{1, 2};
      v1[2] = new int[]{2, 3};
      v1[3] = new int[]{3, 0};
      v1[4] = new int[]{4, 5};
      v1[5] = new int[]{5, 6};
      v1[6] = new int[]{6, 7};
      v1[7] = new int[]{7, 4};
      v1[8] = new int[]{0, 4};
      v1[9] = new int[]{1, 5};
      v1[10] = new int[]{2, 6};
      v1[11] = new int[]{3, 7};
      EDGES = v1;
   }

   @Override
   public void toggle() {
      super.toggle();
      this.f782.clear();
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {

      this.f782.clear();
   }

   private static void m873(VertexConsumer buf, Matrix4f m, int r, int g, int b, int alpha, float s) {
      if (alpha > 0) {
         VertexConsumer result = buf.method_22918(m, -s, -s, 0.0F).method_1336(r, g, b, alpha).method_22913(0.0F, 1.0F);

         result.method_22922(OverlayTexture.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
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
         buf.method_22918(m, -s, s, 0.0F)
            .method_1336(r, g, b, alpha)
            .method_22913(0.0F, 0.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
      }
   }

   public Svetoch() {
      this.tintColor = new tf(this, "Цвет", -16711681).setVisible(() -> {
         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return this.typeColor.is("Свой");
      });
      this.count = new SliderSetting(this, "Кол кубиков", 100.0F, 50.0F, 300.0F, 1.0F);
      this.cubeSize = new SliderSetting(this, "Размер куба", 0.26F, 0.1F, 0.6F, 0.02F);
      this.f782 = new ArrayList<>();
      this.f783 = new ArrayList<>();
      this.f784 = new BufferAllocator(65536);
      this.f785 = System.nanoTime();
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (!this.f782.isEmpty() && mc.field_1724 != null && mc.field_1687 != null) {
         long v2 = System.currentTimeMillis();
         long v4 = System.nanoTime();
         float v6 = (float)Math.min((double)(v4 - this.f785) / 1.0E9, 0.1) * 60.0F;
         this.f785 = v4;
         MatrixStack v7 = e.getMatrixStack();
         Vec3d v8 = mc.field_1773.method_19418().method_71156();
         Matrix4f v9 = v7.method_23760().method_23761();
         Quaternionf v10 = mc.field_1773.method_19418().method_23767();
         float v11 = (float)(v2 % (-4552063569335143669L + 4552063569335152669L)) / 9000.0F * 360.0F;
         float v12 = (float)Math.toRadians((double)(v11 * 0.5F));
         int v13 = this.getColor();
         int v14 = v13 >> 16 & 255;
         int v15 = v13 >> 8 & 255;
         int v16 = v13 & 255;
         float v17 = this.cubeSize.getValue();
         Immediate v18 = VertexConsumerProvider.method_22991(this.f784);
         this.f783.clear();
         Iterator v19 = this.f782.iterator();

         while (v19.hasNext()) {
            vk.wk v20 = (vk.wk)v19.next();
            v20.update(v2, v6);
            if (v20.shouldRemove()) {
               v19.remove();
            } else if (!(v20.alphaValue <= 0.01F)) {
               double v21 = v20.x - v8.field_1352;
               double v23 = v20.y - v8.field_1351;
               double v25 = v20.z - v8.field_1350;
               if (!(v21 * v21 + v23 * v23 + v25 * v25 > 9216.0)) {
                  v20.cubeMatrix
                     .set(v9)
                     .translate((float)v21, (float)v23, (float)v25)
                     .rotateY((float)Math.toRadians((double)(v11 + v20.phase)))
                     .rotateX(v12);
                  v20.glowMatrix.set(v9).translate((float)v21, (float)v23, (float)v25).rotate(v10);
                  this.f783.add(v20);
               }
            }
         }

         if (!this.f783.isEmpty()) {
            VertexConsumer v36 = v18.method_73477(FILL_LAYER);

            for (vk.wk v22 : this.f783) {
               int v44 = (int)(v22.alphaValue * 0.2F * 255.0F);
               if (v44 > 0) {
                  for (int[] v27 : FACES) {
                     for (int v31 : v27) {
                        float[] v32 = CORNERS[v31];
                        v36.method_22918(v22.cubeMatrix, v32[0] * v17, v32[1] * v17, v32[2] * v17).method_1336(v14, v15, v16, v44);
                     }
                  }
               }
            }

            v36 = v18.method_73477(LINE_LAYER);

            for (vk.wk v42 : this.f783) {
               int v45 = (int)(v42.alphaValue * 255.0F);
               if (v45 > 0) {
                  for (int[] v50 : EDGES) {
                     float[] v51 = CORNERS[v50[0]];
                     float[] v52 = CORNERS[v50[1]];
                     float temp2 = v51[0] * v17;
                     float temp3 = v51[1];

                     temp3 *= v17;
                     float temp4 = v51[2] * v17;

                     v36.method_22918(v42.cubeMatrix, temp2, temp3, temp4).method_1336(v14, v15, v16, v45);
                     v36.method_22918(v42.cubeMatrix, v52[0] * v17, v52[1] * v17, v52[2] * v17).method_1336(v14, v15, v16, v45);
                  }
               }
            }

            v36 = v18.method_73477(GLOW_LAYER_BIG);
            Iterator v41 = this.f783.iterator();

            while (v41.hasNext()) {

               vk.wk v43 = (vk.wk)v41.next();
               m873(v36, v43.glowMatrix, v14, v15, v16, (int)(80.0F * v43.alphaValue), v17 * 6.0F);
            }

            v18.method_22993();
         }
      }
   }

   @EventTarget
   public void onUpdate(EventMove e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int v2 = this.count.getValue().intValue();

         for (int v4 = 0; v4 < 3 && this.f782.size() < v2; v4++) {
            Vec3d v5 = mc.field_1724
               .method_73189()
               .method_1031((double)fq.randomValue((float)(-v3), (float)v3), 0.0, (double)fq.randomValue((float)(-v3), (float)v3));
            BlockPos v6 = mc.field_1687.method_8598(Type.field_13197, BlockPos.method_49638(v5));
            double v7 = (double)((float)v6.method_10263() + fq.randomValue(-3.0F, 3.0F));
            double v9 = (double)((float)v6.method_10260() + fq.randomValue(-3.0F, 3.0F));
            double v11 = mc.field_1724.method_23318() + (double)fq.randomValue(mc.field_1724.method_17682(), (float)v3);
            Vec3d v13 = new Vec3d(v7, v11, v9);

            while (true) {
               if (!mc.field_1687.method_22347(BlockPos.method_49638(v13))) {
                  double v18;
                  int result = (v18 = v13.field_1351 - (double)mc.field_1687.method_31600()) == 0.0 ? 0 : (v18 < 0.0 ? -1 : 1);

                  if (result < 0) {
                     v13 = v13.method_1031(0.0, 1.0, 0.0);
                     continue;
                  }
               }

               Vec3d v17 = new Vec3d;
               double temp2 = mc.field_1724.method_18798().field_1352;
               double temp3 = (double)fq.randomValue(-0.6F, 0.6F);

               v17./* $VF: Unable to resugar constructor */<init>(
                  temp2 + temp3, fq.randomValue(-0.01, 0.44), mc.field_1724.method_18798().field_1350 + (double)fq.randomValue(-0.6F, 0.6F)
               );
               Vec3d v14 = v17;
               this.f782.add(new vk.wk(v13, v14));
               break;
            }
         }
      }
   }

   public int getColor() {
      if (this.typeColor.is("Тема")) {
         return up.getClientColor1(1);
      } else {
         Integer result = this.tintColor.getValue();

         return result;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class wk {
      double z;
      final Matrix4f glowMatrix;
      double mX;
      double mY;
      final long start;
      float alphaValue;
      private static final double GRAVITY = 1.0E-4;
      double x;
      double mZ;
      private static final double BASE_VELOCITY = 0.05;
      double y;
      final Matrix4f cubeMatrix;
      final float phase;
      final Animation alpha = new Animation();
      private static final double BOUNCINESS = 0.85;
      boolean fadingOut;

      private static boolean m872(double px, double py, double pz) {
         BlockPos v6 = BlockPos.method_49637(px, py, pz);
         BlockState result = hp.mc.field_1687.method_8320(v6);

         return result.method_26234(hp.mc.field_1687, v6);
      }

      void update(long now, float ticks) {
         double v4 = this.x + this.mX * (double)ticks;
         double v6 = this.y + this.mY * (double)ticks;
         double v8 = this.z + this.mZ * (double)ticks;
         if (Math.floor(v4) == Math.floor(this.x) && Math.floor(v6) == Math.floor(this.y) && Math.floor(v8) == Math.floor(this.z)) {
            if (m872(v4, v6, v8)) {
               this.mX = -this.mX * 0.85;
               this.mY = -this.mY * 0.85;
               this.mZ = -this.mZ * 0.85;
            } else {
               this.x = v4;
               this.y = v6;

               this.z = v8;
            }
         } else {
            if (m872(v4, this.y, this.z)) {
               this.mX = -this.mX * 0.85;
            } else {
               this.x = v4;
            }

            if (m872(this.x, v6, this.z)) {
               this.mY = -this.mY * 0.85;
            } else {
               this.y = v6;
            }

            if (m872(this.x, this.y, v8)) {
               this.mZ = -this.mZ * 0.85;
            } else {
               this.z = v8;
            }
         }

         this.mY -= 1.0E-4 * (double)ticks;
         if (!this.fadingOut && now - this.start > -1677651759182795432L + 1677651759182796932L) {
            this.fadingOut = true;
            this.alpha.run(0.0, 2.0, ap.LINEAR, true);
         }

         this.alpha.update();
         this.alphaValue = this.alpha.get();
      }

      wk(Vec3d pos, Vec3d velocity) {
         this.cubeMatrix = new Matrix4f();
         this.glowMatrix = new Matrix4f();
         this.start = System.currentTimeMillis();
         this.phase = (float)(Math.random() * 100.0);
         this.x = pos.field_1352;
         this.y = pos.field_1351;
         this.z = pos.field_1350;
         this.mX = velocity.field_1352 * 0.05;
         this.mY = velocity.field_1351 * 0.05;
         this.mZ = velocity.field_1350 * 0.05;
         this.alpha.set(0.0);
         this.alpha.run(1.0, 0.5, ap.LINEAR, true);
      }

      boolean shouldRemove() {

         return this.fadingOut && this.alphaValue <= 0.01F;
      }

      
   }
}
