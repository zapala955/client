package ru.white.utils.helper;

import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.vr;
import ru.white.utils.system.Hu;

@Environment(EnvType.CLIENT)
public final class Ra {
   private static final float HOVER_SMOOTH_TIME = 0.07F;
   private static final float DIM_ALPHA = 0.32F;
   private static final float LABEL_SIZE = 10.0F;
   private static final int COLOR_CARD = -15197925;
   private static final float SENSITIVITY = 3.4F;
   private static final List<ka> ENTRIES = qa.ALL;
   private static final ea openAnim = new ea();
   private static boolean f19;
   private static final float ICON_SIZE = 20.0F;
   private static final float OPEN_SMOOTH_TIME = 0.085F;
   private static final float CLOSE_SMOOTH_TIME = 0.14F;
   private static final ea hubGlow = new ea();
   private static final int COLOR_CARD_HOVER = -14803166;
   private static final float HUB_RADIUS = 30.0F;
   private static final int COLOR_PANEL = -15526890;
   private static float f21;
   private static final float RADIUS_FRACTION = 0.155F;
   private static final float MAX_OFFSET = 66.0F;
   private static float f20;
   private static final ea[] hoverAnim = new ea[ENTRIES.size()];
   private static final float DEADZONE = 15.0F;
   private static boolean f18;
   private static int f22 = -1;
   private static final float ICON_BUTTON_SIZE = 40.0F;
   private static final int COLOR_OUTLINES_MEDIUM = 352321535;

   public static void open() {
      f18 = true;
      f20 = 0.0F;

      f21 = 0.0F;
      f22 = -1;
   }

   private static float m78(int index, 

      double v4 = (double)index;

      return (float)((-Math.PI / 2) + v4 * ((Math.PI * 2) / (double)count));
   }

   private Ra() {
   }

   public static void requestCancel() {
      f19 = true;
   }

   public static void render(DrawContext context) {
      long v1 = System.nanoTime();
      float v3 = openAnim.update(f18 ? 1.0F : 0.0F, f18 ? 0.085F : 0.14F, v1);
      if (!(v3 <= 0.002F)) {
         int v4 = context.method_51421();
         int v5 = context.method_51443();
         int v6 = ENTRIES.size();
         float v7 = (float)Math.sqrt((double)(f20 * f20 + f21 * f21));

         f22 = -1;
         if (f18 && v7 > 15.0F) {
            float v8 = (float)Math.atan2((double)f21, (double)f20);
            float v9 = Float.MAX_VALUE;

            for (int v10 = 0; v10 < v6; v10++) {
               float v11 = m78(v10, v6);
               float v12 = Math.abs(m79(v8, v11));
               if (v12 < v9) {
                  v9 = v12;
                  f22 = v10;
               }
            }
         }

         float v23 = hubGlow.update(f22 >= 0 ? 1.0F : 0.0F, 0.08F, v1);
         float v24 = (float)v4 / 2.0F;
         float v25 = (float)v5 / 2.0F;
         float v26 = (float)Math.min(v4, v5) * 0.155F;
         int v27 = up.getColor(0, 0.32F * v3);
         vr.rect(0.0F, 0.0F, (float)v4, (float)v5, v27);
         Matrix3x2fStack v13 = context.method_51448();
         v13.pushMatrix();
         v13.translate(v24, v25);
         float v14 = 0.55F + 0.45F * m80(v3);
         v13.scale(v14, v14);
         v13.translate(-v24, -v25);
         if (v7 > 2.0F) {
            float v15 = Math.min(v7, 66.0F) * (v26 / 66.0F);
            float v16 = (float)Math.atan2((double)f21, (double)f20);
            v13.pushMatrix();
            v13.translate(v24, v25);
            v13.rotate(v16);
            int v17 = up.applyOpacity(up.client(), 0.5F * v3);
            vr.rect(0.0F, -0.75F, v15, 1.5F, v17, 0.75F);
            v13.popMatrix();
         }

         for (int v28 = 0; v28 < v6; v28++) {
            float v29 = m78(v28, v6);
            float v30 = v24 + (float)Math.cos((double)v29) * v26;
            float v18 = v25 + (float)Math.sin((double)v29) * v26;
            boolean v19 = v28 == f22;
            ea result = hoverAnim[v28];
            float v31 = v19 ? 1.0F : 0.0F;

            float v20 = result.update(v31, 0.07F, v1);
            m76(ENTRIES.get(v28), v30, v18, v20, v3);
         }

         m77(v24, v25, v23, v3);
         v13.popMatrix();
      }
   }

