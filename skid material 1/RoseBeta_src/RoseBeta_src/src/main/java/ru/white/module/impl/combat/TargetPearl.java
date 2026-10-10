package ru.white.module.impl.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.lwjgl.glfw.GLFW;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.misc.Of;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.setting.ModeSetting;
import ru.white.utils.misc.Zq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.module.impl.render.SwingAnimation;
import ru.white.utils.render.vs;
import ru.white.utils.MathUtil;
import ru.white.utils.BlockUtil;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Target Pearl",
   desc = "Автоматически кидает перл вслед за перлом врага",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class TargetPearl extends Module {
   private final SliderSetting f316;
   private final ModeSetting f311 = new ModeSetting(this, "Режим", "Всегда", "По бинду");
   private final BooleanSetting f322;
   private final SettingVisibility f313;
   private final SliderSetting cooldown;
   private String f339;
   private long f331;
   private final BooleanSetting f321;
   private final BooleanSetting f317;
   private int f323;
   private Vec3d f333;
   private final SliderSetting f314;
   private int f326;
   private final BooleanSetting f319;
   private final BooleanSetting f318;
   private final ModeSetting f312 = new ModeSetting(this, "Цели", "Все", "Цель ауры");
   private final BlockUtil f324;
   private int f336;
   private int f327;
   private long f334;
   private boolean f332;
   private int f335;
   private final SliderSetting f315;
   private final BooleanSetting f320;
   private boolean f328;
   private int f325;
   private final Set<Integer> f329;
   private long f330;
   private String f337;
   private String f338;

   private void m414(boolean state) {
      if (mc.field_1690 != null && mc.method_22683() != null) {
         KeyBinding[] result = new KeyBinding[6];
         result[0] = mc.field_1690.field_1894;
         result[1] = mc.field_1690.field_1881;
         result[2] = mc.field_1690.field_1913;
         result[3] = mc.field_1690.field_1849;
         result[4] = mc.field_1690.field_1903;

         result[5] = mc.field_1690.field_1867;
         KeyBinding[] v2 = result;
         KeyBinding[] v3 = v2;
         int v4 = v2.length;

         for (int v5 = 0; v5 < v4; v5++) {
            KeyBinding v6 = v3[v5];
            boolean v7 = state && InputUtil.method_15987(mc.method_22683(), v6.method_1429().method_1444());
            v6.method_23481(v7);
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (mc.field_1755 != null) {
            if (this.f323 != 0) {
               this.m402();
            }
         } else if (this.f323 != 0) {
            this.m399();
         } else if (!this.m398()) {
            this.f339 = "жду бинд";
         } else if ((float)(System.currentTimeMillis() - this.f330) < this.cooldown.getValue()) {
            this.f339 = "кулдаун";
         } else if (mc.field_1724.method_7357().method_7904(Items.field_8634.method_7854())) {
            this.f339 = "кд перла";
         } else {
            this.m403();
            this.m405();
            this.m404();
            boolean v2 = this.f321.getValue() && !this.f332;
            if (!v2) {

               for (Entity v4 : mc.field_1687.method_18112()) {
                  if (v4 instanceof EnderPearlEntity v5 && mc.field_1724.equals(v5.method_24921())) {
                     this.f330 = System.currentTimeMillis();
                     this.f339 = "свой перл летит";
                     return;
                  }
               }
            }

            LivingEntity v22 = fg.target;
            this.f335 = 0;
            ArrayList v23 = new ArrayList();

            for (Entity v6 : mc.field_1687.method_18112()) {
               if (v6 instanceof EnderPearlEntity) {
                  EnderPearlEntity v7 = (EnderPearlEntity)v6;
                  this.f335++;
                  if (!this.f329.contains(v7.method_5628())
                     && !(v7.method_18798().method_1027() < 1.0E-4)
                     && !(v7.method_5858(mc.field_1724) > (double)(this.f316.getValue() * this.f316.getValue()))) {
                     Entity v8 = v7.method_24921();
                     if (mc.field_1724.equals(v8)) {
                        if (!v2) {
                           continue;
                        }
                     } else {
                        if (this.f318.getValue() && v8 instanceof PlayerEntity) {
                           PlayerEntity v9 = (PlayerEntity)v8;
                           if (Client.get().friendManager().isFriend(v9.method_5477().getString())) {
                              continue;
                           }
                        }

                        if (this.f312.is("Цель ауры") && !v2 && (v22 == null || v8 == null || !v8.equals(v22))) {
                           continue;
                        }
                     }

                     v23.add(v7);
                  }
               }
            }

            this.f336 = v23.size();
            if (v23.isEmpty()) {
               this.f339 = this.f335 == 0
                  ? "перлов в мире нет"
                  : "кандидатов нет (фильтры)";
               this.f337 = "-";
               this.f338 = "-";
            } else {
               EnderPearlEntity v25 = null;
               Vec3d v26 = null;
               double v27 = Double.MAX_VALUE;
               float v28 = Rotation.cameraYaw();
               float v10 = Rotation.cameraPitch();

               for (EnderPearlEntity v12 : v23) {
                  Vec3d v13 = this.m408(v12.method_73189(), v12.method_18798(), v12);
                  if (v13 == null) {
                     this.f329.add(v12.method_5628());
                  } else if (!(mc.field_1724.method_73189().method_1022(v13) <= (double)this.f314.getValue().floatValue())
                     && !(mc.field_1724.method_73189().method_1022(v13) > 90.0)) {
                     float v14 = this.m412(mc.field_1724.method_33571(), v13);
                     float v33 = this.m413(mc.field_1724.method_33571(), v13);

                     float v15 = v33;
                     double v16 = Math.hypot((double)MathHelper.method_15393(v14 - v28), (double)(v15 - v10));
                     if (v16 < v27) {
                        v27 = v16;
                        v25 = v12;
                        v26 = v13;
                     }
                  }
               }

               if (v25 != null && v26 != null) {
                  int v18 = (int)mc.field_1724.method_73189().method_1022(v26);
                  this.f337 = v18 + "м";
                  int v29 = dq.getItemIndex(Items.field_8634);
                  if (v29 == -1) {
                     this.f339 = "НЕТ ПЕРЛОВ!";
                     this.m411("TargetPearl: нет перлов в инвентаре!");
                  } else if (v29 >= 9 && mc.field_1724.method_5624()) {
                     this.f339 = "жду конца спринта";
                  } else {
                     Vec3d v30 = mc.field_1724.method_33571().method_1019(mc.field_1724.method_18798());
                     float v31 = this.m412(v30, v26);
                     Float v32 = this.m406(v30, v31, v26);
                     if (v32 == null) {
                        this.f339 = "нет решения (не докинуть)";
                        this.f338 = "нет";
                     } else {
                        this.f338 = "ок";
                        this.f339 = "бросаю!";
                        this.f333 = v26;
                        this.f334 = System.currentTimeMillis();
                        xd.update(new Rotation(v31, v32), 500.0F, 500.0F, 5, 100);
                        if (this.f317.getValue()) {
                           this.m414(false);
                        }

                        fg.stoptick = 3;
                        this.f326 = mc.field_1724.method_31548().method_67532();
                        this.f325 = v29;
                        this.f327 = v25.method_5628();
                        this.f329.add(this.f327);
                        if (this.f325 < 9) {
                           mc.field_1724.method_31548().method_61496(this.f325);
                           mc.field_1761.method_2911();
                        } else {
                           mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f325, this.f326, SlotActionType.field_7791, mc.field_1724);
                        }

                        this.f328 = true;
                        this.f330 = System.currentTimeMillis();
                        this.f324.reset();
                        this.f323 = 1;
                        String v19 = this.f337;
                        this.m410("TargetPearl: догоняю перл (" + v19 + ")");
                     }
                  }
               } else {
                  this.f339 = "лендинг близко/далеко";
                  this.f337 = "-";
                  this.f338 = "-";
               }
            }
         }
      }
   }

   private float m412(Vec3d from, Vec3d to) {

      double result = -(to.field_1352 - from.field_1352);
      double temp1 = to.field_1350 - from.field_1350;

      return (float)Math.toDegrees(Math.atan2(result, temp1));
   }

   public TargetPearl() {
      this.f313 = new SettingVisibility(this, "Клавиша броска").setVisible(() -> {

         return this.f311.is("По бинду");
      });
      this.f314 = new SliderSetting(this, "Дистанция срабатывания", 6.0F, 3.0F, 15.0F, 0.5F);
      this.f315 = new SliderSetting(this, "Допуск попадания", 3.0F, 1.0F, 6.0F, 0.5F);
      this.cooldown = new SliderSetting(this, "Задержка", 1000.0F, 200.0F, 3000.0F, 100.0F);
      this.f316 = new SliderSetting(this, "Радиус слежки", 64.0F, 10.0F, 128.0F, 1.0F);
      this.f317 = new BooleanSetting(this, "Стоп движения при броске", true);
      this.f318 = new BooleanSetting(this, "Игнорить друзей", true);
      this.f319 = new BooleanSetting(this, "Маркер точки", true);
      this.f320 = new BooleanSetting(this, "Уведомления", true);
      this.f321 = new BooleanSetting(this, "Тест: гнаться за своими", false);
      this.f322 = new BooleanSetting(this, "Дебаг-статус", true);
      this.f323 = 0;
      this.f324 = new BlockUtil();
      this.f325 = -1;
      this.f326 = -1;
      this.f327 = -1;
      this.f328 = false;
      this.f329 = new HashSet<>();
      this.f330 = 0L;
      this.f331 = 0L;
      this.f332 = false;
      this.f333 = null;
      this.f334 = 0L;
      this.f335 = 0;
      this.f336 = 0;
      this.f337 = "-";
      this.f338 = "-";
      this.f339 = "старт";
   }

   private void m402() {
      this.m401();
      this.m414(true);

      this.f323 = 0;
      this.f325 = -1;
      this.f327 = -1;
   }

   private void m399() {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         if (this.f317.getValue()) {
            this.m414(false);
         }

         fg.stoptick = 3;
         if (this.f324.hasTimePassed(9005789513118549144L + -9005789513118549094L)) {
            this.f324.reset();
            if (this.f323 == 1) {
               EnderPearlEntity v1 = this.m400(this.f327);
               if (v1 == null) {
                  this.f339 = "перл пропал";
                  this.m402();
                  return;
               }

               Vec3d v2 = this.m408(v1.method_73189(), v1.method_18798(), v1);
               if (v2 == null) {
                  this.f339 = "лендинг потерян";
                  this.m402();
                  return;
               }

               Vec3d v3 = mc.field_1724.method_33571().method_1019(mc.field_1724.method_18798());
               float v4 = this.m412(v3, v2);
               Float v5 = this.m406(v3, v4, v2);
               if (v5 == null) {
                  this.f339 = "решение протухло";

                  this.m402();
                  return;
               }

               this.f333 = v2;
               this.f334 = System.currentTimeMillis();
               xd.update(new Rotation(v4, v5), 500.0F, 500.0F, 5, 100);
               xk.interactItem(Hand.field_5808);
               if (mc.field_1724.equals(v1.method_24921())) {
                  this.f332 = true;
               }

               this.f339 = "бросок!";
               this.f323 = 2;
            } else if (this.f323 == 2) {
               this.m401();
               this.m414(true);
               this.f323 = 0;
               this.f339 = "готово";
            }
         }
      } else {
         this.m402();
      }
   }

   private boolean m398() {
      if (!this.f311.is("По бинду")) {
         return true;
      } else {
         int v1 = this.f313.get();
         if (v1 == -1) {
            return false;
         } else {
            if (v1 >= 0) {
               int temp1 = 7;

               if (v1 <= temp1) {
                  return GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), v1) == 1;
               }
            }

            return InputUtil.method_15987(mc.method_22683(), v1);
         }
      }
   }

   private Vec3d m408(Vec3d pos, Vec3d motion, Entity ignore) {
      Vec3d v4 = motion;
      Vec3d v5 = pos;
      int result = mc.field_1687.method_31607();

      int v6 = result;

      for (int v7 = 0; v7 < 200; v7++) {
         Vec3d v8 = v5;
         v5 = v5.method_1019(v4);
         v4 = this.m409(v8, v4);
         BlockHitResult v9 = mc.field_1687.method_17742(new RaycastContext(v8, v5, ShapeType.field_17558, FluidHandling.field_1348, ignore));
         if (v9.method_17783() != Type.field_1333) {
            return v9.method_17784();
         }

         if (v5.field_1351 < (double)(v6 - (16))) {
            return null;
         }
      }

      return null;
   }

   private Float m406(Vec3d eye, float yaw, Vec3d landing) {
      ArrayList v4 = new ArrayList();

      for (; v5 <= 89; v5 += 3) {
         Vec3d v6 = this.m407(eye, yaw, (float)v5);
         if (v6 != null && v6.method_1022(landing) <= (double)this.f315.getValue().floatValue()) {
            v4.add((float)v5);
         }
      }

      if (v4.isEmpty()) {
         return null;
      } else {
         ArrayList v13 = new ArrayList();

         for (float v7 : v4) {
            for (float v8 = v7 - 2.0F; v8 <= v7 + 2.0F; v8++) {
               if (!(v8 < -89.0F) && !(v8 > 89.0F)) {
                  Vec3d v9 = this.m407(eye, yaw, v8);
                  if (v9 != null) {
                     double v18;
                     int v16 = (v18 = v9.method_1022(landing) - (double)this.f315.getValue().floatValue()) == 0.0 ? 0 : (v18 < 0.0 ? -1 : 1);

                     if (v16 <= 0) {
                        v13.add(v8);
                     }
                  }
               }
            }
         }

         if (v13.isEmpty()) {
            return v4.stream().max(Comparator.naturalOrder()).orElse(null);
         } else {
            float v15 = mc.field_1724.method_36455();
            Comparator v17 = Comparator.comparingDouble(p -> {

               return (double)Math.abs(MathHelper.method_15393(p - v15));
            });

            v13.sort(v17.thenComparing(Comparator.reverseOrder()));
            return (Float)v13.get(0);
         }
      }
   }

   private void m410(String msg) {
      if (this.f320.getValue()) {

         if (mc.field_1724 != null) {

            mc.field_1724.method_7353(Text.method_43470(msg), true);
            return;
         }
      }
   }

   private void m404() {
      if (this.f330 != 0L) {
         if (System.currentTimeMillis() - this.f330 <= 6748463357904010742L + -6748463357904009942L) {
            for (Entity v2 : mc.field_1687.method_18112()) {
               if (v2 instanceof EnderPearlEntity) {
                  EnderPearlEntity v3 = (EnderPearlEntity)v2;
                  if (v3.field_6012 <= 2 && !(v3.method_5858(mc.field_1724) > 16.0)) {
                     this.f329.add(v3.method_5628());
                     if (v3.method_24921() == null) {
                        try {
                           v3.method_7432(mc.field_1724);
                        } catch (Exception v6) {
                        }
                     }
                  }
               }
            }

            return;
         }

         
      }
   }

   private Vec3d m409(Vec3d at, Vec3d motion) {
      boolean v3 = mc.field_1687.method_8316(BlockPos.method_49638(at)).method_15767(FluidTags.field_15517);
      double v4 = v3 ? 0.8 : 0.99;
      return motion.method_1021(v4).method_1031(0.0, -0.03, 0.0);
   }

   private void m411(String msg) {
      Boolean result = this.f320.getValue();

      if (result && mc.field_1724 != null) {
         long v2 = System.currentTimeMillis();

         if (v2 - this.f331 >= 542007770507743L + -542007770504743L) {
            this.f331 = v2;

            mc.field_1724.method_7353(Text.method_43470(msg), true);
         }
      }
   }

   private void m403() {
      Iterator v1 = mc.field_1687.method_18112().iterator();

      while (true) {
         boolean result = v1.hasNext();

         if (!result) {
            return;
         }

         Entity v2 = (Entity)v1.next();
         if (v2 instanceof EnderPearlEntity v3 && v3.method_24921() == null && v3.field_6012 <= 8) {
            PlayerEntity v4 = null;
            double v5 = 9.0;
            Iterator v15 = mc.field_1687.method_18456().iterator();

            Iterator v7 = v15;

            while (v7.hasNext()) {
               PlayerEntity v8 = (PlayerEntity)v7.next();
               double v9 = v8.method_5858(v3);
               if (v9 < v5) {
                  v5 = v9;
                  v4 = v8;
               }
            }

            if (v4 != null) {
               try {
                  v3.method_7432(v4);
               } catch (Exception v14) {
               }
            }
         }
      }
   }

   @Override
   protected void onDisable() {
      this.m402();

      this.f329.clear();
      this.f333 = null;
      super.onDisable();
   }

   private void m401() {
      if (this.f328 && mc.field_1724 != null && mc.field_1761 != null) {
         try {
            if (this.f325 < 9) {
               mc.field_1724.method_31548().method_61496(this.f326);
               mc.field_1761.method_2911();
            } else {

               mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f325, this.f326, SlotActionType.field_7791, mc.field_1724);
               if (mc.field_1724.field_3944 != null) {
                  mc.field_1724.field_3944.method_52787(new CloseHandledScreenC2SPacket(mc.field_1724.field_7498.field_7763));
               }

               mc.field_1724.method_7346();
            }
         } catch (Exception v3) {
         }

         this.f328 = false;
      }
   }

   private float m413(Vec3d from, Vec3d to) {

      Vec3d v3 = to.method_1020(from);
      return (float)MathHelper.method_15350(-Math.toDegrees(Math.atan2(v3.field_1351, Math.hypot(v3.field_1352, v3.field_1350))), -90.0, 90.0);
   }

   @Override
   protected void onEnable() {
      this.m402();
      this.f329.clear();
      this.f332 = false;
      this.f330 = 0L;
      this.f339 = "старт";

      super.onEnable();
   }

   @EventTarget
   public void onDisplay(EventReceivePacket e) {
      if (mc.field_1724 != null) {
         if (this.f322.getValue()) {
            DrawContext v2 = e.getDrawContext();
            String v8 = this.f339;
            v2.method_51433(mc.field_1772, "TP: " + v8, 8, v3, -1, true);
            int v9 = this.f336;
            int v10 = this.f335;
            v2.method_51433(
               mc.field_1772, "перлов: " + v10 + " кандид: " + v9, 8, v3 + 10, -5592406, true
            );
            String v11 = this.f338;
            String v12 = this.f337;
            v2.method_51433(
               mc.field_1772, "точка: " + v12 + " питч: " + v11, 8, v3 + 20, -5592406, true
            );
         }

         if (this.f319.getValue() && this.f333 != null) {
            if (System.currentTimeMillis() - this.f334 > 8550212680032270784L + -8550212680032267784L) {
               this.f333 = null;
            } else {
               Vec3d v15 = zq.worldSpaceToScreenSpace(this.f333.method_1031(0.0, 0.25, 0.0));
               if (!(v15.field_1350 <= 0.0) && !(v15.field_1350 >= 1.0)) {
                  double result = mc.field_1724.method_73189().method_1022(this.f333);

                  int v13 = (int)result;
                  String v16 = "TP " + v13 + "м";
                  float v4 = 7.0F;
                  float v5 = hu.sf_regular.getWidth(v16, v4) + 8.0F;
                  float v6 = (float)v15.field_1352 - v5 / 2.0F;
                  float v7 = (float)v15.field_1351;
                  vs.ys.rect(
                     v6,
                     v7,
                     v5,
                     v4 + 5.0F,
                     up.getColor(11, 11, 11, 140),
                     3.0F
                  );
                  hu.sf_regular.draw(v16, v6 + 4.0F, v7 + 2.5F, v4, up.getColor(255));
               }
            }
         }
      }
   }

   private EnderPearlEntity m400(int id) {
      for (Entity v3 : mc.field_1687.method_18112()) {

         if (v3 instanceof EnderPearlEntity v4 && v4.method_5628() == id) {
            return v4;
         }
      }

      return null;
   }

   private void m405() {
      if (this.f329.isEmpty()) {
         
      } else {
         HashSet v1 = new HashSet();

         for (Object result : mc.field_1687.method_18112()) {

            Entity v3 = (Entity)result;
            if (v3 instanceof EnderPearlEntity) {
               v1.add(v3.method_5628());
            }
         }

         this.f329.retainAll(v1);
      }
   }

   private Vec3d m407(Vec3d eye, float yaw, float pitch) {
      double v4 = Math.toRadians((double)yaw);
      double v6 = Math.toRadians((double)pitch);
      double v8 = (double)(-MathHelper.method_15374((double)((float)v4)) * MathHelper.method_15362((double)((float)v6)));
      double v10 = (double)(-MathHelper.method_15374((double)((float)v6)));
      double v12 = (double)(MathHelper.method_15362((double)((float)v4)) * MathHelper.method_15362((double)((float)v6)));
      Vec3d result = new Vec3d(v8, v10, v12);

      Vec3d v14 = result.method_1029();
      Vec3d v15 = v14.method_1021(1.5);
      Vec3d v16 = mc.field_1724.method_18798();
      v15 = v15.method_1031(v16.field_1352, mc.field_1724.method_24828() ? 0.0 : v16.field_1351, v16.field_1350);
      Vec3d v17 = eye.method_1019(v14.method_1021(0.2));
      return this.m408(v17, v15, mc.field_1724);
   }

   
}
