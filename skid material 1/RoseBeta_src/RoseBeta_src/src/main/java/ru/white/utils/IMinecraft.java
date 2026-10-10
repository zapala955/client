package ru.white.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public interface IMinecraft {
   MinecraftClient EventItemUse = MinecraftClient.method_1551();
}
