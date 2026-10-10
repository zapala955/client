package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderSetup.Builder;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap.Type;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.white.utils.helper.Wu;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.gl.Jf;
import ru.white.utils.render.gl.Kf;
import ru.white.utils.render.gl.Lf;
import ru.white.utils.PacketUtil;
import ru.white.event.impl.EventMove;
import ru.white.event.Event;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Particles",
   desc = "Кастомные частицы вокруг игрока",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Particles extends Module implements jf {
   private final BufferAllocator boxAllocator;
   public sf previewButton = lf.button(this);
   private Entity totemTargetEntity;
   public static final Function<Identifier, RenderLayer> ROMB_ESP = Util.method_34866(texture -> {
      Builder result = RenderSetup.method_75927(Particles.TEXTURED_QUADS_PIPELINE);

      result = result.method_75934("Sampler0", texture).method_75937();

      RenderSetup v1 = result.method_75929(v6).method_75938();
      return RenderLayer.method_75940("wtex", v1);
   });
   private int totemParticlesLeftToSpawn;
   private static final Vector3f REUSABLE_NORMAL = new Vector3f(0.0F, 0.0F, 1.0F);
   private final SliderSetting size;
   public SliderSetting onworldsSizs;
   private long lastUpdateTime;
   public BooleanSetting rotations;
   private static final RenderPipeline TEXTURED_QUADS_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56864})
         .withLocation(Identifier.method_60655("client", "pipeline/world/textured_quads"))
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public BooleanSetting glows;
   public final List<Particles.Particle> totemParticles;
   public final List<Particles.Particle> targetParticles;
   private int totalTotemParticlesToSpawn;
   private final ModeSetting particleMode;
   public SliderSetting onTtSizs;
   private final lf previewSettings;
   public SliderSetting onPerkSizs;
   public BooleanSetting perka;
   public BooleanSetting move;
   private final SliderSetting glowsiz;
   private long totemSpawnStartTime;
   public BooleanSetting onAttack = new BooleanSetting(this, "При атаке", true);
   public BooleanSetting totem;
   public final List<Particles.Particle> worldParticles;
   public SliderSetting onAtSizs = new SliderSetting(this, "Кол в атаке", 20.0F, 5.0F, 50.0F, 1.0F).setVisible(() -> {

      return this.onAttack.getValue();
   });
   private static final Map<Particles.ParticleType, RenderLayer> RENDER_LAYER_CACHE = new ConcurrentHashMap<>();
   public final List<Particles.Particle> frameParticles;
   public BooleanSetting worlds;

   private void renderParticlesGlow(
      MatrixStack matrix, Immediate immediate, List<Particles.Particle> particles, long fadeInTime, long fadeOutTime, double deltaTime
   ) {
      if (!particles.isEmpty()) {
         matrix.method_22903();

         for (Particles.Particle v11 : particles) {
            float v12 = v11.animation.get();
            int v13 = (int)(v12 * 255.0F * this.glowsiz.getValue());
            if (v13 > 0) {
               int v14 = up.replAlpha(v11.color(), v13);
               Vec3d v15 = v11.position();
               this.renderParticle2(
                  matrix, immediate, v11, (float)v15.field_1352, (float)v15.field_1351, (float)v15.field_1350, v11.size * v12, v14, v13
               );
            }
         }

         matrix.method_22909();
         
      }
   }

   @EventTarget
   public void onEvent(EventSync event) {
      this.clear();
      int result = v2 * (v2 + 1);

      
   }

   public static void setupOrientationMatrix(MatrixStack matrix, float x, float y, float z) {
      setupOrientationMatrix(matrix, (double)x, (double)y, (double)z);
   }

   private void renderParticle(MatrixStack matrix, Immediate immediate, Particles.Particle particle, float x, float y, float z, float pos, int color, int alpha) {
      matrix.method_22903();
      setupOrientationMatrix(matrix, x, y, z);
      matrix.method_22907(mc.field_1773.method_19418().method_23767());
      Boolean result = this.rotations.getValue();

      if (result) {
         matrix.method_22907(RotationAxis.field_40718.rotationDegrees(particle.rotate()));
      }

      Entry v10 = matrix.method_23760();
      Matrix4f v11 = v10.method_23761();
      Matrix3f v12 = v10.method_23762();
      VertexConsumer v13 = immediate.method_73477(ROMB_ESP.apply(particle.type().texture()));

      this.drawTexturedQuad(v13, v11, v12, -pos, -pos, pos * 2.0F, pos * 2.0F, color, alpha);
      if (particle.type == Particles.ParticleType.BLOOM && this.glows.getValue()) {
         this.drawTexturedQuad(v13, v11, v12, -pos / 2.0F, -pos / 2.0F, pos, pos, color, alpha);
      }

      matrix.method_22909();
   }

   private void drawTexturedQuad(
      VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, float x, float y, float width, float height, int color, int alpha
   ) {
      int v10 = color >> 16 & 255;
      int v11 = color >> 8 & 255;
      int v12 = color & 255;
      REUSABLE_NORMAL.set(0.0F, 0.0F, 1.0F);
      normalMatrix.transform(REUSABLE_NORMAL);
      REUSABLE_NORMAL.normalize();
      float v15 = x + width;
      float v16 = y + height;
      buffer.method_22918(matrix, x, y, 0.0F)
         .method_1336(v10, v11, v12, alpha)
         .method_22913(0.0F, 1.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(REUSABLE_NORMAL.x, REUSABLE_NORMAL.y, REUSABLE_NORMAL.z);
      buffer.method_22918(matrix, v15, y, 0.0F)
         .method_1336(v10, v11, v12, alpha)
         .method_22913(1.0F, 1.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(REUSABLE_NORMAL.x, REUSABLE_NORMAL.y, REUSABLE_NORMAL.z);
      VertexConsumer result = buffer.method_22918(matrix, v15, v16, 0.0F);

      result.method_1336(v10, v11, v12, alpha)
         .method_22913(1.0F, 0.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(REUSABLE_NORMAL.x, REUSABLE_NORMAL.y, REUSABLE_NORMAL.z);
      buffer.method_22918(matrix, x, v16, 0.0F)
         .method_1336(v10, v11, v12, alpha)
         .method_22913(0.0F, 0.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(REUSABLE_NORMAL.x, REUSABLE_NORMAL.y, REUSABLE_NORMAL.z);
   }

   public Particles() {
      this.totem = new BooleanSetting(this, "При тотеме", true);
      this.onTtSizs = new SliderSetting(this, "Кол при тотеме", 75.0F, 20.0F, 150.0F, 1.0F)
         .setVisible(() -> this.totem.getValue());
      this.perka = new BooleanSetting(this, "При кидании", true);
      this.onPerkSizs = new SliderSetting(this, "Кол при кидании", 3.0F, 1.0F, 8.0F, 1.0F).setVisible(() -> {

         return this.perka.getValue();
      });
      this.worlds = new BooleanSetting(this, "В мире", true);
      this.onworldsSizs = new SliderSetting(this, "Кол в мире", 10.0F, 2.0F, 60.0F, 1.0F).setVisible(() -> {

         return this.worlds.getValue();
      });
      this.move = new BooleanSetting(this, "При движении", true);
      String[] temp5 = new String[8];
      temp5[0] = "Client";
      temp5[1] = "Circle";
      temp5[2] = "Bloom";
      temp5[3] = "Star";
      temp5[4] = "Heart";
      temp5[5] = "Snow";
      temp5[6] = "Dollar";
      temp5[7] = "Pumpkin";
      this.particleMode = new ModeSetting(this, "Тип частиц", temp5);
      this.size = new SliderSetting(this, "Размер", 0.5F, 0.0F, 1.0F, 0.1F);
      this.glows = new BooleanSetting(this, "Свечения", true);
      this.glowsiz = new SliderSetting(this, "Сила свечения", 0.5F, 0.0F, 1.0F, 0.1F).setVisible(() -> {

         return this.glows.getValue();
      });
      this.rotations = new BooleanSetting(this, "Ротация", true);
      this.previewSettings = lf.of(this, 4.0F, 0.0F, 2.0F);
      this.targetParticles = new ArrayList<>();
      this.totemParticles = new ArrayList<>();
      this.frameParticles = new ArrayList<>();
      this.worldParticles = new ArrayList<>();
      this.totemSpawnStartTime = 0L;
      this.totemTargetEntity = null;
      this.totemParticlesLeftToSpawn = 0;
      this.totalTotemParticlesToSpawn = 0;
      this.lastUpdateTime = System.nanoTime();
      this.boxAllocator = new BufferAllocator(4096);
   }

   public void spawnParticleTotem(List<Particles.Particle> particles, Vec3d position, Vec3d velocity) {
      float v4 = 0.05F + this.size.getValue() * 0.2F;
      String v6 = this.particleMode.getValue();
      switch (v6.hashCode()) {
         case 2581923:
            if (v6.equals("Snow")) {
               v7 = 2;
            }
            break;
         case 2587250:
            if (v6.equals("Star")) {
               v7 = 1;
            }
            break;
         case 64280035:
            if (v6.equals("Bloom")) {
               v7 = 4;
            }
            break;
         case 69599270:
            if (v6.equals("Heart")) {
               v7 = 0;
            }
            break;
         case 1439567688:
            if (v6.equals("Pumpkin")) {
               v7 = 7;
            }
            break;
         case 2018617584:
            if (v6.equals("Circle")) {
               v7 = 3;
            }
            break;
         case 2021122027:
            if (v6.equals("Client")) {
               v7 = 5;
            }
            break;
         case 2052617436:
            if (v6.equals("Dollar")) {
               v7 = 6;
            }
      }
      Particles.ParticleType v5 = switch (v7) {
         case 0 -> Particles.ParticleType.HEART;
         case 1 -> Particles.ParticleType.STAR;
         case 2 -> Particles.ParticleType.SNOOW;
         case 3 -> Particles.ParticleType.CIRCLE;
         case 4 -> Particles.ParticleType.BLOOM;
         case 5 -> Particles.ParticleType.STAR_NEW;
         case 6 -> Particles.ParticleType.DOLLAR;
         case 7 -> Particles.ParticleType.PUMPKIN;
         default -> Particles.ParticleType.BLOOM;
      };
      int[] v9 = new int[]{
         up.getColor(221, 218, 127),
         up.getColor(127, 221, 144),
         up.getColor(255, 215, 0),
         up.getColor(50, 205, 50)
      };
      v7 = v9[fq.randomInt(0, v9.length - 1)];

      particles.add(
         new Particles.Particle(
            v5,
            position.method_1031(0.0, (double)v4, 0.0),
            velocity,
            particles.size(),
            (int)fq.step(fq.randomValue(0.0F, 360.0F), 15.0F),
            v7,
            v4,
            1.1F,
            false,
            true
         )
      );
   }

   @EventTarget
   public void onEvent(EventMove e) {
      if (this.move.getValue() && this.hasPlayerMoved()) {
         this.spawnParticle(
            this.frameParticles,
            new Vec3d(
               mc.field_1724.method_23317() + fq.randomValue(-0.1, 0.1),
               mc.field_1724.method_23318() + (double)fq.randomValue(0.0F, mc.field_1724.method_17682()),
               mc.field_1724.method_23321() + fq.randomValue(-0.1, 0.1)
            ),
            new Vec3d(fq.randomValue(-0.3, 0.3), fq.randomValue(-0.03, 0.3), fq.randomValue(-0.3, 0.3)),
            true
         );
      }

      if (this.worlds.getValue()) {
         if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
         }

         for (int v3 = 0; (float)v3 < this.onworldsSizs.getValue(); v3++) {
            Vec3d v4 = mc.field_1724
               .method_73189()
               .method_1031((double)fq.randomValue((float)(-v2), (float)v2), 0.0, (double)fq.randomValue((float)(-v2), (float)v2));
            BlockPos v5 = mc.field_1687.method_8598(Type.field_13197, BlockPos.method_49638(v4));
            float result = (float)v5.method_10263();

            double v6 = (double)(result + fq.randomValue(-3.0F, 3.0F));
            double v8 = (double)((float)v5.method_10260() + fq.randomValue(-3.0F, 3.0F));
            double v31 = mc.field_1724.method_23318();
            float temp1 = fq.randomValue(mc.field_1724.method_17682(), (float)v2);

            double v10 = v31 + (double)temp1;
            Vec3d v12 = new Vec3d(v6, v10, v8);

            while (!mc.field_1687.method_22347(BlockPos.method_49638(v12)) && v12.field_1351 < (double)mc.field_1687.method_31600()) {
               v12 = v12.method_1031(0.0, 1.0, 0.0);
            }

            this.spawnParticle(
               this.worldParticles,
               v12,
               new Vec3d(
                  mc.field_1724.method_18798().field_1352 + (double)fq.randomValue(-0.6F, 0.6F),
                  fq.randomValue(-0.01, 0.44),
                  mc.field_1724.method_18798().field_1350 + (double)fq.randomValue(-0.6F, 0.6F)
               ),
               true
            );
         }
      }

      if (this.totemParticlesLeftToSpawn > 0 && this.totemTargetEntity != null) {
         if (!this.totemTargetEntity.method_5805() && mc.field_1687.method_8469(this.totemTargetEntity.method_5628()) == null) {
            this.totemParticlesLeftToSpawn = 0;
            this.totemTargetEntity = null;
         } else {
            long v22 = System.currentTimeMillis() - this.totemSpawnStartTime;
            long v23 = -7746690735011317420L + 7746690735011318920L;
            if (v22 < v23) {
               int v24 = (int)((double)v22 / (double)v23 * (double)this.totalTotemParticlesToSpawn);
               int v7 = this.totalTotemParticlesToSpawn - this.totemParticlesLeftToSpawn;
               int v27 = v24 - v7;
               if (v27 > this.totemParticlesLeftToSpawn) {
                  v27 = this.totemParticlesLeftToSpawn;
               }

               for (int v9 = 0; v9 < v27; v9++) {
                  double v29 = fq.randomValue(0.0, Math.PI * 2);
                  double v30 = fq.randomValue(2.5, 4.0) * 0.1F;
                  double v14 = Math.cos(v29) * v30;
                  double v16 = Math.sin(v29) * v30;
                  double v18 = fq.randomValue(0.0, (double)(this.totemTargetEntity.method_17682() / 4.0F));
                  this.spawnParticleTotem(
                     this.totemParticles,
                     new Vec3d(
                        this.totemTargetEntity.method_23317() + fq.randomValue(-0.2, 0.2),
                        this.totemTargetEntity.method_23318() + (double)fq.randomValue(0.1F, this.totemTargetEntity.method_17682()),
                        this.totemTargetEntity.method_23321() + fq.randomValue(-0.2, 0.2)
                     ),
                     new Vec3d(v14, v18, v16)
                  );
                  this.totemParticlesLeftToSpawn--;
               }
            } else {
               for (int v25 = 0; v25 < this.totemParticlesLeftToSpawn; v25++) {
                  double v26 = fq.randomValue(0.0, Math.PI * 2);
                  double v28 = fq.randomValue(2.5, 4.0) * 0.1F;
                  double v11 = Math.cos(v26) * v28;
                  double v13 = Math.sin(v26) * v28;
                  double v15 = fq.randomValue(0.0, (double)(this.totemTargetEntity.method_17682() / 4.0F));
                  this.spawnParticleTotem(
                     this.totemParticles,
                     new Vec3d(
                        this.totemTargetEntity.method_23317() + fq.randomValue(-0.2, 0.2),
                        this.totemTargetEntity.method_23318() + (double)fq.randomValue(0.1F, this.totemTargetEntity.method_17682()),
                        this.totemTargetEntity.method_23321() + fq.randomValue(-0.2, 0.2)
                     ),
                     new Vec3d(v11, v15, v13)
                  );
               }

               this.totemParticlesLeftToSpawn = 0;
               this.totemTargetEntity = null;
            }
         }
      }

      this.removeExpiredParticles(this.frameParticles, 8339353708731585613L + -8339353708731582613L);
      this.removeExpiredParticles(this.totemParticles, -690382322057695812L + 690382322057699312L);
      this.removeExpiredParticles(this.worldParticles, 3153467299106287991L + -3153467299106284491L);
      this.removeExpiredParticles(this.targetParticles, -794759085597573622L + 794759085597579622L);
   }

   @Override
   public void previewSpawn(kf ctx) {
      OtherClientPlayerEntity v2 = ctx.dummy();
      if (v2 != null) {
         ArrayList result = new ArrayList;

         result./* $VF: Unable to resugar constructor */<init>(3);
         ArrayList v3 = result;
         if (this.onAttack.getValue()) {
            v3.add(() -> this.spawnAttackBurst(v2));
         }

         if (this.totem.getValue()) {
            v3.add(() -> {

               this.spawnTotemBurst(v2, this.onTtSizs.getValue().intValue());
               int v3x = -258685001;
               int var10000x = v3x * (v3x + 1);

               
            });
         }

         if (this.perka.getValue()) {
            v3.add(() -> this.spawnThrowBurst(v2));
         }

         if (v3.isEmpty()) {

            v3.add(() -> {

               this.spawnAttackBurst(v2);
            });
         }

         ((Runnable)v3.get(Math.floorMod(ctx.phase(), v3.size()))).run();
      }
   }

   public static boolean isBlockSolid(double x, double y, double z) {
      if (mc.field_1687 == null) {
         return false;
      } else {
         int temp4 = v7 + 1;

         BlockPos v6 = BlockPos.method_49637(x, y, z);
         BlockState result = mc.field_1687.method_8320(v6);

         return result.method_26234(mc.field_1687, v6);
      }
   }

   private void renderParticle2(
      MatrixStack matrix, Immediate immediate, Particles.Particle particle, float x, float y, float z, float pos, int color, int alpha
   ) {
      matrix.method_22903();

      setupOrientationMatrix(matrix, x, y, z);
      matrix.method_22907(mc.field_1773.method_19418().method_23767());
      if (this.rotations.getValue()) {
         matrix.method_22907(RotationAxis.field_40718.rotationDegrees(particle.rotate()));
      }

      Entry v10 = matrix.method_23760();

      Matrix4f v11 = v10.method_23761();
      Matrix3f v12 = v10.method_23762();
      VertexConsumer v13 = immediate.method_73477(ROMB_ESP.apply(Particles.ParticleType.BLOOM.texture));
      this.drawTexturedQuad(v13, v11, v12, -pos * 3.0F, -pos * 3.0F, pos * 6.0F, pos * 6.0F, color, alpha);
      matrix.method_22909();
   }

   private void clear() {
      this.targetParticles.clear();
      this.frameParticles.clear();
      this.totemParticles.clear();
      this.worldParticles.clear();
      this.totemParticlesLeftToSpawn = 0;
      this.totalTotemParticlesToSpawn = 0;
      this.totemTargetEntity = null;
      this.totemSpawnStartTime = 0L;
   }

   public static void setupOrientationMatrix(MatrixStack matrix, double x, double y, double z) {
      Vec3d v7 = mc.method_1561().field_4686.method_71156();
      double temp1 = x - v7.field_1352;
      double temp2 = y - v7.field_1351;

      double temp3 = z - v7.field_1350;

      matrix.method_22904(temp1, temp2, temp3);
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {

      this.boxAllocator.method_60809();
   }

   public void spawnTotemBurst(Entity target, int count) {
      if (target != null) {
         for (int v3 = 0; v3 < count; v3++) {

            double v4 = fq.randomValue(0.0, Math.PI * 2);
            double v6 = fq.randomValue(2.5, 4.0) * 0.1F;
            this.spawnParticleTotem(
               this.totemParticles,
               new Vec3d(
                  target.method_23317() + fq.randomValue(-0.2, 0.2),
                  target.method_23318() + (double)fq.randomValue(0.1F, target.method_17682()),
                  target.method_23321() + fq.randomValue(-0.2, 0.2)
               ),
               new Vec3d(Math.cos(v4) * v6, fq.randomValue(0.0, (double)(target.method_17682() / 4.0F)), Math.sin(v4) * v6)
            );
         }
      }
   }

   @EventTarget
   public void onEvent(EventTrace event) {
      MatrixStack v2 = event.getMatrixStack();
      long v3 = System.nanoTime();
      double result = (double)(v3 - this.lastUpdateTime);

      double v5 = result / 1.0E9;
      this.lastUpdateTime = v3;

      v2.method_22903();
      Immediate v7 = VertexConsumerProvider.method_22991(this.boxAllocator);
      if (this.glows.getValue()) {
         this.renderParticlesGlow(
            v2, v7, this.targetParticles, -7561420143820851783L + 7561420143820853783L, -1697711855297805906L + 1697711855297808406L, v5
         );
         this.renderParticlesGlow(
            v2, v7, this.totemParticles, 2984438075229362258L + -2984438075229361258L, -7498731686811872485L + 7498731686811873985L, v5
         );
         this.renderParticlesGlow(
            v2, v7, this.worldParticles, 265955241885740374L + -265955241885739374L, 3310120808847455418L + -3310120808847453918L, v5
         );
         this.renderParticlesGlow(
            v2, v7, this.frameParticles, -4514331332644694591L + 4514331332644695341L, 7540545312579850528L + -7540545312579849528L, v5
         );
      }

      long temp4 = -5359704752629621553L + 5359704752629623553L;

      this.renderParticles(v2, v7, this.targetParticles, temp4, -2489227708059088254L + 2489227708059090754L, v5);
      this.renderParticles(v2, v7, this.totemParticles, 184895524636268169L + -184895524636267169L, 6514330379374120296L + -6514330379374118796L, v5);
      this.renderParticles(v2, v7, this.worldParticles, -4376704119438322781L + 4376704119438323781L, 8874907013063514784L + -8874907013063513284L, v5);
      this.renderParticles(v2, v7, this.frameParticles, 4885785626691452549L + -4885785626691451799L, 1827517562348403829L + -1827517562348402829L, v5);
      v7.method_22993();
      v2.method_22909();
   }

   public void spawnThrowBurst(Entity target) {
      if (target != null) {
         Vec3d v2 = target.method_73189().method_1031(0.0, (double)(target.method_17682() * 0.6F), 0.0);

         for (int v3 = 0; (float)v3 < this.onPerkSizs.getValue() * 8.0F; v3++) {

            this.spawnParticle(
               this.frameParticles,
               v2.method_1031(fq.randomValue(-0.1, 0.1), fq.randomValue(-0.1, 0.1), fq.randomValue(-0.1, 0.1)),
               new Vec3d(fq.randomValue(-0.15, 0.15), fq.randomValue(-0.15, 0.15), fq.randomValue(-0.15, 0.15)),
               true
            );
            
         }
      }
   }

   @Override
   public void toggle() {
      super.toggle();
      int result = v1 * (v1 + 1);
      int temp2 = v2 + 1;

      this.clear();
   }

   private void spawnParticle(List<Particles.Particle> particles, Vec3d position, Vec3d velocity, boolean isWorld) {
      float v5 = 0.05F + this.size.getValue() * 0.2F;
      int v6 = up.fade(particles.size() * (100));
      String v8 = this.particleMode.getValue();

      switch (v8.hashCode()) {
         case 2581923:
            if (v8.equals("Snow")) {
               v9 = 2;
            }
            break;
         case 2587250:
            if (v8.equals("Star")) {
               v9 = 1;
            }
            break;
         case 64280035:
            if (v8.equals("Bloom")) {
               v9 = 3;
            }
            break;
         case 69599270:
            if (v8.equals("Heart")) {
               v9 = 0;
            }
            break;
         case 1439567688:
            if (v8.equals("Pumpkin")) {
               v9 = 7;
            }
            break;
         case 2018617584:
            if (v8.equals("Circle")) {
               v9 = 4;
            }
            break;
         case 2021122027:
            if (v8.equals("Client")) {
               v9 = 5;
            }
            break;
         case 2052617436:
            if (v8.equals("Dollar")) {
               v9 = 6;
            }
      }
      Particles.ParticleType v7 = switch (v9) {
         case 0 -> Particles.ParticleType.HEART;
         case 1 -> Particles.ParticleType.STAR;
         case 2 -> Particles.ParticleType.SNOOW;
         case 3 -> Particles.ParticleType.BLOOM;
         case 4 -> Particles.ParticleType.CIRCLE;
         case 5 -> Particles.ParticleType.STAR_NEW;
         case 6 -> Particles.ParticleType.DOLLAR;
         case 7 -> Particles.ParticleType.PUMPKIN;
         default -> Particles.ParticleType.BLOOM;
      };
      Particles.Particle v13 = new Particles.Particle;

      v13./* $VF: Unable to resugar constructor */<init>(
         v7,
         position.method_1031(0.0, (double)v5, 0.0),
         velocity,
         particles.size(),
         (int)fq.step(fq.randomValue(0.0F, 360.0F), 15.0F),
         v6,
         v5,
         1.1F,
         isWorld,
         false
      );
      particles.add(v13);
   }

   @EventTarget
   public void onPacket(EventStep e) {
      if (e.getPacket() instanceof EntityStatusS2CPacket v2) {
         if (v2.method_11470() == 35) {

            Entity v5 = v2.method_11469(mc.field_1687);
            if (v5 == null) {
               return;
            }

            if (this.totem.getValue()) {
               this.totemTargetEntity = v5;
               this.totalTotemParticlesToSpawn = this.onTtSizs.getValue().intValue();
               this.totemParticlesLeftToSpawn = this.totalTotemParticlesToSpawn;
               this.totemSpawnStartTime = System.currentTimeMillis();
            }
         }
      }
   }

   private void removeExpiredParticles(List<Particles.Particle> particles, long lifespan) {
      particles.removeIf(particle -> {
         wu result = particle.time();

         return result.finished((double)lifespan);
      });
   }

   public void spawnAttackBurst(Entity target) {
      if (target != null) {
         float v2 = 1.1F;

         for (int v3 = 0; (float)v3 < this.onAtSizs.getValue(); v3++) {
            List temp1 = this.targetParticles;
            Vec3d temp2 = new Vec3d(
               target.method_23317() + (double)fq.randomValue(-0.035F, 0.035F),
               target.method_23318() + (double)fq.randomValue(0.1F, target.method_17682()),
               target.method_23321() + (double)fq.randomValue(-0.035F, 0.035F)
            );
            Vec3d temp3 = new Vec3d(
               (double)fq.randomValue(-v2 + 0.2F, v2 - 0.2F), (double)fq.randomValue(-v2, v2 / 2.0F), (double)fq.randomValue(-v2 + 0.2F, v2 - 0.2F)
            );

            this.spawnParticle(temp1, temp2, temp3, false);
         }
      }
   }

   private boolean hasPlayerMoved() {
      if (mc.field_1724.field_6038 == mc.field_1724.method_23317() && mc.field_1724.field_5971 == mc.field_1724.method_23318()) {
         double result = mc.field_1724.field_5989;
         double temp1 = mc.field_1724.method_23321();

         if (result == temp1) {
            return false;
         }
      }

      return true;
   }

   @EventTarget
   public void onEvent(EventRenderModel e) {
      ClientPlayerEntity v2 = mc.field_1724;
      if (v2 != null && mc.field_1687 != null) {
         if (this.perka.getValue()) {
            for (Entity v4 : mc.field_1687.method_18112()) {
               if ((v4 instanceof EnderPearlEntity || v4 instanceof ArrowEntity || v4 instanceof TridentEntity)
                  && (!(v4 instanceof TridentEntity v5) || !v5.method_24828())) {
                  boolean v13;
                  label61: {
                     if (v4.field_6038 == v4.method_23317()) {

                        if (v4.field_5971 == v4.method_23318()) {

                           if (v4.field_5989 == v4.method_23321()) {
                              v13 = false;
                              break label61;
                           }
                        }
                     }

                     v13 = true;
                  }

                  boolean v11 = v13;
                  if (v11) {
                     Vec3d v6 = v4.method_73189();

                     for (int v7 = 0; (float)v7 < this.onPerkSizs.getValue(); v7++) {
                        List temp1 = this.frameParticles;
                        Vec3d temp2 = new Vec3d;
                        double temp4 = v6.field_1352 + MathHelper.method_15366(Random.method_43047(), -0.1, 0.1);
                        double temp5 = v6.field_1351 + MathHelper.method_15366(Random.method_43047(), -0.1, 0.1);
                        double var10006 = v6.field_1350;
                        double var10007 = MathHelper.method_15366(Random.method_43047(), -0.1, 0.1);

                        temp2./* $VF: Unable to resugar constructor */<init>(temp4, temp5, var10006 + var10007);
                        this.spawnParticle(
                           temp1,
                           temp2,
                           new Vec3d(
                              MathHelper.method_15366(Random.method_43047(), -0.15, 0.15),
                              MathHelper.method_15366(Random.method_43047(), -0.15, 0.15),
                              MathHelper.method_15366(Random.method_43047(), -0.15, 0.15)
                           ),
                           true
                        );
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean previewNeedsDummy() {

      return true;
   }

   @Override
   public void previewStop() {
      this.clear();
      
   }

   private void renderParticles(
      MatrixStack matrix, Immediate immediate, List<Particles.Particle> particles, long fadeInTime, long fadeOutTime, double deltaTime
   ) {
      if (!particles.isEmpty()) {
         matrix.method_22903();

         for (Particles.Particle v11 : particles) {
            v11.update(deltaTime);
            boolean v12 = !v11.time().finished((double)fadeInTime);
            boolean v13 = v11.time().finished((double)fadeOutTime);
            if (v12) {
               v11.animation().run(1.0, 0.5, ap.LINEAR, true);
            } else if (v13) {
               v11.animation().run(0.0, 2.0, ap.LINEAR, true);
            }

            if (v11.animation.isAlive()) {
               v11.animation.update();
            }

            int temp1 = v18 * (v18 + 1) % 2;

            if (temp1 != 0) {
               }

            float v14 = v11.animation.get();
            int v15 = (int)(v14 * 255.0F);
            if (v15 > 0) {
               int v16 = up.replAlpha(v11.color(), v15);
               Vec3d v17 = v11.position();

               this.renderParticle(
                  matrix, immediate, v11, (float)v17.field_1352, (float)v17.field_1351, (float)v17.field_1350, v11.size * v14, v16, v15
               );
            }
         }

         matrix.method_22909();
      }
   }

   @Override
   public lf previewSettings() {
      return this.previewSettings;
   }

   @EventTarget
   public void onEvent(Event event) {
      if (this.onAttack.getValue()) {
         this.spawnAttackBurst(event.getTarget());
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Particle {
      private Vec3d position;
      private final wu time = new wu();
      private final double speedMultiplier;
      private final int color;
      private final boolean isWorldParticle;
      private final Animation animation = new Animation();
      private static final double BASE_VELOCITY = 0.05;
      private final int index;
      private Vec3d velocity;
      private final Particles.ParticleType type;
      private final boolean isTotem;
      private final float size;
      private float rotate = 0.0F;

      @Generated
      public int color() {

         return this.color;
      }

      @Generated
      public int index() {

         return this.index;
      }

      @Generated
      public boolean isWorldParticle() {
         return this.isWorldParticle;
      }

      @Generated
      public double speedMultiplier() {

         return this.speedMultiplier;
      }

      @Generated
      public wu time() {
         return this.time;
      }

      public void update(double deltaTime) {
         double v3 = deltaTime * 120.0;

         if (!(v3 <= 0.0)) {
            double v5 = 0.85;
            double v7 = this.isWorldParticle ? 1.0E-4 : 5.0E-4;
            double v9 = this.position.field_1352 + this.velocity.field_1352 * v3;
            double v11 = this.position.field_1351 + this.velocity.field_1351 * v3;
            double v13 = this.position.field_1350 + this.velocity.field_1350 * v3;
            if (Particles.isBlockSolid(v9, this.position.field_1351, this.position.field_1350)) {
               this.velocity = new Vec3d(-this.velocity.field_1352 * v5, this.velocity.field_1351, this.velocity.field_1350);
            } else {
               this.position = new Vec3d(v9, this.position.field_1351, this.position.field_1350);
            }

            if (Particles.isBlockSolid(this.position.field_1352, v11, this.position.field_1350)) {
               this.velocity = new Vec3d(this.velocity.field_1352, -this.velocity.field_1351 * v5, this.velocity.field_1350);
            } else {
               this.position = new Vec3d(this.position.field_1352, v11, this.position.field_1350);
            }

            if (Particles.isBlockSolid(this.position.field_1352, this.position.field_1351, v13)) {
               this.velocity = new Vec3d(this.velocity.field_1352, this.velocity.field_1351, -this.velocity.field_1350 * v5);
            } else {
               this.position = new Vec3d(this.position.field_1352, this.position.field_1351, v13);
            }

            this.velocity = this.velocity.method_1023(0.0, v7 * v3, 0.0);
            this.rotate = (float)((double)this.rotate + 2.0 * v3);
         }
      }

      @Generated
      public float size() {

         return this.size;
      }

      @Generated
      public Vec3d velocity() {
         return this.velocity;
      }

      public Particle(
         Particles.ParticleType type,
         Vec3d position,
         Vec3d velocity,
         int index,
         int rotate,
         int color,
         float size,
         double speedMultiplier,
         boolean isWorldParticle,
         boolean isTotem
      ) {
         this.type = type;
         this.position = position;
         this.velocity = isTotem ? velocity.method_1021(0.08) : velocity.method_1021(0.05);
         this.index = index;
         this.color = color;
         this.rotate = (float)rotate;
         this.size = size;
         this.speedMultiplier = speedMultiplier;
         this.isWorldParticle = isWorldParticle;
         this.isTotem = isTotem;
         this.time.reset();
      }

      @Generated
      public float rotate() {

         return this.rotate;
      }

      @Generated
      public Animation animation() {

         return this.animation;
      }

      @Generated
      public boolean isTotem() {
         return this.isTotem;
      }

      @Generated
      public Vec3d position() {

         return this.position;
      }

      @Generated
      public Particles.ParticleType type() {

         return this.type;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static enum ParticleType {
      DOLLAR("dollar", true),
      SNOOW("snowflake", true),
      CIRCLE("circle", true),
      STAR_NEW("client", true),
      BLOOM("glow", true),
      STAR("star", true),
      HEART("heart", true),
      PUMPKIN("pumpkin", true);

      // $VF: synthetic field
      private static final Particles.ParticleType[] $VALUES = $values();
      private final boolean rotatable;
      private final Identifier texture;

      private ParticleType(String name, boolean rotatable) {
         this.texture = Identifier.method_60655("client", "textures/particles/" + name + ".png");
         this.rotatable = rotatable;
      }

      @Generated
      public boolean rotatable() {

         return this.rotatable;
      }

      @Generated
      public Identifier texture() {
         return this.texture;
      }
   }
}
