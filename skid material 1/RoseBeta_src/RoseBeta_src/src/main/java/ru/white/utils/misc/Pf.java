package ru.white.utils.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class Pf extends Setting<List<String>> {
   private static final Map<String, String> DISPLAY = new HashMap<>();
   private static List<String> f213;
   private static String[] f214;
   public String input = "";
   private static final Map<String, Integer> SWATCH = new HashMap<>();

   public static String normalize(String raw) {
      if (raw == null) {
         return null;
      } else {
         String v1 = raw.trim().toLowerCase();
         if (v1.isEmpty()) {
            return null;
         } else {
            if (!v1.contains(":")) {
               v1 = "minecraft:" + v1;
            }

            Identifier v2 = Identifier.method_12829(v1);
            if (v2 != null) {

               if (Registries.field_41175.method_10250(v2)) {
                  return v1;
               }
            }

            return null;
         }
      }
   }

   public void remove(String id) {
      this.getValue().remove(id);
      
   }

   public static String displayName(String id) {
      return DISPLAY.computeIfAbsent(id, pf::m237);
   }

   public Set<Block> resolveBlocks() {
      HashSet v1 = new HashSet();
      Iterator v2 = this.getValue().iterator();

      while (v2.hasNext()) {

         String v3 = (String)v2.next();
         Identifier v4 = Identifier.method_12829(v3);
         if (v4 != null) {
            Registries.field_41175.method_17966(v4).ifPresent(v1::add);
         }
      }

      return v1;
   }

   public boolean addCurrentInput() {

      if (this.addId(this.input)) {
         this.input = "";
         return true;
      } else {
         return false;
      }
   }

   private static int m238(String id) {
      Identifier v1 = Identifier.method_12829(id);

      if (v1 != null) {
         Optional result = Registries.field_41175.method_17966(v1);

         Optional v2 = result;
         if (v2.isPresent()) {
            int v3 = ((Block)v2.get()).method_26403().field_16011;
            if (v3 != 0) {
               return -16777216 | v3 & 16777215;
            }
         }
      }

      return -11184811;
   }

   public pf setVisible(Supplier<Boolean> value) {

      return (pf)super.setVisible(value);
   }

   public static String shortName(String id) {

      int v1 = id.indexOf(58);
      return v1 >= 0 ? id.substring(v1 + 1) : id;
   }

   public Pf(Module parent, String name) {
      super(parent, name, new ArrayList<>());
   }

   public static List<String> search(String query) {
      if (f213 == null) {
         m236();
      }

      String v1 = query == null ? "" : query.trim().toLowerCase();
      if (v1.isEmpty()) {
         return f213;
      } else {
         ArrayList v2 = new ArrayList();

         for (int v3 = 0; v3 < f213.size(); v3++) {
            if (f214[v3].contains(v1)) {
               String temp1 = f213.get(v3);

               v2.add(temp1);
            }
         }

         return v2;
      }
   }

   public static int swatchColor(String id) {
      int v1 = -1901455316;

      return SWATCH.computeIfAbsent(id, pf::m238);
   }

   public boolean addId(String raw) {
      String v2 = normalize(raw);
      if (v2 != null && !this.getValue().contains(v2)) {

         List result = this.getValue();

         result.add(v2);
         return true;
      } else {
         return false;
      }
   }

   public List<String> getIds() {

      return this.getValue();
   }

   public static List<String> allIds() {
      if (f213 == null) {
         m236();
         int v0 = -1309731998;
         int result = v0 * (v0 + 1) % 2;

         if (result != 0) {
            }
      }

      return f213;
   }

   private static void m236() {
      ArrayList v0 = new ArrayList();

      try {
         for (Identifier v2 : Registries.field_41175.method_10235()) {
            v0.add(v2.toString());
         }
      } catch (Exception v14) {
      }

      v0.sort(String::compareTo);
      String[] v15 = new String[v0.size()];

      for (int v16 = 0; v16 < v0.size(); v16++) {
         String v3 = (String)v0.get(v16);

         String v4;
         try {
            v4 = m237(v3);
         } catch (Exception v13) {
            v4 = shortName(v3);
         }

         int v5;
         try {
            v5 = m238(v3);
            
         } catch (Exception v12) {
            v5 = -11184811;
         }

         DISPLAY.put(v3, v4);
         SWATCH.put(v3, v5);
         String v9 = shortName(v3);
         v15[v16] = (v9 + " " + v4 + " " + v3).toLowerCase();
      }

      f214 = v15;
      f213 = v0;
   }

   private static String m237(String id) {
      Identifier v1 = Identifier.method_12829(id);
      if (v1 != null) {
         Optional v2 = Registries.field_41175.method_17966(v1);
         if (v2.isPresent()) {
            return ((Block)v2.get()).method_9518().getString();
         }
      }

      return shortName(id);
   }

   
}
