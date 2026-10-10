package ru.white.module.impl.combat;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Anti Bot",
   desc = "Не дает Attack Aura ударить бота",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class AntiBot extends Module {
   private final List<PlayerEntity> f240 = new ArrayList<>();

   public boolean isBot(LivingEntity entity) {
      if (entity instanceof PlayerEntity v2 && this.f240.contains(v2)) {

         return true;
      }

      return false;
   }

   public static eg getInstance() {
      eg result = uq.get(eg.class);

      return result;
   }

   public boolean isBot(Entity entity) {
      if (entity instanceof PlayerEntity v2) {

         boolean result = this.f240.contains(v2);
         int temp1 = v4 * (v4 + 1) % 2;

         if (temp1 != 0) {
            }

         if (result) {
            return true;
         }
      }

      return false;
   }

   private void m263() {
      if (mc.field_1724 != null && mc.field_1687 != null) {

         for (PlayerEntity v2 : mc.field_1687.method_18456()) {
            if (v2 != null && v2 != mc.field_1724) {
               boolean v3 = false;
               boolean v4 = false;
               boolean v5 = false;
               boolean v6 = v2.method_6079().method_7909() == Items.field_8162;
               boolean v7 = v2.method_6118(EquipmentSlot.field_6166).method_7909() == Items.field_8370
                  || v2.method_6118(EquipmentSlot.field_6166).method_7909() == Items.field_8660
                  || v2.method_6118(EquipmentSlot.field_6172).method_7909() == Items.field_8570
                  || v2.method_6118(EquipmentSlot.field_6172).method_7909() == Items.field_8396
                  || v2.method_6118(EquipmentSlot.field_6174).method_7909() == Items.field_8577
                  || v2.method_6118(EquipmentSlot.field_6174).method_7909() == Items.field_8523
                  || v2.method_6118(EquipmentSlot.field_6169).method_7909() == Items.field_8267
                  || v2.method_6118(EquipmentSlot.field_6169).method_7909() == Items.field_8743;
               boolean v8 = v2.method_6047().method_7909() != Items.field_8162;
               boolean v9 = v2.method_7344().method_7586() == 20;

               for (EquipmentSlot v13 : new EquipmentSlot[]{
                  EquipmentSlot.field_6166, EquipmentSlot.field_6172, EquipmentSlot.field_6174, EquipmentSlot.field_6169
               }) {
                  ItemStack v14 = v2.method_6118(v13);
                  if (!v3 && v14.method_7909() != Items.field_8162) {
                     v3 = true;
                  }

                  if (!v4 && v14.method_7923()) {
                     v4 = true;
                  }

                  if (!v5 && !v14.method_7986()) {
                     v5 = true;
                  }
               }

               if (!v3 || !v4 || !v5 || !v6 || !v7 || !v8 || !v9) {
                  this.f240.remove(v2);
               } else if (!this.f240.contains(v2)) {
                  this.f240.add(v2);
               }
            }
         }
      }
   }

   public boolean checkBot(Entity entity) {

      return this.isBot(entity);
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.f240.clear();
   }

   @Override
   public void onEnable() {

      super.onEnable();
      this.f240.clear();
   }

   @EventTarget
   public void onTick(EventTick event) {
      this.m263();
      int result = v2 * (v2 + 1);

      
   }

   
}
