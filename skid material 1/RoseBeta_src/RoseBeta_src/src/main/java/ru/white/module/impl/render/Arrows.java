package ru.white.module.impl.render;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.animation.Animation;
import ru.white.utils.animation.ap;
import ru.white.utils.PacketUtil;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Arrows",
   category = Category.RENDER,
   desc = "Стрелки на игроков"
)
@Environment(EnvType.CLIENT)
public class Arrows extends Module {
   public SliderSetting size;
   public SliderSetting radius;
   public BooleanSetting onlyArmored = new BooleanSetting(this, "Игнорировать голых", false);
   private static final Identifier ARROW_TEX = Identifier.method_60655("client", "textures/arrow.png");
   private final Animation f402;

   @EventTarget
   public void onTick(EventRenderModel e) {
      if (mc.field_1724 != null) {

         this.f402.run(mc.field_1724.method_5624() ? 1.0 : (mc.field_1755 == null ? 0.0 : 6.0), 0.1F, ap.SINE_OUT);
      }
   }

   private static float m455(Entity entity, float partialTicks) {
      float result = fq.interpolate(entity.field_6038, entity.method_23317(), (double)partialTicks);
      double temp1 = mc.field_1724.field_6038;
      double temp2 = mc.field_1724.method_23317();

      double v2 = (double)(result - fq.interpolate(temp1, temp2, (double)partialTicks));
      double v4 = (double)(
         fq.interpolate(entity.field_5989, entity.method_23321(), (double)partialTicks)
            - fq.interpolate(mc.field_1724.field_5989, mc.field_1724.method_23321(), (double)partialTicks)
      );
      return (float)(-(Math.atan2(v2, v4) * (180.0 / Math.PI)));
   }

   public Arrows() {
      this.size = new SliderSetting(this, "Размер", 30.0F, 12.0F, 80.0F, 1.0F);
      this.radius = new SliderSetting(this, "Радиус от прицела", 50.0F, 20.0F, 80.0F, 1.0F);
      this.f402 = new Animation();
   }

   @EventTarget(
      priority = -500
   )
   public void onDisplay(EventReceivePacket e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!mc.field_1690.field_1842 && mc.field_1690.method_31044().equals(Perspective.field_26664)) {

            List v2 = mc.field_1687.method_18456().stream().filter(p -> p != mc.field_1724).filter(p -> {
               if (this.onlyArmored.getValue()) {
                  if (!m454(p)) {
                     return false;
                  }

                  
               }

               return true;
            }).toList();
            if (!v2.isEmpty()) {
               this.f402.update();
               float v3 = 2.0F;
               float v4 = (float)mc.method_22683().method_4495();
               float v5 = v3 / v4;
               int v6 = (int)((float)mc.method_22683().method_4486() / v5);
               int v7 = (int)((float)mc.method_22683().method_4502() / v5);
               float v8 = e.getPartialTicks();
               float v9 = (float)v6 / 2.0F;
               float v10 = (float)v7 / 2.0F;
               float v11 = this.size.getValue() / 4.0F;
               float v12 = v10 - this.radius.getValue() - this.radius.getValue() / 4.0F * this.f402.get();
               float v13 = v12 + v11 / 2.0F - v10;

               for (AbstractClientPlayerEntity v15 : v2) {
                  int v16 = Client.get().friendManager().isFriend(v15.method_5477().getString()) ? up.GREEN : up.fade(1);
                  float v17 = m455(v15, v8) - mc.field_1773.method_19418().method_19330();
                  float v18 = (float)Math.toRadians((double)v17);
                  float v19 = v9 - v13 * (float)Math.sin((double)v18);
                  float v20 = v10 + v13 * (float)Math.cos((double)v18);
                  Client.get()
                     .render2D()
                     .getTexturePipeline()
                     .drawGlowTexture(
                        ARROW_TEX,
                        v19 - v11 / 2.0F,
                        v20 - v11 / 2.0F,
                        v11,
                        v11,
                        0.0F,
                        0.0F,
                        1.0F,
                        1.0F,
                        new int[]{v16, v16, v16, v16},
                        new float[]{0.0F, 0.0F, 0.0F, 0.0F},
                        1.0F,
                        v17
                     );
               }
            }
         }
      }
   }

   private static boolean m454(AbstractClientPlayerEntity player) {
      if (player.method_6118(EquipmentSlot.field_6169).method_7960() && player.method_6118(EquipmentSlot.field_6174).method_7960()) {
         ItemStack result = player.method_6118(EquipmentSlot.field_6172);

         if (result.method_7960()) {
            boolean v4 = player.method_6118(EquipmentSlot.field_6166).method_7960();

            if (v4) {
               return false;
            }

            
         }
      }

      return true;
   }

   
}
