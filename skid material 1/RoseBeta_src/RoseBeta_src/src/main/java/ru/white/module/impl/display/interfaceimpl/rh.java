package ru.white.module.impl.display.interfaceimpl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import ru.white.utils.IInstanceAccess;
import ru.white.utils.misc.Vf;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.module.impl.combat.UseTracker;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.bt;
import ru.white.utils.render.dt;
import ru.white.utils.render.vr;
import ru.white.utils.render.vs;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class rh implements element {
   private static float f547 = 4.0F * rh.S;
   private static float f532 = 4.0F * rh.S;
   private static float f534 = 5.0F * rh.S;
   private final Animation f552;
   private final Map<String, rh.sh> f553;
   private static float f546 = 0.9F * rh.S;
   private static float S = 1.0F;
   private static float f538 = 12.5F * S;
   private float f528 = 0.0F;
   private static float f544 = 15.0F * S;
   private static float f531 = 5.0F * S;
   private float f529 = 0.0F;
   private static float H = 16.0F * S;
   private static float f536 = 5.0F * S;
   private static float f530 = 60.0F * S;
   private static float f533 = 7.0F * S;
   private String head;
   private static float f543 = 5.0F * S;
   private static float f539 = 3.4F * S;
   private static float f537 = 5.5F * S;
   private final ru.white.utils.animation.satoshi.Animation f550;
   private static float f535 = 6.5F * S;
   private static float f548 = 3.2F * S;
   private final ru.white.utils.animation.satoshi.Animation f549 = new ep(300, 1.0);
   private static float f545 = 15.0F * S;
   private static float f540 = 14.0F * S;
   private static float f541 = 22.0F * S;
   private static float f542 = 5.0F * S;
   private float f551;

   public rh() {
      this.f550 = new ep(300, 1.0);
      this.f551 = 0.0F;
      this.f552 = new Animation();
      this.head = "";
      this.f553 = new LinkedHashMap<>();
   }

   private static String m512(int seconds) {
      String result = String.format("%d:%02d", seconds / (60), seconds % (60));

      return result;
   }

   private static String m513(int level) {
      return switch (level) {
         case 1 -> "";
         case 2 -> " II";
         case 3 -> " III";
         case 4 -> " IV";
         case 5 -> " V";
         default -> {
            StringBuilder result = new StringBuilder;

            result./* $VF: Unable to resugar constructor */<init>();
            yield result.append(" ").append(level).toString();
         }
      };
   }

   @Override
   public void onRender(vf dragSetting, hh interFace) {
      ug v3 = ug.getInstance();
      S = hh.getInstance().sizeHud.getValue();
      H = 16.0F * S;
      f530 = 60.0F * S;
      f531 = 5.0F * S;
      f532 = 4.0F * S;
      f533 = 7.0F * S;
      f534 = 5.0F * S;
      f535 = 6.5F * S;
      f536 = 5.0F * S;
      f537 = 5.5F * S;
      f538 = 12.5F * S;
      f539 = 3.4F * S;
      f540 = 14.0F * S;
      f541 = 22.0F * S;
      f542 = 5.0F * S;
      f543 = 5.0F * S;
      f544 = 15.0F * S;
      f545 = 15.0F * S;
      f546 = 0.9F * S;
      f547 = 4.0F * S;
      f548 = 3.2F * S;
      this.f553.values().forEach(row -> {

         row.active = false;
      });
      boolean v4 = v3.hasTarget();
      if (v4) {
         for (fr.hr v8 : fr.hr.values()) {
            int v9 = v3.remaining(v8);
            if (v9 > 0) {
               rh.sh v10 = this.f553.computeIfAbsent(v8.name(), k -> {
                  rh.sh var10000x = new rh.sh;

                  var10000x./* $VF: Unable to resugar constructor */<init>();
                  return var10000x;
               });
               v10.label = v8.label;
               v10.color = up.getColor(240);
               v10.time.set(m512(v9));
               v10.active = true;
            }
         }

         PlayerEntity v28 = v3.getTargetPlayer();
         if (v28 != null) {
            for (fr.gr v36 : fr.gr.values()) {
               if (v3.buffs.getValue(v36.label)) {
                  StatusEffectInstance v38 = v28.method_6112(v36.effect);
                  if (v38 != null) {
                     rh.sh v11 = this.f553.computeIfAbsent(v36.name(), k -> new rh.sh());
                     String temp1 = v36.label;
                     String v24 = m513(v38.method_5578() + 1);
                     String v25 = temp1;
                     v11.label = v25 + v24;
                     v11.color = v36.color;
                     v11.time.set(m512(v38.method_5584() / (20)));
                     v11.active = true;
                  }
               }
            }
         }
      }

      String v29 = v4 ? v3.getTargetName() : "";
      if (!v29.isEmpty()) {
         this.head = v29;
      }

      this.f552.update();
      this.f552.run(v29.isEmpty() ? 0.0 : 1.0, 0.12F, ap.QUAD_OUT);
      float v31 = this.f552.get();
      boolean v33 = v31 <= 0.01F && this.f553.values().stream().noneMatch(row -> {

         return row.active;
      });
      float v35 = dragSetting.position.x;
      float v37 = dragSetting.position.y;
      boolean v39 = v33 && !(mc.field_1755 instanceof ChatScreen);
      this.f549.setDirection(v39 ? cp.BACKWARDS : cp.FORWARDS);
      this.f550.setDirection(mc.field_1755 instanceof ChatScreen && v33 ? cp.FORWARDS : cp.BACKWARDS);
      dragSetting.active = !v39;
      float v40 = this.f549.getOutput();
      if (v39 && v40 == 0.0F) {
         this.f553.clear();
         this.head = "";
      } else {
         float v12 = this.f550.getOutput();
         if (interFace.isHud2()) {
            this.m514(dragSetting, v33, v40, v12, v31, interFace);
         } else {
            vs.ys.glow(v35, v37, f530, H, up.replAlpha(up.getColor(0), v12 * 0.1F), f531, 12.0F, 1.0F);
            vs.ws.blur(v35, v37, f530, H, v12, f531, up.replAlpha(up.background(), v12 * hh.getInstance().alphaHUD.getValue()));
            yt v13 = hu.sf_regular;
            hu.nightix_2.draw("V", v35 + f536, v37 + f537, f534, up.replAlpha(up.client(), v12));
            String v47 = "User Tracker";
            float temp2 = v35 + f538;

            v13.draw(v47, temp2, v37 + f539, f533, up.multAlpha(up.getColor(240), v12));
            float v14 = f532;
            float v15 = 0.0F;
            ArrayList v16 = new ArrayList();

            for (Entry v18 : this.f553.entrySet()) {
               rh.sh v19 = (rh.sh)v18.getValue();
               v19.animation.update();
               v19.animation.run(v19.active ? 1.0 : 0.0, 0.12F, ap.QUAD_OUT);
               float v20 = v19.animation.get();
               if (v20 <= 0.01F && !v19.active) {
                  v16.add((String)v18.getKey());
               } else {
                  float v21 = f541 + v13.getWidth(v19.label, f535) + v19.time.width(v13, f535);
                  v15 = Math.max(v15, v21 * v20);
                  v14 += f540 * v20;
               }
            }

            v16.forEach(this.f553::remove);
            if (v31 > 0.01F) {
               v15 = Math.max(v15, (f541 + v13.getWidth(this.head, f535)) * v31);
               v14 += f540 * v31;
            }

            this.f551 = this.f551 + (v15 - this.f551) * 0.2F;
            vs.ys.glow(v35, v37, this.f551, v14 - 0.5F, up.replAlpha(up.getColor(0), v40 * 0.1F), f531, 12.0F, 1.0F);
            vs.ws.blur(v35, v37, this.f551, v14, v40, f531, up.replAlpha(up.background(), v40 * hh.getInstance().alphaHUD.getValue()));
            float v41 = v37 + f543;
            float v42 = 0.0F;
            if (v31 > 0.01F) {
               float v43 = 0.0F;
               hu.category.draw("Q", v35 + f542 - v43, v41 + f546, f535, up.replAlpha(up.client(), v40 * v31));
               v13.draw(this.head, v35 + f545 - v43, v41, f535, up.getColor(240, v40 * v31));
               v41 += f540 * v31;
               v42 += f540 * v31;
            }

            boolean v44 = v31 <= 0.01F;

            for (rh.sh v46 : this.f553.values()) {
               float v22 = v46.animation.get();
               if (!(v22 <= 0.01F) || v46.active) {
                  float v23 = f544 - f544 * v22;
                  v13.draw(v46.label, v35 + f542 - v23, v41, f535, up.replAlpha(v46.color, v40 * v22));
                  v46.time
                     .draw(
                        v13,
                        v35 + v23 - f542 + this.f551 - v46.time.width(v13, f535),
                        v41,
                        f535,
                        up.getColor(200, v40 * v22)
                     );
                  if (!v44) {
                     vs.ys.rect(
                        v35 + f547 + v23,
                        v41 - f548,
                        this.f551 - f547 * 2.0F - v23,
                        0.5F,
                        up.getColor(255, 0.05F * v40 * v22),
                        1.0F
                     );
                  }

                  v44 = false;
                  v41 += f540 * v22;

                  v42 += f540 * v22;
               }
            }

            dragSetting.size.set((float)up.overCol((int)Math.max(this.f551, 20.0F * S), (int)f530, v12), (float)up.overCol((int)v42, (int)H, v12));
         }
      }
   }

   private void m514(vf dragSetting, boolean isEmpty, float alpha, float alpha2, float ha, hh interFace) {
      float v7 = dragSetting.position.x;
      float v8 = dragSetting.position.y;
      float v9 = interFace.alphaHUD.getValue();
      yt v10 = hu.sf_medium;
      float v11 = 17.0F * S;
      float v12 = 12.5F * S;
      float v13 = 8.5F * S;
      float v14 = 7.5F * S;
      float v15 = 8.5F * S;
      float v16 = 6.5F * S;
      float v17 = 4.0F * S;
      float v18 = 1.0F * S;
      float v19 = 4.5F * S;
      boolean v20 = isEmpty && mc.field_1755 instanceof ChatScreen;
      float v22 = 84.0F * S;
      float v23 = 0.0F;
      float v24 = v16 + hu.sf_bold.getWidth("Use Tracker", v13) + v16 + v15 + v16;
      v22 = Math.max(v22, v24);
      if (v20) {
         float v25 = v16
            + v10.getWidth("Golden Apple", v14)
            + v16 * 2.0F
            + v10.getWidth("0:15", v14)
            + v16;
         v22 = Math.max(v22, v25);
         v23 = v12;
         v21 = 1;
      } else {
         if (ha > 0.01F) {
            float v49 = v16 + 10.0F * S + v10.getWidth(this.head, v14) + v16;
            v22 = Math.max(v22, v49);
            v23 += (v12 + v18) * ha;
            v21++;
         }

         for (rh.sh v26 : this.f553.values()) {
            float v27 = v26.animation.get();
            if (!(v27 <= 0.005F) || v26.active) {
               float v28 = v16 + v10.getWidth(v26.label, v14) + v16 * 2.0F + v26.time.width(v10, v14) + v16;
               v22 = Math.max(v22, v28);
               v23 += (v12 + v18) * v27;
               v21++;
            }
         }
      }

      if (v21 == 0 && !v20) {
         float v51 = 84.0F * S;
         dragSetting.size.set(v51, v11);
         this.f529 = 0.0F;
         this.f528 = 0.0F;
      } else {
         float v52 = v11 + (v23 > 0.01F ? v17 + v23 + v19 : 0.0F);
         if (this.f528 <= 0.01F) {
            this.f528 = v22;
         } else {
            this.f528 = this.f528 + (v22 - this.f528) * 0.22F;
         }

         if (this.f529 <= 0.01F) {
            this.f529 = v52;
         } else {
            this.f529 = this.f529 + (v52 - this.f529) * 0.22F;
         }

         float v53 = this.f528;
         float v54 = this.f529;
         float v29 = Math.min(8.0F * S, v54 / 2.0F);
         dragSetting.size.set(v53, v54);
         float v30 = (v20 ? alpha2 : alpha) * v9;
         if (!(v30 <= 0.01F)) {
            vr.glow(v7, v8, v53, v54, up.getColor(0, 0.08F * v30), v29, 5.0F * S, 0.5F);
            vr.blur(v7, v8, v53, v54, v30, v29, up.replAlpha(up.background(), v30));
            float temp4 = 0.5F * S;
            int temp5 = 255;

            vr.outline(v7, v8, v53, v54, temp4, up.getColor(temp5, 255, 255, (int)(25.0F * v30)), v29);
            float v31 = v8 + v11 / 2.0F;
            hu.sf_bold
               .draw(
                  "Use Tracker",
                  v7 + v16,
                  v31 - v13 / 2.0F + 0.8F * S,
                  v13,
                  up.getColor(235, 235, 240, (int)(255.0F * v30))
               );
            float temp2 = v7 + v53 - v16 - v15;

            hu.nightix_2.draw("V", temp2, v31 - v15 / 2.0F + 0.8F * S, v15, up.replAlpha(up.client(), v30));
            dt.enable(v7, v8, v53, v54, 2.0F);
            float v32 = v8 + v11 + v17;
            if (v20) {
               float v33 = v32 + v12 / 2.0F;
               v10.draw(
                  "Golden Apple",
                  v7 + v16,
                  v33 - v14 / 2.0F + 0.8F * S,
                  v14,
                  up.getColor(170, 170, 175, (int)(255.0F * v30))
               );
               String v34 = "0:15";
               float v35 = v10.getWidth(v34, v14);
               temp2 = v7 + v53 - v16 - v35;
               float temp3 = v33 - v14 / 2.0F + 0.8F * S;
               temp5 = up.replAlpha(up.client(), v30);

               v10.draw(v34, temp2, temp3, v14, temp5);
            } else {
               if (ha > 0.01F) {
                  float v55 = v12 * ha;
                  float v57 = v32 + v55 / 2.0F;
                  float v59 = v30 * ha;
                  hu.mainmenuicons.draw("e", v7 + v16, v57 - 4.0F * S, 7.5F * S, up.replAlpha(up.client(), v59));
                  v10.draw(
                     this.head, v7 + v16 + 10.0F * S, v57 - v14 / 2.0F + 0.8F * S, v14, up.getColor(240, (int)(255.0F * v59))
                  );
                  v32 += (v12 + v18) * ha;
               }

               for (rh.sh v58 : this.f553.values()) {
                  float v60 = v58.animation.get();
                  if (!(v60 <= 0.005F) || v58.active) {
                     float v36 = v60 * v30;
                     float v37 = v12 * v60;
                     float v38 = v32 + v37 / 2.0F;
                     float v39 = v58.time.width(v10, v14);
                     float v40 = v53 - v16 * 2.0F - v39 - v16;
                     String v41 = v58.label;
                     if (v10.getWidth(v41, v14) > v40) {
                        for (int v42 = v41.length() - 1; v42 > 0; v42--) {
                           String v44 = v41.substring(0, v42);
                           String v43 = v44 + "...";
                           if (v10.getWidth(v43, v14) <= v40) {
                              v41 = v43;
                              break;
                           }
                        }
                     }

                     v10.draw(
                        v41,
                        v7 + v16,
                        v38 - v14 / 2.0F + 0.8F * S,
                        v14,
                        up.getColor(170, 170, 175, (int)(255.0F * v36))
                     );
                     v58.time.draw(v10, v7 + v53 - v16 - v39, v38 - v14 / 2.0F + 0.8F * S, v14, up.replAlpha(up.client(), v36));
                     v32 += (v12 + v18) * v60;
                  }
               }
            }

            dt.disable();
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static class sh {
      boolean active;
      String label;
      final Animation animation;
      int color;
      final bt time = new bt();

      private sh() {
         this.animation = new Animation();
         this.label = "";
      }

      
   }
}
