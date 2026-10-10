package ru.white.module.impl.render;

import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.shader.ku;
import ru.white.event.Event;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Scan World",
   category = Category.RENDER,
   desc = "Сканирующая волна по миру"
)
@Environment(EnvType.CLIENT)
public class ScanWorld extends Module {
   public tf tintColor;
   public BooleanSetting periodic;
   private long f759;
   public SliderSetting interval;
   public SliderSetting duration;
   public ModeSetting typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
   private long f757;
   private static final byte DEATH_STATUS = 3;
   private int f758;
   public SliderSetting width;
   public BooleanSetting onTotem;
   private static final byte TOTEM_STATUS = 35;
   public SliderSetting maxRadius;
   public BooleanSetting onKill;
   private static final long KILL_WINDOW_MS = 6500L;

   @EventTarget
   public void onAttack(Event e) {
      if (e.getTarget() instanceof LivingEntity v2 && v2 != mc.field_1724) {
         this.f758 = v2.method_5628();

         this.f759 = System.currentTimeMillis();
         return;
      }
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int v2 = this.getColor();
         int v3 = v2 >> 16 & 255;
         int v4 = v2 >> 8 & 255;
         int v5 = v2 & 255;
         ku v6 = ku.getInstance();
         v6.setDuration(this.duration.getValue());

         v6.setWidth(this.width.getValue());
         v6.setMaxRadius(this.maxRadius.getValue());
         v6.setOuterColor(new Color(v3, v4, v5, 90));
         v6.setMidColor(new Color(v3, v4, v5, 90));
         v6.setInnerColor(new Color(v3, v4, v5, 180));
         v6.setScanlineColor(new Color(v3, v4, v5, 90));
         Matrix4f temp1 = e.getMatrixStack().method_23760().method_23761();
         GameRenderer v9 = mc.field_1773;
         Object temp3 = mc.field_1690.method_41808().method_41753();

         v6.render(temp1, v9.method_22973(((Integer)temp3).floatValue()));
      }
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {

      this.f758 = -1;
      ku.getInstance().stopScan();
   }

   public ScanWorld() {
      this.tintColor = new tf(this, "Цвет", -11477761).setVisible(() -> {
         boolean result = this.typeColor.is("Свой");

         return result;
      });
      this.periodic = new BooleanSetting(this, "Периодически", true);
      this.interval = new SliderSetting(this, "Интервал", 3.0F, 1.0F, 15.0F, 0.5F).setVisible(() -> {

         return this.periodic.getValue();
      });
      this.onTotem = new BooleanSetting(this, "При тотеме", true);
      this.onKill = new BooleanSetting(this, "При убийстве", true);
      this.duration = new SliderSetting(this, "Длительность", 2.5F, 0.5F, 8.0F, 0.1F);
      this.width = new SliderSetting(this, "Ширина", 10.0F, 1.0F, 32.0F, 1.0F);
      this.maxRadius = new SliderSetting(this, "Радиус", 80.0F, 10.0F, 256.0F, 2.0F);
      this.f758 = -1;
   }

   @Override
   protected void onEnable() {
      this.f758 = -1;

      if (mc.field_1724 != null) {
         this.f757 = System.currentTimeMillis();
         ku.getInstance().startScan(mc.field_1724.method_73189());
      }
   }

   public int getColor() {
      if (this.typeColor.is("Тема")) {
         int result = up.getClientColor1(1);

         return result;
      } else {
         return this.tintColor.getValue();
      }
   }

   @EventTarget
   public void onPacket(EventStep e) {
      if (e.getPacket() instanceof EntityStatusS2CPacket v2) {
         if (mc.field_1687 != null) {
            if (v2.method_11470() == 35 && this.onTotem.getValue()) {
               Entity v8 = v2.method_11469(mc.field_1687);
               if (v8 == null) {
                  return;
               }

               ku.getInstance().startScan(v8.method_73189());
            } else if (v2.method_11470() == 3 && this.onKill.getValue()) {
               Entity v7 = v2.method_11469(mc.field_1687);

               if (!(v7 instanceof LivingEntity v4)) {
                  return;
               }

               if (v4.method_5628() != this.f758) {
                  return;
               }

               long result = System.currentTimeMillis();

               if (result - this.f759 > 6243436267417933916L + -6243436267417927416L) {
                  return;
               }

               this.f758 = -1;
               ku.getInstance().startScan(v4.method_73189());
            }
         }
      }
   }

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null) {
         if (mc.field_1687 != null) {
            if (!this.periodic.getValue()) {
               return;
            }

            long v2 = System.currentTimeMillis();
            long v4 = (long)(this.interval.getValue() * 1000.0F);
            if (v2 - this.f757 >= v4) {

               Vec3d v6 = mc.field_1724.method_73189();
               ku.getInstance().startScan(v6);
               this.f757 = v2;
            }

            return;
         }

         
      }
   }

   @Override
   protected void onDisable() {
      ku result = ku.getInstance();

      result.stopScan();
   }

   
}
