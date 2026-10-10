package ru.white.module.impl.player;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.Perspective;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.PacketUtil;
import ru.white.event.impl.EventKey;
import ru.white.event.impl.EventWorldChange;
import ru.white.event.impl.EventSendPacket;
import ru.white.event.impl.EventStep;

@ModuleAnnotation(
   name = "Free Camera",
   desc = "Камера свободного палета",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class FreeCamera extends Module {
   public Vec3d prevPos;
   public Vec3d pos;

   @EventTarget
   public void onInput(EventKey e) {
      float v2 = 1.5F;
      double[] v3 = this.calculateDirection((float)e.forward(), e.sideways(), (double)v2);
      this.prevPos = this.pos;

      Vec3d v7 = this.pos;
      double temp2 = v3[0];
      double temp3;
      if (e.getInput().comp_3163()) {
         temp3 = (double)v2;
      } else {
         boolean v8 = e.getInput().comp_3164();

         if (v8) {

            temp3 = (double)(-v2);
         } else {
            temp3 = 0.0;
         }
      }

      this.pos = v7.method_1031(temp2, temp3, v3[1]);
      e.inputNone();
   }

   @EventTarget
   public void onCameraPosition(EventSendPacket e) {

      e.setPos(fq.interpolate(this.prevPos, this.pos));
      mc.field_1690.method_31043(Perspective.field_26664);
   }

   @EventTarget
   public void onPacket(EventStep e) {

      Packet result = e.getPacket();
      Objects.requireNonNull(result);
      Object v2 = result;

      switch (v2) {
         case PlayerRespawnS2CPacket v4:
            this.setEnabled(false);
            break;
         case GameJoinS2CPacket v5:
            this.setEnabled(false);
            break;
      }
   }

   @EventTarget
   public void onMove(EventWorldChange e) {

      e.setMovement(Vec3d.field_1353);
   }

   public double[] calculateDirection(float forward, float sideways, double distance) {
      float v5 = mc.field_1724.method_36454();
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            v5 += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            v5 += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double v6 = Math.sin(Math.toRadians((double)(v5 + 90.0F)));
      double v8 = Math.cos(Math.toRadians((double)(v5 + 90.0F)));
      double v10 = (double)forward * distance * v8 + (double)sideways * distance * v6;
      double result = (double)forward * distance * v6;

      double v12 = result - (double)sideways * distance * v8;
      return new double[]{v10, v12};
   }

   @Override
   public void onEnable() {
      Vec3d temp2 = new Vec3d(mc.method_1561().field_4686.method_71156().method_46409());

      this.prevPos = this.pos = temp2;
      super.onEnable();
   }

   
}
