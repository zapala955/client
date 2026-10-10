package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.screen.MainMenuScreen;
import ru.white.screen.GuiCategoryPanel;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.PacketUtil;

@Environment(EnvType.CLIENT)
@Mixin({Screen.class})
public abstract class ScreenMixin {
   @Shadow
   public int field_22789;
   @Shadow
   public int field_22790;
   @Unique
   private final Animation screenOpenAnim = new Animation();
   @Unique
   private boolean screenAnimInitialized = false;

   @Inject(
      method = {"method_25426"},
      at = {@At("HEAD")}
   )
   private void onScreenInit(CallbackInfo ci) {
      Screen self = (Screen)this;
      if (!(self instanceof MainMenuScreen) && !(self instanceof ao)) {
         this.screenOpenAnim.set(0.0);
         this.screenOpenAnim.run(1.0, 0.22, ap.CUBIC_OUT);
         this.screenAnimInitialized = true;
      }
   }

   @Inject(
      method = {"method_47413"},
      at = {@At("HEAD")}
   )
   private void preRenderWithTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      Screen self = (Screen)this;
      if (!(self instanceof MainMenuScreen) && !(self instanceof ao)) {
         if (!this.screenAnimInitialized) {
            this.screenOpenAnim.set(0.0);
            this.screenOpenAnim.run(1.0, 0.22, ap.CUBIC_OUT);
            this.screenAnimInitialized = true;
         }

         this.screenOpenAnim.update();
         float progress = fq.clamp(this.screenOpenAnim.get(), 0.0F, 1.0F);
         if (progress < 0.999F) {
            float scale = 0.96F + 0.04F * progress;
            context.method_51448().pushMatrix();
            context.method_51448().translate((float)this.field_22789 / 2.0F, (float)this.field_22790 / 2.0F);
            context.method_51448().scale(scale, scale);
            context.method_51448().translate((float)(-this.field_22789) / 2.0F, (float)(-this.field_22790) / 2.0F);
         }
      }
   }

   @Inject(
      method = {"method_47413"},
      at = {@At("RETURN")}
   )
   private void postRenderWithTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      Screen self = (Screen)this;
      if (!(self instanceof MainMenuScreen) && !(self instanceof ao)) {
         float progress = fq.clamp(this.screenOpenAnim.get(), 0.0F, 1.0F);
         if (progress < 0.999F) {
            context.method_51448().popMatrix();
         }
      }
   }
}
