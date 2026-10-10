package ru.white.utils.render.gl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.screen.GuiColorPicker;

@Environment(EnvType.CLIENT)
public interface Jf {
   default void previewStop() {
   }

   lf previewSettings();

   default void previewTick(kf ctx) {
   }

   default void previewStart(kf ctx) {
   }

   default boolean previewControlsDummy() {
      int v1 = -286009795;

      return false;
   }

   default boolean previewNeedsDummy() {

      return false;
   }

   default boolean isPreviewActive() {
      if (this instanceof Module) {

         Module v1 = (Module)this;
         if (mo.getInstance().isEditing(v1)) {
            return true;
         }
      }

      return false;
   }

   void previewSpawn(kf v1);
}
