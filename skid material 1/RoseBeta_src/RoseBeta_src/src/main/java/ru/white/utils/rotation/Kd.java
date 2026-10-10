package ru.white.utils.rotation;

import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.misc.FakePlayer;
import ru.white.utils.IMinecraft;
import ru.white.utils.ChatUtil;
import ru.white.event.Event;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventRenderModel;

@Environment(EnvType.CLIENT)
public class Kd implements IMinecraft {
   private final List<float[]> f147;
   private float f142;
   private final List<double[]> f127;
   private md f120;
   private OtherClientPlayerEntity f152;
   private float f130;
   private int f150;
   private float f140;
   private final List<double[]> f126;
   private kd.ld f121;
   private float f146;
   private final AtomicBoolean f124;
   private static kd instance;
   private int f144;
   private final List<float[]> f148;
   private boolean f154;
   private final List<md.nd> f149;
   private LivingEntity f151;
   private float f135;
   private float f129;
   private float f145;
   private Thread f125;
   private float f131;
   private float f134;
   private float f133;
   private int f123;
   private float f139;
   private float f132;
   private Vec3d f153;
   private final Map<String, md> f119 = new LinkedHashMap<>();
   private float f137;
   private boolean f141;
   private float f136;
   private float f138;
   private float f143;
   private float f128;
   private long f122;

   public kd.ld getRecordType() {

      return this.f121;
   }

   public float[] nextMove(float yawErr, float pitchErr, float sinceAttack) {
      return this.f120 == null ? null : this.f120.nextMove(yawErr, pitchErr, sinceAttack);
   }

   public boolean select(String name) {
      if (name != null && !name.isBlank()) {
         this.refreshModels();
         md v2 = this.f119.get(name.toLowerCase());
         if (v2 == null) {
            v2 = md.load(name);
         }

         if (v2 == null) {
            return false;
         } else {
            this.f119.put(name.toLowerCase(), v2);
            this.f120 = v2;
            md.saveCurrentName(v2.name);
            return true;
         }
      } else {
         return false;
      }
   }

   public md.od predictDetailed(float yawErr, float pitchErr) {
      if (this.f120 != null && this.f120.samples > 0) {
         return this.f120.predictDetailed(yawErr, pitchErr);
      } else {
         md.od result = new md.od;

         result./* $VF: Unable to resugar constructor */<init>(90.0F, 90.0F, 0.5F, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, false);
         return result;
      }
   }

   public void stopTrain() {

      this.stopRecording();
   }

   public Collection<md> all() {
      this.refreshModels();
      int v1 = -2087041284;
      int temp1 = v1 * (v1 + 1);

      return this.f119.values();
   }

   public md.od predictDetailed(double[] features) {

      if (this.f120 == null) {
         md.od result = new md.od;

         result./* $VF: Unable to resugar constructor */<init>(
            45.0F, 30.0F, 0.5F, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, false
         );
         return result;
      } else {
         return this.f120.predictDetailed(features);
      }
   }

   public void startLiveRecord(String name) {
      if (name != null && !name.isBlank()) {
         if (!this.f119.containsKey(name.toLowerCase())) {
            this.create(name);
         } else {
            this.select(name);
         }
      }

      if (this.f120 == null) {
         zp.addChatMessage(
            "§c[Neuro] Сначала укажи или создай модель: §f.neuro create <имя>"
         );
      } else if (this.f121 != kd.ld.NONE) {
         zp.addChatMessage(
            "§7[Neuro] Запись уже активна. Напиши §f.neuro stoprec §7для завершения"
         );
      } else {
         this.m158();
         this.f121 = kd.ld.LIVE_DUELS;
         String v2 = this.f120.name;
         StringBuilder result = new StringBuilder();
         int v4 = 60201551;

         zp.addChatMessage(
            result.append(
                  "§a[Neuro] §7Запись реального PvP для модели §f"
               )
               .append((Object)v2)
               .append(
                  "§7 запущена! Сражайся в дуэлях. §f.neuro stoprec §7для сохранения"
               )
               .toString()
         );
      }
   }

   private static Vec3d m165(float yaw, float pitch) {
      double v2 = Math.toRadians((double)yaw);
      double v4 = Math.toRadians((double)pitch);
      double v6 = Math.cos(v4);
      return new Vec3d(-Math.sin(v2) * v6, -Math.sin(v4), Math.cos(v2) * v6);
   }

   private void m157() {
      this.refreshModels();
   }

   public void startTrain() {

      this.startDummyTrain(null);
   }

