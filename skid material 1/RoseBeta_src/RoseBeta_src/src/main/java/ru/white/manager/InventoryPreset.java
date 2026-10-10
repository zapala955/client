package ru.white.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class InventoryPreset {
   public static final int ARMOR_SLOTS = 4;
   private final String name;
   public static final int OFFHAND_SLOT = 40;
   public static final int SLOT_COUNT = 41;
   private final List<ta.ua> slots;
   public static final int INVENTORY_SLOTS = 36;
   private final long createdAt;

   public InventoryPreset(String name, long createdAt, List<ta.ua> slots) {
      this.name = name;
      this.createdAt = createdAt;
      ArrayList v5 = new ArrayList(41);

      for (int v6 = 0; v6 < 41; v6++) {
         ta.ua v7 = v6 < slots.size() ? (ta.ua)slots.get(v6) : ta.ua.EMPTY;
         v5.add(v7 == null ? ta.ua.EMPTY : v7);
      }

      this.slots = Collections.unmodifiableList(v5);
   }

   public InventoryPreset.ua slot(int logicalSlot) {

      return logicalSlot >= 0 && logicalSlot < this.slots.size() ? this.slots.get(logicalSlot) : ta.ua.EMPTY;
   }

   public List<ta.ua> slots() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.slots;
   }

   public long createdAt() {

      return this.createdAt;
   }

   public String name() {

      return this.name;
   }

   @Environment(EnvType.CLIENT)
   public static record InventoryItem(String itemId, int count, String displayName, String potionSignature, String potionData) {
      private final String f24;
      public static final InventoryPreset.ua EMPTY = new ta.ua("", 0, "", "", "");
      public static final String LEGACY_NAME_PREFIX = "legacy-name:";

      public boolean matches(ItemStack stack) {
         if (!this.isEmpty()) {
            if (stack != null && !stack.method_7960() && stack.method_7947() >= this.count) {
               return this.sameItem(stack);
            } else {

               return false;
            }
         } else {
            boolean result;
            if (stack != null && !stack.method_7960()) {
               result = false;
            } else {
               result = true;
               
            }

            return result;
         }
      }

      public int count() {
         int temp2 = v1 + 1;

         return this.count;
      }

      public String itemId() {

         return this.f24;
      }

      private static PotionContentsComponent m83(String json) {
         if (json != null && !json.isBlank()) {
            try {
               Codec result = PotionContentsComponent.field_49275;
               JsonOps v6 = JsonOps.INSTANCE;
               JsonElement v7 = JsonParser.parseString(json);

               return (PotionContentsComponent)result.parse(v6, v7).result().orElse(null);
            } catch (Exception v5) {
               int temp2 = v2 + 1;

               return null;
            }
         } else {
            return null;
         }
      }

      public ItemStack toStack() {
         if (this.isEmpty()) {
            return ItemStack.field_8037;
         } else {
            Identifier v1 = Identifier.method_12829(this.f24);
            if (v1 == null) {
               return ItemStack.field_8037;
            } else {

               ItemStack v2 = Registries.field_41178.method_17966(v1).map(item -> {
                  ItemStack result = new ItemStack;

                  result./* $VF: Unable to resugar constructor */<init>(item, this.count);
                  return result;
               }).orElse(ItemStack.field_8037);
               if (v2.method_7960()) {
                  return v2;
               } else {
                  PotionContentsComponent v3 = m83(this.potionData);
                  if (v3 != null) {
                     v2.method_57379(DataComponentTypes.field_49651, v3);
                  }

                  if (!this.displayName.isBlank() && !this.displayName.equals(v2.method_7964().getString())) {
                     v2.method_57379(DataComponentTypes.field_49631, Text.method_43470(this.displayName));
                  }

                  return v2;
               }
            }
         }
      }

      public String matchKey() {

         String v1 = this.potionSignature;
         String v2 = this.f24;
         StringBuilder result = new StringBuilder().append((Object)v2);

         result = result.append("\u0000");

         return result.append((Object)v1).toString();
      }

      public InventoryItem(String itemId, int count, String displayName, String potionSignature, String potionData) {
         itemId = itemId == null ? "" : itemId;
         count = Math.max(0, count);
         displayName = displayName == null ? "" : displayName;
         potionSignature = potionSignature == null ? "" : potionSignature;
         potionData = potionData == null ? "" : potionData;
         this.f24 = itemId;
         this.count = count;
         this.displayName = displayName;
         this.potionSignature = potionSignature;
         this.potionData = potionData;
      }

      public String potionData() {

         return this.potionData;
      }

      @Override
      public final 

         return ObjectMethods.bootstrap<"hashCode",ta.ua,"itemId;count;displayName;potionSignature;potionData",ta.ua::f24,ta.ua::count,ta.ua::displayName,ta.ua::potionSignature,ta.ua::potionData>(
            this
         );
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",ta.ua,"itemId;count;displayName;potionSignature;potionData",ta.ua::f24,ta.ua::count,ta.ua::displayName,ta.ua::potionSignature,ta.ua::potionData>(
            this, o
         );
      }

      public static ta.ua fromStack(ItemStack stack) {
         if (stack != null && !stack.method_7960()) {
            Identifier v1 = Registries.field_41178.method_10221(stack.method_7909());
            PotionContentsComponent v2 = (PotionContentsComponent)stack.method_58694(DataComponentTypes.field_49651);
            String v3;
            if (v2 != null) {
               String v4 = String.valueOf(v2);
               v3 = "potion:" + v4;
            } else if (v1.method_12832().endsWith("bundle")) {
               String v5 = String.valueOf(stack.method_57353());
               StringBuilder result = new StringBuilder;

               result./* $VF: Unable to resugar constructor */<init>();
               result = result.append("bundle:");

               v3 = result.append((Object)v5).toString();
            } else {
               v3 = "";
            }

            return new ta.ua(v1.toString(), stack.method_7947(), stack.method_7964().getString(), v3, m82(v2));
         } else {
            return EMPTY;
         }
      }

      public boolean isEmpty() {
         if (!this.f24.isBlank()) {

            if (this.count > 0) {
               return false;
            }
         }

         return true;
      }

      private static String m82(PotionContentsComponent contents) {
         int temp2 = v1 + 1;

         return contents == null
            ? ""
            : PotionContentsComponent.field_49275.encodeStart(JsonOps.INSTANCE, contents).result().<String>map(JsonElement::toString).orElse("");
      }

      public boolean sameItem(ItemStack stack) {
         if (!this.isEmpty() && stack != null && !stack.method_7960()) {
            if (!Registries.field_41178.method_10221(stack.method_7909()).toString().equals(this.f24)) {
               return false;
            } else if (this.potionSignature.isBlank()) {
               return true;
            } else if (this.potionSignature.startsWith("legacy-name:")) {
               String v8 = this.potionSignature;
               String temp1 = "legacy-name:";

               return v8.substring(temp1.length()).equals(stack.method_7964().getString());
            } else if (this.potionSignature.startsWith("bundle:")) {
               String v7 = this.potionSignature;
               String v3 = String.valueOf(stack.method_57353());
               return v7.equals("bundle:" + v3);
            } else {
               Object v2 = stack.method_58694(DataComponentTypes.field_49651);
               if (v2 == null) {
                  return false;
               } else {
                  if (!this.potionSignature.equals(v2.toString())) {
                     String result = this.potionSignature;
                     String v4 = String.valueOf(v2);
                     if (!result.equals("potion:" + v4)) {
                        return false;
                     }
                  }

                  return true;
               }
            }
         } else {
            return false;
         }
      }

      public static boolean canHavePotionContents(String itemId) {
         int temp2 = -731699607;

         return itemId.equals(StrDec.d("p09QCxwh0p2U9lQb4RC2rw==", temp2))
            || itemId.equals("minecraft:splash_potion")
            || itemId.equals("minecraft:lingering_potion")
            || itemId.equals("minecraft:tipped_arrow");
      }

      
   }
}
