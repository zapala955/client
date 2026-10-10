package ru.white.utils.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap.Type;
import ru.white.auth.StrDec;
import ru.white.module.impl.render.Atmosphere;

@Environment(EnvType.CLIENT)
public final class kt {
   private static final float RIPPLE_WRAP = 1000.0F;
   private float f1580;
   private static final float BOLT_WINDOW = 0.45F;
   private float f1573;
   private float f1576;
   private boolean f1582;
   private float f1572;
   private float f1574;
   private final List<kt.lt> f1568;
   private float f1581;
   private static final float SNOW_FALL = 2.5F;
   private static final float FLASH_WINDOW = 0.95F;
   private ClientWorld f1570;
   private long f1569;
   private final Random f1567 = new Random();
   private static final float DRIFT_WRAP = 65536.0F;
   private float f1577;
   private static final float MAX_FRAME_DELTA = 0.1F;
   private static final float RAIN_WRAP = 4096.0F;
   private float f1575;
   public static final kt INSTANCE = new kt();
   private static final int MAX_TRACKED = 4;
   private static final float RAIN_FALL = 24.0F;
   private float f1579;
   private float f1578;
   private float f1571;

   public float rainPhase() {
      return this.f1577;
   }

   public float intensity() {

      return this.f1571;
   }

   private void m1908(float delta) {

      this.f1575 += delta * 0.06F;
      float v2 = this.f1575 + MathHelper.method_15374((double)(this.f1575 * 0.73F)) * 1.6F;
      this.f1572 = MathHelper.method_15362((double)v2);
      this.f1573 = MathHelper.method_15374((double)v2);
      this.f1574 = 0.3F + 0.7F * this.f1571;
   }

   public void strikeAt(Vec3d impactPos, double distanceFromCamera) {
      if (impactPos != null) {
         if (this.f1568.size() >= 4) {
            this.f1568.remove(0);
         }

         kt.lt v4 = new kt.lt();
         v4.seed = this.f1567.nextFloat() * 900.0F;
         v4.topX = impactPos.field_1352;

         v4.topY = impactPos.field_1351 + 9.0 + this.f1567.nextDouble() * 5.0;
         v4.topZ = impactPos.field_1350;
         v4.bottomX = impactPos.field_1352;
         v4.bottomY = impactPos.field_1351;
         v4.bottomZ = impactPos.field_1350;
         v4.distance = distanceFromCamera;
         v4.falloff = (float)MathHelper.method_15350(1.0 - distanceFromCamera / 600.0, 0.35, 1.0);
         v4.manual = true;
         this.f1568.add(v4);
      }
   }

   private kt() {
      this.f1568 = new ArrayList<>();
      this.f1572 = 1.0F;
      this.f1576 = 0.8F;
      this.f1580 = 1.0F;
      this.f1581 = 64.0F;
   }

   public void reset() {
      this.f1568.clear();
      this.f1569 = 0L;
      this.f1576 = 0.8F;
      this.f1571 = 0.0F;
      this.f1577 = 0.0F;
      this.f1578 = 0.0F;

      this.f1579 = 0.0F;
      this.f1580 = 1.0F;

      this.f1582 = false;
   }

   private kt.lt spawn(Vec3d cameraPos, double azimuth, double distance) {
      double v6 = cameraPos.field_1352 + Math.cos(azimuth) * distance;
      double v8 = cameraPos.field_1350 + Math.sin(azimuth) * distance;
      double v10 = (6.0 + this.f1567.nextDouble() * 16.0) * (double)this.f1574;
      kt.lt v12 = new kt.lt();
      v12.seed = this.f1567.nextFloat() * 900.0F;
      v12.topX = v6 + (double)this.f1572 * v10;
      v12.topY = cameraPos.field_1351 + 60.0 + this.f1567.nextDouble() * 70.0;
      v12.topZ = v8 + (double)this.f1573 * v10;
      v12.bottomX = v6;
      double temp1 = cameraPos.field_1351 - 20.0;
      double temp2 = this.f1567.nextDouble();

      v12.bottomY = temp1 - temp2 * 30.0;
      v12.bottomZ = v8;
      v12.distance = distance;
      v12.falloff = (float)MathHelper.method_15350(1.0 - distance / 600.0, 0.35, 1.0);
      return v12;
   }

