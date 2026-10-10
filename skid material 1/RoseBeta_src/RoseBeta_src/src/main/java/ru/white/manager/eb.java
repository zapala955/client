package ru.white.manager;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import ru.white.utils.helper.Ou;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Vf;
import ru.white.module.Module;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.rotation.RotationUtil;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.vs;
import ru.white.module.setting.Setting;
import ru.white.utils.IMinecraft;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventJump;
import ru.white.event.impl.EventChatMessage;

@Environment(EnvType.CLIENT)
public class eb extends ud implements IMinecraft {
   public static List<eb.gb> lines = new CopyOnWriteArrayList<>();
   private static final float SNAP = 5.0F;
   private final Animation f54;
   private final ou f49;
   private final List<Float> f51;
   private final Animation f52;
   private final Vector2f f48 = new Vector2f();
   private final List<Float> f50;
   private static final float GRID = 8.0F;
   private final Animation f56;
   private final Animation f53;
   private final List<Module> modules = new CopyOnWriteArrayList<>();
   private vf f47;
   private final Animation f55;

   @Generated
   public Animation getGuideVPos() {
      return this.f55;
   }

   @Generated
   public ou getScript() {

      return this.f49;
   }

   private void m124(int width, int height, float alpha) {
      if (!(alpha <= 0.01F)) {
         float v4 = this.f53.get() * alpha;
         float v5 = this.f54.get() * alpha;
         if (v4 > 0.01F) {
            int v6 = up.getColor(255, 120, 200, (int)(200.0F * v4));
            vs.ys.rect(this.f55.get() - 0.3F, 0.0F, 0.6F, (float)height, v6);
         }

         if (v5 > 0.01F) {
            int result = 255;

            int v10 = up.getColor(result, 120, 200, (int)(200.0F * v5));
            float temp1 = this.f56.get() - 0.3F;
            float v11 = (float)width;

            vs.ys.rect(0.0F, temp1, v11, 0.6F, v10);
         }
      }
   }

   public eb() {
      this.f49 = new ou();
      this.f50 = new CopyOnWriteArrayList<>();
      this.f51 = new CopyOnWriteArrayList<>();
      this.f52 = new Animation();
      this.f53 = new Animation();
      this.f54 = new Animation();
      this.f55 = new Animation();
      this.f56 = new Animation();
   }

   private void m122() {
      boolean v1 = !this.f50.isEmpty();
      boolean v2 = !this.f51.isEmpty();
      this.f53.update();
      this.f54.update();
      this.f55.update();
      this.f56.update();
      if (v1) {

         float v3 = this.f50.get(0);

         float v10;
         int result = (v10 = this.f53.get() - 0.01F) == 0.0F ? 0 : (v10 < 0.0F ? -1 : 1);

         if (result <= 0) {
            this.f55.set((double)v3);
         } else {
            this.f55.run((double)v3, 0.15, ap.BACK_OUT, true);
         }
      }

      if (v2) {
         float v7 = this.f51.get(0);
         if (this.f54.get() <= 0.01F) {
            this.f56.set((double)v7);
         } else {
            this.f56.run((double)v7, 0.15, ap.BACK_OUT, true);
         }
      }

      this.f53.run(v1 ? 1.0 : 0.0, 0.2, ap.SINE_OUT, true);
      this.f54.run(v2 ? 1.0 : 0.0, 0.2, ap.SINE_OUT, true);
   }

   private void m126(int width, 

      for (Module v4 : this.modules) {
         for (Setting v6 : v4.getSettings()) {
            if (v6 instanceof vf) {
               vf v7 = (vf)v6;
               if (v7.active) {
                  v7.position.x = (float)v7.animationX.getValue();
                  v7.position.y = (float)v7.animationY.getValue();
                  v7.position.x = Math.max(0.0F, Math.min((float)width - v7.size.x, v7.position.x));
                  v7.position.y = Math.max(0.0F, Math.min((float)height - v7.size.y, v7.position.y));
                  v7.targetPosition.x = Math.max(0.0F, Math.min((float)width - v7.size.x, v7.targetPosition.x));
                  v7.targetPosition.y = Math.max(0.0F, Math.min((float)height - v7.size.y, v7.targetPosition.y));
               }
            }
         }
      }
   }

