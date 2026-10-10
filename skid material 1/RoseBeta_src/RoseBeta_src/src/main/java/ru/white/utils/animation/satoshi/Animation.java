package ru.white.utils.animation.satoshi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public abstract class Animation {
   protected int duration;
   protected double endPoint;
   protected cp direction;
   public bp timerUtil = new bp();

   public void setEndPoint(double endPoint) {
      this.endPoint = endPoint;

      int v3 = -144410628;
      int result = v3 * (v3 + 1);

      
   }

   public Animation(int ms, double endPoint, cp direction) {
      this.duration = ms;
      this.endPoint = endPoint;
      this.direction = direction;
   }

   public void setDuration(int duration) {
      this.duration = duration;
      int v2 = 1067070969;
      int result = v2 * (v2 + 1);
      int v3 = -660975129;

      
   }

   public long getTimePassed() {

      long result = this.timerUtil.getTimePassed();

      return result;
   }

   public void changeDirection() {
      int v1 = -763942447;

      this.setDirection(this.direction.opposite());
   }

   public void reset() {

      this.timerUtil.reset();
   }

   public cp getDirection() {

      return this.direction;
   }

   public boolean isDone() {

      boolean result = this.timerUtil.hasReached((double)this.duration);

      return result;
   }

   public Animation(int ms, double endPoint) {
      this.duration = ms;
      this.endPoint = endPoint;
      this.direction = cp.FORWARDS;
   }

   public float getOutput() {
      if (this.direction == cp.FORWARDS) {
         boolean result = this.isDone();

         return result ? (float)this.endPoint : (float)(this.getEquation((double)this.timerUtil.getTimePassed()) * this.endPoint);
      } else if (this.isDone()) {
         return 0.0F;
      } else if (this.correctOutput()) {
         double v1 = (double)Math.min((long)this.duration, Math.max(0L, (long)this.duration - this.timerUtil.getTimePassed()));
         return (float)(this.getEquation(v1) * this.endPoint);
      } else {
         return (float)((1.0 - this.getEquation((double)this.timerUtil.getTimePassed())) * this.endPoint);
      }
   }

   public double getLinearOutput() {
      double temp1 = (double)this.timerUtil.getTimePassed();

      double temp2 = (double)this.duration;

      return 1.0 - temp1 / temp2 * this.endPoint;
   }

   public boolean finished(cp direction) {
      boolean result;
      if (this.isDone() && this.direction.equals(direction)) {
         result = true;
      } else {
         result = false;
         int v2 = 25251782;
         int temp1 = v2 * (v2 + 1);

         
      }

      return result;
   }

   public void setDirection(cp direction) {

      if (this.direction != direction) {
         this.direction = direction;
         bp result = this.timerUtil;
         long temp1 = System.currentTimeMillis();
         long v6 = (long)this.duration;
         long temp3 = (long)this.duration;

         result.setTime(temp1 - (v6 - Math.min(temp3, this.timerUtil.getTimePassed())));
      }
   }

   protected boolean correctOutput() {
      return false;
   }

   public double getEndPoint() {

      return this.endPoint;
   }

   protected abstract double getEquation(double v1);

   
}
