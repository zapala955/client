package ru.white.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.js;
import ru.white.utils.IMinecraft;
import ru.white.utils.system.Hu;

@Environment(EnvType.CLIENT)
public class tg extends Screen {
   private Runnable f353;
   private boolean f349;
   private boolean exit;
   private final hg f345;
   private int f351;
   private final Animation f354;
   private boolean f350;
   public static ru.white.utils.animation.satoshi.Animation alpha = new ep(250, 1.0);
   private final float[] f356;
   private final float[] f355;
   public double STEP;
   private long f348;
   private int f347;
   public float INNER_R;
   private Runnable f352;
   public float OUTER_R = 100.0F;
   public double GAP;
   private float f346;
   private static final double[] CENTERS = new double[]{-Math.PI / 2, Math.PI / 6, Math.PI * 5.0 / 6.0};
   public float MID_R;

   public void startSelectionAnimation(int slot, Runnable action) {
      if (!this.f349 && !this.f350 && !this.exit) {
         this.f349 = true;
         this.f351 = slot;
         this.f352 = action;
         this.f353 = action;
         this.f354.set(0.0);

         this.f354.run(1.0, 0.55, ap.SINE_OUT);
      }
   }

   private int m432(double angle) {
      double v3 = (Math.PI / 3) - this.GAP;

      for (int v5 = 0; v5 < 3; v5++) {
         double v6 = angle - CENTERS[v5];

         while (v6 > Math.PI) {
            v6 -= Math.PI * 2;
         }

         while (v6 < -Math.PI) {
            v6 += Math.PI * 2;
         }

         if (Math.abs(v6) <= v3) {

            return v5;
         }
      }

      return -1;
   }

