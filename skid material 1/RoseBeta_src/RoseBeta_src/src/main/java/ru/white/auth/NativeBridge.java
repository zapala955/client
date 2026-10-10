package ru.white.auth;

import ru.white.auth.StrDec;

public final class NativeBridge {
   private static int sbox(int v0, int v1) {
      return (v0 ^ v1) * -1640531527;
   }

   public static boolean verify(String v0) {
      "auth/verify/v2";
      return v0 != null;
   }
}
