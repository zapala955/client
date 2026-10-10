package ru.white.module.impl.display.interfaceimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import ru.white.utils.misc.Vf;
import ru.white.utils.misc.Sp;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.dt;
import ru.white.utils.render.vr;
import ru.white.utils.IMinecraft;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;
import ru.white.event.impl.EventReceivePacket;

@Environment(EnvType.CLIENT)
public class Potions implements IMinecraft {
   private final ep animChat;
   private final Map<String, Potions.EffectData> displayedEffects;
   private float smoothWidth;
   private final ep animIn = new ep(300, 1.0);
   private float smoothHeight;
   private static float S = 1.0F;

   private String toRoman(int n) {
      String result;
      switch (n) {
         case 1:
            result = "I";
            break;
         case 2:
            result = "II";
            break;
         case 3:
            result = "III";
            break;
         case 4:
            result = "IV";
            
            break;
         case 5:
            result = "V";
            break;
         case 6:
            result = "VI";
            break;
         default:
            result = String.valueOf(n);
      }

      return result;
   }

   private String getDurationString(StatusEffectInstance effect) {
      if (effect.method_48559()) {
         return "∞";
      } else {
         int v2 = effect.method_5584() / (20);
         Object[] temp1 = new Object[]{v2 / (60), null};

         temp1[1] = v2 % (60);
         return String.format("%d:%02d", temp1);
      }
   }

