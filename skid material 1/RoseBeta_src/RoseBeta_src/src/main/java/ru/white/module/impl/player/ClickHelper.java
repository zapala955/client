package ru.white.module.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Of;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleHelper;
import ru.white.manager.rotation.AngleUtil;
import ru.white.module.impl.combat.AttackAura;
import ru.white.module.impl.render.InterFace;
import ru.white.module.impl.render.SwingAnimation;
import ru.white.theme.wo;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.render.dt;
import ru.white.utils.render.vr;
import ru.white.utils.render.vs;
import ru.white.utils.SoundUtil;
import ru.white.utils.PacketUtil;
import ru.white.utils.BlockUtil;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventRenderModel;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Click Helper",
   desc = "Помогает кликать по клавише (автокликер)",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class ClickHelper extends Module {
   private static final float BADGE_PAD_Y = 3.0F;
   private final Map<Item, Animation> f611;
   private boolean f600;
   public final SettingVisibility angerKey;
   public final SettingVisibility assassinKey;
   private static final float BADGE_FONT = 5.0F;
   private static final float BADGE_PAD_X = 4.0F;
   private int f605;
   public final SettingVisibility holyStunKey;
   private static final int HUD_COLS = 20;
   public final SettingVisibility eyeKey;
   public final SettingVisibility keyHilka;
   private int f594;
   public final SettingVisibility trapKey;
   private final BlockUtil f604;
   public final SettingVisibility sleepKey;
   public final SettingVisibility chorusKey;
   public SettingVisibility coordsKey;
   public final SettingVisibility holySnowKey;
   public final SettingVisibility bojAurakey;
   private final BlockUtil f597;
   private int f595;
   public final SettingVisibility holyExplosiveTrapKey;
   public final SettingVisibility windKey;
   public SettingVisibility friend = new SettingVisibility(this, "Клавиша добавления в друзья", -1);
   private boolean f608;
   public final SettingVisibility palladinKey;
   private int f603;
   private int f602;
   private static final int HUD_CW = 24;
   public SettingVisibility perka = new SettingVisibility(this, "Клавиша кидание Эндер жемчуга", -1);
   public final SettingVisibility holyExplosiveKey;
   private static final int HUD_GAP = 1;
   public final SettingVisibility plastKey;
   private boolean f593;
   public final SettingVisibility sugarKey;
   public ModeSetting type;
   private Item f606;
   private int f596;
   public final SettingVisibility holyTrapKey;
   private int f601;
   private final Map<Item, Float> f609;
   public final SettingVisibility expKey;
   private static final Item[] CD_ITEMS;
   public final SettingVisibility keySerka;
   public final SettingVisibility snowKey;
   private int f598;
   private final BlockUtil f599;
   private int step;
   private final Map<Item, long[]> f610;
   private static final int HUD_CH = 24;
   private String f607;

   @EventTarget
   public void onKey(EventPostMotion e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         this.m572(e.getKey());
      }
   }

   public static ei getInstance() {
      ei result = uq.get(ei.class);

      return result;
   }

   private void m576(String name) {
      if (mc.field_1724 == null) {
         
      } else {
         this.f606 = null;
         this.f607 = name.toLowerCase();
         this.m577();
      }
   }

   private void m574() {
      if (mc.field_1724 != null && this.f594 != -1) {
         if (this.f594 < 9) {

            mc.field_1724.method_31548().method_61496(this.f595);
            mc.field_1761.method_2911();
         } else {
            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f594, this.f595, SlotActionType.field_7791, mc.field_1724);
            mc.field_1724.field_3944.method_52787(new CloseHandledScreenC2SPacket(mc.field_1724.field_7498.field_7763));
            mc.field_1724.method_7346();
         }

         this.m569(true);
      }

      this.f594 = -1;
      this.f596 = 0;
   }

   private void m572(int key) {
      if (key == this.friend.get() && key != -1) {
         this.m571();
      }

      if (key != -1 && this.step == 0 && this.f601 == -1) {
         Item v2 = null;
         if (key == this.perka.get()) {
            v2 = Items.field_8634;
         }

         if (this.type.is("FunTime")) {
            if (key == this.sugarKey.get()) {
               v2 = Items.field_8479;
            } else if (key == this.eyeKey.get()) {
               v2 = Items.field_8449;
               
            } else if (key == this.snowKey.get()) {
               v2 = Items.field_8543;
            } else if (key == this.plastKey.get()) {
               v2 = Items.field_8551;
            } else if (key == this.trapKey.get()) {
               v2 = Items.field_22021;
            } else if (key == this.windKey.get()) {
               v2 = Items.field_49098;
            } else if (key == this.bojAurakey.get()) {
               v2 = Items.field_8614;
            }

            if (key == this.keyHilka.get()) {
               xd.update(
                  new Rotation(mc.field_1724.method_36454() + fq.randomValue(-3.0F, 3.0F), 80.0F + fq.randomValue(2.0F, 8.0F)),
                  360.0F,
                  360.0F,
                  2,
                  200
               );
               this.m576("Святая вода");
               return;
            }

            if (key == this.keySerka.get()) {
               this.m576("Зелье Радиации");
               return;
            }

            if (key == this.palladinKey.get()) {
               xd.update(
                  new Rotation(mc.field_1724.method_36454() + fq.randomValue(-3.0F, 3.0F), 80.0F + fq.randomValue(2.0F, 8.0F)),
                  360.0F,
                  360.0F,
                  2,
                  200
               );
               this.m576("Зелье Палладина");
               return;
            }

            if (key == this.assassinKey.get()) {
               xd.update(
                  new Rotation(mc.field_1724.method_36454() + fq.randomValue(-3.0F, 3.0F), 80.0F + fq.randomValue(2.0F, 8.0F)),
                  360.0F,
                  360.0F,
                  2,
                  200
               );
               this.m576("Зелье Ассасина");
               return;
            }

            if (key == this.sleepKey.get()) {

               this.m576("Снотворное");
               return;
            }

            if (key == this.angerKey.get()) {
               xd.update(
                  new Rotation(mc.field_1724.method_36454() + fq.randomValue(-3.0F, 3.0F), 80.0F + fq.randomValue(2.0F, 8.0F)),
                  360.0F,
                  360.0F,
                  2,
                  200
               );
               this.m576("Зелье Гнева");
               return;
            }

            if (v2 != null) {
               this.m575(v2);
            }
         } else if (this.type.is("HolyWorld")) {
            if (key == this.holyStunKey.get()) {
               v2 = Items.field_8137;
            } else if (key == this.holyTrapKey.get()) {
               v2 = Items.field_8882;
            } else if (key == this.holyExplosiveTrapKey.get()) {
               v2 = Items.field_8662;
            } else if (key == this.holyExplosiveKey.get()) {
               v2 = Items.field_8814;
            } else if (key == this.holySnowKey.get()) {
               v2 = Items.field_8543;
            } else if (key == this.windKey.get()) {
               v2 = Items.field_49098;
            }

            if (v2 != null) {
               this.m575(v2);
            }
         }
      }
   }

   public ClickHelper() {
      this.coordsKey = new SettingVisibility(this, "Клавиша координат", -1);
      this.chorusKey = new SettingVisibility(this, "Клавиша хоруса", -1);
      this.expKey = new SettingVisibility(this, "Клавиша опыта", -1);
      this.type = new ModeSetting(this, "Режим", "FunTime", "HolyWorld");
      this.eyeKey = new SettingVisibility(this, "Клавиша дезки", -1).setVisible(() -> {
         boolean result = this.type.is("FunTime");

         return result;
      });
      this.sugarKey = new SettingVisibility(this, "Клавиша явки", -1).setVisible(() -> {

         Boolean result = this.type.is("FunTime");
         int v4 = v1 * (v1 + 1);

         return result;
      });
      this.snowKey = new SettingVisibility(this, "Клавиша снежка", -1).setVisible(() -> {

         return this.type.is("FunTime");
      });
      this.plastKey = new SettingVisibility(this, "Клавиша пласта", -1).setVisible(() -> {

         return this.type.is("FunTime");
      });
      this.trapKey = new SettingVisibility(this, "Клавиша трапки", -1).setVisible(() -> this.type.is("FunTime"));
      this.windKey = new SettingVisibility(this, "Клавиша з.ветра", -1).setVisible(() -> {
         Boolean result = this.type.is("FunTime");

         return result;
      });
      this.bojAurakey = new SettingVisibility(this, "Клавиша бож.ауры", -1).setVisible(() -> {
         int temp2 = v1 + 1;

         return this.type.is("FunTime");
      });
      this.keySerka = new SettingVisibility(this, "Клавиша радейки", -1).setVisible(() -> {

         return this.type.is("FunTime");
      });
      this.keyHilka = new SettingVisibility(this, "Клавиша бож.воды", -1).setVisible(() -> this.type.is("FunTime"));
      this.palladinKey = new SettingVisibility(this, "Клавиша Палладина", -1).setVisible(() -> {

         temp1 %= 2;

         if (temp1 != 0) {
            }

         return this.type.is("FunTime");
      });
      this.assassinKey = new SettingVisibility(this, "Клавиша Ассасина", -1)
         .setVisible(() -> this.type.is("FunTime"));
      this.sleepKey = new SettingVisibility(this, "Клавиша Снотворного", -1).setVisible(() -> {

         return this.type.is("FunTime");
      });
      this.angerKey = new SettingVisibility(this, "Клавиша Гнева", -1).setVisible(() -> {

         return this.type.is("FunTime");
      });
      this.holyStunKey = new SettingVisibility(this, "Клавиша стана", -1).setVisible(() -> {

         return this.type.is("HolyWorld");
      });
      this.holyTrapKey = new SettingVisibility(this, "Клавиша трапки", -1).setVisible(() -> {
         Boolean result = this.type.is("HolyWorld");

         return result;
      });
      this.holyExplosiveTrapKey = new SettingVisibility(this, "Клавиша взрывной трапки", -1)
         .setVisible(() -> {

            Boolean result = this.type.is("HolyWorld");

            return result;
         });
      this.holyExplosiveKey = new SettingVisibility(this, "Клавиша взрывной штуки", -1)
         .setVisible(() -> {
            Boolean result = this.type.is("HolyWorld");

            return result;
         });
      this.holySnowKey = new SettingVisibility(this, "Клавиша кома снега", -1).setVisible(() -> {
         boolean result = this.type.is("HolyWorld");

         return result;
      });
      this.f593 = false;
      this.f594 = -1;
      this.f595 = -1;
      this.f596 = 0;
      this.f597 = new BlockUtil();
      this.f598 = -1;
      this.step = 0;
      this.f599 = new BlockUtil();
      this.f600 = false;
      this.f601 = -1;
      this.f602 = -1;
      this.f603 = 0;
      this.f604 = new BlockUtil();
      this.f605 = -1;
      this.f606 = null;
      this.f607 = null;
      this.f608 = false;
      this.f609 = new HashMap<>();
      this.f610 = new ConcurrentHashMap<>();
      this.f611 = new HashMap<>();
   }

   private void m570() {
      if (mc.field_1724 != null) {
         ClientPlayNetworkHandler result = mc.field_1724.field_3944;
         int temp1 = mc.field_1724.method_31477();
         int v1 = mc.field_1724.method_31479();
         int v2 = mc.field_1724.method_31478();
         int v3 = temp1;
         StringBuilder v6 = new StringBuilder();

         StringBuilder v7 = v6.append("/EventPacket ");

         result.method_45729(v7.append(v3).append(" ").append(v2).append(" ").append(v1).toString());
      }
   }

   private void m573() {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1755 == null) {
         int v1 = this.expKey.get();
         if (v1 == -1) {
            if (this.f596 != 0) {
               this.m574();
            }

            this.f593 = false;
         } else {
            boolean v2 = InputUtil.method_15987(mc.method_22683(), v1);
            if (!this.f593 && v2 && this.step == 0 && this.f596 == 0) {

               for (int v4 = 0; v4 < 36; v4++) {
                  if (mc.field_1724.method_31548().method_5438(v4).method_7909() == Items.field_8287) {
                     v3 = v4;
                     break;
                  }
               }

               if (v3 == -1) {
                  mc.field_1724
                     .method_7353(
                        Text.method_43470("Опыт не найден!").method_27692(Formatting.field_1061), true
                     );
               } else {
                  this.f594 = v3;
                  this.f595 = mc.field_1724.method_31548().method_67532();
                  this.f596 = 1;
                  this.f597.reset();
               }
            }

            this.f593 = v2;
            if (this.f596 == 2 && !v2) {
               this.f596 = 3;

               this.f597.reset();
            }

            switch (this.f596) {
               case 1:
                  if (this.f594 > 9) {
                     this.m569(false);
                  }

                  if (this.f597.hasTimePassed(-536486281746753514L + 536486281746753564L)) {
                     if (this.f594 < 9) {
                        mc.field_1724.method_31548().method_61496(this.f594);

                        mc.field_1761.method_2911();
                     } else {
                        this.m569(true);
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f594, this.f595, SlotActionType.field_7791, mc.field_1724);
                     }

                     this.f596 = 2;
                  }
                  break;
               case 2:
                  mc.field_1690.field_1904.method_23481(true);
                  xd.update(new Rotation(wd.freeYaw, 90.0F), 90.0F, 90.0F, 2, 25);
                  break;
               case 3:
                  mc.field_1690.field_1904.method_23481(false);
                  if (this.f594 > 9) {
                     this.m569(false);
                  }

                  if (this.f597.hasTimePassed(4087968173733137435L + -4087968173733137385L)) {
                     if (this.f594 < 9) {
                        mc.field_1724.method_31548().method_61496(this.f595);
                        mc.field_1761.method_2911();
                     } else {

                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f594, this.f595, SlotActionType.field_7791, mc.field_1724);
                        mc.field_1724.field_3944.method_52787(new CloseHandledScreenC2SPacket(mc.field_1724.field_7498.field_7763));
                        mc.field_1724.method_7346();
                        this.m569(true);
                     }

                     this.f594 = -1;
                     this.f596 = 0;
                  }
            }
         }
      }
   }

   @EventTarget
   public void onUpdate2(EventTick e) {
      if (mc.field_1724 != null && mc.field_1761 != null) {
         this.m586();
         this.m573();
         if (this.step != 0) {
            this.m569(false);
            fg.stoptick = 3;
            if (this.f599.hasTimePassed(-3993621410707439595L + 3993621410707439645L)) {
               this.f599.reset();
               switch (this.step) {
                  case 1:
                     if (this.f608) {
                        xd.update(new Rotation(mc.field_1724.method_36454(), 90.0F), 360.0F, 360.0F, 2, 200);
                        int temp1 = v2 * (v2 + 1) % 2;

                        if (temp1 != 0) {
                           }

                        mc.field_1724.method_36457(90.0F);
                     } else {
                        Rotation result = new Rotation;

                        result./* $VF: Unable to resugar constructor */<init>(
                           mc.field_1773.method_19418().method_19330(), mc.field_1773.method_19418().method_19329()
                        );
                        xd.update(
                           result, fq.randomLerp(400.0F, 900.0F), fq.randomLerp(400.0F, 900.0F), (int)fq.randomLerp(4.0F, 7.0F), 100
                        );
                     }

                     if (this.f605 < 9) {
                        mc.field_1724.method_31548().method_61496(this.f605);
                        mc.field_1761.method_2911();
                     } else {
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f605, this.f598, SlotActionType.field_7791, mc.field_1724);
                     }

                     this.step = 2;
                     break;
                  case 2:
                     if (this.f608) {
                        xd.update(new Rotation(mc.field_1724.method_36454(), 90.0F), 360.0F, 360.0F, 2, 200);
                        mc.field_1724.method_36457(90.0F);
                     }

                     xk.interactItem(Hand.field_5808);
                     this.step = 3;
                     break;
                  case 3:
                     if (this.f605 < 9) {
                        mc.field_1724.method_31548().method_61496(this.f598);
                        mc.field_1761.method_2911();
                     } else {
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f605, this.f598, SlotActionType.field_7791, mc.field_1724);
                     }

                     this.step = 4;
                     break;
                  case 4:
                     if (this.f608) {
                        mc.field_1724.method_36457(wd.freePitch);
                        xd.resetParentTimeout();
                        this.f608 = false;
                     }

                     if (this.f605 >= 9) {
                        mc.field_1724.field_3944.method_52787(new CloseHandledScreenC2SPacket(mc.field_1724.field_7498.field_7763));
                        mc.field_1724.method_7346();
                     }

                     this.m569(true);
                     this.step = 0;
                     this.f606 = null;
                     this.f607 = null;
               }
            }
         }
      }
   }

   private float[] m582(List<Integer> active, SettingVisibility[] keys) {
      float[] v3 = new float[active.size()];
      float v4 = 0.0F;

      for (int v5 = 0; v5 < active.size(); v5++) {
         int result = v5 % (20);

         if (result == 0) {
            v4 = 0.0F;
         } else {

            float v6 = this.m581(keys[active.get(v5 - 1)].get());
            float v7 = this.m581(keys[active.get(v5)].get());
            v4 += Math.max(24.0F, (v6 + v7) / 2.0F) + 1.0F;
         }

         v3[v5] = v4;
      }

      return v3;
   }

   private void m575(Item item) {
      if (mc.field_1724 != null) {
         this.f607 = null;
         this.f606 = item;

         this.m577();
      }
   }

   private String m584(int key) {
      if (key <= 0) {
         return "";
      } else {
         try {
            Key result = Type.field_1668.method_1447(key);

            String v2 = result.method_27445().getString();
            String v6;
            if (v2.length() <= 3) {
               v6 = v2;
            } else {
               char v7 = v2.charAt(0);

               v6 = String.valueOf(v7);
            }

            return v6.toUpperCase();
         } catch (Exception v5) {
            return "?";
         }
      }
   }

   private void m569(boolean state) {

      KeyBinding[] result = new KeyBinding[]{mc.field_1690.field_1894, mc.field_1690.field_1881, null, null, null};

      result[2] = mc.field_1690.field_1913;
      result[3] = mc.field_1690.field_1849;

      result[4] = mc.field_1690.field_1903;
      KeyBinding[] v2 = result;
      long v3 = mc.method_22683().method_4490();

      for (KeyBinding v8 : v2) {
         boolean v9 = state && InputUtil.method_15987(mc.method_22683(), v8.method_1429().method_1444());
         v8.method_23481(v9);
      }
   }

   private int m585(Item item) {
      if (mc.field_1724 == null) {
         return 0;
      } else {

         for (int v3 = 0; v3 < 36; v3++) {
            ItemStack v4 = mc.field_1724.method_31548().method_5438(v3);
            if (v4.method_7909() == item) {
               int v7 = v4.method_7947();

               v2 += v7;
            }
         }

         return v2;
      }
   }

   private List<Integer> m580(SettingVisibility[] keys) {
      ArrayList v2 = new ArrayList();

      for (int v3 = 0; v3 < keys.length; v3++) {

         if (keys[v3].get() != -1) {
            v2.add(v3);
         }
      }

      return v2;
   }

   static {
      Item[] result = new Item[11];
      result[0] = Items.field_8449;
      result[1] = Items.field_8479;
      result[2] = Items.field_8543;
      result[3] = Items.field_8551;
      result[4] = Items.field_22021;
      result[5] = Items.field_49098;
      result[6] = Items.field_8614;
      result[7] = Items.field_8137;
      result[8] = Items.field_8882;
      result[9] = Items.field_8662;
      result[10] = Items.field_8814;
      CD_ITEMS = result;
   }

   private void m568() {
      this.f605 = -1;

      for (int v1 = 0; v1 < 36; v1++) {
         ItemStack v2 = mc.field_1724.method_31548().method_5438(v1);
         if (v2.method_7909() == Items.field_8634) {
            this.f605 = v1;
            
            break;
         }
      }

      if (this.f605 == -1) {
         mc.field_1724
            .method_7353(
               Text.method_43470("Перл не найден!").method_27692(Formatting.field_1061), true
            );
      } else {

         this.f598 = mc.field_1724.method_31548().method_67532();
         this.f599.reset();
         this.step = 1;
      }
   }

   public Vector2f getHudSize() {
      SettingVisibility[] v1 = this.m578();
      List v2 = this.m580(v1);
      if (v2.isEmpty()) {
         return new Vector2f(0.0F, 0.0F);
      } else {
         float v3 = hh.getInstance().sizeHud.getValue();
         if (hh.getInstance().isHud2()) {
            float v15 = 22.0F * v3;
            float v16 = 25.0F * v3;
            float v17 = 3.5F * v3;
            float v18 = 4.0F * v3;
            float v19 = v18 * 2.0F + (float)v2.size() * v15 + (float)Math.max(0, v2.size() - 1) * v17;
            float v21 = v18 * 2.0F + v16;
            return new Vector2f(v19, v21);
         } else {
            float[] v4 = this.m582(v2, v1);
            float v5 = 24.0F * v3;
            float result = 24.0F * v3;

            float v6 = result;
            float v7 = 1.0F * v3;
            float v8 = 0.0F;

            while (true) {

               if (v9 >= v2.size()) {
                  v9 = (v2.size() + 20 - 1) / (20);
                  return new Vector2f(v8, (float)v9 * v6 + (float)(v9 - 1) * v7);
               }

               float v10 = this.m581(v1[v2.get(v9)].get()) * v3;
               float v11 = v4[v9] * v3;
               v8 = Math.max(v8, v11 + Math.max(v5, v5 / 2.0F + v10 / 2.0F));
               v9++;
            }
         }
      }
   }

   private void m571() {
      if (fg.target == null) {
         HitResult v1 = mc.field_1765;
         if (v1 != null && v1.method_17783() == net.minecraft.util.hit.HitResult.Type.field_1331) {
            if (((EntityHitResult)v1).method_17782() instanceof PlayerEntity v3) {
               String v4 = v3.method_5477().getString();
               if (!Client.get().friendManager().isFriend(v4)) {
                  Client result = Client.get();

                  result.friendManager().add(v4);
               } else {
                  Client.get().friendManager().remove(v4);
               }
            }
         }
      }
   }

   public void renderHud(DrawContext ctx, float sx, float sy) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         SettingVisibility[] v4 = this.m578();
         Item[] v5 = this.m579();
         List v6 = this.m580(v4);
         if (!v6.isEmpty()) {
            float v7 = hh.getInstance().sizeHud.getValue();
            if (hh.getInstance().isHud2()) {
               this.m583(ctx, sx, sy, v6, v4, v5, v7);
            } else {
               float v8 = 20.0F * v7;
               float v9 = 20.0F * v7;
               float v10 = 0.0F * v7;
               float v11 = 1.0F * v7;
               float[] v12 = this.m582(v6, v4);

               for (int v13 = 0; v13 < v6.size(); v13++) {
                  int v14 = (Integer)v6.get(v13);
                  int v15 = v4[v14].get();
                  Item v16 = v5[v14];
                  int v17 = v13 / (20);
                  float v18 = sx + v12[v13] * v7;
                  float v19 = sy + (float)v17 * (v9 + v10 + v11);
                  vs.ys.glow(v18, v19, v8, v9 - 0.5F * v7, up.getColor(0, 0.1F), 5.0F * v7, 12.0F * v7, 1.0F);
                  vs.ws.blur(v18, v19, v8, v9, 1.0F, 5.0F * v7, up.replAlpha(up.background(), hh.getInstance().alphaHUD.getValue()));
                  Animation v20 = this.f611.computeIfAbsent(v16, i -> {
                     Animation var10000x = new Animation;

                     var10000x./* $VF: Unable to resugar constructor */<init>();
                     return var10000x;
                  });
                  v20.update();
                  v20.run((double)mc.field_1724.method_7357().method_7905(v16.method_7854(), 0.0F), 0.12F, ap.LINEAR);
                  float v21 = fq.clamp(v20.get(), 0.0F, 1.0F);
                  if (v21 > 0.001F) {
                     float v22 = v9 * v21;
                     float v23 = 5.0F * v7 * fq.clamp((v21 - 0.85F) / 0.15F, 0.0F, 1.0F);
                     float temp1 = v19 + v9 - v22;
                     int temp4 = 80;
                     int temp5 = 10;

                     vs.ys.rect(
                        v18, temp1, v8, v22, up.getColor(temp4, temp5, 10, 0.7F), v23, v23, 5.0F * v7, 5.0F * v7
                     );
                  }

                  float v43 = 2.0F;
                  float v44 = (float)mc.method_22683().method_4495();
                  float v24 = v43 / v44;
                  ctx.method_51448().pushMatrix();
                  ctx.method_51448().translate((v18 + v8 / 2.0F) * v24, (v19 + v9 / 2.0F - 1.0F * v7) * v24);
                  ctx.method_51448().scale(v24 * 0.5F * v7, v24 * 0.5F * v7);
                  ctx.method_51427(new ItemStack(v16), -8, -9);
                  ctx.method_51448().popMatrix();

                  int v39 = this.m585(v16);
                  String v25 = "x" + v39;
                  float v26 = 5.0F * v7;
                  float v27 = hu.sf_regular.getWidth(v25, v26);
                  float temp2 = v18 + v8;

                  hu.sf_regular.draw(v25, temp2 - v27 - 3.0F * v7, v19 + v9 - 8.0F * v7, v26, wo.getTextColor());
                  float v28 = v19 + v9 + v11;
                  vs.ys.rect(v18, v28, v8, v10, -870835936, 2.0F * v7);
                  boolean v29 = mc.field_1724.method_7357().method_7904(v16.method_7854());
                  float v30 = 0.0F;
                  float v31 = 0.0F;
                  if (v29) {
                     long[] v32 = this.f610.get(v16);
                     if (v32 != null && v32[1] > 0L) {
                        float v33 = mc.field_1724.method_7357().method_7905(v16.method_7854(), 0.0F);
                        v31 = (float)v32[1] / 20.0F;
                        v30 = v33 * v31;
                     }
                  }

                  String v47 = eq.keyName(v15);
                  float v48 = 5.0F * v7;
                  float v34 = hu.sf_regular.getHeight(v48);
                  float v35 = this.m581(v15) * v7;
                  float v36 = v34 + 6.0F * v7;
                  float v37 = v18 - v35 / 2.0F + v8 / 2.0F;
                  float v38 = v19 - v36 / 2.0F - 9.0F * v7;
                  vs.ys.glow(v37, v38, v35, v36 - 0.5F * v7, up.getColor(0, 0.1F), 4.0F * v7, 7.0F * v7, 1.0F);
                  vs.ws.blur(v37, v38, v35, v36, 1.0F, 4.0F * v7, up.replAlpha(up.background(), hh.getInstance().alphaHUD.getValue()));
                  hu.sf_regular.drawCentered(v47, v18 + v8 / 2.0F, v19 - v34 / 2.0F - 9.0F * v7, v48, wo.getTextColor());
               }
            }
         }
      }
   }

   private void m586() {
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1755 == null) {
         int v1 = this.chorusKey.get();
         if (v1 == -1) {
            if (this.f603 != 0) {
               this.m587();
            }

            this.f600 = false;
         } else {
            boolean v2 = InputUtil.method_15987(mc.method_22683(), v1);
            if (!this.f600 && v2 && this.step == 0 && this.f603 == 0) {

               for (int v4 = 0; v4 < 36; v4++) {
                  if (mc.field_1724.method_31548().method_5438(v4).method_7909() == Items.field_8233) {
                     v3 = v4;
                     break;
                  }
               }

               if (v3 == -1) {
                  mc.field_1724
                     .method_7353(
                        Text.method_43470("Хорус не найден!").method_27692(Formatting.field_1061),
                        true
                     );
               } else {
                  this.f601 = v3;
                  this.f602 = mc.field_1724.method_31548().method_67532();
                  this.f603 = 1;
                  this.f604.reset();
               }
            }

            this.f600 = v2;
            if (this.f603 == 2 && !v2) {
               this.f603 = 3;
               this.f604.reset();
            }

            switch (this.f603) {
               case 1:
                  if (this.f601 > 9) {
                     this.m569(false);
                  }

                  if (this.f604.hasTimePassed(1027494615850911556L + -1027494615850911506L)) {
                     if (this.f601 < 9) {
                        mc.field_1724.method_31548().method_61496(this.f601);
                        mc.field_1761.method_2911();
                     } else {
                        this.m569(true);
                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f601, this.f602, SlotActionType.field_7791, mc.field_1724);
                     }

                     this.f603 = 2;
                  }
                  break;
               case 2:
                  mc.field_1690.field_1904.method_23481(true);
                  break;
               case 3:
                  mc.field_1690.field_1904.method_23481(false);
                  if (this.f601 > 9) {
                     this.m569(false);
                  }

                  if (this.f604.hasTimePassed(2149999832104660541L + -2149999832104660491L)) {
                     if (this.f601 < 9) {
                        mc.field_1724.method_31548().method_61496(this.f602);
                        mc.field_1761.method_2911();
                     } else {

                        mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f601, this.f602, SlotActionType.field_7791, mc.field_1724);
                        mc.field_1724.field_3944.method_52787(new CloseHandledScreenC2SPacket(mc.field_1724.field_7498.field_7763));
                        mc.field_1724.method_7346();
                        this.m569(true);
                     }

                     this.f601 = -1;
                     this.f603 = 0;
                  }
            }
         }
      }
   }

   private float m581(int key) {
      return hu.sf_regular.getWidth(eq.keyName(key), 5.0F) + 8.0F;
   }

   private Item[] m579() {
      Item[] result;
      if (this.type.is("HolyWorld")) {
         result = new Item[7];
         result[0] = Items.field_8634;
         result[1] = Items.field_8137;
         result[2] = Items.field_8882;
         result[3] = Items.field_8662;
         result[4] = Items.field_8814;
         result[5] = Items.field_8543;
         result[6] = Items.field_8233;
      } else {

         result = new Item[9];
         result[0] = Items.field_8634;
         result[1] = Items.field_8449;
         result[2] = Items.field_8479;
         result[3] = Items.field_8543;
         result[4] = Items.field_8551;
         result[5] = Items.field_22021;
         result[6] = Items.field_49098;
         result[7] = Items.field_8614;
         result[8] = Items.field_8233;
      }

      return result;
   }

   private void m587() {

      if (mc.field_1724 != null && this.f601 != -1) {
         if (this.f601 < 9) {
            mc.field_1724.method_31548().method_61496(this.f602);
            mc.field_1761.method_2911();
         } else {
            mc.field_1761.method_2906(mc.field_1724.field_7498.field_7763, this.f601, this.f602, SlotActionType.field_7791, mc.field_1724);
            ClientPlayNetworkHandler result = mc.field_1724.field_3944;
            CloseHandledScreenC2SPacket v3 = new CloseHandledScreenC2SPacket(mc.field_1724.field_7498.field_7763);

            result.method_52787(v3);
            mc.field_1724.method_7346();
         }

         this.m569(true);
      }

      this.f601 = -1;
      this.f603 = 0;
   }

   private SettingVisibility[] m578() {
      SettingVisibility[] result;
      if (this.type.is("HolyWorld")) {
         result = new SettingVisibility[7];
         result[0] = this.perka;
         result[1] = this.holyStunKey;
         result[2] = this.holyTrapKey;
         result[3] = this.holyExplosiveTrapKey;
         result[4] = this.holyExplosiveKey;
         result[5] = this.holySnowKey;
         result[6] = this.chorusKey;
      } else {
         result = new SettingVisibility[9];
         result[0] = this.perka;

         result[1] = this.eyeKey;
         result[2] = this.sugarKey;
         result[3] = this.snowKey;
         result[4] = this.plastKey;
         result[5] = this.trapKey;
         result[6] = this.windKey;
         result[7] = this.bojAurakey;
         result[8] = this.chorusKey;
      }

      return result;
   }

   @Override
   public void onDisable() {
      if (this.f608 && mc.field_1724 != null) {
         mc.field_1724.method_36457(wd.freePitch);
         xd.resetParentTimeout();
         this.f608 = false;
      }

      this.step = 0;
      this.f606 = null;
      this.f607 = null;
      if (this.f603 != 0) {
         this.m587();
      }

      this.f600 = false;
      super.onDisable();
   }

   private void m583(DrawContext ctx, float sx, float sy, List<Integer> active, SettingVisibility[] keys, Item[] its, float S) {
      float v8 = 22.0F * S;
      float v9 = 25.0F * S;
      float v10 = 3.5F * S;
      float v11 = 4.0F * S;
      float v12 = v11 * 2.0F + (float)active.size() * v8 + (float)Math.max(0, active.size() - 1) * v10;
      float v13 = v11 * 2.0F + v9;
      float v14 = hh.getInstance().alphaHUD.getValue();
      vr.glow(sx, sy, v12, v13, up.getColor(0, 0.08F * v14), 7.0F * S, 6.0F * S, 0.5F);
      vr.blur(sx, sy, v12, v13, v14, 7.0F * S, up.replAlpha(up.background(), v14));
      vr.outline(
         sx,
         sy,
         v12,
         v13,
         0.5F * S,
         up.getColor(255, 255, 255, (int)(25.0F * v14)),
         7.0F * S
      );
      float v15 = 2.0F;
      float v16 = (float)mc.method_22683().method_4495();
      float v17 = v15 / v16;

      for (int v18 = 0; v18 < active.size(); v18++) {
         int v19 = (Integer)active.get(v18);
         int v20 = keys[v19].get();
         Item v21 = its[v19];
         float v22 = sx + v11 + (float)v18 * (v8 + v10);
         float v23 = sy + v11;
         vr.rect(
            v22, v23, v8, v9, up.getColor(20, 20, 26, (int)(220.0F * v14)), 3.5F * S
         );
         vr.outline(
            v22,
            v23,
            v8,
            v9,
            0.5F * S,
            up.getColor(255, 255, 255, (int)(10.0F * v14)),
            3.5F * S
         );
         String v24 = eq.keyName(v20);
         float v25 = 5.5F * S;
         float v26 = v8 - 3.0F * S;
         float v27 = hu.aeonik.getWidth(v24, v25);
         float v28 = v25;
         if (v27 > v26 && v27 > 0.001F) {
            v28 = Math.max(3.2F * S, v25 * v26 / v27);
         }

         dt.enable(v22, v23, v8, 9.0F * S);
         hu.aeonik.drawCentered(v24, v22 + v8 / 2.0F, v23 + 2.0F * S, v28, up.replAlpha(up.client(), v14));
         dt.disable();
         ctx.method_51448().pushMatrix();
         Matrix3x2fStack result = ctx.method_51448();
         float temp1 = (v22 + v8 / 2.0F) * v17;
         float temp2 = v23 + v9 / 2.0F;

         result.translate(temp1, (temp2 + 1.5F * S) * v17);
         ctx.method_51448().scale(v17 * 0.48F * S, v17 * 0.48F * S);
         ctx.method_51427(new ItemStack(v21), -8, -9);
         ctx.method_51448().popMatrix();
         int v34 = this.m585(v21);
         String v29 = "x" + v34;
         float v30 = hu.aeonik.getWidth(v29, 5.0F * S);
         hu.aeonik
            .draw(
               v29,
               v22 + v8 - v30 - 2.0F * S,
               v23 + v9 - 6.5F * S,
               5.0F * S,
               up.getColor(235, 235, 240, (int)(240.0F * v14))
            );
         Animation v31 = this.f611.computeIfAbsent(v21, i -> {
            Animation var10000x = new Animation;

            var10000x./* $VF: Unable to resugar constructor */<init>();
            return var10000x;
         });
         v31.update();

         v31.run((double)mc.field_1724.method_7357().method_7905(v21.method_7854(), 0.0F), 0.12F, ap.LINEAR);
         float v32 = fq.clamp(v31.get(), 0.0F, 1.0F);
         if (v32 > 0.001F) {
            float v33 = v9 * v32;
            vr.rect(
               v22,
               v23 + v9 - v33,
               v8,
               v33,
               up.getColor(220, 40, 40, (int)(140.0F * v14)),
               3.5F * S
            );
         }
      }
   }

   private void m577() {
      this.f605 = -1;

      for (int v1 = 0; v1 < 36; v1++) {

         ItemStack v2 = mc.field_1724.method_31548().method_5438(v1);
         if (!v2.method_7960()) {
            if (this.f607 != null) {
               String v3 = v2.method_7964().getString().toLowerCase();
               if (v3.contains(this.f607)) {
                  this.f605 = v1;
                  break;
               }
            } else if (this.f606 != null && v2.method_7909() == this.f606) {
               this.f605 = v1;
               break;
            }
         }
      }

      if (this.f605 == -1) {
         String v6 = this.f607 != null ? this.f607 : this.f606.method_63680().getString();
         mc.field_1724
            .method_7353(
               Text.method_43470("Предмет [" + v6 + "] не найден!").method_27692(Formatting.field_1061),
               true
            );
      } else {
         this.f598 = mc.field_1724.method_31548().method_67532();
         this.f608 = this.f606 == Items.field_49098 || this.f605 != -1 && mc.field_1724.method_31548().method_5438(this.f605).method_31574(Items.field_49098);
         if (this.f608) {
            xd.update(new Rotation(mc.field_1724.method_36454(), 90.0F), 360.0F, 360.0F, 2, 200);
            mc.field_1724.method_36457(90.0F);
         }

         this.f599.reset();
         this.step = 1;
      }
   }

   @EventTarget
   public void onCooldownTick(EventRenderModel e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         Item[] v2 = CD_ITEMS;
         int v3 = v2.length;

         while (true) {

            if (v4 >= v3) {
               return;
            }

            Item v5 = v2[v4];
            boolean v6 = mc.field_1724.method_7357().method_7904(v5.method_7854());
            long[] v7 = this.f610.get(v5);
            if (v6) {
               if (v7 == null) {

                  this.f610.put(v5, new long[]{mc.field_1687.method_75260(), -2013736811807906685L + 2013736811807906684L});
               } else if (v7[1] < 0L) {
                  float v8 = mc.field_1724.method_7357().method_7905(v5.method_7854(), 0.0F);
                  long v9 = mc.field_1687.method_75260() - v7[0];
                  if (v9 > 0L && v8 > 0.0F && v8 < 1.0F) {
                     long v11 = (long)Math.round((float)v9 / (1.0F - v8));

                     if (v11 > 0L) {
                        v7[1] = v11;
                     }
                  }
               }
            } else if (v7 != null) {
               this.f610.remove(v5);
            }

            v4++;
         }
      }
   }

   
}
