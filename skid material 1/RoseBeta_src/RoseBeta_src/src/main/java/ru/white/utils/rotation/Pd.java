package ru.white.utils.rotation;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Pd {
   private final transient double[][] f164;
   public final double[] b1;
   public final double[][] w2;
   public final int hid;
   private static final Random RND = new Random();
   public final double[] b2;
   private final transient double[] f163;
   private final transient double[][] f162;
   private static final double[] OUTPUT_WEIGHTS;
   public final double[][] w1;
   private final transient double[] f165;
   public final int out;
   public final int in;

   private static double m194(double x) {
      double v2 = Math.max(-45.0, Math.min(45.0, x));
      int v4 = 431458601;
      int temp1 = v4 * (v4 + 1);

      return 1.0 / (1.0 + Math.exp(-v2));
   }

   public Pd(int in, int hid, int out) {
      this.in = in;
      this.hid = hid;
      this.out = out;
      this.w1 = new double[hid][in];
      this.b1 = new double[hid];
      this.w2 = new double[out][hid];
      this.b2 = new double[out];
      this.f162 = new double[hid][in];
      this.f163 = new double[hid];
      this.f164 = new double[out][hid];
      this.f165 = new double[out];
      double v4 = Math.sqrt(2.0 / (double)in);
      double v6 = Math.sqrt(2.0 / (double)hid);

      for (int v8 = 0; v8 < hid; v8++) {
         for (int v9 = 0; v9 < in; v9++) {
            this.w1[v8][v9] = (RND.nextDouble() * 2.0 - 1.0) * v4;
         }
      }

      for (int v10 = 0; v10 < out; v10++) {
         for (int v11 = 0; v11 < hid; v11++) {
            this.w2[v10][v11] = (RND.nextDouble() * 2.0 - 1.0) * v6;
         }
      }
   }

   public void copyFrom(pd src) {
      for (int v2 = 0; v2 < this.hid; v2++) {
         System.arraycopy(src.w1[v2], 0, this.w1[v2], 0, this.in);

         this.b1[v2] = src.b1[v2];
      }

      for (int v4 = 0; v4 < this.out; v4++) {
         System.arraycopy(src.w2[v4], 0, this.w2[v4], 0, this.hid);
         this.b2[v4] = src.b2[v4];
      }
   }

   private static double[] m196(String line) {
      if (line != null && !line.isBlank()) {
         String[] v1 = line.trim().split("\\s+");
         double[] v2 = new double[v1.length];

         for (int v3 = 0; v3 < v1.length; v3++) {
            try {
               v2[v3] = Double.parseDouble(v1[v3]);
            } catch (Exception v8) {
               v2[v3] = 0.0;
            }
         }

         return v2;
      } else {
         return new double[0];
      }
   }

   public pd copy() {
      pd result = new pd;
      int v2 = 1227578053;

      result./* $VF: Unable to resugar constructor */<init>(this.in, this.hid, this.out);
      pd v1 = result;
      v1.copyFrom(this);

      return v1;
   }

   public String serialize() {
      StringBuilder v1 = new StringBuilder();
      StringBuilder result = v1.append(this.in).append((char)(32)).append(this.hid);

      result.append((char)(32)).append(this.out).append((char)(10));

      for (double[] v5 : this.w1) {
         for (double v9 : v5) {
            v1.append(v9).append((char)(32));
         }
      }

      v1.append((char)(10));

      for (double v23 : this.b1) {
         v1.append(v23).append((char)(32));
      }

      v1.append((char)(10));

      for (double[] v24 : this.w2) {
         for (double v29 : v24) {
            v1.append(v29).append((char)(32));
         }
      }

      int v31 = 10;

      v1.append((char)v31);

      for (double v25 : this.b2) {
         v1.append(v25).append((char)(32));
      }

      v1.append((char)(10));
      String v30 = v1.toString();

      return v30;
   }

   public double[] forward(double[] x) {
      double[] v2 = x;
      if (x.length != this.in) {
         v2 = new double[this.in];
         System.arraycopy(x, 0, v2, 0, Math.min(x.length, this.in));
      }

      double[] v3 = new double[this.hid];

      for (int v4 = 0; v4 < this.hid; v4++) {
         double v5 = this.b1[v4];

         for (int v7 = 0; v7 < this.in; v7++) {

            v5 += this.w1[v4][v7] * v2[v7];
         }

         v3[v4] = Math.tanh(v5);
      }

      double[] v12 = new double[this.out];
      int v13 = 0;

      while (v13 < this.out) {
         double v6 = this.b2[v13];

         for (int v8 = 0; v8 < this.hid; v8++) {

            v6 += this.w2[v13][v8] * v3[v8];
         }

         v12[v13] = m194(v6);
         v13++;
      }

      return v12;
   }

   public double evalLoss(double[][] X, double[][] Y) {
      int v3 = X.length;
      if (v3 == 0) {
         return 0.0;
      } else {
         double v4 = 0.0;

         for (int v6 = 0; v6 < v3; v6++) {
            double[] v7 = this.forward(X[v6]);
            double[] v8 = Y[v6];
            double v9 = 0.0;

            for (int v11 = 0; v11 < this.out; v11++) {
               double v12 = v11 < v8.length ? v8[v11] : 0.5;
               double v14 = v11 < OUTPUT_WEIGHTS.length ? OUTPUT_WEIGHTS[v11] : 1.0;
               double v16 = (v7[v11] - v12) * v14;
               v9 += v16 * v16;
            }

            v4 += v9 / (double)this.out;
         }

         return v4 / (double)v3;
      }
   }

   public static pd deserialize(String data) {
      String[] v1 = data.split("\n");
      String[] v2 = v1[0].trim().split("\\s+");
      int v3 = Integer.parseInt(v2[0]);
      int v4 = Integer.parseInt(v2[1]);
      int v5 = Integer.parseInt(v2[2]);
      pd v6 = new pd(v3, v4, v5);
      double[] v7 = m196(v1[1]);
      int v8 = 0;

      for (int v9 = 0; v9 < v4; v9++) {
         for (int v10 = 0; v10 < v3; v10++) {
            if (v8 < v7.length) {
               double[] result = v6.w1[v9];

               result[v10] = v7[v8++];
            }
         }
      }

      double[] v17 = m196(v1[2]);

      for (int v18 = 0; v18 < v4 && v18 < v17.length; v18++) {
         v6.b1[v18] = v17[v18];
      }

      double[] v19 = m196(v1[3]);
      v8 = 0;

      for (int v11 = 0; v11 < v5; v11++) {
         for (int v12 = 0; v12 < v4; v12++) {
            if (v8 < v19.length) {
               v6.w2[v11][v12] = v19[v8++];
            }
         }
      }

      String v23 = v1[4];

      double[] v20 = m196(v23);

      for (int v21 = 0; v21 < v5 && v21 < v20.length; v21++) {
         v6.b2[v21] = v20[v21];
      }

      return v6;
   }

   public double train(double[][] X, double[][] Y, int epochs, double initialLr) {
      int v6 = X.length;
      if (v6 == 0) {

         return 0.0;
      } else {
         double v7 = 0.88;
         int[] v9 = new int[v6];
         int v10 = 0;

         while (v10 < v6) {
            v9[v10] = v10++;
         }

         double v16 = 0.0;

         for (int v12 = 0; v12 < epochs; v12++) {
            double v13 = initialLr * (1.0 - 0.65 * ((double)v12 / (double)epochs));
            v16 = this.trainEpoch(X, Y, v13, v7, v9);
         }

         return v16;
      }
   }

   public double trainEpoch(double[][] X, double[][] Y, double lr, double momentum, int[] perm) {
      int v8 = X.length;
      if (v8 == 0) {
         return 0.0;
      } else {
         for (int v9 = v8 - 1; v9 > 0; v9--) {
            int v10 = RND.nextInt(v9 + 1);
            int v11 = perm[v9];
            perm[v9] = perm[v10];
            perm[v10] = v11;
         }

         double v15 = 0.0;

         for (int v16 = 0; v16 < v8; v16++) {

            int v12 = perm[v16];
            v15 += this.m195(X[v12], Y[v12], lr, momentum);
         }

         double temp1 = (double)v8;

         return v15 / temp1;
      }
   }

   private double m195(double[] x, double[] y, double lr, double momentum) {
      double[] v7 = x;
      if (x.length != this.in) {
         v7 = new double[this.in];
         System.arraycopy(x, 0, v7, 0, Math.min(x.length, this.in));
      }

      double[] v8 = new double[this.hid];

      for (int v9 = 0; v9 < this.hid; v9++) {
         double v10 = this.b1[v9];

         for (int v12 = 0; v12 < this.in; v12++) {
            v10 += this.w1[v9][v12] * v7[v12];
         }

         v8[v9] = Math.tanh(v10);
      }

      double[] v24 = new double[this.out];

      for (int v25 = 0; v25 < this.out; v25++) {

         double v11 = this.b2[v25];

         for (int v13 = 0; v13 < this.hid; v13++) {

            v11 += this.w2[v25][v13] * v8[v13];
         }

         v24[v25] = m194(v11);
      }

      double v26 = 0.0;
      double[] v27 = new double[this.out];

      for (int v28 = 0; v28 < this.out; v28++) {
         double v14 = v28 < y.length ? y[v28] : 0.5;
         double v16 = v28 < OUTPUT_WEIGHTS.length ? OUTPUT_WEIGHTS[v28] : 1.0;
         double v18 = (v24[v28] - v14) * v16;
         v26 += v18 * v18;
         double v20 = v18 * v24[v28] * (1.0 - v24[v28]);
         v27[v28] = Math.max(-1.0, Math.min(1.0, v20));
      }

      double[] v29 = new double[this.hid];

      for (int v30 = 0; v30 < this.hid; v30++) {
         double v15 = 0.0;

         for (int v17 = 0; v17 < this.out; v17++) {
            v15 += v27[v17] * this.w2[v17][v30];
         }

         double v37 = v15 * (1.0 - v8[v30] * v8[v30]);
         v29[v30] = Math.max(-1.0, Math.min(1.0, v37));
      }

      for (int v31 = 0; v31 < this.out; v31++) {
         for (int v33 = 0; v33 < this.hid; v33++) {
            double v35 = v27[v31] * v8[v33];
            this.f164[v31][v33] = momentum * this.f164[v31][v33] - lr * v35;
            this.w2[v31][v33] = this.w2[v31][v33] + this.f164[v31][v33];
         }

         this.f165[v31] = momentum * this.f165[v31] - lr * v27[v31];
         this.b2[v31] = this.b2[v31] + this.f165[v31];
      }

      for (int v32 = 0; v32 < this.hid; v32++) {
         for (int v34 = 0; v34 < this.in; v34++) {
            double v36 = v29[v32] * v7[v34];
            this.f162[v32][v34] = momentum * this.f162[v32][v34] - lr * v36;
            this.w1[v32][v34] = this.w1[v32][v34] + this.f162[v32][v34];
         }

         this.f163[v32] = momentum * this.f163[v32] - lr * v29[v32];
         this.b1[v32] = this.b1[v32] + this.f163[v32];
      }

      return v26 / (double)this.out;
   }

   static {
      double[] result = new double[14];
      result[0] = 1.0;
      result[1] = 1.0;
      result[2] = 2.8;
      result[3] = 2.8;
      result[4] = 2.8;
      result[5] = 3.2;
      result[6] = 3.2;
      result[7] = 2.0;
      result[8] = 2.0;
      result[9] = 1.5;
      result[10] = 1.5;
      result[11] = 1.0;
      result[12] = 1.0;
      result[13] = 1.0;
      OUTPUT_WEIGHTS = result;
   }

   
}