   private void renderHud2(
      vf dragSetting, List<Potions.EffectData> sortedEffects, boolean isEmpty, float alpha, float alphaChat, float hudAlpha, EventReceivePacket eventDisplay
   ) {
      float v8 = dragSetting.position.x;
      float v9 = dragSetting.position.y;
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
      float v24 = v16 + v10.getWidth("Potions", v13) + v16 + v15 + v16;
      v22 = Math.max(v22, v24);
      if (v20) {
         float v25 = v16 + 11.0F * S + v10.getWidth("Speed II", v14) + v16 * 2.0F + v10.getWidth("1:30", v14) + v16;
         v22 = Math.max(v22, v25);
         v23 = v12;
         v21 = 1;
      } else {
         for (Potions.EffectData v26 : sortedEffects) {
            float v27 = v26.animation.get();
            if (!(v27 <= 0.005F) || v26.active) {
               int v28 = v26.lvl;
               String result;
               if (v28 > 1) {
                  String v49 = this.toRoman(v28);
                  result = " " + v49;
               } else {
                  result = "";
               }

               String v29 = result;
               String v51 = v26.name;
               StringBuilder v72 = new StringBuilder().append((Object)v51).append((Object)v29);

               String v30 = v72.toString();
               float v31 = v16 + 11.0F * S + v10.getWidth(v30, v14) + v16 * 2.0F + v10.getWidth(v26.duration, v14) + v16;
               v22 = Math.max(v22, v31);
               v23 += (v12 + v18) * v27;
               v21++;
            }
         }
      }

      if (v21 == 0 && !v20) {
         float v62 = 84.0F * S;
         dragSetting.size.set(v62, v11);
         this.smoothHeight = 0.0F;
         this.smoothWidth = 0.0F;
      } else {
         float v63 = v11 + (v23 > 0.01F ? v17 + v23 + v19 : 0.0F);
         if (this.smoothWidth <= 0.01F) {
            this.smoothWidth = v22;
         } else {
            this.smoothWidth = this.smoothWidth + (v22 - this.smoothWidth) * 0.22F;
         }

         if (this.smoothHeight <= 0.01F) {
            this.smoothHeight = v63;
         } else {
            this.smoothHeight = this.smoothHeight + (v63 - this.smoothHeight) * 0.22F;
         }

         float v64 = this.smoothWidth;
         float v65 = this.smoothHeight;
         float v66 = Math.min(8.0F * S, v65 / 2.0F);
         dragSetting.size.set(v64, v65);
         float v67 = (v20 ? alphaChat : alpha) * hudAlpha;
         if (!(v67 <= 0.01F)) {
            vr.glow(v8, v9, v64, v65, up.getColor(0, 0.08F * v67), v66, 5.0F * S, 0.5F);
            vr.blur(v8, v9, v64, v65, v67, v66, up.replAlpha(up.background(), v67));
            vr.outline(
               v8,
               v9,
               v64,
               v65,
               0.5F * S,
               up.getColor(255, 255, 255, (int)(25.0F * v67)),
               v66
            );
            float v68 = v9 + v11 / 2.0F;
            v10.draw(
               "Potions",
               v8 + v16,
               v68 - v13 / 2.0F + 0.8F * S,
               v13,
               up.getColor(235, 235, 240, (int)(255.0F * v67))
            );
            hu.icons5.draw("C", v8 + v64 - v16 - v15, v68 - v15 / 2.0F + 1.0F * S, v15, up.replAlpha(up.client(), v67));
            dt.enable(v8, v9, v64, v65, 2.0F);
            float v32 = v9 + v11 + v17;
            if (v20) {
               float v33 = v32 + v12 / 2.0F;
               float temp2 = v8 + v16 + 11.0F * S;
               float temp3 = v33 - v14 / 2.0F + 0.8F * S;

               v10.draw(
                  "Speed II",
                  temp2,
                  temp3,
                  v14,
                  up.getColor(170, 170, 175, (int)(255.0F * v67))
               );
               String v34 = "1:30";
               float v35 = v10.getWidth(v34, v14);
               v10.draw(v34, v8 + v64 - v16 - v35, v33 - v14 / 2.0F + 0.8F * S, v14, up.replAlpha(up.client(), v67));
            } else {
               ArrayList v69 = new ArrayList();

               for (Potions.EffectData v71 : sortedEffects) {
                  v71.animation.update();
                  v71.animation.run(v71.active ? 1.0 : 0.0, 0.12F, ap.QUAD_OUT);
                  float v36 = v71.animation.get();
                  if (v36 <= 0.005F && !v71.active) {
                     int v52 = v71.lvl;
                     String v53 = v71.name;
                     v69.add(v53 + ":" + v52);
                  } else {
                     float v37 = v36 * v67;
                     float v38 = v12 * v36;
                     float v39 = v32 + v38 / 2.0F;
                     float v40 = 8.5F * S;
                     this.drawEffectIcon(eventDisplay, v71, v8 + v16, v39 - v40 / 2.0F, v37, v40);
                     int v41 = v71.lvl;
                     String v73;
                     if (v41 > 1) {
                        String v54 = this.toRoman(v41);
                        v73 = " " + v54;
                     } else {
                        v73 = "";
                     }

                     String v42 = v73;
                     String v56 = v71.name;
                     String v43 = v56 + v42;
                     float v44 = v10.getWidth(v71.duration, v14);
                     float v45 = v8 + v16 + 11.0F * S;
                     float v46 = v64 - (v45 - v8) - v44 - v16 - 3.0F * S;
                     String v47 = this.displayName(v10, v43, v46, v14);
                     int v48 = v71.negative
                        ? up.getColor(245, 90, 90, (int)(255.0F * v37))
                        : up.getColor(170, 170, 175, (int)(255.0F * v37));

                     v10.draw(v47, v45, v39 - v14 / 2.0F + 0.8F * S, v14, v48);
                     v10.draw(v71.duration, v8 + v64 - v16 - v44, v39 - v14 / 2.0F + 0.8F * S, v14, up.replAlpha(up.client(), v37));
                     v32 += (v12 + v18) * v36;
                  }
               }

               v69.forEach(this.displayedEffects::remove);
            }

            dt.disable();
         }
      }
   }

   private String label(Potions.EffectData data) {
      int v2 = data.effectInstance.method_5578() + 1;
      String v10;
      if (v2 > 1) {

         String v4 = data.name;
         StringBuilder v8 = new StringBuilder();

         StringBuilder v9 = v8.append((Object)v4).append(" ");

         v10 = v9.append(v2).toString();
      } else {
         v10 = data.name;
      }

      return v10;
   }

   private void drawEffectIcon(EventReceivePacket eventDisplay, Potions.EffectData data, float x, float y, float alpha) {
      float var10006 = 10.0F * S;

      this.drawEffectIcon(eventDisplay, data, x, y, alpha, var10006);
   }

