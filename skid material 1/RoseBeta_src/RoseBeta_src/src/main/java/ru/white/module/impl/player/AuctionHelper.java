package ru.white.module.impl.player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.misc.Of;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.BlockUtil;
import ru.white.utils.ChatUtil;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Auction Helper",
   category = Category.PLAYER,
   desc = "Подсвечивает 3 самых дешёвых лота. Поиск и автопродажа по биндам."
)
@Environment(EnvType.CLIENT)
public class AuctionHelper extends Module {
   private String f567;
   private Item f574;
   private List<String> f571;
   public final SettingVisibility sellKey;
   private int f570;
   private int f568;
   private ItemEnchantmentsComponent f572;
   public final BooleanSetting autoNextPage;
   private int f576;
   private final BlockUtil f566;
   private static final float DURABILITY_TOLERANCE = 0.15F;
   private long f569;
   private yh.zh f565;
   private int f575;
   private float f573;
   public final SliderSetting maxPagesSetting;
   public final SettingVisibility searchKey = new SettingVisibility(this, "Клавиша поиска");

   private void m541(String msg) {
      zp.addChatMessage(msg);

      
   }

   private void m542() {
      this.f565 = yh.zh.IDLE;
      this.f570 = 0;
      this.f567 = "";
      this.f568 = 1;
      this.f569 = 0L;

      this.f571.clear();
      this.f572 = null;
      this.f573 = -1.0F;
      this.f574 = null;
      this.f575 = -1;
      this.f576 = 0;
   }

   private long m528(long price) {
      if (price <= 0L) {
         return 1L;
      } else if (price >= 1493226409538636187L + -1493226409538635187L) {
         long v3 = price / (-8983356322554875196L + 8983356322554876196L);

         long v5 = price % (-624845068714545747L + 624845068714546747L) / (-4644146365876249435L + 4644146365876249535L);
         return price % (-3497077242498856526L + 3497077242498857526L) >= 1249214341996513229L + -1249214341996512729L
            ? v3 * (-1511273792297416357L + 1511273792297417357L) + -4145058964693510065L + 4145058964693510565L
            : v3 * (-5886708784415836514L + 5886708784415837514L);
      } else {
         return price / (7403628632603701739L + -7403628632603701729L) * (-8360755155383323755L + 8360755155383323765L);
      }
   }

   public static List<String> getEnchantNames(ItemStack stack) {
      ArrayList v1 = new ArrayList();
      if (stack != null && !stack.method_7960()) {
         ComponentMap result = stack.method_57353();

         LoreComponent v2 = (LoreComponent)result.method_58694(DataComponentTypes.field_49632);

         if (v2 == null) {
            return v1;
         } else {
            for (Text v4 : v2.comp_2400()) {
               String v5 = v4.getString().trim();
               if (!v5.isEmpty()
                  && !v5.contains("$")
                  && !v5.toLowerCase().contains("цена")
                  && !v5.contains("Продавец")
                  && !v5.contains("Истекает")
                  && !v5.contains("Нажмите")
                  && !v5.contains("Починка")
                  && (v5.matches(".*[IVX1-9]+$") || m529(v5))) {
                  v1.add(v5);
               }
            }

            return v1;
         }
      } else {
         return v1;
      }
   }

   private boolean m537(ItemStack stack) {
      ItemEnchantmentsComponent v2 = (ItemEnchantmentsComponent)stack.method_58694(DataComponentTypes.field_49633);
      if (v2 != null && !v2.method_57543()) {
         for (RegistryEntry v4 : v2.method_57534()) {
            if (v4.method_55840().contains("mending")) {
               return true;
            }
         }
      }

      LoreComponent v7 = (LoreComponent)stack.method_57353().method_58694(DataComponentTypes.field_49632);
      if (v7 != null) {
         for (Text v5 : v7.comp_2400()) {
            String result = v5.getString();

            if (result.contains("Починка")) {
               return true;
            }
         }
      }

      return false;
   }

   @EventTarget
   public void onKey(EventPostMotion e) {
      if (mc.field_1724 != null) {
         int result = e.getKey();

         if (result != -1) {
            if (this.f565 != yh.zh.IDLE) {
               return;
            }

            if (e.getKey() == this.searchKey.get()) {
               this.m526();
            } else if (e.getKey() == this.sellKey.get()) {
               this.m527();
            }

            return;
         }
      }
   }

