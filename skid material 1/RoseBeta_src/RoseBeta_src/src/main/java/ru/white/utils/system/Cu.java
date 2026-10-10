package ru.white.utils.system;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;
import ru.white.utils.misc.Sp;
import ru.white.auth.StrDec;
import ru.white.utils.render.wr;

@Environment(EnvType.CLIENT)
public class Cu implements wr.xr {
   private zt f1646;
   private static final RenderPipeline PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation(cu.PIPELINE_ID)
         .withVertexShader(cu.SHADER_ID)
         .withFragmentShader(cu.SHADER_ID)
         .withVertexFormat(VertexFormats.field_60033, DrawMode.field_27379)
         .withUniform("FontData", UniformType.field_60031)
         .withSampler("Sampler0")
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false)
         .build()
   );
   private static final Map<Integer, Integer> SMALL_CAPS_MAP;
   private int f1645;
   private GpuBuffer f1641;
   private int f1640 = 0;
   private GpuBuffer[] f1639;
   private static final int[] LEGACY_COLORS = new int[32];
   private static final int BUFFER_SIZE = 16448;
   private final ArrayList<cu.fu> f1649;
   private static final String LEGACY_CODE_CHARS;
   private static final Map<String, String> preprocessCache;
   private int f1648;
   private ByteBuffer f1642;
   private static final float FIXED_GUI_SCALE = 2.0F;
   private static final int LIGHTNING_CP;
   private static final int HEADER_SIZE = 64;
   private static final int UNIFORM_RING = 32;
   private final Map<GpuTexture, GpuTextureView> f1644;
   private static final Identifier PIPELINE_ID = Identifier.method_60655("client", "pipeline/msdf");
   private static final int MAX_CHARS = 256;
   private static final Identifier SHADER_ID = Identifier.method_60655("client", "core/msdf");
   private static final int PREPROCESS_CACHE_SIZE = 256;
   private boolean f1643 = false;
   private float f1647;

   public void drawText(zt atlas, String text, float x, float y, float size, 

      this.drawText(atlas, text, x, y, size, color, 0.0F, 0, 0.0F);
   }

   private float m1968(zt atlas, String text) {
      float v3 = 0.0F;
      float v4 = 0.0F;
      int v5 = 0;

      while (v5 < text.length()) {
         cu.eu v6 = this.m1971(text, v5);
         if (v6.matched()) {
            v5 += v6.skip();
         } else {
            int v7 = text.codePointAt(v5);
            int v8 = Character.charCount(v7);
            if (v7 == 10) {
               v4 = Math.max(v4, v3);
               v3 = 0.0F;
               v5 += v8;
            } else {
               iu v9 = atlas.getGlyph(v7);
               if (v9 != null) {
                  v3 += v9.xAdvance;
               } else {
                  iu v10 = atlas.getGlyph(63);
                  if (v10 != null) {

                     v3 += v10.xAdvance;
                  } else {
                     v3 += atlas.getFontSize() * 0.5F;
                  }
               }

               v5 += v8;
            }
         }
      }

      return Math.max(v4, v3);
   }

   static {
      for (int v0 = 0; v0 < 16; v0++) {
         int v1 = (v0 >> 3 & 1) * (85);
         int v2 = (v0 >> 2 & 1) * (170) + v1;
         int v3 = (v0 >> 1 & 1) * (170) + v1;
         int v4 = (v0 & 1) * (170) + v1;
         if (v0 == 6) {
            v2 += 85;
         }

         LEGACY_COLORS[v0] = -16777216 | v2 << 16 | v3 << 8 | v4;
         LEGACY_COLORS[v0 + 16] = (v2 & 16579836) >> 2 << 24
            | v2 << 16
            | v3 << 8
            | v4;
      }

      char[] temp2 = new char[22];
      temp2[0] = (char)(48);
      temp2[1] = (char)(49);
      temp2[2] = (char)(50);
      temp2[3] = (char)(51);
      temp2[4] = (char)(52);
      temp2[5] = (char)(53);
      temp2[6] = (char)(54);
      temp2[7] = (char)(55);
      temp2[8] = (char)(56);
      temp2[9] = (char)(57);
      temp2[10] = (char)(97);
      temp2[11] = (char)(98);
      temp2[12] = (char)(99);
      temp2[13] = (char)(100);
      temp2[14] = (char)(101);
      temp2[15] = (char)(102);
      temp2[16] = (char)(107);
      temp2[17] = (char)(108);
      temp2[18] = (char)(109);
      temp2[19] = (char)(110);
      temp2[20] = (char)(111);
      temp2[21] = (char)(114);
      LEGACY_CODE_CHARS = new String(temp2);
      preprocessCache = new LinkedHashMap<String, String>(257, 0.75F, true) {
         @Override
         protected boolean removeEldestEntry(Entry<String, String> eldest) {
            int result = this.size();

            return result > 256;
         }

         
      };
      SMALL_CAPS_MAP = new HashMap<>();
      SMALL_CAPS_MAP.put(7424, 97);
      SMALL_CAPS_MAP.put(665, 98);
      SMALL_CAPS_MAP.put(7428, 99);
      SMALL_CAPS_MAP.put(7429, 100);
      SMALL_CAPS_MAP.put(7431, 101);
      SMALL_CAPS_MAP.put(1171, 102);
      SMALL_CAPS_MAP.put(610, 103);
      SMALL_CAPS_MAP.put(668, 104);
      SMALL_CAPS_MAP.put(618, 105);
      SMALL_CAPS_MAP.put(7434, 106);
      SMALL_CAPS_MAP.put(7435, 107);
      SMALL_CAPS_MAP.put(671, 108);
      SMALL_CAPS_MAP.put(7437, 109);
      SMALL_CAPS_MAP.put(628, 110);
      SMALL_CAPS_MAP.put(7439, 111);
      SMALL_CAPS_MAP.put(7448, 112);
      SMALL_CAPS_MAP.put(491, 113);
      SMALL_CAPS_MAP.put(640, 114);
      SMALL_CAPS_MAP.put(7451, 116);
      SMALL_CAPS_MAP.put(7452, 117);
      SMALL_CAPS_MAP.put(7456, 118);
      SMALL_CAPS_MAP.put(7457, 119);
      SMALL_CAPS_MAP.put(655, 121);
      SMALL_CAPS_MAP.put(7458, 122);
      LIGHTNING_CP = "⚡".codePointAt(0);
   }

   private cu.eu m1971(String text, int index) {
      cu.eu result = this.m1970(text, index, 0, 0);

      return result;
   }

   private void m1966() {
      if (this.f1645 != 0 && this.f1646 != null) {
         MinecraftClient v1 = MinecraftClient.method_1551();
         if (v1.method_1522() == null) {
            this.f1645 = 0;
            this.f1646 = null;
         } else {
            AbstractTexture v2 = v1.method_1531().method_4619(this.f1646.getTextureId());
            if (v2 == null) {

               this.f1645 = 0;
               this.f1646 = null;
            } else {
               GpuTexture v3;
               try {
                  v3 = v2.method_68004();
               } catch (Exception v9) {
                  this.f1645 = 0;
                  this.f1646 = null;
                  return;
               }

               if (this.f1649.size() >= 31) {
                  wr.drawImmediate(this, false);
               }

               int v4 = this.f1645;
               int v5 = this.f1642.position();
               this.f1642.position(0);
               this.f1642.putFloat((float)this.m1962());
               this.f1642.putFloat((float)this.m1963());
               this.f1642.putFloat(2.0F);
               this.f1642.putFloat(this.f1647);
               this.f1642.putFloat((float)(this.f1648 >> 16 & 255) / 255.0F);
               this.f1642.putFloat((float)(this.f1648 >> 8 & 255) / 255.0F);
               this.f1642.putFloat((float)(this.f1648 & 255) / 255.0F);
               this.f1642.putFloat((float)(this.f1648 >> 24 & 255) / 255.0F);
               this.f1642.putFloat(this.f1646.getAtlasWidth());
               this.f1642.putFloat(this.f1646.getAtlasHeight());
               this.f1642.putFloat(this.f1646.getDistanceRange());
               this.f1642.putFloat(this.f1646.getFontSize());
               this.f1642.putInt(v4);
               this.f1642.putInt(0);
               this.f1642.putInt(0);
               this.f1642.putInt(0);
               this.f1642.position(0);
               this.f1642.limit(v5);
               GpuBuffer v6 = this.m1965();
               CommandEncoder result = RenderSystem.getDevice().createCommandEncoder();

               result.writeToBuffer(v6.slice(0L, (long)this.f1642.remaining()), this.f1642);
               this.f1642.limit(this.f1642.capacity());
               this.f1649.add(new cu.fu(v6, this.m1967(v3), v4));
               this.f1645 = 0;
               this.f1646 = null;
            }
         }
      } else {
         this.f1645 = 0;
         this.f1646 = null;
      }
   }

   @Override
   public void drawBatch(RenderPass pass) {
      if (!this.f1649.isEmpty()) {
         GpuSampler v2 = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
         pass.setPipeline(PIPELINE);
         pass.setVertexBuffer(0, this.f1641);
         Iterator v3 = this.f1649.iterator();

         while (v3.hasNext()) {
            cu.fu v4 = (cu.fu)v3.next();

            if (!v4.view().isClosed()) {
               pass.bindTexture("Sampler0", v4.view(), v2);
               pass.setUniform("FontData", v4.buffer());

               pass.draw(0, v4.count() * (6));
            }
         }

         this.f1649.clear();
      }
   }

   public void flush() {
      this.m1966();

      if (!wr.isEnabled() && !this.f1649.isEmpty()) {
         wr.drawImmediate(this, false);
      }
   }

   public void drawText(zt atlas, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor, float rotation) {
      MinecraftClient v10 = MinecraftClient.method_1551();
      if (v10.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            text = this.m1969(text);
            text = sp.get(text);
            atlas.ensureLoaded();
            int result = atlas.getGlyphCount();

            if (result != 0) {
               this.m1964();
               if (wr.isEnabled()) {
                  wr.register(this);
               }

               if (this.f1646 != null && (this.f1646 != atlas || this.f1647 != outlineWidth || this.f1648 != outlineColor)) {
                  this.flush();
               }

               this.f1646 = atlas;
               this.f1647 = outlineWidth;
               this.f1648 = outlineColor;
               float v11 = size / atlas.getFontSize();
               float v12 = x;
               float v13 = y;
               float v14 = (float)Math.toRadians((double)rotation);
               float v15 = 0.0F;
               float v16 = 0.0F;
               if (rotation != 0.0F) {
                  v15 = x + this.getTextWidth(atlas, text, size) / 2.0F;
                  v16 = y + this.getTextHeight(atlas, text, size) / 2.0F;
               }

               int v17 = color;
               int v18 = 0;

               while (v18 < text.length()) {
                  cu.eu v19 = this.m1970(text, v18, color, v17);
                  if (v19.matched()) {
                     v17 = v19.color();
                     v18 += v19.skip();
                  } else {
                     int v20 = text.codePointAt(v18);
                     int v21 = Character.charCount(v20);
                     if (v20 == 10) {
                        v12 = x;
                        v13 += atlas.getLineHeight() * v11;
                        v18 += v21;
                     } else {
                        iu v22 = atlas.getGlyph(v20);
                        if (v22 == null) {
                           iu v23 = atlas.getGlyph(63);
                           if (v23 != null) {
                              v12 += v23.xAdvance * v11;
                           } else {
                              v12 += size * 0.5F;
                           }

                           v18 += v21;
                        } else {
                           float v30 = v12 + v22.xOffset * v11;
                           float v24 = v13 + v22.yOffset * v11;
                           float v25 = v22.width * v11;
                           float v26 = v22.height * v11;
                           if (v22.width > 0.0F && v22.height > 0.0F) {
                              this.m1961(v30, v24, v25, v26, v22.u0, v22.v0, v22.u1, v22.v1, v17, v14, v15, v16, v11);
                           }

                           v12 += v22.xAdvance * v11;
                           if (this.f1645 >= 256) {
                              this.flush();
                              this.f1646 = atlas;
                           }

                           v18 += v21;
                        }
                     }
                  }
               }

               if (!wr.isEnabled()) {
                  this.flush();
               }
            }
         }
      }
   }

   public float getTextWidth(zt atlas, String text, float size) {
      atlas.ensureLoaded();
      float v4 = size / atlas.getFontSize();
      text = this.m1969(text);
      Float v5 = atlas.widthCache.get(text);

      if (v5 != null) {
         return v5 * v4;
      } else {
         String v6 = sp.stripForWidth(text);

         Float v7 = v6.equals(text) ? null : atlas.widthCache.get(v6);
         if (v7 == null) {
            v7 = this.m1968(atlas, v6);
            atlas.widthCache.put(v6, v7);
         }

         atlas.widthCache.put(text, v7);
         return v7 * v4;
      }
   }

   public void drawTextRightAlignFadeLeft(zt atlas, String text, float rightX, float y, float size, float fadeStartX, int color) {
      MinecraftClient v8 = MinecraftClient.method_1551();
      if (v8.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            float v9 = this.getTextWidth(atlas, text, size);
            float v10 = rightX - v9;
            if (v10 >= fadeStartX) {

               this.drawText(atlas, text, v10, y, size, color, 0.0F, 0, 0.0F);
            } else {
               int v11 = color >> 24 & 255;
               if (v11 != 0) {
                  int v12 = color & 16777215;
                  float v13 = Math.min(32.0F, v9 * 0.45F);
                  if (v13 < 1.0F) {
                     v13 = 1.0F;
                  }

                  text = this.m1969(text);
                  text = sp.get(text);
                  atlas.ensureLoaded();
                  if (atlas.getGlyphCount() != 0) {
                     this.m1964();
                     if (wr.isEnabled()) {
                        wr.register(this);
                     }

                     if (this.f1646 != null && (this.f1646 != atlas || this.f1647 != 0.0F || this.f1648 != 0)) {
                        this.flush();
                     }

                     this.f1646 = atlas;
                     this.f1647 = 0.0F;
                     this.f1648 = 0;
                     float v14 = size / atlas.getFontSize();
                     float v15 = 0.0F;
                     int v16 = 0;

                     while (v16 < text.length()) {
                        cu.eu v17 = this.m1971(text, v16);
                        if (v17.matched()) {
                           v16 += v17.skip();
                        } else {
                           int v18 = text.codePointAt(v16);
                           int v19 = Character.charCount(v18);
                           iu v20 = atlas.getGlyph(v18);
                           float v21;
                           if (v20 != null) {
                              v21 = v20.xAdvance * v14;
                           } else {
                              iu v22 = atlas.getGlyph(63);
                              float result;
                              if (v22 != null) {
                                 result = v22.xAdvance * v14;
                              } else {

                                 result = size * 0.5F;
                              }

                              v21 = result;
                           }

                           float v31 = v10 + v15;
                           float v23 = v31 + v21;
                           int v24 = v11;

                           if (v23 <= fadeStartX) {
                              v24 = 0;
                           } else if (v31 < fadeStartX + v13) {
                              float v25 = (v23 - fadeStartX) / v13;
                              if (v25 < 0.0F) {
                                 v25 = 0.0F;
                              }

                              if (v25 > 1.0F) {
                                 v25 = 1.0F;
                              }

                              v24 = (int)((float)v11 * v25);
                           }

                           if (v24 > 0 && v20 != null && v20.width > 0.0F && v20.height > 0.0F) {
                              this.m1961(
                                 v31 + v20.xOffset * v14,
                                 y + v20.yOffset * v14,
                                 v20.width * v14,
                                 v20.height * v14,
                                 v20.u0,
                                 v20.v0,
                                 v20.u1,
                                 v20.v1,
                                 v24 << 24 | v12,
                                 0.0F,
                                 0.0F,
                                 0.0F,
                                 v14
                              );
                              if (this.f1645 >= 256) {
                                 this.flush();
                                 this.f1646 = atlas;
                              }
                           }

                           v15 += v21;
                           v16 += v19;
                        }
                     }

                     if (!wr.isEnabled()) {
                        this.flush();
                     }
                  }
               }
            }
         }
      }
   }

   public void close() {
      this.f1645 = 0;
      this.f1646 = null;
      this.discardBatch();
      Collection result = this.f1644.values();

      for (GpuTextureView v2 : result) {
         if (!v2.isClosed()) {
            v2.close();
         }
      }

      this.f1644.clear();
      if (this.f1639 != null) {
         for (GpuBuffer v4 : this.f1639) {
            if (v4 != null) {
               v4.close();
            }
         }

         this.f1639 = null;
      }

      this.f1640 = 0;
      if (this.f1641 != null) {
         this.f1641.close();

         this.f1641 = null;
      }

      if (this.f1642 != null) {
         MemoryUtil.memFree(this.f1642);
         this.f1642 = null;
      }

      this.f1643 = false;
   }

   @Override
   public void uploadBatch(CommandEncoder encoder) {

      this.m1966();
   }

   public void drawTextFading(zt atlas, String text, float x, float y, float size, float maxWidth, int color) {
      MinecraftClient v8 = MinecraftClient.method_1551();
      if (v8.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            if (this.getTextWidth(atlas, text, size) <= maxWidth) {
               this.drawText(atlas, text, x, y, size, color, 0.0F, 0, 0.0F);
            } else {
               int v9 = color >> 24 & 255;
               if (v9 != 0) {
                  int v10 = color & 16777215;
                  float v11 = maxWidth - 40.0F;
                  text = this.m1969(text);
                  text = sp.get(text);
                  atlas.ensureLoaded();
                  if (atlas.getGlyphCount() != 0) {
                     this.m1964();
                     if (wr.isEnabled()) {
                        wr.register(this);
                     }

                     label82:
                     if (this.f1646 != null) {
                        if (this.f1646 == atlas && this.f1647 == 0.0F) {

                           if (this.f1648 == 0) {
                              break label82;
                           }
                        }

                        this.flush();
                     }

                     this.f1646 = atlas;
                     this.f1647 = 0.0F;
                     this.f1648 = 0;
                     float v12 = size / atlas.getFontSize();
                     float v13 = 0.0F;
                     int v14 = 0;

                     while (v14 < text.length()) {
                        cu.eu v15 = this.m1971(text, v14);
                        if (v15.matched()) {
                           v14 += v15.skip();
                        } else {
                           int v16 = text.codePointAt(v14);
                           int v17 = Character.charCount(v16);
                           iu v18 = atlas.getGlyph(v16);
                           float v19;
                           if (v18 != null) {
                              v19 = v18.xAdvance * v12;
                           } else {
                              iu v20 = atlas.getGlyph(63);
                              v19 = v20 != null ? v20.xAdvance * v12 : size * 0.5F;
                           }

                           if (v13 + v19 > maxWidth) {
                              break;
                           }

                           int v25 = v9;
                           if (v13 > v11) {
                              float v21 = (v13 - v11) / (maxWidth - v11);
                              v25 = (int)((float)v9 * (1.0F - v21));
                           }

                           if (v25 > 0 && v18 != null && v18.width > 0.0F && v18.height > 0.0F) {
                              this.m1961(
                                 x + v13 + v18.xOffset * v12,
                                 y + v18.yOffset * v12,
                                 v18.width * v12,
                                 v18.height * v12,
                                 v18.u0,
                                 v18.v0,
                                 v18.u1,
                                 v18.v1,
                                 v25 << 24 | v10,
                                 0.0F,
                                 0.0F,
                                 0.0F,
                                 v12
                              );
                              if (this.f1645 >= 256) {
                                 this.flush();
                                 this.f1646 = atlas;
                              }
                           }

                           v13 += v19;
                           v14 += v17;
                        }
                     }

                     if (!wr.isEnabled()) {
                        this.flush();
                     }
                  }
               }
            }
         }
      }
   }

   public void drawTextRotatedAroundPoint(
      zt atlas, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor, float rotation, float pivotX, float pivotY
   ) {
      MinecraftClient v12 = MinecraftClient.method_1551();
      if (v12.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            text = this.m1969(text);
            text = sp.get(text);
            atlas.ensureLoaded();

            if (atlas.getGlyphCount() != 0) {
               this.m1964();
               if (wr.isEnabled()) {
                  wr.register(this);
               }

               label72:
               if (this.f1646 != null) {
                  if (this.f1646 == atlas) {
                     float v35;
                     int v33 = (v35 = this.f1647 - outlineWidth) == 0.0F ? 0 : (v35 < 0.0F ? -1 : 1);

                     if (!v33 && this.f1648 == outlineColor) {
                        break label72;
                     }
                  }

                  this.flush();
               }

               this.f1646 = atlas;
               this.f1647 = outlineWidth;
               this.f1648 = outlineColor;
               float v13 = size / atlas.getFontSize();
               float v14 = x;
               float v15 = y;

               float v16 = (float)Math.toRadians((double)rotation);
               int v17 = color;
               int v18 = 0;

               while (v18 < text.length()) {
                  cu.eu v19 = this.m1970(text, v18, color, v17);
                  if (v19.matched()) {
                     v17 = v19.color();
                     v18 += v19.skip();
                  } else {
                     int v20 = text.codePointAt(v18);
                     int v21 = Character.charCount(v20);
                     if (v20 == 10) {
                        v14 = x;
                        v15 += atlas.getLineHeight() * v13;
                        v18 += v21;
                     } else {
                        iu v22 = atlas.getGlyph(v20);
                        if (v22 == null) {
                           iu v23 = atlas.getGlyph(63);
                           if (v23 != null) {
                              v14 += v23.xAdvance * v13;
                           } else {
                              v14 += size * 0.5F;
                           }

                           v18 += v21;
                        } else {
                           float v32 = v14 + v22.xOffset * v13;
                           float v24 = v15 + v22.yOffset * v13;
                           float v25 = v22.width * v13;
                           float v26 = v22.height * v13;
                           if (v22.width > 0.0F && v22.height > 0.0F) {
                              this.m1961(v32, v24, v25, v26, v22.u0, v22.v0, v22.u1, v22.v1, v17, v16, pivotX, pivotY, v13);
                           }

                           v14 += v22.xAdvance * v13;
                           if (this.f1645 >= 256) {
                              this.flush();
                              this.f1646 = atlas;
                           }

                           v18 += v21;
                        }
                     }
                  }
               }

               if (!wr.isEnabled()) {
                  this.flush();
               }
            }
         }
      }
   }

   public Cu() {
      this.f1644 = new HashMap<>();
      this.f1645 = 0;
      this.f1646 = null;
      this.f1647 = 0.0F;
      this.f1648 = 0;
      this.f1649 = new ArrayList<>();
   }

   private GpuTextureView m1967(GpuTexture gpuTexture) {
      GpuTextureView v2 = this.f1644.get(gpuTexture);
      if (v2 == null || v2.isClosed()) {
         this.f1644.entrySet().removeIf(e -> {
            if (!e.getKey().isClosed()) {

               if (!e.getValue().isClosed()) {
                  return false;
               }
            }

            if (!e.getValue().isClosed()) {
               e.getValue().close();
            }

            return true;
         });
         v2 = RenderSystem.getDevice().createTextureView(gpuTexture);
         this.f1644.put(gpuTexture, v2);
         
      }

      return v2;
   }

   private void m1961(
      float x, float y, float w, float h, float u0, float v0, float u1, float v1, int color, float rotation, float pivotX, float pivotY, float glyphScale
   ) {
      if (this.f1645 == 0) {
         this.f1642.clear();
         this.f1642.position(64);
      }

      this.f1642.putFloat(x);
      this.f1642.putFloat(y);
      this.f1642.putFloat(w);
      this.f1642.putFloat(h);
      this.f1642.putFloat(u0);
      this.f1642.putFloat(v0);
      this.f1642.putFloat(u1);
      this.f1642.putFloat(v1);

      float v14 = (float)(color >> 24 & 255) / 255.0F;
      float v15 = (float)(color >> 16 & 255) / 255.0F;
      int v20 = color >> 8;
      int temp1 = 255;

      float v16 = (float)(v20 & temp1) / 255.0F;
      float v17 = (float)(color & 255) / 255.0F;
      this.f1642.putFloat(v15);
      this.f1642.putFloat(v16);
      this.f1642.putFloat(v17);
      this.f1642.putFloat(v14);
      this.f1642.putFloat(rotation);
      this.f1642.putFloat(pivotX);
      this.f1642.putFloat(pivotY);
      this.f1642.putFloat(glyphScale);
      this.f1645++;
   }

   @Override
   public 

      return 3;
   }

   private int m1962() {
      MinecraftClient v1 = MinecraftClient.method_1551();
      if (v1 != null && v1.method_22683() != null) {
         Window result = v1.method_22683();

         return (int)Math.ceil((double)result.method_4489() / 2.0);
      } else {
         return 960;
      }
   }

   @Override
   public void discardBatch() {
      this.f1649.clear();

      
   }

   public void drawTextFadingReverse(zt atlas, String text, float x, float y, float size, float maxWidth, int color) {
      MinecraftClient v8 = MinecraftClient.method_1551();
      if (v8.method_1522() != null) {
         if (text != null && !text.isEmpty()) {
            if (this.getTextWidth(atlas, text, size) <= maxWidth) {
               this.drawText(atlas, text, x, y, size, color, 0.0F, 0, 0.0F);
            } else {

               int v9 = color >> 24 & 255;
               if (v9 != 0) {
                  int v10 = color & 16777215;
                  float v11 = 40.0F;
                  text = this.m1969(text);
                  text = sp.get(text);
                  atlas.ensureLoaded();
                  if (atlas.getGlyphCount() != 0) {
                     this.m1964();
                     if (wr.isEnabled()) {
                        wr.register(this);
                     }

                     if (this.f1646 != null && (this.f1646 != atlas || this.f1647 != 0.0F || this.f1648 != 0)) {
                        this.flush();
                     }

                     this.f1646 = atlas;
                     this.f1647 = 0.0F;
                     this.f1648 = 0;
                     float v12 = size / atlas.getFontSize();
                     float v13 = this.getTextWidth(atlas, text, size);
                     float v14 = v13 - maxWidth;
                     float v15 = 0.0F;
                     float v16 = 0.0F;
                     int v17 = 0;

                     while (v17 < text.length()) {
                        cu.eu v18 = this.m1971(text, v17);
                        if (v18.matched()) {
                           v17 += v18.skip();
                        } else {
                           int v19 = text.codePointAt(v17);
                           int v20 = Character.charCount(v19);
                           iu v21 = atlas.getGlyph(v19);
                           float v22;
                           if (v21 != null) {
                              v22 = v21.xAdvance * v12;
                           } else {
                              iu v23 = atlas.getGlyph(63);
                              v22 = v23 != null ? v23.xAdvance * v12 : size * 0.5F;
                           }

                           if (v15 < v14) {
                              v15 += v22;
                              v17 += v20;
                           } else {
                              if (v16 + v22 > maxWidth) {
                                 break;
                              }

                              int v27 = v9;
                              if (v16 < v11) {
                                 v27 = (int)((float)v9 * (v16 / v11));
                              }

                              if (v27 > 0 && v21 != null && v21.width > 0.0F && v21.height > 0.0F) {
                                 this.m1961(
                                    x + v16 + v21.xOffset * v12,
                                    y + v21.yOffset * v12,
                                    v21.width * v12,
                                    v21.height * v12,
                                    v21.u0,
                                    v21.v0,
                                    v21.u1,
                                    v21.v1,
                                    v27 << 24 | v10,
                                    0.0F,
                                    0.0F,
                                    0.0F,
                                    v12
                                 );
                                 if (this.f1645 >= 256) {
                                    this.flush();
                                    this.f1646 = atlas;
                                 }
                              }

                              v16 += v22;
                              v17 += v20;
                           }
                        }
                     }

                     if (!wr.isEnabled()) {
                        this.flush();
                     }
                  }
               }
            }
         }
      }
   }

   private cu.eu m1970(String text, int index, int defaultColor, int currentColor) {
      sp.tp v5 = sp.parseTag(text, index, defaultColor);
      if (v5 != null) {
         return new cu.eu(v5.length(), v5.color(), true);
      } else {
         int v6 = text.codePointAt(index);
         int v7 = Character.charCount(v6);
         if ((v6 == 167 || v6 == 38) && index + v7 < text.length()) {
            int v8 = text.codePointAt(index + v7);
            if (v8 == 35 && index + v7 + 6 < text.length()) {
               try {
                  String v14 = text.substring(index + v7 + 1, index + v7 + 7);
                  int v15 = -16777216 | Integer.parseInt(v14, 16);
                  return new cu.eu(v7 + 7, v15, true);
               } catch (Exception v13) {
               }
            }

            int v9 = LEGACY_CODE_CHARS.indexOf(Character.toLowerCase((char)v8));
            if (v9 >= 0) {
               int result;
               if (v9 < 16) {
                  result = LEGACY_COLORS[v9];
                  
               } else {
                  result = v9 == 21 ? defaultColor : currentColor;
               }

               int v10 = result;
               return new cu.eu(v7 + Character.charCount(v8), v10, true);
            }
         }

         return new cu.eu(0, currentColor, false);
      }
   }

   public float getTextHeight(zt atlas, String text, float size) {
      atlas.ensureLoaded();

      float v10 = atlas.getFontSize();

      float v4 = size / v10;
      int v5 = 1;

      for (int v6 = 0; v6 < text.length(); v6++) {
         if (text.charAt(v6) == 10) {
            v5++;
         }
      }

      float result = (float)v5;

      return result * atlas.getLineHeight() * v4;
   }

   private void m1964() {
      if (!this.f1643) {
         this.f1642 = MemoryUtil.memAlloc(16448);
         ByteBuffer v1 = MemoryUtil.memAlloc(4);
         v1.putInt(0);
         v1.flip();

         this.f1641 = RenderSystem.getDevice().createBuffer(() -> {
            String result = "client:font_dummy_vertex";

            return result;
         }, 32, v1);
         MemoryUtil.memFree(v1);

         this.f1639 = new GpuBuffer[32];
         this.f1643 = true;
      }
   }

   private String m1969(String text) {
      String v2 = preprocessCache.get(text);
      if (v2 != null) {
         return v2;
      } else {
         StringBuilder v3 = null;
         int v4 = 0;

         while (v4 < text.length()) {
            int v5 = text.codePointAt(v4);
            int v6 = Character.charCount(v5);
            if (v5 == LIGHTNING_CP) {
               if (v3 == null) {
                  v3 = new StringBuilder(text.substring(0, v4));
               }
            } else {
               Integer v7 = SMALL_CAPS_MAP.get(v5);
               if (v7 != null) {
                  if (v3 == null) {
                     v3 = new StringBuilder(text.substring(0, v4));
                  }

                  v3.appendCodePoint(v7);
               } else if (v3 != null) {
                  v3.appendCodePoint(v5);
               }
            }

            v4 += v6;
         }

         String result;
         if (v3 != null) {

            result = v3.toString();
         } else {
            result = text;
         }

         String v9 = result;
         preprocessCache.put(text, v9);
         return v9;
      }
   }

   private GpuBuffer m1965() {
      GpuBuffer v1 = this.f1639[this.f1640];
      if (v1 == null) {
         int v2 = this.f1640;
         v1 = RenderSystem.getDevice().createBuffer(() -> {
            StringBuilder result = new StringBuilder;

            result./* $VF: Unable to resugar constructor */<init>();
            return result.append("client:font_uniform_").append(v2).toString();
         }, 136, 3237198912024260013L + -3237198912024243565L);

         this.f1639[this.f1640] = v1;
      }

      this.f1640 = (this.f1640 + 1) % (32);
      return v1;
   }

   private int m1963() {
      MinecraftClient v1 = MinecraftClient.method_1551();
      if (v1 != null && v1.method_22683() != null) {
         double result = (double)v1.method_22683().method_4506();

         return (int)Math.ceil(result / 2.0);
      } else {
         return 540;
      }
   }

   @Environment(EnvType.CLIENT)
   private static record eu(int skip, int color, boolean matched) {
      private final int f1635;
      private final boolean f1636;

      @Override
      public final boolean equals(Object o) {
         boolean result = ObjectMethods.bootstrap<"equals",cu.eu,"skip;color;matched",cu.eu::f1635,cu.eu::color,cu.eu::f1636>(this, o);

         return result;
      }

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",cu.eu,"skip;color;matched",cu.eu::f1635,cu.eu::color,cu.eu::f1636>(this);

         return result;
      }

      public int skip() {
         int v2 = 695706683;
         int temp2 = v2 * (v2 + 1);

         return this.f1635;
      }

      private eu(int skip, int color, boolean matched) {
         this.f1635 = skip;
         this.color = color;
         this.f1636 = matched;
      }

      public boolean matched() {
         return this.f1636;
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static record fu(GpuBuffer buffer, GpuTextureView view, int count) {
      private final GpuTextureView f1638;
      private final GpuBuffer f1637;

      public GpuBuffer buffer() {

         return this.f1637;
      }

      private fu(GpuBuffer buffer, GpuTextureView view, int count) {
         this.f1637 = buffer;
         this.f1638 = view;
         this.count = count;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",cu.fu,"buffer;view;count",cu.fu::f1637,cu.fu::f1638,cu.fu::count>(this, o);
      }

      @Override
      public final 

         return ObjectMethods.bootstrap<"hashCode",cu.fu,"buffer;view;count",cu.fu::f1637,cu.fu::f1638,cu.fu::count>(this);
      }

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",cu.fu,"buffer;view;count",cu.fu::f1637,cu.fu::f1638,cu.fu::count>(this);

         return result;
      }

      public int count() {
         int v1 = -1538762198;
         int temp2 = v1 + 1;

         return this.count;
      }

      public GpuTextureView view() {
         return this.f1638;
      }

      
   }
}
