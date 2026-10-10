package ru.white.module.impl.misc;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Oq;
import ru.white.utils.ChatUtil;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "DanjHelper",
   desc = "Отслеживание бочек в данже: таймеры открытия и градиентный ESP",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class DanjHelper extends Module {
   private final Set<BlockPos> f953;
   private static final RenderPipeline BOX_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "danj_helper_esp"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private final Map<BlockPos, em.fm> f951 = new ConcurrentHashMap<>();
   private static final int COLOR_YELLOW = up.getColor(255, 220, 50, 180);
   private final Set<BlockPos> f952 = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private static final int COLOR_GREEN = up.getColor(60, 255, 60, 180);
   private static final int COLOR_UNKNOWN = up.getColor(180, 180, 180, 120);
   private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
   private static final int COLOR_RED = up.getColor(255, 60, 60, 180);
   private static final RenderLayer BOX_LAYER = RenderLayer.method_75940(
      "danj_helper_esp", RenderSetup.method_75927(BOX_PIPELINE).method_75929(4096).method_75938()
   );
   private static final Pattern SECONDS_PATTERN = Pattern.compile("(\\d+)\\s*(с|s|сек|sec)");
   private final BufferAllocator f954;

   @EventTarget(
      priority = -500
   )
   public void onRender3D(EventTrace event) {
      if (mc.field_1724 != null) {

         if (mc.field_1687 != null) {
            if (!this.m1224()) {
               return;
            }

            Matrix4f v2 = event.getMatrixStack().method_23760().method_23761();
            Vec3d v3 = mc.field_1773.method_19418().method_71156();
            Immediate v4 = VertexConsumerProvider.method_22991(this.f954);
            VertexConsumer v5 = v4.method_73477(BOX_LAYER);
            ChunkPos v6 = mc.field_1724.method_31476();
            int v7 = (Integer)mc.field_1690.method_42503().method_41753();

            for (int v8 = v6.field_9181 - v7; v8 <= v6.field_9181 + v7; v8++) {
               for (int v9 = v6.field_9180 - v7; v9 <= v6.field_9180 + v7; v9++) {
                  WorldChunk v10 = mc.field_1687.method_8497(v8, v9);
                  if (v10 != null) {
                     for (BlockEntity v12 : v10.method_12214().values()) {
                        if (v12 instanceof BarrelBlockEntity) {
                           BlockPos v13 = v12.method_11016();

                           if (this.m1225(v13)) {
                              if (!this.f951.containsKey(v13)) {
                                 this.f951.put(v13, new em.fm(v13));
                              }

                              em.fm v14 = this.f951.get(v13);
                              int v15 = this.m1229(v14);
                              float v16 = (float)((double)v13.method_10263() - v3.field_1352);
                              float v17 = (float)((double)v13.method_10264() - v3.field_1351);
                              float v18 = (float)((double)v13.method_10260() - v3.field_1350);
                              int v19 = up.replAlpha(v15, 0.8F);
                              int v20 = up.replAlpha(v15, 0.0F);
                              this.m1230(v5, v2, v16, v17, v18, v16 + 1.0F, v17 + 1.0F, v18 + 1.0F, v19, v20);
                           }
                        }
                     }
                  }
               }
            }

            v4.method_22993();
            return;
         }
      }
   }

   @Override
   public void onDisable() {
      this.m1231();
      super.onDisable();
   }

   public DanjHelper() {
      this.f953 = Collections.newSetFromMap(new ConcurrentHashMap<>());
      this.f954 = new BufferAllocator(262144);
   }

   private void m1226() {
      Box v1 = mc.field_1724.method_5829().method_1014(32.0);

      for (Entity v4 : mc.field_1687.method_8335(mc.field_1724, v1)) {
         if (v4 instanceof ArmorStandEntity) {
            ArmorStandEntity v5 = (ArmorStandEntity)v4;
            if (v5.method_16914() && v5.method_5797() != null) {
               String v6 = v5.method_5797().getString();
               String v7 = up.removeFormatting(v6);
               if (v7 != null) {
                  long v8 = this.m1227(v7);
                  if (v8 >= 0L) {
                     BlockPos v10 = v5.method_24515().method_10074();
                     this.f951.computeIfAbsent(v10, em.fm::new).updateFromHologram(v8);
                  }
               }
            }
         }
      }
   }

   private void m1228() {
      for (Entry v2 : this.f951.entrySet()) {
         BlockPos v3 = (BlockPos)v2.getKey();
         em.fm v4 = (em.fm)v2.getValue();
         if (v4.hasTimer()) {
            float v5 = v4.getRemainingSeconds();
            if (v5 <= 30.0F && v5 > 0.0F) {
               if (!this.f952.contains(v3)) {
                  ClientPlayNetworkHandler result = mc.field_1724.field_3944;
                  int v7 = v3.method_10260();
                  int v8 = v3.method_10263();
                  StringBuilder temp1 = new StringBuilder;

                  temp1./* $VF: Unable to resugar constructor */<init>();
                  result.method_45729(temp1.append(".gps set ").append(v8).append(" ").append(v7).toString());
                  this.f952.add(v3);
               }

               if (!this.f953.contains(v3)) {
                  int v17 = (int)v5;
                  int v19 = v3.method_10263();
                  int v9 = v3.method_10260();
                  int v10 = v3.method_10264();
                  int v11 = v19;
                  int v12 = v17;
                  StringBuilder v18 = new StringBuilder()
                     .append("Бочка откроется через ")
                     .append(v12)
                     .append(" сек! [");

                  String v6 = v18.append(v11).append(", ").append(v10).append(", ").append(v9).append("]").toString();
                  zp.addChatMessage("§b[DanjHelper] " + v6);
                  oq.send(v6, oq.qq.WARNING);
                  this.f953.add(v3);
               }
            }
         }
      }
   }

   private boolean m1225(BlockPos pos) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         double v2 = (double)pos.method_10263();

         double v4 = (double)pos.method_10264();
         double v6 = (double)pos.method_10260();
         boolean v13;
         if (Math.abs(Math.abs(v2) - 2000.0) <= 150.0 && Math.abs(Math.abs(v6) - 2000.0) <= 150.0) {
            v13 = true;
            
         } else {
            v13 = false;
         }

         boolean v8 = v13;
         boolean v9 = Math.abs(v4 - mc.field_1724.method_23318()) <= 30.0;
         return v8 && v9;
      }
   }

   @EventTarget
   public void onTick(EventRenderModel event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!this.m1224()) {
            
         } else {
            this.m1226();
            this.m1228();
         }
      }
   }

   private int m1229(em.fm data) {
      if (!data.hasTimer()) {
         return COLOR_UNKNOWN;
      } else {
         float result = data.getRemainingSeconds();

         float v2 = result;
         if (v2 <= 0.0F) {
            float v9 = (float)(Math.sin((double)System.currentTimeMillis() / 200.0) * 0.3 + 0.7);
            int v4 = (int)(255.0F * v9);
            int temp2 = (int)(128.0F * v9);

            int temp3 = 220;

            return up.getColor(0, v4, temp2, temp3);
         } else if (v2 > 120.0F) {
            return COLOR_RED;
         } else if (v2 > 20.0F) {
            float v8 = 1.0F - (v2 - 20.0F) / 100.0F;
            return up.interpolateColor(COLOR_RED, COLOR_YELLOW, v8);
         } else {
            float v3 = 1.0F - v2 / 20.0F;
            return up.interpolateColor(COLOR_YELLOW, COLOR_GREEN, v3);
         }
      }
   }

   private void m1230(VertexConsumer b, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, int cB, int cT) {
      b.method_22918(m, x1, y1, z1).method_39415(cB);
      b.method_22918(m, x2, y1, z1).method_39415(cB);
      b.method_22918(m, x2, y1, z2).method_39415(cB);

      b.method_22918(m, x1, y1, z2).method_39415(cB);
      b.method_22918(m, x1, y2, z1).method_39415(cT);
      b.method_22918(m, x1, y2, z2).method_39415(cT);
      b.method_22918(m, x2, y2, z2).method_39415(cT);
      b.method_22918(m, x2, y2, z1).method_39415(cT);
      b.method_22918(m, x1, y1, z1).method_39415(cB);
      b.method_22918(m, x1, y2, z1).method_39415(cT);
      b.method_22918(m, x2, y2, z1).method_39415(cT);
      b.method_22918(m, x2, y1, z1).method_39415(cB);
      b.method_22918(m, x1, y1, z2).method_39415(cB);
      b.method_22918(m, x2, y1, z2).method_39415(cB);
      b.method_22918(m, x2, y2, z2).method_39415(cT);
      b.method_22918(m, x1, y2, z2).method_39415(cT);
      b.method_22918(m, x1, y1, z1).method_39415(cB);
      b.method_22918(m, x1, y1, z2).method_39415(cB);
      b.method_22918(m, x1, y2, z2).method_39415(cT);
      b.method_22918(m, x1, y2, z1).method_39415(cT);
      b.method_22918(m, x2, y1, z1).method_39415(cB);
      b.method_22918(m, x2, y2, z1).method_39415(cT);
      b.method_22918(m, x2, y2, z2).method_39415(cT);
      b.method_22918(m, x2, y1, z2).method_39415(cB);
   }

   private boolean m1224() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         double v1 = mc.field_1724.method_23317();
         double v3 = mc.field_1724.method_23321();
         boolean result;
         if (Math.abs(Math.abs(v1) - 2000.0) <= 150.0 && Math.abs(Math.abs(v3) - 2000.0) <= 150.0) {
            result = true;
            
         } else {
            result = false;
         }

         return result;
      }
   }

   private long m1227(String text) {
      Matcher v2 = TIME_PATTERN.matcher(text);
      if (v2.find()) {

         if (v2.group(3) != null) {
            int v11 = Integer.parseInt(v2.group(1));

            int v13 = Integer.parseInt(v2.group(2));
            int v14 = Integer.parseInt(v2.group(3));
            return (long)v11 * (-5624114452571252731L + 5624114452571256331L) + (long)v13 * (5478348055371226065L + -5478348055371226005L) + (long)v14;
         } else {
            int v10 = Integer.parseInt(v2.group(1));
            int v12 = Integer.parseInt(v2.group(2));
            return (long)v10 * (7166640676863138222L + -7166640676863138162L) + (long)v12;
         }
      } else {
         Matcher v3 = SECONDS_PATTERN.matcher(text);
         if (v3.find()) {
            return Long.parseLong(v3.group(1));
         } else {
            String v4 = text.replaceAll("[^0-9:]", "").trim();
            if (!v4.isEmpty()) {

               if (v4.matches("\\d+")) {
                  long v5 = Long.parseLong(v4);
                  if (v5 > 0L && v5 < 4592092146544430167L + -4592092146544394167L) {
                     return v5;
                  }
               }
            }

            return -5697456532267250498L + 5697456532267250497L;
         }
      }
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      this.m1231();
      int temp2 = v3 + 1;

      
   }

   private void m1231() {
      this.f951.clear();

      this.f952.clear();
      this.f953.clear();
      
   }

   @Environment(EnvType.CLIENT)
   private static class fm {
      final BlockPos pos;
      long readyAtMs = -8840816442520516787L + 8840816442520516786L;

      fm(BlockPos pos) {
         this.pos = pos;
      }

      boolean hasTimer() {

         return this.readyAtMs > 0L;
      }

      void updateFromHologram(long remainingSeconds) {
         long temp1 = System.currentTimeMillis() + remainingSeconds * (6260795015245929251L + -6260795015245928251L);

         this.readyAtMs = temp1;
      }

      float getRemainingSeconds() {

         if (this.readyAtMs <= 0L) {
            return -1.0F;
         } else {
            float result = (float)(this.readyAtMs - System.currentTimeMillis()) / 1000.0F;

            float v1 = result;
            return Math.max(0.0F, v1);
         }
      }

      
   }
}