   private int m534(String r) {
      switch (r.hashCode()) {
         case 73:
            if (r.equals("I")) {
               v3 = 0;
            }
            break;
         case 86:
            if (r.equals("V")) {
               v3 = 4;
            }
            break;
         case 88:
            if (r.equals("X")) {
               v3 = 9;
            }
            break;
         case 2336:
            if (r.equals("II")) {
               v3 = 1;
            }
            break;
         case 2349:
            if (r.equals("IV")) {
               v3 = 3;
            }
            break;
         case 2351:
            if (r.equals("IX")) {
               v3 = 8;
            }
            break;
         case 2739:
            if (r.equals("VI")) {
               v3 = 5;
            }
            break;
         case 2801:
            if (r.equals("XI")) {
               v3 = 10;
            }
            break;
         case 2814:
            if (r.equals("XV")) {
               v3 = 14;
            }
            break;
         case 72489:
            if (r.equals("III")) {
               v3 = 2;
            }
            break;
         case 84982:
            if (r.equals("VII")) {
               v3 = 6;
            }
            break;
         case 86904:
            if (r.equals("XII")) {
               v3 = 11;
            }
            break;
         case 86917:
            if (r.equals("XIV")) {
               v3 = 13;
            }
            break;
         case 2634515:
            if (r.equals("VIII")) {
               v3 = 7;
            }
            break;
         case 2694097:
            if (r.equals("XIII")) {
               v3 = 12;
            }
      }
      return switch (v3) {
         case 0 -> 1;
         case 1 -> 2;
         case 2 -> 3;
         case 3 -> 4;
         case 4 -> 5;
         case 5 -> 6;
         case 6 -> 7;
         case 7 -> 8;
         case 8 -> 9;
         case 9 -> 10;
         case 10 -> 11;
         case 11 -> 12;
         case 12 -> 13;
         case 13 -> 14;
         case 14 -> 15;
         default -> 0;
      };
   }

   private boolean m531(List<String> hand, List<String> slot) {
      if (hand.isEmpty()) {
         return true;
      } else if (slot.isEmpty()) {

         return false;
      } else {
         for (String v4 : hand) {
            String v5 = this.m532(v4);
            int v6 = this.m533(v4);
            boolean v7 = false;
            Iterator v8 = slot.iterator();

            while (v8.hasNext()) {

               String v9 = (String)v8.next();

               if ((this.m532(v9).contains(v5) || v5.contains(this.m532(v9))) && this.m533(v9) == v6) {
                  v7 = true;
                  break;
               }
            }

            if (!v7) {
               return false;
            }
         }

         return true;
      }
   }

   public AuctionHelper() {
      this.sellKey = new SettingVisibility(this, "Клавиша продажи");
      this.autoNextPage = new BooleanSetting(this, "Листать страницы", true);
      this.maxPagesSetting = new SliderSetting(this, "Макс. страниц", 50.0F, 1.0F, 200.0F, 1.0F).setVisible(() -> {
         Boolean result = this.autoNextPage.getValue();

         return result;
      });
      this.f565 = yh.zh.IDLE;
      this.f566 = new BlockUtil();
      this.f567 = "";
      this.f568 = 1;
      this.f569 = 0L;
      this.f570 = 0;
      this.f571 = new ArrayList<>();
      this.f572 = null;
      this.f573 = -1.0F;
      this.f574 = null;
      this.f575 = -1;
      this.f576 = 0;
   }

   private String m539(long n) {
      Object[] temp1 = new Object[1];

      temp1[0] = n;
      return String.format("%,d", temp1).replace((char)(44), (char)(46));
   }

   private float m536(ItemStack stack) {
      Integer v2 = (Integer)stack.method_58694(DataComponentTypes.field_49629);
      Integer v3 = (Integer)stack.method_58694(DataComponentTypes.field_50072);
      if (v2 != null) {

         if (v3 != null && v3 != 0) {
            return (float)(v3 - v2) / (float)v3.intValue();
         }
      }

      return 1.0F;
   }

