package ru.white.command.impl;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.Identifier;
import org.joml.Vector2f;
import ru.white.event.EventTarget;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.command.Command;
import ru.white.utils.render.pt;
import ru.white.utils.IMinecraft;
import ru.white.utils.ChatUtil;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventReceivePacket;

@Environment(EnvType.CLIENT)
public class GpsCommand extends Command implements IMinecraft {
   private static final float RADIUS = 30.0F;
   private static final Identifier ARROW_TEX = Identifier.method_60655("client", "textures/arrow.png");
   private Vector2f f1 = null;
   private static final float SIZE = 20.0F;

   @EventTarget(
      priority = -500
   )
   public void onDisplay(EventReceivePacket e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (this.f1 != null) {
            if (!mc.field_1690.field_1842) {
               if (mc.field_1690.method_31044().equals(Perspective.field_26664)) {
                  double v2 = (double)this.f1.x + 0.5 - mc.field_1724.method_23317();
                  double v4 = (double)this.f1.y + 0.5 - mc.field_1724.method_23321();
                  int v6 = (int)Math.sqrt(v2 * v2 + v4 * v4);
                  float v7 = 2.0F;
                  float v8 = (float)mc.method_22683().method_4495();
                  float v9 = v7 / v8;
                  int v10 = (int)((float)mc.method_22683().method_4486() / v9);
                  int v11 = (int)((float)mc.method_22683().method_4502() / v9);
                  float v12 = (float)v10 / 2.0F;
                  float v13 = (float)v11 / 2.0F - 90.0F;
                  float v14 = (float)(-(Math.atan2(v2, v4) * (180.0 / Math.PI)));
                  float v15 = v14 - mc.field_1773.method_19418().method_19330();
                  float v16 = (float)Math.toRadians((double)v15);

                  float v17 = v13 - 30.0F;
                  float v18 = v17 + 10.0F - v13;
                  float v19 = v12 - v18 * (float)Math.sin((double)v16);
                  float temp1 = v18 * (float)Math.cos((double)v16);

                  float v20 = v13 + temp1;
                  int v21 = up.fade(1);
                  pt v26 = Client.get().render2D().getTexturePipeline();

                  v26.drawGlowTexture(
                     ARROW_TEX,
                     v19 - 10.0F,
                     v20 - 10.0F,
                     20.0F,
                     20.0F,
                     0.0F,
                     0.0F,
                     1.0F,
                     1.0F,
                     new int[]{v21, v21, v21, v21},
                     new float[]{0.0F, 0.0F, 0.0F, 0.0F},
                     1.0F,
                     v15
                  );
                  hu.sf_regular.drawCentered(v6 + "м", v19, v20 + 10.0F + 4.0F, 6.0F, up.getColor(255, 0.85F));
               }
            }
         }
      }
   }

   @Override
   public void execute(String[] args) {
      if (args.length == 0) {
         this.m27();
      } else {
         String result = args[0].toLowerCase();

         String v2 = result;
         switch (v2) {
            case "set":
               if (args.length < 3) {
                  zp.addChatMessage("§7Использование: §f.gps set <x> <z>");
                  return;
               }

               try {
                  float v4 = Float.parseFloat(args[1]);
                  float v5 = Float.parseFloat(args[2]);
                  this.f1 = new Vector2f(v4, v5);
                  int v11 = (int)v4;
                  int v6 = (int)v5;
                  int v7 = v11;
                  zp.addChatMessage("§aGPS §7→ §f" + v7 + "§7, §f" + v6);
               } catch (NumberFormatException v10) {

                  zp.addChatMessage("§cОшибка: неверные координаты");
               }
               break;
            case "off":
               this.f1 = null;
               zp.addChatMessage("§7GPS сброшен");
               break;
            default:
               this.m27();
         }
      }
   }

   @Override
   public List<String> getSuggestions(String subPrefix) {
      List result = List.of("set", "off");

      return result.stream().filter(s -> {

         return s.startsWith(subPrefix.toLowerCase());
      }).toList();
   }

   public GpsCommand() {
      super(
         "gps",
         ".gps <set|off> <x> <z>",
         "Стрелка на экране ведущая к координатам"
      );
      Client.eventHandler().subscribe(this);
   }

   private void m27() {
      int temp1 = -1446472440;

      zp.addChatMessage(StrDec.d("aTKiPXmQa6ZD2Ol6kb5Sfth23V/U9FAoawsZevjCZorkChOcxvuQrI5xSIf6", temp1));
   }

   
}
