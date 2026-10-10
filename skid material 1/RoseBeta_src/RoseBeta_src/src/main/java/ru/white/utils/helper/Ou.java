package ru.white.utils.helper;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Ou {
   private final List<ou.tu> f1685;
   private ou.ru f1689;
   private final wu f1683 = new wu();
   private int f1686;
   private int f1687;
   private boolean f1688;
   private final List<ou.su> f1684 = Lists.newCopyOnWriteArrayList();

   public ou addTickStep(int ticks, uu action, BooleanSupplier condition, int priority) {
      List result = this.f1685;
      ou.tu temp1 = new ou.tu;

      temp1./* $VF: Unable to resugar constructor */<init>(ticks, action, condition, priority);
      result.add(temp1);
      int v5 = -886534663;
      int v8 = v5 * (v5 + 1);

      Collections.sort(this.f1685);
      return this;
   }

   public void update() {
      if ((!this.f1684.isEmpty() || !this.f1685.isEmpty()) && !this.f1688) {
         List result = this.f1684;
         Consumer temp1 = step -> {

            if (this.f1686 < this.f1684.size()) {
               ou.su v2x = this.f1684.get(this.f1686);
               if (v2x.condition().getAsBoolean()) {

                  wu v6 = this.f1683;
                  double v7 = (double)v2x.delay();

                  if (v6.finished(v7)) {
                     v2x.action().perform();
                     this.f1686++;
                     this.resetTime();
                     if (this.f1689.shouldLoop(this.f1686, this.f1684.size())) {
                        this.resetStepIndex();
                        this.f1689.onLoop();
                     }
                  }
               }
            }
         };

         result.forEach(temp1);
         this.f1685.forEach(step -> {
            if (this.f1687 < this.f1685.size()) {
               ou.tu v2x = this.f1685.get(this.f1687);

               if (v2x.condition().getAsBoolean() && v2x.ticks() <= 0) {
                  v2x.action().perform();
                  this.f1687++;
                  this.resetTime();
                  if (this.f1689.shouldLoop(this.f1687, this.f1685.size())) {
                     this.resetStepIndex();

                     this.f1689.onLoop();
                  }
               }

               v2x.decrementTicks();
            }
         });
         this.f1686 = Math.min(this.f1686, this.f1684.size());
         this.f1687 = Math.min(this.f1687, this.f1685.size());
         
      }
   }

   public ou addTickStep(int ticks, uu action, BooleanSupplier condition) {

      return this.addTickStep(ticks, action, condition, 0);
   }

   public ou addTickStep(int ticks, uu action) {
      ou result = this.addTickStep(ticks, action, () -> {

         return true;
      }, 0);
      int v3 = 816944431;
      int v4 = -158470266;
      int temp1 = v4 * (v4 + 1);

      return result;
   }

   @Generated
   public void setCurrentStepIndex(int currentStepIndex) {
      this.f1686 = currentStepIndex;
   }

   public ou addTickStep(int ticks, uu action, int priority) {
      BooleanSupplier temp3 = () -> {

         return true;
      };
      int v4 = 1715892867;
      int temp5 = v4 * (v4 + 1);

      return this.addTickStep(ticks, action, temp3, priority);
   }

   @Generated
   public ou.ru getLoopStrategy() {
      return this.f1689;
   }

   @Generated
   public List<ou.tu> getScriptTickSteps() {

      return this.f1685;
   }

   public ou addStep(int delay, uu action, int priority) {
      BooleanSupplier temp3 = () -> {
         int v0 = 808605777;

         return true;
      };

      return this.addStep(delay, action, temp3, priority);
   }

   @Generated
   public List<ou.su> getScriptSteps() {

      return this.f1684;
   }

   @Generated
   public 

      return this.f1686;
   }

   @Generated
   public void setCurrentTickStepIndex(int currentTickStepIndex) {
      this.f1687 = currentTickStepIndex;

      
   }

   public void resetStepIndex() {
      this.f1686 = 0;
      this.f1687 = 0;
   }

   @Generated
   public boolean isInterrupt() {
      int v1 = -1841074184;

      return this.f1688;
   }

   @Generated
   public int getCurrentTickStepIndex() {
      return this.f1687;
   }

   public ou addStep(int delay, uu action) {

      BooleanSupplier v5 = () -> true;

      return this.addStep(delay, action, v5, 0);
   }

   public ou addStep(int delay, uu action, BooleanSupplier condition, int priority) {
      List result = this.f1684;
      ou.su temp1 = new ou.su;

      temp1./* $VF: Unable to resugar constructor */<init>(delay, action, condition, priority);
      result.add(temp1);

      Collections.sort(this.f1684);
      return this;
   }

   public boolean isFinished() {

      boolean result;
      if (this.f1686 >= this.f1684.size() && this.f1687 >= this.f1685.size() && !this.f1688 && this.f1689.isFinished()) {

         result = true;
      } else {
         result = false;
      }

      return result;
   }

   public ou cleanup() {
      this.f1684.clear();
      this.f1685.clear();
      this.resetTime();
      int v1 = 1955270751;
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      this.resetStepIndex();
      return this;
   }

   public void resetTime() {

      this.f1683.reset();
   }

   public ou addStep(int delay, uu action, BooleanSupplier condition) {

      return this.addStep(delay, action, condition, 0);
   }

   @Generated
   public wu getTime() {
      return this.f1683;
   }

   public Ou() {
      this.f1685 = Lists.newCopyOnWriteArrayList();
      this.f1689 = new ou.pu(1);
      this.cleanup();
   }

   public ou setLoopStrategy(ou.ru loopStrategy) {
      int v2 = 54311682;

      this.f1689 = loopStrategy;
      return this;
   }

   @Generated
   public void setInterrupt(boolean interrupt) {

      this.f1688 = interrupt;
   }

   @Environment(EnvType.CLIENT)
   public static class pu implements ou.ru {
      private int f1674;
      private final int f1673;

      @Override
      public boolean isFinished() {

         boolean v4;
         if (this.f1674 >= this.f1673) {

            v4 = true;
            
         } else {
            v4 = false;
         }

         return v4;
      }

      @Override
      public void onLoop() {

         this.f1674++;
      }

      @Override
      public boolean shouldLoop(int currentStepIndex, int totalSteps) {
         return currentStepIndex >= totalSteps && this.f1674 < this.f1673;
      }

      public pu(int loopCount) {
         this.f1673 = loopCount - 1;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static class qu implements ou.ru {
      @Override
      public boolean shouldLoop(int currentStepIndex, int totalSteps) {
         boolean result;
         if (currentStepIndex >= totalSteps) {
            result = true;
            
         } else {
            result = false;
         }

         return result;
      }

      @Override
      public void onLoop() {
      }

      @Override
      public boolean isFinished() {

         return false;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public interface ru {
      boolean isFinished();

      boolean shouldLoop(int v1, int v2);

      void onLoop();
   }

   @Environment(EnvType.CLIENT)
   public static final class su implements Comparable<ou.su> {
      private int f1675;
      private int f1678;
      private uu f1676;
      private BooleanSupplier f1677;

      @Generated
      public int priority() {
         int v1 = 251445773;
         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return this.f1678;
      }

      @Generated
      public ou.su delay(int delay) {
         this.f1675 = delay;

         return this;
      }

      @Generated
      public int delay() {
         return this.f1675;
      }

      @Generated
      public BooleanSupplier condition() {

         int v1 = 1455225341;
         int v4 = v1 * (v1 + 1);

         return this.f1677;
      }

      public su(int delay, uu action, BooleanSupplier condition, int priority) {
         this.f1675 = delay;
         this.f1676 = action;
         this.f1677 = condition;
         this.f1678 = priority;
      }

      @Generated
      public ou.su action(uu action) {
         this.f1676 = action;
         int v2 = 269632586;
         int result = v2 * (v2 + 1);

         return this;
      }

      @Generated
      public uu action() {
         int v1 = 564196574;

         return this.f1676;
      }

      @Generated
      public ou.su condition(BooleanSupplier condition) {
         this.f1677 = condition;
         return this;
      }

      @Generated
      public ou.su priority(int priority) {
         this.f1678 = priority;

         return this;
      }

      public int compareTo(ou.su otherStep) {
         int result = Integer.compare(otherStep.priority(), this.priority());
         int v2 = -65064955;
         int temp1 = v2 * (v2 + 1) % 2;

         if (temp1 != 0) {
            }

         return result;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static final class tu implements Comparable<ou.tu> {
      private int f1679;
      private uu f1680;
      private BooleanSupplier f1681;
      private int f1682;

      @Generated
      public ou.tu ticks(int ticks) {
         this.f1679 = ticks;
         return this;
      }

      @Generated
      public uu action() {
         int v1 = -138428583;

         return this.f1680;
      }

      @Generated
      public ou.tu priority(int priority) {
         this.f1682 = priority;
         return this;
      }

      public void decrementTicks() {
         int v1 = -1173712734;
         int temp2 = v1 * (v1 + 1) % 2;

         if (temp2 != 0) {
            }

         this.f1679--;
      }

      public tu(int ticks, uu action, BooleanSupplier condition, int priority) {
         this.f1679 = ticks;
         this.f1680 = action;
         this.f1681 = condition;
         this.f1682 = priority;
      }

      @Generated
      public ou.tu action(uu action) {
         this.f1680 = action;
         int v2 = -1231941704;
         int temp2 = v2 + 1;

         return this;
      }

      @Generated
      public 

         return this.f1682;
      }

      @Generated
      public ou.tu condition(BooleanSupplier condition) {
         this.f1681 = condition;
         int v2 = -1160894740;
         int temp1 = v2 * (v2 + 1);
         int v3 = -1622603663;
         int temp2 = v3 * (v3 + 1) % 2;

         if (temp2 != 0) {
            }

         return this;
      }

      @Generated
      public int ticks() {
         int v1 = -92552954;

         int v4 = v1 + 1;

         return this.f1679;
      }

      public int compareTo(ou.tu otherStep) {
         int result = otherStep.priority();

         return Integer.compare(result, this.priority());
      }

      @Generated
      public BooleanSupplier condition() {

         return this.f1681;
      }

      
   }
}
