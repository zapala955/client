package ru.white.module.impl.misc;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
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
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Warden Helper",
   desc = "Отслеживание сундуков в Варден зоне: таймеры, ESP и неймтеги",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class WardenHelper extends Module {
   private static final RenderPipeline BOX_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "warden_helper_esp"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final Pattern TIME_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})(?::(\\d{2}))?");
   private static final int COLOR_YELLOW = up.getColor(255, 220, 50, 180);
   private static final int COLOR_GREEN = up.getColor(60, 255, 60, 180);
   private static final RenderLayer BOX_LAYER = RenderLayer.method_75940(
      "warden_helper_esp",
      RenderSetup.method_75927(BOX_PIPELINE).method_75929(4096).method_75938()
   );
   private static final Pattern SECONDS_PATTERN = Pattern.compile("(\\d+)\\s*(с|s|сек|sec)");
   private static final int COLOR_UNKNOWN = up.getColor(180, 180, 180, 120);
   private final BufferAllocator f995;
   private final Set<BlockPos> f993;
   private final Set<BlockPos> f994;
   private final Map<BlockPos, qm.rm> f992 = new ConcurrentHashMap<>();
   private static final int COLOR_RED = up.getColor(255, 60, 60, 180);

   private int m1327(qm.rm data) {
      if (!data.hasTimer()) {
         return COLOR_UNKNOWN;
      } else {
         float v2 = data.getRemainingSeconds();
         if (v2 <= 0.0F) {
            float v9 = (float)(Math.sin((double)System.currentTimeMillis() / 200.0) * 0.3 + 0.7);
            int v4 = (int)(255.0F * v9);
            return up.getColor(0, v4, (int)(128.0F * v9), 220);
         } else if (v2 > 120.0F) {
            return COLOR_RED;
         } else {

            if (v2 > 20.0F) {
               float v10 = v2 - 20.0F;

               float v8 = 1.0F - v10 / 100.0F;
               return up.interpolateColor(COLOR_RED, COLOR_YELLOW, v8);
            } else {
               float v3 = 1.0F - v2 / 20.0F;
               return up.interpolateColor(COLOR_YELLOW, COLOR_GREEN, v3);
            }
         }
      }
   }

   private boolean m1323(BlockPos pos) {
      if (mc.field_1724 == null) {
         return false;
      } else {
         double v2 = (double)pos.method_10263();
         double v4 = (double)pos.method_10264();
         double v6 = (double)pos.method_10260();
         boolean v8 = Math.abs(Math.abs(v2) - 2000.0) <= 150.0 && Math.abs(Math.abs(v6) - 2000.0) <= 150.0;

         boolean v9 = Math.abs(v4 - mc.field_1724.method_23318()) <= 30.0;
         return v8 && v9;
      }
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      this.f992.clear();

      this.f993.clear();
      this.f994.clear();
   }

   private void m1328(VertexConsumer b, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, int cB, int cT) {
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

   private String m1326(int totalSec) {
      if (totalSec <= 0) {
         return "0:00";
      } else {
         int temp1 = 60;
         int temp3 = v4 + 1;

         int temp2 = v4 * temp3;

         int v2 = totalSec / temp1;
         int v3 = totalSec % (60);
         return String.format("%d:%02d", v2, v3);
      }
   }

   @EventTarget(
      priority = -500
   )
   public void onRender3D(EventTrace event) {

      if (mc.field_1724 != null) {
         if (mc.field_1687 != null) {
            if (!this.m1322()) {
               return;
            }

            Matrix4f v2 = event.getMatrixStack().method_23760().method_23761();
            Vec3d v3 = mc.field_1773.method_19418().method_71156();
            Immediate v4 = VertexConsumerProvider.method_22991(this.f995);
            VertexConsumer v5 = v4.method_73477(BOX_LAYER);
            ChunkPos v6 = mc.field_1724.method_31476();
            int v7 = (Integer)mc.field_1690.method_42503().method_41753();

            for (int v8 = v6.field_9181 - v7; v8 <= v6.field_9181 + v7; v8++) {
               for (int v9 = v6.field_9180 - v7; v9 <= v6.field_9180 + v7; v9++) {
                  WorldChunk v10 = mc.field_1687.method_8497(v8, v9);
                  if (v10 != null) {
                     for (BlockEntity v12 : v10.method_12214().values()) {
                        if (v12 instanceof ChestBlockEntity) {
                           BlockPos v13 = v12.method_11016();
                           if (this.m1323(v13)) {
                              if (!this.f992.containsKey(v13)) {
                                 this.f992.put(v13, new qm.rm(v13));
                              }

                              qm.rm v14 = this.f992.get(v13);
                              if (v14.hasTimer()) {
                                 int v15 = this.m1327(v14);
                                 float v16 = (float)((double)v13.method_10263() - v3.field_1352);
                                 float v17 = (float)((double)v13.method_10264() - v3.field_1351);
                                 float v18 = (float)((double)v13.method_10260() - v3.field_1350);
                                 int v19 = up.replAlpha(v15, 0.8F);
                                 int v20 = up.replAlpha(v15, 0.0F);
                                 this.m1328(v5, v2, v16, v17, v18, v16 + 1.0F, v17 + 1.0F, v18 + 1.0F, v19, v20);
                              }
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

   private long m1325(String text) {
      Matcher v2 = TIME_PATTERN.matcher(text);
      if (v2.find()) {
         if (v2.group(3) != null) {
            int v10 = Integer.parseInt(v2.group(1));
            int v12 = Integer.parseInt(v2.group(2));

            int v13 = Integer.parseInt(v2.group(3));
            return (long)v10 * (-2882509479644954087L + 2882509479644957687L) + (long)v12 * (-5314759030638491350L + 5314759030638491410L) + (long)v13;
         } else {
            int v9 = Integer.parseInt(v2.group(1));
            int v11 = Integer.parseInt(v2.group(2));
            return (long)v9 * (-7253569545124442068L + 7253569545124442128L) + (long)v11;
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
                  if (v5 > 0L && v5 < -8804574207277490179L + 8804574207277526179L) {
                     return v5;
                  }
               }
            }

            return -7374705216868496627L + 7374705216868496626L;
         }
      }
   }

   @EventTarget
   public void onTick(EventRenderModel event) {

      if (mc.field_1724 != null) {

         if (mc.field_1687 != null) {
            if (!this.m1322()) {
               return;
            }

            this.m1324();
            return;
         }
      }
   }

   private boolean m1322() {
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

   public WardenHelper() {
      this.f993 = new HashSet<>();
      this.f994 = new HashSet<>();
      this.f995 = new BufferAllocator(262144);
   }

   private void m1324() {
      for (Entry v2 : this.f992.entrySet()) {
         BlockPos v3 = (BlockPos)v2.getKey();
         qm.rm v4 = (qm.rm)v2.getValue();
         Box result = new Box;

         result./* $VF: Unable to resugar constructor */<init>(v3);
         Box v5 = result.method_1009(1.0, 3.0, 1.0);

         List v6 = mc.field_1687.method_8390(ArmorStandEntity.class, v5, e -> {
            boolean var10000x = e.method_16914();

            return var10000x;
         });
         boolean v7 = false;

         for (ArmorStandEntity v9 : v6) {
            String v10 = v9.method_5797().getString();
            String v11 = up.removeFormatting(v10);
            if (v11 != null) {
               long v12 = this.m1325(v11);
               if (v12 >= 0L) {
                  v4.updateFromHologram(v12);
                  v7 = true;
                  break;
               }
            }
         }

         if (!v7) {

            v4.hologramVisible = false;
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();

      this.f992.clear();
      int v4 = v1 * (v1 + 1) % 2;

      if (v4 != 0) {
         }

      this.f993.clear();
      this.f994.clear();
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.f992.clear();
      this.f993.clear();
      this.f994.clear();
   }

   @Environment(EnvType.CLIENT)
   private static class rm {
      long lastReadSeconds;
      final BlockPos pos;
      boolean hologramVisible;
      long lastHologramUpdateMs;
      long readyAtMs = 1102675669864512277L + -1102675669864512278L;

      void updateFromHologram(long remainingSeconds) {
         this.hologramVisible = true;
         this.lastHologramUpdateMs = System.currentTimeMillis();
         long v3 = System.currentTimeMillis() + remainingSeconds * (964972437439743984L + -964972437439742984L);
         if (this.readyAtMs == 4121734456557601247L + -4121734456557601248L) {
            this.readyAtMs = v3;
            this.lastReadSeconds = remainingSeconds;
         } else {
            float v5 = this.getRemainingSeconds();
            float v6 = Math.abs(v5 - (float)remainingSeconds);
            if (v6 > 3.0F) {
               this.readyAtMs = v3;
            }

            this.lastReadSeconds = remainingSeconds;
         }
      }

      rm(BlockPos pos) {
         this.lastHologramUpdateMs = 0L;
         this.hologramVisible = false;
         this.lastReadSeconds = 5172420625940150041L + -5172420625940150042L;
         this.pos = pos;
      }

      void reset() {
         this.readyAtMs = -9088487008045587683L + 9088487008045587682L;
         int temp1 = v2 + 1;

         this.hologramVisible = false;
         this.lastReadSeconds = 4917526578660269593L + -4917526578660269594L;
      }

      boolean hasTimer() {
         boolean v2;
         if (this.readyAtMs > 0L) {

            v2 = true;
         } else {
            v2 = false;
         }

         return v2;
      }

      float getRemainingSeconds() {
         if (this.readyAtMs <= 0L) {
            return -1.0F;
         } else {
            float v1 = (float)(this.readyAtMs - System.currentTimeMillis()) / 1000.0F;

            return Math.max(0.0F, v1);
         }
      }

      
   }
}
