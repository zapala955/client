package ru.white.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.movement.Ad;
import ru.white.utils.movement.Cd;
import ru.white.utils.movement.Dd;
import ru.white.utils.movement.Jd;

@Environment(EnvType.CLIENT)
public class EventManager implements IEventManager {
   private final Map<Object, List<cd>> f107;
   private final Map<Class<?>, List<cd>> f109;
   private final Supplier<List<cd>> f110;
   private final Map<Class<?>, List<cd>> f108;
   private final List<tc.uc> f111 = new CopyOnWriteArrayList<>();

   private void m146(List<cd> listeners, Class<?> klass, Object object, boolean staticOnly) {
      while (klass != null) {
         dd.hd v5 = null;
         Method[] result = klass.getDeclaredMethods();

         Method[] v6 = result;
         int v7 = v6.length;

         for (int v8 = 0; v8 < v7; v8++) {
            Method v9 = v6[v8];
            if (this.m147(v9, staticOnly)) {
               if (dd.requireLambdaFactoryRegistration() && v5 == null) {

                  v5 = this.m148(klass);
               }

               listeners.add(new dd(v5, klass, object, v9));
            }
         }

         klass = klass.getSuperclass();
      }
   }

   private List<cd> m143(Class<?> klass) {
      return this.f108.computeIfAbsent(klass, o -> {

         return this.m145((Class<?>)o, null, true);
      });
   }

   @Override
   public void unsubscribe(cd listener) {
      List v2 = this.f109.get(listener.getTarget());
      if (v2 != null) {
         int result = v3 * (v3 + 1);

         jd.remove(v2, listener);
      }
   }

   @Override
   public void subscribe(cd listener) {
      this.m141(listener, true);
      int result = v2 * (v2 + 1);

      
   }

   private void m140(List<cd> listeners, boolean check) {
      Iterator result = listeners.iterator();
      int temp2 = v5 + 1;

      Iterator v3 = result;

      while (v3.hasNext()) {
         cd v4 = (cd)v3.next();

         this.m141(v4, check);
      }
   }

   public boolean areAllSubscribed(Class<?> staticListener) {
      List v2 = this.f108.get(staticListener);
      if (v2 == null) {
         return false;
      } else {
         Iterator result = v2.iterator();

         Iterator v3 = result;

         while (v3.hasNext()) {
            cd v4 = (cd)v3.next();
            if (!this.isSubscribed(v4)) {
               return false;
            }
         }

         return true;
      }
   }

   private dd.hd m148(Class<?> klass) {
      for (tc.uc v3 : this.f111) {
         if (klass.getName().startsWith(v3.f105)) {
            return v3.f106;
         }
      }

      throw new EventException(klass);
   }

   @Override
   public <T extends IListener> T post(T event) {
      List v2 = this.f109.get(event.getClass());
      if (v2 != null) {

         event.setCancelled(false);
         Iterator v3 = v2.iterator();

         do {
            boolean result = v3.hasNext();

            if (!result) {
               break;
            }

            cd v4 = (cd)v3.next();

            try {
               v4.call(event);
            } catch (Exception v8) {
               v8.printStackTrace();
            }
         } while (!event.isCancelled());
      }

      return (T)event;
   }

   @Override
   public void unsubscribe(Object object) {
      Object result = this.f107.get(object);

      List v2 = (List)result;
      if (v2 != null) {
         this.m142(v2);
         this.f107.remove(object);
      }
   }

   public boolean isSubscribed(Object object) {

      return this.f107.containsKey(object);
   }

   private void m141(cd listener, boolean check) {

      Object result = this.f109.computeIfAbsent(listener.getTarget(), klass -> {
         Object var10000x = this.f110.get();

         return (List<cd>)var10000x;
      });

      List v3 = (List)result;
      if (!check || !jd.contains(v3, listener)) {
         jd.insert(v3, listener);
      }
   }

   @Override
   public boolean isListening(Class<?> eventClass) {

      List v2 = this.f109.get(eventClass);
      return v2 != null && !v2.isEmpty();
   }

   @Override
   public void unsubscribe(Class<?> klass) {
      List v2 = this.f108.get(klass);
      if (v2 != null) {
         this.m142(v2);

         int result = v3 * (v3 + 1) % 2;

         if (result != 0) {
            }
      }
   }

   @Override
   public <T> T post(T event) {
      List v2 = this.f109.get(event.getClass());
      if (v2 != null) {
         for (cd v4 : v2) {
            try {
               v4.call(event);
            } catch (Exception v9) {

               v9.printStackTrace();
            }
         }
      }

      return (T)event;
   }

   private void m142(List<cd> listeners) {
      for (cd v3 : listeners) {

         this.unsubscribe(v3);
      }
   }