   public float flashStrength() {
      float v1 = 0.0F;

      for (kt.lt v3 : this.f1568) {
         if (!v3.manual) {
            v1 = Math.max(v1, m1912(v3));
         }
      }

      int v5 = -752850999;
      int v7 = v5 * (v5 + 1) % 2;

      if (v7 != 0) {
         }

      return v1;
   }

   public float windX() {

      return this.f1572;
   }

   private void m1909(qi feature, Vec3d cameraPos, float delta) {
      for (int v4 = this.f1568.size() - 1; v4 >= 0; v4--) {
         kt.lt v5 = this.f1568.get(v4);
         v5.age += delta;
         if (v5.age > 0.95F) {
            this.f1568.remove(v4);
         }
      }

      if (feature.isEnabled() && feature.lightning.getValue() && !(this.f1571 <= 0.02F) && cameraPos != null) {
         this.f1576 -= delta;
         if (!(this.f1576 > 0.0F)) {
            this.f1576 = this.m1910(feature);
            if (this.f1568.size() >= 4) {
               int v6 = 1016782410;
               int temp1 = v6 * (v6 + 1) % 2;

               if (temp1 != 0) {
                  }

               this.f1568.remove(0);
            }

            this.f1568.add(this.spawn(cameraPos, this.m1911(), (double)feature.strikeDistance.getValue().floatValue() * (0.5 + this.f1567.nextDouble() * 0.5)));
            
         }
      }
   }

   public void update(qi feature, Vec3d cameraPos) {
      ClientWorld v3 = MinecraftClient.method_1551().field_1687;
      if (v3 != this.f1570) {
         this.f1570 = v3;
         this.reset();
      }

      long v4 = System.nanoTime();
      long v9;
      int result = (v9 = this.f1569 - 0L) == 0L ? 0 : (v9 < 0L ? -1 : 1);

      float v6 = !result ? 0.0F : (float)(v4 - this.f1569) / 1.0E9F;
      this.f1569 = v4;
      v6 = MathHelper.method_15363(v6, 0.0F, 0.1F);
      this.m1907(feature, v3, v6);
      this.m1908(v6);
      this.m1904(feature, v6);
      this.m1905(v3, cameraPos, v6);
      this.m1906(v3, cameraPos, v6);
      this.m1909(feature, cameraPos, v6);
   }

   public boolean hasVisibleStrike() {
      Iterator result = this.f1568.iterator();

      Iterator v1 = result;

      kt.lt v2;
      do {

         if (!v1.hasNext()) {
            return false;
         }

         result = (Iterator)v1.next();

         v2 = (kt.lt)result;
      } while (!(v2.age <= 0.95F));

      return true;
   }

   public float skyVisibility() {

      return this.f1580;
   }

   private double m1911() {
      ClientPlayerEntity v1 = MinecraftClient.method_1551().field_1724;
      if (v1 != null && !(this.f1567.nextFloat() > 0.66F)) {
         double result = Math.toRadians((double)v1.method_36454());

         double v2 = result;
         double v4 = Math.atan2(Math.cos(v2), -Math.sin(v2));
         return v4 + (this.f1567.nextDouble() - 0.5) * 1.9;
      } else {
         return this.f1567.nextDouble() * Math.PI * 2.0;
      }
   }

   private void m1904(qi feature, float delta) {
      float result;
      if (feature.usesSnow()) {
         result = 2.5F;
      } else {
         result = 24.0F;
         
      }

      float v3 = result * feature.rainSpeed.getValue();
      this.f1577 = (this.f1577 + delta * v3) % 4096.0F;
      this.f1578 = (this.f1578 + delta * (1.5F + 4.0F * this.f1574)) % 65536.0F;
      this.f1579 = (this.f1579 + delta) % 1000.0F;
   }

   public float windStrength() {
      return this.f1574;
   }

   private float m1910(qi feature) {
      float v2 = Math.max(feature.strikeRate.getValue(), 0.05F);
      float v3 = MathHelper.method_16439(this.f1571, 11.0F, 1.2F) / v2;

      return v3 * (0.55F + this.f1567.nextFloat() * 0.9F);
   }

   public float ripplePhase() {

      return this.f1579;
   }

   public float groundHeight() {
      int v2 = -1693020366;
      int temp1 = v2 * (v2 + 1);

      return this.f1581;
   }

