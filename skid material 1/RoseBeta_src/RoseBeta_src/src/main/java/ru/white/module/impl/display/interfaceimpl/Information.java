package ru.white.module.impl.display.interfaceimpl;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.utils.misc.Vf;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.render.bt;
import ru.white.utils.render.vr;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class Information implements element {
   private final bt bpsText;
   private final bt coordX = new bt(3.0F);
   private final bt coordY = new bt(3.0F);
   private final bt coordZ = new bt(3.0F);

   @Override
   public void onRender(vf dragSetting, hh interFace) {
      if (interFace.isHud2()) {
         this.renderHud2(dragSetting);
      } else {
         float v3 = hh.getInstance().sizeHud.getValue();
         float v4 = dragSetting.position.x;
         float v5 = dragSetting.position.y;
         yt v6 = hu.aeonik;
         yt v7 = hu.mainmenuicons;
         yt v8 = hu.icons2;

         double v9 = mc.field_1724.method_23317() - mc.field_1724.field_6014;
         double v11 = mc.field_1724.method_23321() - mc.field_1724.field_5969;
         float v13 = (float)(Math.sqrt(v9 * v9 + v11 * v11) * 20.0);
         long result = Math.round(mc.field_1724.method_23317());
         int v36 = (int)mc.field_1724.method_23321();
         int v37 = (int)mc.field_1724.method_23318();
         long v38 = result;
         String v14 = v38 + ", " + v37 + ", " + v36;
         String v15 = String.format(Locale.US, "%.1f", v13);
         float v16 = v6.getWidth(v14, 9.0F * v3);
         float v17 = v6.getWidth(v15 + "b/s", 9.0F * v3);
         float v18 = v6.getWidth(v15, 9.0F * v3);
         float v19 = 22.4F * v3;
         float v20 = v16 + v17 + 76.2F * v3 - 24.3F * v3;
         float v21 = v19 + 1.0F * v3;
         dragSetting.size.set(v20, v21);
         float v23 = v5 - 4.5F * v3;
         float v24 = v4 - 18.0F * v3 + 15.0F * v3 + 2.5F * v3;
         float v25 = v23 + 5.0F * v3;
         float v26 = v16 + v17 + 76.2F * v3 - 24.3F * v3;
         float v28 = hh.getInstance().alphaHUD.getValue();
         float temp3 = v19 - 0.5F * v3;
         int temp4 = up.getColor(0, 0.06F * v28);
         float temp5 = 12.0F * v3;
         float var10006 = 8.0F * v3;

         vr.glow(v24, v25, v26, temp3, temp4, temp5, var10006, 0.5F);
         vr.blur(v24, v25, v26, v19, 1.0F, 12.0F * v3, up.replAlpha(up.background(), v28));
         int v29 = up.getColor(255, 0.09F * v28);
         vr.rect(v4 + 0.7F * v3, v23 + 6.3F * v3, v16 + 25.2F * v3, 19.85F * v3, v29, 11.0F * v3, 2.5F * v3, 2.5F * v3, 11.0F * v3);
         vr.rect(
            v4 + v16 + 26.6F * v3, v23 + 6.3F * v3, v17 + 23.7F * v3, 19.85F * v3, v29, 2.5F * v3, 11.0F * v3, 11.0F * v3, 2.5F * v3
         );
         float v30 = v4 + 15.0F * v3 + 21.0F * v3 + 10.0F * v3 + 5.0F * v3;
         float v31 = v23 + 10.0F * v3;
         int v32 = up.client();
         int v33 = up.replAlpha(v32, 0.92F * v28);
         vr.glow(v30 - 44.5F * v3, v31 + 1.45F * v3, 9.5F * v3, 9.5F * v3, up.replAlpha(v32, 0.15F * v28), 1.1F * v3, 6.0F * v3, 0.4F);
         vr.rect(v30 - 44.5F * v3, v31 + 1.45F * v3, 9.5F * v3, 9.5F * v3, v33, 1.1F * v3);
         vr.rect(v30 - 44.5F * v3 + v16 + 25.5F * v3, v31 + 1.45F * v3, 9.5F * v3, 9.5F * v3, v33, 1.1F * v3);
         v7.draw(
            "x",
            v30 - 43.67F * v3,
            v31 + 2.45F * v3,
            8.0F * v3,
            up.getColor(20, 20, 20, (int)(235.0F * v28))
         );
         v8.draw(
            "@",
            v30 - 43.67F * v3 + v16 + 25.0F * v3,
            v31 + 5.0F * v3,
            6.0F * v3,
            up.getColor(20, 20, 20, (int)(235.0F * v28))
         );
         int v34 = up.getColor(255, 0.98F * v28);
         int v35 = up.replAlpha(v32, 0.98F * v28);
         v6.draw(v14, v30 - 32.2F * v3, v31 + 1.3F * v3, 9.0F * v3, v34);
         v6.draw(v15, v30 - 32.2F * v3 + v16 + 25.5F * v3, v31 + 1.3F * v3, 9.0F * v3, v34);
         v6.draw("b/s", v30 - 32.2F * v3 + v16 + 25.0F * v3 + v18 + 1.0F * v3, v31 + 1.3F * v3, 9.0F * v3, v35);
      }
   }

   private void renderHud2(vf dragSetting) {
      float v2 = hh.getInstance().sizeHud.getValue();
      float v3 = dragSetting.position.x;
      float v4 = dragSetting.position.y;
      float v5 = hh.getInstance().alphaHUD.getValue();
      yt v6 = hu.aeonik;
      yt v7 = hu.mainmenuicons;
      double v8 = mc.field_1724.method_23317() - mc.field_1724.field_6014;
      double v10 = mc.field_1724.method_23321() - mc.field_1724.field_5969;
      float v12 = (float)(Math.sqrt(v8 * v8 + v10 * v10) * 20.0);
      long result = Math.round(mc.field_1724.method_23317());
      int v38 = (int)mc.field_1724.method_23321();
      int v39 = (int)mc.field_1724.method_23318();
      long v40 = result;
      String v13 = v40 + ", " + v39 + ", " + v38;
      String v42 = String.format(Locale.US, "%.1f", v12);
      String v14 = v42 + " b/s";
      float v15 = 17.0F * v2;
      float v16 = 12.5F * v2;
      float v17 = 8.5F * v2;
      float v18 = 7.5F * v2;
      float v19 = 8.5F * v2;
      float v20 = 6.5F * v2;
      float v21 = 2.5F * v2;
      float v22 = 1.0F * v2;
      float v23 = 4.5F * v2;
      float v24 = 86.0F * v2;
      float v26 = v20 + v6.getWidth("Information", v17) + v20 + v19 + v20;
      float v25 = Math.max(v24, v26);
      float v27 = v20 + v6.getWidth("XYZ", v18) + v20 * 2.0F + v6.getWidth(v13, v18) + v20;
      float v28 = v20 + v6.getWidth("Speed", v18) + v20 * 2.0F + v6.getWidth(v14, v18) + v20;
      v25 = Math.max(v25, Math.max(v27, v28));
      float v30 = v15 + v21 + 2.0F * v16 + v22 + v23;
      float v31 = Math.min(8.0F * v2, v30 / 2.0F);
      dragSetting.size.set(v25, v30);
      if (!(v5 <= 0.01F)) {
         vr.glow(v3, v4, v25, v30, up.getColor(0, 0.08F * v5), v31, 5.0F * v2, 0.5F);
         vr.blur(v3, v4, v25, v30, v5, v31, up.replAlpha(up.background(), v5));
         vr.outline(
            v3,
            v4,
            v25,
            v30,
            0.5F * v2,
            up.getColor(255, 255, 255, (int)(25.0F * v5)),
            v31
         );
         float v32 = v4 + v15 / 2.0F;
         v6.draw(
            "Information",
            v3 + v20,
            v32 - v17 / 2.0F + 0.8F * v2,
            v17,
            up.getColor(235, 235, 240, (int)(255.0F * v5))
         );
         v7.draw("x", v3 + v25 - v20 - v19, v32 - v19 / 2.0F + 1.0F * v2, v19, up.replAlpha(up.client(), v5));
         float v33 = v4 + v15 + v21;
         float v34 = v33 + v16 / 2.0F;
         float temp2 = v3 + v20;
         float temp3 = v34 - v18 / 2.0F + 0.8F * v2;

         v6.draw(
            "XYZ", temp2, temp3, v18, up.getColor(170, 170, 175, (int)(255.0F * v5))
         );
         float v35 = v6.getWidth(v13, v18);
         v6.draw(v13, v3 + v25 - v20 - v35, v34 - v18 / 2.0F + 0.8F * v2, v18, up.replAlpha(up.client(), v5));
         v33 += v16 + v22;
         float v36 = v33 + v16 / 2.0F;
         v6.draw(
            "Speed",
            v3 + v20,
            v36 - v18 / 2.0F + 0.8F * v2,
            v18,
            up.getColor(170, 170, 175, (int)(255.0F * v5))
         );
         float v37 = v6.getWidth(v14, v18);
         v6.draw(v14, v3 + v25 - v20 - v37, v36 - v18 / 2.0F + 0.8F * v2, v18, up.replAlpha(up.client(), v5));
      }
   }

   public Information() {
      this.bpsText = new bt(3.0F);
   }

   
}
