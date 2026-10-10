package ru.white.manager.rotation;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2f;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public class Rotation implements IMinecraft {
   public float pitch;
   public float yaw;

   @Generated
   public void setPitch(float pitch) {

      this.pitch = pitch;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public static float cameraYaw() {
      float result = mc.field_1773.method_19418().method_19330();
      int temp1;
      if (mc.field_1690.method_31044().method_31035()) {
         temp1 = 180;
      } else {
         temp1 = 0;

         
      }

      result = MathHelper.method_15393(result + (float)temp1);

      return result;
   }

   public Rotation(Entity entity) {
      this.yaw = entity.method_36454();
      this.pitch = entity.method_36455();
   }

   @Generated
   public float getPitch() {

      return this.pitch;
   }

   public float getDelta(Rotation target) {
      float v2 = MathHelper.method_15393(target.yaw - this.yaw);

      float v3 = target.pitch - this.pitch;
      int result = v5 * (v5 + 1) % 2;

      if (result != 0) {
         }

      return (float)Math.hypot((double)Math.abs(v2), (double)Math.abs(v3));
   }

   public static float cameraPitch() {
      byte v1;
      if (mc.field_1690.method_31044().method_31035()) {

         v1 = -1;
      } else {
         v1 = 1;
      }

      return (float)v1 * mc.field_1773.method_19418().method_19329();
   }

   public double getDeltaDouble(Rotation target) {
      double v2 = (double)MathHelper.method_15393(target.yaw - this.yaw);

      double v4 = (double)MathHelper.method_15393(target.pitch - this.pitch);
      return Math.hypot(v2, v4);
   }

   public Rotation(float yawN, float pitchN) {
      this.yaw = yawN;
      this.pitch = pitchN;
   }

   public static Vector2f camera() {
      Vector2f result = new Vector2f;
      float temp2 = cameraYaw();

      result./* $VF: Unable to resugar constructor */<init>(temp2, cameraPitch());
      return result;
   }

   @Generated
   public float getYaw() {

      return this.yaw;
   }

   
}
