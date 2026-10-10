package ru.white.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Of;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Oq;
import ru.white.utils.MathUtil;
import ru.white.utils.EntityUtil;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Auto Swap",
   category = Category.COMBAT,
   desc = "Свап предметов по клавише."
)
@Environment(EnvType.CLIENT)
public class AutoSwap extends Module {
   private final EntityUtil f277;
   private boolean f276;
   private boolean f278;
   private final ModeSetting f269 = new ModeSetting(this, "Режим", "Двойной", "Тройной");
   private final BooleanSetting f273;
   private boolean f279;
   private final BooleanSetting f274;
   private int f280;
   private String f281;
   ItemStack[] tripleSlotItems;
   private final SettingVisibility bind;
   private final ModeSetting f271;
   private final BooleanSetting f272;
   private boolean f275;
   private final ModeSetting f270 = new ModeSetting(
         this,
         "Первый предмет",
         "Шар",
         "Золотое яблоко",
         "Щит",
         "Тотем"
      )
      .setVisible(() -> {

         Boolean result = this.f269.is("Двойной");

         return result;
      });

   private int m340(ItemStack target) {

      while (true) {

         if (v2 >= 36) {
            for (int v7 = 0; v7 < 36; v7++) {
               ItemStack v8 = mc.field_1724.method_31548().method_5438(v7);
               if (!v8.method_7960() && v8.method_7909() == target.method_7909()) {
                  return v7 < 9 ? v7 + 36 : v7;
               }
            }

            return -1;
         }

         ItemStack v3 = mc.field_1724.method_31548().method_5438(v2);
         if (ItemStack.method_31577(v3, target)) {

            return v2 < 9 ? v2 + 36 : v2;
         }

         v2++;
      }
   }

   public AutoSwap() {
      this.f271 = new ModeSetting(
            this,
            "Второй предмет",
            "Шар 2",
            "Золотое яблоко 2",
            "Щит 2",
            "Тотем 2"
         )
         .setVisible(() -> {
            int temp1 = v1 * (v1 + 1) % 2;
            int temp2 = v2 * (v2 + 1) % 2;

            if (temp2 != 0) {
               }

            if (temp1 != 0) {
               }

            return this.f269.is("Двойной");
         });
      this.bind = new SettingVisibility(this, "Клавиша свапа", -1);
      this.f272 = new BooleanSetting(this, "Уведомления о свапе", true);
      this.f273 = new BooleanSetting(this, "Только Зач. Тотем", false).setVisible(() -> {

         return this.f269.is("Двойной");
      });
      this.f274 = new BooleanSetting(this, "Анимации тройного", true).setVisible(() -> {
         ModeSetting result = this.f269;
         String temp1 = "РўСЂРѕР№РЅРѕР№";

         int v4 = v1 + 1;

         return result.is(temp1);
      });
      this.f277 = new EntityUtil();
      this.f280 = -1;
      this.f281 = "";
      this.tripleSlotItems = new ItemStack[]{null, null, null};
   }

   boolean isTripleAnimationsEnabled() {

      return this.f274.getValue();
   }

   @EventTarget
   public void input(EventPostMotion event) {
      if (mc.field_1755 == null && this.bind.get() != -1) {
         if (event.getKey() == this.bind.get()) {
            String result = this.f269.getValue();

            if (result.equals("Двойной")) {
               this.f275 = true;
            } else if (this.f269.getValue().equals("Тройной")) {
               MinecraftClient v4 = EventItemUse;
               Runnable temp1 = () -> {

                  mc.method_1507(new tg(this));
                  
               };

               v4.execute(temp1);
            }
         }
      }
   }

   private int m342(boolean onlyEnchanted) {
      if (mc.field_1724 == null) {
         return -1;
      } else {

         while (v2 < 36) {
            ItemStack v3 = mc.field_1724.method_31548().method_5438(v2);
            if (v3.method_7909() == Items.field_8288 && v3.method_7958()) {
               int v10;
               if (v2 < 9) {

                  v10 = v2 + 36;
               } else {
                  v10 = v2;
               }

               return v10;
            }

            v2++;
            
         }

         if (!onlyEnchanted) {
            v2 = 0;

            while (true) {
               int temp1 = 36;

               if (v2 >= temp1) {
                  break;
               }

               ItemStack v8 = mc.field_1724.method_31548().method_5438(v2);
               if (v8.method_7909() == Items.field_8288) {
                  return v2 < 9 ? v2 + 36 : v2;
               }

               v2++;
            }
         }

         return -1;
      }
   }

   private void m341(Item item, String itemName, boolean onlyEnchanted) {
      int v4 = item == Items.field_8288 ? this.m342(onlyEnchanted) : dq.find(item);

      if (v4 != -1) {
         this.m343(false);
         fg.stoptick = 3;
         this.f278 = true;
         this.f279 = false;
         this.f280 = v4;
         this.f281 = itemName;
         this.f277.reset();
      }

      this.f275 = false;
   }

   @Override
   public void onDisable() {

      label18: {
         if (!(mc.field_1755 instanceof tg)) {

            if (!(mc.field_1755 instanceof sg)) {
               break label18;
            }
         }

         mc.execute(() -> {

            mc.method_1507(null);
         });
      }

      super.onDisable();
   }

