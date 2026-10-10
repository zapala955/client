package ru.white.module.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.manager.rotation.Rotation;
import ru.white.manager.rotation.AngleUtil;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventTick;

@ModuleAnnotation(
   name = "Mace Helper",
   desc = "Логика булавы: свап, атака при падении, наводка",
   category = Category.COMBAT
)
@Environment(EnvType.CLIENT)
public class MaceHelper extends Module {
   public final SliderSetting minTargetHeight;
   private int f308;
   private LivingEntity f307;
   public final SliderSetting maceDistance;
   public final BooleanSetting maceAutoSwap = new BooleanSetting(this, "Свап на булаву", true);

   private boolean m383(LivingEntity entity, double rangeSq, double minY, boolean smashReady) {
      if (entity == null || entity == mc.field_1724) {
         int temp1 = v9 * (v9 + 1) % 2;

         if (temp1 != 0) {
            }

         return false;
      } else if (!entity.method_5805()) {
         return false;
      } else if (entity instanceof ArmorStandEntity) {
         return false;
      } else if (entity instanceof PlayerEntity && Client.get().friendManager().isFriend(entity.method_5477().getString())) {
         return false;
      } else {
         double v7 = mc.field_1724.method_23318() - entity.method_23318();
         return !smashReady && v7 < minY ? false : mc.field_1724.method_5858(entity) < rangeSq;
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.field_1724 == null || mc.field_1761 == null) {
         this.f307 = null;
      } else if (this.f308 > 0) {
         this.f308--;
      } else {
         boolean v2 = mc.field_1724.method_6128();
         LivingEntity v3 = this.m382();
         if (this.maceAutoSwap.getValue() && !mc.field_1724.method_24828() && !v2 && !this.m385() && this.m380() > 3.5 && this.m381(28.0)) {
            int v4 = this.m379();
            if (v4 >= 0) {
               mc.field_1724.method_31548().method_61496(v4);
               mc.field_1761.method_2911();
            }
         }

         if (!(mc.field_1724.field_6017 <= 1.5) && !v2 && this.m385()) {
            this.f307 = v3;
            if (this.f307 != null) {
               this.m384();

               mc.field_1761.method_2918(mc.field_1724, this.f307);
               mc.field_1724.method_6104(Hand.field_5808);
               this.f308 = 4;
            }
         } else {
            this.f307 = null;
         }
      }
   }

   @Override
   protected void onDisable() {

      this.f307 = null;

      this.f308 = 0;
      super.onDisable();
   }

   private int m379() {
      if (mc.field_1724 == null) {
         return -1;
      } else {
         for (int v1 = 0; v1 <= 8; v1++) {
            PlayerInventory result = mc.field_1724.method_31548();

            ItemStack v2 = result.method_5438(v1);
            if (!v2.method_7960() && (v2.method_7909() instanceof MaceItem || v2.method_31574(Items.field_49814))) {
               return v1;
            }
         }

         return -1;
      }
   }

   @Override
   protected void onEnable() {
      this.f307 = null;
      this.f308 = 0;

      super.onEnable();
   }

   private double m380() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         BlockPos result = mc.field_1724.method_24515();

         BlockPos v1 = result;

         for (int v2 = 1; v2 <= 24; v2++) {

            if (!mc.field_1687.method_8320(v1.method_10087(v2)).method_26215()) {
               double v6 = (double)(v2 - 1);

               return v6;
            }
         }

         return 24.0;
      } else {
         return 0.0;
      }
   }

   private void m384() {
      if (this.f307 != null && mc.field_1724 != null) {
         Vec3d v1 = this.f307.method_5829().method_1005();
         double v2 = v1.field_1352 - mc.field_1724.method_23317();
         double v4 = v1.field_1351 - mc.field_1724.method_23320();
         double result = v1.field_1350;
         double temp1 = mc.field_1724.method_23321();

         double v6 = result - temp1;
         double v8 = (double)MathHelper.method_15355((float)(v2 * v2 + v6 * v6));
         float v10 = (float)(MathHelper.method_15349(v6, v2) * (180.0 / Math.PI) - 90.0);
         result = -MathHelper.method_15349(v4, v8) * (180.0 / Math.PI);

         float v11 = (float)result;
         xd.update(new Rotation(v10, v11), 360.0F, 360.0F, 360.0F, 360.0F, 1, 1, false);
      }
   }

   @EventTarget
   public void onDisplay(EventReceivePacket event) {

      if (mc.field_1724 != null) {

         if (mc.field_1687 != null && this.f307 != null && this.m385()) {
            this.m384();
         }
      }
   }

   public static mg getInstance() {
      mg result = uq.get(mg.class);

      return result;
   }

   private LivingEntity m382() {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         double v1 = (double)this.maceDistance.getValue().floatValue();
         double v3 = v1 * v1;
         double v5 = (double)this.minTargetHeight.getValue().floatValue();
         boolean v7 = mc.field_1724.field_6017 > 1.5;
         if (fg.get() != null && fg.get().isEnabled() && fg.target != null) {
            LivingEntity v8 = fg.target;

            if (this.m383(v8, v3, v5, v7)) {
               return v8;
            }
         }

         PlayerEntity v16 = null;
         double v9 = v3;

         for (PlayerEntity v12 : mc.field_1687.method_18456()) {

            if (this.m383(v12, v9, v5, v7)) {
               v16 = v12;
               v9 = mc.field_1724.method_5858(v12);
            }
         }

         return v16;
      } else {
         return null;
      }
   }

   private boolean m381(double range) {
      if (mc.field_1724 != null && mc.field_1687 != null) {

         double v3 = range * range;

         for (PlayerEntity v6 : mc.field_1687.method_18456()) {
            if (v6 != mc.field_1724
               && v6.method_5805()
               && !Client.get().friendManager().isFriend(v6.method_5477().getString())
               && mc.field_1724.method_5858(v6) < v3) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public MaceHelper() {
      this.maceDistance = new SliderSetting(this, "Дист. булавы", 3.0F, 1.0F, 6.0F, 0.1F);
      this.minTargetHeight = new SliderSetting(this, "Мин. высота над целью", 0.1F, 0.0F, 2.0F, 0.1F);
      this.f307 = null;
      this.f308 = 0;
   }

   private boolean m385() {
      if (mc.field_1724 == null) {
         return false;
      } else {
         ItemStack v1 = mc.field_1724.method_6047();

         if (!v1.method_7960()) {

            if (v1.method_7909() instanceof MaceItem || v1.method_31574(Items.field_49814)) {
               return true;
            }
         }

         return false;
      }
   }

   
}
