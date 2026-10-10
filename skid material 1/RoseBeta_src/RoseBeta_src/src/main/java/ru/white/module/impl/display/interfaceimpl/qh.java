package ru.white.module.impl.display.interfaceimpl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import ru.white.utils.helper.Wu;
import ru.white.utils.misc.Vf;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.combat.AttackAura;
import ru.white.module.impl.display.gh;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.animation.satoshi.cp;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.js;
import ru.white.utils.render.vr;
import ru.white.module.impl.misc.NameProtect;
import ru.white.utils.ItemUtil;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventReceivePacket;

@Environment(EnvType.CLIENT)
public class qh implements element {
   private static final Identifier TARGET_AVATAR = Identifier.method_60655("client", "textures/target_avatar.png");
   private final Animation f525;
   private static float S = 1.0F;
   private final wu f526;
   private LivingEntity f527;
   private final ru.white.utils.animation.satoshi.Animation f523 = new ep(200, 1.0);
   private final Animation f524 = new Animation();

   public qh() {
      this.f525 = new Animation();
      this.f526 = new wu();
   }

   public void onRender(vf drag, hh interFace, EventReceivePacket eventDisplay) {
      S = hh.getInstance().sizeHud.getValue();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         LivingEntity v4 = fg.target;
         if (Client.get().moduleManager().get(fg.class).isEnabled() && v4 != null) {
            this.f527 = v4;
            this.f526.reset();
         }

         if (v4 == null && mc.field_1692 != null) {
            LivingEntity v6 = mc.field_1692.method_72393();
            if (v6 instanceof LivingEntity) {
               this.f527 = v6;
               this.f526.reset();
            }
         }

         if (mc.field_1755 instanceof ChatScreen) {
            this.f527 = mc.field_1724;
            this.f526.reset();
         }

         boolean v5 = this.f527 != null && mc.field_1687 != null && !this.f527.method_31481();
         boolean v45 = !v5 || this.f526.finished(400.0);
         this.f523.setDirection(v45 ? cp.BACKWARDS : cp.FORWARDS);
         if (!((double)this.f523.getOutput() <= 0.0) && this.f527 != null) {
            drag.active = true;
            float v7 = 102.0F * S;
            float v8 = 38.3F * S;
            drag.size.set(v7, v8);
            float v9 = drag.position.x;
            float v10 = drag.position.y;
            float v11 = this.f523.getOutput();
            float v12 = 26.0F * S;
            float v13 = 5.0F * S;
            float v14 = 13.2F * S;
            float v15 = hh.getInstance().alphaHUD.getValue();
            float v16 = v11 * v15;
            if (interFace.isTargetHudTest()) {
               this.m511(drag, this.f527, v16, eventDisplay);
            } else if (interFace.isHud2()) {
               this.m510(drag, this.f527, v16, eventDisplay);
            } else {
               vr.glow(v9, v10, v7, v8, up.getColor(0, 0.12F * v16), v14, 8.0F * S, 0.5F);
               vr.blur(v9, v10, v7, v8, v16, v14, up.replAlpha(up.background(), v16));
               int v17 = up.getColor(255, 0.09F * v16);
               vr.rect(v9 + 1.3F * S, v10 + 1.3F * S, v7 - 66.0F * S, v8 - 2.6F * S, v17, 11.0F * S, 2.5F * S, 2.5F * S, 11.0F * S);
               vr.rect(
                  v9 + 1.3F * S + v7 - 65.3F * S,
                  v10 + 1.3F * S,
                  v7 - 1.3F * S - 38.0F * S,
                  v8 - 21.6F * S,
                  v17,
                  2.5F * S,
                  15.0F * S,
                  2.5F * S,
                  2.5F * S
               );
               vr.rect(
                  v9 + 1.3F * S + v7 - 65.3F * S,
                  v10 + 1.1F * S + v8 - 20.6F * S,
                  v7 - 1.3F * S - 38.0F * S,
                  v8 - 20.1F * S,
                  v17,
                  2.5F * S,
                  2.5F * S,
                  14.0F * S,
                  2.5F * S
               );
               float v18 = 22.5F * S;
               float v19 = v9 + 5.3F * S;
               float v20 = v10 + 4.5F * S;
               this.m509(this.f527, eventDisplay.getPartialTicks(), v19, v20, 28.5F * S, 3.7F * S, v16);
               this.f524.update();
               this.f525.update();
               float v21 = this.m508(this.f527);
               float v22 = this.f527.method_6063();
               float v23 = v9 + 7.05F * S - 1.5F * S + v12 + v13 + v13;
               float v24 = v10 + 24.0F * S + 5.1F * S - 0.5F * S;
               float v25 = v7 - 47.0F * S + 0.3F * S;
               float v26 = 0.5F * S + 4.8F * S;
               float v27 = 2.05F * S;
               this.f524.run((double)Math.round(v21 / Math.max(1.0F, v22) * v25), 0.5, ap.BACK_OUT);
               this.f525.run((double)v21, 0.15F, ap.LINEAR);
               float v28 = this.f525.get();
               String v29 = iq.isCopyTime() ? String.format("%.1f", v28) : (this.f527.method_5767() ? "null" : String.format("%.1f", v28));
               String v30 = this.f527
                  .method_5477()
                  .getString()
                  .replace(
                     mc.field_1724.method_5477().getString(),
                     Client.get().moduleManager().get(lm.class).isEnabled() ? "rose.fun" : mc.field_1724.method_5477().getString()
                  );
               vr.rect(v23, v24, v25, v26, up.getColor(255, 0.06F * v16), v27);
               int v31 = up.client();
               int v32 = up.multDark(v31, 0.5F);
               v31 = up.replAlpha(v31, v16);
               v32 = up.replAlpha(v32, v16);
               float v33 = Math.min(v25, this.f524.get());
               vr.gradientRect(v23, v24, v33, v26, new int[]{v32, v31, v31, v32}, v27);
               float v34 = v9 + 10.5F * S + v12 + v13 + v13 + 2.5F * S;
               float v35 = v10 + 5.6F * S;
               hu.mainmenuicons.draw("e", v34 - 7.5F * S, v35 + 1.2F * S, 7.0F * S, up.replAlpha(up.client(), 0.92F * v16));
               float v36 = v9 + v7 - 4.0F * S - v34;
               if (v36 < 10.0F * S) {
                  v36 = 10.0F * S;
               }

               if (hu.aeonik.getWidth(v30, 8.8F * S) > v36) {
                  hu.aeonik.drawFadingText(v30, v34, v35, v36, up.getColor(255, 0.98F * v16), 8.8F * S);
               } else {
                  hu.aeonik.draw(v30, v34, v35, 8.8F * S, up.getColor(255, 0.98F * v16));
               }

               float v37 = v9 - 2.5F * S + v12 + v13 + v13 + 7.5F * S;
               float v38 = v10 - 8.4F * S + v13 + v13 + v13;
               float v39 = v9 - 2.5F * S + v12 + v13 + v13 + 7.5F * S;
               float v40 = v10 + 6.7F * S + v13 + v13 + v13;
               hu.mainmenuicons.draw("e", v37, v38, 8.5F * S, up.replAlpha(up.client(), 0.92F * v16));
               hu.mainmenuicons.draw("b", v39, v40, 6.5F * S, up.replAlpha(up.client(), 0.92F * v16));
               float v41 = v9 + 9.3F * S + v12 + v13 + v13 + 3.0F * S;
               float v42 = v10 + 5.9F * S + v13 + v13 + v13;
               float temp4 = 7.0F * S;
               int temp5 = 255;
               float var10006 = 0.98F * v16;

               hu.aeonik.draw(v29, v41, v42, temp4, up.getColor(temp5, var10006));
            }
         } else {
            drag.active = false;
         }
      }
   }

   private void m510(vf drag, LivingEntity target, float effAlpha, EventReceivePacket eventDisplay) {
      float v5 = drag.position.x;
      float v6 = drag.position.y;
      float v7 = 98.0F * S;
      float v8 = 30.0F * S;
      float v9 = 8.0F * S;
      drag.size.set(v7, v8);
      if (!(effAlpha <= 0.01F)) {
         vr.glow(v5, v6, v7, v8, up.getColor(0, 0.08F * effAlpha), v9, 5.0F * S, 0.5F);
         vr.blur(v5, v6, v7, v8, effAlpha, v9, up.replAlpha(up.background(), effAlpha));
         vr.outline(
            v5,
            v6,
            v7,
            v8,
            0.5F * S,
            up.getColor(255, 255, 255, (int)(25.0F * effAlpha)),
            v9
         );
         float v10 = 20.0F * S;
         float v11 = 5.0F * S;
         float v12 = v5 + 5.0F * S;
         float v13 = v6 + (v8 - v10) / 2.0F;
         this.m509(target, eventDisplay.getPartialTicks(), v12, v13, v10, v11, effAlpha);
         this.f524.update();
         this.f525.update();
         float v14 = this.m508(target);
         float v15 = target.method_6063();
         this.f524.run((double)v14, 0.2F, ap.QUAD_OUT);
         this.f525.run((double)v14, 0.15F, ap.LINEAR);
         float v16 = this.f525.get();
         String v17 = iq.isCopyTime() ? String.format("%.1f HP", v16) : (target.method_5767() ? "null" : String.format("%.1f HP", v16));
         String v18 = target.method_5477()
            .getString()
            .replace(
               mc.field_1724.method_5477().getString(),
               Client.get().moduleManager().get(lm.class).isEnabled() ? "rose.fun" : mc.field_1724.method_5477().getString()
            );
         float v19 = v12 + v10 + 6.0F * S;
         float v20 = 6.0F * S;
         float v21 = v5 + v7 - v20 - v19;
         float v22 = v6 + 6.0F * S;
         float v23 = hu.sf_medium.getWidth(v17, 7.0F * S);
         hu.sf_medium.draw(v17, v5 + v7 - v20 - v23, v22, 7.0F * S, up.replAlpha(up.client(), effAlpha));
         float v24 = v21 - v23 - 3.5F * S;
         if (hu.sf_bold.getWidth(v18, 7.5F * S) > v24) {
            hu.sf_bold
               .drawFadingText(
                  v18,
                  v19,
                  v22,
                  v24,
                  up.getColor(235, 235, 240, (int)(255.0F * effAlpha)),
                  7.5F * S
               );
         } else {
            hu.sf_bold
               .draw(
                  v18,
                  v19,
                  v22,
                  7.5F * S,
                  up.getColor(235, 235, 240, (int)(255.0F * effAlpha))
               );
         }

         float v25 = v6 + 17.5F * S;
         float v26 = 3.5F * S;
         float v27 = 1.75F * S;

         vr.rect(
            v19,
            v25,
            v21,
            v26,
            up.getColor(25, 25, 30, (int)(255.0F * effAlpha)),
            v27
         );
         float v28 = MathHelper.method_15363(this.f524.get() / Math.max(1.0F, v15), 0.0F, 1.0F);
         float v29 = v28 * v21;
         if (v29 > 0.5F * S) {
            int v30 = up.client();
            vr.rect(v19, v25, v29, v26, up.replAlpha(v30, effAlpha), v27);
            vr.glow(v19, v25, v29, v26, up.replAlpha(v30, 0.25F * effAlpha), v27, 3.0F * S, 0.4F);
         }
      }
   }

   private float m508(LivingEntity entity) {
      float result = entity.method_6032();
      float temp1 = entity.method_6067();

      float v2 = result + temp1;
      if (entity instanceof PlayerEntity v3 && mc.field_1687 != null) {
         ScoreboardObjective v4 = mc.field_1687.method_8428().method_1189(ScoreboardDisplaySlot.field_45158);
         if (v4 != null) {
            MutableText v5 = ReadableScoreboardScore.method_55398(
               mc.field_1687.method_8428().method_55430(v3, v4), v4.method_55380(StyledNumberFormat.field_47566)
            );

            try {
               v2 = Float.parseFloat(up.removeFormatting(v5.getString()));
            } catch (Exception v10) {
            }
         }
      }

      return MathHelper.method_15363(v2, 0.0F, entity.method_6063() + entity.method_6067());
   }

   private void m511(vf drag, LivingEntity target, float effAlpha, EventReceivePacket eventDisplay) {
      float v5 = drag.position.x;
      float v6 = drag.position.y;
      float v7 = 114.0F * S;
      float v8 = 36.0F * S;
      drag.size.set(v7, v8);
      if (!(effAlpha <= 0.005F)) {
         gh.cardWithGlow(v5, v6, v7, v8, up.client(), S, effAlpha);
         float v9 = 25.0F * S;
         float v10 = v5 + 5.0F * S;
         float v11 = v6 + (v8 - v9) / 2.0F;
         boolean result;
         if (mc.field_1755 instanceof ChatScreen && target == mc.field_1724) {
            result = true;
            
         } else {
            result = false;
         }

         boolean v12 = result;
         vr.texture(TARGET_AVATAR, v10, v11, v9, v9, 0.0F, 0.0F, 1.0F, 1.0F, up.getColor(255, effAlpha), 1.0F, 4.0F * S);
         vr.outline(v10, v11, v9, v9, 0.6F * S, up.replAlpha(up.client(), 0.5F * effAlpha), 4.0F * S);
         this.f524.update();
         this.f525.update();
         float v13 = v12 ? 20.0F : this.m508(target);
         float v14 = v12 ? 20.0F : target.method_6063();
         this.f524.run((double)v13, 0.2F, ap.QUAD_OUT);
         this.f525.run((double)v13, 0.15F, ap.LINEAR);
         float v15 = v12 ? 20.0F : this.f525.get();
         String v16 = String.format("%.0f ❤", v15);
         String v17 = v12
            ? "danyapro2011"
            : target.method_5477()
               .getString()
               .replace(
                  mc.field_1724.method_5477().getString(),
                  Client.get().moduleManager().get(lm.class).isEnabled() ? "rose.fun" : mc.field_1724.method_5477().getString()
               );
         float v47 = v10 + v9;

         float v18 = v47 + 5.5F * S;
         float v19 = v5 + v7 - 5.0F * S - v18;
         float v20 = v6 + 4.5F * S;
         float v21 = 7.2F * S;
         float v22 = 6.8F * S;
         float v23 = hu.sf_bold.getWidth(v16, v22);
         hu.sf_bold
            .draw(
               v16,
               v5 + v7 - 5.0F * S - v23,
               v20,
               v22,
               up.getColor(255, 75, 75, (int)(245.0F * effAlpha))
            );
         float v24 = v19 - v23 - 3.5F * S;
         if (hu.sf_bold.getWidth(v17, v21) > v24) {
            hu.sf_bold
               .drawFadingText(
                  v17,
                  v18,
                  v20,
                  v24,
                  up.getColor(245, 245, 250, (int)(245.0F * effAlpha)),
                  v21
               );
         } else {
            hu.sf_bold
               .draw(
                  v17, v18, v20, v21, up.getColor(245, 245, 250, (int)(245.0F * effAlpha))
               );
         }

         float v25 = 10.5F * S;
         float v26 = 3.2F * S;

         float v27 = v6 + 13.5F * S;
         EquipmentSlot[] v28 = new EquipmentSlot[]{EquipmentSlot.field_6169, EquipmentSlot.field_6174, EquipmentSlot.field_6172, EquipmentSlot.field_6166};

         for (int v29 = 0; v29 < v28.length; v29++) {
            float v30 = v18 + (float)v29 * (v25 + v26);
            ItemStack v31 = target.method_6118(v28[v29]);
            if (v31.method_7960() && v12) {
               v31 = switch (v29) {
                  case 0 -> new ItemStack(Items.field_22027);
                  case 1 -> new ItemStack(Items.field_22028);
                  case 2 -> new ItemStack(Items.field_22029);
                  default -> new ItemStack(Items.field_22030);
               };

               v31.method_7974(switch (v29) {
                  case 0 -> 100;
                  case 1 -> 80;
                  case 2 -> 150;
                  default -> 60;
               });
            }

            vr.rect(
               v30,
               v27,
               v25,
               v25,
               up.getColor(255, 255, 255, (int)(9.0F * effAlpha)),
               gh.chipRadius(S)
            );
            if (!v31.method_7960()) {
               js.drawItemWithContext(eventDisplay.getDrawContext(), v31, v30 + 0.5F * S, v27 + 0.5F * S, 0.6F * S, effAlpha);
               if (v31.method_7963()) {
                  float v32 = (float)v31.method_7936();
                  float v33 = (float)v31.method_7919();
                  float v34 = MathHelper.method_15363(1.0F - v33 / Math.max(1.0F, v32), 0.0F, 1.0F);
                  float v35 = v27 + v25 + 1.2F * S;
                  float v36 = 1.2F * S;
                  vr.rect(
                     v30,
                     v35,
                     v25,
                     v36,
                     up.getColor(24, 18, 32, (int)(200.0F * effAlpha)),
                     0.6F * S
                  );
                  int v37 = up.getColor((int)(255.0F * (1.0F - v34)), (int)(255.0F * v34), 35, (int)(240.0F * effAlpha));
                  vr.rect(v30, v35, v25 * v34, v36, v37, 0.6F * S);
               }
            } else {
               vr.outline(
                  v30,
                  v27,
                  v25,
                  v25,
                  0.5F * S,
                  up.getColor(255, 255, 255, (int)(13.0F * effAlpha)),
                  gh.chipRadius(S)
               );
            }
         }

         float v41 = 2.8F * S;
         float v42 = v41 / 2.0F;
         float v43 = v6 + v8 - 3.8F * S - v41;
         vr.rect(
            v18,
            v43,
            v19,
            v41,
            up.getColor(255, 255, 255, (int)(14.0F * effAlpha)),
            v42
         );
         float v44 = MathHelper.method_15363(v12 ? 1.0F : this.f524.get() / Math.max(1.0F, v14), 0.0F, 1.0F);
         float v45 = v44 * v19;
         if (v45 > 0.5F * S) {
            int v46 = up.replAlpha(gh.accent(), effAlpha);
            vr.rect(v18, v43, v45, v41, v46, v42);
            vr.glow(v18, v43, v45, v41, up.replAlpha(gh.accent(), 0.55F * effAlpha), v42, 4.5F * S, 0.55F);
         }
      }
   }

   @Override
   public void onRender(vf drag, hh interFace) {
   }

   private void m509(LivingEntity lastTarget, float lastTickDelta, float x, float y, float size, float radius, float alpha) {
      try {
         if (!(mc.method_1561().method_3953(lastTarget) instanceof LivingEntityRenderer v9)) {
            return;
         }

         LivingEntityRenderState v10 = (LivingEntityRenderState)v9.method_62425(lastTarget, lastTickDelta);
         Identifier v11 = v9.method_3885(v10);
         float v12 = lastTarget.field_6235 > 0 ? (float)lastTarget.field_6235 / 10.0F : 0.0F;
         int result = (int)(255.0F * (1.0F - v12 * 0.8F));

         int v14 = result;

         int v15 = (int)(255.0F * (1.0F - v12 * 0.8F));
         int v16 = up.replAlpha(up.getColor(v13, v14, v15), alpha);

         vr.texture(v11, x, y, size, size, 0.125F, 0.125F, 0.25F, 0.25F, v16, 0.0F, radius);
      } catch (Exception v20) {
      }
   }

   
}
