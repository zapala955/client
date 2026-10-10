package ru.white.module.impl.display;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.js;
import ru.white.utils.render.vr;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public final class gh {
   public static int textDim(float alpha) {
      int result = 168;
      int temp1 = 160;
      int temp2 = 188;

      result = up.getColor(result, temp1, temp2, (int)(215.0F * alpha));

      return result;
   }

   public static float rowGap(float S) {

      return 2.5F * S;
   }

   public static void drawStatChip(float x, float y, float width, float height, float S, float alpha) {
      float v6 = chipRadius(S);
      vr.rect(x, y, width, height, bgChip(alpha), v6);
      int temp4 = 255;
      int temp5 = 255;
      int var10006 = 255;

      float var10007 = 8.0F * alpha;

      vr.rect(x, y, width, height, up.getColor(temp4, temp5, var10006, (int)var10007), v6);
      vr.outline(x, y, width, height, 0.5F * S, outlineSoft(alpha), v6);
   }

   public static float headerPill(float x, float y, float S, float alpha, yt iconFont, String iconGlyph, int iconColor, String title) {
      float v8 = headerHeight(S);
      float v9 = v8 / 2.0F;
      float v10 = 7.5F * S;
      float v11 = 6.8F * S;
      float v12 = 7.5F * S;
      float v13 = 4.0F * S;
      boolean v14 = iconFont != null && iconGlyph != null && !iconGlyph.isEmpty();
      float v15 = v14 ? iconFont.getWidth(iconGlyph, v10) : 0.0F;
      float v16 = hu.sf_bold.getWidth(title, v11);
      float v17 = v12 * 2.0F + v15 + (v14 ? v13 : 0.0F) + v16;
      vr.blur(x, y, v17, v8, 1.0F, v9, bgDeep(alpha));

      vr.glow(x, y, v17, v8, up.replAlpha(iconColor, 0.18F * alpha), v9, 6.0F * S, 0.5F);
      vr.rect(x, y, v17, v8, bgPanel(alpha), v9);
      vr.outline(x, y, v17, v8, 0.5F * S, up.replAlpha(iconColor, 0.25F * alpha), v9);
      float v18 = x + v12;
      float v19 = y + v8 / 2.0F;
      if (v14) {
         iconFont.draw(iconGlyph, v18, v19 - v10 / 2.0F - 0.5F * S, v10, iconColor);
         v18 += v15 + v13;
      }

      hu.sf_bold.draw(title, v18, v19 - v11 / 2.0F - 0.5F * S, v11, textMain(alpha));
      return v17;
   }

   public static void drawCircularCooldown(DrawContext context, float x, float y, ItemStack stack, String timerText, int accentColor, float S, float alpha) {
      float v8 = 18.0F * S;
      float v9 = v8 / 2.0F;
      int temp4 = up.replAlpha(accentColor, 0.3F * alpha);

      vr.glow(x, y, v8, v8, temp4, v9, 6.0F * S, 0.55F);
      vr.rect(x, y, v8, v8, up.getColor(26, 21, 36, (int)(235.0F * alpha)), v9);
      vr.outline(x, y, v8, v8, 0.9F * S, up.replAlpha(accentColor, 0.75F * alpha), v9);
      if (stack != null && !stack.method_7960()) {
         js.drawItemCenteredWithContext(context, stack, x + v9, y + v9, 0.65F * S, alpha);
      }

      float v10 = x + v8 + 4.5F * S;
      float v11 = 7.2F * S;
      float v12 = y + (v8 - v11) / 2.0F - 0.4F * S;
      hu.sf_bold.draw(timerText, v10, v12, v11, textMain(alpha));
   }

   public static int getPotionAccentColor(String name, boolean negative) {
      if (negative) {
         return up.getColor(235, 75, 75, 255);
      } else if (name == null) {
         return up.client();
      } else {
         String v2 = name.toLowerCase();
         if (v2.contains("скорост") || v2.contains("speed") || v2.contains("прыгуч") || v2.contains("jump")) {
            return up.getColor(50, 225, 130, 255);
         } else if (v2.contains("сил") || v2.contains("strength") || v2.contains("гнев") || v2.contains("anger")) {
            return up.getColor(245, 80, 80, 255);
         } else if (v2.contains("огнест") || v2.contains("fire") || v2.contains("золот") || v2.contains("gold")) {
            return up.getColor(255, 160, 40, 255);
         } else if (!v2.contains("сопротив") && !v2.contains("resist")) {
            if (!v2.contains("реген") && !v2.contains("regen")) {

               if (!v2.contains("исцел") && !v2.contains("heal")) {
                  if (!v2.contains("поглощ") && !v2.contains("absorp")) {
                     if (!v2.contains("невид") && !v2.contains("invis")) {
                        if (!v2.contains("спешк") && !v2.contains("haste")) {
                           if (!v2.contains("свечен") && !v2.contains("glow")) {
                              if (!v2.contains("замедл")
                                 && !v2.contains("slow")
                                 && !v2.contains("иссуш")
                                 && !v2.contains("wither")
                                 && !v2.contains("слабост")) {
                                 return up.client();
                              }

                              return up.getColor(235, 60, 60, 255);
                           }

                           return up.getColor(245, 235, 90, 255);
                        }

                        return up.getColor(235, 190, 60, 255);
                     }

                     int temp1 = 175;
                     int temp2 = 190;
                     int temp3 = 255;

                     return up.getColor(v5, temp1, temp2, temp3);
                  }

                  return up.getColor(245, 195, 45, 255);
               }
            }

            return up.getColor(245, 95, 175, 255);
         } else {
            return up.getColor(110, 135, 160, 255);
         }
      }
   }

   public static int outlineSoft(float alpha) {
      int result = up.client();
      int temp3 = v1 + 1;

      return up.replAlpha(result, 0.18F * alpha);
   }

   private gh() {
   }

   public static float chipRadius(float S) {

      return 4.5F * S;
   }

   public static float headerGap(float S) {
      return 4.0F * S;
   }

   public static float chipGap(float S) {

      return 3.5F * S;
   }

   public static float headerHeight(float S) {

      return 15.5F * S;
   }

   public static int bgChip(float alpha) {
      int v1 = up.client();
      int v2 = v1 >> 16 & 255;
      int v3 = v1 >> 8 & 255;
      int v4 = v1 & 255;
      int v5 = Math.max(0, Math.min(255, (int)(24.0F + (float)(v2 - (24)) * 0.06F)));

      int v6 = Math.max(0, Math.min(255, (int)(20.0F + (float)(v3 - (20)) * 0.06F)));

      int v7 = Math.max(0, Math.min(255, (int)(34.0F + (float)(v4 - (34)) * 0.06F)));
      return up.getColor(v5, v6, v7, (int)(110.0F * alpha));
   }

   public static void card(float x, float y, float width, float height, float S, float alpha) {
      float v6 = cardRadius(S);
      int temp4 = up.getColor(0, 0, 0, (int)(100.0F * alpha));

      vr.glow(x, y, width, height, temp4, v6, 6.0F * S, 0.5F);
      int var10006 = up.background();

      vr.blur(x, y, width, height, alpha, v6, up.replAlpha(var10006, alpha));

      vr.outline(x, y, width, height, 0.5F * S, outlineSoft(alpha), v6);
   }

   public static float rowHeight(float S) {

      return 13.5F * S;
   }

   public static int bgDeep(float alpha) {
      int v1 = up.client();

      int v2 = v1 >> 16 & 255;
      int result = v1 >> 8;

      int v3 = result & v11;
      int v4 = v1 & 255;
      int v5 = Math.max(0, Math.min(255, (int)(22.0F + (float)(v2 - (22)) * 0.08F)));
      int v6 = Math.max(0, Math.min(255, (int)(18.0F + (float)(v3 - (18)) * 0.08F)));
      int v7 = Math.max(0, Math.min(255, (int)(32.0F + (float)(v4 - (32)) * 0.08F)));

      return up.getColor(v5, v6, v7, (int)(205.0F * alpha));
   }

   public static int accent() {
      int result = up.client();

      return result;
   }

   public static float drawTextChip(float x, float y, String text, yt font, float textSize, int textColor, float S, float alpha) {
      float v8 = 5.0F * S;
      float v9 = font.getWidth(text, textSize);
      float v10 = v9 + v8 * 2.0F;
      float v11 = rowHeight(S);
      float v12 = chipRadius(S);
      vr.rect(x, y, v10, v11, bgChip(alpha), v12);
      vr.rect(x, y, v10, v11, up.getColor(255, 255, 255, (int)(9.0F * alpha)), v12);

      vr.outline(x, y, v10, v11, 0.5F * S, outlineSoft(alpha), v12);
      float temp2 = x + v8;
      float temp3 = y + (v11 - textSize) / 2.0F;
      float v15 = 0.5F * S;

      font.draw(text, temp2, temp3 - v15, textSize, textColor);
      return v10;
   }

   public static int textMain(float alpha) {
      int result = 245;
      int temp1 = 245;
      int temp2 = 250;

      return up.getColor(result, temp1, temp2, (int)(245.0F * alpha));
   }

   public static void drawFloatingBadge(float x, float y, float w, float h, float S, float alpha) {
      float v6 = h / 2.0F;
      vr.blur(x, y, w, h, 1.0F, v6, bgDeep(alpha));
      vr.glow(x, y, w, h, up.replAlpha(accent(), 0.18F * alpha), v6, 6.0F * S, 0.5F);

      vr.rect(x, y, w, h, bgPanel(alpha), v6);
      float v9 = 0.5F * S;

      vr.outline(x, y, w, h, v9, outlineSoft(alpha), v6);
   }

   public static void cardWithGlow(float x, float y, float width, float height, int glowColor, float S, float alpha) {
      float v7 = cardRadius(S);
      int temp4 = up.replAlpha(glowColor, 0.26F * alpha);

      vr.glow(x, y, width, height, temp4, v7, 8.0F * S, 0.6F);

      vr.blur(x, y, width, height, alpha, v7, up.replAlpha(up.background(), alpha));
      vr.outline(x, y, width, height, 0.6F * S, up.replAlpha(glowColor, 0.32F * alpha), v7);
   }

   public static void drawWaypointPill(float x, float y, String name, int distance, float alpha) {
      float v5 = 15.0F;
      float v6 = v5 / 2.0F;
      float v7 = 7.2F;
      float v8 = 6.0F;
      float v9 = 4.0F;
      String v10 = distance + "м";
      float v11 = hu.sf_bold.getWidth(name, v7);
      float v12 = hu.sf_medium.getWidth(v10, v7);
      float v13 = 9.0F;
      float v14 = v8 + v13 + v9 + v11 + 3.0F + v12 + v8;
      vr.blur(x, y, v14, v5, 1.0F, v6, bgDeep(alpha));
      vr.glow(x, y, v14, v5, up.replAlpha(accent(), 0.25F * alpha), v6, 6.0F, 0.5F);
      vr.rect(x, y, v14, v5, bgPanel(alpha), v6);
      vr.outline(x, y, v14, v5, 0.5F, outlineSoft(alpha), v6);
      float v15 = x + v8;
      float v16 = y + (v5 - v13) / 2.0F;
      vr.glow(v15, v16, v13, v13, up.replAlpha(accent(), 0.35F * alpha), v13 / 2.0F, 4.0F, 0.5F);
      vr.rect(v15, v16, v13, v13, up.replAlpha(accent(), 0.95F * alpha), v13 / 2.0F);
      float result = v15 + v13 / 2.0F - 1.2F;

      vr.rect(
         result,
         v16 + v13 / 2.0F - 1.2F,
         2.4F,
         2.4F,
         up.getColor(255, 255, 255, (int)(245.0F * alpha)),
         1.2F
      );
      float v17 = v15 + v13 + v9;
      float v18 = y + (v5 - v7) / 2.0F - 0.4F;
      hu.sf_bold.draw(name, v17, v18, v7, textMain(alpha));
      hu.sf_medium.draw(v10, v17 + v11 + 3.0F, v18, v7, up.replAlpha(accent(), 0.95F * alpha));
   }

   public static float cardRadius(float S) {
      int temp2 = v1 * (v1 + 1) % 2;

      if (temp2 != 0) {
         }

      return 8.0F * S;
   }

   public static int bgPanel(float alpha) {
      int v1 = up.client();
      int v2 = v1 >> 16 & 255;
      int v3 = v1 >> 8 & 255;

      int v4 = v1 & 255;
      int v5 = Math.max(0, Math.min(255, (int)(28.0F + (float)(v2 - (28)) * 0.1F)));
      int v6 = Math.max(0, Math.min(255, (int)(22.0F + (float)(v3 - (22)) * 0.1F)));
      int v7 = Math.max(0, Math.min(255, (int)(40.0F + (float)(v4 - (40)) * 0.1F)));

      return up.getColor(v5, v6, v7, (int)(228.0F * alpha));
   }

   public static float textChipWidth(String text, yt font, float textSize, float S) {
      float result = font.getWidth(text, textSize);

      return result + 10.0F * S;
   }

   
}