   private boolean m339() {
      int v1 = this.bind.get();
      if (v1 == -1) {
         return false;
      } else {
         long v2 = mc.method_22683().method_4490();
         if (v1 >= 0 && v1 <= 7) {
            int result = GLFW.glfwGetMouseButton(v2, v1);

            return result == 1;
         } else {
            return InputUtil.method_15987(mc.method_22683(), v1);
         }
      }
   }

   void executeTripleSwap(int slotIndex) {
      if (mc.field_1724 != null) {
         ItemStack v2 = this.tripleSlotItems[slotIndex];
         if (v2 != null && !v2.method_7960()) {
            int v3 = this.m340(v2);
            if (v3 != -1) {
               this.m343(false);
               fg.stoptick = 3;
               this.f278 = true;
               this.f279 = false;
               this.f280 = v3;
               this.f281 = v2.method_7964().getString();
               this.f277.reset();

               this.f275 = false;
            }
         }
      }
   }

   private void m343(boolean state) {
      KeyBinding[] result = new KeyBinding[]{mc.field_1690.field_1894, mc.field_1690.field_1881, null, null, null};

      result[2] = mc.field_1690.field_1913;
      result[3] = mc.field_1690.field_1849;
      result[4] = mc.field_1690.field_1903;
      KeyBinding[] v2 = result;

      for (KeyBinding v6 : v2) {
         label29: {
            if (state) {

               if (InputUtil.method_15987(mc.method_22683(), v6.method_1429().method_1444())) {
                  v11 = true;
                  break label29;
               }
            }

            v11 = false;
         }

         boolean v7 = v11;
         v6.method_23481(v7);
      }
   }

   @EventTarget
   public void update(EventRenderModel event) {
      if (!mc.field_1724.method_5805()) {
         this.m343(true);
      }

      if (this.f269.getValue().equals("Тройной") && mc.field_1755 instanceof tg v9) {
         if (!this.m339() && !v9.isSelectionAnimating()) {
            int v11 = v9.getHoveredSlot();
            if (this.f274.getValue()) {
               if (v11 < 0) {
                  v9.startCloseAnimation(null);
                  return;
               }

               v9.startSelectionAnimation(v11, () -> {

                  this.executeTripleSwap(v11);
               });
            } else {
               mc.method_1507(null);
               if (v11 >= 0) {
                  this.executeTripleSwap(v11);
               }
            }
         }
      } else {

         ScreenHandler v2 = mc.field_1724.field_7512;
         if (this.f278) {
            if (!this.f279 && this.f280 != -1 && !mc.field_1724.method_5624()) {
               int v10 = this.f280 >= 36 ? this.f280 - (36) : this.f280;
               ItemStack v4 = mc.field_1724.method_31548().method_5438(v10).method_7972();
               mc.field_1761
                  .method_2906(
                     v2.field_7763,
                     this.f280 < 9 ? this.f280 + 36 : this.f280,
                     40,
                     SlotActionType.field_7791,
                     mc.field_1724
                  );
               mc.field_1724.field_3944.method_52787(new CloseHandledScreenC2SPacket(v2.field_7763));
               this.m343(true);
               if (this.f272.getValue()) {
                  String v5 = !this.f281.isEmpty() ? this.f281 : (v4.method_7960() ? "предмет" : v4.method_7964().getString());
                  if (!v4.method_7960()) {
                     oq.send("Свапнул на " + v5, oq.qq.INFO, v4.method_7972(), 7765787465695256048L + -7765787465695254048L);
                  } else {
                     oq.send("Свапнул на " + v5, oq.qq.INFO, -8974361425459572794L + 8974361425459574794L);
                  }
               }

               this.f279 = true;
            }

            if (this.f279 || this.f277.hasTimeElapsed(-4197868789862846742L + 4197868789862847242L)) {
               this.f278 = false;
               this.f279 = false;
               this.f280 = -1;
               this.f281 = "";
            }
         } else {
            if (this.f269.getValue().equals("Двойной")) {
               if (this.f275 && this.f276) {
                  if (this.f270.getValue().equals("Шар")) {
                     this.m341(Items.field_8575, "Шар", false);
                  } else if (this.f270.getValue().equals("Тотем")) {
                     this.m341(Items.field_8288, "Тотем", this.f273.getValue());
                  } else if (this.f270.getValue().equals("Золотое яблоко")) {
                     this.m341(Items.field_8463, "Золотое яблоко", false);
                  } else if (this.f270.getValue().equals("Щит")) {
                     this.m341(Items.field_8255, "Щит", false);
                  }

                  this.f276 = false;
               }

               if (this.f275) {
                  if (this.f271.getValue().equals("Шар 2")) {
                     this.m341(Items.field_8575, "Шар", false);
                  } else if (this.f271.getValue().equals("Золотое яблоко 2")) {
                     this.m341(Items.field_8463, "Золотое яблоко", false);
                  } else if (this.f271.getValue().equals("Тотем 2")) {
                     this.m341(Items.field_8288, "Тотем", this.f273.getValue());
                  } else if (this.f271.getValue().equals("Щит 2")) {
                     this.m341(Items.field_8255, "Щит", false);
                  }

                  this.f276 = true;
               }
            }
         }
      }
   }

   
}
