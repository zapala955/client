package ru.white.module.impl.display.interfaceimpl;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import ru.white.utils.helper.Wu;
import ru.white.utils.misc.Vf;
import ru.white.utils.misc.Vq;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.dt;
import ru.white.utils.render.ps;
import ru.white.utils.render.vr;
import ru.white.utils.misc.Oq;
import ru.white.utils.PacketUtil;
import ru.white.utils.ItemUtil;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;
import ru.white.event.impl.EventJump;

@Environment(EnvType.CLIENT)
public class jh implements element {
   private long f456;
   private long f439;
   private float f473;
   private volatile nh f445;
   private float f461;
   private float f477;
   private String f438;
   private volatile MediaInfo f443;
   private float f467;
   private final wu f446;
   private float f434;
   private float f462;
   public boolean optExplicit;
   private float f466;
   private final Animation f427;
   private String f437;
   private static float S = 1.0F;
   private boolean f478;
   private String f451;
   private float f468;
   private final Animation f425;
   private float f433;
   private float f460;
   public boolean optScissor;
   private float f480;
   private float f469;
   private final AtomicBoolean f422 = new AtomicBoolean(false);
   private double f448;
   public boolean optSmoothness;
   private final Animation f428;
   private float f464;
   private float f470;
   private long f453;
   private float f472;
   private String f454;
   public boolean funMenuOpen;
   private oq.pq f452;
   private float f474;
   private float f458;
   private jh.kh f440;
   private final Animation f423 = new Animation();
   private jh.kh f441;
   private float f463;
   private static final MediaInfo EMPTY = new MediaInfo("Нет трека", "—", new byte[0], 0L, 0L, false);
   private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
   public boolean optWave;
   private boolean f455;
   private final Animation f457;
   private String f436;
   private float f476;
   public boolean optShowSource;
   private float f481;
   private float f471;
   private double f450;
   private float f475;
   private float f479;
   private float f459;
   public boolean optReverse;
   private long f442;
   private float f432;
   private String f435;
   private float f482;
   public boolean optFullAlpha;
   private final Animation f426;
   private boolean f431;
   private boolean f430;
   private long f449;
   private volatile IMediaSession f444;
   private ExecutorService f447;
   public boolean optGlow;
   private float f465;
   private boolean f429;
   private final Animation f424 = new Animation();

