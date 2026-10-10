package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.animation.ap;
import ru.white.event.impl.EventPacket;
import ru.white.event.impl.EventFOV;

@ModuleAnnotation(
   name = "Swing Animation",
   category = Category.RENDER,
   desc = "Анимации взмаха руки"
)
@Environment(EnvType.CLIENT)
public class SwingAnimation extends Module {
   public ModeSetting typeSwing;
   public SliderSetting angle;
   public SliderSetting animSpeed;
   public ModeSetting easeSwin;
   public SliderSetting swingPowessss;
   public BooleanSetting auraOnly;
   public ModeSetting swingMode;
   public SliderSetting animSize;

   public static xk get() {
      xk result = uq.get(xk.class);

      return result;
   }

   public static void interactItem(Hand hand) {
      mc.field_1724.method_6104(Hand.field_5808);

      sendSequencedPacket(id -> {
         PlayerInteractItemC2SPacket var10000x = new PlayerInteractItemC2SPacket;
         float temp4 = mc.field_1724.method_36454();
         float temp5 = mc.field_1724.method_36455();
         int v2x = -184172428;
         int var10006 = v2x * (v2x + 1);

         var10000x./* $VF: Unable to resugar constructor */<init>(hand, id, temp4, temp5);
         return var10000x;
      });
   }

   public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {

      mc.field_1761.method_41931(mc.field_1687, packetCreator);
   }

   @EventTarget
   public void onSwingDuration(EventFOV e) {
      if (this.auraCheck()) {
         Item v2 = mc.field_1724.method_5998(Hand.field_5808).method_7909();
         if (v2 != Items.field_8162) {

            float v3 = this.animSpeed.getValue();
            e.setAnimation(v3);
            e.cancel();
         }
      }
   }

   public SwingAnimation() {
      String temp4 = "Режим анимации";
      String[] temp5 = new String[7];
      temp5[0] = "Первый";
      temp5[1] = "Второй";
      temp5[2] = "Третий";
      temp5[3] = "Четвёртый";
      temp5[4] = "Пятый";
      temp5[5] = "Шестой";
      temp5[6] = "Translate";
      this.swingMode = new ModeSetting(this, temp4, temp5);
      this.typeSwing = new ModeSetting(this, "Тип", "Первый", "Второй").setVisible(() -> {

         return this.swingMode.is("Третий");
      });
      this.swingPowessss = new SliderSetting(this, "Угол", 90.0F, -180.0F, 180.0F, 1.0F).setVisible(() -> {

         return this.swingMode.is("Третий");
      });
      this.angle = new SliderSetting(this, "Угол наклона", 40.0F, 0.0F, 70.0F, 1.0F)
         .setVisible(() -> this.swingMode.is("Translate"));
      this.easeSwin = new ModeSetting(this, "Режим интерполяции", "Первый", "Второй", "Третий");
      this.auraOnly = new BooleanSetting(this, "Только с аурой", false);
      this.animSpeed = new SliderSetting(this, "Скорость анимации", 1.0F, 0.1F, 4.0F, 0.1F);
      this.animSize = new SliderSetting(this, "Сила анимации", 6.0F, 0.1F, 10.0F, 0.1F);
   }

   public boolean auraCheck() {

      if (!this.auraOnly.getValue()) {
         return true;
      } else {
         fg v1 = fg.get();
         return v1 != null && v1.isEnabled() && fg.target != null;
      }
   }

