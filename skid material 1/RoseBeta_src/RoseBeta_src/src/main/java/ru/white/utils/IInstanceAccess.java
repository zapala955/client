package ru.white.utils;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;
import ru.white.utils.misc.Oq;
import ru.white.event.impl.EventTick;

@Environment(EnvType.CLIENT)
public final class IInstanceAccess implements IMinecraft {
   private static final int USE_TOLERANCE = 8;
   private static final Map<UUID, Map<fr.hr, Long>> COOLDOWNS = new HashMap<>();
   private static final Map<UUID, fr.ir> USING = new HashMap<>();
   private static EventTick f1303;
   public static boolean debug;
   private static final List<BiConsumer<PlayerEntity, fr.hr>> LISTENERS = new ArrayList<>();
   private static final IInstanceAccess INSTANCE = new IInstanceAccess();

   private IInstanceAccess() {
   }

   public static void tick(EventTick event) {
      if (event != f1303) {
         f1303 = event;
         if (mc.field_1724 != null && mc.field_1687 != null) {
            HashSet v1 = new HashSet();

            for (PlayerEntity v3 : mc.field_1687.method_18456()) {
               if (v3 != mc.field_1724) {
                  v1.add(v3.method_5667());
                  fr.ir v4 = USING.computeIfAbsent(v3.method_5667(), u -> {
                     fr.ir var10000x = new fr.ir();
                     int v1x = 1919035589;
                     int var10001x = v1x * (v1x + 1) % 2;

                     if (var10001x != 0) {
                        }

                     return var10000x;
                  });
                  boolean v5 = v3.method_6115();
                  if (v5 && !v4.active) {
                     ItemStack v6 = v3.method_6030();
                     if (v6.method_7960()) {
                        v6 = v3.method_6047();
                     }

                     if (v6.method_7960()) {
                        v6 = v3.method_6079();
                     }

                     v4.stack = v6.method_7972();
                     v4.ticks = 0;
                  }

                  if (v5) {
                     v4.ticks++;
                  }

                  if (!v5 && v4.active) {
                     int v12 = v4.stack.method_7960() ? 0 : v4.stack.method_7935(v3);
                     if (debug && v12 > 0) {
                        String result = v3.method_5477().getString();
                        String temp1 = v4.stack.method_7909().method_63680().getString();
                        int v8 = v4.ticks;
                        String v9 = temp1;
                        String v10 = result;
                        oq.send(v10 + ": " + v9 + " " + v8 + "/" + v12, oq.qq.INFO);
                     }

                     if (v12 > 0) {

                        if (v4.ticks >= v12 - (8)) {
                           m1657(v3, v4.stack);
                        }
                     }

                     v4.stack = ItemStack.field_8037;
                     v4.ticks = 0;
                  }

                  v4.active = v5;
               }
            }

            USING.keySet().retainAll(v1);
            m1658();
         }
      }
   }

   public static Map<fr.hr, Long> SettingVisibility(UUID uuid) {
      Map result;
      if (uuid == null) {
         result = null;
         int v2 = -2102314287;

         
      } else {
         result = COOLDOWNS.get(uuid);
      }

      Map v1 = result;
      return v1 == null ? Map.of() : v1;
   }

   public static int remaining(UUID uuid, fr.hr item) {
      if (uuid == null) {
         return 0;
      } else {
         Map v2 = COOLDOWNS.get(uuid);
         if (v2 == null) {
            int v4 = 570013408;

            return 0;
         } else {
            Long v3 = (Long)v2.get(item);
            return v3 == null ? 0 : (int)Math.max(0.0, Math.ceil((double)(v3 - System.currentTimeMillis()) / 1000.0));
         }
      }
   }

   private static void m1658() {
      long result = System.currentTimeMillis();

      long v0 = result;
      COOLDOWNS.values().forEach(map -> map.values().removeIf(end -> {
            long var10000x = end;

            boolean v5;
            if (var10000x <= v0) {
               v5 = true;
            } else {
               v5 = false;
               
            }

            return v5;
         }));
      COOLDOWNS.entrySet().removeIf(entry -> {
         boolean var10000x = entry.getValue().isEmpty();

         return var10000x;
      });
   }

