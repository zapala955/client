package ru.white.module.impl.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.DiscordManager;

@ModuleAnnotation(
   name = "DiscordRPC",
   desc = "Отображает статус в Discord RPC",
   category = Category.RENDER,
   autoEnabled = true
)
@Environment(EnvType.CLIENT)
public class DiscordRPC extends Module {
   private static zi instance;
   private static final MinecraftClient EventItemUse = MinecraftClient.method_1551();

   public static zi getInstance() {

      return instance;
   }

   @Override
   public void setEnabled(boolean enabled, boolean notification) {
      super.setEnabled(enabled, notification);
      DiscordManager v3 = Client.get() != null ? Client.get().getDiscordManager() : null;
      if (v3 != null) {
         if (enabled) {
            v3.init();
            if (mc.field_1705 != null && mc.field_1724 != null && notification) {

               if (v3.isInitialized()) {
                  mc.field_1705
                     .method_1743()
                     .method_1812(
                        Text.method_43470("Discord RPC » Включено")
                           .method_27692(Formatting.field_1060)
                     );
               } else {
                  ChatHud v6 = mc.field_1705.method_1743();
                  String temp1 = "Discord RPC » Ошибка загрузки библиотеки";

                  v6.method_1812(Text.method_43470(temp1).method_27692(Formatting.field_1061));
               }
            }
         } else {
            v3.stopRPC();
            if (mc.field_1705 != null && mc.field_1724 != null && notification) {
               mc.field_1705
                  .method_1743()
                  .method_1812(
                     Text.method_43470("Discord RPC » Выключено").method_27692(Formatting.field_1061)
                  );
            }
         }
      }
   }

   @Override
   protected void onDisable() {
      DiscordManager v1 = Client.get() != null ? Client.get().getDiscordManager() : null;
      if (v1 != null) {
         v1.stopRPC();
         
      }
   }

   public DiscordRPC() {
      instance = this;
   }

   @Override
   protected void onEnable() {
      Client result = Client.get();

      DiscordManager v1 = result != null ? Client.get().getDiscordManager() : null;
      if (v1 != null) {
         v1.init();
         
      }
   }

   
}
