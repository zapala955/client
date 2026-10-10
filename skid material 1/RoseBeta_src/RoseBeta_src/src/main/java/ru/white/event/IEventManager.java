package ru.white.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.movement.Cd;
import ru.white.utils.movement.Dd;

@Environment(EnvType.CLIENT)
public interface IEventManager {
   void unsubscribe(Class<?> v1);

   void subscribe(cd v1);

   boolean isListening(Class<?> v1);

   void registerLambdaFactory(String v1, dd.hd v2);

   <T> T post(T v1);

   <T extends IListener> T post(T v1);

   void unsubscribe(cd v1);

   void unsubscribe(Object v1);

   void subscribe(Class<?> v1);

   void subscribe(Object v1);
}
