package ru.white.module.impl.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.StopWatch;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Auto Potion",
   desc = "Смотрит вниз и кидает все нужные зелья сразу (Сила, Скорость, Огнестойкость), когда эффекта нет",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class AutoPotion extends Module {
   private final StopWatch f935;
   public final MultiSetting potions = new MultiSetting(
      this, "Зелья", new BooleanSetting("Сила", true), new BooleanSetting("Скорость", true), new BooleanSetting("Огнестойкость", true)
   );
   public final SliderSetting delay = new SliderSetting(this, "Задержка", 500.0F, 100.0F, 2000.0F, 50.0F);
   private int f936;

   @Override
   protected void onDisable() {
      this.m1193();
   }

   private List<String> m1195(ItemStack stack) {
      ArrayList v2 = new ArrayList();
      PotionContentsComponent result = (PotionContentsComponent)stack.method_58694(DataComponentTypes.field_49651);

      PotionContentsComponent v3 = result;
      if (v3 == null) {
         return v2;
      } else {
         for (StatusEffectInstance v5 : v3.method_57397()) {
            RegistryEntry v6 = v5.method_5579();
            String v11 = this.m1196(v6);

            String v7 = v11;
            if (v7 != null && this.potions.getValue(v7) && !mc.field_1724.method_6059(v6)) {
               v2.add(v7);
            }
         }

         return v2;
      }
   }

   private void m1193() {
      if (this.f936 != -1 && mc.field_1724 != null) {
         mc.field_1724.method_31548().method_61496(this.f936);
         if (mc.field_1724.field_3944 != null) {

            mc.field_1724.field_3944.method_52787(new UpdateSelectedSlotC2SPacket(this.f936));
         }

         this.f936 = -1;
      }
   }

   private List<Integer> m1194() {
      ArrayList v1 = new ArrayList();
      HashSet result = new HashSet;

      result./* $VF: Unable to resugar constructor */<init>();
      HashSet v2 = result;

      for (int v3 = 0; v3 <= 8; v3++) {
         ItemStack v4 = mc.field_1724.method_31548().method_5438(v3);
         if (v4.method_7909() == Items.field_8436) {
            List v5 = this.m1195(v4);
            if (!v5.isEmpty()) {
               boolean v6 = false;

               for (String v8 : v5) {

                  if (!v2.contains(v8)) {
                     v6 = true;
                     break;
                  }
               }

               if (v6) {
                  v1.add(v3);
                  v2.addAll(v5);
               }
            }
         }
      }

      return v1;
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null && mc.field_1724.field_3944 != null) {
         if (mc.field_1755 == null && !mc.field_1724.method_6115()) {
            List v2 = this.m1194();
            if (v2.isEmpty()) {
               this.m1193();
            } else {
               xd.update(new Rotation(mc.field_1724.method_36454(), 90.0F), 180.0F, 180.0F, 2, 90);

               if (!(mc.field_1724.method_36455() < 88.0F)) {
                  if (this.f935.isReached((long)this.delay.getValue().floatValue())) {
                     this.f936 = mc.field_1724.method_31548().method_67532();

                     for (int v4 : v2) {
                        mc.field_1724.method_31548().method_61496(v4);
                        mc.field_1724.field_3944.method_52787(new UpdateSelectedSlotC2SPacket(v4));
                        mc.field_1761.method_2919(mc.field_1724, Hand.field_5808);
                        mc.field_1724.method_6104(Hand.field_5808);
                     }

                     this.m1193();
                     this.f935.reset();
                  }
               }
            }
         }
      }
   }

   private String m1196(RegistryEntry<StatusEffect> type) {
      if (type.comp_349() == StatusEffects.field_5910.comp_349()) {
         return "Сила";
      } else {
         Object result = type.comp_349();

         if (result == StatusEffects.field_5904.comp_349()) {
            return "Скорость";
         } else {
            result = type.comp_349();

            return result == StatusEffects.field_5918.comp_349() ? "Огнестойкость" : null;
         }
      }
   }

   public AutoPotion() {
      this.f935 = new StopWatch();
      this.f936 = -1;
   }

   
}
