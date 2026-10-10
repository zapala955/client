package ru.white.utils.animation;

import java.io.PrintStream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Animation {
   private double fromValue;
   private Runnable finishAction;
   private double toValue;
   private double duration;
   private boolean debug;
   private double prevValue;
   private double value;
   private long start;
   private zo easing = ap.LINEAR;

   public Animation run(double valueTo, double duration, boolean safe) {

      return this.run(valueTo, duration, ap.LINEAR, safe);
   }

   @Generated
   public double getDuration() {
      int v2 = 164632957;

      return this.duration;
   }

   public Animation setToValue(double toValue) {
      this.toValue = toValue;

      return this;
   }

   public float getPrev() {
      float result = (float)this.getPrevValue();
      int v1 = 676015357;

      return result;
   }

   public boolean isFinished() {
      boolean result;
      if (this.calculatePart() >= 1.0) {
         result = true;
         int v1 = 2067165348;
         int temp1 = v1 * (v1 + 1);

         
      } else {
         result = false;
         
      }

      return result;
   }

   public Animation onFinished(Runnable action) {
      this.finishAction = action;
      int v2 = -613392235;

      return this;
   }

   public boolean check(boolean safe, double valueTo) {
      if (safe && this.isAlive()) {
         if (valueTo == this.getFromValue() || valueTo == this.getToValue()) {
            return true;
         }

         if (valueTo == this.getValue()) {

            return true;
         }
      }

      return false;
   }

   public Animation setFromValue(double fromValue) {
      this.fromValue = fromValue;
      return this;
   }

   @Generated
   public double getPrevValue() {
      return this.prevValue;
   }

   public double interpolate(double start, double end, double pct) {
      int v8 = 1956594816;

      double temp1 = end - start;

      return start + temp1 * pct;
   }

   @Generated
   public boolean isDebug() {
      int v1 = -455060352;
      int v2 = 1778993591;

      return this.debug;
   }

   public Animation run(double valueTo, double duration) {
      Animation result = this.run(valueTo, duration, ap.LINEAR, false);

      return result;
   }

   public float get() {
      return (float)this.getValue();
   }

   public boolean isAlive() {
      boolean result;
      if (!this.isFinished()) {
         result = true;
         
      } else {
         result = false;
         int v1 = 1994997402;
         int temp2 = v1 + 1;

         
      }

      return result;
   }

   public Animation setDebug(boolean debug) {

      this.debug = debug;
      return this;
   }

   @Generated
   public zo getEasing() {

      return this.easing;
   }

   @Generated
   public double getFromValue() {
      return this.fromValue;
   }

   @Generated
   public double getValue() {
      return this.value;
   }

   public double calculatePart() {
      double result = (double)(System.currentTimeMillis() - this.getStart());

      return result / this.getDuration();
   }

   @Generated
   public Runnable getFinishAction() {
      return this.finishAction;
   }

   public Animation setDuration(double duration) {

      this.duration = duration;
      return this;
   }

   @Generated
   public double getToValue() {
      int v2 = 1273841731;
      int temp1 = v2 * (v2 + 1);

      return this.toValue;
   }

   public Animation setStart(long start) {

      this.start = start;
      int v3 = 1610852183;
      int result = v3 * (v3 + 1) % 2;

      if (result != 0) {
         }

      return this;
   }

   public Animation run(double valueTo, double duration, zo easing) {
      return this.run(valueTo, duration, easing, false);
   }

   @Generated
   public long getStart() {

      return this.start;
   }

   public void set(double value) {
      this.run(value, 0.0);
      this.update();
      this.setValue(value);
      
   }

   public Animation setValue(double value) {
      int v3 = 705045315;

      this.value = value;
      return this;
   }

   public Animation run(double valueTo, double duration, zo easing, boolean safe) {
      if (this.check(safe, valueTo)) {

         if (this.isDebug()) {
            System.out.println("Animate cancelled due to target val equals from val");
         }
      } else {
         Animation v15 = this.setEasing(easing).setDuration(duration * 1000.0).setStart(System.currentTimeMillis());

         v15.setFromValue(this.getValue()).setToValue(valueTo);
         if (this.isDebug()) {
            PrintStream v16 = System.out;
            double v17 = this.getToValue();
            double v7 = this.getDuration();
            double v9 = this.getValue();
            double v11 = v17;
            v16.println(
               "#animate {\n    to value: "
                  + v11
                  + "\n    from value: "
                  + v9
                  + "\n    duration: "
                  + v7
                  + "\n}"
            );
         }
      }

      return this;
   }

   public Animation setEasing(zo easing) {
      this.easing = easing;
      return this;
   }

   public Animation setPrevValue(double prevValue) {
      this.prevValue = prevValue;

      return this;
   }

   public Animation() {
      this.debug = false;
   }

   public boolean update() {
      this.setPrevValue(this.getValue());

      boolean v1 = this.isAlive();
      if (v1) {

         this.setValue(this.interpolate(this.getFromValue(), this.getToValue(), this.getEasing().ease(this.calculatePart())));
      } else {
         this.setStart(0L);
         this.setValue(this.getToValue());
         if (this.finishAction != null) {
            this.finishAction.run();
            this.finishAction = null;
         }
      }

      return v1;
   }

   
}
