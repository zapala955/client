package ru.white.utils.media;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class Nr {
   private static Entity f1308;

   public static boolean validate() {
      if (f1308 == null) {
         return false;
      } else {
         MinecraftClient v0 = MinecraftClient.method_1551();
         label35:
         if (v0.field_1724 != null && v0.field_1687 != null && !f1308.method_31481() && f1308.method_73183() == v0.field_1687) {
            if (f1308 instanceof LivingEntity v1 && !v1.method_5805()) {
               break label35;
            }

            if (v0.method_1560() != f1308) {
               v0.method_1504(f1308);
            }

            return false;
         }

         stop();
         return true;
      }
   }

   public static Entity getTarget() {

      int v0 = -565463346;
      int temp2 = v0 + 1;

      return f1308;
   }

   public static int chunksInSquare(int radius) {
      int v1 = radius * 2 + 1;

      return v1 * v1;
   }

   public static int loadedChunksAround(Entity entity, int radius) {
      MinecraftClient v2 = MinecraftClient.method_1551();

      if (entity != null && v2.field_1687 != null) {
         ChunkPos v3 = entity.method_31476();
         int v4 = 0;

         for (int v5 = -radius; v5 <= radius; v5++) {
            for (int v6 = -radius; v6 <= radius; v6++) {
               if (v2.field_1687.method_2935().method_2857(v3.field_9181 + v5, v3.field_9180 + v6, ChunkStatus.field_12803, false) != null) {
                  v4++;
               }
            }
         }

         return v4;
      } else {
         return 0;
      }
   }

   private Nr() {
   }

   public static boolean isActive() {
      boolean v4;
      if (f1308 != null) {

         v4 = true;
      } else {
         v4 = false;
      }

      return v4;
   }

   public static boolean start(Entity entity) {
      MinecraftClient v1 = MinecraftClient.method_1551();
      if (entity == null || v1.field_1724 == null || v1.field_1687 == null) {
         return false;
      } else if (entity != v1.field_1724 && !entity.method_31481()) {
         f1308 = entity;

         v1.method_1504(entity);
         if (v1.field_1769 != null) {
            v1.field_1769.method_3292();
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean isTerrainLoaded(Entity entity) {
      MinecraftClient v1 = MinecraftClient.method_1551();

      if (entity != null && v1.field_1687 != null) {
         ChunkPos v2 = entity.method_31476();
         return v1.field_1687.method_2935().method_2857(v2.field_9181, v2.field_9180, ChunkStatus.field_12803, false) != null;
      } else {
         return false;
      }
   }

   public static void stop() {
      MinecraftClient v0 = MinecraftClient.method_1551();
      f1308 = null;

      if (v0.field_1724 != null) {
         v0.method_1504(v0.field_1724);
      }
   }

   public static void reset() {
      f1308 = null;
   }

   
}
