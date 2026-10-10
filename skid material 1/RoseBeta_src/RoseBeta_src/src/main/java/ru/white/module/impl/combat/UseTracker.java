package ru.white.module.impl.combat;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.IInstanceAccess;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.setting.MultiSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Oq;
import ru.white.event.Event;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Use Tracker",
   desc = "Кулдауны хилок цели и её баффы",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class UseTracker extends Module {
   private boolean f360;
   public final SliderSetting forget;
   public final MultiSetting items;
   public final MultiSetting buffs;
   public final BooleanSetting notifications;
   private UUID f357;
   private long f359;
   private String f358;

   public PlayerEntity getTargetPlayer() {
      PlayerEntity result;
      if (this.f357 != null && mc.field_1687 != null) {

         result = mc.field_1687.method_18470(this.f357);
      } else {
         result = null;
      }

      return result;
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      this.onDisable();

      fr.clear();
   }

   public static ug getInstance() {

      return uq.get(ug.class);
   }

   @Override
   protected void onDisable() {
      this.f357 = null;

      this.f358 = null;
      this.f359 = 0L;
      this.f360 = false;
   }

   public boolean hasTarget() {
      if (this.isEnabled() && this.f357 != null) {

         if (System.currentTimeMillis() - this.f359 < (long)(this.forget.getValue() * 1000.0F)) {
            return true;
         }
      }

      return false;
   }

   public String getTargetName() {
      String result;
      if (this.f358 == null) {
         result = "";
         
      } else {
         result = this.f358;
         
      }

      return result;
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      fr.tick(event);
      if (mc.field_1724 != null && mc.field_1687 != null) {
         boolean v2 = mc.field_1724.method_6059(StatusEffects.field_5916);
         if (v2 && !this.f360 && this.hasTarget()) {
            PlayerEntity v3 = this.getTargetPlayer();
            if (v3 != null) {
               fr.trigger(v3, fr.hr.NAUSEA);
            }
         }

         this.f360 = v2;
      }
   }

   public UUID getTargetUuid() {

      return this.f357;
   }

   public int remaining(fr.hr item) {
      if (!this.items.getValue(item.label)) {

         return 0;
      } else {

         return fr.remaining(this.f357, item);
      }
   }

   @EventTarget
   public void onAttack(Event event) {
      if (event.getTarget() instanceof PlayerEntity v2) {

         if (v2 != mc.field_1724) {
            this.f357 = v2.method_5667();
            this.f358 = v2.method_5477().getString();
            this.f359 = System.currentTimeMillis();
            return;
         }
      }
   }

   public UseTracker() {
      BooleanSetting[] temp5 = new BooleanSetting[7];
      temp5[0] = new BooleanSetting(fr.hr.NOTCH.label, true);
      temp5[1] = new BooleanSetting(fr.hr.GAPPLE.label, true);
      temp5[2] = new BooleanSetting(fr.hr.HEAL.label, true);
      temp5[3] = new BooleanSetting(fr.hr.NAUSEA.label, true);
      temp5[4] = new BooleanSetting(fr.hr.CHORUS.label, true);
      temp5[5] = new BooleanSetting(fr.hr.KELP.label, false);
      temp5[6] = new BooleanSetting(fr.hr.SCRAP.label, false);
      this.items = new MultiSetting(this, "Предметы", temp5);
      temp5 = new BooleanSetting[6];
      temp5[0] = new BooleanSetting(fr.gr.STRENGTH.label, true);
      temp5[1] = new BooleanSetting(fr.gr.SPEED.label, true);
      temp5[2] = new BooleanSetting(fr.gr.RESISTANCE.label, true);
      temp5[3] = new BooleanSetting(fr.gr.REGENERATION.label, true);
      temp5[4] = new BooleanSetting(fr.gr.FIRE_RESISTANCE.label, true);
      temp5[5] = new BooleanSetting(fr.gr.ABSORPTION.label, true);
      this.buffs = new MultiSetting(this, "Баффы цели", temp5);
      this.notifications = new BooleanSetting(this, "Уведомления", true);
      this.forget = new SliderSetting(this, "Забывать цель, сек", 60.0F, 10.0F, 300.0F, 10.0F);
      fr.listen(this::m441);
   }

   private void m441(PlayerEntity player, fr.hr item) {
      boolean result = this.isEnabled();

      if (result) {
         if (this.notifications.getValue()) {
            if (!player.method_5667().equals(this.f357)) {
               return;
            }

            if (!this.items.getValue(item.label)) {
               return;
            }

            String v9 = player.method_5477().getString();

            String v3 = item.label;
            String v4 = v9;
            oq.send(v4 + " — " + v3, oq.qq.INFO, new ItemStack(item.icon));
            return;
         }

         
      }
   }

   
}