   public void method_25394(DrawContext context, int mouseX, int mouseY, float delta) {
      this.f346 = 2.0F / (float)hp.mc.method_22683().method_4495();
      this.f347 = this.m433((double)mouseX, (double)mouseY);
      if ((this.exit || this.f350) && alpha.isDone()) {
         Runnable v56 = this.f353;
         this.f353 = null;
         this.exit = false;
         this.f350 = false;
         hp.mc.method_1507(null);
         if (v56 != null) {
            v56.run();
         }
      } else {
         float v5 = alpha.getOutput();
         float v6 = this.m430();
         float v7 = this.m431();
         boolean v8 = this.f345.isTripleAnimationsEnabled();
         if (this.f349) {
            this.f354.update();
            if (this.f354.isFinished()) {
               this.f349 = false;
               this.f352 = null;
               this.f350 = true;
               alpha.setDuration(700);
               alpha.setDirection(cp.BACKWARDS);
            }
         }

         float v9 = !this.f349 && !this.f350 ? 0.0F : MathHelper.method_15363(this.f354.get(), 0.0F, 1.0F);

         for (int v10 = 0; v10 < 3; v10++) {
            boolean v11 = this.f347 == v10 && !this.f350 && !this.exit;
            boolean v12 = (this.f349 || this.f350) && this.f351 == v10;
            this.f355[v10] = m439(this.f355[v10], v11 ? 1.0F : 0.0F, 0.08F);
            this.f356[v10] = m439(this.f356[v10], v12 ? 1.0F : 0.0F, 0.08F);
         }

         for (int v57 = 0; v57 < 3; v57++) {
            int v59 = v57;
            if (this.f347 >= 0) {
               if (v57 == 2) {
                  v59 = this.f347;
               } else {

                  for (int v13 = 0; v13 < 3; v13++) {
                     if (v13 != this.f347) {
                        if (v61 == v57) {
                           v59 = v13;
                           break;
                        }

                        v61++;
                     }
                  }
               }
            }

            double v62 = (Math.PI / 3) - this.GAP;
            double v14 = CENTERS[v59] - v62;
            double v16 = CENTERS[v59] + v62;
            boolean v18 = this.m434(v59);
            boolean v19 = (this.f349 || this.f350) && this.f351 != v59;
            float v20 = this.m438(v59);
            float v21 = v8 ? this.f355[v59] : (this.f347 == v59 ? 1.0F : 0.0F);
            float v22 = v8 ? this.f356[v59] : 0.0F;
            float v23 = v19 ? 1.0F - 0.82F * v9 : 1.0F;
            float v24 = v5 * v20 * v23;
            float v25 = v8 ? 0.42F + v20 * 0.58F : 1.0F;
            float v26 = (this.INNER_R * v25 - v21 * 3.0F - v22 * 5.0F) * this.f346;
            float v27 = (this.OUTER_R * v25 + v21 * 5.0F + v22 * 8.0F) * this.f346;
            float v28 = (v26 + v27) / 2.0F;
            float v29 = ((v8 ? (1.0F - v20) * -16.0F : 0.0F) + v21 * 3.0F + v22 * 5.0F) * this.f346;
            float v30 = v6 + (float)(Math.cos(CENTERS[v59]) * (double)v29);
            float v31 = v7 + (float)(Math.sin(CENTERS[v59]) * (double)v29);
            float v32 = v18 ? 210.0F : 150.0F;
            float v33 = v18 ? 50.0F : 150.0F;
            float v34 = v18 ? 40.0F : 150.0F;
            float v35 = v18 ? 0.55F : 0.2F;
            float v36 = 210.0F;
            float v37 = 195.0F;
            float v38 = 125.0F;
            float v39 = 0.38F;
            float v40 = 238.0F;
            float v41 = 210.0F;
            float v42 = 85.0F;
            float v43 = 0.48F;
            float v44 = v32 + (v36 - v32) * v21;
            float v45 = v33 + (v37 - v33) * v21;
            float v46 = v34 + (v38 - v34) * v21;
            float v47 = v35 + (v39 - v35) * v21;
            v44 += (v40 - v44) * v22;
            v45 += (v41 - v45) * v22;
            v46 += (v42 - v46) * v22;
            v47 += (v43 - v47) * v22;
            int v48 = m435(Math.round(v44), Math.round(v45), Math.round(v46), v47 * v24);
            this.m440(context, v30, v31, v26, v27, v14, v16, v48);
            ItemStack v49 = this.f345.tripleSlotItems[v59];
            if (v49 != null && !v49.method_7960()) {
               float v50 = v30 + (float)((double)v28 * Math.cos(CENTERS[v59]));
               float v51 = v31 + (float)((double)v28 * Math.sin(CENTERS[v59]));
               float result = 0.82F + v21 * 0.08F;

               float v52 = (result + v22 * 0.12F) * v20;
               js.drawItemCenteredWithContext(context, v49, v50, v51, v52, v24);
            }
         }

         String v58;
         if (this.f347 >= 0) {

            ItemStack v60 = this.f345.tripleSlotItems[this.f347];
            if (v60 != null && !v60.method_7960()) {
               String v53 = v60.method_7964().getString();
               v58 = v53
                  + "  —  отпусти для свапа  |  ПКМ — изменить";
            } else {
               v58 = "Пусто  —  ПКМ чтобы назначить предмет";
            }
         } else {
            v58 = "Двигай мышку в нужную сторону  |  ПКМ — назначить";
         }

         hu.sf_regular
            .drawCentered(
               v58,
               v6 / this.f346,
               v7 / this.f346 + this.OUTER_R + 16.0F,
               8.5F,
               m435(255, 255, 255, 0.55F * v5)
            );
         super.method_25394(context, mouseX, mouseY, delta);
      }
   }

   private float m438(int slot) {
      boolean result = this.f345.isTripleAnimationsEnabled();

      if (!result) {
         return 1.0F;
      } else {
         long v6 = System.currentTimeMillis() - this.f348;
         long v7 = (long)slot;

         v7 *= -5004029493728494750L + 5004029493728494835L;

         float v2 = (float)(v6 - v7) / 520.0F;
         return m437(v2);
      }
   }

   private int m433(double mx, double my) {
      double v5 = mx - (double)this.m430();
      double v7 = my - (double)this.m431();
      return Math.hypot(v5, v7) < (double)(12.0F * this.f346) ? -1 : this.m432(Math.atan2(v7, v5));
   }

   private static float m437(float value) {
      value = m436(value, 0.0F, 1.0F);

      return 1.0F - (float)Math.pow((double)(1.0F - value), 3.0);
   }

