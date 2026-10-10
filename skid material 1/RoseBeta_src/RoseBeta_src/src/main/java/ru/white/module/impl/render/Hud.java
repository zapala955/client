package ru.white.module.impl.render;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.joml.Vector2f;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Vf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.theme.wo;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.satoshi.ep;
import ru.white.utils.render.zr;

@ModuleAnnotation(
   name = "Hud",
   desc = "Настройка интерфейса (HUD) клиента",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class Hud extends Module {
   private zr f410;
   private long f408;
   private double f405;
   public BooleanSetting bpstps;
   public Animation keybindsMaxKeyWidthAnim;
   public vf clickbinds_drag;
   public vf inventory_drag;
   private final Map<String, eh.fh> f404;
   public MinecraftClient EventItemUse;
   public Animation keybindsWidthAnim;
   public BooleanSetting arrayListHude;
   private boolean f411;
   private float f409;
   public BooleanSetting keybindslist;
   public BooleanSetting porionList;
   public SliderSetting volume;
   public Animation mainAnim;
   ru.white.utils.animation.satoshi.Animation animation;
   public vf armor_drag;
   public BooleanSetting armorHud;
   public BooleanSetting waterMark = new BooleanSetting("Лого клиента", true);
   private double f407;
   private double f406;
   public ModeSetting typeNotify;
   public vf keybinds_drag;
   public MultiSetting elempt;
   public vf arraylist_drag;
   public BooleanSetting inventoryHud;
   public vf cords_drag;
   public vf bps_drag;
   public vf potions_drag;
   private float f403;
   public vf tps_drag;
   public Animation withpan;
   public vf watermark_drag;
   public BooleanSetting cortds = new BooleanSetting("Координаты", true);

   public Hud() {
      this.bpstps = new BooleanSetting("BPS и TPS", true);
      this.porionList = new BooleanSetting("Активные эффекты", true);
      this.keybindslist = new BooleanSetting("Активные клавиши", true);
      this.arrayListHude = new BooleanSetting("Аррай лист", false);
      this.armorHud = new BooleanSetting("Armor", true);
      this.inventoryHud = new BooleanSetting("Inventory", true);
      BooleanSetting[] temp5 = new BooleanSetting[8];
      temp5[0] = this.waterMark;
      temp5[1] = this.keybindslist;
      temp5[2] = this.porionList;
      temp5[3] = this.bpstps;
      temp5[4] = this.cortds;
      temp5[5] = this.arrayListHude;
      temp5[6] = this.armorHud;
      temp5[7] = this.inventoryHud;
      this.elempt = new MultiSetting(this, "Отображать", temp5);
      this.volume = new SliderSetting(this, "Громкость уведомления", 0.5F, 0.1F, 1.0F, 0.1F);
      this.typeNotify = new ModeSetting(this, "Тип уведомления", "Первый", "Второй", "Третий");
      this.withpan = new Animation();
      this.watermark_drag = new vf(this, "watermark_draggings");
      this.keybinds_drag = new vf(this, "Keybinds_dragings");
      this.potions_drag = new vf(this, "Potions_dragings");
      this.clickbinds_drag = new vf(this, "ClickBinds_dragings", new Vector2f(8.0F, 100.0F));
      this.arraylist_drag = new vf(this, "ArrayList_dragings", new Vector2f(10000.0F, 4.0F));
      this.armor_drag = new vf(this, "Armor_dragings", new Vector2f(160.0F, 90.0F));
      this.inventory_drag = new vf(this, "Inventory_dragings", new Vector2f(160.0F, 170.0F));
      this.cords_drag = new vf(this, "Cords_dragings", new Vector2f(-999.0F, -999.0F));
      this.bps_drag = new vf(this, "Bps_dragings", new Vector2f(-999.0F, -999.0F));
      this.tps_drag = new vf(this, "Tps_dragings", new Vector2f(-999.0F, -999.0F));
      this.f403 = -1.0F;
      this.mainAnim = new Animation();
      this.keybindsWidthAnim = new Animation();
      this.keybindsMaxKeyWidthAnim = new Animation();
      this.f404 = new LinkedHashMap<>();
      this.animation = new ep(200, 1.0);
      this.mc = MinecraftClient.method_1551();
      this.f411 = false;
   }

   public static int getHudColor() {
      int result = wo.getHudColor();

      return result;
   }

   public static int getVisualsColor() {
      int result = wo.getVisualColor();
      int temp3 = v1 + 1;

      return result;
   }

   public static float getAlpha() {
      float result;
      if (wo.getOpacity() < 0.98F) {
         result = wo.getOpacity();
      } else {
         result = 0.98F;

         
      }

      return result;
   }

   public static boolean getShadow() {

      return false;
   }

   @Environment(EnvType.CLIENT)
   private static class fh {
      boolean active;
      String name;
      boolean negative;
      int durationTicks;
      StatusEffectInstance effectInstance;
      String duration;
      int lvl;
      Animation animation;

      public fh(String name, String duration, int lvl, boolean negative, int durationTicks, StatusEffectInstance effectInstance) {
         this.name = name;
         this.duration = duration;
         this.lvl = lvl;
         this.negative = negative;
         this.durationTicks = durationTicks;
         this.animation = new Animation();
         this.active = true;
         this.effectInstance = effectInstance;
      }

      
   }
}
