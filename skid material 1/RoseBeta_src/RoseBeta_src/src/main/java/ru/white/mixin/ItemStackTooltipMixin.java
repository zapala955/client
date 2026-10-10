package ru.white.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin({ItemStack.class})
public abstract class ItemStackTooltipMixin {
   @Unique
   private static final Pattern PRICE_PATTERN = Pattern.compile("Цен[аaAАыЫ]?:?\\s*([\\d,\\s\\.]+)", 2);

   @Inject(
      method = {"method_7950"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void appendUnitPrice(TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {
      try {
         MinecraftClient EventItemUse = MinecraftClient.method_1551();
         if (!(mc.field_1755 instanceof GenericContainerScreen)) {
            return;
         }

         ItemStack stack = (ItemStack)this;
         if (stack.method_7960() || stack.method_7947() <= 1) {
            return;
         }

         int totalPrice = parsePriceFromLore(stack);
         if (totalPrice <= 0) {
            return;
         }

         int unitPrice = totalPrice / stack.method_7947();
         if (unitPrice <= 0) {
            unitPrice = 1;
         }

         List<Text> tooltip = new ArrayList<>((Collection<? extends Text>)cir.getReturnValue());
         int priceLineIndex = findPriceLineIndex(tooltip);
         Text unitLine = Text.method_43470("")
            .method_10852(Text.method_43470("$ ").method_27692(Formatting.field_1077))
            .method_10852(Text.method_43470("За 1 шт: ").method_27692(Formatting.field_1068))
            .method_10852(Text.method_43470(formatPrice(unitPrice)).method_27692(Formatting.field_1060));
         if (priceLineIndex >= 0 && priceLineIndex + 1 <= tooltip.size()) {
            tooltip.add(priceLineIndex + 1, unitLine);
         } else {
            tooltip.add(unitLine);
         }

         cir.setReturnValue(tooltip);
      } catch (Exception v12) {
      }
   }

   @Unique
   private static int parsePriceFromLore(ItemStack stack) {
      LoreComponent loreComp = (LoreComponent)stack.method_58694(DataComponentTypes.field_49632);
      if (loreComp == null) {
         return 0;
      } else {
         for (Text text : loreComp.comp_2400()) {
            String line = Formatting.method_539(text.getString());
            if (line != null) {
               Matcher m = PRICE_PATTERN.matcher(line);
               if (m.find()) {
                  try {
                     String priceStr = m.group(1).replaceAll("[,\\s\\.]", "");
                     return Integer.parseInt(priceStr);
                  } catch (NumberFormatException v7) {
                  }
               }
            }
         }

         return 0;
      }
   }

   @Unique
   private static int findPriceLineIndex(List<Text> tooltip) {
      for (int i = 0; i < tooltip.size(); i++) {
         String line = Formatting.method_539(tooltip.get(i).getString());
         if (line != null) {
            Matcher m = PRICE_PATTERN.matcher(line);
            if (m.find()) {
               return i;
            }

            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("цена") || lowerLine.contains("price") || lowerLine.contains("цен")) {
               return i;
            }
         }
      }

      return -1;
   }

   @Unique
   private static String formatPrice(int price) {
      String value = String.valueOf(price);
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < value.length(); i++) {
         if (i > 0 && (value.length() - i) % 3 == 0) {
            builder.append('.');
         }

         builder.append(value.charAt(i));
      }

      return builder.toString();
   }
}
