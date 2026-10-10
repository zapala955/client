package ru.white.utils.render.gl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import ru.white.module.impl.combat.AttackAura;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public interface Vg extends IMinecraft {
   default void onAttack() {
   }

   void onRotation(fg v1, LivingEntity v2, float[] v3, boolean v4);

   default void reset() {
   }
}
