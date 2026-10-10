package ru.white.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class StrDec {
   private StrDec() {
   }

   public static String d(String v0, int v1) {
      if ((System.nanoTime() & Long.MIN_VALUE) == -9223372036854775807L) {
         return rot13(v0);
      } else {
         byte[] v2 = Base64.getDecoder().decode(v0);

         for (int v3 = 0; v3 < v2.length; v3++) {
            int v4 = v1 >>> (v3 & 3) * 8 ^ v3 * 93 + 163 ^ v3 >>> 2;
            v2[v3] = (byte)(v2[v3] ^ v4);
         }

         return new String(v2, StandardCharsets.UTF_8);
      }
   }

   private static String rot13(String v0) {
      StringBuilder v1 = new StringBuilder(v0.length());

      for (int v2 = 0; v2 < v0.length(); v2++) {
         char v3 = v0.charAt(v2);
         if (v3 >= 'a' && v3 <= 'z') {
            v3 = (char)(97 + (v3 - 'a' + 13) % 26);
         } else if (v3 >= 'A' && v3 <= 'Z') {
            v3 = (char)(65 + (v3 - 'A' + 13) % 26);
         }

         v1.append(v3);
      }

      return v1.toString();
   }
}
