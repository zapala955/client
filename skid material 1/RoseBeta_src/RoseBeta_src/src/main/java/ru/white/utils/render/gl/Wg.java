package ru.white.utils.render.gl;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.friend.Ah;
import ru.white.utils.friend.Bh;
import ru.white.utils.friend.Ch;
import ru.white.utils.friend.Xg;
import ru.white.utils.friend.Yg;
import ru.white.utils.friend.Zg;

@Environment(EnvType.CLIENT)
public enum Wg {
   SPOOKYTIME("SpookyTime", new bh()),
   SNAP("Snap", new zg()),
   WHITERISE("DonutSMP", new ch()),
   SPOOKYDUEL("SpookyDuel", new ah()),
   NEURO("Neuro", new yg()),
   HVH("HvH", new xg());
   private final String name;
   // $VF: synthetic field
   private static final wg[] $VALUES = m442();
   private final vg f361;

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public vg getRotation() {
      int v2 = 2142296874;
      int temp1 = v2 * (v2 + 1);

      return this.f361;
   }

   @Generated
   private Wg(final String name, final vg rotation) {
      this.name = name;
      this.f361 = rotation;
   }
}
