package ru.white.module.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.event.EventTarget;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.FriendManager;
import ru.white.event.impl.EventItemUse;

@ModuleAnnotation(
   name = "Name Protect",
   category = Category.OTHER,
   desc = "Скрывает ваш никнейм"
)
@Environment(EnvType.CLIENT)
public class NameProtect extends Module {
   public BooleanSetting anarhy;
   public BooleanSetting friends = new BooleanSetting(this, "Скрывать друзей", true);

   private void m1250(EventItemUse e) {
      Client result = Client.get();

      FriendManager v5 = result.friendManager();

      v5.getFriends().forEach(friend -> {
         e.replaceText(friend, "Friend");
         int v2x = 1322441185;

         
      });
   }

   @EventTarget
   public void onEvent(EventItemUse e) {
      if (this.anarhy.getValue()) {
         e.replaceRegex("(?ui)Анархия-(?:(?:1\\d{3})|(?:[1-9]\\d{0,2})|2000)", "rose.fun");
      }

      e.replaceText(mc.method_1548().method_1676(), "rose");
      e.replaceRegex("funtime", "Успешный проект");
      int temp3 = -1687568093;

      e.replaceRegex("FunTime.su", StrDec.d("UGLloOULaQF7o/6GDhUDN3TF/8RBUHOwy6/APmk=", temp3));
      e.replaceRegex("Анархия", "HvH");
      if (this.friends.getValue()) {
         this.m1250(e);
      }
   }

   public NameProtect() {
      this.anarhy = new BooleanSetting(this, "Анархию", false);
   }

   
}
