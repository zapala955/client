package ru.white.module.impl.misc;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.StringSetting;
import ru.white.utils.misc.Of;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.EntityUtil;
import ru.white.manager.ConfigFile;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Auto Msg",
   category = Category.OTHER,
   desc = "модуль для ремиора пиздабола"
)
@Environment(EnvType.CLIENT)
public class AutoMsg extends Module implements ConfigFile {
   public final BooleanSetting notify;
   public final sf removeBtn;
   private final EntityUtil f933;
   public final sf addBtn;
   private final List<vl.wl> f932;
   private int f934;
   public final SliderSetting delay;
   public final ModeSetting sendMode = new ModeSetting(this, "Режим", "По бинду", "По таймеру", "Оба");

   public AutoMsg() {
      this.delay = new SliderSetting(this, "Задержка (сек)", 5.0F, 1.0F, 60.0F, 1.0F)
         .setVisible(() -> !this.sendMode.is("По бинду"));
      this.notify = new BooleanSetting(this, "Уведомления", true);
      this.addBtn = new sf(this, "Добавить текст", this::addEntry);
      this.f932 = new ArrayList<>();
      this.removeBtn = new sf(this, "Удалить последний", this::removeLastEntry)
         .setVisible(() -> {
            boolean result;
            if (this.f932.size() > 1) {
               result = true;
               
            } else {
               result = false;
            }

            return result;
         });
      this.f933 = new EntityUtil();
      this.f934 = 0;
      this.addEntry();
   }

   @Override
   public void onPreLoad(JsonObject settings) {

      for (String v4 : settings.keySet()) {
         if (!v4.startsWith("Тип #")) {

            if (!v4.startsWith("Текст #") && !v4.startsWith("Бинд #")) {
               continue;
            }
         }

         try {
            int v5 = Integer.parseInt(v4.substring(v4.indexOf(35) + 1).trim());
            if (v5 > v2) {
               v2 = v5;
            }
         } catch (NumberFormatException v8) {
         }
      }

      while (this.f932.size() < v2) {
         this.addEntry();
      }

      while (this.f932.size() > v2) {
         this.removeLastEntry();
      }
   }

   @Override
   protected void onEnable() {

      super.onEnable();

      this.f933.reset();
      this.f934 = 0;
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!this.sendMode.is("По бинду")) {
            if (!this.f932.isEmpty()) {
               long v2 = (long)(this.delay.getValue() * 1000.0F);
               if (this.f933.hasTimeElapsed(v2, true)) {
                  if (this.f934 >= this.f932.size()) {
                     this.f934 = 0;
                  }

                  vl.wl v4 = this.f932.get(this.f934);
                  this.sendEntry(v4);
                  this.f934 = (this.f934 + 1) % this.f932.size();
               }
            }
         }
      }
   }

   public void sendEntry(vl.wl entry) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         String v2 = entry.text.getValue();
         if (v2 != null && !v2.trim().isEmpty()) {
            if (entry.mode.is("/msg")) {
               String v10 = entry.targetNick.getValue();
               if (v10 == null || v10.trim().isEmpty()) {
                  if (this.notify.getValue()) {
                     mc.field_1724
                        .method_7353(
                           Text.method_43470("§c[Auto Msg] Ник игрока не указан!"),
                           true
                        );
                  }

                  return;
               }

               ClientPlayNetworkHandler result = mc.field_1724.field_3944;

               String v14 = v10.trim();
               String v4 = v2.trim();
               String v5 = v14;
               result.method_45730("msg " + v5 + " " + v4);
               Boolean v11 = this.notify.getValue();

               boolean v12 = v11;

               if (v12) {
                  ClientPlayerEntity v13 = mc.field_1724;
                  String v6 = v10.trim();
                  v13.method_7353(
                     Text.method_43470("§a[Auto Msg] Отправлено /msg для " + v6), true
                  );
               }
            } else {
               String v3 = v2.trim();
               if (v3.startsWith("/")) {
                  mc.field_1724.field_3944.method_45730(v3.substring(1));
               } else {
                  mc.field_1724.field_3944.method_45729(v3);
               }

               if (this.notify.getValue()) {
                  mc.field_1724
                     .method_7353(
                        Text.method_43470(
                           "§a[Auto Msg] Сообщение отправлено в чат"
                        ),
                        true
                     );
               }
            }
         }
      }
   }

   public void removeLastEntry() {
      if (this.f932.size() > 1) {

         vl.wl v1 = this.f932.remove(this.f932.size() - 1);
         v1.removeFrom(this);
      }
   }

   public void addEntry() {
      int v1 = this.f932.size() + 1;
      vl.wl v2 = new vl.wl(this, v1);

      this.f932.add(v2);
   }

   @EventTarget
   public void onKey(EventPostMotion event) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1755 == null) {
         if (!this.sendMode.is("По таймеру")) {
            ArrayList result = new ArrayList;

            result./* $VF: Unable to resugar constructor */<init>(this.f932);

            for (vl.wl v3 : result) {
               int v4 = v3.bind.get();
               if (v4 != -1) {
                  int temp1 = event.getKey();

                  if (v4 == temp1) {
                     this.sendEntry(v3);
                  }
               }
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static class wl {
      public final int id;
      public final ModeSetting mode;
      public final StringSetting targetNick;
      public final StringSetting text;
      public final SettingVisibility bind;

      public void removeFrom(Module parent) {
         List result = parent.getSettings();
         int temp1 = v2 * (v2 + 1) % 2;

         if (temp1 != 0) {
            }

         result.remove(this.mode);
         parent.getSettings().remove(this.targetNick);
         parent.getSettings().remove(this.text);
         parent.getSettings().remove(this.bind);
      }

      public wl(Module parent, int id) {
         this.id = id;
         this.mode = new ModeSetting(parent, "Тип #" + id, "Обычный чат", "/msg");
         this.targetNick = new StringSetting(parent, "Ник #" + id, "Player").setVisible(() -> {

            Boolean result = this.mode.is("/msg");

            return result;
         });
         this.text = new StringSetting(parent, "Текст #" + id, "Привет!");
         this.bind = new SettingVisibility(parent, "Бинд #" + id, -1);
      }

      
   }
}