   public static void close(MinecraftClient EventItemUse) {
      if (f18) {
         f18 = false;
         boolean result;
         if (!f19) {
            result = true;
         } else {
            result = false;
            
         }

         boolean v1 = result;
         f19 = false;
         if (v1) {

            if (f22 >= 0 && f22 < ENTRIES.size() && mc.field_1724 != null) {
               la v5 = la.INSTANCE;
               UUID v7 = mc.field_1724.method_5667();

               v5.play(v7, ENTRIES.get(f22));
            }
         }
      }
   }

   private static float m80(float x) {
      float v1 = 1.70158F;
      float v2 = v1 + 1.0F;
      float v3 = x - 1.0F;
      float result = 1.0F + v2 * v3 * v3 * v3;

      return result + v1 * v3 * v3;
   }

   public static boolean isOpen() {

      return f18;
   }

   private static float m79(float a, float b) {
      float v2 = (a - b) % (float) (Math.PI * 2);
      if ((double)v2 > Math.PI) {
         v2 -= (float) (Math.PI * 2);
      } else if ((double)v2 < -Math.PI) {

         int v4 = -1434069126;

         v2 += (float) (Math.PI * 2);
      }

      return v2;
   }

   private static void m77(float centerX, float centerY, float hubT, float openT) {
      float v4 = 30.0F * (1.0F + 0.08F * hubT);
      int v5 = up.applyOpacity(-15526890, openT * 0.92F);
      int v6 = up.interpolateColor(352321535, up.client(), hubT);
      int result = up.applyOpacity(v6, openT);

      int v7 = result;
      vr.rect(centerX - v4, centerY - v4, v4 * 2.0F, v4 * 2.0F, v5, v4);
      float v13 = centerX - v4;

      vr.outline(v13, centerY - v4, v4 * 2.0F, v4 * 2.0F, 1.5F, v7, v4);
      String v8 = f22 >= 0 ? ENTRIES.get(f22).displayName() : "Emotions";
      if (hubT > 0.05F) {

         result = up.applyOpacity(-1, openT);
      } else {
         result = up.applyOpacity(-6907500, openT);
      }

      int v9 = result;
      hu.sfsemi.drawCentered(v8, centerX, centerY - 5.0F, 10.0F, v9);
   }

   public static boolean onMouseTurn(double yawDelta, double pitchDelta) {
      if (!f18) {
         return false;
      } else {
         float temp1 = (float)yawDelta;

         f20 += temp1 * 3.4F;
         f21 += (float)pitchDelta * 3.4F;
         float v4 = (float)Math.sqrt((double)(f20 * f20 + f21 * f21));
         if (v4 > 66.0F) {
            float v5 = 66.0F / v4;
            f20 *= v5;

            f21 *= v5;
         }

         return true;
      }
   }

   private static void m76(ka entry, float centerX, float centerY, float hoverT, float openT) {
      float v5 = 40.0F * (1.0F + 0.22F * hoverT);
      float v6 = v5 / 2.0F;
      int v7 = up.interpolateColor(-15197925, -14803166, hoverT);
      int v8 = up.applyOpacity(v7, openT);

      int v9 = up.interpolateColor(352321535, up.client(), hoverT);
      int v10 = up.applyOpacity(v9, openT);
      vr.rect(centerX - v6, centerY - v6, v5, v5, v8, v6);
      vr.outline(centerX - v6, centerY - v6, v5, v5, Math.max(1.0F, 1.5F * (1.0F + hoverT)), v10, v6);
      float v11 = 20.0F * (1.0F + 0.1F * hoverT);
      int v12 = up.applyOpacity(-1, openT);
      vr.texture(entry.icon(), centerX - v11 / 2.0F, centerY - v11 / 2.0F, v11, v11, v12);
      if (hoverT > 0.05F) {
         float v13 = centerY + v6 + 6.0F;
         int v14 = up.applyOpacity(-1184021, openT * hoverT);
         hu.sfsemi.drawCentered(entry.displayName(), centerX, v13, 10.0F, v14);
      }
   }

   static {
      for (int v0 = 0; v0 < hoverAnim.length; v0++) {
         hoverAnim[v0] = new ea();
      }
   }

   
}
