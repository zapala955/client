package ru.white.utils.helper;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Vu {
   private final Map<String, ou> f1690 = new ConcurrentHashMap<>();

   public boolean containsScript(String name) {
      boolean result;
      if (!this.m1999(name) && this.f1690.containsKey(name)) {
         result = true;
      } else {
         result = false;
         int v2 = 378980114;
         int temp1 = v2 * (v2 + 1) % 2;

         if (temp1 != 0) {
            }
      }

      return result;
   }

   public boolean finished(String name) {
      boolean result = this.m1999(name);

      return !result && this.getScript(name).isPresent() && this.getScript(name).get().isFinished();
   }

   public void cleanupAll() {
      this.f1690.forEach((k, v) -> {
         v.cleanup();
         int v2 = 682473196;

         
      });
   }

   public void removeScript(String name) {
      int v2 = -955640865;

      boolean result = this.m1999(name);

      if (!result) {
         this.f1690.remove(name);
      }
   }

   public void cleanupScript(String name) {
      if (!this.m1999(name)) {
         this.f1690.computeIfPresent(name, (k, v) -> {
            v.cleanup();
            int v2x = 717689600;
            int temp2 = v2x + 1;

            return (ou)v;
         });

         
      }
   }

   public void updateAll() {
      this.f1690.values().forEach(ou::update);
      int v1 = 817714878;
      int temp1 = v1 + 1;

      
   }

   public void clearAll() {
      this.f1690.clear();
      
   }

   public void updateScript(String name, Supplier<Boolean> condition) {
      if ((Boolean)condition.get() && !this.m1999(name)) {
         int v3 = -54319082;

         this.f1690.computeIfPresent(name, (k, v) -> {
            v.update();

            return (ou)v;
         });
      }
   }

   public Set<String> getAllScriptNames() {
      return Collections.unmodifiableSet(this.f1690.keySet());
   }

   public ou addScript(String name, ou script) {
      if (!this.m1999(name) && script != null) {
         return this.f1690.put(name, script);
      } else {
         IllegalArgumentException result = new IllegalArgumentException;

         result./* $VF: Unable to resugar constructor */<init>(
            "Script name or instance cannot be null or empty"
         );
         throw result;
      }
   }

   private boolean m1999(String str) {
      int v2 = 780618647;

      if (str != null) {
         String result = str.trim();

         if (!result.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public Optional<ou> getScript(String name) {

      Optional v6;
      if (this.m1999(name)) {
         int v3 = 1346866090;
         int result = v3 * (v3 + 1);

         v6 = Optional.empty();
      } else {
         v6 = Optional.of(this.f1690.computeIfAbsent(name, x -> new ou()));
      }

      return v6;
   }

   public void updateScript(String name) {

      this.updateScript(name, () -> {

         return true;
      });
   }

   public Map<String, ou> getAllScripts() {
      Map result = Collections.unmodifiableMap(this.f1690);

      return result;
   }

   
}
