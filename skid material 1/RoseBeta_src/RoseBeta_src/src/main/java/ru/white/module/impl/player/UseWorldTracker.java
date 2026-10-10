package ru.white.module.impl.player;

import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import ru.white.event.EventTarget;
import ru.white.utils.IInstanceAccess;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.render.vs;
import ru.white.utils.system.Hu;
import ru.white.utils.system.Yt;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Use World Tracker",
   desc = "Кулдауны хилок всех игроков в мире прямо над ником",
   category = Category.PLAYER
)
@Environment(EnvType.CLIENT)
public class UseWorldTracker extends Module {
   public final BooleanSetting blur;
   public final BooleanSetting background;
   public final BooleanSetting debug;
   public final BooleanSetting names;
   public final MultiSetting items;
   private static final float ROW_H = 12.0F;
   private static final float FONT_SIZE = 6.0F;
   private static final float PADDING = 3.5F;
   private static final float GAP = 4.0F;

   public static pi get() {
      pi result = uq.get(pi.class);

      return result;
   }

   public void render(DrawContext context, PlayerEntity player, float centerX, float topY) {
      if (this.isEnabled()) {
         ArrayList v5 = new ArrayList();

         for (fr.hr v7 : fr.of(player.method_5667()).keySet()) {
            if (this.items.getValue(v7.label)) {
               v5.add(v7);
            }
         }

         if (!v5.isEmpty()) {
            yt v20 = hu.sf_regular;
            ArrayList v21 = new ArrayList();
            ArrayList v8 = new ArrayList();
            float v9 = 3.0F;

            for (fr.hr v11 : v5) {
               String result;
               if (this.names.getValue()) {
                  String v17 = v11.label;
                  result = v17 + " ";
               } else {
                  result = "";
               }

               String v12 = result;
               String v13 = fr.format(fr.remaining(player.method_5667(), v11));
               v21.add(v12);
               v8.add(v13);
               v9 += v20.getWidth(v12, 6.0F) + v20.getWidth(v13, 6.0F) + 4.0F;
            }

            float v22 = centerX - v9 / 2.0F;
            float v23 = topY - 12.0F - 2.0F;
            if (this.background.getValue() && this.blur.getValue()) {
               vs.ws.blur(v22, v23, v9, 12.0F, 1.0F, 4.0F, up.multAlpha(up.getColor(0), 0.2F));
            } else if (this.background.getValue()) {
               vs.ys.rect(v22, v23, v9, 12.0F, up.multAlpha(up.getColor(0), 0.4F), 4.0F);
            }

            Client.get().render2D().flushAll();
            float v24 = v22 + 3.5F;
            float v25 = v23 + 6.0F - 3.65F;

            while (v14 < v5.size()) {
               String v15 = (String)v21.get(v14);
               String v16 = (String)v8.get(v14);
               if (!v15.isEmpty()) {
                  v20.draw(v15, v24, v25, 6.0F, up.getColor(220));
                  v24 += v20.getWidth(v15, 6.0F);
               }

               v20.draw(v16, v24, v25, 6.0F, up.client());
               float temp1 = v20.getWidth(v16, 6.0F);

               v24 += temp1 + 4.0F;
               v14++;
            }
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      fr.tick(event);
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      fr.clear();
   }

   @Override
   protected void onEnable() {

      Boolean result = this.debug.getValue();

      fr.debug = result;
   }

   @Override
   protected void onDisable() {
      fr.debug = false;

      
   }

   public UseWorldTracker() {
      BooleanSetting[] temp5 = new BooleanSetting[7];
      temp5[0] = new BooleanSetting(fr.hr.NOTCH.label, true);
      temp5[1] = new BooleanSetting(fr.hr.GAPPLE.label, true);
      temp5[2] = new BooleanSetting(fr.hr.HEAL.label, true);
      temp5[3] = new BooleanSetting(fr.hr.NAUSEA.label, true);
      temp5[4] = new BooleanSetting(fr.hr.CHORUS.label, true);
      temp5[5] = new BooleanSetting(fr.hr.KELP.label, false);
      temp5[6] = new BooleanSetting(fr.hr.SCRAP.label, false);
      this.items = new MultiSetting(this, "Предметы", temp5);
      this.names = new BooleanSetting(this, "Названия", true);
      this.background = new BooleanSetting(this, "Фон", true);
      this.blur = new BooleanSetting(this, "Блюр фон", true).setVisible(() -> {

         return this.background.getValue();
      });
      this.debug = new BooleanSetting(this, "Логи детекта", false).onAction(() -> {
         pi result = get();

         fr.debug = result.debug.getValue();
      });
   }

   
}
