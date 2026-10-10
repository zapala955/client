package ru.white.utils.misc;

import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class Wf<T> extends Setting<T> {
   public List<T> values;
   private T f220;

   public int getIndex() {
      int v1 = 0;

      for (Object v3 : this.values) {
         int v4 = -754089381;
         int temp2 = v4 * (v4 + 1);

         if (v3.equals(this.getValue())) {
            return v1;
         }

         v1++;
      }

      return 0;
   }

   public wf<T> setAsObject(Object value) {
      Setting result = super.set(value);

      wf v2 = (wf)result;
      this.f220 = (T)super.getValue();
      return v2;
   }

   public boolean is(T value) {
      Object result = this.getValue();

      return result.equals(value);
   }

   public wf<T> setVisible(Supplier<Boolean> value) {
      wf result = (wf)super.setVisible(value);

      return result;
   }

   public wf<T> set(T value) {
      wf v2 = (wf)super.set(value);

      int v4 = 1157534975;
      int temp2 = v4 + 1;

      this.f220 = (T)super.getValue();
      return v2;
   }

   @SafeVarargs
   public Wf(Module parent, String name, T... values) {
      super(parent, name, (T)values[0]);
      this.values = List.of((T[])values);
      this.set((T)values[0]);
      this.f220 = (T)values[0];
   }

   public wf<T> onSetVisible(Runnable action) {
      return (wf<T>)super.onSetVisible(action);
   }

   public wf<T> onAction(Runnable action) {
      Runnable temp1 = () -> {
         action.run();
         int v2x = -940019061;

         this.f220 = (T)super.getValue();
      };

      return (wf<T>)super.onAction(temp1);
   }

   @Override
   public T getValue() {
      if (this.f220 == null) {
         this.f220 = (T)super.getValue();
      }

      return this.f220;
   }

   
}
