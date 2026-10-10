package ru.white.notification;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.PacketUtil;

@Environment(EnvType.CLIENT)
public final class NotificationType {
   private final String f1218;
   private float x;
   private final Animation f1220;
   private final Animation f1219 = new Animation();
   private float y;
   private float height;
   private float width;
   private final boolean accent;

   public float pressValue() {

      return this.f1220.get();
   }

   public void update(float mouseX, float mouseY) {
      this.f1219.update();
      Animation result = this.f1219;
      double temp1;
      if (this.contains(mouseX, mouseY)) {
         temp1 = 1.0;
      } else {
         temp1 = 0.0;
         
      }

      result.run(temp1, 0.18F, ap.QUAD_OUT, true);
      this.f1220.update();
   }

   public float y() {

      return this.y;
   }

   public float x() {

      return this.x;
   }

   public float hoverValue() {

      return this.f1219.get();
   }

   public NotificationType(String label, boolean accent) {
      this.f1220 = new Animation();
      this.f1218 = label;
      this.accent = accent;
   }

   public float height() {

      return this.height;
   }

   public void press() {

      this.f1220.set(1.0);
      this.f1220.run(0.0, 0.35F, ap.QUAD_OUT);
   }

   public boolean accent() {

      return this.accent;
   }

   public String label() {

      return this.f1218;
   }

   public float width() {

      return this.width;
   }

   public void set(float x, float y, float width, float height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;

      
   }

   public boolean contains(float mouseX, float mouseY) {

      return fq.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
   }

   
}
