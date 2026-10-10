package ru.white.module.impl.display.interfaceimpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import ru.white.utils.misc.Vf;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.render.bt;
import ru.white.utils.render.vr;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class WaterMark implements element {
   private static final Identifier AVATAR = Identifier.method_60655("client", "textures/user_clickgui_avatar.png");
   private final bt fpsText = new bt(3.0F);
   private final bt pingText = new bt(3.0F);

   @Override
   public void onRender(vf dragSetting, hh interFace) {
      if (interFace.isHud2()) {
         this.renderHud2(dragSetting);
      } else {
         float v3 = hh.getInstance().sizeHud.getValue();
         float v5 = dragSetting.position.x;
         float v6 = dragSetting.position.y;
         yt v7 = hu.aeonik;
         yt v8 = hu.mainmenuicons;
         String v9 = System.getProperty("user.name") != null ? System.getProperty("user.name") : "Zodiac";

         try {
            if (mc.method_1562() != null && mc.field_1724 != null) {
               PlayerListEntry v11 = mc.method_1562().method_2871(mc.field_1724.method_5667());
               if (v11 != null) {
                  v10 = v11.method_2959();
               }
            }
         } catch (Exception v41) {
         }

         int v42 = mc.method_47599();
         this.fpsText.set(String.valueOf(v42));
         this.pingText.set(String.valueOf(v10));
         float v12 = v7.getWidth(v9, 9.0F * v3);
         float v13 = v7.getWidth(v42 + "fps", 9.0F * v3);
         float v14 = v7.getWidth(v10 + "ms", 9.0F * v3);
         float v15 = v7.getWidth(String.valueOf(v42), 9.0F * v3);
         float v16 = v7.getWidth(String.valueOf(v10), 9.0F * v3);
         float v17 = 22.4F * v3;
         float v18 = v12 + v13 + v14 + 76.2F * v3;
         float v19 = v17 + 1.0F * v3;
         dragSetting.size.set(v18, v19);
         float v21 = v6 - 4.5F * v3;
         float v22 = v5 - 18.0F * v3 + 15.0F * v3 + 2.5F * v3;
         float v23 = v21 + 5.0F * v3;
         float v24 = v12 + v13 + v14 + 76.2F * v3;
         float v26 = hh.getInstance().alphaHUD.getValue();
         vr.glow(v22, v23, v24, v17 - 0.5F * v3, up.getColor(0, 0.06F * v26), 12.0F * v3, 8.0F * v3, 0.5F);
         vr.blur(v22, v23, v24, v17, 1.0F, 12.0F * v3, up.replAlpha(up.background(), v26));
         int v27 = up.getColor(255, 0.09F * v26);
         vr.rect(v5 + 0.7F * v3, v21 + 6.3F * v3, v12 + 25.2F * v3, 19.85F * v3, v27, 11.0F * v3, 2.5F * v3, 2.5F * v3, 11.0F * v3);
         vr.rect(v5 + v12 + 26.6F * v3, v21 + 6.3F * v3, v13 + 23.5F * v3, 19.85F * v3, v27, 2.5F * v3);
         vr.rect(
            v5 + v12 + 26.6F * v3 + v13 + 24.2F * v3,
            v21 + 6.3F * v3,
            v14 + 23.7F * v3,
            19.85F * v3,
            v27,
            2.5F * v3,
            11.0F * v3,
            11.0F * v3,
            2.5F * v3
         );
         float v28 = v5 + 15.0F * v3 + 21.0F * v3 + 10.0F * v3 + 5.0F * v3;
         float v29 = v21 + 9.5F * v3;
         int v30 = up.client();
         int v31 = up.replAlpha(v30, 0.92F * v26);
         int v32 = up.getColor(20, 20, 20, (int)(235.0F * v26));
         vr.glow(v28 - 44.5F * v3, v29 + 1.45F * v3, 9.5F * v3, 9.5F * v3, up.replAlpha(v30, 0.15F * v26), 1.1F * v3, 6.0F * v3, 1.0F);
         vr.rect(v28 - 44.5F * v3, v29 + 1.45F * v3, 9.5F * v3, 9.5F * v3, v31, 1.1F * v3);
         vr.rect(v28 - 44.5F * v3 + v12 + 25.5F * v3, v29 + 1.45F * v3, 9.5F * v3, 9.5F * v3, v31, 1.1F * v3);
         vr.rect(v28 - 44.5F * v3 + v12 + v13 + 24.3F * v3 + 25.5F * v3, v29 + 1.45F * v3, 9.5F * v3, 9.5F * v3, v31, 1.1F * v3);
         float temp2 = v28 - 43.75F * v3;
         float temp3 = v29 + 2.5F * v3;
         float temp4 = 8.0F * v3;

         v8.draw(
            "e", temp2, temp3, temp4, up.getColor(20, 20, 20, (int)(235.0F * v26))
         );
         v8.draw(
            "m",
            v28 - 42.6F * v3 + v12 + 25.0F * v3,
            v29 + 3.0F * v3,
            7.0F * v3,
            up.getColor(20, 20, 20, (int)(235.0F * v26))
         );
         temp2 = v28 - 42.6F * v3;

         v8.draw(
            "o",
            temp2 + v12 + v13 + 23.6F * v3 + 25.0F * v3,
            v29 + 2.5F * v3,
            8.0F * v3,
            up.getColor(20, 20, 20, (int)(235.0F * v26))
         );
         int v33 = up.getColor(255, 0.98F * v26);
         int v34 = up.replAlpha(v30, 0.98F * v26);
         v7.draw(v9, v28 - 32.2F * v3, v29 + 1.0F * v3, 9.0F * v3, v33);
         float v35 = v28 - 32.2F * v3 + v12 + 25.7F * v3;
         v7.draw(String.valueOf(v42), v35, v29 + 1.0F * v3, 9.0F * v3, v33);
         v7.draw("fps", v35 + v15 + 0.5F * v3, v29 + 1.0F * v3, 9.0F * v3, v34);
         float v36 = v28 - 32.2F * v3 + v12 + v13 + 23.5F * v3 + 25.5F * v3;
         v7.draw(String.valueOf(v10), v36, v29 + 1.0F * v3, 9.0F * v3, v33);
         v7.draw("ms", v36 + v16 + 0.5F * v3, v29 + 1.0F * v3, 9.0F * v3, v34);
      }
   }

   private void renderHud2(vf dragSetting) {
      float v2 = hh.getInstance().sizeHud.getValue();
      float v3 = dragSetting.position.x;
      float v4 = dragSetting.position.y;
      float v5 = hh.getInstance().alphaHUD.getValue();
      yt v6 = hu.sf_medium;
      yt v7 = hu.sf_bold;
      yt v8 = hu.mainmenuicons;
      String v9 = System.getProperty("user.name") != null ? System.getProperty("user.name") : "Zodiac";

      try {
         if (mc.method_1562() != null && mc.field_1724 != null) {
            PlayerListEntry v11 = mc.method_1562().method_2871(mc.field_1724.method_5667());
            if (v11 != null) {
               v10 = v11.method_2959();
            }
         }
      } catch (Exception v48) {
      }

      int v49 = mc.method_47599();
      this.fpsText.set(String.valueOf(v49));
      this.pingText.set(String.valueOf(v10));
      float v12 = 17.0F * v2;
      float v13 = 8.0F * v2;
      float v14 = 7.5F * v2;
      float v15 = 6.8F * v2;
      float v16 = 5.5F * v2;
      float result = 5.0F * v2;

      float v17 = result;
      float v18 = 3.0F * v2;
      float v19 = 10.5F * v2;
      float v20 = v7.getWidth("Rose", v14);
      float v21 = v6.getWidth("Solution", v14);
      float v22 = 2.0F * v2;
      float v23 = 6.8F * v2;
      float v24 = v6.getWidth(v9, v14);
      String v25 = String.valueOf(v49);
      float v26 = 6.2F * v2;
      float v27 = v6.getWidth(v25, v14);
      float v28 = v6.getWidth(" fps", v15);
      String v29 = String.valueOf(v10);
      float v30 = 6.8F * v2;
      float v31 = v6.getWidth(v29, v14);
      float v32 = v6.getWidth(" ms", v15);
      float v33 = 0.6F * v2;
      float v34 = v16
         + v19
         + v22
         + v20
         + v22
         + v21
         + v17
         + v33
         + v17
         + v23
         + v18
         + v24
         + v17
         + v33
         + v17
         + v26
         + v18
         + v27
         + v28
         + v17
         + v33
         + v17
         + v30
         + v18
         + v31
         + v32
         + v16;
      dragSetting.size.set(v34, v12);
      if (!(v5 <= 0.01F)) {
         vr.glow(v3, v4, v34, v12, up.getColor(0, 0.08F * v5), v13, 5.0F * v2, 0.5F);
         vr.blur(v3, v4, v34, v12, v5, v13, up.replAlpha(up.background(), v5));
         vr.outline(
            v3,
            v4,
            v34,
            v12,
            0.5F * v2,
            up.getColor(255, 255, 255, (int)(25.0F * v5)),
            v13
         );
         float v35 = v4 + (v12 - v14) / 2.0F - 0.2F * v2;
         float v36 = 7.5F * v2;
         float v37 = v4 + (v12 - v36) / 2.0F;
         int v38 = up.getColor(255, 255, 255, (int)(26.0F * v5));
         int v39 = up.getColor(220, 220, 230, (int)(240.0F * v5));
         int v40 = up.getColor(145, 145, 155, (int)(200.0F * v5));
         int v41 = up.getColor(170, 170, 185, (int)(225.0F * v5));
         float v42 = v3 + v16;
         float v43 = v4 + (v12 - v19) / 2.0F;
         vr.texture(AVATAR, v42, v43, v19, v19, 0.0F, 0.0F, 1.0F, 1.0F, up.replAlpha(up.getColor(255), v5), 1.0F, 2.5F * v2);
         v42 += v19 + v22;
         v7.draw("Rose", v42, v35, v14, up.getColor(245, 245, 250, (int)(255.0F * v5)));
         v42 += v20 + v22;
         v6.draw("Solution", v42, v35, v14, up.replAlpha(up.client(), v5));
         v42 += v21 + v17;
         vr.rect(v42, v37, v33, v36, v38, 0.3F * v2);
         v42 += v33 + v17;
         float v44 = v4 + (v12 - v23) / 2.0F - 0.2F * v2;
         v8.draw("e", v42, v44, v23, v41);
         v42 += v23 + v18;
         v6.draw(v9, v42, v35, v14, v39);
         v42 += v24 + v17;
         vr.rect(v42, v37, v33, v36, v38, 0.3F * v2);
         v42 += v33 + v17;
         float v45 = v4 + (v12 - v26) / 2.0F;
         v8.draw("m", v42, v45, v26, v41);
         v42 += v26 + v18;
         v6.draw(v25, v42, v35, v14, up.replAlpha(up.client(), v5));
         v42 += v27;
         v6.draw(" fps", v42, v35 + 0.3F * v2, v15, v40);
         v42 += v28 + v17;
         vr.rect(v42, v37, v33, v36, v38, 0.3F * v2);
         v42 += v33 + v17;
         float v46 = v4 + (v12 - v30) / 2.0F - 0.2F * v2;
         v8.draw("o", v42, v46, v30, v41);
         v42 += v30 + v18;
         v6.draw(v29, v42, v35, v14, up.replAlpha(up.client(), v5));
         v42 += v31;
         v6.draw(" ms", v42, v35 + 0.3F * v2, v15, v40);
      }
   }

   
}
