package ru.white.notification;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public interface INotification {
   boolean mouseScrolled(float v1, float v2, double v3);

   boolean isActive();

   default boolean mouseDragged(float deltaX, float deltaY) {

      return this.isActive();
   }

   default void rawMouseMoved(float mouseX, float mouseY) {
   }

   void render(float v1, float v2, float v3, float v4, float v5);

   boolean mouseClicked(float v1, float v2, int v3);

   default void rawMouseButton(float mouseX, float mouseY, int button, boolean pressed) {
   }

   void closeFromMenuRemoval();

   void saveAndExit();

   boolean mouseReleased(int v1);
}