   public boolean delete(String name) {
      md v2 = this.f119.remove(name.toLowerCase());

      try {
         Files.deleteIfExists(md.DIR.resolve(name + ".neuro"));
         Path result = md.DIR;
         StringBuilder temp1 = new StringBuilder();

         Files.deleteIfExists(result.resolve(temp1.append((Object)name).append(".onnx").toString()));
      } catch (Exception v8) {
      }

      if (v2 != null && this.f120 == v2) {

         this.f120 = this.f119.values().stream().max(Comparator.comparingInt(model -> model.samples)).orElse(null);
         md.saveCurrentName(this.f120 == null ? null : this.f120.name);
      }

      return v2 != null;
   }

   private static float m167(Vec3d aim) {
      double result = Math.toDegrees(Math.atan2(aim.field_1351, Math.hypot(aim.field_1352, aim.field_1350)));

      float v4 = (float)MathHelper.method_15350(-result, -90.0, 90.0);

      return v4;
   }

   public boolean load(String name) {
      if (name != null && !name.isBlank()) {
         this.refreshModels();
         md v2 = md.load(name);
         if (v2 != null) {
            this.f119.put(name.toLowerCase(), v2);

            this.f120 = v2;

            md.saveCurrentName(v2.name);
            return true;
         } else {
            return this.select(name);
         }
      } else {
         return false;
      }
   }