   public void onRender(vf dragSetting, hh interFace, EventReceivePacket eventDisplay) {
      Collection v4 = mc.field_1724.method_6026();
      this.displayedEffects.values().forEach(data -> {

         data.active = false;
         
      });
      S = hh.getInstance().sizeHud.getValue();

      for (StatusEffectInstance v6 : v4) {
         if (v6.method_5584() > 10) {
            String v7 = ((StatusEffect)v6.method_5579().comp_349()).method_5560().getString();
            int v8 = v6.method_5578() + 1;
            String v9 = this.getDurationString(v6);
            boolean v10 = ((StatusEffect)v6.method_5579().comp_349()).method_18792() == StatusEffectCategory.field_18272;
            Map result = this.displayedEffects;
            StringBuilder temp1 = new StringBuilder().append((Object)v7);

            Potions.EffectData v11 = result.computeIfAbsent(temp1.append(":").append(v8).toString(), k -> {
               Potions.EffectData var10000x = new Potions.EffectData;

               var10000x./* $VF: Unable to resugar constructor */<init>(v7, v9, v8, v10, v6.method_5584(), v6);
               return var10000x;
            });
            if (!v9.equals(v11.duration)) {
               v11.prevDuration = v11.duration;
               v11.duration = v9;
               v11.digitAnim.set(0.0);
               v11.digitAnim.run(1.0, 0.22, ap.QUAD_OUT);
            }

            v11.durationTicks = v6.method_5584();
            v11.negative = v10;
            v11.effectInstance = v6;
            v11.active = true;
         }
      }

      boolean v65 = this.displayedEffects.isEmpty();
      float v66 = dragSetting.position.x;
      float v67 = dragSetting.position.y;

      boolean v68 = v65 && !(mc.field_1755 instanceof ChatScreen);
      this.animIn.setDirection(v68 ? cp.BACKWARDS : cp.FORWARDS);
      this.animChat.setDirection(mc.field_1755 instanceof ChatScreen && v65 ? cp.FORWARDS : cp.BACKWARDS);
      dragSetting.active = !v68;
      float v69 = hh.getInstance().alphaHUD.getValue();
      float v70 = this.animIn.getOutput() * v69;
      if (!v68 || v70 != 0.0F) {
         float v84 = this.animChat.getOutput();

         float v71 = v84 * v69;
         yt v12 = hu.aeonik;
         float v13 = 20.0F * S;
         float v14 = 16.0F * S;
         float v15 = 11.7F * S + 0.5F * S;
         float v16 = 126.0F * S;
         float v17 = 7.0F * S;
         List v18 = this.displayedEffects.values().stream().sorted(Comparator.<Potions.EffectData>comparingInt(data -> {
            int var10000x = data.name.length();

            return var10000x + data.duration.length();
         }).reversed()).collect(Collectors.toList());
         if (interFace.isHud2()) {
            this.renderHud2(dragSetting, v18, v65, v70, v71, v69, eventDisplay);
         } else {
            if (v71 > 0.01F) {
               vr.glow(v66, v67, v16, v13, up.getColor(0, 0.08F * v71), v15, 6.0F * S, 0.5F);
               vr.blur(v66, v67, v16, v13, v71, v15, up.replAlpha(up.background(), v71));
               int v20 = up.getColor(255, 0.09F * v71);
               vr.rect(v66 + 1.3F * S, v67 + 1.3F * S, v16 - 2.6F * S, v13 - 2.6F * S, v20, 4.7F * S);
               float v21 = 10.0F * S;
               float v22 = v66 + v16 - v21 - v17;
               float v23 = v67 + (v13 - v21) / 2.0F;
               vr.glow(v22, v23, v21, v21, up.replAlpha(up.client(), 0.25F * v71), 2.5F * S, 8.0F * S, 0.6F);
               vr.rect(v22, v23, v21, v21, up.replAlpha(up.client(), 0.95F * v71), 2.5F * S);
               hu.icons5
                  .draw(
                     "C",
                     v22 + 1.25F * S,
                     v23 + 1.35F * S,
                     7.5F * S,
                     up.getColor(20, 20, 20, (int)(235.0F * v71))
                  );
               v12.draw("Effects", v66 + 7.5F * S, v67 + 5.5F * S, 9.1F * S, up.getColor(255, 0.98F * v71));
            }

            if (v65) {
               if (v70 > 0.01F) {
                  vr.glow(v66, v67, v16, v13, up.getColor(0, 0.08F * v70), v15, 6.0F * S, 0.5F);
                  vr.blur(v66, v67, v16, v13, v70, v15, up.replAlpha(up.background(), v70));
                  int v74 = up.getColor(255, 0.09F * v70);
                  vr.rect(v66 + 1.3F * S, v67 + 1.3F * S, v16 - 2.6F * S, v13 - 2.6F * S, v74, 4.7F * S);
                  float v76 = 10.0F * S;
                  float v78 = v66 + v16 - v76 - v17;
                  float v81 = v67 + (v13 - v76) / 2.0F;
                  vr.glow(v78, v81, v76, v76, up.replAlpha(up.client(), 0.25F * v70), 2.5F * S, 8.0F * S, 0.6F);
                  vr.rect(v78, v81, v76, v76, up.replAlpha(up.client(), 0.95F * v70), 2.5F * S);
                  hu.icons5
                     .draw(
                        "C",
                        v78 + 1.25F * S,
                        v81 + 1.35F * S,
                        7.5F * S,
                        up.getColor(20, 20, 20, (int)(235.0F * v70))
                     );
                  v12.draw("Effects", v66 + 7.5F * S, v67 + 5.5F * S, 9.1F * S, up.getColor(255, 0.98F * v70));
               }

               dragSetting.size.set(v16, v13);
            } else {
               float v19 = v16;
               float v72 = 22.0F * S;
               ArrayList v75 = new ArrayList();

               for (Potions.EffectData v24 : v18) {
                  v24.animation.update();
                  v24.animation.run(v24.active ? 1.0 : 0.0, 0.12F, ap.QUAD_OUT);
                  v24.digitAnim.update();
                  float v25 = v24.animation.get();
                  if (v25 <= 0.01F && !v24.active) {
                     int v56 = v24.lvl;
                     String v57 = v24.name;
                     v75.add(v57 + ":" + v56);
                  } else {
                     v72 += v14 * v25;
                     v77++;
                  }
               }

               v72 += 4.0F * S;
               vr.glow(v66, v67, v16, v72, up.getColor(0, 0.08F * v70), v15, 6.0F * S, 0.5F);
               vr.blur(v66, v67, v16, v72, v70, v15, up.replAlpha(up.background(), v70));
               int v80 = up.getColor(255, 0.09F * v70);
               vr.rect(v66 + 1.3F * S, v67 + 1.3F * S, v16 - 2.6F * S, v13 - 2.6F * S + 1.3F * S, v80, 13.7F * S, 13.0F * S, 2.5F * S, 2.5F * S);
               vr.rect(v66 + 1.3F * S, v67 + 22.5F * S, v16 - 2.6F * S, v72 - 24.3F * S + 0.5F * S, v80, 2.5F * S, 2.5F * S, 11.0F * S, 11.0F * S);
               float v82 = 10.0F * S;
               float v83 = v66 + v16 - v82 - v17;
               float v26 = v67 + (v13 - v82) / 2.0F;
               vr.glow(v83, v26, v82, v82, up.replAlpha(up.client(), 0.25F * v70), 2.5F * S, 8.0F * S, 0.6F);
               vr.rect(v83, v26, v82, v82, up.replAlpha(up.client(), 0.95F * v70), 2.5F * S);
               hu.icons5
                  .draw(
                     "C",
                     v83 + 1.0F * S,
                     v26 + 1.35F * S,
                     7.5F * S,
                     up.getColor(20, 20, 20, (int)(235.0F * v70))
                  );
               v12.draw("Effects", v66 + 7.5F * S, v67 + 5.5F * S, 9.1F * S, up.getColor(255, 0.98F * v70));
               float v27 = v67 + 22.5F * S;
               boolean v28 = true;

               for (Potions.EffectData v30 : v18) {
                  float v31 = v30.animation.get();
                  if (!(v31 <= 0.01F) || v30.active) {
                     int v32 = v30.effectInstance.method_5578() + 1;
                     boolean v33 = v30.negative;
                     int v34 = v33
                        ? up.getColor(235, 75, 75, v70 * v31)
                        : up.getColor(240, v70 * v31);
                     int v35 = v33
                        ? up.getColor(180, 60, 60, v70 * v31)
                        : up.getColor(155, v70 * v31);
                     String v86;
                     if (v32 > 1) {
                        String v59 = sp.getColor(v35);
                        v86 = " " + v59 + v32;
                     } else {
                        v86 = "";
                     }

                     String v60 = v86;
                     String v61 = v30.name;
                     String v36 = v61 + v60;
                     float v37 = v12.getWidth(v30.duration, 7.4F * S);
                     float v38 = Math.max(18.0F * S, v37 + 4.0F * S);
                     float v39 = 10.0F * S;
                     float v40 = v66 + v19 - v38 - v17;
                     float v41 = v27 + (v14 - v39) / 2.0F;
                     float v42 = v27 + (v14 - 8.6F * S) / 3.0F + 0.5F * S;
                     float v43 = v41 + (v39 - 8.4F * S) / 16.0F + 0.5F * S;
                     vr.glow(v40, v41, v38, v39, up.replAlpha(up.client(), 0.15F * v70 * v31), 1.5F * S, 5.0F * S, 0.4F);
                     vr.rect(v40, v41, v38, v39, up.replAlpha(up.client(), 0.95F * v70 * v31), 1.5F * S);
                     this.drawEffectIcon(eventDisplay, v30, v66 + 7.5F * S, v27 + (v14 - 10.0F * S) / 2.0F, v70 * v31);
                     float v44 = v66 + 21.5F * S;
                     float v45 = v40 - v44 - 4.0F * S;
                     if (v12.getWidth(this.label(v30), 8.6F * S) > v45) {
                        v12.drawFadingText(v36, v44, v42, v45, v34, 8.6F * S);
                     } else {
                        v12.draw(v36, v44, v42, 8.6F * S, v34);
                     }

                     float v46 = v30.digitAnim.get();
                     String v47 = v30.duration;
                     String v48 = v30.prevDuration == null ? v47 : v30.prevDuration;
                     float v49 = v40 + (v38 - v37) / 2.0F;
                     dt.enable(v40 - 0.5F * S, v41 - 0.5F * S, v38 + 1.0F * S, v39 + 1.0F * S);

                     for (int v50 = 0; v50 < v47.length(); v50++) {
                        String v51 = v47.substring(v50, v50 + 1);
                        int v52 = v48.length() - (v47.length() - v50);
                        String v53 = v52 >= 0 && v52 < v48.length() ? v48.substring(v52, v52 + 1) : null;
                        if (!(v46 >= 1.0F) && !v51.equals(v53) && v53 != null) {
                           v12.draw(
                              v51,
                              v49,
                              v43 + 7.0F * S * (1.0F - v46),
                              7.4F * S,
                              up.multAlpha(
                                 up.getColor(20, 20, 20, (int)(240.0F * v70 * v31)), v46
                              )
                           );
                           v12.draw(
                              v53,
                              v49,
                              v43 - 7.0F * S * v46,
                              7.4F * S,
                              up.multAlpha(
                                 up.getColor(20, 20, 20, (int)(240.0F * v70 * v31)),
                                 1.0F - v46
                              )
                           );
                        } else {
                           v12.draw(
                              v51,
                              v49,
                              v43,
                              7.4F * S,
                              up.getColor(20, 20, 20, (int)(240.0F * v70 * v31))
                           );
                        }

                        v49 += v12.getWidth(v51, 7.4F * S);
                     }

                     dt.disable();
                     if (!v28) {
                        vr.rect(
                           v66 + 4.0F * S,
                           v27 - 0.5F * S,
                           v19 - 8.0F * S,
                           0.5F * S,
                           up.getColor(255, 0.05F * v70 * v31),
                           1.0F * S
                        );
                     }

                     v28 = false;
                     v27 += v14 * v31;
                  }
               }

               v75.forEach(this.displayedEffects::remove);
               dragSetting.size.set(v19, v72);
            }
         }
      }
   }

