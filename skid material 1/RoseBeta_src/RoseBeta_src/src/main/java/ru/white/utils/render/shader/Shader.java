package ru.white.utils.render.shader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Shader {
   private int program = 0;
   private final Map<String, Integer> uniformCache = new HashMap<>();
   private boolean compiled = false;

   public Shader(String group, String name) {
      try {
         String v3 = "shaders/" + group + "/" + name + ".vsh";
         String v4 = "shaders/" + group + "/" + name + ".fsh";
         Identifier v5 = Identifier.method_60655("client", v3);
         Identifier v6 = Identifier.method_60655("client", v4);
         ResourceManager v7 = MinecraftClient.method_1551().method_1478();

         String v8;
         String v9;
         try (
            InputStream v10 = ((Resource)v7.method_14486(v5).orElseThrow()).method_14482();
            InputStream v11 = ((Resource)v7.method_14486(v6).orElseThrow()).method_14482();
         ) {
            v8 = new BufferedReader(new InputStreamReader(v10)).lines().collect(Collectors.joining("\n"));
            v9 = new BufferedReader(new InputStreamReader(v11)).lines().collect(Collectors.joining("\n"));
         }

         int v28 = this.compile(35633, v8, v5.toString());
         int v29 = this.compile(35632, v9, v6.toString());
         if (v28 == 0 || v29 == 0) {
            if (v28 != 0) {
               GL20.glDeleteShader(v28);
            }

            if (v29 != 0) {
               GL20.glDeleteShader(v29);
            }

            return;
         }

         this.program = GL20.glCreateProgram();
         GL20.glAttachShader(this.program, v28);
         GL20.glAttachShader(this.program, v29);
         GL20.glLinkProgram(this.program);
         int v12 = GL20.glGetProgrami(this.program, 35714);
         if (v12 == 0) {
            String v13 = GL20.glGetProgramInfoLog(this.program);
            System.err.println("[Shader] link failed for " + name + ": " + v13);
            GL20.glDeleteProgram(this.program);
            this.program = 0;
         } else {
            this.compiled = true;
         }

         GL20.glDeleteShader(v28);
         GL20.glDeleteShader(v29);
      } catch (Exception v27) {
         PrintStream result = System.err;
         String v20 = v27.getMessage();
         result.println("[Shader] failed to load " + group + "/" + name + ": " + v20);
         v27.printStackTrace();
         if (this.program != 0) {
            GL20.glDeleteProgram(this.program);
            this.program = 0;
         }
      }
   }

   private int compile(int type, String src, String name) {
      int v4 = GL20.glCreateShader(type);
      GL20.glShaderSource(v4, src);
      GL20.glCompileShader(v4);

      int v5 = GL20.glGetShaderi(v4, 35713);
      if (v5 == 0) {
         String v6 = GL20.glGetShaderInfoLog(v4);
         PrintStream result = System.err;
         String v11 = "[Shader] compile failed " + name + ": " + v6;

         result.println(v11);
         GL20.glDeleteShader(v4);
         return 0;
      } else {
         return v4;
      }
   }

   public void delete() {
      if (this.program != 0) {
         GL20.glDeleteProgram(this.program);

         this.program = 0;
         this.uniformCache.clear();

         this.compiled = false;
      }
   }

   public void setUniformMatrix4f(String name, Matrix4f mat) {
      int v3 = this.loc(name);
      if (v3 != -1) {
         MemoryStack v4 = MemoryStack.stackPush();

         try {
            FloatBuffer v5 = v4.mallocFloat(16);

            mat.get(v5);
            GL20.glUniformMatrix4fv(v3, false, v5);
         } catch (Throwable v11) {
            if (v4 != null) {
               try {
                  v4.close();
               } catch (Throwable v10) {
                  v11.addSuppressed(v10);
               }
            }

            throw v11;
         }

         if (v4 != null) {
            v4.close();
            int v7 = 209294581;

            
         }
      }
   }

   public void setUniform1i(String name, int value) {
      int v3 = this.loc(name);

      if (v3 != -1) {

         GL20.glUniform1i(v3, value);
      }
   }

   public void bind() {
      if (this.program != 0) {

         GL20.glUseProgram(this.program);
      }
   }

   public void setUniform2f(String name, float x, float y) {
      int v4 = this.loc(name);
      int v6 = -288692320;

      if (v4 != -1) {
         GL20.glUniform2f(v4, x, y);
      }
   }

   private 

      Integer v2 = this.uniformCache.get(name);
      if (v2 != null) {
         int result = v2;

         return result;
      } else {
         int v3 = GL20.glGetUniformLocation(this.program, name);
         this.uniformCache.put(name, v3);
         return v3;
      }
   }

   public boolean isCompiled() {
      boolean result;
      if (this.compiled && this.program != 0) {

         result = true;
      } else {
         result = false;
         
      }

      return result;
   }

   public void setUniform1f(String name, float value) {

      int v3 = this.loc(name);

      if (v3 != -1) {
         GL20.glUniform1f(v3, value);
      }
   }

   
}
