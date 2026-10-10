package ru.white.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.BiomeEffectSoundPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.white.utils.media.Nr;

@Environment(EnvType.CLIENT)
@Mixin({BiomeEffectSoundPlayer.class})
public class BiomeEffectSoundPlayerMixin {
   @ModifyExpressionValue(
      method = {"method_4756"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_73189()Lnet/minecraft/class_243;"
      )},
      require = 0
   )
   private Vec3d spectator$ambientPos(Vec3d original) {
      Entity target = nr.getTarget();
      return target != null ? target.method_73189() : original;
   }

   @ModifyExpressionValue(
      method = {"method_75840"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_23317()D"
      )},
      require = 0
   )
   private double spectator$moodX(double original) {
      Entity target = nr.getTarget();
      return target != null ? target.method_23317() : original;
   }

   @ModifyExpressionValue(
      method = {"method_75840"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_23320()D"
      )},
      require = 0
   )
   private double spectator$moodY(double original) {
      Entity target = nr.getTarget();
      return target != null ? target.method_23320() : original;
   }

   @ModifyExpressionValue(
      method = {"method_75840"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_746;method_23321()D"
      )},
      require = 0
   )
   private double spectator$moodZ(double original) {
      Entity target = nr.getTarget();
      return target != null ? target.method_23321() : original;
   }
}