   @EventTarget
   public void onEvent(EventChatMessage event) {
      if (this.f47 != null) {

         this.f49.cleanup().addTickStep(0, () -> {
            this.f47.targetPosition.set(this.f47.position);

            this.f47 = null;
         }, () -> this.f47 != null && this.f47.animationX.isFinished() && this.f47.animationY.isFinished());
      }
   }

   private void m129() {
      this.modules.clear();
      Stream result = Client.get().moduleManager().values().stream();
      Predicate temp1 = m -> {
         if (m.isEnabled()) {
            List var10000x = m.getSettings();

            if (var10000x.stream().anyMatch(s -> s instanceof vf)) {
               return true;
            }
         }

         return false;
      };
      int temp2 = v1 * (v1 + 1) % 2;

      if (temp2 != 0) {
         }

      result = result.filter(temp1);
      List v5 = this.modules;
      Objects.requireNonNull(this.modules);

      result.forEach(v5::add);
   }

   @Generated
   public Animation getGuideHAlpha() {

      return this.f54;
   }

   @Generated
   public Animation getGuideVAlpha() {

      return this.f53;
   }

   private void m127(MatrixStack matrices, int color) {
      for (eb.gb v6 : lines) {
         switch (v6.direction) {
            case VERTICAL:
               double v12 = Double.MAX_VALUE;
               float v13 = -this.f47.size.y;

               for (; v13 <= 0.0F; v13 += this.f47.size.y / 2.0F) {
                  if ((v13 != -this.f47.size.y / 2.0F || v6.center) && (v13 != -this.f47.size.y || v6.left) && (v13 != 0.0F || v6.right)) {
                     double v14 = (double)Math.abs(this.f47.targetPosition.y - (v6.position + v13));
                     if (v14 < (double)v6.distance && v14 < v12) {
                        v12 = v14;
                        this.f47.targetPosition.y = v6.position + v13;
                     }
                  }
               }
               break;
            case HORIZONTAL:
               double v3 = Double.MAX_VALUE;

               for (float v7 = -this.f47.size.x; v7 <= 0.0F; v7 += this.f47.size.x / 2.0F) {
                  if ((v7 != -this.f47.size.x / 2.0F || v6.center) && (v7 != -this.f47.size.x || v6.left) && (v7 != 0.0F || v6.right)) {
                     float v8 = Math.abs(this.f47.targetPosition.x - (v6.position + v7));
                     if (v8 < v6.distance && (double)v8 < v3) {
                        v3 = (double)v8;
                        this.f47.targetPosition.x = v6.position + v7;
                     }
                  }

                  
               }
         }
      }
   }

   @Generated
   public Animation getGuideHPos() {

      return this.f56;
   }

   private void m128() {
      List result = this.modules;
      Consumer temp1 = m -> {

         m.getSettings().forEach(s -> {
            if (s instanceof vf var10000x) {

               vf v2x = var10000x;
               v2x.animationX.update();
               v2x.animationY.update();
               if (this.f49.isFinished()) {
                  v2x.animationX.run((double)v2x.targetPosition.x, 0.06, true);
                  v2x.animationY.run((double)v2x.targetPosition.y, 0.06, true);
               }
            }
         });
      };

      result.forEach(temp1);
   }

   @Generated
   public Vector2f getOffset() {
      return this.f48;
   }

