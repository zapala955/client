package ru.white.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class as {
   private final int f1348;
   private final int f1357;
   private final boolean f1353;
   private final int f1350;
   private final int f1355;
   private final int f1356;
   private final int f1354;
   private final boolean f1352;
   private final boolean f1351;
   private final int f1349;

   public static as capture() {
      try {
         int v0 = GL11.glGetInteger(35725);
         int v1 = GL11.glGetInteger(36006);
         int v2 = GL11.glGetInteger(36010);
         int v3 = GL11.glGetInteger(36006);
         int v4 = GL11.glGetInteger(34016);
         boolean v5 = GL11.glIsEnabled(3042);
         int result = 2929;
         int v8 = 1083052770;

         boolean v6 = GL11.glIsEnabled(result);
         boolean v7 = GL11.glIsEnabled(2884);
         return new as(v0, v1, v4, v5, v6, v7, new int[]{v2, v3, 0, 0});
      } catch (Throwable v10) {
         return new as(0, 0, 0, false, false, false, new int[]{0, 0, 0, 0});
      }
   }

   private as(int program, int framebuffer, int activeTexture, boolean blend, boolean depthTest, boolean cullFace, int[] viewport) {
      this.f1348 = program;
      this.f1349 = framebuffer;
      this.f1350 = activeTexture;
      this.f1351 = blend;
      this.f1352 = depthTest;
      this.f1353 = cullFace;
      this.f1354 = viewport[0];
      this.f1355 = viewport[1];
      this.f1356 = viewport[2];
      this.f1357 = viewport[3];
   }

   public void restore() {
      try {
         GL20.glUseProgram(this.f1348);

         try {
            int v1 = this.f1354;

            int v2 = this.f1355;
            if (v1 != 0 || v2 != 0) {
               GL30.glBindFramebuffer(36008, v1);
               GL30.glBindFramebuffer(36009, v2);
               
            }

            GL30.glBindFramebuffer(36160, this.f1349);
         } catch (Throwable v5) {
            GL30.glBindFramebuffer(36160, this.f1349);
         }

         GL13.glActiveTexture(this.f1350);
         if (this.f1351) {
            GL11.glEnable(3042);
         } else {
            GL11.glDisable(3042);
         }

         if (this.f1352) {
            GL11.glEnable(2929);
         } else {
            GL11.glDisable(2929);
         }

         if (this.f1353) {
            GL11.glEnable(2884);
         } else {
            GL11.glDisable(2884);
         }
      } catch (Throwable v6) {
      }
   }

   
}
