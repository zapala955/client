package ru.white.module.impl.misc;

import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.StringSetting;
import ru.white.utils.misc.Of;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Vf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.module.impl.render.Hud;
import ru.white.module.impl.render.EntityEsp;
import ru.white.utils.render.vs;
import ru.white.utils.misc.Np;
import ru.white.utils.ItemUtil;
import ru.white.utils.ChatUtil;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Aura Crafter",
   category = Category.OTHER,
   desc = "Крафт Божьей ауры, простая закупка и простая продажа по биндам."
)
@Environment(EnvType.CLIENT)
public class AuraCrafter extends Module {
   private long f893;
   public final StringSetting buyItem;
   private pl.rl f868;
   private static final int AUTO_INVIS_CHECK_TICKS = 80;
   public final BooleanSetting randomSellPrice;
   public final StringSetting clanInvestAt;
   private String f876;
   public final StringSetting maxBuyPrice;
   private int f865;
   public final BooleanSetting autoPausePlayers;
   private int f884;
   private boolean f882;
   private int f856;
   private boolean f907;
   public final BooleanSetting autoInvis;
   private boolean f881;
   private boolean f878;
   private boolean f852;
   private long f854;
   private int f901;
   public final SettingVisibility buyDiamondsBind;
   public final StringSetting sellPrice;
   private int f879;
   private int f860;
   private int f911;
   public final StringSetting sellItem;
   private int f851;
   private int f877;
   public final BooleanSetting clanInvest;
   public final StringSetting clanInvestAmount;
   private int f903;
   private boolean f867;
   private int f897;
   public final StringSetting reserveMoney;
   private long f861;
   private pl.rl f845;
   private static final double COW_INTERACT_RANGE = 4.5;
   public final SettingVisibility fullAutoBind;
   private boolean f895;
   private int f847;
   private int f866;
   private int f915;
   private static final int BALANCE_REFRESH_CALLS = 55;
   private pl.ql f872;
   private int f908;
   private String f883;
   private pl.rl f885;
   private static final int MAX_MILK_BUCKET_USES = 8;
   public final StringSetting anarchyList;
   private long f890;
   public final SettingVisibility buyNetheriteBind;
   private long f892;
   private static final int TICK_DELAY = 6;
   public final SliderSetting autoPauseRange;
   private int f871;
   private int f858;
   private int f912;
   private int f886;
   private static final int INVIS_DRINK_TIMEOUT_TICKS = 60;
   private int f909;
   private int f846;
   private boolean f873;
   private int f894;
   private boolean f899;
   public final SliderSetting sellSlots;
   private pl.rl f902;
   private static final int JOIN_AH_DELAY = 200;
   private int f848;
   private long f891;
   private int f874;
   private String f869;
   private long f880;
   private boolean f905;
   private boolean f896;
   public final SettingVisibility sellBind;
   private String f853;
   private static final int STORAGE_EMPTY_SETTLE_TICKS = 60;
   private int f918;
   private int f863;
   private int f857;
   private int f917;
   private Item f855;
   private int f859;
   public final ModeSetting autoInvisType;
   private int f904;
   public final SettingVisibility craftBind = new SettingVisibility(this, "Бинд крафта");
   public final StringSetting diamondStackPrice;
   private String f870;
   private long f889;
   private static final int AH_SEARCH_RETRY_TICKS = 100;
   private int f910;
   private int f850;
   private int f913;
   private static final String AURA_NAME = "Божья аура";
   private boolean f888;
   private int f862;
   private static final int STORAGE_REOPEN_TIMEOUT_TICKS = 180;
   private int f864;
   private static final int NETHERITE_SWITCH_TICKS = 1800;
   private int f916;
   private int f849;
   public final vf infoDrag;
   private String f914;
   private int f906;
   private String f875;
   private static final String INVIS_SEARCH_QUERY = "инвиз";
   private int f900;
   private int f898;
   public final StringSetting randomSellPriceRange;
   private static final int PLAYER_PAUSE_RETURN_TICKS = 6000;
   private String f887;

   private boolean m1090(ItemStack stack) {

      String v2 = this.m1169(stack);
      boolean v3 = v2.matches(".*\\+\\s*\\d{1,2}\\s*[:：]\\s*\\d{2}.*");
      return v3 || v2.contains("8+") || v2.contains("+8") || v2.contains("усилен") || v2.contains("plus");
   }

   private int m1096(GenericContainerScreenHandler handler) {
      this.m1040();
      String v2 = this.m1170(this.f853 == null ? this.buyItem.getValue() : this.f853);
      long result;
      if (this.f854 > 0L) {

         result = this.f854;
      } else {
         result = this.m1129(this.maxBuyPrice.getValue(), -8722248953004610615L + -501123083850165194L);
      }

      long v3 = result;
      int v5 = handler.method_17388() * (9);
      long v7 = 8502818823549116229L + 720553213305659578L;
      long v9 = 0L;
      long v11 = this.m1129(this.reserveMoney.getValue(), 8650563073295490189L + -8650563073292490189L);

      for (int v19 = 0; v19 < v5; v19++) {
         Slot v20 = handler.method_7611(v19);
         if (v20.method_7681()) {
            ItemStack v21 = v20.method_7677();
            v13++;
            if (this.f855 != null && v21.method_7909() != this.f855) {
               v14++;
            } else if (!this.m1170(v21.method_7964().getString()).contains(v2)) {
               v15++;
            } else {
               long v22 = this.m1106(v21);
               if (v22 <= 0L) {
                  v16++;
               } else if (this.f852 && this.f855 == Items.field_22020 && this.f861 > 0L && this.f861 - v22 < v11) {
                  v17++;
               } else {
                  long v24 = v22 / (long)Math.max(1, v21.method_7947());
                  if (v24 <= v3 && v24 < v7) {
                     v7 = v24;
                     v6 = v19;
                     v9 = v22;
                  } else {
                     v18++;
                  }
               }
            }
         }
      }

      this.f880 = v9;
      String v32;
      if (v6 >= 0) {
         v32 = this.m1139(v9);
         String v26 = this.m1139(v7);
         String v27 = v32;
         StringBuilder v31 = new StringBuilder();

         v32 = v31.append("$").append((Object)v27).append("/").append((Object)v26).toString();
      } else {
         v32 = v13 == 0
            ? "нет лотов"
            : (
               v14 >= v13
                  ? "item-id"
                  : (v15 >= v13 - v14 ? "имя" : (v16 > 0 ? "нет цены" : (v17 > 0 ? "резерв" : (v18 > 0 ? "дорого" : "нет слота"))))
            );
      }

      this.f887 = v32;
      return v6;
   }

   private boolean m1121() {
      if (this.f845 != pl.rl.BUY_OPEN
         && this.f845 != pl.rl.BUY_READ
         && this.f845 != pl.rl.BUY_CONFIRM
         && this.f845 != pl.rl.INVIS_OPEN
         && this.f845 != pl.rl.INVIS_READ
         && this.f845 != pl.rl.INVIS_CONFIRM
         && this.f845 != pl.rl.SELL) {

         if (this.f845 != pl.rl.SELL_PREPARE
            && this.f845 != pl.rl.SELL_SEND
            && this.f845 != pl.rl.STORAGE_OPEN
            && this.f845 != pl.rl.STORAGE_CLICK
            && this.f845 != pl.rl.STORAGE_TAKE) {
            return false;
         }
      }

      return true;
   }

   private int m1103(GenericContainerScreenHandler handler, String name) {
      String v3 = name.toLowerCase();
      int v4 = handler.method_17388() * (9);

      for (int v5 = 0; v5 < v4; v5++) {
         Slot v6 = handler.method_7611(v5);
         if (v6.method_7681() && this.m1169(v6.method_7677()).contains(v3)) {
            return v5;
         }
      }

      return -1;
   }

   private String m1170(String text) {
      String result;
      if (text == null) {
         result = "";

         
      } else {
         result = text.replaceAll("§.", "").trim().toLowerCase();
      }

      return result;
   }

   private int m1085() {

      for (int v2 = 0; v2 < 36; v2++) {
         ItemStack v3 = mc.field_1724.method_31548().method_5438(v2);
         if (this.m1088(v3, true)) {
            return v2;
         }

         if (v1 == -1) {

            if (this.m1088(v3, false)) {
               v1 = v2;
            }
         }
      }

      return v1;
   }

   private String m1167() {
      if (this.f845 == pl.rl.PLAYER_PAUSE) {
         return "пауза игрок";
      } else {
         return switch (this.f845) {
            case IDLE -> "ожидает бинда";
            case CRAFT_MILK -> "пауза игрок";
            case STORAGE_TAKE -> "ждет анку";
            case INVIS_CONFIRM -> "ждет баланс";
            case INVIS_READ, AUTO_WAIT_BUDGET, CRAFT -> "закупает";
            case BUY_OPEN, INVIS_DRINK, SELL_SEND, BUY_CONFIRM -> "крафтит";
            case PLAYER_PAUSE, SELL_PREPARE, INVIS_OPEN, STORAGE_OPEN -> "инвиз";
            case CRAFT_OPEN_TABLE, STORAGE_CLICK, BUY_READ -> "продает";
            case CRAFT_FILL, AUTO_WAIT_JOIN, SELL -> {

               yield "перевыставляет";
            }
            default -> {
               MatchException result = new MatchException;

               result./* $VF: Unable to resugar constructor */<init>(null, null);
               throw result;
            }
         };
      }
   }

   private void m1056() {
      pl.rl v1 = this.f868 == null ? pl.rl.CRAFT : this.f868;
      this.f869 = null;
      this.f865 = 0;
      this.f866 = 0;
      this.f867 = false;
      this.f868 = pl.rl.IDLE;
      this.f850 = 0;
      this.f886 = 0;
      this.f883 = "";
      this.f884 = 0;
      switch (v1) {
         case INVIS_READ:
         case AUTO_WAIT_BUDGET:
         case CRAFT:
            if (this.f853 == null && this.f855 == null) {
               this.f845 = pl.rl.CRAFT;
               this.f846 = 6;
            } else {
               if (this.f852 && this.f855 == Items.field_22020 && !this.m1035(0L)) {
                  this.m1037();
                  return;
               }

               this.m1039();
               this.f845 = pl.rl.BUY_OPEN;
               this.f846 = 20;
            }
            break;
         case BUY_OPEN:
         case INVIS_DRINK:
         case SELL_SEND:
         case BUY_CONFIRM:
            this.f845 = pl.rl.CRAFT;
            this.f846 = 6;
            break;
         case PLAYER_PAUSE:
         case SELL_PREPARE:
         case INVIS_OPEN:
         case STORAGE_OPEN:
         default:
            this.f845 = pl.rl.CRAFT;
            this.f846 = 6;
            break;
         case CRAFT_OPEN_TABLE:
         case STORAGE_CLICK:
         case BUY_READ:
            this.f845 = pl.rl.SELL;
            this.f846 = 6;
            break;
         case CRAFT_FILL:
         case AUTO_WAIT_JOIN:
         case SELL:
            this.m1073();
      }

      int v2 = this.f864;
      StringBuilder temp1 = new StringBuilder;

      temp1./* $VF: Unable to resugar constructor */<init>();
      this.m1172(
         temp1.append(
               "Â§a[AuraCrafter] ÐŸÐ°ÑƒÐ·Ð° Ð·Ð°ÐºÐ¾Ð½Ñ‡Ð¸Ð»Ð°Ñ\u0081ÑŒ, Ð²ÐµÑ€Ð½ÑƒÐ»Ñ\u0081Ñ\u008f Ð½Ð° Ð°Ð½ÐºÑƒ "
            )
            .append(v2)
            .append(".")
            .toString()
      );
   }

   private void m1063() {
      if (this.m1085() >= 0) {
         this.f845 = pl.rl.INVIS_DRINK;
         this.f846 = 0;
      } else if (mc.field_1755 instanceof GenericContainerScreen v1) {
         GenericContainerScreenHandler v7 = (GenericContainerScreenHandler)v1.method_17577();
         if (this.m1098(v1, v7)) {
            this.f845 = pl.rl.INVIS_CONFIRM;
            this.f846 = 6;
         } else {
            int v3 = this.m1086(v7);
            if (v3 == -1) {
               this.m1097(v7);
               this.f846 = 10;
            } else {
               this.m1115(v7.field_7763, v3, 0, SlotActionType.field_7794);
               this.f845 = pl.rl.INVIS_CONFIRM;
               this.f846 = 10;
            }
         }
      } else {
         this.f845 = pl.rl.INVIS_OPEN;

         this.f846 = 6;
      }
   }

   private int m1151() {
      if (mc.field_1687 == null) {
         return -1;
      } else {
         Scoreboard v1 = mc.field_1687.method_8428();
         ScoreboardObjective v2 = v1.method_1189(ScoreboardDisplaySlot.field_45157);
         if (v2 == null) {
            return -1;
         } else {
            int v3 = this.m1152(v2.method_1114().getString());
            if (v3 > 0) {

               return v3;
            } else {
               for (ScoreboardEntry v5 : v1.method_1184(v2)) {
                  String v6 = Team.method_1142(v1.method_1164(v5.comp_2127()), v5.method_55387()).getString();
                  int result = this.m1152(v6);

                  int v7 = result;
                  if (v7 > 0) {
                     return v7;
                  }
               }

               return -1;
            }
         }
      }
   }

   private int m1104(GenericContainerScreenHandler handler) {
      int v2 = handler.method_17388() * (9);

      for (int v3 = 0; v3 < v2; v3++) {

         Slot v4 = handler.method_7611(v3);
         if (v4.method_7681() && v4.method_7677().method_7909() == Items.field_8466) {
            return v3;
         }
      }

      int v6 = this.m1103(handler, "Хранилище");
      return v6 >= 0 ? v6 : -1;
   }

   private long m1148(String text) {
      if (text == null) {

         return 0L;
      } else {
         int v2 = text.indexOf(36);
         if (v2 < 0) {
            return 0L;
         } else {
            String v3 = text.substring(v2).replaceAll("[^0-9]", "");

            return this.m1129(v3, 0L);
         }
      }
   }