   public static int getPrice(Slot slot) {
      if (!slot.method_7681()) {
         return 0;
      } else {
         LoreComponent v1 = (LoreComponent)slot.method_7677().method_57353().method_58694(DataComponentTypes.field_49632);
         if (v1 == null) {

            return 0;
         } else {
            Iterator v2 = v1.comp_2400().iterator();

            while (v2.hasNext()) {

               Text v3 = (Text)v2.next();
               String v4 = v3.getString();
               if (v4.contains("$") || v4.toLowerCase().contains("цена")) {
                  String v5 = v4.replaceAll("[^0-9]", "");
                  if (!v5.isEmpty()) {
                     try {
                        return Integer.parseInt(v5);
                     } catch (NumberFormatException v9) {
                     }
                  }
               }
            }

            return 0;
         }
      }
   }

   public static yh get() {

      return uq.get(yh.class);
   }

   private String m535(String name) {
      if (name != null && !name.isEmpty()) {
         name = name.replaceAll("§.", "");
         StringBuilder v2 = new StringBuilder();

         for (char v6 : name.toCharArray()) {
            if (Character.isLetterOrDigit(v6)
               || v6 == 32
               || v6 == 45
               || v6 == 39
               || v6 == 95) {
               v2.append(v6);
            }
         }

         String result = v2.toString()
            .trim()
            .replace(
               "Пузырёк опыта 15 Ур",
               "Пузырек с уровнем 15"
            )
            .replace(
               "Пузырёк опыта 30 Ур",
               "Пузырек с уровнем 30"
            )
            .replace(
               "Пузырёк опыта 45 Ур",
               "Пузырек с уровнем 45"
            )
            .replace(
               "Пузырёк опыта 50 Ур",
               "Пузырек с уровнем 50"
            )
            .replace("xxx", "")
            .replace("TNT - TIER WHITE", "Таер вайт")
            .replace("TNT - TIER BLACK", "Таер блэк")
            .replace("Динамит", "Динамит обычный")
            .replace(
               "Яйцо призыва крестьянина-зомби",
               "яйцо зомби-крестьянина"
            );

         return result.replace(
            "Снежок заморозка",
            "Снежок заморозка"
         );
      } else {
         return "";
      }
   }

   private int m538(GenericContainerScreenHandler handler, int containerSlots) {
      int v3 = containerSlots - (9);
      int v4 = v3;

      while (true) {

         if (v4 >= containerSlots) {
            return -1;
         }

         Slot v5 = handler.method_7611(v4);
         if (v5.method_7681()) {
            String v6 = v5.method_7677().method_7964().getString().replaceAll("§.", "");
            if (v6.contains("Следующая страница")) {
               return v4;
            }
         }

         v4++;
      }
   }

   private void m527() {
      ItemStack v1 = mc.field_1724.method_6047();
      if (v1.method_7960()) {
         this.m540("§cНет предмета!");
      } else {
         String temp1 = this.m535(v1.method_7964().getString());

         this.f567 = temp1;
         this.f568 = v1.method_7947();
         this.f574 = v1.method_7909();
         this.f571 = getEnchantNames(v1);
         ItemEnchantmentsComponent v2 = (ItemEnchantmentsComponent)v1.method_58694(DataComponentTypes.field_49633);
         this.f572 = v2 != null && !v2.method_57543() ? v2 : null;
         if (!this.f567.isEmpty()) {
            if (this.m537(v1)) {
               this.f573 = -1.0F;
            } else {
               float v3 = this.m536(v1);

               this.f573 = v3 < 0.999F ? v3 : -1.0F;
            }

            String v4 = this.f567;
            mc.field_1724.field_3944.method_45730("ah search " + v4);
            if (this.f572 != null || !this.f571.isEmpty()) {
               String v10 = this.f571.isEmpty()
                  ? "ванильные зачары"
                  : String.join(", ", this.f571);
               this.m541("§e[AH] Зачарования: §f" + v10);
            }

            if (this.f573 >= 0.0F) {
               int v6 = (int)(this.f573 * 100.0F);
               this.m541("§e[AH] Прочность: §f" + v6 + "%");
            }

            this.m540("§e[AH] Анализ цен...");
            this.f565 = yh.zh.WAITING_AH_OPEN;
            this.f566.reset();
            this.f570 = 0;
         }
      }
   }

