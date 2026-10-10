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
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderSetup.Builder;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.ClientUtil;
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
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Totem Ghost",
   desc = "Призрак модельки при лопании тотема",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class TotemGhost extends Module implements jf {
   private final List<zk.al> f792;
   private static final RenderLayer GLOW_LAYER_BIG;
   private static final RenderPipeline LINE_PIPELINE;
   public tf tintColor;
   private static final RenderLayer FILL_LAYER;
   private static final Identifier GLOW_TEXTURE_SMALL;
   private final BufferAllocator f793;
   public SliderSetting volume;
   private static final float[][] PIVOTS;
   public ModeSetting mode;
   private static final RenderPipeline VIDEO_PIPELINE;
   private static final int[][] FACES;
   private static final int PART_COUNT;
   public SliderSetting rise;
   private static final RenderPipeline GLOW_PIPELINE;
   private static final RenderLayer[] VIDEO_LAYERS = new RenderLayer[117];
   public sf previewButton = lf.button(this);
   private static final Identifier[] VIDEO_FRAMES = new Identifier[117];
   private final lf f791;
   public ModeSetting typeColor;
   private static final int[][] EDGES;
   public SliderSetting videoScale;
   private static final RenderLayer GLOW_LAYER_SMALL;
   private static final RenderPipeline FILL_PIPELINE;
   private static final RenderLayer LINE_LAYER;
   public SliderSetting duration;
   private static final float[][] PARTS;
   private static final int FRAME_COUNT = 117;
   private static final Identifier GLOW_TEXTURE_BIG;

   private static void m907(VertexConsumer buf, Matrix4f m, float[] p, int r, int g, int b, int alpha) {
      int[][] v7 = EDGES;
      int v8 = v7.length;

      for (int v9 = 0; v9 < v8; v9++) {

         int[] v10 = v7[v9];

         buf.method_22918(m, m905(p, v10[0], 0), m905(p, v10[0], 1), m905(p, v10[0], 2)).method_1336(r, g, b, alpha);
         buf.method_22918(m, m905(p, v10[1], 0), m905(p, v10[1], 1), m905(p, v10[1], 2)).method_1336(r, g, b, alpha);
      }
   }

   public void spawnGhost(Entity entity) {
      if (this.isEnabled() && entity != null) {
         boolean v2 = this.mode.is("Призрак") || this.mode.is("Оба");
         boolean v3 = this.mode.is("MusorDrop") || this.mode.is("Оба");
         zk.al result = new zk.al;
         Vec3d temp2 = entity.method_73189();
         float temp3 = entity.method_36454();

         result./* $VF: Unable to resugar constructor */<init>(temp2, temp3, v2, v3);
         zk.al v4 = result;
         if (v2 && entity instanceof LivingEntity v5) {
            v4.bodyYaw = v5.method_73188();
            v4.headYawDeg = MathHelper.method_15363(MathHelper.method_15393(v5.method_5791() - v5.method_73188()), -75.0F, 75.0F);
            float v6 = v5.field_42108.method_48569();
            float v7 = Math.min(v5.field_42108.method_48566(), 1.0F);
            v4.partPitchDeg[0] = v5.method_36455();
            v4.partPitchDeg[2] = (float)Math.toDegrees((double)(MathHelper.method_15362((double)(v6 * 0.6662F + (float) Math.PI)) * 2.0F * v7 * 0.5F));

            v4.partPitchDeg[3] = (float)Math.toDegrees((double)(MathHelper.method_15362((double)(v6 * 0.6662F)) * 2.0F * v7 * 0.5F));
            v4.partPitchDeg[4] = (float)Math.toDegrees((double)(MathHelper.method_15362((double)(v6 * 0.6662F)) * 1.4F * v7));
            v4.partPitchDeg[5] = (float)Math.toDegrees((double)(MathHelper.method_15362((double)(v6 * 0.6662F + (float) Math.PI)) * 1.4F * v7));
         } else {
            v4.bodyYaw = entity.method_36454();
         }

         if (v3) {
            br.playSound_wav("musordrop", this.volume.getValue() / 100.0F);
         }

         this.f792.add(v4);
      }
   }

   private static void m909(VertexConsumer buf, Matrix4f m, float halfW, float halfH, int alpha) {
      if (alpha > 0) {
         VertexConsumer result = buf.method_22918(m, -halfW, -halfH, 0.0F);
         int temp1 = 255;
         int temp2 = 255;

         result.method_1336(temp1, temp2, 255, alpha)
            .method_22913(0.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buf.method_22918(m, halfW, -halfH, 0.0F)
            .method_1336(255, 255, 255, alpha)
            .method_22913(1.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         buf.method_22918(m, halfW, halfH, 0.0F)
            .method_1336(255, 255, 255, alpha)
            .method_22913(1.0F, 0.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         result = buf.method_22918(m, -halfW, halfH, 0.0F);
         temp1 = 255;

         result.method_1336(temp1, 255, 255, alpha)
            .method_22913(0.0F, 0.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
      }
   }

   private static void m906(VertexConsumer buf, Matrix4f m, float[] p, int r, int g, int b, int alpha) {
      int[][] v7 = FACES;

      int v8 = v7.length;

      for (int v9 = 0; v9 < v8; v9++) {
         int[] v10 = v7[v9];

         for (int v14 : v10) {
            buf.method_22918(m, m905(p, v14, 0), m905(p, v14, 1), m905(p, v14, 2)).method_1336(r, g, b, alpha);
         }
      }
   }

   @Override
   public void previewSpawn(kf ctx) {
      this.spawnGhost(ctx.dummy());

      
   }

   @Override
   public void previewStop() {

      this.f792.clear();
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
      int[][] v2 = new int[6][];
      v2[0] = new int[]{0, 1, 3, 2};
      v2[1] = new int[]{4, 5, 7, 6};
      v2[2] = new int[]{0, 1, 5, 4};
      v2[3] = new int[]{2, 3, 7, 6};
      v2[4] = new int[]{0, 2, 6, 4};
      v2[5] = new int[]{1, 3, 7, 5};
      FACES = v2;
      int[][] v3 = new int[12][];
      v3[0] = new int[]{0, 1};
      v3[1] = new int[]{1, 3};
      v3[2] = new int[]{3, 2};
      v3[3] = new int[]{2, 0};
      v3[4] = new int[]{4, 5};
      v3[5] = new int[]{5, 7};
      v3[6] = new int[]{7, 6};
      v3[7] = new int[]{6, 4};
      v3[8] = new int[]{0, 4};
      v3[9] = new int[]{1, 5};
      v3[10] = new int[]{2, 6};
      v3[11] = new int[]{3, 7};
      EDGES = v3;

      for (int v0 = 0; v0 < 117; v0++) {
         VIDEO_FRAMES[v0] = Identifier.method_60655(
            "client", String.format("textures/musordrop/frame_%03d.png", v0 + 1)
         );
      }

      VIDEO_PIPELINE = RenderPipelines.method_67887(
         RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56864})
            .withLocation(Identifier.method_60655("client", "totem_musordrop_video"))
            .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .build()
      );
      GLOW_TEXTURE_BIG = Identifier.method_60655("client", "textures/visuals/particles_1.png");
      GLOW_TEXTURE_SMALL = Identifier.method_60655("client", "textures/visuals/particles_2.png");
      FILL_PIPELINE = RenderPipelines.method_67887(
         RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
            .withLocation(Identifier.method_60655("client", "totem_ghost_fill"))
            .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.LIGHTNING)
            .build()
      );
      FILL_LAYER = RenderLayer.method_75940(
         "totem_ghost_fill",
         RenderSetup.method_75927(FILL_PIPELINE).method_75937().method_75929(4096).method_75938()
      );
      LINE_PIPELINE = RenderPipelines.method_67887(
         RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
            .withLocation(Identifier.method_60655("client", "totem_ghost_lines"))
            .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.LIGHTNING)
            .build()
      );
      LINE_LAYER = RenderLayer.method_75940(
         "totem_ghost_lines",
         RenderSetup.method_75927(LINE_PIPELINE).method_75929(4096).method_75938()
      );
      GLOW_PIPELINE = RenderPipelines.method_67887(
         RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56864})
            .withLocation(Identifier.method_60655("client", "totem_ghost_glow"))
            .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.LIGHTNING)
            .build()
      );
      GLOW_LAYER_BIG = RenderLayer.method_75940(
         "totem_ghost_glow_big",
         RenderSetup.method_75927(GLOW_PIPELINE).method_75934("Sampler0", GLOW_TEXTURE_BIG).method_75937().method_75929(1024).method_75938()
      );
      GLOW_LAYER_SMALL = RenderLayer.method_75940(
         "totem_ghost_glow_small",
         RenderSetup.method_75927(GLOW_PIPELINE)
            .method_75934("Sampler0", GLOW_TEXTURE_SMALL)
            .method_75937()
            .method_75929(1024)
            .method_75938()
      );
   }

   private static float m905(float[] p, int bit, int axis) {
      float v6;
      if ((bit >> axis & 1) == 0) {

         v6 = p[axis];
      } else {
         v6 = p[axis + 3];
      }

      return v6;
   }

   @Override
   public lf previewSettings() {

      return this.f791;
   }

   @Override
   public void toggle() {
      super.toggle();

      this.f792.clear();
      
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (!this.f792.isEmpty() && mc.field_1724 != null && mc.field_1687 != null) {
         long v2 = System.currentTimeMillis();
         float v4 = this.rise.getValue();
         int v5 = this.getColor();

         int v6 = v5 >> 16 & 255;
         int v7 = v5 >> 8 & 255;
         int v8 = v5 & 255;
         MatrixStack v9 = e.getMatrixStack();
         Vec3d v10 = mc.field_1773.method_19418().method_71156();
         Iterator v11 = this.f792.iterator();

         while (v11.hasNext()) {
            zk.al v12 = (zk.al)v11.next();
            long v13 = v12.isMusorDrop ? -8048283243166189459L + 8048283243166195309L : (long)(this.duration.getValue() * 1000.0F);
            float v15 = (float)(v2 - v12.start) / (float)v13;
            if (v15 >= 1.0F) {
               v11.remove();
            } else {
               float v16 = 1.0F - (1.0F - v15) * (1.0F - v15) * (1.0F - v15);
               float v17 = Math.min(v15 * 8.0F, 1.0F);
               float v18 = v12.isMusorDrop
                  ? (v15 > 0.85F ? 1.0F - (v15 - 0.85F) / 0.15F : 1.0F)
                  : (v15 > 0.55F ? 1.0F - (v15 - 0.55F) / 0.45F : 1.0F);
               v12.alphaValue = v17 * v18;
               double v19 = v12.x - v10.field_1352;
               double v21 = v12.y - v10.field_1351 + (double)(v16 * v4);
               double v23 = v12.z - v10.field_1350;
               if (v12.isGhost) {
                  v9.method_22903();
                  v9.method_22904(v19, v21, v23);
                  v9.method_22907(RotationAxis.field_40716.rotationDegrees(-v12.bodyYaw + v15 * 40.0F));

                  for (int v25 = 0; v25 < PART_COUNT; v25++) {
                     float v26 = v12.partPitchDeg[v25];
                     float v27 = v25 == 0 ? v12.headYawDeg : 0.0F;
                     if (v26 == 0.0F && v27 == 0.0F) {
                        v12.partMatrices[v25].set(v9.method_23760().method_23761());
                     } else {
                        float[] v28 = PIVOTS[v25];
                        v9.method_22903();
                        v9.method_46416(v28[0], v28[1], v28[2]);
                        if (v27 != 0.0F) {
                           v9.method_22907(RotationAxis.field_40716.rotationDegrees(-v27));
                        }

                        if (v26 != 0.0F) {
                           v9.method_22907(RotationAxis.field_40714.rotationDegrees(v26));
                        }

                        v9.method_46416(-v28[0], -v28[1], -v28[2]);
                        v12.partMatrices[v25].set(v9.method_23760().method_23761());
                        v9.method_22909();
                     }
                  }

                  v9.method_22909();
                  v9.method_22903();
                  v9.method_22904(v19, v21 + 1.1, v23);
                  v9.method_22907(mc.field_1773.method_19418().method_23767());
                  v12.glowMatrix.set(v9.method_23760().method_23761());
                  v9.method_22909();
               }

               if (v12.isMusorDrop) {
                  v9.method_22903();
                  v9.method_22904(v19, v21 + 1.2, v23);
                  v9.method_22907(mc.field_1773.method_19418().method_23767());
                  v12.videoMatrix.set(v9.method_23760().method_23761());
                  v9.method_22909();
               }
            }
         }

         if (!this.f792.isEmpty()) {
            Immediate v30 = VertexConsumerProvider.method_22991(this.f793);
            VertexConsumer v31 = v30.method_73477(FILL_LAYER);

            for (zk.al v39 : this.f792) {
               if (v39.isGhost) {
                  int v44 = (int)(v39.alphaValue * 0.25F * 255.0F);
                  if (v44 > 0) {
                     for (int v47 = 0; v47 < PART_COUNT; v47++) {
                        m906(v31, v39.partMatrices[v47], PARTS[v47], v6, v7, v8, v44);
                     }
                  }
               }
            }

            v31 = v30.method_73477(LINE_LAYER);

            for (zk.al v40 : this.f792) {
               if (v40.isGhost) {
                  int v45 = (int)(v40.alphaValue * 0.45F * 255.0F);
                  if (v45 > 0) {
                     for (int v48 = 0; v48 < PART_COUNT; v48++) {
                        m907(v31, v40.partMatrices[v48], PARTS[v48], v6, v7, v8, v45);
                     }
                  }
               }
            }

            v31 = v30.method_73477(GLOW_LAYER_BIG);

            for (zk.al v41 : this.f792) {
               if (v41.isGhost) {
                  m908(v31, v41.glowMatrix, v6, v7, v8, (int)(110.0F * v41.alphaValue), 1.5F);
               }
            }

            v31 = v30.method_73477(GLOW_LAYER_SMALL);

            for (zk.al v42 : this.f792) {
               if (v42.isGhost) {
                  m908(v31, v42.glowMatrix, v6, v7, v8, (int)(160.0F * v42.alphaValue), 0.7F);
               }
            }

            float v38 = this.videoScale.getValue();
            float v43 = 0.55F * v38;
            float v46 = v43 * 1.905F;

            for (zk.al v50 : this.f792) {
               if (v50.isMusorDrop) {
                  int v51 = (int)(v50.alphaValue * 255.0F);
                  if (v51 > 0) {
                     float v20 = MathHelper.method_15363((float)(v2 - v50.start) / 5850.0F, 0.0F, 1.0F);
                     int v52 = MathHelper.method_15340((int)(v20 * 117.0F), 0, 116);
                     RenderLayer v22 = m910(v52);
                     VertexConsumer v53 = v30.method_73477(v22);
                     m909(v53, v50.videoMatrix, v46, v43, v51);
                  }
               }
            }

            v30.method_22993();
         }
      }
   }

   private static void m908(VertexConsumer buf, Matrix4f m, int r, int g, int b, int alpha, float s) {
      if (alpha > 0) {
         buf.method_22918(m, -s, -s, 0.0F)
            .method_1336(r, g, b, alpha)
            .method_22913(0.0F, 1.0F)
            .method_22922(OverlayTexture.field_21444)
            .method_60803(15728880)
            .method_22914(0.0F, 0.0F, 1.0F);
         VertexConsumer result = buf.method_22918(m, s, -s, 0.0F).method_1336(r, g, b, alpha);

         result.method_22913(1.0F, 1.0F).method_22922(OverlayTexture.field_21444).method_60803(15728880).method_22914(0.0F, 0.0F, 1.0F);
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

   private static RenderLayer m910(int frameIdx) {
      int v1 = MathHelper.method_15340(frameIdx, 0, 116);
      if (VIDEO_LAYERS[v1] == null) {
         RenderLayer[] result = VIDEO_LAYERS;

         String temp2 = "totem_musordrop_" + v1;
         Builder v7 = RenderSetup.method_75927(VIDEO_PIPELINE).method_75934("Sampler0", VIDEO_FRAMES[v1]).method_75937();

         result[v1] = RenderLayer.method_75940(temp2, v7.method_75929(1024).method_75938());
      }

      return VIDEO_LAYERS[v1];
   }

   public 

      if (this.typeColor.is("Тема")) {
         int result = up.getClientColor1(1);

         return result;
      } else {
         return this.tintColor.getValue();
      }
   }

   public TotemGhost() {
      this.mode = new ModeSetting(this, "Режим", "Призрак", "MusorDrop", "Оба");
      this.typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой").setVisible(() -> !this.mode.is("MusorDrop"));
      this.tintColor = new tf(this, "Цвет", -16711681).setVisible(() -> {
         if (!this.mode.is("MusorDrop")) {

            if (this.typeColor.is("Свой")) {
               return true;
            }
         }

         return false;
      });
      this.rise = new SliderSetting(this, "Высота подъёма", 2.0F, 0.5F, 4.0F, 0.1F);
      this.duration = new SliderSetting(this, "Длительность", 2.0F, 1.0F, 6.0F, 0.1F);
      this.volume = new SliderSetting(this, "Громкость", 30.0F, 0.0F, 100.0F, 1.0F).setVisible(() -> {
         boolean result;
         if (!this.mode.is("Призрак")) {
            result = true;
            
         } else {
            result = false;
         }

         return result;
      });
      this.videoScale = new SliderSetting(this, "Размер видео", 1.8F, 0.5F, 4.0F, 0.1F).setVisible(() -> {
         boolean result = this.mode.is("Призрак");

         return !result;
      });
      this.f791 = lf.of(this, 4.0F, 0.0F, 3.0F);
      this.f792 = new ArrayList<>();
      this.f793 = new BufferAllocator(16384);
   }

   @Override
   public boolean previewNeedsDummy() {
      return true;
   }

   @EventTarget
   public void onPacket(EventStep e) {
      if (e.getPacket() instanceof EntityStatusS2CPacket v2) {
         if (v2.method_11470() == 35) {

            if (mc.field_1687 != null) {

               Entity v7 = v2.method_11469(mc.field_1687);
               if (v7 != null) {

                  this.spawnGhost(v7);
               }
            }
         }
      }
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {
      this.f792.clear();
   }

   @Environment(EnvType.CLIENT)
   private static class al {
      final Matrix4f videoMatrix;
      final boolean isGhost;
      final Matrix4f glowMatrix;
      final boolean isMusorDrop;
      final double y;
      final double z;
      float headYawDeg;
      final double x;
      final float[] partPitchDeg = new float[zk.PART_COUNT];
      float bodyYaw;
      final Matrix4f[] partMatrices = new Matrix4f[zk.PART_COUNT];
      final long start;
      float alphaValue;

      al(Vec3d pos, float yaw, boolean isGhost, boolean isMusorDrop) {
         this.glowMatrix = new Matrix4f();
         this.videoMatrix = new Matrix4f();
         this.x = pos.field_1352;
         this.y = pos.field_1351;
         this.z = pos.field_1350;
         this.bodyYaw = yaw;
         this.isGhost = isGhost;
         this.isMusorDrop = isMusorDrop;
         this.start = System.currentTimeMillis();

         for (int v5 = 0; v5 < zk.PART_COUNT; v5++) {
            this.partMatrices[v5] = new Matrix4f();
         }
      }

      
   }
}
