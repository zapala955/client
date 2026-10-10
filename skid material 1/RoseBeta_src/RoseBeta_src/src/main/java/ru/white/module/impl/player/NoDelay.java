package ru.white.module.impl.player;

import java.lang.reflect.Field;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import ru.white.event.EventTarget;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "No Delay",
   category = Category.PLAYER,
   desc = "Убирает задержки"
)
@Environment(EnvType.CLIENT)
public class NoDelay extends Module {
   public BooleanSetting expBottle;
   public BooleanSetting jump = new BooleanSetting(this, "Прыжок", true);

   public static mi get() {
      Module result = uq.get(mi.class);

      return (mi)result;
   }

   private void m628() {
      try {
         Field v1 = MinecraftClient.class.getDeclaredField("itemUseCooldown");
         v1.setAccessible(true);
         v1.setInt(EventItemUse, 0);
      } catch (Exception v6) {
         try {
            Field v2 = MinecraftClient.class.getDeclaredField("field_1752");
            v2.setAccessible(true);

            v2.setInt(EventItemUse, 0);
         } catch (Exception v5) {
         }
      }
   }

   @EventTarget
   private void onUpdate(EventTick event) {
      if (mc.field_1724 != null) {

         Boolean result = this.expBottle.getValue();

         if (result && mc.field_1724.method_6047().method_7909() == Items.field_8287) {
            ItemCooldownManager v5 = mc.field_1724.method_7357();

            v5.method_7900(Registries.field_41178.method_10221(Items.field_8287));
            this.m628();
         }
      }
   }

   public NoDelay() {
      this.expBottle = new BooleanSetting(this, "Пузырёк опыта", true);
   }

   
}
