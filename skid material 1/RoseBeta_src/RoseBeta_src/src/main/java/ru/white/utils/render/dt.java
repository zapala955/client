package ru.white.utils.render;

import java.util.ArrayDeque;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class dt {
   private static final Deque<int[]> scissorStack = new ArrayDeque<>();
   private static final MinecraftClient EventItemUse = MinecraftClient.method_1551();

   public static void disable() {
      wr.flushPending();
      if (!scissorStack.isEmpty()) {
         scissorStack.pop();
         
      }

      if (scissorStack.isEmpty()) {
         GL11.glDisable(3089);
      } else {
         int[] v0 = scissorStack.peek();
         GL11.glScissor(v0[0], v0[1], v0[2], v0[3]);
      }
   }

   public static void enable(float x, float y, float width, float height) {
      Window result = mc.method_22683();

      int v8 = (Integer)mc.field_1690.method_42474().method_41753();

      int v4 = result.method_4476(v8, mc.method_1573());

      enable(x, y, width, height, (float)v4);
   }

   public static void reset() {
      wr.flushPending();
      scissorStack.clear();
      int result = 3089;

      GL11.glDisable(result);
   }

   public static void enable(float x, float y, float width, float height, float guiScale) {
      wr.flushPending();
      int v5 = mc.method_22683().method_4507();
      int v6 = (int)(x * guiScale);
      int v7 = (int)((float)v5 - (y + height) * guiScale);
      int v8 = (int)(width * guiScale);
      int v9 = (int)(height * guiScale);
      if (!scissorStack.isEmpty()) {
         int[] v10 = scissorStack.peek();
         int v11 = v10[0];
         int v12 = v10[1];
         int v13 = v11 + v10[2];
         int v14 = v12 + v10[3];

         int v15 = v6 + v8;
         int v16 = v7 + v9;
         v6 = Math.max(v6, v11);
         v7 = Math.max(v7, v12);
         v15 = Math.min(v15, v13);
         v16 = Math.min(v16, v14);
         int v22 = v15 - v6;

         v8 = Math.max(0, v22);
         v9 = Math.max(0, v16 - v7);
      }

      scissorStack.push(new int[]{v6, v7, v8, v9});
      GL11.glEnable(3089);
      GL11.glScissor(v6, v7, v8, v9);
   }

   
}
