package ru.white.module.impl.display.interfaceimpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Vf;
import ru.white.module.Module;
import ru.white.utils.misc.Sp;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.InterFace;
import ru.white.theme.wo;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.js;
import ru.white.utils.render.vr;
import ru.white.utils.render.vs;
import ru.white.utils.misc.Oq;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventReceivePacket;

@Environment(EnvType.CLIENT)
public class oh implements element {
   private static float f516 = 16.0F * oh.S;
   private static float f515 = 5.0F * oh.S;
   private final Set<EquipmentSlot> f512;
   private final Map<String, Boolean> f510;
   private final Animation f508 = new Animation();
   private static float f522 = 12.0F * oh.S;
   private static float f518 = 19.0F * oh.S;
   private static float f519 = 80.0F * oh.S;
   private boolean f511;
   private static float f520 = 14.0F * oh.S;
   private static float f513 = 5.5F * oh.S;
   private final Set<String> f509 = new HashSet<>();
   private static float f514 = 6.5F * oh.S;
   private static float f517 = 15.5F * oh.S;
   private static float S = 1.0F;
   private static float f521 = 24.0F * S;

   public oh() {
      this.f510 = new HashMap<>();
      this.f511 = false;
      this.f512 = new HashSet<>();
   }

   private void m497(vf drag, List<oq.pq> notifs, int screenWidth, float alpha, float scaleFix, EventReceivePacket eventDisplay) {
      float v7 = f519;
      float v8 = 0.0F;

      for (oq.pq v10 : notifs) {
         float v11 = v10.anim.get();
         if (!(v11 <= 0.01F)) {
            float v12 = hu.sf_regular.getWidth(v10.text, f513);
            v7 = Math.max(v7, f521 + v12);
            v8 += (f516 + 2.0F * S) * v11;
         }
      }

      if (v8 <= 0.0F) {
         v8 = f516;
      }

      drag.size.set(v7, v8);
      drag.targetPosition.x = (float)screenWidth / 2.0F - v7 / 2.0F;
      drag.position.x = (float)screenWidth / 2.0F - v7 / 2.0F;
      float v22 = drag.position.y;
      boolean v23 = mc.field_1755 instanceof ChatScreen && notifs.stream().noneMatch(e -> e.anim.get() > 0.01F);
      drag.active = v23 || notifs.stream().anyMatch(e -> {
         float v3;
         int var10000x = (v3 = e.anim.get() - 0.01F) == 0.0F ? 0 : (v3 < 0.0F ? -1 : 1);

         return var10000x > 0;
      });
      this.f508.update();
      this.f508.run(v23 ? 1.0 : 0.0, 0.2, ap.BACK_OUT, true);
      float v24 = MathHelper.method_15363(this.f508.get(), 0.0F, 1.0F);
      if (v24 > 0.01F) {
         float v25 = f519 * (0.6F + 0.4F * this.f508.get());
         float v13 = f520 * (0.6F + 0.4F * this.f508.get());
         float v14 = (float)screenWidth / 2.0F - v25 / 2.0F;
         float v15 = v22 + (f520 - v13) / 2.0F;
         vs.ys.rect(v14, v15, v25, v13, up.getColor(255, 0.05F * v24), f515);
         hu.sf_regular
            .draw(
               "Уведомления",
               (float)screenWidth / 2.0F - hu.sf_medium.getWidth("Уведомления", f513) / 2.0F,
               v22 + 4.5F * S,
               f513,
               up.getColor(175, 0.6F * v24)
            );
      }

      Iterator v26 = notifs.iterator();

      while (true) {
         boolean v30 = v26.hasNext();

         if (!v30) {
            return;
         }

         oq.pq v27 = (oq.pq)v26.next();
         float v28 = v27.anim.get();
         if (!(v28 <= 0.01F)) {
            float v29 = hu.sf_regular.getWidth(v27.text, f514);
            float v16 = f522 + v29;
            float v17 = (float)screenWidth / 2.0F - v16 / 2.0F;
            vs.ys.glow(v17, v22, v16, f517, up.getColor(0, 0.1F * v28), f515, 12.0F, 1.0F);
            vs.ws.glass(
               v17, v22, v16, f516, v28, f515, up.replAlpha(up.background(), hh.getInstance().alphaHUD.getValue() * v28), 20.0F, 1.0F, 1.0F, 4.0F
            );
            float v19 = v17 + v16 / 2.0F;
            Client.get().render2D().flushAll();
            hu.sf_regular.drawCentered(m499(v27, v28, v28), v19, v22 + 3.6F * S, f514, up.getColor(255, v28));
            v22 += f518 * v28;
         }
      }
   }

