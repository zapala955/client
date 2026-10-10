package ru.white.module.impl.display.interfaceimpl;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.MathHelper;
import ru.white.utils.helper.Wu;
import ru.white.utils.misc.Vf;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.module.impl.render.InterFace;
import ru.white.theme.wo;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.dt;
import ru.white.utils.render.ps;
import ru.white.utils.render.vr;
import ru.white.utils.render.vs;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;
import ru.white.event.impl.EventJump;

@Environment(EnvType.CLIENT)
public class mh implements element {
   private final Animation f489;
   private float f502;
   private float f503;
   private ExecutorService f487;
   private float f504;
   private static float S = 1.0F;
   private static final MediaInfo EMPTY = new MediaInfo("Нет трека", "—", new byte[0], 0L, 0L, false);
   private float f492;
   private float f495;
   private float f499;
   private float f501;
   private volatile IMediaSession f491;
   private float f494;
   private float f500;
   private float f496;
   private volatile MediaInfo f490;
   private float f498;
   private float f493;
   private final wu f488 = new wu();
   private float f497;

   private void m486(vf dragSetting, hh interFace, float alpha, MediaInfo info) {
      float v5 = dragSetting.position.x;
      float v6 = dragSetting.position.y;
      float v7 = interFace.alphaHUD.getValue() * alpha;
      float v8 = 104.0F * S;
      float v9 = 44.0F * S;
      float v10 = 8.0F * S;
      float v11 = 6.5F * S;
      dragSetting.size.set(v8, v9);
      if (!(v7 <= 0.01F)) {
         vr.glow(v5, v6, v8, v9, up.getColor(0, 0.08F * v7), v10, 5.0F * S, 0.5F);
         vr.blur(v5, v6, v8, v9, v7, v10, up.replAlpha(up.background(), v7));

         vr.outline(
            v5, v6, v8, v9, 0.5F * S, up.getColor(255, 255, 255, (int)(25.0F * v7)), v10
         );
         float v12 = v6 + 7.5F * S;
         hu.sf_bold
            .draw(
               "Music",
               v5 + v11,
               v12 - 3.8F * S,
               8.0F * S,
               up.getColor(235, 235, 240, (int)(255.0F * v7))
            );
         hu.icon.draw("M", v5 + v8 - v11 - 7.5F * S, v12 - 3.8F * S, 7.5F * S, up.replAlpha(up.client(), v7));
         float v13 = 15.0F * S;
         float v14 = v5 + v11;
         float v15 = v6 + 14.0F * S;
         float v16 = 3.0F * S;
         if (ps.hasArtwork()) {
            vr.texture(
               ps.ARTWORK_ID, v14, v15, v13, v13, 0.0F, 0.0F, 1.0F, 1.0F, up.getColor(255, (int)(255.0F * v7)), 0.5F, v16
            );
            vr.outline(
               v14,
               v15,
               v13,
               v13,
               0.5F * S,
               up.getColor(255, 255, 255, (int)(25.0F * v7)),
               v16
            );
         } else {
            vr.rect(
               v14, v15, v13, v13, up.getColor(24, 24, 30, (int)(230.0F * v7)), v16
            );
            hu.icon.drawCentered("M", v14 + v13 / 2.0F, v15 + v13 / 2.0F - 2.5F * S, 5.5F * S, up.replAlpha(up.client(), v7));
         }

         float v17 = v14 + v13 + 5.5F * S;
         float v18 = v8 - (v17 - v5) - v11;
         String v19 = info.getTitle().isEmpty() ? "Нет трека" : info.getTitle();
         String v20 = info.getArtist().isEmpty() ? "—" : info.getArtist();
         dt.enable(v17, v15 - 1.0F * S, v18, v13 + 2.0F * S);
         hu.sf_medium
            .drawFadingText(
               v19,
               v17,
               v15 + 0.5F * S,
               v18,
               up.getColor(245, 245, 250, (int)(255.0F * v7)),
               6.8F * S
            );
         float v38 = v15 + 8.0F * S;
         int temp5 = 155;

         hu.sf_medium
            .drawFadingText(v20, v17, v38, v18, up.getColor(temp5, 155, 165, (int)(220.0F * v7)), 5.8F * S);
         dt.disable();
         long v21 = Math.max(1L, info.getDuration());
         long v23 = MathHelper.method_53062(info.getPosition(), 0L, v21);
         float v25 = v5 + v11;
         float v26 = v15 + v13 + 2.5F * S;
         float v27 = v8 - 2.0F * v11;
         float v28 = 1.5F * S;
         float v29 = v27 * ((float)v23 / (float)v21);
         this.f492 = MathHelper.method_16439(0.18F, this.f492, MathHelper.method_15363(v29, 0.0F, v27));
         vr.rect(
            v25, v26, v27, v28, up.getColor(28, 28, 36, (int)(220.0F * v7)), v28 / 2.0F
         );
         if (this.f492 > 0.5F) {
            vr.rect(v25, v26, this.f492, v28, up.replAlpha(up.client(), v7), v28 / 2.0F);
         }

         float v30 = v26 + v28 + 2.0F * S;
         String v31 = m488(v23);
         String v32 = m488(v21);
         hu.sf_medium
            .draw(
               v31,
               v5 + v11,
               v30,
               5.2F * S,
               up.getColor(140, 140, 150, (int)(200.0F * v7))
            );
         float v33 = hu.sf_medium.getWidth(v32, 5.2F * S);
         hu.sf_medium
            .draw(
               v32,
               v5 + v8 - v11 - v33,
               v30,
               5.2F * S,
               up.getColor(140, 140, 150, (int)(200.0F * v7))
            );
         float v34 = v5 + v8 / 2.0F;
         float v35 = 9.0F * S;
         this.f499 = 5.0F * S;
         this.f500 = 5.0F * S;
         this.f497 = v34 - v35 - this.f499 / 2.0F;
         this.f498 = v30 - 0.8F * S;
         this.f495 = 5.5F * S;
         this.f496 = 5.5F * S;
         this.f493 = v34 - this.f495 / 2.0F;
         this.f494 = v30 - 0.8F * S;
         this.f503 = 5.0F * S;
         this.f504 = 5.0F * S;
         this.f501 = v34 + v35 - this.f503 / 2.0F;
         this.f502 = v30 - 0.8F * S;
         hu.icon
            .drawCentered(
               "O",
               this.f497 + this.f499 / 2.0F,
               this.f498 + 0.8F * S,
               4.0F * S,
               up.getColor(160, 160, 170, (int)(200.0F * v7))
            );
         hu.icon.drawCentered(info.getPlaying() ? "V" : "M", this.f493 + this.f495 / 2.0F, this.f494 + 0.8F * S, 4.5F * S, up.replAlpha(up.client(), v7));
         hu.icon
            .drawCentered(
               "N",
               this.f501 + this.f503 / 2.0F,
               this.f502 + 0.8F * S,
               4.0F * S,
               up.getColor(160, 160, 170, (int)(200.0F * v7))
            );
      }
   }

