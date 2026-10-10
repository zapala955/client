package ru.white.manager.rotation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import ru.white.event.EventTarget;
import ru.white.auth.StrDec;
import ru.white.utils.ItemUtil;
import ru.white.event.impl.EventPlayerInput;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventRaytrace;
import ru.white.event.impl.EventRenderModel;

@Environment(EnvType.CLIENT)
public class AngleHelper extends ud {
   public static boolean active;
   public static boolean forced;
   public static float freePitch;
   public static float freeYaw;

   @EventTarget
   public void onEvent(EventRaytrace event) {
      if (active) {
         event.setYaw(freeYaw);
         event.setPitch(freePitch);
      } else {
         freeYaw = event.getYaw();
         float result = event.getPitch();

         freePitch = result;
      }
   }

   @EventTarget
   public void onEvent(EventRenderModel event) {
      iq.tick();

      int v5 = v3 * (v3 + 1);

      
   }

   @EventTarget
   public void onPacket(EventStep e) {
      iq.packet(e);

      int v5 = v2 + 1;

      
   }

   @EventTarget
   public void onEvent(EventPlayerInput event) {
      if (active) {

         double v6 = event.getYaw();
         double v7 = event.getPitch();

         this.m202(v6, v7);
         event.cancel();
      }
   }

   public static void setActive(boolean value) {
      if (!value) {

         if (forced) {
            return;
         }
      }

      active = value;
   }

   private void m202(double targetYaw, double targetPitch) {
      freePitch = MathHelper.method_15363((float)((double)freePitch + targetPitch * 0.15), -90.0F, 90.0F);
      float result = (float)((double)freeYaw + targetYaw * 0.15);

      freeYaw = result;
   }

   
}
