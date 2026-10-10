package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector2f;
import ru.white.module.impl.player.ClickHelper;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.utils.misc.Vf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.module.impl.display.interfaceimpl.Information;
import ru.white.module.impl.display.interfaceimpl.Potions;
import ru.white.module.impl.display.interfaceimpl.WaterMark;
import ru.white.module.impl.display.interfaceimpl.ih;
import ru.white.module.impl.display.interfaceimpl.jh;
import ru.white.module.impl.display.interfaceimpl.lh;
import ru.white.module.impl.display.interfaceimpl.mh;
import ru.white.module.impl.display.interfaceimpl.oh;
import ru.white.module.impl.display.interfaceimpl.qh;
import ru.white.module.impl.display.interfaceimpl.rh;
import ru.white.event.impl.EventJump;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Inter Face",
   desc = "Настройка интерфейса (HUD) клиента",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class InterFace extends Module {
   private final ih f418;
   public ModeSetting targetHudMode;
   public vf targetHudDrag;
   public ModeSetting typeNotify;
   public vf arrayList;
   private final jh f417;
   private final mh f416;
   public ModeSetting hudStyle;
   public BooleanSetting notifyEffects;
   public BooleanSetting notifyArmor;
   public vf itemHud;
   public vf keyBind;
   public SliderSetting sizeHud;
   public SliderSetting alphaHUD;
   public vf music;
   private final Information f413;
   public BooleanSetting notifyModules;
   private final lh f414;
   public MultiSetting element;
   public vf notifications;
   public SliderSetting volume;
   public BooleanSetting islandLyrics;
   private final rh f421;
   public vf useTracker;
   private final qh f420;
   public vf islandDrag;
   private final oh f419;
   public BooleanSetting islandAlways;
   public vf potion;
   private final WaterMark f412;
   public vf information;
   public vf waterMark;
   private final Potions f415;

   @EventTarget
   public void onMousePress(EventJump event) {
      if (this.isEnabled()) {
         if (this.element.getValue("Music Player")) {
            this.f416.onMouseClick(event);
         }

         if (this.element.getValue("Dynamic Island")) {
            this.f417.onMouseClick(event, this, this.islandDrag);
         }
      }
   }

   @Override
   protected void onDisable() {
      this.f416.shutdown();
      this.f417.shutdown();
   }

   @EventTarget
   public void onDisplayEvent(EventReceivePacket eventDisplay) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.isEnabled()) {
         if (this.element.getValue("Potions")) {
            this.f415.onRender(this.potion, this, eventDisplay);
         }

         if (this.element.getValue("Information")) {
            this.f413.onRender(this.information, this);
         }

         if (this.element.getValue("Key Binds")) {
            this.f414.onRender(this.keyBind, this);
         }

         if (this.element.getValue("Water mark")) {
            this.f412.onRender(this.waterMark, this);
         }

         if (this.element.getValue("Music Player")) {
            this.f416.onRender(this.music, this);
         }

         if (this.element.getValue("Dynamic Island")) {
            this.f417.onRender(this.islandDrag, this);
         }

         if (this.element.getValue("ArrayList")) {
            this.f418.onRender(this.arrayList, this);
         }

         if (this.element.getValue("Notifications")) {
            this.f419.onRender(this.notifications, this, eventDisplay);
         }

         if (this.element.getValue("Target Hud")) {
            this.f420.onRender(this.targetHudDrag, this, eventDisplay);
         }

         if (this.element.getValue("Use Tracker")) {

            this.f421.onRender(this.useTracker, this);
         }

         if (this.element.getValue("Item hud")) {
            Vector2f v2 = ei.getInstance().getHudSize();

            this.itemHud.size.set(v2.x, v2.y);
            ei.getInstance().renderHud(eventDisplay.getDrawContext(), this.itemHud.position.x, this.itemHud.position.y);
         }
      }
   }

   public boolean isTargetHudTest() {
      return this.targetHudMode.is("Второй");
   }

   public InterFace() {
      BooleanSetting[] temp5 = new BooleanSetting[11];
      temp5[0] = new BooleanSetting("Water mark", true);
      temp5[1] = new BooleanSetting("Information", true);
      temp5[2] = new BooleanSetting("Key Binds", true);
      temp5[3] = new BooleanSetting("Potions", true);
      temp5[4] = new BooleanSetting("Music Player", false);
      temp5[5] = new BooleanSetting("Dynamic Island", true);
      temp5[6] = new BooleanSetting("ArrayList", false);
      temp5[7] = new BooleanSetting("Notifications", true);
      temp5[8] = new BooleanSetting("Target Hud", true);
      temp5[9] = new BooleanSetting("Item hud", false);
      temp5[10] = new BooleanSetting("Use Tracker", false);
      this.element = new MultiSetting(this, "Элементы", temp5);
      this.volume = new SliderSetting(this, "Громкость уведомления", 0.5F, 0.1F, 1.0F, 0.1F);
      this.typeNotify = new ModeSetting(this, "Тип уведомления", "Первый", "Второй", "Третий");
      this.hudStyle = new ModeSetting(this, "Стиль худа", "Hud 1", "Hud 2");
      this.targetHudMode = new ModeSetting(this, "Вариант TargetHud", "Первый", "Второй")
         .setVisible(() -> this.element.getValue("Target Hud"));
      this.sizeHud = new SliderSetting(this, "Размер интерфейса", 1.0F, 0.5F, 1.5F, 0.05F);
      this.alphaHUD = new SliderSetting(this, "Прозрачность худа", 0.6F, 0.0F, 0.9F, 0.1F);
      this.notifyEffects = new BooleanSetting(this, "эффектах", true);
      this.notifyModules = new BooleanSetting(this, "модулях", true);
      this.notifyArmor = new BooleanSetting(this, "броне", true);
      this.islandLyrics = new BooleanSetting(this, "Текст песни", true);
      this.islandAlways = new BooleanSetting(this, "Не скрывать остров", true);
      this.waterMark = new vf(this, "WaterMark", new Vector2f(5.0F, 5.0F));
      this.information = new vf(this, "Information", new Vector2f(5.0F, 30.0F));
      this.keyBind = new vf(this, "Key Binds", new Vector2f(750.0F, 80.0F));
      this.potion = new vf(this, "Potions", new Vector2f(5.0F, 60.0F));
      this.music = new vf(this, "Music Player", new Vector2f(10.0F, 400.0F));
      this.islandDrag = new vf(this, "Dynamic Island", new Vector2f(350.0F, 6.0F));
      this.arrayList = new vf(this, "ArrayList", new Vector2f(90.0F, 40.0F));
      this.notifications = new vf(this, "Notifications", new Vector2f(0.0F, 200.0F));
      this.targetHudDrag = new vf(this, "Target Hud", new Vector2f(550.0F, 230.0F));
      this.itemHud = new vf(this, "Item hud", new Vector2f(200.0F, 90.0F));
      this.useTracker = new vf(this, "Use Tracker", new Vector2f(200.0F, 120.0F));
      this.f412 = new WaterMark();
      this.f413 = new Information();
      this.f414 = new lh();
      this.f415 = new Potions();
      this.f416 = new mh();
      this.f417 = new jh();
      this.f418 = new ih();
      this.f419 = new oh();
      this.f420 = new qh();
      this.f421 = new rh();
      this.notifications.lockX = true;
      this.islandDrag.lockX = true;
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null && this.isEnabled()) {
         MultiSetting result = this.element;
         String temp1 = "Music Player";

         if (result.getValue(temp1)) {
            this.f416.onTick();
         }

         if (this.element.getValue("Dynamic Island")) {
            this.f417.onTick();
         }

         result = this.element;
         temp1 = "Notifications";

         if (result.getValue(temp1)) {
            this.f419.onTick(this.notifyModules, this.notifyArmor, this.notifyEffects);
         }
      }
   }

   public boolean isHud2() {
      int temp2 = v1 + 1;

      return this.hudStyle.is("Hud 2");
   }

   public static hh getInstance() {
      return uq.get(hh.class);
   }

   
}