   private void m467(
      float x,
      float y,
      float w,
      float h,
      yt font,
      yt aeonik,
      MediaInfo current,
      String title,
      String artist,
      float pos,
      float len,
      float alpha,
      boolean isPlaying,
      float plyLyrProg
   ) {
      float v15 = 9.0F * S;
      float v16 = 25.0F * S;
      float v17 = x + v15;
      float v18 = y + v15;
      if (ps.hasArtwork()) {
         vr.texture(
            ps.ARTWORK_ID,
            v17,
            v18,
            v16,
            v16,
            0.0F,
            0.0F,
            1.0F,
            1.0F,
            up.getColor(255, 255, 255, (int)(255.0F * alpha)),
            0.5F,
            5.0F * S
         );
         vr.outline(
            v17,
            v18,
            v16,
            v16,
            0.6F * S,
            up.getColor(255, 255, 255, (int)(35.0F * alpha)),
            5.0F * S
         );
         if (this.optShowSource && this.f445 != nh.OTHER && this.f445.getIcon() != null) {
            float v19 = 8.5F * S;
            float v20 = v17 + v16 - v19 + 1.2F * S;
            float v21 = v18 + v16 - v19 + 1.2F * S;
            vr.rect(
               v20 - 0.6F * S,
               v21 - 0.6F * S,
               v19 + 1.2F * S,
               v19 + 1.2F * S,
               up.getColor(10, 10, 14, (int)(245.0F * alpha)),
               (v19 + 1.2F * S) / 2.0F
            );
            vr.texture(
               this.f445.getIcon(),
               v20,
               v21,
               v19,
               v19,
               0.0F,
               0.0F,
               1.0F,
               1.0F,
               up.getColor(255, 255, 255, (int)(255.0F * alpha)),
               0.5F,
               v19 / 2.0F
            );
         }
      } else {
         vr.rect(
            v17,
            v18,
            v16,
            v16,
            up.getColor(26, 26, 32, (int)(230.0F * alpha)),
            5.0F * S
         );
         if (this.optShowSource && this.f445 != nh.OTHER && this.f445.getIcon() != null) {
            float v51 = 16.0F * S;
            vr.texture(
               this.f445.getIcon(),
               v17 + (v16 - v51) / 2.0F,
               v18 + (v16 - v51) / 2.0F,
               v51,
               v51,
               0.0F,
               0.0F,
               1.0F,
               1.0F,
               up.getColor(255, 255, 255, (int)(255.0F * alpha)),
               0.5F,
               v51 / 2.0F
            );
         } else {
            hu.icon
               .draw(
                  "M",
                  v17 + 6.5F * S,
                  v18 + 6.5F * S,
                  12.0F * S,
                  up.getColor(240, 240, 250, (int)(230.0F * alpha))
               );
         }
      }

      float v52 = v17 + v16 + 8.0F * S;
      float v53 = v18 + 1.0F * S;
      float v54 = w - (v52 - x) - 28.0F * S;
      String v22 = this.m474(font, title, v54, 7.8F * S);
      font.draw(v22, v52, v53, 7.8F * S, up.getColor(250, 250, 255, (int)(250.0F * alpha)));
      float v23 = v53 + 9.5F * S;
      float v24 = v52;
      if (this.optExplicit) {
         float v25 = 8.5F * S;
         float v26 = 7.0F * S;
         vr.rect(
            v52,
            v23 - 0.5F * S,
            v25,
            v26,
            up.getColor(255, 255, 255, (int)(55.0F * alpha)),
            1.5F * S
         );
         hu.sf_bold
            .draw(
               "E",
               v52 + 2.0F * S,
               v23,
               5.0F * S,
               up.getColor(12, 12, 16, (int)(240.0F * alpha))
            );
         v24 = v52 + v25 + 3.5F * S;
      }

      String v55 = this.m474(font, artist, w - (v24 - x) - 28.0F * S, 6.5F * S);
      font.draw(v55, v24, v23, 6.5F * S, up.getColor(160, 160, 175, (int)(210.0F * alpha)));
      if (this.optShowSource && this.f445 != nh.OTHER) {
         float v56 = v23 + 8.5F * S;
         float v27 = 7.0F * S;
         float v28 = 5.0F * S;
         String v29 = this.f445.getDisplayName();
         float v30 = font.getWidth(v29, 5.0F * S);
         float v31 = v28 + v30 + 7.0F * S;
         vr.rect(v52, v56, v31, v27, this.f445.getBgColor(alpha), v27 / 2.0F);
         vr.outline(v52, v56, v31, v27, 0.5F * S, this.f445.getBorderColor(alpha), v27 / 2.0F);
         if (this.f445.getIcon() != null) {
            vr.texture(
               this.f445.getIcon(),
               v52 + 1.8F * S,
               v56 + (v27 - v28) / 2.0F,
               v28,
               v28,
               0.0F,
               0.0F,
               1.0F,
               1.0F,
               up.getColor(255, 255, 255, (int)(255.0F * alpha)),
               0.5F,
               v28 / 2.0F
            );
         }

         font.draw(
            v29,
            v52 + v28 + 3.8F * S,
            v56 + 1.0F * S,
            5.0F * S,
            up.getColor(235, 235, 245, (int)(240.0F * alpha))
         );
      }

      float v57 = x + w - v15 - 10.0F * S;
      float v58 = v18 + 10.0F * S;
      this.m470(v57, v58, alpha, isPlaying, 4);
      float v59 = len > 0.0F ? MathHelper.method_15363(pos / len, 0.0F, 1.0F) : 0.0F;
      String v60 = this.m473((int)pos);
      String v61 = this.m473((int)len);
      this.f474 = x + v15 + 18.0F * S;
      this.f475 = v18 + v16 + 8.5F * S;
      this.f476 = w - 2.0F * v15 - 36.0F * S;

      this.f477 = 2.5F * S;
      float v62 = font.getWidth(v60, 5.5F * S);
      font.draw(
         v60,
         this.f474 - v62 - 4.0F * S,
         this.f475 - 2.0F * S,
         5.5F * S,
         up.getColor(160, 160, 175, (int)(200.0F * alpha))
      );

      font.draw(
         v61,
         this.f474 + this.f476 + 4.0F * S,
         this.f475 - 2.0F * S,
         5.5F * S,
         up.getColor(160, 160, 175, (int)(200.0F * alpha))
      );
      vr.rect(
         this.f474,
         this.f475,
         this.f476,
         this.f477,
         up.getColor(255, 255, 255, (int)(35.0F * alpha)),
         1.25F * S
      );
      float v32 = this.f476 * v59;
      if (v32 > 0.5F) {
         vr.rect(
            this.f474,
            this.f475,
            v32,
            this.f477,
            up.getColor(255, 255, 255, (int)(225.0F * alpha)),
            1.25F * S
         );
      }

      float v33 = 2.4F * S;
      vr.rect(
         this.f474 + v32 - v33,
         this.f475 + this.f477 / 2.0F - v33,
         v33 * 2.0F,
         v33 * 2.0F,
         up.getColor(255, 255, 255, (int)(255.0F * alpha)),
         v33
      );
      if (plyLyrProg > 0.01F && this.f431) {
         float v34 = plyLyrProg * alpha;
         float v35 = this.f475 + this.f477 + 8.0F * S;
         float v36 = 60.0F * S;
         float v37 = x + v15 + 3.0F * S;
         float v38 = w - 2.0F * v15 - 6.0F * S;
         dt.enable(x + v15, v35 - 2.0F * S, v38 + 6.0F * S, v36 + 4.0F * S);
         vq.xq v39 = vq.getLyricsData(artist, title, len);
         if (v39 != null && !v39.getSyncedLines().isEmpty()) {
            int v71 = v39.getActiveIndex(pos);
            List v72 = v39.getSyncedLines();

            for (int v73 = v71 - 2; v73 <= v71 + 3; v73++) {
               if (v73 >= 0 && v73 < v72.size()) {
                  String v74 = filterForbiddenText(((vq.wq)v72.get(v73)).text().trim());
                  int v75 = v73 - v71;
                  float v76 = v35 + (float)(v75 + 2) * 10.0F * S;
                  if (v75 == 0) {
                     hu.sf_bold
                        .draw(
                           v74,
                           v37,
                           v76,
                           7.5F * S,
                           up.getColor(255, 255, 255, (int)(255.0F * v34))
                        );
                  } else if (Math.abs(v75) == 1) {
                     hu.sf_medium
                        .draw(
                           v74,
                           v37,
                           v76,
                           6.8F * S,
                           up.getColor(185, 185, 195, (int)(165.0F * v34))
                        );
                  } else if (Math.abs(v75) == 2) {
                     hu.sf_medium
                        .draw(
                           v74,
                           v37,
                           v76,
                           6.5F * S,
                           up.getColor(160, 160, 175, (int)(95.0F * v34))
                        );
                  } else {
                     hu.sf_medium
                        .draw(
                           v74,
                           v37,
                           v76,
                           6.2F * S,
                           up.getColor(140, 140, 155, (int)(55.0F * v34))
                        );
                  }
               }
            }
         } else if (v39 != null && !v39.getPlainLines().isEmpty()) {
            List v40 = v39.getPlainLines();
            float v41 = len > 0.0F ? len : 180.0F;
            float v42 = MathHelper.method_15363(pos / v41, 0.0F, 1.0F);
            int v43 = (int)Math.min((float)(v40.size() - 1), Math.max(0.0F, v42 * (float)v40.size()));

            for (int v44 = v43 - 2; v44 <= v43 + 3; v44++) {
               if (v44 >= 0 && v44 < v40.size()) {
                  String v45 = filterForbiddenText(((String)v40.get(v44)).trim());
                  int v46 = v44 - v43;
                  float v47 = v35 + (float)(v46 + 2) * 10.0F * S;
                  if (v46 == 0) {
                     hu.sf_bold
                        .draw(
                           v45,
                           v37,
                           v47,
                           7.5F * S,
                           up.getColor(255, 255, 255, (int)(255.0F * v34))
                        );
                  } else if (Math.abs(v46) == 1) {
                     hu.sf_medium
                        .draw(
                           v45,
                           v37,
                           v47,
                           6.8F * S,
                           up.getColor(185, 185, 195, (int)(165.0F * v34))
                        );
                  } else if (Math.abs(v46) == 2) {
                     hu.sf_medium
                        .draw(
                           v45,
                           v37,
                           v47,
                           6.5F * S,
                           up.getColor(160, 160, 175, (int)(95.0F * v34))
                        );
                  } else {
                     hu.sf_medium
                        .draw(
                           v45,
                           v37,
                           v47,
                           6.2F * S,
                           up.getColor(140, 140, 155, (int)(55.0F * v34))
                        );
                  }
               }
            }
         }

         dt.disable();
      }

      float v63 = y + h - 16.0F * S;
      if (this.f431) {
         this.f460 = 16.0F * S;
         this.f461 = 14.0F * S;
         this.f458 = x + v15 + 2.0F * S;
         this.f459 = v63 - 3.0F * S;
         int v64 = this.f430
            ? up.getColor(255, 255, 255, (int)(255.0F * alpha))
            : up.getColor(175, 175, 190, (int)(210.0F * alpha));
         float v66 = 1.35F * S;
         float v68 = 1.8F * S;
         float v69 = this.f458 + 2.0F * S;
         float v70 = this.f459 + 2.8F * S;
         vr.rect(v69, v70, 11.5F * S, v66, v64, v66 / 2.0F);
         vr.rect(v69, v70 + v66 + v68, 11.5F * S, v66, v64, v66 / 2.0F);
         vr.rect(v69, v70 + 2.0F * (v66 + v68), 7.5F * S, v66, v64, v66 / 2.0F);
      } else {
         this.f460 = 0.0F;
         this.f461 = 0.0F;
         this.f458 = -9999.0F;
         this.f459 = -9999.0F;
      }

      float v65 = x + w / 2.0F;
      this.f464 = 16.0F * S;
      this.f465 = 16.0F * S;
      this.f462 = v65 - 26.0F * S - this.f464 / 2.0F;
      this.f463 = v63 - 4.0F * S;
      hu.icon
         .drawCentered(
            "O",
            this.f462 + this.f464 / 2.0F,
            this.f463 + 4.5F * S,
            7.5F * S,
            up.getColor(235, 235, 245, (int)(235.0F * alpha))
         );
      this.f468 = 18.0F * S;
      this.f469 = 18.0F * S;
      this.f466 = v65 - this.f468 / 2.0F;
      this.f467 = v63 - 5.0F * S;
      String v67 = isPlaying ? "V" : "M";
      hu.icon
         .drawCentered(
            v67,
            this.f466 + this.f468 / 2.0F,
            this.f467 + 4.0F * S,
            9.0F * S,
            up.getColor(255, 255, 255, (int)(255.0F * alpha))
         );
      this.f472 = 16.0F * S;
      this.f473 = 16.0F * S;
      this.f470 = v65 + 26.0F * S - this.f472 / 2.0F;
      this.f471 = v63 - 4.0F * S;
      hu.icon
         .drawCentered(
            "N",
            this.f470 + this.f472 / 2.0F,
            this.f471 + 4.5F * S,
            7.5F * S,
            up.getColor(235, 235, 245, (int)(235.0F * alpha))
         );
   }