   private int m1099(GenericContainerScreenHandler handler) {
      int v2 = this.m1101(handler);
      return v2 >= 0 ? v2 : this.m1100(handler);
   }

   private void m1029() {
      if (this.f852
         || this.f845 != pl.rl.SELL
            && this.f845 != pl.rl.SELL_PREPARE
            && this.f845 != pl.rl.SELL_SEND
            && this.f845 != pl.rl.STORAGE_OPEN
            && this.f845 != pl.rl.STORAGE_CLICK
            && this.f845 != pl.rl.STORAGE_TAKE) {
         this.reset();
         this.m1135();
         this.f845 = pl.rl.SELL;

         this.f848 = this.sellSlots.getValue().intValue();
         String v7 = this.sellItem.getValue();
         String v8 = this.sellPrice.getValue();
         String v1 = this.m1131();
         String v2 = v8;
         String v3 = v7;
         this.m1172("§e[AuraCrafter] Продажа: " + v3 + " по $" + v2 + v1);
      } else {
         this.reset();
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
         }

         this.m1172("§7[AuraCrafter] Продажа выключена.");
      }
   }

   private int m1092() {
      for (int v1 = 8; v1 >= 0; v1--) {

         if (mc.field_1724.method_31548().method_5438(v1).method_7960()) {
            return v1;
         }
      }

      return -1;
   }

   private int m1102(GenericContainerScreenHandler handler) {
      int v2 = handler.method_17388() * (9);
      int v3 = Math.max(0, v2 - (9));

      for (int v4 = v3; v4 < v2; v4++) {
         Slot v5 = handler.method_7611(v4);

         if (v5.method_7681() && v5.method_7677().method_7909() == Items.field_8137) {
            return v4;
         }
      }

      for (int v8 = 0; v8 < v2; v8++) {
         Slot v9 = handler.method_7611(v8);
         if (v9.method_7681()) {

            if (v9.method_7677().method_7909() == Items.field_8137) {
               return v8;
            }
         }
      }

      return -1;
   }

   private int m1101(GenericContainerScreenHandler handler) {
      int v2 = handler.method_17388() * (9);
      int v3 = Math.max(1, handler.method_17388());

      for (int v4 = 0; v4 < v3; v4++) {

         while (true) {

            if (v5 >= 4) {
               break;
            }

            int v6 = v4 * (9) + v5;
            if (v6 < v2) {
               Slot v7 = handler.method_7611(v6);
               if (v7.method_7681() && v7.method_7677().method_7909() == Items.field_8656) {
                  return v6;
               }
            }

            v5++;
         }
      }

      for (int v10 = 0; v10 < v2; v10++) {
         Slot v11 = handler.method_7611(v10);
         if (v11.method_7681() && v11.method_7677().method_7909() == Items.field_8656) {
            return v10;
         }
      }

      return -1;
   }

   private int m1072(String label, String value) {
      if ("Заработал".equals(label)) {
         return value.startsWith("-")
            ? up.getColor(255, 120, 120, 1.0F)
            : up.getColor(105, 255, 156, 1.0F);
      } else if ("Время".equals(label)) {
         return up.getColor(150, 225, 255, 1.0F);
      } else if ("В клан".equals(label)) {
         return up.getColor(255, 229, 148, 1.0F);
      } else if ("Вёдра".equals(label)) {
         return up.getColor(172, 210, 255, 1.0F);
      } else {
         int result = up.getColor(230);

         return result;
      }
   }

   private void m1048() {
      if (mc.field_1755 instanceof GenericContainerScreen v1) {
         GenericContainerScreenHandler v5 = (GenericContainerScreenHandler)v1.method_17577();
         if (!this.m1098(v1, v5)) {
            this.m1145();
            this.f878 = false;
            this.f879 = 0;
            this.f845 = pl.rl.BUY_READ;
            this.f846 = 10;
         } else if (this.f878) {
            if (++this.f879 < 20) {
               this.f846 = 2;
            } else {
               this.m1145();
               this.f878 = false;
               this.f879 = 0;

               this.f845 = pl.rl.BUY_READ;
               this.f846 = 10;
            }
         } else {
            int v3 = this.m1099(v5);
            if (v3 >= 0) {
               this.m1115(v5.field_7763, v3, 0, SlotActionType.field_7790);
               if (this.f861 > 0L && this.f880 > 0L) {
                  this.f861 = Math.max(0L, this.f861 - this.f880);
                  this.f881 = true;
               }

               this.f878 = true;
               this.f879 = 0;
               this.f846 = 10;
            } else {
               this.f845 = pl.rl.BUY_CONFIRM;
               this.f846 = 6;
            }
         }
      } else {
         this.m1145();
         this.f878 = false;
         this.f879 = 0;
         this.f845 = pl.rl.BUY_OPEN;
         this.f846 = 6;
      }
   }

   private long m1129(String value, long fallback) {
      String v4 = value == null ? "" : value.replaceAll("[^0-9]", "");
      if (v4.isEmpty()) {
         return fallback;
      } else {
         try {
            return Long.parseLong(v4);
         } catch (NumberFormatException v7) {
            return fallback;
         }
      }
   }

   private void m1064() {
      if (this.m1085() >= 0) {
         this.f845 = pl.rl.INVIS_DRINK;
         this.f846 = 0;
      } else if (mc.field_1755 instanceof GenericContainerScreen v1) {
         GenericContainerScreenHandler v6 = (GenericContainerScreenHandler)v1.method_17577();
         if (!this.m1098(v1, v6)) {
            this.f905 = false;
            this.f906 = 0;
            this.f845 = pl.rl.INVIS_READ;
            this.f846 = 10;
         } else if (this.f905) {
            if (++this.f906 < 20) {
               this.f846 = 2;
            } else {
               this.f905 = false;
               this.f906 = 0;
               this.f845 = pl.rl.INVIS_DRINK;
               this.f846 = 4;
            }
         } else {
            int v3 = this.m1099(v6);
            if (v3 >= 0) {
               this.m1115(v6.field_7763, v3, 0, SlotActionType.field_7790);
               this.f905 = true;
               this.f906 = 0;
               this.f846 = 10;
            } else {
               this.f846 = 6;
            }
         }
      } else {
         this.f845 = pl.rl.INVIS_DRINK;

         this.f846 = 4;
      }
   }

   private void m1069(EventReceivePacket event) {
      int v2 = this.m1112(Items.field_8477);
      int v3 = this.m1112(Items.field_22020);
      int v4 = this.m1112(Items.field_8103);
      int v5 = this.m1112(Items.field_8550);
      int v6 = Math.min(v3, v2 / 4);
      int v7 = this.m1113();
      int v8 = this.sellSlots.getValue().intValue();
      String[] result = new String[15];
      result[0] = "Режим";
      result[1] = "Авто";
      result[2] = "Анка";
      result[3] = "Баланс";
      result[4] = "Крафтов";
      result[5] = "Алмазы";
      result[6] = "Незер";
      result[7] = "Молоко";
      result[8] = "Пустые";
      result[9] = "Закуп";
      result[10] = "Резерв";
      result[11] = "Клан";
      result[12] = "Фильтр";
      result[13] = "Продажа";
      result[14] = "Лоты";
      String[] v9 = result;
      result = new String[15];
      result[0] = this.m1167();
      result[1] = this.f852 ? "ON" : "OFF";
      String temp3 = this.f862 > 0 ? String.valueOf(this.f862) : "?";
      String temp4;
      if (this.f863 > 0) {
         int v25 = this.f863;
         temp4 = " -> " + v25;
      } else {
         temp4 = "";
      }

      String v26 = temp4;
      String v27 = temp3;
      result[2] = v27 + v26;
      if (this.f861 > 0L) {
         String v28 = this.m1139(this.f861);
         temp3 = "$" + v28;
      } else {
         temp3 = "?";
      }

      result[3] = temp3;
      result[4] = this.m1139((long)v6);
      result[5] = this.m1139((long)v2);
      result[6] = this.m1139((long)v3);
      int temp2 = 7;
      temp3 = this.m1139((long)v4);
      String v29 = this.m1139((long)v7);
      String v30 = temp3;
      result[temp2] = v30 + "/" + v29;
      result[8] = this.m1139((long)v5);
      temp2 = 9;
      temp3 = this.f853 == null ? this.buyItem.getValue() : this.f853;
      String v31 = this.m1139(this.f854 > 0L ? this.f854 : this.m1129(this.maxBuyPrice.getValue(), 0L));
      String v32 = temp3;
      result[temp2] = v32 + " <= $" + v31;
      temp2 = 10;
      String v33 = this.m1139(this.m1129(this.reserveMoney.getValue(), 0L));
      result[temp2] = "$" + v33;
      temp2 = 11;
      if (this.clanInvest.getValue()) {
         temp3 = this.m1139(this.m1129(this.clanInvestAmount.getValue(), 0L));
         String v34 = this.m1139(this.m1129(this.clanInvestAt.getValue(), 0L));
         String v35 = temp3;
         temp3 = v35 + " от " + v34;
      } else {
         temp3 = "OFF";
      }

      result[temp2] = temp3;
      result[12] = this.f887.isEmpty() ? "ok" : this.f887;
      temp2 = 13;
      temp3 = this.sellItem.getValue();
      temp4 = this.m1139(this.m1129(this.sellPrice.getValue(), 0L));
      String v36 = this.m1131();
      String v37 = temp4;
      String v38 = temp3;
      result[temp2] = v38 + " $" + v37 + v36;
      temp2 = 14;
      int v40 = this.f847;
      result[temp2] = v40 + "/" + v8;
      String[] v10 = result;
      String[] v11 = new String[]{"Время", "Заработал", "Продажи", "В клан", "Вёдра"};

      result = new String[]{this.m1137(), this.m1138(this.m1136()), null, null, null};
      String v41 = this.m1139(this.f891);
      result[2] = "$" + v41;
      String v42 = this.m1139(this.f893);
      result[3] = "$" + v42;

      result[4] = v7 + "/8";
      String[] v12 = result;
      float v13 = 130.0F;

      for (int v14 = 0; v14 < v11.length; v14++) {
         v13 = Math.max(v13, 16.0F + hu.sf_medium.getWidth(v11[v14], 5.0F) + hu.sf_medium.getWidth(v12[v14], 5.0F) + 16.0F);
      }

      for (int v47 = 0; v47 < v9.length; v47++) {
         v13 = Math.max(v13, 16.0F + hu.sf_medium.getWidth(v9[v47], 5.0F) + hu.sf_medium.getWidth(v10[v47], 5.0F) + 16.0F);
      }

      float v48 = 8.0F;
      float v15 = 24.0F + (float)(v9.length + v11.length) * v48;
      float v16 = this.infoDrag.position.x;
      float v17 = this.infoDrag.position.y;
      this.infoDrag.size.set(v13, v15);
      if (eh.getAlpha() != 1.0F) {
         vs.ws.glass(v16, v17, v13, v15, 1.0F, 7.0F, up.getRectColor(eh.getAlpha()), 15.0F, 1.0F, 1.0F, 3.0F);
      }

      String v18 = this.f845 == pl.rl.IDLE ? "IDLE" : "ACTIVE";
      float v19 = hu.sf_medium.getWidth(v18, 6.5F);
      hu.sf_medium.draw("Aura Crafter", v16 + 6.5F, v17 + 7.0F, 6.5F, up.getColor(255));
      hu.sf_medium
         .draw(
            v18,
            v16 + v13 - 6.5F - v19,
            v17 + 7.0F,
            6.5F,
            this.f845 == pl.rl.IDLE
               ? up.getColor(170)
               : up.getColor(85, 255, 119, 1.0F)
         );
      float v20 = v17 + 18.0F;

      for (int v21 = 0; v21 < v11.length; v21++) {
         float v22 = v20 + (float)v21 * v48;
         hu.sf_medium.draw(v11[v21], v16 + 6.5F, v22 + 2.0F, 6.0F, this.m1071(v11[v21]));
         String temp1 = v12[v21];
         float v60 = v16 + v13 - 6.5F;
         temp4 = v12[v21];

         hu.sf_medium.draw(temp1, v60 - hu.sf_medium.getWidth(temp4, 6.0F), v22 + 2.0F, 6.0F, this.m1072(v11[v21], v12[v21]));
      }

      v20 += (float)v11.length * v48;

      for (int v50 = 0; v50 < v9.length; v50++) {
         float v51 = v20 + (float)v50 * v48;
         int v23 = v50 == 0 ? up.getClientColor(255) : up.getColor(180);
         int v24 = v50 == 1 && this.f852
            ? up.getColor(85, 255, 119, 1.0F)
            : up.getColor(220);
         hu.sf_medium.draw(v9[v50], v16 + 6.5F, v51 + 2.0F, 6.0F, v23);
         hu.sf_medium.draw(v10[v50], v16 + v13 - 6.5F - hu.sf_medium.getWidth(v10[v50], 6.0F), v51 + 2.0F, 6.0F, v24);
      }
   }

   private void m1110(CowEntity cow) {
      Vec3d v2 = new Vec3d(cow.method_23317(), cow.method_23318(), cow.method_23321())
         .method_1031(
            this.m1134(-0.18, 0.18), MathHelper.method_15350((double)cow.method_17682() * this.m1134(0.45, 0.78), 0.45, 1.35), this.m1134(-0.18, 0.18)
         );
      Vec3d v3 = mc.field_1724.method_33571();
      double v4 = v2.field_1352 - v3.field_1352;
      double v6 = v2.field_1351 - v3.field_1351;
      double v8 = v2.field_1350 - v3.field_1350;
      double v10 = Math.sqrt(v4 * v4 + v8 * v8);
      float v12 = (float)MathHelper.method_15338(Math.toDegrees(Math.atan2(v8, v4)) - 90.0);

      double result = Math.toDegrees(Math.atan2(v6, v10));

      float v13 = (float)MathHelper.method_15350(-result, -90.0, 90.0);
      xd.update(this.m1109(v12, v13, 0.58F, 0.82F), this.m1133(13.0F, 24.0F), this.m1133(11.0F, 21.0F), this.m1132(1, 2), 2);
   }

   private boolean m1168(ItemStack stack) {
      if (!stack.method_7960()) {
         String result = this.m1170(stack.method_7964().getString());
         String temp1 = this.sellItem.getValue();

         temp1 = temp1.trim().toLowerCase();

         if (result.contains(temp1)) {
            return true;
         }
      }

      return false;
   }

   private int m1132(int min, int max) {
      if (max <= min) {

         return min;
      } else {
         double temp1 = Math.random();

         return min + (int)Math.floor(temp1 * (double)(max - min + 1));
      }
   }

   private void m1058() {
      if (!this.autoInvis.getValue()) {
         this.f899 = false;
         this.f900 = -1;
      } else {
         if (!this.f899) {
            this.f899 = true;
            this.f900 = this.m1150();
            
         }

         if (this.f900 <= 0) {
            this.f900 = this.m1150();
         }
      }
   }

   private int m1112(Item item) {

      for (int v3 = 0; v3 < 36; v3++) {
         ItemStack v4 = mc.field_1724.method_31548().method_5438(v3);

         if (v4.method_7909() == item) {
            v2 += v4.method_7947();
         }
      }

      return v2;
   }

   private int m1125() {
      for (int v1 = 9; v1 < 36; v1++) {
         if (mc.field_1724.method_31548().method_5438(v1).method_7960()) {
            return this.m1127(v1);
         }
      }

      for (int v3 = 1; v3 < 9; v3++) {
         if (mc.field_1724.method_31548().method_5438(v3).method_7960()) {

            return this.m1127(v3);
         }
      }

      return -1;
   }

   private void m1073() {
      if (this.f847 <= 0 && this.m1157() <= 0) {
         this.f895 = false;
         this.f896 = true;
         this.f898 = 0;
         if (this.f852) {
            this.m1032();
         } else {
            this.stop(
               "§a[AuraCrafter] Продажа выключена: лотов и предметов для продажи нет."
            );
         }
      } else {
         this.f895 = false;
         this.f896 = true;
         this.f898 = 0;
         this.m1120("ah");
         this.f845 = pl.rl.STORAGE_OPEN;

         this.f850 = 0;
         this.f886 = 0;
         this.f846 = 20;
      }
   }

   private long m1153(String line) {
      String v2 = this.m1170(line);
      if (!v2.contains("монет")) {

         if (!v2.contains("баланс")) {

            return -2773638674854270997L + 2773638674854270996L;
         }
      }

      return this.m1129(v2, -8285314353548747666L + 8285314353548747665L);
   }

   private void m1171(String command) {
      if (command != null) {
         boolean result = command.isEmpty();

         if (!result) {
            this.f876 = command;
            if (this.f882) {
               this.m1165(command);
               return;
            }

            mc.field_1724.field_3944.method_45730(command.startsWith("/") ? command.substring(1) : command);
            return;
         }
      }
   }

   private PlayerEntity m1057() {
      cj v1 = cj.get();
      if (v1 != null && v1.isEnabled() && v1.player.getValue()) {
         if (mc.field_1687 != null && mc.field_1724 != null) {
            double v2 = this.autoPauseRange.getValue().doubleValue();
            double v4 = v2 * v2;
            PlayerEntity v6 = null;
            double v7 = v4;

            for (PlayerEntity v10 : mc.field_1687.method_18456()) {
               if (v10 != mc.field_1724) {
                  String v11 = v10.method_5477().getString();
                  if (v11 != null) {

                     if (!v11.isEmpty()
                        && (v10.method_5797() == null || !v10.method_5797().getString().startsWith("Ghost_"))
                        && !Client.get().friendManager().isFriend(v11)
                        && (!v1.ignoreNaked.getValue() || cj.hasArmor(v10))) {
                        double v12 = v10.method_5858(mc.field_1724);
                        if (v12 <= v7) {
                           v7 = v12;
                           v6 = v10;
                        }
                     }
                  }
               }
            }

            return v6;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private void m1079(CraftingScreenHandler handler) {

      while (v2 <= 9) {
         Item v3 = this.m1081(v2);
         ItemStack v4 = handler.method_7611(v2).method_7677();
         if (!v4.method_7960() && v4.method_7909() != v3) {
            this.m1115(handler.field_7763, v2, 0, SlotActionType.field_7794);
         }

         v2++;
         
      }
   }

   private void m1120(String command) {
      if (command != null && !command.isEmpty()) {
         if (this.f914 != null) {
            this.f914 = command;
         } else {
            this.f914 = command;

            this.f915 = this.m1132(4, 18);
            this.f912 = 0;

            double v5 = Math.random();

            this.f913 = v5 < 0.65 ? this.m1132(3, 13) : 0;
         }
      }
   }

   private void m1116() {
      if (this.f916 > 0) {
         this.f916--;
         this.m1117();
         this.f910 = 0;
      } else if (this.f875 == null) {
         if (this.f845 != pl.rl.IDLE
            && this.f845 != pl.rl.PLAYER_PAUSE
            && mc.field_1755 == null
            && !mc.field_1724.method_6115()
            && this.f845 != pl.rl.CRAFT_OPEN_TABLE
            && this.f845 != pl.rl.CRAFT_FILL
            && this.f845 != pl.rl.CRAFT_MILK) {
            if (this.f910 > 0) {
               this.f910--;
               if (this.f910 <= 0) {
                  this.m1117();

                  this.f911 = this.m1132(v7, 520);
               }
            } else if (this.f911 > 0) {
               this.f911--;
            } else {
               this.m1117();
               int v1 = this.m1132(0, 8);

               mc.field_1690.field_1894.method_23481(v1 == 0 || v1 == 4 || v1 == 6);
               boolean temp1;
               if (v1 == 1) {
                  temp1 = true;
               } else {
                  temp1 = false;
                  
               }

               mc.field_1690.field_1881.method_23481(temp1);
               mc.field_1690.field_1913.method_23481(v1 == 2 || v1 == 4 || v1 == 7);
               mc.field_1690.field_1849.method_23481(v1 == 3 || v1 == 5 || v1 == 6);
               mc.field_1690.field_1903.method_23481(v1 == 7 && Math.random() < 0.35);
               mc.field_1690.field_1832.method_23481(v1 == 8 || Math.random() < 0.12);
               this.f910 = this.m1132(5, 32);
               if (Math.random() < 0.45) {
                  float v2 = mc.field_1724.method_36454() + this.m1133(-14.0F, 14.0F);
                  float v3 = MathHelper.method_15363(mc.field_1724.method_36455() + this.m1133(-4.5F, 4.5F), -75.0F, 75.0F);
                  xd.update(this.m1109(v2, v3, 0.28F, 0.48F), this.m1133(5.0F, 14.0F), this.m1133(5.0F, 13.0F), this.m1132(3, 7), 1);
               }
            }
         } else {
            if (this.f910 > 0) {
               this.m1117();
            }

            this.f910 = 0;
            if (this.f911 <= 0) {
               this.f911 = this.m1132(130, 380);
            }
         }
      }
   }

   private void m1046() {
      if (mc.field_1755 instanceof GenericContainerScreen v1) {
         if (this.m1042()) {
            this.m1041();
            this.f846 = 2;
         } else {
            this.f859 = -1;
            this.f849 = ((GenericContainerScreenHandler)v1.method_17577()).field_7763;
            this.f850 = 0;
            this.f845 = pl.rl.BUY_READ;
            this.f846 = 6;
         }
      } else {
         this.f850++;
         if (this.f849 == -1 && this.f850 > 17) {

            this.m1039();
            this.f846 = 20;
         } else if (this.f849 != -1 && this.f850 > 20) {
            if (this.f852) {
               this.m1154();
            } else {
               this.stop("§7[AuraCrafter] Закуп остановлен: AH закрыт.");
            }
         } else {
            this.f846 = 6;
         }
      }
   }

   private void m1039() {
      this.m1040();
      String v1 = this.f853 == null ? this.buyItem.getValue().trim() : this.f853;

      this.m1041();
      this.f849 = -1;
      this.f850 = 0;
      this.f858 = 0;
      this.m1120("ah search " + v1);
   }

   private void m1025() {
      if (!this.f852 && this.m1027(Items.field_22020)) {
         this.m1028();
      } else {
         this.reset();
         this.m1135();

         this.m1038(this.buyItem.getValue().trim(), this.m1129(this.maxBuyPrice.getValue(), 1798651491294184839L + -1798651491293859839L), Items.field_22020, 0);

         String v1 = this.maxBuyPrice.getValue();
         String temp1 = "§e[AuraCrafter] Закуп незера до $" + v1;

         this.m1172(temp1);
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1761 != null) {
         this.m1164();
         this.m1143();
         this.m1116();
         this.m1118();
         if (!this.m1054()) {
            if (!this.m1119()) {
               if (!this.m1053()) {
                  if (this.f845 != pl.rl.IDLE) {
                     if (!this.m1043()) {
                        this.m1140();
                        if (this.f852 && this.f862 <= 0) {
                           this.f862 = this.m1150();
                        }

                        if (!this.f852 || !this.m1161()) {
                           if (this.f846 > 0) {
                              this.f846--;
                           } else {
                              switch (this.f845) {
                                 case IDLE:
                                 default:
                                    break;
                                 case CRAFT_MILK:
                                    this.m1055();
                                    break;
                                 case STORAGE_TAKE:
                                    this.m1044();
                                    break;
                                 case INVIS_CONFIRM:
                                    this.m1045();
                                    break;
                                 case INVIS_READ:
                                    this.m1046();
                                    break;
                                 case AUTO_WAIT_BUDGET:
                                    this.m1047();
                                    break;
                                 case CRAFT:
                                    this.m1048();
                                    break;
                                 case BUY_OPEN:
                                    this.m1049();
                                    break;
                                 case INVIS_DRINK:
                                    this.m1050();
                                    int v5 = v2 * (v2 + 1) % 2;

                                    if (v5 != 0) {
                                       }
                                    break;
                                 case SELL_SEND:
                                    this.m1051();
                                    break;
                                 case BUY_CONFIRM:
                                    this.m1052();
                                    
                                    break;
                                 case PLAYER_PAUSE:
                                    this.m1062();
                                    break;
                                 case SELL_PREPARE:
                                    this.m1063();
                                    break;
                                 case INVIS_OPEN:
                                    this.m1064();
                                    break;
                                 case STORAGE_OPEN:
                                    this.m1065();
                                    break;
                                 case CRAFT_OPEN_TABLE:
                                    this.m1067();
                                    break;
                                 case STORAGE_CLICK:
                                    this.m1068();
                                    break;
                                 case CRAFT_FILL:
                                    this.m1074();
                                    break;
                                 case AUTO_WAIT_JOIN:
                                    this.m1075();
                                    break;
                                 case SELL:
                                    this.m1076();
                                    break;
                                 case BUY_READ:
                                    this.m1066();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void m1093() {
      if (mc.field_1690 != null) {

         mc.field_1690.field_1904.method_23481(false);
      }
   }

   private void m1166() {
      if (mc.field_1690 != null) {

         mc.field_1690.field_1894.method_23481(false);
         mc.field_1690.field_1913.method_23481(false);
         mc.field_1690.field_1849.method_23481(false);
         mc.field_1690.field_1903.method_23481(false);
         mc.field_1690.field_1832.method_23481(false);
      }
   }

   private int m1105(GenericContainerScreenHandler handler) {
      int v2 = handler.method_17388() * (9);

      for (int v3 = 0; v3 < v2; v3++) {
         Slot v4 = handler.method_7611(v3);
         if (v4.method_7681()) {
            ItemStack v5 = v4.method_7677();
            if (this.m1168(v5) && this.m1169(v5).contains("продается")) {
               return v3;
            }
         }
      }

      for (int v8 = 0; v8 < v2; v8++) {

         Slot result = handler.method_7611(v8);

         Slot v9 = result;
         if (v9.method_7681() && this.m1168(v9.method_7677())) {
            return v8;
         }
      }

      return -1;
   }

   private boolean m1053() {
      this.m1058();
      if (this.m1083()) {
         return false;
      } else if (this.autoInvis.getValue() && !this.m1084()) {
         if (++this.f901 < 80) {
            return false;
         } else {
            this.f901 = 0;
            if (!this.m1059()) {
               return false;
            } else {
               this.m1060();
               if (this.m1085() >= 0) {

                  this.f845 = pl.rl.INVIS_DRINK;
                  this.f846 = 0;
               } else {
                  this.m1061();
                  this.f845 = pl.rl.INVIS_OPEN;
                  this.f846 = 20;
               }

               return true;
            }
         }
      } else {
         this.f901 = 0;
         return false;
      }
   }

   private boolean m1083() {
      if (this.f845 != pl.rl.INVIS_OPEN) {

         if (this.f845 != pl.rl.INVIS_READ && this.f845 != pl.rl.INVIS_CONFIRM && this.f845 != pl.rl.INVIS_DRINK) {
            return false;
         }
      }

      return true;
   }

   private String m1131() {
      if (!this.randomSellPrice.getValue()) {
         return "";
      } else {
         long v1 = this.m1129(this.randomSellPriceRange.getValue(), 0L);

         String v8;
         if (v1 > 0L) {
            String v3 = this.m1139(v1);
            StringBuilder v7 = new StringBuilder().append(" ±$");

            v8 = v7.append((Object)v3).toString();
         } else {
            v8 = "";
         }

         return v8;
      }
   }

   private void m1162() {
      if (this.f862 <= 0) {
         this.f862 = this.m1150();
      }

      if (this.f862 > 0) {

         this.m1163(this.f862, pl.ql.CRAFT);
      }
   }

   private String m1169(ItemStack stack) {
      StringBuilder v2 = new StringBuilder(this.m1170(stack.method_7964().getString()));
      LoreComponent v3 = (LoreComponent)stack.method_57353().method_58694(DataComponentTypes.field_49632);
      if (v3 != null) {

         Iterator v4 = v3.comp_2400().iterator();

         while (true) {

            if (!v4.hasNext()) {
               break;
            }

            Text v5 = (Text)v4.next();
            v2.append((char)(32)).append(this.m1170(v5.getString()));
         }
      }

      return v2.toString();
   }

   private int m1150() {
      int result = this.m1151();

      int v1 = result;
      if (v1 > 0) {
         return v1;
      } else {
         int v2 = iq.getAnarchy();
         return v2 > 0 ? v2 : -1;
      }
   }

   private boolean m1146(String lower) {
      if (!lower.contains("не удалось купить") && !lower.contains("уже куп")) {

         if (!lower.contains("куплен") && !lower.contains("истек") && !lower.contains("нет в наличии")) {
            return false;
         }
      }

      return true;
   }

   private int m1100(GenericContainerScreenHandler handler) {
      int v2 = handler.method_17388() * (9);

      for (int v3 = 0; v3 < v2; v3++) {

         Slot v4 = handler.method_7611(v3);
         if (v4.method_7681()) {
            ItemStack v5 = v4.method_7677();
            if (v5.method_7909() == Items.field_8581 && this.m1169(v5).contains("купить")) {
               return v3;
            }
         }
      }

      return -1;
   }

   private boolean m1027(Item item) {

      if (this.f845 != pl.rl.BUY_OPEN) {

         if (this.f845 != pl.rl.BUY_READ && this.f845 != pl.rl.BUY_CONFIRM) {
            return false;
         }
      }

      return this.f855 == item;
   }

   private void m1065() {
      if (this.m1084()) {
         this.m1094();
      } else if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
         this.f846 = 2;
      } else {
         int v1 = this.m1085();
         if (!this.f907) {
            if (v1 == -1) {
               if (++this.f908 > 40) {

                  this.m1061();
                  this.f845 = pl.rl.INVIS_OPEN;
                  this.f846 = 20;
               } else {
                  this.f846 = 2;
               }
            } else {
               this.f909 = mc.field_1724.method_31548().method_67532();
               int v2 = this.m1091(v1);
               if (v2 < 0) {
                  this.f846 = 5;
               } else {
                  mc.field_1724.method_31548().method_61496(v2);
                  mc.field_1690.field_1904.method_23481(true);
                  mc.field_1761.method_2919(mc.field_1724, Hand.field_5808);
                  this.f907 = true;
                  this.f908 = 0;
                  this.f846 = 1;
               }
            }
         } else {
            mc.field_1690.field_1904.method_23481(true);
            if (++this.f908 <= 60) {
               this.f846 = 1;
            } else {
               this.m1093();
               this.f907 = false;
               this.f908 = 0;
               if (this.m1085() == -1) {
                  this.m1061();
                  this.f845 = pl.rl.INVIS_OPEN;
                  this.f846 = 20;
               } else {
                  this.f846 = 4;
               }
            }
         }
      }
   }

   private boolean m1088(ItemStack stack, boolean strictPlus) {
      if (!stack.method_7960() && stack.method_7909() == Items.field_8574) {
         String v3 = this.autoInvisType.getValue();
         String v4 = this.m1169(stack);
         boolean v5 = v4.contains("инвиз") || v4.contains("невид");
         boolean v6 = this.m1089(stack, Potions.field_8997);
         boolean v7 = this.m1089(stack, Potions.field_9000);
         if ("3m".equalsIgnoreCase(v3)) {
            return v6 || v5 && v4.contains("3") && !v4.contains("8");
         } else if (!"8+".equalsIgnoreCase(v3)) {
            return v7 || v5 && v4.contains("8");
         } else {
            return strictPlus ? (v7 || v5) && this.m1090(stack) : v7 || v5 && v4.contains("8");
         }
      } else {
         return false;
      }
   }

   private void m1154() {
      this.m1040();
      if (!this.f852) {
         this.reset();
      } else {
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
         }

         if (this.f888) {
            this.f888 = false;

            this.f853 = null;
            this.f855 = null;
            this.f856 = 0;
            this.f854 = 6376348204585258437L + -6376348204585258438L;
            this.m1155();
            
         } else if (this.f855 == Items.field_8477) {
            this.m1033();
         } else {
            this.m1155();
         }
      }
   }

   private int m1122(Item item) {
      for (int v2 = 0; v2 < 36; v2++) {
         Item result = mc.field_1724.method_31548().method_5438(v2).method_7909();

         if (result == item) {
            return v2;
         }
      }

      return -1;
   }

   private void m1040() {
      if (this.f853 != null) {
         String v1 = this.m1170(this.f853);

         boolean v2 = v1.contains("алмаз") || v1.contains("diamond");
         if (v2) {
            this.f855 = Items.field_8477;
            if (this.f856 <= 0) {
               this.f856 = 256;
            }
         } else {
            int v6 = v3 + 1;

            if (this.f855 != Items.field_22020 || this.f856 > 0) {
               this.f855 = Items.field_22020;
               this.f856 = 0;
               this.f854 = this.m1129(this.maxBuyPrice.getValue(), 4133904337712952603L + -4133904337712627603L);
               this.f887 = "";
            }
         }
      }
   }

   private int m1086(GenericContainerScreenHandler handler) {
      int v2 = this.m1087(handler, true);
      return v2 >= 0 ? v2 : this.m1087(handler, false);
   }

   private void m1047() {
      this.m1040();
      if (mc.field_1755 instanceof GenericContainerScreen v1) {
         GenericContainerScreenHandler v7 = (GenericContainerScreenHandler)v1.method_17577();
         if (this.f849 != -1 && v7.field_7763 != this.f849) {
            this.f849 = v7.field_7763;
         }

         if (this.m1098(v1, v7)) {
            this.f845 = pl.rl.BUY_CONFIRM;

            this.f846 = 6;
         } else if (this.f855 != null && this.f856 > 0 && this.m1112(this.f855) >= this.f856) {
            this.m1154();
         } else {
            if (this.f852 && this.f855 == Items.field_22020) {
               this.m1144(false);
               if (!this.m1035(0L)) {

                  if (this.m1112(Items.field_22020) > 0) {
                     this.m1154();
                  } else {
                     this.m1037();
                  }

                  return;
               }
            }

            if (this.f852 && this.f855 == Items.field_22020 && this.f861 <= 0L && this.f861 <= 0L) {
               this.m1171("money");
               this.f846 = 20;
            } else {
               int v3 = this.m1096(v7);
               if (v3 == -1) {
                  if (this.f852 && this.f855 == Items.field_22020 && "резерв".equals(this.f887)) {
                     if (this.m1112(Items.field_22020) > 0) {
                        this.m1154();
                     } else {
                        this.m1037();
                     }
                  } else if (this.f852 && this.f855 == Items.field_22020 && (this.f857 += 10) >= 1800) {
                     this.m1158();
                  } else {
                     this.m1097(v7);
                     this.f846 = 10;
                  }
               } else if (!this.f852
                  || this.f855 != Items.field_22020
                  || this.f861 <= 0L
                  || this.f861 - this.f880 >= this.m1129(this.reserveMoney.getValue(), -6466977448009982245L + 6466977448012982245L)) {
                  this.m1115(v7.field_7763, v3, 0, SlotActionType.field_7794);

                  this.f845 = pl.rl.BUY_CONFIRM;
                  this.f846 = 10;
               } else if (this.m1112(Items.field_22020) > 0) {
                  this.m1154();
               } else {
                  this.m1037();
               }
            }
         }
      } else {
         if (this.f852) {
            this.m1154();
         } else {
            this.stop("§7[AuraCrafter] Закуп остановлен: AH закрыт.");
         }
      }
   }

   private boolean m1059() {
      if (this.f875 == null) {

         if (!mc.field_1724.method_6115()) {
            if (!this.f873 && this.f863 <= 0) {
               int temp2 = v2 + 1;

               if (this.f845 != pl.rl.AUTO_WAIT_JOIN && this.f845 != pl.rl.AUTO_WAIT_BUDGET) {
                  int v1 = this.m1150();
                  return this.f900 > 0 && v1 == this.f900;
               }
            }

            return false;
         }
      }

      return false;
   }

   private double m1134(double min, double max) {
      double temp1 = Math.random() * (max - min);

      return min + temp1;
   }

   private boolean m1147(String lower) {
      if (lower.contains("баланс")) {

         if (lower.contains("аукцион") && lower.contains("монет")) {
            return true;
         }
      }

      return false;
   }

   private long m1136() {

      if (this.f890 > 0L && this.f861 >= 0L) {
         long result = this.f861 - this.f890 + this.f893;

         return result;
      } else {
         return this.f891 - this.f892;
      }
   }

   private void m1097(GenericContainerScreenHandler handler) {
      int v2 = this.m1102(handler);
      if (v2 >= 0) {

         this.m1115(handler.field_7763, v2, 0, SlotActionType.field_7790);
      }
   }

   private void m1034() {
      this.f888 = true;
      this.f873 = false;
      long temp1 = this.m1129(this.diamondStackPrice.getValue(), 8079609421829958204L + -8079609421829708204L);

      long v1 = Math.max(1L, temp1 / (-8800672262597763720L + 8800672262597763784L));
      this.m1038("Алмаз", v1, Items.field_8477, 256);
      this.m1172(
         "§e[AuraCrafter] Алмазы кончились, докупаю из резерва и вернусь к крафту."
      );
   }

   private boolean m1098(GenericContainerScreen screen, GenericContainerScreenHandler handler) {
      String v3 = this.m1170(screen.method_25440().getString());
      if (!v3.contains("подтверждение")
         && !v3.contains("подозрительная цена")) {

         if (this.m1100(handler) < 0) {
            return false;
         }
      }

      return true;
   }

   private void m1074() {
      if (mc.field_1755 instanceof GenericContainerScreen) {
         this.f850 = 0;
         this.f845 = pl.rl.STORAGE_CLICK;
         this.f846 = 2;
      } else if (++this.f850 > 180) {
         this.m1120("ah");
         this.f850 = 0;
         this.f846 = 20;
      } else {

         this.f846 = 6;
      }
   }

   private void m1126(int inventorySlot, int hotbarSlot) {
      if (inventorySlot != hotbarSlot) {
         int v3 = this.m1127(inventorySlot);

         this.m1115(mc.field_1724.field_7512.field_7763, v3, hotbarSlot, SlotActionType.field_7791);
      }
   }

   private int m1159() {
      String[] v1 = this.anarchyList.getValue().split("[,; ]+");

      while (v2 < v1.length) {
         String v3 = v1[this.f871++ % Math.max(1, v1.length)].replaceAll("[^0-9]", "");
         if (!v3.isEmpty()) {
            int v4 = (int)this.m1129(v3, 2234200627101752741L + -2234200627101752742L);
            if (v4 > 0 && v4 != this.m1151()) {
               return v4;
            }
         }

         v2++;
         
      }

      return -1;
   }

   private void m1041() {
      Screen v2 = mc.field_1755;

      if (v2 instanceof GenericContainerScreen v1) {

         this.f859 = ((GenericContainerScreenHandler)v1.method_17577()).field_7763;
         mc.field_1724.method_7346();
      } else {
         this.f859 = -1;
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
         }
      }
   }

   private void m1076() {
      if (mc.field_1755 instanceof GenericContainerScreen v1) {
         GenericContainerScreenHandler v6 = (GenericContainerScreenHandler)v1.method_17577();
         int v3 = this.m1105(v6);
         if (v3 != -1) {
            this.f850 = 0;
            this.f886 = 0;
            this.m1115(v6.field_7763, v3, 0, SlotActionType.field_7790);
            this.f895 = true;
            this.f897 = 0;
            this.f898++;
            this.f847 = Math.max(0, this.f847 - 1);
            if (this.f847 > 0 && this.f898 < this.sellSlots.getValue().intValue() + 2) {
               this.f846 = 2;
            } else {
               mc.field_1724.method_7346();
               this.f845 = pl.rl.SELL;
               this.f850 = 0;
               this.f886 = 0;
               this.f846 = 6;
            }
         } else if (this.f895) {
            if (this.f847 > 0 && ++this.f850 <= 20) {
               this.f846 = 3;
            } else {
               this.f847 = 0;
               mc.field_1724.method_7346();

               this.f845 = pl.rl.SELL;
               this.f850 = 0;
               this.f886 = 0;
               this.f846 = 6;
            }
         } else {
            if (this.f847 > 0 && ++this.f850 <= 60) {
               this.f846 = 5;
            } else {
               this.f847 = 0;
               mc.field_1724.method_7346();
               this.f845 = pl.rl.SELL;
               this.f846 = 6;
            }
         }
      } else {
         this.f845 = pl.rl.SELL;
         this.f846 = 6;
      }
   }

   private void m1075() {
      if (mc.field_1755 instanceof GenericContainerScreen v1) {
         GenericContainerScreenHandler v6 = (GenericContainerScreenHandler)v1.method_17577();
         int v3 = this.m1104(v6);
         if (v3 == -1) {

            int v7 = this.f850 + 1;
            int result = this.f850 + 1;

            this.f850 = v7;
            if (result > 180) {
               mc.field_1724.method_7346();
               this.m1073();
            } else {
               this.f846 = 5;
            }
         } else {
            this.f850 = 0;
            this.m1115(v6.field_7763, v3, 0, SlotActionType.field_7790);
            this.f845 = pl.rl.STORAGE_TAKE;
            this.f898 = 0;
            this.f846 = 6;
         }
      } else {
         this.f845 = pl.rl.STORAGE_OPEN;
         this.f846 = 6;
      }
   }

   private void m1165(String command) {
      if (command == null || command.isEmpty()) {
         command = this.f876;
      }

      if (command != null && !command.isEmpty()) {
         if (!command.equals(this.f875)) {
            this.f875 = command;
            this.f874 = 0;
            this.f877++;
            this.m1166();
            
         }
      }
   }

   private void m1030() {
      this.reset();
      this.f852 = true;
      this.f862 = this.m1150();
      this.f861 = this.m1149();
      this.m1135();
      this.m1171("money");
      int v1 = this.f862;
      this.m1172(
         "§e[AuraCrafter] Фулл авто: анка "
            + v1
            + ", старт цикла."
      );

      this.m1032();
   }

   private boolean m1161() {

      if (this.f862 > 0 && !this.f873 && this.f845 != pl.rl.AUTO_WAIT_JOIN) {
         int v1 = this.m1150();
         if (v1 == this.f862) {
            return false;
         } else {
            this.m1162();
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean m1084() {
      if (mc.field_1724 != null) {

         if (mc.field_1724.method_6059(StatusEffects.field_5905)) {
            return true;
         }
      }

      return false;
   }

   private void m1068() {
      mc.field_1724.method_31548().method_61496(0);
      ItemStack v1 = mc.field_1724.method_6047();
      if (this.m1168(v1)) {

         if (v1.method_7947() == 1) {
            long v2 = this.m1130();
            this.m1171("ah sell " + v2);
            this.f845 = pl.rl.SELL;
            this.f846 = this.m1132(8, 18);
            return;
         }
      }

      this.f845 = pl.rl.SELL_PREPARE;
      this.f846 = 3;
   }

   private int m1127(int inventorySlot) {
      int result;
      if (inventorySlot < 9) {

         result = inventorySlot + 36;
      } else {
         result = inventorySlot;
      }

      return result;
   }

   private boolean m1089(ItemStack stack, RegistryEntry<Potion> potion) {
      PotionContentsComponent v3 = (PotionContentsComponent)stack.method_58694(DataComponentTypes.field_49651);
      return v3 != null && v3.comp_2378().isPresent() && ((RegistryEntry)v3.comp_2378().get()).equals(potion);
   }

   private void m1143() {
      if (this.f894 > 0) {
         this.f894--;
         
      } else if (this.clanInvest.getValue() && mc.field_1755 == null) {
         long v1 = this.m1149();
         if (v1 > 0L) {
            this.f861 = v1;
         }

         if (this.f861 > 0L) {
            long v3 = this.m1129(this.clanInvestAt.getValue(), 546913626303630401L + 8676458410551145406L);
            long v5 = this.m1129(this.clanInvestAmount.getValue(), 0L);
            if (v5 > 0L && this.f861 >= v3 && this.f861 - v5 >= 0L) {
               String temp1 = "clan invest " + v5;

               this.m1171(temp1);
               this.f893 += v5;
               this.f861 = Math.max(0L, this.f861 - v5);

               this.f894 = 1200;
            }
         }
      }
   }

   private boolean m1124() {
      String v1 = this.sellItem.getValue();

      ItemStack v2 = mc.field_1724.method_31548().method_5438(0);
      if (this.m1168(v2) && v2.method_7947() == 1) {
         return true;
      } else {
         int v3 = mc.field_1724.field_7512.field_7763;
         int v4 = this.m1127(0);
         if (!v2.method_7960()) {
            int v5 = this.m1125();
            if (v5 == -1) {
               return false;
            }

            if (this.m1168(v2) && v2.method_7947() > 1) {
               this.m1115(v3, v4, 0, SlotActionType.field_7790);
               this.m1115(v3, v4, 1, SlotActionType.field_7790);
               this.m1115(v3, v5, 0, SlotActionType.field_7790);
               return true;
            }

            this.m1115(v3, v4, 0, SlotActionType.field_7790);

            this.m1115(v3, v5, 0, SlotActionType.field_7790);
         }

         int v11 = this.m1123(v1);
         if (v11 == -1) {

            return false;
         } else {
            ItemStack v6 = mc.field_1724.method_31548().method_5438(v11);
            if (v6.method_7947() == 1) {
               this.m1126(v11, 0);
               return true;
            } else {
               int v7 = this.m1127(v11);
               this.m1115(v3, v7, 0, SlotActionType.field_7790);
               this.m1115(v3, v4, 1, SlotActionType.field_7790);
               this.m1115(v3, v7, 0, SlotActionType.field_7790);
               return true;
            }
         }
      }
   }

   private int m1128(int inventorySlot) {
      int result;
      if (inventorySlot < 9) {

         result = inventorySlot + 37;
      } else {
         result = inventorySlot + 1;
      }

      return result;
   }

   private void m1135() {
      this.f889 = System.currentTimeMillis();

      this.f890 = this.f861 > 0L ? this.f861 : this.m1149();
      this.f864 = this.m1150();
      this.f865 = 0;
      this.f866 = 0;
      this.f867 = false;
      this.f868 = pl.rl.IDLE;

      this.f869 = null;
      this.f870 = null;
      this.f891 = 0L;
      this.f892 = 0L;
      this.f893 = 0L;
   }

   private int m1091(int inventorySlot) {
      if (inventorySlot < 0) {
         return -1;
      } else if (inventorySlot < 9) {
         return inventorySlot;
      } else {
         int v2 = this.m1092();
         if (v2 == -1) {

            v2 = 8;
         }

         this.m1126(inventorySlot, v2);
         return v2;
      }
   }

   private long m1036() {
      if (this.f861 <= 0L) {
         long v1 = this.m1149();
         if (v1 > 0L) {
            this.f861 = v1;

            this.f860 = 0;
         }
      }

      return this.f861;
   }

   private void stop(String message) {
      this.m1172(message);
      this.reset();

      
   }

   private void m1055() {
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
      }

      if (!this.f867) {

         if (++this.f865 >= 6000) {

            if (this.f864 > 0) {
               int v1 = this.f864;
               this.m1171("an" + v1);
            }

            this.f867 = true;
            this.f866 = 0;
         }
      } else if (++this.f866 >= 200) {
         this.m1056();
      }
   }

   private void m1070(float x, float y, float w, float h, int value, int max, int color) {
      float v8 = Math.max(0.0F, Math.min(w, w * ((float)value / (float)max)));

      vs.ys.rect(x, y, w, h, up.getColor(0, 0.18F), 2.0F);
      vs.ys.rect(x, y, v8, h, color, 2.0F);
      vs.ys.outline(x, y, w, h, 0.5F, up.getColor(255, 0.08F), 2.0F);
   }

   private int m1123(String name) {
      String v2 = name.trim().toLowerCase();

      while (true) {
         int temp1 = 36;

         if (v3 >= temp1) {
            return -1;
         }

         ItemStack v4 = mc.field_1724.method_31548().method_5438(v3);
         if (!v4.method_7960() && this.m1170(v4.method_7964().getString()).contains(v2)) {
            return v3;
         }

         v3++;
      }
   }

   public AuraCrafter() {
      this.sellBind = new SettingVisibility(this, "Бинд продажи");
      this.fullAutoBind = new SettingVisibility(this, "Бинд фулл авто");
      this.buyNetheriteBind = new SettingVisibility(this, "Бинд незера");
      this.buyDiamondsBind = new SettingVisibility(this, "Бинд алмазов");
      this.buyItem = new StringSetting(
         this,
         "Что покупать",
         "Незеритовый слиток"
      );
      this.maxBuyPrice = new StringSetting(this, "Макс цена 1шт", "325000", true);
      this.diamondStackPrice = new StringSetting(this, "Алмазы 64шт", "250000", true);
      this.reserveMoney = new StringSetting(this, "Резерв денег", "3000000", true);
      this.anarchyList = new StringSetting(this, "Анки закупа", "320,322,323");
      this.clanInvest = new BooleanSetting(this, "Clan invest", false);
      this.clanInvestAt = new StringSetting(this, "Invest порог", "10000000", true).setVisible(() -> {

         return this.clanInvest.getValue();
      });
      this.clanInvestAmount = new StringSetting(this, "Invest сумма", "1000000", true)
         .setVisible(() -> this.clanInvest.getValue());
      this.autoInvis = new BooleanSetting(this, "Авто инвиз", false);
      this.autoInvisType = new ModeSetting(this, "Тип инвиза", "3m", "8m", "8+").setVisible(() -> {

         return this.autoInvis.getValue();
      });
      this.sellItem = new StringSetting(this, "Что продавать", "Божья аура");
      this.sellPrice = new StringSetting(this, "Цена продажи", "420000", true);
      this.randomSellPrice = new BooleanSetting(this, "Рандом цены", false);
      this.randomSellPriceRange = new StringSetting(this, "Разброс цены", "10000", true).setVisible(() -> {
         Boolean result = this.randomSellPrice.getValue();

         return result;
      });
      this.autoPausePlayers = new BooleanSetting(this, "Авто пауза игроки", true);
      this.autoPauseRange = new SliderSetting(this, "Радиус игроков", 64.0F, 8.0F, 128.0F, 1.0F)
         .setVisible(() -> {
            int temp2 = v1 + 1;

            return this.autoPausePlayers.getValue();
         });
      this.sellSlots = new SliderSetting(this, "Слотов AH", 5.0F, 1.0F, 50.0F, 1.0F);
      this.infoDrag = new vf(this, "Инфо панель", new Vector2f(8.0F, 145.0F));
      this.f845 = pl.rl.IDLE;
      this.f849 = -1;
      this.f854 = -4956145462259377374L + 4956145462259377373L;
      this.f859 = -1;
      this.f861 = 8538201436222787709L + -8538201436222787710L;
      this.f862 = -1;
      this.f863 = -1;
      this.f864 = -1;
      this.f868 = pl.rl.IDLE;
      this.f872 = pl.ql.NONE;
      this.f883 = "";
      this.f885 = pl.rl.IDLE;
      this.f887 = "";
      this.f890 = 7977086066224156481L + -7977086066224156482L;
      this.f900 = -1;
      this.f902 = pl.rl.IDLE;
      this.f909 = -1;
   }

   private void m1061() {
      this.m1041();
      this.f850 = 0;
      this.f904 = 0;

      this.m1120("ah search инвиз");
   }

   private void m1067() {
      if (!this.m1124()) {
         if (this.f852) {

            this.m1032();
         } else {
            if (this.f896 && !this.f895 && this.f847 <= 0) {
               this.f897++;
               if (this.f897 >= 3) {
                  this.stop(
                     "§a[AuraCrafter] Продажа выключена: хранилище пустое 3 раза."
                  );
                  return;
               }
            }

            this.m1073();
         }
      } else {
         this.f896 = false;
         this.f897 = 0;
         mc.field_1724.method_31548().method_61496(0);
         this.f845 = pl.rl.SELL_SEND;

         this.f846 = this.m1132(2, 7);
      }
   }

   private boolean m1082(CraftingScreenHandler handler) {
      ItemStack v2 = handler.method_7611(0).method_7677();
      if (!v2.method_7960()) {
         int temp2 = v3 + 1;

         if (this.m1170(v2.method_7964().getString()).contains("Божья аура".toLowerCase())) {
            return true;
         }
      }

      return false;
   }

   private void m1024() {
      this.reset();
      this.m1135();

      this.f845 = pl.rl.CRAFT;
      this.m1172("§e[AuraCrafter] Крафт запущен.");
   }

   private void reset() {
      this.f845 = pl.rl.IDLE;
      this.f846 = 0;
      this.f847 = 0;
      this.f848 = 0;
      this.f849 = -1;
      this.f850 = 0;
      this.f851 = 0;
      this.f852 = false;
      this.f853 = null;
      this.f854 = -625189958847415320L + 625189958847415319L;
      this.f855 = null;
      this.f856 = 0;
      this.f857 = 0;
      this.f858 = 0;
      this.f859 = -1;
      this.f860 = 0;
      this.f862 = -1;
      this.f863 = -1;
      this.f864 = -1;
      this.f865 = 0;
      this.f866 = 0;
      this.f867 = false;
      this.f868 = pl.rl.IDLE;
      this.f869 = null;
      this.f870 = null;
      this.f871 = 0;
      this.f872 = pl.ql.NONE;
      this.f873 = false;
      this.f874 = 0;
      this.f875 = null;
      this.f876 = null;
      this.f877 = 0;
      this.f878 = false;
      this.f879 = 0;
      this.f880 = 0L;
      this.f881 = false;
      this.f882 = false;
      this.f883 = "";
      this.f884 = 0;
      this.f885 = pl.rl.IDLE;
      this.f886 = 0;
      this.f887 = "";
      this.f888 = false;

      this.f894 = 0;
      this.f895 = false;
      this.f896 = false;
      this.f897 = 0;
      this.f898 = 0;
      this.f901 = 0;
      this.f902 = pl.rl.IDLE;
      this.f903 = 0;
      this.f904 = 0;
      this.f905 = false;
      this.f906 = 0;
      this.f907 = false;

      this.f908 = 0;
      this.f909 = -1;
      this.f910 = 0;
      this.f911 = 0;
      this.f912 = 0;
      this.f913 = 0;
      this.f914 = null;
      this.f915 = 0;
      this.f916 = 0;
      this.f917 = 0;
      this.f918 = 0;
      this.m1093();
      this.m1117();
      this.m1166();
      
   }

   private void m1052() {
      if (mc.field_1755 instanceof CraftingScreen) {
         ScreenHandler v2 = mc.field_1724.field_7512;
         boolean result = v2 instanceof CraftingScreenHandler;

         if (result) {
            CraftingScreenHandler v1 = (CraftingScreenHandler)v2;
            if (this.m1082(v1)) {
               this.m1115(v1.field_7763, 0, 0, SlotActionType.field_7794);
               this.f846 = this.m1132(1, 3);
               this.f845 = pl.rl.CRAFT;
               return;
            }

            if (!this.m1077(v1)) {
               this.m1080(v1);
               this.f845 = pl.rl.CRAFT;
               this.f846 = this.m1132(2, 4);
               return;
            }

            this.f846 = 1;
            return;
         }
      }

      this.f845 = pl.rl.CRAFT_OPEN_TABLE;
      this.f846 = 6;
   }

   private int m1087(GenericContainerScreenHandler handler, boolean strictPlus) {
      int result = handler.method_17388() * (9);

      int v3 = result;
      long v5 = -3180368244939974015L + -6043003791914801794L;

      for (int v7 = 0; v7 < v3; v7++) {
         Slot v8 = handler.method_7611(v7);
         if (v8.method_7681()) {
            ItemStack v9 = v8.method_7677();
            if (v9.method_7947() == 1 && this.m1088(v9, strictPlus)) {
               long v10 = this.m1106(v9);
               if (v10 > 0L) {

                  if (v10 < v5) {
                     v5 = v10;
                     v4 = v7;
                  }
               }
            }
         }
      }

      return v4;
   }

   private long m1130() {
      long v1 = this.m1129(this.sellPrice.getValue(), 1L);
      if (!this.randomSellPrice.getValue()) {
         long result = Math.max(1L, v1);

         return result;
      } else {
         long v3 = this.m1129(this.randomSellPriceRange.getValue(), 0L);
         if (v3 <= 0L) {
            return Math.max(1L, v1);
         } else {
            long v5 = Math.max(1L, v1 - v3);
            long v7 = Math.max(v5, v1 + v3);
            long v9 = v7 - v5 + 1L;
            double temp1 = Math.floor(Math.random() * (double)v9);

            return v5 + (long)temp1;
         }
      }
   }

   private void m1114() {
      this.f916 = Math.max(this.f916, 8);
      this.m1117();
      int result = v1 * (v1 + 1);

      this.f912 = Math.max(this.f912, 3);
   }

   private void m1163(int anarchy, pl.ql action) {
      this.f863 = anarchy;
      this.f872 = action;
      this.f857 = 0;

      this.f850 = 0;
      StringBuilder v6 = new StringBuilder();

      this.m1171(v6.append("an").append(anarchy).toString());
      this.f845 = pl.rl.AUTO_WAIT_JOIN;
      this.f846 = 200;
   }

   private void m1155() {
      this.f873 = false;
      if (this.f862 <= 0) {
         this.f862 = this.m1150();
      }

      int v1 = this.m1150();
      if (this.f862 > 0 && v1 != this.f862) {
         this.m1163(this.f862, pl.ql.CRAFT);
      } else {
         this.f845 = pl.rl.CRAFT;

         this.f846 = 6;
      }
   }

   private void m1164() {
      if (this.f875 != null) {
         this.f874++;
         mc.field_1690.field_1894.method_23481(this.f874 < 10);
         boolean temp1;
         if (this.f874 >= 10 && this.f874 < 22) {

            temp1 = true;
         } else {
            temp1 = false;
         }

         mc.field_1690.field_1913.method_23481(temp1);
         mc.field_1690.field_1849.method_23481(this.f874 >= 22 && this.f874 < 34);
         mc.field_1690.field_1903.method_23481(this.f874 == 8 || this.f874 == 24);
         if (this.f874 >= 38) {
            this.m1166();
            this.f876 = this.f875;
            mc.field_1724.field_3944.method_45730(this.f875.startsWith("/") ? this.f875.substring(1) : this.f875);
            this.f875 = null;
            this.f882 = false;
            this.f874 = 0;
            this.f877 = 0;
         }
      }
   }

   private void m1033() {
      this.f888 = false;
      this.f873 = true;
      if (this.f852) {

         if (!this.m1035(0L)) {
            this.m1037();
            return;
         }
      }

      this.m1038(this.buyItem.getValue().trim(), this.m1129(this.maxBuyPrice.getValue(), -2843763611972081356L + 2843763611972406356L), Items.field_22020, 0);
   }

   private Item m1081(int slot) {
      Item result;
      switch (slot) {
         case 1:
         case 3:
         case 7:
         case 9:
            result = Items.field_8477;
            
            break;
         case 2:
         case 4:
         case 6:
         case 8:
            result = Items.field_8103;
            break;
         case 5:
            result = Items.field_22020;
            break;
         default:
            result = Items.field_8162;
            
      }

      return result;
   }

   private void m1049() {
      if (this.m1112(Items.field_22020) <= 0) {
         if (this.f852) {
            this.m1156();
         } else {
            this.stop(
               "§a[AuraCrafter] Крафт закончен: незеритовые слитки закончились."
            );
         }
      } else if (this.m1112(Items.field_8477) < 4) {
         if (this.f852) {
            this.m1034();
         } else {
            this.stop(
               "§c[AuraCrafter] Нет алмазов для следующей Божки."
            );
         }
      } else if (this.m1112(Items.field_8103) < this.m1113()) {
         if (this.m1112(Items.field_8550) <= 0) {
            this.stop("§c[AuraCrafter] Нет молока и пустых ведер.");
         } else {
            this.f845 = pl.rl.CRAFT_MILK;
            this.f846 = 6;
         }
      } else {
         this.f845 = mc.field_1755 instanceof CraftingScreen ? pl.rl.CRAFT_FILL : pl.rl.CRAFT_OPEN_TABLE;
         this.f846 = 6;
         
      }
   }

   private 

      String result = this.m1170(line);

      String v2 = result;
      int v3 = v2.indexOf("анархия");
      if (v3 < 0) {
         return -1;
      } else {
         String v4 = v2.substring(v3).replaceAll("[^0-9]", "");
         return (int)this.m1129(v4, -2541360912457687426L + 2541360912457687425L);
      }
   }

   private int m1071(String label) {
      if ("Время".equals(label)) {

         return up.getColor(v5, v6, 255, 1.0F);
      } else if ("Заработал".equals(label)) {
         return up.getColor(159, 238, 188, 1.0F);
      } else if ("В клан".equals(label)) {
         int result = 255;
         int temp1 = 220;
         int temp2 = 132;

         return up.getColor(result, temp1, temp2, 1.0F);
      } else {
         return up.getColor(205);
      }
   }

   private long m1149() {
      if (mc.field_1687 == null) {
         return 7510439837053884823L + -7510439837053884824L;
      } else {
         Scoreboard v1 = mc.field_1687.method_8428();
         ScoreboardObjective v2 = v1.method_1189(ScoreboardDisplaySlot.field_45157);
         if (v2 == null) {

            return 3155205007628719367L + -3155205007628719368L;
         } else {
            long v3 = this.m1153(v2.method_1114().getString());
            if (v3 > 0L) {
               return v3;
            } else {
               for (ScoreboardEntry v6 : v1.method_1184(v2)) {
                  Team result = v1.method_1164(v6.comp_2127());
                  Text temp1 = v6.method_55387();

                  String v7 = Team.method_1142(result, temp1).getString();
                  long v8 = this.m1153(v7);
                  if (v8 > 0L) {
                     return v8;
                  }
               }

               return 71947903555199764L + -71947903555199765L;
            }
         }
      }
   }

   private void m1115(int syncId, int slot, int button, SlotActionType actionType) {
      this.m1114();

      mc.field_1761.method_2906(syncId, slot, button, actionType, mc.field_1724);
   }

   private void m1044() {
      int v1 = this.m1150();

      if (this.f863 > 0 && v1 != this.f863) {
         int v2 = this.f863;
         this.m1171("an" + v2);
         this.f846 = 200;
      } else if (this.f872 == pl.ql.SEARCH) {
         this.f863 = -1;
         this.m1039();
         this.f845 = pl.rl.BUY_OPEN;
         this.f846 = 20;
      } else if (this.f872 == pl.ql.CRAFT) {
         this.f863 = -1;
         this.f845 = pl.rl.CRAFT;
         this.f846 = 6;
      } else {
         this.f845 = pl.rl.IDLE;
      }
   }

   private void m1156() {
      if (this.m1157() <= 0 && this.f847 <= 0) {
         this.m1032();
         
      } else {
         this.f845 = pl.rl.SELL;
         this.f846 = 6;
      }
   }

   private void m1094() {
      pl.rl v1 = this.f902 == null ? pl.rl.IDLE : this.f902;
      int v2 = this.f903;
      this.m1093();
      if (this.f909 >= 0) {
         PlayerInventory result = mc.field_1724.method_31548();

         result.method_61496(this.f909);
      }

      this.f902 = pl.rl.IDLE;
      this.f903 = 0;
      this.f904 = 0;
      this.f905 = false;
      this.f906 = 0;
      this.f907 = false;
      this.f908 = 0;
      this.f909 = -1;
      this.f883 = "";
      this.f884 = 0;
      this.m1095(v1, v2);
   }

   private void m1160() {
      if (mc.field_1755 instanceof GenericContainerScreen v1) {
         ScreenHandler temp1 = v1.method_17577();

         this.m1097((GenericContainerScreenHandler)temp1);
      }

      this.f846 = 20;
   }

   private BlockPos m1107() {
      if (mc.field_1724 != null) {
         if (mc.field_1687 != null) {
            BlockPos v1 = mc.field_1724.method_24515();
            BlockPos v2 = null;
            double v3 = Double.MAX_VALUE;

            for (int v5 = v13; v5 <= 2; v5++) {
               for (int v6 = -4; v6 <= 4; v6++) {
                  for (int v7 = -4; v7 <= 4; v7++) {
                     BlockPos v8 = v1.method_10069(v6, v5, v7);
                     if (mc.field_1687.method_8320(v8).method_27852(Blocks.field_9980)) {
                        double v9 = mc.field_1724.method_5707(Vec3d.method_24953(v8));
                        if (v9 < v3) {
                           v3 = v9;
                           v2 = v8;
                        }
                     }
                  }
               }
            }

            return v2;
         }

         
      }

      return null;
   }

   private int m1141(pl.rl mode) {
      return switch (mode) {
         case CRAFT_MILK -> 0;
         default -> 0;
         case INVIS_READ, PLAYER_PAUSE -> 160;
         case CRAFT, INVIS_OPEN -> 140;
         case SELL_SEND -> 140;
         case BUY_CONFIRM -> 180;
         case STORAGE_OPEN -> 100;
         case CRAFT_OPEN_TABLE, STORAGE_CLICK -> 120;
         case CRAFT_FILL, AUTO_WAIT_JOIN, SELL -> 220;
      };
   }

   private String m1139(long value) {
      String v3 = Long.toString(Math.max(0L, value));
      StringBuilder v4 = new StringBuilder(v3);

      for (int v5 = v4.length() - 3; v5 > 0; v5 -= 3) {

         v4.insert(v5, (char)(46));
      }

      String result = v4.toString();

      return result;
   }

   @EventTarget
   public void onKey(EventPostMotion event) {
      if (mc.field_1724 != null) {

         if (event.getKey() != -1) {
            if (event.getKey() == this.craftBind.get()) {
               this.m1024();
            } else if (event.getKey() == this.sellBind.get()) {
               this.m1029();
            } else if (event.getKey() == this.buyNetheriteBind.get()) {
               this.m1025();
            } else if (event.getKey() == this.buyDiamondsBind.get()) {
               this.m1026();
            } else if (event.getKey() == this.fullAutoBind.get()) {

               if (this.f852) {
                  this.m1031();
               } else {
                  this.m1030();
               }
            }

            return;
         }

         
      }
   }

   @EventTarget
   public void onPacket(EventStep event) {
      if (mc.field_1724 != null && !event.isSend()) {
         if (event.getPacket() instanceof GameMessageS2CPacket v2) {
            String v10 = v2.comp_763().getString();
            String v4 = this.m1170(v10);
            String v5 = this.m1170(this.sellItem.getValue());
            long v6 = this.m1148(v10);
            if ((v4.contains("ваш баланс") || v4.contains("баланс")) && v6 > 0L) {
               this.f861 = v6;
               this.f860 = 0;
            }

            if ((
                  v4.contains("успешно купили")
                     || v4.contains("вы успешно купили")
               )
               && v6 > 0L
               && this.f861 > 0L) {
               this.f892 += v6;
               if (!this.f881) {
                  this.f861 = Math.max(0L, this.f861 - v6);
               }

               this.f881 = false;
               this.f860 = 0;
            }

            if (v4.contains("данная команда недоступна в режиме afk")
               )
             {
               this.f882 = true;
               this.m1165(this.f876);
            } else if ((v4.contains("хаб") || v4.contains("lobby")) && this.f852) {
               this.m1162();
            } else {
               if (this.f881 && this.m1146(v4)) {
                  this.m1145();
                  this.m1171("money");
               }

               if ((v4.contains("не хватает") || v4.contains("не хвататет")) && v4.contains("монет") && this.f852) {
                  this.m1145();
                  this.m1154();
               } else {
                  if ((v4.contains("у вас купили") || v4.contains("купили"))
                     && v4.contains(v5)) {
                     if (v6 > 0L) {
                        this.f891 += v6;
                        if (this.f861 > 0L) {
                           this.f861 += v6;
                        } else {
                           this.f861 = v6;
                        }
                     }

                     if (this.f852) {
                        this.m1144(false);
                     }

                     this.f847 = Math.max(0, this.f847 - 1);
                     this.f851 = 0;
                     if (this.f845 == pl.rl.SELL || this.f845 == pl.rl.SELL_PREPARE || this.f845 == pl.rl.SELL_SEND) {
                        this.f848++;
                     }
                  }

                  if (v4.contains("выставлен") || v4.contains("выставили")) {

                     if (v4.contains(v5)) {
                        this.f847++;
                     }
                  }

                  if (v4.contains("не удалось выставить")
                     && v4.contains(v5)
                     && !this.m1147(v4)) {
                     this.f847 = this.sellSlots.getValue().intValue();
                     this.m1073();
                  }
               }
            }
         }
      }
   }

   private boolean m1119() {
      if (this.f914 == null) {

         return false;
      } else {
         if (this.f913 <= 0 && Math.random() < 0.35) {
            this.f913 = this.m1132(3, 11);
         }

         if (this.f915-- > 0) {
            if (Math.random() < 0.18) {
               mc.field_1690.field_1832.method_23481(true);
               
            }

            return true;
         } else {
            String v1 = this.f914;
            this.f914 = null;
            this.f915 = 0;
            this.m1171(v1);

            this.f846 = Math.max(this.f846, this.m1132(8, 22));
            return true;
         }
      }
   }

   private void m1051() {
      if (mc.field_1755 instanceof CraftingScreen) {
         this.f845 = pl.rl.CRAFT_FILL;
         this.f917 = 0;
         this.f846 = this.m1132(1, 3);
      } else {
         HitResult v3 = mc.field_1765;

         BlockHitResult v7;
         label41: {
            if (v3 instanceof BlockHitResult v2 && mc.field_1687.method_8320(v2.method_17777()).method_27852(Blocks.field_9980)) {
               v7 = v2;
               break label41;
            }

            v7 = null;
         }

         BlockHitResult v1 = v7;
         BlockPos v5 = v1 != null ? v1.method_17777() : this.m1107();
         if (v5 == null) {
            this.f917 = 0;
            if (this.f852) {
               this.f846 = this.m1132(8, 16);
            } else {
               this.stop("§c[AuraCrafter] Наведись на верстак.");
            }
         } else {
            this.m1108(v5);
            if (v1 == null && this.f917++ < this.m1132(1, 2)) {
               this.f846 = 1;
            } else {
               if (v1 == null) {
                  Vec3d v6 = new Vec3d((double)v5.method_10263() + 0.5, (double)v5.method_10264() + 1.0, (double)v5.method_10260() + 0.5);
                  v1 = new BlockHitResult(v6, Direction.field_11036, v5, false);
               }

               mc.field_1761.method_2896(mc.field_1724, Hand.field_5808, v1);
               this.f917 = 0;
               this.f846 = this.m1132(2, 4);
            }
         }
      }
   }

   private int m1113() {
      int result = this.m1112(Items.field_22020);
      int temp1 = this.m1112(Items.field_8477);

      int v1 = Math.min(result, temp1 / 4);
      if (v1 <= 0) {
         return 4;
      } else {
         int v2 = this.m1112(Items.field_8550) + this.m1112(Items.field_8103);
         result = Math.min(v2, v1 * 4);

         return Math.min(result, 8);
      }
   }

   private void m1028() {
      this.reset();
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
      }

      this.m1172("§7[AuraCrafter] Закуп выключен.");
   }

   private void m1144(boolean forceCommand) {
      if (!forceCommand && this.f861 <= 0L) {
         long v2 = this.m1149();
         if (v2 > 0L) {

            this.f861 = v2;
            this.f860 = 0;
         }
      }

      if (!forceCommand) {

         int v7 = ++this.f860;
         int temp1 = 55;

         if (v7 < temp1 + this.m1132(0, 18)) {
            return;
         }
      }

      this.m1171("money");
      this.f860 = 0;
   }

   private void m1038(String name, long maxUnitPrice, Item item, int targetCount) {
      this.f853 = name;
      this.f854 = maxUnitPrice;
      this.f855 = item;
      this.f856 = targetCount;
      this.m1040();

      this.f857 = 0;
      this.f887 = "";
      this.f849 = -1;
      this.f850 = 0;
      this.m1039();

      this.f845 = pl.rl.BUY_OPEN;
      this.f846 = 20;
   }

   private int m1157() {

      for (int v2 = 0; v2 < 36; v2++) {
         ItemStack v7 = mc.field_1724.method_31548().method_5438(v2);

         ItemStack v3 = v7;
         if (this.m1168(v3)) {
            v1 += v3.method_7947();
         }
      }

      return v1;
   }

   @Override
   public void onDisable() {
      this.reset();
      this.f899 = false;
      this.f900 = -1;
      super.onDisable();
   }

   private boolean m1077(CraftingScreenHandler handler) {
      this.m1079(handler);
      if (!this.m1078(handler, 1, Items.field_8477)) {
         return false;
      } else if (!this.m1078(handler, 2, Items.field_8103)) {
         return false;
      } else if (!this.m1078(handler, 3, Items.field_8477)) {
         return false;
      } else if (!this.m1078(handler, 4, Items.field_8103)) {
         return false;
      } else if (!this.m1078(handler, 5, Items.field_22020)) {
         return false;
      } else if (!this.m1078(handler, 6, Items.field_8103)) {
         return false;
      } else if (!this.m1078(handler, 7, Items.field_8477)) {
         return false;
      } else {
         boolean result = this.m1078(handler, 8, Items.field_8103);

         return !result ? false : this.m1078(handler, 9, Items.field_8477);
      }
   }

   private void m1080(CraftingScreenHandler handler) {

      while (true) {

         if (v2 > 9) {
            return;
         }

         if (!handler.method_7611(v2).method_7677().method_7960()) {

            this.m1115(handler.field_7763, v2, 0, SlotActionType.field_7794);
         }

         v2++;
      }
   }

   private boolean m1042() {
      if (this.f859 != -1) {
         Screen v2 = mc.field_1755;
         if (v2 instanceof GenericContainerScreen) {

            GenericContainerScreen v1 = (GenericContainerScreen)v2;
            if (((GenericContainerScreenHandler)v1.method_17577()).field_7763 == this.f859) {
               return true;
            }
         }
      }

      return false;
   }

   private void m1142() {
      this.m1172(
         "§e[AuraCrafter] Слишком долго без прогресса, перезапускаю этап."
      );
      switch (this.f845) {
         case IDLE:
         case CRAFT_MILK:
         default:
            break;
         case STORAGE_TAKE:
            if (this.f863 > 0) {
               int v1 = this.f863;
               this.m1171("an" + v1);
            }

            this.f846 = 200;
            break;
         case INVIS_CONFIRM:
            this.m1144(true);
            this.f846 = 40;
            break;
         case INVIS_READ:
         case AUTO_WAIT_BUDGET:
         case CRAFT:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.f878 = false;
            this.f879 = 0;
            if (this.f852 && this.f855 == Items.field_22020 && !this.m1035(0L)) {
               this.m1037();
               return;
            }

            this.m1039();
            this.f845 = pl.rl.BUY_OPEN;
            this.f846 = 20;
            break;
         case BUY_OPEN:
         case INVIS_DRINK:
         case SELL_SEND:
         case BUY_CONFIRM:

            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.f845 = pl.rl.CRAFT;
            this.f846 = 20;
            break;
         case PLAYER_PAUSE:
         case SELL_PREPARE:
         case INVIS_OPEN:
         case STORAGE_OPEN:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.m1093();
            this.f905 = false;
            this.f906 = 0;
            this.f907 = false;
            this.f908 = 0;
            this.m1061();
            this.f845 = pl.rl.INVIS_OPEN;
            this.f846 = 20;
            break;
         case CRAFT_OPEN_TABLE:
         case STORAGE_CLICK:
         case BUY_READ:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            this.f845 = pl.rl.SELL;
            this.f846 = 10;
            break;
         case CRAFT_FILL:
         case AUTO_WAIT_JOIN:
         case SELL:
            if (mc.field_1755 != null) {
               mc.field_1724.method_7346();
            }

            if (this.f895) {
               this.f847 = 0;
               this.f845 = pl.rl.SELL;
               this.f850 = 0;

               this.f846 = 10;
            } else {
               this.m1073();
            }
      }
   }

   private String m1138(long value) {
      String result = value >= 0L ? "+$" : "-$";
      long temp2 = Math.abs(value);

      String v3 = this.m1139(temp2);
      String v4 = result;
      StringBuilder v8 = new StringBuilder();

      return v8.append((Object)v4).append((Object)v3).toString();
   }

   private void m1145() {
      if (!this.f881) {
         
      } else {

         if (this.f861 >= 0L && this.f880 > 0L) {
            this.f861 = this.f861 + this.f880;
         }

         this.f881 = false;
         this.f880 = 0L;
      }
   }

   private void m1066() {
      int result = this.f847;

      Float temp1 = this.sellSlots.getValue();
      int temp3 = v2 + 1;

      if (result >= temp1.intValue()) {
         if (++this.f851 >= 34) {
            this.m1073();
         } else {
            this.f846 = 2;
         }
      } else {
         this.f851 = 0;
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
            this.f846 = 6;
         } else {
            this.f845 = pl.rl.SELL_PREPARE;
         }
      }
   }

   private long m1106(ItemStack stack) {
      LoreComponent v2 = (LoreComponent)stack.method_57353().method_58694(DataComponentTypes.field_49632);
      if (v2 == null) {
         return 0L;
      } else {
         for (Text v4 : v2.comp_2400()) {
            String v5 = v4.getString();
            if (v5.contains("$") || v5.toLowerCase().contains("цена")) {
               String result = v5.replaceAll("[^0-9]", "");

               String v6 = result;
               if (!v6.isEmpty()) {
                  return this.m1129(v6, 0L);
               }
            }
         }

         return 0L;
      }
   }

   private String m1137() {
      long v16;
      int result = (v16 = this.f889 - 0L) == 0L ? 0 : (v16 < 0L ? -1 : 1);

      long v1 = result <= 0 ? System.currentTimeMillis() : this.f889;
      long v3 = Math.max(0L, (System.currentTimeMillis() - v1) / (-5816516121270041486L + 5816516121270042486L));
      long v5 = v3 / (270569355516161837L + -270569355516158237L);
      long v7 = v3 % (4763455652641067021L + -4763455652641063421L) / (-1940617081501392102L + 1940617081501392162L);
      long v9 = v3 % (-630521103767807505L + 630521103767807565L);
      String v14;
      if (v5 > 0L) {
         String v13 = "%d:%02d:%02d";
         Object[] v15 = new Object[]{v5, null, null};

         v15[1] = v7;
         v15[2] = v9;
         v14 = String.format(v13, v15);
      } else {
         v14 = String.format("%02d:%02d", v7, v9);
      }

      return v14;
   }

   private CowEntity m1111() {
      CowEntity v1 = null;
      double v2 = 36.0;
      Iterator v4 = mc.field_1687.method_18112().iterator();
      int result = v9 * (v9 + 1) % 2;

      if (result != 0) {
         }

      while (v4.hasNext()) {
         Entity v5 = (Entity)v4.next();
         if (v5 instanceof CowEntity) {
            CowEntity v6 = (CowEntity)v5;
            double v7 = v6.method_5858(mc.field_1724);
            if (v7 < v2) {
               v2 = v7;
               v1 = v6;
            }
         }
      }

      return v1;
   }

   private Rotation m1109(float targetYaw, float targetPitch, float minFactor, float maxFactor) {
      float v5 = this.m1133(minFactor, maxFactor);
      float v6 = mc.field_1724.method_36454() + MathHelper.method_15393(targetYaw - mc.field_1724.method_36454()) * v5;
      float result = mc.field_1724.method_36455();

      float v7 = result + (targetPitch - mc.field_1724.method_36455()) * v5;
      return new Rotation(MathHelper.method_15393(v6), MathHelper.method_15363(v7, -90.0F, 90.0F));
   }

   private boolean m1054() {
      if (this.f845 == pl.rl.PLAYER_PAUSE) {
         this.m1055();
         return true;
      } else {
         boolean result = this.autoPausePlayers.getValue();

         if (result) {

            if (this.f845 != pl.rl.IDLE && this.f864 > 0) {
               if (this.m1150() != this.f864) {
                  return false;
               }

               PlayerEntity v1 = this.m1057();
               if (v1 == null) {
                  return false;
               }

               String v2 = v1.method_5477().getString();
               if (this.f870 != null && this.f870.equalsIgnoreCase(v2)) {
                  if (mc.field_1755 != null) {
                     mc.field_1724.method_7346();
                  }

                  this.m1093();
                  this.m1117();
                  this.m1166();
                  this.m1171("hub");
                  this.m1172(
                     "Â§c[AuraCrafter] Ð˜Ð³Ñ€Ð¾Ðº "
                        + v2
                        + " Ñ\u0081Ð½Ð¾Ð²Ð° Ñ€Ñ\u008fÐ´Ð¾Ð¼, Ð²Ñ‹Ñ…Ð¾Ð¶Ñƒ Ð¸ Ð²Ñ‹ÐºÐ»ÑŽÑ‡Ð°ÑŽÑ\u0081ÑŒ."
                  );
                  this.setEnabled(false);
                  return true;
               }

               if (mc.field_1755 != null) {
                  mc.field_1724.method_7346();
               }

               this.m1093();
               this.m1117();
               this.m1166();
               this.m1145();
               this.f868 = this.f845;
               this.f869 = v2;
               this.f870 = v2;
               this.f865 = 0;
               this.f866 = 0;
               this.f867 = false;
               this.m1171("hub");
               this.f845 = pl.rl.PLAYER_PAUSE;
               this.f846 = 0;

               this.m1172(
                  "Â§e[AuraCrafter] Ð˜Ð³Ñ€Ð¾Ðº "
                     + v2
                     + " Ñ€Ñ\u008fÐ´Ð¾Ð¼, Ð¿Ð°ÑƒÐ·Ð° 5 Ð¼Ð¸Ð½ÑƒÑ‚."
               );
               return true;
            }
         }

         return false;
      }
   }

   private void m1108(BlockPos pos) {
      Vec3d result = Vec3d.method_24953(pos);

      Vec3d v2 = result.method_1031(this.m1134(-0.12, 0.12), this.m1134(-0.08, 0.1), this.m1134(-0.12, 0.12));
      Vec3d v3 = mc.field_1724.method_33571();
      double v4 = v2.field_1352 - v3.field_1352;
      double v6 = v2.field_1351 - v3.field_1351;
      double v8 = v2.field_1350 - v3.field_1350;
      double v10 = Math.sqrt(v4 * v4 + v8 * v8);
      float v12 = (float)MathHelper.method_15338(Math.toDegrees(Math.atan2(v8, v4)) - 90.0);
      float v13 = (float)MathHelper.method_15350(-Math.toDegrees(Math.atan2(v6, v10)), -90.0, 90.0);
      Rotation v16 = this.m1109(v12, v13, 0.62F, 0.86F);
      float temp1 = this.m1133(14.0F, 26.0F);
      float v17 = this.m1133(12.0F, 22.0F);

      xd.update(v16, temp1, v17, this.m1132(1, 2), 2);
   }

   private void m1140() {
      if (this.f885 != this.f845) {
         this.f885 = this.f845;
         this.f886 = 0;
         this.f883 = "";
      }

      String result = String.valueOf(this.f845);
      String temp1 = mc.field_1755 == null ? "none" : mc.field_1755.getClass().getSimpleName();
      int temp2 = mc.field_1724 == null ? -1 : mc.field_1724.field_7512.field_7763;
      int temp3 = this.f847;
      int temp4 = this.m1112(Items.field_8477);
      int temp5 = this.m1112(Items.field_22020);
      int var10006 = this.m1112(Items.field_8103);
      String v3 = this.f875;
      int v4 = this.m1157();
      int v5 = var10006;
      int v6 = temp5;
      int v7 = temp4;
      int v8 = temp3;
      int v9 = temp2;
      String v10 = temp1;
      String v11 = result;
      StringBuilder v13 = new StringBuilder()
         .append((Object)v11)
         .append("|s")
         .append((Object)v10)
         .append("|sid")
         .append(v9)
         .append("|l")
         .append(v8)
         .append("|d")
         .append(v7)
         .append("|n")
         .append(v6);

      String v1 = v13.append("|m").append(v5).append("|a").append(v4).append("|cmd").append((Object)v3).toString();
      if (!v1.equals(this.f883)) {
         this.f883 = v1;
         this.f884 = 0;
         this.f886 = 0;
      } else {
         this.f886++;
         int v2 = this.m1141(this.f845);
         if (v2 > 0 && this.f886 > v2) {
            this.f886 = 0;
            this.m1142();
         } else if (++this.f884 >= 1200) {
            this.f884 = 0;
            this.m1142();
         }
      }
   }

   private void m1117() {
      if (mc.field_1690 != null) {
         mc.field_1690.field_1894.method_23481(false);
         mc.field_1690.field_1881.method_23481(false);
         mc.field_1690.field_1913.method_23481(false);

         mc.field_1690.field_1849.method_23481(false);
         mc.field_1690.field_1903.method_23481(false);
         mc.field_1690.field_1832.method_23481(false);
      }
   }

   private float m1133(float min, float max) {
      float temp1 = (float)Math.random() * (max - min);
      int temp3 = v3 + 1;

      int temp2 = v3 * temp3;

      return min + temp1;
   }

   private void m1060() {
      this.f902 = this.f845;
      this.f903 = this.f846;
      this.m1145();
      this.f904 = 0;
      this.f905 = false;
      this.f906 = 0;
      this.f907 = false;
      this.f908 = 0;
      this.f909 = -1;
      this.f883 = "";

      this.f884 = 0;
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
      }

      this.f878 = false;
      this.f879 = 0;
   }

   private void m1050() {
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
         this.f846 = 1;
      } else if (this.m1112(Items.field_8103) < this.m1113() && this.m1112(Items.field_8550) > 0) {
         int v1 = this.m1122(Items.field_8550);
         if (v1 == -1) {
            this.f845 = pl.rl.CRAFT;
            this.f846 = 6;
         } else {
            CowEntity v2 = this.m1111();
            if (v2 == null) {
               this.f918 = 0;
               if (this.f852) {
                  this.f846 = 40;
               } else {
                  this.stop("§c[AuraCrafter] Коровы рядом нет.");
               }
            } else {
               this.m1110(v2);
               if (this.f918++ < 1) {
                  this.f846 = 1;
               } else {

                  float v6 = MathHelper.method_15393(mc.field_1724.method_36454());
                  float v8 = mc.field_1724.method_36455();

                  if (!np.rayTraceEntity(v6, v8, 4.5, v2)) {
                     this.f846 = 1;
                     
                  } else {
                     this.m1126(v1, 0);
                     mc.field_1724.method_31548().method_61496(0);
                     mc.field_1761.method_2905(mc.field_1724, v2, Hand.field_5808);
                     this.f918 = 0;
                     this.f846 = 1;
                  }
               }
            }
         }
      } else {
         this.f845 = pl.rl.CRAFT;
         this.f846 = 6;
      }
   }

   private void m1062() {
      if (mc.field_1755 instanceof GenericContainerScreen) {
         if (this.m1042()) {
            this.m1041();
            this.f846 = 2;
         } else {
            this.f859 = -1;
            this.f850 = 0;
            this.f904 = 0;

            this.f845 = pl.rl.INVIS_READ;
            this.f846 = 6;
         }
      } else if (++this.f850 > 17) {
         this.m1061();
         this.f846 = 20;
      } else {
         this.f846 = 6;
      }
   }

   private void m1158() {
      int v1 = this.m1159();
      if (v1 <= 0) {
         this.f857 = 0;
         this.m1160();
      } else {
         if (mc.field_1755 != null) {
            mc.field_1724.method_7346();
         }

         this.f873 = true;

         int temp3 = v3 + 1;

         this.m1172(
            "§e[AuraCrafter] Нету подходящих лотов, меняю анку на "
               + v1
               + "."
         );
         this.m1163(v1, pl.ql.SEARCH);
      }
   }

   private boolean m1035(long nextPrice) {
      long v3 = this.m1036();
      if (v3 <= 0L) {
         return true;
      } else {
         long v5 = this.m1129(this.reserveMoney.getValue(), 6310467352611803149L + -6310467352608803149L);
         long v7 = nextPrice > 0L ? nextPrice : Math.max(1L, this.m1129(this.maxBuyPrice.getValue(), 4301182311599941946L + -4301182311599616946L));
         boolean v10;
         if (v3 - v7 >= v5) {

            v10 = true;
         } else {
            v10 = false;
         }

         return v10;
      }
   }

   private void m1037() {
      if (mc.field_1755 != null) {
         mc.field_1724.method_7346();
      }

      this.m1145();
      this.f853 = null;
      this.f855 = null;
      this.f856 = 0;
      this.f854 = 1804620685572225185L + -1804620685572225186L;
      this.f857 = 0;
      this.f858 = 0;
      this.f873 = false;
      if (this.m1157() <= 0 && this.f847 <= 0) {
         int result = this.m1112(Items.field_22020);

         if (result > 0 && this.m1112(Items.field_8477) >= 4) {
            this.m1155();
         } else {
            this.f845 = pl.rl.AUTO_WAIT_BUDGET;
            this.f846 = 40;
         }
      } else {
         this.f845 = pl.rl.SELL;
         this.f846 = 6;
      }
   }

   private boolean m1078(CraftingScreenHandler handler, int craftSlot, Item item) {
      ItemStack v4 = handler.method_7611(craftSlot).method_7677();
      if (v4.method_7909() == item) {
         return true;
      } else {
         boolean result = v4.method_7960();

         if (!result) {
            return false;
         } else {
            int v5 = this.m1122(item);
            if (v5 == -1) {

               return false;
            } else {
               int v6 = this.m1128(v5);
               this.m1115(handler.field_7763, v6, 0, SlotActionType.field_7790);
               this.m1115(handler.field_7763, craftSlot, 1, SlotActionType.field_7790);
               this.m1115(handler.field_7763, v6, 0, SlotActionType.field_7790);
               return true;
            }
         }
      }
   }

   private boolean m1043() {
      if (this.f845 == pl.rl.INVIS_OPEN) {
         if (mc.field_1755 instanceof GenericContainerScreen) {
            if (this.m1042()) {
               this.m1041();
               this.f846 = 2;
               return true;
            } else {
               this.f859 = -1;
               this.f904 = 0;
               return false;
            }
         } else {
            int temp2 = this.f904 + 1;
            int result = this.f904 + 1;

            this.f904 = temp2;
            if (result < 100) {
               return false;
            } else {
               this.m1061();
               this.f846 = 20;
               return true;
            }
         }
      } else if (this.f845 != pl.rl.BUY_OPEN) {
         return false;
      } else if (mc.field_1755 instanceof GenericContainerScreen) {
         if (this.m1042()) {
            this.m1041();
            this.f846 = 2;
            return true;
         } else {
            this.f859 = -1;
            this.f858 = 0;
            return false;
         }
      } else if (++this.f858 < 100) {
         return false;
      } else if (this.f852 && this.f855 == Items.field_22020 && !this.m1035(0L)) {
         this.m1037();
         return true;
      } else {
         this.m1039();
         this.f846 = 20;
         return true;
      }
   }

   private void m1032() {
      if (this.f852) {
         this.f873 = false;
         this.f888 = false;
         if (this.m1112(Items.field_22020) > 0) {
            if (this.m1112(Items.field_8477) < 4) {
               this.m1034();
            } else {
               this.m1155();
            }
         } else if (this.m1112(Items.field_8477) < 256) {
            long v1 = Math.max(
               1L, this.m1129(this.diamondStackPrice.getValue(), 2874756316325070970L + -2874756316324820970L) / (-4365012693550532245L + 4365012693550532309L)
            );

            this.m1038("Алмаз", v1, Items.field_8477, 256);
         } else {
            this.m1033();
            
         }
      }
   }

   private void m1172(String text) {
      zp.addChatMessage(text);

      
   }

   private void m1026() {
      if (!this.f852 && this.m1027(Items.field_8477)) {
         this.m1028();
      } else {
         this.reset();
         this.m1135();
         long v1 = Math.max(
            1L, this.m1129(this.diamondStackPrice.getValue(), -918119561708270394L + 918119561708520394L) / (7548483147310648947L + -7548483147310648883L)
         );
         this.m1038("Алмаз", v1, Items.field_8477, 256);

         String v3 = this.diamondStackPrice.getValue();
         this.m1172("§e[AuraCrafter] Закуп алмазов до $" + v3 + " за 64шт.");
      }
   }

   private void m1118() {
      if (this.f845 != pl.rl.IDLE && this.f845 != pl.rl.PLAYER_PAUSE && mc.field_1724 != null) {
         if (this.f916 <= 0 || this.f845 == pl.rl.CRAFT_MILK || this.f845 == pl.rl.STORAGE_OPEN) {
            if (this.f913 > 0) {
               this.f913--;
               mc.field_1690.field_1832.method_23481(true);
            }

            if (this.f912 > 0) {
               this.f912--;
            } else if (this.m1121() || mc.field_1755 instanceof GenericContainerScreen) {
               this.f912 = this.m1132(5, mc.field_1755 instanceof GenericContainerScreen ? 12 : 26);
               float v1 = mc.field_1724.method_36454() + this.m1133(-14.0F, 14.0F);
               float result = mc.field_1724.method_36455() + this.m1133(-4.5F, 4.5F);

               float v2 = MathHelper.method_15363(result, -78.0F, 78.0F);
               xd.update(this.m1109(v1, v2, 0.24F, 0.44F), this.m1133(4.0F, 12.0F), this.m1133(4.0F, 11.0F), this.m1132(3, 7), 0);
            }
         }
      }
   }

   private void m1095(pl.rl resume, int savedWait) {
      switch (resume) {
         case INVIS_READ:
         case AUTO_WAIT_BUDGET:
         case CRAFT:
            if (this.f853 == null && this.f855 == null) {
               this.f845 = pl.rl.IDLE;
               this.f846 = 0;
            } else {
               if (this.f852 && this.f855 == Items.field_22020 && !this.m1035(0L)) {
                  this.m1037();
                  return;
               }

               this.m1039();
               this.f845 = pl.rl.BUY_OPEN;
               this.f846 = 20;
            }
            break;
         case BUY_OPEN:
         default:
            this.f845 = resume;
            this.f846 = Math.max(6, savedWait);
            break;
         case INVIS_DRINK:
         case SELL_SEND:
         case BUY_CONFIRM:
            this.f845 = pl.rl.CRAFT;
            this.f846 = 6;
            break;
         case PLAYER_PAUSE:
         case SELL_PREPARE:
         case INVIS_OPEN:
         case STORAGE_OPEN:
            this.f845 = pl.rl.IDLE;
            this.f846 = 0;
            break;
         case CRAFT_OPEN_TABLE:
         case STORAGE_CLICK:
            this.f845 = pl.rl.SELL;

            this.f846 = 6;
            break;
         case CRAFT_FILL:
         case AUTO_WAIT_JOIN:
         case SELL:
            this.m1073();
      }
   }

   private void m1045() {
      if (!this.f852) {
         this.f845 = pl.rl.IDLE;
         this.f846 = 0;
         
      } else {
         this.f873 = false;
         this.m1144(false);
         if (this.m1157() > 0 || this.f847 > 0) {
            this.f845 = pl.rl.SELL;

            this.f846 = 6;
         } else if (this.m1112(Items.field_22020) > 0) {
            if (this.m1112(Items.field_8477) < 4) {
               this.m1034();
            } else {
               this.m1155();
            }
         } else if (this.m1112(Items.field_8477) < 256) {
            long v1 = Math.max(
               1L, this.m1129(this.diamondStackPrice.getValue(), -4998274092304335174L + 4998274092304585174L) / (8999353772548363058L + -8999353772548362994L)
            );
            this.m1038("Алмаз", v1, Items.field_8477, 256);
         } else {

            if (this.m1035(0L)) {
               this.m1033();
            } else {
               this.f846 = 40;
            }
         }
      }
   }

   @EventTarget
   public void onDisplay(EventReceivePacket event) {
      if (mc.field_1724 == null) {

         
      } else {
         this.m1069(event);
      }
   }

   private void m1031() {
      this.reset();
      this.m1172("§7[AuraCrafter] Фулл авто выключен.");
      
   }

   @Environment(EnvType.CLIENT)
   private static enum ql {
      NONE,
      CRAFT,
      SEARCH;
      // $VF: synthetic field
      private static final pl.ql[] $VALUES = m1022();
   }

   @Environment(EnvType.CLIENT)
   private static enum rl {
      IDLE,
      CRAFT_MILK,
      STORAGE_TAKE,
      INVIS_CONFIRM,
      INVIS_READ,
      AUTO_WAIT_BUDGET,
      CRAFT,
      BUY_OPEN,
      INVIS_DRINK,
      SELL_SEND,
      BUY_CONFIRM,
      PLAYER_PAUSE,
      SELL_PREPARE,
      INVIS_OPEN,
      STORAGE_OPEN,
      CRAFT_OPEN_TABLE,
      STORAGE_CLICK,
      CRAFT_FILL,
      AUTO_WAIT_JOIN,
      SELL,
      BUY_READ;
      // $VF: synthetic field
      private static final pl.rl[] $VALUES = m1023();
   }
}