   @EventTarget
   public void onEvent(EventPacket event) {
      if (this.isEnabled()) {
         if (this.auraCheck()) {
            boolean v2 = event.getArm() == Arm.field_6183;
            ModeSetting v3 = this.swingMode;
            if (event.getArm() == mc.field_1724.method_6068()) {
               ItemStack v4 = mc.field_1724.method_5998(event.getHand());
               MatrixStack v5 = event.getMatrices();
               float v6 = event.getSwingProgress();
               int v7 = v2 ? 1 : -1;
               float v8;
               if (this.easeSwin.is("Первый")) {
                  v8 = (float)ap.SINE_OUT.ease((double)v6);
               } else if (this.easeSwin.is("Третий")) {
                  v8 = v6;
               } else {
                  v8 = (float)ap.BACK_OUT.ease((double)v6);
               }

               float v10 = MathHelper.method_15374((double)(v8 * v8 * (float) Math.PI));
               float v11 = MathHelper.method_15374((double)(MathHelper.method_15355(v8) * (float) Math.PI));
               float v12 = v10 * v11;
               if (this.easeSwin.is("Третий")) {
                  v12 = (float)Math.sin((double)v6 * (Math.PI / 2) * 2.0);
               }

               float v13 = this.animSize.getValue();
               Arm v14 = event.getArm();
               String v15 = v3.getValue();
               switch (v15.hashCode()) {
                  case -1238167250:
                     if (v15.equals("Translate")) {
                        v16 = 6;
                     }
                     break;
                  case 806875501:
                     if (v15.equals("Второй")) {
                        v16 = 1;
                     }
                     break;
                  case 1008256768:
                     if (v15.equals("Пятый")) {
                        v16 = 4;
                     }
                     break;
                  case 1167095222:
                     if (v15.equals("Первый")) {
                        v16 = 0;
                     }
                     break;
                  case 1262828492:
                     if (v15.equals("Третий")) {
                        v16 = 2;
                     }
                     break;
                  case 1380603331:
                     if (v15.equals("Четвёртый")) {
                        v16 = 3;
                     }
                     break;
                  case 1424802345:
                     if (v15.equals("Шестой")) {
                        v16 = 5;
                     }
               }

               switch (v16) {
                  case 0:
                     v5.method_46416((float)v7 * 0.56F, -0.52F, -0.72F);

                     v5.method_22907(RotationAxis.field_40714.rotationDegrees(v12 * -180.0F * v13 / 10.0F));
                     v5.method_22907(RotationAxis.field_40717.rotationDegrees(v12 * (v2 ? -60.0F * v13 / 10.0F : 60.0F * v13 / 10.0F)));
                     v5.method_22907(RotationAxis.field_40716.rotationDegrees(v12 * (v2 ? -60.0F * v13 / 10.0F : 60.0F * v13 / 10.0F)));
                     break;
                  case 1:
                     v5.method_46416((float)v7 * 0.56F, -0.42F, -0.72F);
                     v5.method_22907(RotationAxis.field_40714.rotationDegrees(v12 * -(24.0F * v13)));
                     v5.method_22904(0.0, -0.1, 0.0);
                     break;
                  case 2:
                     v5.method_22904((double)((float)v7 * 0.7F), -0.35F, -0.9);
                     v5.method_22907(RotationAxis.field_40716.rotationDegrees((float)(v7 * (60))));
                     v5.method_22907(RotationAxis.field_40718.rotationDegrees((float)(v7 * (-60))));
                     v5.method_22907(
                        RotationAxis.field_40714
                           .rotationDegrees(-this.swingPowessss.getValue() - (this.typeSwing.is("Первый") ? v13 * 24.0F * v12 : -(v13 * 24.0F) * v12))
                     );
                     break;
                  case 3:
                     v5.method_22904(
                        (double)v7 * (0.2F - 0.1 * (double)v12 * (double)(0.25F * v13)), 0.0, -0.2F - 0.2 * (double)v12 * (double)(0.25F * v13)
                     );
                     this.m875(v5, v14, 0.0F);
                     v5.method_22907(RotationAxis.field_40714.rotationDegrees(50.0F - 4.0F * v12 * 0.35F * v13));
                     float v23 = (float)v7;

                     v5.method_22907(RotationAxis.field_40716.rotationDegrees(v23 * (-60.0F + 0.6F * v13 * v12)));
                     v5.method_22907(RotationAxis.field_40718.rotationDegrees((float)v7 * (110.0F + 40.0F * v12 * 0.2F * v13)));
                     break;
                  case 4:
                     v5.method_46416((float)v7 * 0.56F, -0.42F, -0.72F);
                     v5.method_22907(RotationAxis.field_40714.rotationDegrees(0.0F + v8 * 360.0F));
                     v5.method_22904(0.0, -0.1, 0.0);
                     break;
                  case 5:
                     v5.method_22904((double)((float)v7 * 0.7F), -0.35F, -0.9);
                     v5.method_22907(RotationAxis.field_40716.rotationDegrees((float)(v7 * (65))));
                     v5.method_22907(RotationAxis.field_40718.rotationDegrees((float)(v7 * (-10))));
                     v5.method_22907(RotationAxis.field_40714.rotationDegrees(-90.0F - v13 * 24.0F * v12));
                     break;
                  case 6:
                     v5.method_46416((float)v7 * 0.6F, 0.0F, -0.8F);
                     v5.method_46416((float)v7 * 0.56F, -1.0F, -0.82F);
                     v5.method_22907(RotationAxis.field_40716.rotationDegrees((float)v7 * 90.0F));
                     float temp2 = (float)v7;

                     v5.method_22907(RotationAxis.field_40718.rotationDegrees(temp2 * -60.0F));
                     float v17 = MathHelper.method_15363(v6, 0.0F, 1.0F);
                     float v18 = v13 / 6.0F * 70.0F;
                     float v19 = -this.angle.getValue() - 40.0F * MathHelper.method_15374((double)(v17 * (float) Math.PI)) * v18 / 50.0F;
                     v5.method_22907(RotationAxis.field_40714.rotationDegrees(v19));
                     v5.method_22907(RotationAxis.field_40714.rotationDegrees(-20.0F));
                     v5.method_46416((float)v7 * -0.5F, 0.08F, 0.0F);
               }

               event.cancel();
            }
         }
      }
   }

   public void onEatAnimsHands(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player) {
      float v6 = (float)player.method_6014() - tickDelta + 1.0F;
      float v7 = v6 / (float)stack.method_7935(player);
      if (v7 < 0.8F) {
         float v8 = MathHelper.method_15379(MathHelper.method_15362((double)(v6 / 5.0F * (float) Math.PI)) * 0.015F);
         matrices.method_46416(0.0F, v8, 0.0F);
      }

      float v13 = 1.0F - (float)Math.pow((double)v7, 27.0);
      int v9 = arm == Arm.field_6183 ? 1 : -1;
      float temp1 = v13 * 0.6F;

      matrices.method_46416(temp1 * (float)v9, v13 * -0.5F, v13 * 0.0F);
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)v9 * v13 * 90.0F));
      matrices.method_22907(RotationAxis.field_40714.rotationDegrees(v13 * 10.0F));
      matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)v9 * v13 * 30.0F));
   }

   private void m875(MatrixStack matrices, Arm arm, float equipProgress) {
      int v4 = arm == Arm.field_6183 ? 1 : -1;
      matrices.method_46416((float)v4 * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
      int result = v5 * (v5 + 1) % 2;

      if (result != 0) {
         }
   }

   
}
