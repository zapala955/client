package ru.white.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Glass Farmer",
   desc = "Легитный фарм стекла: сначала наводится, затем с задержкой строит/ломает центр",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class GlassFarmer extends Module {
   private final long f965;
   private int f961;
   private BlockPos f964;
   private int f967;
   private int f960;
   private long f962;
   private BlockPos f957 = null;
   private boolean f963;
   private int f959;
   private im.jm f958 = im.jm.BUILDING;
   private final int f966;
   protected static final MinecraftClient EventItemUse = MinecraftClient.method_1551();

   private void m1243() {
      int v1 = this.m1247(Items.field_8858);
      if (v1 == -1) {
         this.f960--;
         this.f958 = im.jm.DIGGING;
      } else {

         BlockPos v2 = this.f957.method_10069(this.f959, this.f960, this.f961);
         if (mc.field_1687.method_8320(v2).method_26204() == Blocks.field_10102) {
            this.m1246();
            this.m1245();
         } else {
            if (this.f959 == 0 && this.f961 == 0 && mc.field_1724.method_24515().method_10264() <= v2.method_10264()) {
               if (mc.field_1724.method_24828()) {
                  mc.field_1690.field_1903.method_23481(true);
                  return;
               }

               mc.field_1690.field_1903.method_23481(false);
            }

            mc.field_1724.method_31548().method_61496(v1);
            this.m1249(v2);
            if (this.f963 && v2.equals(this.f964)) {
               if (System.currentTimeMillis() - this.f962 >= -4138955517859563026L + 4138955517859563061L) {
                  new BlockHitResult(
                     new Vec3d((double)v2.method_10263() + 0.5, (double)v2.method_10264() + 0.5, (double)v2.method_10260() + 0.5),
                     Direction.field_11036,
                     v2,
                     false
                  );
                  mc.field_1690.field_1904.method_23481(true);
                  this.f967 = 0;
                  this.m1246();
                  this.m1245();
               }
            } else {
               this.f962 = System.currentTimeMillis();
               this.f963 = true;
               this.f964 = v2;
            }
         }
      }
   }

   public GlassFarmer() {
      this.f959 = -1;
      this.f960 = 0;
      this.f961 = -1;
      this.f962 = 0L;
      this.f963 = false;
      this.f964 = null;
      this.f965 = -5807240798992629179L + 5807240798992629214L;
      this.f966 = 3;
      this.f967 = 0;
   }

   private void m1246() {

      this.f959++;
      if (this.f959 > 1) {
         this.f959 = -1;
         this.f961++;
         if (this.f961 > 1) {
            this.f961 = -1;

            this.f960++;
         }
      }
   }

   @Override
   public void onEnable() {
      if (mc.field_1724 == null) {
         this.toggle();
      } else {
         this.f957 = mc.field_1724.method_24515();
         this.f958 = im.jm.BUILDING;
         this.f959 = -1;
         this.f960 = 0;

         this.f961 = -1;
         this.f967 = 0;
         this.f962 = 0L;

         this.f963 = false;
         this.f964 = null;
      }
   }

   private void m1245() {
      this.f963 = false;
      this.f962 = 0L;
   }

   private int m1248() {

      while (true) {

         if (v1 >= 9) {
            return -1;
         }

         String v2 = mc.field_1724.method_31548().method_5438(v1).method_7909().method_7876();
         if (v2.contains("shovel")) {
            return v1;
         }

         v1++;
         
      }
   }

   @Override
   public void onDisable() {
      this.f957 = null;

      mc.field_1690.field_1886.method_23481(false);
      mc.field_1690.field_1904.method_23481(false);
      mc.field_1690.field_1903.method_23481(false);
      this.f964 = null;
   }

   private void m1244() {
      int v1 = this.m1248();
      if (v1 == -1) {
         this.toggle();
      } else {
         BlockPos v2 = this.f957.method_10069(0, this.f960, 0);
         if (mc.field_1687.method_8320(v2).method_26204() == Blocks.field_10102) {
            mc.field_1724.method_31548().method_61496(v1);
            this.m1249(v2);
            if (this.f963) {

               if (v2.equals(this.f964)) {
                  if (System.currentTimeMillis() - this.f962 < -2071331663817401580L + 2071331663817401615L) {
                     return;
                  }

                  if (mc.field_1761.method_2902(v2, Direction.field_11036)) {
                     if (!mc.field_1690.field_1886.method_1434()) {
                        mc.field_1690.field_1886.method_23481(true);
                     }

                     this.m1245();
                  }

                  return;
               }
            }

            this.f962 = System.currentTimeMillis();
            this.f963 = true;

            this.f964 = v2;
            return;
         } else {

            this.f960--;
            this.m1245();
            if (this.f960 < 0) {
               this.toggle();
            }
         }
      }
   }

   private int m1247(Item item) {

      while (true) {

         if (v2 >= 9) {
            return -1;
         }

         if (mc.field_1724.method_31548().method_5438(v2).method_7909() == item) {
            return v2;
         }

         v2++;
      }
   }

   private void m1249(BlockPos pos) {
      double v2 = (double)pos.method_10263() + 0.5 - mc.field_1724.method_23317();
      double v4 = (double)pos.method_10264() + 0.5 - (mc.field_1724.method_23318() + (double)mc.field_1724.method_5751());
      double v6 = (double)pos.method_10260() + 0.5 - mc.field_1724.method_23321();
      double result = v2 * v2 + v6 * v6;
      int temp1 = v12 * (v12 + 1) % 2;

      if (temp1 != 0) {
         }

      double v8 = Math.sqrt(result);
      float v10 = (float)(Math.toDegrees(Math.atan2(v6, v2)) - 90.0);
      float v11 = (float)(-Math.toDegrees(Math.atan2(v4, v8)));
      Rotation v15 = new Rotation;

      v15./* $VF: Unable to resugar constructor */<init>(v10, v11);
      xd.update(v15, 140.0F, 140.0F, 6, 1);
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1687 != null) {
         if (this.f958 == im.jm.BUILDING) {
            mc.field_1690.field_1886.method_23481(false);

            if (this.f967 < 3) {
               this.f967++;
               return;
            }

            this.m1243();
         } else if (this.f958 == im.jm.DIGGING) {
            mc.field_1690.field_1904.method_23481(false);

            mc.field_1690.field_1903.method_23481(false);

            this.m1244();
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static enum jm {
      BUILDING,
      DIGGING;
      // $VF: synthetic field
      private static final im.jm[] $VALUES = m1242();
   }
}
