package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import ru.white.event.EventTarget;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.utils.misc.Zq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.combat.HitBoxes;
import ru.white.utils.render.vs;
import ru.white.module.impl.misc.ReportHelper;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Entity Esp",
   category = Category.RENDER,
   desc = "Боксы сущностей через стены"
)
@Environment(EnvType.CLIENT)
public class EntityEsp extends Module {
   public BooleanSetting items;
   private static final RenderLayer BOX_FILL_LAYER = RenderLayer.method_75940(
      "night_esp_box_fill",
      RenderSetup.method_75927(cj.BOX_FILL_PIPELINE).method_75929(4096).method_75938()
   );
   private static final RenderPipeline BOX_LINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("minecraft", "rendertype_lines"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private final List<Entity> f651;
   private static final int QUAD_BUFFER_SIZE_BYTES = 4096;
   public ModeSetting box;
   private final BufferAllocator f652;
   public BooleanSetting mobs;
   public BooleanSetting player = new BooleanSetting(this, "Отображения игроков", true);
   public BooleanSetting ignoreNaked = new BooleanSetting(this, "Игнорировать голых", false).setVisible(() -> {

      return this.player.getValue();
   });
   private static final RenderLayer BOX_LINE_LAYER = RenderLayer.method_75940(
      "night_esp_box_line",
      RenderSetup.method_75927(BOX_LINE_PIPELINE).method_75929(4096).method_75938()
   );
   private static final RenderPipeline BOX_FILL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("minecraft", "rendertype_lequal_depth_test"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );

   public static boolean hasArmor(PlayerEntity p) {
      if (p.method_6118(EquipmentSlot.field_6169).method_7960() && p.method_6118(EquipmentSlot.field_6174).method_7960()) {

         if (p.method_6118(EquipmentSlot.field_6172).method_7960() && p.method_6118(EquipmentSlot.field_6166).method_7960()) {
            return false;
         }
      }

      return true;
   }

   public EntityEsp() {
      this.mobs = new BooleanSetting(this, "Отображения мобов", true);
      this.items = new BooleanSetting(this, "Отображения предметов", true);
      this.box = new ModeSetting(this, "Вид бокса", "2д", "3д");
      this.f651 = new ArrayList<>();
      this.f652 = new BufferAllocator(262144);
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      this.f651.clear();
      if (mc.field_1687 != null) {
         for (Entity v3 : mc.field_1687.method_18112()) {
            if (v3 instanceof PlayerEntity) {
               PlayerEntity v4 = (PlayerEntity)v3;
               if (this.player.getValue()) {
                  if (v4.method_5797() != null) {
                     Text result = v4.method_5797();

                     if (result.getString().startsWith("Ghost_")) {
                        continue;
                     }
                  }

                  if (!this.ignoreNaked.getValue() || hasArmor(v4)) {
                     this.f651.add(v3);
                  }
               }
            } else if (v3 instanceof LivingEntity) {
               if (this.mobs.getValue()) {
                  this.f651.add(v3);
               }
            } else if (v3 instanceof ItemEntity && this.items.getValue()) {

               this.f651.add(v3);
            }
         }
      }
   }

   private void m709(boolean friend, Vector4d vec, Entity player) {
      int v4 = friend ? up.getColor(100, 200, 100) : up.fade(1);
      if (nm.isReported(player)) {
         v4 = nm.getReportColor(v4);
      }

      float v5 = (float)vec.x;
      float v6 = (float)vec.y;
      float v7 = (float)vec.z;
      float v8 = (float)vec.w;
      float v9 = (v7 - v5) / 3.0F;
      vs.ys.rect(v5 - 0.5F, v6 - 0.5F, v9, 0.5F, v4);

      vs.ys.rect(v5 - 0.5F, v6, 0.5F, v9 + 0.5F, v4);
      vs.ys.rect(v5 - 0.5F, v8 - v9 - 0.5F, 0.5F, v9, v4);
      vs.ys.rect(v5 - 0.5F, v8 - 0.5F, v9, 0.5F, v4);
      vs.ys.rect(v7 - v9 + 1.0F, v6 - 0.5F, v9, 0.5F, v4);
      vs.ys.rect(v7 + 0.5F, v6, 0.5F, v9 + 0.5F, v4);
      vs.ys.rect(v7 + 0.5F, v8 - v9 - 0.5F, 0.5F, v9, v4);
      vs.ys.rect(v7 - v9 + 1.0F, v8 - 0.5F, v9, 0.5F, v4);
   }

   public static void drawDashedLineSegment(
      VertexConsumer buffer,
      Matrix4f matrix,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      int color1,
      int color2,
      double dashLength,
      double gapLength
   ) {
      double v20 = x2 - x1;
      double v22 = y2 - y1;
      double v24 = z2 - z1;
      double v26 = Math.sqrt(v20 * v20 + v22 * v22 + v24 * v24);
      if (!(v26 < 0.001)) {
         double v28 = v20 / v26;
         double v30 = v22 / v26;
         double v32 = v24 / v26;
         int v34 = color1 >> 16 & 255;
         int v35 = color1 >> 8 & 255;
         int v36 = color1 & 255;
         int v37 = color1 >> 24 & 255;
         int v38 = color2 >> 16 & 255;
         int v39 = color2 >> 8 & 255;
         int v40 = color2 & 255;
         int v41 = color2 >> 24 & 255;
         double v42 = dashLength + gapLength;
         double v44 = 0.0;

         while (v44 < v26) {
            double v48 = Math.min(v44 + dashLength, v26);
            if (v48 > v44) {
               double v50 = x1 + v28 * v44;

               double v52 = y1 + v30 * v44;
               double v54 = z1 + v32 * v44;
               double v56 = x1 + v28 * v48;
               double v58 = y1 + v30 * v48;
               double v60 = z1 + v32 * v48;
               double v62 = v44 / v26;
               int v64 = (int)((double)v34 + (double)(v38 - v34) * v62);
               int v65 = (int)((double)v35 + (double)(v39 - v35) * v62);
               int v66 = (int)((double)v36 + (double)(v40 - v36) * v62);
               int v67 = (int)((double)v37 + (double)(v41 - v37) * v62);
               float temp2 = (float)v50;
               float temp3 = (float)v52;

               buffer.method_22918(matrix, temp2, temp3, (float)v54).method_1336(v64, v65, v66, v67);
               v62 = v48 / v26;
               v64 = (int)((double)v34 + (double)(v38 - v34) * v62);
               v65 = (int)((double)v35 + (double)(v39 - v35) * v62);
               v66 = (int)((double)v36 + (double)(v40 - v36) * v62);
               v67 = (int)((double)v37 + (double)(v41 - v37) * v62);
               buffer.method_22918(matrix, (float)v56, (float)v58, (float)v60).method_1336(v64, v65, v66, v67);
            }

            double v76 = v44 + v42;

            v44 = v76;
         }
      }
   }

   private void m710(MatrixStack matrices, Immediate immediate, Entity target, float partialTicks) {
      if (target != null) {
         Vec3d v5 = mc.field_1773.method_19418().method_71156();

         double v6 = target.field_6038 + (target.method_23317() - target.field_6038) * (double)partialTicks;
         double v37 = target.field_5971 + (target.method_23318() - target.field_5971) * (double)partialTicks;

         double v8 = v37;
         double v10 = target.field_5989 + (target.method_23321() - target.field_5989) * (double)partialTicks;
         Box v12 = target.method_5829();
         double v13 = 0.0;
         if (lg.get().isEnabled() && target instanceof PlayerEntity) {
            v13 = (double)lg.get().size.getValue().floatValue();
         }

         double v15;
         double v17;
         double v19;
         double v21;
         double v23;
         double v25;
         label31: {
            v15 = v12.field_1323 - target.method_23317() + v6 - v13 - v5.field_1352;
            v17 = v12.field_1322 - target.method_23318() + v8 - v13 - v5.field_1351;
            v19 = v12.field_1321 - target.method_23321() + v10 - v13 - v5.field_1350;
            v21 = v12.field_1320 - target.method_23317() + v6 + v13 - v5.field_1352;
            v23 = v12.field_1325 - target.method_23318() + v8 + v13 - v5.field_1351;
            v25 = v12.field_1324 - target.method_23321() + v10 + v13 - v5.field_1350;
            if (target instanceof AbstractClientPlayerEntity v28 && Client.get().friendManager().isFriend(v28.method_5820())) {
               v38 = up.GREEN;
               break label31;
            }

            v38 = up.getClientColor1(1);
         }

         int v27 = v38;
         if (nm.isReported(target)) {
            v27 = nm.getReportColor(v27);
         }

         int[] v30 = new int[]{
            up.gradient(v27, v27, 0, 7),
            up.gradient(v27, v27, 90, 7),
            up.gradient(v27, v27, 180, 7),
            up.gradient(v27, v27, 270, 7)
         };
         Matrix4f v31 = matrices.method_23760().method_23761();
         VertexConsumer v32 = immediate.method_73477(BOX_FILL_LAYER);
         drawBoxFill(v32, v31, v15, v17, v19, v21, v23, v25, v30, 10);
         VertexConsumer v33 = immediate.method_73477(BOX_LINE_LAYER);
         drawBoxOutline(v33, v31, v15, v17, v19, v21, v23, v25, v30, 255, 0.12, 0.08);
      }
   }

   public static void drawBoxOutline(
      VertexConsumer buffer,
      Matrix4f matrix,
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      int[] colors,
      int outlineAlpha,
      double dashLength,
      double gapLength
   ) {
      int[] v20 = new int[4];

      for (int v21 = 0; v21 < 4; v21++) {
         v20[v21] = up.replAlpha(colors[v21], outlineAlpha);
      }

      int var10008 = v20[0];
      int var10009 = v20[1];

      drawDashedLineSegment(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, var10008, var10009, dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, v20[1], v20[2], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ, v20[2], v20[3], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, minY, maxZ, minX, minY, minZ, v20[3], v20[0], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, v20[0], v20[1], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, v20[1], v20[2], dashLength, gapLength);
      var10008 = v20[2];

      drawDashedLineSegment(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ, var10008, v20[3], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ, v20[3], v20[0], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, v20[0], v20[0], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, v20[1], v20[1], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, v20[2], v20[2], dashLength, gapLength);
      drawDashedLineSegment(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, v20[3], v20[3], dashLength, gapLength);
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {

      this.f651.clear();
      this.f652.method_60809();
   }

   @EventTarget
   public void onTick(EventTrace e) {
      float v2 = e.getTickDelta();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (!this.f651.isEmpty()) {
            if (!this.box.is("3д")) {
               return;
            }

            Immediate v3 = VertexConsumerProvider.method_22991(this.f652);

            for (Entity v10 : this.f651) {

               Entity v5 = v10;
               if ((v5 != mc.field_1724 || !mc.field_1690.method_31044().method_31034()) && zq.isInFrontOfCamera(v5.method_5829().method_1005())) {
                  this.m710(e.getMatrixStack(), v3, v5, v2);
               }
            }

            v3.method_22993();
            return;
         }

         
      }
   }

   @EventTarget
   public void onTick(EventReceivePacket e) {
      float v2 = e.getPartialTicks();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (this.box.is("2д")) {
            for (Entity v4 : this.f651) {
               Vector4d v5 = zq.getVector4D(v4, v2);
               if (!zq.cantSee(v5)) {
                  float v6 = (float)mc.field_1773.method_19418().method_71156().method_1022(v4.method_5829().method_1005());

                  float v13;
                  int v11 = (v13 = v6 - 1.0F) == 0.0F ? 0 : (v13 < 0.0F ? -1 : 1);

                  if (v11 >= 0) {
                     label35: {
                        if (v4 instanceof PlayerEntity v8 && Client.get().friendManager().isFriend(v8.method_5477().getString())) {
                           v12 = true;
                           break label35;
                        }

                        v12 = false;
                     }

                     boolean v7 = v12;
                     this.m709(v7, v5, v4);
                  }
               }
            }
         }
      }
   }

