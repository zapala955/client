package ru.white.module.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderSetup.Builder;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.utils.render.mt;
import ru.white.utils.render.gl.Jf;
import ru.white.utils.render.gl.Kf;
import ru.white.utils.render.gl.Lf;
import ru.white.event.Event;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Target Esp",
   desc = "Отображения таргета",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class TargetEsp extends Module implements jf {
   public SliderSetting speed;
   public tf lightningColor;
   public SliderSetting ringSpeed;
   public SliderSetting sizeGlowOFF;
   public BooleanSetting mandalaBeams;
   public Animation alpha_2;
   public SliderSetting lightningBranches;
   public SliderSetting gyroSize;
   public BooleanSetting gyroCore;
   private float f789;
   public SliderSetting speed_2;
   private static final RenderLayer RING_LINE_LAYER = RenderLayer.method_75940(
      "ring_esp_line",
      RenderSetup.method_75927(yk.RING_LINE_PIPELINE).method_75929(16384).method_75938()
   );
   private static final RenderLayer RING_FILL_LAYER = RenderLayer.method_75940(
      "ring_esp_fill",
      RenderSetup.method_75927(yk.RING_FILL_PIPELINE).method_75929(65536).method_75938()
   );
   private final lf f786;
   private static final RenderPipeline RING_FILL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "ring_esp_fill"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27382)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );
   public SliderSetting lightningWidth;
   public SliderSetting bublikSize;
   public SliderSetting size_2;
   public ModeSetting type;
   public ModeSetting typeGhost;
   public static final Function<Identifier, RenderLayer> ROMB_ESP = Util.method_34866(texture -> {
      Builder result = RenderSetup.method_75927(yk.ROMB_ESP_PIPELINE).method_75934("Sampler0", texture);

      RenderSetup v1 = result.method_75937().method_75929(1536).method_75938();
      return RenderLayer.method_75940("wtex", v1);
   });
   public SliderSetting mandalaSpeed;
   private long f790;
   public static final RenderPipeline ROMB_ESP_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_60125})
         .withLocation("pipeline/wtex")
         .withVertexShader("core/position_tex_color")
         .withFragmentShader("core/position_tex_color")
         .withSampler("Sampler0")
         .withBlend(BlendFunction.LIGHTNING)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withCull(false)
         .withVertexFormat(VertexFormats.field_1575, DrawMode.field_27382)
         .build()
   );
   public LivingEntity target;
   public SliderSetting bublikSpeed;
   public ModeSetting typeImages;
   public sf previewButton = lf.button(this);
   public SliderSetting mandalaSize;
   public SliderSetting sizeGlow;
   public Animation alpha;
   private LivingEntity f788;
   public SliderSetting gyroSpeed;
   private final BufferAllocator f787;
   public SliderSetting mandalaHeight;
   private static final RenderPipeline RING_LINE_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "ring_esp_line"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_29344)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.LIGHTNING)
         .build()
   );

   @Override
   public void previewStop() {
      this.f788 = null;
      this.target = null;
      mt.clear();
   }

   @Override
   public void previewTick(kf ctx) {
      this.f788 = ctx.dummy();
      if (this.type.is("Молнии") && this.f788 != null && !mt.hasActive()) {
         Vec3d result = this.f788.method_73189();

         Vec3d v2 = result.method_1031(0.0, (double)this.f788.method_17682() * 0.5, 0.0);
         mt.strikeAt(v2);
      }
   }

   @Override
   public void previewStart(kf ctx) {

      this.f788 = ctx.dummy();
      if (this.type.is("Молнии") && this.f788 != null) {
         Vec3d v2 = this.f788.method_73189().method_1031(0.0, (double)this.f788.method_17682() * 0.5, 0.0);
         mt.strikeAt(v2);
      }
   }

   @EventTarget
   public void onAttack(Event event) {
      if (this.type.is("Молнии")) {

         if (event.getTarget() != null) {
            Vec3d v5 = event.getTarget().method_73189();

            Vec3d v2 = v5.method_1031(0.0, (double)event.getTarget().method_17682() * 0.5, 0.0);
            mt.strikeAt(v2);
         }
      }
   }

   @EventTarget
   public void onRender(EventTrace e) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
      //
      // Bytecode:
      // 0000: aload 0
      // 0001: getfield ru/white/module/impl/render/yk.alpha Lru/white/utils/animation/Animation;
      // 0004: invokevirtual ru/white/utils/animation/Animation.update ()Z
      // 0007: pop
      // 0008: aload 0
      // 0009: getfield ru/white/module/impl/render/yk.f788 Lnet/minecraft/entity/LivingEntity;
      // 000c: ifnull 0016
      // 000f: aload 0
      // 0010: getfield ru/white/module/impl/render/yk.f788 Lnet/minecraft/entity/LivingEntity;
      // 0013: goto 0019
      // 0016: getstatic ru/white/module/impl/combat/fg.target Lnet/minecraft/entity/LivingEntity;
      // 0019: astore 2
      // 001a: aload 2
      // 001b: ifnonnull 0036
      // 001e: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 0021: getfield net/minecraft/client/MinecraftClient.field_1692 Lnet/minecraft/entity/Entity;
      // 0024: astore 4
      // 0026: aload 4
      // 0028: instanceof net/minecraft/entity/LivingEntity
      // 002b: ifeq 0036
      // 002e: aload 4
      // 0030: checkcast net/minecraft/entity/LivingEntity
      // 0033: astore 3
      // 0034: aload 3
      // 0035: astore 2
      // 0036: aload 2
      // 0037: ifnull 003f
      // 003a: aload 0
      // 003b: aload 2
      // 003c: putfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 003f: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 0042: getfield net/minecraft/client/MinecraftClient.field_1687 Lnet/minecraft/client/world/ClientWorld;
      // 0045: ifnull 0051
      // 0048: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 004b: getfield net/minecraft/client/MinecraftClient.field_1724 Lnet/minecraft/client/network/ClientPlayerEntity;
      // 004e: ifnonnull 0052
      // 0051: return
      // 0052: aload 0
      // 0053: getfield ru/white/module/impl/render/yk.alpha Lru/white/utils/animation/Animation;
      // 0056: aload 2
      // 0057: ifnull 005e
      // 005a: dconst_1
      // 005b: goto 005f
      // 005e: dconst_0
      // 005f: ldc2_w 0.15000000596046448
      // 0062: getstatic ru/white/utils/animation/ap.SINE_OUT Lru/white/utils/animation/zo;
      // 0065: invokevirtual ru/white/utils/animation/Animation.run (DDLru/white/utils/animation/zo;)Lru/white/utils/animation/Animation;
      // 0068: pop
      // 0069: aload 0
      // 006a: getfield ru/white/module/impl/render/yk.alpha Lru/white/utils/animation/Animation;
      // 006d: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 0070: fstore 3
      // 0071: aload 0
      // 0072: getfield ru/white/module/impl/render/yk.f787 Lnet/minecraft/client/util/BufferAllocator;
      // 0075: invokestatic net/minecraft/client/render/VertexConsumerProvider.method_22991 (Lnet/minecraft/client/util/BufferAllocator;)Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;
      // 0078: astore 4
      // 007a: aload 0
      // 007b: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 007e: ldc "Молнии"
      // 0080: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 0083: ifeq 0113
      // 0086: invokestatic ru/white/module/impl/combat/fg.get ()Lru/white/module/impl/combat/fg;
      // 0089: invokevirtual ru/white/module/impl/combat/fg.isEnabled ()Z
      // 008c: ifeq 009d
      // 008f: invokestatic ru/white/module/impl/combat/fg.get ()Lru/white/module/impl/combat/fg;
      // 0092: getfield ru/white/module/impl/combat/fg.typeTargetESP Lq2/z/ModeSetting;
      // 0095: ldc "Молнии"
      // 0097: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 009a: ifne 0113
      // 009d: aload 0
      // 009e: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 00a1: ifnull 00ConfigManager
      // 00a4: aload 0
      // 00a5: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 00a8: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 00ab: ldc_w 1849651154
      // 00ae: ldc_w -1849651144
      // 00b1: iadd
      // 00b2: if_icmpne 00ConfigManager
      // 00b5: invokestatic ru/white/utils/render/mt.hasActive ()Z
      // 00b8: ifne 00ConfigManager
      // 00DraggableWidget: aload 0
      // 00bc: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 00bf: invokevirtual net/minecraft/entity/LivingEntity.method_73189 ()Lnet/minecraft/util/math/Vec3d;
      // 00c2: dconst_0
      // 00c3: aload 0
      // 00c4: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 00c7: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 00ConfigFile: f2d
      // 00cb: ldc2_w 0.5
      // 00ce: dmul
      // 00cf: dconst_0
      // 00d0: invokevirtual net/minecraft/util/math/Vec3d.method_1031 (DDD)Lnet/minecraft/util/math/Vec3d;
      // 00d3: astore 5
      // 00d5: aload 5
      // 00d7: invokestatic ru/white/utils/render/mt.strikeAt (Lnet/minecraft/util/math/Vec3d;)V
      // 00ConfigManager: invokestatic ru/white/utils/render/mt.hasActive ()Z
      // 00dd: ifeq 0113
      // 00e0: aload 1
      // 00e1: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 00e4: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 00e7: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 00ea: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 00ed: aload 4
      // 00ef: aload 0
      // 00f0: getfield ru/white/module/impl/render/yk.lightningColor Lq2/z/tf;
      // 00f3: invokevirtual q2/z/tf.getValue ()Ljava/lang/Integer;
      // 00f6: invokevirtual java/lang/Integer.intValue ()I
      // 00f9: aload 0
      // 00fa: getfield ru/white/module/impl/render/yk.lightningWidth Lq2/z/SliderSetting;
      // 00fd: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0100: invokevirtual java/lang/Float.floatValue ()F
      // 0103: aload 0
      // 0104: getfield ru/white/module/impl/render/yk.lightningBranches Lq2/z/SliderSetting;
      // 0107: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 010a: invokevirtual java/lang/Float.floatValue ()F
      // 010d: invokestatic java/lang/Math.round (F)I
      // 0110: invokestatic ru/white/utils/render/mt.render (Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;IFI)V
      // 0113: fload 3
      // 0114: ldc_w 0.001
      // 0117: fcmpl
      // 0118: ifle 0600
      // 011b: aload 0
      // 011c: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 011f: ifnull 0600
      // 0122: aload 0
      // 0123: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 0126: ldc "Призраки"
      // 0128: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 012b: ifeq 0600
      // 012e: aload 0
      // 012f: getfield ru/white/module/impl/render/yk.typeGhost Lq2/z/ModeSetting;
      // 0132: ldc_w "2"
      // 0135: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 0138: ifeq 0600
      // 013b: invokestatic java/lang/System.currentTimeMillis ()J
      // 013e: lstore 5
      // 0140: aload 0
      // 0141: getfield ru/white/module/impl/render/yk.f790 J
      // 0144: lconst_0
      // 0145: lcmp
      // 0146: ifne 014f
      // 0149: aload 0
      // 014a: lload 5
      // 014c: putfield ru/white/module/impl/render/yk.f790 J
      // 014f: lload 5
      // 0151: aload 0
      // 0152: getfield ru/white/module/impl/render/yk.f790 J
      // 0155: lsub
      // 0156: lstore 7
      // 0158: lload 7
      // 015a: lconst_0
      // 015b: lcmp
      // 015c: ifle 017f
      // 015f: aload 0
      // 0160: dup
      // 0161: getfield ru/white/module/impl/render/yk.f789 F
      // 0164: ldc2_w -4790930769101560745
      // 0167: ldc2_w 4790930769101560750
      // 016a: ladd
      // 016b: lload 7
      // 016d: lmul
      // 016e: l2f
      // 016f: aload 0
      // 0170: getfield ru/white/module/impl/render/yk.speed Lq2/z/SliderSetting;
      // 0173: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0176: invokevirtual java/lang/Float.longValue ()J
      // 0179: l2f
      // 017a: fdiv
      // 017b: fadd
      // 017c: putfield ru/white/module/impl/render/yk.f789 F
      // 017f: aload 0
      // 0180: lload 5
      // 0182: putfield ru/white/module/impl/render/yk.f790 J
      // 0185: aload 1
      // 0186: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 0189: astore 9
      // 018b: aload 0
      // 018c: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 018f: aload 1
      // 0190: invokevirtual x9/sb.getTickDelta ()F
      // 0193: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 0196: astore 10
      // 0198: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 019b: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 019e: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 01a1: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 01a4: astore 11
      // 01a6: aload 10
      // 01a8: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 01ab: aload 11
      // 01ad: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 01b0: dsub
      // 01b1: dstore 12
      // 01b3: aload 10
      // 01b5: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 01b8: aload 11
      // 01ba: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 01bd: dsub
      // 01be: dstore 14
      // 01c0: aload 10
      // 01c2: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 01c5: aload 11
      // 01c7: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 01ConfigFile: dsub
      // 01cb: dstore 16
      // 01cd: aload 0
      // 01ce: getfield ru/white/module/impl/render/yk.alpha Lru/white/utils/animation/Animation;
      // 01d1: invokevirtual ru/white/utils/animation/Animation.getValue ()D
      // 01d4: d2f
      // 01d5: fstore 3
      // 01d6: aload 0
      // 01d7: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 01ConfigManager: invokevirtual ru/white/utils/animation/Animation.update ()Z
      // 01dd: pop
      // 01de: aload 0
      // 01df: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 01e2: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 01e5: istore 18
      // 01e7: iload 18
      // 01e9: i2d
      // 01ea: ldc2_w 0.15707963267948966
      // 01ed: dmul
      // 01ee: invokestatic java/lang/Math.sin (D)D
      // 01f1: d2f
      // 01f2: fstore 19
      // 01f4: aload 0
      // 01f5: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 01f8: fload 19
      // 01fa: f2d
      // 01fb: ldc2_w 0.10000000149011612
      // 01fe: getstatic ru/white/utils/animation/ap.SINE_OUT Lru/white/utils/animation/zo;
      // 0201: invokevirtual ru/white/utils/animation/Animation.run (DDLru/white/utils/animation/zo;)Lru/white/utils/animation/Animation;
      // 0204: pop
      // 0205: aload 0
      // 0206: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 0209: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 020c: fstore 20
      // 020e: bipush 1
      // 020f: invokestatic q4/z/up.fade (I)I
      // 0212: istore 21
      // 0214: ldc_w 1984079468
      // 0217: ldc_w -1984079268
      // 021a: iadd
      // 021b: ldc_w -208639324
      // 021e: ldc_w 208639394
      // 0221: iadd
      // 0222: ldc_w 1865317044
      // 0225: ldc_w -1865316974
      // 0228: iadd
      // 0229: ldc_w 255.0
      // 022c: fload 3
      // 022d: fmul
      // 022e: f2i
      // 022f: invokestatic q4/z/up.getColor (IIII)I
      // 0232: istore 22
      // 0234: iload 21
      // 0236: fload 3
      // 0237: invokestatic q4/z/up.multAlpha (IF)I
      // 023a: iload 22
      // 023c: fload 20
      // 023e: invokestatic q4/z/up.overCol (IIF)I
      // 0241: istore 23
      // 0243: bipush 3
      // 0244: istore 24
      // 0246: ldc_w 702288362
      // 0249: ldc_w -702288350
      // 024c: iadd
      // 024d: istore 25
      // 024f: bipush 3
      // 0250: iload 24
      // 0252: imul
      // 0253: istore 26
      // 0255: aload 9
      // 0257: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 025a: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 025d: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 0260: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 0263: astore 27
      // 0265: bipush 0
      // 0266: istore 28
      // 0268: iload 28
      // 026a: iload 26
      // 026c: if_icmpge 05fb
      // 026f: bipush 0
      // 0270: istore 29
      // 0272: iload 29
      // 0274: iload 25
      // 0276: if_icmpge 05f1
      // 0279: aload 0
      // 027a: getfield ru/white/module/impl/render/yk.f789 F
      // 027d: iload 29
      // 027f: i2f
      // 0280: ldc_w 0.1
      // 0283: fmul
      // 0284: fadd
      // 0285: fstore 30
      // 0287: ldc_w 0.6
      // 028a: fstore 31
      // 028c: ldc_w 0.4
      // 028f: fstore 32
      // 0291: iload 28
      // 0293: i2d
      // 0294: ldc2_w 2.0
      // 0297: invokestatic java/lang/Math.pow (DD)D
      // 029a: d2i
      // 029b: istore 33
      // 029d: aload 9
      // 029f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 02a2: dload 12
      // 02a4: fload 31
      // 02a6: f2d
      // 02a7: fload 30
      // 02a9: iload 33
      // 02ab: i2f
      // 02EventMotion: fadd
      // 02ad: f2d
      // 02ae: invokestatic java/lang/Math.sin (D)D
      // 02b1: dmul
      // 02b2: dadd
      // 02b3: dstore 34
      // 02b5: dload 14
      // 02b7: fload 32
      // 02b9: f2d
      // 02ba: dadd
      // 02DraggableWidget: ldc2_w 0.30000001192092896
      // 02be: aload 0
      // 02bf: getfield ru/white/module/impl/render/yk.f789 F
      // 02c2: iload 29
      // 02c4: i2f
      // 02c5: ldc_w 0.2
      // 02c8: fmul
      // 02c9: fadd
      // 02ConfigFile: f2d
      // 02cb: invokestatic java/lang/Math.sin (D)D
      // 02ce: dmul
      // 02cf: dadd
      // 02d0: ldc_w 0.2
      // 02d3: iload 28
      // 02d5: i2f
      // 02d6: fmul
      // 02d7: f2d
      // 02d8: dadd
      // 02d9: dstore 36
      // 02db: dload 16
      // 02dd: fload 31
      // 02df: f2d
      // 02e0: fload 30
      // 02e2: iload 33
      // 02e4: i2f
      // 02e5: fsub
      // 02e6: f2d
      // 02e7: invokestatic java/lang/Math.cos (D)D
      // 02ea: dmul
      // 02eb: dadd
      // 02EventAttack: dstore 38
      // 02ee: aload 9
      // 02f0: dload 34
      // 02f2: dload 36
      // 02f4: dload 38
      // 02f6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 02f9: ldc_w 0.006
      // 02EventMove: iload 29
      // 02fe: i2f
      // 02Category: ldc_w 2000.0
      // 0302: fdiv
      // 0303: fadd
      // 0304: fload 3
      // 0305: fmul
      // 0306: fstore 40
      // 0308: aload 9
      // 030a: fload 40
      // 030c: fload 40
      // 030e: fload 40
      // 0310: ldc_w -671905165
      // 0313: istore 59
      // 0315: iload 59
      // 0317: dup
      // 0318: bipush 1
      // 0319: iadd
      // 031a: imul
      // 031b: bipush 2
      // 031c: irem
      // 031d: ifeq 0322
      // 0320: aconst_null
      // 0321: pop
      // 0322: nop
      // 0323: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 0326: aload 9
      // 0328: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 032b: aload 27
      // 032d: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 0330: fneg
      // 0331: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0336: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0339: aload 9
      // 033b: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 033e: aload 27
      // 0340: invokevirtual net/minecraft/client/render/Camera.method_19329 ()F
      // 0343: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0348: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 034b: aload 9
      // 034d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 0350: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 0353: astore 41
      // 0355: aload 4
      // 0357: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 035a: ldc_w "client"
      // 035d: ldc_w "vi8ZGgpNiYjP1hIH4HTqsjHudCj/YvtDUYdwk48R3FI="
      // 0360: ldc_w 1984776343
      // 0363: ldc_w 1575944658
      // 0366: iadd
      // 0367: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 036a: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 036d: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 0372: checkcast net/minecraft/client/render/RenderLayer
      // 0375: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 0378: astore 42
      // 037a: iload 23
      // 037c: istore 43
      // 037e: ldc_w -1089394239
      // 0381: ldc_w 1089394219
      // 0384: iadd
      // 0385: istore 44
      // 0387: ldc_w 1139187357
      // 038a: ldc_w -1139187322
      // 038d: iadd
      // 038e: istore 45
      // 0390: aload 42
      // 0392: aload 41
      // 0394: iload 44
      // 0396: i2f
      // 0397: iload 44
      // 0399: iload 45
      // 039b: iadd
      // 039c: i2f
      // 039d: fconst_0
      // 039e: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 03a3: iload 23
      // 03a5: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 03aa: fconst_0
      // 03ab: fconst_1
      // 03EventMotion: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 03b1: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 03b4: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 03b9: ldc_w 312633110
      // 03bc: ldc_w -296904230
      // 03bf: iadd
      // 03c0: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 03c5: fconst_0
      // 03c6: fconst_0
      // 03c7: fconst_1
      // 03c8: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 03cd: pop
      // 03ce: aload 42
      // 03d0: aload 41
      // 03d2: iload 44
      // 03d4: iload 45
      // 03d6: iadd
      // 03d7: i2f
      // 03d8: iload 44
      // 03ConfigManager: iload 45
      // 03EventKey: iadd
      // 03dd: i2f
      // 03de: fconst_0
      // 03df: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 03e4: iload 23
      // 03e6: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 03eb: fconst_1
      // 03EventAttack: fconst_1
      // 03ed: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 03f2: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 03f5: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 03fa: ldc_w -1961219534
      // 03fd: ldc_w 1976948414
      // 0400: iadd
      // 0401: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0406: fconst_0
      // 0407: fconst_0
      // 0408: fconst_1
      // 0409: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 040e: pop
      // 040f: aload 42
      // 0411: aload 41
      // 0413: iload 44
      // 0415: iload 45
      // 0417: iadd
      // 0418: i2f
      // 0419: iload 44
      // 041b: i2f
      // 041c: fconst_0
      // 041d: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 0422: iload 23
      // 0424: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0429: fconst_1
      // 042a: fconst_0
      // 042b: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 0430: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 0433: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0438: ldc_w -1781036245
      // 043b: ldc_w 1796765125
      // 043e: iadd
      // 043f: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0444: fconst_0
      // 0445: fconst_0
      // 0446: fconst_1
      // 0447: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 044c: pop
      // 044d: aload 42
      // 044f: aload 41
      // 0451: iload 44
      // 0453: i2f
      // 0454: iload 44
      // 0456: i2f
      // 0457: fconst_0
      // 0458: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 045d: iload 23
      // 045f: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0464: fconst_0
      // 0465: fconst_0
      // 0466: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 046b: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 046e: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0473: ldc_w -909984754
      // 0476: ldc_w 925713634
      // 0479: iadd
      // 047a: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 047f: fconst_0
      // 0480: fconst_0
      // 0481: fconst_1
      // 0482: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 0487: pop
      // 0488: ldc_w -20.0
      // 048b: ldc_w 20.0
      // 048e: aload 0
      // 048f: getfield ru/white/module/impl/render/yk.sizeGlowOFF Lq2/z/SliderSetting;
      // 0492: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0495: invokevirtual java/lang/Float.floatValue ()F
      // 0498: fmul
      // 0499: fsub
      // 049a: f2i
      // 049b: istore 44
      // 049d: ldc_w 35.0
      // 04a0: ldc_w 40.0
      // 04a3: aload 0
      // 04a4: getfield ru/white/module/impl/render/yk.sizeGlowOFF Lq2/z/SliderSetting;
      // 04a7: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 04aa: invokevirtual java/lang/Float.floatValue ()F
      // 04ad: fmul
      // 04ae: fadd
      // 04af: f2i
      // 04b0: istore 45
      // 04b2: aload 42
      // 04b4: aload 41
      // 04b6: iload 44
      // 04b8: i2f
      // 04b9: iload 44
      // 04DraggableWidget: iload 45
      // 04bd: iadd
      // 04be: i2f
      // 04bf: fconst_0
      // 04c0: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 04c5: iload 23
      // 04c7: fload 3
      // 04c8: aload 0
      // 04c9: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 04EventPacket: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 04cf: invokevirtual java/lang/Float.floatValue ()F
      // 04d2: fmul
      // 04d3: invokestatic q4/z/up.replAlpha (IF)I
      // 04d6: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 04db: fconst_0
      // 04EventKey: fconst_1
      // 04dd: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 04e2: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 04e5: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 04ea: ldc_w -1814490985
      // 04ed: ldc_w 1830219865
      // 04f0: iadd
      // 04f1: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 04f6: fconst_0
      // 04f7: fconst_0
      // 04f8: fconst_1
      // 04f9: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 04fe: pop
      // 04Category: aload 42
      // 0501: aload 41
      // 0503: iload 44
      // 0505: iload 45
      // 0507: iadd
      // 0508: i2f
      // 0509: iload 44
      // 050b: iload 45
      // 050d: iadd
      // 050e: i2f
      // 050f: fconst_0
      // 0510: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 0515: iload 23
      // 0517: fload 3
      // 0518: aload 0
      // 0519: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 051c: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 051f: invokevirtual java/lang/Float.floatValue ()F
      // 0522: fmul
      // 0523: invokestatic q4/z/up.replAlpha (IF)I
      // 0526: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 052b: fconst_1
      // 052c: fconst_1
      // 052d: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 0532: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 0535: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 053a: ldc_w -2057630173
      // 053d: ldc_w 2073359053
      // 0540: iadd
      // 0541: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0546: fconst_0
      // 0547: fconst_0
      // 0548: fconst_1
      // 0549: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 054e: pop
      // 054f: aload 42
      // 0551: aload 41
      // 0553: iload 44
      // 0555: iload 45
      // 0557: iadd
      // 0558: i2f
      // 0559: iload 44
      // 055b: i2f
      // 055c: fconst_0
      // 055d: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 0562: iload 23
      // 0564: fload 3
      // 0565: aload 0
      // 0566: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 0569: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 056c: invokevirtual java/lang/Float.floatValue ()F
      // 056f: fmul
      // 0570: invokestatic q4/z/up.replAlpha (IF)I
      // 0573: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0578: fconst_1
      // 0579: fconst_0
      // 057a: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 057f: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 0582: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0587: ldc_w 799561122
      // 058a: ldc_w -783832242
      // 058d: iadd
      // 058e: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0593: fconst_0
      // 0594: fconst_0
      // 0595: fconst_1
      // 0596: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 059b: pop
      // 059c: aload 42
      // 059e: aload 41
      // 05a0: iload 44
      // 05a2: i2f
      // 05a3: iload 44
      // 05a5: i2f
      // 05a6: fconst_0
      // 05a7: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 05EventMotion: iload 23
      // 05ae: fload 3
      // 05af: aload 0
      // 05b0: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 05b3: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 05b6: invokevirtual java/lang/Float.floatValue ()F
      // 05b9: fmul
      // 05ba: invokestatic q4/z/up.replAlpha (IF)I
      // 05bd: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 05c2: fconst_0
      // 05c3: fconst_0
      // 05c4: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 05c9: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 05EventPacket: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 05d1: ldc_w 915031228
      // 05d4: ldc_w -899302348
      // 05d7: iadd
      // 05d8: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 05dd: fconst_0
      // 05de: fconst_0
      // 05df: fconst_1
      // 05e0: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 05e5: pop
      // 05e6: aload 9
      // 05e8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 05eb: iinc 29 1
      // 05ee: goto 0272
      // 05f1: iload 28
      // 05f3: iload 24
      // 05f5: iadd
      // 05f6: istore 28
      // 05f8: goto 0268
      // 05fb: aload 9
      // 05fd: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 0600: fload 3
      // 0601: ldc_w 0.001
      // 0604: fcmpl
      // 0605: ifle 0ade
      // 0608: aload 0
      // 0609: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 060c: ifnull 0ade
      // 060f: aload 0
      // 0610: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 0613: ldc "Призраки"
      // 0615: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 0618: ifeq 0ade
      // 061b: aload 0
      // 061c: getfield ru/white/module/impl/render/yk.typeGhost Lq2/z/ModeSetting;
      // 061f: ldc_w "4"
      // 0622: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 0625: ifeq 0ade
      // 0628: invokestatic java/lang/System.currentTimeMillis ()J
      // 062b: lstore 5
      // 062d: aload 0
      // 062e: getfield ru/white/module/impl/render/yk.f790 J
      // 0631: lconst_0
      // 0632: lcmp
      // 0633: ifne 063c
      // 0636: aload 0
      // 0637: lload 5
      // 0639: putfield ru/white/module/impl/render/yk.f790 J
      // 063c: lload 5
      // 063e: aload 0
      // 063f: getfield ru/white/module/impl/render/yk.f790 J
      // 0642: lsub
      // 0643: lstore 7
      // 0645: lload 7
      // 0647: lconst_0
      // 0648: lcmp
      // 0649: ifle 066c
      // 064c: aload 0
      // 064d: dup
      // 064e: getfield ru/white/module/impl/render/yk.f789 F
      // 0651: ldc2_w -9161306280316624715
      // 0654: ldc2_w 9161306280316624720
      // 0657: ladd
      // 0658: lload 7
      // 065a: lmul
      // 065b: l2f
      // 065c: aload 0
      // 065d: getfield ru/white/module/impl/render/yk.speed Lq2/z/SliderSetting;
      // 0660: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0663: invokevirtual java/lang/Float.longValue ()J
      // 0666: l2f
      // 0667: fdiv
      // 0668: fadd
      // 0669: putfield ru/white/module/impl/render/yk.f789 F
      // 066c: aload 0
      // 066d: lload 5
      // 066f: putfield ru/white/module/impl/render/yk.f790 J
      // 0672: aload 1
      // 0673: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 0676: astore 9
      // 0678: aload 0
      // 0679: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 067c: aload 1
      // 067d: invokevirtual x9/sb.getTickDelta ()F
      // 0680: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 0683: astore 10
      // 0685: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 0688: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 068b: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 068e: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 0691: astore 11
      // 0693: aload 10
      // 0695: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 0698: aload 11
      // 069a: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 069d: dsub
      // 069e: dstore 12
      // 06a0: aload 10
      // 06a2: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 06a5: aload 11
      // 06a7: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 06aa: dsub
      // 06ab: dstore 14
      // 06ad: aload 10
      // 06af: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 06b2: aload 11
      // 06b4: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 06b7: dsub
      // 06b8: dstore 16
      // 06ba: aload 0
      // 06DraggableWidget: getfield ru/white/module/impl/render/yk.alpha Lru/white/utils/animation/Animation;
      // 06be: invokevirtual ru/white/utils/animation/Animation.getValue ()D
      // 06c1: d2f
      // 06c2: fstore 3
      // 06c3: aload 0
      // 06c4: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 06c7: invokevirtual ru/white/utils/animation/Animation.update ()Z
      // 06ConfigFile: pop
      // 06cb: aload 0
      // 06EventPacket: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 06cf: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 06d2: istore 18
      // 06d4: iload 18
      // 06d6: i2d
      // 06d7: ldc2_w 0.15707963267948966
      // 06ConfigManager: dmul
      // 06db: invokestatic java/lang/Math.sin (D)D
      // 06de: d2f
      // 06df: fstore 19
      // 06e1: aload 0
      // 06e2: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 06e5: fload 19
      // 06e7: f2d
      // 06e8: ldc2_w 0.10000000149011612
      // 06eb: getstatic ru/white/utils/animation/ap.SINE_OUT Lru/white/utils/animation/zo;
      // 06ee: invokevirtual ru/white/utils/animation/Animation.run (DDLru/white/utils/animation/zo;)Lru/white/utils/animation/Animation;
      // 06f1: pop
      // 06f2: aload 0
      // 06f3: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 06f6: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 06f9: fstore 20
      // 06fb: bipush 1
      // 06EventMove: invokestatic q4/z/up.fade (I)I
      // 06Category: istore 21
      // 0701: ldc_w 355614994
      // 0704: ldc_w -355614794
      // 0707: iadd
      // 0708: ldc_w 1935658744
      // 070b: ldc_w -1935658674
      // 070e: iadd
      // 070f: ldc_w 723578282
      // 0712: ldc_w -723578212
      // 0715: iadd
      // 0716: ldc_w 255.0
      // 0719: fload 3
      // 071a: fmul
      // 071b: f2i
      // 071c: invokestatic q4/z/up.getColor (IIII)I
      // 071f: istore 22
      // 0721: iload 21
      // 0723: fload 3
      // 0724: invokestatic q4/z/up.multAlpha (IF)I
      // 0727: iload 22
      // 0729: fload 20
      // 072b: invokestatic q4/z/up.overCol (IIF)I
      // 072e: istore 23
      // 0730: bipush 3
      // 0731: istore 24
      // 0733: ldc_w -489569893
      // 0736: ldc_w 489569917
      // 0739: iadd
      // 073a: istore 25
      // 073c: bipush 3
      // 073d: iload 24
      // 073f: imul
      // 0740: istore 26
      // 0742: aload 9
      // 0744: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 0747: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 074a: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 074d: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 0750: astore 27
      // 0752: bipush 0
      // 0753: istore 28
      // 0755: iload 28
      // 0757: iload 26
      // 0759: if_icmpge 0ad9
      // 075c: bipush 0
      // 075d: istore 29
      // 075f: iload 29
      // 0761: iload 25
      // 0763: if_icmpge 0acf
      // 0766: aload 0
      // 0767: getfield ru/white/module/impl/render/yk.f789 F
      // 076a: iload 29
      // 076c: i2f
      // 076d: ldc_w 0.05
      // 0770: fmul
      // 0771: fadd
      // 0772: fstore 30
      // 0774: aload 0
      // 0775: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0778: invokevirtual net/minecraft/entity/LivingEntity.method_17681 ()F
      // 077b: fstore 31
      // 077d: ldc_w 0.45
      // 0780: fstore 32
      // 0782: iload 28
      // 0784: i2d
      // 0785: ldc2_w 2.0
      // 0788: invokestatic java/lang/Math.pow (DD)D
      // 078b: d2i
      // 078c: istore 33
      // 078e: aload 9
      // 0790: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 0793: dload 12
      // 0795: fload 31
      // 0797: f2d
      // 0798: fload 30
      // 079a: iload 33
      // 079c: i2f
      // 079d: fadd
      // 079e: f2d
      // 079f: invokestatic java/lang/Math.sin (D)D
      // 07a2: dmul
      // 07a3: dadd
      // 07a4: dstore 34
      // 07a6: dload 14
      // 07a8: fload 32
      // 07aa: f2d
      // 07ab: dadd
      // 07EventMotion: ldc2_w 0.10000000149011612
      // 07af: aload 0
      // 07b0: getfield ru/white/module/impl/render/yk.f789 F
      // 07b3: iload 29
      // 07b5: i2f
      // 07b6: ldc_w 0.1
      // 07b9: fmul
      // 07ba: fadd
      // 07DraggableWidget: f2d
      // 07bc: invokestatic java/lang/Math.sin (D)D
      // 07bf: dmul
      // 07c0: dadd
      // 07c1: ldc_w 0.2
      // 07c4: iload 28
      // 07c6: i2f
      // 07c7: fmul
      // 07c8: f2d
      // 07c9: dadd
      // 07ConfigFile: dstore 36
      // 07EventPacket: dload 16
      // 07ce: fload 31
      // 07d0: f2d
      // 07d1: fload 30
      // 07d3: iload 33
      // 07d5: i2f
      // 07d6: fsub
      // 07d7: f2d
      // 07d8: invokestatic java/lang/Math.cos (D)D
      // 07db: dmul
      // 07EventKey: dadd
      // 07dd: dstore 38
      // 07df: aload 9
      // 07e1: dload 34
      // 07e3: dload 36
      // 07e5: dload 38
      // 07e7: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 07ea: ldc_w 0.009
      // 07ed: iload 29
      // 07ef: i2f
      // 07f0: ldc_w 2000.0
      // 07f3: fdiv
      // 07f4: fadd
      // 07f5: fload 3
      // 07f6: fmul
      // 07f7: fstore 40
      // 07f9: aload 9
      // 07fb: fload 40
      // 07fd: fload 40
      // 07Category: fload 40
      // 0801: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 0804: aload 9
      // 0806: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0809: aload 27
      // 080b: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 080e: fneg
      // 080f: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0814: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0817: aload 9
      // 0819: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 081c: aload 27
      // 081e: invokevirtual net/minecraft/client/render/Camera.method_19329 ()F
      // 0821: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0826: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0829: aload 9
      // 082b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 082e: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 0831: astore 41
      // 0833: aload 4
      // 0835: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 0838: ldc_w "client"
      // 083b: ldc_w "Tm/TNPoNQ6Y/ltgpEDQgnMGuvgYPIjFtoce6vX9RFnw="
      // 083e: ldc_w -1442016692
      // 0841: ldc_w 1357477965
      // 0844: iadd
      // 0845: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 0848: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 084b: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 0850: checkcast net/minecraft/client/render/RenderLayer
      // 0853: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 0856: astore 42
      // 0858: iload 23
      // 085a: istore 43
      // 085c: ldc_w -1073931945
      // 085f: ldc_w 1073931933
      // 0862: iadd
      // 0863: istore 44
      // 0865: ldc_w 1630877897
      // 0868: ldc_w -1630877881
      // 086b: iadd
      // 086c: istore 45
      // 086e: aload 42
      // 0870: aload 41
      // 0872: iload 44
      // 0874: i2f
      // 0875: iload 44
      // 0877: iload 45
      // 0879: iadd
      // 087a: i2f
      // 087b: fconst_0
      // 087c: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 0881: iload 23
      // 0883: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0888: fconst_0
      // 0889: fconst_1
      // 088a: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 088f: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 0892: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0897: ldc_w -335910348
      // 089a: ldc_w 351639228
      // 089d: iadd
      // 089e: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 08a3: fconst_0
      // 08a4: fconst_0
      // 08a5: fconst_1
      // 08a6: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 08ab: pop
      // 08EventMotion: aload 42
      // 08ae: aload 41
      // 08b0: iload 44
      // 08b2: iload 45
      // 08b4: iadd
      // 08b5: i2f
      // 08b6: iload 44
      // 08b8: iload 45
      // 08ba: iadd
      // 08DraggableWidget: i2f
      // 08bc: fconst_0
      // 08bd: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 08c2: iload 23
      // 08c4: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 08c9: fconst_1
      // 08ConfigFile: fconst_1
      // 08cb: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 08d0: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 08d3: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 08d8: ldc_w -1051247112
      // 08db: ldc_w 1066975992
      // 08de: iadd
      // 08df: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 08e4: fconst_0
      // 08e5: fconst_0
      // 08e6: fconst_1
      // 08e7: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 08EventAttack: pop
      // 08ed: aload 42
      // 08ef: aload 41
      // 08f1: iload 44
      // 08f3: iload 45
      // 08f5: iadd
      // 08f6: i2f
      // 08f7: iload 44
      // 08f9: i2f
      // 08fa: fconst_0
      // 08fb: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 0900: iload 23
      // 0902: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0907: fconst_1
      // 0908: fconst_0
      // 0909: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 090e: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 0911: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0916: ldc_w -1422318443
      // 0919: ldc_w 1438047323
      // 091c: iadd
      // 091d: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0922: fconst_0
      // 0923: fconst_0
      // 0924: fconst_1
      // 0925: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 092a: pop
      // 092b: aload 42
      // 092d: aload 41
      // 092f: iload 44
      // 0931: i2f
      // 0932: iload 44
      // 0934: i2f
      // 0935: fconst_0
      // 0936: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 093b: iload 23
      // 093d: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0942: fconst_0
      // 0943: fconst_0
      // 0944: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 0949: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 094c: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0951: ldc_w 1364118084
      // 0954: ldc_w -1348389204
      // 0957: iadd
      // 0958: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 095d: fconst_0
      // 095e: fconst_0
      // 095f: fconst_1
      // 0960: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 0965: pop
      // 0966: ldc_w -12.0
      // 0969: ldc_w 20.0
      // 096c: aload 0
      // 096d: getfield ru/white/module/impl/render/yk.sizeGlowOFF Lq2/z/SliderSetting;
      // 0970: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0973: invokevirtual java/lang/Float.floatValue ()F
      // 0976: fmul
      // 0977: fsub
      // 0978: f2i
      // 0979: istore 44
      // 097b: ldc_w 16.0
      // 097e: ldc_w 40.0
      // 0981: aload 0
      // 0982: getfield ru/white/module/impl/render/yk.sizeGlowOFF Lq2/z/SliderSetting;
      // 0985: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0988: invokevirtual java/lang/Float.floatValue ()F
      // 098b: fmul
      // 098c: fadd
      // 098d: f2i
      // 098e: istore 45
      // 0990: aload 42
      // 0992: aload 41
      // 0994: iload 44
      // 0996: i2f
      // 0997: iload 44
      // 0999: iload 45
      // 099b: iadd
      // 099c: i2f
      // 099d: fconst_0
      // 099e: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 09a3: iload 23
      // 09a5: fload 3
      // 09a6: aload 0
      // 09a7: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 09aa: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 09ad: invokevirtual java/lang/Float.floatValue ()F
      // 09b0: fmul
      // 09b1: invokestatic q4/z/up.replAlpha (IF)I
      // 09b4: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 09b9: fconst_0
      // 09ba: fconst_1
      // 09DraggableWidget: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 09c0: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 09c3: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 09c8: ldc_w 1363293200
      // 09cb: ldc_w -1347564320
      // 09ce: iadd
      // 09cf: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 09d4: fconst_0
      // 09d5: fconst_0
      // 09d6: fconst_1
      // 09d7: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 09EventKey: pop
      // 09dd: aload 42
      // 09df: aload 41
      // 09e1: iload 44
      // 09e3: iload 45
      // 09e5: iadd
      // 09e6: i2f
      // 09e7: iload 44
      // 09e9: iload 45
      // 09eb: iadd
      // 09EventAttack: i2f
      // 09ed: fconst_0
      // 09ee: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 09f3: iload 23
      // 09f5: fload 3
      // 09f6: aload 0
      // 09f7: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 09fa: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 09fd: invokevirtual java/lang/Float.floatValue ()F
      // 0a00: fmul
      // 0a01: invokestatic q4/z/up.replAlpha (IF)I
      // 0a04: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0a09: fconst_1
      // 0a0a: fconst_1
      // 0a0b: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 0a10: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 0a13: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0a18: ldc_w 1464190666
      // 0a1b: ldc_w -1448461786
      // 0a1e: iadd
      // 0a1f: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0a24: fconst_0
      // 0a25: fconst_0
      // 0a26: fconst_1
      // 0a27: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 0a2c: pop
      // 0a2d: aload 42
      // 0a2f: aload 41
      // 0a31: iload 44
      // 0a33: iload 45
      // 0a35: iadd
      // 0a36: i2f
      // 0a37: iload 44
      // 0a39: i2f
      // 0a3a: fconst_0
      // 0a3b: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 0a40: iload 23
      // 0a42: fload 3
      // 0a43: aload 0
      // 0a44: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 0a47: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0a4a: invokevirtual java/lang/Float.floatValue ()F
      // 0a4d: fmul
      // 0a4e: invokestatic q4/z/up.replAlpha (IF)I
      // 0a51: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0a56: fconst_1
      // 0a57: fconst_0
      // 0a58: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 0a5d: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 0a60: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0a65: ldc_w -960788554
      // 0a68: ldc_w 976517434
      // 0a6b: iadd
      // 0a6c: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0a71: fconst_0
      // 0a72: fconst_0
      // 0a73: fconst_1
      // 0a74: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 0a79: pop
      // 0a7a: aload 42
      // 0a7c: aload 41
      // 0a7e: iload 44
      // 0a80: i2f
      // 0a81: iload 44
      // 0a83: i2f
      // 0a84: fconst_0
      // 0a85: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 0a8a: iload 23
      // 0a8c: fload 3
      // 0a8d: aload 0
      // 0a8e: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 0a91: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0a94: invokevirtual java/lang/Float.floatValue ()F
      // 0a97: fmul
      // 0a98: invokestatic q4/z/up.replAlpha (IF)I
      // 0a9b: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0aa0: fconst_0
      // 0aa1: fconst_0
      // 0aa2: invokeinterface net/minecraft/client/render/VertexConsumer.method_22913 (FF)Lnet/minecraft/client/render/VertexConsumer; 3
      // 0aa7: getstatic net/minecraft/client/render/OverlayTexture.field_21444 I
      // 0aaa: invokeinterface net/minecraft/client/render/VertexConsumer.method_22922 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0aaf: ldc_w -210977375
      // 0ab2: ldc_w 226706255
      // 0ab5: iadd
      // 0ab6: invokeinterface net/minecraft/client/render/VertexConsumer.method_60803 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 0abb: fconst_0
      // 0abc: fconst_0
      // 0abd: fconst_1
      // 0abe: invokeinterface net/minecraft/client/render/VertexConsumer.method_22914 (FFF)Lnet/minecraft/client/render/VertexConsumer; 4
      // 0ac3: pop
      // 0ac4: aload 9
      // 0ac6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 0ac9: iinc 29 1
      // 0acc: goto 075f
      // 0acf: iload 28
      // 0ad1: iload 24
      // 0ad3: iadd
      // 0ad4: istore 28
      // 0ad6: goto 0755
      // 0ad9: aload 9
      // 0adb: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 0ade: fload 3
      // 0adf: ldc_w 0.001
      // 0ae2: fcmpl
      // 0ae3: ifle 0df9
      // 0ae6: aload 0
      // 0ae7: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0aea: ifnull 0df9
      // 0aed: aload 0
      // 0aee: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 0af1: ldc "Призраки"
      // 0af3: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 0af6: ifeq 0df9
      // 0af9: aload 0
      // 0afa: getfield ru/white/module/impl/render/yk.typeGhost Lq2/z/ModeSetting;
      // 0afd: ldc_w "1"
      // 0b00: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 0b03: ifeq 0df9
      // 0b06: aload 0
      // 0b07: getfield ru/white/module/impl/render/yk.speed Lq2/z/SliderSetting;
      // 0b0a: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0b0d: invokevirtual java/lang/Float.longValue ()J
      // 0b10: lstore 5
      // 0b12: invokestatic java/lang/System.currentTimeMillis ()J
      // 0b15: lstore 7
      // 0b17: aload 0
      // 0b18: getfield ru/white/module/impl/render/yk.f790 J
      // 0b1b: lconst_0
      // 0b1c: lcmp
      // 0b1d: ifne 0b26
      // 0b20: aload 0
      // 0b21: lload 7
      // 0b23: putfield ru/white/module/impl/render/yk.f790 J
      // 0b26: lload 7
      // 0b28: aload 0
      // 0b29: getfield ru/white/module/impl/render/yk.f790 J
      // 0b2c: lsub
      // 0b2d: lstore 9
      // 0b2f: lload 9
      // 0b31: lconst_0
      // 0b32: lcmp
      // 0b33: ifle 0b4e
      // 0b36: aload 0
      // 0b37: dup
      // 0b38: getfield ru/white/module/impl/render/yk.f789 F
      // 0b3b: ldc2_w 1984332279177890548
      // 0b3e: ldc2_w -1984332279177890543
      // 0b41: ladd
      // 0b42: lload 9
      // 0b44: lmul
      // 0b45: l2f
      // 0b46: lload 5
      // 0b48: l2f
      // 0b49: fdiv
      // 0b4a: fadd
      // 0b4b: putfield ru/white/module/impl/render/yk.f789 F
      // 0b4e: aload 0
      // 0b4f: lload 7
      // 0b51: putfield ru/white/module/impl/render/yk.f790 J
      // 0b54: aload 0
      // 0b55: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0b58: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 0b5b: istore 11
      // 0b5d: iload 11
      // 0b5f: i2d
      // 0b60: ldc2_w 0.15707963267948966
      // 0b63: dmul
      // 0b64: invokestatic java/lang/Math.sin (D)D
      // 0b67: d2f
      // 0b68: fstore 12
      // 0b6a: ldc_w 1414423149
      // 0b6d: ldc_w -1414422894
      // 0b70: iadd
      // 0b71: ldc_w -1590563302
      // 0b74: ldc_w 1590563402
      // 0b77: iadd
      // 0b78: ldc_w -1316316265
      // 0b7b: ldc_w 1316316365
      // 0b7e: iadd
      // 0b7f: ldc_w 255.0
      // 0b82: fload 3
      // 0b83: fmul
      // 0b84: f2i
      // 0b85: invokestatic q4/z/up.getColor (IIII)I
      // 0b88: istore 13
      // 0b8a: bipush 0
      // 0b8b: invokestatic q4/z/up.fade (I)I
      // 0b8e: fload 3
      // 0b8f: invokestatic q4/z/up.multAlpha (IF)I
      // 0b92: iload 13
      // 0b94: fload 12
      // 0b96: invokestatic q4/z/up.overCol (IIF)I
      // 0b99: istore 14
      // 0b9b: ldc_w 1740731611
      // 0b9e: ldc_w -1740731521
      // 0ba1: iadd
      // 0ba2: invokestatic q4/z/up.fade (I)I
      // 0ba5: fload 3
      // 0ba6: invokestatic q4/z/up.multAlpha (IF)I
      // 0ba9: iload 13
      // 0bab: fload 12
      // 0bad: invokestatic q4/z/up.overCol (IIF)I
      // 0bb0: istore 15
      // 0bb2: ldc_w 1450348993
      // 0bb5: ldc_w -1450348813
      // 0bb8: iadd
      // 0bb9: invokestatic q4/z/up.fade (I)I
      // 0bbc: fload 3
      // 0bbd: invokestatic q4/z/up.multAlpha (IF)I
      // 0bc0: iload 13
      // 0bc2: fload 12
      // 0bc4: invokestatic q4/z/up.overCol (IIF)I
      // 0bc7: istore 16
      // 0bc9: ldc_w 192591983
      // 0bcc: ldc_w -192591623
      // 0bcf: iadd
      // 0bd0: invokestatic q4/z/up.fade (I)I
      // 0bd3: fload 3
      // 0bd4: invokestatic q4/z/up.multAlpha (IF)I
      // 0bd7: iload 13
      // 0bd9: fload 12
      // 0bdb: invokestatic q4/z/up.overCol (IIF)I
      // 0bde: istore 17
      // 0be0: aload 1
      // 0be1: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 0be4: astore 18
      // 0be6: aload 0
      // 0be7: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0bea: aload 1
      // 0beb: invokevirtual x9/sb.getTickDelta ()F
      // 0bee: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 0bf1: astore 19
      // 0bf3: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 0bf6: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 0bf9: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 0bfc: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 0bff: astore 20
      // 0c01: invokestatic java/lang/System.currentTimeMillis ()J
      // 0c04: lstore 21
      // 0c06: lload 21
      // 0c08: l2d
      // 0c09: lload 5
      // 0c0b: l2d
      // 0c0c: ddiv
      // 0c0d: dstore 23
      // 0c0f: aload 0
      // 0c10: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0c13: invokevirtual net/minecraft/entity/LivingEntity.method_17681 ()F
      // 0c16: ldc_w 0.25
      // 0c19: fadd
      // 0c1a: ldc_w 0.25
      // 0c1d: fload 3
      // 0c1e: fmul
      // 0c1f: fsub
      // 0c20: fstore 25
      // 0c22: bipush 3
      // 0c23: istore 26
      // 0c25: ldc_w 2083462007
      // 0c28: ldc_w -2083461997
      // 0c2b: iadd
      // 0c2c: istore 27
      // 0c2e: bipush 0
      // 0c2f: istore 28
      // 0c31: iload 28
      // 0c33: iload 26
      // 0c35: if_icmpge 0df9
      // 0c38: bipush 0
      // 0c39: istore 29
      // 0c3b: iload 29
      // 0c3d: iload 27
      // 0c3f: if_icmpge 0df3
      // 0c42: aload 18
      // 0c44: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 0c47: aload 0
      // 0c48: getfield ru/white/module/impl/render/yk.f789 F
      // 0c4b: iload 29
      // 0c4d: i2f
      // 0c4e: ldc_w 0.1
      // 0c51: fmul
      // 0c52: fadd
      // 0c53: fstore 30
      // 0c55: dload 23
      // 0c57: iload 28
      // 0c59: i2d
      // 0c5a: ldc2_w 6.283185307179586
      // 0c5d: iload 26
      // 0c5f: i2d
      // 0c60: ddiv
      // 0c61: dmul
      // 0c62: dadd
      // 0c63: dstore 31
      // 0c65: dload 31
      // 0c67: fload 30
      // 0c69: f2d
      // 0c6a: dadd
      // 0c6b: invokestatic java/lang/Math.cos (D)D
      // 0c6e: fload 25
      // 0c70: f2d
      // 0c71: dmul
      // 0c72: dstore 33
      // 0c74: dload 31
      // 0c76: fload 30
      // 0c78: f2d
      // 0c79: dadd
      // 0c7a: invokestatic java/lang/Math.sin (D)D
      // 0c7d: fload 25
      // 0c7f: f2d
      // 0c80: dmul
      // 0c81: dstore 35
      // 0c83: dload 23
      // 0c85: iload 28
      // 0c87: i2d
      // 0c88: dadd
      // 0c89: invokestatic java/lang/Math.sin (D)D
      // 0c8c: ldc2_w 0.6
      // 0c8f: dmul
      // 0c90: ldc2_w 0.15
      // 0c93: dload 23
      // 0c95: iload 29
      // 0c97: i2f
      // 0c98: ldc_w 0.11
      // 0c9b: fmul
      // 0c9c: f2d
      // 0c9d: dadd
      // 0c9e: invokestatic java/lang/Math.sin (D)D
      // 0ca1: dmul
      // 0ca2: dadd
      // 0ca3: dstore 37
      // 0ca5: aload 18
      // 0ca7: aload 19
      // 0ca9: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 0cac: aload 20
      // 0cae: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 0cb1: dsub
      // 0cb2: dload 33
      // 0cb4: dadd
      // 0cb5: aload 19
      // 0cb7: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 0cba: aload 20
      // 0cbc: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 0cbf: dsub
      // 0cc0: aload 0
      // 0cc1: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0cc4: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 0cc7: fconst_2
      // 0cc8: fdiv
      // 0cc9: f2d
      // 0cca: dadd
      // 0ccb: dload 37
      // 0ccd: dadd
      // 0cce: aload 19
      // 0cd0: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 0cd3: aload 20
      // 0cd5: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 0cd8: dsub
      // 0cd9: dload 35
      // 0cdb: dadd
      // 0cdc: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0cdf: aload 18
      // 0ce1: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0ce4: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 0ce7: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 0cea: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 0ced: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 0cf0: fneg
      // 0cf1: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0cf6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0cf9: aload 18
      // 0cfb: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 0cfe: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 0d01: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 0d04: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 0d07: invokevirtual net/minecraft/client/render/Camera.method_19329 ()F
      // 0d0a: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 0d0f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 0d12: ldc_w 0.12
      // 0d15: iload 29
      // 0d17: i2f
      // 0d18: ldc_w 40.0
      // 0d1b: fdiv
      // 0d1c: fadd
      // 0d1d: ldc_w 1.25
      // 0d20: fmul
      // 0d21: fload 3
      // 0d22: fmul
      // 0d23: fstore 39
      // 0d25: aload 18
      // 0d27: fload 39
      // 0d29: fload 39
      // 0d2b: fload 39
      // 0d2d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 0d30: aload 18
      // 0d32: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 0d35: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 0d38: astore 40
      // 0d3a: aload 4
      // 0d3c: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 0d3f: ldc_w "client"
      // 0d42: ldc_w "jpvaTDr5St7/YtFR0MAp5AFat37P1jgVYTOzxb+lHwQ="
      // 0d45: ldc_w -822918387
      // 0d48: ldc_w -1274234036
      // 0d4b: iadd
      // 0d4c: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 0d4f: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 0d52: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 0d57: checkcast net/minecraft/client/render/RenderLayer
      // 0d5a: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 0d5d: astore 41
      // 0d5f: aload 41
      // 0d61: aload 40
      // 0d63: iload 14
      // 0d65: iload 15
      // 0d67: iload 16
      // 0d69: iload 17
      // 0d6b: ldc_w 255.0
      // 0d6e: fload 3
      // 0d6f: fmul
      // 0d70: f2i
      // 0d71: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 0d74: ldc_w 0.12
      // 0d77: iload 29
      // 0d79: i2f
      // 0d7a: ldc_w 40.0
      // 0d7d: fdiv
      // 0d7e: fadd
      // 0d7f: ldc_w 100.0
      // 0d82: aload 0
      // 0d83: getfield ru/white/module/impl/render/yk.sizeGlowOFF Lq2/z/SliderSetting;
      // 0d86: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0d89: invokevirtual java/lang/Float.floatValue ()F
      // 0d8c: fmul
      // 0d8d: fmul
      // 0d8e: ldc_w 10.0
      // 0d91: fdiv
      // 0d92: fload 3
      // 0d93: fmul
      // 0d94: fstore 39
      // 0d96: aload 18
      // 0d98: fload 39
      // 0d9a: fload 39
      // 0d9c: fload 39
      // 0d9e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 0da1: aload 4
      // 0da3: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 0da6: ldc_w "client"
      // 0da9: ldc_w "FuIPjKKAnx5nGwSRSLn8JJkjYr5Xr+3V+UpmBCfcysQ="
      // 0dac: ldc_w -235926802
      // 0daf: ldc_w 1346010323
      // 0db2: iadd
      // 0db3: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 0db6: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 0db9: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 0dbe: checkcast net/minecraft/client/render/RenderLayer
      // 0dc1: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 0dc4: astore 42
      // 0dc6: aload 42
      // 0dc8: aload 40
      // 0dca: iload 14
      // 0dcc: iload 15
      // 0dce: iload 16
      // 0dd0: iload 17
      // 0dd2: ldc_w 255.0
      // 0dd5: aload 0
      // 0dd6: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 0dd9: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0ddc: invokevirtual java/lang/Float.floatValue ()F
      // 0ddf: fmul
      // 0de0: fconst_1
      // 0de1: fmul
      // 0de2: fload 3
      // 0de3: fmul
      // 0de4: f2i
      // 0de5: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 0de8: aload 18
      // 0dea: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 0ded: iinc 29 1
      // 0df0: goto 0c3b
      // 0df3: iinc 28 1
      // 0df6: goto 0c31
      // 0df9: fload 3
      // 0dfa: ldc_w 0.001
      // 0dfd: fcmpl
      // 0dfe: ifle 110c
      // 0e01: aload 0
      // 0e02: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0e05: ifnull 110c
      // 0e08: aload 0
      // 0e09: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 0e0c: ldc "Призраки"
      // 0e0e: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 0e11: ifeq 110c
      // 0e14: aload 0
      // 0e15: getfield ru/white/module/impl/render/yk.typeGhost Lq2/z/ModeSetting;
      // 0e18: ldc_w "3"
      // 0e1b: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 0e1e: ifeq 110c
      // 0e21: aload 0
      // 0e22: getfield ru/white/module/impl/render/yk.speed Lq2/z/SliderSetting;
      // 0e25: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 0e28: invokevirtual java/lang/Float.longValue ()J
      // 0e2b: lstore 5
      // 0e2d: invokestatic java/lang/System.currentTimeMillis ()J
      // 0e30: lstore 7
      // 0e32: aload 0
      // 0e33: getfield ru/white/module/impl/render/yk.f790 J
      // 0e36: lconst_0
      // 0e37: lcmp
      // 0e38: ifne 0e41
      // 0e3b: aload 0
      // 0e3c: lload 7
      // 0e3e: putfield ru/white/module/impl/render/yk.f790 J
      // 0e41: lload 7
      // 0e43: aload 0
      // 0e44: getfield ru/white/module/impl/render/yk.f790 J
      // 0e47: lsub
      // 0e48: lstore 9
      // 0e4a: lload 9
      // 0e4c: lconst_0
      // 0e4d: lcmp
      // 0e4e: ifle 0e69
      // 0e51: aload 0
      // 0e52: dup
      // 0e53: getfield ru/white/module/impl/render/yk.f789 F
      // 0e56: ldc2_w -527899756314972214
      // 0e59: ldc2_w 527899756314972219
      // 0e5c: ladd
      // 0e5d: lload 9
      // 0e5f: lmul
      // 0e60: l2f
      // 0e61: lload 5
      // 0e63: l2f
      // 0e64: fdiv
      // 0e65: fadd
      // 0e66: putfield ru/white/module/impl/render/yk.f789 F
      // 0e69: aload 0
      // 0e6a: lload 7
      // 0e6c: putfield ru/white/module/impl/render/yk.f790 J
      // 0e6f: aload 0
      // 0e70: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0e73: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 0e76: istore 11
      // 0e78: iload 11
      // 0e7a: i2d
      // 0e7b: ldc2_w 0.15707963267948966
      // 0e7e: dmul
      // 0e7f: invokestatic java/lang/Math.sin (D)D
      // 0e82: d2f
      // 0e83: fstore 12
      // 0e85: ldc_w 1126161265
      // 0e88: ldc_w -1126161010
      // 0e8b: iadd
      // 0e8c: ldc_w 357990537
      // 0e8f: ldc_w -357990437
      // 0e92: iadd
      // 0e93: ldc_w -1609927431
      // 0e96: ldc_w 1609927531
      // 0e99: iadd
      // 0e9a: ldc_w 255.0
      // 0e9d: fload 3
      // 0e9e: fmul
      // 0e9f: f2i
      // 0ea0: invokestatic q4/z/up.getColor (IIII)I
      // 0ea3: istore 13
      // 0ea5: bipush 0
      // 0ea6: invokestatic q4/z/up.fade (I)I
      // 0ea9: fload 3
      // 0eaa: invokestatic q4/z/up.multAlpha (IF)I
      // 0ead: iload 13
      // 0eaf: fload 12
      // 0eb1: invokestatic q4/z/up.overCol (IIF)I
      // 0eb4: istore 14
      // 0eb6: ldc_w -1744636531
      // 0eb9: ldc_w 1744636621
      // 0ebc: iadd
      // 0ebd: invokestatic q4/z/up.fade (I)I
      // 0ec0: fload 3
      // 0ec1: invokestatic q4/z/up.multAlpha (IF)I
      // 0ec4: iload 13
      // 0ec6: fload 12
      // 0ec8: invokestatic q4/z/up.overCol (IIF)I
      // 0ecb: istore 15
      // 0ecd: ldc_w -1492329025
      // 0ed0: ldc_w 1492329205
      // 0ed3: iadd
      // 0ed4: invokestatic q4/z/up.fade (I)I
      // 0ed7: fload 3
      // 0ed8: invokestatic q4/z/up.multAlpha (IF)I
      // 0edb: iload 13
      // 0edd: fload 12
      // 0edf: invokestatic q4/z/up.overCol (IIF)I
      // 0ee2: istore 16
      // 0ee4: ldc_w 1946203622
      // 0ee7: ldc_w -1946203262
      // 0eea: iadd
      // 0eeb: invokestatic q4/z/up.fade (I)I
      // 0eee: fload 3
      // 0eef: invokestatic q4/z/up.multAlpha (IF)I
      // 0ef2: iload 13
      // 0ef4: fload 12
      // 0ef6: invokestatic q4/z/up.overCol (IIF)I
      // 0ef9: istore 17
      // 0efb: aload 1
      // 0efc: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 0eff: astore 18
      // 0f01: aload 0
      // 0f02: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0f05: aload 1
      // 0f06: invokevirtual x9/sb.getTickDelta ()F
      // 0f09: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 0f0c: astore 19
      // 0f0e: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 0f11: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 0f14: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 0f17: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 0f1a: astore 20
      // 0f1c: invokestatic java/lang/System.currentTimeMillis ()J
      // 0f1f: lstore 21
      // 0f21: lload 21
      // 0f23: l2d
      // 0f24: lload 5
      // 0f26: l2d
      // 0f27: ddiv
      // 0f28: dstore 23
      // 0f2a: aload 0
      // 0f2b: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0f2e: invokevirtual net/minecraft/entity/LivingEntity.method_17681 ()F
      // 0f31: ldc_w 0.25
      // 0f34: fadd
      // 0f35: ldc_w 0.25
      // 0f38: fload 3
      // 0f39: fmul
      // 0f3a: fsub
      // 0f3b: fstore 25
      // 0f3d: bipush 3
      // 0f3e: istore 26
      // 0f40: ldc_w 524005945
      // 0f43: ldc_w -524005936
      // 0f46: iadd
      // 0f47: istore 27
      // 0f49: bipush 0
      // 0f4a: istore 28
      // 0f4c: iload 28
      // 0f4e: iload 26
      // 0f50: if_icmpge 110c
      // 0f53: bipush 0
      // 0f54: istore 29
      // 0f56: iload 29
      // 0f58: iload 27
      // 0f5a: if_icmpge 1106
      // 0f5d: aload 18
      // 0f5f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 0f62: aload 0
      // 0f63: getfield ru/white/module/impl/render/yk.f789 F
      // 0f66: iload 29
      // 0f68: i2f
      // 0f69: ldc_w 0.1
      // 0f6c: fmul
      // 0f6d: fadd
      // 0f6e: fstore 30
      // 0f70: dload 23
      // 0f72: iload 28
      // 0f74: i2d
      // 0f75: ldc2_w 6.283185307179586
      // 0f78: iload 26
      // 0f7a: i2d
      // 0f7b: ddiv
      // 0f7c: dmul
      // 0f7d: dadd
      // 0f7e: dstore 31
      // 0f80: dload 31
      // 0f82: fload 30
      // 0f84: f2d
      // 0f85: dadd
      // 0f86: invokestatic java/lang/Math.cos (D)D
      // 0f89: fload 25
      // 0f8b: f2d
      // 0f8c: dmul
      // 0f8d: dstore 33
      // 0f8f: dload 31
      // 0f91: fload 30
      // 0f93: f2d
      // 0f94: dadd
      // 0f95: invokestatic java/lang/Math.sin (D)D
      // 0f98: fload 25
      // 0f9a: f2d
      // 0f9b: dmul
      // 0f9c: dstore 35
      // 0f9e: dload 23
      // 0fa0: invokestatic java/lang/Math.sin (D)D
      // 0fa3: ldc2_w 0.7
      // 0fa6: dmul
      // 0fa7: ldc2_w 0.2
      // 0faa: dload 23
      // 0fac: iload 29
      // 0fae: i2f
      // 0faf: ldc_w 0.11
      // 0fb2: fmul
      // 0fb3: f2d
      // 0fb4: dadd
      // 0fb5: invokestatic java/lang/Math.sin (D)D
      // 0fb8: dmul
      // 0fb9: dadd
      // 0fba: dstore 37
      // 0fbc: aload 18
      // 0fbe: aload 19
      // 0fc0: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 0fc3: aload 20
      // 0fc5: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 0fc8: dsub
      // 0fc9: dload 33
      // 0fcb: dadd
      // 0fcc: aload 19
      // 0fce: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 0fd1: aload 20
      // 0fd3: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 0fd6: dsub
      // 0fd7: aload 0
      // 0fd8: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 0fdb: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 0fde: fconst_2
      // 0fdf: fdiv
      // 0fe0: f2d
      // 0fe1: dadd
      // 0fe2: dload 37
      // 0fe4: dadd
      // 0fe5: aload 19
      // 0fe7: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 0fea: aload 20
      // 0fec: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 0fef: dsub
      // 0ff0: dload 35
      // 0ff2: dadd
      // 0ff3: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 0ff6: aload 18
      // 0ff8: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 0ffb: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 0ffe: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 1001: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 1004: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 1007: fneg
      // 1008: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 100d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1010: aload 18
      // 1012: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1015: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 1018: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 101b: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 101e: invokevirtual net/minecraft/client/render/Camera.method_19329 ()F
      // 1021: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1026: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1029: ldc_w 0.12
      // 102c: iload 29
      // 102e: i2f
      // 102f: ldc_w 50.0
      // 1032: fdiv
      // 1033: fadd
      // 1034: ldc_w 1.5
      // 1037: fmul
      // 1038: fstore 39
      // 103a: aload 18
      // 103c: fload 39
      // 103e: fload 39
      // 1040: fload 39
      // 1042: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 1045: aload 18
      // 1047: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 104a: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 104d: astore 40
      // 104f: aload 4
      // 1051: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 1054: ldc_w "client"
      // 1057: ldc_w "iGd8ZDwF7Pb5nnd51jyPzAemEVbJKp49Z88V7blZuSw="
      // 105a: ldc_w -1702177211
      // 105d: ldc_w 265169946
      // 1060: iadd
      // 1061: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 1064: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 1067: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 106c: checkcast net/minecraft/client/render/RenderLayer
      // 106f: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 1072: astore 41
      // 1074: aload 41
      // 1076: aload 40
      // 1078: iload 14
      // 107a: iload 15
      // 107c: iload 16
      // 107e: iload 17
      // 1080: ldc_w 255.0
      // 1083: fload 3
      // 1084: fmul
      // 1085: f2i
      // 1086: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 1089: ldc_w 0.12
      // 108c: iload 29
      // 108e: i2f
      // 108f: ldc_w 50.0
      // 1092: fdiv
      // 1093: fadd
      // 1094: ldc_w 100.0
      // 1097: aload 0
      // 1098: getfield ru/white/module/impl/render/yk.sizeGlowOFF Lq2/z/SliderSetting;
      // 109b: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 109e: invokevirtual java/lang/Float.floatValue ()F
      // 10a1: fmul
      // 10a2: fmul
      // 10a3: ldc_w 10.0
      // 10a6: fdiv
      // 10a7: fstore 39
      // 10a9: aload 18
      // 10ab: fload 39
      // 10ad: fload 39
      // 10af: fload 39
      // 10b1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 10b4: aload 4
      // 10b6: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 10b9: ldc_w "client"
      // 10bc: ldc_w "pOUVOxCHhanVHB4m+r7mkyskeAnlqPdiS018s5Xb0HM="
      // 10bf: ldc_w -1768756942
      // 10c2: ldc_w 1587386177
      // 10c5: iadd
      // 10c6: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 10c9: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 10EventPacket: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 10d1: checkcast net/minecraft/client/render/RenderLayer
      // 10d4: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 10d7: astore 42
      // 10d9: aload 42
      // 10db: aload 40
      // 10dd: iload 14
      // 10df: iload 15
      // 10e1: iload 16
      // 10e3: iload 17
      // 10e5: ldc_w 255.0
      // 10e8: aload 0
      // 10e9: getfield ru/white/module/impl/render/yk.sizeGlow Lq2/z/SliderSetting;
      // 10EventAttack: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 10ef: invokevirtual java/lang/Float.floatValue ()F
      // 10f2: fmul
      // 10f3: fconst_1
      // 10f4: fmul
      // 10f5: fload 3
      // 10f6: fmul
      // 10f7: f2i
      // 10f8: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 10fb: aload 18
      // 10fd: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 1100: iinc 29 1
      // 1103: goto 0f56
      // 1106: iinc 28 1
      // 1109: goto 0f4c
      // 110c: fload 3
      // 110d: ldc_w 0.001
      // 1110: fcmpl
      // 1111: ifle 13cb
      // 1114: aload 0
      // 1115: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1118: ifnull 13cb
      // 111b: aload 0
      // 111c: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 111f: ldc "Картинка"
      // 1121: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 1124: ifeq 13cb
      // 1127: aload 0
      // 1128: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 112b: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 112e: istore 5
      // 1130: iload 5
      // 1132: i2d
      // 1133: ldc2_w 0.15707963267948966
      // 1136: dmul
      // 1137: invokestatic java/lang/Math.sin (D)D
      // 113a: d2f
      // 113b: fstore 6
      // 113d: aload 0
      // 113e: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1141: invokevirtual ru/white/utils/animation/Animation.update ()Z
      // 1144: pop
      // 1145: aload 0
      // 1146: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1149: fload 6
      // 114b: f2d
      // 114c: ldc2_w 0.15000000596046448
      // 114f: getstatic ru/white/utils/animation/ap.SINE_OUT Lru/white/utils/animation/zo;
      // 1152: invokevirtual ru/white/utils/animation/Animation.run (DDLru/white/utils/animation/zo;)Lru/white/utils/animation/Animation;
      // 1155: pop
      // 1156: ldc_w 1047471293
      // 1159: ldc_w -1047471108
      // 115c: iadd
      // 115d: ldc_w 1424316373
      // 1160: ldc_w -1424316293
      // 1163: iadd
      // 1164: ldc_w 1957486659
      // 1167: ldc_w -1957486579
      // 116a: iadd
      // 116b: ldc_w 255.0
      // 116e: fload 3
      // 116f: fmul
      // 1170: f2i
      // 1171: invokestatic q4/z/up.getColor (IIII)I
      // 1174: istore 7
      // 1176: bipush 0
      // 1177: invokestatic q4/z/up.fade (I)I
      // 117a: fload 3
      // 117b: invokestatic q4/z/up.multAlpha (IF)I
      // 117e: iload 7
      // 1180: aload 0
      // 1181: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1184: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 1187: invokestatic q4/z/up.overCol (IIF)I
      // 118a: istore 8
      // 118c: ldc_w -1309783684
      // 118f: ldc_w 1309783774
      // 1192: iadd
      // 1193: invokestatic q4/z/up.fade (I)I
      // 1196: fload 3
      // 1197: invokestatic q4/z/up.multAlpha (IF)I
      // 119a: iload 7
      // 119c: aload 0
      // 119d: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 11a0: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 11a3: invokestatic q4/z/up.overCol (IIF)I
      // 11a6: istore 9
      // 11a8: ldc_w -1208690421
      // 11ab: ldc_w 1208690601
      // 11ae: iadd
      // 11af: invokestatic q4/z/up.fade (I)I
      // 11b2: fload 3
      // 11b3: invokestatic q4/z/up.multAlpha (IF)I
      // 11b6: iload 7
      // 11b8: aload 0
      // 11b9: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 11bc: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 11bf: invokestatic q4/z/up.overCol (IIF)I
      // 11c2: istore 10
      // 11c4: ldc_w 1459807569
      // 11c7: ldc_w -1459807209
      // 11ConfigFile: iadd
      // 11cb: invokestatic q4/z/up.fade (I)I
      // 11ce: fload 3
      // 11cf: invokestatic q4/z/up.multAlpha (IF)I
      // 11d2: iload 7
      // 11d4: aload 0
      // 11d5: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 11d8: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 11db: invokestatic q4/z/up.overCol (IIF)I
      // 11de: istore 11
      // 11e0: aload 1
      // 11e1: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 11e4: astore 12
      // 11e6: aload 4
      // 11e8: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 11eb: ldc_w "client"
      // 11ee: ldc_w "PcCzR4miI9VMObhaY5tA77IF3nVvgUZcx3Xi"
      // 11f1: ldc_w -787062998
      // 11f4: ldc_w -1199552832
      // 11f7: iadd
      // 11f8: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 11fb: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 11fe: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 1203: checkcast net/minecraft/client/render/RenderLayer
      // 1206: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 1209: astore 13
      // 120b: aload 0
      // 120c: getfield ru/white/module/impl/render/yk.typeImages Lq2/z/ModeSetting;
      // 120f: ldc_w "2"
      // 1212: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 1215: ifeq 123d
      // 1218: aload 4
      // 121a: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 121d: ldc_w "client"
      // 1220: ldc_w "ToXqo/rnejE/fOG+EN4ZC8FZh5EQxBm4tDC7"
      // 1223: ldc_w -1271333126
      // 1226: ldc_w -1181294177
      // 1229: iadd
      // 122a: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 122d: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 1230: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 1235: checkcast net/minecraft/client/render/RenderLayer
      // 1238: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 123b: astore 13
      // 123d: aload 0
      // 123e: getfield ru/white/module/impl/render/yk.typeImages Lq2/z/ModeSetting;
      // 1241: ldc_w "3"
      // 1244: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 1247: ifeq 126f
      // 124a: aload 4
      // 124c: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 124f: ldc_w "client"
      // 1252: ldc_w "fuEq5cqDuncPGCH4ILrZTfEkR9csoN+j2kpyOA=="
      // 1255: ldc_w 335259445
      // 1258: ldc_w 387177844
      // 125b: iadd
      // 125c: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 125f: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 1262: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 1267: checkcast net/minecraft/client/render/RenderLayer
      // 126a: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 126d: astore 13
      // 126f: aload 0
      // 1270: getfield ru/white/module/impl/render/yk.typeImages Lq2/z/ModeSetting;
      // 1273: ldc_w "4"
      // 1276: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 1279: ifeq 12a1
      // 127c: aload 4
      // 127e: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 1281: ldc_w "client"
      // 1284: ldc_w "QuVr+PaH+2ozHGDlHL6YUM0gBsoQpJ785k4zJQ=="
      // 1287: ldc_w 314942703
      // 128a: ldc_w 596171686
      // 128d: iadd
      // 128e: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 1291: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 1294: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 1299: checkcast net/minecraft/client/render/RenderLayer
      // 129c: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 129f: astore 13
      // 12a1: aload 0
      // 12a2: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 12a5: aload 1
      // 12a6: invokevirtual x9/sb.getTickDelta ()F
      // 12a9: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 12EventMotion: astore 14
      // 12ae: aload 14
      // 12b0: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 12b3: dstore 15
      // 12b5: aload 14
      // 12b7: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 12ba: dstore 17
      // 12bc: aload 14
      // 12be: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 12c1: dstore 19
      // 12c3: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 12c6: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 12c9: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 12EventPacket: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 12cf: astore 21
      // 12d1: aload 12
      // 12d3: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 12d6: aload 12
      // 12d8: dload 15
      // 12ConfigManager: aload 21
      // 12EventKey: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 12df: dsub
      // 12e0: dload 17
      // 12e2: aload 21
      // 12e4: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 12e7: dsub
      // 12e8: aload 0
      // 12e9: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 12EventAttack: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 12ef: ldc_w 1.75
      // 12f2: fdiv
      // 12f3: f2d
      // 12f4: dadd
      // 12f5: dload 19
      // 12f7: aload 21
      // 12f9: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 12EventMove: dsub
      // 12fd: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1300: aload 12
      // 1302: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 1305: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 1308: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 130b: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 130e: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 1311: fneg
      // 1312: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1317: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 131a: aload 12
      // 131c: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 131f: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 1322: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 1325: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 1328: invokevirtual net/minecraft/client/render/Camera.method_19329 ()F
      // 132b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1330: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1333: invokestatic java/lang/System.currentTimeMillis ()J
      // 1336: lstore 22
      // 1338: dconst_0
      // 1339: ldc2_w 720.0
      // 133c: lload 22
      // 133e: l2d
      // 133f: aload 0
      // 1340: getfield ru/white/module/impl/render/yk.speed_2 Lq2/z/SliderSetting;
      // 1343: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 1346: invokevirtual java/lang/Float.doubleValue ()D
      // 1349: ddiv
      // 134a: invokestatic java/lang/Math.sin (D)D
      // 134d: dconst_1
      // 134e: dadd
      // 134f: ldc2_w 2.0
      // 1352: ddiv
      // 1353: ldc2_w 360.0
      // 1356: dmul
      // 1357: ldc2_w 2.0
      // 135a: dmul
      // 135b: invokestatic x30/fq.clamps (DDD)D
      // 135e: d2f
      // 135f: fstore 24
      // 1361: aload 12
      // 1363: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 1366: fload 24
      // 1368: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 136d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1370: aload 0
      // 1371: getfield ru/white/module/impl/render/yk.size_2 Lq2/z/SliderSetting;
      // 1374: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 1377: invokevirtual java/lang/Float.floatValue ()F
      // 137a: aload 0
      // 137b: getfield ru/white/module/impl/render/yk.size_2 Lq2/z/SliderSetting;
      // 137e: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 1381: invokevirtual java/lang/Float.floatValue ()F
      // 1384: ldc_w 0.4
      // 1387: fmul
      // 1388: aload 0
      // 1389: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 138c: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 138f: fmul
      // 1390: fsub
      // 1391: ldc_w 0.8
      // 1394: fadd
      // 1395: ldc_w 0.8
      // 1398: fload 3
      // 1399: fmul
      // 139a: fsub
      // 139b: fstore 25
      // 139d: aload 12
      // 139f: fload 25
      // 13a1: fload 25
      // 13a3: fconst_1
      // 13a4: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 13a7: aload 12
      // 13a9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 13EventMotion: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 13af: astore 26
      // 13b1: aload 13
      // 13b3: aload 26
      // 13b5: iload 8
      // 13b7: iload 9
      // 13b9: iload 10
      // 13DraggableWidget: iload 11
      // 13bd: ldc_w 255.0
      // 13c0: fload 3
      // 13c1: fmul
      // 13c2: f2i
      // 13c3: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 13c6: aload 12
      // 13c8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 13cb: fload 3
      // 13EventPacket: ldc_w 0.001
      // 13cf: fcmpl
      // 13d0: ifle 1714
      // 13d3: aload 0
      // 13d4: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 13d7: ifnull 1714
      // 13ConfigManager: aload 0
      // 13db: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 13de: ldc_w "Бублик"
      // 13e1: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 13e4: ifeq 1714
      // 13e7: aload 0
      // 13e8: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 13eb: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 13ee: istore 5
      // 13f0: iload 5
      // 13f2: i2d
      // 13f3: ldc2_w 0.15707963267948966
      // 13f6: dmul
      // 13f7: invokestatic java/lang/Math.sin (D)D
      // 13fa: d2f
      // 13fb: fstore 6
      // 13fd: aload 0
      // 13fe: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1401: invokevirtual ru/white/utils/animation/Animation.update ()Z
      // 1404: pop
      // 1405: aload 0
      // 1406: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1409: fload 6
      // 140b: f2d
      // 140c: ldc2_w 0.10000000149011612
      // 140f: getstatic ru/white/utils/animation/ap.SINE_OUT Lru/white/utils/animation/zo;
      // 1412: invokevirtual ru/white/utils/animation/Animation.run (DDLru/white/utils/animation/zo;)Lru/white/utils/animation/Animation;
      // 1415: pop
      // 1416: ldc_w -1804011346
      // 1419: ldc_w 1804011601
      // 141c: iadd
      // 141d: ldc_w -813656535
      // 1420: ldc_w 813656635
      // 1423: iadd
      // 1424: ldc_w -2116870029
      // 1427: ldc_w 2116870129
      // 142a: iadd
      // 142b: ldc_w 255.0
      // 142e: fload 3
      // 142f: fmul
      // 1430: f2i
      // 1431: invokestatic q4/z/up.getColor (IIII)I
      // 1434: istore 7
      // 1436: bipush 0
      // 1437: invokestatic q4/z/up.fade (I)I
      // 143a: fload 3
      // 143b: invokestatic q4/z/up.multAlpha (IF)I
      // 143e: iload 7
      // 1440: aload 0
      // 1441: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1444: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 1447: invokestatic q4/z/up.overCol (IIF)I
      // 144a: istore 8
      // 144c: ldc_w 1198821956
      // 144f: ldc_w -1198821866
      // 1452: iadd
      // 1453: invokestatic q4/z/up.fade (I)I
      // 1456: fload 3
      // 1457: invokestatic q4/z/up.multAlpha (IF)I
      // 145a: iload 7
      // 145c: aload 0
      // 145d: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1460: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 1463: invokestatic q4/z/up.overCol (IIF)I
      // 1466: istore 9
      // 1468: ldc_w -1065823842
      // 146b: ldc_w 1065824022
      // 146e: iadd
      // 146f: invokestatic q4/z/up.fade (I)I
      // 1472: fload 3
      // 1473: invokestatic q4/z/up.multAlpha (IF)I
      // 1476: iload 7
      // 1478: aload 0
      // 1479: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 147c: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 147f: invokestatic q4/z/up.overCol (IIF)I
      // 1482: istore 10
      // 1484: ldc_w -652377513
      // 1487: ldc_w 652377873
      // 148a: iadd
      // 148b: invokestatic q4/z/up.fade (I)I
      // 148e: fload 3
      // 148f: invokestatic q4/z/up.multAlpha (IF)I
      // 1492: iload 7
      // 1494: aload 0
      // 1495: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1498: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 149b: invokestatic q4/z/up.overCol (IIF)I
      // 149e: istore 11
      // 14a0: aload 1
      // 14a1: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 14a4: astore 12
      // 14a6: aload 0
      // 14a7: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 14aa: aload 1
      // 14ab: invokevirtual x9/sb.getTickDelta ()F
      // 14ae: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 14b1: astore 13
      // 14b3: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 14b6: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 14b9: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 14bc: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 14bf: astore 14
      // 14c1: aload 0
      // 14c2: getfield ru/white/module/impl/render/yk.bublikSpeed Lq2/z/SliderSetting;
      // 14c5: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 14c8: invokevirtual java/lang/Float.doubleValue ()D
      // 14cb: dstore 15
      // 14cd: invokestatic java/lang/System.currentTimeMillis ()J
      // 14d0: l2d
      // 14d1: dload 15
      // 14d3: drem
      // 14d4: dstore 17
      // 14d6: dload 17
      // 14d8: dload 15
      // 14ConfigManager: ldc2_w 2.0
      // 14dd: ddiv
      // 14de: dcmpl
      // 14df: ifle 14e6
      // 14e2: bipush 1
      // 14e3: goto 14e7
      // 14e6: bipush 0
      // 14e7: istore 19
      // 14e9: dload 17
      // 14eb: dload 15
      // 14ed: ldc2_w 2.0
      // 14f0: ddiv
      // 14f1: ddiv
      // 14f2: dstore 20
      // 14f4: iload 19
      // 14f6: ifeq 1500
      // 14f9: dload 20
      // 14fb: dconst_1
      // 14EventMove: dsub
      // 14fd: goto 1504
      // 1500: dconst_1
      // 1501: dload 20
      // 1503: dsub
      // 1504: dstore 20
      // 1506: dload 20
      // 1508: ldc2_w 0.5
      // 150b: dcmpg
      // 150c: ifge 151b
      // 150f: ldc2_w 2.0
      // 1512: dload 20
      // 1514: dmul
      // 1515: dload 20
      // 1517: dmul
      // 1518: goto 1531
      // 151b: dconst_1
      // 151c: ldc2_w -2.0
      // 151f: dload 20
      // 1521: dmul
      // 1522: ldc2_w 2.0
      // 1525: dadd
      // 1526: ldc2_w 2.0
      // 1529: invokestatic java/lang/Math.pow (DD)D
      // 152c: ldc2_w 2.0
      // 152f: ddiv
      // 1530: dsub
      // 1531: dstore 22
      // 1533: aload 0
      // 1534: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1537: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 153a: fstore 24
      // 153c: ldc_w 0.12
      // 153f: dload 22
      // 1541: d2f
      // 1542: ldc_w 0.2
      // 1545: fload 24
      // 1547: ldc_w 0.24
      // 154a: fsub
      // 154b: invokestatic java/lang/Math.max (FF)F
      // 154e: fmul
      // 154f: fadd
      // 1550: fstore 25
      // 1552: iload 19
      // 1554: ifeq 155d
      // 1557: ldc_w -1.0
      // 155a: goto 155e
      // 155d: fconst_1
      // 155e: fstore 26
      // 1560: invokestatic java/lang/System.currentTimeMillis ()J
      // 1563: l2d
      // 1564: aload 0
      // 1565: getfield ru/white/module/impl/render/yk.bublikSpeed Lq2/z/SliderSetting;
      // 1568: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 156b: invokevirtual java/lang/Float.doubleValue ()D
      // 156e: ldc2_w 0.55
      // 1571: dmul
      // 1572: ddiv
      // 1573: dstore 27
      // 1575: aload 0
      // 1576: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1579: invokevirtual net/minecraft/entity/LivingEntity.method_17681 ()F
      // 157c: ldc_w 0.15
      // 157f: fsub
      // 1580: fstore 29
      // 1582: ldc_w 80.0
      // 1585: fload 3
      // 1586: fmul
      // 1587: f2i
      // 1588: istore 30
      // 158a: bipush 2
      // 158b: istore 31
      // 158d: iload 31
      // 158f: iflt 1714
      // 1592: fload 3
      // 1593: fconst_1
      // 1594: iload 31
      // 1596: i2f
      // 1597: ldc_w 0.28
      // 159a: fmul
      // 159b: fsub
      // 159c: fmul
      // 159d: fstore 32
      // 159f: fload 25
      // 15a1: fload 26
      // 15a3: iload 31
      // 15a5: i2f
      // 15a6: fmul
      // 15a7: ldc_w 0.075
      // 15aa: fmul
      // 15ab: fadd
      // 15EventMotion: fstore 33
      // 15ae: fload 29
      // 15b0: iload 31
      // 15b2: i2f
      // 15b3: ldc_w 0.025
      // 15b6: fmul
      // 15b7: fadd
      // 15b8: fstore 34
      // 15ba: bipush 0
      // 15DraggableWidget: istore 35
      // 15bd: iload 35
      // 15bf: iload 30
      // 15c1: if_icmpge 170e
      // 15c4: iload 35
      // 15c6: i2f
      // 15c7: iload 30
      // 15c9: i2f
      // 15ConfigFile: fdiv
      // 15cb: fstore 36
      // 15cd: fload 36
      // 15cf: ldc_w 6.2831855
      // 15d2: fmul
      // 15d3: f2d
      // 15d4: dload 27
      // 15d6: dadd
      // 15d7: iload 31
      // 15d9: i2d
      // 15ConfigManager: ldc2_w 0.18
      // 15dd: dmul
      // 15de: dadd
      // 15df: dstore 37
      // 15e1: dload 37
      // 15e3: invokestatic java/lang/Math.cos (D)D
      // 15e6: fload 34
      // 15e8: f2d
      // 15e9: dmul
      // 15ea: dstore 39
      // 15EventAttack: dload 37
      // 15ee: invokestatic java/lang/Math.sin (D)D
      // 15f1: fload 34
      // 15f3: f2d
      // 15f4: dmul
      // 15f5: dstore 41
      // 15f7: ldc_w 0.2
      // 15fa: iload 31
      // 15EventMove: i2f
      // 15fd: ldc_w 0.018
      // 1600: fmul
      // 1601: fadd
      // 1602: aload 0
      // 1603: getfield ru/white/module/impl/render/yk.bublikSize Lq2/z/SliderSetting;
      // 1606: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 1609: invokevirtual java/lang/Float.floatValue ()F
      // 160c: fmul
      // 160d: fstore 43
      // 160f: iload 31
      // 1611: ifne 161e
      // 1614: ldc_w 1062201707
      // 1617: ldc_w -1062201452
      // 161a: iadd
      // 161b: goto 1625
      // 161e: ldc_w -1286892025
      // 1621: ldc_w 1286892130
      // 1624: iadd
      // 1625: i2f
      // 1626: fload 32
      // 1628: fmul
      // 1629: f2i
      // 162a: istore 44
      // 162c: iload 31
      // 162e: ifeq 1634
      // 1631: goto 1708
      // 1634: aload 12
      // 1636: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 1639: aload 12
      // 163b: aload 13
      // 163d: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1640: aload 14
      // 1642: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1645: dsub
      // 1646: dload 39
      // 1648: dadd
      // 1649: aload 13
      // 164b: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 164e: aload 14
      // 1650: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1653: dsub
      // 1654: fload 33
      // 1656: f2d
      // 1657: dadd
      // 1658: aload 13
      // 165a: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 165d: aload 14
      // 165f: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1662: dsub
      // 1663: dload 41
      // 1665: dadd
      // 1666: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1669: aload 12
      // 166b: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 166e: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 1671: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 1674: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 1677: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 167a: fneg
      // 167b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1680: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1683: aload 12
      // 1685: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 1688: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 168b: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 168e: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 1691: invokevirtual net/minecraft/client/render/Camera.method_19329 ()F
      // 1694: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 1699: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 169c: aload 12
      // 169e: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 16a1: dload 37
      // 16a3: dneg
      // 16a4: invokestatic java/lang/Math.toDegrees (D)D
      // 16a7: d2f
      // 16a8: dload 27
      // 16aa: ldc2_w 80.0
      // 16ad: dmul
      // 16ae: d2f
      // 16af: fadd
      // 16b0: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 16b5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 16b8: aload 12
      // 16ba: fload 43
      // 16bc: ldc_w 1.25
      // 16bf: fmul
      // 16c0: fload 43
      // 16c2: ldc_w 1.25
      // 16c5: fmul
      // 16c6: fload 43
      // 16c8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 16cb: aload 4
      // 16cd: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 16d0: ldc_w "client"
      // 16d3: ldc_w "RuN/ZvKB7/Q3GnR7GLiMzskiElQHrp0/qUsW73fdui4="
      // 16d6: ldc_w 501841699
      // 16d9: ldc_w -1972304018
      // 16EventKey: iadd
      // 16dd: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 16e0: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 16e3: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 16e8: checkcast net/minecraft/client/render/RenderLayer
      // 16eb: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 16ee: aload 12
      // 16f0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 16f3: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 16f6: iload 8
      // 16f8: iload 9
      // 16fa: iload 10
      // 16EventMove: iload 11
      // 16fe: iload 44
      // 1700: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 1703: aload 12
      // 1705: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 1708: iinc 35 1
      // 170b: goto 15bd
      // 170e: iinc 31 -1
      // 1711: goto 158d
      // 1714: fload 3
      // 1715: ldc_w 0.001
      // 1718: fcmpl
      // 1719: ifle 1a43
      // 171c: aload 0
      // 171d: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1720: ifnull 1a43
      // 1723: aload 0
      // 1724: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 1727: ldc_w "Кольцо"
      // 172a: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 172d: ifeq 1a43
      // 1730: aload 0
      // 1731: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1734: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 1737: istore 5
      // 1739: iload 5
      // 173b: i2d
      // 173c: ldc2_w 0.3141592653589793
      // 173f: dmul
      // 1740: invokestatic java/lang/Math.sin (D)D
      // 1743: d2f
      // 1744: fstore 6
      // 1746: ldc_w -384241763
      // 1749: ldc_w 384242018
      // 174c: iadd
      // 174d: ldc_w 381114140
      // 1750: ldc_w -381114040
      // 1753: iadd
      // 1754: ldc_w -1175311572
      // 1757: ldc_w 1175311672
      // 175a: iadd
      // 175b: ldc_w 255.0
      // 175e: fload 3
      // 175f: fmul
      // 1760: f2i
      // 1761: invokestatic q4/z/up.getColor (IIII)I
      // 1764: istore 7
      // 1766: aload 0
      // 1767: getfield ru/white/module/impl/render/yk.ringSpeed Lq2/z/SliderSetting;
      // 176a: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 176d: invokevirtual java/lang/Float.doubleValue ()D
      // 1770: dstore 8
      // 1772: invokestatic java/lang/System.currentTimeMillis ()J
      // 1775: l2d
      // 1776: dload 8
      // 1778: drem
      // 1779: dstore 10
      // 177b: dload 10
      // 177d: dload 8
      // 177f: ldc2_w 2.0
      // 1782: ddiv
      // 1783: dcmpl
      // 1784: ifle 178b
      // 1787: bipush 1
      // 1788: goto 178c
      // 178b: bipush 0
      // 178c: istore 12
      // 178e: dload 10
      // 1790: dload 8
      // 1792: ldc2_w 2.0
      // 1795: ddiv
      // 1796: ddiv
      // 1797: dstore 13
      // 1799: iload 12
      // 179b: ifeq 17a5
      // 179e: dload 13
      // 17a0: dconst_1
      // 17a1: dsub
      // 17a2: goto 17a9
      // 17a5: dconst_1
      // 17a6: dload 13
      // 17a8: dsub
      // 17a9: dstore 13
      // 17ab: dload 13
      // 17ad: ldc2_w 0.5
      // 17b0: dcmpg
      // 17b1: ifge 17c0
      // 17b4: ldc2_w 2.0
      // 17b7: dload 13
      // 17b9: dmul
      // 17ba: dload 13
      // 17bc: dmul
      // 17bd: goto 17d6
      // 17c0: dconst_1
      // 17c1: ldc2_w -2.0
      // 17c4: dload 13
      // 17c6: dmul
      // 17c7: ldc2_w 2.0
      // 17ConfigFile: dadd
      // 17cb: ldc2_w 2.0
      // 17ce: invokestatic java/lang/Math.pow (DD)D
      // 17d1: ldc2_w 2.0
      // 17d4: ddiv
      // 17d5: dsub
      // 17d6: dstore 15
      // 17d8: aload 0
      // 17d9: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 17EventKey: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 17df: fstore 17
      // 17e1: fload 17
      // 17e3: f2d
      // 17e4: ldc2_w 1.7
      // 17e7: ddiv
      // 17e8: dload 15
      // 17ea: ldc2_w 0.5
      // 17ed: dcmpl
      // 17ee: ifle 17f8
      // 17f1: dconst_1
      // 17f2: dload 15
      // 17f4: dsub
      // 17f5: goto 17fa
      // 17f8: dload 15
      // 17fa: dmul
      // 17fb: iload 12
      // 17fd: ifeq 1804
      // 1800: bipush -1
      // 1801: goto 1805
      // 1804: bipush 1
      // 1805: i2d
      // 1806: dmul
      // 1807: ldc2_w 0.7
      // 180a: ddiv
      // 180b: dstore 18
      // 180d: aload 1
      // 180e: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 1811: astore 20
      // 1813: aload 0
      // 1814: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1817: aload 1
      // 1818: invokevirtual x9/sb.getTickDelta ()F
      // 181b: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 181e: astore 21
      // 1820: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 1823: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 1826: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 1829: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 182c: astore 22
      // 182e: aload 20
      // 1830: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 1833: aload 20
      // 1835: aload 21
      // 1837: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 183a: aload 22
      // 183c: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 183f: dsub
      // 1840: aload 21
      // 1842: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1845: aload 22
      // 1847: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 184a: dsub
      // 184b: aload 21
      // 184d: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1850: aload 22
      // 1852: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1855: dsub
      // 1856: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1859: aload 20
      // 185b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 185e: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 1861: astore 23
      // 1863: aload 0
      // 1864: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1867: invokevirtual net/minecraft/entity/LivingEntity.method_17681 ()F
      // 186a: ldc_w 0.1
      // 186d: fsub
      // 186e: ldc_w 0.35
      // 1871: fadd
      // 1872: ldc_w 0.35
      // 1875: fload 3
      // 1876: fmul
      // 1877: fsub
      // 1878: fstore 24
      // 187a: fload 17
      // 187c: f2d
      // 187d: dload 15
      // 187f: dmul
      // 1880: d2f
      // 1881: fstore 25
      // 1883: fload 17
      // 1885: f2d
      // 1886: dload 15
      // 1888: dmul
      // 1889: dload 18
      // 188b: dadd
      // 188c: d2f
      // 188d: fstore 26
      // 188f: aload 4
      // 1891: getstatic ru/white/module/impl/render/yk.RING_FILL_LAYER Lnet/minecraft/client/render/RenderLayer;
      // 1894: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 1897: astore 27
      // 1899: bipush 0
      // 189a: istore 28
      // 189c: iload 28
      // 189e: ldc_w 1007831113
      // 18a1: ldc_w -1007830753
      // 18a4: iadd
      // 18a5: if_icmpge 1990
      // 18a8: iload 28
      // 18aa: i2d
      // 18ab: invokestatic java/lang/Math.toRadians (D)D
      // 18ae: d2f
      // 18af: fstore 29
      // 18b1: iload 28
      // 18b3: bipush 1
      // 18b4: iadd
      // 18b5: i2d
      // 18b6: invokestatic java/lang/Math.toRadians (D)D
      // 18b9: d2f
      // 18ba: fstore 30
      // 18bc: iload 28
      // 18be: invokestatic q4/z/up.fade (I)I
      // 18c1: fload 3
      // 18c2: invokestatic q4/z/up.multAlpha (IF)I
      // 18c5: iload 7
      // 18c7: fload 6
      // 18c9: invokestatic q4/z/up.overCol (IIF)I
      // 18EventPacket: istore 31
      // 18ce: iload 28
      // 18d0: invokestatic q4/z/up.fade (I)I
      // 18d3: fload 3
      // 18d4: invokestatic q4/z/up.multAlpha (IF)I
      // 18d7: iload 7
      // 18d9: fload 6
      // 18db: invokestatic q4/z/up.overCol (IIF)I
      // 18de: istore 32
      // 18e0: fload 29
      // 18e2: f2d
      // 18e3: invokestatic java/lang/Math.cos (D)D
      // 18e6: fload 24
      // 18e8: f2d
      // 18e9: dmul
      // 18ea: d2f
      // 18eb: fstore 33
      // 18ed: fload 29
      // 18ef: f2d
      // 18f0: invokestatic java/lang/Math.sin (D)D
      // 18f3: fload 24
      // 18f5: f2d
      // 18f6: dmul
      // 18f7: d2f
      // 18f8: fstore 34
      // 18fa: fload 30
      // 18EventMove: f2d
      // 18fd: invokestatic java/lang/Math.cos (D)D
      // 1900: fload 24
      // 1902: f2d
      // 1903: dmul
      // 1904: d2f
      // 1905: fstore 35
      // 1907: fload 30
      // 1909: f2d
      // 190a: invokestatic java/lang/Math.sin (D)D
      // 190d: fload 24
      // 190f: f2d
      // 1910: dmul
      // 1911: d2f
      // 1912: fstore 36
      // 1914: aload 27
      // 1916: aload 23
      // 1918: fload 33
      // 191a: fload 25
      // 191c: fload 34
      // 191e: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 1923: iload 31
      // 1925: ldc_w 120.0
      // 1928: fload 3
      // 1929: fmul
      // 192a: f2i
      // 192b: invokestatic q4/z/up.replAlpha (II)I
      // 192e: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 1933: pop
      // 1934: aload 27
      // 1936: aload 23
      // 1938: fload 35
      // 193a: fload 25
      // 193c: fload 36
      // 193e: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 1943: iload 32
      // 1945: ldc_w 120.0
      // 1948: fload 3
      // 1949: fmul
      // 194a: f2i
      // 194b: invokestatic q4/z/up.replAlpha (II)I
      // 194e: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 1953: pop
      // 1954: aload 27
      // 1956: aload 23
      // 1958: fload 35
      // 195a: fload 26
      // 195c: fload 36
      // 195e: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 1963: iload 32
      // 1965: bipush 0
      // 1966: invokestatic q4/z/up.replAlpha (II)I
      // 1969: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 196e: pop
      // 196f: aload 27
      // 1971: aload 23
      // 1973: fload 33
      // 1975: fload 26
      // 1977: fload 34
      // 1979: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 197e: iload 31
      // 1980: bipush 0
      // 1981: invokestatic q4/z/up.replAlpha (II)I
      // 1984: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 1989: pop
      // 198a: iinc 28 1
      // 198d: goto 189c
      // 1990: aload 4
      // 1992: getstatic ru/white/module/impl/render/yk.RING_LINE_LAYER Lnet/minecraft/client/render/RenderLayer;
      // 1995: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 1998: astore 28
      // 199a: bipush 0
      // 199b: istore 29
      // 199d: iload 29
      // 199f: ldc_w 986267660
      // 19a2: ldc_w -986267300
      // 19a5: iadd
      // 19a6: if_icmpge 1a3e
      // 19a9: iload 29
      // 19ab: i2d
      // 19EventMotion: invokestatic java/lang/Math.toRadians (D)D
      // 19af: d2f
      // 19b0: fstore 30
      // 19b2: iload 29
      // 19b4: bipush 1
      // 19b5: iadd
      // 19b6: i2d
      // 19b7: invokestatic java/lang/Math.toRadians (D)D
      // 19ba: d2f
      // 19DraggableWidget: fstore 31
      // 19bd: bipush 1
      // 19be: invokestatic q4/z/up.fade (I)I
      // 19c1: ldc_w 0.7
      // 19c4: invokestatic q4/z/up.multBright (IF)I
      // 19c7: iload 7
      // 19c9: fload 6
      // 19cb: invokestatic q4/z/up.overCol (IIF)I
      // 19ce: fload 3
      // 19cf: invokestatic q4/z/up.multAlpha (IF)I
      // 19d2: istore 32
      // 19d4: aload 28
      // 19d6: aload 23
      // 19d8: fload 30
      // 19ConfigManager: f2d
      // 19db: invokestatic java/lang/Math.cos (D)D
      // 19de: fload 24
      // 19e0: f2d
      // 19e1: dmul
      // 19e2: d2f
      // 19e3: fload 25
      // 19e5: fload 30
      // 19e7: f2d
      // 19e8: invokestatic java/lang/Math.sin (D)D
      // 19eb: fload 24
      // 19ed: f2d
      // 19ee: dmul
      // 19ef: d2f
      // 19f0: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 19f5: iload 32
      // 19f7: ldc_w 150.0
      // 19fa: fload 3
      // 19fb: fmul
      // 19EventMove: f2i
      // 19fd: invokestatic q4/z/up.replAlpha (II)I
      // 1a00: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 1a05: pop
      // 1a06: aload 28
      // 1a08: aload 23
      // 1a0a: fload 31
      // 1a0c: f2d
      // 1a0d: invokestatic java/lang/Math.cos (D)D
      // 1a10: fload 24
      // 1a12: f2d
      // 1a13: dmul
      // 1a14: d2f
      // 1a15: fload 25
      // 1a17: fload 31
      // 1a19: f2d
      // 1a1a: invokestatic java/lang/Math.sin (D)D
      // 1a1d: fload 24
      // 1a1f: f2d
      // 1a20: dmul
      // 1a21: d2f
      // 1a22: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 1a27: iload 32
      // 1a29: ldc_w 150.0
      // 1a2c: fload 3
      // 1a2d: fmul
      // 1a2e: f2i
      // 1a2f: invokestatic q4/z/up.replAlpha (II)I
      // 1a32: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 1a37: pop
      // 1a38: iinc 29 1
      // 1a3b: goto 199d
      // 1a3e: aload 20
      // 1a40: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 1a43: fload 3
      // 1a44: ldc_w 0.001
      // 1a47: fcmpl
      // 1a48: ifle 1eb5
      // 1a4b: aload 0
      // 1a4c: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1a4f: ifnull 1eb5
      // 1a52: aload 0
      // 1a53: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 1a56: ldc_w "Кубики"
      // 1a59: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 1a5c: ifeq 1eb5
      // 1a5f: aload 0
      // 1a60: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1a63: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 1a66: istore 5
      // 1a68: iload 5
      // 1a6a: i2d
      // 1a6b: ldc2_w 0.3141592653589793
      // 1a6e: dmul
      // 1a6f: invokestatic java/lang/Math.sin (D)D
      // 1a72: d2f
      // 1a73: fstore 6
      // 1a75: aload 0
      // 1a76: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1a79: invokevirtual ru/white/utils/animation/Animation.update ()Z
      // 1a7c: pop
      // 1a7d: aload 0
      // 1a7e: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1a81: fload 6
      // 1a83: f2d
      // 1a84: ldc2_w 0.10000000149011612
      // 1a87: getstatic ru/white/utils/animation/ap.SINE_OUT Lru/white/utils/animation/zo;
      // 1a8a: invokevirtual ru/white/utils/animation/Animation.run (DDLru/white/utils/animation/zo;)Lru/white/utils/animation/Animation;
      // 1a8d: pop
      // 1a8e: ldc_w 500004349
      // 1a91: ldc_w -500004094
      // 1a94: iadd
      // 1a95: ldc_w 621723952
      // 1a98: ldc_w -621723852
      // 1a9b: iadd
      // 1a9c: ldc_w 1463188415
      // 1a9f: ldc_w -1463188315
      // 1aa2: iadd
      // 1aa3: ldc_w 255.0
      // 1aa6: fload 3
      // 1aa7: fmul
      // 1aa8: f2i
      // 1aa9: invokestatic q4/z/up.getColor (IIII)I
      // 1aac: istore 7
      // 1aae: bipush 1
      // 1aaf: invokestatic q4/z/up.fade (I)I
      // 1ab2: fload 3
      // 1ab3: invokestatic q4/z/up.multAlpha (IF)I
      // 1ab6: iload 7
      // 1ab8: aload 0
      // 1ab9: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1abc: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 1abf: invokestatic q4/z/up.overCol (IIF)I
      // 1ac2: istore 8
      // 1ac4: aload 1
      // 1ac5: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 1ac8: astore 9
      // 1aca: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 1acd: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 1ad0: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 1ad3: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 1ad6: astore 10
      // 1ad8: aload 0
      // 1ad9: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1adc: aload 1
      // 1add: invokevirtual x9/sb.getTickDelta ()F
      // 1ae0: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 1ae3: astore 11
      // 1ae5: invokestatic java/lang/System.currentTimeMillis ()J
      // 1ae8: l2d
      // 1ae9: ldc2_w 2000.0
      // 1aec: ddiv
      // 1aed: invokestatic java/lang/Math.cos (D)D
      // 1af0: d2f
      // 1af1: ldc_w 360.0
      // 1af4: fmul
      // 1af5: aload 0
      // 1af6: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1af9: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 1afc: ldc_w 20.0
      // 1aff: fmul
      // 1b00: fadd
      // 1b01: fstore 12
      // 1b03: ldc_w -627267547
      // 1b06: ldc_w 627267559
      // 1b09: iadd
      // 1b0a: istore 13
      // 1b0c: aload 0
      // 1b0d: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1b10: invokevirtual net/minecraft/entity/LivingEntity.method_17681 ()F
      // 1b13: ldc_w 1.5
      // 1b16: fmul
      // 1b17: fstore 14
      // 1b19: fconst_1
      // 1b1a: ldc_w 0.3
      // 1b1d: aload 0
      // 1b1e: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1b21: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 1b24: fmul
      // 1b25: fsub
      // 1b26: fload 3
      // 1b27: fmul
      // 1b28: fstore 15
      // 1b2a: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 1b2d: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 1b30: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 1b33: astore 16
      // 1b35: bipush 0
      // 1b36: istore 17
      // 1b38: iload 17
      // 1b3a: ldc_w -1868963995
      // 1b3d: ldc_w 1868964355
      // 1b40: iadd
      // 1b41: if_icmpge 1c53
      // 1b44: ldc_w 0.70000005
      // 1b47: fstore 18
      // 1b49: iload 17
      // 1b4b: i2f
      // 1b4c: fload 12
      // 1b4e: fadd
      // 1b4f: f2d
      // 1b50: invokestatic java/lang/Math.toRadians (D)D
      // 1b53: d2f
      // 1b54: f2d
      // 1b55: invokestatic java/lang/Math.sin (D)D
      // 1b58: fload 14
      // 1b5a: f2d
      // 1b5b: dmul
      // 1b5c: fload 18
      // 1b5e: f2d
      // 1b5f: dmul
      // 1b60: d2f
      // 1b61: fstore 19
      // 1b63: iload 17
      // 1b65: i2f
      // 1b66: fload 12
      // 1b68: fadd
      // 1b69: f2d
      // 1b6a: invokestatic java/lang/Math.toRadians (D)D
      // 1b6d: d2f
      // 1b6e: f2d
      // 1b6f: invokestatic java/lang/Math.cos (D)D
      // 1b72: fload 14
      // 1b74: f2d
      // 1b75: dmul
      // 1b76: fload 18
      // 1b78: f2d
      // 1b79: dmul
      // 1b7a: d2f
      // 1b7b: fstore 20
      // 1b7d: aload 11
      // 1b7f: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1b82: fload 19
      // 1b84: f2d
      // 1b85: dadd
      // 1b86: dstore 21
      // 1b88: aload 11
      // 1b8a: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1b8d: fload 20
      // 1b8f: f2d
      // 1b90: dadd
      // 1b91: dstore 23
      // 1b93: aload 11
      // 1b95: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1b98: aload 0
      // 1b99: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1b9c: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 1b9f: f2d
      // 1ba0: iload 17
      // 1ba2: i2d
      // 1ba3: invokestatic x30/fq.sin (D)D
      // 1ba6: invokestatic java/lang/Math.abs (D)D
      // 1ba9: dmul
      // 1baa: dadd
      // 1bab: dstore 25
      // 1bad: aload 9
      // 1baf: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 1bb2: aload 9
      // 1bb4: dload 21
      // 1bb6: aload 10
      // 1bb8: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1bbb: dsub
      // 1bbc: dload 25
      // 1bbe: aload 10
      // 1bc0: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1bc3: dsub
      // 1bc4: dload 23
      // 1bc6: aload 10
      // 1bc8: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1bcb: dsub
      // 1bcc: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1bcf: aload 9
      // 1bd1: aload 16
      // 1bd3: invokevirtual net/minecraft/client/render/Camera.method_23767 ()Lorg/joml/Quaternionf;
      // 1bd6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1bd9: ldc_w 0.6
      // 1bdc: fload 15
      // 1bde: fmul
      // 1bdf: fstore 27
      // 1be1: aload 9
      // 1be3: fload 27
      // 1be5: fload 27
      // 1be7: fload 27
      // 1be9: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 1bec: aload 4
      // 1bee: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 1bf1: ldc_w "client"
      // 1bf4: ldc_w "Fz5jTaNc899mx2hQSWWQ5Zj/Dn9Wc4EU+JYKxiYApgU="
      // 1bf7: ldc_w -1845661770
      // 1bfa: ldc_w -246879222
      // 1bfd: iadd
      // 1bfe: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 1c01: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 1c04: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 1c09: checkcast net/minecraft/client/render/RenderLayer
      // 1c0c: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 1c0f: aload 9
      // 1c11: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 1c14: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 1c17: iload 8
      // 1c19: ldc_w 0.3
      // 1c1c: invokestatic q4/z/up.multAlpha (IF)I
      // 1c1f: iload 8
      // 1c21: ldc_w 0.3
      // 1c24: invokestatic q4/z/up.multAlpha (IF)I
      // 1c27: iload 8
      // 1c29: ldc_w 0.3
      // 1c2c: invokestatic q4/z/up.multAlpha (IF)I
      // 1c2f: iload 8
      // 1c31: ldc_w 0.3
      // 1c34: invokestatic q4/z/up.multAlpha (IF)I
      // 1c37: fload 3
      // 1c38: ldc_w 0.35
      // 1c3b: fmul
      // 1c3c: ldc_w 255.0
      // 1c3f: fmul
      // 1c40: f2i
      // 1c41: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 1c44: aload 9
      // 1c46: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 1c49: iload 17
      // 1c4b: iload 13
      // 1c4d: iadd
      // 1c4e: istore 17
      // 1c50: goto 1b38
      // 1c53: bipush 0
      // 1c54: istore 17
      // 1c56: iload 17
      // 1c58: ldc_w 421977562
      // 1c5b: ldc_w -421977202
      // 1c5e: iadd
      // 1c5f: if_icmpge 1d86
      // 1c62: ldc_w 0.70000005
      // 1c65: fstore 18
      // 1c67: iload 17
      // 1c69: i2f
      // 1c6a: fload 12
      // 1c6c: fadd
      // 1c6d: f2d
      // 1c6e: invokestatic java/lang/Math.toRadians (D)D
      // 1c71: d2f
      // 1c72: f2d
      // 1c73: invokestatic java/lang/Math.sin (D)D
      // 1c76: fload 14
      // 1c78: f2d
      // 1c79: dmul
      // 1c7a: fload 18
      // 1c7c: f2d
      // 1c7d: dmul
      // 1c7e: d2f
      // 1c7f: fstore 19
      // 1c81: iload 17
      // 1c83: i2f
      // 1c84: fload 12
      // 1c86: fadd
      // 1c87: f2d
      // 1c88: invokestatic java/lang/Math.toRadians (D)D
      // 1c8b: d2f
      // 1c8c: f2d
      // 1c8d: invokestatic java/lang/Math.cos (D)D
      // 1c90: fload 14
      // 1c92: f2d
      // 1c93: dmul
      // 1c94: fload 18
      // 1c96: f2d
      // 1c97: dmul
      // 1c98: d2f
      // 1c99: fstore 20
      // 1c9b: aload 11
      // 1c9d: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1ca0: fload 19
      // 1ca2: f2d
      // 1ca3: dadd
      // 1ca4: dstore 21
      // 1ca6: aload 11
      // 1ca8: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1cab: fload 20
      // 1cad: f2d
      // 1cae: dadd
      // 1caf: dstore 23
      // 1cb1: aload 11
      // 1cb3: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1cb6: aload 0
      // 1cb7: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1cba: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 1cbd: f2d
      // 1cbe: iload 17
      // 1cc0: i2d
      // 1cc1: invokestatic x30/fq.sin (D)D
      // 1cc4: invokestatic java/lang/Math.abs (D)D
      // 1cc7: dmul
      // 1cc8: dadd
      // 1cc9: dstore 25
      // 1ccb: new net/minecraft/util/math/Vec3d
      // 1cce: dup
      // 1ccf: dload 21
      // 1cd1: dload 25
      // 1cd3: dload 23
      // 1cd5: invokespecial net/minecraft/util/math/Vec3d.<init> (DDD)V
      // 1cd8: astore 27
      // 1cda: new org/joml/Vector3f
      // 1cdd: dup
      // 1cde: aload 11
      // 1ce0: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1ce3: aload 27
      // 1ce5: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1ce8: dsub
      // 1ce9: d2f
      // 1cea: aload 11
      // 1cec: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1cef: aload 27
      // 1cf1: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1cf4: dsub
      // 1cf5: d2f
      // 1cf6: aload 11
      // 1cf8: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1cfb: aload 27
      // 1cfd: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1d00: dsub
      // 1d01: d2f
      // 1d02: invokespecial org/joml/Vector3f.<init> (FFF)V
      // 1d05: invokevirtual org/joml/Vector3f.normalize ()Lorg/joml/Vector3f;
      // 1d08: astore 28
      // 1d0a: aload 9
      // 1d0c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 1d0f: aload 9
      // 1d11: dload 21
      // 1d13: aload 10
      // 1d15: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1d18: dsub
      // 1d19: dload 25
      // 1d1b: aload 10
      // 1d1d: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1d20: dsub
      // 1d21: dload 23
      // 1d23: aload 10
      // 1d25: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1d28: dsub
      // 1d29: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1d2c: aload 9
      // 1d2e: new org/joml/Quaternionf
      // 1d31: dup
      // 1d32: invokespecial org/joml/Quaternionf.<init> ()V
      // 1d35: new org/joml/Vector3f
      // 1d38: dup
      // 1d39: fconst_0
      // 1d3a: fconst_1
      // 1d3b: fconst_0
      // 1d3c: invokespecial org/joml/Vector3f.<init> (FFF)V
      // 1d3f: aload 28
      // 1d41: invokevirtual org/joml/Quaternionf.rotationTo (Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;)Lorg/joml/Quaternionf;
      // 1d44: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1d47: aload 9
      // 1d49: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 1d4c: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 1d4f: astore 29
      // 1d51: ldc_w 0.06
      // 1d54: fload 15
      // 1d56: fmul
      // 1d57: fstore 30
      // 1d59: aload 4
      // 1d5b: getstatic ru/white/module/impl/render/yk.RING_FILL_LAYER Lnet/minecraft/client/render/RenderLayer;
      // 1d5e: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 1d61: aload 29
      // 1d63: fload 30
      // 1d65: iload 8
      // 1d67: fload 3
      // 1d68: ldc_w 0.2
      // 1d6b: fmul
      // 1d6c: ldc_w 255.0
      // 1d6f: fmul
      // 1d70: f2i
      // 1d71: invokestatic q4/z/up.replAlpha (II)I
      // 1d74: invokestatic ru/white/module/impl/render/yk.m882 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;FI)V
      // 1d77: aload 9
      // 1d79: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 1d7c: iload 17
      // 1d7e: iload 13
      // 1d80: iadd
      // 1d81: istore 17
      // 1d83: goto 1c56
      // 1d86: bipush 0
      // 1d87: istore 17
      // 1d89: iload 17
      // 1d8b: ldc_w 814521998
      // 1d8e: ldc_w -814521638
      // 1d91: iadd
      // 1d92: if_icmpge 1eb5
      // 1d95: ldc_w 0.70000005
      // 1d98: fstore 18
      // 1d9a: iload 17
      // 1d9c: i2f
      // 1d9d: fload 12
      // 1d9f: fadd
      // 1da0: f2d
      // 1da1: invokestatic java/lang/Math.toRadians (D)D
      // 1da4: d2f
      // 1da5: f2d
      // 1da6: invokestatic java/lang/Math.sin (D)D
      // 1da9: fload 14
      // 1dab: f2d
      // 1dac: dmul
      // 1dad: fload 18
      // 1daf: f2d
      // 1db0: dmul
      // 1db1: d2f
      // 1db2: fstore 19
      // 1db4: iload 17
      // 1db6: i2f
      // 1db7: fload 12
      // 1db9: fadd
      // 1dba: f2d
      // 1dbb: invokestatic java/lang/Math.toRadians (D)D
      // 1dbe: d2f
      // 1dbf: f2d
      // 1dc0: invokestatic java/lang/Math.cos (D)D
      // 1dc3: fload 14
      // 1dc5: f2d
      // 1dc6: dmul
      // 1dc7: fload 18
      // 1dc9: f2d
      // 1dca: dmul
      // 1dcb: d2f
      // 1dcc: fstore 20
      // 1dce: aload 11
      // 1dd0: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1dd3: fload 19
      // 1dd5: f2d
      // 1dd6: dadd
      // 1dd7: dstore 21
      // 1dd9: aload 11
      // 1ddb: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1dde: fload 20
      // 1de0: f2d
      // 1de1: dadd
      // 1de2: dstore 23
      // 1de4: aload 11
      // 1de6: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1de9: aload 0
      // 1dea: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1ded: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 1df0: f2d
      // 1df1: iload 17
      // 1df3: i2d
      // 1df4: invokestatic x30/fq.sin (D)D
      // 1df7: invokestatic java/lang/Math.abs (D)D
      // 1dfa: dmul
      // 1dfb: dadd
      // 1dfc: dstore 25
      // 1dfe: new net/minecraft/util/math/Vec3d
      // 1e01: dup
      // 1e02: dload 21
      // 1e04: dload 25
      // 1e06: dload 23
      // 1e08: invokespecial net/minecraft/util/math/Vec3d.<init> (DDD)V
      // 1e0b: astore 27
      // 1e0d: new org/joml/Vector3f
      // 1e10: dup
      // 1e11: aload 11
      // 1e13: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1e16: aload 27
      // 1e18: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1e1b: dsub
      // 1e1c: d2f
      // 1e1d: aload 11
      // 1e1f: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1e22: aload 27
      // 1e24: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1e27: dsub
      // 1e28: d2f
      // 1e29: aload 11
      // 1e2b: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1e2e: aload 27
      // 1e30: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1e33: dsub
      // 1e34: d2f
      // 1e35: invokespecial org/joml/Vector3f.<init> (FFF)V
      // 1e38: invokevirtual org/joml/Vector3f.normalize ()Lorg/joml/Vector3f;
      // 1e3b: astore 28
      // 1e3d: aload 9
      // 1e3f: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 1e42: aload 9
      // 1e44: dload 21
      // 1e46: aload 10
      // 1e48: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1e4b: dsub
      // 1e4c: dload 25
      // 1e4e: aload 10
      // 1e50: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1e53: dsub
      // 1e54: dload 23
      // 1e56: aload 10
      // 1e58: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1e5b: dsub
      // 1e5c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 1e5f: aload 9
      // 1e61: new org/joml/Quaternionf
      // 1e64: dup
      // 1e65: invokespecial org/joml/Quaternionf.<init> ()V
      // 1e68: new org/joml/Vector3f
      // 1e6b: dup
      // 1e6c: fconst_0
      // 1e6d: fconst_1
      // 1e6e: fconst_0
      // 1e6f: invokespecial org/joml/Vector3f.<init> (FFF)V
      // 1e72: aload 28
      // 1e74: invokevirtual org/joml/Quaternionf.rotationTo (Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;)Lorg/joml/Quaternionf;
      // 1e77: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 1e7a: aload 9
      // 1e7c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 1e7f: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 1e82: astore 29
      // 1e84: ldc_w 0.06
      // 1e87: fload 15
      // 1e89: fmul
      // 1e8a: fstore 30
      // 1e8c: aload 4
      // 1e8e: getstatic ru/white/module/impl/render/yk.RING_LINE_LAYER Lnet/minecraft/client/render/RenderLayer;
      // 1e91: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 1e94: aload 29
      // 1e96: fload 30
      // 1e98: iload 8
      // 1e9a: fload 3
      // 1e9b: ldc_w 255.0
      // 1e9e: fmul
      // 1e9f: f2i
      // 1ea0: invokestatic q4/z/up.replAlpha (II)I
      // 1ea3: invokestatic ru/white/module/impl/render/yk.m883 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;FI)V
      // 1ea6: aload 9
      // 1ea8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 1eab: iload 17
      // 1ead: iload 13
      // 1eaf: iadd
      // 1eb0: istore 17
      // 1eb2: goto 1d89
      // 1eb5: fload 3
      // 1eb6: ldc_w 0.001
      // 1eb9: fcmpl
      // 1eba: ifle 2bfb
      // 1ebd: aload 0
      // 1ebe: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1ec1: ifnull 2bfb
      // 1ec4: aload 0
      // 1ec5: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 1ec8: ldc "Гироскоп"
      // 1eca: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 1ecd: ifeq 2bfb
      // 1ed0: aload 0
      // 1ed1: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1ed4: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 1ed7: istore 5
      // 1ed9: iload 5
      // 1edb: i2d
      // 1edc: ldc2_w 0.15707963267948966
      // 1edf: dmul
      // 1ee0: invokestatic java/lang/Math.sin (D)D
      // 1ee3: d2f
      // 1ee4: fstore 6
      // 1ee6: aload 0
      // 1ee7: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1eea: invokevirtual ru/white/utils/animation/Animation.update ()Z
      // 1eed: pop
      // 1eee: aload 0
      // 1eef: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1ef2: fload 6
      // 1ef4: f2d
      // 1ef5: ldc2_w 0.11999999731779099
      // 1ef8: getstatic ru/white/utils/animation/ap.SINE_OUT Lru/white/utils/animation/zo;
      // 1efb: invokevirtual ru/white/utils/animation/Animation.run (DDLru/white/utils/animation/zo;)Lru/white/utils/animation/Animation;
      // 1efe: pop
      // 1eff: ldc_w -1931869907
      // 1f02: ldc_w 1931870162
      // 1f05: iadd
      // 1f06: ldc_w 822464339
      // 1f09: ldc_w -822464249
      // 1f0c: iadd
      // 1f0d: ldc_w 527266527
      // 1f10: ldc_w -527266437
      // 1f13: iadd
      // 1f14: ldc_w 255.0
      // 1f17: fload 3
      // 1f18: fmul
      // 1f19: f2i
      // 1f1a: invokestatic q4/z/up.getColor (IIII)I
      // 1f1d: istore 7
      // 1f1f: bipush 0
      // 1f20: invokestatic q4/z/up.fade (I)I
      // 1f23: fload 3
      // 1f24: invokestatic q4/z/up.multAlpha (IF)I
      // 1f27: iload 7
      // 1f29: aload 0
      // 1f2a: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1f2d: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 1f30: invokestatic q4/z/up.overCol (IIF)I
      // 1f33: istore 8
      // 1f35: aload 1
      // 1f36: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 1f39: astore 9
      // 1f3b: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 1f3e: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 1f41: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 1f44: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 1f47: astore 10
      // 1f49: aload 0
      // 1f4a: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1f4d: aload 1
      // 1f4e: invokevirtual x9/sb.getTickDelta ()F
      // 1f51: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 1f54: astore 11
      // 1f56: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 1f59: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 1f5c: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 1f5f: astore 12
      // 1f61: aload 11
      // 1f63: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1f66: aload 10
      // 1f68: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 1f6b: dsub
      // 1f6c: dstore 13
      // 1f6e: aload 11
      // 1f70: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1f73: aload 10
      // 1f75: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 1f78: dsub
      // 1f79: aload 0
      // 1f7a: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1f7d: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 1f80: f2d
      // 1f81: ldc2_w 0.5
      // 1f84: dmul
      // 1f85: dadd
      // 1f86: dstore 15
      // 1f88: aload 11
      // 1f8a: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1f8d: aload 10
      // 1f8f: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 1f92: dsub
      // 1f93: dstore 17
      // 1f95: invokestatic java/lang/System.currentTimeMillis ()J
      // 1f98: lstore 19
      // 1f9a: aload 0
      // 1f9b: getfield ru/white/module/impl/render/yk.gyroSpeed Lq2/z/SliderSetting;
      // 1f9e: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 1fa1: invokevirtual java/lang/Float.doubleValue ()D
      // 1fa4: dstore 21
      // 1fa6: lload 19
      // 1fa8: ldc2_w -1789034046330913901
      // 1fab: ldc2_w 1789034046331913901
      // 1fae: ladd
      // 1faf: lrem
      // 1fb0: l2f
      // 1fb1: ldc_w 1000.0
      // 1fb4: fdiv
      // 1fb5: fstore 23
      // 1fb7: lload 19
      // 1fb9: l2d
      // 1fba: dload 21
      // 1fbc: ddiv
      // 1fbd: ldc2_w 360.0
      // 1fc0: dmul
      // 1fc1: ldc2_w 360.0
      // 1fc4: drem
      // 1fc5: d2f
      // 1fc6: fstore 24
      // 1fc8: fconst_1
      // 1fc9: ldc_w 0.35
      // 1fcc: aload 0
      // 1fcd: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 1fd0: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 1fd3: fmul
      // 1fd4: fsub
      // 1fd5: fstore 25
      // 1fd7: aload 0
      // 1fd8: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 1fdb: invokevirtual net/minecraft/entity/LivingEntity.method_17681 ()F
      // 1fde: ldc_w 1.15
      // 1fe1: fmul
      // 1fe2: ldc_w 0.15
      // 1fe5: fadd
      // 1fe6: aload 0
      // 1fe7: getfield ru/white/module/impl/render/yk.gyroSize Lq2/z/SliderSetting;
      // 1fea: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 1fed: invokevirtual java/lang/Float.floatValue ()F
      // 1ff0: fmul
      // 1ff1: fload 25
      // 1ff3: fmul
      // 1ff4: fstore 26
      // 1ff6: ldc_w 0.045
      // 1ff9: aload 0
      // 1ffa: getfield ru/white/module/impl/render/yk.gyroSize Lq2/z/SliderSetting;
      // 1ffd: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 2000: invokevirtual java/lang/Float.floatValue ()F
      // 2003: fmul
      // 2004: fstore 27
      // 2006: aload 0
      // 2007: getfield ru/white/module/impl/render/yk.gyroCore Lq2/z/BooleanSetting;
      // 200a: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 200d: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 2010: istore 28
      // 2012: bipush 3
      // 2013: anewarray 944
      // 2016: dup
      // 2017: bipush 0
      // 2018: bipush 5
      // 2019: newarray 6
      // 201b: dup
      // 201c: bipush 0
      // 201d: ldc_w 45.0
      // 2020: ldc_w 10.0
      // 2023: fload 23
      // 2025: ldc_w 0.7
      // 2028: fmul
      // 2029: f2d
      // 202a: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 202d: fmul
      // 202e: fadd
      // 202f: fastore
      // 2030: dup
      // 2031: bipush 1
      // 2032: ldc_w 15.0
      // 2035: fload 23
      // 2037: ldc_w 0.5
      // 203a: fmul
      // 203b: f2d
      // 203c: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 203f: fmul
      // 2040: fastore
      // 2041: dup
      // 2042: bipush 2
      // 2043: fconst_1
      // 2044: fastore
      // 2045: dup
      // 2046: bipush 3
      // 2047: fconst_0
      // 2048: fastore
      // 2049: dup
      // 204a: bipush 4
      // 204b: fconst_0
      // 204c: fastore
      // 204d: aastore
      // 204e: dup
      // 204f: bipush 1
      // 2050: bipush 5
      // 2051: newarray 6
      // 2053: dup
      // 2054: bipush 0
      // 2055: ldc_w -20.0
      // 2058: fload 23
      // 205a: ldc_w 0.6
      // 205d: fmul
      // 205e: f2d
      // 205f: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2062: fmul
      // 2063: fastore
      // 2064: dup
      // 2065: bipush 1
      // 2066: ldc_w -50.0
      // 2069: ldc_w 12.0
      // 206c: fload 23
      // 206e: ldc_w 0.8
      // 2071: fmul
      // 2072: f2d
      // 2073: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 2076: fmul
      // 2077: fadd
      // 2078: fastore
      // 2079: dup
      // 207a: bipush 2
      // 207b: ldc_w -0.85
      // 207e: fastore
      // 207f: dup
      // 2080: bipush 3
      // 2081: ldc_w 120.0
      // 2084: fastore
      // 2085: dup
      // 2086: bipush 4
      // 2087: ldc_w 0.33
      // 208a: fastore
      // 208b: aastore
      // 208c: dup
      // 208d: bipush 2
      // 208e: bipush 5
      // 208f: newarray 6
      // 2091: dup
      // 2092: bipush 0
      // 2093: ldc_w -30.0
      // 2096: ldc_w 8.0
      // 2099: fload 23
      // 209b: ldc_w 0.9
      // 209e: fmul
      // 209f: f2d
      // 20a0: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 20a3: fmul
      // 20a4: fadd
      // 20a5: fastore
      // 20a6: dup
      // 20a7: bipush 1
      // 20a8: ldc_w 40.0
      // 20ab: ldc_w 10.0
      // 20ae: fload 23
      // 20b0: ldc_w 0.6
      // 20b3: fmul
      // 20b4: f2d
      // 20b5: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 20b8: fmul
      // 20b9: fadd
      // 20ba: fastore
      // 20DraggableWidget: dup
      // 20bc: bipush 2
      // 20bd: ldc_w 0.65
      // 20c0: fastore
      // 20c1: dup
      // 20c2: bipush 3
      // 20c3: ldc_w 240.0
      // 20c6: fastore
      // 20c7: dup
      // 20c8: bipush 4
      // 20c9: ldc_w 0.66
      // 20EventPacket: fastore
      // 20cd: aastore
      // 20ce: astore 29
      // 20d0: ldc_w 1138035621
      // 20d3: ldc_w -1138035573
      // 20d6: iadd
      // 20d7: istore 30
      // 20d9: aload 4
      // 20db: getstatic ru/white/module/impl/render/yk.RING_FILL_LAYER Lnet/minecraft/client/render/RenderLayer;
      // 20de: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 20e1: astore 31
      // 20e3: fload 26
      // 20e5: fload 27
      // 20e7: fsub
      // 20e8: fstore 32
      // 20ea: fload 26
      // 20EventAttack: fload 27
      // 20ee: fadd
      // 20ef: fstore 33
      // 20f1: bipush 0
      // 20f2: istore 34
      // 20f4: iload 34
      // 20f6: bipush 3
      // 20f7: if_icmpge 22a5
      // 20fa: aload 29
      // 20EventMove: iload 34
      // 20fe: aaload
      // 20Category: bipush 0
      // 2100: faload
      // 2101: fstore 35
      // 2103: aload 29
      // 2105: iload 34
      // 2107: aaload
      // 2108: bipush 1
      // 2109: faload
      // 210a: fstore 36
      // 210c: fload 24
      // 210e: aload 29
      // 2110: iload 34
      // 2112: aaload
      // 2113: bipush 2
      // 2114: faload
      // 2115: fmul
      // 2116: fstore 37
      // 2118: aload 29
      // 211a: iload 34
      // 211c: aaload
      // 211d: bipush 3
      // 211e: faload
      // 211f: fstore 38
      // 2121: aload 9
      // 2123: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 2126: aload 9
      // 2128: dload 13
      // 212a: dload 15
      // 212c: dload 17
      // 212e: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2131: aload 9
      // 2133: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2136: fload 35
      // 2138: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 213d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2140: aload 9
      // 2142: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 2145: fload 36
      // 2147: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 214c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 214f: aload 9
      // 2151: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 2154: fload 37
      // 2156: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 215b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 215e: aload 9
      // 2160: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 2163: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 2166: astore 39
      // 2168: bipush 0
      // 2169: istore 40
      // 216b: iload 40
      // 216d: iload 30
      // 216f: if_icmpge 229a
      // 2172: iload 40
      // 2174: i2d
      // 2175: ldc2_w 3.141592653589793
      // 2178: dmul
      // 2179: ldc2_w 2.0
      // 217c: dmul
      // 217d: iload 30
      // 217f: i2d
      // 2180: ddiv
      // 2181: d2f
      // 2182: fstore 41
      // 2184: iload 40
      // 2186: bipush 1
      // 2187: iadd
      // 2188: i2d
      // 2189: ldc2_w 3.141592653589793
      // 218c: dmul
      // 218d: ldc2_w 2.0
      // 2190: dmul
      // 2191: iload 30
      // 2193: i2d
      // 2194: ddiv
      // 2195: d2f
      // 2196: fstore 42
      // 2198: fload 41
      // 219a: f2d
      // 219b: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 219e: fstore 43
      // 21a0: fload 41
      // 21a2: f2d
      // 21a3: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 21a6: fstore 44
      // 21a8: fload 42
      // 21aa: f2d
      // 21ab: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 21ae: fstore 45
      // 21b0: fload 42
      // 21b2: f2d
      // 21b3: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 21b6: fstore 46
      // 21b8: fload 38
      // 21ba: iload 40
      // 21bc: i2f
      // 21bd: ldc_w 360.0
      // 21c0: iload 30
      // 21c2: i2f
      // 21c3: fdiv
      // 21c4: fmul
      // 21c5: fadd
      // 21c6: f2i
      // 21c7: invokestatic q4/z/up.fade (I)I
      // 21ConfigFile: istore 47
      // 21EventPacket: fload 38
      // 21ce: iload 40
      // 21d0: bipush 1
      // 21d1: iadd
      // 21d2: i2f
      // 21d3: ldc_w 360.0
      // 21d6: iload 30
      // 21d8: i2f
      // 21d9: fdiv
      // 21ConfigManager: fmul
      // 21db: fadd
      // 21EventKey: f2i
      // 21dd: invokestatic q4/z/up.fade (I)I
      // 21e0: istore 48
      // 21e2: iload 47
      // 21e4: fload 3
      // 21e5: invokestatic q4/z/up.multAlpha (IF)I
      // 21e8: iload 7
      // 21ea: aload 0
      // 21eb: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 21ee: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 21f1: invokestatic q4/z/up.overCol (IIF)I
      // 21f4: istore 49
      // 21f6: iload 48
      // 21f8: fload 3
      // 21f9: invokestatic q4/z/up.multAlpha (IF)I
      // 21EventMove: iload 7
      // 21fe: aload 0
      // 21Category: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2202: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2205: invokestatic q4/z/up.overCol (IIF)I
      // 2208: istore 50
      // 220a: iload 49
      // 220c: ldc_w 70.0
      // 220f: fload 3
      // 2210: fmul
      // 2211: f2i
      // 2212: invokestatic q4/z/up.replAlpha (II)I
      // 2215: istore 51
      // 2217: iload 50
      // 2219: ldc_w 70.0
      // 221c: fload 3
      // 221d: fmul
      // 221e: f2i
      // 221f: invokestatic q4/z/up.replAlpha (II)I
      // 2222: istore 52
      // 2224: aload 31
      // 2226: aload 39
      // 2228: fload 43
      // 222a: fload 32
      // 222c: fmul
      // 222d: fconst_0
      // 222e: fload 44
      // 2230: fload 32
      // 2232: fmul
      // 2233: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2238: iload 51
      // 223a: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 223f: pop
      // 2240: aload 31
      // 2242: aload 39
      // 2244: fload 45
      // 2246: fload 32
      // 2248: fmul
      // 2249: fconst_0
      // 224a: fload 46
      // 224c: fload 32
      // 224e: fmul
      // 224f: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2254: iload 52
      // 2256: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 225b: pop
      // 225c: aload 31
      // 225e: aload 39
      // 2260: fload 45
      // 2262: fload 33
      // 2264: fmul
      // 2265: fconst_0
      // 2266: fload 46
      // 2268: fload 33
      // 226a: fmul
      // 226b: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2270: iload 52
      // 2272: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2277: pop
      // 2278: aload 31
      // 227a: aload 39
      // 227c: fload 43
      // 227e: fload 33
      // 2280: fmul
      // 2281: fconst_0
      // 2282: fload 44
      // 2284: fload 33
      // 2286: fmul
      // 2287: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 228c: iload 51
      // 228e: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2293: pop
      // 2294: iinc 40 1
      // 2297: goto 216b
      // 229a: aload 9
      // 229c: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 229f: iinc 34 1
      // 22a2: goto 20f4
      // 22a5: iload 28
      // 22a7: ifeq 246a
      // 22aa: ldc_w 0.85
      // 22ad: ldc_w 0.15
      // 22b0: fload 23
      // 22b2: ldc_w 6.0
      // 22b5: fmul
      // 22b6: f2d
      // 22b7: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 22ba: fmul
      // 22DraggableWidget: fadd
      // 22bc: ldc_w 0.35
      // 22bf: aload 0
      // 22c0: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 22c3: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 22c6: fmul
      // 22c7: fadd
      // 22c8: fstore 34
      // 22ConfigFile: ldc_w 0.08
      // 22cd: aload 0
      // 22ce: getfield ru/white/module/impl/render/yk.gyroSize Lq2/z/SliderSetting;
      // 22d1: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 22d4: invokevirtual java/lang/Float.floatValue ()F
      // 22d7: fmul
      // 22d8: fload 34
      // 22ConfigManager: fmul
      // 22db: fstore 35
      // 22dd: aload 9
      // 22df: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 22e2: aload 9
      // 22e4: dload 13
      // 22e6: dload 15
      // 22e8: dload 17
      // 22ea: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 22ed: aload 9
      // 22ef: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 22f2: fload 24
      // 22f4: ldc_w 1.5
      // 22f7: fmul
      // 22f8: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 22fd: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2300: aload 9
      // 2302: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2305: ldc_w 45.0
      // 2308: ldc_w 15.0
      // 230b: fload 23
      // 230d: fconst_2
      // 230e: fmul
      // 230f: f2d
      // 2310: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2313: fmul
      // 2314: fadd
      // 2315: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 231a: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 231d: aload 9
      // 231f: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 2322: fload 24
      // 2324: ldc_w 0.8
      // 2327: fmul
      // 2328: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 232d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2330: aload 9
      // 2332: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 2335: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 2338: astore 36
      // 233a: ldc_w -2081085055
      // 233d: ldc_w 2081085235
      // 2340: iadd
      // 2341: invokestatic q4/z/up.fade (I)I
      // 2344: fload 3
      // 2345: invokestatic q4/z/up.multAlpha (IF)I
      // 2348: iload 7
      // 234a: aload 0
      // 234b: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 234e: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2351: invokestatic q4/z/up.overCol (IIF)I
      // 2354: istore 37
      // 2356: iload 37
      // 2358: ldc_w 140.0
      // 235b: fload 3
      // 235c: fmul
      // 235d: f2i
      // 235e: invokestatic q4/z/up.replAlpha (II)I
      // 2361: istore 38
      // 2363: aload 31
      // 2365: aload 36
      // 2367: fconst_0
      // 2368: fload 35
      // 236a: fconst_0
      // 236b: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2370: iload 38
      // 2372: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2377: pop
      // 2378: aload 31
      // 237a: aload 36
      // 237c: fload 35
      // 237e: fconst_0
      // 237f: fconst_0
      // 2380: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2385: iload 38
      // 2387: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 238c: pop
      // 238d: aload 31
      // 238f: aload 36
      // 2391: fconst_0
      // 2392: fload 35
      // 2394: fneg
      // 2395: fconst_0
      // 2396: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 239b: iload 38
      // 239d: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 23a2: pop
      // 23a3: aload 31
      // 23a5: aload 36
      // 23a7: fload 35
      // 23a9: fneg
      // 23aa: fconst_0
      // 23ab: fconst_0
      // 23EventMotion: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 23b1: iload 38
      // 23b3: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 23b8: pop
      // 23b9: aload 31
      // 23DraggableWidget: aload 36
      // 23bd: fconst_0
      // 23be: fload 35
      // 23c0: fconst_0
      // 23c1: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 23c6: iload 38
      // 23c8: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 23cd: pop
      // 23ce: aload 31
      // 23d0: aload 36
      // 23d2: fconst_0
      // 23d3: fconst_0
      // 23d4: fload 35
      // 23d6: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 23db: iload 38
      // 23dd: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 23e2: pop
      // 23e3: aload 31
      // 23e5: aload 36
      // 23e7: fconst_0
      // 23e8: fload 35
      // 23ea: fneg
      // 23eb: fconst_0
      // 23EventAttack: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 23f1: iload 38
      // 23f3: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 23f8: pop
      // 23f9: aload 31
      // 23fb: aload 36
      // 23fd: fconst_0
      // 23fe: fconst_0
      // 23Category: fload 35
      // 2401: fneg
      // 2402: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2407: iload 38
      // 2409: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 240e: pop
      // 240f: aload 31
      // 2411: aload 36
      // 2413: fload 35
      // 2415: fconst_0
      // 2416: fconst_0
      // 2417: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 241c: iload 38
      // 241e: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2423: pop
      // 2424: aload 31
      // 2426: aload 36
      // 2428: fconst_0
      // 2429: fconst_0
      // 242a: fload 35
      // 242c: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2431: iload 38
      // 2433: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2438: pop
      // 2439: aload 31
      // 243b: aload 36
      // 243d: fload 35
      // 243f: fneg
      // 2440: fconst_0
      // 2441: fconst_0
      // 2442: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2447: iload 38
      // 2449: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 244e: pop
      // 244f: aload 31
      // 2451: aload 36
      // 2453: fconst_0
      // 2454: fconst_0
      // 2455: fload 35
      // 2457: fneg
      // 2458: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 245d: iload 38
      // 245f: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2464: pop
      // 2465: aload 9
      // 2467: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 246a: aload 4
      // 246c: getstatic ru/white/module/impl/render/yk.RING_LINE_LAYER Lnet/minecraft/client/render/RenderLayer;
      // 246f: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 2472: astore 34
      // 2474: bipush 0
      // 2475: istore 35
      // 2477: iload 35
      // 2479: bipush 3
      // 247a: if_icmpge 25e8
      // 247d: aload 29
      // 247f: iload 35
      // 2481: aaload
      // 2482: bipush 0
      // 2483: faload
      // 2484: fstore 36
      // 2486: aload 29
      // 2488: iload 35
      // 248a: aaload
      // 248b: bipush 1
      // 248c: faload
      // 248d: fstore 37
      // 248f: fload 24
      // 2491: aload 29
      // 2493: iload 35
      // 2495: aaload
      // 2496: bipush 2
      // 2497: faload
      // 2498: fmul
      // 2499: fstore 38
      // 249b: aload 29
      // 249d: iload 35
      // 249f: aaload
      // 24a0: bipush 3
      // 24a1: faload
      // 24a2: fstore 39
      // 24a4: aload 9
      // 24a6: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 24a9: aload 9
      // 24ab: dload 13
      // 24ad: dload 15
      // 24af: dload 17
      // 24b1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 24b4: aload 9
      // 24b6: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 24b9: fload 36
      // 24DraggableWidget: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 24c0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 24c3: aload 9
      // 24c5: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 24c8: fload 37
      // 24ConfigFile: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 24cf: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 24d2: aload 9
      // 24d4: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 24d7: fload 38
      // 24d9: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 24de: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 24e1: aload 9
      // 24e3: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 24e6: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 24e9: astore 40
      // 24eb: bipush 0
      // 24EventAttack: istore 41
      // 24ee: iload 41
      // 24f0: iload 30
      // 24f2: if_icmpge 25dd
      // 24f5: iload 41
      // 24f7: i2d
      // 24f8: ldc2_w 3.141592653589793
      // 24fb: dmul
      // 24EventMove: ldc2_w 2.0
      // 24Category: dmul
      // 2500: iload 30
      // 2502: i2d
      // 2503: ddiv
      // 2504: d2f
      // 2505: fstore 42
      // 2507: iload 41
      // 2509: bipush 1
      // 250a: iadd
      // 250b: i2d
      // 250c: ldc2_w 3.141592653589793
      // 250f: dmul
      // 2510: ldc2_w 2.0
      // 2513: dmul
      // 2514: iload 30
      // 2516: i2d
      // 2517: ddiv
      // 2518: d2f
      // 2519: fstore 43
      // 251b: fload 42
      // 251d: f2d
      // 251e: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 2521: fstore 44
      // 2523: fload 42
      // 2525: f2d
      // 2526: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2529: fstore 45
      // 252b: fload 43
      // 252d: f2d
      // 252e: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 2531: fstore 46
      // 2533: fload 43
      // 2535: f2d
      // 2536: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2539: fstore 47
      // 253b: fload 39
      // 253d: iload 41
      // 253f: i2f
      // 2540: ldc_w 360.0
      // 2543: iload 30
      // 2545: i2f
      // 2546: fdiv
      // 2547: fmul
      // 2548: fadd
      // 2549: f2i
      // 254a: invokestatic q4/z/up.fade (I)I
      // 254d: istore 48
      // 254f: fload 39
      // 2551: iload 41
      // 2553: bipush 1
      // 2554: iadd
      // 2555: i2f
      // 2556: ldc_w 360.0
      // 2559: iload 30
      // 255b: i2f
      // 255c: fdiv
      // 255d: fmul
      // 255e: fadd
      // 255f: f2i
      // 2560: invokestatic q4/z/up.fade (I)I
      // 2563: istore 49
      // 2565: iload 48
      // 2567: fload 3
      // 2568: invokestatic q4/z/up.multAlpha (IF)I
      // 256b: iload 7
      // 256d: aload 0
      // 256e: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2571: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2574: invokestatic q4/z/up.overCol (IIF)I
      // 2577: istore 50
      // 2579: iload 49
      // 257b: fload 3
      // 257c: invokestatic q4/z/up.multAlpha (IF)I
      // 257f: iload 7
      // 2581: aload 0
      // 2582: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2585: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2588: invokestatic q4/z/up.overCol (IIF)I
      // 258b: istore 51
      // 258d: aload 34
      // 258f: aload 40
      // 2591: fload 44
      // 2593: fload 26
      // 2595: fmul
      // 2596: fconst_0
      // 2597: fload 45
      // 2599: fload 26
      // 259b: fmul
      // 259c: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 25a1: iload 50
      // 25a3: ldc_w 220.0
      // 25a6: fload 3
      // 25a7: fmul
      // 25a8: f2i
      // 25a9: invokestatic q4/z/up.replAlpha (II)I
      // 25EventMotion: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 25b1: pop
      // 25b2: aload 34
      // 25b4: aload 40
      // 25b6: fload 46
      // 25b8: fload 26
      // 25ba: fmul
      // 25DraggableWidget: fconst_0
      // 25bc: fload 47
      // 25be: fload 26
      // 25c0: fmul
      // 25c1: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 25c6: iload 51
      // 25c8: ldc_w 220.0
      // 25cb: fload 3
      // 25EventPacket: fmul
      // 25cd: f2i
      // 25ce: invokestatic q4/z/up.replAlpha (II)I
      // 25d1: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 25d6: pop
      // 25d7: iinc 41 1
      // 25ConfigManager: goto 24ee
      // 25dd: aload 9
      // 25df: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 25e2: iinc 35 1
      // 25e5: goto 2477
      // 25e8: iload 28
      // 25ea: ifeq 28c2
      // 25ed: ldc_w 0.85
      // 25f0: ldc_w 0.15
      // 25f3: fload 23
      // 25f5: ldc_w 6.0
      // 25f8: fmul
      // 25f9: f2d
      // 25fa: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 25fd: fmul
      // 25fe: fadd
      // 25Category: ldc_w 0.35
      // 2602: aload 0
      // 2603: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2606: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2609: fmul
      // 260a: fadd
      // 260b: fstore 35
      // 260d: ldc_w 0.08
      // 2610: aload 0
      // 2611: getfield ru/white/module/impl/render/yk.gyroSize Lq2/z/SliderSetting;
      // 2614: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 2617: invokevirtual java/lang/Float.floatValue ()F
      // 261a: fmul
      // 261b: fload 35
      // 261d: fmul
      // 261e: fstore 36
      // 2620: aload 9
      // 2622: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 2625: aload 9
      // 2627: dload 13
      // 2629: dload 15
      // 262b: dload 17
      // 262d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2630: aload 9
      // 2632: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 2635: fload 24
      // 2637: ldc_w 1.5
      // 263a: fmul
      // 263b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2640: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2643: aload 9
      // 2645: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2648: ldc_w 45.0
      // 264b: ldc_w 15.0
      // 264e: fload 23
      // 2650: fconst_2
      // 2651: fmul
      // 2652: f2d
      // 2653: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2656: fmul
      // 2657: fadd
      // 2658: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 265d: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2660: aload 9
      // 2662: getstatic net/minecraft/util/math/RotationAxis.field_40718 Lnet/minecraft/util/math/RotationAxis;
      // 2665: fload 24
      // 2667: ldc_w 0.8
      // 266a: fmul
      // 266b: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2670: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2673: aload 9
      // 2675: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 2678: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 267b: astore 37
      // 267d: ldc_w 1743626378
      // 2680: ldc_w -1743626198
      // 2683: iadd
      // 2684: invokestatic q4/z/up.fade (I)I
      // 2687: fload 3
      // 2688: invokestatic q4/z/up.multAlpha (IF)I
      // 268b: iload 7
      // 268d: aload 0
      // 268e: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2691: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2694: invokestatic q4/z/up.overCol (IIF)I
      // 2697: istore 38
      // 2699: iload 38
      // 269b: ldc_w 240.0
      // 269e: fload 3
      // 269f: fmul
      // 26a0: f2i
      // 26a1: invokestatic q4/z/up.replAlpha (II)I
      // 26a4: istore 39
      // 26a6: aload 34
      // 26a8: aload 37
      // 26aa: fconst_0
      // 26ab: fload 36
      // 26ad: fconst_0
      // 26ae: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 26b3: iload 39
      // 26b5: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 26ba: pop
      // 26DraggableWidget: aload 34
      // 26bd: aload 37
      // 26bf: fload 36
      // 26c1: fconst_0
      // 26c2: fconst_0
      // 26c3: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 26c8: iload 39
      // 26ConfigFile: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 26cf: pop
      // 26d0: aload 34
      // 26d2: aload 37
      // 26d4: fconst_0
      // 26d5: fload 36
      // 26d7: fconst_0
      // 26d8: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 26dd: iload 39
      // 26df: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 26e4: pop
      // 26e5: aload 34
      // 26e7: aload 37
      // 26e9: fload 36
      // 26eb: fneg
      // 26EventAttack: fconst_0
      // 26ed: fconst_0
      // 26ee: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 26f3: iload 39
      // 26f5: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 26fa: pop
      // 26fb: aload 34
      // 26fd: aload 37
      // 26Category: fconst_0
      // 2700: fload 36
      // 2702: fconst_0
      // 2703: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2708: iload 39
      // 270a: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 270f: pop
      // 2710: aload 34
      // 2712: aload 37
      // 2714: fconst_0
      // 2715: fconst_0
      // 2716: fload 36
      // 2718: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 271d: iload 39
      // 271f: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2724: pop
      // 2725: aload 34
      // 2727: aload 37
      // 2729: fconst_0
      // 272a: fload 36
      // 272c: fconst_0
      // 272d: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2732: iload 39
      // 2734: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2739: pop
      // 273a: aload 34
      // 273c: aload 37
      // 273e: fconst_0
      // 273f: fconst_0
      // 2740: fload 36
      // 2742: fneg
      // 2743: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2748: iload 39
      // 274a: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 274f: pop
      // 2750: aload 34
      // 2752: aload 37
      // 2754: fconst_0
      // 2755: fload 36
      // 2757: fneg
      // 2758: fconst_0
      // 2759: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 275e: iload 39
      // 2760: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2765: pop
      // 2766: aload 34
      // 2768: aload 37
      // 276a: fload 36
      // 276c: fconst_0
      // 276d: fconst_0
      // 276e: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2773: iload 39
      // 2775: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 277a: pop
      // 277b: aload 34
      // 277d: aload 37
      // 277f: fconst_0
      // 2780: fload 36
      // 2782: fneg
      // 2783: fconst_0
      // 2784: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2789: iload 39
      // 278b: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2790: pop
      // 2791: aload 34
      // 2793: aload 37
      // 2795: fload 36
      // 2797: fneg
      // 2798: fconst_0
      // 2799: fconst_0
      // 279a: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 279f: iload 39
      // 27a1: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 27a6: pop
      // 27a7: aload 34
      // 27a9: aload 37
      // 27ab: fconst_0
      // 27EventMotion: fload 36
      // 27ae: fneg
      // 27af: fconst_0
      // 27b0: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 27b5: iload 39
      // 27b7: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 27bc: pop
      // 27bd: aload 34
      // 27bf: aload 37
      // 27c1: fconst_0
      // 27c2: fconst_0
      // 27c3: fload 36
      // 27c5: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 27ConfigFile: iload 39
      // 27EventPacket: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 27d1: pop
      // 27d2: aload 34
      // 27d4: aload 37
      // 27d6: fconst_0
      // 27d7: fload 36
      // 27d9: fneg
      // 27ConfigManager: fconst_0
      // 27db: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 27e0: iload 39
      // 27e2: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 27e7: pop
      // 27e8: aload 34
      // 27ea: aload 37
      // 27EventAttack: fconst_0
      // 27ed: fconst_0
      // 27ee: fload 36
      // 27f0: fneg
      // 27f1: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 27f6: iload 39
      // 27f8: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 27fd: pop
      // 27fe: aload 34
      // 2800: aload 37
      // 2802: fload 36
      // 2804: fconst_0
      // 2805: fconst_0
      // 2806: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 280b: iload 39
      // 280d: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2812: pop
      // 2813: aload 34
      // 2815: aload 37
      // 2817: fconst_0
      // 2818: fconst_0
      // 2819: fload 36
      // 281b: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2820: iload 39
      // 2822: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2827: pop
      // 2828: aload 34
      // 282a: aload 37
      // 282c: fconst_0
      // 282d: fconst_0
      // 282e: fload 36
      // 2830: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2835: iload 39
      // 2837: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 283c: pop
      // 283d: aload 34
      // 283f: aload 37
      // 2841: fload 36
      // 2843: fneg
      // 2844: fconst_0
      // 2845: fconst_0
      // 2846: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 284b: iload 39
      // 284d: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2852: pop
      // 2853: aload 34
      // 2855: aload 37
      // 2857: fload 36
      // 2859: fneg
      // 285a: fconst_0
      // 285b: fconst_0
      // 285c: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2861: iload 39
      // 2863: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2868: pop
      // 2869: aload 34
      // 286b: aload 37
      // 286d: fconst_0
      // 286e: fconst_0
      // 286f: fload 36
      // 2871: fneg
      // 2872: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2877: iload 39
      // 2879: ldc_w 831486005
      // 287c: istore 60
      // 287e: iload 60
      // 2880: dup
      // 2881: bipush 1
      // 2882: iadd
      // 2883: imul
      // 2884: bipush 2
      // 2885: irem
      // 2886: ifeq 288b
      // 2889: aconst_null
      // 288a: pop
      // 288b: nop
      // 288c: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2891: pop
      // 2892: aload 34
      // 2894: aload 37
      // 2896: fconst_0
      // 2897: fconst_0
      // 2898: fload 36
      // 289a: fneg
      // 289b: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 28a0: iload 39
      // 28a2: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 28a7: pop
      // 28a8: aload 34
      // 28aa: aload 37
      // 28EventMotion: fload 36
      // 28ae: fconst_0
      // 28af: fconst_0
      // 28b0: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 28b5: iload 39
      // 28b7: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 28bc: pop
      // 28bd: aload 9
      // 28bf: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 28c2: aload 4
      // 28c4: getstatic ru/white/module/impl/render/yk.ROMB_ESP Ljava/util/function/Function;
      // 28c7: ldc_w "client"
      // 28ConfigFile: ldc_w "G2+Zia8NCRtqlpKURTRqIZSu9LtaInvQ9MfwACpRXME="
      // 28cd: ldc_w 1626700129
      // 28d0: ldc_w -423194261
      // 28d3: iadd
      // 28d4: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 28d7: invokestatic net/minecraft/util/Identifier.method_60655 (Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/util/Identifier;
      // 28ConfigManager: invokeinterface java/util/function/Function.apply (Ljava/lang/Object;)Ljava/lang/Object; 2
      // 28df: checkcast net/minecraft/client/render/RenderLayer
      // 28e2: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 28e5: astore 35
      // 28e7: bipush 0
      // 28e8: istore 36
      // 28ea: iload 36
      // 28EventAttack: bipush 3
      // 28ed: if_icmpge 2b3f
      // 28f0: aload 29
      // 28f2: iload 36
      // 28f4: aaload
      // 28f5: bipush 0
      // 28f6: faload
      // 28f7: fstore 37
      // 28f9: aload 29
      // 28fb: iload 36
      // 28fd: aaload
      // 28fe: bipush 1
      // 28Category: faload
      // 2900: fstore 38
      // 2902: fload 24
      // 2904: aload 29
      // 2906: iload 36
      // 2908: aaload
      // 2909: bipush 2
      // 290a: faload
      // 290b: fmul
      // 290c: fstore 39
      // 290e: aload 29
      // 2910: iload 36
      // 2912: aaload
      // 2913: bipush 3
      // 2914: faload
      // 2915: fstore 40
      // 2917: aload 29
      // 2919: iload 36
      // 291b: aaload
      // 291c: bipush 4
      // 291d: faload
      // 291e: fstore 41
      // 2920: lload 19
      // 2922: l2d
      // 2923: dload 21
      // 2925: ldc2_w 0.35
      // 2928: dmul
      // 2929: ddiv
      // 292a: fload 41
      // 292c: f2d
      // 292d: ldc2_w 3.141592653589793
      // 2930: dmul
      // 2931: ldc2_w 2.0
      // 2934: dmul
      // 2935: dadd
      // 2936: ldc2_w 6.283185307179586
      // 2939: drem
      // 293a: d2f
      // 293b: fstore 42
      // 293d: new org/joml/Quaternionf
      // 2940: dup
      // 2941: invokespecial org/joml/Quaternionf.<init> ()V
      // 2944: fload 37
      // 2946: f2d
      // 2947: invokestatic java/lang/Math.toRadians (D)D
      // 294a: d2f
      // 294b: invokevirtual org/joml/Quaternionf.rotateX (F)Lorg/joml/Quaternionf;
      // 294e: fload 38
      // 2950: f2d
      // 2951: invokestatic java/lang/Math.toRadians (D)D
      // 2954: d2f
      // 2955: invokevirtual org/joml/Quaternionf.rotateZ (F)Lorg/joml/Quaternionf;
      // 2958: fload 39
      // 295a: f2d
      // 295b: invokestatic java/lang/Math.toRadians (D)D
      // 295e: d2f
      // 295f: invokevirtual org/joml/Quaternionf.rotateY (F)Lorg/joml/Quaternionf;
      // 2962: astore 43
      // 2964: fload 42
      // 2966: f2d
      // 2967: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 296a: fload 26
      // 296c: fmul
      // 296d: fstore 44
      // 296f: fload 42
      // 2971: f2d
      // 2972: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2975: fload 26
      // 2977: fmul
      // 2978: fstore 45
      // 297a: new org/joml/Vector3f
      // 297d: dup
      // 297e: fload 44
      // 2980: fconst_0
      // 2981: fload 45
      // 2983: invokespecial org/joml/Vector3f.<init> (FFF)V
      // 2986: astore 46
      // 2988: aload 43
      // 298a: aload 46
      // 298c: invokevirtual org/joml/Quaternionf.transform (Lorg/joml/Vector3f;)Lorg/joml/Vector3f;
      // 298f: pop
      // 2990: dload 13
      // 2992: aload 46
      // 2994: getfield org/joml/Vector3f.x F
      // 2997: f2d
      // 2998: dadd
      // 2999: dstore 47
      // 299b: dload 15
      // 299d: aload 46
      // 299f: getfield org/joml/Vector3f.y F
      // 29a2: f2d
      // 29a3: dadd
      // 29a4: dstore 49
      // 29a6: dload 17
      // 29a8: ldc_w -917990440
      // 29ab: istore 61
      // 29ad: iload 61
      // 29af: dup
      // 29b0: bipush 1
      // 29b1: iadd
      // 29b2: imul
      // 29b3: bipush 2
      // 29b4: irem
      // 29b5: ifeq 29c4
      // 29b8: aconst_null
      // 29b9: pop
      // 29ba: ldc_w "qiOUaEAAXvvHxc124itlkT76oAr7Li95"
      // 29bd: ldc_w -1212527493
      // 29c0: invokestatic rose/guard/rt/StrDec.d (Ljava/lang/String;I)Ljava/lang/String;
      // 29c3: pop
      // 29c4: nop
      // 29c5: aload 46
      // 29c7: getfield org/joml/Vector3f.z F
      // 29ConfigFile: f2d
      // 29cb: dadd
      // 29EventPacket: dstore 51
      // 29ce: ldc_w 0.16
      // 29d1: aload 0
      // 29d2: getfield ru/white/module/impl/render/yk.gyroSize Lq2/z/SliderSetting;
      // 29d5: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 29d8: invokevirtual java/lang/Float.floatValue ()F
      // 29db: fmul
      // 29EventKey: fconst_1
      // 29dd: ldc_w 0.3
      // 29e0: fload 23
      // 29e2: ldc_w 5.0
      // 29e5: fmul
      // 29e6: f2d
      // 29e7: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 29ea: fmul
      // 29eb: fadd
      // 29EventAttack: fmul
      // 29ed: fstore 53
      // 29ef: fload 40
      // 29f1: f2i
      // 29f2: invokestatic q4/z/up.fade (I)I
      // 29f5: fload 3
      // 29f6: invokestatic q4/z/up.multAlpha (IF)I
      // 29f9: iload 7
      // 29fb: aload 0
      // 29EventMove: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 29Category: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2a02: invokestatic q4/z/up.overCol (IIF)I
      // 2a05: istore 54
      // 2a07: aload 9
      // 2a09: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 2a0c: aload 9
      // 2a0e: dload 47
      // 2a10: dload 49
      // 2a12: dload 51
      // 2a14: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2a17: aload 9
      // 2a19: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 2a1c: aload 12
      // 2a1e: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 2a21: fneg
      // 2a22: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2a27: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2a2a: aload 9
      // 2a2c: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2a2f: aload 12
      // 2a31: invokevirtual net/minecraft/client/render/Camera.method_19329 ()F
      // 2a34: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2a39: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2a3c: aload 9
      // 2a3e: fload 53
      // 2a40: fload 53
      // 2a42: fload 53
      // 2a44: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2a47: aload 35
      // 2a49: aload 9
      // 2a4b: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 2a4e: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 2a51: iload 54
      // 2a53: iload 54
      // 2a55: iload 54
      // 2a57: iload 54
      // 2a59: ldc_w 255.0
      // 2a5c: fload 3
      // 2a5d: fmul
      // 2a5e: f2i
      // 2a5f: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 2a62: aload 9
      // 2a64: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 2a67: bipush 1
      // 2a68: istore 55
      // 2a6a: iload 55
      // 2a6c: bipush 3
      // 2a6d: if_icmpgt 2b39
      // 2a70: fload 42
      // 2a72: iload 55
      // 2a74: i2f
      // 2a75: ldc_w 0.12
      // 2a78: fmul
      // 2a79: fsub
      // 2a7a: fstore 56
      // 2a7c: new org/joml/Vector3f
      // 2a7f: dup
      // 2a80: fload 56
      // 2a82: f2d
      // 2a83: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 2a86: fload 26
      // 2a88: fmul
      // 2a89: fconst_0
      // 2a8a: fload 56
      // 2a8c: f2d
      // 2a8d: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2a90: fload 26
      // 2a92: fmul
      // 2a93: invokespecial org/joml/Vector3f.<init> (FFF)V
      // 2a96: astore 57
      // 2a98: aload 43
      // 2a9a: aload 57
      // 2a9c: invokevirtual org/joml/Quaternionf.transform (Lorg/joml/Vector3f;)Lorg/joml/Vector3f;
      // 2a9f: pop
      // 2aa0: fload 53
      // 2aa2: fconst_1
      // 2aa3: iload 55
      // 2aa5: i2f
      // 2aa6: ldc_w 0.25
      // 2aa9: fmul
      // 2aaa: fsub
      // 2aab: fmul
      // 2aac: fstore 58
      // 2aae: aload 9
      // 2ab0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 2ab3: aload 9
      // 2ab5: dload 13
      // 2ab7: aload 57
      // 2ab9: getfield org/joml/Vector3f.x F
      // 2abc: f2d
      // 2abd: dadd
      // 2abe: dload 15
      // 2ac0: aload 57
      // 2ac2: getfield org/joml/Vector3f.y F
      // 2ac5: f2d
      // 2ac6: dadd
      // 2ac7: dload 17
      // 2ac9: aload 57
      // 2acb: getfield org/joml/Vector3f.z F
      // 2ace: f2d
      // 2acf: dadd
      // 2ad0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2ad3: aload 9
      // 2ad5: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 2ad8: aload 12
      // 2ada: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 2add: fneg
      // 2ade: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2ae3: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2ae6: aload 9
      // 2ae8: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2aeb: aload 12
      // 2aed: invokevirtual net/minecraft/client/render/Camera.method_19329 ()F
      // 2af0: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2af5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2af8: aload 9
      // 2afa: fload 58
      // 2afc: fload 58
      // 2afe: fload 58
      // 2b00: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2b03: aload 35
      // 2b05: aload 9
      // 2b07: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 2b0a: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 2b0d: iload 54
      // 2b0f: iload 54
      // 2b11: iload 54
      // 2b13: iload 54
      // 2b15: ldc_w 935232170
      // 2b18: ldc_w -935231950
      // 2b1b: iadd
      // 2b1c: iload 55
      // 2b1e: ldc_w -752670296
      // 2b21: ldc_w 752670356
      // 2b24: iadd
      // 2b25: imul
      // 2b26: isub
      // 2b27: i2f
      // 2b28: fload 3
      // 2b29: fmul
      // 2b2a: f2i
      // 2b2b: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 2b2e: aload 9
      // 2b30: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 2b33: iinc 55 1
      // 2b36: goto 2a6a
      // 2b39: iinc 36 1
      // 2b3c: goto 28ea
      // 2b3f: iload 28
      // 2b41: ifeq 2bfb
      // 2b44: ldc_w 0.85
      // 2b47: ldc_w 0.15
      // 2b4a: fload 23
      // 2b4c: ldc_w 6.0
      // 2b4f: fmul
      // 2b50: f2d
      // 2b51: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2b54: fmul
      // 2b55: fadd
      // 2b56: ldc_w 0.35
      // 2b59: aload 0
      // 2b5a: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2b5d: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2b60: fmul
      // 2b61: fadd
      // 2b62: fstore 36
      // 2b64: ldc_w 0.08
      // 2b67: aload 0
      // 2b68: getfield ru/white/module/impl/render/yk.gyroSize Lq2/z/SliderSetting;
      // 2b6b: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 2b6e: invokevirtual java/lang/Float.floatValue ()F
      // 2b71: fmul
      // 2b72: fload 36
      // 2b74: fmul
      // 2b75: fstore 37
      // 2b77: ldc_w -178376982
      // 2b7a: ldc_w 178377162
      // 2b7d: iadd
      // 2b7e: invokestatic q4/z/up.fade (I)I
      // 2b81: fload 3
      // 2b82: invokestatic q4/z/up.multAlpha (IF)I
      // 2b85: iload 7
      // 2b87: aload 0
      // 2b88: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2b8b: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2b8e: invokestatic q4/z/up.overCol (IIF)I
      // 2b91: istore 38
      // 2b93: aload 9
      // 2b95: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 2b98: aload 9
      // 2b9a: dload 13
      // 2b9c: dload 15
      // 2b9e: dload 17
      // 2ba0: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2ba3: aload 9
      // 2ba5: getstatic net/minecraft/util/math/RotationAxis.field_40716 Lnet/minecraft/util/math/RotationAxis;
      // 2ba8: aload 12
      // 2baa: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 2bad: fneg
      // 2bae: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2bb3: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2bb6: aload 9
      // 2bb8: getstatic net/minecraft/util/math/RotationAxis.field_40714 Lnet/minecraft/util/math/RotationAxis;
      // 2bbb: aload 12
      // 2bbd: invokevirtual net/minecraft/client/render/Camera.method_19329 ()F
      // 2bc0: invokeinterface net/minecraft/util/math/RotationAxis.rotationDegrees (F)Lorg/joml/Quaternionf; 2
      // 2bc5: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22907 (Lorg/joml/Quaternionfc;)V
      // 2bc8: fload 37
      // 2bca: ldc_w 3.5
      // 2bcd: fmul
      // 2bce: fstore 39
      // 2bd0: aload 9
      // 2bd2: fload 39
      // 2bd4: fload 39
      // 2bd6: fload 39
      // 2bd8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22905 (FFF)V
      // 2bdb: aload 35
      // 2bdd: aload 9
      // 2bdf: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 2be2: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 2be5: iload 38
      // 2be7: iload 38
      // 2be9: iload 38
      // 2beb: iload 38
      // 2bed: ldc_w 120.0
      // 2bf0: fload 3
      // 2bf1: fmul
      // 2bf2: f2i
      // 2bf3: invokestatic ru/white/module/impl/render/yk.m881 (Lnet/minecraft/client/render/VertexConsumer;Lorg/joml/Matrix4f;IIIII)V
      // 2bf6: aload 9
      // 2bf8: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 2bfb: fload 3
      // 2bfc: ldc_w 0.001
      // 2bff: fcmpl
      // 2c00: ifle 369b
      // 2c03: aload 0
      // 2c04: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 2c07: ifnull 369b
      // 2c0a: aload 0
      // 2c0b: getfield ru/white/module/impl/render/yk.type Lq2/z/ModeSetting;
      // 2c0e: ldc "Мандала"
      // 2c10: invokevirtual q2/z/xf.is (Ljava/lang/String;)Z
      // 2c13: ifeq 369b
      // 2c16: aload 0
      // 2c17: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 2c1a: getfield net/minecraft/entity/LivingEntity.field_6235 I
      // 2c1d: istore 5
      // 2c1f: iload 5
      // 2c21: i2d
      // 2c22: ldc2_w 0.15707963267948966
      // 2c25: dmul
      // 2c26: invokestatic java/lang/Math.sin (D)D
      // 2c29: d2f
      // 2c2a: fstore 6
      // 2c2c: aload 0
      // 2c2d: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2c30: invokevirtual ru/white/utils/animation/Animation.update ()Z
      // 2c33: pop
      // 2c34: aload 0
      // 2c35: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2c38: fload 6
      // 2c3a: f2d
      // 2c3b: ldc2_w 0.10000000149011612
      // 2c3e: getstatic ru/white/utils/animation/ap.SINE_OUT Lru/white/utils/animation/zo;
      // 2c41: invokevirtual ru/white/utils/animation/Animation.run (DDLru/white/utils/animation/zo;)Lru/white/utils/animation/Animation;
      // 2c44: pop
      // 2c45: ldc_w 963061325
      // 2c48: ldc_w -963061070
      // 2c4b: iadd
      // 2c4c: ldc_w -845773490
      // 2c4f: ldc_w 845773565
      // 2c52: iadd
      // 2c53: ldc_w 1836043123
      // 2c56: ldc_w -1836043048
      // 2c59: iadd
      // 2c5a: ldc_w 255.0
      // 2c5d: fload 3
      // 2c5e: fmul
      // 2c5f: f2i
      // 2c60: invokestatic q4/z/up.getColor (IIII)I
      // 2c63: istore 7
      // 2c65: bipush 0
      // 2c66: invokestatic q4/z/up.fade (I)I
      // 2c69: fload 3
      // 2c6a: invokestatic q4/z/up.multAlpha (IF)I
      // 2c6d: iload 7
      // 2c6f: aload 0
      // 2c70: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2c73: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2c76: invokestatic q4/z/up.overCol (IIF)I
      // 2c79: istore 8
      // 2c7b: aload 1
      // 2c7c: invokevirtual x9/sb.getMatrixStack ()Lnet/minecraft/client/util/math/MatrixStack;
      // 2c7f: astore 9
      // 2c81: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 2c84: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 2c87: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 2c8a: invokevirtual net/minecraft/client/render/Camera.method_71156 ()Lnet/minecraft/util/math/Vec3d;
      // 2c8d: astore 10
      // 2c8f: aload 0
      // 2c90: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 2c93: aload 1
      // 2c94: invokevirtual x9/sb.getTickDelta ()F
      // 2c97: invokevirtual net/minecraft/entity/LivingEntity.method_30950 (F)Lnet/minecraft/util/math/Vec3d;
      // 2c9a: astore 11
      // 2c9c: getstatic ru/white/module/impl/render/yk.mc Lnet/minecraft/client/MinecraftClient;
      // 2c9f: getfield net/minecraft/client/MinecraftClient.field_1773 Lnet/minecraft/client/render/GameRenderer;
      // 2ca2: invokevirtual net/minecraft/client/render/GameRenderer.method_19418 ()Lnet/minecraft/client/render/Camera;
      // 2ca5: astore 12
      // 2ca7: invokestatic java/lang/System.currentTimeMillis ()J
      // 2caa: lstore 13
      // 2cac: aload 0
      // 2cad: getfield ru/white/module/impl/render/yk.mandalaSpeed Lq2/z/SliderSetting;
      // 2cb0: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 2cb3: invokevirtual java/lang/Float.doubleValue ()D
      // 2cb6: dstore 15
      // 2cb8: dconst_1
      // 2cb9: aload 0
      // 2cba: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2cbd: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2cc0: f2d
      // 2cc1: ldc2_w 2.5
      // 2cc4: dmul
      // 2cc5: dadd
      // 2cc6: dstore 17
      // 2cc8: lload 13
      // 2cca: l2d
      // 2ccb: dload 15
      // 2ccd: ddiv
      // 2cce: ldc2_w 360.0
      // 2cd1: dmul
      // 2cd2: dload 17
      // 2cd4: dmul
      // 2cd5: ldc2_w 360.0
      // 2cd8: drem
      // 2cd9: d2f
      // 2cda: fstore 19
      // 2cdc: lload 13
      // 2cde: l2d
      // 2cdf: dload 15
      // 2ce1: ldc2_w 1.3
      // 2ce4: dmul
      // 2ce5: ddiv
      // 2ce6: ldc2_w 360.0
      // 2ce9: dmul
      // 2cea: dload 17
      // 2cec: dmul
      // 2ced: ldc2_w 360.0
      // 2cf0: drem
      // 2cf1: d2f
      // 2cf2: fstore 20
      // 2cf4: aload 0
      // 2cf5: getfield ru/white/module/impl/render/yk.mandalaSize Lq2/z/SliderSetting;
      // 2cf8: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 2cfb: invokevirtual java/lang/Float.floatValue ()F
      // 2cfe: fconst_1
      // 2cff: ldc_w 0.15
      // 2d02: aload 0
      // 2d03: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2d06: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2d09: fmul
      // 2d0a: fadd
      // 2d0b: fmul
      // 2d0c: fstore 21
      // 2d0e: aload 0
      // 2d0f: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 2d12: invokevirtual net/minecraft/entity/LivingEntity.method_17681 ()F
      // 2d15: ldc_w 1.3
      // 2d18: fmul
      // 2d19: fload 21
      // 2d1b: fmul
      // 2d1c: fstore 22
      // 2d1e: fload 22
      // 2d20: ldc_w 0.72
      // 2d23: fmul
      // 2d24: fstore 23
      // 2d26: fload 22
      // 2d28: ldc_w 0.32
      // 2d2b: fmul
      // 2d2c: fstore 24
      // 2d2e: aload 0
      // 2d2f: getfield ru/white/module/impl/render/yk.mandalaBeams Lq2/z/BooleanSetting;
      // 2d32: invokevirtual q2/z/qf.getValue ()Ljava/lang/Boolean;
      // 2d35: invokevirtual java/lang/Boolean.booleanValue ()Z
      // 2d38: istore 25
      // 2d3a: aload 11
      // 2d3c: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 2d3f: aload 10
      // 2d41: getfield net/minecraft/util/math/Vec3d.field_1352 D
      // 2d44: dsub
      // 2d45: dstore 26
      // 2d47: aload 11
      // 2d49: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 2d4c: aload 10
      // 2d4e: getfield net/minecraft/util/math/Vec3d.field_1351 D
      // 2d51: dsub
      // 2d52: ldc2_w 0.02
      // 2d55: dadd
      // 2d56: dstore 28
      // 2d58: aload 11
      // 2d5a: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 2d5d: aload 10
      // 2d5f: getfield net/minecraft/util/math/Vec3d.field_1350 D
      // 2d62: dsub
      // 2d63: dstore 30
      // 2d65: fload 20
      // 2d67: fneg
      // 2d68: fstore 32
      // 2d6a: fconst_1
      // 2d6b: ldc_w 0.12
      // 2d6e: lload 13
      // 2d70: ldc2_w -2962322518371620957
      // 2d73: ldc2_w 2962322518372620957
      // 2d76: ladd
      // 2d77: lrem
      // 2d78: l2f
      // 2d79: ldc_w 250.0
      // 2d7c: fdiv
      // 2d7d: f2d
      // 2d7e: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2d81: fmul
      // 2d82: fadd
      // 2d83: fstore 33
      // 2d85: fload 24
      // 2d87: fload 33
      // 2d89: fmul
      // 2d8a: fstore 34
      // 2d8c: ldc_w -769651387
      // 2d8f: ldc_w 769651411
      // 2d92: iadd
      // 2d93: istore 35
      // 2d95: aload 4
      // 2d97: getstatic ru/white/module/impl/render/yk.RING_FILL_LAYER Lnet/minecraft/client/render/RenderLayer;
      // 2d9a: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 2d9d: astore 36
      // 2d9f: aload 9
      // 2da1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 2da4: aload 9
      // 2da6: dload 26
      // 2da8: dload 28
      // 2daa: dload 30
      // 2dac: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 2daf: aload 9
      // 2db1: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 2db4: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 2db7: astore 37
      // 2db9: bipush 0
      // 2dba: istore 38
      // 2dbc: iload 38
      // 2dbe: bipush 2
      // 2dbf: if_icmpge 2efa
      // 2dc2: iload 38
      // 2dc4: i2f
      // 2dc5: ldc_w 45.0
      // 2dc8: fmul
      // 2dc9: fstore 39
      // 2dcb: bipush 0
      // 2dcc: istore 40
      // 2dce: iload 40
      // 2dd0: bipush 4
      // 2dd1: if_icmpge 2ef4
      // 2dd4: fload 32
      // 2dd6: fload 39
      // 2dd8: fadd
      // 2dd9: iload 40
      // 2ddb: i2f
      // 2ddc: ldc_w 90.0
      // 2ddf: fmul
      // 2de0: fadd
      // 2de1: f2d
      // 2de2: invokestatic java/lang/Math.toRadians (D)D
      // 2de5: d2f
      // 2de6: fstore 41
      // 2de8: fload 32
      // 2dea: fload 39
      // 2dec: fadd
      // 2ded: iload 40
      // 2def: bipush 1
      // 2df0: iadd
      // 2df1: i2f
      // 2df2: ldc_w 90.0
      // 2df5: fmul
      // 2df6: fadd
      // 2df7: f2d
      // 2df8: invokestatic java/lang/Math.toRadians (D)D
      // 2dfb: d2f
      // 2dfc: fstore 42
      // 2dfe: fload 41
      // 2e00: f2d
      // 2e01: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 2e04: fload 23
      // 2e06: fmul
      // 2e07: fstore 43
      // 2e09: fload 41
      // 2e0b: f2d
      // 2e0c: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2e0f: fload 23
      // 2e11: fmul
      // 2e12: fstore 44
      // 2e14: fload 42
      // 2e16: f2d
      // 2e17: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 2e1a: fload 23
      // 2e1c: fmul
      // 2e1d: fstore 45
      // 2e1f: fload 42
      // 2e21: f2d
      // 2e22: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2e25: fload 23
      // 2e27: fmul
      // 2e28: fstore 46
      // 2e2a: iload 38
      // 2e2c: ldc_w 384400000
      // 2e2f: ldc_w -384399910
      // 2e32: iadd
      // 2e33: imul
      // 2e34: iload 40
      // 2e36: ldc_w -2041110396
      // 2e39: ldc_w 2041110441
      // 2e3c: iadd
      // 2e3d: imul
      // 2e3e: iadd
      // 2e3f: invokestatic q4/z/up.fade (I)I
      // 2e42: fload 3
      // 2e43: invokestatic q4/z/up.multAlpha (IF)I
      // 2e46: iload 7
      // 2e48: aload 0
      // 2e49: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2e4c: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2e4f: invokestatic q4/z/up.overCol (IIF)I
      // 2e52: istore 47
      // 2e54: iload 38
      // 2e56: ldc_w 1815919393
      // 2e59: ldc_w -1815919303
      // 2e5c: iadd
      // 2e5d: imul
      // 2e5e: iload 40
      // 2e60: bipush 1
      // 2e61: iadd
      // 2e62: ldc_w -532710373
      // 2e65: ldc_w 532710418
      // 2e68: iadd
      // 2e69: imul
      // 2e6a: iadd
      // 2e6b: invokestatic q4/z/up.fade (I)I
      // 2e6e: fload 3
      // 2e6f: invokestatic q4/z/up.multAlpha (IF)I
      // 2e72: iload 7
      // 2e74: aload 0
      // 2e75: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2e78: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2e7b: invokestatic q4/z/up.overCol (IIF)I
      // 2e7e: istore 48
      // 2e80: iload 47
      // 2e82: ldc_w 40.0
      // 2e85: fload 3
      // 2e86: fmul
      // 2e87: f2i
      // 2e88: invokestatic q4/z/up.replAlpha (II)I
      // 2e8b: istore 49
      // 2e8d: iload 48
      // 2e8f: ldc_w 15.0
      // 2e92: fload 3
      // 2e93: fmul
      // 2e94: f2i
      // 2e95: invokestatic q4/z/up.replAlpha (II)I
      // 2e98: istore 50
      // 2e9a: aload 36
      // 2e9c: aload 37
      // 2e9e: fconst_0
      // 2e9f: fconst_0
      // 2ea0: fconst_0
      // 2ea1: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2ea6: iload 50
      // 2ea8: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2ead: pop
      // 2eae: aload 36
      // 2eb0: aload 37
      // 2eb2: fload 43
      // 2eb4: fconst_0
      // 2eb5: fload 44
      // 2eb7: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2ebc: iload 49
      // 2ebe: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2ec3: pop
      // 2ec4: aload 36
      // 2ec6: aload 37
      // 2ec8: fload 45
      // 2eca: fconst_0
      // 2ecb: fload 46
      // 2ecd: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2ed2: iload 49
      // 2ed4: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2ed9: pop
      // 2eda: aload 36
      // 2edc: aload 37
      // 2ede: fconst_0
      // 2edf: fconst_0
      // 2ee0: fconst_0
      // 2ee1: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2ee6: iload 50
      // 2ee8: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2eed: pop
      // 2eee: iinc 40 1
      // 2ef1: goto 2dce
      // 2ef4: iinc 38 1
      // 2ef7: goto 2dbc
      // 2efa: bipush 0
      // 2efb: istore 38
      // 2efd: iload 38
      // 2eff: iload 35
      // 2f01: if_icmpge 2fdc
      // 2f04: iload 38
      // 2f06: i2d
      // 2f07: ldc2_w 3.141592653589793
      // 2f0a: dmul
      // 2f0b: ldc2_w 2.0
      // 2f0e: dmul
      // 2f0f: iload 35
      // 2f11: i2d
      // 2f12: ddiv
      // 2f13: d2f
      // 2f14: fstore 39
      // 2f16: iload 38
      // 2f18: bipush 1
      // 2f19: iadd
      // 2f1a: i2d
      // 2f1b: ldc2_w 3.141592653589793
      // 2f1e: dmul
      // 2f1f: ldc2_w 2.0
      // 2f22: dmul
      // 2f23: iload 35
      // 2f25: i2d
      // 2f26: ddiv
      // 2f27: d2f
      // 2f28: fstore 40
      // 2f2a: fload 39
      // 2f2c: f2d
      // 2f2d: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 2f30: fload 34
      // 2f32: fmul
      // 2f33: fstore 41
      // 2f35: fload 39
      // 2f37: f2d
      // 2f38: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2f3b: fload 34
      // 2f3d: fmul
      // 2f3e: fstore 42
      // 2f40: fload 40
      // 2f42: f2d
      // 2f43: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 2f46: fload 34
      // 2f48: fmul
      // 2f49: fstore 43
      // 2f4b: fload 40
      // 2f4d: f2d
      // 2f4e: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 2f51: fload 34
      // 2f53: fmul
      // 2f54: fstore 44
      // 2f56: iload 38
      // 2f58: ldc_w -1125381782
      // 2f5b: ldc_w 1125381797
      // 2f5e: iadd
      // 2f5f: imul
      // 2f60: invokestatic q4/z/up.fade (I)I
      // 2f63: fload 3
      // 2f64: invokestatic q4/z/up.multAlpha (IF)I
      // 2f67: iload 7
      // 2f69: aload 0
      // 2f6a: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 2f6d: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 2f70: invokestatic q4/z/up.overCol (IIF)I
      // 2f73: istore 45
      // 2f75: iload 45
      // 2f77: ldc_w 60.0
      // 2f7a: fload 3
      // 2f7b: fmul
      // 2f7c: f2i
      // 2f7d: invokestatic q4/z/up.replAlpha (II)I
      // 2f80: istore 46
      // 2f82: aload 36
      // 2f84: aload 37
      // 2f86: fconst_0
      // 2f87: fconst_0
      // 2f88: fconst_0
      // 2f89: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2f8e: iload 46
      // 2f90: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2f95: pop
      // 2f96: aload 36
      // 2f98: aload 37
      // 2f9a: fload 41
      // 2f9c: fconst_0
      // 2f9d: fload 42
      // 2f9f: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2fa4: iload 46
      // 2fa6: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2fab: pop
      // 2fac: aload 36
      // 2fae: aload 37
      // 2fb0: fload 43
      // 2fb2: fconst_0
      // 2fb3: fload 44
      // 2fb5: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2fba: iload 46
      // 2fbc: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2fc1: pop
      // 2fc2: aload 36
      // 2fc4: aload 37
      // 2fc6: fconst_0
      // 2fc7: fconst_0
      // 2fc8: fconst_0
      // 2fc9: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 2fce: iload 46
      // 2fd0: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 2fd5: pop
      // 2fd6: iinc 38 1
      // 2fd9: goto 2efd
      // 2fdc: iload 25
      // 2fde: ifeq 31f8
      // 2fe1: aload 0
      // 2fe2: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 2fe5: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 2fe8: aload 0
      // 2fe9: getfield ru/white/module/impl/render/yk.mandalaHeight Lq2/z/SliderSetting;
      // 2fec: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 2fef: invokevirtual java/lang/Float.floatValue ()F
      // 2ff2: fmul
      // 2ff3: fstore 38
      // 2ff5: lload 13
      // 2ff7: ldc2_w 705769478504144929
      // 2ffa: ldc2_w -705769478504143729
      // 2ffd: ladd
      // 2ffe: lrem
      // 2fff: l2d
      // 3000: ldc2_w 1200.0
      // 3003: ddiv
      // 3004: d2f
      // 3005: fstore 39
      // 3007: ldc_w 0.035
      // 300a: fload 21
      // 300c: fmul
      // 300d: fstore 40
      // 300f: bipush 0
      // 3010: istore 41
      // 3012: iload 41
      // 3014: ldc_w 1051834248
      // 3017: ldc_w -1051834240
      // 301a: iadd
      // 301b: if_icmpge 31f8
      // 301e: fload 32
      // 3020: iload 41
      // 3022: i2f
      // 3023: ldc_w 45.0
      // 3026: fmul
      // 3027: fadd
      // 3028: f2d
      // 3029: invokestatic java/lang/Math.toRadians (D)D
      // 302c: d2f
      // 302d: fstore 42
      // 302f: fload 42
      // 3031: f2d
      // 3032: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 3035: fload 23
      // 3037: fmul
      // 3038: fstore 43
      // 303a: fload 42
      // 303c: f2d
      // 303d: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 3040: fload 23
      // 3042: fmul
      // 3043: fstore 44
      // 3045: aload 12
      // 3047: invokevirtual net/minecraft/client/render/Camera.method_19330 ()F
      // 304a: fneg
      // 304b: ldc_w 90.0
      // 304e: fadd
      // 304f: f2d
      // 3050: invokestatic java/lang/Math.toRadians (D)D
      // 3053: d2f
      // 3054: fstore 45
      // 3056: fload 45
      // 3058: f2d
      // 3059: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 305c: fload 40
      // 305e: fmul
      // 305f: fstore 46
      // 3061: fload 45
      // 3063: f2d
      // 3064: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 3067: fload 40
      // 3069: fmul
      // 306a: fstore 47
      // 306c: iload 41
      // 306e: ldc_w 1442527635
      // 3071: ldc_w -1442527590
      // 3074: iadd
      // 3075: imul
      // 3076: invokestatic q4/z/up.fade (I)I
      // 3079: fload 3
      // 307a: invokestatic q4/z/up.multAlpha (IF)I
      // 307d: iload 7
      // 307f: aload 0
      // 3080: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 3083: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 3086: invokestatic q4/z/up.overCol (IIF)I
      // 3089: istore 48
      // 308b: ldc_w 200.0
      // 308e: fload 3
      // 308f: fmul
      // 3090: f2i
      // 3091: istore 49
      // 3093: bipush 0
      // 3094: istore 50
      // 3096: aload 36
      // 3098: aload 37
      // 309a: fload 43
      // 309c: fload 46
      // 309e: fsub
      // 309f: fconst_0
      // 30a0: fload 44
      // 30a2: fload 47
      // 30a4: fsub
      // 30a5: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 30aa: iload 48
      // 30EventMotion: iload 49
      // 30ae: invokestatic q4/z/up.replAlpha (II)I
      // 30b1: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 30b6: pop
      // 30b7: aload 36
      // 30b9: aload 37
      // 30DraggableWidget: fload 43
      // 30bd: fload 46
      // 30bf: fadd
      // 30c0: fconst_0
      // 30c1: fload 44
      // 30c3: fload 47
      // 30c5: fadd
      // 30c6: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 30cb: iload 48
      // 30cd: iload 49
      // 30cf: invokestatic q4/z/up.replAlpha (II)I
      // 30d2: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 30d7: pop
      // 30d8: aload 36
      // 30ConfigManager: aload 37
      // 30EventKey: fload 43
      // 30de: fload 46
      // 30e0: fadd
      // 30e1: fload 38
      // 30e3: fload 44
      // 30e5: fload 47
      // 30e7: fadd
      // 30e8: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 30ed: iload 48
      // 30ef: iload 50
      // 30f1: invokestatic q4/z/up.replAlpha (II)I
      // 30f4: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 30f9: pop
      // 30fa: aload 36
      // 30EventMove: aload 37
      // 30fe: fload 43
      // 3100: fload 46
      // 3102: fsub
      // 3103: fload 38
      // 3105: fload 44
      // 3107: fload 47
      // 3109: fsub
      // 310a: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 310f: iload 48
      // 3111: iload 50
      // 3113: invokestatic q4/z/up.replAlpha (II)I
      // 3116: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 311b: pop
      // 311c: fload 38
      // 311e: fload 39
      // 3120: iload 41
      // 3122: i2f
      // 3123: ldc_w 0.125
      // 3126: fmul
      // 3127: fadd
      // 3128: fconst_1
      // 3129: frem
      // 312a: fmul
      // 312b: fstore 51
      // 312d: fload 51
      // 312f: fload 38
      // 3131: fdiv
      // 3132: f2d
      // 3133: ldc2_w 3.141592653589793
      // 3136: dmul
      // 3137: invokestatic java/lang/Math.sin (D)D
      // 313a: d2f
      // 313b: fstore 52
      // 313d: ldc_w 255.0
      // 3140: fload 52
      // 3142: fmul
      // 3143: fload 3
      // 3144: fmul
      // 3145: f2i
      // 3146: istore 53
      // 3148: iload 53
      // 314a: bipush 5
      // 314b: if_icmple 31f2
      // 314e: fload 40
      // 3150: ldc_w 2.2
      // 3153: fmul
      // 3154: fstore 54
      // 3156: ldc_w 0.08
      // 3159: fload 21
      // 315b: fmul
      // 315c: fstore 55
      // 315e: aload 36
      // 3160: aload 37
      // 3162: fload 43
      // 3164: fload 54
      // 3166: fsub
      // 3167: fload 51
      // 3169: fload 55
      // 316b: fsub
      // 316c: fload 44
      // 316e: fload 54
      // 3170: fsub
      // 3171: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 3176: iload 48
      // 3178: iload 53
      // 317a: invokestatic q4/z/up.replAlpha (II)I
      // 317d: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 3182: pop
      // 3183: aload 36
      // 3185: aload 37
      // 3187: fload 43
      // 3189: fload 54
      // 318b: fadd
      // 318c: fload 51
      // 318e: fload 55
      // 3190: fsub
      // 3191: fload 44
      // 3193: fload 54
      // 3195: fadd
      // 3196: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 319b: iload 48
      // 319d: iload 53
      // 319f: invokestatic q4/z/up.replAlpha (II)I
      // 31a2: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 31a7: pop
      // 31a8: aload 36
      // 31aa: aload 37
      // 31EventMotion: fload 43
      // 31ae: fload 54
      // 31b0: fadd
      // 31b1: fload 51
      // 31b3: fload 55
      // 31b5: fadd
      // 31b6: fload 44
      // 31b8: fload 54
      // 31ba: fadd
      // 31DraggableWidget: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 31c0: iload 48
      // 31c2: iload 53
      // 31c4: invokestatic q4/z/up.replAlpha (II)I
      // 31c7: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 31EventPacket: pop
      // 31cd: aload 36
      // 31cf: aload 37
      // 31d1: fload 43
      // 31d3: fload 54
      // 31d5: fsub
      // 31d6: fload 51
      // 31d8: fload 55
      // 31ConfigManager: fadd
      // 31db: fload 44
      // 31dd: fload 54
      // 31df: fsub
      // 31e0: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 31e5: iload 48
      // 31e7: iload 53
      // 31e9: invokestatic q4/z/up.replAlpha (II)I
      // 31EventAttack: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 31f1: pop
      // 31f2: iinc 41 1
      // 31f5: goto 3012
      // 31f8: aload 9
      // 31fa: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 31fd: aload 4
      // 31Category: getstatic ru/white/module/impl/render/yk.RING_LINE_LAYER Lnet/minecraft/client/render/RenderLayer;
      // 3202: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_73477 (Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;
      // 3205: astore 38
      // 3207: aload 9
      // 3209: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22903 ()V
      // 320c: aload 9
      // 320e: dload 26
      // 3210: dload 28
      // 3212: dload 30
      // 3214: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22904 (DDD)V
      // 3217: aload 9
      // 3219: invokevirtual net/minecraft/client/util/math/MatrixStack.method_23760 ()Lnet/minecraft/client/util/math/MatrixStack$Entry;
      // 321c: invokevirtual net/minecraft/client/util/math/MatrixStack$Entry.method_23761 ()Lorg/joml/Matrix4f;
      // 321f: astore 39
      // 3221: ldc_w -1169982228
      // 3224: ldc_w 1169982292
      // 3227: iadd
      // 3228: istore 40
      // 322a: bipush 0
      // 322b: istore 41
      // 322d: iload 41
      // 322f: iload 40
      // 3231: if_icmpge 32fe
      // 3234: iload 41
      // 3236: i2d
      // 3237: ldc2_w 3.141592653589793
      // 323a: dmul
      // 323b: ldc2_w 2.0
      // 323e: dmul
      // 323f: iload 40
      // 3241: i2d
      // 3242: ddiv
      // 3243: d2f
      // 3244: fstore 42
      // 3246: iload 41
      // 3248: bipush 1
      // 3249: iadd
      // 324a: i2d
      // 324b: ldc2_w 3.141592653589793
      // 324e: dmul
      // 324f: ldc2_w 2.0
      // 3252: dmul
      // 3253: iload 40
      // 3255: i2d
      // 3256: ddiv
      // 3257: d2f
      // 3258: fstore 43
      // 325a: fload 42
      // 325c: f2d
      // 325d: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 3260: fload 22
      // 3262: fmul
      // 3263: fstore 44
      // 3265: fload 42
      // 3267: f2d
      // 3268: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 326b: fload 22
      // 326d: fmul
      // 326e: fstore 45
      // 3270: fload 43
      // 3272: f2d
      // 3273: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 3276: fload 22
      // 3278: fmul
      // 3279: fstore 46
      // 327b: fload 43
      // 327d: f2d
      // 327e: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 3281: fload 22
      // 3283: fmul
      // 3284: fstore 47
      // 3286: iload 41
      // 3288: bipush 5
      // 3289: imul
      // 328a: invokestatic q4/z/up.fade (I)I
      // 328d: fload 3
      // 328e: invokestatic q4/z/up.multAlpha (IF)I
      // 3291: iload 7
      // 3293: aload 0
      // 3294: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 3297: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 329a: invokestatic q4/z/up.overCol (IIF)I
      // 329d: istore 48
      // 329f: iload 41
      // 32a1: bipush 1
      // 32a2: iadd
      // 32a3: bipush 5
      // 32a4: imul
      // 32a5: invokestatic q4/z/up.fade (I)I
      // 32a8: fload 3
      // 32a9: invokestatic q4/z/up.multAlpha (IF)I
      // 32EventMotion: iload 7
      // 32ae: aload 0
      // 32af: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 32b2: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 32b5: invokestatic q4/z/up.overCol (IIF)I
      // 32b8: istore 49
      // 32ba: aload 38
      // 32bc: aload 39
      // 32be: fload 44
      // 32c0: fconst_0
      // 32c1: fload 45
      // 32c3: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 32c8: iload 48
      // 32ConfigFile: ldc_w 180.0
      // 32cd: fload 3
      // 32ce: fmul
      // 32cf: f2i
      // 32d0: invokestatic q4/z/up.replAlpha (II)I
      // 32d3: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 32d8: pop
      // 32d9: aload 38
      // 32db: aload 39
      // 32dd: fload 46
      // 32df: fconst_0
      // 32e0: fload 47
      // 32e2: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 32e7: iload 49
      // 32e9: ldc_w 180.0
      // 32EventAttack: fload 3
      // 32ed: fmul
      // 32ee: f2i
      // 32ef: invokestatic q4/z/up.replAlpha (II)I
      // 32f2: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 32f7: pop
      // 32f8: iinc 41 1
      // 32fb: goto 322d
      // 32fe: ldc_w 1005924093
      // 3301: ldc_w -1005924077
      // 3304: iadd
      // 3305: istore 41
      // 3307: bipush 0
      // 3308: istore 42
      // 330a: iload 42
      // 330c: iload 41
      // 330e: if_icmpge 33f3
      // 3311: fload 19
      // 3313: iload 42
      // 3315: i2f
      // 3316: ldc_w 360.0
      // 3319: iload 41
      // 331b: i2f
      // 331c: fdiv
      // 331d: fmul
      // 331e: fadd
      // 331f: f2d
      // 3320: invokestatic java/lang/Math.toRadians (D)D
      // 3323: d2f
      // 3324: fstore 43
      // 3326: fload 43
      // 3328: f2d
      // 3329: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 332c: fstore 44
      // 332e: fload 43
      // 3330: f2d
      // 3331: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 3334: fstore 45
      // 3336: iload 42
      // 3338: bipush 4
      // 3339: irem
      // 333a: ifne 3341
      // 333d: bipush 1
      // 333e: goto 3342
      // 3341: bipush 0
      // 3342: istore 46
      // 3344: fload 22
      // 3346: iload 46
      // 3348: ifeq 3351
      // 334b: ldc_w 0.12
      // 334e: goto 3354
      // 3351: ldc_w 0.06
      // 3354: fload 21
      // 3356: fmul
      // 3357: fsub
      // 3358: fstore 47
      // 335a: fload 22
      // 335c: iload 46
      // 335e: ifeq 3367
      // 3361: ldc_w 0.06
      // 3364: goto 336a
      // 3367: ldc_w 0.02
      // 336a: fload 21
      // 336c: fmul
      // 336d: fadd
      // 336e: fstore 48
      // 3370: iload 42
      // 3372: ldc_w 1999124232
      // 3375: ldc_w -1999124212
      // 3378: iadd
      // 3379: imul
      // 337a: invokestatic q4/z/up.fade (I)I
      // 337d: fload 3
      // 337e: invokestatic q4/z/up.multAlpha (IF)I
      // 3381: iload 7
      // 3383: aload 0
      // 3384: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 3387: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 338a: invokestatic q4/z/up.overCol (IIF)I
      // 338d: istore 49
      // 338f: iload 46
      // 3391: ifeq 339e
      // 3394: ldc_w 1230469922
      // 3397: ldc_w -1230469682
      // 339a: iadd
      // 339b: goto 33a5
      // 339e: ldc_w 2117841288
      // 33a1: ldc_w -2117841148
      // 33a4: iadd
      // 33a5: i2f
      // 33a6: fload 3
      // 33a7: fmul
      // 33a8: f2i
      // 33a9: istore 50
      // 33ab: aload 38
      // 33ad: aload 39
      // 33af: fload 44
      // 33b1: fload 47
      // 33b3: fmul
      // 33b4: fconst_0
      // 33b5: fload 45
      // 33b7: fload 47
      // 33b9: fmul
      // 33ba: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 33bf: iload 49
      // 33c1: iload 50
      // 33c3: invokestatic q4/z/up.replAlpha (II)I
      // 33c6: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 33cb: pop
      // 33EventPacket: aload 38
      // 33ce: aload 39
      // 33d0: fload 44
      // 33d2: fload 48
      // 33d4: fmul
      // 33d5: fconst_0
      // 33d6: fload 45
      // 33d8: fload 48
      // 33ConfigManager: fmul
      // 33db: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 33e0: iload 49
      // 33e2: iload 50
      // 33e4: invokestatic q4/z/up.replAlpha (II)I
      // 33e7: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 33EventAttack: pop
      // 33ed: iinc 42 1
      // 33f0: goto 330a
      // 33f3: bipush 0
      // 33f4: istore 42
      // 33f6: iload 42
      // 33f8: bipush 2
      // 33f9: if_icmpge 3504
      // 33EventMove: iload 42
      // 33fe: i2f
      // 33Category: ldc_w 45.0
      // 3402: fmul
      // 3403: fstore 43
      // 3405: bipush 0
      // 3406: istore 44
      // 3408: iload 44
      // 340a: bipush 4
      // 340b: if_icmpge 34fe
      // 340e: fload 32
      // 3410: fload 43
      // 3412: fadd
      // 3413: iload 44
      // 3415: i2f
      // 3416: ldc_w 90.0
      // 3419: fmul
      // 341a: fadd
      // 341b: f2d
      // 341c: invokestatic java/lang/Math.toRadians (D)D
      // 341f: d2f
      // 3420: fstore 45
      // 3422: fload 32
      // 3424: fload 43
      // 3426: fadd
      // 3427: iload 44
      // 3429: bipush 1
      // 342a: iadd
      // 342b: i2f
      // 342c: ldc_w 90.0
      // 342f: fmul
      // 3430: fadd
      // 3431: f2d
      // 3432: invokestatic java/lang/Math.toRadians (D)D
      // 3435: d2f
      // 3436: fstore 46
      // 3438: fload 45
      // 343a: f2d
      // 343b: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 343e: fload 23
      // 3440: fmul
      // 3441: fstore 47
      // 3443: fload 45
      // 3445: f2d
      // 3446: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 3449: fload 23
      // 344b: fmul
      // 344c: fstore 48
      // 344e: fload 46
      // 3450: f2d
      // 3451: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 3454: fload 23
      // 3456: fmul
      // 3457: fstore 49
      // 3459: fload 46
      // 345b: f2d
      // 345c: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 345f: fload 23
      // 3461: fmul
      // 3462: fstore 50
      // 3464: iload 42
      // 3466: ldc_w -1988803922
      // 3469: ldc_w 1988804012
      // 346c: iadd
      // 346d: imul
      // 346e: iload 44
      // 3470: ldc_w -375400789
      // 3473: ldc_w 375400834
      // 3476: iadd
      // 3477: imul
      // 3478: iadd
      // 3479: invokestatic q4/z/up.fade (I)I
      // 347c: fload 3
      // 347d: invokestatic q4/z/up.multAlpha (IF)I
      // 3480: iload 7
      // 3482: aload 0
      // 3483: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 3486: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 3489: invokestatic q4/z/up.overCol (IIF)I
      // 348c: istore 51
      // 348e: iload 42
      // 3490: ldc_w 1607477583
      // 3493: ldc_w -1607477493
      // 3496: iadd
      // 3497: imul
      // 3498: iload 44
      // 349a: bipush 1
      // 349b: iadd
      // 349c: ldc_w -1140693964
      // 349f: ldc_w 1140694009
      // 34a2: iadd
      // 34a3: imul
      // 34a4: iadd
      // 34a5: invokestatic q4/z/up.fade (I)I
      // 34a8: fload 3
      // 34a9: invokestatic q4/z/up.multAlpha (IF)I
      // 34EventMotion: iload 7
      // 34ae: aload 0
      // 34af: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 34b2: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 34b5: invokestatic q4/z/up.overCol (IIF)I
      // 34b8: istore 52
      // 34ba: aload 38
      // 34bc: aload 39
      // 34be: fload 47
      // 34c0: fconst_0
      // 34c1: fload 48
      // 34c3: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 34c8: iload 51
      // 34ConfigFile: ldc_w 220.0
      // 34cd: fload 3
      // 34ce: fmul
      // 34cf: f2i
      // 34d0: invokestatic q4/z/up.replAlpha (II)I
      // 34d3: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 34d8: pop
      // 34d9: aload 38
      // 34db: aload 39
      // 34dd: fload 49
      // 34df: fconst_0
      // 34e0: fload 50
      // 34e2: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 34e7: iload 52
      // 34e9: ldc_w 220.0
      // 34EventAttack: fload 3
      // 34ed: fmul
      // 34ee: f2i
      // 34ef: invokestatic q4/z/up.replAlpha (II)I
      // 34f2: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 34f7: pop
      // 34f8: iinc 44 1
      // 34fb: goto 3408
      // 34fe: iinc 42 1
      // 3501: goto 33f6
      // 3504: bipush 0
      // 3505: istore 42
      // 3507: iload 42
      // 3509: iload 35
      // 350b: if_icmpge 35e4
      // 350e: iload 42
      // 3510: i2d
      // 3511: ldc2_w 3.141592653589793
      // 3514: dmul
      // 3515: ldc2_w 2.0
      // 3518: dmul
      // 3519: iload 35
      // 351b: i2d
      // 351c: ddiv
      // 351d: d2f
      // 351e: fstore 43
      // 3520: iload 42
      // 3522: bipush 1
      // 3523: iadd
      // 3524: i2d
      // 3525: ldc2_w 3.141592653589793
      // 3528: dmul
      // 3529: ldc2_w 2.0
      // 352c: dmul
      // 352d: iload 35
      // 352f: i2d
      // 3530: ddiv
      // 3531: d2f
      // 3532: fstore 44
      // 3534: fload 43
      // 3536: f2d
      // 3537: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 353a: fload 34
      // 353c: fmul
      // 353d: fstore 45
      // 353f: fload 43
      // 3541: f2d
      // 3542: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 3545: fload 34
      // 3547: fmul
      // 3548: fstore 46
      // 354a: fload 44
      // 354c: f2d
      // 354d: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 3550: fload 34
      // 3552: fmul
      // 3553: fstore 47
      // 3555: fload 44
      // 3557: f2d
      // 3558: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 355b: fload 34
      // 355d: fmul
      // 355e: fstore 48
      // 3560: iload 42
      // 3562: ldc_w 2054396086
      // 3565: ldc_w -2054396071
      // 3568: iadd
      // 3569: imul
      // 356a: invokestatic q4/z/up.fade (I)I
      // 356d: fload 3
      // 356e: invokestatic q4/z/up.multAlpha (IF)I
      // 3571: iload 7
      // 3573: aload 0
      // 3574: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 3577: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 357a: invokestatic q4/z/up.overCol (IIF)I
      // 357d: istore 49
      // 357f: iload 42
      // 3581: bipush 1
      // 3582: iadd
      // 3583: ldc_w -1781431511
      // 3586: ldc_w 1781431526
      // 3589: iadd
      // 358a: imul
      // 358b: invokestatic q4/z/up.fade (I)I
      // 358e: fload 3
      // 358f: invokestatic q4/z/up.multAlpha (IF)I
      // 3592: iload 7
      // 3594: aload 0
      // 3595: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 3598: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 359b: invokestatic q4/z/up.overCol (IIF)I
      // 359e: istore 50
      // 35a0: aload 38
      // 35a2: aload 39
      // 35a4: fload 45
      // 35a6: fconst_0
      // 35a7: fload 46
      // 35a9: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 35ae: iload 49
      // 35b0: ldc_w 255.0
      // 35b3: fload 3
      // 35b4: fmul
      // 35b5: f2i
      // 35b6: invokestatic q4/z/up.replAlpha (II)I
      // 35b9: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 35be: pop
      // 35bf: aload 38
      // 35c1: aload 39
      // 35c3: fload 47
      // 35c5: fconst_0
      // 35c6: fload 48
      // 35c8: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 35cd: iload 50
      // 35cf: ldc_w 255.0
      // 35d2: fload 3
      // 35d3: fmul
      // 35d4: f2i
      // 35d5: invokestatic q4/z/up.replAlpha (II)I
      // 35d8: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 35dd: pop
      // 35de: iinc 42 1
      // 35e1: goto 3507
      // 35e4: iload 25
      // 35e6: ifeq 3696
      // 35e9: aload 0
      // 35ea: getfield ru/white/module/impl/render/yk.target Lnet/minecraft/entity/LivingEntity;
      // 35ed: invokevirtual net/minecraft/entity/LivingEntity.method_17682 ()F
      // 35f0: aload 0
      // 35f1: getfield ru/white/module/impl/render/yk.mandalaHeight Lq2/z/SliderSetting;
      // 35f4: invokevirtual q2/z/bg.getValue ()Ljava/lang/Float;
      // 35f7: invokevirtual java/lang/Float.floatValue ()F
      // 35fa: fmul
      // 35fb: fstore 42
      // 35fd: bipush 0
      // 35fe: istore 43
      // 3600: iload 43
      // 3602: ldc_w -1111046435
      // 3605: ldc_w 1111046443
      // 3608: iadd
      // 3609: if_icmpge 3696
      // 360c: fload 32
      // 360e: iload 43
      // 3610: i2f
      // 3611: ldc_w 45.0
      // 3614: fmul
      // 3615: fadd
      // 3616: f2d
      // 3617: invokestatic java/lang/Math.toRadians (D)D
      // 361a: d2f
      // 361b: fstore 44
      // 361d: fload 44
      // 361f: f2d
      // 3620: invokestatic net/minecraft/util/math/MathHelper.method_15362 (D)F
      // 3623: fload 23
      // 3625: fmul
      // 3626: fstore 45
      // 3628: fload 44
      // 362a: f2d
      // 362b: invokestatic net/minecraft/util/math/MathHelper.method_15374 (D)F
      // 362e: fload 23
      // 3630: fmul
      // 3631: fstore 46
      // 3633: iload 43
      // 3635: ldc_w -148412510
      // 3638: ldc_w 148412555
      // 363b: iadd
      // 363c: imul
      // 363d: invokestatic q4/z/up.fade (I)I
      // 3640: fload 3
      // 3641: invokestatic q4/z/up.multAlpha (IF)I
      // 3644: iload 7
      // 3646: aload 0
      // 3647: getfield ru/white/module/impl/render/yk.alpha_2 Lru/white/utils/animation/Animation;
      // 364a: invokevirtual ru/white/utils/animation/Animation.get ()F
      // 364d: invokestatic q4/z/up.overCol (IIF)I
      // 3650: istore 47
      // 3652: aload 38
      // 3654: aload 39
      // 3656: fload 45
      // 3658: fconst_0
      // 3659: fload 46
      // 365b: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 3660: iload 47
      // 3662: ldc_w 230.0
      // 3665: fload 3
      // 3666: fmul
      // 3667: f2i
      // 3668: invokestatic q4/z/up.replAlpha (II)I
      // 366b: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 3670: pop
      // 3671: aload 38
      // 3673: aload 39
      // 3675: fload 45
      // 3677: fload 42
      // 3679: ldc_w 0.75
      // 367c: fmul
      // 367d: fload 46
      // 367f: invokeinterface net/minecraft/client/render/VertexConsumer.method_22918 (Lorg/joml/Matrix4fc;FFF)Lnet/minecraft/client/render/VertexConsumer; 5
      // 3684: iload 47
      // 3686: bipush 0
      // 3687: invokestatic q4/z/up.replAlpha (II)I
      // 368a: invokeinterface net/minecraft/client/render/VertexConsumer.method_39415 (I)Lnet/minecraft/client/render/VertexConsumer; 2
      // 368f: pop
      // 3690: iinc 43 1
      // 3693: goto 3600
      // 3696: aload 9
      // 3698: invokevirtual net/minecraft/client/util/math/MatrixStack.method_22909 ()V
      // 369b: aload 4
      // 369d: invokevirtual net/minecraft/client/render/VertexConsumerProvider$Immediate.method_22993 ()V
      // 36a0: return
   }

   @Override
   public boolean previewNeedsDummy() {
      return true;
   }

   @Override
   public lf previewSettings() {

      return this.f786;
   }

   private static void m881(VertexConsumer buffer, Matrix4f matrix, int color, int color2, int color3, int color4, int alpha) {
      buffer.method_22918(matrix, -0.5F, -0.5F, 0.0F)
         .method_39415(up.replAlpha(color, alpha))
         .method_22913(0.0F, 1.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(matrix, 0.5F, -0.5F, 0.0F)
         .method_39415(up.replAlpha(color2, alpha))
         .method_22913(1.0F, 1.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);

      buffer.method_22918(matrix, 0.5F, 0.5F, 0.0F)
         .method_39415(up.replAlpha(color3, alpha))
         .method_22913(1.0F, 0.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
      buffer.method_22918(matrix, -0.5F, 0.5F, 0.0F)
         .method_39415(up.replAlpha(color4, alpha))
         .method_22913(0.0F, 0.0F)
         .method_22922(OverlayTexture.field_21444)
         .method_60803(15728880)
         .method_22914(0.0F, 0.0F, 1.0F);
   }

   private static void m883(VertexConsumer buf, Matrix4f m, float s, int color) {
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      float temp2 = -s;

      buf.method_22918(m, temp2, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      VertexConsumer result = buf.method_22918(m, s, s, s);

      result.method_39415(color);
      temp2 = -s;

      buf.method_22918(m, temp2, -s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
   }

   public TargetEsp() {
      String[] temp5 = new String[8];
      temp5[0] = "Призраки";
      temp5[1] = "Картинка";
      temp5[2] = "Кольцо";
      temp5[3] = "Бублик";
      temp5[4] = "Кубики";
      temp5[5] = "Гироскоп";
      temp5[6] = "Мандала";
      temp5[7] = "Молнии";
      this.type = new ModeSetting(this, "Режим", temp5);
      this.typeGhost = new ModeSetting(this, "Тип призраков", "1", "2", "3", "4").setVisible(() -> {
         int temp1 = v1 * (v1 + 1) % 2;

         if (temp1 != 0) {
            }

         return this.type.is("Призраки");
      });
      this.typeImages = new ModeSetting(this, "Тип картинки", "1", "2", "3", "4").setVisible(() -> {
         Boolean result = this.type.is("Картинка");

         return result;
      });
      this.speed = new SliderSetting(this, "Скорость призраков", 900.0F, 400.0F, 1500.0F, 50.0F)
         .setVisible(() -> {
            int temp1 = v1 * (v1 + 1) % 2;

            if (temp1 != 0) {
               }

            return this.type.is("Призраки");
         });
      this.sizeGlow = new SliderSetting(this, "Сила свечения", 0.35F, 0.1F, 0.75F, 0.05F).setVisible(() -> {

         return this.type.is("Призраки");
      });
      this.sizeGlowOFF = new SliderSetting(this, "Размер свечения", 0.7F, 0.1F, 2.0F, 0.05F).setVisible(() -> {
         boolean result = this.type.is("Призраки");

         return result;
      });
      this.speed_2 = new SliderSetting(this, "Скорость картинки", 1000.0F, 500.0F, 3000.0F, 100.0F)
         .setVisible(() -> this.type.is("Картинка"));
      this.size_2 = new SliderSetting(this, "Размер картинки", 1.0F, 0.25F, 3.0F, 0.05F).setVisible(() -> {
         Boolean result = this.type.is("Картинка");

         return result;
      });
      this.ringSpeed = new SliderSetting(this, "Скорость кольца", 1800.0F, 600.0F, 4000.0F, 100.0F)
         .setVisible(() -> {

            return this.type.is("Кольцо");
         });
      this.bublikSpeed = new SliderSetting(this, "Скорость бублика", 1400.0F, 600.0F, 4000.0F, 100.0F)
         .setVisible(() -> {
            int temp2 = v1 + 1;

            return this.type.is("Бублик");
         });
      this.bublikSize = new SliderSetting(this, "Размер бублика", 1.0F, 0.5F, 2.5F, 0.05F)
         .setVisible(() -> this.type.is("Бублик"));
      this.gyroSpeed = new SliderSetting(this, "Скорость гироскопа", 1800.0F, 600.0F, 4000.0F, 100.0F)
         .setVisible(() -> {
            boolean result = this.type.is("Гироскоп");

            return result;
         });
      this.gyroSize = new SliderSetting(this, "Размер гироскопа", 1.0F, 0.4F, 2.0F, 0.05F)
         .setVisible(() -> {

            return this.type.is("Гироскоп");
         });
      this.gyroCore = new BooleanSetting(this, "Ядро", true).setVisible(() -> {

         return this.type.is("Гироскоп");
      });
      this.mandalaSpeed = new SliderSetting(this, "Скорость мандалы", 2200.0F, 600.0F, 5000.0F, 100.0F)
         .setVisible(() -> {

            return this.type.is("Мандала");
         });
      this.mandalaSize = new SliderSetting(this, "Размер мандалы", 1.1F, 0.5F, 2.2F, 0.05F).setVisible(() -> {

         return this.type.is("Мандала");
      });
      this.mandalaHeight = new SliderSetting(this, "Высота лучей", 1.0F, 0.2F, 2.0F, 0.1F)
         .setVisible(() -> this.type.is("Мандала"));
      this.mandalaBeams = new BooleanSetting(this, "Лазерные лучи", true).setVisible(() -> {

         return this.type.is("Мандала");
      });
      this.lightningColor = new tf(this, "Цвет молнии", -3416833).setVisible(() -> this.type.is("Молнии"));
      this.lightningWidth = new SliderSetting(this, "Ширина молнии", 1.6F, 0.5F, 5.0F, 0.1F).setVisible(() -> {

         return this.type.is("Молнии");
      });
      this.lightningBranches = new SliderSetting(this, "Ветви молнии", 3.0F, 0.0F, 4.0F, 1.0F).setVisible(() -> {
         int temp2 = v1 + 1;

         return this.type.is("Молнии");
      });
      this.f786 = lf.of(this, 4.0F, 0.0F, 2.0F);
      this.f787 = new BufferAllocator(262144);
      this.target = null;
      this.alpha = new Animation();
      this.alpha_2 = new Animation();
      this.f789 = 0.0F;
      this.f790 = 0L;
   }

   @Override
   public void previewSpawn(kf ctx) {
      if (ctx.dummy() != null) {
         ctx.dummy().field_6235 = 10;
         if (this.type.is("Молнии")) {

            Vec3d v2 = ctx.dummy().method_73189().method_1031(0.0, (double)ctx.dummy().method_17682() * 0.5, 0.0);
            mt.strikeAt(v2);
         }
      }
   }

   private static void m882(VertexConsumer buf, Matrix4f m, float s, int color) {
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);

      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
      float temp2 = -s;
      float temp3 = -s;

      buf.method_22918(m, temp2, temp3, s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      VertexConsumer result = buf.method_22918(m, -s, s, s);

      result.method_39415(color);
      buf.method_22918(m, -s, -s, s).method_39415(color);
      buf.method_22918(m, s, -s, s).method_39415(color);
      buf.method_22918(m, s, s, s).method_39415(color);
      buf.method_22918(m, -s, s, s).method_39415(color);
      buf.method_22918(m, s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, -s, -s).method_39415(color);
      buf.method_22918(m, -s, s, -s).method_39415(color);
      buf.method_22918(m, s, s, -s).method_39415(color);
   }

   private static void m880(VertexConsumer buffer, Matrix4f matrix, 

      int v4 = color >> 16 & 255;
      int v5 = color >> 8 & 255;
      int v6 = color & 255;
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {
      this.f787.method_60809();
      mt.clear();
      
   }

   
}
