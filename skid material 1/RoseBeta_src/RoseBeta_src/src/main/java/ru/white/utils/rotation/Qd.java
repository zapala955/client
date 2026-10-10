package ru.white.utils.rotation;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;
import ru.white.auth.StrDec;
import ru.white.utils.NetworkUtil;

@Environment(EnvType.CLIENT)
public class Qd {
   private static final float PEIRCE_SCALE = 0.45F;
   private float f170;
   private static final float DAMPING = 0.32F;
   private static final float SPRING = 0.14F;
   private float f167;
   private float f166 = 0.0F;
   private float f168;
   private float f169;

   public float sampleStochasticStep() {
      float v1 = gq.noise(this.f170 * 1.8F + 52.1F);
      float v2 = m197() * 0.12F;
      float result = v1 * 0.25F + v2;

      return MathHelper.method_15363(result, -0.4F, 0.4F);
   }

   public Qd() {
      this.f167 = 0.0F;
      this.f168 = 0.0F;
      this.f169 = 0.0F;
      this.f170 = 0.0F;
   }

   public void reset() {
      this.f166 = 0.0F;

      this.f167 = 0.0F;
      this.f168 = 0.0F;
      this.f169 = 0.0F;
      this.f170 = ThreadLocalRandom.current().nextFloat() * 100.0F;
   }

   public float[] sample(float intensity, float speed, int hitCount) {
      this.f170 += 0.22F;
      float v4 = 1.0F / (1.0F + speed * 0.08F);
      float v5 = MathHelper.method_15363(intensity, 0.02F, 2.5F) * v4;
      float v6 = 1.0F + (float)Math.min(hitCount, 15) * 0.025F;
      v5 *= v6;
      float v7 = m197() * 0.45F * v5;
      float v8 = m197() * 0.33749998F * v5;
      this.f168 = this.f168 + -0.32F * this.f168 - 0.14F * this.f166 + v7;
      this.f169 = this.f169 + -0.32F * this.f169 - 0.14F * this.f167 + v8;

      this.f168 = MathHelper.method_15363(this.f168, -2.5F, 2.5F);
      this.f169 = MathHelper.method_15363(this.f169, -1.8F, 1.8F);
      this.f166 = this.f166 + this.f168;
      this.f167 = this.f167 + this.f169;
      this.f166 = MathHelper.method_15363(this.f166, -4.0F, 4.0F);
      this.f167 = MathHelper.method_15363(this.f167, -2.8F, 2.8F);
      float v9 = gq.noise(this.f170 * 0.65F) * 0.35F * v5;
      float v10 = gq.noise(this.f170 * 0.65F + 31.7F) * 0.25F * v5;
      float v11 = gq.noise(this.f170 * 2.4F + 13.5F) * 0.18F * v5;

      float v12 = gq.noise(this.f170 * 2.4F + 87.2F) * 0.12F * v5;
      float v13 = this.f166 * 0.55F + v9 + v11;
      float v14 = this.f167 * 0.55F + v10 + v12;
      return new float[]{v13, v14};
   }

   private static float m197() {
      ThreadLocalRandom v0 = ThreadLocalRandom.current();
      double v1 = v0.nextDouble();
      if (v1 < 1.0E-6) {
         v1 = 1.0E-6;
      }

      if (v1 > 0.999999) {
         v1 = 0.999999;
      }

      double v3 = v1 < 0.5 ? -1.0 : 1.0;
      int v9 = -1349648129;
      int temp2 = v9 + 1;

      double v5 = Math.abs(v1 - 0.5) * 2.0;
      double result = Math.log(1.0 - v5 * 0.94);

      double v7 = -result;
      return (float)(v3 * Math.min(v7, 2.4));
   }

   
}
