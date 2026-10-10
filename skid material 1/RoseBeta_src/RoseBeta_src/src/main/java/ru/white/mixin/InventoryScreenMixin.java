package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.module.impl.player.LockSlot;
import ru.white.Client;

@Environment(EnvType.CLIENT)
@Mixin({InventoryScreen.class})
public class InventoryScreenMixin {
   @Unique
   private static final int BTN_W = 52;
   @Unique
   private static final int BTN_H = 12;
   @Unique
   private static final int BTN_OFFSET_X = 3;
   @Unique
   private static final int BTN_OFFSET_Y = -15;
   @Unique
   private int btnX;
   @Unique
   private int btnY;
   @Unique
   private boolean hovered = false;

   @Inject(
      method = {"method_25394"},
      at = {@At("TAIL")}
   )
   private void onRender(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      InventoryScreen self = (InventoryScreen)this;
      MinecraftClient EventItemUse = MinecraftClient.method_1551();
      if (mc.field_1724 != null) {
         int screenX = ((HandledScreenAccessor)self).getScreenX();
         int screenY = ((HandledScreenAccessor)self).getScreenY();
         this.btnX = screenX + 3;
         this.btnY = screenY + -15;
         this.hovered = mouseX >= this.btnX && mouseX <= this.btnX + 52 && mouseY >= this.btnY && mouseY <= this.btnY + 12;
         int SliderSetting = this.hovered ? -3394765 : -7855582;
         ctx.method_25294(this.btnX, this.btnY, this.btnX + 52, this.btnY + 12, SliderSetting);
         String label = "Выкинуть всё";
         int textW = mc.field_1772.method_1727(label);
         int textX = this.btnX + (52 - textW) / 2;
         int textY = this.btnY + (12 - 9) / 2 + 1;
         ctx.method_51433(mc.field_1772, label, textX, textY, -1, false);
      }
   }

   @Inject(
      method = {"method_25406"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMouseClicked(Click click, CallbackInfoReturnable<Boolean> cir) {
      if (click.method_74245() == 0) {
         if (this.hovered) {
            MinecraftClient EventItemUse = MinecraftClient.method_1551();
            if (mc.field_1724 != null && mc.field_1761 != null) {
               int syncId = mc.field_1724.field_7512.field_7763;
               int slotCount = mc.field_1724.field_7512.field_7761.size();
               li lockSlot = Client.get().moduleManager().get(li.class);

               for (int i = 0; i < slotCount; i++) {
                  if (((Slot)mc.field_1724.field_7512.field_7761.get(i)).method_7681()
                     && (lockSlot == null || !lockSlot.isDropProtected(((Slot)mc.field_1724.field_7512.field_7761.get(i)).method_7677(), toInventorySlot(i)))) {
                     mc.field_1761.method_2906(syncId, i, 1, SlotActionType.field_7795, mc.field_1724);
                  }
               }

               cir.setReturnValue(true);
               cir.cancel();
            }
         }
      }
   }

   @Unique
   private static int toInventorySlot(int screenSlot) {
      if (screenSlot >= 36 && screenSlot <= 44) {
         return screenSlot - 36;
      } else {
         return screenSlot >= 9 && screenSlot <= 35 ? screenSlot : -1;
      }
   }
}
