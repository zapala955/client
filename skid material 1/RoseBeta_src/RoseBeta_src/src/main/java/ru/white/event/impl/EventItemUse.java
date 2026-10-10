package ru.white.event.impl;

import java.util.regex.Pattern;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;
import ru.white.utils.misc.Rc;

@Environment(EnvType.CLIENT)
public class EventItemUse extends rc {
   private String text;

   @Generated
   public EventItemUse(String text) {
      this.text = text;
   }

   @Generated
   public void setText(String text) {
      this.text = text;
   }

   public void replaceRegex(String regex, String replaced) {
      if (this.text != null) {
         if (!this.text.isEmpty()) {
            this.text = this.text.replaceAll(regex, replaced);
            return;
         }

         
      }
   }

   @Generated
   public String getText() {

      return this.text;
   }

   public void replaceText(String protect, String replaced) {
      if (this.text != null) {

         boolean v8 = this.text.isEmpty();

         if (!v8 && protect != null && !protect.isEmpty()) {
            if (this.text.toLowerCase().contains(protect.toLowerCase())) {
               String v9 = this.text;
               String v3 = Pattern.quote(protect);
               this.text = v9.replaceAll("(?ui)" + v3, replaced);
            }

            return;
         }
      }
   }

   
}
