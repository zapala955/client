package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.module.impl.player.AuctionHelper;
import ru.white.Client;
import ru.white.manager.InventoryPreset;
import ru.white.manager.InventoryPresetManager;

@Environment(EnvType.CLIENT)
@Mixin({HandledScreen.class})
public abstract class HandledScreenMixin {
   @Shadow
   @Final
   protected ScreenHandler field_2797;
   @Unique
   @Mutable
   private Slot ahSlot1 = null;
   @Unique
   @Mutable
   private Slot ahSlot2 = null;
   @Unique
   @Mutable
   private Slot ahSlot3 = null;

   @Shadow
   public abstract ScreenHandler method_17577();

   @Inject(
      method = {"method_25393"},
      at = {@At("HEAD")}
   )
   private void onTick(CallbackInfo ci) {
      yh mod = yh.get();
      if (mod != null && mod.isEnabled()) {
         MinecraftClient EventItemUse = MinecraftClient.method_1551();
         if (mc.field_1755 != null) {
            String title = mc.field_1755.method_25440().getString();
            if (!this.isAuctionScreen(title)) {
               this.ahSlot1 = this.ahSlot2 = this.ahSlot3 = null;
            } else {
               this.ahSlot1 = this.ahSlot2 = this.ahSlot3 = null;
               long p1 = Long.MAX_VALUE;
               long p2 = Long.MAX_VALUE;
               long p3 = Long.MAX_VALUE;

               for (Slot slot : this.method_17577().field_7761) {
                  if (slot.method_7681()) {
                     int total = yh.getPrice(slot);
                     if (total > 0) {
                        long perOne = (long)total / (long)Math.max(slot.method_7677().method_7947(), 1);
                        if (perOne < p1) {
                           p3 = p2;
                           this.ahSlot3 = this.ahSlot2;
                           p2 = p1;
                           this.ahSlot2 = this.ahSlot1;
                           p1 = perOne;
                           this.ahSlot1 = slot;
                        } else if (perOne < p2) {
                           p3 = p2;
                           this.ahSlot3 = this.ahSlot2;
                           p2 = perOne;
                           this.ahSlot2 = slot;
                        } else if (perOne < p3) {
                           p3 = perOne;
                           this.ahSlot3 = slot;
                        }
                     }
                  }
               }
            }
         }
      } else {
         this.ahSlot1 = this.ahSlot2 = this.ahSlot3 = null;
      }
   }

   @Inject(
      method = {"method_2385"},
      at = {@At("HEAD")}
   )
   private void onDrawSlot(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
      yh mod = yh.get();
      if (mod != null && mod.isEnabled()) {
         if (slot == this.ahSlot1) {
            mod.renderSlot(context, slot, 1);
         } else if (slot == this.ahSlot2) {
            mod.renderSlot(context, slot, 2);
         } else if (slot == this.ahSlot3) {
            mod.renderSlot(context, slot, 3);
         }
      }
   }

   @Inject(
      method = {"method_2385"},
      at = {@At("TAIL")}
   )
   private void onDrawPresetSlot(DrawContext context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
      if (this instanceof InventoryScreen && Client.get() != null && Client.get().inventoryPresetManager() != null) {
         InventoryPresetManager manager = Client.get().inventoryPresetManager();
         InventoryPreset preset = manager.activePreset().orElse(null);
         if (preset != null) {
            int logical = va.screenToLogicalSlot(slot.field_7874);
            if (logical >= 0) {
               ta.ua expected = preset.slot(logical);
               if (!expected.isEmpty()) {
                  va.ya state = manager.stateFor(logical);

                  int color = switch (state) {
                     case CORRECT -> -12400277;
                     case WRONG_SLOT -> -1787569;
                     case MISSING -> -2073503;
                     default -> 0;
                  };
                  if (state != va.ya.CORRECT) {
                     ItemStack ghost = expected.toStack();
                     if (!ghost.method_7960()) {
                        context.method_51427(ghost, slot.field_7873, slot.field_7872);
                        context.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 16, 1695156746);
                        context.method_51431(MinecraftClient.method_1551().field_1772, ghost, slot.field_7873, slot.field_7872);
                     }
                  }

                  if (color != 0) {
                     context.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 1, color);
                     context.method_25294(slot.field_7873, slot.field_7872 + 15, slot.field_7873 + 16, slot.field_7872 + 16, color);
                     context.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 1, slot.field_7872 + 16, color);
                     context.method_25294(slot.field_7873 + 15, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 16, color);
                  }
               }
            }
         }
      }
   }

   @Unique
   private boolean isAuctionScreen(String title) {
      return title.contains("Аукцион")
         || title.contains("Auction")
         || title.contains("Маркет")
         || title.contains("ꈁꀀꈂꌲꈂꀁ")
         || title.contains("Аукционы")
         || title.contains("Поиск:")
         || title.contains("Аукцион")
         || title.contains("Аукционы ")
         || title.equals("☃️ Аукцион")
         || title.startsWith("☃️ П:")
         || title.startsWith("☃️ Поиск:")
         || title.startsWith("0A2z")
         || title.contains("Поиск: ");
   }
}
