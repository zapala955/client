package ru.white.notification;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.ui.DraggableWidget;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.vs;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public final class NotificationRenderer {
   private float f1224;
   private final Map<String, Animation> f1221 = new HashMap<>();
   private final Map<String, float[]> f1222 = new HashMap<>();
   private float f1223;

   public void drawRowBackground(to b, float hover, float alpha) {
      vs.ys.glow(b.x(), b.y(), b.width(), b.height(), up.getColor(0, 0.06F * alpha), 4.0F, 6.0F, 1.0F);
      vs.ys.rect(b.x(), b.y(), b.width(), b.height(), up.getColor(40, 0.15F * alpha), 4.0F);
      float result = b.x();
      float temp1 = b.y();

      vs.ys.outline(result, temp1, b.width(), b.height(), 0.5F, up.replAlpha(up.client(), alpha * hover), 4.0F);
      
   }

   public float smooth(String key, float target) {
      float[] v3 = this.f1222.computeIfAbsent(key, k -> {
         float[] var10000x = new float[1];

         var10000x[0] = target;
         return var10000x;
      });
      v3[0] += (target - v3[0]) * 0.2F;

      return v3[0];
   }

   public float mouseY() {

      return this.f1224;
   }

   public void drawButton(oo button, float alpha) {
      float v3 = button.hoverValue();
      float v4 = button.pressValue();
      float v5 = Math.max(v3, v4);
      vs.ys.glow(button.x(), button.y(), button.width(), button.height(), up.getColor(0, 0.15F * alpha), 5.0F, 10.0F, 1.0F);
      vs.ws.blur(button.x(), button.y(), button.width(), button.height(), alpha, 5.0F, up.multAlpha(up.multDark(up.background(), 0.6F), alpha));
      if (button.accent()) {
         vs.ys.rect(button.x(), button.y(), button.width(), button.height(), up.replAlpha(up.client(), alpha * (0.2F + 0.2F * v5)), 5.0F);
      }

      float result = button.x();
      float temp1 = button.y();
      float temp2 = button.width();
      float temp3 = button.height();
      int temp5 = up.client();
      float var10007;
      if (button.accent()) {

         var10007 = 0.5F + 0.5F * v5;
      } else {
         var10007 = v5;
      }

      vs.ys.outline(result, temp1, temp2, temp3, 0.5F, up.replAlpha(temp5, alpha * var10007), 5.0F);
      yt v9 = hu.sf_regular;
      String v10 = bb.tr(button.label());
      temp2 = button.x() + button.width() / 2.0F;

      temp3 = button.y() + button.height() / 2.0F - 3.8F;
      temp5 = 255;

      v9.drawCentered(v10, temp2, temp3, 6.5F, up.getColor(temp5, alpha * (0.6F + 0.4F * v5)));
   }

   public float mouseX() {

      return this.f1223;
   }

   public Animation anim(String key) {

      return this.f1221.computeIfAbsent(key, k -> {
         ep result = new ep;

         result./* $VF: Unable to resugar constructor */<init>(300, 1.0);

         return result;
      });
   }

   public float rowHover(String key, to bounds) {
      Animation v3 = this.anim(key + ":hover");

      v3.setDirection(bounds.contains(this.f1223, this.f1224) ? cp.FORWARDS : cp.BACKWARDS);
      return v3.getOutput();
   }

   public void setMouse(float mouseX, float mouseY) {
      this.f1223 = mouseX;
      this.f1224 = mouseY;
   }

   public void drawToggleRow(to b, String label, boolean value, String key, float alpha) {
      if (!(b.width() <= 0.0F) && !(alpha <= 0.01F)) {
         float v6 = this.rowHover(key, b);
         this.drawRowBackground(b, v6, alpha);
         hu.sf_regular.draw(label, b.x() + 6.0F, b.y() + 3.2F, 6.0F, up.getColor(255, alpha * (0.5F + 0.5F * v6)));
         Animation v7 = this.anim(key + ":on");
         v7.setDirection(value ? cp.FORWARDS : cp.BACKWARDS);
         float v8 = v7.getOutput();
         vs.ys.rect(
            b.x() + b.width() - 6.0F - 11.0F,
            b.y() + 3.5F,
            11.0F,
            6.0F,
            up.overCol(up.getColor(0, 0.2F * alpha), up.replAlpha(up.client(), alpha * (0.5F + 0.5F * v6)), v8),
            3.0F
         );
         float result = b.x() + b.width() - 6.0F - 11.0F + 1.0F + 4.5F * v8;
         float temp1 = b.y() + 4.5F;
         int temp4 = 255;
         float var10006 = 0.2F + 0.4F * v8;

         vs.ys.rect(result, temp1, 4.0F, 4.0F, up.getColor(temp4, alpha * (var10006 + 0.4F * v6)), 2.0F);
         
      }
   }

   
}
