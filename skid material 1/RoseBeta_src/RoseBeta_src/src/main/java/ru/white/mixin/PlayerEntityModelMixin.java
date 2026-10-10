package ru.white.mixin;

import java.util.EnumMap;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.white.utils.helper.Fa;
import ru.white.utils.helper.Oa;
import ru.white.utils.helper.Pa;

@Environment(EnvType.CLIENT)
@Mixin({PlayerEntityModel.class})
public abstract class PlayerEntityModelMixin {
   @Inject(
      method = {"method_62110"},
      at = {@At("TAIL")}
   )
   private void rose$applyEmotePose(PlayerEntityRenderState state, CallbackInfo ci) {
      if (state instanceof pa carrier) {
         EnumMap<oa, float[]> pose = carrier.rose$getEmotePose();
         if (pose != null) {
            PlayerEntityModel model = (PlayerEntityModel)this;

            for (Entry<oa, float[]> entry : pose.entrySet()) {
               ModelPart part = switch ((oa)entry.getKey()) {
                  case HEAD -> model.field_3398;
                  case BODY -> model.field_3391;
                  case RIGHT_ARM -> model.field_3401;
                  case LEFT_ARM -> model.field_27433;
                  case RIGHT_LEG -> model.field_3392;
                  case LEFT_LEG -> model.field_3397;
               };
               rose$apply(part, entry.getValue());
            }
         }
      }
   }

   @Unique
   private static void rose$apply(ModelPart part, float[] values) {
      if (part != null) {
         float x = values[fa.POS_X.ordinal()];
         float y = values[fa.POS_Y.ordinal()];
         float z = values[fa.POS_Z.ordinal()];
         if (!Float.isNaN(x)) {
            part.field_3657 = x;
         }

         if (!Float.isNaN(y)) {
            part.field_3656 = y;
         }

         if (!Float.isNaN(z)) {
            part.field_3655 = z;
         }

         float pitch = values[fa.ROT_PITCH.ordinal()];
         float yaw = values[fa.ROT_YAW.ordinal()];
         float roll = values[fa.ROT_ROLL.ordinal()];
         if (!Float.isNaN(pitch)) {
            part.field_3654 = pitch;
         }

         if (!Float.isNaN(yaw)) {
            part.field_3675 = yaw;
         }

         if (!Float.isNaN(roll)) {
            part.field_3674 = roll;
         }
      }
   }
}
