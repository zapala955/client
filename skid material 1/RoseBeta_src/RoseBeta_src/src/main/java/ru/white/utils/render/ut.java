package ru.white.utils.render;

import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import ru.white.auth.StrDec;
import ru.white.utils.render.shader.Shader;

@Environment(EnvType.CLIENT)
public final class ut implements AutoCloseable {
   private Shader f1619;
   private int f1621;
   private final ut.xt f1618 = new ut.xt();
   private static final float EPSILON = 1.0E-4F;
   private boolean f1624;
   private int f1622;
   private int f1620;
   private boolean f1625;
   private int f1623;
   private static final ut INSTANCE = new ut();

   @Override
   public void close() {
      if (!m1952()) {
         this.m1954();
      } else {
         this.m1953();
         if (this.f1620 != 0) {
            try {
               GL30.glDeleteFramebuffers(this.f1620);
            } catch (Throwable v8) {
            }
         }

         if (this.f1621 != 0) {
            try {
               GL30.glDeleteFramebuffers(this.f1621);
            } catch (Throwable v7) {
            }
         }

         if (this.f1622 != 0) {
            try {
               GL30.glDeleteVertexArrays(this.f1622);
            } catch (Throwable v6) {
            }
         }

         if (this.f1623 != 0) {
            try {
               GL15.glDeleteBuffers(this.f1623);
            } catch (Throwable v5) {
            }
         }

         if (this.f1619 != null) {
            try {

               this.f1619.delete();
            } catch (Throwable v4) {
            }
         }

         this.m1954();
      }
   }

   private static float m1951(float value, float min, float max) {
      int v3 = 1086258518;

      return !Float.isFinite(value) ? min : Math.max(min, Math.min(max, value));
   }