   public static cj get() {
      cj result = uq.get(cj.class);

      return result;
   }

   public static void drawBoxFill(
      VertexConsumer buffer, Matrix4f matrix, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int[] colors, int fillAlpha
   ) {
      int v16 = up.replAlpha(colors[0], fillAlpha);
      int v17 = up.replAlpha(colors[1], fillAlpha);
      int v18 = up.replAlpha(colors[2], fillAlpha);
      int v19 = up.replAlpha(colors[3], fillAlpha);
      float v20 = (float)minX;
      float v21 = (float)maxX;
      float v22 = (float)minY;
      float v23 = (float)maxY;
      float v24 = (float)minZ;
      float v25 = (float)maxZ;
      buffer.method_22918(matrix, v20, v22, v24).method_39415(v16);
      buffer.method_22918(matrix, v21, v22, v24).method_39415(v17);
      buffer.method_22918(matrix, v21, v22, v25).method_39415(v18);
      buffer.method_22918(matrix, v20, v22, v25).method_39415(v19);
      buffer.method_22918(matrix, v20, v23, v24).method_39415(v16);
      buffer.method_22918(matrix, v20, v23, v25).method_39415(v19);
      buffer.method_22918(matrix, v21, v23, v25).method_39415(v18);
      buffer.method_22918(matrix, v21, v23, v24).method_39415(v17);
      buffer.method_22918(matrix, v20, v22, v25).method_39415(v19);
      buffer.method_22918(matrix, v21, v22, v25).method_39415(v18);
      buffer.method_22918(matrix, v21, v23, v25).method_39415(v18);
      buffer.method_22918(matrix, v20, v23, v25).method_39415(v19);
      buffer.method_22918(matrix, v21, v22, v24).method_39415(v17);
      buffer.method_22918(matrix, v20, v22, v24).method_39415(v16);
      buffer.method_22918(matrix, v20, v23, v24).method_39415(v16);
      buffer.method_22918(matrix, v21, v23, v24).method_39415(v17);
      buffer.method_22918(matrix, v20, v22, v24).method_39415(v16);
      buffer.method_22918(matrix, v20, v22, v25).method_39415(v19);
      buffer.method_22918(matrix, v20, v23, v25).method_39415(v19);
      buffer.method_22918(matrix, v20, v23, v24).method_39415(v16);
      buffer.method_22918(matrix, v21, v22, v25).method_39415(v18);
      buffer.method_22918(matrix, v21, v22, v24).method_39415(v17);

      buffer.method_22918(matrix, v21, v23, v24).method_39415(v17);
      buffer.method_22918(matrix, v21, v23, v25).method_39415(v18);
   }

   
}
