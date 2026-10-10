package ru.white.utils.misc;

import java.lang.runtime.ObjectMethods;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.PacketUtil;

@Environment(EnvType.CLIENT)
public record to(float x, float y, float width, float height) {
   public static final to EMPTY = new to(0.0F, 0.0F, 0.0F, 0.0F);

   @Override
   public final boolean equals(Object o) {

      return ObjectMethods.bootstrap<"equals",to,"x;y;width;height",to::x,to::y,to::width,to::height>(this, o);
   }

   public float centerX() {
      float result = this.x + this.width / 2.0F;

      return result;
   }

   public float y() {

      return this.y;
   }

   public float x() {
      int v1 = -858277348;

      return this.x;
   }

   public float centerY() {
      int v1 = -460721619;

      return this.y + this.height / 2.0F;
   }

   @Override
   public final String toString() {
      int v1 = 1789078323;
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return ObjectMethods.bootstrap<"toString",to,"x;y;width;height",to::x,to::y,to::width,to::height>(this);
   }

   public boolean contains(float mouseX, float mouseY) {
      boolean result = fq.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
      int v3 = 92531629;

      return result;
   }

   public float distanceSquaredToCenter(float mouseX, float mouseY) {
      float v3 = mouseX - this.centerX();
      float v4 = mouseY - this.centerY();
      return v3 * v3 + v4 * v4;
   }

   @Override
   public final int hashCode() {
      int result = ObjectMethods.bootstrap<"hashCode",to,"x;y;width;height",to::x,to::y,to::width,to::height>(this);

      return result;
   }

   public float width() {

      return this.width;
   }

   
}
