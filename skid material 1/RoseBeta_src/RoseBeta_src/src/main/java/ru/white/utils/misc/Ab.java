package ru.white.utils.misc;

import java.util.ArrayList;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.joml.Matrix3x2fStack;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.mixin.HandledScreenAccessor;
import ru.white.utils.render.us;
import ru.white.utils.render.vs;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public final class Ab {
   private static final float ROW_H = 21.0F;
   private static final float PANEL_W = 118.0F;

   private static boolean m117(float mouseX, float mouseY, float x, float y, float w, float h) {

      if (mouseX >= x) {
         float v9 = x + w;

         if (mouseX < v9 && mouseY >= y) {

            if (mouseY < y + h) {
               return true;
            }
         }
      }

      return false;
   }

   private Ab() {
   }

   public static void render(DrawContext context, int mouseX, int mouseY) {
      MinecraftClient v3 = MinecraftClient.method_1551();
      if (Client.get() != null && Client.get().inventoryPresetManager() != null) {
         if (v3.field_1755 instanceof HandledScreen v4) {
            InventoryPresetManager v30 = Client.get().inventoryPresetManager();
            HandledScreenAccessor v6 = (HandledScreenAccessor)v4;
            int v7 = v6.getScreenX();
            int v8 = v6.getScreenY();
            float v9 = 2.0F / (float)v3.method_22683().method_4495();
            us.beginOverlay();
            m116(
               context,
               Items.field_8106.method_7854(),
               (float)(v7 + 160) / v9,
               (float)(v8 - (11)) / v9,
               v9
            );
            if (v30.activePreset().isEmpty()) {
               us.endOverlay();
            } else {
               Map v10 = v30.missingItems();
               ArrayList v11 = new ArrayList(v10.values());
               int v12 = Math.min(8, Math.max(1, v11.size()));
               int v13 = v30.missingScroll(v11.size(), v12);
               float v14 = (float)(v7 + 182) / v9;
               float v15 = (float)(v8 + 8) / v9;
               float v16 = 31.0F + (float)v12 * 21.0F + 27.0F;
               float v17 = (float)mouseX / v9;
               float v18 = (float)mouseY / v9;
               yt v19 = hu.sf_regular;
               vs.ys.glow(v14, v15, 118.0F, v16 - 0.5F, up.getColor(0, 0.15F), 8.0F, 15.0F, 1.0F);
               vs.ws.blur(v14, v15, 118.0F, v16, 1.0F, 8.0F, up.multAlpha(up.multDark(up.background(), 0.6F), 1.0F));
               vs.ys.outline(v14, v15, 118.0F, v16, 0.5F, up.replAlpha(up.client(), 0.35F), 8.0F);
               hu.icon.draw("K", v14 + 8.0F, v15 + 11.0F, 6.5F, up.client());
               v19.draw("Не хватает", v14 + 19.0F, v15 + 10.0F, 6.5F, up.getColor(200));
               if (v11.size() > v12) {
                  int result = v13 + 1;
                  int temp1 = Math.min(v11.size(), v13 + v12);
                  int v25 = v11.size();
                  int v26 = temp1;
                  int v27 = result;
                  String v20 = v27 + "–" + v26 + " / " + v25;
                  v19.draw(v20, v14 + 118.0F - 8.0F - v19.getWidth(v20, 5.5F), v15 + 10.5F, 5.5F, up.getColor(255, 0.35F));
               }

               float v31 = v15 + 25.0F;
               if (v10.isEmpty()) {
                  v19.draw(
                     "Все предметы найдены",
                     v14 + 8.0F,
                     v31 + 6.0F,
                     6.0F,
                     up.getColor(110, 220, 140, 1.0F)
                  );
               } else {
                  for (int v21 = v13; v21 < Math.min(v11.size(), v13 + v12); v21++) {
                     va.xa v22 = (va.xa)v11.get(v21);
                     boolean v23 = m117(v17, v18, v14 + 4.0F, v31, 110.0F, 19.0F);
                     vs.ys.rect(v14 + 4.0F, v31, 110.0F, 19.0F, up.getColor(40, v23 ? 0.3F : 0.15F), 5.0F);
                     if (v23) {
                        vs.ys.outline(v14 + 4.0F, v31, 110.0F, 19.0F, 0.5F, up.replAlpha(up.client(), 1.0F), 5.0F);
                     }

                     ItemStack v24 = v22.stack();
                     if (!v24.method_7960()) {
                        m116(context, v24, v14 + 13.0F, v31 + 10.5F - 1.0F, v9);
                     }

                     int v28 = v22.count();
                     v19.draw("x" + v28, v14 + 24.0F, v31 + 5.4F, 8.0F, up.getColor(230));
                     v31 += 21.0F;
                  }
               }

               float v32 = v15 + v16 - 23.0F;
               boolean v33 = m117(v17, v18, v14 + 5.0F, v32, 108.0F, 18.0F);
               boolean v34 = v30.isArranging();
               float v35 = v14 + 5.0F;
               int v36;
               if (v34) {
                  v36 = 200;
                  int temp5 = 60;

                  v36 = up.getColor(v36, temp5, 60, 0.35F);
               } else {
                  v36 = up.replAlpha(up.client(), v33 ? 0.35F : 0.2F);
               }

               vs.ys.rect(v35, v32, 108.0F, 18.0F, v36, 5.0F);
               vs.ys.outline(
                  v14 + 5.0F,
                  v32,
                  108.0F,
                  18.0F,
                  0.5F,
                  v34
                     ? up.getColor(200, 60, 60, 1.0F)
                     : up.replAlpha(up.client(), v33 ? 1.0F : 0.4F),
                  5.0F
               );
               v19.drawCentered(
                  v34 ? "Остановить" : "Разложить всё",
                  v14 + 59.0F,
                  v32 + 5.5F,
                  6.5F,
                  up.getColor(255, 0.9F)
               );
               us.endOverlay();
            }
         }
      }
   }

   private static void m116(DrawContext context, ItemStack stack, float centerX, float centerY, float scaleFix) {
      Matrix3x2fStack v5 = context.method_51448();

      v5.pushMatrix();
      v5.translate(centerX * scaleFix, centerY * scaleFix);
      v5.scale(scaleFix, scaleFix);
      context.method_51427(stack, -8, -8);
      v5.popMatrix();
   }

   
}
