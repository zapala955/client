package ru.white.utils;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public final class PacketUtil implements IMinecraft {
   private static final Random RANDOM = new Random();
   public static final Matrix4f lastModMat = new Matrix4f();
   public static double PI2 = Math.PI * 2;
   public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
   private static float[] f1266;
   private static long f1267 = 0L;
   private static final double[] TRIG_TABLE = new double[65536];
   public static final Matrix4f lastProjMat = new Matrix4f();
   private static float[] f1265;

   public static float clampMin(float value, float min) {

      float result;
      if (value < min) {
         result = min;
      } else {
         result = value;
         
      }

      return result;
   }

   public static Vec3d interpolate(Vec3d prevPos, Vec3d pos) {
      Vec3d result = new Vec3d;
      double temp2 = interpolate(prevPos.field_1352, pos.field_1352);

      result./* $VF: Unable to resugar constructor */<init>(
         temp2, interpolate(prevPos.field_1351, pos.field_1351), interpolate(prevPos.field_1350, pos.field_1350)
      );
      return result;
   }

   public static float random1(float min, float max) {
      double result = Math.random() * (double)(max - min);

      return (float)(result + (double)min);
   }

   public static float interpolate(double oldValue, double newValue, double interpolationValue) {
      double temp1 = newValue - oldValue;

      return (float)(oldValue + temp1 * interpolationValue);
   }

   public static double round(double value, int increment) {
      double result = Math.pow(10.0, (double)increment);

      double v3 = result;

      return (double)Math.round(value * v3) / v3;
   }

   public static float smoothRandom(float min, float max, float smoothness) {
      if (f1265 == null) {

         f1265 = new float[]{(min + max) / 2.0F};
         f1266 = new float[]{randomValue(min, max)};
      }

      long v3 = System.currentTimeMillis();

      if ((double)(v3 - f1267) > 250.0 + Math.random() * 300.0) {

         f1266[0] = randomValue(min, max);
         f1267 = v3;
      }

      f1265[0] = f1265[0] + (f1266[0] - f1265[0]) * smoothness;
      return f1265[0];
   }

   public static double getRandomNumberBetween(double min, double max) {
      double result = Math.random() * (max - min) + min;

      return result;
   }

   public static float round(float value, int decimals) {
      float v2 = (float)Math.pow(10.0, (double)decimals);
      float result = (float)Math.round(value * v2) / v2;

      return result;
   }

   public static float randomValue(float min, float max) {
      double result = (double)min;
      double temp1 = (double)max;

      m1597(result, temp1);
      float v4 = ThreadLocalRandom.current().nextFloat();

      return min + v4 * (max - min);
   }

   public static int randomInt(int min, int max) {
      double temp1 = Math.random();

      return min + (int)(temp1 * (double)(max - min + 1));
   }

   public static float clamp(float value, float min, float max) {
      if (value < min) {
         return min;
      } else {
         return value > max ? max : value;
      }
   }

   public static int clampMin(int value, int min) {
      int result;
      if (value < min) {
         result = min;
      } else {
         result = value;
         
      }

      return result;
   }

   @Generated
   private PacketUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static int floorNearestMulN(int x, int n) {
      double temp1 = (double)x;

      temp1 /= (double)n;

      int result = n * (int)Math.floor(temp1);

      return result;
   }

   public static float random(double min, double max) {
      return (float)(min + (max - min) * Math.random());
   }

   public static double deltaTime() {
      double result;
      if (mc.method_47599() > 0) {

         result = 1.0 / (double)mc.method_47599();
         
      } else {
         result = 1.0;
      }

      return result;
   }

   public static float fast(float end, float start, float multiple) {
      double temp1 = deltaTime();

      float result = (1.0F - MathHelper.method_15363((float)(temp1 * (double)multiple), 0.0F, 1.0F)) * end;

      return result + MathHelper.method_15363((float)(deltaTime() * (double)multiple), 0.0F, 1.0F) * start;
   }

   public static float invertScaleValue(float value, float minInput, float maxInput, float minOutput, float maxOutput) {
      if (maxInput - minInput == 0.0F) {
         throw new IllegalArgumentException(
            "Диапазон входных значений не может быть равен нулю."
         );
      } else {
         float result = (maxInput - value) / (maxInput - minInput) * (maxOutput - minOutput);

         float v5 = result + minOutput;
         return Math.max(minOutput, Math.min(maxOutput, v5));
      }
   }

   public static double interpolate(double prev, double orig) {
      RenderTickCounter result = mc.method_61966();

      return lerp((double)result.method_60637(false), prev, orig);
   }

   public static float textScrolling(float textWidth) {
      int v1 = 6000;
      double result = (double)(System.currentTimeMillis() % (long)v1) * Math.PI;

      return (float)MathHelper.method_15350(result / (double)v1, 0.0, 1.0) * textWidth;
   }

   public static double getScaleFactor() {
      double result = (double)mc.method_22683().method_4495();
      int v0 = 1163093679;
      int temp1 = v0 * (v0 + 1);

      temp1 %= 2;

      if (temp1 != 0) {
         }

      return result;
   }

   public static double easeInOutQuadWave(double value) {
      value = (value > 0.5 ? 1.0 - value : value) * 2.0;
      double result;
      if (value < 0.5) {
         result = 2.0 * value * value;
      } else {

         result = 1.0 - Math.pow(-2.0 * value + 2.0, 2.0) / 2.0;
      }

      value = result;
      return value > 1.0 ? 1.0 : (value < 0.0 ? 0.0 : value);
   }

   public static float step(float value, float step) {
      float result = (float)Math.round(value / step);

      result *= step;

      return result;
   }

   public static float randomCos(double min, double max, double time) {
      double result = Math.cos((double)System.currentTimeMillis() / time);
      float temp1 = (float)min;
      float temp2 = (float)max;

      float v8 = (float)(result * (double)randomLerp(temp1, temp2));

      return v8;
   }

   static {
      for (int v0 = 0; v0 < 65536; v0++) {
         TRIG_TABLE[v0] = Math.sin((double)v0 * (Math.PI * 2) / 65536.0);
      }
   }

   public static double cos(double radians) {
      int result = (int)(radians * 10430.378350470453 + 16384.0);

      int v2 = result & 65535;
      return TRIG_TABLE[v2];
   }

   public static boolean isHovered(float mouseX, float mouseY, float x, float y, float width, float height) {
      if (mouseX > x) {

         if (mouseX < x + width && mouseY > y) {
            float temp1 = y + height;

            if (mouseY < temp1) {
               return true;
            }
         }
      }

      return false;
   }

   public static double clampMin(double value, double min) {
      double v8;
      if (value < min) {
         int v4 = -1534531224;
         int result = v4 * (v4 + 1);
         int v5 = -1792711102;
         int temp2 = v5 * (v5 + 1);

         v8 = min;
      } else {
         v8 = value;
      }

      return v8;
   }

   public static float randomSin(double min, double max, double time) {
      double result = (double)System.currentTimeMillis() / time;

      return (float)(Math.sin(result) * (double)randomLerp((float)min, (float)max));
   }

   public static boolean isInRegion(int mouseX, int mouseY, float x, float y, float width, float height) {
      return (float)mouseX >= x && (float)mouseY >= y && (float)mouseX <= x + width && (float)mouseY <= y + height;
   }

   public static double lerp(double delta, double start, double end) {
      return start + delta * (end - start);
   }

   public static double randomValue(double min, double max) {
      m1597(min, max);
      double temp1 = ThreadLocalRandom.current().nextDouble() * (max - min);

      return min + temp1;
   }

   public static float randomLerp(float min, float max) {
      Number result = cq.lerp(max, min, (double)new SecureRandom().nextFloat());

      return (Float)result;
   }

   public static double sin(double radians) {
      double result = radians * 10430.378350470453;

      int v2 = (int)result & 65535;
      return TRIG_TABLE[v2];
   }

   public static float wrapLerp(float step, float input, float target) {
      float result = input + step * MathHelper.method_15393(target - input);

      return result;
   }

   public static Vec3d cosSin(int i, 

      int v4 = Math.min(i, size);
      float v5 = (float)(Math.cos((double)v4 * PI2 / (double)size) * width);
      double result = (double)v4;

      float v6 = (float)(-Math.sin(result * PI2 / (double)size) * width);
      return new Vec3d((double)v5, 0.0, (double)v6);
   }

   private static void m1597(double min, double max) {
      if (max < min) {
         IllegalArgumentException result = new IllegalArgumentException;

         result./* $VF: Unable to resugar constructor */<init>(
            "max не может быть меньше min."
         );
         throw result;
      }
   }

   public static float lerp(float delta, float start, float end) {
      int v3 = 2109911624;

      int v6 = v3 * (v3 + 1) % 2;

      if (v6 != 0) {
         }

      return start + delta * (end - start);
   }

   public static float randomGaussianSmooth(double min, double max, double time) {
      double v6 = (min + max) * 0.5;
      double v8 = (max - min) / 6.0;
      double v10 = RANDOM.nextGaussian() * v8;
      int v16 = -165735777;

      double v12 = Math.sin((double)System.currentTimeMillis() / time);
      double v14 = v6 + v10 * v12;
      float v19 = (float)Math.max(min, Math.min(max, v14));

      return v19;
   }

   public static double clamps(double min, double max, double value) {

      return Math.max(min, Math.min(max, value));
   }

   public static double clamp(double value, double min, double max) {

      return Math.max(min, Math.min(max, value));
   }

   public static float randomGaussian(double min, double max) {
      double v4 = (min + max) * 0.5;
      double result = (max - min) / 6.0;

      double v6 = result;
      double v8 = v4 + RANDOM.nextGaussian() * v6;
      result = Math.max(min, Math.min(max, v8));

      return (float)result;
   }

   public static float wrapAngleTo180_float(float p_76142_0_) {
      float result = p_76142_0_ % 360.0F;
      int v1 = -692794256;
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      p_76142_0_ = result;
      if (result >= 180.0F) {
         p_76142_0_ -= 360.0F;
      }

      if (p_76142_0_ < -180.0F) {
         p_76142_0_ += 360.0F;
      }

      return p_76142_0_;
   }

   public static float round(float value, float increment) {
      float result = value / increment;

      int v5 = Math.round(result);

      return (float)v5 * increment;
   }

   
}