   public void onRender(vf drag, hh interFace, EventReceivePacket eventDisplay) {
      boolean v4 = interFace.isHud2();
      boolean v5 = interFace.element.getValue("Dynamic Island");
      if (v5 && !v4) {
         drag.active = false;
      } else {
         S = hh.getInstance().sizeHud.getValue();
         f513 = 5.5F * S;
         f514 = 6.5F * S;
         f515 = 5.0F * S;
         f516 = 16.0F * S;

         f517 = 15.5F * S;
         f518 = 19.0F * S;
         f519 = 80.0F * S;

         f520 = 14.0F * S;
         f521 = 24.0F * S;
         f522 = 12.0F * S;
         float v6 = 2.0F;
         float v7 = (float)mc.method_22683().method_4495();
         float v8 = v6 / v7;
         int v9 = (int)((float)mc.method_22683().method_4486() / v8);
         float v10 = m500();
         synchronized (oq.entries()) {
            List v12 = oq.entries();
            v12.removeIf(e -> {
               if (e.removing) {
                  float var10000x = e.anim.get();

                  if (var10000x <= 0.01F) {
                     return true;
                  }
               }

               return false;
            });
            Iterator v13 = v12.iterator();

            while (v13.hasNext()) {
               oq.pq v14 = (oq.pq)v13.next();
               v14.anim.update();
               if (v14.isExpired()) {
                  v14.removing = true;
               }

               v14.anim.run(v14.removing ? 0.0 : 1.0, 0.15F, ap.SINE_OUT);
            }

            if (v4) {
               this.m495(drag, v12, v9, v10, v8);
            } else {
               this.m497(drag, v12, v9, v10, v8, eventDisplay);
            }
         }
      }
   }

