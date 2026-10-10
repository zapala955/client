package ru.white.module.impl.display.interfaceimpl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatScreen;
import ru.white.utils.misc.Vf;
import ru.white.module.Module;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.dt;
import ru.white.utils.render.vr;
import ru.white.utils.SoundUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;

@Environment(EnvType.CLIENT)
public class lh implements element {
   private float f485;
   private float f486;
   private final ep f483 = new ep(300, 1.0);
   private final ep f484 = new ep(300, 1.0);
   private static float S = 1.0F;

   @Override
   public void onRender(vf dragSetting, hh interFace) {
      S = hh.getInstance().sizeHud.getValue();

      for (Module v4 : Client.get().moduleManager().values()) {
         if (v4.getKey() > 0 && !v4.getName().equalsIgnoreCase("ClickGui")) {
            v4.animation.setDirection(v4.isEnabled() ? cp.FORWARDS : cp.BACKWARDS);
         }
      }

      List v41 = Client.get().moduleManager().values().stream().filter(m -> {
         if (m.getKey() > 0 && !m.getName().equalsIgnoreCase("ClickGui")) {
            boolean result = m.isEnabled();

            if (result || m.getAnimation().getOutput() > 0.0F) {
               return true;
            }
         }

         return false;
      }).sorted(Comparator.<Module>comparingInt(m -> {
         int result = m.getName().length() + eq.keyName(m.getKey()).length();

         return result;
      }).reversed()).collect(Collectors.toList());
      boolean v42 = v41.isEmpty();
      float v5 = dragSetting.position.x;
      float v6 = dragSetting.position.y;
      boolean v7 = v42 && !(mc.field_1755 instanceof ChatScreen);
      this.f483.setDirection(v7 ? cp.BACKWARDS : cp.FORWARDS);
      this.f484.setDirection(mc.field_1755 instanceof ChatScreen && v42 ? cp.FORWARDS : cp.BACKWARDS);
      dragSetting.active = !v7;
      float v8 = this.f483.getOutput();
      if (!v7 || v8 != 0.0F) {
         float v9 = this.f484.getOutput();
         yt v10 = hu.aeonik;
         float v11 = 20.0F * S;
         float v12 = 15.8F * S;
         float v13 = 11.7F * S + 0.5F * S;
         float v14 = hh.getInstance().alphaHUD.getValue();
         if (interFace.isHud2()) {
            this.m480(dragSetting, v41, v8, v9, v14);
         } else {
            if (v9 > 0.01F) {
               float v15 = v9 * v14;
               vr.glow(v5, v6, 68.32F * S, v11, up.getColor(0, 0.08F * v15), v13, 6.0F * S, 0.5F);
               vr.blur(v5, v6, 68.32F * S, v11, v15, v13, up.replAlpha(up.background(), v15));
               int v16 = up.getColor(255, 0.09F * v15);
               vr.rect(v5 + 1.3F * S, v6 + 1.3F * S, 68.32F * S - 2.6F * S, v11 - 2.6F * S, v16, 4.7F * S);
               float v17 = v5 + 68.32F * S - 16.5F * S;
               vr.rect(v17, v6 + 6.3F * S, 9.5F * S, 9.5F * S, up.replAlpha(up.client(), 0.92F * v15), 1.1F * S);
               hu.icons5
                  .draw(
                     "B",
                     v17 + 1.3F * S,
                     v6 + 7.9F * S,
                     7.0F * S,
                     up.getColor(20, 20, 20, (int)(235.0F * v15))
                  );
               v10.draw("Binds", v5 + 7.5F * S, v6 + 4.5F * S, 9.1F * S, up.getColor(255, 0.98F * v15));
            }

            boolean v43 = v41.stream().anyMatch(m -> m.isEnabled() && m.getKey() > 0);
            boolean v44 = v41.stream().anyMatch(m -> m.getAnimation().getOutput() > 0.01F);
            if (!v43 && !v44) {
               float v46 = 68.32F * S;
               if (v8 > 0.01F) {
                  float v47 = v8 * v14;
                  vr.glow(v5, v6, v46, v11, up.getColor(0, 0.08F * v47), v13, 6.0F * S, 0.5F);
                  vr.blur(v5, v6, v46, v11, v47, v13, up.replAlpha(up.background(), v47));
                  int v50 = up.getColor(255, 0.09F * v47);
                  vr.rect(v5 + 1.3F * S, v6 + 1.3F * S, v46 - 3.6F * S, v11 - 2.6F * S, v50, 4.7F * S);
                  float v51 = v5 + v46 - 9.5F * S - 7.0F * S;
                  vr.glow(v51, v6 + 6.3F * S, 9.5F * S, 9.5F * S, up.replAlpha(up.client(), 0.15F * v47), 1.1F * S, 6.0F * S, 0.4F);
                  vr.rect(v51, v6 + 6.3F * S, 9.5F * S, 9.5F * S, up.replAlpha(up.client(), 0.92F * v47), 1.1F * S);
                  hu.icons5
                     .draw(
                        "B",
                        v51 + 1.3F * S,
                        v6 + 7.9F * S,
                        7.0F * S,
                        up.getColor(20, 20, 20, (int)(235.0F * v47))
                     );
                  v10.draw("Binds", v5 + 7.5F * S, v6 + 4.5F * S, 9.1F * S, up.getColor(255, 0.98F * v47));
               }

               dragSetting.size.set(v46, v11);
            } else {
               float v45 = 93.0F * S;
               float v18 = v45;
               float v19 = 22.0F * S;

               for (Module v22 : v41) {
                  if (v22.isEnabled() || v22.getAnimation().getOutput() > 0.01F) {
                     v20++;
                  }
               }

               v19 += v12 * (float)v20;
               v19 += 4.0F * S;

               for (Module v54 : v41) {
                  v54.animation.setDirection(v54.isEnabled() ? cp.FORWARDS : cp.BACKWARDS);
               }

               float v53 = v8 * v14;
               vr.glow(v5, v6, v45, v19, up.getColor(0, 0.08F * v53), v13, 6.0F * S, 0.5F);
               vr.blur(v5, v6, v45, v19, v53, v13, up.replAlpha(up.background(), v53));
               int v55 = up.getColor(255, 0.09F * v53);
               vr.rect(v5 + 1.3F * S, v6 + 1.3F * S, 68.32F * S, v11 - 2.6F * S + 1.3F * S, v55, 13.7F * S, 2.5F * S, 2.5F * S, 2.5F * S);
               vr.rect(v5 + 1.3F * S, v6 + 22.5F * S, 68.32F * S, v19 - 24.3F * S + 0.5F * S, v55, 2.5F * S, 4.7F * S, 2.5F * S, 11.0F * S);
               vr.rect(
                  v5 + 1.3F * S + 68.32F * S + 1.0F * S,
                  v6 + 22.5F * S,
                  v45 - 72.0F * S,
                  v19 - 24.3F * S + 0.5F * S,
                  v55,
                  2.5F * S,
                  2.5F * S,
                  13.0F * S,
                  2.5F * S
               );
               float v23 = 9.5F * S;
               float v24 = v5 + v45 - v23 - 7.0F * S;
               float v25 = v6 + 6.3F * S;

               vr.glow(v24, v25, v23, v23, up.replAlpha(up.client(), 0.15F * v53), 1.1F * S, 6.0F * S, 0.4F);
               vr.rect(v24, v25, v23, v23, up.replAlpha(up.client(), 0.92F * v53), 1.1F * S);
               hu.icons5
                  .draw(
                     "B",
                     v24 + 0.6F * S,
                     v25 + 1.25F * S,
                     8.0F * S,
                     up.getColor(20, 20, 20, (int)(235.0F * v53))
                  );
               v10.draw("Binds", v5 + 7.5F * S, v6 + 6.5F * S, 9.1F * S, up.getColor(255, 0.98F * v53));
               float v26 = v6 + 17.5F * S;
               boolean v27 = true;

               for (Module v29 : v41) {
                  float v30 = v29.getAnimation().getOutput();
                  if (!(v30 <= 0.0F)) {
                     String v31 = eq.keyName(v29.getKey());
                     float v32 = v10.getWidth(v31, 7.4F * S);
                     float v33 = v5 + v18 - 5.0F * S - v32 + 2.0F * S - 9.0F * S;
                     float v34 = 5.0F * S + v32;
                     float v35 = v26 + 10.0F * S;
                     float v36 = v26 + 10.6F * S;
                     float v37 = v26 + 11.5F * S;
                     float v38 = v30 * v14;
                     vr.rect(v33, v35, v34, 9.5F * S, up.replAlpha(up.client(), 0.92F * v8 * v38), 1.1F * S);
                     float v39 = v18 - 6.5F * S - v34 - 10.0F * S - 4.0F * S;
                     if (v10.getWidth(v29.getBigName(), 8.7F * S) > v39) {
                        v10.drawFadingText(
                           v29.getBigName(), v5 + 6.5F * S, v36, v39, up.getColor(255, 0.98F * v8 * v38), 8.7F * S
                        );
                     } else {
                        v10.draw(v29.getBigName(), v5 + 6.5F * S, v36, 8.7F * S, up.getColor(255, 0.98F * v8 * v38));
                     }

                     v10.draw(
                        v31,
                        v33 + 2.1F * S,
                        v37,
                        7.4F * S,
                        up.getColor(20, 20, 20, v8 * v38)
                     );
                     if (!v27) {
                        vr.rect(
                           v5 + 4.0F * S, v26 - 3.2F * S, v18 - 8.0F * S, 0.5F * S, up.getColor(255, 0.05F * v8 * v38), 1.0F * S
                        );
                     }

                     v27 = false;
                     v26 += v12 * v30;
                  }
               }

               dragSetting.size.set(v18, v19);
            }
         }
      }
   }

