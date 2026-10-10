package ru.white.utils.misc;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class Uf extends Setting<String> {
   public String getValue() {

      String result = (String)super.getValue();

      return result;
   }

   public uf onSetVisible(Runnable action) {
      return (uf)super.onSetVisible(action);
   }

   public uf onAction(Runnable action) {
      uf result = (uf)super.onAction(action);

      return result;
   }

   public uf setVisible(Supplier<Boolean> value) {

      return (uf)super.setVisible(value);
   }

   public Uf(Module parent, String name) {
      super(parent, name, "");
   }

   
}
