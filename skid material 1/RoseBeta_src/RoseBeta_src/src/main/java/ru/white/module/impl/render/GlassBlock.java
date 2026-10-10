package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.Collections;
import java.util.List;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Tf;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.bs;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Glass Block",
   category = Category.RENDER,
   desc = "Делает наведенный блок стеклянным"
)
@Environment(EnvType.CLIENT)
public class GlassBlock extends Module {
   private static nj instance;
   public SliderSetting blurRadius;
   public SliderSetting tintIntensity;
   private BlockPos f668;
   public SliderSetting edgeGlowIntensity;
   private Vec3d f669;
   public BooleanSetting shimmer;
   public BooleanSetting enableEdgeGlow;
   public BooleanSetting enableTint;
   private Vec3d f670;
   private final Animation f665;
   public BooleanSetting enableBlur = new BooleanSetting(this, "Блюр", true);
   private final Animation f666;
   private static final RenderLayer MASK_LAYER = RenderLayer.method_75940(
      "glass_block_mask",
      RenderSetup.method_75927(nj.MASK_PIPELINE).method_75929(4096).method_75938()
   );
   private static final RenderPipeline MASK_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "glass_block_mask"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   public SliderSetting blurIterations;
   public SliderSetting saturation;
   public SliderSetting shimmerWidth;
   public tf tintColor;
   private List<Box> f671;
   public SliderSetting shimmerPeriod;
   private BlockPos f667;
   public SliderSetting moveSpeed;
   private final BufferAllocator f664;

   @Generated
   public Animation getMoveAnimation() {

      return this.f665;
   }

   @Generated
   public BufferAllocator getAllocator() {

      return this.f664;
   }

   public GlassBlock() {
      this.blurRadius = new SliderSetting(this, "Сила размытия", 2.5F, 1.0F, 5.0F, 0.1F).setVisible(() -> {
         Boolean result = this.enableBlur.getValue();

         return result;
      });
      this.blurIterations = new SliderSetting(this, "Качество", 3.0F, 1.0F, 5.0F, 1.0F).setVisible(() -> {

         return this.enableBlur.getValue();
      });
      this.saturation = new SliderSetting(this, "Насыщенность", 0.0F, 0.0F, 2.0F, 0.1F).setVisible(() -> {

         return this.enableBlur.getValue();
      });
      this.enableTint = new BooleanSetting(this, "Оттенок", false);
      this.tintIntensity = new SliderSetting(this, "Сила оттенка", 0.2F, 0.0F, 0.5F, 0.01F).setVisible(() -> {
         boolean result = this.enableBlur.getValue();

         return result && this.enableTint.getValue();
      });
      this.tintColor = new tf(this, "Цвет оттенка", -16711681)
         .setVisible(() -> this.enableTint.getValue());
      this.enableEdgeGlow = new BooleanSetting(this, "Свечение краёв", true);
      this.edgeGlowIntensity = new SliderSetting(this, "Сила свечения", 0.2F, 0.0F, 1.0F, 0.01F)
         .setVisible(() -> {
            Boolean result = this.enableEdgeGlow.getValue();

            return result;
         });
      this.shimmer = new BooleanSetting(this, "Шиммер", true).setVisible(() -> {

         return this.enableEdgeGlow.getValue();
      });
      this.shimmerWidth = new SliderSetting(this, "Ширина шиммера", 0.04F, 0.01F, 0.15F, 0.01F).setVisible(() -> {
         if (this.enableEdgeGlow.getValue()) {

            if (this.shimmer.getValue()) {
               return true;
            }
         }

         return false;
      });
      this.shimmerPeriod = new SliderSetting(this, "Период шиммера", 5.0F, 1.0F, 15.0F, 0.5F).setVisible(() -> {

         return this.enableEdgeGlow.getValue() && this.shimmer.getValue();
      });
      this.moveSpeed = new SliderSetting(this, "Скорость перемещения", 0.18F, 0.05F, 0.6F, 0.01F);
      this.f664 = new BufferAllocator(16384);
      this.f665 = new Animation();
      this.f666 = new Animation();
      this.f669 = Vec3d.field_1353;
      this.f670 = Vec3d.field_1353;
      this.f671 = Collections.emptyList();
      instance = this;
   }

   @Generated
   public SliderSetting getShimmerPeriod() {
      return this.shimmerPeriod;
   }

   @Generated
   public Vec3d getFromOrigin() {
      return this.f669;
   }

   private void m742() {
      BlockPos v1 = this.m743();
      if (v1 != null) {
         BlockState v2 = mc.field_1687.method_8320(v1);
         VoxelShape v3 = v2.method_26218(mc.field_1687, v1);
         if (v3.method_1110()) {

            v3 = v2.method_26220(mc.field_1687, v1);
         }

         List v4 = v3.method_1110() ? Collections.singletonList(new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)) : v3.method_1090();
         this.f671 = v4;
      }

