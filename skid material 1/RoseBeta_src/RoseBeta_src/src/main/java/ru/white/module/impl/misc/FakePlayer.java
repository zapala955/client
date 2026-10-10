package ru.white.module.impl.misc;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.Handler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import ru.white.event.EventTarget;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.Particles;
import ru.white.module.impl.render.TotemGhost;
import ru.white.utils.PacketUtil;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTrace;

@ModuleAnnotation(
   name = "Fake Player",
   desc = "Создает фейк игрока [Бан на SP / HW]",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class FakePlayer extends Module {
   public static OtherClientPlayerEntity fakePlayer;
   private int f955 = 0;
   private long f956 = 0L;

   @EventTarget
   public void onPacket(EventStep event) {
      if (mc.field_1724 != null && mc.field_1687 != null && fakePlayer != null) {
         if (event.isSend()) {
            if (event.getPacket() instanceof PlayerInteractEntityC2SPacket v2 && mc.field_1765 instanceof EntityHitResult v8) {
               Entity v10 = v8.method_17782();
               if (v10 != null) {

                  if (v10.method_5628() == fakePlayer.method_5628()) {
                     v2.method_34209(new Handler() {
                        public void method_34218() {
                           gm.this.m1232();

                           
                        }

                        public void method_34220(Hand hand, Vec3d pos) {
                        }

                        public void method_34219(Hand hand) {
                        }

                        
                     });
                  }
               }
            }

            if (event.getPacket() instanceof ExplosionS2CPacket v7) {
               this.m1238(v7);
            }
         }
      }
   }

   private void m1237(OtherClientPlayerEntity target) {
      mc.field_1724.method_5783(SoundEvents.field_14931, 1.0F, 1.0F);
      zk v2 = Client.get().moduleManager().get(zk.class);
      if (v2 != null) {
         v2.spawnGhost(target);
      }

      Particles v3 = Client.get().moduleManager().get(Particles.class);
      if (v3.totem.getValue()) {
         for (int v4 = 0; (float)v4 < v3.onTtSizs.getValue(); v4++) {
            double v5 = fq.randomValue(0.0, Math.PI * 2);
            double v7 = fq.randomValue(2.5, 4.0);
            double v9 = Math.cos(v5) * v7;
            double result = Math.sin(v5);

            double v11 = result * v7;
            double v13 = fq.randomValue(0.0, (double)(target.method_17682() / 2.0F));
            v3.spawnParticleTotem(
               v3.totemParticles,
               new Vec3d(
                  target.method_23317() + fq.randomValue(-0.2, 0.2),
                  target.method_23318() + (double)fq.randomValue(0.1F, target.method_17682()),
                  target.method_23321() + fq.randomValue(-0.2, 0.2)
               ),
               new Vec3d(v9, v13, v11)
            );
         }
      } else {
         mc.field_1713.method_3051(target, ParticleTypes.field_11220, 30);
      }

      target.method_6012();
      target.method_6033(4.0F);
      target.method_6073(12.0F);
      this.f955 = 1400;
   }

   private void m1238(ExplosionS2CPacket explosion) {
      Vec3d v2 = explosion.comp_2883();
      double v3 = fakePlayer.method_73189().method_1022(v2);

      if (!(v3 > 11.0)) {
         float v5 = (float)((1.0 - v3 / 11.0) * 12.0);
         this.m1236(fakePlayer, v5);
         mc.field_1724.method_5783((SoundEvent)SoundEvents.field_15152.comp_349(), 0.7F, 1.0F);
      }
   }

   private void m1236(OtherClientPlayerEntity target, float damage) {
      float v3 = target.method_6067();

      if (v3 > 0.0F) {
         float v4 = Math.min(v3, damage);
         target.method_6073(v3 - v4);
         damage -= v4;
      }

      if (damage > 0.0F) {

         float v9 = target.method_6032();
         float v5 = Math.max(target.method_6032() - damage, 0.5F);
         target.method_6033(v5);
      }

      target.field_6235 = 10;
      target.field_6008 = 10;
      if (!target.method_6059(StatusEffects.field_5924) || target.method_6112(StatusEffects.field_5924).method_5584() < 100) {
         target.method_6092(new StatusEffectInstance(StatusEffects.field_5924, 900, 1));
      }

      if (!target.method_6059(StatusEffects.field_5898) || target.method_6112(StatusEffects.field_5898).method_5584() < 20) {
         target.method_6092(new StatusEffectInstance(StatusEffects.field_5898, 100, 1));
      }

      if (target.method_6032() <= 1.0F && target.method_6079().method_31574(Items.field_8288)) {
         this.m1237(target);
      }
   }

   private float m1233(PlayerEntity attacker, LivingEntity target) {
      ItemStack result = attacker.method_6047();

      ItemStack v3 = result;
      double v4 = attacker.method_45325(EntityAttributes.field_23721);
      float v6 = attacker.method_7261(0.5F);
      float v7 = (float)(v4 * (double)(0.2F + v6 * v6 * 0.8F));
      boolean v8 = attacker.method_18798().field_1351 < -0.08 && !attacker.method_24828() && !attacker.method_5624();
      if (v8) {
         v7 *= 1.5F;
      }

      DynamicRegistryManager v9 = attacker.method_73183().method_30349();
      Registry v10 = v9.method_30530(RegistryKeys.field_41265);
      int v11 = v10.method_10223(Enchantments.field_9118.method_29177()).map(entry -> {

         return EnchantmentHelper.method_8225(entry, v3);
      }).orElse(0);
      if (v11 > 0) {
         float v12 = 0.5F + 0.5F * (float)v11;
         v7 += v12;
      }

      int v18 = v10.method_10223(Enchantments.field_9123.method_29177()).map(entry -> {

         return EnchantmentHelper.method_8225(entry, v3);
      }).orElse(0);
      if (v18 > 0 && this.m1234(target)) {
         float v21 = (float)v18;

         float v13 = 2.5F * v21;
         v7 += v13;
      }

      int v19 = v10.method_10223(Enchantments.field_9112.method_29177()).map(entry -> {

         Integer var10000x = EnchantmentHelper.method_8225(entry, v3);

         return var10000x;
      }).orElse(0);

      if (v19 > 0 && this.m1235(target)) {
         float v14 = 2.5F * (float)v19;
         v7 += v14;
      }

      return v7;
   }

   private boolean m1234(Entity e) {
      if (!(e instanceof ZombieEntity) && !(e instanceof SkeletonEntity)) {

         if (!(e instanceof WitherSkeletonEntity)
            && !(e instanceof WitherEntity)
            && !(e instanceof ZoglinEntity)
            && !(e instanceof ZombieHorseEntity)
            && !(e instanceof ZombieVillagerEntity)
            && !(e instanceof HuskEntity)
            && !(e instanceof StrayEntity)
            && !(e instanceof DrownedEntity)
            && !(e instanceof PhantomEntity)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public void onDisable() {
      this.onToggled(false);
      super.onDisable();
      
   }

   @EventTarget
   public void onRender(EventTrace event) {
      if (mc.field_1724 != null && mc.field_1687 != null && fakePlayer != null) {
         fakePlayer.method_36456(0.0F);
         fakePlayer.method_36457(0.0F);
         fakePlayer.method_5847(0.0F);
         fakePlayer.method_5636(0.0F);
         fakePlayer.method_74090(mc.field_1724.method_6068());
         if (this.f955 > 0) {

            this.f955--;

            if (this.f955 % (20) == 0) {
               fakePlayer.method_6033(Math.min(fakePlayer.method_6032() + 1.0F, 20.0F));
            }
         }
      }
   }

   private void m1232() {
      if (!(mc.field_1724.method_5858(fakePlayer) > 36.0)) {
         float v1 = this.m1233(mc.field_1724, fakePlayer);
         long v2 = System.currentTimeMillis();
         long result = v2 - this.f956;

         if (result >= -7428504770041729774L + 7428504770041729874L) {
            boolean v4 = mc.field_1724.method_18798().field_1351 < -0.08 && !mc.field_1724.method_24828() && !mc.field_1724.method_5624();
            mc.field_1724.method_5783(v4 ? SoundEvents.field_15016 : SoundEvents.field_14840, 1.0F, 1.0F);
            this.f956 = v2;
         }

         this.m1236(fakePlayer, v1);
      }
   }

   @Override
   public void onEnable() {
      this.onToggled(true);
      int temp1 = v1 + 1;

      super.onEnable();
   }

   public void onToggled(boolean active) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!active) {
            if (fakePlayer != null) {
               fakePlayer.method_31472();
               fakePlayer = null;
            }
         } else {
            GameProfile result = new GameProfile;

            result./* $VF: Unable to resugar constructor */<init>(
               UUID.fromString("32d0a964-137a-2c03-7cc9-df6700000000"), "ROSE_BOT"
            );
            GameProfile v2 = result;
            fakePlayer = new OtherClientPlayerEntity(mc.field_1687, v2);
            fakePlayer.method_5808(
               mc.field_1724.method_23317(),
               mc.field_1724.method_23318(),
               mc.field_1724.method_23321(),
               mc.field_1724.method_36454(),
               mc.field_1724.method_36455()
            );
            fakePlayer.method_6122(Hand.field_5808, new ItemStack(Items.field_22022));
            fakePlayer.method_6122(Hand.field_5810, new ItemStack(Items.field_8288));
            fakePlayer.method_6033(20.0F);
            fakePlayer.method_6073(0.0F);
            mc.field_1687.method_53875(fakePlayer);
         }
      }
   }

   private boolean m1235(Entity e) {
      if (!(e instanceof SpiderEntity)) {

         if (!(e instanceof CaveSpiderEntity)) {

            if (!(e instanceof SilverfishEntity) && !(e instanceof EndermiteEntity) && !(e instanceof BeeEntity)) {
               return false;
            }
         }
      }

      return true;
   }

   
}
