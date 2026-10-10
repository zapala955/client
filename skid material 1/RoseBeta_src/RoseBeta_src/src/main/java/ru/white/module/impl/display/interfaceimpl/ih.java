package ru.white.module.impl.display.interfaceimpl;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.white.utils.misc.Vf;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.InterFace;
import ru.white.theme.wo;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.render.vr;
import ru.white.utils.render.vs;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class ih implements element {
   private static float S = 1.0F;

   private void m460(vf dragSetting, List<Module> activeModules, float dragX, float dragY, boolean rightSide) {
      float v6 = hh.getInstance().alphaHUD.getValue();
      float v7 = 7.5F * S;
      float v8 = 12.5F * S;
      float v9 = 3.5F * S;
      yt v10 = hu.sf_medium;
      float v11 = 0.0F;

      for (Module v13 : activeModules) {
         v11 = Math.max(v11, v10.getWidth(v13.getBigName(), v7));
      }

      float v28 = v11 + 12.0F * S;
      float v29 = dragY;

      for (Module v15 : activeModules) {
         v15.animation15.setDirection(v15.isEnabled() ? cp.FORWARDS : cp.BACKWARDS);

         float v16 = v15.animation15.getOutput();
         if (!(v16 <= 0.005F)) {
            String v17 = v15.getBigName();
            float v18 = v10.getWidth(v17, v7);
            float v19 = v16 * v6;
            float v20 = v18 + 9.5F * S;
            float v21 = rightSide ? dragX + v28 - v20 : dragX;
            vr.glow(v21, v29, v20, v8, up.getColor(0, 0.08F * v19), v9, 4.0F * S, 0.4F);
            vr.blur(v21, v29, v20, v8, v19, v9, up.replAlpha(up.background(), v19));
            vr.outline(
               v21,
               v29,
               v20,
               v8,
               0.5F * S,
               up.getColor(255, 255, 255, (int)(25.0F * v19)),
               v9
            );
            float v22 = 1.5F * S;
            float v23 = rightSide ? v21 + v20 - v22 : v21;
            vr.rect(v23, v29, v22, v8, up.replAlpha(up.client(), v19), v9);
            float v24 = rightSide ? v21 + 4.0F * S : v21 + v22 + 4.0F * S;
            float v25 = v29 + (v8 - v7) / 2.0F + 0.8F * S;
            v10.draw(
               v17, v24, v25, v7, up.getColor(235, 235, 240, (int)(255.0F * v19))
            );
            v29 += (v8 + 1.5F * S) * v16;
         }
      }

      float v30 = Math.max(v29 - dragY, v8);
      dragSetting.size.set(v28, v30);
      
   }

   private static float m459() {
      float result = wo.getOpacity();

      float v0 = result;
      return v0 < 0.98F ? v0 : 0.98F;
   }

   @Override
   public void onRender(vf dragSetting, hh interFace) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         S = hh.getInstance().sizeHud.getValue();
         float v3 = 7.0F * S;
         float v4 = 12.0F * S;
         float v5 = Math.max(1.0F, 1.0F * S);
         float v6 = 4.0F * S;
         List v7 = Client.get().moduleManager().values().stream().filter(module -> {
            Animation var10000x = module.getAnimation();

            return var10000x.getOutput() > 0.0F && module.getCategory() != Category.RENDER;
         }).sorted((m1, m2) -> {
            int v3x = (int)hu.sf_regular.getWidth(m1.getBigName(), v3);
            int v4x = (int)hu.sf_regular.getWidth(m2.getBigName(), v3);

            return Integer.compare(v4x, v3x);
         }).toList();
         boolean v8 = mc.field_1755 instanceof ChatScreen;
         if (v7.isEmpty() && !v8) {
            dragSetting.active = false;
         } else {
            dragSetting.active = true;
            float v9 = 0.0F;

            for (Module v11 : v7) {
               v9 = Math.max(v9, hu.sf_regular.getWidth(v11.getBigName(), v3));
            }

            float v27 = v9 + 11.0F * S;
            float v28 = dragSetting.position.x;
            float v12 = dragSetting.position.y;
            float v13 = (float)mc.method_22683().method_4495();
            int v14 = (int)((float)mc.method_22683().method_4486() / v13);
            boolean v15 = v28 + v27 / 2.0F > (float)v14 / 2.0F;
            if (interFace.isHud2()) {
               this.m460(dragSetting, v7, v28, v12, v15);
            } else {
               float v16 = v12;

               for (Module v18 : v7) {
                  v18.animation15.setDirection(v18.isEnabled() ? cp.FORWARDS : cp.BACKWARDS);
                  String v19 = v18.getBigName();
                  float v20 = hu.sf_regular.getWidth(v19, v3);
                  float v21 = v18.animation15.getOutput();
                  if (!(v21 <= 0.0F)) {
                     float v31;
                     if (v15) {

                        v31 = v28 + v27 - 5.0F * S - (v20 + 4.0F * S) - 2.0F * S;
                     } else {
                        v31 = v28 + 7.0F * S + 2.0F * S;
                     }

                     float v22 = v31;
                     float v23 = v20 + 8.0F * S;
                     v31 = v22 - 2.0F * S;

                     vs.ws.blur(v31, v16 - 1.0F * S, v23, v4, v21, v6, up.replAlpha(up.background(), hh.getInstance().alphaHUD.getValue() * v21));
                     hu.sf_regular.draw(v19, v22 + 1.5F * S, v16 + 0.5F * S, v3, wo.getTextColor(v21));
                     v16 += (v4 + 0.5F * S) * v21;
                  }
               }

               float v29 = Math.max(v16 - v12, v4);
               if (!v7.isEmpty()) {
                  float v30 = v15 ? v28 + v27 - 5.0F * S : v28 + 4.0F * S;
                  vs.ws.blur(
                     v30 - 2.0F * S,
                     v12 - 1.0F * S - 2.0F * S,
                     v5 + 4.0F * S,
                     v29 + 4.0F * S,
                     1.0F,
                     2.0F * S,
                     up.replAlpha(up.background(), hh.getInstance().alphaHUD.getValue())
                  );

                  vs.ys.rect(v30, v12 - 1.0F * S, v5, v29, wo.getHudColor(), 3.0F * S);
               }

               dragSetting.size.set(v27, v29);
            }
         }
      }
   }

   
}