   private String m474(yt f, String text, float maxW, float size) {
      if (text != null) {

         if (!text.isEmpty()) {
            if (f.getWidth(text, size) <= maxW) {
               return text;
            }

            while (!text.isEmpty()) {
               StringBuilder v9 = new StringBuilder().append((Object)text);

               if (!(f.getWidth(v9.append("...").toString(), size) > maxW)) {
                  break;
               }

               text = text.substring(0, text.length() - 1);
            }

            return text + "...";
         }
      }

      return "";
   }

   private void m471(hh interFace) {
      long v2 = System.currentTimeMillis();
      long v4 = -9157438758999468979L + 9157438758999471479L;
      synchronized (oq.entries()) {
         List v7 = oq.entries();
         oq.pq v8 = null;
         int v9 = v7.size() - 1;

         for (; v9 >= 0; v9--) {
            oq.pq v10 = (oq.pq)v7.get(v9);
            if (!v10.isExpired()) {

               if (!v10.removing) {
                  v8 = v10;
                  break;
               }
            }
         }

         if (v8 != null) {
            if (this.f452 != v8) {
               this.f452 = v8;
               this.f453 = v2;

               this.f454 = this.m472(v8);
               this.f427.set(0.0);
               this.f427.run(1.0, 0.22F, ap.CUBIC_OUT);
            } else if (v2 - this.f453 >= v4) {
               this.f452 = null;
            }
         } else if (this.f452 != null && v2 - this.f453 >= v4) {
            this.f452 = null;
         }
      }
   }

   public synchronized void shutdown() {
      if (this.f447 != null) {
         this.f447.shutdownNow();
      }
   }

   public static String filterForbiddenText(String text) {
      if (text == null) {
         return "";
      } else if (text.contains(
         "Новое отображение текста песни в Rockstar 2.1"
      )) {
         int result = v1 * (v1 + 1);

         return "";
      } else {
         return text.contains("Новое отображение текста") ? "" : text;
      }
   }

   private void m465(float islandX, float islandY, float islandW, float barH, float alpha, yt font) {
      String v7 = LocalTime.now().format(TIME_FORMATTER);
      float v8 = 7.5F * S;
      float v9 = font.getWidth(v7, v8);
      float v10 = islandX - v9 - 14.0F * S;
      float v11 = islandY + (barH - v8) / 2.0F - 0.5F * S;

      font.draw(v7, v10, v11, v8, up.getColor(245, 245, 250, (int)(235.0F * alpha)));
      float v12 = islandX + islandW + 14.0F * S;
      font.draw("✈", v12, v11, v8, up.getColor(245, 245, 250, (int)(235.0F * alpha)));
   }

   public jh() {
      this.f425 = new Animation();
      this.f426 = new Animation();
      this.f427 = new Animation();
      this.f428 = new Animation();
      this.f429 = false;
      this.f430 = false;
      this.f431 = false;
      this.funMenuOpen = false;
      this.optSmoothness = true;
      this.optFullAlpha = false;
      this.optWave = true;
      this.optGlow = true;
      this.optReverse = false;
      this.optScissor = true;
      this.optExplicit = true;
      this.optShowSource = true;
      this.f432 = 0.0F;
      this.f433 = 0.0F;
      this.f434 = 0.0F;
      this.f435 = "";
      this.f436 = "";
      this.f437 = "";
      this.f438 = "";
      this.f439 = 0L;
      this.f440 = null;
      this.f441 = null;
      this.f442 = 0L;
      this.f443 = EMPTY;
      this.f444 = null;
      this.f445 = nh.OTHER;
      this.f446 = new wu();
      this.f448 = 0.0;
      this.f449 = 0L;
      this.f450 = -1.0;
      this.f451 = "";
      this.f452 = null;
      this.f453 = 0L;
      this.f454 = "";
      this.f455 = false;
      this.f456 = 0L;
      this.f457 = new Animation();
      this.f478 = false;
   }

   private String m472(oq.pq entry) {
      if (entry == null) {
         return "";
      } else {
         String v2 = entry.text;
         if (v2.startsWith("Функция ")) {
            String v3 = v2.substring("Функция ".length());

            int v4 = v3.indexOf(" активирована");
            if (v4 != -1) {
               String v5 = v3.substring(0, v4);
               return v5 + " включено";
            }

            v4 = v3.indexOf(" деактивирована");
            if (v4 != -1) {
               String v6 = v3.substring(0, v4);
               return v6 + " деактивировано";
            }
         }

         return v2;
      }
   }

   public void onMouseClick(EventJump event, hh interFace, vf dragSetting) {
      if (!(mc.field_1755 instanceof ChatScreen)) {
         this.f429 = false;
         this.funMenuOpen = false;
         this.f478 = false;
      } else {
         float v4 = 2.0F;
         float v5 = (float)mc.method_22683().method_4495();
         float v6 = v4 / v5;
         float v7 = (float)(event.getMouseX() / (double)v6);
         float v8 = (float)(event.getMouseY() / (double)v6);
         if (event.getAction() == 0) {
            this.f478 = false;
         } else if (event.getAction() == 1) {
            float v9 = dragSetting.position.x;
            float v10 = dragSetting.position.y;
            float v11 = dragSetting.size.x;
            float v12 = dragSetting.size.y;
            boolean v13 = fq.isHovered(v7, v8, v9, v10, v11, v12);
            if (event.getButton() != 1) {
               if (event.getButton() == 0) {
                  if (this.funMenuOpen && fq.isHovered(v7, v8, this.f479, this.f480, this.f481, this.f482)) {
                     this.m464(v7, v8, interFace);
                     return;
                  }

                  if (this.f429) {
                     if (this.f431 && fq.isHovered(v7, v8, this.f458 - 4.0F * S, this.f459 - 4.0F * S, this.f460 + 8.0F * S, this.f461 + 8.0F * S)) {
                        interFace.islandLyrics.set(!interFace.islandLyrics.getValue());
                        this.f430 = interFace.islandLyrics.getValue();
                        return;
                     }

                     if (fq.isHovered(v7, v8, this.f462 - 4.0F * S, this.f463 - 4.0F * S, this.f464 + 8.0F * S, this.f465 + 8.0F * S)) {
                        if (this.f444 != null) {
                           this.f444.previous();
                        }

                        return;
                     }

                     if (fq.isHovered(v7, v8, this.f466 - 4.0F * S, this.f467 - 4.0F * S, this.f468 + 8.0F * S, this.f469 + 8.0F * S)) {
                        if (this.f444 != null) {
                           this.f444.playPause();
                        }

                        return;
                     }

                     if (fq.isHovered(v7, v8, this.f470 - 4.0F * S, this.f471 - 4.0F * S, this.f472 + 8.0F * S, this.f473 + 8.0F * S)) {
                        if (this.f444 != null) {
                           this.f444.next();
                        }

                        return;
                     }

                     if (fq.isHovered(v7, v8, this.f474 - 4.0F * S, this.f475 - 4.0F * S, this.f476 + 8.0F * S, this.f477 + 8.0F * S)) {
                        this.f478 = true;
                        return;
                     }

                     if (v13 && v8 < v10 + 24.0F * S) {
                        this.f429 = false;
                        this.funMenuOpen = false;
                        return;
                     }
                  } else if (v13) {
                     this.f429 = true;
                     return;
                  }

                  if (!v13) {
                     this.f429 = false;
                     this.funMenuOpen = false;
                  }
               }
            } else {
               if (v13) {
                  this.funMenuOpen = !this.funMenuOpen;
               } else {
                  if (this.funMenuOpen) {

                     if (fq.isHovered(v7, v8, this.f479, this.f480, this.f481, this.f482)) {
                        this.m464(v7, v8, interFace);
                        return;
                     }
                  }

                  this.funMenuOpen = false;
               }
            }
         }
      }
   }

