package ru.white.utils.rotation;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Md {
   public static final float MAX_PITCH_ACCEL = 22.0F;
   private int f158;
   public static final int HIDDEN = 48;
   public static final int OUTPUTS = 14;
   public static final float MAX_YAW_LEAD = 12.0F;
   public static final float MAX_PITCH_LEAD = 8.0F;
   public static final int TRAJ_SIZE = 5;
   public static final float MAX_YAW_JERK = 45.0F;
   public static final float MAX_YAW_SHAKE = 1.2F;
   public static final float MAX_PITCH_SHAKE = 0.8F;
   public double loss = -1.0;
   public static final int MAX_MULTIPOINTS = 50000;
   private static final int MAX_TRAJ = 60000;
   private static final Path CURRENT_FILE = md.DIR.resolve("current.txt");
   private final List<float[]> f157;
   private final List<float[]> f159;
   private static final int MAX_STROKES = 50000;
   private int f160;
   public static final float MAX_YAW_ACCEL = 35.0F;
   private final List<double[]> f155 = new ArrayList<>();
   private static final int MAX_MEMORY = 60000;
   public int samples;
   public static final float MAX_PEAK_SPEED = 90.0F;
   public static final int STROKE_SIZE = 3;
   public final String name;
   public static final int INPUTS = 30;
   public static final float MAX_PITCH_JERK = 30.0F;
   public static final Path DIR = Path.of("rose/client1_21_11/neuro");
   public static final float MAX_PITCH_SPEED = 120.0F;
   private final List<double[]> f156 = new ArrayList<>();
   private final List<md.nd> f161;
   public pd net;
   public static final float MAX_YAW_SPEED = 180.0F;

   public List<double[]> getMemoryY() {

      return this.f156;
   }

   private void m188(String data) {
      String[] v2 = data.split("\n");
      int v3 = -1;

      for (int v4 = 0; v4 < v2.length; v4++) {
         if (v2[v4].startsWith("MULTIPOINTS ")) {
            v3 = v4 + 1;
            break;
         }
      }

      if (v3 >= 0) {
         this.f161.clear();

         for (int v16 = v3; v16 < v2.length && this.f161.size() < 50000; v16++) {
            String v5 = v2[v16].trim();
            if (!v5.isEmpty()) {
               if (v5.startsWith("MEMORY ") || v5.startsWith("STROKES ") || v5.startsWith("TRAJ ")) {
                  break;
               }

               String[] v6 = v5.split("\\s+");
               if (v6.length >= 7) {
                  try {
                     float v7 = Float.parseFloat(v6[0]);
                     float v8 = Float.parseFloat(v6[1]);
                     float v9 = Float.parseFloat(v6[2]);

                     boolean v10 = "1".equals(v6[3]) || Boolean.parseBoolean(v6[3]);
                     int v11 = Integer.parseInt(v6[4]);
                     float v12 = Float.parseFloat(v6[5]);
                     float v13 = Float.parseFloat(v6[6]);
                     this.f161.add(new md.nd(v7, v8, v9, v10, v11, v12, v13));
                  } catch (Exception v15) {
                  }
               }
            }
         }
      }
   }

   public void exportOnnx() {
      try {
         Files.createDirectories(DIR);

         String v3 = this.name;
         Path v1 = DIR.resolve(v3 + ".onnx");
         StringBuilder v2 = new StringBuilder();
         v2.append("// ROSE Neuro ONNX Model Descriptor: ").append(this.name).append("\n");
         v2.append("version=1.0\n");
         v2.append("inputs=").append(30).append("\n");
         v2.append("hidden=").append(48).append("\n");
         v2.append("outputs=").append(14).append("\n");

         v2.append("samples=").append(this.samples).append("\n");
         v2.append("loss=").append(this.loss).append("\n");
         v2.append("weights=\n").append(this.net.serialize());
         Files.writeString(v1, v2.toString(), StandardCharsets.UTF_8);
      } catch (Exception v6) {
      }
   }

   public List<md.nd> getMultiPoints() {

      return this.f161;
   }

   private static boolean m176(float recYawErr, float recPitchErr, float yawErr, float pitchErr) {
      float v4 = (float)Math.hypot((double)recYawErr, (double)recPitchErr);
      float v5 = (float)Math.hypot((double)yawErr, (double)pitchErr);
      if (v5 < 6.0F) {
         return v4 < 14.0F;
      } else {

         return v4 > v5 * 0.35F && v4 < v5 * 3.0F + 6.0F;
      }
   }

   public md copy(String newName) {
      md v2 = new md(newName);
      v2.samples = this.samples;
      v2.loss = this.loss;

      if (this.net != null) {
         v2.net = new pd(this.net.in, this.net.hid, this.net.out);
         v2.net.copyFrom(this.net);
      }

      for (double[] v4 : this.f155) {

         v2.f155.add((double[])v4.clone());
      }

      for (double[] v12 : this.f156) {
         v2.f156.add((double[])v12.clone());
      }

      for (float[] v13 : this.f157) {
         v2.f157.add((float[])v13.clone());
      }

      for (float[] v14 : this.f159) {
         v2.f159.add((float[])v14.clone());
      }

      for (md.nd v15 : this.f161) {
         v2.f161.add(v15);
      }

      return v2;
   }

   public float[] nextMoveEx(float yawErr, float pitchErr, float sinceAttack) {
      if (this.f159.isEmpty()) {
         return null;
      } else {
         if (this.f160 >= 0 && this.f160 < this.f159.size()) {

            float[] v4 = this.f159.get(this.f160);
            if (v4[4] >= 0.0F && m176(v4[0], v4[1], yawErr, pitchErr) && m178(v4, yawErr, pitchErr, sinceAttack) <= m179(yawErr, pitchErr)) {
               this.f160++;
               float[] v13 = m177(v4, yawErr, pitchErr);
               return new float[]{v13[0], v13[1], v4[0], v4[1]};
            }
         }

         int v12 = -1;
         float v5 = Float.MAX_VALUE;
         int v6 = ThreadLocalRandom.current().nextInt(this.f159.size());

         for (int v7 = 0; v7 < this.f159.size(); v7++) {
            int v8 = (v6 + v7) % this.f159.size();
            float[] v9 = this.f159.get(v8);
            if (!(v9[4] < 0.0F) && m176(v9[0], v9[1], yawErr, pitchErr)) {
               float v10 = m178(v9, yawErr, pitchErr, sinceAttack);
               if (v10 < v5) {
                  v5 = v10;
                  v12 = v8;
               }

               if (v10 <= m179(yawErr, pitchErr) * 0.2F) {
                  v12 = v8;
                  break;
               }
            }
         }

         if (v12 >= 0 && !(v5 > m179(yawErr, pitchErr) * 4.0F)) {
            this.f160 = v12 + 1;
            float[] v14 = m177(this.f159.get(v12), yawErr, pitchErr);
            float[] v15 = this.f159.get(v12);
            return new float[]{v14[0], v14[1], v15[0], v15[1]};
         } else {
            this.f160 = -1;
            return null;
         }
      }
   }

   public float[] sampleMultiPoint(float yawErr, float pitchErr, int currentHitCount) {
      if (this.f161.isEmpty()) {
         return new float[]{0.5F, 0.55F, 0.5F};
      } else {
         ThreadLocalRandom v4 = ThreadLocalRandom.current();
         float v5 = (float)Math.hypot((double)yawErr, (double)pitchErr);
         Object v6 = new ArrayList();

         for (md.nd v8 : this.f161) {
            float v9 = Math.abs((float)Math.hypot((double)v8.yawAngle, (double)v8.pitchAngle) - v5);
            if (v9 <= 28.0F || this.f161.size() < 15) {
               v6.add(v8);
               
            }
         }

         if (v6.isEmpty()) {
            v6 = this.f161;
         }

         md.nd v15 = null;
         if (v4.nextFloat() < 0.75F) {
            ArrayList v16 = new ArrayList();

            for (md.nd v10 : v6) {
               if (v10.isHit) {
                  v16.add(v10);
               }
            }

            if (!v16.isEmpty()) {
               int v19 = currentHitCount % Math.max(1, v16.size() * 2);
               int v21 = 2147483647;

               for (md.nd v12 : v16) {
                  int v13 = Math.abs(v12.hitIndex - v19);
                  if (v13 < v21) {
                     v21 = v13;
                     v15 = v12;
                  }
               }
            }
         }

         if (v15 == null) {
            v15 = (md.nd)v6.get(v4.nextInt(v6.size()));
         }

         float v17 = (v4.nextFloat() - 0.5F) * 0.07F;
         float v20 = (v4.nextFloat() - 0.5F) * 0.07F;
         float v22 = (v4.nextFloat() - 0.5F) * 0.07F;
         return new float[]{m181(v15.x + v17), m181(v15.y + v20), m181(v15.z + v22)};
      }
   }

   private static Path m187(String name) {
      String temp1 = name + ".neuro";

      Path v5 = DIR.resolve(temp1);

      return v5;
   }

   private static float m175(float speed) {
      float result = Math.max(3.0F, speed * 0.6F);

      return result;
   }

   public int multiPointCount() {
      int v1 = 1046799742;
      int temp2 = v1 + 1;

      return this.f161.size();
   }

   public static Path getDir() {

      return DIR;
   }

   private void m191(String data) {
      String[] v2 = data.split("\n");
      int v3 = -1;

      for (int v4 = 0; v4 < v2.length; v4++) {
         if (v2[v4].startsWith("STROKES ")) {
            v3 = v4 + 1;
            break;
         }
      }

      if (v3 >= 0) {
         this.f157.clear();

         for (int v10 = v3; v10 < v2.length && this.f157.size() < 50000; v10++) {
            String v5 = v2[v10].trim();

            if (!v5.isEmpty()) {
               if (v5.startsWith("TRAJ ")) {
                  break;
               }

               String[] v6 = v5.split("\\s+");
               if (v6.length >= 3) {
                  try {
                     float v7 = Float.parseFloat(v6[2]);
                     if (!(v7 < 0.05F)) {
                        this.f157.add(new float[]{Float.parseFloat(v6[0]), Float.parseFloat(v6[1]), v7});
                     }
                  } catch (Exception v9) {
                  }
               }
            }
         }
      }
   }

   private static float[] m177(float[] s, float yawErr, float pitchErr) {
      float v3 = (float)Math.hypot((double)s[0], (double)s[1]);
      float v4 = (float)Math.hypot((double)yawErr, (double)pitchErr);
      float v5 = v3 < 0.5F ? 1.0F : m182(v4 / v3, 0.7F, 1.35F);

      return new float[]{s[2] * v5, s[3] * v5};
   }

   private String m183() {
      StringBuilder v1 = new StringBuilder();
      v1.append("MULTIPOINTS ").append(this.f161.size()).append((char)(10));

      for (md.nd v3 : this.f161) {
         StringBuilder result = v1.append(v3.x)
            .append((char)(32))
            .append(v3.y)
            .append((char)(32))
            .append(v3.z);

         result.append((char)(32))
            .append(v3.isHit ? 1 : 0)
            .append((char)(32))
            .append(v3.hitIndex)
            .append((char)(32))
            .append(v3.yawAngle)
            .append((char)(32))
            .append(v3.pitchAngle)
            .append((char)(10));
      }

      return v1.toString();
   }

   private String m184() {
      StringBuilder v1 = new StringBuilder();
      StringBuilder result = v1.append("STROKES ");
      int temp1 = this.f157.size();

      result.append(temp1).append((char)(10));
      int v4 = -1989246986;
      temp1 = v4 * (v4 + 1);

      for (float[] v3 : this.f157) {
         v1.append(v3[0])
            .append((char)(32))
            .append(v3[1])
            .append((char)(32))
            .append(v3[2])
            .append((char)(10));
      }

      return v1.toString();
   }

   private String m186() {
      StringBuilder v1 = new StringBuilder();
      v1.append("MEMORY ").append(this.f155.size()).append((char)(10));

      for (int v2 = 0; v2 < this.f155.size(); v2++) {
         for (double v6 : this.f155.get(v2)) {
            v1.append(v6).append((char)(32));
         }

         v1.append("| ");
         double[] v11 = this.f156.get(v2);
         int v12 = v11.length;

         for (int v13 = 0; v13 < v12; v13++) {

            double v14 = v11[v13];
            v1.append(v14).append((char)(32));
         }

         v1.append((char)(10));
      }

      return v1.toString();
   }

   public void addTrajectory(List<float[]> recorded) {
      for (float[] v3 : recorded) {
         if (v3.length == 5) {
            int result = this.f159.size();

            if (result >= 60000) {
               this.f159.remove(0);
               
            }

            this.f159.add((float[])v3.clone());
            
         }
      }
   }

   private static double[] m193(String text, int expected) {
      String result = text.trim();

      String[] v2 = result.split("\\s+");
      if (v2.length < expected) {
         return null;
      } else {
         double[] v3 = new double[expected];

         try {
            for (int v4 = 0; v4 < expected; v4++) {
               double v9 = Double.parseDouble(v2[v4]);

               v3[v4] = v9;
            }

            return v3;
         } catch (Exception v8) {
            return null;
         }
      }
   }

   public float[] nextMove(float yawErr, float pitchErr, float sinceAttack) {
      float[] v4 = this.nextMoveEx(yawErr, pitchErr, sinceAttack);
      int v6 = -1326095755;

      return v4 == null ? null : new float[]{v4[0], v4[1]};
   }

   public Md(String name) {
      this.f157 = new ArrayList<>();
      this.f158 = -1;
      this.f159 = new ArrayList<>();
      this.f160 = -1;
      this.f161 = new ArrayList<>();
      this.name = name;
      this.net = new pd(30, 48, 14);
   }

   public static double[] features(
      float yawErr,
      float pitchErr,
      float prevAppliedYaw,
      float prevAppliedPitch,
      float prevPrevAppliedYaw,
      float prevPrevAppliedPitch,
      float prevYawAccel,
      float prevPitchAccel,
      float hitX,
      float hitY,
      float hitZ,
      float distance,
      float elevationDelta,
      float currentPitch,
      float relativePitchToTarget,
      float targetRelYawSpeed,
      float targetRelPitchSpeed,
      float targetVelX,
      float targetVelY,
      float targetVelZ,
      float playerVelY,
      float attackCdProgress,
      float sinceAttack,
      float prevSmoothness,
      float prevPeak
   ) {
      float v25 = (float)Math.hypot((double)yawErr, (double)pitchErr);
      double[] result = new double[30];
      result[0] = signedToUnit(yawErr, 180.0F);
      result[1] = signedToUnit(pitchErr, 90.0F);
      result[2] = Math.min((double)Math.abs(yawErr) / 180.0, 1.0);
      result[3] = Math.min((double)Math.abs(pitchErr) / 90.0, 1.0);
      result[4] = Math.min((double)v25 / 180.0, 1.0);
      result[5] = signedToUnit(prevAppliedYaw, 180.0F);
      result[6] = signedToUnit(prevAppliedPitch, 120.0F);
      result[7] = signedToUnit(prevPrevAppliedYaw, 180.0F);
      result[8] = signedToUnit(prevPrevAppliedPitch, 120.0F);
      int temp2 = 9;

      result[temp2] = signedToUnit(prevYawAccel, 35.0F);
      result[10] = signedToUnit(prevPitchAccel, 22.0F);
      result[11] = signedToUnit(prevYawAccel - prevPitchAccel, 45.0F);
      result[12] = signedToUnit(prevPitchAccel - prevYawAccel, 30.0F);
      result[13] = (double)m181(hitX);
      result[14] = (double)m181(hitY);
      temp2 = 15;
      float temp3 = m181(hitZ);

      result[temp2] = (double)temp3;
      result[16] = Math.min((double)Math.max(distance, 0.0F) / 8.0, 1.0);
      result[17] = signedToUnit(elevationDelta, 4.0F);
      result[18] = signedToUnit(currentPitch, 90.0F);
      result[19] = signedToUnit(relativePitchToTarget, 90.0F);
      result[20] = signedToUnit(targetRelYawSpeed, 35.0F);
      result[21] = signedToUnit(targetRelPitchSpeed, 20.0F);
      result[22] = signedToUnit(targetVelX, 1.5F);
      result[23] = signedToUnit(targetVelY, 1.5F);
      result[24] = signedToUnit(targetVelZ, 1.5F);
      result[25] = signedToUnit(playerVelY, 1.5F);
      result[26] = (double)m181(attackCdProgress);
      result[27] = Math.min((double)Math.max(sinceAttack, 0.0F) / 30.0, 1.0);
      result[28] = (double)m181(prevSmoothness);
      result[29] = (double)m181(prevPeak);
      return result;
   }

   private static float m179(float yawErr, float pitchErr) {
      float v2 = (float)Math.hypot((double)yawErr, (double)pitchErr);
      float v3 = 6.0F + v2 * 0.35F;

      return v3 * v3;
   }

   public md.od predictDetailed(float yawErr, float pitchErr) {
      int v3 = -2140037716;
      int temp2 = v3 * (v3 + 1);

      return this.predictDetailed(defaultFeatures(yawErr, pitchErr));
   }

   public void recordSample(double[] x, double[] y) {
      if (this.f155.size() >= 60000) {
         this.f155.remove(0);

         this.f156.remove(0);
      }

      List result = this.f155;
      Object v7 = x.clone();

      result.add((double[])v7);
      this.f156.add((double[])y.clone());
      this.samples++;
   }

   private static float m180(float value) {
      float result = m181(value) * 2.0F - 1.0F;

      return result;
   }

   public int strokeCount() {
      int v1 = 1265628606;
      int temp1 = v1 * (v1 + 1);

      return this.f157.size();
   }

   public List<double[]> getMemoryX() {

      return this.f155;
   }

   private void m189(String json) {
      try {
         Pattern v2 = Pattern.compile(
            "\\{\\s*\"distance\":\\s*([0-9.\\-E]+),\\s*\"yawError\":\\s*([0-9.\\-E]+),\\s*\"pitchError\":\\s*([0-9.\\-E]+),\\s*\"targetSpeed\":\\s*([0-9.\\-E]+),\\s*\"yawGain\":\\s*([0-9.\\-E]+),\\s*\"pitchGain\":\\s*([0-9.\\-E]+),\\s*\"yawStepAbs\":\\s*([0-9.\\-E]+),\\s*\"pitchStepAbs\":\\s*([0-9.\\-E]+),\\s*\"rayAligned\":\\s*(true|false)",
            2
         );
         Matcher v3 = v2.matcher(json);

         while (v3.find()) {
            double v4 = Double.parseDouble(v3.group(1));
            double v6 = Double.parseDouble(v3.group(2));
            double v8 = Double.parseDouble(v3.group(3));
            double v10 = Double.parseDouble(v3.group(4));
            double v12 = Double.parseDouble(v3.group(7));
            double v14 = Double.parseDouble(v3.group(8));
            boolean v16 = Boolean.parseBoolean(v3.group(9));
            double[] v17 = defaultFeatures((float)v6, (float)v8);
            v17[16] = Math.min(Math.max(v4, 0.0) / 8.0, 1.0);
            v17[20] = signedToUnit((float)v10, 35.0F);
            double[] v18 = new double[14];
            v18[0] = Math.min(v12 / 180.0, 1.0);
            v18[1] = Math.min(v14 / 120.0, 1.0);
            v18[2] = 0.5;
            v18[3] = 0.5;
            v18[4] = 0.5;
            v18[11] = v16 ? 1.0 : 0.6;
            this.f155.add(v17);

            this.f156.add(v18);
            this.samples++;
         }
      } catch (Exception v20) {
      }
   }

   public void save() {
      try {
         Files.createDirectories(DIR);

         int result = this.samples;
         double v13 = this.loss;
         String temp2 = this.net.serialize();
         String temp3 = this.m186();
         String temp4 = this.m184();
         String v2 = this.m183();
         String v3 = this.m185();
         String v4 = temp4;
         String v5 = temp3;
         String v6 = temp2;
         double v7 = v13;
         int v9 = result;
         String v1 = v9 + " " + v7 + "\n" + v6 + v5 + v4 + v3 + v2;
         Files.writeString(this.file(), v1, StandardCharsets.UTF_8);

         this.exportOnnx();
      } catch (Exception v12) {
      }
   }

   public void ensureModernShape() {
      if (this.net.in != 30 || this.net.out != 14) {
         this.net = new pd(30, 48, 14);
         this.samples = 0;
         this.loss = -1.0;
         this.f155.clear();
         this.f156.clear();
         this.f161.clear();
      }
   }

   public static void saveCurrentName(String name) {
      try {
         Files.createDirectories(DIR);
         if (name != null) {

            if (!name.isBlank()) {
               Files.writeString(CURRENT_FILE, name, StandardCharsets.UTF_8);
               return;
            }
         }

         Files.deleteIfExists(CURRENT_FILE);
      } catch (Exception v3) {
      }
   }

   private static float m181(float value) {
      return m182(value, 0.0F, 1.0F);
   }

   private static float m178(float[] s, float yawErr, float pitchErr, float sinceAttack) {
      float v4 = s[0] - yawErr;
      float v5 = s[1] - pitchErr;
      float result = (Math.min(s[4], 40.0F) - Math.min(sinceAttack, 40.0F)) * 1.2F;

      float v6 = result;
      return v4 * v4 + v5 * v5 + v6 * v6;
   }

   public void addStrokes(List<float[]> recorded) {

      for (float[] result : recorded) {

         float[] v3 = result;
         if (v3.length == 3) {
            if (this.f157.size() >= 50000) {
               this.f157.remove(0);
            }

            this.f157.add((float[])v3.clone());
         }
      }
   }

   private md.od m174(double[] x) {
      double[] v2 = this.net.forward(x);
      float v3 = (float)(v2[0] * 180.0);
      float v4 = (float)(v2[1] * 120.0);
      int result = v2.length;

      if (result < 14) {
         md.od v7 = new md.od;
         float temp2 = Math.max(v3, 12.0F);

         v7./* $VF: Unable to resugar constructor */<init>(
            temp2, Math.max(v4, 10.0F), 0.5F, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, false
         );
         return v7;
      } else {
         return new md.od(
            Math.max(v3, 12.0F),
            Math.max(v4, 10.0F),
            m181((float)v2[2]),
            m181((float)v2[3]),
            m181((float)v2[4]),
            m180((float)v2[5]) * 1.2F,
            m180((float)v2[6]) * 0.8F,
            m180((float)v2[7]) * 12.0F,
            m180((float)v2[8]) * 8.0F,
            m180((float)v2[9]) * 35.0F,
            m180((float)v2[10]) * 22.0F,
            m181((float)v2[11]),
            m180((float)v2[12]) * 3.5F,
            m181((float)v2[13]),
            0.0F,
            false
         );
      }
   }

   public List<float[]> getTraj() {
      return this.f159;
   }

   public int trajCount() {
      int v2 = -658055567;
      int temp1 = v2 * (v2 + 1);

      int result = this.f159.size();

      return result;
   }

   private void m190(String data) {
      String[] v2 = data.split("\n");
      int v3 = -1;

      for (int v4 = 0; v4 < v2.length; v4++) {
         if (v2[v4].startsWith("MEMORY ")) {
            v3 = v4 + 1;
            break;
         }
      }

      if (v3 >= 0) {
         this.f155.clear();

         this.f156.clear();

         for (int v10 = v3; v10 < v2.length && this.f155.size() < 60000; v10++) {
            String v5 = v2[v10].trim();
            if (!v5.isEmpty()) {
               if (v5.startsWith("STROKES ")) {
                  break;
               }

               int v6 = v5.indexOf(124);
               if (v6 >= 0) {
                  double[] v7 = m193(v5.substring(0, v6), 30);
                  double[] v8 = m193(v5.substring(v6 + 1), 14);
                  if (v7 != null
                     && v8 != null
                     && (!(v8[0] < 0.002) || !(v8[1] < 0.002) || v7.length <= 25 || !(v7[25] > 0.3))) {
                     this.f155.add(v7);
                     this.f156.add(v8);
                  }
               }
            }
         }
      }
   }

   private void m192(String data) {
      String[] v2 = data.split("\n");
      int v3 = -1;

      for (int v4 = 0; v4 < v2.length; v4++) {
         if (v2[v4].startsWith("TRAJ ")) {
            v3 = v4 + 1;
            break;
         }
      }

      if (v3 >= 0) {
         this.f159.clear();

         for (int v12 = v3; v12 < v2.length && this.f159.size() < 60000; v12++) {
            String v5 = v2[v12].trim();
            if (!v5.isEmpty()) {
               String[] v6 = v5.split("\\s+");
               if (v6.length >= 5) {
                  try {
                     float v7 = Float.parseFloat(v6[2]);
                     float v8 = Float.parseFloat(v6[3]);
                     if (!(Math.hypot((double)v7, (double)v8) < 0.05F)) {
                        List result = this.f159;
                        float[] temp1 = new float[]{Float.parseFloat(v6[0]), Float.parseFloat(v6[1]), v7, v8, 0.0F};

                        temp1[4] = Float.parseFloat(v6[4]);
                        result.add(temp1);
                     }
                  } catch (Exception v11) {
                  }
               }
            }
         }
      }
   }

   public static String loadCurrentName() {
      try {
         if (!Files.exists(CURRENT_FILE)) {
            return null;
         } else {
            String v0 = Files.readString(CURRENT_FILE, StandardCharsets.UTF_8).trim();
            return v0.isEmpty() ? null : v0;
         }
      } catch (Exception v2) {

         return null;
      }
   }

   public Path file() {
      Path result = DIR;
      String v1 = this.name;
      StringBuilder temp1 = new StringBuilder();

      result = result.resolve(temp1.append((Object)v1).append(".neuro").toString());

      return result;
   }

   public float[] predict(float yawErr, float pitchErr) {

      md.od v3 = this.predictDetailed(yawErr, pitchErr);
      float[] result = new float[]{v3.yawSpeed, 0.0F};

      result[1] = v3.pitchSpeed;
      return result;
   }

   public void addMultiPoints(List<md.nd> recorded) {
      for (md.nd v3 : recorded) {
         if (this.f161.size() >= 50000) {
            this.f161.remove(0);
            
         }

         this.f161.add(v3);
      }
   }

   private static float m182(float value, float min, float max) {
      return value < min ? min : Math.min(value, max);
   }

   public void remember(double[][] x, double[][] y) {
      for (int v3 = 0; v3 < x.length && v3 < y.length; v3++) {
         if (this.f155.size() >= 60000) {
            this.f155.remove(0);
            this.f156.remove(0);
         }

         this.f155.add((double[])x[v3].clone());

         this.f156.add((double[])y[v3].clone());
      }
   }

   public float[] nextJitter(float speed) {
      if (this.f157.isEmpty()) {
         return new float[]{0.0F, 0.0F};
      } else {
         if (this.f158 >= 0 && this.f158 < this.f157.size()) {
            float[] v2 = this.f157.get(this.f158);
            if (Math.abs(v2[2] - speed) <= m175(speed)) {
               this.f158++;
               return new float[]{v2[0], v2[1]};
            }
         }

         int v10 = 0;
         float v3 = Float.MAX_VALUE;
         ThreadLocalRandom result = ThreadLocalRandom.current();
         int v8 = -1946260461;

         int v4 = result.nextInt(this.f157.size());

         for (int v5 = 0; v5 < this.f157.size(); v5++) {
            int v6 = (v4 + v5) % this.f157.size();
            float v7 = Math.abs(this.f157.get(v6)[2] - speed);
            if (v7 < v3) {
               v3 = v7;
               v10 = v6;
            }

            if (v7 <= m175(speed) * 0.25F) {
               v10 = v6;
               break;
            }
         }

         this.f158 = v10 + 1;
         float[] v11 = this.f157.get(v10);
         return new float[]{v11[0], v11[1]};
      }
   }

   public static double[] defaultFeatures(float yawErr, float pitchErr) {

      return features(
         yawErr,
         pitchErr,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.5F,
         0.5F,
         0.5F,
         3.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         1.0F,
         0.0F,
         1.0F,
         0.0F
      );
   }

   public static md load(String name) {
      try {
         Path v1 = m187(name);
         if (!Files.exists(v1)) {
            String v8 = System.getProperty("user.home");
            String v2 = v8 + "\\Desktop\\" + name + ".neuro";
            v1 = Path.of(v2);
            if (!Files.exists(v1)) {
               return null;
            }
         }

         String v12 = Files.readString(v1, StandardCharsets.UTF_8).trim();
         if (v12.startsWith("{")) {
            md v13 = new md(name);
            v13.m189(v12);
            return v13;
         } else {
            int v3 = v12.indexOf(10);
            if (v3 < 0) {
               return null;
            } else {
               String v4 = v12.substring(0, v3).trim();
               String[] v5 = v4.split("\\s+");
               md result = new md;

               result./* $VF: Unable to resugar constructor */<init>(name);
               md v6 = result;
               v6.samples = Integer.parseInt(v5[0]);
               v6.loss = Double.parseDouble(v5[1]);
               v6.net = pd.deserialize(v12.substring(v3 + 1));
               v6.m190(v12.substring(v3 + 1));
               v6.m191(v12.substring(v3 + 1));
               v6.m192(v12.substring(v3 + 1));
               v6.m188(v12.substring(v3 + 1));
               return v6;
            }
         }
      } catch (Exception v11) {

         return null;
      }
   }

   public void smoothStrokesByIndex() {
      int v1 = this.f157.size();
      if (v1 >= 5) {
         ArrayList v2 = new ArrayList(v1);
         float[] v3 = new float[]{0.06F, 0.24F, 0.4F, 0.24F, 0.06F};

         for (int v4 = 0; v4 < v1; v4++) {
            float v5 = 0.0F;
            float v6 = 0.0F;
            float v7 = 0.0F;
            float v8 = 0.0F;

            for (int v9 = -2; v9 <= 2; v9++) {
               int temp1 = v1 - 1;

               int v10 = Math.max(0, Math.min(temp1, v4 + v9));
               float v11 = v3[v9 + 2];
               float[] v12 = this.f157.get(v10);
               v5 += v12[0] * v11;
               v6 += v12[1] * v11;
               v7 += v12[2] * v11;
               v8 += v11;
            }

            v2.add(new float[]{v5 / v8, v6 / v8, v7 / v8});
         }

         this.f157.clear();
         this.f157.addAll(v2);
      }
   }

   public md.od predictDetailed(double[] x) {
      md.od v2 = this.m174(x);
      if (!this.f155.isEmpty() && !this.f156.isEmpty()) {
         int v3 = this.f155.size();
         int v4 = Math.min(v3, 128);
         int v5 = Math.max(1, v3 / v4);
         int v6 = -1;

         double v7 = Double.MAX_VALUE;

         for (int v9 = 0; v9 < v3; v9 += v5) {
            double[] v10 = this.f155.get(v9);
            double v11 = x[0] - v10[0];
            double v13 = x[1] - v10[1];
            double v15 = x.length > 16 && v10.length > 16
               ? x[16] - v10[16]
               : 0.0;
            double v32 = v11 * v11 * 2.0 + v13 * v13 * 2.0;

            double v17 = v32 + v15 * v15;
            if (v17 < v7) {
               v7 = v17;
               v6 = v9;
               if (v17 < 0.001) {
                  break;
               }
            }
         }

         if (v6 >= 0 && v6 < this.f156.size()) {
            double[] v26 = this.f156.get(v6);
            float v27 = (float)(v26[0] * 180.0);
            float v28 = (float)(v26[1] * 120.0);
            float v12 = m181((float)v26[2]);
            float v29 = m181((float)v26[3]);
            float v14 = m181((float)v26[4]);
            float v30 = m180((float)v26[5]) * 1.2F;
            float v16 = m180((float)v26[6]) * 0.8F;
            float v31 = m180((float)v26[7]) * 12.0F;
            float v18 = m180((float)v26[8]) * 8.0F;
            float v19 = m180((float)v26[9]) * 35.0F;
            float v33 = m180((float)v26[10]);

            float v20 = v33 * 22.0F;
            float v21 = 0.7F;
            float v22 = 0.3F;
            return new md.od(
               v2.yawSpeed * v21 + v27 * v22,
               v2.pitchSpeed * v21 + v28 * v22,
               v2.hitX * v21 + v12 * v22,
               v2.hitY * v21 + v29 * v22,
               v2.hitZ * v21 + v14 * v22,
               v2.yawShake * v21 + v30 * v22,
               v2.pitchShake * v21 + v16 * v22,
               v2.yawLead * v21 + v31 * v22,
               v2.pitchLead * v21 + v18 * v22,
               v2.yawAccel * v21 + v19 * v22,
               v2.pitchAccel * v21 + v20 * v22,
               v2.smoothness,
               v2.curveArc,
               v2.overshootRatio,
               0.0F,
               false
            );
         } else {
            return v2;
         }
      } else {
         return v2;
      }
   }

   private String m185() {
      StringBuilder result = new StringBuilder;
      int v4 = -1186314117;
      int temp2 = v4 + 1;

      result./* $VF: Unable to resugar constructor */<init>();
      StringBuilder v1 = result;
      v1.append("TRAJ ").append(this.f159.size()).append((char)(10));

      for (float[] v3 : this.f159) {
         v1.append(v3[0])
            .append((char)(32))
            .append(v3[1])
            .append((char)(32))
            .append(v3[2])
            .append((char)(32))
            .append(v3[3])
            .append((char)(32))
            .append(v3[4])
            .append((char)(10));
      }

      return v1.toString();
   }

   public List<float[]> getStrokes() {

      return this.f157;
   }

   public static double signedToUnit(float value, float maxAbs) {

      return maxAbs <= 0.0F ? 0.5 : (double)(m182(value / maxAbs, -1.0F, 1.0F) + 1.0F) * 0.5;
   }

   @Environment(EnvType.CLIENT)
   public static class nd {
      public final float x;
      public final float y;
      public final float z;
      public final float pitchAngle;
      public final boolean isHit;
      public final int hitIndex;
      public final float yawAngle;

      public nd(float x, float y, float z, boolean isHit, int hitIndex, float yawAngle, float pitchAngle) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.isHit = isHit;
         this.hitIndex = hitIndex;
         this.yawAngle = yawAngle;
         this.pitchAngle = pitchAngle;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static class od {
      public final float pitchShake;
      public final float yawLead;
      public final float hitZ;
      public final float yawAccel;
      public final float yawSpeed;
      public final float pitchSpeed;
      public final float overshootRatio;
      public final float yawShake;
      public final float hitX;
      public final boolean remembered;
      public final float pitchAccel;
      public final float curveArc;
      public final float peak;
      public final float pitchLead;
      public final float smoothness;
      public final float hitY;

      public od(
         float yawSpeed,
         float pitchSpeed,
         float hitX,
         float hitY,
         float hitZ,
         float yawShake,
         float pitchShake,
         float yawLead,
         float pitchLead,
         float yawAccel,
         float pitchAccel,
         float smoothness,
         float curveArc,
         float overshootRatio,
         float peak,
         boolean remembered
      ) {
         this.yawSpeed = yawSpeed;
         this.pitchSpeed = pitchSpeed;
         this.hitX = hitX;
         this.hitY = hitY;
         this.hitZ = hitZ;
         this.yawShake = yawShake;
         this.pitchShake = pitchShake;
         this.yawLead = yawLead;
         this.pitchLead = pitchLead;
         this.yawAccel = yawAccel;
         this.pitchAccel = pitchAccel;
         this.smoothness = smoothness;
         this.curveArc = curveArc;
         this.overshootRatio = overshootRatio;
         this.peak = peak;
         this.remembered = remembered;
      }

      public md.od withSpeed(float yawSpeed, float pitchSpeed) {
         md.od result = new md.od;

         result./* $VF: Unable to resugar constructor */<init>(
            yawSpeed,
            pitchSpeed,
            this.hitX,
            this.hitY,
            this.hitZ,
            this.yawShake,
            this.pitchShake,
            this.yawLead,
            this.pitchLead,
            this.yawAccel,
            this.pitchAccel,
            this.smoothness,
            this.curveArc,
            this.overshootRatio,
            this.peak,
            this.remembered
         );
         return result;
      }

      public od(
         float yawSpeed,
         float pitchSpeed,
         float hitX,
         float hitY,
         float hitZ,
         float yawShake,
         float pitchShake,
         float yawLead,
         float pitchLead,
         float yawAccel,
         float pitchAccel,
         float smoothness,
         float peak,
         boolean remembered
      ) {
         this(yawSpeed, pitchSpeed, hitX, hitY, hitZ, yawShake, pitchShake, yawLead, pitchLead, yawAccel, pitchAccel, smoothness, 0.0F, 0.0F, peak, remembered);
      }

      
   }
}
