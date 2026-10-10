package ru.white.utils.discord;

import com.sun.jna.Structure;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class An extends Structure {
   public String largeImageText;
   public String matchSecret;
   public String smallImageKey;
   public int partyMax;
   public String partyId;
   public String button_url_2;
   public String largeImageKey;
   public String partyPrivacy;
   public String details;
   public int partySize;
   public String smallImageText;
   public String state;
   public String button_label_1;
   public String button_url_1;
   public String button_label_2;
   public long endTimestamp;
   public int instance;
   public long startTimestamp;
   public String spectateSecret;
   public String joinSecret;

   protected List<String> getFieldOrder() {
      String[] result = new String[20];
      result[0] = "state";
      result[1] = "details";
      result[2] = "startTimestamp";
      result[3] = "endTimestamp";

      result[4] = "largeImageKey";
      result[5] = "largeImageText";
      result[6] = "smallImageKey";
      result[7] = "smallImageText";
      result[8] = "partyId";
      result[9] = "partySize";
      result[10] = "partyMax";
      result[11] = "partyPrivacy";
      result[12] = "matchSecret";
      result[13] = "joinSecret";
      result[14] = "spectateSecret";
      result[15] = "button_label_1";
      result[16] = "button_url_1";
      result[17] = "button_label_2";
      int temp2 = 18;
      int temp4 = -1372814144;

      result[temp2] = StrDec.d("AfEFYLmfo/Q7AjQ8", temp4);
      result[19] = "instance";
      return Arrays.asList(result);
   }

   public An() {
      this.setStringEncoding("UTF-8");
   }

   @Environment(EnvType.CLIENT)
   public static class bn {
      private final an f1004 = new an();

      public an.bn setSmallImage(String v1) {
         an.bn result = this.setSmallImage(v1, "");

         return result;
      }

      public an.bn setSecrets(String v1, String v2, String v3) {
         if (this.f1004.button_label_1 != null) {

            if (this.f1004.button_label_1.isEmpty()) {
               return this;
            }
         }

         if (this.f1004.button_label_2 == null || !this.f1004.button_label_2.isEmpty()) {

            this.f1004.matchSecret = v1;
            this.f1004.joinSecret = v2;
            this.f1004.spectateSecret = v3;
         }

         return this;
      }

      public an.bn setEndTimestamp(long v1) {
         int v3 = -494709457;
         int temp1 = v3 * (v3 + 1);

         this.f1004.endTimestamp = v1;
         return this;
      }

      public an.bn setState(String v1) {
         if (v1 != null && !v1.isEmpty()) {

            this.f1004.state = v1.substring(0, Math.min(v1.length(), 128));
         }

         return this;
      }

      public an.bn setInstance(boolean v1) {
         if (this.f1004.button_label_1 == null || !this.f1004.button_label_1.isEmpty()) {
            if (this.f1004.button_label_2 != null) {

               if (this.f1004.button_label_2.isEmpty()) {
                  return this;
               }
            }

            this.f1004.instance = v1 ? 1 : 0;
         }

         return this;
      }

      public an.bn setEndTimestamp(OffsetDateTime v1) {
         an result = this.f1004;
         long temp1 = v1.toEpochSecond();
         int v2 = -250617110;
         int temp2 = v2 * (v2 + 1) % 2;

         if (temp2 != 0) {
            }

         result.endTimestamp = temp1;
         return this;
      }

      public an.bn setButtons(List<mn> buttons) {
         if (buttons != null && !buttons.isEmpty()) {
            int v2 = Math.min(buttons.size(), 2);
            this.f1004.button_label_1 = ((mn)buttons.get(0)).getLabel();
            this.f1004.button_url_1 = ((mn)buttons.get(0)).getUrl();
            if (v2 == 2) {
               this.f1004.button_label_2 = ((mn)buttons.get(1)).getLabel();

               this.f1004.button_url_2 = ((mn)buttons.get(1)).getUrl();
            }
         }

         return this;
      }

      public an.bn setStartTimestamp(long v1) {
         this.f1004.startTimestamp = v1;
         int v3 = -1024658393;
         int result = v3 * (v3 + 1);

         return this;
      }

      public an.bn setButtons(mn v1, mn v2) {

         an.bn result = this.setButtons(Arrays.asList(v1, v2));
         int v3 = 1902156807;
         int v6 = v3 + 1;

         return result;
      }

      public an.bn setButtons(mn v1) {
         int v2 = -915636816;

         return this.setButtons(Collections.singletonList(v1));
      }

      public an.bn setLargeImage(String v1) {

         an.bn result = this.setLargeImage(v1, "");
         int v2 = 681661860;

         return result;
      }

      public an.bn setLargeImage(String v1, String v2) {
         this.f1004.largeImageKey = v1;

         this.f1004.largeImageText = v2;
         return this;
      }

      public an.bn setDetails(String v1) {
         if (v1 != null) {

            if (!v1.isEmpty()) {
               this.f1004.details = v1.substring(0, Math.min(v1.length(), 128));
            }
         }

         return this;
      }

      public an build() {
         int v1 = 55819628;

         return this.f1004;
      }

      public an.bn setSmallImage(String v1, String v2) {
         this.f1004.smallImageKey = v1;
         this.f1004.smallImageText = v2;

         return this;
      }

      public an.bn setSecrets(String v1, String v2) {
         if (this.f1004.button_label_1 != null) {

            if (this.f1004.button_label_1.isEmpty()) {
               return this;
            }
         }

         if (this.f1004.button_label_2 != null) {

            if (this.f1004.button_label_2.isEmpty()) {
               return this;
            }
         }

         this.f1004.joinSecret = v1;
         this.f1004.spectateSecret = v2;
         return this;
      }

      public an.bn setStartTimestamp(OffsetDateTime v1) {

         this.f1004.startTimestamp = v1.toEpochSecond();
         return this;
      }

      
   }
}
