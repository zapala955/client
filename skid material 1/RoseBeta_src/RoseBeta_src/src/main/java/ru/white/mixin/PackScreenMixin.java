package ru.white.mixin;

import java.io.File;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.Builder;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.white.module.impl.misc.UnHook;

@Environment(EnvType.CLIENT)
@Mixin({PackScreen.class})
public abstract class PackScreenMixin {
   @Unique
   private static final Logger LOGGER = LoggerFactory.getLogger(PackScreenMixin.class);
   @Unique
   private static final Text OPEN_FOLDER = Text.method_43471("pack.openFolder");
   @Unique
   private static final Text FOLDER_INFO = Text.method_43471("pack.folderInfo");
   @Shadow
   private Path field_25474;

   @Redirect(
      method = {"method_25426"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4185$class_7840;method_46431()Lnet/minecraft/class_4185;"
      )
   )
   private ButtonWidget redirectOpenFolderButton(Builder builder) {
      Text builderMessage = ((MessageAccessor)builder).getMessage();
      return builderMessage.equals(OPEN_FOLDER) ? ButtonWidget.method_46430(OPEN_FOLDER, button -> {
         File folderToOpen;
         if (pm.unhooked && pm.resourcePackFolder != null) {
            folderToOpen = pm.resourcePackFolder;
         } else {
            folderToOpen = this.field_25474.toFile();
         }

         if (!folderToOpen.exists()) {
            folderToOpen.mkdirs();
         }

         LOGGER.info("Opening folder: {}", folderToOpen.getPath());
         Util.method_668().method_672(folderToOpen);
      }).method_46436(Tooltip.method_47407(FOLDER_INFO)).method_46431() : builder.method_46431();
   }
}
