package ru.white.module.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Of;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.manager.rotation.AngleHelper;
import ru.white.module.impl.combat.AttackAura;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Free Look",
   desc = "Позволяет крутить свободно камерой.",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class FreeLook extends Module {
   private boolean f625;
   public SettingVisibility bind;
   public ModeSetting type = new ModeSetting(this, "Режим", "Зажать", "Нажать");

   @EventTarget
   public void onTick(EventRenderModel event) {
      int v2 = this.bind.get();
      if (v2 <= 0) {

         if (this.f625) {
            this.m617();
         }
      } else {
         if (this.type.is("Зажать")) {
            boolean v3 = mc.method_22683() != null && GLFW.glfwGetKey(mc.method_22683().method_4490(), v2) == 1;
            if (v3 && !this.f625) {
               this.m616();
            } else if (!v3 && this.f625) {
               this.m617();
            }
         }

         if (this.f625) {
            wd.active = true;
         }
      }
   }

   @EventTarget
   public void onKey(EventPostMotion event) {
      int v2 = this.bind.get();
      if (v2 > 0 && event.getKey() == v2) {
         if (this.type.is("Нажать")) {

            if (this.f625) {
               this.m617();
            } else {
               this.m616();
            }
         } else if (!this.f625) {
            this.m616();
         }
      }
   }

   private void m616() {
      if (mc.field_1724 != null) {

         this.f625 = true;
         wd.freeYaw = mc.field_1724.method_36454();

         wd.freePitch = mc.field_1724.method_36455();
         wd.forced = true;
         wd.active = true;

         mc.field_1690.method_31043(Perspective.field_26665);
      }
   }

   @Override
   public void onDisable() {
      if (this.f625) {
         this.m617();
      }
   }

   public FreeLook() {
      this.bind = new SettingVisibility(this, "Клавиша", -1);
   }

   private void m617() {
      this.f625 = false;

      wd.forced = false;
      fg v1 = fg.get();
      if (v1 == null || !v1.isEnabled()) {
         wd.active = false;
      }

      if (mc.field_1690 != null) {

         mc.field_1690.method_31043(Perspective.field_26664);
      }
   }

   
}
