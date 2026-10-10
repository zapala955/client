package ru.white.utils.render;

import java.lang.runtime.ObjectMethods;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.Window;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix3x2fStack;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class js {
   private static final int FORCED_GUI_SCALE = 2;
   private static final Random RANDOM = Random.method_43047();
   private static final Map<String, js.ks> SPRITE_CACHE = new ConcurrentHashMap<>();
   private static final MinecraftClient EventItemUse = MinecraftClient.method_1551();

   public static boolean hasGlint(ItemStack stack) {
      boolean result = stack.method_7958();

      return result;
   }

   private static int m1805(int color, float alpha) {
      int temp1 = 24;

      int v2 = (int)((float)(color >> temp1 & 255) * alpha);
      return v2 << 24 | color & 16777215;
   }

   public static void drawItemCenteredWithContext(DrawContext context, ItemStack stack, float centerX, float centerY, float scale, float alpha) {
      float v6 = 16.0F * scale;
      float v7 = centerX - v6 / 2.0F;

      float v8 = centerY - v6 / 2.0F;
      drawItemWithContext(context, stack, v7, v8, scale, alpha);
   }

   public static void clearCache() {

      SPRITE_CACHE.clear();
   }

   private static float m1802() {
      int temp1 = m1801();
      int v0 = 1278490804;

      int temp2 = v0 * (v0 + 1);

      return 2.0F / (float)temp1;
   }

   private static Sprite m1803(ItemStack stack) {
      String v1 = m1804(stack);
      js.ks result = SPRITE_CACHE.get(v1);

      js.ks v2 = result;
      if (v2 != null) {
         return v2.f1482;
      } else {
         try {
            ItemRenderState v3 = new ItemRenderState();
            mc.method_65386().method_65598(v3, stack, ItemDisplayContext.field_4317, mc.field_1687, null, 0);
            Sprite v4 = v3.method_65603(RANDOM);
            if (v4 != null) {
               SPRITE_CACHE.put(v1, new js.ks(v4));
               return v4;
            }
         } catch (Exception v6) {
         }

         return null;
      }
   }

   public static void drawItem(ItemStack stack, float x, float y, float scale, float alpha, int tintColor) {
      if (!stack.method_7960() && !(alpha <= 0.01F)) {
         if (!needsContextRender(stack)) {
            Sprite v6 = m1803(stack);
            if (v6 != null) {

               int v7 = m1805(tintColor, alpha);
               float result = 16.0F * scale;

               float v8 = result;
               us.drawSprite(v6, x, y, v8, v8, v7, true);
            }
         }
      }
   }

   public static void drawItem(ItemStack stack, float x, float y, float scale, float alpha) {
      drawItem(stack, x, y, scale, alpha, -1);

      
   }

   public static void drawItemWithContext(DrawContext context, ItemStack stack, float x, float y, float scale, float alpha) {
      if (!stack.method_7960() && !(alpha <= 0.01F)) {
         float v6 = m1802();
         float v7 = scale * v6;
         float v8 = 16.0F * scale;
         float v9 = x + v8 / 2.0F;
         float v10 = y + v8 / 2.0F;
         Matrix3x2fStack v11 = context.method_51448();
         v11.pushMatrix();
         v11.translate(v9, v10);
         int v12 = -1259801219;

         v11.scale(v7, v7);

         v11.translate(-8.0F, -8.0F);
         context.method_51427(stack, 0, 0);
         v11.popMatrix();
      }
   }

   private static int m1801() {
      int v0 = (Integer)mc.field_1690.method_42474().method_41753();
      if (v0 == 0) {
         Window result = mc.method_22683();

         v0 = result.method_4476(0, mc.method_1573());
      }

      return v0;
   }

   public static boolean isBlockItem(ItemStack stack) {

      return stack.method_7909() instanceof BlockItem;
   }

   public static boolean isPotionItem(ItemStack stack) {
      if (stack.method_7909() != Items.field_8574 && stack.method_7909() != Items.field_8436) {
         Item result = stack.method_7909();
         int v1 = -2009865502;
         int temp1 = v1 * (v1 + 1);

         if (result != Items.field_8150 && stack.method_7909() != Items.field_8087) {
            return false;
         }
      }

      return true;
   }

   public static void drawBlockItem(DrawContext context, ItemStack stack, float x, float y, float scale, float alpha) {
      if (!stack.method_7960() && !(alpha <= 0.01F)) {
         float v6 = m1802();

         float v7 = scale * v6;
         float v8 = 16.0F * scale;
         float v9 = x + v8 / 2.0F;
         float result = y + v8 / 2.0F;

         float v10 = result;
         Matrix3x2fStack v11 = context.method_51448();
         v11.pushMatrix();
         v11.translate(v9, v10);
         v11.scale(v7, v7);
         v11.translate(-8.0F, -8.0F);
         context.method_51427(stack, 0, 0);
         v11.popMatrix();
      }
   }

   private static String m1804(ItemStack stack) {
      int v1 = stack.method_57353().hashCode();
      String v2 = stack.method_7909().toString();
      StringBuilder result = new StringBuilder().append((Object)v2);

      return result.append("_").append(v1).toString();
   }

   public static void drawItemCentered(ItemStack stack, float centerX, float centerY, float scale, float alpha) {
      float v5 = 16.0F * scale;
      float v6 = centerX - v5 / 2.0F;
      float result = centerY - v5 / 2.0F;

      float v7 = result;
      drawItem(stack, v6, v7, scale, alpha);
   }

   public static boolean needsContextRender(ItemStack stack) {

      return isBlockItem(stack) || isPotionItem(stack) || hasGlint(stack);
   }

   @Environment(EnvType.CLIENT)
   private static record ks(Sprite sprite) {
      private final Sprite f1482;

      private ks(Sprite sprite) {
         this.f1482 = sprite;
      }

      @Override
      public final boolean equals(Object o) {
         boolean result = ObjectMethods.bootstrap<"equals",js.ks,"sprite",js.ks::f1482>(this, o);
         int v2 = 1963847184;

         return result;
      }

      public Sprite sprite() {
         return this.f1482;
      }

      
   }
}