   public static void listen(BiConsumer<PlayerEntity, fr.hr> listener) {
      int v1 = -1057681365;
      int temp1 = v1 * (v1 + 1);

      LISTENERS.add(listener);
      
   }

   public static int players() {
      int v0 = -42079387;
      int temp1 = v0 * (v0 + 1);

      return COOLDOWNS.size();
   }

   public static void clear() {
      COOLDOWNS.clear();
      USING.clear();

      
   }

   public static String format(int seconds) {
      Object[] temp1 = new Object[2];

      temp1[0] = seconds / (60);
      temp1[1] = seconds % (60);
      return String.format("%d:%02d", temp1);
   }

   private static void m1657(PlayerEntity player, ItemStack stack) {
      if (stack.method_31574(Items.field_8367)) {
         trigger(player, fr.hr.NOTCH);
      } else if (stack.method_31574(Items.field_8463)) {
         trigger(player, fr.hr.GAPPLE);
      } else if (stack.method_31574(Items.field_8233)) {
         trigger(player, fr.hr.CHORUS);
      } else if (stack.method_31574(Items.field_8551)) {
         trigger(player, fr.hr.KELP);
      } else if (stack.method_31574(Items.field_22021)) {
         trigger(player, fr.hr.SCRAP);
      } else if (stack.method_31574(Items.field_8574)) {

         PotionContentsComponent v2 = (PotionContentsComponent)stack.method_58694(DataComponentTypes.field_49651);
         if (v2 == null) {
            return;
         }

         Iterator v3 = v2.method_57397().iterator();

         while (true) {

            if (!v3.hasNext()) {
               break;
            }

            StatusEffectInstance v4 = (StatusEffectInstance)v3.next();
            if (v4.method_5579().comp_349() == StatusEffects.field_5915.comp_349()) {
               trigger(player, fr.hr.HEAL);
            }

            if (v4.method_5579().comp_349() == StatusEffects.field_5916.comp_349()) {
               trigger(player, fr.hr.NAUSEA);
            }
         }
      }
   }

   public static void trigger(PlayerEntity player, fr.hr item) {
      Map result = COOLDOWNS.computeIfAbsent(player.method_5667(), u -> {
         EnumMap var10000x = new EnumMap;

         var10000x./* $VF: Unable to resugar constructor */<init>(fr.hr.class);

         return var10000x;
      });
      long temp2 = System.currentTimeMillis();

      result.put(item, temp2 + (long)item.seconds * (6223312682563616521L + -6223312682563615521L));

      for (BiConsumer v3 : LISTENERS) {
         v3.accept(player, item);
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum gr {
      FIRE_RESISTANCE("Огнеупор", StatusEffects.field_5918, -22016),
      ABSORPTION("Абсорб", StatusEffects.field_5898, -171),
      STRENGTH("Сила", StatusEffects.field_5910, -43691),
      REGENERATION("Регена", StatusEffects.field_5924, -43521),
      RESISTANCE("Резист", StatusEffects.field_5907, -7829249),
      SPEED("Скорость", StatusEffects.field_5904, -11141121);
      public final RegistryEntry<StatusEffect> effect;
      public final int color;
      // $VF: synthetic field
      private static final fr.gr[] $VALUES = m1655();
      public final String label;

      private gr(String label, RegistryEntry<StatusEffect> effect, int color) {
         this.label = label;
         this.effect = effect;
         this.color = color;
      }
   }

   @Environment(EnvType.CLIENT)
   public static enum hr {
      GAPPLE("Гепл", 30, Items.field_8463),
      NAUSEA("Тошнотка", 60, Items.field_8574),
      KELP("Пласт", 20, Items.field_8551),
      HEAL("Хилка", 20, Items.field_8574),
      CHORUS("Хорус", 20, Items.field_8233),
      NOTCH("Чарка", 150, Items.field_8367),
      SCRAP("Трапка", 15, Items.field_22021);
      public final String label;
      public final int seconds;
      // $VF: synthetic field
      private static final fr.hr[] $VALUES = m1656();
      public final Item icon;

      private hr(String label, int seconds, Item icon) {
         this.label = label;
         this.seconds = seconds;
         this.icon = icon;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class ir {
      ItemStack stack = ItemStack.field_8037;
      boolean active;
      int ticks;

      
   }
}
