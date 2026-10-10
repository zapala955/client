package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;

@ModuleAnnotation(
   name = "No Render",
   desc = "Убирает всякую хуйню на твоем ебале.",
   category = Category.RENDER
)
@Environment(EnvType.CLIENT)
public class NoRender extends Module {
   public BooleanSetting ignoreScoreboard;
   public BooleanSetting ignoreTotemPop;
   public BooleanSetting noCameraClip;
   public BooleanSetting ignoreBossBar;
   public BooleanSetting ignoreZalupa;
   public BooleanSetting ignoreFire = new BooleanSetting(this, "Убирать огонь", true);
   public BooleanSetting ignoreLava = new BooleanSetting(this, "Убирать туман лавы", true);
   public BooleanSetting removeCamreZalupa;

   public NoRender() {
      this.ignoreZalupa = new BooleanSetting(this, "Убирать плохие эффекты", true);
      this.ignoreScoreboard = new BooleanSetting(this, "Убирать Скорборт", false);
      this.ignoreBossBar = new BooleanSetting(this, "Убирать Босс бар", false);
      this.noCameraClip = new BooleanSetting(this, "Камера сквозь блоки", false);
      this.ignoreTotemPop = new BooleanSetting(this, "Убирать тотем на экране", true);
      this.removeCamreZalupa = new BooleanSetting(this, "Убирать дерганее камеры", true);
   }

   public static dk getInstance() {
      return uq.get(dk.class);
   }

   
}
