package ru.white.module;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.ep;

@Environment(EnvType.CLIENT)
public enum Category {
   MOVEMENT("Movement", "H"),
   PLAYER("Player", "N"),
   OTHER("Misc", "L"),
   COMBAT("Combat", "B"),
   RENDER("Render", "Q");

   private final String name;
   private final String f193;
   public Animation alphaS2;
   public ru.white.utils.animation.Animation animation;
   public Animation alphaS = new ep(300, 1.0);

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   private Category(final String name, final String icon) {
      this.alphaS2 = new ep(300, 1.0);
      this.animation = new ru.white.utils.animation.Animation();
      this.name = name;
      this.f193 = icon;
   }

   @Generated
   public ru.white.utils.animation.Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public Animation getAlphaS() {

      return this.alphaS;
   }

   @Generated
   public String getIcon() {
      return this.f193;
   }

   @Generated
   public Animation getAlphaS2() {

      return this.alphaS2;
   }
}