      if (v1 == null) {
         this.f667 = null;
         this.f666.run(0.0, (double)this.moveSpeed.getValue().floatValue(), ap.QUAD_OUT, true);
      } else {
         if (!v1.equals(this.f667)) {
            this.f669 = this.f668 == null ? Vec3d.method_24954(v1) : this.m744();
            this.f670 = Vec3d.method_24954(v1);
            this.f667 = v1;
            this.f668 = v1;
            this.f665.set(0.0);
            this.f665.run(1.0, (double)this.moveSpeed.getValue().floatValue(), ap.QUAD_OUT);
         }

         this.f666.run(1.0, (double)(this.moveSpeed.getValue() * 0.75F), ap.QUAD_OUT, true);
      }
   }

   @Generated
   public SliderSetting getEdgeGlowIntensity() {

      return this.edgeGlowIntensity;
   }

   private Vec3d m744() {
      float v1 = this.f665.get();
      Vec3d result = new Vec3d;
      double temp2 = this.f669.field_1352 + (this.f670.field_1352 - this.f669.field_1352) * (double)v1;
      double temp3 = this.f669.field_1351 + (this.f670.field_1351 - this.f669.field_1351) * (double)v1;

      double v5 = (this.f670.field_1350 - this.f669.field_1350) * (double)v1;

      result./* $VF: Unable to resugar constructor */<init>(temp2, temp3, this.f669.field_1350 + v5);
      return result;
   }

   private void m746() {
      bs v1 = bs.getInstance();
      v1.setBlurEnabled(this.enableBlur.getValue());
      v1.setBlurRadius(this.blurRadius.getValue());
      v1.setBlurIterations(this.blurIterations.getValue().intValue());
      v1.setSaturation(this.saturation.getValue());
      v1.setReflect(true);
      if (this.enableBlur.getValue() && this.enableTint.getValue()) {
         v1.setTintColor(this.tintColor.getValue());
         v1.setTintIntensity(this.tintIntensity.getValue());
      } else {
         v1.setTintColor(0);
         v1.setTintIntensity(0.0F);
      }

      v1.setOutlineEnabled(this.enableEdgeGlow.getValue());
      if (this.enableEdgeGlow.getValue()) {
         float v2 = this.edgeGlowIntensity.getValue() * 20.0F;
         v1.setOutlineGlowStrength(v2);
         int v3 = this.enableTint.getValue() ? this.tintColor.getValue() : -1;
         v1.setOutlineColor(v3);
         v1.setShimmerEnabled(this.shimmer.getValue());
         v1.setShimmerWidth(this.shimmerWidth.getValue());
         v1.setShimmerPeriodSec(this.shimmerPeriod.getValue());
      }
   }

   @Generated
   public SliderSetting getBlurRadius() {

      return this.blurRadius;
   }

   @Generated
   public tf getTintColor() {
      return this.tintColor;
   }

   private static void m747(VertexConsumer buffer, Matrix4f matrix, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color) {
      float v15 = (float)minX;
      float v16 = (float)maxX;
      float v17 = (float)minY;
      float v18 = (float)maxY;
      float v19 = (float)minZ;
      float v20 = (float)maxZ;
      buffer.method_22918(matrix, v15, v17, v19).method_39415(color);
      buffer.method_22918(matrix, v16, v17, v19).method_39415(color);
      buffer.method_22918(matrix, v16, v17, v20).method_39415(color);
      buffer.method_22918(matrix, v15, v17, v20).method_39415(color);

      buffer.method_22918(matrix, v15, v18, v19).method_39415(color);
      buffer.method_22918(matrix, v15, v18, v20).method_39415(color);
      buffer.method_22918(matrix, v16, v18, v20).method_39415(color);
      VertexConsumer result = buffer.method_22918(matrix, v16, v18, v19);

      result.method_39415(color);
      buffer.method_22918(matrix, v15, v17, v20).method_39415(color);
      buffer.method_22918(matrix, v16, v17, v20).method_39415(color);
      buffer.method_22918(matrix, v16, v18, v20).method_39415(color);

      buffer.method_22918(matrix, v15, v18, v20).method_39415(color);
      buffer.method_22918(matrix, v16, v17, v19).method_39415(color);
      buffer.method_22918(matrix, v15, v17, v19).method_39415(color);
      buffer.method_22918(matrix, v15, v18, v19).method_39415(color);
      buffer.method_22918(matrix, v16, v18, v19).method_39415(color);
      buffer.method_22918(matrix, v15, v17, v19).method_39415(color);
      buffer.method_22918(matrix, v15, v17, v20).method_39415(color);
      buffer.method_22918(matrix, v15, v18, v20).method_39415(color);
      buffer.method_22918(matrix, v15, v18, v19).method_39415(color);
      buffer.method_22918(matrix, v16, v17, v20).method_39415(color);
      buffer.method_22918(matrix, v16, v17, v19).method_39415(color);
      buffer.method_22918(matrix, v16, v18, v19).method_39415(color);
      buffer.method_22918(matrix, v16, v18, v20).method_39415(color);
   }

   @Generated
   public SliderSetting getShimmerWidth() {

      return this.shimmerWidth;
   }

   @Generated
   public SliderSetting getMoveSpeed() {

      return this.moveSpeed;
   }

   @Generated
   public Vec3d getToOrigin() {
      return this.f670;
   }

   @Generated
   public List<Box> getTargetBoxes() {

      return this.f671;
   }

   @Generated
   public BooleanSetting getEnableEdgeGlow() {

      return this.enableEdgeGlow;
   }

   @Generated
   public SliderSetting getSaturation() {

      return this.saturation;
   }

   @Generated
   public BooleanSetting getShimmer() {

      return this.shimmer;
   }

   @Generated
   public Animation getAlphaAnimation() {

      return this.f666;
   }

   private void m745(EventTrace event, Vec3d origin, float alpha) {
      Immediate v4 = VertexConsumerProvider.method_22991(this.f664);
      VertexConsumer v5 = v4.method_73477(MASK_LAYER);
      Matrix4f v6 = event.getMatrixStack().method_23760().method_23761();
      Vec3d v7 = mc.field_1773.method_19418().method_71156();
      int v8 = (int)(255.0F * alpha) << 24 | 16777215;

      for (Box v10 : this.f671) {

         double v11 = origin.field_1352 + v10.field_1323 - v7.field_1352;
         double v13 = origin.field_1351 + v10.field_1322 - v7.field_1351;
         double v15 = origin.field_1350 + v10.field_1321 - v7.field_1350;
         double v17 = origin.field_1352 + v10.field_1320 - v7.field_1352;
         double v19 = origin.field_1351 + v10.field_1325 - v7.field_1351;
         double v21 = origin.field_1350 + v10.field_1324 - v7.field_1350;
         m747(v5, v6, v11, v13, v15, v17, v19, v21, v8);
      }

      v4.method_22993();
   }

   @Generated
   public SliderSetting getBlurIterations() {
      return this.blurIterations;
   }

   @EventTarget
   public void onRender3D(EventTrace event) {
      if (this.isEnabled() && mc.field_1687 != null && mc.field_1724 != null) {
         this.m742();
         this.f665.update();
         this.f666.update();
         if (!(this.f666.get() <= 0.01F)) {

            if (!this.f671.isEmpty()) {
               bs v2 = bs.getInstance();
               this.m746();
               if (!v2.captureSceneBeforeBlock()) {
                  return;
               }

               this.m745(event, this.m744(), this.f666.get());
               v2.captureSceneAfterBlock();
               v2.renderGlassEffect();
               return;
            }
         }
      }
   }

   @Generated
   public BooleanSetting getEnableBlur() {
      return this.enableBlur;
   }

   public static nj getInstance() {
      int temp2 = v0 + 1;

      return instance;
   }

   private BlockPos m743() {
      HitResult v1 = mc.field_1765;
      if (v1 instanceof BlockHitResult v2 && v1.method_17783() == Type.field_1332) {
         BlockPos v3 = v2.method_17777();
         if (mc.field_1687 != null) {

            if (!mc.field_1687.method_8320(v3).method_26215()) {
               return v3;
            }
         }

         return null;
      }

      return null;
   }

   @Generated
   public BlockPos getLastTargetPos() {

      return this.f668;
   }

   @Generated
   public BooleanSetting getEnableTint() {

      return this.enableTint;
   }

   @Override
   protected void onDisable() {
      bs.getInstance().setEnabled(false);

      this.f667 = null;
      this.f668 = null;
      this.f671 = Collections.emptyList();
   }

   @Override
   protected void onEnable() {
      this.f667 = null;
      this.f668 = null;
      this.f669 = Vec3d.field_1353;
      this.f670 = Vec3d.field_1353;

      this.f665.set(1.0);
      this.f666.set(0.0);
      bs v1 = bs.getInstance();
      v1.invalidate();
      v1.setEnabled(true);
      this.m746();
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      if (this.isEnabled()) {
         this.f667 = null;
         this.f668 = null;
         this.f671 = Collections.emptyList();

         bs v2 = bs.getInstance();

         v2.invalidate();
         v2.setEnabled(true);
         this.m746();
      }
   }

   @Generated
   public SliderSetting getTintIntensity() {

      return this.tintIntensity;
   }

   @Generated
   public BlockPos getTargetPos() {
      return this.f667;
   }

   
}
