package ru.white.command;

import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public abstract class Command {
   private final String description;
   private final String name;
   private final String usage;

   public abstract void execute(String[] v1);

   public List<String> getSuggestions(String subPrefix) {
      List result = Collections.emptyList();
      int temp1 = v2 * (v2 + 1) % 2;

      if (temp1 != 0) {
         }

      return result;
   }

   public String getDescription() {
      return this.description;
   }

   public String getName() {
      return this.name;
   }

   public String getUsage() {

      return this.usage;
   }

   public Command(String name, String usage, String description) {
      this.name = name;
      this.usage = usage;
      this.description = description;
   }

   
}
