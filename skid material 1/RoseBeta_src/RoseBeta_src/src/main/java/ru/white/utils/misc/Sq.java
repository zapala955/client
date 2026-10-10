package ru.white.utils.misc;

import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.FloatControl.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class Sq {
   private static volatile float f1278 = 0.0F;
   private static final int FADE_MS = 500;
   private static volatile Clip f1277 = null;
   private static final int FADE_STEPS = 60;
   private static volatile int f1279 = 0;
   private static final String RESOURCE = "/assets/client/sound/gui/gui_bg.wav";

   public static void stop() {
      synchronized (sq.class) {
         f1279++;
         Clip v1 = f1277;
         if (v1 != null) {
            v1.stop();
            v1.close();
            f1277 = null;
         }

         
      }
   }

   private static void m1611(Clip clip, float target) throws InterruptedException {
      long v2 = 2932501773054077673L + -2932501773054077665L;

      int v4 = 1;

      while (true) {

         if (v4 > 60 || !clip.isOpen()) {
            if (clip.isOpen()) {
               m1612(clip, target);
            }

            return;
         }

         float v5 = (float)v4 / 60.0F;
         float v6 = 1.0F - (1.0F - v5) * (1.0F - v5);
         m1612(clip, v6 * target);
         Thread.sleep(v2);
         v4++;
      }
   }

   public static void setVolume(float volume) {
      f1278 = volume;
      Clip v1 = f1277;
      if (v1 != null && v1.isOpen()) {

         m1612(v1, volume);
      }
   }

   public static void start(float volume) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
      // java.lang.NullPointerException: Cannot invoke "org.jetbrains.java.decompiler.util.collections.fixed.FastFixedSet.contains(Object)" because "predset" is null
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.FastExtendedPostdominanceHelper.lambda$removeErroneousNodes$1(FastExtendedPostdominanceHelper.java:231)
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.FastExtendedPostdominanceHelper.iterateReachability(FastExtendedPostdominanceHelper.java:373)
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.FastExtendedPostdominanceHelper.removeErroneousNodes(FastExtendedPostdominanceHelper.java:207)
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.FastExtendedPostdominanceHelper.getExtendedPostdominators(FastExtendedPostdominanceHelper.java:63)
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.DomHelper.findGeneralStatement(DomHelper.java:516)
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.DomHelper.processStatement(DomHelper.java:451)
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.DomHelper.processStatement(DomHelper.java:358)
      //   at org.jetbrains.java.decompiler.modules.decompiler.decompose.DomHelper.parseGraph(DomHelper.java:208)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:166)
      //
      // Bytecode:
      // 00: ldc q2/z/sq
      // 02: dup
      // 03: astore 1
      // 04: monitorenter
      // 05: getstatic q2/z/sq.f1277 Ljavax/sound/sampled/Clip;
      // 08: ifnull 2b
      // 0b: getstatic q2/z/sq.f1277 Ljavax/sound/sampled/Clip;
      // 0e: invokeinterface javax/sound/sampled/Clip.isRunning ()Z 1
      // 13: ifeq 2b
      // 16: ldc 270463226
      // 18: istore 4
      // 1a: iload 4
      // 1c: dup
      // 1d: bipush 1
      // 1e: iadd
      // 1f: imul
      // 20: bipush 2
      // 21: irem
      // 22: ifeq 27
      // 25: aconst_null
      // 26: pop
      // 27: nop
      // 28: aload 1
      // 29: monitorexit
      // 2a: return
      // 2b: getstatic q2/z/sq.f1279 I
      // 2e: bipush 1
      // 2f: iadd
      // 30: putstatic q2/z/sq.f1279 I
      // 33: aload 1
      // 34: monitorexit
      // 35: goto 67
      // 38: nop
      // 39: nop
      // 3a: athrow
      // 3b: nop
      // 3c: nop
      // 3d: nop
      // 3e: nop
      // 3f: nop
      // 40: nop
      // 41: nop
      // 42: nop
      // 43: nop
      // 44: athrow
      // 45: nop
      // 46: nop
      // 47: nop
      // 48: nop
      // 49: nop
      // 4a: nop
      // 4b: nop
      // 4c: nop
      // 4d: nop
      // 4e: athrow
      // 4f: athrow
      // 50: astore 2
      // 51: aload 1
      // 52: monitorexit
      // 53: ldc -687980903
      // 55: istore 5
      // 57: iload 5
      // 59: dup
      // 5a: bipush 1
      // 5b: iadd
      // 5c: imul
      // 5d: bipush 2
      // 5e: irem
      // 5f: ifeq 64
      // 62: aconst_null
      // 63: pop
      // 64: nop
      // 65: aload 2
      // 66: athrow
      // 67: getstatic q2/z/sq.f1279 I
      // 6a: istore 1
      // 6b: fload 0
      // 6c: putstatic q2/z/sq.f1278 F
      // 6f: iload 1
      // 70: fload 0
      // 71: invokedynamic run (IF)Ljava/lang/Runnable; bsm=java/lang/invoke/LambdaMetafactory.metafactory (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite; args=[ ()V, q2/z/sq.m1613 (IF)V, ()V ]
      // 76: invokestatic java/util/concurrent/CompletableFuture.runAsync (Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;
      // 79: pop
      // 7a: return
   }

   private Sq() {
   }

   private static void m1612(Clip clip, float volume) {
      if (clip.isControlSupported(Type.MASTER_GAIN)) {
         Control result = clip.getControl(Type.MASTER_GAIN);

         FloatControl v2 = (FloatControl)result;
         float v3 = (float)(Math.log10(volume <= 1.0E-4F ? 1.0E-4F : (double)volume) * 20.0);
         int v5 = 965476373;

         v2.setValue(Math.max(v2.getMinimum(), Math.min(v2.getMaximum(), v3)));
      }
   }

   
}