   public void reset() {
      this.f1625 = false;
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
   public void apply(MinecraftClient EventItemUse, Camera camera, Matrix4f viewMatrix, Matrix4f projectionMatrix, ut.wt parameters) {
      if (!this.f1625 && EventItemUse != null && camera != null && viewMatrix != null && projectionMatrix != null && parameters != null) {
         if (mc.field_1687 != null && mc.field_1724 != null && m1950(EventItemUse) && !(parameters.wetness <= 1.0E-4F) && !(parameters.reflectionStrength <= 1.0E-4F)) {
            Window v6 = mc.method_22683();
            int v7 = v6.method_4489();
            int v8 = v6.method_4506();
            if (v7 > 1 && v8 > 1) {
               Framebuffer v9 = mc.method_1522();
               if (v9 != null) {
                  GpuTexture v10 = v9.method_30277();
                  GpuTexture v11 = v9.method_30278();
                  if (v10 != null && v11 != null) {
                     int v12 = ((GlTexture)v10).method_68427();
                     int v13 = ((GlTexture)v11).method_68427();
                     if (v12 > 0 && v13 > 0) {
                        as v14 = as.capture();
                        boolean v15 = false;
                        boolean v26 = false /* VF: Semaphore variable */;

                        label212: {
                           label220: {
                              try {
                                 v26 = true;
                                 this.m1949();
                                 if (!this.f1625) {
                                    if (this.m1947(v7, v8)) {
                                       if (this.m1946(v12, v7, v8)) {
                                          Matrix4f v16 = new Matrix4f(projectionMatrix).invert();
                                          Matrix4f v17 = new Matrix4f(viewMatrix).invert();
                                          Vec3d v18 = camera.method_71156();
                                          if (Math.abs(viewMatrix.m30()) < 0.01F && Math.abs(viewMatrix.m31()) < 0.01F && Math.abs(viewMatrix.m32()) < 0.01F) {
                                             v17.m30((float)v18.field_1352);
                                             v17.m31((float)v18.field_1351);
                                             v17.m32((float)v18.field_1350);
                                          }

                                          v15 = this.m1945(v12, v13, v7, v8, viewMatrix, projectionMatrix, v16, v17, parameters);
                                          v26 = false;
                                          break label212;
                                       }

                                       v26 = false;
                                    } else {
                                       v26 = false;
                                    }
                                 } else {
                                    v26 = false;
                                 }
                                 break label220;
                              } catch (Throwable v27) {
                                 this.f1625 = true;
                                 PrintStream v35 = System.err;
                                 String v20 = v27.getMessage();
                                 StringBuilder v37 = new StringBuilder();

                                 v35.println(
                                    v37.append("[Rain] wet-surface renderer disabled: ")
                                       .append((Object)v20)
                                       .toString()
                                 );
                                 v27.printStackTrace();
                                 v26 = false;
                              } finally {
                                 if (v26) {
                                    if (v15 && this.f1621 != 0) {
                                       GL30.glBindFramebuffer(36160, this.f1621);
                                       int result = 36160;
                                       int temp1 = 36064;

                                       GL30.glFramebufferTexture2D(result, temp1, 3553, 0, 0);
                                    }

                                    GL13.glActiveTexture(33985);
                                    GL11.glBindTexture(3553, 0);
                                    GL13.glActiveTexture(33984);
                                    GL11.glBindTexture(3553, 0);
                                    GL20.glUseProgram(0);
                                    v14.restore();
                                 }
                              }

                              if (v15 && this.f1621 != 0) {
                                 GL30.glBindFramebuffer(36160, this.f1621);
                                 GL30.glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
                              }

                              GL13.glActiveTexture(33985);
                              GL11.glBindTexture(3553, 0);
                              GL13.glActiveTexture(33984);
                              GL11.glBindTexture(3553, 0);
                              GL20.glUseProgram(0);
                              v14.restore();
                              return;
                           }

                           if (v15 && this.f1621 != 0) {
                              GL30.glBindFramebuffer(36160, this.f1621);
                              GL30.glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
                           }

                           GL13.glActiveTexture(33985);

                           GL11.glBindTexture(3553, 0);
                           GL13.glActiveTexture(33984);
                           GL11.glBindTexture(3553, 0);
                           GL20.glUseProgram(0);
                           v14.restore();
                           return;
                        }

                        if (v15 && this.f1621 != 0) {
                           GL30.glBindFramebuffer(36160, this.f1621);
                           GL30.glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
                        }

                        GL13.glActiveTexture(33985);
                        GL11.glBindTexture(3553, 0);
                        GL13.glActiveTexture(33984);
                        GL11.glBindTexture(3553, 0);
                        GL20.glUseProgram(0);
                        v14.restore();
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean m1952() {
      if (RenderSystem.isOnRenderThread()) {
         long result = GLFW.glfwGetCurrentContext();

         if (result != 0L) {
            return true;
         }
      }

      return false;
   }

   private boolean m1947(int width, int height) {
      label33:
      if (this.f1618.texture != null && !this.f1618.texture.isClosed()) {
         if (this.f1618.width == width) {
            if (this.f1618.height == height) {
               break label33;
            }

            
         }

         this.m1953();
      }

      if (this.f1618.texture == null || this.f1618.texture.isClosed()) {
         this.f1618.texture = RenderSystem.getDevice().createTexture(() -> {
            int var10001x = 1840855181;

            return StrDec.d("TVSNsvU5UzVhpqG+EgJtHQ==", var10001x);
         }, 13, TextureFormat.RGBA8, width, height, 1, 1);
         ut.xt v5 = this.f1618;
         GpuDevice temp1 = RenderSystem.getDevice();

         v5.view = temp1.createTextureView(this.f1618.texture);
         this.f1618.width = width;
         this.f1618.height = height;
      }

      this.f1618.width = width;
      this.f1618.height = height;
      return true;
   }

   private ut() {
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
   private static void m1948(int internalFormat, int width, int height, int format, int type) {
      int v5 = GL11.glGetInteger(35055);
      if (v5 != 0) {

         GL15.glBindBuffer(35052, 0);
      }

      boolean v11 = false /* VF: Semaphore variable */;

      try {
         v11 = true;
         int result = 3553;

         ByteBuffer var10008 = (ByteBuffer)null;

         GL11.glTexImage2D(result, 0, internalFormat, width, height, 0, format, type, var10008);
         v11 = false;
      } finally {
         if (v11) {
            if (v5 != 0) {
               GL15.glBindBuffer(35052, v5);
            }
         }
      }

      if (v5 != 0) {
         GL15.glBindBuffer(35052, v5);
      }
   }

   private void m1954() {
      this.f1618.width = 0;

      this.f1618.height = 0;

      this.f1620 = 0;
      this.f1621 = 0;
      this.f1622 = 0;
      this.f1623 = 0;
      this.f1619 = null;
      this.f1624 = false;
      this.f1625 = false;
   }

   private boolean m1946(int colorTexture, int width, int height) {
      try {
         Framebuffer v4 = MinecraftClient.method_1551().method_1522();
         if (v4 == null) {
            return false;
         } else {
            GpuTexture v12 = v4.method_30277();
            GpuTexture v13 = this.f1618.texture;
            if (v12 != null && v13 != null && !v12.isClosed() && !v13.isClosed()) {
               CommandEncoder v7 = RenderSystem.getDevice().createCommandEncoder();
               v7.copyTextureToTexture(v12, v13, 0, 0, 0, 0, 0, width, height);
               return true;
            } else {
               return false;
            }
         }
      } catch (Throwable v11) {
         if (colorTexture > 0 && width > 0 && height > 0) {
            try {
               if (this.f1620 == 0) {
                  this.f1620 = GL30.glGenFramebuffers();
               }

               GL30.glBindFramebuffer(36008, this.f1620);
               GL30.glFramebufferTexture2D(36008, 36064, 3553, colorTexture, 0);

               if (GL30.glCheckFramebufferStatus(36008) != 36053) {
                  GL30.glFramebufferTexture2D(36008, 36064, 3553, 0, 0);
                  return false;
               } else {
                  int v5 = 0;

                  try {
                     v5 = ((GlTexture)this.f1618.texture).method_68427();
                  } catch (Throwable v9) {
                  }

                  if (v5 == 0) {
                     return false;
                  } else {
                     int v6 = GL30.glGenFramebuffers();
                     GL30.glBindFramebuffer(36009, v6);
                     GL30.glFramebufferTexture2D(36009, 36064, 3553, v5, 0);
                     GL11.glReadBuffer(36064);
                     GL11.glDrawBuffer(36064);
                     GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, 16384, 9728);
                     GL30.glBindFramebuffer(36008, this.f1620);
                     GL30.glFramebufferTexture2D(36008, 36064, 3553, 0, 0);
                     GL30.glDeleteFramebuffers(v6);
                     return true;
                  }
               }
            } catch (Throwable v10) {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private void m1949() {
      if (!this.f1624) {
         this.f1622 = GL30.glGenVertexArrays();
         this.f1623 = GL15.glGenBuffers();
         GL30.glBindVertexArray(this.f1622);
         GL15.glBindBuffer(34962, this.f1623);
         float[] result = new float[24];
         result[0] = -1.0F;
         result[1] = -1.0F;
         result[2] = 0.0F;

         result[3] = 0.0F;
         result[4] = 1.0F;
         result[5] = -1.0F;
         result[6] = 1.0F;
         result[7] = 0.0F;
         result[8] = 1.0F;
         result[9] = 1.0F;
         result[10] = 1.0F;
         int temp2 = 11;

         result[temp2] = 1.0F;
         result[12] = -1.0F;
         result[13] = -1.0F;
         result[14] = 0.0F;
         result[15] = 0.0F;
         result[16] = 1.0F;
         result[17] = 1.0F;
         result[18] = 1.0F;
         result[19] = 1.0F;
         result[20] = -1.0F;
         result[21] = 1.0F;
         result[22] = 0.0F;
         result[23] = 1.0F;
         float[] v1 = result;
         GL15.glBufferData(34962, v1, 35044);
         GL20.glEnableVertexAttribArray(0);
         GL20.glVertexAttribPointer(0, 2, 5126, false, 16, 0L);
         GL20.glEnableVertexAttribArray(1);
         GL20.glVertexAttribPointer(1, 2, 5126, false, 16, -5433592428458011268L + 5433592428458011276L);
         GL15.glBindBuffer(34962, 0);
         GL30.glBindVertexArray(0);
         this.f1619 = new Shader("postfx", "wet_surface");
         this.f1624 = true;
         System.out.println("[Rain] Wet Ground renderer initialized");
      }
   }

   public static ut getInstance() {

      return INSTANCE;
   }

   private static boolean m1950(MinecraftClient EventItemUse) {
      Window v1 = EventItemUse == null ? null : mc.method_22683();
      return v1 != null && v1.method_4489() > 0 && v1.method_4506() > 0;
   }

   private void m1953() {
      if (this.f1618.view != null) {
         try {

            this.f1618.view.close();
         } catch (Throwable v6) {
         }

         this.f1618.view = null;
      }

      int v2 = 1786785389;

      if (this.f1618.texture != null) {
         try {
            this.f1618.texture.close();
         } catch (Throwable v5) {
         }

         this.f1618.texture = null;
      }

      this.f1618.width = 0;
      this.f1618.height = 0;
   }

   private boolean m1945(
      int colorTexture,
      int depthTexture,
      int width,
      int height,
      Matrix4f viewMatrix,
      Matrix4f projectionMatrix,
      Matrix4f inverseProjection,
      Matrix4f inverseView,
      ut.wt parameters
   ) {
      if (this.f1621 == 0) {
         this.f1621 = GL30.glGenFramebuffers();
      }

      GL30.glBindFramebuffer(36160, this.f1621);
      GL30.glFramebufferTexture2D(36160, 36064, 3553, colorTexture, 0);
      GL11.glDrawBuffer(36064);
      if (GL30.glCheckFramebufferStatus(36160) != 36053) {
         return true;
      } else {
         GL11.glViewport(0, 0, width, height);
         GL11.glDisable(3089);
         GL11.glDisable(2929);
         GL11.glDisable(2884);
         GL11.glDisable(3042);
         GL11.glDisable(36281);
         GL11.glColorMask(true, true, true, true);
         GL11.glDepthMask(false);
         this.f1619.bind();
         this.f1619.setUniform1i("u_SceneTexture", 0);
         this.f1619.setUniform1i("u_DepthTexture", 1);
         this.f1619.setUniform2f("u_Resolution", (float)width, (float)height);
         this.f1619.setUniformMatrix4f("u_ViewMatrix", viewMatrix);
         this.f1619.setUniformMatrix4f("u_ProjectionMatrix", projectionMatrix);
         this.f1619.setUniformMatrix4f("u_InverseProjectionMatrix", inverseProjection);
         this.f1619.setUniformMatrix4f("u_InverseViewMatrix", inverseView);
         this.f1619.setUniform1f("u_Time", (float)(System.currentTimeMillis() % (-533573946781680796L + 533573946782680796L)) / 1000.0F);
         this.f1619.setUniform1f("u_Wetness", m1951(parameters.wetness, 0.0F, 2.0F));
         this.f1619.setUniform1f("u_PuddleCoverage", m1951(parameters.puddleCoverage, 0.0F, 1.0F));
         this.f1619.setUniform1f("u_ReflectionStrength", m1951(parameters.reflectionStrength, 0.0F, 2.5F));
         this.f1619.setUniform1f("u_MaxDistance", m1951(parameters.maxDistance, 4.0F, 128.0F));
         this.f1619.setUniform1f("u_RippleStrength", m1951(parameters.rippleStrength, 0.0F, 2.0F));
         this.f1619.setUniform1f("u_RainAmount", m1951(parameters.rainAmount, 0.0F, 2.0F));
         this.f1619
            .setUniform1i(
               "u_ReflectionSteps", Math.max(4, Math.min(16, parameters.reflectionSteps))
            );
         this.f1619.setUniform1i("u_Ripples", parameters.ripples ? 1 : 0);
         GL13.glActiveTexture(33984);

         try {
            int v10 = ((GlTexture)this.f1618.texture).method_68427();
            GL11.glBindTexture(3553, v10);
         } catch (Throwable v12) {
            GL11.glBindTexture(3553, 0);
         }

         GL13.glActiveTexture(33985);
         GL11.glBindTexture(3553, depthTexture);
         GL13.glActiveTexture(33984);
         GL30.glBindVertexArray(this.f1622);
         GL11.glDrawArrays(4, 0, 6);
         GL30.glBindVertexArray(0);
         return true;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class vt {
      private static final int GL_PIXEL_UNPACK_BUFFER_BINDING = 35055;
      private static final int GL_PIXEL_UNPACK_BUFFER = 35052;

      
   }

   @Environment(EnvType.CLIENT)
   public static final class wt {
      public boolean ripples;
      public float wetness = 1.0F;
      public float reflectionStrength;
      public float puddleCoverage = 0.82F;
      public float maxDistance;
      public int reflectionSteps;
      public float rippleStrength;
      public float rainAmount;

      public wt() {
         this.reflectionStrength = 1.25F;
         this.maxDistance = 56.0F;
         this.rippleStrength = 0.8F;
         this.rainAmount = 1.0F;
         this.reflectionSteps = 10;
         this.ripples = true;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static final class xt {
      int height;
      int width;
      GpuTextureView view;
      GpuTexture texture;

      
   }
}
