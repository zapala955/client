package ru.white.utils;

import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public enum SoundUtil {
   KEY_9("9", 57),
   KEY_F("F", 70),
   KEY_F12("F12", 301),
   KEY_GRAVE("GRAVE", 96),
   KEY_SPACE("SPACE", 32),
   KEY_EQUAL("EQUAL", 61),
   KEY_DOWN("DOWN", 264),
   KEY_COMMA("COMMA", 44),
   MOUSE_3("MS3", 2),
   KEY_KP_EQUAL("KPEQUAL", 336),
   KEY_SCROLL_LOCK("SCROLLLOCK", 281),
   KEY_F20("F20", 309),
   KEY_UP("UP", 265),
   KEY_WORLD_2("WORLD2", 162),
   KEY_KP_DIVIDE("KPDIVIDE", 331),
   KEY_PAGE_UP("PAGEUP", 266),
   KEY_KP_4("NUMPAD4", 324),
   KEY_C("C", 67),
   KEY_HOME("HOME", 268),
   KEY_V("V", 86),
   KEY_RIGHT_SUPER("RSUPER", 347),
   KEY_ENTER("ENTER", 257),
   MOUSE_RIGHT("MOUSERIGHT", 1),
   KEY_F23("F23", 312),
   KEY_P("P", 80),
   KEY_R("R", 82),
   KEY_SLASH("SLASH", 47),
   KEY_X("X", 88),
   KEY_N("N", 78),
   KEY_Z("Z", 90),
   KEY_L("L", 76),
   KEY_D("D", 68),
   KEY_RIGHT_SHIFT("RSHIFT", 344),
   KEY_F4("F4", 293),
   KEY_F15("F15", 304),
   MOUSE_7("MS7", 6),
   KEY_W("W", 87),
   KEY_KP_7("NUMPAD7", 327),
   KEY_7("7", 55),
   KEY_0("0", 48),
   KEY_8("8", 56),
   KEY_4("4", 52),
   MOUSE_4("MS4", 3),
   KEY_LEFT_SHIFT("LSHIFT", 340),
   KEY_PAGE_DOWN("PAGEDOWN", 267),
   KEY_F3("F3", 292),
   KEY_INSERT("INSERT", 260),
   KEY_H("H", 72),
   KEY_F13("F13", 302),
   KEY_KP_6("NUMPAD6", 326),
   KEY_END("END", 269),
   KEY_E("E", 69),
   KEY_LEFT("LEFT", 263),
   KEY_F24("F24", 313),
   MOUSE_MIDDLE("MOUSEMIDDLE", 2),
   KEY_3("3", 51),
   KEY_LEFT_CONTROL("LCONTROL", 341),
   KEY_F21("F21", 310),
   KEY_BACKSLASH("BACKSLASH", 92),
   KEY_KP_DECIMAL("KPDECIMAL", 330),
   KEY_Q("Q", 81),
   KEY_B("B", 66),
   KEY_RIGHT("RIGHT", 262),
   KEY_KP_ADD("KPADD", 334),
   KEY_M("M", 77),
   KEY_K("K", 75),
   KEY_S("S", 83),
   MOUSE_LEFT("MOUSELEFT", 0),
   KEY_RIGHT_CONTROL("RCONTROL", 345),
   KEY_KP_3("NUMPAD3", 323),
   MOUSE_5("MS5", 4),
   KEY_G("G", 71),
   MOUSE_LAST("MOUSELAST", 7),
   KEY_KP_ENTER("KPENTER", 335),
   KEY_J("J", 74),
   MOUSE_1("MS1", 0),
   MOUSE_6("MS6", 5),
   KEY_PRINT_SCREEN("PRINTSCREEN", 283),
   KEY_LEFT_SUPER("LSUPER", 343),
   KEY_1("1", 49),
   KEY_F16("F16", 305),
   KEY_T("T", 84),
   KEY_O("O", 79),
   KEY_KP_5("NUMPAD5", 325),
   KEY_MINUS("MINUS", 45),
   KEY_ESCAPE("ESCAPE", 256),
   KEY_F19("F19", 308),
   KEY_NUM_LOCK("NUMLOCK", 282),
   MOUSE_2("MS2", 1),
   KEY_F25("F25", 314),
   KEY_F8("F8", 297),
   KEY_KP_8("NUMPAD8", 328),
   KEY_6("6", 54),
   KEY_F1("F1", 290),
   KEY_F6("F6", 295),
   KEY_F9("F9", 298),
   KEY_F7("F7", 296),
   KEY_BACKSPACE("BACKSPACE", 259),
   KEY_KP_0("NUMPAD0", 320),
   KEY_F14("F14", 303),
   KEY_F17("F17", 306),
   KEY_LEFT_BRACKET("LBRACKET", 91),
   KEY_2("2", 50),
   KEY_APOSTROPHE("APOSTROPHE", 39),
   KEY_MENU("MENU", 348),
   KEY_CAPS_LOCK("CAPSLOCK", 280),
   KEY_F22("F22", 311),
   KEY_F11("F11", 300),
   KEY_NONE("NONE", -1),
   KEY_KP_9("NUMPAD9", 329),
   KEY_U("U", 85),
   KEY_F10("F10", 299),
   KEY_F2("F2", 291),
   KEY_F18("F18", 307),
   KEY_LEFT_ALT("LALT", 342),
   KEY_Y("Y", 89),
   MOUSE_8("MS8", 7),
   KEY_5("5", 53),
   KEY_KP_MULTIPLY("KPMULTIPLY", 332),
   KEY_KP_SUBTRACT("KPSUBTRACT", 333),
   KEY_F5("F5", 294),
   KEY_PERIOD("PERIOD", 46),
   KEY_DELETE("DELETE", 261),
   KEY_RIGHT_ALT("RALT", 346),
   KEY_KP_1("NUMPAD1", 321),
   KEY_LAST("LAST", 348),
   KEY_RIGHT_BRACKET("RBRACKET", 93),
   KEY_I("I", 73),
   KEY_WORLD_1("WORLD1", 161),
   KEY_KP_2("NUMPAD2", 322),
   KEY_SEMICOLON("SEMICOLON", 59),
   KEY_TAB("TAB", 258),
   KEY_A("A", 65),
   KEY_PAUSE("PAUSE", 284);
   private static final Map<String, SoundUtil> KEY_NAME_MAP = new HashMap<>();
   private final String name;
   private static final Map<Integer, SoundUtil> KEY_CODE_MAP = new HashMap<>();
   private final int key;
   // $VF: synthetic field
   private static final SoundUtil[] $VALUES = m1596();

   public static SoundUtil findByKeyCode(int keyCode) {
      Map result = KEY_CODE_MAP;
      Integer temp1 = keyCode;

      return result.getOrDefault(temp1, KEY_NONE);
   }

   @Generated
   public String getName() {

      return this.name;
   }

   public static String keyName(int keyCode) {
      int v2 = -1248148044;

      int temp2 = v2 + 1;

      SoundUtil v1 = KEY_CODE_MAP.getOrDefault(keyCode, KEY_NONE);
      return v1.name;
   }

   @Generated
   private SoundUtil(final String name, final int key) {
      this.name = name;
      this.key = key;
   }

   public static int keyCode(String keyName) {
      Map result = KEY_NAME_MAP;
      String temp1 = keyName.toLowerCase();

      SoundUtil v4 = result.getOrDefault(temp1, KEY_NONE);

      SoundUtil v1 = v4;
      return v1.key;
   }

   public boolean isKey(int keyCode) {
      int temp1 = this.getKey();

      return keyCode == temp1;
   }

   @Generated
   public int getKey() {
      int v1 = -378527719;

      return this.key;
   }

   @Override
   public String toString() {

      return this.name;
   }

   static {
      for (SoundUtil v3 : values()) {
         KEY_CODE_MAP.put(v3.key, v3);
         KEY_NAME_MAP.put(v3.name.toLowerCase(), v3);
      }
   }
}