   public Potions() {
      this.animChat = new ep(300, 1.0);
      this.displayedEffects = new LinkedHashMap<>();
      this.smoothWidth = 0.0F;
      this.smoothHeight = 0.0F;
   }

   private void drawEffectIcon(EventReceivePacket eventDisplay, Potions.EffectData data, float x, float y, float alpha, float size) {
      RegistryEntry result = data.effectInstance.method_5579();

      Identifier v7 = InGameHud.method_71644(result);
      Matrix3x2fStack v8 = eventDisplay.getDrawContext().method_51448();
      v8.pushMatrix();
      v8.translate(x, y);
      v8.scale(size / 12.0F, size / 12.0F);
      eventDisplay.getDrawContext()
         .method_52707(RenderPipelines.field_56883, v7, 0, 0, 12, 12, up.getColor(255, alpha));
      v8.popMatrix();
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
         String result = v8 + "...";

         return result;
      }
   }

   @Environment(EnvType.CLIENT)
   private static class EffectData {
      String prevDuration;
      int lvl;
      int durationTicks;
      String duration;
      Animation animation;
      boolean active;
      boolean negative;
      StatusEffectInstance effectInstance;
      final Animation digitAnim = new Animation();
      String name;

      public EffectData(String name, String duration, int lvl, boolean negative, int durationTicks, StatusEffectInstance effectInstance) {
         this.name = name;
         this.duration = duration;
         this.prevDuration = duration;
         this.lvl = lvl;
         this.negative = negative;
         this.durationTicks = durationTicks;
         this.animation = new Animation();
         this.active = true;
         this.effectInstance = effectInstance;
      }

      
   }
}