   private String displayName(yt font, String name, float maxWidth, float textSize) {
      if (font.getWidth(name, textSize) <= maxWidth) {
         return name;
      } else {
         for (int v5 = name.length() - 1; v5 > 0; v5--) {
            String v7 = name.substring(0, v5);

            String v6 = v7 + "...";

            if (font.getWidth(v6, textSize) <= maxWidth) {
               return v6;
            }
         }

         String v8 = name.substring(0, 1);
         return v8 + "...";
      }
   }

   public lh() {
      this.f485 = 0.0F;
      this.f486 = 0.0F;
   }

   private void m480(vf dragSetting, List<Module> modules, float alpha, float alphaChat, float hudAlpha) {
      float v6;
      float v7;
      yt v8;
      float v9;
      float v10;
      float v11;
      float v12;
      float v13;
      float v14;
      float v15;
      float v16;
      float v17;
      boolean v54;
      label88: {
         v6 = dragSetting.position.x;
         v7 = dragSetting.position.y;
         v8 = hu.aeonik;
         v9 = 17.0F * S;
         v10 = 12.5F * S;
         v11 = 8.5F * S;
         v12 = 7.5F * S;
         v13 = 8.5F * S;
         v14 = 6.5F * S;
         v15 = 2.5F * S;
         v16 = 1.0F * S;
         v17 = 4.5F * S;
         if (modules.isEmpty()) {
            v54 = mc.field_1755 instanceof ChatScreen;

            if (v54) {
               v54 = true;
               break label88;
            }
         }

         v54 = false;
      }

      boolean v18 = v54;
      float v19 = 84.0F * S;
      float v21 = v14 + v8.getWidth("Keybinds", v11) + v14 + v13 + v14;
      float v20 = Math.max(v19, v21);
      float v22 = 0.0F;
      if (v18) {
         float v24 = v14 + v8.getWidth("Example Key", v12) + v14 * 2.0F + v8.getWidth("C", v12) + v14;
         v20 = Math.max(v20, v24);
         v22 = v10;
         v23 = 1;
      } else {
         for (Module v25 : modules) {
            float v26 = v25.getAnimation().getOutput();
            if (!(v26 <= 0.005F) || v25.isEnabled()) {
               String v27 = eq.keyName(v25.getKey());
               float v28 = v14 + v8.getWidth(v25.getBigName(), v12) + v14 * 2.0F + v8.getWidth(v27, v12) + v14;
               v20 = Math.max(v20, v28);
               v22 += (v10 + v16) * v26;
               v23++;
            }
         }
      }

      if (v23 == 0 && !v18) {
         float v46 = 84.0F * S;
         dragSetting.size.set(v46, v9);
         this.f486 = 0.0F;
         this.f485 = 0.0F;
      } else {
         float v47 = v9 + (v22 > 0.01F ? v15 + v22 + v17 : 0.0F);
         if (this.f485 <= 0.01F) {
            this.f485 = v20;
         } else {
            this.f485 = this.f485 + (v20 - this.f485) * 0.22F;
         }

         if (this.f486 <= 0.01F) {
            this.f486 = v47;
         } else {
            this.f486 = this.f486 + (v47 - this.f486) * 0.22F;
         }

         float v48 = this.f485;
         float v49 = this.f486;
         float v50 = Math.min(8.0F * S, v49 / 2.0F);
         dragSetting.size.set(v48, v49);
         float v29 = (v18 ? alphaChat : alpha) * hudAlpha;
         if (!(v29 <= 0.01F)) {
            vr.glow(v6, v7, v48, v49, up.getColor(0, 0.08F * v29), v50, 5.0F * S, 0.5F);
            vr.blur(v6, v7, v48, v49, v29, v50, up.replAlpha(up.background(), v29));
            vr.outline(
               v6,
               v7,
               v48,
               v49,
               0.5F * S,
               up.getColor(255, 255, 255, (int)(25.0F * v29)),
               v50
            );
            float v30 = v7 + v9 / 2.0F;
            v8.draw(
               "Keybinds",
               v6 + v14,
               v30 - v11 / 2.0F + 0.8F * S,
               v11,
               up.getColor(235, 235, 240, (int)(255.0F * v29))
            );
            hu.icons5.draw("B", v6 + v48 - v14 - v13, v30 - v13 / 2.0F + 1.0F * S, v13, up.replAlpha(up.client(), v29));
            dt.enable(v6, v7, v48, v49, 2.0F);
            float v31 = v7 + v9 + v15;
            if (v18) {
               float v32 = v31 + v10 / 2.0F;
               v8.draw(
                  "Example Key",
                  v6 + v14,
                  v32 - v12 / 2.0F + 0.8F * S,
                  v12,
                  up.getColor(170, 170, 175, (int)(255.0F * v29))
               );
               String v33 = "C";
               float v34 = v8.getWidth(v33, v12);
               v8.draw(v33, v6 + v48 - v14 - v34, v32 - v12 / 2.0F + 0.8F * S, v12, up.replAlpha(up.client(), v29));
            } else {
               for (Module v52 : modules) {
                  float v53 = v52.getAnimation().getOutput();
                  if (!(v53 <= 0.005F) || v52.isEnabled()) {
                     float v35 = v10 * v53;
                     float v36 = v53 * v29;
                     float v37 = (1.0F - v53) * 5.0F * S;
                     float v38 = v31 + v35 / 2.0F;
                     String v39 = eq.keyName(v52.getKey());
                     float v40 = v8.getWidth(v39, v12);
                     float v41 = v48 - v14 * 2.0F - v40 - v14;
                     String v42 = this.displayName(v8, v52.getBigName(), v41, v12);
                     float temp2 = v6 + v14 - v37;

                     v8.draw(
                        v42,
                        temp2,
                        v38 - v12 / 2.0F + 0.8F * S,
                        v12,
                        up.getColor(170, 170, 175, (int)(255.0F * v36))
                     );
                     v8.draw(v39, v6 + v48 - v14 - v40 + v37, v38 - v12 / 2.0F + 0.8F * S, v12, up.replAlpha(up.client(), v36));
                     v31 += (v10 + v16) * v53;
                  }
               }
            }

            dt.disable();
         }
      }
   }

   
}