   private static boolean m529(String s) {

      if (!s.contains("Острота")) {
         boolean result = s.contains("Эффективность");

         if (!result && !s.contains("Прочность")) {

            if (!s.contains("Удача")
               && !s.contains("Добыча")
               && !s.contains("Небесная кара")
               && !s.contains("Заговор огня")
               && !s.contains("Бич")
               && !s.contains("Разящий")
               && !s.contains("Вампиризм")
               && !s.contains("Окисление")
               && !s.contains("Яд")
               && !s.contains("Детекция")
               && !s.contains("Бульдозер")
               && !s.contains("Магнит")
               && !s.contains("Авто-Плавка")
               && !s.contains("Паутина")
               && !s.contains("Пингер")
               && !s.contains("Опытный")
               && !s.contains("Шёлковое")
               && !s.contains("Шелковое")
               && !s.contains("Подводная")
               && !s.contains("Защита")
               && !s.contains("Отделка")
               && !s.contains("Улучшение")
               && !s.contains("Шипы")
               && !s.contains("Легкость")
               && !s.contains("Книга")) {
               return false;
            }
         }
      }

      return true;
   }

   public void renderSlot(DrawContext context, Slot slot, int rank) {
      int v4 = switch (rank) {
         case 1 -> 1616248661;
         case 2 -> 1627389781;
         default -> 1627367936;
      };
      
      int v5 = switch (rank) {
         case 1 -> -16720640;
         case 2 -> -2237184;
         default -> -30720;
      };
      int v6 = slot.field_7873;
      int v7 = slot.field_7872;

      context.method_25294(v6, v7, v6 + 16, v7 + 16, v4);
      context.method_25294(v6, v7, v6 + 16, v7 + 1, v5);

      context.method_25294(v6, v7 + 15, v6 + 16, v7 + 16, v5);
      context.method_25294(v6, v7, v6 + 1, v7 + 16, v5);
      context.method_25294(v6 + 15, v7, v6 + 16, v7 + 16, v5);
   }

   @Override
   public void onDisable() {
      this.m542();
      super.onDisable();
   }

   private int m533(String s) {
      String v2 = s.trim();

      int v3 = v2.lastIndexOf(32);
      if (v3 == -1) {
         return 1;
      } else {
         String v4 = v2.substring(v3 + 1);
         int v5 = this.m534(v4);
         if (v5 > 0) {
            return v5;
         } else {
            try {
               return Integer.parseInt(v4);
            } catch (NumberFormatException v9) {
               return 1;
            }
         }
      }
   }

   private boolean m530(ItemEnchantmentsComponent hand, ItemEnchantmentsComponent slot) {
      if (slot != null && !slot.method_57543()) {
         Iterator v3 = hand.method_57534().iterator();

         RegistryEntry v4;
         do {
            boolean v7 = v3.hasNext();

            if (!v7) {
               return true;
            }

            v4 = (RegistryEntry)v3.next();
         } while (hand.method_57536(v4) == slot.method_57536(v4));

         return false;
      } else {
         return false;
      }
   }

   private void m526() {
      ItemStack v1 = mc.field_1724.method_6047();
      if (v1.method_7960()) {
         this.m540("§cНет предмета!");
      } else {
         String v2 = this.m535(v1.method_7964().getString());
         if (!v2.isEmpty()) {
            ClientPlayNetworkHandler result = mc.field_1724.field_3944;
            StringBuilder temp1 = new StringBuilder().append("ah search ");
            int temp3 = v5 * (v5 + 1) % 2;

            if (temp3 != 0) {
               }

            result.method_45730(temp1.append((Object)v2).toString());
            this.m540("§aПоиск: " + v2);
         }
      }
   }