   private static String m488(long seconds) {
      long v2 = seconds / (7173976475224840021L + -7173976475224839961L);
      long v4 = seconds % (2983597507389796617L + -2983597507389796557L);
      Object[] temp1 = new Object[2];
      Long temp4 = v2;

      temp1[0] = temp4;
      temp1[1] = v4;
      return String.format("%d:%02d", temp1);
   }

   public boolean onMouseClick(EventJump event) {
      if (event.getAction() == 1 && event.getButton() == 0) {
         if (this.f489.get() <= 0.01F) {
            return false;
         } else {
            double v2 = event.getMouseX();
            double v4 = event.getMouseY();
            IMediaSession v6 = this.f491;
            if (v6 == null) {

               return false;
            } else if (m487(v2, v4, this.f497, this.f498, this.f499, this.f500)) {
               v6.previous();
               return true;
            } else {

               if (m487(v2, v4, this.f493, this.f494, this.f495, this.f496)) {
                  v6.playPause();
                  return true;
               } else {

                  if (m487(v2, v4, this.f501, this.f502, this.f503, this.f504)) {
                     v6.next();
                     return true;
                  } else {
                     return false;
                  }
               }
            }
         }
      } else {
         return false;
      }
   }

   private synchronized ExecutorService m485() {
      if (this.f487 == null || this.f487.isShutdown()) {
         this.f487 = Executors.newSingleThreadExecutor(r -> {
            Thread var10000x = new Thread;

            var10000x./* $VF: Unable to resugar constructor */<init>(r, "white-media-hud");
            Thread v1x = var10000x;
            v1x.setDaemon(true);
            return v1x;
         });
         
      }

      return this.f487;
   }

   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null) {

         if (mc.field_1724.field_6012 % 5 == 0) {
            this.m485().submit(() -> {
               try {
                  List v1x = MediaPlayerInfo.Instance.getMediaSessions();
                  if (v1x == null || v1x.isEmpty()) {
                     return;
                  }

                  IMediaSession v2 = v1x.stream().filter(s -> {
                     MediaInfo var1xx = s.getMedia();
                     if (var1xx != null) {
                        if (!var1xx.getTitle().trim().isEmpty()) {
                           return true;
                        }

                        if (!var1xx.getArtist().trim().isEmpty()) {
                           return true;
                        }
                     }

                     return false;
                  }).max(Comparator.comparingInt(s -> {

                     MediaInfo var1xx = s.getMedia();
                     short v2x = 0;

                     if (var1xx.getPlaying()) {
                        v2x += 1000;
                     }

                     if (var1xx.getPosition() > 0L) {
                        v2x += 100;
                     }

                     if (var1xx.getDuration() > 0L) {
                        v2x += 50;
                     }

                     String v3x = var1xx.getArtist().toLowerCase();
                     String v4x = var1xx.getTitle().toLowerCase();
                     if (v3x.contains("spotify") || v4x.contains("spotify")) {
                        v2x += 200;
                     }

                     label39: {
                        if (!v3x.contains("yandex")) {
                           if (!v4x.contains("яндекс")) {
                              break label39;
                           }

                           
                        }

                        v2x += 200;
                     }

                     if (var1xx.getArtworkPng() != null && var1xx.getArtworkPng().length > 0) {
                        v2x += 50;
                     }

                     return v2x;
                  })).orElse(null);
                  this.f491 = v2;
                  if (v2 == null) {
                     return;
                  }

                  MediaInfo result = v2.getMedia();

                  MediaInfo v3 = result;
                  if (v3.getTitle().isEmpty()) {

                     if (v3.getArtist().isEmpty()) {
                        return;
                     }
                  }

                  this.f490 = v3;
                  this.f488.reset();
                  ps.updateArtwork(v3.getArtworkPng());
               } catch (Throwable v7) {
               }
            });
         }
      }
   }

   public synchronized void shutdown() {
      if (this.f487 != null) {
         this.f487.shutdownNow();
         
      }

      ps.reset();
   }

   @Override
   public void onRender(vf dragSetting, hh interFace) {
      boolean v3 = this.f491 != null
         || this.f490 != null && !this.f490.getTitle().isEmpty() && !this.f490.getTitle().equals("Нет трека") && !this.f488.finished(60000.0);
      boolean v4 = mc.field_1755 instanceof ChatScreen;
      boolean v5 = v3 || v4;
      this.f489.update();
      this.f489.run(v5 ? 1.0 : 0.0, 0.12F, ap.SINE_OUT, true);
      float v6 = this.f489.get();
      if (!v5 && v6 <= 0.01F) {
         dragSetting.active = false;
      } else {
         dragSetting.active = true;
         S = hh.getInstance().sizeHud.getValue();
         MediaInfo v7 = this.f490;
         if (interFace.isHud2()) {
            this.m486(dragSetting, interFace, v6, v7);
         } else {
            float v8 = 79.0F * S;
            float v9 = 41.0F * S;
            float v10 = 4.0F * S;
            float v11 = 5.0F * S;
            float v12 = 19.0F * S;
            float v13 = 4.0F * S;
            float v14 = dragSetting.position.x;
            float v15 = dragSetting.position.y;
            vs.ys.glow(v14, v15, v8, v9, up.replAlpha(up.getColor(0), 0.1F * v6), v11, 12.0F, 1.0F);
            vs.ws.blur(v14, v15, v8, v9, v6, v11, up.replAlpha(up.background(), hh.getInstance().alphaHUD.getValue() * v6));
            float v16 = v14 + v10;
            float v17 = v15 + v10;
            if (ps.hasArtwork()) {
               vs.xs.texture(ps.ARTWORK_ID, v16, v17, v12, v12, 1.0F, v13, up.getColor(255, v6));
            } else {
               vs.ys.rect(
                  v16,
                  v17,
                  v12,
                  v12,
                  up.multAlpha(new Color(30, 30, 36).getRGB(), v6),
                  v13
               );
               hu.icon.drawCentered("M", v16 + v12 / 2.0F, v17 + v12 / 2.0F - 1.75F * S, 3.75F * S, wo.getHudColor(v6));
            }

            float v18 = v16 + v12 + 5.0F * S;
            float v19 = v14 + v8 - v10 - v18;
            yt v20 = hu.sf_regular;
            yt v21 = hu.sf_regular;
            String v22 = v7.getTitle().isEmpty() ? "Нет трека" : v7.getTitle();
            String v23 = v7.getArtist().isEmpty() ? "—" : v7.getArtist();
            dt.enable(v18, v17, v19, v12);
            v20.drawFadingText(v22, v18, v17 + 2.0F * S, v19, up.getColor(255, v6), 7.0F * S);
            v21.drawFadingText(v23, v18, v17 + 10.0F * S, v19, up.getColor(180, v6), 6.0F * S);
            dt.disable();
            long v24 = Math.max(1L, v7.getDuration());
            long v26 = MathHelper.method_53062(v7.getPosition(), 0L, v24);
            String v28 = m488(v26);
            String v29 = m488(v24);
            float v30 = 6.0F * S;
            float v31 = v17 + v12 + 2.0F * S;
            float v32 = v21.getWidth(v28, v30);
            float v33 = v21.getWidth(v29, v30);
            int result = 150;

            int v34 = up.getColor(result, v6);
            v21.draw(v28, v14 + v10, v31 + 0.5F * S, v30, v34);
            v21.draw(v29, v14 + v8 - v10 - v33, v31 + 0.5F * S, v30, v34);
            float v35 = v14 + v10 + v32 + 2.5F * S;
            float v36 = v14 + v8 - v10 - v33 - 2.5F * S - v35;
            float v37 = v31 + 3.25F * S;
            float v38 = 1.25F * S;
            float v39 = v36 * ((float)v26 / (float)v24);
            this.f492 = MathHelper.method_16439(0.18F, this.f492, MathHelper.method_15363(v39, 0.0F, v36));
            int v40 = up.multAlpha(new Color(0).getRGB(), v6 * 0.1F);
            vs.ys.rect(v35, v37, v36, v38, v40, v38 / 2.0F);
            if (this.f492 > 0.5F) {
               int[] v52 = new int[]{
                  up.multAlpha(up.getClientColor1(1), v6), up.multAlpha(up.getClientColor1(1), v6), up.multAlpha(up.getClientColor1(1), v6), 0
               };
               int temp3 = up.getClientColor1(1);

               v52[3] = up.multAlpha(temp3, v6);
               int[] v41 = v52;
               vs.ys.gradientRect(v35, v37, this.f492, v38, v41, v38 / 2.0F);
            }

            float v51 = v15 + v9 - v10 - 5.0F * S;

            float v42 = v14 + v8 / 2.0F;
            float v43 = 10.0F * S;
            int v44 = wo.getHudColor(v6);
            int v45 = wo.getHudColor(v6 * 0.6F);
            this.f499 = 6.0F * S;
            this.f500 = 5.0F * S;
            this.f497 = v42 - v43 - this.f499 / 2.0F;
            this.f498 = v51;
            this.f495 = 6.0F * S;
            this.f496 = 5.0F * S;
            this.f493 = v42 - this.f495 / 2.0F;
            this.f494 = v51;
            this.f503 = 6.0F * S;
            this.f504 = 5.0F * S;
            this.f501 = v42 + v43 - this.f503 / 2.0F;
            this.f502 = v51;
            yt v46 = hu.icon;
            float v47 = 4.5F * S;
            v46.drawCentered("O", this.f497 + this.f499 / 2.0F, v51 + 0.75F * S, v47, v45);
            v46.drawCentered(v7.getPlaying() ? "V" : "M", this.f493 + this.f495 / 2.0F, v51 + 0.75F * S, v47, v44);
            v46.drawCentered("N", this.f501 + this.f503 / 2.0F, v51 + 0.75F * S, v47, v45);
            dragSetting.size.set(v8, v9);
         }
      }
   }

   public mh() {
      this.f489 = new Animation();
      this.f490 = EMPTY;
   }

   private static boolean m487(double mx, double my, float x, float y, float w, float h) {
      if (mx >= (double)x && mx <= (double)(x + w)) {

         if (my >= (double)y && my <= (double)(y + h)) {
            return true;
         }
      }

      return false;
   }

   
}
