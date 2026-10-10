package ru.white.event.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventTrace extends rc {
   private final float f67;
   private final MatrixStack f66;
   private final Matrix4f f68;

   public MatrixStack getMatrixStack() {

      return this.f66;
   }

   public EventTrace(MatrixStack matrixStack, float tickDelta) {
      this.f66 = matrixStack;
      this.f67 = tickDelta;
      this.f68 = new Matrix4f();
   }

   public float getTickDelta() {
      return this.f67;
   }

   public EventTrace(MatrixStack matrixStack, float tickDelta, Matrix4f projectionMatrix) {
      this.f66 = matrixStack;
      this.f67 = tickDelta;
      this.f68 = projectionMatrix;
   }

   public Matrix4f getProjectionMatrix() {
      return this.f68;
   }

   
}
