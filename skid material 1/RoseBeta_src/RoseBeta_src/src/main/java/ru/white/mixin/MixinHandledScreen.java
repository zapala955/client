package ru.white.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.module.impl.player.LockSlot;
import ru.white.Client;
import ru.white.screen.GuiModeButton;
import ru.white.manager.InventoryPresetManager;
import ru.white.module.impl.misc.ItemScroller;

@Environment(EnvType.CLIENT)
@Mixin({HandledScreen.class})
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen {
   @Shadow
   @Final
   protected T field_2797;
   @Shadow
   @Nullable
   protected Slot field_2787;
   private long time = 0L;
   @Unique
   private static final int PRESET_BUTTON_SIZE = 18;
   @Unique
   private ButtonWidget presetButton;

   @Shadow
   protected abstract void method_2383(Slot v1, int v2, int v3, SlotActionType v4);

   protected MixinHandledScreen(Text title) {
      super(title);
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("TAIL")}
   )
   private void initPresetButton(CallbackInfo ci) {
      if (this instanceof InventoryScreen && this.field_22787 != null) {
         HandledScreenAccessor accessor = (HandledScreenAccessor)this;
         this.presetButton = ButtonWidget.method_46430(Text.method_43473(), button -> this.field_22787.method_1507(new io(this)))
            .method_46434(accessor.getScreenX() + 151, accessor.getScreenY() - 20, 18, 18)
            .method_46431();
         this.method_37063(this.presetButton);
      }
   }

   @Inject(
      method = {"method_25402"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void clickPresetUi(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
      if (this instanceof InventoryScreen && this.field_22787 != null && click.method_74245() == 0 && Client.get() != null) {
         InventoryPresetManager manager = Client.get().inventoryPresetManager();
         if (manager != null && !manager.activePreset().isEmpty()) {
            HandledScreenAccessor accessor = (HandledScreenAccessor)this;
            int guiX = accessor.getScreenX();
            int guiY = accessor.getScreenY();
            double mouseX = click.comp_4798();
            double mouseY = click.comp_4799();
            Map<String, va.xa> missing = manager.missingItems();
            List<va.xa> missingItems = new ArrayList<>(missing.values());
            int visibleRows = Math.min(8, Math.max(1, missingItems.size()));
            int missingScroll = manager.missingScroll(missingItems.size(), visibleRows);
            int panelX = guiX + 182;
            int panelY = guiY + 8;
            int panelW = 118;
            int rowH = 21;
            int rowY = panelY + 25;

            for (int index = missingScroll; index < Math.min(missingItems.size(), missingScroll + visibleRows); index++) {
               va.xa item = missingItems.get(index);
               if (inside(mouseX, mouseY, panelX + 4, rowY, panelW - 8, rowH - 2)) {
                  manager.searchAuction(item);
                  cir.setReturnValue(true);
                  return;
               }

               rowY += rowH;
            }

            if (missing.isEmpty()) {
               rowY += rowH;
            }

            int panelH = 31 + visibleRows * rowH + 27;
            int arrangeY = panelY + panelH - 23;
            if (inside(mouseX, mouseY, panelX + 5, arrangeY, panelW - 10, 18)) {
               if (manager.isArranging()) {
                  manager.cancelArrange(true);
               } else {
                  manager.startArrange();
               }

               cir.setReturnValue(true);
            }
         }
      }
   }

   @Inject(
      method = {"method_25401"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void scrollPresetMissing(double mouseX, double mouseY, double horizontal, double vertical, CallbackInfoReturnable<Boolean> cir) {
      if (this instanceof InventoryScreen && Client.get() != null) {
         InventoryPresetManager manager = Client.get().inventoryPresetManager();
         if (manager != null && !manager.activePreset().isEmpty()) {
            int size = manager.missingItems().size();
            if (size > 8) {
               HandledScreenAccessor accessor = (HandledScreenAccessor)this;
               int panelX = accessor.getScreenX() + 182;
               int panelY = accessor.getScreenY() + 8;
               int panelH = 226;
               if (inside(mouseX, mouseY, panelX, panelY, 118, panelH)) {
                  manager.scrollMissing(vertical < 0.0 ? 1 : -1, size, 8);
                  cir.setReturnValue(true);
               }
            }
         }
      }
   }

   @Unique
   private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
      return mouseX >= (double)x && mouseX < (double)(x + w) && mouseY >= (double)y && mouseY < (double)(y + h);
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")}
   )
   private void onRender(CallbackInfo ci) {
      km itemScroller = km.getInstance();
      if (this.field_22787 != null && itemScroller.isEnabled()) {
         boolean isShiftDown = this.hasShiftDown();
         if (GLFW.glfwGetMouseButton(this.field_22787.method_22683().method_4490(), 0) == 1
            && isShiftDown
            && this.field_2787 != null
            && this.field_2787.method_7681()
            && (float)(System.currentTimeMillis() - this.time) > itemScroller.delay.getValue()) {
            this.method_2383(this.field_2787, this.field_2787.field_7874, 0, SlotActionType.field_7794);
            this.time = System.currentTimeMillis();
         }
      }
   }

   @Unique
   private boolean hasShiftDown() {
      return InputUtil.method_15987(MinecraftClient.method_1551().method_22683(), 340)
         || InputUtil.method_15987(MinecraftClient.method_1551().method_22683(), 344);
   }

   @Unique
   private boolean hasControlDown() {
      return InputUtil.method_15987(MinecraftClient.method_1551().method_22683(), 341)
         || InputUtil.method_15987(MinecraftClient.method_1551().method_22683(), 345);
   }

   @Inject(
      method = {"method_25404"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
      if (this.field_22787 != null && this.field_22787.field_1724 != null) {
         li lockSlot = Client.get().moduleManager().get(li.class);
         if (this.field_22787.field_1690.field_1869.method_1417(input)
            && this.field_2787 != null
            && this.field_2787.method_7681()
            && lockSlot != null
            && lockSlot.isDropProtected(this.field_2787.method_7677(), this.toInventorySlot(this.field_2787.field_7874))) {
            cir.setReturnValue(true);
            cir.cancel();
         } else {
            km itemScroller = km.getInstance();
            if (itemScroller != null && itemScroller.isEnabled()) {
               if (this.field_22787.field_1690.field_1869.method_1417(input)
                  && this.hasControlDown()
                  && this.hasShiftDown()
                  && this.field_2787 != null
                  && this.field_2787.method_7681()) {
                  ItemStack hoveredStack = this.field_2787.method_7677();
                  Item itemToDrop = hoveredStack.method_7909();

                  for (Slot slot : this.field_2797.field_7761) {
                     int inventorySlot = this.toInventorySlot(slot.field_7874);
                     if (slot.method_7681()
                        && slot.method_7677().method_7909() == itemToDrop
                        && (lockSlot == null || !lockSlot.isDropProtected(slot.method_7677(), inventorySlot))) {
                        this.method_2383(slot, slot.field_7874, 1, SlotActionType.field_7795);
                     }
                  }

                  cir.setReturnValue(true);
                  cir.cancel();
               }
            }
         }
      }
   }

   @Unique
   private int toInventorySlot(int screenSlot) {
      if (this instanceof InventoryScreen) {
         return va.screenToLogicalSlot(screenSlot);
      } else {
         if (this instanceof GenericContainerScreen screen) {
            int containerSlots = ((GenericContainerScreenHandler)screen.method_17577()).method_17388() * 9;
            int relative = screenSlot - containerSlots;
            if (relative >= 0 && relative < 27) {
               return 9 + relative;
            }

            if (relative >= 27 && relative < 36) {
               return relative - 27;
            }
         }

         return -1;
      }
   }
}
