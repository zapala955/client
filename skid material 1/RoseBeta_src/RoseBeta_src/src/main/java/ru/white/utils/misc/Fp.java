package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack.Entry;
import org.joml.Matrix4f;
import org.joml.Vector4i;

@Environment(EnvType.CLIENT)
public interface Fp {
   void quad(Matrix4f v1, float v2, float v3, float v4, float v5, int v6);

   void quadTexture(Entry v1, BufferBuilder v2, float v3, float v4, float v5, float v6, Vector4i v7);

   void quad(Matrix4f v1, BufferBuilder v2, float v3, float v4, float v5, float v6);

   void quad(Matrix4f v1, BufferBuilder v2, float v3, float v4, float v5, float v6, int v7);
}
