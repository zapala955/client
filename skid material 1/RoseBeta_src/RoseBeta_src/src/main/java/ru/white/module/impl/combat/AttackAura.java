package ru.white.module.impl.combat;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.StopWatch;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Tf;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.screen.TestGui;
import ru.white.screen.ClickGuiScreen;
import ru.white.utils.animation.ap;
import ru.white.utils.render.mt;
import ru.white.utils.render.vs;
import ru.white.utils.misc.Jp;
import ru.white.utils.misc.Kp;
import ru.white.utils.render.ColorPalette;
import ru.white.utils.render.gl.Jf;
import ru.white.utils.render.gl.Kf;
import ru.white.utils.render.gl.Lf;
import ru.white.utils.render.gl.Wg;
import ru.white.module.impl.misc.FakePlayer;
import ru.white.utils.ItemUtil;
import ru.white.utils.media.Kr;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventInteract;
import ru.white.event.impl.EventPlayerLook;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Attack Aura",
   desc = "Автоматический наводится и атакует цель",
   category = Category.COMBAT,
   key = 82
)
@Environment(EnvType.CLIENT)
public class AttackAura extends Module implements jf {
   private long f260;
   public Vec3d targetPoint;
   private Matrix4f f268;
   private float f258;
   public SliderSetting imgSpeed;
   public SliderSetting conOrbit;
   public SliderSetting pulseCount;
   public Vec3d currentPoint;
   private double f264;
   public SliderSetting spiralPointSize;
   private float f257;
   public SliderSetting conPointSize;
   public BooleanSetting cubeColorOnHit;
   public SliderSetting bublikSpeed;
   public sf previewButton = lf.button(this);
   private final lf f251 = lf.of(this, 4.0F, 0.0F, 2.0F);
   public SliderSetting gyroSpeed;
   public SliderSetting lightningWidth;
   public ModeSetting typeTargetESP;
   private int count;
   private float f254;
   public static int stoptick = 0;
   public ModeSetting typeSnap;
   public SliderSetting funnelSpeed;
   private float f263;
   public SliderSetting ghostSpeed;
   public LivingEntity previewTarget;
   public StopWatch timeSped1;
   public boolean pitchFlickActive;
   private static final RenderPipeline CUBE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "pipeline/aura_target_cube"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   public static float lastPitch;
   public tf lightningColor;
   public BooleanSetting cubeGlow;
   public SliderSetting sizeGlow;
   public SliderSetting spiralGlow;
   public ModeSetting typeSprint;
   public MultiSetting others;
   public MultiSetting noattackto;
   private static final RenderLayer CUBE_LAYER = RenderLayer.method_75940(
      "aura_target_cube",
      RenderSetup.method_75927(CUBE_PIPELINE).method_75937().method_75929(16384).method_75938()
   );
   public BooleanSetting funnelReverse;
   public static float lastYaw;
   public SliderSetting conSpeed;
   private boolean f265;
   public SliderSetting gyroSize;
   public SliderSetting pulseSegs;
   public static long lastAttackTime = 0L;
   public StopWatch timeSped2;
   public SliderSetting funnelSize;
   public SliderSetting spiralPoints;
   private LivingEntity f261;
   public SliderSetting spiralHeight;
   private float f255;
   public SliderSetting attackRange = new SliderSetting(this, "Радиус атаки", 3.0F, 2.5F, 6.0F, 0.1F);
   public SliderSetting spiralSpeed;
   public SliderSetting lightningBranches;
   private Vec3d f262;
   public SliderSetting cubeRotateSpeed;
   private final List<fg.gg> f266;
   public SliderSetting pulseSpeed;
   public SliderSetting spiralRadius;
   private float f256;
   public SliderSetting conStars;
   public SliderSetting cubeThickness;
   public float tick;
   public SliderSetting preRange = new SliderSetting(this, "Радиус обнаружения", 1.0F, 0.0F, 3.0F, 0.1F);
   public ModeSetting typeImages;
   private int f252;
   public SliderSetting pulseRadius;
   private int f259;
   public SliderSetting spiralArms;
   public SliderSetting tespRingSpeed;
   public SliderSetting imgSize;
   public ModeSetting typeMove;
   public BooleanSetting conLines;
   public boolean justAttacked;
   public ModeSetting typeGhost;
   public static LivingEntity target = null;
   public SliderSetting funnelCoils;
   public SliderSetting conGlow;
   public SliderSetting mandalaSpeed;
   private final Random f267;
   public SliderSetting fov;
   public SliderSetting mandalaSize;
   public long attackFlickAt;
   public SliderSetting sizeGlowOFF;
   private int f253;
   public SliderSetting funnelGlow;
   public BooleanSetting gyroCore;
   public SliderSetting funnelPoints;
   public SliderSetting funnelPointSize;
   public BooleanSetting fovRender;
   public ModeSetting typeRotation;
   public long pitchFlickEndTime;
   public SliderSetting cubeSize;
   public SliderSetting mandalaHeight;
   public MultiSetting targets = new MultiSetting(
      this,
      "Кого атаковать",
      new BooleanSetting("Игроков", true),
      new BooleanSetting("Голых игроков", true),
      new BooleanSetting("Мобов", false),
      new BooleanSetting("Друзей", false)
   );
   public SliderSetting bublikSize;
   public BooleanSetting mandalaBeams;

   @Override
   public void previewSpawn(kf c) {
      if (c.dummy() != null) {
         c.dummy().field_6235 = 10;
         if (this.typeTargetESP.is("Молнии")) {
            Vec3d result = c.dummy().method_73189();
            double temp2 = (double)c.dummy().method_17682() * 0.5;

            Vec3d v2 = result.method_1031(0.0, temp2, 0.0);
            mt.strikeAt(v2);
            int v6 = v3 * (v3 + 1);

            
         }
      }
   }

   private void m277() {
      float v1 = this.f263;
      Vec3d v2 = this.f262;
      Quaternionf v3 = new Quaternionf().rotateY((float)Math.toRadians((double)this.f254)).rotateX((float)Math.toRadians((double)this.f255));
      double v4 = this.f264 + 0.05;

      for (float[] v9 : this.m268(v1)) {
         Vector3f v10 = v3.transform(new Vector3f(v9[0], v9[1], v9[2]));
         Vector3f v11 = v3.transform(new Vector3f(v9[3], v9[4], v9[5]));
         Vec3d v12 = v2.method_1031((double)v10.x, (double)v10.y, (double)v10.z);
         Vec3d v13 = v2.method_1031((double)v11.x, (double)v11.y, (double)v11.z);

         Vec3d v14 = v12.method_1019(v13).method_1021(0.5);
         Vector3f v15 = new Vector3f(
            (float)(v13.field_1352 - v14.field_1352), (float)(v13.field_1351 - v14.field_1351), (float)(v13.field_1350 - v14.field_1350)
         );
         Vec3d v16 = v14.method_1020(v2);
         if (v16.method_1027() < 1.0E-6) {
            v16 = new Vec3d(this.f267.nextDouble() - 0.5, 0.0, this.f267.nextDouble() - 0.5);
         }

         v16 = v16.method_1029();
         double v17 = 0.12 + this.f267.nextDouble() * 0.14;
         Vec3d v19 = v16.method_1021(v17)
            .method_1031((this.f267.nextDouble() - 0.5) * 0.06, 0.08 + this.f267.nextDouble() * 0.16, (this.f267.nextDouble() - 0.5) * 0.06);
         Vector3f v20 = new Vector3f(this.f267.nextFloat() - 0.5F, this.f267.nextFloat() - 0.5F, this.f267.nextFloat() - 0.5F);
         if (v20.lengthSquared() < 1.0E-6F) {
            v20.set(0.0F, 1.0F, 0.0F);
         }

         v20.normalize();
         float v21 = 0.12F + this.f267.nextFloat() * 0.28F;
         int v22 = 50 + this.f267.nextInt(30);
         this.f266.add(new fg.gg(v14, v15, v19, v20, v21, v4, v22));
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.field_1724 != null && mc.field_1773.method_19418() != null && mc.field_1687 != null) {
         lastPitch = mc.field_1773.method_19418().method_19329();
         lastYaw = mc.field_1773.method_19418().method_19330();
      }

      target = null;
      this.targetPoint = null;

      this.currentPoint = null;
      this.f252 = 0;
      this.pitchFlickActive = false;
      this.resetCubeState();
      mt.clear();
      wg[] v1 = wg.values();

      for (wg v4 : v1) {
         v4.getRotation().reset();
      }
   }

