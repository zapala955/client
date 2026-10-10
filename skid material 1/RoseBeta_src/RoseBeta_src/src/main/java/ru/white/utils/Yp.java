package ru.white.utils;

import java.lang.runtime.ObjectMethods;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public record yp(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
   private final float f1264;
   private final float f1261;
   public static final yp ZERO = new yp(0.0F, 0.0F, 0.0F, 0.0F);
   private final float f1262;
   private final float f1263;

   public static yp bottomRight(float radius) {
      yp result = new yp;

      result./* $VF: Unable to resugar constructor */<init>(0.0F, 0.0F, radius, 0.0F);
      return result;
   }

   public static yp all(float radius) {
      return new yp(radius, radius, radius, radius);
   }

   public static yp bottomLeft(float radius) {
      yp result = new yp;
      int v1 = 744047541;
      int v2 = -675292583;

      result./* $VF: Unable to resugar constructor */<init>(0.0F, 0.0F, 0.0F, radius);
      return result;
   }

   public Yp(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
      this.f1261 = topLeftRadius;
      this.f1262 = topRightRadius;
      this.f1263 = bottomRightRadius;
      this.f1264 = bottomLeftRadius;
   }

   public float bottomRightRadius() {
      int v1 = -1839048210;
      int temp1 = v1 * (v1 + 1);

      temp1 %= 2;

      if (temp1 != 0) {
         }

      return this.f1263;
   }

   @NotNull
   @Override
   public String toString() {

      float v1 = this.f1264;
      float v2 = this.f1263;
      float v3 = this.f1262;
      float v4 = this.f1261;
      StringBuilder result = new StringBuilder()
         .append("BorderRadius{topLeftRadius=")
         .append(v4)
         .append(", topRightRadius=")
         .append(v3)
         .append(", bottomRightRadius=")
         .append(v2);
      int v5 = 1715864791;

      return result.append(", bottomLeftRadius=").append(v1).append("}").toString();
   }

   public float bottomLeftRadius() {

      return this.f1264;
   }

   public static yp topLeft(float radius) {
      return new yp(radius, 0.0F, 0.0F, 0.0F);
   }

   public float topRightRadius() {
      return this.f1262;
   }

   public float topLeftRadius() {
      int v1 = 1979079413;

      return this.f1261;
   }

   public static yp right(float topRadius, float bottomRadius) {
      yp result = new yp;

      result./* $VF: Unable to resugar constructor */<init>(0.0F, topRadius, bottomRadius, 0.0F);

      return result;
   }

   public static yp left(float topRadius, float bottomRadius) {
      yp result = new yp(topRadius, 0.0F, 0.0F, bottomRadius);
      int v2 = -1643606396;
      int temp1 = v2 * (v2 + 1) % 2;

      if (temp1 != 0) {
         }

      return result;
   }

   public static yp bottom(float leftRadius, float rightRadius) {
      yp result = new yp;

      result./* $VF: Unable to resugar constructor */<init>(0.0F, 0.0F, rightRadius, leftRadius);
      return result;
   }

   @Override
   public final 

      return ObjectMethods.bootstrap<"hashCode",yp,"topLeftRadius;topRightRadius;bottomRightRadius;bottomLeftRadius",yp::f1261,yp::f1262,yp::f1263,yp::f1264>(
         this
      );
   }

   public static yp top(float leftRadius, float rightRadius) {
      return new yp(leftRadius, rightRadius, 0.0F, 0.0F);
   }

   public static yp topRight(float radius) {
      yp result = new yp;

      result./* $VF: Unable to resugar constructor */<init>(0.0F, radius, 0.0F, 0.0F);
      return result;
   }

   
}
