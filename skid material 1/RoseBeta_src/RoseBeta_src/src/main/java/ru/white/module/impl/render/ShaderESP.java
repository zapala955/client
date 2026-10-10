package ru.white.module.impl.render;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Sf;
import ru.white.utils.misc.Tf;
import ru.white.module.setting.ModeSetting;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.ft;
import ru.white.utils.render.ht;
import ru.white.utils.render.gl.Jf;
import ru.white.utils.render.gl.Kf;
import ru.white.utils.render.gl.Lf;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Shader ESP",
   category = Category.RENDER,
   desc = "Шейдерный ESP с ореолом свечения для сущностей"
)
@Environment(EnvType.CLIENT)
public class ShaderESP extends Module implements jf {
   public SliderSetting shimmerBrightness;
   public BooleanSetting outline;
   public ModeSetting typeColor;
   public BooleanSetting shimmer;
   public SliderSetting outlineWhite;
   public SliderSetting opacity;
   public SliderSetting innerGlow;
   private final lf f760;
   private static ok instance;
   public sf previewButton = lf.button(this);
   public Framebuffer customFboReference;
   public BooleanSetting ignoreNaked;
   public SliderSetting shimmerWidth;
   public SliderSetting outlineWidth;
   public SliderSetting saturation;
   public BooleanSetting items;
   public SliderSetting glowStrength;
   public tf tintColor;
   public BooleanSetting localPlayer = new BooleanSetting(this, "Локальный игрок", true);
   public ModeSetting outlineMode;
   public BooleanSetting friendColor;
   public SliderSetting glowFalloff;
   public BooleanSetting pulse;
   public BooleanSetting players = new BooleanSetting(this, "Игроки", true);
   public SliderSetting outlineStrength;
   public BooleanSetting mobs;
   public BooleanSetting fill;
   private Entity f761;
   public SliderSetting fillOpacity;
   public SliderSetting shimmerPeriod;
   public SliderSetting pulseSpeed;
   public BooleanSetting glow;
   public SliderSetting glowRadius;
   public SliderSetting pulseAmount;
   public tf friendTint;
   private static final Set<Entity> allTargets = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

