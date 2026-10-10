package ru.white.module.setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class MatrixSetting extends Setting<boolean[][]> {
   public final int size;

   public boolean isEmpty() {
      boolean[][] v1 = this.getValue();

      for (boolean[] v4 : v1) {
         for (boolean v8 : v4) {
            if (v8) {
               return false;
            }
         }
      }

      return true;
   }

   public void reset() {
      this.clear();
      int v1 = this.center();

      int v2 = Math.max(1, this.size / (7));
      int v3 = Math.max(2, this.size / 5);

      for (int v4 = v2; v4 < v2 + v3; v4++) {
         int result = v1 + v4;

         if (result >= this.size) {
            break;
         }

         this.put(v1, v1 - v4, true);
         this.put(v1, v1 + v4, true);
         this.put(v1 - v4, v1, true);
         this.put(v1 + v4, v1, true);
      }
   }

   public int center() {
      return this.size / 2;
   }

   public void clear() {
      boolean[][] v1 = this.getValue();
      int v2 = v1.length;

      while (true) {

         if (v3 >= v2) {
            return;
         }

         boolean[] v4 = v1[v3];
         Arrays.fill(v4, false);
         v3++;
      }
   }

   public boolean inBounds(int x, 

      return x >= 0 && y >= 0 && x < this.size && y < this.size;
   }

   public boolean get(int x, int y) {
      if (this.inBounds(x, y)) {
         boolean[] result = this.getValue()[y];

         if (result[x]) {
            return true;
         }
      }

      return false;
   }

   public void fromRows(List<String> rows) {
      this.clear();

      for (int v2 = 0; v2 < this.size && v2 < rows.size(); v2++) {
         String v3 = (String)rows.get(v2);

         for (int v4 = 0; v4 < this.size && v4 < v3.length(); v4++) {
            boolean[] result = this.getValue()[v2];
            boolean temp2;
            if (v3.charAt(v4) == 49) {
               temp2 = true;
               
            } else {
               temp2 = false;
            }

            result[v4] = temp2;
         }
      }
   }

   public void put(int x, int y, boolean filled) {
      if (this.inBounds(x, y)) {
         Object result = this.getValue();

         ((boolean[][])result)[y][x] = filled;
      }
   }

   public List<String> toRows() {
      ArrayList result = new ArrayList;

      result./* $VF: Unable to resugar constructor */<init>(this.size);
      ArrayList v1 = result;

      for (boolean[] v5 : this.getValue()) {
         StringBuilder v6 = new StringBuilder(this.size);

         for (boolean v10 : v5) {
            v6.append((char)(v10 ? 49 : 48));
         }

         v1.add(v6.toString());
      }

      return v1;
   }

   public MatrixSetting setVisible(Supplier<Boolean> value) {
      MatrixSetting result = (MatrixSetting)super.setVisible(value);

      return result;
   }

   public MatrixSetting(Module parent, String name, int size) {
      super(parent, name, new boolean[size][size]);
      this.size = size;
      this.reset();
   }

   
}