   private void m540(String msg) {

      zp.addChatMessage(msg);
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null && this.f565 != yh.zh.IDLE) {
         switch (this.f565) {
            case READING_PRICES:
               if (++this.f570 > 10) {
                  this.m541("§c[AH] Аукцион не открылся.");
                  this.m542();
                  return;
               }

               if (mc.field_1755 instanceof GenericContainerScreen) {

                  if (this.f570 < 4) {
                     return;
                  }

                  this.f565 = yh.zh.READING_PRICES;
                  this.f570 = 0;
               }
               break;
            case SEND:
               if (!(mc.field_1755 instanceof GenericContainerScreen v28)) {
                  this.m542();
                  return;
               }

               GenericContainerScreenHandler v31 = (GenericContainerScreenHandler)v28.method_17577();
               int v4 = v31.method_17388() * (9);
               long v5 = -6802444138445811822L + -2420927898408963987L;
               float v8 = Float.MAX_VALUE;
               long v9 = 4769849654690355113L + 4453522382164420694L;

               for (; v11 < v4; v11++) {
                  Slot v12 = v31.method_7611(v11);
                  int v13 = getPrice(v12);

                  if (v13 > 0 && (this.f574 == null || v12.method_7677().method_7909() == this.f574)) {
                     if (this.f572 != null) {
                        ItemEnchantmentsComponent v14 = (ItemEnchantmentsComponent)v12.method_7677().method_58694(DataComponentTypes.field_49633);

                        if (!this.m530(this.f572, v14)) {
                           continue;
                        }
                     } else if (!this.f571.isEmpty()) {
                        try {
                           if (!this.m531(this.f571, getEnchantNames(v12.method_7677()))) {
                              continue;
                           }
                        } catch (Exception v27) {
                           continue;
                        }
                     }

                     long v34 = (long)v13 / (long)Math.max(v12.method_7677().method_7947(), 1);
                     if (this.f573 >= 0.0F) {
                        float v16 = this.m536(v12.method_7677());
                        float v17 = Math.abs(v16 - this.f573);
                        if (v17 < v8 || v17 == v8 && v34 < v9) {
                           v8 = v17;
                           v9 = v34;
                        }

                        if (v17 > 0.15F) {
                           continue;
                        }
                     }

                     v7++;
                     if (v34 < v5) {
                        v5 = v34;
                     }
                  }
               }

               if (v7 == 0 && this.f573 >= 0.0F && v9 != -4057263650177741955L + -5166108386677033854L) {
                  int v18 = (int)(v8 * 100.0F);
                  this.m541(
                     "§e[AH] Точной прочности нет, ближайшая ("
                        + v18
                        + "% разница)"
                  );
                  v5 = v9;
                  v7 = 1;
               }

               if (v7 == 0 || v5 == 6011801446562417254L + 3211570590292358553L) {
                  v11 = this.autoNextPage.getValue() ? this.m538(v31, v4) : -1;
                  if (v11 >= 0 && this.f576 < this.maxPagesSetting.getValue().intValue()) {
                     this.f575 = v11;
                     this.f576++;
                     this.f565 = yh.zh.NEXT_PAGE;
                     this.f570 = 0;
                     return;
                  }

                  int v19 = this.f576;
                  this.m541(
                     "§c[AH] Нет подходящих лотов (просмотрено страниц: "
                        + v19
                        + ")."
                  );
                  mc.field_1724.method_7346();
                  this.m542();
                  return;
               }

               long v32 = this.m528(v5 * (long)this.f568);
               this.f569 = v32;
               String v20 = this.m539(v32);
               this.m541(
                  "§a[AH] Нашёл "
                     + v7
                     + " лотов. Продаю за §l$"
                     + v20
               );
               mc.field_1724.method_7346();
               this.f565 = yh.zh.SEND;
               this.f570 = 0;
               break;
            case IDLE:
               if (!(mc.field_1755 instanceof GenericContainerScreen v2)) {
                  this.m542();
                  return;
               }

               if (this.f570 == 0) {
                  GenericContainerScreenHandler v29 = (GenericContainerScreenHandler)v2.method_17577();
                  mc.field_1761.method_2906(v29.field_7763, this.f575, 0, SlotActionType.field_7790, mc.field_1724);
               }

               if (++this.f570 >= 6) {
                  this.f565 = yh.zh.READING_PRICES;
                  this.f570 = 0;
               }
               break;
            case NEXT_PAGE:
               if (++this.f570 < 4) {
                  return;
               }

               if (this.f569 > 0L) {
                  long v22 = this.f569;
                  mc.field_1724.field_3944.method_45730("ah sell " + v22);
               }

               this.m542();
         }
      }
   }

   private String m532(String s) {
      String result = s.replaceAll("\\s+[IVX0-9]+$", "");

      return result.trim();
   }

   @Environment(EnvType.CLIENT)
   private static enum zh {
      WAITING_AH_OPEN,
      READING_PRICES,
      SEND,
      IDLE,
      NEXT_PAGE;
      // $VF: synthetic field
      private static final yh.zh[] $VALUES = m525();
   }
}
