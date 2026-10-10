package ru.white.manager.rotation;

import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventManager;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.eb;

@Environment(EnvType.CLIENT)
public final class RotationManager extends HashMap<Class<? extends ud>, ud> {
   public void add(ud... components) {
      ud[] v2 = components;
      int v3 = components.length;

      for (int v4 = 0; v4 < v3; v4++) {
         int temp3 = v6 + 1;

         ud v5 = v2[v4];
         Class temp1 = v5.getClass();

         this.put(temp1, v5);
      }
   }

   public void init() {
      ud[] temp1 = new ud[]{new xd(), new wd(), null};
      eb temp4 = new eb;

      temp4./* $VF: Unable to resugar constructor */<init>();
      temp1[2] = temp4;
      this.add(temp1);
      this.values().forEach(component -> {
         EventManager result = Client.eventHandler();

         result.subscribe(component);
      });
   }

   public void unregister(ud... components) {
      for (ud v5 : components) {
         EventManager result = Client.eventHandler();

         result.unsubscribe(v5);
         this.remove(v5.getClass());
      }
   }

   public <T extends ud> T get(Class<T> clazz) {
      Stream result = this.values().stream();
      Predicate temp1 = component -> {
         int v2x = 1713245475;
         int var10002x = v2x + 1;

         return component.getClass() == clazz;
      };

      result = result.filter(temp1).map(clazz::cast).findFirst().orElse(null);

      return (T)result;
   }

   
}
