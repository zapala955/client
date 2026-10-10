package ru.white.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.FriendManager;
import ru.white.event.Event;

@ModuleAnnotation(
   name = "No Friend Damage",
   desc = "Убирает удары по друзьям",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class NoFriendDamage extends Module {
   @EventTarget
   public void onEvent(Event event) {
      FriendManager result = Client.get().friendManager();
      Entity temp1 = event.getTarget();

      if (result.isFriend(temp1.method_5477().getString())) {
         event.cancel();
         
      }
   }

   
}
