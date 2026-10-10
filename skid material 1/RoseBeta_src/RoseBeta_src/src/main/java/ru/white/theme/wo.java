package ru.white.theme;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.manager.ModuleManager;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.ClickGui;

@Environment(EnvType.CLIENT)
public final class wo {
   public static int getHudColor(float alpha) {
      int result = up.client();

      return up.replAlpha(result, alpha);
   }

   public static int getBackgroundColor() {
      Theme result = xo.get().getActive();

      return result.get(1);
   }

   public static int getSeparatorColor(float alpha) {
      int result = xo.get().getActive().get(6);

      return up.multAlpha(result, alpha);
   }

   public static 

      return up.getColor(175);
   }

   public static int getTextColor(float alpha) {
      int result = xo.get().getActive().get(3);

      return up.replAlpha(result, alpha);
   }

   public static boolean getDistortion() {
      try {
         if (Client.get() != null) {
            if result = Client.get().moduleManager();

            if (result != null) {
               result = Client.get().moduleManager();
               int v1 = -1865803910;
               int temp2 = v1 + 1;

               ClickGui v0 = result.get(ClickGui.class);
               if (v0 != null && v0.distortion != null && v0.distortion.getValue()) {
                  return true;
               }
            }
         }
      } catch (Exception v4) {
      }

      return xo.get().getActive().getBool(1);
   }

   public static int getOutlineColor(float alpha) {
      Theme result = xo.get().getActive();

      return up.multAlpha(result.get(2), alpha);
   }

   public static int getBlur() {
      try {
         Client result = Client.get();

         if (result != null && Client.get().moduleManager() != null) {
            Module v5 = Client.get().moduleManager().get(ClickGui.class);

            ClickGui v0 = (ClickGui)v5;
            if (v0 != null && v0.blurStrength != null) {
               return Math.round(v0.blurStrength.getValue());
            }
         }
      } catch (Exception v4) {
      }

      return Math.max(5, Math.round(xo.get().getActive().getSlider(1) * 5.0F));
   }

   public static int getLightBackgroundColor(float alpha) {
      Theme result = xo.get().getActive();
      int v1 = 81643010;

      return up.multAlpha(result.get(7), alpha);
   }

   public static float getOpacity() {
      return xo.get().getActive().getSlider(0);
   }

   public static int getSeparatorColor() {
      int result = 255;

      return up.getColor(result, 0.1F);
   }

   public static 

      int result = up.getColor(255);
      int v1 = 1433592260;
      int v3 = v1 * (v1 + 1) % 2;

      if (v3 != 0) {
         }

      return result;
   }

   public static boolean getShadow() {
      xo result = xo.get();
      int v1 = 1709134237;
      int temp1 = v1 * (v1 + 1);

      return result.getActive().getBool(0);
   }

   public static int getOutlineColor() {
      Theme result = xo.get().getActive();

      int v2 = result.get(2);

      return v2;
   }

   public static int getLightBackgroundColor() {
      Theme result = xo.get().getActive();

      int v1 = -910909684;

      return result.get(7);
   }

   private wo() {
   }

   public static int getBackgroundColor(float alpha) {
      int result = xo.get().getActive().get(1);

      return up.replAlpha(result, alpha);
   }

   public static int getVisualColor() {
      int result = up.client();

      return result;
   }

   public static int getDarkTextColor(float alpha) {
      Theme result = xo.get().getActive();
      int v1 = 928919577;

      return up.replAlpha(result.get(4), alpha);
   }

   public static int getHudColor() {
      int result = up.client();

      return result;
   }

   public static int byIndex(int index) {
      xo result = xo.get();

      Theme v3 = result.getActive();

      return v3.get(index);
   }

   
}
