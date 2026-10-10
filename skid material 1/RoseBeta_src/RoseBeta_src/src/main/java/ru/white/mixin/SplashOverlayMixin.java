package ru.white.mixin;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({SplashOverlay.class})
public abstract class SplashOverlayMixin {
   @Shadow
   @Final
   @Mutable
   public static Identifier field_2483;
   @Shadow
   @Final
   private MinecraftClient field_18217;
   @Shadow
   @Final
   private ResourceReload field_17767;
   @Shadow
   @Final
   private Consumer<Optional<Throwable>> field_18218;
   @Shadow
   @Final
   private boolean field_18219;
   @Shadow
   private float field_17770;
   @Shadow
   private long field_17771;
   @Shadow
   private long field_18220;
   @Unique
   private static boolean textureLoaded = false;

   @Unique
   private static int withAlphaLocal(int color, int alpha) {
      return color & 16777215 | alpha << 24;
   }

   @Inject(
      method = {"<clinit>"},
      at = {@At("TAIL")}
   )
   private static void modifyLogoIdentifier(CallbackInfo ci) {
      field_2483 = Identifier.method_60655("godweer", "images/ui/pic/cute/cutie2.png");
   }

   @Unique
   private void ensureTextureLoaded() {
      if (!textureLoaded) {
         textureLoaded = true;

         try {
            InputStream is = SplashOverlayMixin.class.getResourceAsStream("/assets/godweer/images/ui/pic/cute/cutie2.png");
            if (is == null) {
               is = SplashOverlayMixin.class.getResourceAsStream("/assets/client/textures/cutie2.png");
            }

            if (is == null) {
               is = SplashOverlayMixin.class.getResourceAsStream("/assets/minecraft/textures/cutie2.png");
            }

            if (is != null) {
               NativeImage img = NativeImage.method_4309(is);
               NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "cutie2", img);
               this.field_18217.method_1531().method_4616(field_2483, tex);
               is.close();
            }
         } catch (Throwable v4) {
            v4.printStackTrace();
         }
      }
   }

   @Inject(
      method = {"method_25394"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void godweerRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
      this.ensureTextureLoaded();
      int width = context.method_51421();
      int height = context.method_51443();
      long time = Util.method_658();
      if (this.field_18219 && this.field_18220 == -1L) {
         this.field_18220 = time;
      }

      float f = this.field_17771 > -1L ? (float)(time - this.field_17771) / 1000.0F : -1.0F;
      float g = this.field_18220 > -1L ? (float)(time - this.field_18220) / 500.0F : -1.0F;
      int backgroundColor = ColorHelper.method_61324(255, 8, 10, 18);
      float opacity;
      if (f >= 0.0F) {
         if (this.field_18217.field_1755 != null) {
            this.field_18217.field_1755.method_47413(context, 0, 0, deltaTicks);
         }

         float progress = MathHelper.method_15363(f, 0.0F, 1.0F);
         float ease = 1.0F - progress;
         float smoothFactor = ease * ease * (3.0F - 2.0F * ease);
         int alpha = MathHelper.method_15340(MathHelper.method_15386(smoothFactor * 255.0F), 0, 255);
         if (alpha > 0) {
            context.method_71048();
            context.method_25294(0, 0, width, height, withAlphaLocal(backgroundColor, alpha));
         }

         opacity = smoothFactor;
      } else if (this.field_18219) {
         if (this.field_18217.field_1755 != null && g < 1.0F) {
            this.field_18217.field_1755.method_47413(context, mouseX, mouseY, deltaTicks);
         }

         int alpha = MathHelper.method_15384(MathHelper.method_15350((double)g, 0.15, 1.0) * 255.0);
         context.method_71048();
         context.method_25294(0, 0, width, height, withAlphaLocal(backgroundColor, alpha));
         opacity = MathHelper.method_15363(g, 0.0F, 1.0F);
      } else {
         context.method_71048();
         context.method_25294(0, 0, width, height, backgroundColor);
         opacity = 1.0F;
      }

      float rp = this.field_17767.method_18229();
      this.field_17770 = MathHelper.method_15363(this.field_17770 * 0.95F + rp * 0.050000012F, 0.0F, 1.0F);
      float centerX = (float)width / 2.0F;
      float centerY = (float)height / 2.0F;
      double d = Math.min((double)width * 0.75, (double)height) * 0.25;
      float expand = f >= 0.0F ? 1.0F + (1.0F - opacity) * 0.04F : 1.0F;
      float size = (float)(d * 1.5) * expand;
      float halfSize = size / 2.0F;
      int drawX = (int)(centerX - halfSize);
      int drawY = (int)(centerY - halfSize);
      int intSize = (int)size;
      int alphaValue = MathHelper.method_15340(MathHelper.method_15386(opacity * 255.0F), 0, 255);
      int tintColor = ColorHelper.method_61324(alphaValue, 255, 255, 255);
      if (alphaValue > 0) {
         try {
            context.method_25293(RenderPipelines.field_56883, field_2483, drawX, drawY, 0.0F, 0.0F, intSize, intSize, 540, 540, 540, 540, tintColor);
         } catch (Throwable v29) {
            context.method_25293(RenderPipelines.field_56839, field_2483, drawX, drawY, 0.0F, 0.0F, intSize, intSize, 540, 540, 540, 540, tintColor);
         }
      }

      if (f >= 1.0F) {
         this.field_18217.method_18502(null);
      }

      if (this.field_17771 == -1L && this.field_17767.method_18787() && (!this.field_18219 || g >= 1.0F)) {
         try {
            this.field_17767.method_18849();
            this.field_18218.accept(Optional.empty());
         } catch (Throwable v28) {
            this.field_18218.accept(Optional.of(v28));
         }

         this.field_17771 = Util.method_658();
         if (this.field_18217.field_1755 != null) {
            this.field_18217.field_1755.method_25423(context.method_51421(), context.method_51443());
         }
      }

      ci.cancel();
   }
}
