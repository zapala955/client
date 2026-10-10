package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.setting.MultiSetting;
import ru.white.utils.misc.Zq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.js;
import ru.white.utils.render.vs;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Trajectories",
   desc = "Показывает траекторию, иконку и точку падения жемчуга, стрелы и трезубца",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Trajectories extends Module {
   private final Map<Integer, hl.kl> f826;
   private static final RenderLayer RIBBON_LAYER = RenderLayer.method_75940(
      "trajectories_ribbon",
      RenderSetup.method_75927(hl.RIBBON_PIPELINE).method_75929(32768).method_75938()
   );
   private static final RenderLayer LINE_LAYER = RenderLayer.method_75940(
      "trajectories_line",
      RenderSetup.method_75927(hl.LINE_PIPELINE).method_75929(16384).method_75938()
   );
   private static final RenderPipeline LINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "trajectories_line"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final int MAX_STEPS = 200;
   public final BooleanSetting showLine;
   public final MultiSetting items = new MultiSetting(
      this, "Предметы", qf.of("Жемчуг эндера", true), qf.of("Стрелы", true), qf.of("Трезубец", true)
   );
   public final SliderSetting indicatorSize;
   private static final RenderLayer GLOW_RIBBON_LAYER = RenderLayer.method_75940(
      "trajectories_glow_ribbon",
      RenderSetup.method_75927(hl.GLOW_RIBBON_PIPELINE).method_75929(32768).method_75938()
   );
   private final List<hl.kl> f827;
   public final BooleanSetting glow;
   private static final RenderPipeline FILL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "trajectories_fill"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public final SliderSetting lineWidth;
   private long f828;
   public final BooleanSetting showTime;
   public final BooleanSetting flying;
   private static final int HELD_KEY = 0;
   private final BufferAllocator f825;
   public final BooleanSetting showIcon;
   private static final RenderPipeline GLOW_RIBBON_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "trajectories_glow_ribbon"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   private static final RenderLayer FILL_LAYER = RenderLayer.method_75940(
      "trajectories_fill",
      RenderSetup.method_75927(FILL_PIPELINE).method_75929(4096).method_75938()
   );
   public final BooleanSetting fromHand = new BooleanSetting(this, "Из рук", true);
   private static final RenderPipeline RIBBON_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "trajectories_ribbon"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );

   private static void m975(VertexConsumer buffer, Matrix4f matrix, float x0, float y0, float z0, float x1, float y1, float z1, int r, int g, int b, int a) {
      VertexConsumer result = buffer.method_22918(matrix, x0, y0, z0);

      result.method_1336(r, g, b, a);

      buffer.method_22918(matrix, x1, y1, z1).method_1336(r, g, b, a);
   }

   private ItemStack m963(ItemStack crossbow) {
      ChargedProjectilesComponent v2 = (ChargedProjectilesComponent)crossbow.method_58694(DataComponentTypes.field_49649);
      return v2 != null && !v2.method_57437().isEmpty() ? (ItemStack)v2.method_57437().getFirst() : new ItemStack(Items.field_8107);
   }

   private void m960(float partial, float dt) {
      for (hl.kl v4 : this.f826.values()) {
         v4.active = false;
      }

      if (this.fromHand.getValue()) {
         this.m962(partial, dt);
      }

      if (this.flying.getValue()) {
         this.m964(partial, dt);
      }

      this.f826.entrySet().removeIf(entry -> {

         return !entry.getValue().active;
      });

      this.f827.clear();
      this.f827.addAll(this.f826.values());
   }

   private static int m976(int color, int shift) {
      return color >> shift & 255;
   }

   private void m974(VertexConsumer lines, Matrix4f matrix, Vec3d cam, hl.kl state) {
      float v5 = this.indicatorSize.getValue();
      if (!(v5 <= 0.01F)) {
         Vec3d v6 = state.impact;
         boolean v7 = state.hitEntity instanceof LivingEntity;
         int v8 = v7
            ? up.getColor(64, 255, 64, 160)
            : up.multAlpha(up.fade(1), 0.6F);
         int v21;
         if (v7) {
            v21 = 64;
            int temp1 = 255;
            int temp2 = 64;

            v21 = up.getColor(v21, temp1, temp2, 200);
         } else {
            v21 = up.multAlpha(v8, 1.0F);
         }

         int v9 = v21;
         float v10 = (float)(v6.field_1352 - cam.field_1352);
         float v11 = (float)(v6.field_1351 - cam.field_1351) + 0.02F;
         float v12 = (float)(v6.field_1350 - cam.field_1350);
         int v13 = m976(v9, 16);
         int v14 = m976(v9, 8);
         int v15 = m976(v9, 0);
         int v16 = m977(v9);
         float v22 = v5 * 0.35F;

         float v17 = Math.max(0.15F, v22);
         float v24 = v10 - v17;
         float v25 = v10 + v17;

         m975(lines, matrix, v24, v11, v12, v25, v11, v12, v13, v14, v15, v16);
         m975(lines, matrix, v10, v11, v12 - v17, v10, v11, v12 + v17, v13, v14, v15, v16);
      }
   }

   private hl.jl m968(hl.kl state, Vec3d cam) {
      List v3 = state.points;

      while (v3.size() - v4 > 2 && ((Vec3d)v3.get(v4)).method_1025(cam) < 0.16) {
         v4++;
      }

      int v5 = v3.size() - v4;
      if (v5 < 2) {
         return null;
      } else {
         boolean v6 = state.hitEntity instanceof LivingEntity;
         int v7 = v6 ? up.getColor(64, 255, 64, 255) : up.fade(1);
         hl.jl v8 = new hl.jl();
         v8.cr = m976(v7, 16);
         v8.cg = m976(v7, 8);
         v8.cb = m976(v7, 0);
         v8.rel = new Vec3d[v5];
         v8.side = new Vec3d[v5];
         v8.alpha = new float[v5];

         v8.halfW = new float[v5];
         double v9 = Math.tan(Math.toRadians((double)((Integer)mc.field_1690.method_41808().method_41753()).floatValue()) / 2.0);
         double v11 = 2.0 * v9 / (double)Math.max(1, mc.method_22683().method_4506());
         float v13 = this.lineWidth.getValue();

         for (int v14 = 0; v14 < v5; v14++) {
            v8.rel[v14] = ((Vec3d)v3.get(v4 + v14)).method_1020(cam);
         }

         double v23 = 0.0;
         Vec3d v16 = new Vec3d(0.0, 1.0, 0.0);

         for (int v17 = 0; v17 < v5; v17++) {

            Vec3d v18 = v8.rel[Math.min(v17 + 1, v5 - 1)].method_1020(v8.rel[Math.max(v17 - 1, 0)]);
            Vec3d v19 = v18.method_1036(v8.rel[v17]);
            if (v19.method_1027() < 1.0E-8) {
               v19 = v16;
            } else {
               v19 = v19.method_1029();
            }

            v16 = v19;
            v8.side[v17] = v19;
            v8.halfW[v17] = (float)(0.5 * (double)v13 * v8.rel[v17].method_1033() * v11);
            if (v17 > 0) {

               v23 += ((Vec3d)v3.get(v4 + v17)).method_1022((Vec3d)v3.get(v4 + v17 - 1));
            }

            v8.alpha[v17] = (float)MathHelper.method_15350((v23 - 0.1) / 0.55, 0.0, 1.0);
         }

         return v8;
      }
   }

   private static void m969(VertexConsumer buf, Matrix4f m, hl.jl d) {
      int v3 = d.rel.length;

      for (int v4 = 0; v4 < v3 - 1; v4++) {
         int v5 = (int)(d.alpha[v4] * 255.0F);
         int v6 = (int)(d.alpha[v4 + 1] * 255.0F);
         if (v5 > 0 || v6 > 0) {
            float v7 = (float)v4 / (float)(v3 - 1);
            float v8 = (float)(v4 + 1) / (float)(v3 - 1);
            int v9 = m971(d.cr, v7);
            int v10 = m971(d.cg, v7);
            int v11 = m971(d.cb, v7);
            int v12 = m971(d.cr, v8);
            int v13 = m971(d.cg, v8);
            int v14 = m971(d.cb, v8);
            Vec3d v15 = d.rel[v4];
            Vec3d v16 = d.rel[v4 + 1];
            Vec3d v17 = d.side[v4].method_1021((double)d.halfW[v4]);
            Vec3d v18 = d.side[v4 + 1].method_1021((double)d.halfW[v4 + 1]);
            Vec3d v19 = v17.method_1021(0.7);
            Vec3d v20 = v18.method_1021(0.7);
            m972(buf, m, v15.method_1020(v19), v9, v10, v11, v5);
            m972(buf, m, v16.method_1020(v20), v12, v13, v14, v6);
            m972(buf, m, v16.method_1019(v20), v12, v13, v14, v6);
            m972(buf, m, v15.method_1019(v19), v9, v10, v11, v5);
            m972(buf, m, v15.method_1019(v19), v9, v10, v11, v5);
            m972(buf, m, v16.method_1019(v20), v12, v13, v14, v6);
            m972(buf, m, v16.method_1019(v18), v12, v13, v14, 0);
            m972(buf, m, v15.method_1019(v17), v9, v10, v11, 0);
            m972(buf, m, v15.method_1020(v19), v9, v10, v11, v5);
            m972(buf, m, v16.method_1020(v20), v12, v13, v14, v6);
            m972(buf, m, v16.method_1020(v18), v12, v13, v14, 0);
            m972(buf, m, v15.method_1020(v17), v9, v10, v11, 0);
         }
      }
   }

   private hl.il m965(ItemStack icon, float partial, double speed, double gravity, boolean offHand) {
      float v8 = mc.field_1724.method_5705(partial);
      float v9 = mc.field_1724.method_5695(partial);
      double v10 = (double)(
         -MathHelper.method_15374((double)(v8 * (float) (Math.PI / 180.0))) * MathHelper.method_15362((double)(v9 * (float) (Math.PI / 180.0)))
      );
      double v12 = (double)(-MathHelper.method_15374((double)(v9 * (float) (Math.PI / 180.0))));
      float result = v8 * (float) (Math.PI / 180.0);

      double v14 = (double)(MathHelper.method_15362((double)result) * MathHelper.method_15362((double)(v9 * (float) (Math.PI / 180.0))));
      Vec3d v16 = new Vec3d(v10, v12, v14).method_1029().method_1021(speed);
      Vec3d v17 = mc.field_1724.method_18798();
      v16 = v16.method_1031(v17.field_1352, mc.field_1724.method_24828() ? 0.0 : v17.field_1351, v17.field_1350);
      Vec3d v18 = mc.field_1724.method_30950(partial);
      double v19 = v18.field_1351 + (double)mc.field_1724.method_5751();
      Vec3d v21 = new Vec3d(v18.field_1352, v19, v18.field_1350).method_1019(new Vec3d(v10, v12, v14).method_1029().method_1021(0.2));
      hl.il v22 = this.m966(v21, v16, gravity, mc.field_1724, mc.field_1724, icon);
      if (v22 == null) {
         return null;
      } else {
         Vec3d v23 = new Vec3d(v10, v12, v14).method_1029();
         Vec3d v24 = Vec3d.method_1030(0.0F, v8 + 90.0F);
         if (offHand) {

            v24 = v24.method_1021(-1.0);
         }

         Vec3d v25 = v24.method_1021(0.32).method_1031(0.0, -0.18, 0.0).method_1019(v23.method_1021(0.25));
         List v26 = v22.f820;
         int v27 = Math.min(10, v26.size());

         for (int v28 = 0; v28 < v27; v28++) {
            float v29 = 1.0F - (float)v28 / (float)v27;
            v29 *= v29;
            Vec3d v35 = (Vec3d)v26.get(v28);

            v26.set(v28, v35.method_1019(v25.method_1021((double)v29)));
         }

         return v22;
      }
   }

   private static boolean m978(ItemStack stack) {

      ChargedProjectilesComponent v1 = (ChargedProjectilesComponent)stack.method_58694(DataComponentTypes.field_49649);
      return v1 != null && !v1.method_57442();
   }

   private static 

      float v4 = (float)(255 - c) * 0.45F;

      return c + (int)(v4 * t);
   }

   @EventTarget
   public void onDisplay(EventReceivePacket e) {
      if (mc.field_1724 != null && !this.f827.isEmpty()) {
         if (this.showIcon.getValue() || this.showTime.getValue()) {
            for (hl.kl v3 : this.f827) {
               Vec3d v4 = zq.worldSpaceToScreenSpace(v3.impact.method_1031(0.0, 0.25, 0.0));
               if (!(v4.field_1350 <= 0.0) && !(v4.field_1350 >= 1.0)) {
                  float v5 = (float)v4.field_1352;

                  float v6 = (float)v4.field_1351;
                  float v7 = 10.0F;
                  float v8 = v7 + 4.0F;
                  if (this.showIcon.getValue() && !v3.icon.method_7960()) {
                     vs.ys.rect(
                        v5 - v8 / 2.0F,
                        v6 - v8 / 2.0F,
                        v8,
                        v8,
                        up.getColor(11, 11, 11, 140),
                        4.0F
                     );
                     js.drawItemWithContext(e.getDrawContext(), v3.icon, v5 - v7 / 2.0F, v6 - v7 / 2.0F, v7 / 16.0F, 1.0F);
                  }

                  boolean result = this.showTime.getValue();
                  int v17 = v14 * (v14 + 1) % 2;

                  if (v17 != 0) {
                     }

                  if (result) {
                     String v9 = String.format(Locale.ROOT, "%.1fs", (float)v3.ticks / 20.0F);
                     float v10 = 6.0F;
                     float v11 = hu.sf_regular.getWidth(v9, v10) + 6.0F;
                     float v12 = v5 - v11 / 2.0F;
                     float v13 = v6 + v8 / 2.0F + 2.0F;
                     vs.ys.rect(v12, v13, v11, v10 + 3.0F, up.getColor(0, 0, 0, 128), 3.0F);
                     hu.sf_regular.draw(v9, v12 + 3.0F, v13 + 1.5F, v10, up.getColor(255));
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         float v2 = e.getTickDelta();
         long v3 = System.nanoTime();
         float v5 = this.f828 == 0L ? 0.05F : Math.min(0.1F, (float)(v3 - this.f828) / 1.0E9F);
         this.f828 = v3;
         this.m960(v2, v5);

         if (!this.f827.isEmpty()) {
            MatrixStack v6 = e.getMatrixStack();
            Vec3d v7 = mc.field_1773.method_19418().method_71156();
            Matrix4f v8 = v6.method_23760().method_23761();
            Immediate v9 = VertexConsumerProvider.method_22991(this.f825);
            if (this.showLine.getValue()) {
               ArrayList v10 = new ArrayList(this.f827.size());

               for (hl.kl v12 : this.f827) {
                  hl.jl v13 = this.m968(v12, v7);
                  if (v13 != null) {
                     v10.add(v13);
                  }
               }

               if (this.glow.getValue()) {
                  VertexConsumer v17 = v9.method_73477(GLOW_RIBBON_LAYER);

                  for (hl.jl v25 : v10) {
                     m970(v17, v8, v25);
                  }
               }

               VertexConsumer v18 = v9.method_73477(RIBBON_LAYER);

               for (hl.jl v26 : v10) {
                  m969(v18, v8, v26);
                  
               }
            }

            VertexConsumer v16 = v9.method_73477(FILL_LAYER);

            for (hl.kl v23 : this.f827) {
               this.m973(v16, v8, v7, v23);
            }

            VertexConsumer v20 = v9.method_73477(LINE_LAYER);

            for (hl.kl v27 : this.f827) {
               this.m974(v20, v8, v7, v27);
            }

            v9.method_22993();
         }
      }
   }

   public Trajectories() {
      this.flying = new BooleanSetting(this, "Летящие снаряды", true);
      this.showLine = new BooleanSetting(this, "Линия траектории", true);
      this.glow = new BooleanSetting(this, "Свечение линии", false).setVisible(() -> {

         return this.showLine.getValue();
      });
      this.showIcon = new BooleanSetting(this, "Иконка предмета", true);
      this.showTime = new BooleanSetting(this, "Время до падения", true);
      this.lineWidth = new SliderSetting(this, "Толщина линии", 2.0F, 1.0F, 5.0F, 0.5F).setVisible(() -> {

         return this.showLine.getValue();
      });
      this.indicatorSize = new SliderSetting(this, "Размер метки", 0.5F, 0.1F, 2.0F, 0.1F);
      this.f825 = new BufferAllocator(131072);
      this.f826 = new LinkedHashMap<>();
      this.f827 = new ArrayList<>();
      this.f828 = 0L;
   }

   private void m962(float partial, float dt) {
      ItemStack v3 = mc.field_1724.method_6047();
      ItemStack v4 = mc.field_1724.method_6079();

      if (this.items.getValue("Жемчуг эндера")) {
         if (v3.method_31574(Items.field_8634)) {
            this.m961(0, this.m965(v3, partial, 1.5, 0.03, false), dt);
         } else if (v4.method_31574(Items.field_8634)) {
            this.m961(0, this.m965(v4, partial, 1.5, 0.03, true), dt);
         }
      }

      if (this.items.getValue("Стрелы")) {
         if (mc.field_1724.method_6115() && mc.field_1724.method_6030().method_7909() == Items.field_8102) {
            float v11 = BowItem.method_7722(mc.field_1724.method_6048());
            if (v11 >= 0.1F) {
               boolean v13 = mc.field_1724.method_6058() == Hand.field_5810;
               ItemStack temp3 = new ItemStack;

               temp3./* $VF: Unable to resugar constructor */<init>(Items.field_8107);
               this.m961(0, this.m965(temp3, partial, (double)v11 * 3.0, 0.05, v13), dt);
            }
         } else {
            ItemStack v5 = null;

            boolean v6 = false;
            if (v3.method_31574(Items.field_8399)) {
               v5 = v3;
            } else if (v4.method_31574(Items.field_8399)) {
               v5 = v4;
               v6 = true;
            }

            if (v5 != null && m978(v5)) {
               this.m961(0, this.m965(this.m963(v5), partial, 3.15, 0.05, v6), dt);
            }
         }
      }

      if (this.items.getValue("Трезубец") && mc.field_1724.method_6115() && mc.field_1724.method_6030().method_7909() == Items.field_8547) {
         int v12 = mc.field_1724.method_6048();
         if (v12 > 0) {
            float v14 = Math.min((float)v12 / 10.0F, 1.0F);
            if (v14 < 0.1F) {
               v14 = 0.1F;
            }

            boolean v7 = mc.field_1724.method_6058() == Hand.field_5810;
            this.m961(0, this.m965(new ItemStack(Items.field_8547), partial, 2.5 * (double)v14, 0.05, v7), dt);
         }
      }
   }

   private static void m972(VertexConsumer buf, Matrix4f m, Vec3d v, int r, int g, int b, int a) {
      VertexConsumer result = buf.method_22918(m, (float)v.field_1352, (float)v.field_1351, (float)v.field_1350);

      result.method_1336(r, g, b, a);
   }

   private hl.il m966(Vec3d pos, Vec3d vel, double gravity, Entity ignore, Entity owner, ItemStack icon) {
      ArrayList v8 = new ArrayList(128);
      v8.add(pos);
      double v9 = 0.25;
      double v11 = Math.pow(0.99, v9);
      double v13 = gravity * v9;

      for (int v15 = 0; v15 < 200; v15++) {
         for (int v16 = 0; v16 < 4; v16++) {
            Vec3d v17 = pos.method_1019(vel.method_1021(v9));
            BlockHitResult v18 = mc.field_1687.method_17742(new RaycastContext(pos, v17, ShapeType.field_17558, FluidHandling.field_1348, ignore));
            Vec3d v19 = v18.method_17783() == Type.field_1332 ? v18.method_17784() : v17;
            Entity v20 = null;
            Vec3d v21 = null;
            double v22 = Double.MAX_VALUE;
            Box v24 = new Box(pos, v19).method_1014(0.25);

            for (Object result : mc.field_1687.method_8333(ignore, v24, other -> {
               if (other instanceof LivingEntity && other.method_5805() && other != owner) {
                  boolean var10000x = other.method_7325();
                  int var10001x = v2 * (v2 + 1);

                  if (!var10000x) {
                     return true;
                  }
               }

               return false;
            })) {

               Entity v26 = (Entity)result;
               Vec3d v27 = (Vec3d)v26.method_5829().method_1014(0.25).method_992(pos, v19).orElse(null);
               if (v27 != null) {
                  double v28 = v27.method_1025(pos);
                  if (v28 < v22) {
                     v22 = v28;
                     v20 = v26;
                     v21 = v27;
                  }
               }
            }

            if (v20 != null) {
               v8.add(v21);
               return new hl.il(m967(v8), v21, v15 + 1, v20, icon);
            }

            if (v18.method_17783() == Type.field_1332) {
               v8.add(v19);
               return new hl.il(m967(v8), v19, v15 + 1, null, icon);
            }

            v8.add(v17);
            pos = v17;
            vel = vel.method_1021(v11).method_1023(0.0, v13, 0.0);
            if (v17.field_1351 < (double)(mc.field_1687.method_31607() - (16))) {
               return null;
            }
         }
      }

      return null;
   }

   private void m964(float partial, float dt) {
      for (Entity v4 : mc.field_1687.method_18112()) {
         if (!(v4.method_18798().method_1027() < 4.0E-4)) {
            double v5;
            ItemStack v7;
            if (v4 instanceof EnderPearlEntity) {
               if (!this.items.getValue("Жемчуг эндера")) {
                  continue;
               }

               v5 = 0.03;
               v7 = new ItemStack(Items.field_8634);
            } else if (v4 instanceof TridentEntity) {
               TridentEntity v8 = (TridentEntity)v4;
               if (!this.items.getValue("Трезубец") || v8.method_24828() || v8.method_7441()) {
                  continue;
               }

               v5 = 0.05;
               v7 = new ItemStack(Items.field_8547);
            } else {
               if (!(v4 instanceof ArrowEntity)) {

                  if (!(v4 instanceof PersistentProjectileEntity)) {
                     continue;
                  }
               }

               if (!this.items.getValue("Стрелы")) {
                  continue;
               }

               boolean v17 = v4 instanceof ArrowEntity;

               if (v17) {
                  ArrowEntity v9 = (ArrowEntity)v4;
                  if (v9.method_24828()) {
                     continue;
                  }
               }

               v5 = 0.05;
               v7 = new ItemStack(Items.field_8107);
            }

            Vec3d v15 = v4.method_30950(partial);
            Vec3d v16 = v4.method_18798();
            Entity v10 = v4 instanceof ProjectileEntity v11 ? v11.method_24921() : null;
            this.m961(v4.method_5628(), this.m966(v15, v16, v5, v4, v10, v7), dt);
         }
      }
   }

   private static int m977(int color) {
      int result = color >> 24;

      int v1 = result & 255;
      return v1 == 0 ? 255 : v1;
   }

   @Override
   protected void onDisable() {
      this.f826.clear();

      this.f827.clear();

      this.f828 = 0L;
   }

   private static void m970(VertexConsumer buf, Matrix4f m, hl.jl d) {
      int v3 = d.rel.length;

      for (int v4 = 0; v4 < v3 - 1; v4++) {
         int v5 = (int)(d.alpha[v4] * 0.3F * 255.0F);
         int v6 = (int)(d.alpha[v4 + 1] * 0.3F * 255.0F);
         if (v5 > 0 || v6 > 0) {
            Vec3d v7 = d.rel[v4];
            Vec3d v8 = d.rel[v4 + 1];
            Vec3d result = d.side[v4];
            double temp1 = (double)(d.halfW[v4] * 3.0F);

            Vec3d v9 = result.method_1021(temp1);
            Vec3d v10 = d.side[v4 + 1].method_1021((double)(d.halfW[v4 + 1] * 3.0F));
            m972(buf, m, v7, d.cr, d.cg, d.cb, v5);
            m972(buf, m, v8, d.cr, d.cg, d.cb, v6);
            m972(buf, m, v8.method_1019(v10), d.cr, d.cg, d.cb, 0);
            m972(buf, m, v7.method_1019(v9), d.cr, d.cg, d.cb, 0);
            m972(buf, m, v7, d.cr, d.cg, d.cb, v5);
            m972(buf, m, v8, d.cr, d.cg, d.cb, v6);
            m972(buf, m, v8.method_1020(v10), d.cr, d.cg, d.cb, 0);
            m972(buf, m, v7.method_1020(v9), d.cr, d.cg, d.cb, 0);
         }
      }
   }

   private void m961(int key, hl.il raw, float dt) {
      if (raw != null) {
         hl.kl v4 = this.f826.get(key);
         if (v4 == null) {
            v4 = hl.kl.from(raw);
            this.f826.put(key, v4);
         } else {
            v4.blendToward(raw, dt, 100.0F);
         }

         v4.active = true;
      }
   }

   private static List<Vec3d> m967(List<Vec3d> points) {
      if (points.size() < 2) {

         return points;
      } else {
         ArrayList v1 = new ArrayList(points.size() * 2);
         v1.add((Vec3d)points.getFirst());

         for (int v2 = 0; v2 + 1 < points.size(); v2++) {
            Vec3d v3 = (Vec3d)points.get(v2);
            Vec3d v4 = (Vec3d)points.get(v2 + 1);
            v1.add(v3.method_35590(v4, 0.33));
            v1.add(v3.method_35590(v4, 0.66));
            v1.add(v4);
         }

         return v1;
      }
   }

   private void m973(VertexConsumer fill, Matrix4f matrix, Vec3d cam, hl.kl state) {
      float v5 = this.indicatorSize.getValue();
      if (!(v5 <= 0.01F)) {
         Vec3d v6 = state.impact;
         boolean v7 = state.hitEntity instanceof LivingEntity;
         int v8 = v7
            ? up.getColor(64, 255, 64, 160)
            : up.multAlpha(up.fade(1), 0.6F);
         int v33;
         if (v7) {
            v33 = 64;
            int temp1 = 255;

            v33 = up.getColor(v33, temp1, 64, 40);
         } else {
            v33 = up.multAlpha(up.fade(1), 0.15F);
         }

         int v9 = v33;
         float v10 = (float)(v6.field_1352 - cam.field_1352);
         float v11 = (float)(v6.field_1351 - cam.field_1351) + 0.02F;
         float v12 = (float)(v6.field_1350 - cam.field_1350);
         int v13 = m976(v8, 16);
         int v14 = m976(v8, 8);
         int v15 = m976(v8, 0);
         int v16 = m977(v8);
         int v17 = m976(v9, 16);
         int v18 = m976(v9, 8);
         int v19 = m976(v9, 0);
         int v20 = m977(v9);

         for (int v21 = 0; v21 < 24; v21++) {
            double v22 = (Math.PI * 2) * (double)v21 / 24.0;
            int v35 = v21 + 1;

            double v34 = (Math.PI * 2) * (double)v35 / 24.0;

            double v24 = v34;
            float v26 = v10 + (float)(Math.cos(v22) * (double)v5);
            float v27 = v12 + (float)(Math.sin(v22) * (double)v5);
            float v28 = v10 + (float)(Math.cos(v24) * (double)v5);
            float v29 = v12 + (float)(Math.sin(v24) * (double)v5);
            fill.method_22918(matrix, v10, v11, v12).method_1336(v13, v14, v15, v16);
            fill.method_22918(matrix, v26, v11, v27).method_1336(v17, v18, v19, v20);
            fill.method_22918(matrix, v28, v11, v29).method_1336(v17, v18, v19, v20);
            fill.method_22918(matrix, v10, v11, v12).method_1336(v13, v14, v15, v16);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static record il(List<Vec3d> points, Vec3d impact, int ticks, Entity hitEntity, ItemStack icon) {
      private final ItemStack f824;
      private final int f822;
      private final List<Vec3d> f820;
      private final Vec3d f821;
      private final Entity f823;

      public Entity hitEntity() {

         return this.f823;
      }

      private il(List<Vec3d> points, Vec3d impact, int ticks, Entity hitEntity, ItemStack icon) {
         this.f820 = points;
         this.f821 = impact;
         this.f822 = ticks;
         this.f823 = hitEntity;
         this.f824 = icon;
      }

      public 

         return this.f822;
      }

      @Override
      public final boolean equals(Object o) {
         int temp2 = v2 + 1;

         return ObjectMethods.bootstrap<"equals",hl.il,"points;impact;ticks;hitEntity;icon",hl.il::f820,hl.il::f821,hl.il::f822,hl.il::f823,hl.il::f824>(
            this, o
         );
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",hl.il,"points;impact;ticks;hitEntity;icon",hl.il::f820,hl.il::f821,hl.il::f822,hl.il::f823,hl.il::f824>(
            this
         );

         return result;
      }

      public ItemStack icon() {

         return this.f824;
      }

      public Vec3d impact() {

         return this.f821;
      }

      public List<Vec3d> points() {

         return this.f820;
      }

      @Override
      public final String toString() {

         return ObjectMethods.bootstrap<"toString",hl.il,"points;impact;ticks;hitEntity;icon",hl.il::f820,hl.il::f821,hl.il::f822,hl.il::f823,hl.il::f824>(this);
      }

      
   }

   @Environment(EnvType.CLIENT)
   private static final class jl {
      Vec3d[] rel;
      int StringSetting;
      int cr;
      float[] halfW;
      float[] alpha;
      int cb;
      Vec3d[] side;

      
   }

   @Environment(EnvType.CLIENT)
   private static final class kl {
      boolean active;
      Entity hitEntity;
      ItemStack icon;
      List<Vec3d> points = new ArrayList<>();
      int ticks;
      Vec3d impact = Vec3d.field_1353;

      static hl.kl from(hl.il raw) {
         hl.kl v1 = new hl.kl();
         v1.points = new ArrayList<>(raw.points());

         v1.impact = raw.impact();

         v1.ticks = raw.ticks();
         v1.hitEntity = raw.hitEntity();
         v1.icon = raw.icon().method_7972();
         return v1;
      }

      private kl() {
         this.icon = ItemStack.field_8037;
      }

      void blendToward(hl.il raw, float dt, float speed) {
         float v4 = 1.0F - (float)Math.exp((double)(-dt * speed));

         if (this.impact.method_1025(raw.impact()) > 4096.0) {
            this.points = new ArrayList<>(raw.points());
            this.impact = raw.impact();
         } else {
            this.impact = this.impact.method_35590(raw.impact(), (double)v4);
            this.m957(raw.points(), v4);
         }

         this.ticks = raw.ticks();
         this.hitEntity = raw.hitEntity();
         this.icon = raw.icon().method_7972();
      }

      private void m957(List<Vec3d> target, float k) {
         if (target.isEmpty()) {
            this.points.clear();
         } else {
            int v3 = Math.max(this.points.size(), target.size());
            if (v3 <= 1) {
               this.points = new ArrayList<>(target);
            } else {
               List v4 = m958(this.points, v3);
               List v5 = m958(target, v3);
               this.points = new ArrayList<>(v3);

               while (true) {

                  if (v6 >= v3) {
                     return;
                  }

                  float v7 = k * (0.15F + 0.85F * (float)v6 / (float)(v3 - 1));
                  this.points.add(((Vec3d)v4.get(v6)).method_35590((Vec3d)v5.get(v6), (double)v7));
                  v6++;
               }
            }
         }
      }

      private static List<Vec3d> m958(List<Vec3d> src, int count) {
         if (src.isEmpty()) {
            return new ArrayList<>();
         } else if (src.size() != 1 && count > 1) {
            double v9 = 0.0;

            for (int v10 = 0; v10 + 1 < src.size(); v10++) {

               v9 += ((Vec3d)src.get(v10)).method_1022((Vec3d)src.get(v10 + 1));
            }

            ArrayList v11 = new ArrayList(count);
            v11.add((Vec3d)src.getFirst());

            for (int v5 = 1; v5 < count - 1; v5++) {
               v11.add(m959(src, v9 * (double)v5 / (double)(count - 1)));
            }

            v11.add((Vec3d)src.getLast());
            return v11;
         } else {
            ArrayList v2 = new ArrayList(count);
            Vec3d v3 = (Vec3d)src.getFirst();

            for (int v4 = 0; v4 < count; v4++) {

               v2.add(v3);
            }

            return v2;
         }
      }

      private static Vec3d m959(List<Vec3d> src, double dist) {
         double v3 = 0.0;

         for (int v5 = 0; v5 + 1 < src.size(); v5++) {
            Vec3d v6 = (Vec3d)src.get(v5);
            Vec3d v7 = (Vec3d)src.get(v5 + 1);
            double v8 = v6.method_1022(v7);
            if (v3 + v8 >= dist) {
               double v10 = v8 <= 1.0E-8 ? 0.0 : (dist - v3) / v8;
               return v6.method_35590(v7, v10);
            }

            v3 += v8;
         }

         return (Vec3d)src.getLast();
      }

      
   }
}