   @EventTarget
   public void onTick(EventRenderModel event) {
      allTargets.clear();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         for (Entity v3 : mc.field_1687.method_18112()) {
            if (this.m835(v3)) {
               allTargets.add(v3);
            }
         }

         if (this.f761 != null && !this.f761.method_31481()) {
            allTargets.add(this.f761);
         }
      }
   }

   @Override
   public void previewTick(kf ctx) {

      this.f761 = ctx.dummy();
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      allTargets.clear();

      if (this.isEnabled()) {
         ht.getInstance().invalidate();
      }
   }

   private static boolean m836(PlayerEntity p) {
      ItemStack result = p.method_6118(EquipmentSlot.field_6169);

      if (result.method_7960()) {

         if (p.method_6118(EquipmentSlot.field_6174).method_7960()
            && p.method_6118(EquipmentSlot.field_6172).method_7960()
            && p.method_6118(EquipmentSlot.field_6166).method_7960()) {
            return false;
         }
      }

      return true;
   }

   public int getColor() {
      return this.typeColor.is("Тема") ? up.getClientColor1(1) : this.tintColor.getValue();
   }

   private boolean m835(Entity entity) {
      if (entity instanceof PlayerEntity v2) {
         if (v2.method_5797() != null && v2.method_5797().getString().startsWith("Ghost_")) {
            return false;
         } else if (!this.players.getValue()) {
            return false;
         } else if (this.ignoreNaked.getValue() && !m836(v2)) {

            return false;
         } else {
            return true;
         }
      } else if (entity instanceof ClientPlayerEntity) {
         return this.localPlayer.getValue();
      } else if (entity instanceof LivingEntity) {
         return this.mobs.getValue();
      } else {
         return entity instanceof ItemEntity ? this.items.getValue() : false;
      }
   }

   @Override
   public boolean previewNeedsDummy() {
      return true;
   }

   public static boolean isEspTarget(Entity entity) {
      boolean result = allTargets.contains(entity);

      return result;
   }

   @Override
   public void previewStop() {

      this.f761 = null;
      allTargets.clear();
   }

   private void m834(ft.gt p) {
      p.color = this.getColor();
      p.friendEnabled = this.friendColor.getValue();

      p.friendColor = this.friendTint.getValue();
      p.opacity = this.opacity.getValue();
      p.saturation = this.saturation.getValue();
      p.glowEnabled = this.glow.getValue();
      p.glowRadius = this.glowRadius.getValue();
      p.glowStrength = this.glowStrength.getValue();
      p.glowFalloff = this.glowFalloff.getValue();
      p.fillEnabled = this.fill.getValue();
      p.fillOpacity = this.fillOpacity.getValue();
      p.innerGlow = this.innerGlow.getValue();
      p.outlineEnabled = this.outline.getValue();
      p.outlineMode = this.outlineMode.getIndex();
      p.outlineWidth = this.outlineWidth.getValue().intValue();
      p.outlineStrength = this.outlineStrength.getValue();
      p.outlineWhite = this.outlineWhite.getValue();

      p.pulseEnabled = this.pulse.getValue();
      p.pulseSpeed = this.pulseSpeed.getValue();
      p.pulseAmount = this.pulseAmount.getValue();
      p.shimmerEnabled = this.shimmer.getValue();
      p.shimmerWidth = this.shimmerWidth.getValue();
      p.shimmerPeriodSec = this.shimmerPeriod.getValue();
      p.shimmerBrightness = this.shimmerBrightness.getValue();
   }

   @Override
   public void previewSpawn(kf ctx) {
   }

   @Override
   public lf previewSettings() {
      int temp1 = v1 * (v1 + 1) % 2;

      if (temp1 != 0) {
         }

      return this.f760;
   }

   public void onRender3D() {
      if (mc.field_1687 != null) {

         if (mc.field_1724 != null) {
            if (allTargets.isEmpty()) {
               return;
            }

            Framebuffer v1 = mc.method_1522();
            if (v1 == null) {
               return;
            }

            Framebuffer v2 = this.customFboReference;
            if (v2 != null && v2.method_71639() != null) {
               ht v3 = ht.getInstance();
               this.m834(v3.params);
               v3.renderFromFbo(v2, v1.field_1482, v1.field_1481);
               return;
            }

            return;
         }
      }
   }

   public static Set<Entity> getAllTargets() {
      return allTargets;
   }

   public ShaderESP() {
      this.ignoreNaked = new BooleanSetting(this, "Игнорировать голых", false).setVisible(() -> {
         Boolean result = this.players.getValue();

         return result;
      });
      this.mobs = new BooleanSetting(this, "Мобы", true);
      this.items = new BooleanSetting(this, "Предметы", false);
      this.typeColor = new ModeSetting(this, "Режим цвета", "Тема", "Свой");
      this.tintColor = new tf(this, "Цвет", -52395).setVisible(() -> {
         Boolean result = this.typeColor.is("Свой");

         return result;
      });
      this.saturation = new SliderSetting(this, "Насыщенность", 1.15F, 0.0F, 2.0F, 0.05F);
      this.opacity = new SliderSetting(this, "Прозрачность", 1.0F, 0.05F, 1.0F, 0.01F);
      this.friendColor = new BooleanSetting(this, "Цвет друзей", true);
      this.friendTint = new tf(this, "Цвет друга", -11141291).setVisible(() -> this.friendColor.getValue());
      this.glow = new BooleanSetting(this, "Свечение", true);
      this.glowRadius = new SliderSetting(this, "Радиус свечения", 0.55F, 0.0F, 1.0F, 0.01F).setVisible(() -> {
         Boolean result = this.glow.getValue();

         return result;
      });
      this.glowStrength = new SliderSetting(this, "Сила свечения", 1.6F, 0.0F, 4.0F, 0.05F).setVisible(() -> {

         return this.glow.getValue();
      });
      this.glowFalloff = new SliderSetting(this, "Спад свечения", 1.1F, 0.2F, 4.0F, 0.05F)
         .setVisible(() -> this.glow.getValue());
      this.fill = new BooleanSetting(this, "Заливка", true);
      this.fillOpacity = new SliderSetting(this, "Плотность заливки", 0.45F, 0.0F, 1.5F, 0.01F)
         .setVisible(() -> {

            return this.fill.getValue();
         });
      this.innerGlow = new SliderSetting(this, "Внутреннее свечение", 0.6F, 0.0F, 2.0F, 0.05F)
         .setVisible(() -> {
            Boolean result = this.fill.getValue();

            return result;
         });
      this.outline = new BooleanSetting(this, "Обводка", true);
      this.outlineMode = new ModeSetting(this, "Тип обводки", "Снаружи", "Внутри", "Обе").setVisible(() -> {

         return this.outline.getValue();
      });
      this.outlineWidth = new SliderSetting(this, "Ширина обводки", 2.0F, 1.0F, 5.0F, 1.0F).setVisible(() -> {

         return this.outline.getValue();
      });
      this.outlineStrength = new SliderSetting(this, "Яркость обводки", 1.8F, 0.0F, 3.0F, 0.05F)
         .setVisible(() -> {

            return this.outline.getValue();
         });
      this.outlineWhite = new SliderSetting(this, "Белизна обводки", 0.45F, 0.0F, 1.0F, 0.01F)
         .setVisible(() -> {

            return this.outline.getValue();
         });
      this.pulse = new BooleanSetting(this, "Пульсация", false);
      this.pulseSpeed = new SliderSetting(this, "Скорость пульсации", 1.0F, 0.1F, 5.0F, 0.1F)
         .setVisible(() -> {

            int temp1 = v1 * (v1 + 1) % 2;

            if (temp1 != 0) {
               }

            return this.pulse.getValue();
         });
      this.pulseAmount = new SliderSetting(this, "Сила пульсации", 0.3F, 0.0F, 1.0F, 0.05F).setVisible(() -> {
         Boolean result = this.pulse.getValue();

         return result;
      });
      this.shimmer = new BooleanSetting(this, "Шиммер", false);
      this.shimmerWidth = new SliderSetting(this, "Ширина шиммера", 0.04F, 0.01F, 0.15F, 0.01F)
         .setVisible(() -> {
            Boolean result = this.shimmer.getValue();
            int temp1 = v1 * (v1 + 1) % 2;

            if (temp1 != 0) {
               }

            return result;
         });
      this.shimmerPeriod = new SliderSetting(this, "Период шиммера", 5.0F, 1.0F, 15.0F, 0.5F).setVisible(() -> {

         return this.shimmer.getValue();
      });
      this.shimmerBrightness = new SliderSetting(this, "Яркость шиммера", 0.8F, 0.0F, 2.0F, 0.05F)
         .setVisible(() -> {

            return this.shimmer.getValue();
         });
      this.f760 = lf.withoutInterval(this, 4.0F, 0.0F);
      instance = this;
   }

   public static ok getInstance() {

      return instance;
   }

   @Override
   protected void onDisable() {
      allTargets.clear();
      int result = v1 * (v1 + 1) % 2;

      if (result != 0) {
         }
   }

   @Override
   protected void onEnable() {
      ht.getInstance().invalidate();
   }

   @Override
   public void previewStart(kf ctx) {
      OtherClientPlayerEntity temp1 = ctx.dummy();

      this.f761 = temp1;
   }

   
}
