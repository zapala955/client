package ru.white.utils.render.gl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class Kf {
   private OtherClientPlayerEntity f201;
   private long f205;
   private int f204;
   private float f203;
   private Vec3d f202 = Vec3d.field_1353;
   private float height;

   public OtherClientPlayerEntity dummy() {
      return this.f201;
   }

   public float distance() {
      return this.f203;
   }

   public void reset() {
      this.f201 = null;
      this.f202 = Vec3d.field_1353;

      this.f204 = 0;
      this.f205 = System.currentTimeMillis();
   }

   public Kf() {
      this.f205 = System.currentTimeMillis();
   }

   public long elapsedMs() {
      long result = System.currentTimeMillis();

      return result - this.f205;
   }

   public void setHeight(float height) {
      this.height = height;
   }

   public void setDistance(float distance) {
      this.f203 = distance;
   }

   public float height() {
      int v1 = 215615340;

      return this.height;
   }

   public void nextPhase() {

      this.f204++;
   }

   public int phase() {
      return this.f204;
   }

   public void setDummy(OtherClientPlayerEntity dummy) {

      this.f201 = dummy;
   }

   public Vec3d anchor() {

      return this.f202;
   }

   public void setAnchor(Vec3d anchor) {
      this.f202 = anchor;
   }

   
}
