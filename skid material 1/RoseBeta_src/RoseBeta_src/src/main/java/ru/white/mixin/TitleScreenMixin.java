package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.ui.DraggableWidget;
import ru.white.Client;
import ru.white.screen.MainMenuScreen;
import ru.white.module.impl.misc.UnHook;

@Environment(EnvType.CLIENT)
@Mixin({TitleScreen.class})
public abstract class TitleScreenMixin extends Screen {
   protected TitleScreenMixin(Text title) {
      super(title);
   }

   @Unique
   private boolean rose$isUnhooked() {
      try {
         if (Client.get() != null && Client.get().moduleManager() != null) {
            pm unhook = Client.get().moduleManager().get(pm.class);
            return unhook != null && unhook.isEnabled();
         }
      } catch (Throwable v2) {
      }

      return false;
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("HEAD")}
   )
   private void onInit(CallbackInfo ci) {
      if (!this.rose$isUnhooked() && MainMenuScreen.isCustomMenuEnabled()) {
         MinecraftClient.method_1551().method_1507(new MainMenuScreen());
      }
   }

   @Inject(
      method = {"method_25426"},
      at = {@At("TAIL")}
   )
   private void addCustomMenuButton(CallbackInfo ci) {
      if (!this.rose$isUnhooked() && !MainMenuScreen.isCustomMenuEnabled()) {
         int btnW = 105;
         int btnH = 20;
         int btnX = this.field_22789 - btnW - 10;
         int btnY = 10;
         ButtonWidget switchBtn = ButtonWidget.method_46430(Text.method_43470(bb.pick("Меню Rose", "Rose Menu")), button -> {
               MainMenuScreen.setCustomMenuEnabled(true);
               if (this.field_22787 != null) {
                  this.field_22787.method_1507(new MainMenuScreen());
               }
            })
            .method_46434(btnX, btnY, btnW, btnH)
            .method_46436(Tooltip.method_47407(Text.method_43470(bb.pick("Переключиться на кастомное меню Rose", "Switch to custom Rose menu"))))
            .method_46431();
         this.method_37063(switchBtn);
      }
   }
}
