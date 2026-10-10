package ru.white.mixin;

import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.white.Client;

@Environment(EnvType.CLIENT)
@Mixin({ChatScreen.class})
public class ChatScreenMixin {
   @Shadow
   protected TextFieldWidget field_2382;
   @Unique
   private List<String> rose$suggestions = Collections.emptyList();
   @Unique
   private int rose$selected = 0;
   @Unique
   private int rose$boxX;
   @Unique
   private int rose$boxY;
   @Unique
   private int rose$boxW;
   @Unique
   private int rose$boxCount;
   @Unique
   private static final int ROSE_LINE_H = 12;

   @Inject(
      method = {"method_44056"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void interceptMessage(String message, boolean addToHistory, CallbackInfo ci) {
      if (!message.isEmpty()) {
         char prefix = Client.get().commandManager().getPrefix();
         if (message.charAt(0) == prefix) {
            ci.cancel();
            Client.get().commandManager().handleMessage(message);
         }
      }
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("TAIL")}
   )
   private void rose$renderSuggestions(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      this.rose$boxCount = 0;
      String text = this.field_2382.method_1882();
      char prefix = Client.get().commandManager().getPrefix();
      if (!text.isEmpty() && text.charAt(0) == prefix) {
         this.rose$suggestions = Client.get().commandManager().getSuggestions(text);
         if (!this.rose$suggestions.isEmpty()) {
            if (this.rose$selected >= this.rose$suggestions.size()) {
               this.rose$selected = 0;
            }

            TextRenderer tr = MinecraftClient.method_1551().field_1772;
            int count = Math.min(this.rose$suggestions.size(), 10);
            int width = 0;

            for (int i = 0; i < count; i++) {
               width = Math.max(width, tr.method_1727(this.rose$suggestions.get(i)));
            }

            width += 6;
            int boxH = count * 12;
            int x = this.field_2382.method_46426() - 2;
            int y = this.field_2382.method_46427() - boxH - 1;
            this.rose$boxX = x;
            this.rose$boxY = y;
            this.rose$boxW = width;
            this.rose$boxCount = count;
            context.method_25294(x, y, x + width, y + boxH, -436207616);
            context.method_25294(x, y, x + width, y + 1, 1090519039);

            for (int i = 0; i < count; i++) {
               int ly = y + i * 12;
               boolean sel = i == this.rose$selected;
               if (sel) {
                  context.method_25294(x, ly, x + width, ly + 12, 1442840575);
               }

               context.method_51433(tr, this.rose$suggestions.get(i), x + 3, ly + 2, sel ? -171 : -4473925, false);
            }
         }
      } else {
         this.rose$suggestions = Collections.emptyList();
      }
   }

   @Inject(
      method = {"method_25404"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void rose$keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
      if (!this.rose$suggestions.isEmpty()) {
         int n = this.rose$suggestions.size();
         switch (input.method_74228()) {
            case 258:
               this.rose$apply(this.rose$suggestions.get(Math.min(this.rose$selected, n - 1)));
               cir.setReturnValue(true);
               break;
            case 264:
               this.rose$selected = (this.rose$selected + 1) % n;
               cir.setReturnValue(true);
               break;
            case 265:
               this.rose$selected = (this.rose$selected - 1 + n) % n;
               cir.setReturnValue(true);
         }
      }
   }

   @Inject(
      method = {"method_25402"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void rose$mouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
      int button = click.method_74245();
      double mouseX = click.comp_4798();
      double mouseY = click.comp_4799();
      if (!this.rose$suggestions.isEmpty() && this.rose$boxCount != 0 && button == 0) {
         if (mouseX >= (double)this.rose$boxX
            && mouseX <= (double)(this.rose$boxX + this.rose$boxW)
            && mouseY >= (double)this.rose$boxY
            && mouseY <= (double)(this.rose$boxY + this.rose$boxCount * 12)) {
            int idx = (int)((mouseY - (double)this.rose$boxY) / 12.0);
            if (idx >= 0 && idx < this.rose$boxCount && idx < this.rose$suggestions.size()) {
               this.rose$apply(this.rose$suggestions.get(idx));
               cir.setReturnValue(true);
            }
         }
      }
   }

   @Unique
   private void rose$apply(String suggestion) {
      this.field_2382.method_1852(suggestion);
      this.field_2382.method_1872(false);
      this.rose$selected = 0;
   }
}