   private void m275(
      VertexConsumer consumer,
      Vector3f start00,
      Vector3f start10,
      Vector3f start11,
      Vector3f start01,
      Vector3f end00,
      Vector3f end10,
      Vector3f end11,
      Vector3f end01,
      int color
   ) {
      this.m276(consumer, this.f268, start00, end00, end10, start10, color);

      this.m276(consumer, this.f268, start10, end10, end11, start11, color);
      this.m276(consumer, this.f268, start11, end11, end01, start01, color);
      this.m276(consumer, this.f268, start01, end01, end00, start00, color);
   }

   private 

      int v1 = up.getClientColor1(1);
      if ((v1 >> 24 & 255) == 0) {
         v1 |= -16777216;
         
      }

      return v1;
   }

   private void m279(VertexConsumer consumer, float startX, float startY, float startZ, float endX, float endY, float endZ, float thickness, int color) {
      if ((color >> 24 & 255) > 0) {
         Vector3f v10 = new Vector3f(startX, startY, startZ);
         Vector3f v11 = new Vector3f(endX, endY, endZ);
         Vector3f v12 = new Vector3f(v11).sub(v10);
         if (!(v12.lengthSquared() < 1.0E-10F)) {
            v12.normalize();
            Vector3f result;
            if (Math.abs(v12.y) < 0.99F) {
               result = new Vector3f(0.0F, 1.0F, 0.0F);
            } else {
               result = new Vector3f;

               result./* $VF: Unable to resugar constructor */<init>(1.0F, 0.0F, 0.0F);
            }

            Vector3f v13 = result;
            Vector3f v14 = new Vector3f();
            v12.cross(v13, v14).normalize().mul(thickness);
            Vector3f v15 = new Vector3f();
            v12.cross(v14, v15).normalize().mul(thickness);
            Vector3f v16 = new Vector3f(v10).sub(v14).sub(v15);
            Vector3f v17 = new Vector3f(v10).add(v14).sub(v15);
            Vector3f v18 = new Vector3f(v10).add(v14).add(v15);
            Vector3f v19 = new Vector3f(v10).sub(v14).add(v15);
            Vector3f v20 = new Vector3f(v11).sub(v14).sub(v15);
            Vector3f v21 = new Vector3f(v11).add(v14).sub(v15);
            Vector3f v22 = new Vector3f(v11).add(v14).add(v15);
            Vector3f v23 = new Vector3f(v11).sub(v14).add(v15);
            this.m275(consumer, v16, v17, v18, v19, v20, v21, v22, v23, color);
         }
      }
   }

   public AttackAura() {
      String temp4 = "Тип наведения";
      String[] temp5 = new String[6];
      temp5[0] = "SpookyTime";
      temp5[1] = "Snap";
      temp5[2] = "HvH";
      temp5[3] = "DonutSMP";
      temp5[4] = "Neuro";
      temp5[5] = "SpookyDuel";
      this.typeRotation = new ModeSetting(this, temp4, temp5);
      this.typeSnap = new ModeSetting(this, "Режим снапа", "360", "Fov").setVisible(() -> {
         boolean result = this.typeRotation.is("Snap");

         return result;
      });
      this.fov = new SliderSetting(this, "Fov", 50.0F, 25.0F, 90.0F, 1.0F).setVisible(() -> {
         boolean result = this.typeRotation.is("Snap");
         int v1x = 634296923;
         int temp1 = v1x * (v1x + 1);

         return result && this.typeSnap.is("Fov");
      });
      this.fovRender = new BooleanSetting(this, "Отображать Fov", false).setVisible(() -> {
         boolean result = this.typeRotation.is("Snap");

         if (result) {

            if (this.typeSnap.is("Fov")) {
               return true;
            }
         }

         return false;
      });
      this.typeSprint = new ModeSetting(this, "Тип спринта", "Packet", "Silent", "Legit");
      this.typeMove = new ModeSetting(
         this,
         "Коррекция движения",
         "Сфокусированная",
         "Свободная"
      );
      temp4 = "Отображение таргета";
      temp5 = new String[16];
      temp5[0] = "Картинка";
      temp5[1] = "Духи";
      temp5[2] = "Кольцо";
      temp5[3] = "Кубики";
      temp5[4] = "Куб";
      temp5[5] = "Skush";
      temp5[6] = "Призраки";
      temp5[7] = "Бублик";
      temp5[8] = "Воронка";
      temp5[9] = "Спираль";
      temp5[10] = "Импульс";
      temp5[11] = "Созвездие";
      temp5[12] = "Гироскоп";
      temp5[13] = "Мандала";
      temp5[14] = "Молнии";
      temp5[15] = "Не отображать";
      this.typeTargetESP = new ModeSetting(this, temp4, temp5);
      this.typeGhost = new ModeSetting(this, "Тип призраков", "1", "2", "3", "4")
         .setVisible(() -> this.typeTargetESP.is("Призраки"));
      this.ghostSpeed = new SliderSetting(this, "Скорость призраков", 900.0F, 400.0F, 1500.0F, 50.0F)
         .setVisible(() -> {
            int v1x = -573726146;
            int temp2 = v1x * (v1x + 1);

            return this.typeTargetESP.is("Призраки");
         });
      this.sizeGlow = new SliderSetting(this, "Сила свечения", 0.35F, 0.1F, 0.75F, 0.05F).setVisible(() -> {
         boolean result = this.typeTargetESP.is("Призраки");

         Boolean v4 = result;
         int v1x = 1283096067;

         return v4;
      });
      this.sizeGlowOFF = new SliderSetting(this, "Размер свечения", 0.7F, 0.1F, 2.0F, 0.05F)
         .setVisible(() -> {
            boolean result = this.typeTargetESP.is("Призраки");

            return result;
         });
      this.typeImages = new ModeSetting(this, "Тип картинки", "1", "2", "3", "4").setVisible(() -> {
         boolean result = this.typeTargetESP.is("Картинка");

         return result;
      });
      this.imgSpeed = new SliderSetting(this, "Скорость картинки", 1000.0F, 500.0F, 3000.0F, 100.0F)
         .setVisible(() -> {
            int v1x = -301862641;
            int v2x = 1333742381;
            int temp3 = v2x + 1;

            return this.typeTargetESP.is("Картинка");
         });
      this.imgSize = new SliderSetting(this, "Размер картинки", 1.0F, 0.25F, 3.0F, 0.05F).setVisible(() -> {
         int v2x = 1434856412;

         return this.typeTargetESP.is("Картинка");
      });
      this.tespRingSpeed = new SliderSetting(this, "Скорость кольца", 1800.0F, 600.0F, 4000.0F, 100.0F)
         .setVisible(() -> {

            return this.typeTargetESP.is("Кольцо");
         });
      this.bublikSpeed = new SliderSetting(this, "Скорость бублика", 1400.0F, 600.0F, 4000.0F, 100.0F)
         .setVisible(() -> {

            return this.typeTargetESP.is("Бублик");
         });
      this.bublikSize = new SliderSetting(this, "Размер бублика", 1.0F, 0.5F, 2.5F, 0.05F).setVisible(() -> {
         boolean result = this.typeTargetESP.is("Бублик");

         return result;
      });
      this.cubeSize = new SliderSetting(this, "Размер куба", 1.15F, 0.6F, 2.5F, 0.05F).setVisible(() -> {

         return this.typeTargetESP.is("Куб");
      });
      this.cubeRotateSpeed = new SliderSetting(this, "Скорость вращения куба", 1.2F, 0.2F, 4.0F, 0.05F)
         .setVisible(() -> {
            Boolean result = this.typeTargetESP.is("Куб");

            return result;
         });
      this.cubeThickness = new SliderSetting(this, "Толщина куба", 2.2F, 0.6F, 5.0F, 0.1F)
         .setVisible(() -> this.typeTargetESP.is("Куб"));
      this.cubeColorOnHit = new BooleanSetting(this, "Цвет куба при ударе", true)
         .setVisible(() -> this.typeTargetESP.is("Куб"));
      this.cubeGlow = new BooleanSetting(this, "Свечение куба", true).setVisible(() -> {

         return this.typeTargetESP.is("Куб");
      });
      this.funnelCoils = new SliderSetting(this, "Витков воронки", 3.0F, 1.0F, 6.0F, 1.0F).setVisible(() -> {
         int v1x = -1281378661;

         return this.typeTargetESP.is("Воронка");
      });
      this.funnelPoints = new SliderSetting(this, "Точек в витке", 28.0F, 10.0F, 60.0F, 2.0F).setVisible(() -> {

         return this.typeTargetESP.is("Воронка");
      });
      this.funnelSpeed = new SliderSetting(this, "Скорость воронки", 1400.0F, 400.0F, 4000.0F, 100.0F)
         .setVisible(() -> {
            int v1x = -357808834;

            return this.typeTargetESP.is("Воронка");
         });
      this.funnelSize = new SliderSetting(this, "Ширина воронки", 1.0F, 0.3F, 2.5F, 0.05F).setVisible(() -> {

         return this.typeTargetESP.is("Воронка");
      });
      this.funnelReverse = new BooleanSetting(this, "Обратная воронка", false).setVisible(() -> {

         return this.typeTargetESP.is("Воронка");
      });
      this.funnelPointSize = new SliderSetting(this, "Размер точек воронки", 0.16F, 0.04F, 0.6F, 0.02F)
         .setVisible(() -> {

            return this.typeTargetESP.is("Воронка");
         });
      this.funnelGlow = new SliderSetting(this, "Свечение воронки", 0.5F, 0.0F, 1.0F, 0.05F)
         .setVisible(() -> {

            return this.typeTargetESP.is("Воронка");
         });
      this.spiralArms = new SliderSetting(this, "Рук спирали", 3.0F, 1.0F, 5.0F, 1.0F).setVisible(() -> {
         int v1x = 56527261;

         return this.typeTargetESP.is("Спираль");
      });
      this.spiralPoints = new SliderSetting(this, "Точек в руке", 20.0F, 8.0F, 40.0F, 1.0F).setVisible(() -> {

         return this.typeTargetESP.is("Спираль");
      });
      this.spiralSpeed = new SliderSetting(this, "Скорость спирали", 2000.0F, 500.0F, 5000.0F, 100.0F)
         .setVisible(() -> {
            Boolean result = this.typeTargetESP.is("Спираль");
            int v1x = -327024439;
            int temp1 = v1x * (v1x + 1) % 2;

            if (temp1 != 0) {
               }

            return result;
         });
      this.spiralRadius = new SliderSetting(this, "Радиус спирали", 1.0F, 0.3F, 2.0F, 0.05F).setVisible(() -> {
         Boolean result = this.typeTargetESP.is("Спираль");

         return result;
      });
      this.spiralHeight = new SliderSetting(this, "Высота спирали", 1.2F, 0.5F, 3.0F, 0.1F)
         .setVisible(() -> this.typeTargetESP.is("Спираль"));
      this.spiralPointSize = new SliderSetting(this, "Размер точек спирали", 0.16F, 0.04F, 0.6F, 0.02F)
         .setVisible(() -> {
            Boolean result = this.typeTargetESP.is("Спираль");
            int v1x = 555511940;

            return result;
         });
      this.spiralGlow = new SliderSetting(this, "Свечение спирали", 0.5F, 0.0F, 1.0F, 0.05F)
         .setVisible(() -> {

            return this.typeTargetESP.is("Спираль");
         });
      this.pulseCount = new SliderSetting(this, "Кол-во волн", 3.0F, 1.0F, 6.0F, 1.0F).setVisible(() -> {

         return this.typeTargetESP.is("Импульс");
      });
      this.pulseSpeed = new SliderSetting(this, "Скорость волны", 1200.0F, 400.0F, 3000.0F, 100.0F)
         .setVisible(() -> this.typeTargetESP.is("Импульс"));
      this.pulseRadius = new SliderSetting(this, "Макс. радиус", 1.5F, 0.5F, 3.0F, 0.1F).setVisible(() -> {

         boolean result = this.typeTargetESP.is("Импульс");

         return result;
      });
      this.pulseSegs = new SliderSetting(this, "Сегментов кольца", 40.0F, 16.0F, 80.0F, 4.0F)
         .setVisible(() -> this.typeTargetESP.is("Импульс"));
      this.conStars = new SliderSetting(this, "Звёзд", 8.0F, 3.0F, 16.0F, 1.0F).setVisible(() -> {

         boolean result = this.typeTargetESP.is("Созвездие");

         return result;
      });
      this.conSpeed = new SliderSetting(this, "Скорость созвездия", 3000.0F, 800.0F, 7000.0F, 200.0F)
         .setVisible(() -> this.typeTargetESP.is("Созвездие"));
      this.conOrbit = new SliderSetting(this, "Орбита", 1.0F, 0.4F, 2.5F, 0.05F).setVisible(() -> {

         return this.typeTargetESP.is("Созвездие");
      });
      this.conLines = new BooleanSetting(this, "Линии между звёздами", true).setVisible(() -> {

         return this.typeTargetESP.is("Созвездие");
      });
      this.conPointSize = new SliderSetting(this, "Размер звёзд", 0.16F, 0.04F, 0.6F, 0.02F).setVisible(() -> {

         return this.typeTargetESP.is("Созвездие");
      });
      this.conGlow = new SliderSetting(this, "Свечение звёзд", 0.5F, 0.0F, 1.0F, 0.05F).setVisible(() -> {

         return this.typeTargetESP.is("Созвездие");
      });
      this.gyroSpeed = new SliderSetting(this, "Скорость гироскопа", 1800.0F, 600.0F, 4000.0F, 100.0F)
         .setVisible(() -> {
            int v1x = 1214196022;

            return this.typeTargetESP.is("Гироскоп");
         });
      this.gyroSize = new SliderSetting(this, "Размер гироскопа", 1.0F, 0.4F, 2.0F, 0.05F)
         .setVisible(() -> {

            return this.typeTargetESP.is("Гироскоп");
         });
      this.gyroCore = new BooleanSetting(this, "Ядро", true).setVisible(() -> {
         Boolean result = this.typeTargetESP.is("Гироскоп");

         return result;
      });
      this.mandalaSpeed = new SliderSetting(this, "Скорость мандалы", 2200.0F, 600.0F, 5000.0F, 100.0F)
         .setVisible(() -> {
            boolean result = this.typeTargetESP.is("Мандала");
            int v1x = -242496212;

            Boolean v4 = result;

            return v4;
         });
      this.mandalaSize = new SliderSetting(this, "Размер мандалы", 1.1F, 0.5F, 2.2F, 0.05F).setVisible(() -> {
         Boolean result = this.typeTargetESP.is("Мандала");

         return result;
      });
      this.mandalaHeight = new SliderSetting(this, "Высота лучей", 1.0F, 0.2F, 2.0F, 0.1F).setVisible(() -> {
         boolean result = this.typeTargetESP.is("Мандала");
         int v1x = 1815858176;
         int temp1 = v1x * (v1x + 1) % 2;

         if (temp1 != 0) {
            }

         return result;
      });
      this.mandalaBeams = new BooleanSetting(this, "Лазерные лучи", true).setVisible(() -> {
         boolean result = this.typeTargetESP.is("Мандала");

         return result;
      });
      this.lightningColor = new tf(this, "Цвет молнии", -3416833).setVisible(() -> {

         return this.typeTargetESP.is("Молнии");
      });
      this.lightningWidth = new SliderSetting(this, "Ширина молнии", 1.6F, 0.5F, 5.0F, 0.1F).setVisible(() -> {

         return this.typeTargetESP.is("Молнии");
      });
      this.lightningBranches = new SliderSetting(this, "Ветви молнии", 3.0F, 0.0F, 4.0F, 1.0F)
         .setVisible(() -> this.typeTargetESP.is("Молнии"));
      this.others = new MultiSetting(
         this,
         "Доп. проверки",
         new BooleanSetting("Умные криты", false),
         new BooleanSetting("Бить через блоки", false)
      );
      this.noattackto = new MultiSetting(
         this,
         "Не бить если",
         new BooleanSetting("Используешь еду", false),
         new BooleanSetting("Открыт контейнер", false)
      );
      this.tick = 0.0F;
      this.justAttacked = false;
      this.attackFlickAt = 0L;
      this.f252 = 0;
      this.f253 = ThreadLocalRandom.current().nextInt(14, 19);
      this.pitchFlickActive = false;
      this.pitchFlickEndTime = 0L;
      this.timeSped1 = new StopWatch();
      this.timeSped2 = new StopWatch();
      this.f260 = System.currentTimeMillis();
      this.f263 = 0.6F;
      this.f266 = new ArrayList<>();
      this.f267 = new Random();
   }

   @EventTarget
   private void onResetOnWorld(EventSync event) {
      if (mc.field_1724 != null && mc.field_1773.method_19418() != null && mc.field_1687 != null) {
         lastPitch = mc.field_1773.method_19418().method_19329();
         lastYaw = mc.field_1773.method_19418().method_19330();
      }

      target = null;
      this.targetPoint = null;
      this.currentPoint = null;
      this.f252 = 0;

      this.pitchFlickActive = false;
      this.resetCubeState();
      mt.clear();
      wg[] v2 = wg.values();
      int v3 = v2.length;

      while (v4 < v3) {
         wg v5 = v2[v4];
         v5.getRotation().reset();
         v4++;
      }
   }

   public void renderTargetCubeFragments(EventTrace event, Immediate immediate) {
      if (!this.f266.isEmpty()) {
         Vec3d v3 = mc.field_1773.method_19418().method_71156();
         int v4 = this.m281();
         VertexConsumer v5 = immediate.method_73477(CUBE_LAYER);
         this.f268 = event.getMatrixStack().method_23760().method_23761();
         float v6 = event.getTickDelta();

         for (fg.gg v8 : this.f266) {
            float v9 = 1.0F - (float)v8.f250 / (float)Math.max(1, v8.f249);
            float v10 = v9 > 0.3F ? 1.0F : Math.max(0.0F, v9 / 0.3F);
            if (!(v10 <= 0.0F)) {
               Vec3d v11 = new Vec3d(
                  MathHelper.method_16436((double)v6, v8.f241.field_1352, v8.f242.field_1352),
                  MathHelper.method_16436((double)v6, v8.f241.field_1351, v8.f242.field_1351),
                  MathHelper.method_16436((double)v6, v8.f241.field_1350, v8.f242.field_1350)
               );
               Vector3f result = new Vector3f;

               result./* $VF: Unable to resugar constructor */<init>(v8.f243);
               Vector3f v12 = result.lerp(v8.f244, v6);
               double v13 = v11.field_1352 - v3.field_1352;
               double v15 = v11.field_1351 - v3.field_1351;
               double v17 = v11.field_1350 - v3.field_1350;
               float v19 = this.m280(v3, v11.field_1352, v11.field_1351, v11.field_1350) * this.cubeThickness.getValue() * 0.5F;
               int v20 = this.m282(this.m283(v4, 255), v10);
               this.m279(
                  v5,
                  (float)(v13 - (double)v12.x),
                  (float)(v15 - (double)v12.y),
                  (float)(v17 - (double)v12.z),
                  (float)(v13 + (double)v12.x),
                  (float)(v15 + (double)v12.y),
                  (float)(v17 + (double)v12.z),
                  v19,
                  v20
               );
            }
         }

         immediate.method_22994(CUBE_LAYER);
      }
   }

   @EventTarget
   public void onDisplay(EventInteract e) {
   }

   private void m269(
      EventTrace event,
      Quaternionf rotation,
      double relativeX,
      double relativeY,
      double relativeZ,
      float half,
      float coreHalf,
      int baseColor,
      float appear,
      Immediate immediate
   ) {
      RenderLayer v14 = xd.ROMB_ESP
         .apply(Identifier.method_60655("client", "textures/visuals/particles_2.png"));
      VertexConsumer v15 = immediate.method_73477(v14);
      MatrixStack v16 = event.getMatrixStack();
      Quaternionf v17 = mc.field_1773.method_19418().method_23767();
      int v18 = this.m282(this.m283(baseColor, 55), 0.45F * appear);
      float v19 = coreHalf * 5.0F;

      for (float[] v23 : this.m268(half)) {
         for (int v24 = 0; v24 <= 2; v24++) {
            float v25 = (float)v24 * 0.5F;
            float v26 = v23[0] + (v23[3] - v23[0]) * v25;
            float v27 = v23[1] + (v23[4] - v23[1]) * v25;
            float result = v23[2];
            float temp1 = v23[5];
            float temp2 = v23[2];

            float v28 = result + (temp1 - temp2) * v25;
            Vector3f v29 = rotation.transform(new Vector3f(v26, v27, v28));
            this.m270(v15, v16, v17, relativeX + (double)v29.x, relativeY + (double)v29.y, relativeZ + (double)v29.z, v19, v18);
         }
      }

      for (byte v32 = -1; v32 <= 1; v32 += 2) {
         for (byte v33 = -1; v33 <= 1; v33 += 2) {
            for (byte v34 = -1; v34 <= 1; v34 += 2) {
               Vector3f v35 = rotation.transform(new Vector3f((float)v32 * half, (float)v33 * half, (float)v34 * half));
               this.m270(v15, v16, v17, relativeX + (double)v35.x, relativeY + (double)v35.y, relativeZ + (double)v35.z, v19 * 1.2F, v18);
            }
         }
      }

      immediate.method_22994(v14);
   }

   private int m284(int first, int second, float factor) {
      float v4 = MathHelper.method_15363(factor, 0.0F, 1.0F);
      int v5 = first >> 16 & 255;

      int v6 = first >> 8 & 255;

      int v7 = first & 255;
      int v8 = first >> 24 & 255;
      int v9 = second >> 16 & 255;
      int v10 = second >> 8 & 255;
      int v11 = second & 255;
      int v12 = second >> 24 & 255;
      int v13 = (int)((float)v5 + (float)(v9 - v5) * v4);
      int v14 = (int)((float)v6 + (float)(v10 - v6) * v4);
      int result = (int)((float)v7 + (float)(v11 - v7) * v4);

      int v15 = result;
      int v16 = (int)((float)v8 + (float)(v12 - v8) * v4);
      return v16 << 24 | v13 << 16 | v14 << 8 | v15;
   }

   public void renderTargetCube(EventTrace event, LivingEntity renderTarget, float alphaProgress, Immediate immediate) {
      Vec3d v5 = mc.field_1773.method_19418().method_71156();
      Vec3d v6 = renderTarget.method_30950(event.getTickDelta());
      float v7 = renderTarget.method_17682();
      float v8 = renderTarget.method_17681();

      if (renderTarget.method_5805()) {
         if (this.f261 != renderTarget) {
            this.f259 = renderTarget.field_6235;
         }

         this.f261 = renderTarget;
         this.f265 = false;
      }

      float v9 = (float)ap.SINE_IN_OUT.ease((double)MathHelper.method_15363(alphaProgress, 0.0F, 1.0F));
      if (!(v9 <= 0.001F)) {
         float v10 = (v8 * 0.5F + 0.08F) * this.cubeSize.getValue() * (0.55F + 0.45F * v9);
         Vec3d v11 = v6.method_1031(0.0, (double)v7 * 0.5, 0.0);
         this.f262 = v11;
         this.f263 = v10;
         this.f264 = v6.field_1351;
         long v12 = System.currentTimeMillis();
         float v14 = MathHelper.method_15363((float)(v12 - this.f260) / 1000.0F, 0.0F, 0.1F);
         this.f260 = v12;
         float v15 = 1.0F - (float)Math.exp((double)(-(6.0F + this.cubeRotateSpeed.getValue() * 3.0F) * v14));
         this.f254 = this.f254 + (this.f256 - this.f254) * v15;
         this.f255 = this.f255 + (this.f257 - this.f255) * v15;
         if (Math.abs(this.f256 - this.f254) < 0.05F) {
            this.f254 = this.f256;
         }

         if (Math.abs(this.f257 - this.f255) < 0.05F) {
            this.f255 = this.f257;
         }

         this.f258 = Math.max(0.0F, this.f258 - v14 * 2.2F);
         int v16 = this.m281();
         int v17 = v16;

         if (this.cubeColorOnHit.getValue() && this.f258 > 0.0F) {
            v17 = this.m284(v16, up.getColor(255, 25, 25, 255), this.f258);
         }

         int v39 = this.m282(this.m283(v17, 190), v9);

         v17 = v39;
         Quaternionf v18 = new Quaternionf().rotateY((float)Math.toRadians((double)this.f254)).rotateX((float)Math.toRadians((double)this.f255));
         float v19 = this.m280(v5, v11.field_1352, v11.field_1351, v11.field_1350) * this.cubeThickness.getValue() * 0.5F;
         VertexConsumer v20 = immediate.method_73477(CUBE_LAYER);
         double v21 = v11.field_1352 - v5.field_1352;
         double v23 = v11.field_1351 - v5.field_1351;
         double v25 = v11.field_1350 - v5.field_1350;
         this.f268 = event.getMatrixStack().method_23760().method_23761();

         for (float[] v30 : this.m268(v10)) {
            this.m273(v20, v18, v21, v23, v25, v30, v19, v17);
         }

         for (byte v35 = -1; v35 <= 1; v35 += 2) {
            for (byte v36 = -1; v36 <= 1; v36 += 2) {
               for (byte v37 = -1; v37 <= 1; v37 += 2) {
                  this.m272(v20, v18, v21, v23, v25, (float)v35 * v10, (float)v36 * v10, (float)v37 * v10, v19, v17);
               }
            }
         }

         immediate.method_22994(CUBE_LAYER);
         if (this.cubeGlow.getValue()) {
            this.m269(event, v18, v21, v23, v25, v10, v19, v16, v9, immediate);
         }
      }
   }

   @EventTarget
   public void onDisplay(EventReceivePacket e) {
      if (mc.field_1724 != null && mc.field_1687 != null && target != null) {
         if (!this.typeRotation.is("Snap") || !this.typeSnap.is("Fov") || !this.fovRender.getValue() || mc.field_1724 == null || mc.field_1687 == null) {
            return;
         }

         float v2 = 2.0F;
         float v3 = (float)mc.method_22683().method_4495();

         float v4 = v2 / v3;
         int v5 = (int)((float)mc.method_22683().method_4486() / v4);
         int v6 = (int)((float)mc.method_22683().method_4502() / v4);
         float v7 = (float)v5;
         float v8 = (float)v6;
         float v9 = v7 / 2.0F;
         float v10 = v8 / 2.0F;
         float v11 = vs.zs.lastProjMat.m00();
         int v12 = target.field_6235;
         float v13 = (float)Math.sin((double)v12 * (Math.PI / 10));
         int v14 = up.getColor(255, 100, 100, 255);
         float v15 = (float)(Math.tan(Math.toRadians((double)(this.fov.getValue() - 15.0F))) * (double)(v7 / 2.0F) * (double)v11);
         float v16 = v15 * 2.0F;
         float v17 = v9 - v15;

         float v18 = v10 - v15;
         boolean v19 = target != null;
         int v21 = up.overCol(up.multAlpha(up.getColor(255), 255.0F), v14, v13);
         vs.ys.outline(v17, v18, v16, v16, 1.0F, up.replAlpha(v21, 0.1F), v15);
      }
   }

   @Override
   public void previewStop() {

      this.previewTarget = null;
      mt.clear();
   }

   private void m270(VertexConsumer consumer, MatrixStack matrices, Quaternionf cameraRotation, double x, double y, double z, float size, int color) {
      if ((color >> 24 & 255) > 0) {
         matrices.method_22903();
         matrices.method_22904(x, y, z);
         matrices.method_22907(cameraRotation);
         Matrix4f v12 = matrices.method_23760().method_23761();
         this.m271(consumer, v12, -size, -size, 0.0F, 0.0F, 1.0F, color);
         float temp4 = -size;

         this.m271(consumer, v12, size, temp4, 0.0F, 1.0F, 1.0F, color);
         this.m271(consumer, v12, size, size, 0.0F, 1.0F, 0.0F, color);
         this.m271(consumer, v12, -size, size, 0.0F, 0.0F, 0.0F, color);
         matrices.method_22909();
      }
   }

   public double getTargetFov(LivingEntity entity) {
      Vec3d v2 = entity.method_73189().method_1031(0.0, (double)entity.method_17682() * 0.5, 0.0);
      Vec3d v3 = v2.method_1020(mc.field_1724.method_33571());
      float v4 = (float)Math.toDegrees(Math.atan2(-v3.field_1352, v3.field_1350));
      double result = -Math.toDegrees(Math.atan2(v3.field_1351, Math.hypot(v3.field_1352, v3.field_1350)));

      float v5 = (float)MathHelper.method_15350(result, -90.0, 90.0);
      float v6 = MathHelper.method_15393(v4 - Rotation.cameraYaw());
      float v12 = Rotation.cameraPitch();

      float v7 = v5 - v12;

      return Math.hypot((double)v6, (double)v7);
   }

   @Override
   public void previewStart(kf c) {
      this.previewTarget = c.dummy();
      if (this.typeTargetESP.is("Молнии") && this.previewTarget != null) {
         Vec3d result = this.previewTarget.method_73189();

         Vec3d v2 = result.method_1031(0.0, (double)this.previewTarget.method_17682() * 0.5, 0.0);
         mt.strikeAt(v2);
      }
   }

   @EventTarget
   public void onEvent(EventTick e) {
      LivingEntity v2 = target;
      if (target == null || !this.m267(target)) {
         this.m265();
      }

      if (stoptick != 0) {
         stoptick--;
      }

      if (!this.m264() && target != null) {
         this.attackEntity();
      }
   }

   public void resetCubeState() {
      this.f254 = 0.0F;

      this.f255 = 0.0F;
      this.f256 = 0.0F;
      this.f257 = 0.0F;
      this.f258 = 0.0F;
      this.f259 = 0;
      this.f260 = System.currentTimeMillis();
      this.f261 = null;
      this.f262 = null;
      this.f263 = 0.6F;
      this.f264 = 0.0;
      this.f265 = false;

      this.f266.clear();
      this.f268 = null;
   }

   private void m273(
      VertexConsumer consumer, Quaternionf rotation, double relativeX, double relativeY, double relativeZ, float[] arm, float thickness, int color
   ) {
      if ((color >> 24 & 255) > 0) {
         Vector3f v12 = new Vector3f(arm[0], arm[1], arm[2]);
         Vector3f v13 = new Vector3f(arm[3], arm[4], arm[5]);
         Vector3f v14 = new Vector3f(v13).sub(v12);
         Vector3f v15;
         Vector3f v16;
         if (Math.abs(v14.x) > 1.0E-5F) {
            v15 = new Vector3f(0.0F, thickness, 0.0F);
            v16 = new Vector3f(0.0F, 0.0F, thickness);
         } else if (Math.abs(v14.y) > 1.0E-5F) {
            v15 = new Vector3f(thickness, 0.0F, 0.0F);
            v16 = new Vector3f(0.0F, 0.0F, thickness);
         } else {
            v15 = new Vector3f(thickness, 0.0F, 0.0F);
            v16 = new Vector3f(0.0F, thickness, 0.0F);
         }

         Vector3f v17 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(v12).sub(v15).sub(v16));
         Vector3f v18 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(v12).add(v15).sub(v16));
         Vector3f temp5 = new Vector3f;

         temp5./* $VF: Unable to resugar constructor */<init>(v12);
         Vector3f v19 = this.m274(rotation, relativeX, relativeY, relativeZ, temp5.add(v15).add(v16));
         Vector3f v20 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(v12).sub(v15).add(v16));
         Vector3f v21 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(v13).sub(v15).sub(v16));
         Vector3f v22 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(v13).add(v15).sub(v16));
         Vector3f v23 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(v13).add(v15).add(v16));
         Vector3f v24 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(v13).sub(v15).add(v16));
         this.m275(consumer, v17, v18, v19, v20, v21, v22, v23, v24, color);
      }
   }

   @Override
   public lf previewSettings() {
      return this.f251;
   }

   private float[][] m268(float half) {
      float v2 = half * 0.46F;
      float v3 = half - v2;
      float[][] v4 = new float[24][];

      for (byte v6 = -1; v6 <= 1; v6 += 2) {
         for (byte v7 = -1; v7 <= 1; v7 += 2) {
            for (byte v8 = -1; v8 <= 1; v8 += 2) {
               float v9 = (float)v6 * half;
               float v10 = (float)v7 * half;
               float v11 = (float)v8 * half;
               int temp1 = v5++;
               float[] temp2 = new float[6];
               temp2[0] = v9;
               temp2[1] = v10;
               temp2[2] = v11;
               temp2[3] = (float)v6 * v3;
               temp2[4] = v10;
               temp2[5] = v11;
               v4[temp1] = temp2;
               temp1 = v5++;
               temp2 = new float[6];
               temp2[0] = v9;
               temp2[1] = v10;
               temp2[2] = v11;
               temp2[3] = v9;
               temp2[4] = (float)v7 * v3;
               temp2[5] = v11;
               v4[temp1] = temp2;
               temp1 = v5++;
               temp2 = new float[6];
               temp2[0] = v9;
               temp2[1] = v10;
               temp2[2] = v11;

               temp2[3] = v9;
               temp2[4] = v10;
               temp2[5] = (float)v8 * v3;
               v4[temp1] = temp2;
            }
         }
      }

      return v4;
   }

   private void m278() {
      Iterator v1 = this.f266.iterator();

      while (v1.hasNext()) {
         fg.gg v2 = (fg.gg)v1.next();
         v2.f241 = v2.f242;
         v2.f243 = new Vector3f(v2.f244);
         v2.f242 = v2.f242.method_1019(v2.f245);
         v2.f245 = new Vec3d(v2.f245.field_1352 * 0.98, v2.f245.field_1351 - 0.028, v2.f245.field_1350 * 0.98);
         if (v2.f242.field_1351 <= v2.f248) {

            if (v2.f245.field_1351 < 0.0) {
               v2.f242 = new Vec3d(v2.f242.field_1352, v2.f248, v2.f242.field_1350);
               v2.f245 = new Vec3d(v2.f245.field_1352 * 0.55, -v2.f245.field_1351 * 0.38, v2.f245.field_1350 * 0.55);

               v2.f247 *= 0.6F;
            }
         }

         Quaternionf v3 = new Quaternionf().fromAxisAngleRad(v2.f246.x, v2.f246.y, v2.f246.z, v2.f247);
         v2.f244 = v3.transform(new Vector3f(v2.f244));
         v2.f250++;
         if (v2.f250 >= v2.f249) {
            v1.remove();
         }
      }
   }

   @Override
   public boolean previewNeedsDummy() {
      return true;
   }

   private float m266() {
      Float result = this.attackRange.getValue();

      return result + this.preRange.getValue();
   }

   private float m280(Vec3d cameraPos, double worldX, double worldY, double worldZ) {
      double v8 = worldX - cameraPos.field_1352;
      double v10 = worldY - cameraPos.field_1351;
      double v12 = worldZ - cameraPos.field_1350;
      double v14 = Math.sqrt(v8 * v8 + v10 * v10 + v12 * v12);

      return (float)Math.max(0.1, v14 * 0.007);
   }

   private boolean m264() {
      boolean v5;
      label42: {
         if (mc.field_1724.method_6115()) {
            MultiSetting result = this.noattackto;
            String temp1 = "Используешь еду";
            int temp3 = v3 + 1;

            if (result.getValue(temp1)) {
               v5 = true;
               break label42;
            }
         }

         v5 = false;
      }

      boolean v1 = v5;
      boolean v2 = this.noattackto.getValue("Открыт контейнер")
         && mc.field_1755 != null
         && !(mc.field_1755 instanceof ko)
         && !(mc.field_1755 instanceof TestGui);
      return v1 || v2;
   }

   private void m272(
      VertexConsumer consumer,
      Quaternionf rotation,
      double relativeX,
      double relativeY,
      double relativeZ,
      float centerX,
      float centerY,
      float centerZ,
      float thickness,
      int color
   ) {
      if ((color >> 24 & 255) > 0) {

         Vector3f v14 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX - thickness, centerY - thickness, centerZ - thickness));
         Vector3f v15 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX + thickness, centerY - thickness, centerZ - thickness));
         Vector3f v16 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX + thickness, centerY + thickness, centerZ - thickness));
         Vector3f v17 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX - thickness, centerY + thickness, centerZ - thickness));
         Vector3f v18 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX - thickness, centerY - thickness, centerZ + thickness));
         Vector3f v19 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX + thickness, centerY - thickness, centerZ + thickness));
         Vector3f v20 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX + thickness, centerY + thickness, centerZ + thickness));
         Vector3f v21 = this.m274(rotation, relativeX, relativeY, relativeZ, new Vector3f(centerX - thickness, centerY + thickness, centerZ + thickness));
         this.m276(consumer, this.f268, v14, v15, v16, v17, color);
         this.m276(consumer, this.f268, v18, v21, v20, v19, color);
         this.m276(consumer, this.f268, v14, v18, v19, v15, color);
         this.m276(consumer, this.f268, v17, v16, v20, v21, color);
         this.m276(consumer, this.f268, v14, v17, v21, v18, color);

         this.m276(consumer, this.f268, v15, v19, v20, v16, color);
      }
   }

   private boolean m267(LivingEntity entity) {
      if (entity instanceof ClientPlayerEntity) {
         return false;
      } else if (mc.field_1724.method_5739(entity) > this.m266()) {
         return false;
      } else if (!this.others.getValue("Бить через блоки") && !mc.field_1724.method_6057(entity)) {
         return false;
      } else {
         if (entity instanceof PlayerEntity v2) {
            if (eg.getInstance().isBot(v2)) {
               return false;
            }

            if (Client.get().friendManager().isFriend(v2.method_5477().getString())) {

               if (!this.targets.getValue("Друзей")) {
                  return false;
               }
            }
         }

         if (entity instanceof PlayerEntity && !this.targets.getValue("Игроков")) {
            return false;
         } else if (entity instanceof PlayerEntity
            && entity.method_18396() <= 0.0F
            && !this.targets.getValue("Голых игроков")) {
            return false;
         } else if (entity instanceof PlayerEntity && ((PlayerEntity)entity).method_68878()) {
            return false;
         } else {
            return (
                     entity instanceof Monster
                        || entity instanceof SlimeEntity
                        || entity instanceof VillagerEntity
                        || entity instanceof AnimalEntity
                        || entity instanceof FishEntity
                        || entity instanceof SchoolingFishEntity
                        || entity instanceof CodEntity
                        || entity instanceof SalmonEntity
                        || entity instanceof TropicalFishEntity
                        || entity instanceof PufferfishEntity
                        || entity instanceof SquidEntity
                        || entity instanceof GlowSquidEntity
                        || entity instanceof DolphinEntity
                        || entity instanceof TurtleEntity
                        || entity instanceof AxolotlEntity
                  )
                  && !this.targets.getValue("Мобов")
               ? false
               : !entity.method_5655() && entity.method_5805() && !(entity instanceof ArmorStandEntity);
         }
      }
   }

   private 

      int temp2 = 255;

      return MathHelper.method_15340(alpha, 0, temp2) << 24 | color & 16777215;
   }

   @EventTarget
   public void onCubeTick(EventRenderModel event) {
      if (this.typeTargetESP.is("Куб")) {
         this.m278();
         LivingEntity v2 = this.f261;
         if (v2 != null) {
            boolean v3 = v2.method_31481() || v2.method_6032() <= 0.0F || v2.method_29504();
            if (v3) {
               if (!this.f265 && this.f262 != null) {
                  this.m277();
               }

               this.f265 = true;
               this.f261 = null;
               this.f259 = 0;
            } else {
               int v4 = v2.field_6235;
               if (v4 > this.f259) {
                  int v5 = 1 + this.f267.nextInt(3);
                  int v6 = this.f267.nextInt(2);
                  float v7 = this.f267.nextBoolean() ? 1.0F : -1.0F;

                  float v8 = this.f267.nextBoolean() ? 1.0F : -1.0F;

                  this.f256 += v7 * 90.0F * (float)v5;
                  this.f257 += v8 * 90.0F * (float)v6;
                  this.f258 = 1.0F;
               }

               this.f259 = v4;
            }
         }
      }
   }

   @EventTarget
   public void onRotate(EventRenderModel e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (target == null) {
            if (this.typeRotation.is("DonutSMP") || this.typeRotation.is("SpookyDuel")) {
               for (wg v14 : wg.values()) {
                  if (this.typeRotation.is(v14.getName())) {
                     v14.getRotation().onRotation(this, null, null, false);
                     break;
                  }
               }
            }
         } else {
            float[] v2 = this.getRanges();
            v2 = new float[]{v2[0], v2[1], v2[0] + v2[1]};
            boolean v3 = qp.shouldAttack(target, false, true, true, -945235212172201806L + 945235212172201456L, v2);
            wg[] v4 = wg.values();
            int v5 = v4.length;

            for (int v6 = 0; v6 < v5; v6++) {

               wg v7 = v4[v6];
               if (this.typeRotation.is(v7.getName())) {
                  v7.getRotation().onRotation(this, target, v2, v3);
                  break;
               }
            }
         }
      }
   }

   private void m265() {
      if (gm.fakePlayer != null && gm.fakePlayer.method_5805() && mc.field_1724.method_5739(gm.fakePlayer) <= this.m266()) {
         target = gm.fakePlayer;
      } else {
         LivingEntity v1 = null;
         double v2 = Double.MAX_VALUE;
         Vec3d v4 = mc.field_1724.method_33571();
         Vec3d v5 = mc.field_1724.method_5828(1.0F).method_1029();

         for (Entity v7 : mc.field_1687.method_18112()) {
            if (v7 instanceof LivingEntity) {
               LivingEntity v8 = (LivingEntity)v7;
               if (this.m267(v8)) {
                  Vec3d v9 = v8.method_73189().method_1031(0.0, (double)v8.method_17682() * 0.5, 0.0);
                  Vec3d v10 = v9.method_1020(v4).method_1029();
                  double v11 = Math.acos(MathHelper.method_15350(v5.method_1026(v10), -1.0, 1.0));
                  if (v11 < v2) {
                     v2 = v11;
                     v1 = v8;
                  }
               }
            }
         }

         target = v1;
      }
   }

   private int m282(int color, float multiplier) {
      int v3 = MathHelper.method_15340((int)((float)(color >> 24 & 255) * multiplier), 0, 255);

      return v3 << 24 | color & 16777215;
   }

   @EventTarget
   private void setCorrection(EventPlayerLook eventMoveInput) {
      if (target != null
         && mc.field_1724 != null
         && mc.field_1687 != null
         && qp.resetSprintTick(target, this.getRanges())
         && target != null
         && !jp.hasMovementRestrictions()
         && this.typeSprint.is("Legit")) {
         eventMoveInput.setForward(0.0F);
         eventMoveInput.setStrafe(0.0F);
      }

      if (target != null && mc.field_1724 != null && mc.field_1687 != null && this.typeMove.is("Свободная")) {
         kr.fixMovement(eventMoveInput, mc.field_1724.method_36454(), mc.field_1773.method_19418().method_19330());
      }

      if (target != null
         && mc.field_1724 != null
         && mc.field_1687 != null
         && this.typeMove.is("Сфокусированная")
         && iq.isFunTime()
         && !mc.field_1724.method_5869()) {
         if (iq.isCopyTime()) {
            Vec3d v2 = kp.getVector2(target);
            float v3 = (float)Math.toDegrees(Math.atan2(-v2.field_1352, v2.field_1350));
            kr.fixMovement(eventMoveInput, mc.field_1724.method_36454(), v3);
         } else {
            Vec3d v7 = target.method_73189().method_1020(mc.field_1724.method_33571()).method_1029();
            float v10 = (float)Math.toDegrees(Math.atan2(-v7.field_1352, v7.field_1350));
            kr.fixMovement(eventMoveInput, mc.field_1724.method_36454(), v10);
         }
      }

      if (target != null && mc.field_1724 != null && mc.field_1687 != null) {

         if (this.typeMove.is("Сфокусированная") && !iq.isFunTime()) {
            if (iq.isCopyTime()) {
               Vec3d v8 = kp.getVector2(target);
               float v11 = (float)Math.toDegrees(Math.atan2(-v8.field_1352, v8.field_1350));
               kr.fixMovement(eventMoveInput, mc.field_1724.method_36454(), v11);
            } else {
               Vec3d v9 = target.method_73189().method_1020(mc.field_1724.method_33571()).method_1029();

               float v12 = (float)Math.toDegrees(Math.atan2(-v9.field_1352, v9.field_1350));
               kr.fixMovement(eventMoveInput, mc.field_1724.method_36454(), v12);
               
            }
         }
      }
   }

   private Vector3f m274(Quaternionf rotation, double relativeX, double relativeY, double relativeZ, Vector3f local) {

      rotation.transform(local);
      local.add((float)relativeX, (float)relativeY, (float)relativeZ);
      return local;
   }

   public void attackEntity() {
      double result = kp.getStrictDistance(target);

      if (!(result >= (double)this.attackRange.getValue().floatValue())) {
         float[] v1 = this.getRanges();
         v1 = new float[]{v1[0], v1[1], v1[0] + v1[1]};
         if (target != null) {
            qp.antiMissesHittingUpdate(target, false, true, false);

            boolean v2 = qp.shouldAttack(target, !this.typeRotation.is("HvH"), true, true, 0L, v1);
            if (v2) {
               Runnable[] v3 = qp.hitShieldBreakTaskForUse(target, true);
               Runnable[] v4 = qp.resetShieldSilentTaskForUse(true);
               Runnable[] v5 = qp.skipSilentSprintingTaskForUse(this.typeSprint.is("Packet") || this.typeSprint.is("Silent"));
               Runnable v6 = () -> {

                  v5[0].run();
                  v4[0].run();
                  v3[0].run();
               };
               Runnable v7 = () -> {
                  v3[1].run();
                  v4[1].run();

                  v5[1].run();
               };
               qp.useEntity(target, v6, v7, Hand.field_5808, false);
               if (mt.DEBUG) {
                  PrintStream v18 = System.out;
                  String v12 = this.typeTargetESP.getValue();
                  v18.println("[TargetLightning] hit, mode=" + v12);
               }

               if (this.typeTargetESP.is("Молнии") && target != null) {
                  try {
                     Vec3d v8 = target.method_73189().method_1031(0.0, (double)target.method_17682() * 0.5, 0.0);
                     mt.strikeAt(v8);
                  } catch (Throwable v15) {
                  }
               }

               for (wg v11 : wg.values()) {
                  if (this.typeRotation.is(v11.getName())) {
                     v11.getRotation().onAttack();
                     break;
                  }
               }

               this.justAttacked = true;
               this.attackFlickAt = System.currentTimeMillis() + 5975466280302265377L + -5975466280302265127L;
               this.count = (this.count + 1) % 2;
               this.f252++;
               if (this.f252 >= this.f253) {
                  this.f252 = 0;
                  this.f253 = ThreadLocalRandom.current().nextInt(15, 19);
                  this.pitchFlickActive = true;
                  this.pitchFlickEndTime = System.currentTimeMillis()
                     + (long)ThreadLocalRandom.current().nextInt(50, 70);
               }
            }
         }
      }
   }

   private void m276(VertexConsumer consumer, Matrix4f matrix, Vector3f first, Vector3f second, Vector3f third, Vector3f fourth, int color) {
      consumer.method_22918(matrix, first.x, first.y, first.z).method_39415(color);

      consumer.method_22918(matrix, second.x, second.y, second.z).method_39415(color);
      consumer.method_22918(matrix, third.x, third.y, third.z).method_39415(color);

      consumer.method_22918(matrix, fourth.x, fourth.y, fourth.z).method_39415(color);
   }

   public static fg get() {
      Module result = uq.get(fg.class);

      int temp1 = v0 * (v0 + 1) % 2;

      if (temp1 != 0) {
         }

      return (fg)result;
   }

   @Override
   public void previewTick(kf c) {
      this.previewTarget = c.dummy();
      if (this.typeTargetESP.is("Молнии") && this.previewTarget != null) {
         boolean result = mt.hasActive();

         if (!result) {
            Vec3d v5 = this.previewTarget.method_73189();
            double temp2 = (double)this.previewTarget.method_17682() * 0.5;

            Vec3d v2 = v5.method_1031(0.0, temp2, 0.0);
            mt.strikeAt(v2);
         }
      }
   }

   private void m271(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u, float v, int color) {

      consumer.method_22918(matrix, x, y, z)
         .method_39415(color)
         .method_22913(u, v)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
   }

   public float[] getRanges() {
      float[] result = new float[]{this.attackRange.getValue(), 0.0F};

      result[1] = this.preRange.getValue();
      return result;
   }

   @Environment(EnvType.CLIENT)
   private static class gg {
      private Vec3d f241;
      private Vector3f f243;
      private int f250;
      private Vec3d f242;
      private Vec3d f245;
      private float f247;
      private final Vector3f f246;
      private final double f248;
      private final int f249;
      private Vector3f f244;

      private gg(Vec3d center, Vector3f offset, Vec3d velocity, Vector3f axis, float angularSpeed, double groundY, int maxAge) {
         this.f241 = center;
         this.f242 = center;
         this.f243 = new Vector3f(offset);
         this.f244 = offset;
         this.f245 = velocity;
         this.f246 = axis;
         this.f247 = angularSpeed;
         this.f248 = groundY;
         this.f249 = maxAge;
      }

      
   }
}
