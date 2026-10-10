package ru.white.module.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.manager.ModuleManager;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.combat.TriggerBot;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.misc.Pp;
import ru.white.utils.render.ColorPalette;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Sprint",
   desc = "Автоматический спринт",
   category = Category.MOVEMENT
)
@Environment(EnvType.CLIENT)
public class Sprint extends Module {
   public int tick;

   @EventTarget
   public void onEvent(EventTick eventUpdate) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!mc.field_1724.method_5805()) {
            this.tick += 2;
         }

         if (this.tick != 0) {
            mc.field_1724.method_5728(false);
            mc.field_1690.field_1867.method_23481(false);
            this.tick--;
         } else if (qp.resetSprintTick(fg.target, fg.get().getRanges()) && fg.get().isEnabled() && fg.get().typeSprint.is("Silent")) {
            mc.field_1690.field_1867.method_23481(false);
         } else if (qp.resetSprintTick(fg.target, fg.get().getRanges()) && fg.get().isEnabled() && fg.get().typeSprint.is("Packet")) {
            mc.field_1724.method_5728(false);
            mc.field_1690.field_1867.method_23481(false);
         } else {
            LivingEntity result = TriggerBot.targets;
            if temp1 = Client.get.moduleManager();

            if (pp.resetSprintTick(result, temp1.get(TriggerBot.class).getRanges())
               && Client.get.moduleManager().get(TriggerBot.class).isEnabled()
               && Client.get.moduleManager().get(TriggerBot.class).typeSprint.is("Silent")) {
               mc.field_1690.field_1867.method_23481(false);
            } else if (pp.resetSprintTick(TriggerBot.targets, Client.get.moduleManager().get(TriggerBot.class).getRanges())
               && Client.get.moduleManager().get(TriggerBot.class).isEnabled()
               && Client.get.moduleManager().get(TriggerBot.class).typeSprint.is("Packet")) {
               mc.field_1724.method_5728(false);
               mc.field_1690.field_1867.method_23481(false);
            } else {
               boolean v2 = mc.field_1724.field_5976 && !mc.field_1724.field_34927;
               boolean v3 = mc.field_1724.method_5715() && !mc.field_1724.method_5681();
               if (!v2 && mc.field_1724.field_6250 > 0.0F) {
                  boolean v7 = true;
               } else {
                  boolean v6 = false;
               }

               if (!v3) {
                  mc.field_1690.field_1867.method_23481(true);
               }
            }
         }
      }
   }

   @EventTarget
   public void onEvent(EventSync e) {

      this.tick += 4;
   }

   public static xh get() {

      Module result = uq.get(xh.class);

      return (xh)result;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.field_1724 != null) {
         mc.field_1724.method_5728(false);

         
      }
   }

   
}
