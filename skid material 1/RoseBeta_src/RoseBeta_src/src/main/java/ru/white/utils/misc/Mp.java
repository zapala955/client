package ru.white.utils.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Mp {
   public static final float DELTA_PITCH_NORM = 10.0F;
   private static final Path SAMPLES_PATH = Path.of("rose/client1_21_11/neuro_samples.bin");
   private final float[] b1;
   private final float[][] w1 = new float[24][8];
   private final float[][] w3;
   public static final float SPEED_NORM = 0.3F;
   public static final float DIST_NORM = 6.0F;
   private volatile boolean f1252;
   private static final Path MODEL_PATH = Path.of("rose/client1_21_11/neuro_rotation.json");
   public static final int H1 = 24;
   public static final int OUT = 2;
   public static final float YAW_ERR_NORM = 180.0F;
   private final float[] b3;
   public static final int MIN_SAMPLES = 300;
   public static final float PITCH_ERR_NORM = 90.0F;
   public static final int IN = 8;
   private static final int MAX_SAMPLES = 40000;
   private volatile boolean f1253;
   private final float[][] w2;
   private final float[] b2;
   public static final int H2 = 16;
   private final List<float[]> f1251;
   private static final Gson GSON = new GsonBuilder().create();
   public static final float DELTA_YAW_NORM = 20.0F;

   private static boolean m1522(float[] v) {
      float[] v1 = v;
      int v2 = v.length;
      int v3 = 0;

      while (true) {
         if (v3 >= v2) {
            return false;
         }

         float v4 = v1[v3];
         if (Float.isNaN(v4)) {
            break;
         }

         boolean result = Float.isInfinite(v4);

         if (result) {
            break;
         }

         v3++;
      }

      return true;
   }

   public boolean hasMemory() {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot read field "id" because the return value SettingVisibility "org.jetbrains.java.decompiler.modules.decompiler.flow.FlattenStatementsHelper.getDirectNode(org.jetbrains.java.decompiler.modules.decompiler.stats.Statement)" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.collectCatchVars(ExprProcessor.java:179)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.collectCatchVars(ExprProcessor.java:184)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.collectCatchVars(ExprProcessor.java:184)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.collectCatchVars(ExprProcessor.java:184)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.collectCatchVars(ExprProcessor.java:184)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.collectCatchVars(ExprProcessor.java:184)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.collectCatchVars(ExprProcessor.java:184)
      //   at org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor.processStatement(ExprProcessor.java:112)
      //   at org.jetbrains.java.decompiler.modules.decompiler.FinallyProcessor.getFinallyInformation(FinallyProcessor.java:136)
      //   at org.jetbrains.java.decompiler.modules.decompiler.FinallyProcessor.iterateGraph(FinallyProcessor.java:85)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:178)
      //
      // Bytecode:
      // 00: aload 0
      // 01: getfield x0/y28/mp.f1251 Ljava/util/List;
      // 04: dup
      // 05: astore 1
      // 06: monitorenter
      // 07: aload 0
      // 08: getfield x0/y28/mp.f1251 Ljava/util/List;
      // 0b: invokeinterface java/util/List.isEmpty ()Z 1
      // 10: ifne 17
      // 13: bipush 1
      // 14: goto 18
      // 17: bipush 0
      // 18: aload 1
      // 19: ldc -639791335
      // 1b: istore 4
      // 1d: iload 4
      // 1f: dup
      // 20: bipush 1
      // 21: iadd
      // 22: imul
      // 23: bipush 2
      // 24: irem
      // 25: ifeq 32
      // 28: aconst_null
      // 29: pop
      // 2a: ldc "QN9fUar8lcItOQZPCNeuqNQGazMR0uY="
      // 2c: ldc -1905151855
      // 2e: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 31: pop
      // 32: nop
      // 33: monitorexit
      // 34: ireturn
      // 35: astore 2
      // 36: aload 1
      // 37: ldc -554125859
      // 39: istore 3
      // 3a: iload 3
      // 3b: dup
      // 3c: bipush 1
      // 3d: iadd
      // 3e: imul
      // 3f: bipush 2
      // 40: irem
      // 41: ifeq 46
      // 44: aconst_null
      // 45: pop
      // 46: nop
      // 47: monitorexit
      // 48: aload 2
      // 49: athrow
   }

   private static void m1523(float[][] w, float[] b, Random r) {
      float v3 = (float)Math.sqrt(2.0 / (double)w[0].length);

      for (float[] v7 : w) {
         for (int v8 = 0; v8 < v7.length; v8++) {
            v7[v8] = (float)(r.nextGaussian() * (double)v3);
         }
      }

      Arrays.fill(b, 0.0F);
   }

   private void m1526(float[] in, float[] h1, float[] h2, float[] out) {
      for (int v5 = 0; v5 < 24; v5++) {

         float v6 = this.b1[v5];

         for (int v7 = 0; v7 < 8; v7++) {
            v6 += this.w1[v5][v7] * in[v7];
         }

         h1[v5] = (float)Math.tanh((double)v6);
      }

      for (int v10 = 0; v10 < 16; v10++) {
         float v12 = this.b2[v10];

         for (int v14 = 0; v14 < 24; v14++) {
            v12 += this.w2[v10][v14] * h1[v14];
         }

         h2[v10] = (float)Math.tanh((double)v12);
      }

      for (int v11 = 0; v11 < 2; v11++) {
         float v13 = this.b3[v11];

         for (int v15 = 0; v15 < 16; v15++) {
            v13 += this.w3[v11][v15] * h2[v15];
         }

         out[v11] = v13;
      }
   }

   private static boolean m1521(float[][] m) {
      float[][] v1 = m;

      int v2 = m.length;

      for (int v3 = 0; v3 < v2; v3++) {
         float[] v4 = v1[v3];
         if (m1522(v4)) {
            return true;
         }
      }

      return false;
   }

   private float[] m1524(float[] features) {
      if (features.length != 8) {
         return null;
      } else {
         ArrayList v2;
         synchronized (this.f1251) {
            if (this.f1251.isEmpty()) {
               return null;
            }

            v2 = new ArrayList<>(this.f1251);
            
         }

         int v16 = Math.min(5, v2.size());
         float[] v4 = new float[v16];
         float[] v5 = new float[v16];
         float[] v6 = new float[v16];
         Arrays.fill(v4, Float.MAX_VALUE);

         for (float[] v8 : v2) {
            float v9 = m1525(features, v8);

            for (int v10 = 0; v10 < v16; v10++) {
               if (!(v9 >= v4[v10])) {
                  for (int v11 = v16 - 1; v11 > v10; v11--) {
                     v4[v11] = v4[v11 - 1];
                     v5[v11] = v5[v11 - 1];
                     v6[v11] = v6[v11 - 1];
                  }

                  v4[v10] = v9;
                  v5[v10] = v8[8] * 20.0F;
                  v6[v10] = v8[9] * 10.0F;
                  break;
               }
            }
         }

         float v17 = 0.0F;
         float v18 = 0.0F;
         float v19 = 0.0F;

         for (int v20 = 0; v20 < v16; v20++) {
            if (v4[v20] != Float.MAX_VALUE) {
               float v21 = 1.0F / (1.0E-4F + v4[v20] * v4[v20]);
               v17 += v5[v20] * v21;
               v18 += v6[v20] * v21;
               v19 += v21;
            }
         }

         if (v19 <= 0.0F) {
            return null;
         } else {
            float[] v22 = new float[]{m1533(v17 / v19, -30.0F, 30.0F), 0.0F};
            float temp3 = v18 / v19;

            v22[1] = m1533(temp3, -18.0F, 18.0F);
            return v22;
         }
      }
   }

   public boolean isTrained() {

      return this.f1252;
   }

   private void m1527(List<float[]> data) {
      try {
         Files.createDirectories(SAMPLES_PATH.getParent());

         try (DataOutputStream v2 = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(SAMPLES_PATH)))) {
            v2.writeInt(10);
            v2.writeInt(data.size());

            for (float[] v4 : data) {
               for (int v5 = 0; v5 < 10; v5++) {
                  v2.writeFloat(v4[v5]);
               }
            }
         }
      } catch (Exception v11) {
      }
   }

   // $VF: Could not create synchronized statement, marking monitor enters and exits
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
   public 

      List v1 = this.f1251;
      synchronized (this.f1251){} // $VF: monitorenter 
      int v3 = -172836598;

      try {
         int v8 = this.f1251.size();
         // $VF: monitorexit
         return v8;
      } finally {
         // $VF: monitorexit
      }
   }

   private static float m1533(float v, float min, float max) {

      return v < min ? min : Math.min(v, max);
   }

   public void save() {
      if (!this.m1520()) {
         try {
            Files.createDirectories(MODEL_PATH.getParent());
            JsonObject v1 = new JsonObject();
            v1.add("w1", m1529(this.w1));

            v1.add("b1", m1530(this.b1));
            v1.add("w2", m1529(this.w2));
            v1.add("b2", m1530(this.b2));
            v1.add("w3", m1529(this.w3));
            v1.add("b3", m1530(this.b3));

            try (BufferedWriter v2 = Files.newBufferedWriter(MODEL_PATH, StandardCharsets.UTF_8)) {
               GSON.toJson(v1, v2);
            }
         } catch (Exception v8) {
         }
      }
   }

   private static void m1532(JsonArray arr, float[] v) {

      for (int v2 = 0; v2 < v.length; v2++) {
         JsonElement temp2 = arr.get(v2);

         v[v2] = temp2.getAsFloat();
      }
   }

   public Mp() {
      this.b1 = new float[24];
      this.w2 = new float[16][24];
      this.b2 = new float[16];
      this.w3 = new float[2][16];
      this.b3 = new float[2];
      this.f1251 = new ArrayList<>();
      this.f1252 = false;
      this.f1253 = false;
      this.m1519();
      this.load();
      this.m1528();
   }

   public void record(float[] features, float dYaw, float dPitch) {
      if (features.length == 8) {
         if (!m1522(features) && !Float.isNaN(dYaw) && !Float.isNaN(dPitch) && !Float.isInfinite(dYaw) && !Float.isInfinite(dPitch)) {
            if (!(Math.abs(dYaw) > 120.0F) && !(Math.abs(dPitch) > 90.0F)) {
               float[] v4 = new float[10];
               System.arraycopy(features, 0, v4, 0, 8);
               int temp1 = 8;
               float temp2 = dYaw / 20.0F;

               v4[temp1] = m1533(temp2, -1.5F, 1.5F);
               temp1 = 9;
               temp2 = dPitch / 10.0F;

               v4[temp1] = m1533(temp2, -1.5F, 1.5F);
               synchronized (this.f1251) {
                  if (this.f1251.size() >= 40000) {
                     this.f1251.remove(0);
                  }

                  this.f1251.add(v4);
               }
            }
         }
      }
   }

   private static JsonArray m1529(float[][] m) {
      JsonArray result = new JsonArray();
      int v6 = 1860685374;

      JsonArray v1 = result;

      for (float[] v5 : m) {
         v1.add(m1530(v5));
      }

      return v1;
   }

   private static void m1531(JsonArray arr, float[][] m) {

      int v2 = 0;

      while (v2 < m.length) {
         m1532(arr.get(v2).getAsJsonArray(), m[v2]);
         v2++;
         
      }
   }

   private void m1528() {
      if (!Files.exists(SAMPLES_PATH)) {
         
      } else {
         try {
            label69: {
               try (DataInputStream v1 = new DataInputStream(new BufferedInputStream(Files.newInputStream(SAMPLES_PATH)))) {
                  int v2 = v1.readInt();
                  if (v2 == 10) {
                     int v3 = Math.min(v1.readInt(), 40000);
                     synchronized (this.f1251) {
                        this.f1251.clear();

                        for (int v5 = 0; v5 < v3; v5++) {
                           float[] v6 = new float[10];

                           for (int v7 = 0; v7 < v6.length; v7++) {
                              v6[v7] = v1.readFloat();
                           }

                           this.f1251.add(v6);
                        }
                        break label69;
                     }
                  }
               }

               return;
            }
         } catch (Exception v14) {
         }
      }
   }

   public boolean isTraining() {

      return this.f1253;
   }

   // $VF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // $VF: Could not create synchronized statement, marking monitor enters and exits
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
   public void clearSamples() {
      List v1 = this.f1251;
      synchronized (this.f1251){} // $VF: monitorenter 

      boolean v8 = false /* VF: Semaphore variable */;

      try {
         v8 = true;

         this.f1251.clear();
         // $VF: monitorexit
         v8 = false;
      } finally {
         if (v8) {
            // $VF: monitorexit
         }
      }

      try {
         Files.deleteIfExists(SAMPLES_PATH);
      } catch (Exception v9) {
      }
   }

   public boolean trainAsync(BiConsumer<Boolean, Integer> onDone) {
      if (this.f1253) {
         return false;
      } else {
         ArrayList v2;
         synchronized (this.f1251) {
            v2 = new ArrayList<>(this.f1251);
         }

         this.f1253 = true;
         Thread v7 = new Thread(() -> {
            boolean v3 = false;

            try {
               this.m1527(v2);
               if (v2.size() >= 300) {
                  if (this.m1520()) {
                     this.m1519();
                  }

                  this.train(v2);
                  if (this.m1520()) {
                     this.m1519();
                     this.f1252 = false;
                  } else {
                     this.f1252 = true;
                     this.save();
                     v3 = true;
                  }
               }
            } catch (Throwable v9) {
               this.m1519();
               this.f1252 = false;
            } finally {
               this.f1253 = false;
            }

            if (onDone != null) {
               Boolean temp1 = v3;
               int temp2 = v2.size();

               onDone.accept(temp1, temp2);
            }
         }, "neuro-rotation-train");
         v7.setDaemon(true);
         v7.start();

         return true;
      }
   }

   private static JsonArray m1530(float[] v) {
      JsonArray v1 = new JsonArray();

      for (float v5 : v) {
         int v6 = 1422613983;

         int temp2 = v6 * (v6 + 1);

         v1.add(v5);
      }

      return v1;
   }

   private boolean m1520() {
      if (!m1521(this.w1) && !m1522(this.b1) && !m1521(this.w2) && !m1522(this.b2) && !m1521(this.w3)) {

         if (!m1522(this.b3)) {
            return false;
         }
      }

      return true;
   }

   private void train(List<float[]> data) {
      int v2 = 60;
      float v3 = 0.004F;
      float v4 = 0.85F;
      float v5 = 3.0F;
      float[][] v6 = new float[24][8];
      float[] v7 = new float[24];
      float[][] v8 = new float[16][24];
      float[] v9 = new float[16];
      float[][] v10 = new float[2][16];
      float[] v11 = new float[2];
      float[] v12 = new float[24];
      float[] v13 = new float[16];
      float[] v14 = new float[2];
      float[] v15 = new float[2];
      float[] v16 = new float[16];
      float[] v17 = new float[24];
      Random v18 = new Random();
      ArrayList v19 = new ArrayList(data);

      for (int v20 = 0; v20 < v2; v20++) {
         Collections.shuffle(v19, v18);
         float v21 = v3 * (1.0F - (float)v20 / (float)v2 * 0.9F);

         for (float[] v23 : v19) {
            this.m1526(v23, v12, v13, v14);

            for (int v24 = 0; v24 < 2; v24++) {
               int v27 = 1971658522;

               v15[v24] = m1533(v14[v24] - v23[8 + v24], -v5, v5);
            }

            for (int v29 = 0; v29 < 2; v29++) {
               for (int v25 = 0; v25 < 16; v25++) {
                  v10[v29][v25] = v4 * v10[v29][v25] - v21 * v15[v29] * v13[v25];
                  this.w3[v29][v25] = this.w3[v29][v25] + v10[v29][v25];
               }

               v11[v29] = v4 * v11[v29] - v21 * v15[v29];
               this.b3[v29] = this.b3[v29] + v11[v29];
            }

            for (int v30 = 0; v30 < 16; v30++) {
               float v34 = 0.0F;

               for (int v26 = 0; v26 < 2; v26++) {
                  v34 += v15[v26] * this.w3[v26][v30];
               }

               v16[v30] = m1533(v34 * (1.0F - v13[v30] * v13[v30]), -v5, v5);
            }

            for (int v31 = 0; v31 < 16; v31++) {
               for (int v35 = 0; v35 < 24; v35++) {
                  v8[v31][v35] = v4 * v8[v31][v35] - v21 * v16[v31] * v12[v35];
                  this.w2[v31][v35] = this.w2[v31][v35] + v8[v31][v35];
               }

               v9[v31] = v4 * v9[v31] - v21 * v16[v31];
               this.b2[v31] = this.b2[v31] + v9[v31];
            }

            for (int v32 = 0; v32 < 24; v32++) {
               float v36 = 0.0F;

               for (int v38 = 0; v38 < 16; v38++) {
                  v36 += v16[v38] * this.w2[v38][v32];
               }

               v17[v32] = m1533(v36 * (1.0F - v12[v32] * v12[v32]), -v5, v5);
            }

            for (int v33 = 0; v33 < 24; v33++) {
               for (int v37 = 0; v37 < 8; v37++) {
                  v6[v33][v37] = v4 * v6[v33][v37] - v21 * v17[v33] * v23[v37];
                  this.w1[v33][v37] = this.w1[v33][v37] + v6[v33][v37];
               }

               v7[v33] = v4 * v7[v33] - v21 * v17[v33];
               this.b1[v33] = this.b1[v33] + v7[v33];
            }
         }
      }
   }

   public float[] predict(float[] features) {
      float[] v2 = this.m1524(features);
      if (v2 != null) {
         return v2;
      } else {
         float[] v3 = new float[24];
         float[] v4 = new float[16];

         float[] v5 = new float[2];
         this.m1526(features, v3, v4, v5);
         if (!Float.isNaN(v5[0]) && !Float.isNaN(v5[1]) && !Float.isInfinite(v5[0]) && !Float.isInfinite(v5[1])) {
            float[] result = new float[2];

            result[0] = m1533(v5[0] * 20.0F, -30.0F, 30.0F);
            result[1] = m1533(v5[1] * 10.0F, -18.0F, 18.0F);
            return result;
         } else {
            return new float[]{0.0F, 0.0F};
         }
      }
   }

   private void m1519() {
      Random result = new Random;

      result./* $VF: Unable to resugar constructor */<init>();
      Random v1 = result;
      m1523(this.w1, this.b1, v1);
      m1523(this.w2, this.b2, v1);
      m1523(this.w3, this.b3, v1);
   }

   public void load() {
      if (Files.exists(MODEL_PATH)) {
         try {
            BufferedReader result = Files.newBufferedReader(MODEL_PATH, StandardCharsets.UTF_8);

            try (BufferedReader v1 = result) {
               JsonObject v2 = JsonParser.parseReader(v1).getAsJsonObject();
               m1531(v2.getAsJsonArray("w1"), this.w1);
               m1532(v2.getAsJsonArray("b1"), this.b1);
               m1531(v2.getAsJsonArray("w2"), this.w2);
               m1532(v2.getAsJsonArray("b2"), this.b2);
               m1531(v2.getAsJsonArray("w3"), this.w3);
               m1532(v2.getAsJsonArray("b3"), this.b3);
               if (!this.m1520()) {
                  this.f1252 = true;
                  return;
               }

               this.m1519();
            }
         } catch (Exception v7) {
            this.m1519();
         }
      }
   }

   private static float m1525(float[] features, float[] sample) {
      float v2 = 0.0F;

      for (int v3 = 0; v3 < 8; v3++) {
         float v4 = switch (v3) {
            case 0 -> 4.0F;
            case 1 -> 3.0F;
            default -> 1.0F;
            case 6 -> 1.7F;
            case 7 -> 1.4F;
         };

         float v5 = features[v3] - sample[v3];
         v2 += v5 * v5 * v4;
      }

      return v2;
   }

   
}
