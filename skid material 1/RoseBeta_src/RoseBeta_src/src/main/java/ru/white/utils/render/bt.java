package ru.white.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class bt {
   private final Animation f1536;
   private String value = "";
   private final float f1535;
   private String prev = "";

   public float width(yt font, float size) {

      return font.getWidth(this.value, size);
   }

   public String get() {
      return this.value;
   }

   public void draw(yt font, float x, float y, float size, int color) {
      this.f1536.update();
      float v6 = this.f1536.get();
      float v7 = x;

      for (int v8 = 0; v8 < this.value.length(); v8++) {
         int temp2 = v8 + 1;

         String v9 = this.value.substring(v8, temp2);
         int v10 = this.prev.length() - (this.value.length() - v8);
         String result;
         if (v10 >= 0 && v10 < this.prev.length()) {
            temp2 = v10 + 1;

            result = this.prev.substring(v10, temp2);
         } else {
            result = null;
         }

         String v11 = result;
         if (!(v6 >= 1.0F) && !v9.equals(v11)) {
            font.draw(v9, v7, y + this.f1535 * (1.0F - v6), size, up.multAlpha(color, v6));
            if (v11 != null) {
               font.draw(v11, v7, y - this.f1535 * v6, size, up.multAlpha(color, 1.0F - v6));
            }
         } else {
            font.draw(v9, v7, y, size, color);
         }

         v7 += font.getWidth(v9, size);
      }
   }

   public bt(float slide) {
      this.f1536 = new Animation();
      this.f1535 = slide;
   }

   public void set(String next) {
      if (next != null && !next.equals(this.value)) {
         this.prev = this.value;
         this.value = next;
         this.f1536.set(0.0);
         this.f1536.run(1.0, 0.18, ap.QUAD_OUT);
      }
   }

   public bt() {
      this(4.0F);
   }

   
}
