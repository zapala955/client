package ru.white.utils.system;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Hu {
   public static final yt gui = m1975("icongui", "icongui");
   public static final yt sf_regular = m1975("sf_regular", "sf_regular");
   private static final Map<String, String> FONT_REGISTRY = new LinkedHashMap<>();
   public static final yt icons5 = m1975("icons5", "icons5");
   public static final yt icons2 = m1975("icons2", "icons2");
   public static final yt category = m1975("category", "category");
   public static final yt icon = m1975("nightix", "nightix");
   public static final yt sfsemi = m1975("sfsemi", "sfsemi");
   public static final yt sf_medium = m1975("sf_medium", "sf_medium");
   public static final yt aeonik = m1975("aeonik", "aeonik");
   public static final yt sf_bold = m1975("sf_bold", "sf_bold");
   public static final yt mainmenuicons = m1975("mainmenuicons", "mainmenuicon");
   public static final yt nightix_2 = m1975("nightix_2", "nightix_2");

   private static yt m1975(String name, String path) {
      FONT_REGISTRY.put(name, path);
      yt result = new yt;

      result./* $VF: Unable to resugar constructor */<init>(name);
      return result;
   }

   private Hu() {
   }

   public static Map<String, String> getRegistry() {
      return FONT_REGISTRY;
   }

   
}