   public boolean areAnySubscribed(Class<?> staticListener) {
      List v2 = this.f108.get(staticListener);
      if (v2 == null) {
         return false;
      } else {
         Iterator result = v2.iterator();

         Iterator v3 = result;

         cd v4;
         do {

            if (!v3.hasNext()) {
               return false;
            }

            result = (Iterator)v3.next();

            v4 = (cd)result;
         } while (!this.isSubscribed(v4));

         return true;
      }
   }

   @Override
   public void registerLambdaFactory(String packagePrefix, dd.hd factory) {

      this.f111.add(new tc.uc(packagePrefix, factory));
   }

   public static EventManager threadSafe() {
      EventManager result = new EventManager;
      Map temp2 = Collections.synchronizedMap(new IdentityHashMap());
      ConcurrentHashMap temp3 = new ConcurrentHashMap();
      ConcurrentHashMap temp4 = new ConcurrentHashMap;

      temp4./* $VF: Unable to resugar constructor */<init>();

      result./* $VF: Unable to resugar constructor */<init>(temp2, temp3, temp4, CopyOnWriteArrayList::new);
      return result;
   }

   private boolean m147(Method method, boolean staticOnly) {
      if (staticOnly && !Modifier.isStatic(method.getModifiers())) {
         return false;
      } else {

         if (!method.isAnnotationPresent(vc.class)) {

            return false;
         } else if (method.getReturnType() != void.class) {
            return false;
         } else {
            return method.getParameterCount() != 1 ? false : !method.getParameters()[0].getType().isPrimitive();
         }
      }
   }

   private List<cd> m145(Class<?> klass, Object object, boolean staticOnly) {
      ArrayList result = new ArrayList;

      result./* $VF: Unable to resugar constructor */<init>();
      ArrayList v4 = result;
      this.m146(v4, klass, object, staticOnly);
      return new ad<>(v4);
   }

   @Override
   public void subscribe(Object object) {
      if (!this.f107.containsKey(object)) {
         List temp1 = this.m144(object);

         this.m140(temp1, false);
      }
   }

   public boolean isSubscribed(cd listener) {
      List v2 = this.f109.get(listener.getTarget());

      boolean v6;
      if (v2 != null && jd.contains(v2, listener)) {
         v6 = true;
      } else {
         v6 = false;
         
      }

      return v6;
   }

   public boolean isSubscribed(Class<?> staticListener) {
      for (cd v4 : this.f108.get(staticListener)) {
         if (v4 instanceof dd v5) {
            if (v5.owner != staticListener) {
               break;
            }

            if (!this.isSubscribed(v4)) {
               return true;
            }
         }
      }

      return true;
   }

   public EventManager(
      Map<Object, List<cd>> listenerCache,
      Map<Class<?>, List<cd>> staticListenerCache,
      Map<Class<?>, List<cd>> listenerMap,
      Supplier<List<cd>> listenerListFactory
   ) {
      this.f107 = listenerCache;
      this.f108 = staticListenerCache;
      this.f109 = listenerMap;
      this.f110 = listenerListFactory;
   }

   @Override
   public void subscribe(Class<?> klass) {

      List v2 = this.m143(klass);

      this.m140(v2, true);
   }

   public static EventManager threadUnsafe() {
      EventManager result = new EventManager;
      IdentityHashMap temp2 = new IdentityHashMap;

      temp2./* $VF: Unable to resugar constructor */<init>();
      IdentityHashMap v3 = new IdentityHashMap;

      v3./* $VF: Unable to resugar constructor */<init>();
      result./* $VF: Unable to resugar constructor */<init>(temp2, v3, new IdentityHashMap<>(), CopyOnWriteArrayList::new);
      return result;
   }

   private List<cd> m144(Object object) {
      return this.f107.computeIfAbsent(object, o -> {
         Class temp1 = o.getClass();

         return this.m145(temp1, o, false);
      });
   }

   @Environment(EnvType.CLIENT)
   private static record uc(String packagePrefix, dd.hd factory) {
      private final String f105;
      private final dd.hd f106;

      public dd.hd factory() {

         return this.f106;
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",tc.uc,"packagePrefix;factory",tc.uc::f105,tc.uc::f106>(this);

         return result;
      }

      @Override
      public final String toString() {

         return ObjectMethods.bootstrap<"toString",tc.uc,"packagePrefix;factory",tc.uc::f105,tc.uc::f106>(this);
      }

      private uc(String packagePrefix, dd.hd factory) {
         this.f105 = packagePrefix;
         this.f106 = factory;
      }

      public String packagePrefix() {

         return this.f105;
      }

      
   }
}
