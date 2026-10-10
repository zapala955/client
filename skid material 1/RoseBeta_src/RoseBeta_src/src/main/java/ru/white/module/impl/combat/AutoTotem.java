package ru.white.module.impl.combat;

import java.util.Iterator;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.item.ShieldItem;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.module.impl.movement.Sprint;
import ru.white.utils.misc.Oq;
import ru.white.utils.TimerUtil;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Auto Totem",
   desc = "Автоматически держит тотем в руке",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class AutoTotem extends Module {
   private final SliderSetting f289;
   private ItemStack f293;
   private final BooleanSetting f284;
   private final SliderSetting f290;
   private final BooleanSetting f283;
   private int f296;
   private final BooleanSetting f282 = new BooleanSetting(this, "Здоровье с элитрами", true);
   private final SliderSetting f287;
   private boolean f295;
   private int f292;
   private final BooleanSetting f285;
   private final TimerUtil f294;
   private final BooleanSetting f286;
   private final SliderSetting f288;
   int nonEnchantedTotems;
   private final BooleanSetting f291;

   public AutoTotem() {
      this.f283 = new BooleanSetting(this, "Динамит", true);
      this.f284 = new BooleanSetting(this, "Падение", false);
      this.f285 = new BooleanSetting(this, "Эндер-кристалл", false);
      this.f286 = new BooleanSetting("Не свап при еде", true);
      this.f287 = new SliderSetting(this, "Здоровье", 4.0F, 1.0F, 20.0F, 0.5F);
      this.f288 = new SliderSetting(this, "Здоровье на элитре", 9.0F, 0.0F, 20.0F, 0.5F)
         .setVisible(() -> {

            return this.f282.getValue();
         });
      this.f289 = new SliderSetting(this, "Дистанция до крист", 4.0F, 1.0F, 10.0F, 1.0F)
         .setVisible(() -> {

            return this.f285.getValue();
         });
      this.f290 = new SliderSetting(this, "Дистанция до тнт", 30.0F, 3.0F, 50.0F, 1.0F).setVisible(() -> {

         return this.f283.getValue();
      });
      this.f291 = new BooleanSetting(this, "Не свапать если шар", false);
      this.f292 = -1;
      this.f293 = ItemStack.field_8037;
      this.f294 = new TimerUtil();
      this.f296 = -1;
   }

   private void m354() {
      this.f293 = ItemStack.field_8037;

      this.f292 = -1;
      this.f296 = -1;
   }

   private boolean m360() {
      if (!this.f285.getValue()) {
         return false;
      } else {
         Iterator result = mc.field_1687.method_18112().iterator();

         Iterator v1 = result;

         while (v1.hasNext()) {
            Entity v2 = (Entity)v1.next();
            if (v2 instanceof EndCrystalEntity && mc.field_1724.method_5739(v2) <= this.f289.getValue()) {
               return !(mc.field_1724.method_6079().method_7909() instanceof PlayerHeadItem) || !this.f291.getValue();
            }
         }

         return false;
      }
   }

   private float m362() {

      return mc.field_1724.method_6067();
   }

   private boolean m351() {

      boolean result;
      if (mc.field_1724.method_6115() && this.f286.getValue() && !(mc.field_1724.method_6030().method_7909() instanceof ShieldItem)) {

         result = true;
      } else {
         result = false;
      }

      return result;
   }

   private void m363(boolean state) {
      KeyBinding[] v2 = new KeyBinding[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      };
      Window result = mc.method_22683();

      long v3 = result.method_4490();

      for (KeyBinding v8 : v2) {
         boolean v9 = state && InputUtil.method_15987(mc.method_22683(), v8.method_1429().method_1444());
         v8.method_23481(v9);
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();

      if (this.f295) {
         this.f295 = false;
      }

      this.m354();
   }

   private boolean m359() {
      boolean v2;
      if (mc.field_1724.method_6032() + this.m362() <= this.f288.getValue()) {

         v2 = true;
      } else {
         v2 = false;
      }

      return v2;
   }

   @EventTarget
   public void update(EventTick eventUpdate) {
      if (mc.field_1724 != null && mc.field_1724.method_5805() && mc.field_1687 != null) {
         this.nonEnchantedTotems = (int)IntStream.range(0, 36).mapToObj(i -> {
            ItemStack result = mc.field_1724.method_31548().method_5438(i);

            return result;
         }).filter(s -> s.method_7909() == Items.field_8288 && !s.method_7958()).count();

         if (!this.m351()) {
            this.m352();
         }
      } else {
         if (this.f295) {
            this.f295 = false;
            this.m354();
         }
      }
   }

   private boolean m356() {
      boolean v1 = this.m357();
      boolean v2 = this.m360();
      boolean v3 = this.m361();
      boolean v4 = this.m358();
      float result = mc.field_1724.method_6032() + this.m362();

      boolean v5 = result <= this.f287.getValue();
      if (!v1 && !v2 && !v3) {

         if (!v4 && !v5) {
            return false;
         }
      }

      return true;
   }

   private boolean m357() {

      ItemStack v1 = mc.field_1724.method_6118(EquipmentSlot.field_6174);
      boolean v2 = v1.method_7909() == Items.field_8833 && this.f282.getValue();

      return v2 && this.m359();
   }

   private void m353(int slot, int button) {
      mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, slot, button, SlotActionType.field_7791, mc.field_1724);
      ClientPlayNetworkHandler result = mc.field_1724.field_3944;
      CloseHandledScreenC2SPacket temp1 = new CloseHandledScreenC2SPacket(mc.field_1724.field_7498.field_7763);

      result.method_52787(temp1);
   }

   private boolean m361() {
      int temp2 = v4 + 1;

      if (!this.f283.getValue()) {
         return false;
      } else {
         for (Entity v2 : mc.field_1687.method_18112()) {
            float v3 = mc.field_1724.method_5739(v2);
            if ((v2 instanceof TntEntity || v2 instanceof TntMinecartEntity) && v3 <= this.f290.getValue()) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean m358() {
      if (!this.f284.getValue()) {

         return false;
      } else {
         return !mc.field_1724.method_24828() && mc.field_1724.method_18798().field_1351 < -0.8F;
      }
   }

   private int m355() {
      for (int v1 = 0; v1 < 36; v1++) {
         ItemStack v2 = mc.field_1724.method_31548().method_5438(v1);
         if (v2.method_7909() == Items.field_8288 && !v2.method_7958()) {
            int temp1 = 9;

            return v1 < temp1 ? v1 + 36 : v1;
         }
      }

      for (int v4 = 0; v4 < 36; v4++) {
         ItemStack v5 = mc.field_1724.method_31548().method_5438(v4);
         if (v5.method_7909() == Items.field_8288) {
            return v4 < 9 ? v4 + 36 : v4;
         }
      }

      return -1;
   }

   public boolean isTotemInHands() {
      ItemStack v1 = mc.field_1724.method_6047();
      ItemStack v2 = mc.field_1724.method_6079();
      if (v1.method_7909() == Items.field_8288) {
         boolean result = v1.method_7958();

         return !result || this.nonEnchantedTotems <= 0;
      } else {
         return v2.method_7909() != Items.field_8288 ? false : !v2.method_7958() || this.nonEnchantedTotems <= 0;
      }
   }

   private void m352() {
      int v1 = this.m355();
      boolean v2 = this.isTotemInHands();
      if (this.m356()) {
         if (v1 >= 0 && !v2) {
            if (!this.f295) {
               this.m363(false);
               fg.stoptick = 3;
               xh.get().tick++;
               this.f295 = true;
               this.f294.reset();
            }

            if (mc.field_1755 == null && !mc.field_1724.method_5624()) {
               if (this.f293.method_7960() && !mc.field_1724.method_6079().method_7960()) {
                  ItemStack temp1 = mc.field_1724.method_6079();

                  this.f293 = temp1.method_7972();
                  this.f292 = v1;
               }

               if (v1 != this.f296) {
                  this.f296 = v1;
                  int v3 = v1 >= 36 ? v1 - (36) : v1;
                  ItemStack v4 = mc.field_1724.method_31548().method_5438(v3).method_7972();
                  if (!v4.method_7960()) {
                     oq.send(
                        "Тотем подложен",
                        oq.qq.MODULE,
                        v4,
                        3334013406802573236L + -3334013406802571236L
                     );
                  }
               }

               this.m353(v1, 40);
               this.f295 = false;
               this.m363(true);
               return;
            }

            if (this.f295 && this.f294.isReached(-4926586438064100337L + 4926586438064100587L)) {
               this.f295 = false;
               this.m363(true);
               this.m354();
            }
         }
      } else if (this.f292 != -1 && !this.f293.method_7960()) {
         if (mc.field_1724.method_6079().method_31574(Items.field_8288) && !this.m356()) {
            if (!this.f295) {
               this.m363(false);
               fg.stoptick = 3;
               xh.get().tick++;
               this.f295 = true;
               this.f294.reset();
            }

            if (mc.field_1755 == null && !mc.field_1724.method_5624()) {
               this.m353(this.f292, 40);
               this.f295 = false;
               this.m354();
               this.m363(true);
               return;
            }

            if (this.f295 && this.f294.isReached(5790026608884528038L + -5790026608884527788L)) {
               this.f295 = false;
               this.m363(true);
               this.m354();
            }
         } else {
            this.m353(this.f292, 40);
            this.m354();
         }
      } else {
         this.m354();
      }
   }

   
}
