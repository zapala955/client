package ru.white.utils.misc;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.ep;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class Yf extends Setting<String> {
   public Animation animation = new ep(300, 1.0);
   public List<String> values;
   private String f222;
   public boolean opened;

   public yf onAction(Runnable action) {
      int v2 = 1942791099;
      int temp2 = v2 * (v2 + 1);

      return (yf)super.onAction(() -> {

         action.run();
         this.f222 = (String)super.getValue();
      });
   }

   public boolean is(String value) {

      return this.getValue().equalsIgnoreCase(value) && this.getVisible().get();
   }

   public int getIndex() {
      int v1 = 0;
      Iterator v2 = this.values.iterator();

      while (true) {
         boolean result = v2.hasNext();

         if (!result) {
            return 0;
         }

         String v3 = (String)v2.next();
         if (v3.equalsIgnoreCase(this.getValue())) {
            return v1;
         }

         v1++;
      }
   }

   public yf set(String value) {
      int v3 = -1842129529;
      int temp1 = v3 * (v3 + 1);
      int v4 = -1032945374;

      yf v2 = (yf)super.set(value);
      this.f222 = (String)super.getValue();
      return v2;
   }

   public yf onSetVisible(Runnable action) {
      yf result = (yf)super.onSetVisible(action);

      return result;
   }

   public String getValue() {

      if (this.f222 == null) {
         this.f222 = (String)super.getValue();
      }

      return this.f222;
   }

   public yf setVisible(Supplier<Boolean> value) {
      Setting result = super.setVisible(value);

      yf v4 = (yf)result;

      return v4;
   }

   public Yf(Module parent, String name, String... values) {
      super(parent, name, values[0]);
      this.values = List.of(values);
      this.set(values[0]);
      this.f222 = values[0];
   }

   
}
