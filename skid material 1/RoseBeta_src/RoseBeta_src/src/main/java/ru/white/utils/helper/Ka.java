package ru.white.utils.helper;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public final class Ka {
   private ha f15;
   private final Supplier<ha> f14;
   private final String displayName;
   private final String id;
   private final Identifier f13;

   public Identifier icon() {

      return this.f13;
   }

   public String id() {

      return this.id;
   }

   public String displayName() {
      int v1 = -658753937;

      return this.displayName;
   }

   public synchronized ha animation() {
      if (this.f15 == null) {

         this.f15 = this.f14.get();
      }

      return this.f15;
   }

   ka(String id, String displayName, Identifier icon) {
      this.id = id;
      this.displayName = displayName;
      this.f13 = icon;
      this.f14 = () -> na.load(id, displayName, "/assets/white/emotes/" + id + ".json");
   }

   
}
