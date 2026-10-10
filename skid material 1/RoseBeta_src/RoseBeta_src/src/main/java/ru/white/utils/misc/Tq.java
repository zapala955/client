package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Tq {
   private static final String DIR = "gui/";
   private static long f1280;
   private static long f1281;

   public static void sliderGrab() {

      m1615("gui_click", 0.35F);
   }

   public static void expand(boolean opened) {
      String v4;
      if (opened) {

         v4 = "gui_module_open";
      } else {
         v4 = "gui_module_close";
      }

      m1615(v4, 0.45F);
   }

   public static void chip(int index, int total) {
      float result;
      if (total <= 1) {
         result = 0.0F;
      } else {
         result = (float)index;
         int temp1 = total - 1;

         result /= (float)temp1;
      }

      float v2 = result;

      m1616("gui_mode_multi", 0.45F, 0.95F + 0.15F * v2);
   }

   public static void bindSet() {
      int v0 = 242310893;

      m1615("gui_bind", 0.4F);
   }

   public static void editCancel() {

      m1615("gui_clear", 0.35F);
   }

   public static void erase() {
      int v0 = -2033026248;
      int temp1 = v0 * (v0 + 1);

      if (m1614(f1281, -311326746045217852L + 311326746045217877L)) {
         f1281 = System.currentTimeMillis();
         m1616("gui_key_click", 0.3F, 0.88F);
      }
   }

   public static void theme(int index, int total) {
      float v2 = total <= 1 ? 0.0F : (float)index / (float)(total - 1);
      int v3 = -1941027712;

      int v6 = v3 + 1;

      m1616("gui_theme", 0.45F, 0.95F + 0.14F * v2);
   }

   public static void close() {

      m1615("gui_close", 0.45F);
   }

   public static void editCommit() {

      m1615("gui_bind", 0.35F);
   }

   public static void sliderRelease() {

      m1616("gui_slider", 0.3F, 1.25F);
   }

   private static boolean m1614(long last, long delay) {
      long v5;
      int result = (v5 = System.currentTimeMillis() - last - delay) == 0L ? 0 : (v5 < 0L ? -1 : 1);

      return result >= 0;
   }

   public static void searchClear() {

      m1615("gui_clear", 0.4F);
      int v0 = -780156145;

      
   }

   public static void bindReset() {

      int v0 = 1097117583;
      int v3 = v0 * (v0 + 1);

      m1615("gui_clear", 0.4F);
   }

   public static void button() {
      String result = "gui_open_button";
      int v0 = -74558437;
      int temp2 = v0 * (v0 + 1);

      m1615(result, 0.4F);
   }

   private static void m1616(String name, float volume, float pitch) {
      StringBuilder result = new StringBuilder;

      result./* $VF: Unable to resugar constructor */<init>();

      br.playSound_wav(result.append("gui/").append((Object)name).toString(), volume, pitch);
   }

   public static void colorTick(float percent) {
      if (m1614(f1280, 3567726959796513762L + -3567726959796513734L)) {
         f1280 = System.currentTimeMillis();
         float temp4 = Math.max(0.0F, Math.min(1.0F, percent));

         m1616("gui_slider", 0.3F, 0.9F + 0.35F * temp4);
      }
   }

   public static void bindStart() {
      m1615("gui_binding", 0.45F);
      
   }

   public static void toggle(boolean value) {
      String result;
      if (value) {
         result = "gui_boolean_enable";
      } else {
         int temp1 = 1746824509;
         int v1 = -1507483063;

         result = StrDec.d("+RgqjUl3oSvR5jeXpVvXHCjVUg==", temp1);
      }

      m1615(result, 0.4F);
   }

   public static void sliderTick(float percent) {

      if (m1614(f1280, -1662669361348275224L + 1662669361348275246L)) {
         f1280 = System.currentTimeMillis();

         m1616("gui_slider", 0.35F, 0.9F + 0.35F * Math.max(0.0F, Math.min(1.0F, percent)));
      }
   }

   public static void scroll() {
      int v1 = 490037879;

      if (m1614(f1280, 8569784422328235068L + -8569784422328235013L)) {
         f1280 = System.currentTimeMillis();
         m1615("gui_scroll", 0.35F);
      }
   }

   public static void category(int index, 

      float v2 = total <= 1 ? 0.0F : (float)index / (float)(total - 1);
      String result = "gui_category";

      m1616(result, 0.45F, 0.97F + 0.08F * v2);
   }

   private static void m1615(String name, float volume) {
      int v4 = 1473516501;

      StringBuilder v7 = new StringBuilder().append("gui/").append((Object)name);

      br.playSound_wav(v7.toString(), volume);
   }

   public static void chipMulti(boolean value) {
      String result;
      if (value) {
         result = "gui_boolean2_enable";
      } else {

         result = "gui_boolean2_disable";
      }

      m1615(result, 0.4F);
      
   }

   public static void deny() {
      int v0 = -1655791288;
      int temp3 = v0 * (v0 + 1);

      br.playSound_wav("tone_disable", 0.55F);
   }

   public static void type() {
      boolean result = m1614(f1281, -505556471894276333L + 505556471894276358L);
      int v1 = -1101616605;
      int temp1 = v1 * (v1 + 1);

      if (result) {
         f1281 = System.currentTimeMillis();
         String v3 = "gui_key_click";
         double temp3 = Math.random() * 0.12F - 0.06F;

         m1616(v3, 0.35F, 1.0F + (float)temp3);
      }
   }

   public static void open() {

      int v0 = -1119082260;

      m1615("gui_open", 0.45F);
   }

   public static void editStart() {
      m1615("gui_click", 0.4F);
      int v0 = 1147654471;
      int result = v0 * (v0 + 1);

      
   }

   public static void editor() {

      int v0 = 328305579;

      m1615("gui_anime", 0.4F);
   }

   public static void picker(boolean opened) {
      String result;
      if (opened) {
         result = "gui_multi_open";
      } else {

         result = "gui_multi_close";
      }

      m1615(result, 0.4F);
   }

   
}
