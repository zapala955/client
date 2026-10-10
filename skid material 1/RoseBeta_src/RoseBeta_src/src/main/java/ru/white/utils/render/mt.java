package ru.white.utils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class mt {
   private static final int SAMPLES_BRANCH = 64;
   private static final BufferAllocator internalAllocator = new BufferAllocator(262144);
   private static final Random random = new Random();
   public static final RenderLayer LIGHTNING_LINE_LAYER = RenderLayer.method_75940(
      "lightning_line",
      RenderSetup.method_75927(mt.LIGHTNING_LINE_PIPELINE).method_75929(32768).method_75938()
   );
   private static long f1591;
   private static final int SAMPLES_MAIN = 144;
   private static final int MAX_TRACKED = 5;
   public static final RenderPipeline LIGHTNING_LINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "lightning_line"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public static final RenderPipeline LIGHTNING_FILL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "lightning_fill"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final List<mt.nt> strikes = new ArrayList<>();
   private static final float BOLT_JAG = 0.088F;
   private static final float MAX_FRAME_DELTA = 0.1F;
   public static final RenderLayer LIGHTNING_FILL_LAYER = RenderLayer.method_75940(
      "lightning_fill",
      RenderSetup.method_75927(LIGHTNING_FILL_PIPELINE).method_75929(131072).method_75938()
   );
   public static boolean DEBUG = false;
   private static final float FLASH_WINDOW = 0.85F;
   private static final float BOLT_WINDOW = 0.45F;

   private static double[] m1925(double[][] pts, float t) {
      if (t <= 0.0F) {
         return pts[0];
      } else if (t >= 1.0F) {
         return pts[pts.length - 1];
      } else {
         float v2 = t * (float)(pts.length - 1);
         int v3 = (int)v2;
         float v4 = v2 - (float)v3;
         double[] v5 = pts[v3];
         double[] v6 = pts[Math.min(v3 + 1, pts.length - 1)];
         double[] result = new double[3];

         result[0] = v5[0] + (v6[0] - v5[0]) * (double)v4;
         result[1] = v5[1] + (v6[1] - v5[1]) * (double)v4;
         result[2] = v5[2] + (v6[2] - v5[2]) * (double)v4;
         return result;
      }
   }

   private mt() {
   }

   private static float m1920(float t, float seed, float nodes) {
      float v3 = t * nodes;
      float v4 = (float)Math.floor((double)v3);
      float v5 = v3 - v4;
      float result = m1919(v4 * 1.37F + seed);

      float v6 = result - 0.5F;
      float v7 = m1919((v4 + 1.0F) * 1.37F + seed) - 0.5F;
      float v12 = (v7 - v6) * v5;

      return (v6 + v12) * 2.0F;
   }

   private static void m1915(
      Matrix4f matrix,
      VertexConsumer fill,
      float ax0,
      float ay0,
      float az0,
      float ax1,
      float ay1,
      float az1,
      float offX,
      float offY,
      float offZ,
      int centerCol,
      int edgeCol
   ) {
      fill.method_22918(matrix, ax0 - offX, ay0 - offY, az0 - offZ).method_39415(edgeCol);

      fill.method_22918(matrix, ax0, ay0, az0).method_39415(centerCol);
      fill.method_22918(matrix, ax1, ay1, az1).method_39415(centerCol);
      fill.method_22918(matrix, ax1 - offX, ay1 - offY, az1 - offZ).method_39415(edgeCol);
      fill.method_22918(matrix, ax0, ay0, az0).method_39415(centerCol);
      fill.method_22918(matrix, ax0 + offX, ay0 + offY, az0 + offZ).method_39415(edgeCol);
      fill.method_22918(matrix, ax1 + offX, ay1 + offY, az1 + offZ).method_39415(edgeCol);
      fill.method_22918(matrix, ax1, ay1, az1).method_39415(centerCol);
   }

   private static float m1921(float t, float seed) {
      float result = m1920(t, seed, 7.0F) + m1920(t, seed + 19.3F, 19.0F) * 0.34F;
      float temp1 = m1920(t, seed + 41.7F, 47.0F);

      return result + temp1 * 0.11F;
   }

   private static double m1924(double x0, double y0, double z0, double x1, double y1, double z1) {
      double v12 = x1 - x0;
      double v14 = y1 - y0;
      int v18 = -1112020419;
      int temp2 = v18 * (v18 + 1) % 2;

      if (temp2 != 0) {
         }

      double v16 = z1 - z0;

      return Math.sqrt(v12 * v12 + v14 * v14 + v16 * v16);
   }

   private static void m1916(Matrix4f matrix, Vec3d camPos, VertexConsumer lines, double[][] pts, float brightness, float alphaMul, float taper) {
      int v7 = pts.length - 1;
      if (v7 >= 1) {
         double v8 = camPos.field_1352;
         double v10 = camPos.field_1351;
         double v12 = camPos.field_1350;

         for (int v14 = 0; v14 < v7; v14++) {
            double[] v15 = pts[v14];
            double[] v16 = pts[v14 + 1];
            float v17 = ((float)v14 + 0.5F) / (float)v7;
            int v27 = -1623554642;
            int result = v27 * (v27 + 1);

            float v18 = 1.0F - m1922(0.85F, 1.0F, v17) * taper * 0.5F;
            float v19 = brightness * alphaMul * v18;
            if (!(v19 <= 0.003F)) {

               float v20 = (float)(v15[0] - v8);
               float v21 = (float)(v15[1] - v10);
               float v22 = (float)(v15[2] - v12);
               float v23 = (float)(v16[0] - v8);
               float v24 = (float)(v16[1] - v10);
               float v25 = (float)(v16[2] - v12);
               int v26 = up.replAlpha(-1, v19 * 0.95F);
               lines.method_22918(matrix, v20, v21, v22).method_39415(v26);
               lines.method_22918(matrix, v23, v24, v25).method_39415(v26);
            }
         }
      }
   }

   public static void update() {
      long v0 = System.nanoTime();
      float v2 = f1591 == 0L ? 0.0F : (float)(v0 - f1591) / 1.0E9F;

      f1591 = v0;

      if (v2 < 0.0F) {
         v2 = 0.0F;
      }

      if (v2 > 0.1F) {
         v2 = 0.1F;
      }

      for (int v3 = strikes.size() - 1; v3 >= 0; v3--) {
         mt.nt v4 = strikes.get(v3);
         v4.f1590 += v2;
         if (v4.f1590 > 0.85F) {
            strikes.remove(v3);
         }
      }
   }

   private static float m1922(float edge0, float edge1, float x) {
      float v3 = (x - edge0) / (edge1 - edge0);
      if (v3 < 0.0F) {
         return 0.0F;
      } else if (v3 > 1.0F) {
         return 1.0F;
      } else {
         float result = v3 * v3;
         int v4 = 343378076;
         int temp1 = v4 * (v4 + 1) % 2;

         if (temp1 != 0) {
            }

         return result * (3.0F - 2.0F * v3);
      }
   }

   private static float m1918(mt.nt strike) {
      float v1 = strike.f1590;

      if (!(v1 < 0.0F) && !(v1 > 0.45F)) {
         float v2 = Math.min(1.0F, v1 / 0.01F);
         float v8 = -v1 * 6.2F;

         float v3 = (float)Math.exp((double)v8);

         float v4 = 0.42F + 0.58F * Math.abs((float)Math.sin((double)(v1 * 34.0F + strike.f1589)));
         return v2 * v3 * v4;
      } else {
         return 0.0F;
      }
   }

   public static void clear() {
      strikes.clear();

      f1591 = 0L;
      internalAllocator.method_60809();
   }

   public static void render(MatrixStack matrices, Camera camera, int boltColor, float width, int branches) {
      Immediate v5 = VertexConsumerProvider.method_22991(internalAllocator);
      render(matrices, camera, v5, boltColor, width, branches);
      
   }

   private static float m1919(float p) {
      p = m1923(p * 0.1031F);
      float result = p * (p + 33.33F);

      p = result;
      p *= p + p;
      return m1923(p);
   }

   private static double[][] m1913(
      double topX, double topY, double topZ, double bottomX, double bottomY, double bottomZ, float seed, float jag, int samples, boolean pinBottom
   ) {
      double v16 = m1924(topX, topY, topZ, bottomX, bottomY, bottomZ);
      if (v16 < 1.0E-5) {
         return new double[][]{{topX, topY, topZ}, {bottomX, bottomY, bottomZ}};
      } else {
         double v18 = (bottomX - topX) / v16;
         double v20 = (bottomY - topY) / v16;
         double v22 = (bottomZ - topZ) / v16;
         double v24;
         double v26;
         double v28;
         if (Math.abs(v20) < 0.92) {
            double v32 = -v18;
            double v34 = Math.sqrt(v22 * v22 + v32 * v32);
            v24 = v22 / v34;
            v26 = 0.0;
            v28 = v32 / v34;
         } else {
            double v30 = -v22;
            double v55 = Math.sqrt(v30 * v30 + v20 * v20);
            v24 = 0.0;
            v26 = v30 / v55;
            v28 = v20 / v55;
         }

         double v53 = v26 * v22 - v28 * v20;
         double v54 = v28 * v18 - v24 * v22;
         double v56 = v24 * v20 - v26 * v18;
         double[][] v36 = new double[samples + 1][3];

         for (int v37 = 0; v37 <= samples; v37++) {
            float v38 = (float)v37 / (float)samples;
            double v39 = topX + (bottomX - topX) * (double)v38;
            double v41 = topY + (bottomY - topY) * (double)v38;
            double v43 = topZ + (bottomZ - topZ) * (double)v38;
            float v45 = m1922(0.0F, 0.09F, v38);
            float result;
            if (pinBottom) {
               float temp2 = 1.0F - v38;

               result = m1922(0.0F, 0.045F, temp2);
            } else {
               result = 1.0F;
            }

            float v46 = result;
            float v47 = v45 * v46;
            float v48 = m1921(v38, seed) * jag * (float)v16 * v47;
            float v49 = m1921(v38, seed + 53.71F) * jag * (float)v16 * v47 * 0.85F;
            result = m1921(v38, seed + 107.1F) * 0.018F * (float)v16;

            float v50 = result * v47;
            v36[v37][0] = v39 + v18 * (double)v50 + v24 * (double)v48 + v53 * (double)v49;
            v36[v37][1] = v41 + v20 * (double)v50 + v26 * (double)v48 + v54 * (double)v49;
            v36[v37][2] = v43 + v22 * (double)v50 + v28 * (double)v48 + v56 * (double)v49;
         }

         return v36;
      }
   }

   private static float m1923(float x) {
      return x - (float)Math.floor((double)x);
   }

   public static void render(MatrixStack matrices, Camera camera, VertexConsumer fill, VertexConsumer lines, int boltColor, float width, int branches) {
      if (matrices != null && camera != null && fill != null) {
         update();
         if (!strikes.isEmpty()) {
            if (width < 0.1F) {
               width = 0.1F;
            }

            if (branches < 0) {
               branches = 0;
            }

            if (branches > 4) {
               branches = 4;
            }

            Vec3d v7 = camera.method_71156();
            ArrayList v8 = new ArrayList();

            for (mt.nt v10 : strikes) {

               float v11 = m1918(v10);
               if (!(v11 <= 0.003F)) {
                  double[][] v12 = m1913(
                     v10.f1583, v10.f1584, v10.f1585, v10.f1586, v10.f1587, v10.f1588, v10.f1589, 0.088F, 144, true
                  );
                  ArrayList v13 = new ArrayList();
                  double v14 = m1924(v10.f1583, v10.f1584, v10.f1585, v10.f1586, v10.f1587, v10.f1588);
                  if (v14 >= 0.001) {
                     double v16 = (v10.f1586 - v10.f1583) / v14;
                     double v18 = (v10.f1587 - v10.f1584) / v14;
                     double v20 = (v10.f1588 - v10.f1585) / v14;
                     double v22 = v20;
                     double v24 = -v16;
                     double v26 = Math.sqrt(v20 * v20 + v24 * v24);
                     if (v26 < 1.0E-5) {
                        v22 = 1.0;
                        v24 = 0.0;
                        v26 = 1.0;
                     }

                     v22 /= v26;
                     v24 /= v26;

                     for (int v28 = 0; v28 < branches; v28++) {
                        float v29 = (float)v28;
                        float v30 = 0.18F + 0.64F * m1919(v10.f1589 + v29 * 7.31F + 3.1F);
                        float v31 = m1919(v10.f1589 + v29 * 13.77F) * 6.283185F;
                        float v32 = 0.18F + 0.26F * m1919(v10.f1589 + v29 * 3.19F + 8.4F);
                        double[] v33 = m1925(v12, v30);
                        double v34 = v16 * 0.75 + v22 * Math.cos((double)v31) * 0.8;
                        double v36 = v18 * 0.75;
                        double v38 = v20 * 0.75 + v24 * Math.sin((double)v31) * 0.8;
                        double v40 = Math.sqrt(v34 * v34 + v36 * v36 + v38 * v38);
                        if (!(v40 < 1.0E-5)) {
                           double v42 = (double)v32 * v14;
                           double v44 = v33[0] + v34 / v40 * v42;
                           double v46 = v33[1] + v36 / v40 * v42;
                           double v48 = v33[2] + v38 / v40 * v42;
                           double[][] v50 = m1913(
                              v33[0], v33[1], v33[2], v44, v46, v48, v10.f1589 + v29 * 31.7F + 11.0F, 0.1188F, 64, false
                           );
                           v13.add(v50);
                        }
                     }
                  }

                  v8.add(new mt.ot(v10, v11, v12, v13));
               }
            }

            if (!v8.isEmpty()) {
               matrices.method_22903();
               Matrix4f v52 = matrices.method_23760().method_23761();

               for (mt.ot v55 : v8) {
                  m1914(v52, v7, fill, v55.mainChannel, boltColor, width, v55.brightness, 1.0F, 0.35F);

                  for (double[][] v59 : v55.branches) {
                     m1914(v52, v7, fill, v59, boltColor, width * 0.55F, v55.brightness * 0.7F, 0.85F, 1.0F);
                  }

                  m1917(v52, v7, fill, v55.strike.f1586, v55.strike.f1587, v55.strike.f1588, v55.strike.f1590, boltColor, width, v55.brightness);
               }

               if (lines != null) {
                  for (mt.ot v56 : v8) {
                     m1916(v52, v7, lines, v56.mainChannel, v56.brightness, 1.0F, 0.35F);

                     for (double[][] v60 : v56.branches) {
                        m1916(v52, v7, lines, v60, v56.brightness * 0.7F, 0.85F, 1.0F);
                     }
                  }
               }

               matrices.method_22909();
            }
         }
      }
   }

   public static void render(MatrixStack matrices, Camera camera, Immediate immediate, int boltColor, float width, int branches) {
      if (matrices != null && camera != null && immediate != null) {
         update();
         if (!strikes.isEmpty()) {
            if (width < 0.1F) {
               width = 0.1F;
            }

            if (branches < 0) {
               branches = 0;
            }

            if (branches > 4) {
               branches = 4;
            }

            Vec3d v6 = camera.method_71156();
            ArrayList v7 = new ArrayList();

            for (mt.nt v9 : strikes) {
               float v10 = m1918(v9);
               if (!(v10 <= 0.003F)) {
                  double[][] v11 = m1913(
                     v9.f1583, v9.f1584, v9.f1585, v9.f1586, v9.f1587, v9.f1588, v9.f1589, 0.088F, 144, true
                  );
                  ArrayList v12 = new ArrayList();
                  double v13 = m1924(v9.f1583, v9.f1584, v9.f1585, v9.f1586, v9.f1587, v9.f1588);
                  if (v13 >= 0.001) {
                     double v15 = (v9.f1586 - v9.f1583) / v13;
                     double v17 = (v9.f1587 - v9.f1584) / v13;
                     double v19 = (v9.f1588 - v9.f1585) / v13;
                     double v21;
                     double v23;
                     double v25;
                     if (Math.abs(v17) < 0.92) {
                        double v29 = -v15;
                        double v31 = Math.sqrt(v19 * v19 + v29 * v29);
                        v21 = v19 / v31;
                        v23 = 0.0;
                        v25 = v29 / v31;
                     } else {
                        double v27 = 0.0;
                        double v96 = -v19;
                        double v33 = Math.sqrt(v96 * v96 + v17 * v17);
                        v21 = v27 / v33;
                        v23 = v96 / v33;
                        v25 = v17 / v33;
                     }

                     double v95 = v23 * v19 - v25 * v17;
                     double v97 = v25 * v15 - v21 * v19;
                     double v98 = v21 * v17 - v23 * v15;

                     for (int v99 = 0; v99 < branches; v99++) {
                        float v34 = (float)v99;
                        float temp2 = v9.f1589 + v34 * 7.31F;

                        float v35 = 0.18F + 0.64F * m1919(temp2 + 3.1F);
                        float v36 = m1919(v9.f1589 + v34 * 13.77F) * 6.283185F;
                        float v37 = 0.18F + 0.26F * m1919(v9.f1589 + v34 * 3.19F + 8.4F);
                        double[] v38 = m1925(v11, v35);
                        double result = v21 * Math.cos((double)v36);
                        double var100 = Math.sin((double)v36);

                        double v39 = result + v95 * var100;
                        double v41 = v23 * Math.cos((double)v36) + v97 * Math.sin((double)v36);
                        double v43 = v25 * Math.cos((double)v36) + v98 * Math.sin((double)v36);
                        double v45 = 0.65 + 0.35 * (double)m1919(v9.f1589 + v34 * 5.11F);
                        double v47 = v15 * 0.75 + v39 * v45;
                        double v49 = v17 * 0.75 + v41 * v45;
                        double v51 = v19 * 0.75 + v43 * v45;
                        double v53 = Math.sqrt(v47 * v47 + v49 * v49 + v51 * v51);
                        if (!(v53 < 1.0E-5)) {
                           double v55 = (double)v37 * v13;
                           double v57 = v38[0] + v47 / v53 * v55;
                           double v59 = v38[1] + v49 / v53 * v55;
                           double v61 = v38[2] + v51 / v53 * v55;
                           double[][] v63 = m1913(
                              v38[0], v38[1], v38[2], v57, v59, v61, v9.f1589 + v34 * 31.7F + 11.0F, 0.1188F, 64, false
                           );
                           v12.add(v63);
                           if (branches >= 3 && v99 == 0) {
                              double[] v64 = m1925(v63, 0.45F);
                              double v65 = (double)(v36 + 1.2F);
                              double v67 = v21 * Math.cos(v65) + v95 * Math.sin(v65);
                              double v69 = v23 * Math.cos(v65) + v97 * Math.sin(v65);

                              double v71 = v25 * Math.cos(v65) + v98 * Math.sin(v65);
                              double v73 = v55 * 0.42;
                              double v75 = v64[0] + v67 * v73;
                              double v77 = v64[1] + (v17 * 0.5 + v69 * 0.5) * v73;
                              double v79 = v64[2] + v71 * v73;
                              double[][] v81 = m1913(
                                 v64[0], v64[1], v64[2], v75, v77, v79, v9.f1589 + 89.3F, 0.1408F, 32, false
                              );
                              v12.add(v81);
                           }
                        }
                     }
                  }

                  v7.add(new mt.ot(v9, v10, v11, v12));
               }
            }

            if (!v7.isEmpty()) {
               matrices.method_22903();
               Matrix4f v85 = matrices.method_23760().method_23761();
               VertexConsumer v86 = immediate.method_73477(LIGHTNING_FILL_LAYER);

               for (mt.ot v89 : v7) {
                  m1914(v85, v6, v86, v89.mainChannel, boltColor, width, v89.brightness, 1.0F, 0.35F);

                  for (double[][] v93 : v89.branches) {
                     m1914(v85, v6, v86, v93, boltColor, width * 0.55F, v89.brightness * 0.7F, 0.85F, 1.0F);
                  }

                  m1917(v85, v6, v86, v89.strike.f1586, v89.strike.f1587, v89.strike.f1588, v89.strike.f1590, boltColor, width, v89.brightness);
               }

               immediate.method_22994(LIGHTNING_FILL_LAYER);
               VertexConsumer v88 = immediate.method_73477(LIGHTNING_LINE_LAYER);

               for (mt.ot v92 : v7) {
                  m1916(v85, v6, v88, v92.mainChannel, v92.brightness, 1.0F, 0.35F);

                  for (double[][] v14 : v92.branches) {
                     m1916(v85, v6, v88, v14, v92.brightness * 0.7F, 0.85F, 1.0F);
                  }
               }

               immediate.method_22994(LIGHTNING_LINE_LAYER);
               matrices.method_22909();
            }
         }
      }
   }

   public static void strikeAt(Vec3d impactPos) {
      if (impactPos != null) {
         if (strikes.size() >= 5) {
            strikes.remove(0);
         }

         mt.nt v1 = new mt.nt();
         v1.f1589 = random.nextFloat() * 900.0F + 10.0F;
         double v2 = random.nextDouble() * Math.PI * 2.0;
         double v4 = 0.8 + random.nextDouble() * 2.2;
         double temp1 = impactPos.field_1352 + Math.cos(v2) * v4;

         v1.f1583 = temp1;
         v1.f1584 = impactPos.field_1351 + 12.0 + random.nextDouble() * 6.0;
         v1.f1585 = impactPos.field_1350 + Math.sin(v2) * v4;
         v1.f1586 = impactPos.field_1352;
         v1.f1587 = impactPos.field_1351;
         v1.f1588 = impactPos.field_1350;
         v1.f1590 = 0.0F;
         strikes.add(v1);
         if (DEBUG) {
            PrintStream result = System.out;
            String v10 = String.format(
               "%.1f, %.1f, %.1f", impactPos.field_1352, impactPos.field_1351, impactPos.field_1350
            );
            int v6 = strikes.size();
            String v7 = v10;
            StringBuilder v11 = new StringBuilder();

            result.println(
               v11.append("[TargetLightning] strikeAt impact=(")
                  .append((Object)v7)
                  .append(") total=")
                  .append(v6)
                  .toString()
            );
         }
      }
   }

   public static boolean hasActive() {
      update();
      int v2 = -96328390;
      int v3 = 420457407;

      for (mt.nt v1 : strikes) {
         if (v1.f1590 <= 0.45F) {
            return true;
         }
      }

      return false;
   }

   private static void m1914(
      Matrix4f matrix, Vec3d camPos, VertexConsumer fill, double[][] pts, int boltColor, float width, float brightness, float alphaMul, float taper
   ) {
      int v71 = 418836441;
      int temp1 = v71 * (v71 + 1);

      int v9 = pts.length - 1;
      if (v9 >= 1) {
         double v10 = camPos.field_1352;
         double v12 = camPos.field_1351;
         double v14 = camPos.field_1350;

         for (int v16 = 0; v16 < v9; v16++) {
            double[] v17 = pts[v16];
            double[] v18 = pts[v16 + 1];
            float v19 = ((float)v16 + 0.5F) / (float)v9;
            float v20 = 1.0F - 0.7F * taper * v19;
            float v21 = 1.0F - m1922(0.85F, 1.0F, v19) * taper * 0.5F;
            float v22 = brightness * alphaMul * v21;
            if (!(v22 <= 0.003F)) {
               double v23 = (v17[0] + v18[0]) * 0.5;
               double v25 = (v17[1] + v18[1]) * 0.5;
               double v27 = (v17[2] + v18[2]) * 0.5;
               double v29 = v18[0] - v17[0];
               double v31 = v18[1] - v17[1];
               double v33 = v18[2] - v17[2];
               double v35 = v10 - v23;
               double v37 = v12 - v25;
               double v39 = v14 - v27;

               double v41 = Math.sqrt(v35 * v35 + v37 * v37 + v39 * v39);
               if (v41 < 0.01) {
                  v41 = 0.01;
               }

               double v43 = v31 * v39 - v33 * v37;
               double v45 = v33 * v35 - v29 * v39;
               double v47 = v29 * v37 - v31 * v35;
               double v49 = Math.sqrt(v43 * v43 + v45 * v45 + v47 * v47);
               if (v49 < 1.0E-6) {
                  v43 = 1.0;
                  v45 = 0.0;
                  v47 = 0.0;
                  v49 = 1.0;
               }

               double v51 = 1.0 / v49;
               v43 *= v51;
               v45 *= v51;
               v47 *= v51;
               float v53 = (float)Math.max(0.045, Math.min(0.26, v41 * 0.0125));
               float v54 = width / 1.6F;
               float v55 = v53 * v54 * v20;
               float v56 = v55 * 3.8F;
               float v57 = v55 * 1.15F;
               float v58 = v55 * 0.28F;
               float v59 = (float)(v17[0] - v10);
               float v60 = (float)(v17[1] - v12);
               float v61 = (float)(v17[2] - v14);
               float v62 = (float)(v18[0] - v10);
               float v63 = (float)(v18[1] - v12);
               float v64 = (float)(v18[2] - v14);
               int v65 = up.replAlpha(boltColor, v22 * 0.24F);
               int v66 = up.replAlpha(boltColor, 0.0F);
               m1915(
                  matrix,
                  fill,
                  v59,
                  v60,
                  v61,
                  v62,
                  v63,
                  v64,
                  (float)(v43 * (double)v56),
                  (float)(v45 * (double)v56),
                  (float)(v47 * (double)v56),
                  v65,
                  v66
               );
               int v67 = up.replAlpha(boltColor, v22 * 0.72F);
               int v68 = up.replAlpha(boltColor, 0.0F);
               m1915(
                  matrix,
                  fill,
                  v59,
                  v60,
                  v61,
                  v62,
                  v63,
                  v64,
                  (float)(v43 * (double)v57),
                  (float)(v45 * (double)v57),
                  (float)(v47 * (double)v57),
                  v67,
                  v68
               );
               int v69 = up.replAlpha(-1, v22 * 0.98F);
               int v70 = up.replAlpha(boltColor, 0.0F);
               m1915(
                  matrix,
                  fill,
                  v59,
                  v60,
                  v61,
                  v62,
                  v63,
                  v64,
                  (float)(v43 * (double)v58),
                  (float)(v45 * (double)v58),
                  (float)(v47 * (double)v58),
                  v69,
                  v70
               );
            }
         }
      }
   }

   private static void m1917(
      Matrix4f matrix,
      Vec3d camPos,
      VertexConsumer fill,
      double impactX,
      double impactY,
      double impactZ,
      float age,
      int boltColor,
      float width,
      float brightness
   ) {
      if (!(age > 0.28F)) {
         float v13 = age / 0.28F;
         float v14 = (1.0F - v13) * brightness;
         float v15 = (0.35F + 0.95F * v13) * Math.max(0.8F, width * 0.55F);
         float v16 = (float)(impactX - camPos.field_1352);
         float v17 = (float)(impactY - camPos.field_1351);
         float v18 = (float)(impactZ - camPos.field_1350);
         int v19 = up.replAlpha(boltColor, v14 * 0.55F);

         int v20 = up.replAlpha(boltColor, 0.0F);
         int v21 = up.replAlpha(-1, v14 * 0.9F);
         int v22 = 12;

         for (int v23 = 0; v23 < v22; v23++) {
            float v24 = (float)((double)v23 * 2.0 * Math.PI / (double)v22);
            float v25 = (float)((double)(v23 + 1) * 2.0 * Math.PI / (double)v22);

            float v26 = (float)Math.cos((double)v24);
            float v27 = (float)Math.sin((double)v24);
            float v28 = (float)Math.cos((double)v25);
            float v29 = (float)Math.sin((double)v25);
            VertexConsumer result = fill.method_22918(matrix, v16, v17, v18);

            result.method_39415(v21);
            fill.method_22918(matrix, v16 + v26 * v15, v17, v18 + v27 * v15).method_39415(v20);
            fill.method_22918(matrix, v16 + v28 * v15, v17, v18 + v29 * v15).method_39415(v20);
            fill.method_22918(matrix, v16, v17, v18).method_39415(v21);
         }

         float v33 = v15 * 0.65F;
         fill.method_22918(matrix, v16 - v33, v17, v18).method_39415(v20);
         fill.method_22918(matrix, v16, v17 + v33 * 1.4F, v18).method_39415(v21);
         fill.method_22918(matrix, v16 + v33, v17, v18).method_39415(v20);
         fill.method_22918(matrix, v16, v17 - v33 * 0.5F, v18).method_39415(v20);
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class nt {
      private float f1590;
      private double f1587;
      private double f1584;
      private double f1586;
      private float f1589;
      private double f1585;
      private double f1588;
      private double f1583;

      
   }

   @Environment(EnvType.CLIENT)
   private static final class ot {
      final double[][] mainChannel;
      final mt.nt strike;
      final float brightness;
      final List<double[][]> branches;

      ot(mt.nt strike, float brightness, double[][] mainChannel, List<double[][]> branches) {
         this.strike = strike;
         this.brightness = brightness;
         this.mainChannel = mainChannel;
         this.branches = branches;
      }

      
   }
}
