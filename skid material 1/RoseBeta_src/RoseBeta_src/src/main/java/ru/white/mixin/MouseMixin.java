package ru.white.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.MouseInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ru.white.utils.helper.Ra;
import ru.white.notification.INotification;
import ru.white.notification.NotificationTask;
import ru.white.module.impl.render.Emotions;
import ru.white.event.impl.EventJump;
import ru.white.event.impl.EventChatMessage;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventPlayerInput;
import ru.white.event.impl.EventRender3D;

@Environment(EnvType.CLIENT)
@Mixin({Mouse.class})
public class MouseMixin {
   @Shadow
   @Final
   private MinecraftClient field_1779;
   @Shadow
   private double field_1789;
   @Shadow
   private double field_1787;

   @Inject(
      method = {"method_1601"},
      at = {@At("HEAD")}
   )
   private void onMouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
      ro editor = so.active();
      if (editor != null && (action == 1 || action == 0)) {
         editor.rawMouseButton(
            this.editorMouseX(this.field_1779.field_1729.method_1603()),
            this.editorMouseY(this.field_1779.field_1729.method_1604()),
            input.comp_4801(),
            action == 1
         );
      }

      Emotions emotions = Emotions.get();
      if (emotions != null && emotions.isEnabled() && this.field_1779.field_1755 == null && input.comp_4801() == emotions.wheelBind.getValue()) {
         if (action == 1) {
            ra.open();
         } else if (action == 0) {
            ra.close(this.field_1779);
         }
      }

      if (action == 1 && this.field_1779.field_1755 == null || action == 1 && this.field_1779.field_1755 instanceof ChatScreen) {
         EventPostMotion event = new EventPostMotion(input.method_74228());
         event.hook();
      }

      if (action == 1 && this.field_1779.field_1755 == null || action == 1 && this.field_1779.field_1755 instanceof ChatScreen) {
         double mouseX = this.field_1779.field_1729.method_1603();
         double mouseY = this.field_1779.field_1729.method_1604();
         double d0 = mouseX * (double)this.field_1779.method_22683().method_4486() / (double)this.field_1779.method_22683().method_4480();
         double d1 = mouseY * (double)this.field_1779.method_22683().method_4502() / (double)this.field_1779.method_22683().method_4507();
         Screen screen = this.field_1779.field_1755;
         EventJump event = new EventJump(input.comp_4801(), action, input.comp_4797(), screen, d0, d1);
         event.hook();
      }

      if (action == 0) {
         MinecraftClient EventItemUse = MinecraftClient.method_1551();
         if (mc.field_1755 != null) {
            double x = mc.field_1729.method_1603() * (double)mc.method_22683().method_4486() / (double)mc.method_22683().method_4480();
            double y = mc.field_1729.method_1604() * (double)mc.method_22683().method_4502() / (double)mc.method_22683().method_4507();
            new EventChatMessage(x, y, input.comp_4801(), mc.field_1755).hook();
         }
      }
   }

   @Inject(
      method = {"method_1600"},
      at = {@At("TAIL")}
   )
   private void onEditorCursorPos(long window, double x, double y, CallbackInfo ci) {
      ro editor = so.active();
      if (editor != null) {
         editor.rawMouseMoved(this.editorMouseX(x), this.editorMouseY(y));
      }
   }

   @Unique
   private float editorMouseX(double rawX) {
      float scaleFix = 2.0F / (float)this.field_1779.method_22683().method_4495();
      return (float)(Mouse.method_68880(this.field_1779.method_22683(), rawX) / (double)scaleFix);
   }

   @Unique
   private float editorMouseY(double rawY) {
      float scaleFix = 2.0F / (float)this.field_1779.method_22683().method_4495();
      return (float)(Mouse.method_68884(this.field_1779.method_22683(), rawY) / (double)scaleFix);
   }

   @Inject(
      method = {"method_1606"},
      at = {@At("HEAD")}
   )
   private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
      EventRender3D event = new EventRender3D();
      event.hook();
      if (event.isCancelled() && event.isModifySensitivity() && (Integer)this.field_1779.field_1690.method_41808().method_41753() > 0) {
         double slowdown = (double)event.getFov() / (double)((Integer)this.field_1779.field_1690.method_41808().method_41753()).intValue();
         this.field_1789 *= slowdown;
         this.field_1787 *= slowdown;
      }
   }

   @Inject(
      method = {"method_1606"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_5872(DD)V"
      )},
      locals = LocalCapture.CAPTURE_FAILHARD,
      cancellable = true
   )
   private void onLook(double timeDelta, CallbackInfo ci, double i, double j) {
      if (ra.onMouseTurn(i, j)) {
         ci.cancel();
      } else {
         MinecraftClient client = MinecraftClient.method_1551();
         if (client != null && client.field_1724 != null) {
            EventPlayerInput event = new EventPlayerInput(i, j);
            event.hook();
            if (!event.isCancelled()) {
               client.field_1724.method_5872(event.yaw, event.pitch);
            }

            ci.cancel();
         }
      }
   }
}
