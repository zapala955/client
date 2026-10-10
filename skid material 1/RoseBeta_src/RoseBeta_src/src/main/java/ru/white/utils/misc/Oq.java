package ru.white.utils.misc;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;

@Environment(EnvType.CLIENT)
public class Oq {
   private static final List<oq.pq> entries = new ArrayList<>();

   public static void send(String text, oq.qq type, Identifier texture, long ms) {

      add(text, type, null, texture, null, ms);
   }

   public static void send(String text, oq.qq type, Identifier texture) {

      add(text, type, null, texture, null, -5806241729028253041L + 5806241729028256041L);
   }

   public static void send(String text) {

      add(text, oq.qq.INFO, null, null, null, -5188614054444044406L + 5188614054444047406L);
   }

   public static void send(String text, oq.qq type, String glyph, long ms) {
      add(text, type, glyph, null, null, ms);
      
   }

   public static void send(String text, oq.qq type, String glyph) {
      int v3 = 825898042;

      long v6 = 6433714409658919558L + -6433714409658916558L;

      add(text, type, glyph, null, null, v6);
   }

   public static void send(String text, oq.qq type, long ms) {
      int v4 = -2087589046;

      add(text, type, null, null, null, ms);
   }

   public static void send(String text, oq.qq type, ItemStack item, long ms) {
      int v5 = 141123551;
      int temp5 = v5 * (v5 + 1) % 2;

      if (temp5 != 0) {
         }

      add(text, type, null, null, item, ms);
   }

   public static void send(String text, oq.qq type, ItemStack item) {

      add(text, type, null, null, item, 6523837450566461252L + -6523837450566458252L);
   }

   public static void send(String text, oq.qq type) {

      long temp5 = 6531463129147555173L + -6531463129147552173L;

      add(text, type, null, null, null, temp5);
   }

   public static List<oq.pq> entries() {

      return entries;
   }

   // $VF: Could not create synchronized statement, marking monitor enters and exits
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy SettingVisibility the class file (if you have the rights to distribute it!)
   private static void add(String text, oq.qq type, String glyph, Identifier texture, ItemStack item, long ms) {
      List v7 = entries;
      synchronized (entries){} // $VF: monitorenter 
      int v9 = -2088174800;

      try {
         List v14 = entries;
         oq.pq temp1 = new oq.pq;

         temp1./* $VF: Unable to resugar constructor */<init>(text, type, glyph, texture, item, ms);
         v14.add(temp1);
         // $VF: monitorexit
      } finally {
         // $VF: monitorexit
      }
   }

   @Environment(EnvType.CLIENT)
   public static class pq {
      public final oq.qq type;
      public final String iconGlyph;
      public boolean removing;
      private final long created;
      public final Identifier effectTexture;
      public final String text;
      private final long duration;
      public final ItemStack itemStack;
      public final Animation anim = new Animation();

      public boolean isExpired() {
         long result = System.currentTimeMillis();
         int v2 = 1751930919;
         int temp1 = v2 * (v2 + 1);

         result -= this.created;

         return result >= this.duration;
      }

      pq(String text, oq.qq type, String iconGlyph, Identifier effectTexture, ItemStack itemStack, long duration) {
         this.removing = false;
         this.created = System.currentTimeMillis();
         this.text = text;
         this.type = type;
         this.iconGlyph = iconGlyph;
         this.effectTexture = effectTexture;
         this.itemStack = itemStack;
         this.duration = duration;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static enum qq {
      INFO,
      WARNING,
      MODULE,
      EFFECT;
      // $VF: synthetic field
      private static final oq.qq[] $VALUES = m1607();
   }
}