   // $VF: Unable to simplify switch on enum
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
   public void onTick(BooleanSetting notifModules, BooleanSetting notifArmor, BooleanSetting notifEffects) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         Collection v4 = Client.get().moduleManager().values();
         if (!this.f511) {
            for (Module v6 : v4) {
               this.f510.put(v6.getName(), v6.isEnabled());
            }

            this.f511 = true;
         } else if (notifModules.getValue()) {
            for (Module v23 : v4) {
               boolean v7 = this.f510.getOrDefault(v23.getName(), false);
               boolean v8 = v23.isEnabled();
               if (v7 != v8) {

                  switch (v23.getCategory()) {
                     case COMBAT:
                        String v36 = "e";
                        break;
                     case MOVEMENT:
                        String v35 = "i";
                        break;
                     case PLAYER:
                        String v34 = "n";
                        break;
                     case OTHER:
                        String v33 = "l";
                        break;
                     case RENDER:
                        String v32 = "u";
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  String v37 = v23.getBigName();
                  String v13 = v8
                     ? " активирована"
                     : " деактивирована";
                  String v14 = v37;
                  oq.send("Функция " + v14 + v13, oq.qq.MODULE, v23.getBigName());
               }

               this.f510.put(v23.getName(), v8);
            }
         } else {
            for (Module v24 : v4) {
               this.f510.put(v24.getName(), v24.isEnabled());
            }
         }

         if (notifArmor.getValue()) {
            EquipmentSlot[] v21 = new EquipmentSlot[]{EquipmentSlot.field_6169, EquipmentSlot.field_6174, EquipmentSlot.field_6172, EquipmentSlot.field_6166};

            for (EquipmentSlot v9 : v21) {
               ItemStack v10 = mc.field_1724.method_6118(v9);
               if (!v10.method_7960() && v10.method_7936() != 0) {
                  float v11 = 1.0F - (float)v10.method_7919() / (float)v10.method_7936();
                  if (v11 < 0.1F && !this.f512.contains(v9)) {
                     this.f512.add(v9);

                     String v12 = switch (v9) {
                        case field_6169 -> "Шлем";
                        case field_6174 -> "Нагрудник";
                        case field_6172 -> "Поножи";
                        case field_6166 -> "Ботинки";
                        default -> v9.method_5923();
                     };
                     oq.send(
                        v12 + " почти сломан!",
                        oq.qq.WARNING,
                        7589942591331181317L + -7589942591331176317L
                     );
                  } else if (v11 >= 0.1F) {
                     this.f512.remove(v9);
                  }
               } else {
                  this.f512.remove(v9);
               }
            }
         }

         if (notifEffects.getValue()) {
            for (StatusEffectInstance v26 : mc.field_1724.method_6026()) {
               String v28 = ((StatusEffect)v26.method_5579().comp_349()).method_5560().getString();
               int v30 = v26.method_5584();
               if (v30 > 0) {
                  if (v30 > 200) {
                     this.f509.remove(v28);
                  } else if (!this.f509.contains(v28)) {
                     this.f509.add(v28);
                     Identifier v31 = m501(v26.method_5579());
                     oq.send(
                        v28 + " заканчивается!",
                        oq.qq.EFFECT,
                        v31,
                        -9175551635772714703L + 9175551635772718703L
                     );
                  }
               }
            }
         }
      }
   }

   private void m498(EventReceivePacket eventDisplay, oq.pq e, float nx, float actualY, float a, float scaleFix) {
      if (e.itemStack != null) {
         Matrix3x2fStack v7 = eventDisplay.getDrawContext().method_51448();
         v7.pushMatrix();
         v7.translate((nx + 8.5F * S) * scaleFix, (actualY + 7.0F * S) * scaleFix);
         v7.scale(0.6F * S, 0.6F * S);
         js.drawItemWithContext(eventDisplay.getDrawContext(), e.itemStack, -8.0F, -8.0F, a, a);
         v7.popMatrix();
      } else if (e.effectTexture != null) {
         Matrix3x2fStack v10 = eventDisplay.getDrawContext().method_51448();
         v10.pushMatrix();
         v10.translate((nx + 5.0F * S) * scaleFix, (actualY + 2.25F * S) * scaleFix);
         DrawContext result = eventDisplay.getDrawContext();
         int temp5 = (int)(8.0F * S * scaleFix);
         float var10006 = 8.0F * S;

         result.method_52707(
            RenderPipelines.field_56883, e.effectTexture, 0, 0, temp5, (int)(var10006 * scaleFix), up.getColor(255, a)
         );
         v10.popMatrix();
      } else {
         float v11 = 6.0F * S;
         if (e.text.contains("почти")) {
            hu.icon.draw("r", nx + 6.0F * S, actualY + 3.8F * S, v11, up.getColorRectMain(a));
         }

         if (e.text.contains("активирована")
            && !e.text.contains("деактивирована")) {
            hu.icon.draw("2", nx + 6.0F * S, actualY + 3.6F * S, v11, up.getColorRectMain(a));
         } else if (e.text.contains("деактивирована")) {
            float temp2 = nx + 6.0F * S;
            float temp3 = actualY + 3.6F * S;

            hu.icon.draw("1", temp2, temp3, v11, up.getColorRectMain(a));
         }
      }
   }

   private static Identifier m501(RegistryEntry<StatusEffect> effect) {
      Optional result = effect.method_40230().<Identifier>map(RegistryKey::method_29177).map(id -> {

         return id.method_45138("mob_effect/");
      });

      return result.orElse(Identifier.method_60656(StrDec.d("1G8MSGkThefwnlt+ljrs3A==", v4)));
   }

   private static String m499(oq.pq e, float a, float alpha) {
      if (e.iconGlyph != null) {
         boolean result = e.iconGlyph.isEmpty();

         if (!result && e.text.contains(e.iconGlyph)) {
            String v7 = e.text;
            String v8 = e.iconGlyph;
            String temp2 = sp.getColor(wo.getHudColor(a * alpha));
            String temp3 = e.iconGlyph;
            String v3 = sp.reset();
            String v4 = temp3;
            String v5 = temp2;
            return v7.replace(v8, v5 + v4 + v3);
         }
      }

      return e.text;
   }

   private String[] m496(oq.pq e) {
      String v2 = "Информация";
      String v3 = e.text;
      if (e.type == oq.qq.WARNING) {
         v2 = "Предупреждение";
      }

      if ((e.type != oq.qq.MODULE || e.itemStack != null)
         && !e.text.contains("активирована")
         && !e.text.contains("деактивирована")) {
         if (e.text.contains(" — ")) {
            String[] v9 = e.text.split(" — ", 2);
            v2 = v9[0];
            v3 = v9[1];
         }
      } else {
         String v4 = e.iconGlyph != null && !e.iconGlyph.isEmpty() ? e.iconGlyph : "";
         if (v4.isEmpty() && e.text.startsWith("Функция ")) {
            int v5 = e.text.indexOf(" активирована");
            if (v5 < 0) {
               v5 = e.text.indexOf(" деактивирована");
            }

            if (v5 > 8) {
               v4 = e.text.substring(8, v5).trim();
            }
         }

         boolean result = v4.isEmpty();

         if (result) {
            v4 = "Модуль";
         }

         boolean v10 = !e.text.contains("деактивирована")
            && (
               e.text.contains("активирована")
                  || e.text.contains("включен")
                  || e.text.contains("включено")
            );
         String v6 = v10 ? " включено" : " деактивировано";
         v3 = v4 + v6;
      }

      return new String[]{v2, v3};
   }

   @Override
   public void onRender(vf drag, hh interFace) {
   }

   private void m495(vf drag, List<oq.pq> notifs, int screenWidth, float alpha, float scaleFix) {
      float v6 = 22.0F * S;
      float v7 = 26.0F * S;
      float v8 = 8.0F * S;
      float v9 = 86.0F * S;
      float v10 = 0.0F;

      for (oq.pq v12 : notifs) {
         float v13 = v12.anim.get();
         if (!(v13 <= 0.01F)) {
            String[] v14 = this.m496(v12);
            String v15 = v14[0];
            String v16 = v14[1];
            float v17 = 5.5F * S;
            float v18 = 8.0F * S + v17 * 2.0F + 5.0F * S;
            float v19 = Math.max(hu.sf_bold.getWidth(v15, 6.8F * S), hu.sf_medium.getWidth(v16, 5.8F * S));
            float v20 = Math.max(86.0F * S, v18 + v19 + 9.0F * S);
            v9 = Math.max(v9, v20);
            v10 += v7 * v13;
         }
      }

      if (v10 <= 0.0F) {
         v10 = v6;
      }

      if (drag.position.x == 0.0F && (drag.position.y == 0.0F || drag.position.y == 200.0F)) {
         drag.position.set(((float)screenWidth - v9) / 2.0F, 25.0F * S);
         drag.targetPosition.set(drag.position);
      }

      drag.size.set(v9, v10);
      float v32 = drag.position.x;
      float v33 = drag.position.y;
      boolean v34 = mc.field_1755 instanceof ChatScreen && notifs.stream().noneMatch(e -> {
         float result = e.anim.get();

         return result > 0.01F;
      });
      drag.active = v34 || notifs.stream().anyMatch(e -> {
         boolean v4;
         if (e.anim.get() > 0.01F) {

            v4 = true;
         } else {
            v4 = false;
         }

         return v4;
      });
      this.f508.update();
      this.f508.run(v34 ? 1.0 : 0.0, 0.2, ap.BACK_OUT, true);
      float v35 = MathHelper.method_15363(this.f508.get(), 0.0F, 1.0F);
      if (v35 > 0.01F) {
         float v36 = 108.0F * S;
         vr.glow(v32, v33, v36, v6, up.getColor(0, 0.1F * v35), v8, 5.0F * S, 0.5F);
         vr.glow(v32, v33, v36, v6, up.replAlpha(up.client(), 0.12F * v35), v8, 4.0F * S, 0.4F);
         vr.blur(v32, v33, v36, v6, v35, v8, up.replAlpha(up.background(), v35));
         vr.outline(
            v32,
            v33,
            v36,
            v6,
            0.6F * S,
            up.getColor(255, 255, 255, (int)(25.0F * v35)),
            v8
         );
         float v41 = 5.5F * S;
         float v43 = v32 + 8.0F * S + v41;
         float v21 = v33 + v6 / 2.0F;
         vr.rect(
            v43 - v41,
            v21 - v41,
            v41 * 2.0F,
            v41 * 2.0F,
            up.getColor(255, 255, 255, (int)(255.0F * v35)),
            v41
         );
         hu.sf_bold
            .drawCentered(
               "i",
               v43,
               v21 - 3.2F * S,
               6.8F * S,
               up.getColor(15, 15, 18, (int)(255.0F * v35))
            );
         float v22 = v43 + v41 + 5.0F * S;
         hu.sf_bold
            .draw(
               "Информация",
               v22,
               v33 + 4.2F * S,
               6.8F * S,
               up.getColor(255, 255, 255, (int)(255.0F * v35))
            );
         hu.sf_medium
            .draw(
               "Attack Aura включено",
               v22,
               v33 + 12.0F * S,
               5.8F * S,
               up.getColor(180, 180, 190, (int)(235.0F * v35))
            );
      }

      for (oq.pq v38 : notifs) {
         float v39 = v38.anim.get();
         if (!(v39 <= 0.01F)) {
            String[] v40 = this.m496(v38);
            String v42 = v40[0];
            String v44 = v40[1];
            float v45 = 5.5F * S;
            float v46 = 8.0F * S + v45 * 2.0F + 5.0F * S;
            float v23 = Math.max(hu.sf_bold.getWidth(v42, 6.8F * S), hu.sf_medium.getWidth(v44, 5.8F * S));
            float v24 = Math.max(86.0F * S, v46 + v23 + 9.0F * S);
            vr.glow(v32, v33, v24, v6, up.getColor(0, 0.1F * v39), v8, 5.0F * S, 0.5F);
            vr.glow(v32, v33, v24, v6, up.replAlpha(up.client(), 0.12F * v39), v8, 4.0F * S, 0.4F);
            int var10006 = up.background();

            vr.blur(v32, v33, v24, v6, v39, v8, up.replAlpha(var10006, v39));
            vr.outline(
               v32,
               v33,
               v24,
               v6,
               0.6F * S,
               up.getColor(255, 255, 255, (int)(25.0F * v39)),
               v8
            );
            float v27 = v32 + 8.0F * S + v45;
            float v28 = v33 + v6 / 2.0F;
            vr.rect(
               v27 - v45,
               v28 - v45,
               v45 * 2.0F,
               v45 * 2.0F,
               up.getColor(255, 255, 255, (int)(255.0F * v39)),
               v45
            );
            hu.sf_bold
               .drawCentered(
                  "i",
                  v27,
                  v28 - 3.2F * S,
                  6.8F * S,
                  up.getColor(15, 15, 18, (int)(255.0F * v39))
               );
            float v29 = v27 + v45 + 5.0F * S;
            int v30 = v38.type == oq.qq.WARNING
               ? up.getColor(255, 120, 120, (int)(255.0F * v39))
               : up.getColor(255, 255, 255, (int)(255.0F * v39));
            hu.sf_bold.draw(v42, v29, v33 + 4.2F * S, 6.8F * S, v30);
            hu.sf_medium
               .draw(
                  v44,
                  v29,
                  v33 + 12.0F * S,
                  5.8F * S,
                  up.getColor(180, 180, 190, (int)(235.0F * v39))
               );
            v33 += v7 * v39;
         }
      }
   }

   private static float m500() {
      float result = wo.getOpacity();

      return result < 0.98F ? wo.getOpacity() : 0.98F;
   }

   
}