   private void m468(float islandX, float islandY, float islandW, float islandH, yt font, float mAlpha, hh interFace) {
      this.f481 = 88.0F * S;
      this.f482 = 133.5F * S;
      this.f479 = islandX + (islandW - this.f481) / 2.0F;
      this.f480 = islandY + islandH + 5.0F * S;
      vr.glow(this.f479, this.f480, this.f481, this.f482, up.getColor(0, 0, 0, (int)(120.0F * mAlpha)), 6.0F * S, 8.0F * S, 0.5F);
      vr.blur(this.f479, this.f480, this.f481, this.f482, mAlpha, 6.0F * S, up.replAlpha(up.background(), mAlpha));
      if (this.optFullAlpha) {
         vr.rect(this.f479, this.f480, this.f481, this.f482, up.replAlpha(up.background(), mAlpha), 6.0F * S);
      }

      vr.outline(
         this.f479,
         this.f480,
         this.f481,
         this.f482,
         0.6F * S,
         up.getColor(255, 255, 255, (int)(30.0F * mAlpha)),
         6.0F * S
      );
      font.draw(
         "Настройки",
         this.f479 + 8.0F * S,
         this.f480 + 5.0F * S,
         6.8F * S,
         up.getColor(245, 245, 250, (int)(245.0F * mAlpha))
      );
      float v8 = this.f480 + 16.0F * S;
      float v9 = 11.5F * S;

      this.m469(font, this.f479 + 6.0F * S, v8, "Плавность", this.optSmoothness, mAlpha);
      v8 += v9;
      this.m469(font, this.f479 + 6.0F * S, v8, "Непрозрачность", this.optFullAlpha, mAlpha);
      v8 += v9;
      this.m469(font, this.f479 + 6.0F * S, v8, "Волна", this.optWave, mAlpha);
      v8 += v9;
      this.m469(font, this.f479 + 6.0F * S, v8, "Свечение", this.optGlow, mAlpha);
      v8 += v9;
      this.m469(font, this.f479 + 6.0F * S, v8, "Реверс", this.optReverse, mAlpha);
      v8 += v9;
      this.m469(font, this.f479 + 6.0F * S, v8, "Обрезка", this.optScissor, mAlpha);
      v8 += v9;
      this.m469(font, this.f479 + 6.0F * S, v8, "Иконка 18+", this.optExplicit, mAlpha);
      v8 += v9;
      float temp2 = this.f479 + 6.0F * S;

      this.m469(
         font, temp2, v8, "Отображать текст", interFace.islandLyrics.getValue(), mAlpha
      );
      v8 += v9;
      this.m469(font, this.f479 + 6.0F * S, v8, "Источник", this.optShowSource, mAlpha);
   }

   private void m466(
      float x,
      float y,
      float w,
      float h,
      yt font,
      MediaInfo current,
      String text,
      boolean hasNotif,
      boolean hasPvp,
      boolean isSinging,
      float lyrProg,
      float alpha,
      boolean isPlaying,
      boolean hasMedia,
      float dt
   ) {
      float v16 = 12.0F * S;
      float v17 = x + 6.0F * S;
      float v18 = y + (h - v16) / 2.0F;
      float v19 = hasNotif && !hasPvp ? x + 8.0F * S : v17 + v16 + 5.5F * S;
      float v20 = hasNotif && !hasPvp ? x + w - 8.0F * S : x + w - 18.0F * S;
      float v21 = Math.max(10.0F * S, v20 - v19);
      float v22 = 7.5F * S;
      float v23 = y + (h - v22) / 2.0F - 0.5F * S;
      long v24 = System.currentTimeMillis();
      if (this.f441 != null && v24 - this.f441.exitTime >= -576328159020666024L + 576328159020666424L) {
         this.f441 = null;
      }

      if (this.f440 != null) {
         this.f440.update(this.f434, isSinging, v21, dt, S, this.optSmoothness);
      }

      dt.enable(v19, y, v21, h);
      if (this.f441 != null && !hasNotif && !hasPvp) {
         this.m475(font, this.f441, v19, v23, v21, h, alpha, false, true, v24, hasNotif, hasPvp);
      }

      if (this.f440 != null) {
         float v26 = hasNotif && !hasPvp ? x + (w - this.f440.totalWidth) / 2.0F : v19;
         this.m475(font, this.f440, v26, v23, v21, h, alpha, isSinging, false, v24, hasNotif, hasPvp);
      }

      dt.disable();
      if (hasPvp) {
         vr.glow(
            v17,
            v18,
            v16,
            v16,
            up.getColor(255, 60, 70, (int)(140.0F * alpha)),
            3.5F * S,
            4.0F * S,
            0.6F
         );
         vr.rect(
            v17, v18, v16, v16, up.getColor(180, 25, 35, (int)(230.0F * alpha)), 3.5F * S
         );
         vr.outline(
            v17,
            v18,
            v16,
            v16,
            0.6F * S,
            up.getColor(255, 100, 110, (int)(180.0F * alpha)),
            3.5F * S
         );
         float v33 = 8.5F * S;
         float v27 = hu.nightix_2.getWidth("B", v33);
         hu.nightix_2
            .draw(
               "B",
               v17 + (v16 - v27) / 2.0F,
               v18 + (v16 - v33) / 2.0F + 0.3F * S,
               v33,
               up.getColor(255, 255, 255, (int)(255.0F * alpha))
            );
      } else if (!hasNotif && hasMedia) {
         if (ps.hasArtwork()) {
            vr.texture(
               ps.ARTWORK_ID,
               v17,
               v18,
               v16,
               v16,
               0.0F,
               0.0F,
               1.0F,
               1.0F,
               up.getColor(255, 255, 255, (int)(255.0F * alpha)),
               0.5F,
               3.0F * S
            );
            vr.outline(
               v17,
               v18,
               v16,
               v16,
               0.5F * S,
               up.getColor(255, 255, 255, (int)(30.0F * alpha)),
               3.0F * S
            );
            if (this.optShowSource && this.f445 != nh.OTHER && this.f445.getIcon() != null) {
               float v34 = 5.2F * S;
               float v38 = v17 + v16 - v34 + 0.8F * S;
               float v28 = v18 + v16 - v34 + 0.8F * S;
               vr.rect(
                  v38 - 0.4F * S,
                  v28 - 0.4F * S,
                  v34 + 0.8F * S,
                  v34 + 0.8F * S,
                  up.getColor(10, 10, 14, (int)(240.0F * alpha)),
                  (v34 + 0.8F * S) / 2.0F
               );
               Identifier result = this.f445.getIcon();
               int var10009 = 255;
               int var10010 = 255;
               int var10011 = 255;

               vr.texture(
                  result,
                  v38,
                  v28,
                  v34,
                  v34,
                  0.0F,
                  0.0F,
                  1.0F,
                  1.0F,
                  up.getColor(var10009, var10010, var10011, (int)(255.0F * alpha)),
                  0.5F,
                  v34 / 2.0F
               );
            }
         } else {
            vr.rect(
               v17,
               v18,
               v16,
               v16,
               up.getColor(30, 30, 38, (int)(220.0F * alpha)),
               3.5F * S
            );
            if (this.optShowSource && this.f445 != nh.OTHER && this.f445.getIcon() != null) {
               float v35 = 9.0F * S;
               vr.texture(
                  this.f445.getIcon(),
                  v17 + (v16 - v35) / 2.0F,
                  v18 + (v16 - v35) / 2.0F,
                  v35,
                  v35,
                  0.0F,
                  0.0F,
                  1.0F,
                  1.0F,
                  up.getColor(255, 255, 255, (int)(255.0F * alpha)),
                  0.5F,
                  v35 / 2.0F
               );
            } else {
               hu.icon
                  .draw(
                     "M",
                     v17 + 2.5F * S,
                     v18 + 2.8F * S,
                     6.5F * S,
                     up.getColor(235, 235, 245, (int)(230.0F * alpha))
                  );
            }
         }
      } else if (!hasNotif && !hasMedia) {

         vr.rect(
            v17,
            v18,
            v16,
            v16,
            up.getColor(30, 30, 38, (int)(220.0F * alpha)),
            3.5F * S
         );
         hu.icon
            .draw(
               "M",
               v17 + 2.5F * S,
               v18 + 2.8F * S,
               6.5F * S,
               up.getColor(235, 235, 245, (int)(230.0F * alpha))
            );
      }

      if (hasPvp) {
         float v36 = 4.5F * S;
         float v39 = x + w - 10.5F * S;
         float v41 = y + (h - v36) / 2.0F;
         float v29 = (float)Math.sin((double)System.currentTimeMillis() / 180.0) * 0.5F + 0.5F;
         float temp1 = 1.0F * S;

         vr.glow(
            v39 - temp1,
            v41 - 1.0F * S,
            v36 + 2.0F * S,
            v36 + 2.0F * S,
            up.getColor(255, 45, 55, (int)((120.0F + 80.0F * v29) * alpha)),
            (v36 + 2.0F * S) / 2.0F,
            4.0F * S,
            0.7F
         );
         vr.rect(
            v39,
            v41,
            v36,
            v36,
            up.getColor(255, 60, 70, (int)(255.0F * alpha)),
            v36 / 2.0F
         );
      } else if (!hasNotif && hasMedia) {
         float v37 = x + w - 9.75F * S;
         float v40 = y + h / 2.0F;
         this.m470(v37, v40, alpha, isPlaying, 4);
      }
   }

