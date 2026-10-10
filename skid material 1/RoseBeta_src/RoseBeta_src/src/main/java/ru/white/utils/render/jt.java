package ru.white.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderSetup.Builder;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public class jt implements IMinecraft {
   public static final Function<Identifier, RenderLayer> BLOOM_LAYER = Util.method_34866(texture -> {
      Builder result = RenderSetup.method_75927(jt.BLOOM_PIPELINE);

      result = result.method_75934("Sampler0", texture);

      RenderSetup v1 = result.method_75937().method_75929(2048).method_75938();
      return RenderLayer.method_75940("spawn_bloom", v1);
   });
   public static final RenderPipeline BLOOM_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation("pipeline/spawn_bloom_tex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
         .build()
   );
   private static final Identifier BLOOM_TEX = Identifier.method_60655(
      "client", "textures/visuals/particles_3.png"
   );

   private static void m1902(
      Immediate immediate, MatrixStack stack, Vec3d renderPos, LivingEntity target, float ringWidth, float ringScale, float moving, float size, int color
   ) {
      VertexConsumer v9 = immediate.method_73477(BLOOM_LAYER.apply(BLOOM_TEX));
      Quaternionf v10 = mc.field_1773.method_19418().method_23767();
      Vec3d v11 = mc.field_1773.method_19418().method_71156();
      float v12 = size / 2.0F;

      for (byte v13 = 0; v13 < 360; v13 += 20) {
         float v14 = (float)Math.toRadians((double)((float)v13 + moving * 0.3F));
         float v15 = (float)Math.sin((double)v14) * ringWidth * ringScale;
         float v16 = (float)Math.cos((double)v14) * ringWidth * ringScale;
         float v17 = 0.1F + target.method_17682() * Math.abs((float)Math.sin(Math.toRadians((double)v13)));
         stack.method_22903();
         double v22 = renderPos.method_10216() + (double)v15;

         v22 -= v11.field_1352;
         double v24 = renderPos.method_10214() + (double)v17 - v11.field_1351;

         stack.method_22904(v22, v24, renderPos.method_10215() + (double)v16 - v11.field_1350);
         stack.method_22907(v10);
         Matrix4f v18 = stack.method_23760().method_23761();
         v9.method_22918(v18, -v12, -v12, 0.0F).method_22913(0.0F, 0.0F).method_39415(color);
         v9.method_22918(v18, -v12, v12, 0.0F).method_22913(0.0F, 1.0F).method_39415(color);
         v9.method_22918(v18, v12, v12, 0.0F).method_22913(1.0F, 1.0F).method_39415(color);
         v9.method_22918(v18, v12, -v12, 0.0F).method_22913(1.0F, 0.0F).method_39415(color);
         stack.method_22909();
      }
   }

   public static void render(MatrixStack stack, Immediate immediate, LivingEntity target, float anim, int color, float tickDelta) {
      if (target != null && !(anim <= 0.001F)) {
         float v6 = (float)(System.currentTimeMillis() % (-7243550648850050124L + 7243550648850410124L)) / 2.5F;
         Vec3d v7 = target.method_30950(tickDelta);
         float v8 = target.method_17681() * 1.5F;
         float v9 = 1.25F - 0.5F * anim;
         float v10 = anim * 0.22F * (2.0F - anim);
         int result = up.replAlpha(color, (int)(v10 * 255.0F));

         int v11 = result;
         m1902(immediate, stack, v7, target, v8, v9 * 1.25F, v6, 1.4F * anim, up.replAlpha(color, (int)(v10 * 0.45F * 255.0F)));
         m1902(immediate, stack, v7, target, v8, v9, v6, 0.55F * anim, v11);
      }
   }

   
}
