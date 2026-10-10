package ru.white.manager;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ConfigFile {
   default void onPreLoad(JsonObject settings) {
   }

   default void onPostLoad(JsonObject settings) {
   }
}