   @EventTarget
   public void onEvent(EventJump event) {
      if (event.getKey() == 0) {
         if (event.getScreen() instanceof ChatScreen) {
            for (Module v3 : this.modules) {
               for (Setting result : v3.getSettings()) {

                  if (result instanceof vf v6 && v6.active) {
                     Vector2f v7 = v6.position;
                     Vector2f v8 = v6.size;
                     double v17 = (double)((float)event.getMouseX());

                     Vector2f v9 = this.getMouse(v17, (double)((float)event.getMouseY()));
                     double v10 = (double)v9.x;
                     double v12 = (double)v9.y;
                     if (!v6.active) {
                        return;
                     }

                     if (!v6.structure) {

                        if (isHover(v10, v12, (double)v7.x, (double)v7.y, (double)v8.x, (double)v8.y)) {
                           this.f47 = v6;
                           this.f48.set((double)v7.x - v10, (double)v7.y - v12);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Generated
   public List<Module> getModules() {

      return this.modules;
   }

   private void m125() {
      float v1 = this.f47.targetPosition.x;
      float v2 = this.f47.targetPosition.y;
      float v3 = this.f47.size.x;
      float v4 = this.f47.size.y;
      float[] v5 = new float[]{v1, v1 + v3 / 2.0F, v1 + v3};
      float[] v6 = new float[]{v2, v2 + v4 / 2.0F, v2 + v4};
      Float v7 = null;
      float v8 = 5.0F;
      float v9 = 0.0F;
      Float v10 = null;
      float v11 = 5.0F;
      float v12 = 0.0F;

      for (Module v14 : this.modules) {
         for (Setting v16 : v14.getSettings()) {
            if (v16 instanceof vf) {
               vf v17 = (vf)v16;
               if (v17 != this.f47 && v17.active) {
                  float[] v18 = new float[]{v17.position.x, v17.position.x + v17.size.x / 2.0F, v17.position.x + v17.size.x};
                  float[] result = new float[]{v17.position.y, 0.0F, 0.0F};

                  result[1] = v17.position.y + v17.size.y / 2.0F;
                  result[2] = v17.position.y + v17.size.y;
                  float[] v19 = result;

                  for (float v23 : v18) {
                     for (float v27 : v5) {
                        float v28 = Math.abs(v27 - v23);
                        if (v28 < v8) {
                           v8 = v28;
                           v7 = v1 + (v23 - v27);
                           v9 = v23;
                        }
                     }
                  }

                  for (float v34 : v19) {
                     for (float v38 : v6) {
                        float v39 = Math.abs(v38 - v34);
                        if (v39 < v11) {
                           v11 = v39;
                           v10 = v2 + (v34 - v38);
                           v12 = v34;
                        }
                     }
                  }
               }
            }
         }
      }

      if (!this.f47.lockX) {
         if (v7 != null) {
            this.f47.targetPosition.x = v7;
            this.f50.add(v9);
         } else {
            this.f47.targetPosition.x = (float)Math.round(v1 / 8.0F) * 8.0F;
         }
      }

      if (v10 != null) {
         this.f47.targetPosition.y = v10;
         this.f51.add(v12);
      } else {
         this.f47.targetPosition.y = (float)Math.round(v2 / 8.0F) * 8.0F;
      }
   }

   public Vector2f getMouse(double mouseX, double mouseY) {
      Vector2f result = new Vector2f;
      double temp2 = mouseX * (double)mc.method_22683().method_4495();

      float v7 = (float)(temp2 / 2.0);

      result./* $VF: Unable to resugar constructor */<init>(v7, (float)(mouseY * (double)mc.method_22683().method_4495() / 2.0));
      return result;
   }

   @Generated
   public List<Float> getGuidesH() {
      return this.f51;
   }

   public static boolean isHover(double mouseX, double mouseY, double x, double y, double width, double height) {
      if (mouseX >= x && mouseX <= x + width && mouseY >= y) {

         double v15 = y + height;

         if (mouseY <= v15) {
            return true;
         }
      }

      return false;
   }

   @Generated
   public List<Float> getGuidesV() {

      return this.f50;
   }

   @Generated
   public Animation getGridAnim() {

      return this.f52;
   }

   private void m123(int width, int height, float alpha) {
      int v4 = up.getColor(255, 255, 255, (int)(16.0F * alpha));
      int result = 255;
      int temp4 = v8 + 1;

      int v5 = up.getColor(result, 255, 255, (int)(34.0F * alpha));
      float v7 = 0.0F;

      while (true) {

         if (!(v7 <= (float)width)) {
            v6 = 0;

            for (float v12 = 0.0F; v12 <= (float)height; v6++) {
               vs.ys.rect(0.0F, v12, (float)width, 0.5F, v6 % 5 == 0 ? v5 : v4);
               v12 += 8.0F;
            }

            int v13 = up.getColor(255, 120, 170, (int)(70.0F * alpha));
            vs.ys.rect((float)width / 2.0F, 0.0F, 0.6F, (float)height, v13);
            vs.ys.rect(0.0F, (float)height / 2.0F, (float)width, 0.6F, v13);
            return;
         }

         vs.ys.rect(v7, 0.0F, 0.5F, (float)height, v6 % 5 == 0 ? v5 : v4);
         v7 += 8.0F;
         v6++;
      }
   }

   public void post(Matrix3x2f matrices) {
      this.f49.update();
      Window v2 = mc.method_22683();
      float v3 = 2.0F;
      float v4 = (float)mc.method_22683().method_4495();
      float v5 = v3 / v4;
      Window result = mc.method_22683();

      int v6 = (int)((float)result.method_4486() / v5);
      int v7 = (int)((float)mc.method_22683().method_4502() / v5);
      boolean v8 = mc.field_1755 instanceof ChatScreen;
      if (!v8) {
         this.f47 = null;
      }

      this.m129();
      this.m128();
      boolean v9 = v8 && this.f47 != null && this.f47.active;
      this.f52.update();
      this.f52.run(v9 ? 1.0 : 0.0, 0.25, ap.SINE_OUT, true);
      if (this.f52.get() > 0.01F) {
         this.m123(v6, v7, this.f52.get());
      }

      this.f50.clear();
      this.f51.clear();
      boolean v10 = InputUtil.method_15987(mc.method_22683(), 342) || InputUtil.method_15987(mc.method_22683(), 346);
      if (this.f47 != null && this.f47.active) {
         float v11 = (float)(mc.field_1729.method_1603() / 2.0);
         float v12 = (float)(mc.field_1729.method_1604() / 2.0);
         float v13 = v11 + this.f48.x;
         float v14 = v12 + this.f48.y;
         if (!this.f47.lockX) {
            this.f47.targetPosition.x = v13;
         }

         this.f47.targetPosition.y = v14;
         lines.clear();
         if (!v10) {
            this.m125();
         }

         this.m126(v6, v7);
      }

      if (this.f52.get() > 0.01F) {
         String v17 = "ALT - свободное перемещение";

         hu.sf_bold
            .draw(
               v17,
               (float)v6 / 2.0F - hu.sf_bold.getWidth(v17, 15.0F) / 2.0F,
               (float)v7 / 2.0F + 120.0F,
               15.0F,
               up.getColor(255, 0.3F * this.f52.get())
            );
      }

      this.m122();
      this.m124(v6, v7, this.f52.get());
   }

   @Generated
   public vf getSelected() {

      return this.f47;
   }

   @Environment(EnvType.CLIENT)
   public static enum fb {
      VERTICAL,
      HORIZONTAL;
      // $VF: synthetic field
      private static final eb.fb[] $VALUES = m121();
   }

   @Environment(EnvType.CLIENT)
   public static class gb {
      public boolean right;
      public float distance;
      public float position;
      public boolean center;
      public boolean left;
      public eb.fb direction;

      @Generated
      public gb(float position, float distance, eb.fb direction, boolean center, boolean right, boolean left) {
         this.position = position;
         this.distance = distance;
         this.direction = direction;
         this.center = center;
         this.right = right;
         this.left = left;
      }

      
   }
}
