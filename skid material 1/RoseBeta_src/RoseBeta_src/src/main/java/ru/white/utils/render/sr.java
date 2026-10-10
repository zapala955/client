package ru.white.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
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
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.ChinaHat;

@Environment(EnvType.CLIENT)
public class sr extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
   public static final RenderLayer CHINA_HAT_OUTLINE = RenderLayer.method_75940(
      "china_hat_outline",
      RenderSetup.method_75927(sr.CHINA_HAT_OUTLINE_PIPELINE).method_75937().method_75929(4096).method_75938()
   );
   private static final float PI2 = (float) (Math.PI * 2);
   public static final RenderLayer CHINA_HAT = RenderLayer.method_75940(
      "china_hat", RenderSetup.method_75927(sr.CHINA_HAT_PIPELINE).method_75937().method_75929(8192).method_75938()
   );
   public static final RenderPipeline CHINA_HAT_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation("pipeline/china_hat")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27381)
         .build()
   );
   private static final int OUTLINE_SEGMENTS = 64;
   public static final RenderPipeline CHINA_HAT_OUTLINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation("pipeline/china_hat_outline")
         .withVertexShader("core/position_color")
         .withFragmentShader("core/position_color")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(true)
         .withCull(false)
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29345)
         .build()
   );
   private static final int CIRCLE_SEGMENTS = 128;

   private boolean m1722(PlayerEntityRenderState state, MinecraftClient EventItemUse) {
      try {
         if (state.field_53528 == mc.field_1724.method_5628()) {
            return true;
         }
      } catch (Exception v8) {
      }

      try {
         if (state.field_53525 != null) {
            Text result = mc.field_1724.method_5477();

            if (result != null) {
               return state.field_53525.getString().equals(mc.field_1724.method_5477().getString());
            }
         }
      } catch (Exception v7) {
      }

      return false;
   }

   private void m1724(MatrixStack stack, VertexConsumerProvider provider, vi chinaHat) {
      VertexConsumer v4 = provider.method_73477(CHINA_HAT_OUTLINE);
      Matrix4f v5 = stack.method_23760().method_23761();
      float v6 = 0.55F;
      float v7 = 5.0F;
      int v8 = 255;

      for (int v9 = 0; v9 <= 64; v9++) {
         int v10 = this.m1725(v9 * 2, 128, chinaHat, v7);
         v10 = up.replAlpha(v10, v8);
         float v11 = (float)v9 * (float) (Math.PI * 2) / 64.0F;

         float v12 = -MathHelper.method_15374((double)v11) * v6;
         float v13 = MathHelper.method_15362((double)v11) * v6;
         v4.method_22918(v5, v12, 0.0F, v13).method_39415(v10);
      }
   }

   private int m1725(int index, int size, vi chinaHat, float animSpeed) {
      long v5 = System.currentTimeMillis();
      float v7 = (float)v5 / (1000.0F / animSpeed) % (float)size;

      float result = (float)index;

      result *= 360.0F / (float)size;

      return up.fade((int)(result + v7 * (360.0F / (float)size)));
   }

   private void m1723(MatrixStack stack, VertexConsumerProvider provider, vi chinaHat) {
      VertexConsumer v4 = provider.method_73477(CHINA_HAT);
      Matrix4f v5 = stack.method_23760().method_23761();
      float v6 = 0.55F;
      float v7 = 0.31F;
      int v8 = 200;
      float v9 = 5.0F;
      int v10 = this.m1725(0, 128, chinaHat, v9);
      v10 = up.replAlpha(v10, v8);
      v4.method_22918(v5, 0.0F, v7, 0.0F).method_39415(v10);

      for (int v11 = 0; v11 <= 128; v11++) {
         int v12 = this.m1725(v11, 128, chinaHat, v9);
         v12 = up.replAlpha(v12, v8);
         float v13 = (float)v11 * (float) (Math.PI * 2) / 128.0F;
         float v14 = -MathHelper.method_15374((double)v13) * v6;
         float v15 = MathHelper.method_15362((double)v13) * v6;
         v4.method_22918(v5, v14, 0.0F, v15).method_39415(v12);
      }

      for (int v19 = 128; v19 >= 0; v19--) {
         int v21 = this.m1725(v19, 128, chinaHat, v9);
         v21 = up.replAlpha(v21, v8);
         float v23 = (float)v19 * (float) (Math.PI * 2) / 128.0F;
         float v24 = -MathHelper.method_15374((double)v23) * v6;
         float v25 = MathHelper.method_15362((double)v23) * v6;
         v4.method_22918(v5, v24, 0.0F, v25).method_39415(v21);
         
      }

      v4.method_22918(v5, 0.0F, v7, 0.0F).method_39415(v10);
   }

   public void render(MatrixStack matrixStack, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
      MinecraftClient v7 = MinecraftClient.method_1551();
      vi v8 = vi.getInstance();
      if (v8 != null && v8.isEnabled()) {
         if (v7.field_1724 != null) {
            boolean v9 = this.m1722(state, v7);
            boolean v10 = this.m1721(state);
            if (v9 || v10) {
               if (v9) {
                  boolean result = v7.field_1690.method_31044().method_31034();

                  if (result) {
                     return;
                  }
               }

               matrixStack.method_22903();
               ((PlayerEntityModel)this.method_17165()).field_3398.method_22703(matrixStack);
               Quaternionf v15 = RotationAxis.field_40718.rotationDegrees(180.0F);

               matrixStack.method_22907(v15);
               matrixStack.method_22907(RotationAxis.field_40716.rotationDegrees(90.0F));

               matrixStack.method_46416(0.0F, 0.5F, 0.0F);
               Immediate v11 = v7.method_22940().method_23000();
               this.m1723(matrixStack, v11, v8);
               this.m1724(matrixStack, v11, v8);
               v11.method_22993();
               matrixStack.method_22909();
            }
         }
      }
   }

   private boolean m1721(PlayerEntityRenderState state) {
      try {
         MinecraftClient v2 = MinecraftClient.method_1551();
         if (v2.field_1687 == null) {
            return false;
         }

         Entity v3 = v2.field_1687.method_8469(state.field_53528);

         if (v3 instanceof PlayerEntity v4) {
            return Client.get().friendManager().isFriend(v4.method_5477().getString());
         }
      } catch (Exception v6) {
      }

      return false;
   }

   public sr(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
      super(context);
   }

   
}
