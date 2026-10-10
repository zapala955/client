package ru.white.module.impl.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.ChatUtil;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Potion Farmer",
   desc = "Автоматически варит зелье [не роботает временно]",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class PotionFarmer extends Module {
   private final Item[] f972;
   private final MinecraftClient f968 = MinecraftClient.method_1551();
   private int f971;
   private BlockPos f970;
   private final List<BlockPos> f969 = new ArrayList<>();

   private void m1252() {
      if (this.f968.field_1724 != null && this.f968.field_1687 != null) {

         BlockPos v1 = this.f968.field_1724.method_24515();
         ArrayList v2 = new ArrayList();

         for (int v3 = -5; v3 <= 5; v3++) {
            for (int v4 = -5; v4 <= 5; v4++) {
               for (int v5 = -5; v5 <= 5; v5++) {
                  BlockPos v6 = v1.method_10069(v3, v4, v5);
                  Block v7 = this.f968.field_1687.method_8320(v6).method_26204();
                  if (v7 == Blocks.field_10034) {
                     v2.add(v6);
                  } else if (v7 == Blocks.field_10333) {
                     this.f970 = v6;
                  }
               }
            }
         }

         int result = v2.size();

         if (result >= 3) {
            v2.sort((b1, b2) -> {
               int v3x = 1646255843;

               return Integer.compare(b1.method_10264(), b2.method_10264());
            });
            this.f969.addAll(v2.subList(0, 3));
         }
      }
   }

   private void m1266(GenericContainerScreenHandler chest, int amount) {
      int v3 = chest.method_17388() * (9);

      for (int v5 = 0; v5 < v3; v5++) {

         if (this.m1257(chest.method_7611(v5).method_7677())) {

            this.f968.field_1761.method_2906(chest.field_7763, v5, 0, SlotActionType.field_7794, this.f968.field_1724);
            if (++v4 >= amount) {
               break;
            }
         }
      }
   }

   private boolean m1258() {
      if (this.f968.field_1724 == null) {
         return false;
      } else {
         PlayerInventory result = this.f968.field_1724.method_31548();

         PlayerInventory v1 = result;

         for (int v2 = 0; v2 < 36; v2++) {
            if (this.m1257(v1.method_5438(v2))) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean m1257(ItemStack stack) {
      if (stack.method_7909() != Items.field_8574) {
         return false;
      } else {
         PotionContentsComponent v2 = (PotionContentsComponent)stack.method_58694(DataComponentTypes.field_49651);
         if (v2 != null) {

            Optional result = v2.comp_2378();

            if (result.isPresent()) {
               RegistryEntry v3 = (RegistryEntry)v2.comp_2378().get();
               return v3.equals(Potions.field_8991);
            }
         }

         return false;
      }
   }

   private void m1253() {
      if (!this.m1258()) {

         this.m1262(this.f969.get(1));
      } else if (this.f971 < 4 && !this.m1260(this.f972[this.f971])) {
         this.m1262(this.f969.get(0));
      } else if (this.m1259()) {
         this.m1262(this.f969.get(2));
      } else {

         this.m1262(this.f970);
      }
   }

   private void m1263(Item item, BrewingStandScreenHandler stand) {
      for (int v3 = 5; v3 < stand.field_7761.size(); v3++) {
         if (stand.method_7611(v3).method_7677().method_7909() == item) {

            this.f968.field_1761.method_2906(stand.field_7763, v3, 0, SlotActionType.field_7794, this.f968.field_1724);
            break;
         }
      }
   }

   private int m1261(Item item) {
      if (this.f968.field_1724 == null) {
         return -1;
      } else {

         PlayerInventory v2 = this.f968.field_1724.method_31548();

         for (int v3 = 0; v3 < 36; v3++) {
            if (v2.method_5438(v3).method_7909() == item) {
               return v3;
            }
         }

         return -1;
      }
   }

   public PotionFarmer() {
      this.f970 = null;
      this.f971 = 0;
      this.f972 = new Item[]{Items.field_8790, Items.field_8071, Items.field_8711, Items.field_8725};
   }

   private void m1254(BrewingStandScreenHandler stand) {
      ItemStack v2 = stand.method_7611(3).method_7677();
      ItemStack v3 = stand.method_7611(4).method_7677();

      if (v3.method_7960() && this.m1260(Items.field_8183)) {
         this.m1263(Items.field_8183, stand);
      } else {
         boolean v4 = !stand.method_7611(0).method_7677().method_7960()
            || !stand.method_7611(1).method_7677().method_7960()
            || !stand.method_7611(2).method_7677().method_7960();
         if (!v4) {
            this.m1264(stand);
         } else {
            if (v2.method_7960() && this.f971 < 4) {
               int v5 = this.m1261(this.f972[this.f971]);
               if (v5 != -1) {
                  int v6 = v5 + 5;

                  this.f968.field_1761.method_2906(stand.field_7763, v6, 1, SlotActionType.field_7790, this.f968.field_1724);
                  this.f968.field_1761.method_2906(stand.field_7763, 3, 0, SlotActionType.field_7790, this.f968.field_1724);
                  this.f968.field_1761.method_2906(stand.field_7763, v6, 0, SlotActionType.field_7790, this.f968.field_1724);
                  this.f971++;
                  if (this.f971 > 3) {
                     this.f971 = 0;
                  }
               }
            }

            for (int v10 = 0; v10 < 3; v10++) {

               ItemStack v11 = stand.method_7611(v10).method_7677();
               if (this.m1256(v11)) {
                  this.f968.field_1761.method_2906(stand.field_7763, v10, 0, SlotActionType.field_7794, this.f968.field_1724);
               }
            }
         }
      }
   }

   private boolean m1256(ItemStack stack) {
      if (stack.method_7909() != Items.field_8574) {
         return false;
      } else {
         Object result = stack.method_58694(DataComponentTypes.field_49651);

         PotionContentsComponent v2 = (PotionContentsComponent)result;
         if (v2 != null && v2.comp_2378().isPresent()) {
            RegistryEntry v3 = (RegistryEntry)v2.comp_2378().get();
            return v3.equals(Potions.field_9000);
         } else {
            return false;
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick eventUpdate) {
      if (this.f968.field_1724 != null && this.f968.field_1687 != null) {

         if (this.f968.field_1761 != null) {
            this.toggle();
            zp.addChatMessageDev(
               "[PotionFarmer] Ошибка: Модуль временно не роботает!"
            );
            if (this.f968.field_1724.field_7512 instanceof BrewingStandScreenHandler) {
               this.m1254((BrewingStandScreenHandler)this.f968.field_1724.field_7512);
            } else if (this.f968.field_1724.field_7512 instanceof GenericContainerScreenHandler) {
               this.m1255((GenericContainerScreenHandler)this.f968.field_1724.field_7512);
            } else {
               this.m1253();
            }

            return;
         }
      }
   }

   private void m1264(BrewingStandScreenHandler stand) {

      while (true) {

         if (v2 >= stand.field_7761.size()) {
            return;
         }

         if (this.m1257(stand.method_7611(v2).method_7677())) {
            this.f968.field_1761.method_2906(stand.field_7763, v2, 0, SlotActionType.field_7794, this.f968.field_1724);
         }

         v2++;
      }
   }

   private void m1255(GenericContainerScreenHandler chest) {
      if (this.m1259()) {
         for (int v7 = 0; v7 < chest.field_7761.size(); v7++) {
            ItemStack v8 = chest.method_7611(v7).method_7677();
            if (this.m1256(v8) && v7 >= chest.method_17388() * (9)) {
               this.f968.field_1761.method_2906(chest.field_7763, v7, 0, SlotActionType.field_7794, this.f968.field_1724);
            }
         }
      } else {
         for (Item v5 : this.f972) {
            boolean result = this.m1260(v5);

            if (!result) {
               this.m1265(chest, v5, 1);
               return;
            }
         }

         if (!this.m1260(Items.field_8183)) {
            this.m1265(chest, Items.field_8183, 2);
         } else {
            if (!this.m1258()) {
               this.m1266(chest, 3);
            }
         }
      }
   }

   private boolean m1260(Item item) {

      int result = this.m1261(item);

      return result != -1;
   }

   private boolean m1259() {
      if (this.f968.field_1724 == null) {
         return false;
      } else {
         PlayerInventory result = this.f968.field_1724.method_31548();

         PlayerInventory v1 = result;

         for (int v2 = 0; v2 < 36; v2++) {
            if (this.m1256(v1.method_5438(v2))) {
               return true;
            }
         }

         return false;
      }
   }

   private void m1265(GenericContainerScreenHandler chest, Item item, int amount) {
      int v4 = chest.method_17388() * (9);

      for (int v6 = 0; v6 < v4; v6++) {
         if (chest.method_7611(v6).method_7677().method_7909() == item) {

            this.f968.field_1761.method_2906(chest.field_7763, v6, 0, SlotActionType.field_7794, this.f968.field_1724);
            v5++;

            if (v5 >= amount) {
               break;
            }
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();

      this.f969.clear();
      this.f970 = null;
      this.f971 = 0;
      this.m1252();
   }

   private void m1262(BlockPos pos) {
      if (pos != null && this.f968.field_1761 != null && this.f968.field_1724 != null) {
         BlockHitResult result = new BlockHitResult;
         Vec3d temp2 = new Vec3d;
         double temp4 = (double)pos.method_10263() + 0.5;
         double temp5 = (double)pos.method_10264();

         temp2./* $VF: Unable to resugar constructor */<init>(temp4, temp5 + 0.5, (double)pos.method_10260() + 0.5);
         result./* $VF: Unable to resugar constructor */<init>(temp2, Direction.field_11036, pos, false);
         BlockHitResult v2 = result;

         this.f968.field_1761.method_2896(this.f968.field_1724, Hand.field_5808, v2);
      }
   }

   
}
