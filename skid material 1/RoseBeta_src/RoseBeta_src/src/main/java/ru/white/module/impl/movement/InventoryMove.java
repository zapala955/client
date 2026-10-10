package ru.white.module.impl.movement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import ru.white.event.EventTarget;
import ru.white.utils.StopWatch;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.module.impl.combat.sg;
import ru.white.module.impl.combat.tg;
import ru.white.screen.DropDownMenu;
import ru.white.screen.TestGui;
import ru.white.screen.ClickGuiScreen;
import ru.white.module.impl.misc.ReportHelper;
import ru.white.event.impl.EventSound;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Inventory Move",
   desc = "Позволяет двигаться с открытым инвентарём",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class InventoryMove extends Module {
   private boolean f634;
   private long f631;
   private int f632;
   private final Set<Packet<?>> f627;
   public final ModeSetting mode = new ModeSetting(this, "Тип", "FunTime", "Vanilla");
   private boolean f630;
   public boolean stop;
   private final StopWatch f629;
   private boolean f633;
   private final Queue<ClickSlotC2SPacket> f628;
   public int ticksPostOnStop;
   private final List<Packet<?>> f626 = new ArrayList<>();

   private void m622(SlotActionType actionType) {
      int temp1 = this.m623(actionType == null ? SlotActionType.field_7790 : actionType);

      this.f632 = temp1 + 1;
   }

   public boolean isMoving() {
      if (mc.field_1724 == null) {

         return false;
      } else {
         if (mc.field_1724.field_3913.method_3128().field_1342 == 0.0F) {
            int temp3 = v2 + 1;

            if (mc.field_1724.field_3913.method_3128().field_1343 == 0.0F) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean m621() {
      if (!this.isMoving()) {
         boolean result = mc.field_1690.field_1903.method_1434();

         if (!result && !this.f633) {

            return false;
         }
      }

      return true;
   }

   @EventTarget
   public void onClose(EventSound e) {
      if (!nm.isAutoListActive()) {
         if (mc.field_1755 instanceof InventoryScreen || mc.field_1755 instanceof GenericContainerScreen) {
            if (this.mode.is("FunTime")) {
               e.cancel();
               this.f629.reset();
               this.f631 = System.currentTimeMillis() + 2216553848686161437L + -2216553848686161377L;
               this.f630 = true;
            }

            if (this.mode.is("FunTime_2")) {

               e.cancel();
               this.f629.reset();

               this.f631 = System.currentTimeMillis() + -3028294037087761318L + 3028294037087761368L;
               this.f630 = true;
            }
         }
      }
   }

   @Override
   public void onEnable() {
      this.m624();
      this.f628.clear();

      this.ticksPostOnStop = 1;
   }

   private void m618() {
      KeyBinding[] v1 = new KeyBinding[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      };
      if (nm.isAutoListActive()) {
         this.m620(false);
      } else if (!this.f629.isReached(1203825523525612491L + -1203825523525612431L)) {
         this.m620(false);
      } else if (this.mode.is("FunTime_2") && this.f633) {
         this.m620(false);
      } else {
         if (!(mc.field_1755 instanceof ChatScreen)
            && (
               mc.field_1755 instanceof InventoryScreen
                  || mc.field_1755 instanceof tg
                  || mc.field_1755 instanceof ko && !ko.searchActive
                  || mc.field_1755 instanceof DropDownMenu
                  || mc.field_1755 instanceof sg
                  || mc.field_1755 instanceof GenericContainerScreen
            )) {
            this.m619(v1);
         }
      }
   }

   private void m619(KeyBinding[] keyBindings) {
      for (KeyBinding v5 : keyBindings) {
         int result = v5.method_1429().method_1444();

         int v6 = result;
         boolean v7 = InputUtil.method_15987(mc.method_22683(), v6);
         v5.method_23481(v7);
      }
   }

   private int m623(SlotActionType actionType) {

      return 1;
   }

   @Override
   public void onDisable() {
      this.stop = false;
      this.f630 = false;
      this.m624();
      this.ticksPostOnStop = 1;

      this.f626.clear();
      this.f628.clear();
      this.f627.clear();

      super.onDisable();
   }

   private void m624() {
      if (!this.f628.isEmpty()) {
         while (!this.f628.isEmpty()) {
            ClickSlotC2SPacket v1 = this.f628.poll();
            if (v1 != null) {
               this.f627.add(v1);
               ClientPlayNetworkHandler result = mc.method_1562();

               result.method_48296().method_10752(v1, null);
            }
         }

         this.ticksPostOnStop = 0;
      }
   }

   private void m620(boolean state) {
      KeyBinding[] v2 = new KeyBinding[]{
         mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
      };

      for (KeyBinding v6 : v2) {
         boolean v7 = state && InputUtil.method_15987(mc.method_22683(), v6.method_1429().method_1444());
         v6.method_23481(v7);
      }
   }

   public InventoryMove() {
      this.f627 = new HashSet<>();
      this.f628 = new LinkedList<>();
      this.f629 = new StopWatch();
      this.stop = false;
      this.f630 = false;
      this.f631 = 0L;
      this.ticksPostOnStop = 1;
   }

   @EventTarget
   public void onPacket(EventStep e) {
      if (!(e.getPacket() instanceof CraftRequestC2SPacket)) {
         if (!nm.isAutoListActive()) {
            if (e.getPacket() instanceof CloseScreenS2CPacket) {
               if (this.mode.is("FunTime_2")) {
                  e.cancel();
               }
            } else {
               Packet v3 = e.getPacket();
               boolean result = v3 instanceof ClickSlotC2SPacket;

               if (result) {
                  ClickSlotC2SPacket v2 = (ClickSlotC2SPacket)v3;
                  if (this.f627.contains(v2)) {
                     this.f627.remove(v2);
                  } else {
                     boolean v5 = mc.field_1755 instanceof InventoryScreen || mc.field_1755 instanceof GenericContainerScreen;
                     if (e.isSend() && v5) {
                        if (this.mode.is("FunTime_2") && this.m621() && v2.comp_3844() != -1) {
                           this.m622(v2.comp_3846());
                           if (!this.f633) {
                              if (!this.f628.contains(v2)) {
                                 this.f628.add(v2);
                              }

                              this.ticksPostOnStop = 0;
                              e.cancel();
                           }
                        }

                        if (this.mode.is("FunTime") && (this.isMoving() || mc.field_1690.field_1903.method_1434())) {
                           this.f626.add(v2);
                           e.cancel();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null) {
         if (this.mode.is("Vanilla") && !nm.isAutoListActive()) {
            KeyBinding[] v2 = new KeyBinding[]{
               mc.field_1690.field_1894, mc.field_1690.field_1881, mc.field_1690.field_1913, mc.field_1690.field_1849, mc.field_1690.field_1903
            };
            label81:
            if (!(mc.field_1755 instanceof ChatScreen)) {
               if (!(mc.field_1755 instanceof InventoryScreen) && !(mc.field_1755 instanceof tg)) {

                  if ((!(mc.field_1755 instanceof ko) || ko.searchActive)
                     && !(mc.field_1755 instanceof DropDownMenu)
                     && (!(mc.field_1755 instanceof TestGui v3) || v3.searchActive)
                     && !(mc.field_1755 instanceof sg)
                     && !(mc.field_1755 instanceof GenericContainerScreen)) {
                     break label81;
                  }
               }

               this.m619(v2);
            }
         }

         if (this.mode.is("FunTime_2")) {
            if (this.f634 && this.f633 && this.f632 > 0) {
               this.m624();
            }

            this.f634 = this.f633;
            if (this.f632 > 0) {
               this.f632--;
            }

            this.f633 = this.f632 > 0;
            this.ticksPostOnStop++;
            if (this.f633) {
               this.ticksPostOnStop = 0;
            }
         }

         if (this.f630 && System.currentTimeMillis() >= this.f631) {
            this.f630 = false;

            for (Packet v7 : this.f626) {
               this.f627.add(v7);
               mc.method_1562().method_48296().method_10752(v7, null);
            }

            this.f626.clear();
            if (mc.field_1724 != null) {
               mc.execute(() -> {
                  if (mc.field_1724 != null) {
                     this.m620(true);
                     mc.field_1724.method_7346();
                     
                  }
               });
            }
         }

         this.m618();
      }
   }

   
}