   public void onTick() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!(mc.field_1755 instanceof ChatScreen)) {
            this.f429 = false;
            this.funMenuOpen = false;

            this.f478 = false;
         }

         if (mc.field_1724.field_6012 % 4 == 0) {
            if (this.f422.compareAndSet(false, true)) {
               this.m463()
                  .submit(
                     () -> {
                        try {
                           List v1x = MediaPlayerInfo.Instance.getMediaSessions();
                           if (v1x != null && !v1x.isEmpty()) {
                              IMediaSession v2 = v1x.stream()
                                 .filter(s -> {
                                    try {
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
                                    } catch (Throwable v5x) {
                                       return false;
                                    }
                                 })
                                 .max(
                                    Comparator.comparingInt(
                                       s -> {
                                          try {
                                             MediaInfo var1xx = s.getMedia();
                                             if (var1xx == null) {
                                                return -1000;
                                             } else {
                                                short v2x = 0;

                                                try {
                                                   if (var1xx.getPlaying()) {
                                                      v2x += 1000;
                                                   }
                                                } catch (Throwable v13) {
                                                }

                                                try {
                                                   if (var1xx.getPosition() > 0L) {
                                                      v2x += 100;
                                                   }
                                                } catch (Throwable var12x) {
                                                }

                                                try {
                                                   if (var1xx.getDuration() > 0L) {
                                                      v2x += 50;
                                                      
                                                   }
                                                } catch (Throwable var11x) {
                                                }

                                                String v3x = var1xx.getArtist() != null ? var1xx.getArtist().toLowerCase() : "";
                                                String v4 = var1xx.getTitle() != null ? var1xx.getTitle().toLowerCase() : "";
                                                String v15 = s.getOwner();

                                                String v5x = v15 != null ? s.getOwner().toLowerCase() : "";
                                                if (v5x.contains("spotify") || v3x.contains("spotify") || v4.contains("spotify")) {
                                                   v2x += 300;
                                                }

                                                if (v5x.contains("yandex") || v5x.contains("яндекс") || v3x.contains("yandex") || v4.contains("яндекс")
                                                   )
                                                 {
                                                   v2x += 300;
                                                }

                                                if (v5x.contains("soundcloud")
                                                   || v3x.contains("soundcloud")
                                                   || v4.contains("soundcloud")
                                                   || v5x.contains("brave")
                                                   || v5x.contains("ypvl")) {
                                                   v2x += 300;
                                                }

                                                if (v5x.contains("telegram") || v3x.contains("telegram") || v4.contains("telegram")) {
                                                   v2x += 300;
                                                }

                                                try {
                                                   if (var1xx.getArtworkPng() != null && var1xx.getArtworkPng().length > 0) {
                                                      v2x += 50;
                                                   }
                                                } catch (Throwable var10x) {
                                                }

                                                return v2x;
                                             }
                                          } catch (Throwable v14) {
                                             return -1000;
                                          }
                                       }
                                    )
                                 )
                                 .orElse(null);
                              if (v2 != null) {
                                 this.f444 = v2;

                                 try {
                                    MediaInfo v3 = v2.getMedia();
                                    if (v3 != null && (!v3.getTitle().isEmpty() || !v3.getArtist().isEmpty())) {
                                       this.f443 = v3;
                                       this.f446.reset();
                                       ps.updateArtwork(v3.getArtworkPng());
                                       this.f445 = nh.detect(v2, v3);
                                       return;
                                    }
                                 } catch (Throwable v10) {
                                 }

                                 return;
                              } else {
                                 boolean var10000x = this.f446.finished(10000.0);

                                 if (var10000x) {
                                    this.f444 = null;
                                    this.f445 = nh.OTHER;
                                    return;
                                 }

                                 return;
                              }
                           }

                           if (this.f446.finished(10000.0)) {
                              this.f444 = null;
                           }
                        } catch (Throwable v11) {
                           return;
                        } finally {
                           this.f422.set(false);
                        }
                     }
                  );
            }
         }
      }
   }

   @Override
   public void onRender(vf dragSetting, hh interFace) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         S = interFace.sizeHud.getValue();
         boolean v3 = mc.field_1755 instanceof ChatScreen;
         if (!v3) {
            this.f429 = false;
            this.funMenuOpen = false;
            this.f478 = false;
         }

         boolean v4 = this.f444 != null || this.f443 != null && !this.f443.getTitle().isEmpty() && !this.f443.getTitle().equals("Нет трека");
         boolean v5 = iq.isPvp();
         long v6 = System.currentTimeMillis();
         if (v5 && !this.f455) {
            this.f457.set(0.0);
            this.f457.run(1.0, 0.25, ap.CUBIC_OUT);
            this.f455 = true;
         } else if (!v5 && this.f455) {
            this.f455 = false;
            this.f456 = v6;
            this.f457.set(0.0);
            this.f457.run(1.0, 0.25, ap.CUBIC_OUT);
         }

         boolean v8 = v5 || v6 - this.f456 < 5522737888165144834L + -5522737888165142334L;
         this.m471(interFace);
         boolean v9 = this.f452 != null && !this.f452.isExpired();
         boolean v10 = interFace.islandAlways.getValue() || v4 || v9 || v8 || v3;
         this.f423.update();
         this.f423.run(v10 ? 1.0 : 0.0, 0.16F, ap.CUBIC_OUT);
         float v11 = this.f423.get();
         dragSetting.active = v11 > 0.01F;
         if (!(v11 <= 0.001F)) {
            this.f424.update();
            this.f424.run(this.f429 ? 1.0 : 0.0, this.optSmoothness ? 0.2F : 0.4F, ap.CUBIC_OUT);
            float v12 = this.f424.get();
            this.f428.update();
            this.f428.run(this.funMenuOpen && v3 ? 1.0 : 0.0, 0.22F, ap.CUBIC_OUT);
            yt v13 = hu.sf_bold;
            yt v14 = hu.aeonik;
            MediaInfo v15 = v4 ? this.f443 : EMPTY;
            String v16 = v15.getTitle().isEmpty()
               ? (v3 ? "Музыка не играет" : "Нет трека")
               : v15.getTitle();
            String v17 = filterForbiddenText(v16);
            if (v17.isEmpty()) {
               v17 = v3 ? "Музыка не играет" : "Нет трека";
            }

            String result;
            if (v15.getArtist().isEmpty()) {
               result = "—";
            } else {
               result = v15.getArtist();
               
            }

            String v18 = filterForbiddenText(result);
            String v19 = v18 + "|" + v17;
            if (!v19.equals(this.f437)) {
               this.f437 = v19;
               this.f435 = "";
               this.f436 = "";
               this.f434 = 0.0F;
               this.f432 = 0.0F;
               this.f438 = "";
               this.f439 = System.currentTimeMillis();
            }

            long v20 = System.nanoTime();
            if (this.f449 == 0L) {
               this.f449 = v20;
            }

            double v22 = (double)(v20 - this.f449) / 1.0E9;
            this.f449 = v20;
            if (v22 < 0.0) {
               v22 = 0.0;
            }

            if (v22 > 0.1) {
               v22 = 0.1;
            }

            boolean v24 = v4 && v15.getPlaying();
            if (!v19.equals(this.f451)) {
               this.f451 = v19;
               this.f448 = (double)v15.getPosition();
               this.f450 = (double)v15.getPosition();
            } else {
               if (v24) {
                  this.f448 += v22;
               }

               double v25 = (double)v15.getPosition();
               if (v25 > 0.0 && v25 != this.f450) {

                  this.f450 = v25;
                  double v27 = v25 - this.f448;
                  if (Math.abs(v27) > 1.5) {
                     this.f448 = v25;
                  } else {
                     this.f448 += v27 * 0.15;
                  }
               }

               if (!v24 && v15.getPosition() > 0L) {
                  this.f448 = (double)v15.getPosition();
               }
            }

            float v68 = (float)Math.max(1L, v15.getDuration());
            float v26 = (float)MathHelper.method_15350(this.f448, 0.0, (double)v68);
            vq.xq v69 = v4 ? vq.getLyricsData(v18, v17, v68) : null;
            this.f431 = v69 != null && !v69.isNotFound() && !v69.isEmpty();
            vq.yq v28 = null;
            if (v4 && interFace.islandLyrics.getValue()) {
               v28 = vq.getLyricsView(v18, v17, v26, v68);
            }

            if (v28 != null && v28.isSynced()) {
               boolean v80 = true;
            } else {
               boolean v79 = false;
            }

            String v30 = v28 != null ? v28.currentLine() : null;
            String v31 = this.f431
                  && v30 != null
                  && !v30.isEmpty()
                  && !v30.equals("· · ·")
                  && !v30.equals("Загрузка текста...")
               ? filterForbiddenText(v30)
               : "";
            if (!v31.equals(this.f435)) {
               this.f436 = this.f435;
               this.f435 = v31;
               this.f426.set(0.0);
               this.f426.run(1.0, 0.3F, ap.CUBIC_OUT);
               this.f434 = 0.0F;
            }

            this.f426.update();
            float v32 = this.f426.get();
            boolean v33 = v28 != null && v28.isSynced() && v28.isSinging() && v24 && !v31.isEmpty();
            float v34 = v33 ? v28.lineProgress() : 0.0F;
            this.f434 = this.f434 + (v34 - this.f434) * (this.optSmoothness ? 0.4F : 0.7F);
            boolean v35 = interFace.islandLyrics.getValue();
            this.f425.update();
            this.f425.run(this.f429 && v35 && this.f431 ? 1.0 : 0.0, this.optSmoothness ? 0.2F : 0.4F, ap.CUBIC_OUT);
            float v36 = this.f425.get();
            float v37 = 2.0F;
            float v38 = (float)mc.method_22683().method_4495();
            float v39 = v37 / v38;
            float v40 = (float)mc.method_22683().method_4486() / v39;
            float v41 = 21.0F * S;
            float v42 = 68.0F * S;
            float v43 = 142.0F * S;
            float v44 = MathHelper.method_16439(v36, v42, v43);
            float v45 = MathHelper.method_16439(v36, 168.0F * S, 185.0F * S);
            float v46 = 12.0F * S;
            float v47 = 7.5F * S;
            String v48;
            if (v8) {
               if (!v5) {
                  v48 = "Вы вышли из боя";
               } else {
                  String v49 = iq.getPvpTitle();
                  v48 = v49 != null && !v49.isEmpty() ? v49 : "Вы вошли в режим боя";
               }
            } else if (v9) {
               v48 = this.f454;
            } else if (this.f431 && !v31.isEmpty() && interFace.islandLyrics.getValue()) {
               v48 = v31;
            } else if (v4) {
               v48 = !v18.equals("—") && !v18.isEmpty() ? v18 + " — " + v17 : v17;
            } else {
               v48 = "Dynamic Island";
            }

            if (v48.equals(this.f438) && this.f440 != null) {
               if (Math.abs(this.f440.textSize - v47) > 0.01F) {
                  long v71 = this.f440.startTime;
                  float v51 = this.f440.scrollOffset;
                  this.f440 = new jh.kh(v13, v47, v48, v71);
                  this.f440.scrollOffset = v51;
               }
            } else {
               this.f438 = v48;
               v6 = System.currentTimeMillis();
               if (this.f440 != null) {
                  this.f440.exitTime = v6;
                  this.f441 = this.f440;
               }

               long v70 = this.f441 != null ? -5752311385881602653L + 5752311385881602743L : 0L;
               this.f440 = new jh.kh(v13, v47, v48, v6 + v70);
               this.f439 = v6;
            }

            float v72 = v13.getWidth(v48, v47);
            float v50;
            if (v8) {
               v50 = Math.max(72.0F * S, v72 + 36.0F * S);
            } else if (v9) {
               v50 = Math.max(75.0F * S, v72 + 32.0F * S);
            } else if (v4) {
               float v73 = 72.0F * S;
               float v52 = Math.min(162.0F * S, v40 * 0.42F);
               float v53 = v72 + 41.5F * S;
               v50 = MathHelper.method_15363(v53, v73, v52);
            } else {
               v50 = Math.max(75.0F * S, Math.min(v72 + 36.0F * S, 162.0F * S));
            }

            float v74 = MathHelper.method_16439(v12, v50, v45);
            float v75 = MathHelper.method_16439(v12, v41, v44);
            float v76 = this.optSmoothness ? 0.22F : 0.45F;
            if (this.f432 <= 0.01F) {
               this.f432 = v74;
            } else {
               this.f432 = this.f432 + (v74 - this.f432) * v76;
            }

            if (this.f433 <= 0.01F) {
               this.f433 = v75;
            } else {
               this.f433 = this.f433 + (v75 - this.f433) * v76;
            }

            float v54 = this.f432;
            float v55 = this.f433;
            float v56 = (v40 - v54) / 2.0F;
            float v57 = 6.0F * S;
            dragSetting.position.set(v56, v57);
            dragSetting.size.set(v54, v55);
            this.m465(v56, v57, v54, v41, v11, v13);
            float v58 = MathHelper.method_16439(v12, v41 / 2.0F, 16.0F * S);
            float v59 = this.optFullAlpha ? 1.0F : 0.92F * v11;
            if (this.optGlow) {
               int v60 = v8
                  ? up.getColor(220, 30, 40, (int)(140.0F * v11))
                  : up.getColor(0, 0, 0, (int)(140.0F * v11));
               vr.glow(v56 - 2.0F * S, v57 - 2.0F * S, v54 + 4.0F * S, v55 + 4.0F * S, v60, v58, 10.0F * S, 0.6F);
            }

            vr.blur(v56, v57, v54, v55, v11, v58, up.replAlpha(up.background(), v59));
            if (this.optFullAlpha) {
               vr.rect(v56, v57, v54, v55, up.replAlpha(up.background(), v59), v58);
            }

            int v77 = v8
               ? up.getColor(255, 60, 70, (int)(120.0F * v11))
               : up.getColor(255, 255, 255, (int)(28.0F * v11));
            vr.outline(v56, v57, v54, v55, 0.6F * S, v77, v58);
            if (this.optScissor) {
               dt.enable(v56, v57, v54, v55);
            }

            if (v12 < 0.95F) {
               this.m466(v56, v57, v54, v41, v13, v15, v48, v9, v8, v33, v32, v11 * (1.0F - v12), v24, v4, (float)v22);
            }

            if (v12 > 0.05F) {
               this.m467(v56, v57, v54, v55, v13, v14, v15, v17, v18, v26, v68, v12 * v11, v24, v36);
            }

            if (this.optScissor) {
               dt.disable();
            }

            if (this.f428.get() > 0.01F) {
               this.m468(v56, v57, v54, v55, v13, this.f428.get(), interFace);
            }
         }
      }
   }

   private String m473(int totalSeconds) {
      int v2 = totalSeconds / (60);
      int v3 = totalSeconds % (60);

      Object[] temp1 = new Object[]{v2, v3};

      return String.format("%d:%02d", temp1);
   }

   private void m469(yt font, float x, float y, String label, boolean checked, float alpha) {
      float v7 = 6.0F * S;
      vr.rect(
         x,
         y + 1.0F * S,
         v7,
         v7,
         checked
            ? up.getColor(255, 255, 255, (int)(230.0F * alpha))
            : up.getColor(255, 255, 255, (int)(30.0F * alpha)),
         1.8F * S
      );
      if (checked) {
         float temp2 = x + 1.2F * S;
         float temp3 = y + 1.5F * S;
         float temp4 = 4.8F * S;
         int temp5 = 12;
         int var10006 = 12;

         hu.sf_bold.draw("✓", temp2, temp3, temp4, up.getColor(temp5, var10006, 16, (int)(255.0F * alpha)));
      }

      font.draw(
         label,
         x + v7 + 4.5F * S,
         y + 1.2F * S,
         5.8F * S,
         checked
            ? up.getColor(245, 245, 250, (int)(245.0F * alpha))
            : up.getColor(160, 160, 175, (int)(200.0F * alpha))
      );
   }

   private void m475(
      yt font,
      jh.kh line,
      float textStartX,
      float textY,
      float visibleTextW,
      float h,
      float alpha,
      boolean isSinging,
      boolean isOutgoing,
      long nowMs,
      boolean hasNotif,
      boolean hasPvp
   ) {
      long v14 = nowMs - (isOutgoing ? line.exitTime : line.startTime);
      if (v14 > 0L) {
         int v16 = line.charStrings.length;
         if (v16 != 0) {
            float v17 = Math.min(16.0F, (isOutgoing ? 220.0F : 320.0F) / (float)Math.max(1, v16));
            float v18 = isOutgoing ? 180.0F : 240.0F;
            float v19 = this.optReverse ? -1.0F : 1.0F;
            float v20 = 6.0F * S * v19;

            for (int v21 = 0; v21 < line.wordStrings.length; v21++) {
               float v22 = line.wordScale[v21];
               float v23 = line.wordExtraX[v21];
               float v24 = (v22 - 1.0F) * 3.0F * S;
               float v25 = (line.wordStartX[v21] + line.wordEndX[v21]) / 2.0F;
               int v26 = line.wordStartCharIdx[v21];
               int v27 = line.wordEndCharIdx[v21];

               for (int v28 = v26; v28 <= v27; v28++) {
                  char v29 = line.text.charAt(v28);
                  if (!Character.isWhitespace(v29)) {
                     float v30 = (float)v14 - (float)v28 * v17;
                     if (!(v30 <= 0.0F) || isOutgoing) {
                        float v31 = isOutgoing
                           ? 1.0F - MathHelper.method_15363(v30 / v18, 0.0F, 1.0F)
                           : MathHelper.method_15363(v30 / v18, 0.0F, 1.0F);

                        if (!(v31 <= 0.001F)) {
                           float v32 = (float)ap.CUBIC_OUT.ease((double)v31);
                           float v33 = textStartX - line.scrollOffset + v23 + v25 + (line.charStartX[v28] - v25) * v22;
                           float v34 = (1.0F - v32) * (isOutgoing ? -v20 : v20);
                           float v35 = (v22 - 1.0F) * 0.4F * font.getHeight(line.textSize);
                           float v36 = textY - v35 - v24 + v34;
                           float v37 = line.textSize * v22;
                           float v38 = 1.0F;
                           if (!hasNotif && !hasPvp && line.totalWidth > visibleTextW) {
                              float v39 = v33 - textStartX;
                              float v40 = textStartX + visibleTextW - (v33 + line.charWidth[v28] * v22);
                              float v41 = 8.0F * S;
                              if (line.scrollOffset > 0.5F && v39 < v41) {
                                 v38 = Math.min(v38, MathHelper.method_15363(v39 / v41, 0.0F, 1.0F));
                              }

                              if (v40 < v41) {
                                 v38 = Math.min(v38, MathHelper.method_15363(v40 / v41, 0.0F, 1.0F));
                              }
                           }

                           if (!(v38 <= 0.001F)) {
                              float v47;
                              if (isSinging && !hasNotif && !hasPvp) {
                                 float v48 = MathHelper.method_15363(
                                    (line.karaokeHeadX - line.charStartX[v28]) / Math.max(0.5F, line.charWidth[v28] * 0.55F), 0.0F, 1.0F
                                 );
                                 v47 = 0.42F + 0.58F * v48;
                              } else {
                                 v47 = 1.0F;
                              }

                              float v49 = alpha * v47 * v32 * v38;
                              if (hasPvp) {
                                 v42 = 240;
                                 v43 = 240;
                              } else if (!isSinging && isOutgoing) {
                                 v50 = 180;
                                 v42 = 180;
                                 v43 = 195;
                              }

                              int v51 = up.getColor(v50, v42, v43, (int)(255.0F * v49));

                              int v44 = v51;
                              font.draw(line.charStrings[v28], v33, v36, v37, v44);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private synchronized ExecutorService m463() {

      if (this.f447 != null) {

         boolean v4 = this.f447.isShutdown();

         if (!v4) {
            return this.f447;
         }
      }

      this.f447 = Executors.newSingleThreadExecutor(r -> {
         Thread var10000x = new Thread;

         var10000x./* $VF: Unable to resugar constructor */<init>(r, "dynamic-island-media");
         Thread v1x = var10000x;

         v1x.setDaemon(true);
         return v1x;
      });
      return this.f447;
   }

   private void m470(float cx, float cy, float alpha, boolean isPlaying, int numBars) {
      double v6 = (double)(System.currentTimeMillis() % (8909069315742410326L + -8909069315732410326L)) * 0.001;
      float v8 = 1.3F * S;
      float v9 = 1.2F * S;
      float v10 = (float)numBars * v8 + (float)(numBars - 1) * v9;
      float v11 = cx - v10 / 2.0F;
      float v12 = (numBars == 4 ? 9.5F : 8.5F) * S;
      int v13 = this.f445 != null && this.f445 != nh.OTHER
         ? this.f445.getColor(alpha)
         : up.getColor(255, 125, 165, (int)(245.0F * alpha));

      for (int v14 = 0; v14 < numBars; v14++) {
         float v15;
         if (isPlaying && this.optWave) {
            double v16 = 4.2 + (v14 == 0 ? 0.8 : (v14 == 1 ? 2.2 : (v14 == 2 ? 1.4 : 3.0)));
            double v18 = 8.6 + (v14 == 0 ? 1.7 : (v14 == 1 ? 3.4 : (v14 == 2 ? 2.1 : 4.0)));
            double v20 = (double)v14 * 1.85;
            double v22 = Math.sin(v6 * v16 + v20) * 0.5 + 0.5;
            double v24 = Math.sin(v6 * v18 + v20 * 1.4) * 0.5 + 0.5;
            double v26 = Math.cos(v6 * v16 * 0.6 + (double)v14 * 0.8) * 0.5 + 0.5;
            v15 = (float)(v22 * 0.5 + v24 * 0.3 + v26 * 0.2);
         } else {
            v15 = 0.0F;
         }

         float v29 = 2.0F * S;
         float v17 = v29 + v15 * (v12 - v29);
         float v30 = v8 / 2.0F;
         float result = v11 + (float)v14 * (v8 + v9);
         float temp1 = cy - v17 / 2.0F;

         vr.rect(result, temp1, v8, v17, v13, v30);
      }
   }

   private void m464(float mx, float my, hh interFace) {
      float v4 = 11.5F * S;
      float v5 = this.f480 + 16.0F * S;
      if (fq.isHovered(mx, my, this.f479 + 4.0F * S, v5, this.f481 - 8.0F * S, v4)) {
         this.optSmoothness = !this.optSmoothness;
      } else {
         v5 += v4;
         if (fq.isHovered(mx, my, this.f479 + 4.0F * S, v5, this.f481 - 8.0F * S, v4)) {
            this.optFullAlpha = !this.optFullAlpha;
         } else {
            v5 += v4;
            if (fq.isHovered(mx, my, this.f479 + 4.0F * S, v5, this.f481 - 8.0F * S, v4)) {
               this.optWave = !this.optWave;
            } else {
               v5 += v4;
               if (fq.isHovered(mx, my, this.f479 + 4.0F * S, v5, this.f481 - 8.0F * S, v4)) {

                  this.optGlow = !this.optGlow;
               } else {
                  v5 += v4;
                  if (fq.isHovered(mx, my, this.f479 + 4.0F * S, v5, this.f481 - 8.0F * S, v4)) {
                     this.optReverse = !this.optReverse;
                  } else {
                     v5 += v4;
                     if (fq.isHovered(mx, my, this.f479 + 4.0F * S, v5, this.f481 - 8.0F * S, v4)) {
                        this.optScissor = !this.optScissor;
                     } else {
                        v5 += v4;
                        if (fq.isHovered(mx, my, this.f479 + 4.0F * S, v5, this.f481 - 8.0F * S, v4)) {
                           this.optExplicit = !this.optExplicit;
                        } else {
                           v5 += v4;
                           if (fq.isHovered(mx, my, this.f479 + 4.0F * S, v5, this.f481 - 8.0F * S, v4)) {
                              interFace.islandLyrics.set(!interFace.islandLyrics.getValue());
                              this.f430 = interFace.islandLyrics.getValue();
                           } else {
                              v5 += v4;

                              if (fq.isHovered(mx, my, this.f479 + 4.0F * S, v5, this.f481 - 8.0F * S, v4)) {
                                 this.optShowSource = !this.optShowSource;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class kh {
      final float[] wordEndX;
      final String[] wordStrings;
      final String[] charStrings;
      final int[] wordEndCharIdx;
      long exitTime;
      float karaokeHeadX;
      float maxScroll;
      final float[] charWidth;
      float totalExtraWidth = 0.0F;
      final String text;
      final float textSize;
      final float[] charStartX;
      final int[] wordStartCharIdx;
      final float[] wordExtraX;
      float scrollOffset = 0.0F;
      final float[] wordScale;
      final float totalWidth;
      final float[] wordStartX;
      final long startTime;

      public int getActiveWordIndex(float lineProgress) {
         if (this.wordStrings.length == 0) {
            return 0;
         } else {
            float v2 = MathHelper.method_15363(lineProgress, 0.0F, 1.0F) * (float)this.charStrings.length;
            int result = this.wordStrings.length - 1;

            for (int v3 = result; v3 >= 0; v3--) {
               if (v2 >= (float)this.wordStartCharIdx[v3]) {
                  return v3;
               }
            }

            return 0;
         }
      }

      public void update(float lineProgress, boolean isSinging, float visibleWidth, float dt, float S, boolean optSmoothness) {
         if (this.charStrings.length != 0) {
            int v7 = this.getActiveWordIndex(lineProgress);
            float v8 = (optSmoothness ? 11.0F : 18.0F) * dt;
            float v9 = Math.min(1.0F, v8);

            for (int v10 = 0; v10 < this.wordScale.length; v10++) {
               float v11 = v10 == v7 && isSinging ? 1.13F : 1.0F;
               this.wordScale[v10] = this.wordScale[v10] + (v11 - this.wordScale[v10]) * v9;
            }

            float v15 = 0.0F;

            for (int v16 = 0; v16 < this.wordScale.length; v16++) {
               float v12 = (this.wordScale[v16] - 1.0F) * (this.wordEndX[v16] - this.wordStartX[v16] + 4.0F * S);
               this.wordExtraX[v16] = v15 + v12 / 2.0F;
               v15 += v12;
            }

            this.totalExtraWidth = v15;
            this.maxScroll = Math.max(0.0F, this.totalWidth - visibleWidth);
            if (this.maxScroll <= 0.5F) {
               this.scrollOffset = this.scrollOffset - this.scrollOffset * Math.min(1.0F, dt * 9.0F);
            } else {
               float v17 = Math.max(this.maxScroll, this.totalWidth + this.totalExtraWidth - visibleWidth);
               float v19 = MathHelper.method_15363(this.wordEndX[v7] + this.wordExtraX[v7] - visibleWidth * 0.6F, 0.0F, v17);
               if (v19 > this.scrollOffset) {
                  float temp2 = v19 - this.scrollOffset;
                  float temp4 = -9.0F * dt;

                  this.scrollOffset = this.scrollOffset + temp2 * (1.0F - (float)Math.exp((double)temp4));
               } else {
                  this.scrollOffset = this.scrollOffset + (v19 - this.scrollOffset) * (1.0F - (float)Math.exp((double)(-4.0F * dt)));
               }
            }

            if (isSinging) {
               float v18 = MathHelper.method_15363(lineProgress, 0.0F, 1.0F) * (float)this.charStrings.length;
               if (v18 >= (float)this.charStrings.length) {
                  this.karaokeHeadX = this.totalWidth;
               } else {
                  int v20 = (int)v18;
                  float v13 = v18 - (float)v20;
                  this.karaokeHeadX = this.charStartX[v20] + this.charWidth[v20] * v13;
               }
            } else {
               this.karaokeHeadX = this.totalWidth;
            }
         }
      }

      public kh(yt font, float textSize, String text, long startTime) {
         this.maxScroll = 0.0F;
         this.karaokeHeadX = 0.0F;
         this.text = text != null ? text : "";
         this.textSize = textSize;
         this.startTime = startTime;
         int v6 = this.text.length();
         this.charStrings = new String[v6];
         this.charStartX = new float[v6];
         this.charWidth = new float[v6];
         boolean v8 = false;

         for (int v9 = 0; v9 < v6; v9++) {
            boolean v10 = Character.isWhitespace(this.text.charAt(v9));
            if (!v10 && !v8) {
               v7++;
            }

            v8 = !v10;
         }

         this.wordStrings = new String[v7];
         this.wordStartCharIdx = new int[v7];
         this.wordEndCharIdx = new int[v7];
         this.wordStartX = new float[v7];
         this.wordEndX = new float[v7];
         this.wordScale = new float[v7];
         this.wordExtraX = new float[v7];
         Arrays.fill(this.wordScale, 1.0F);
         v8 = false;

         for (int v16 = 0; v16 < v6; v16++) {
            char v11 = this.text.charAt(v16);
            this.charStrings[v16] = String.valueOf(v11);
            this.charStartX[v16] = font.getWidth(this.text.substring(0, v16), textSize);
            float v12 = v16 + 1 < v6 ? font.getWidth(this.text.substring(0, v16 + 1), textSize) : font.getWidth(this.text, textSize);
            this.charWidth[v16] = Math.max(0.1F, v12 - this.charStartX[v16]);
            boolean v13 = Character.isWhitespace(v11);
            if (!v13) {
               if (!v8) {
                  v15++;
                  this.wordStartCharIdx[v15] = v16;
                  this.wordStartX[v15] = this.charStartX[v16];
               }

               this.wordEndCharIdx[v15] = v16;
               this.wordEndX[v15] = v12;
            }

            v8 = !v13;
         }

         for (int v17 = 0; v17 < v7; v17++) {
            this.wordStrings[v17] = this.text.substring(this.wordStartCharIdx[v17], this.wordEndCharIdx[v17] + 1);
         }

         this.totalWidth = font.getWidth(this.text, textSize);
      }

      
   }
}
