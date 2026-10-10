package ru.white.utils.misc;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class Sf extends Setting<Boolean> {
   public final Animation pressAnim = new Animation();
   private final Runnable f217;

   public Sf(Module parent, String name, Runnable action) {
      super(parent, name, false);
      this.f217 = action;
   }

   public sf setVisible(Supplier<Boolean> value) {
      int v2 = 357988241;
      int temp1 = v2 * (v2 + 1) % 2;

      if (temp1 != 0) {
         }

      return (sf)super.setVisible(value);
   }

   public void press() {

      if (this.f217 != null) {
         this.f217.run();
      }

      this.pressAnim.set(1.0);
      this.pressAnim.run(0.0, 0.45F, ap.QUAD_OUT);
   }

   
}
