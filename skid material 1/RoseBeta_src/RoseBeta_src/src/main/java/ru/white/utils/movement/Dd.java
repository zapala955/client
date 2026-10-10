package ru.white.utils.movement;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventTarget;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Dd implements cd {
   private final Consumer<Object> f118;
   private final Class<?> f115;
   public final Class<?> owner;
   private static final dd.id LOOKUP_FACTORY;
   private static final Map<Method, MethodHandle> methodHandleCache = new ConcurrentHashMap<>();
   private final int f117;
   private final boolean f116;

   @Override
   public boolean isStatic() {
      int v1 = 1467844504;

      return this.f116;
   }

   private static MethodHandle m152(dd.hd factory, Class<?> klass, Method method) throws Throwable {
      String v3 = method.getName();
      Lookup v4 = LOOKUP_FACTORY.of(factory, klass);
      MethodType v5 = MethodType.methodType(void.class, method.getParameters()[0].getType());
      MethodHandle v6 = v4.findStatic(klass, v3, v5);
      MethodType v7 = MethodType.methodType(Consumer.class);

      MethodType temp3 = MethodType.methodType(void.class, Object.class);

      return LambdaMetafactory.metafactory(v4, "accept", v7, temp3, v6, v5).getTarget();
   }

   @Override
   public 

      return this.f117;
   }

   @Override
   public Class<?> getTarget() {
      int v1 = -434226625;

      return this.f115;
   }

   static {
      dd.id v0;
      try {
         try {
            final Method v1 = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, Lookup.class);
            v0 = new dd.id(true) {
               @Override
               public Lookup SettingVisibility(dd.hd lambdaFactory, Class<?> klass) throws ReflectiveOperationException {

                  return lambdaFactory.create(v1, klass);
               }

               
            };
         } catch (NoSuchMethodException v3) {
            final Constructor v2 = Lookup.class.getDeclaredConstructor(Class.class);
            v2.setAccessible(true);
            v0 = new dd.id(false) {
               @Override
               public Lookup SettingVisibility(dd.hd lambdaFactory, Class<?> klass) throws ReflectiveOperationException {
                  Constructor result = v2;
                  Object[] temp1 = new Object[1];
                  int v3 = -207198469;

                  temp1[0] = klass;
                  Lookup v6 = (Lookup)result.newInstance(temp1);

                  return v6;
               }

               
            };
         }
      } catch (SecurityException | ReflectiveOperationException v4) {
         v0 = new dd.id(false) {
            @Override
            public Lookup SettingVisibility(dd.hd lambdaFactory, Class<?> klass) {
               Lookup result = MethodHandles.publicLookup();

               return result.in(klass);
            }

            
         };
      }

      LOOKUP_FACTORY = v0;
   }

   private static MethodHandle m153(dd.hd factory, Class<?> klass, Method method) throws Throwable {
      MethodHandle result = methodHandleCache.get(method);

      MethodHandle v3 = result;
      if (v3 != null) {
         return v3;
      } else {
         String v4 = method.getName();
         Lookup v5 = LOOKUP_FACTORY.of(factory, klass);
         MethodType v6 = MethodType.methodType(void.class, method.getParameters()[0].getType());
         MethodHandle v7 = v5.findVirtual(klass, v4, v6);
         MethodType v8 = MethodType.methodType(Consumer.class, klass);
         v3 = LambdaMetafactory.metafactory(v5, "accept", v8, MethodType.methodType(void.class, Object.class), v7, v6).getTarget();
         methodHandleCache.put(method, v3);
         return v3;
      }
   }

   public static boolean requireLambdaFactoryRegistration() {

      return LOOKUP_FACTORY.requireLambdaFactory;
   }

   @Override
   public void call(Object event) {
      int v2 = 1453452610;
      int temp2 = v2 * (v2 + 1) % 2;

      if (temp2 != 0) {
         }

      this.f118.accept(event);
   }

   public Dd(dd.hd factory, Class<?> klass, Object object, Method method) {
      this.f115 = method.getParameters()[0].getType();
      this.f116 = Modifier.isStatic(method.getModifiers());
      this.f117 = method.getAnnotation(vc.class).priority();
      this.owner = klass;

      try {
         this.f118 = this.f116 ? (Consumer)m152(factory, klass, method).invoke() : (Consumer)m153(factory, klass, method).invoke((Object)object);
      } catch (Throwable v6) {
         throw new RuntimeException(v6);
      }
   }

   @Environment(EnvType.CLIENT)
   public interface hd {
      Lookup create(Method v1, Class<?> v2) throws ReflectiveOperationException;
   }

   @Environment(EnvType.CLIENT)
   private abstract static class id {
      public final boolean requireLambdaFactory;

      public id(boolean requireLambdaFactory) {
         this.requireLambdaFactory = requireLambdaFactory;
      }

      public abstract Lookup SettingVisibility(dd.hd v1, Class<?> v2) throws ReflectiveOperationException;

      
   }
}