   private void m163() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.f151 != null && this.f151.method_5805() && !this.f151.method_31481() && this.f151 != mc.field_1724) {

            if (mc.field_1724.method_5858(this.f151) < 64.0) {
               return;
            }
         }

         if (mc.field_1765 instanceof EntityHitResult v1 && v1.method_17782() instanceof LivingEntity v2 && v2 != mc.field_1724 && v2.method_5805()) {
            this.f151 = v2;
            return;
         }

         double v10 = 36.0;
         LivingEntity v12 = null;

         for (Entity v5 : mc.field_1687.method_8335(mc.field_1724, mc.field_1724.method_5829().method_1014(6.5))) {
            if (v5 instanceof LivingEntity) {
               LivingEntity v6 = (LivingEntity)v5;
               if (v6 != mc.field_1724 && v6.method_5805()) {
                  double v7 = mc.field_1724.method_5858(v6);
                  if (v7 < v10) {
                     v10 = v7;
                     v12 = v6;
                  }
               }
            }
         }

         if (v12 != null) {
            this.f151 = v12;
         }
      }
   }

   public float[] predict(float yawErr, float pitchErr) {

      if (this.f120 != null) {

         if (this.f120.samples > 0) {
            return this.f120.predict(yawErr, pitchErr);
         }
      }

      return new float[]{90.0F, 90.0F};
   }

   private void m164(LivingEntity target) {
      if (mc.field_1724 != null) {
         Vec3d v2 = mc.field_1724.method_33571();
         Vec3d v3 = target.method_5829().method_1005();
         Vec3d v4 = v3.method_1020(v2);
         float v5 = (float)MathHelper.method_15338(Math.toDegrees(Math.atan2(v4.field_1350, v4.field_1352)) - 90.0);
         float v6 = (float)(-Math.toDegrees(Math.atan2(v4.field_1351, Math.hypot(v4.field_1352, v4.field_1350))));
         float v7 = this.f128;
         float v8 = this.f129;
         float v9 = MathHelper.method_15393(v5 - v7);
         float v10 = v6 - v8;
         float v11 = mc.field_1724.method_36454();
         float v12 = mc.field_1724.method_36455();
         float v13 = MathHelper.method_15393(v11 - v7);
         float v14 = v12 - v8;
         this.f128 = v11;
         this.f129 = v12;
         float v15 = (float)Math.hypot((double)v13, (double)v14);
         Box v16 = target.method_5829();
         boolean v17 = v16.method_992(v2, v2.method_1019(m165(v11, v12).method_1021(6.0))).isPresent();
         boolean v18 = v15 > 0.02F || v17 || this.f150 <= 15;
         boolean v19 = v15 < 0.03F && this.f142 < 0.02F && !v17 && this.f150 > 20;
         if (v18 && !v19) {
            double v20 = v2.method_1022(v3);
            float v22 = (float)(target.method_23318() - mc.field_1724.method_23318());
            Vec3d v23 = target.method_18798();
            Vec3d v24 = mc.field_1724.method_18798();
            double v25 = v4.method_1033();
            float v27 = 0.0F;
            float v28 = 0.0F;
            if (v25 > 0.01) {
               Vec3d v29 = v4.method_1021(1.0 / v25);
               Vec3d v30 = new Vec3d(-v29.field_1350, 0.0, v29.field_1352);
               v27 = (float)(v23.method_1026(v30) / v25 * 20.0);
               v28 = (float)(v23.field_1351 / v25 * 20.0);
            }

            float v42 = mc.field_1724.method_7261(0.5F);
            float v43 = this.f144 > 1 && this.f142 > 0.001F
               ? MathHelper.method_15363((float)Math.hypot((double)v13, (double)v14) / this.f142, 0.0F, 1.0F)
               : 1.0F;
            float v31 = this.f143;
            this.f142 = 0.0F;
            this.f143 = 0.0F;
            this.f144 = 0;
            Optional v32 = v16.method_992(v2, v2.method_1019(m165(v11, v12).method_1021(6.0)));
            if (v32.isPresent()) {
               Vec3d v33 = (Vec3d)v32.get();
               double v34 = v16.field_1320 - v16.field_1323;

               double v36 = v16.field_1325 - v16.field_1322;
               double v38 = v16.field_1324 - v16.field_1321;
               if (v34 > 0.001) {
                  this.f136 = (float)MathHelper.method_15350((v33.field_1352 - v16.field_1323) / v34, 0.05, 0.95);
               }

               if (v36 > 0.001) {
                  this.f137 = (float)MathHelper.method_15350((v33.field_1351 - v16.field_1322) / v36, 0.05, 0.95);
               }

               if (v38 > 0.001) {
                  this.f138 = (float)MathHelper.method_15350((v33.field_1350 - v16.field_1321) / v38, 0.05, 0.95);
               }

               if (this.f149.size() < 50000 && (v15 > 0.03F || this.f150 <= 4)) {
                  this.f149.add(new md.nd(this.f136, this.f137, this.f138, false, this.f123, v11, v12));
               }
            }

            float v44 = MathHelper.method_15363(v13 - this.f130, -1.2F, 1.2F);
            float v45 = MathHelper.method_15363(v14 - this.f131, -0.8F, 0.8F);
            double[] v35 = md.features(
               v9,
               v10,
               this.f130,
               this.f131,
               this.f132,
               this.f133,
               this.f134,
               this.f135,
               this.f136,
               this.f137,
               this.f138,
               (float)v20,
               v22,
               v8,
               v6,
               v27,
               v28,
               (float)v23.field_1352,
               (float)v23.field_1351,
               (float)v23.field_1350,
               (float)v24.field_1351,
               v42,
               (float)this.f150,
               v43,
               v31
            );
            double[] v46 = new double[14];
            v46[0] = MathHelper.method_15350((double)(Math.abs(v13) / 180.0F), 0.0, 1.0);
            v46[1] = MathHelper.method_15350((double)(Math.abs(v14) / 120.0F), 0.0, 1.0);
            v46[2] = (double)this.f136;
            v46[3] = (double)this.f137;
            v46[4] = (double)this.f138;
            v46[5] = md.signedToUnit(v44, 1.2F);
            v46[6] = md.signedToUnit(v45, 0.8F);
            v46[7] = md.signedToUnit(v27 * 0.12F, 12.0F);
            v46[8] = md.signedToUnit(v28 * 0.12F, 8.0F);
            float v37 = v13 - this.f130;
            float v47 = v14 - this.f131;
            v46[9] = md.signedToUnit(v37, 35.0F);
            v46[10] = md.signedToUnit(v47, 22.0F);
            v46[11] = (double)v43;
            v46[12] = 0.5;
            v46[13] = 0.5;
            this.f126.add(v35);
            this.f127.add(v46);
            if (this.f150 <= 2) {
               this.f126.add(v35);
               this.f127.add(v46);
            }

            this.f147.add(new float[]{v44, v45, v15});
            this.f148.add(new float[]{v9, v10, v13, v14, (float)this.f150});
            this.f134 = v37;
            this.f135 = v47;
            this.f132 = this.f130;
            this.f133 = this.f131;
            this.f130 = v13;
            this.f131 = v14;

            this.f145 = v43;
            this.f146 = v31;
         } else {
            this.f130 = 0.0F;
            this.f131 = 0.0F;
            this.f132 = 0.0F;
            this.f133 = 0.0F;
            this.f134 = 0.0F;
            this.f135 = 0.0F;
         }
      }
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (this.f121 != kd.ld.NONE && mc.field_1724 != null && mc.field_1687 != null) {
         if (mc.field_1755 == null && mc.method_1569()) {
            this.f150 = Math.min(this.f150 + 1, 999);
            if (this.f121 == kd.ld.LIVE_DUELS) {
               this.m163();
            } else if (this.f121 == kd.ld.DUMMY_TRAIN) {
               this.m161();
               if ((this.f152 == null || this.f152.method_31481()) && gm.fakePlayer != null && gm.fakePlayer.method_5805()) {
                  this.f152 = gm.fakePlayer;
               }

               this.f151 = this.f152;
            }

            if (this.f151 != null && !this.f151.method_29504() && this.f151.method_5805()) {

               this.m164(this.f151);
            }
         } else {
            this.f128 = mc.field_1724.method_36454();
            this.f129 = mc.field_1724.method_36455();
            this.f130 = 0.0F;
            this.f131 = 0.0F;
            this.f142 = 0.0F;
            this.f143 = 0.0F;
            this.f144 = 0;
            this.f141 = false;
         }
      }
   }

   public void refreshModels() {
      try {
         if (!Files.isDirectory(md.DIR)) {
            return;
         }

         this.f119.entrySet().removeIf(entry -> {
            md v2 = entry.getValue();
            boolean v3 = Files.exists(v2.file());
            if (!v3 && this.f120 == v2) {
               this.f120 = null;
            }

            boolean v5x;
            if (!v3) {

               v5x = true;
            } else {
               v5x = false;
            }

            return v5x;
         });

         try (Stream v1 = Files.list(md.DIR)) {
            v1.filter(path -> {
               boolean result = path.getFileName().toString().endsWith(".neuro");
               int v1x = -127105152;

               int var10001x = v1x * (v1x + 1);

               return result;
            }).forEach(path -> {
               String v2 = path.getFileName().toString();
               String v3 = v2.substring(0, v2.length() - ".neuro".length());
               if (!this.f119.containsKey(v3.toLowerCase())) {
                  md v4x = md.load(v3);

                  if (v4x != null) {
                     this.f119.put(v3.toLowerCase(), v4x);
                  }
               }
            });
         }

         if (this.f120 == null && !this.f119.isEmpty()) {
            String v9 = md.loadCurrentName();
            if (v9 != null) {

               this.f120 = this.f119.get(v9.toLowerCase());
            }

            if (this.f120 == null) {
               this.f120 = this.f119.values().stream().max(Comparator.comparingInt(model -> model.samples)).orElse(null);
            }
         }
      } catch (Exception v8) {
      }
   }

   public boolean save(String name) {
      if (this.f120 == null) {
         zp.addChatMessage(
            "§c[Neuro] Модель не выбрана для сохранения. Сначала создай: §f.neuro create <имя>"
         );
         return false;
      } else if (name != null && !name.isBlank() && !name.equalsIgnoreCase(this.f120.name)) {
         md v2 = this.f120.copy(name);
         this.f119.put(name.toLowerCase(), v2);
         this.f120 = v2;
         v2.save();
         md.saveCurrentName(v2.name);
         int v4 = v2.samples;
         zp.addChatMessage(
            "§a[Neuro] §7Модель сохранена как §f"
               + name
               + "§7 (§f"
               + v4
               + "§7 сэмплов)"
         );
         return true;
      } else {
         this.f120.save();
         String v3 = this.f120.name;
         StringBuilder result = new StringBuilder()
            .append("§a[Neuro] §7Модель §f")
            .append((Object)v3);

         zp.addChatMessage(result.append("§7 сохранена").toString());
         return true;
      }
   }

   public boolean isRecording() {

      return this.f121 != kd.ld.NONE;
   }

   private void m160() {
      if (this.f152 != null) {
         int v3 = -1962982114;
         int temp1 = v3 * (v3 + 1);

         if (this.f154) {
            try {
               this.f152.method_31472();
            } catch (Exception v5) {
            }
         }
      }

      this.f152 = null;

      this.f154 = false;
   }

   private static float m166(Vec3d aim) {
      double result = -aim.field_1352;

      return (float)Math.toDegrees(Math.atan2(result, aim.field_1350));
   }

   public md getCurrent() {

      return this.f120;
   }

   private void m158() {
      this.f126.clear();
      this.f127.clear();
      if (mc.field_1724 != null) {
         this.f128 = mc.field_1724.method_36454();
         this.f129 = mc.field_1724.method_36455();
         this.f139 = this.f128;
         this.f140 = this.f129;
      }

      this.f130 = 0.0F;
      this.f131 = 0.0F;
      this.f132 = 0.0F;

      this.f133 = 0.0F;
      this.f134 = 0.0F;
      this.f135 = 0.0F;
      this.f136 = 0.5F;
      this.f137 = 0.5F;
      this.f138 = 0.5F;
      this.f141 = false;
      this.f142 = 0.0F;
      this.f143 = 0.0F;
      this.f144 = 0;
      this.f145 = 1.0F;
      this.f146 = 0.0F;
      this.f147.clear();
      this.f148.clear();
      this.f149.clear();
      this.f150 = 999;
      this.f123 = 0;
      this.f122 = System.currentTimeMillis();
      this.f151 = null;
   }

   @EventTarget
   public void onAttack(Event event) {
      if (this.f121 != kd.ld.NONE && mc.field_1724 != null) {
         this.f150 = 0;
         Entity v2 = event.getTarget();
         if (v2 instanceof LivingEntity v3) {
            this.f151 = v3;
            this.f123++;
            Vec3d v4 = mc.field_1724.method_33571();
            float v5 = mc.field_1724.method_36454();
            float v6 = mc.field_1724.method_36455();
            Box v7 = v3.method_5829();
            Vec3d v8 = m165(v5, v6).method_1021(6.0);
            Optional v9 = v7.method_992(v4, v4.method_1019(v8));
            float v10 = 0.5F;
            float v11 = 0.5F;

            float v12 = 0.5F;
            if (v9.isPresent()) {
               Vec3d v13 = (Vec3d)v9.get();
               double v14 = v7.field_1320 - v7.field_1323;
               double v16 = v7.field_1325 - v7.field_1322;
               double v18 = v7.field_1324 - v7.field_1321;
               if (v14 > 0.001) {

                  v10 = (float)MathHelper.method_15350((v13.field_1352 - v7.field_1323) / v14, 0.05, 0.95);
               }

               if (v16 > 0.001) {
                  v11 = (float)MathHelper.method_15350((v13.field_1351 - v7.field_1322) / v16, 0.05, 0.95);
               }

               if (v18 > 0.001) {
                  v12 = (float)MathHelper.method_15350((v13.field_1350 - v7.field_1321) / v18, 0.05, 0.95);
               }
            }

            this.f136 = v10;
            this.f137 = v11;
            this.f138 = v12;
            if (this.f149.size() < 50000) {
               this.f149.add(new md.nd(v10, v11, v12, true, this.f123, v5, v6));
            }

            if (this.f152 != null && v2.method_5628() == this.f152.method_5628()) {
               try {
                  mc.field_1724.method_5783(SoundEvents.field_14840, 1.0F, 1.0F);
               } catch (Exception v23) {
               }

               this.f152.field_6235 = 10;

               try {
                  this.f152.method_5879(0.0F);
               } catch (Exception v22) {
               }
            }
         }
      }
   }

   public float[] nextJitter(float speed) {
      if (this.f120 == null) {
         float[] result = new float[2];

         result[0] = 0.0F;
         result[1] = 0.0F;
         return result;
      } else {
         return this.f120.nextJitter(speed);
      }
   }

   public static kd get() {
      if (instance == null) {
         kd result = new kd;
         int v0 = -659187132;

         result./* $VF: Unable to resugar constructor */<init>();
         instance = result;
         Client.eventHandler().subscribe(instance);
      }

      return instance;
   }

   private void m161() {
      if (this.f152 != null && this.f154 && this.f153 != null && mc.field_1724 != null) {
         double result = -(mc.field_1724.method_23317() - this.f153.field_1352);
         double temp1 = mc.field_1724.method_23321();

         float v1 = (float)Math.toDegrees(Math.atan2(result, temp1 - this.f153.field_1350));
         this.f152.method_5808(this.f153.field_1352, this.f153.field_1351, this.f153.field_1350, v1, 0.0F);
         this.f152.method_18799(Vec3d.field_1353);
         this.f152.method_6033(20.0F);
      }
   }

   public void stopRecording(String targetName) {
      if (this.f121 == kd.ld.NONE) {
         String v11 = "§7[Neuro] Запись не активна";
         int v6 = -1206659817;

         zp.addChatMessage(v11);
      } else {
         kd.ld v2 = this.f121;
         this.f121 = kd.ld.NONE;
         if (v2 == kd.ld.DUMMY_TRAIN) {
            this.m160();
            this.f152 = null;
         }

         int v3 = this.f126.size();
         if (v3 == 0) {
            zp.addChatMessage(
               "§c[Neuro] Сэмплов не собрано (цель не была в прицеле/не атакована)"
            );
         } else {
            if (targetName != null && !targetName.isBlank()) {
               if (!this.f119.containsKey(targetName.toLowerCase())) {
                  this.create(targetName);
               } else {
                  this.select(targetName);
               }
            }

            if (this.f120 == null) {
               this.create("default");
            }

            this.f120.ensureModernShape();

            for (int v4 = 0; v4 < v3; v4++) {
               this.f120.recordSample(this.f126.get(v4), this.f127.get(v4));
            }

            this.f120.addMultiPoints(this.f149);
            this.f120.addStrokes(this.f147);
            this.f120.smoothStrokesByIndex();
            this.f120.addTrajectory(this.f148);
            this.f120.save();
            long result = (System.currentTimeMillis() - this.f122) / (5666748880170371986L + -5666748880170370986L);

            long v9 = result;
            String v10 = "§a[Neuro] §7Запись завершена! Сохранено в §f%s§7 (§f%d§7 сэмплов, §f%d§7 мультипоинтов, §f%d§7 штрихов, §f%d§7 тиков траектории за §f%d§7 сек, ударов: §f%d§7). Теперь напиши §f.neuro train %s";
            Object[] v12 = new Object[8];
            v12[0] = this.f120.name;
            v12[1] = v3;
            v12[2] = this.f149.size();
            v12[3] = this.f147.size();
            v12[4] = this.f148.size();
            v12[5] = v9;
            v12[6] = this.f123;
            v12[7] = this.f120.name;
            zp.addChatMessage(String.format(v10, v12));
         }
      }
   }

   public void startDummyTrain(String name) {
      if (name != null && !name.isBlank()) {
         if (!this.f119.containsKey(name.toLowerCase())) {
            this.create(name);
         } else {
            this.select(name);
         }
      }

      if (this.f120 == null) {
         zp.addChatMessage(
            "§c[Neuro] Сначала укажи или создай модель: §f.neuro create <имя>"
         );
      } else if (this.f121 != kd.ld.NONE) {
         zp.addChatMessage(
            "§7[Neuro] Запись уже активна. Напиши §f.neuro stoprec"
         );
      } else if (mc.field_1724 != null && mc.field_1687 != null) {
         this.m158();
         this.f121 = kd.ld.DUMMY_TRAIN;
         if (gm.fakePlayer != null && gm.fakePlayer.method_5805()) {
            this.f152 = gm.fakePlayer;
            this.f153 = this.f152.method_73189();
            this.f154 = false;
         } else {
            this.m159();
         }

         this.f151 = this.f152;
         String v2 = this.f120.name;
         StringBuilder result = new StringBuilder();

         zp.addChatMessage(
            result.append("§a[Neuro] §7Запись на боте для §f")
               .append((Object)v2)
               .append(
                  "§7 запущена! Бей бота. §f.neuro stoprec §7для сохранения"
               )
               .toString()
         );
      }
   }

   private Kd() {
      this.f121 = kd.ld.NONE;
      this.f122 = 0L;
      this.f123 = 0;
      this.f124 = new AtomicBoolean(false);
      this.f125 = null;
      this.f126 = new ArrayList<>();
      this.f127 = new ArrayList<>();
      this.f132 = 0.0F;
      this.f133 = 0.0F;
      this.f134 = 0.0F;
      this.f135 = 0.0F;
      this.f136 = 0.5F;
      this.f137 = 0.5F;
      this.f138 = 0.5F;
      this.f145 = 1.0F;
      this.f146 = 0.0F;
      this.f147 = new ArrayList<>();
      this.f148 = new ArrayList<>();
      this.f149 = new ArrayList<>();
      this.f150 = 999;
      this.f151 = null;
      this.m157();
   }

   public float[] nextMoveEx(float yawErr, float pitchErr, float sinceAttack) {

      return this.f120 == null ? null : this.f120.nextMoveEx(yawErr, pitchErr, sinceAttack);
   }

   private void m159() {
      double v1 = Math.toRadians((double)mc.field_1724.method_36454());
      double result = Math.sin(v1);

      double v3 = -result * 3.5;
      double v5 = Math.cos(v1) * 3.5;
      this.f153 = new Vec3d(mc.field_1724.method_23317() + v3, mc.field_1724.method_23318(), mc.field_1724.method_23321() + v5);
      this.f154 = true;
      GameProfile v7 = new GameProfile(UUID.fromString("32d0a964-137a-2c03-7cc9-df6700000001"), "NEURO_DUMMY");
      this.f152 = new OtherClientPlayerEntity(mc.field_1687, v7);
      this.f152.method_5808(this.f153.field_1352, this.f153.field_1351, this.f153.field_1350, 0.0F, 0.0F);
      this.f152.method_5673(EquipmentSlot.field_6169, new ItemStack(Items.field_22027));
      this.f152.method_5673(EquipmentSlot.field_6174, new ItemStack(Items.field_22028));
      this.f152.method_5673(EquipmentSlot.field_6172, new ItemStack(Items.field_22029));
      this.f152.method_5673(EquipmentSlot.field_6166, new ItemStack(Items.field_22030));
      this.f152.method_6122(Hand.field_5808, new ItemStack(Items.field_22022));
      this.f152.method_6122(Hand.field_5810, new ItemStack(Items.field_8288));
      this.f152.method_6033(20.0F);
      this.f152.method_5834(true);
      mc.field_1687.method_53875(this.f152);
   }

   @EventTarget
   public void onDisplay(EventReceivePacket e) {
      if (this.f121 != kd.ld.NONE && mc.field_1724 != null) {
         float v2 = mc.field_1724.method_36454();

         float v3 = mc.field_1724.method_36455();
         if (!this.f141) {
            this.f139 = v2;
            this.f140 = v3;

            this.f141 = true;
         } else {
            float v4 = Math.abs(MathHelper.method_15393(v2 - this.f139));
            float v5 = Math.abs(v3 - this.f140);
            float v6 = (float)Math.hypot((double)v4, (double)v5);

            this.f142 += v6;
            if (v6 > this.f143) {
               this.f143 = v6;
            }

            this.f144++;
            this.f139 = v2;
            this.f140 = v3;
         }
      }
   }

   private static double m162(double v, double target, double maxDelta) {
      return v < target ? Math.min(v + maxDelta, target) : Math.max(v - maxDelta, target);
   }

   public boolean hasTrainedModel() {
      if (this.f120 != null) {

         if (this.f120.samples > 0) {
            return true;
         }
      }

      return false;
   }

   public void startLearn(String name, int epochs) {
      if (name != null && !name.isBlank()) {

         this.select(name);
      }

      if (this.f120 == null) {
         zp.addChatMessage(
            "§c[Neuro] Модель не выбрана. Выбери или создай: §f.neuro create <имя>"
         );
      } else if (this.f124.get()) {
         zp.addChatMessage(
            "§c[Neuro] Обучение уже выполняется в фоне!"
         );
      } else {
         int v3 = Math.max(200, epochs <= 0 ? 2000 : epochs);
         List v4 = this.f120.getMemoryX();
         List v5 = this.f120.getMemoryY();
         if (v4.isEmpty()) {
            String v18 = this.f120.name;
            String v19 = this.f120.name;
            zp.addChatMessage(
               "§c[Neuro] В датасете модели §f"
                  + v19
                  + "§c нет сэмплов! Сначала запиши движения: §f.neuro rec "
                  + v18
            );
         } else {
            int v6 = v4.size();
            int result = 20;
            int v24 = 1000;
            int temp2 = v6 / (20);

            int v7 = Math.max(result, Math.min(v24, temp2));
            ArrayList v8 = new ArrayList(v6);
            ArrayList v9 = new ArrayList(v6);
            ArrayList v10 = new ArrayList(v7);
            ArrayList v11 = new ArrayList(v7);
            int v12 = Math.max(1, v6 / v7);

            for (int v13 = 0; v13 < v6; v13++) {
               if (v13 % v12 == 0 && v10.size() < v7) {
                  v10.add((double[])v4.get(v13));
                  v11.add((double[])v5.get(v13));
               } else {
                  v8.add((double[])v4.get(v13));
                  v9.add((double[])v5.get(v13));
               }
            }

            if (v8.isEmpty()) {
               v8.addAll(v10);
               v9.addAll(v11);
            }

            double[][] v23 = v8.toArray(new double[0][]);
            double[][] v14 = v9.toArray(new double[0][]);
            double[][] v15 = v10.toArray(new double[0][]);
            double[][] v16 = v11.toArray(new double[0][]);
            md v17 = this.f120;
            this.f124.set(true);
            zp.addChatMessage(
               String.format(
                  "§a[Neuro] §7Запуск обучения модели §f%s§7: §f%d§7 эпох, трейн §f%d§7 + вал §f%d§7...",
                  v17.name,
                  v3,
                  v23.length,
                  v15.length
               )
            );
            Thread v25 = new Thread;

            v25./* $VF: Unable to resugar constructor */<init>(
               () -> {
                  try {
                     long v8x = System.currentTimeMillis();
                     int var10x = v23.length;
                     int[] var11x = new int[var10x];
                     int var12x = 0;

                     while (var12x < var10x) {
                        var11x[var12x] = var12x++;
                     }

                     double v40 = 0.03;
                     double var14x = 0.88;
                     int var16x = Math.max(5, v3 / (20));
                     int var17x = Math.max(40, v3 / (6));
                     double var18x = Double.MAX_VALUE;
                     pd var20x = null;
                     int var21x = 0;
                     double var22x = 0.0;
                     double var24x = Double.MAX_VALUE;
                     int v26 = Math.max(1, v3 / (10));

                     for (int v27 = 1; v27 <= v3 && this.f124.get(); v27++) {
                        double v28;
                        if (v27 <= var16x) {
                           v28 = v40 * (0.2 + 0.8 * (double)v27 / (double)var16x);
                        } else {
                           double v30 = (double)(v27 - var16x) / (double)Math.max(1, v3 - var16x);
                           v28 = v40 * (0.05 + 0.475 * (1.0 + Math.cos(Math.PI * v30)));
                        }

                        boolean var10000x;
                        label124: {
                           var22x = v17.net.trainEpoch(v23, v14, v28, var14x, var11x);
                           if (v27 % v26 != 0) {

                              if (v27 != v3) {
                                 var10000x = false;
                                 break label124;
                              }
                           }

                           var10000x = true;
                        }

                        boolean v44 = var10000x;
                        if (v44 || v27 % Math.max(1, v26 / 2) == 0) {
                           var24x = v17.net.evalLoss(v15, v16);
                           if (var24x < var18x - 1.0E-7) {
                              var18x = var24x;
                              var21x = v27;
                              if (var20x == null) {
                                 var20x = v17.net.copy();
                              } else {
                                 var20x.copyFrom(v17.net);
                              }
                           } else if (v27 - var21x >= var17x) {
                              zp.addChatMessage(
                                 String.format(
                                    "§7[Neuro] Вал встал на эпохе §f%d§7 — останавливаюсь, возвращаю лучшие веса (вал §e%.4f§7)",
                                    v27,
                                    var18x
                                 )
                              );
                              break;
                           }
                        }

                        if (v44) {
                           int v31 = (int)Math.round((double)v27 / (double)v3 * 100.0);
                           zp.addChatMessage(
                              String.format(
                                 "§7[Neuro] §f%s§7: §a%d%%§7, трейн §e%.4f§7 / вал §e%.4f§7 (лучший вал §e%.4f§7)",
                                 v17.name,
                                 v31,
                                 var22x,
                                 var24x,
                                 var18x
                              )
                           );
                        }
                     }

                     if (var20x != null) {
                        v17.net.copyFrom(var20x);
                        var22x = v17.net.evalLoss(v23, v14);
                     }

                     v17.loss = var18x;
                     v17.samples = v6;
                     v17.save();
                     v17.exportOnnx();
                     long v43 = (System.currentTimeMillis() - v8x) / (-7786956229203508645L + 7786956229203509645L);
                     zp.addChatMessage(
                        String.format(
                           "§a[Neuro] §7Обучение модели §f%s§7 завершено за §f%dс§7! Вал-loss: §a%.5f§7. Активирована для ротации Neuro",
                           v17.name,
                           v43,
                           v17.loss
                        )
                     );
                  } catch (Exception v38) {
                     String v33 = v38.getMessage();
                     zp.addChatMessage(
                        "§c[Neuro] Ошибка во время обучения: " + v33
                     );
                     v38.printStackTrace();
                  } finally {
                     this.f124.set(false);
                  }
               },
               "Rose-Neuro-Trainer"
            );
            this.f125 = v25;
            this.f125.setDaemon(true);
            this.f125.start();
         }
      }
   }

   public boolean create(String name) {
      Map result = this.f119;
      String temp1 = name.toLowerCase();

      if (result.containsKey(temp1)) {
         return false;
      } else {
         md v2 = new md(name);
         this.f119.put(name.toLowerCase(), v2);

         this.f120 = v2;
         v2.save();
         md.saveCurrentName(name);
         return true;
      }
   }

   public boolean isLearning() {

      return this.f124.get();
   }

   public void stopRecording() {
      this.stopRecording(null);
      
   }

   @Environment(EnvType.CLIENT)
   public static enum ld {
      NONE,
      LIVE_DUELS,
      DUMMY_TRAIN;
      // $VF: synthetic field
      private static final kd.ld[] $VALUES = m156();
   }
}
