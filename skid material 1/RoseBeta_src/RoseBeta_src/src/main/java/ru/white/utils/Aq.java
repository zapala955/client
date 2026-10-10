package ru.white.utils;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFWNativeWin32;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Aq {
   public static void enableImmersiveDarkMode(long i_hwnd) {
      if (Platform.isWindows()) {
         long v2 = GLFWNativeWin32.glfwGetWin32Window(i_hwnd);
         HWND v4 = new HWND(new Pointer(v2));
         int v5 = 20;
         Memory v6 = new Memory(-5690894758402494166L + 5690894758402494170L);
         v6.setInt(0L, 1);

         aq.bq.INSTANCE.DwmSetWindowAttribute(v4, v5, v6, 4);
      }
   }

   @Environment(EnvType.CLIENT)
   public interface bq extends StdCallLibrary {
      aq.bq INSTANCE = (aq.bq)Native.loadLibrary("dwmapi", aq.bq.class);

      int DwmSetWindowAttribute(HWND v1, int v2, Pointer v3, int v4);
   }
}