   public static float boltEnvelope(kt.lt strike) {
      float v1 = strike.age;
      if (!(v1 < 0.0F) && !(v1 > 0.45F)) {

         float v2 = Math.min(1.0F, v1 / 0.012F);
         double result = Math.exp((double)(-v1 * 6.5F));

         float v3 = (float)result;
         return v2 * v3 * (0.42F + 0.58F * Math.abs(MathHelper.method_15374((double)(v1 * 33.0F + strike.seed))));
      } else {
         return 0.0F;
      }
   }

   private void m1906(ClientWorld world, Vec3d cameraPos, float delta) {

      if (world != null && cameraPos != null) {
         BlockPos v4 = BlockPos.method_49638(cameraPos);
         if (world.method_22340(v4)) {
            float v5 = (float)world.method_8624(Type.field_13203, v4.method_10263(), v4.method_10260());

            if (!this.f1582) {
               this.f1581 = v5;
               this.f1582 = true;
            } else {
               float v6 = v5 - this.f1581;
               float v7 = delta * (2.5F + Math.abs(v6) * 1.8F);
               this.f1581 = Math.abs(v6) <= v7 ? v5 : this.f1581 + Math.signum(v6) * v7;
            }
         }
      }
   }

   public float windZ() {
      return this.f1573;
   }

   private static float m1912(kt.lt strike) {
      float v1 = strike.age;
      if (!(v1 < 0.0F) && !(v1 > 0.95F)) {
         float v2 = Math.min(1.0F, v1 / 0.02F);
         float v3 = (float)Math.exp((double)(-v1 * 4.2F));

         float result = v2 * v3 * (0.6F + 0.4F * MathHelper.method_15374((double)(v1 * 27.0F + strike.seed))) * strike.falloff;

         return result;
      } else {
         return 0.0F;
      }
   }

   private void m1905(ClientWorld world, Vec3d cameraPos, float delta) {
      ClientPlayerEntity v4 = MinecraftClient.method_1551().field_1724;
      boolean v5 = world != null
         && cameraPos != null
         && world.method_8597().comp_642()
         && world.method_8311(BlockPos.method_49638(cameraPos))
         && (v4 == null || !v4.method_5869());
      float v6 = v5 ? 1.0F : 0.0F;
      float v7 = delta * 2.5F;
      float temp1;
      if (Math.abs(v6 - this.f1580) <= v7) {
         temp1 = v6;
      } else {
         temp1 = this.f1580;
         float temp2 = Math.signum(v6 - this.f1580) * v7;
         int v8 = -463594354;

         temp1 += temp2;
      }

      this.f1580 = temp1;
   }

   public float fogDrift() {

      return this.f1578;
   }

   private void m1907(qi feature, ClientWorld world, float delta) {
      float v4;
      if (!feature.isEnabled()) {
         v4 = 0.0F;
      } else if ("Manual".equals(feature.stormSource.getValue())) {
         v4 = feature.stormPower.getValue();
      } else if (world != null) {
         v4 = MathHelper.method_15363(world.method_8430(1.0F) * 0.45F + world.method_8478(1.0F) * 0.85F, 0.0F, 1.0F);
      } else {
         v4 = 0.0F;
      }

      float v5 = delta * 0.5F;
      float temp1;
      if (Math.abs(v4 - this.f1571) <= v5) {
         temp1 = v4;
      } else {
         temp1 = this.f1571;
         float temp2 = Math.signum(v4 - this.f1571);

         temp1 += temp2 * v5;
      }

      this.f1571 = temp1;
   }

   public kt.lt activeStrike() {
      kt.lt v1 = null;

      for (kt.lt result : this.f1568) {
         int v4 = -1231886982;
         int temp1 = v4 * (v4 + 1);

         kt.lt v3 = result;
         if (v3.age <= 0.45F && (v1 == null || v3.age < v1.age)) {
            v1 = v3;
         }
      }

      return v1;
   }

   @Environment(EnvType.CLIENT)
   public static final class lt {
      public double bottomX;
      public float seed;
      public double distance;
      public double topX;
      public double topZ;
      public double topY;
      public double bottomY;
      public double bottomZ;
      float falloff = 1.0F;
      float age;
      boolean manual;

      
   }
}