   protected void method_25426() {
      this.exit = false;

      this.f348 = System.currentTimeMillis();
      this.f349 = false;
      this.f350 = false;
      this.f351 = -1;

      this.f352 = null;
      this.f353 = null;
      this.f354.set(0.0);

      for (int v1 = 0; v1 < 3; v1++) {
         this.f355[v1] = 0.0F;
         this.f356[v1] = 0.0F;
      }

      alpha.setDuration(300);
      alpha.reset();
      alpha.setDirection(cp.FORWARDS);
   }

   private static float m439(float value, float target, float speed) {
      float v4;
      int result = (v4 = Math.abs(value - target) - 0.001F) == 0.0F ? 0 : (v4 < 0.0F ? -1 : 1);

      return result < 0 ? target : value + (target - value) * speed;
   }

   public boolean method_25402(Click click, boolean doubled) {
      if (click.method_74245() == 1) {
         int v3 = this.m433(click.comp_4798(), click.comp_4799());

         if (v3 >= 0) {
            hp.mc.method_1507(new sg(this.f345, v3, this));
            return true;
         }
      }

      return super.method_25402(click, doubled);
   }

   public tg(hg module) {
      super(Text.method_43470("Auto Swap"));
      this.INNER_R = 60.0F;
      this.MID_R = (this.OUTER_R + this.INNER_R) / 2.0F;
      this.GAP = Math.toRadians(1.0);
      this.STEP = Math.toRadians(0.5);
      this.f346 = 1.0F;
      this.exit = false;
      this.f347 = -1;
      this.f351 = -1;
      this.f354 = new Animation();
      this.f355 = new float[3];
      this.f356 = new float[3];
      this.f345 = module;
   }

   public boolean isSelectionAnimating() {
      if (!this.f349) {

         if (!this.f350 && !this.exit) {
            return false;
         }
      }

      return true;
   }

   public boolean method_25421() {

      return false;
   }

   public int getHoveredSlot() {
      return this.f347;
   }

   private float m431() {
      float result = (float)this.field_22790;

      return result / 2.0F;
   }

   private static int m435(int r, int g, int b, float a) {
      int result = Math.round(m436(a, 0.0F, 1.0F) * 255.0F) << 24 | r << 16 | g << 8;

      return result | b;
   }

   public void method_25420(DrawContext context, int mouseX, int mouseY, float delta) {
   }

   private float m430() {

      return (float)this.field_22789 / 2.0F;
   }

   private boolean m434(int i) {
      if (hp.mc.field_1724 == null) {
         return false;
      } else {
         ItemStack v2 = this.f345.tripleSlotItems[i];
         if (v2 != null) {
            if (!v2.method_7960()) {
               ItemStack v3 = hp.mc.field_1724.method_6079();
               return !v3.method_7960() && v3.method_7909() == v2.method_7909();
            }

            
         }

         return false;
      }
   }

   private static float m436(float value, float min, float max) {
      int temp2 = v4 + 1;

      return Math.max(min, Math.min(max, value));
   }

   public void startCloseAnimation(Runnable action) {
      if (!this.f350 && !this.exit) {

         this.f353 = action;
         this.f350 = true;
         alpha.setDuration(700);
         alpha.setDirection(cp.BACKWARDS);
      }
   }

   private void m440(DrawContext ctx, float cx, float cy, float innerR, float outerR, double startAngle, double endAngle, int color) {
      Matrix3x2fStack v11 = ctx.method_51448();
      double v12 = endAngle - startAngle;
      int v14 = Math.max(1, (int)Math.ceil(v12 / this.STEP));
      double v15 = v12 / (double)v14;
      double temp1 = (double)outerR * Math.sin(v15 / 2.0) + 1.5;

      int v17 = Math.max(1, (int)temp1);

      for (int v18 = 0; v18 < v14; v18++) {
         double v19 = startAngle + ((double)v18 + 0.5) * v15;
         v11.pushMatrix();
         v11.translate(cx, cy);
         v11.rotate((float)v19);
         ctx.method_25294((int)innerR, -v17, (int)outerR + 1, v17 + 1, color);
         v11.popMatrix();
      }
   }

   public boolean method_25422() {
      if (!this.exit) {
         alpha.setDuration(700);

         alpha.setDirection(cp.BACKWARDS);
         this.exit = true;
         
      }

      return false;
   }

   
}
