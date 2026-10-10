package ru.white.utils.media;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import ru.white.auth.StrDec;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public class Lr implements IMinecraft {
   public boolean touchingWater;
   public boolean isJumping;
   public Vec3d velocity;
   public int jumpingCooldown;
   private int f1306 = 0;
   public final PlayerEntity player;
   public float yaw;
   public boolean verticalCollision;
   public Vec3d pos;
   private final Object2DoubleMap<TagKey<Fluid>> f1304;
   public boolean isFallFlying;
   public boolean horizontalCollision;
   public float fallDistance;
   public boolean submergedInWater;
   private boolean f1307 = false;
   private final HashSet<TagKey<Fluid>> f1305;
   public float pitch;
   public boolean onGround;
   public Box boundingBox;
   public boolean sprinting;
   public boolean isSwimming;
   private static final double STEP_HEIGHT = 0.5;
   public final lr.mr input;

   public lr clone() {
      lr result = new lr;

      result./* $VF: Unable to resugar constructor */<init>(
         this.player,
         this.input,
         this.pos,
         this.velocity,
         this.boundingBox,
         this.yaw,
         this.pitch,
         this.sprinting,
         this.fallDistance,
         this.jumpingCooldown,
         this.isJumping,
         this.isFallFlying,
         this.onGround,
         this.horizontalCollision,
         this.verticalCollision,
         this.touchingWater,
         this.isSwimming,
         this.submergedInWater,
         new Object2DoubleArrayMap(this.f1304),
         new HashSet<>(this.f1305)
      );
      return result;
   }

   private double m1690() {
      int v1 = -693736383;
      int temp3 = v1 + 1;

      return this.pos.field_1351 + (double)this.player.method_5751();
   }

   public boolean isInLava() {
      boolean v3;
      if (this.f1304.getDouble(FluidTags.field_15518) > 0.0) {

         v3 = true;
      } else {
         v3 = false;
      }

      return v3;
   }

   private double m1691(TagKey<Fluid> tag) {

      return this.f1304.getDouble(tag);
   }

   public BlockState getState(BlockPos pos) {

      return this.player.method_73183().method_8320(pos);
   }

   public boolean isClimbing() {
      BlockPos v1 = this.posToBlockPos(this.pos);
      BlockState v2 = this.getState(v1);
      if (v2.method_26164(BlockTags.field_22414)) {

         return true;
      } else {
         boolean result;
         if (v2.method_26204() instanceof TrapdoorBlock && this.m1674(v1, v2)) {

            result = true;
         } else {
            result = false;
            
         }

         return result;
      }
   }

   private StatusEffectInstance m1696(RegistryEntry<StatusEffect> effect) {
      StatusEffectInstance v2 = this.player.method_6112(effect);
      if (v2 != null) {
         if (v2.method_5584() >= this.f1306) {
            return v2;
         }

         
      }

      return null;
   }

   private void m1670(Vec3d movement) {
      Vec3d v2 = this.m1675(movement);
      Vec3d v3 = this.m1671(v2);
      if (v3.method_1027() > 1.0E-7) {
         this.pos = this.pos.method_1019(v3);
         this.boundingBox = this.player.field_18065.method_30757(this.pos);
      }

      boolean v4 = !MathHelper.method_20390(movement.field_1352, v3.field_1352);
      boolean v5 = !MathHelper.method_20390(movement.field_1350, v3.field_1350);
      this.horizontalCollision = v4 || v5;
      this.verticalCollision = movement.field_1351 != v3.field_1351;
      this.onGround = this.verticalCollision && movement.field_1351 < 0.0;
      if (!this.m1686()) {
         this.m1687();
      }

      if (this.onGround) {
         this.m1672();
      } else if (movement.field_1351 < 0.0) {
         this.fallDistance = this.fallDistance - (float)movement.field_1351;
      }

      Vec3d v6 = this.velocity;
      if (this.horizontalCollision || this.verticalCollision) {
         Vec3d temp1 = new Vec3d;
         double temp3 = v4 ? 0.0 : v6.field_1352;
         double temp4 = this.onGround ? 0.0 : v6.field_1351;

         temp1./* $VF: Unable to resugar constructor */<init>(temp3, temp4, v5 ? 0.0 : v6.field_1350);
         this.velocity = temp1;
      }
   }

   private boolean m1686() {

      return this.touchingWater;
   }

   private boolean m1674(BlockPos pos, BlockState state) {
      if (!(Boolean)state.method_11654(TrapdoorBlock.field_11631)) {
         return false;
      } else {
         BlockState v3 = this.player.method_73183().method_8320(pos.method_10074());
         boolean result = v3.method_27852(Blocks.field_9983);

         return result && ((Direction)v3.method_11654(LadderBlock.field_11253)).equals(state.method_11654(TrapdoorBlock.field_11177));
      }
   }

   private float m1678() {
      float result = 0.42F * this.m1680() + this.m1679();

      return result;
   }

   private void m1689() {
      this.submergedInWater = this.f1305.contains(FluidTags.field_15517);
      this.f1305.clear();
      double v1 = this.m1690() - 0.11111111F;
      if (this.player.method_5854() instanceof BoatEntity v4
         && !v4.method_5869()
         && v4.method_5829().field_1325 >= v1
         && v4.method_5829().field_1322 <= v1) {
         return;
      }

      BlockPos v10 = BlockPos.method_49637(this.pos.field_1352, v1, this.pos.field_1350);
      FluidState v5 = this.player.method_73183().method_8316(v10);
      int result = v10.method_10264();

      double v6 = (double)((float)result + v5.method_15763(this.player.method_73183(), v10));
      if (v6 > v1) {

         this.f1305.addAll(v5.method_40181().toList());
      }
   }

   private void m1683(TagKey<Fluid> fluidTag) {
      int v2 = 660924438;
      int temp2 = v2 * (v2 + 1);

      this.velocity = this.velocity.method_1031(0.0, 0.04F, 0.0);
   }

   public boolean hasStatusEffect(RegistryEntry<StatusEffect> effect) {
      int v3 = 1101607457;

      int temp2 = v3 + 1;

      StatusEffectInstance v2 = this.player.method_6112(effect);
      return v2 != null && v2.method_5584() >= this.f1306;
   }

   private Vec3d m1695(float pitch, float yaw) {
      float v3 = (float)((double)pitch * Math.PI / 180.0);
      double result = (double)(-yaw) * Math.PI;

      float v4 = (float)(result / 180.0);
      float v5 = MathHelper.method_15362((double)v4);
      float v6 = MathHelper.method_15374((double)v4);
      float v7 = MathHelper.method_15362((double)v3);
      float v8 = MathHelper.method_15374((double)v3);
      return new Vec3d((double)(v6 * v7), (double)(-v8), (double)(v5 * v7));
   }

   public static lr simulateLocalPlayer(int ticks) {
      lr v1 = fromClientPlayer(lr.mr.fromClientPlayer(mc.field_1724.field_3913.field_54155));

      for (int v2 = 0; v2 < ticks; v2++) {

         v1.tick();
      }

      return v1;
   }

   private boolean m1677() {
      int v1 = -122842265;
      int temp1 = v1 * (v1 + 1);

      return this.sprinting;
   }

   public void jump() {
      this.velocity = this.velocity.method_1031(0.0, (double)this.m1678() - this.velocity.field_1351, 0.0);
      if (this.m1677()) {
         float v1 = (float)Math.toRadians((double)this.yaw);
         this.velocity = this.velocity
            .method_1031((double)(-MathHelper.method_15374((double)v1)) * 0.2, 0.0, (double)MathHelper.method_15362((double)v1) * 0.2);
      }
   }

   private float m1669() {
      return 0.1F;
   }

   private void m1664(Vec3d movementInput) {
      if (this.isSwimming && !this.player.method_5765()) {
         double v2 = this.m1694().field_1351;
         double v4 = v2 < -0.2 ? 0.085 : 0.06;
         BlockPos v6 = new BlockPos(
            MathHelper.method_15357(this.pos.field_1352),
            MathHelper.method_15357(this.pos.field_1351 + 1.0 - 0.1),
            MathHelper.method_15357(this.pos.field_1350)
         );
         if (v2 <= 0.0 || this.input.playerInput.comp_3163() || !this.player.method_73183().method_8320(v6).method_26227().method_15769()) {
            this.velocity = this.velocity.method_1031(0.0, (v2 - this.velocity.field_1351) * v4, 0.0);
         }
      }

      double v21 = this.velocity.field_1351;
      double v22 = 0.08;
      boolean v23 = this.velocity.field_1351 <= 0.0;
      if (this.velocity.field_1351 <= 0.0 && this.hasStatusEffect(StatusEffects.field_5906)) {
         v22 = 0.01;
         this.m1672();
      }

      if (this.m1686() && this.player.method_29920()) {
         double v27 = this.pos.field_1351;
         float v30 = this.m1677() ? 0.9F : 0.8F;
         float v32 = 0.02F;
         float v34 = (float)this.getAttributeValue(EntityAttributes.field_51578);
         if (!this.onGround) {
            v34 *= 0.5F;
         }

         if (v34 > 0.0F) {
            v30 += (0.54600006F - v30) * v34 / 3.0F;
            v32 += (this.m1669() - v32) * v34 / 3.0F;
         }

         if (this.hasStatusEffect(StatusEffects.field_5900)) {
            v30 = 0.96F;
         }

         this.m1666(v32, movementInput);
         this.m1670(this.velocity);
         Vec3d v35 = this.velocity;
         if (this.horizontalCollision && this.isClimbing()) {
            v35 = new Vec3d(v35.field_1352, 0.2, v35.field_1350);
         }

         this.velocity = v35.method_18805((double)v30, 0.8, (double)v30);
         Vec3d v36 = this.player.method_26317(v22, v23, this.velocity);
         this.velocity = v36;
         if (this.horizontalCollision) {
            double v38 = v36.field_1351 + 0.6 - this.pos.field_1351 + v27;

            if (this.m1681(v36.field_1352, v38, v36.field_1350)) {
               this.velocity = new Vec3d(v36.field_1352, 0.3, v36.field_1350);
            }
         }
      } else if (this.isInLava() && this.player.method_29920()) {
         double v26 = this.pos.field_1351;
         this.m1666(0.02F, movementInput);
         this.m1670(this.velocity);
         if (this.m1691(FluidTags.field_15518) <= this.m1685()) {
            this.velocity = this.velocity.method_18805(0.5, 0.8, 0.5);
            this.velocity = this.player.method_26317(v22, v23, this.velocity);
         } else {
            this.velocity = this.velocity.method_1021(0.5);
         }

         if (!this.player.method_5740()) {
            this.velocity = this.velocity.method_1031(0.0, -v22 / 4.0, 0.0);
         }

         if (this.horizontalCollision
            && this.m1681(this.velocity.field_1352, this.velocity.field_1351 + 0.6 - this.pos.field_1351 + v26, this.velocity.field_1350)) {
            this.velocity = new Vec3d(this.velocity.field_1352, 0.3, this.velocity.field_1350);
         }
      } else if (this.isFallFlying) {
         Vec3d v9 = this.velocity;
         if (v9.field_1351 > -0.5) {
            this.fallDistance = 1.0F;
         }

         Vec3d v10 = this.m1694();
         float v11 = this.pitch * (float) (Math.PI / 180.0);
         double v12 = Math.sqrt(v10.field_1352 * v10.field_1352 + v10.field_1350 * v10.field_1350);
         double v14 = this.velocity.method_37267();
         double v16 = v10.method_1033();
         float v18 = MathHelper.method_15362((double)v11);
         v18 = (float)((double)v18 * (double)v18 * Math.min(1.0, v16 / 0.4));
         v9 = this.velocity.method_1031(0.0, v22 * (-1.0 + (double)v18 * 0.75), 0.0);
         if (v9.field_1351 < 0.0 && v12 > 0.0) {
            double v7 = v9.field_1351 * -0.1 * (double)v18;

            v9 = v9.method_1031(v10.field_1352 * v7 / v12, v7, v10.field_1350 * v7 / v12);
         }

         if (v11 < 0.0F && v12 > 0.0) {
            double v24 = v14 * (double)(-MathHelper.method_15374((double)v11)) * 0.04;
            v9 = v9.method_1031(-v10.field_1352 * v24 / v12, v24 * 3.2, -v10.field_1350 * v24 / v12);
         }

         if (v12 > 0.0) {
            v9 = v9.method_1031((v10.field_1352 / v12 * v14 - v9.field_1352) * 0.1, 0.0, (v10.field_1350 / v12 * v14 - v9.field_1350) * 0.1);
         }

         this.velocity = v9.method_18805(0.99, 0.98, 0.99);
         this.m1670(this.velocity);
      } else {
         BlockPos v25 = this.m1684();
         float v8 = this.player.method_73183().method_8320(v25).method_26204().method_9499();
         float v29 = this.onGround ? v8 * 0.91F : 0.91F;
         Vec3d v31 = this.m1665(movementInput, v8);
         double v33 = v31.field_1351;
         if (this.hasStatusEffect(StatusEffects.field_5902)) {
            StatusEffectInstance v13 = this.m1696(StatusEffects.field_5902);
            if (v13 != null) {
               v33 += (0.05 * (double)(v13.method_5578() + 1) - v31.field_1351) * 0.2;
            }
         } else if (this.player.method_73183().method_8608() && !this.player.method_73183().method_22340(v25)) {
            v33 = this.pos.field_1351 > (double)this.player.method_73183().method_31607() ? -0.1 : 0.0;
         } else if (!this.player.method_5740()) {
            v33 -= v22;
         }

         if (this.player.method_35053()) {
            this.velocity = new Vec3d(v31.field_1352, v33, v31.field_1350);
         } else {
            this.velocity = new Vec3d(v31.field_1352 * (double)v29, v33 * 0.98F, v31.field_1350 * (double)v29);
         }
      }

      if (this.player.method_31549().field_7479 && !this.player.method_5765()) {
         this.velocity = new Vec3d(this.velocity.field_1352, v21 * 0.6, this.velocity.field_1350);
         this.m1672();
      }
   }

   private void m1672() {

      this.fallDistance = 0.0F;
   }

   private boolean m1681(double offsetX, double offsetY, double offsetZ) {

      return this.m1682(this.boundingBox.method_989(offsetX, offsetY, offsetZ));
   }

   private Vec3d m1673(Vec3d motion) {
      if (!this.isClimbing()) {
         return motion;
      } else {
         this.m1672();
         double v2 = MathHelper.method_15350(motion.field_1352, -0.15F, 0.15F);
         double result = MathHelper.method_15350(motion.field_1350, -0.15F, 0.15F);

         double v4 = result;
         double v6 = Math.max(motion.field_1351, -0.15F);
         if (v6 < 0.0 && !this.getState(this.posToBlockPos(this.pos)).method_27852(Blocks.field_16492) && this.player.method_21754()) {

            v6 = 0.0;
         }

         return new Vec3d(v2, v6, v4);
      }
   }

   public static lr fromClientPlayer(lr.mr input) {
      ClientPlayerEntity v1 = mc.field_1724;

      lr v5 = new lr;

      v5./* $VF: Unable to resugar constructor */<init>(
         v1,
         input,
         v1.method_73189(),
         v1.method_18798(),
         v1.method_5829(),
         v1.method_36454(),
         v1.method_36455(),
         v1.method_5624(),
         (float)v1.field_6017,
         v1.field_6228,
         v1.field_6282,
         v1.method_6128(),
         v1.method_24828(),
         v1.field_5976,
         v1.field_5992,
         v1.method_5799(),
         v1.method_5681(),
         v1.method_5869(),
         new Object2DoubleArrayMap(v1.field_5964),
         new HashSet<>(v1.field_25599)
      );
      return v5;
   }

   private float m1668() {
      float v1 = 0.02F;

      return this.input.playerInput.comp_3165() ? v1 + 0.006F : v1;
   }

   public Vec3d pos() {

      return this.player.method_73189();
   }

   private Vec3d m1671(Vec3d movement) {
      Box v2 = new Box(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3).method_997(this.pos);
      List v3 = Collections.emptyList();
      double result = movement.method_1027();

      Vec3d v4;
      if (result == 0.0) {
         v4 = movement;
      } else {
         v4 = Entity.method_20736(this.player, movement, v2, this.player.method_73183(), v3);
      }

      boolean v5 = movement.field_1352 != v4.field_1352;
      boolean v6 = movement.field_1351 != v4.field_1351;
      boolean v7 = movement.field_1350 != v4.field_1350;
      boolean v8 = this.onGround || v6 && movement.field_1351 < 0.0;
      if (this.player.method_49476() > 0.0F && v8 && (v5 || v7)) {
         Vec3d v9 = Entity.method_20736(
            this.player, new Vec3d(movement.field_1352, (double)this.player.method_49476(), movement.field_1350), v2, this.player.method_73183(), v3
         );
         PlayerEntity v14 = this.player;
         Vec3d temp1 = new Vec3d;

         temp1./* $VF: Unable to resugar constructor */<init>(0.0, (double)this.player.method_49476(), 0.0);
         Vec3d v10 = Entity.method_20736(v14, temp1, v2.method_1012(movement.field_1352, 0.0, movement.field_1350), this.player.method_73183(), v3);
         Vec3d v11 = Entity.method_20736(
               this.player, new Vec3d(movement.field_1352, 0.0, movement.field_1350), v2.method_997(v10), this.player.method_73183(), v3
            )
            .method_1019(v10);
         if (v10.field_1351 < (double)this.player.method_49476() && v11.method_37268() > v9.method_37268()) {
            v9 = v11;
         }

         if (v9.method_37268() > v4.method_37268()) {
            return v9.method_1019(
               Entity.method_20736(
                  this.player, new Vec3d(0.0, -v9.field_1351 + movement.field_1351, 0.0), v2.method_997(v9), this.player.method_73183(), v3
               )
            );
         }
      }

      return v4;
   }

   private boolean m1692(TagKey<Fluid> tag, double speed) {
      if (this.m1693()) {
         return false;
      } else {
         Box v4 = this.boundingBox.method_1011(0.001);
         int v5 = MathHelper.method_15357(v4.field_1323);
         int v6 = MathHelper.method_15384(v4.field_1320);
         int v7 = MathHelper.method_15357(v4.field_1322);
         int v8 = MathHelper.method_15384(v4.field_1325);
         int v9 = MathHelper.method_15357(v4.field_1321);
         int v10 = MathHelper.method_15384(v4.field_1324);
         double v11 = 0.0;
         boolean v13 = true;
         boolean v14 = false;
         Vec3d v15 = Vec3d.field_1353;
         int v16 = 0;
         Mutable v17 = new Mutable();

         for (int v18 = v5; v18 < v6; v18++) {
            for (int v19 = v7; v19 < v8; v19++) {
               for (int v20 = v9; v20 < v10; v20++) {
                  v17.method_10103(v18, v19, v20);
                  FluidState v21 = this.player.method_73183().method_8316(v17);
                  if (v21.method_15767(tag)) {
                     double v22 = (double)((float)v19 + v21.method_15763(this.player.method_73183(), v17));
                     if (v22 >= v4.field_1322) {
                        v14 = true;
                        v11 = Math.max(v22 - v4.field_1322, v11);
                        if (v13) {
                           Vec3d v24 = v21.method_15758(this.player.method_73183(), v17);
                           if (v11 < 0.4) {
                              v24 = v24.method_1021(v11);
                           }

                           v15 = v15.method_1019(v24);
                           v16++;
                        }
                     }
                  }
               }
            }
         }

         if (v15.method_1033() > 0.0) {
            if (v16 > 0) {
               v15 = v15.method_1021(1.0 / (double)v16);
            }

            v15 = v15.method_1021(speed);
            if (Math.abs(this.velocity.field_1352) < 0.003) {

               if (Math.abs(this.velocity.field_1350) < 0.003 && v15.method_1033() < 0.0045) {
                  v15 = v15.method_1029().method_1021(0.0045);
               }
            }

            this.velocity = this.velocity.method_1019(v15);
         }

         this.f1304.put(tag, v11);
         return v14;
      }
   }

   public double getAttributeValue(RegistryEntry<EntityAttribute> attribute) {
      AttributeContainer result = this.player.method_6127();

      return result.method_26852(attribute);
   }

   public static lr fromOtherPlayer(PlayerEntity player, lr.mr input) {
      lr result = new lr;

      Vec3d temp4 = player.method_73189();
      Vec3d temp5 = player.method_73189().method_1020(new Vec3d(player.field_6014, player.field_6036, player.field_5969));
      Box var10006 = player.method_5829();

      float var10007 = player.method_36454();
      float v5 = player.method_36455();
      boolean var10009 = player.method_5624();
      float var10010 = (float)player.field_6017;
      int var10011 = player.field_6228;
      boolean var10012 = player.field_6282;
      boolean var10013 = player.method_6128();
      boolean var10014 = player.method_24828();
      boolean var10015 = player.field_5976;
      boolean var10016 = player.field_5992;
      boolean var10017 = player.method_5799();
      boolean var10018 = player.method_5681();
      boolean var10019 = player.method_5869();
      Object2DoubleArrayMap var10020 = new Object2DoubleArrayMap(player.field_5964);
      HashSet var10021 = new HashSet;

      var10021./* $VF: Unable to resugar constructor */<init>(player.field_25599);
      result./* $VF: Unable to resugar constructor */<init>(
         player,
         input,
         temp4,
         temp5,
         var10006,
         var10007,
         v5,
         var10009,
         var10010,
         var10011,
         var10012,
         var10013,
         var10014,
         var10015,
         var10016,
         var10017,
         var10018,
         var10019,
         var10020,
         var10021
      );
      return result;
   }

   private BlockPos m1684() {
      int v1 = -713883305;
      int temp1 = v1 * (v1 + 1);

      return BlockPos.method_49637(this.pos.field_1352, this.boundingBox.field_1322 - 0.5000001, this.pos.field_1350);
   }

   private boolean m1676() {
      if (!this.onGround) {
         double result = (double)this.fallDistance;

         if (!(result < 0.5) || this.player.method_73183().method_8587(this.player, this.boundingBox.method_989(0.0, (double)this.fallDistance - 0.5, 0.0))) {
            return false;
         }
      }

      return true;
   }

   public BlockPos posToBlockPos(Vec3d pos) {
      BlockPos result = new BlockPos;
      int temp2 = MathHelper.method_15357(pos.field_1352);

      result./* $VF: Unable to resugar constructor */<init>(temp2, MathHelper.method_15357(pos.field_1351), MathHelper.method_15357(pos.field_1350));
      return result;
   }

   private Vec3d m1675(Vec3d movement) {
      if (movement.field_1351 <= 0.0 && this.m1676()) {
         double v2 = movement.field_1352;

         double v4 = movement.field_1350;
         double v6 = 0.05;

         while (v2 != 0.0 && this.player.method_73183().method_8587(this.player, this.boundingBox.method_989(v2, -0.5, 0.0))) {
            if (v2 < v6 && v2 >= -v6) {
               v2 = 0.0;
               break;
            }

            double v11;
            if (v2 > 0.0) {

               v11 = -v6;
            } else {
               v11 = v6;
            }

            v2 += v11;
         }

         for (;
            v4 != 0.0 && this.player.method_73183().method_8587(this.player, this.boundingBox.method_989(0.0, -0.5, v4));
            v4 += v4 > 0.0 ? -v6 : v6
         ) {
            if (v4 < v6 && v4 >= -v6) {
               v4 = 0.0;
               break;
            }

            
         }

         while (v2 != 0.0 && v4 != 0.0 && this.player.method_73183().method_8587(this.player, this.boundingBox.method_989(v2, -0.5, v4))) {
            v2 = v2 < v6 && v2 >= -v6 ? 0.0 : (v2 > 0.0 ? v2 - v6 : v2 + v6);
            if (v4 < v6 && v4 >= -v6) {
               v4 = 0.0;
               break;
            }

            v4 += v4 > 0.0 ? -v6 : v6;
         }

         if (movement.field_1352 != v2 || movement.field_1350 != v4) {
            this.f1307 = true;
         }

         if (this.shouldClipAtLedge()) {
            movement = new Vec3d(v2, movement.field_1351, v4);
         }
      }

      return movement;
   }

   private double m1685() {
      double v2;
      int result = (v2 = (double)this.player.method_5751() - 0.4) == 0.0 ? 0 : (v2 < 0.0 ? -1 : 1);

      return result < 0 ? 0.0 : 0.4;
   }

   private Vec3d m1694() {
      return this.m1695(this.pitch, this.yaw);
   }

   private boolean m1693() {
      Box v1 = this.boundingBox.method_1014(1.0);
      int v2 = MathHelper.method_15357(v1.field_1323);
      int v3 = MathHelper.method_15384(v1.field_1320);
      int v4 = MathHelper.method_15357(v1.field_1321);
      int v5 = MathHelper.method_15384(v1.field_1324);
      boolean v7;
      if (!this.player.method_73183().method_33597(v2, v4, v3, v5)) {

         v7 = true;
      } else {
         v7 = false;
      }

      return v7;
   }

   private Vec3d m1665(Vec3d movementInput, float slipperiness) {
      this.m1666(this.m1667(slipperiness), movementInput);

      this.velocity = this.m1673(this.velocity);
      this.m1670(this.velocity);
      Vec3d v3 = this.velocity;

      BlockPos v4 = this.posToBlockPos(this.pos);
      BlockState v5 = this.getState(v4);
      if ((this.horizontalCollision || this.isJumping)
         && (this.isClimbing() || v5 != null && v5.method_27852(Blocks.field_27879) && PowderSnowBlock.method_32355(this.player))) {
         v3 = new Vec3d(v3.field_1352, 0.2, v3.field_1350);
      }

      return v3;
   }

   private float m1680() {
      float v1 = 0.0F;

      Block v2 = this.getState(this.posToBlockPos(this.pos)).method_26204();
      if (v2 != null) {
         v1 = v2.method_23350();
      }

      float v3 = 0.0F;
      Block v4 = this.getState(this.m1684()).method_26204();
      if (v4 != null) {
         v3 = v4.method_23350();
      }

      return v1 == 1.0F ? v3 : v1;
   }

   private void m1687() {
      if (this.player.method_5854() instanceof BoatEntity) {
         BoatEntity v1 = (BoatEntity)this.player.method_5854();

         if (!v1.method_5869()) {
            this.touchingWater = false;
            return;
         }
      }

      if (this.m1692(FluidTags.field_15517, 0.014)) {

         this.m1672();
         this.touchingWater = true;
      } else {
         this.touchingWater = false;
      }
   }

   public Lr(
      PlayerEntity player,
      lr.mr input,
      Vec3d pos,
      Vec3d velocity,
      Box boundingBox,
      float yaw,
      float pitch,
      boolean sprinting,
      float fallDistance,
      int jumpingCooldown,
      boolean isJumping,
      boolean isFallFlying,
      boolean onGround,
      boolean horizontalCollision,
      boolean verticalCollision,
      boolean touchingWater,
      boolean isSwimming,
      boolean submergedInWater,
      Object2DoubleMap<TagKey<Fluid>> fluidHeight,
      HashSet<TagKey<Fluid>> submergedFluidTag
   ) {
      this.player = player;
      this.input = input;
      this.pos = pos;
      this.velocity = velocity;
      this.boundingBox = boundingBox;
      this.yaw = yaw;
      this.pitch = pitch;
      this.sprinting = sprinting;
      this.fallDistance = fallDistance;
      this.jumpingCooldown = jumpingCooldown;
      this.isJumping = isJumping;
      this.isFallFlying = isFallFlying;
      this.onGround = onGround;
      this.horizontalCollision = horizontalCollision;
      this.verticalCollision = verticalCollision;
      this.touchingWater = touchingWater;
      this.isSwimming = isSwimming;
      this.submergedInWater = submergedInWater;
      this.f1304 = fluidHeight;
      this.f1305 = submergedFluidTag;
   }

   public boolean isSubmergedInWater() {
      return this.submergedInWater && this.m1686();
   }

   private float m1679() {
      if (this.hasStatusEffect(StatusEffects.field_5913)) {
         StatusEffectInstance v1 = this.m1696(StatusEffects.field_5913);
         int v2 = 1940751170;

         return 0.1F * (float)(v1.method_5578() + 1);
      } else {
         return 0.0F;
      }
   }

   private void m1688() {
      if (this.isSwimming) {
         this.isSwimming = this.m1677() && this.m1686() && !this.player.method_5765();
      } else {

         boolean temp1;
         if (this.m1677()
            && this.isSubmergedInWater()
            && !this.player.method_5765()
            && this.player.method_73183().method_8316(this.posToBlockPos(this.pos)).method_15767(FluidTags.field_15517)) {
            temp1 = true;
         } else {
            temp1 = false;
            
         }

         this.isSwimming = temp1;
      }
   }

   private boolean m1682(Box box) {
      if (this.player.method_73183().method_8587(this.player, box)) {

         if (!this.player.method_73183().method_22345(box)) {
            return true;
         }
      }

      return false;
   }

   private void m1666(float speed, Vec3d movementInput) {
      Vec3d v3 = Entity.method_18795(movementInput, speed, this.yaw);
      Vec3d temp1 = this.velocity.method_1019(v3);

      this.velocity = temp1;
   }

   public void tick() {
      this.f1306++;
      this.f1307 = false;
      if (!(this.pos.field_1351 <= -70.0)) {
         this.input.update();
         this.m1687();
         this.m1689();
         this.m1688();
         if (this.jumpingCooldown > 0) {
            this.jumpingCooldown--;
         }

         this.isJumping = this.input.playerInput.comp_3163();
         double v1 = this.velocity.field_1352;
         double v3 = this.velocity.field_1351;
         double v5 = this.velocity.field_1350;
         if (Math.abs(this.velocity.field_1352) < 0.003) {
            v1 = 0.0;
         }

         if (Math.abs(this.velocity.field_1351) < 0.003) {
            v3 = 0.0;
         }

         if (Math.abs(this.velocity.field_1350) < 0.003) {
            v5 = 0.0;
         }

         if (this.onGround) {
            this.isFallFlying = false;
         }

         this.velocity = new Vec3d(v1, v3, v5);
         if (this.isJumping) {
            double v7 = this.isInLava() ? this.m1691(FluidTags.field_15518) : this.m1691(FluidTags.field_15517);
            boolean v9 = this.m1686() && v7 > 0.0;
            double v10 = this.m1685();
            if (!v9 || this.onGround && !(v7 > v10)) {

               if (!this.isInLava() || this.onGround && !(v7 > v10)) {
                  if ((this.onGround || v9 && v7 <= v10) && this.jumpingCooldown == 0) {
                     this.jump();
                     this.jumpingCooldown = 10;
                  }
               } else {
                  this.m1683(FluidTags.field_15518);
               }
            } else {
               this.m1683(FluidTags.field_15517);
            }
         }

         float v13 = this.input.movementSideways * 0.98F;
         float v8 = this.input.movementForward * 0.98F;
         float v14 = 0.0F;
         if (this.hasStatusEffect(StatusEffects.field_5906) || this.hasStatusEffect(StatusEffects.field_5902)) {
            this.m1672();
         }

         this.m1664(new Vec3d((double)v13, (double)v14, (double)v8));
      }
   }

   public static lr simulateOtherPlayer(PlayerEntity player, 

      lr v2 = fromOtherPlayer(player, lr.mr.guessInput(player));
      int v3 = 0;

      while (true) {

         if (v3 >= ticks) {
            return v2;
         }

         v2.tick();
         v3++;
      }
   }

   protected boolean shouldClipAtLedge() {
      boolean result = this.input.playerInput.comp_3164();

      if (!result && !this.input.forceSafeWalk) {
         result = false;
      } else {
         result = true;
         
      }

      return result;
   }

   private float m1667(float slipperiness) {
      float v3;
      if (this.onGround) {
         v3 = this.m1669();

         v3 *= 0.21600002F / (slipperiness * slipperiness * slipperiness);
      } else {
         v3 = this.m1668();
      }

      return v3;
   }

   @Environment(EnvType.CLIENT)
   public static class mr extends Input {
      public PlayerInput playerInput;
      public boolean forceSafeWalk = false;
      public float movementSideways;
      public static final double MAX_WALKING_SPEED = 0.121;
      public float movementForward;

      public static lr.mr guessInput(PlayerEntity entity) {
         Vec3d result = entity.method_73189();
         Vec3d temp1 = new Vec3d;
         int v9 = 1828460351;

         temp1./* $VF: Unable to resugar constructor */<init>(entity.field_6014, entity.field_6036, entity.field_5969);
         Vec3d v1 = result.method_1020(temp1);
         double v2 = v1.method_37268();
         PlayerInput v12 = new PlayerInput;
         boolean var10006 = !entity.method_24828();
         boolean var10007 = entity.method_5715();
         boolean var10008;
         if (v2 >= 0.014641) {
            var10008 = true;
         } else {
            var10008 = false;
            
         }

         v12./* $VF: Unable to resugar constructor */<init>(false, false, false, false, var10006, var10007, var10008);
         PlayerInput v4 = v12;
         if (v2 > 0.0025000000000000005) {
            double v5 = kr.getDegreesRelativeToView(v1, entity.method_36454());
            double v7 = MathHelper.method_15338(v5);
            v4 = kr.getDirectionalInputForDegrees(v4, v7);
         }

         return new lr.mr(v4);
      }

      public mr(PlayerInput input) {
         this.playerInput = input;
      }

      public static lr.mr fromClientPlayer(PlayerInput input) {
         lr.mr result = new lr.mr;

         result./* $VF: Unable to resugar constructor */<init>(input);
         return result;
      }

      public void update() {
         if (this.playerInput.comp_3159() != this.playerInput.comp_3160()) {
            float temp1;
            if (this.playerInput.comp_3159()) {
               temp1 = 1.0F;
            } else {
               temp1 = -1.0F;
               int v2 = 820546741;
               int temp2 = v2 * (v2 + 1);

               
            }

            this.movementForward = temp1;
         } else {
            this.movementForward = 0.0F;
         }

         if (this.playerInput.comp_3161() == this.playerInput.comp_3162()) {
            this.movementSideways = 0.0F;
         } else {
            this.movementSideways = this.playerInput.comp_3161() ? 1.0F : -1.0F;
         }

         if (this.playerInput.comp_3164()) {
            this.movementSideways *= 0.3F;
            this.movementForward *= 0.3F;
         }
      }

      public String toString() {
         boolean result = this.playerInput.comp_3159();
         boolean temp1 = this.playerInput.comp_3160();
         int temp2 = this.playerInput.comp_3161();

         boolean temp3 = this.playerInput.comp_3162();
         boolean v16 = this.playerInput.comp_3163();
         boolean v1 = this.playerInput.comp_3164();
         boolean v2 = this.playerInput.comp_3165();
         boolean v3 = v16;
         boolean v4 = temp3;
         boolean v5 = (boolean)temp2;
         boolean v6 = temp1;
         boolean v7 = result;
         StringBuilder v11 = new StringBuilder()
            .append("SimulatedPlayerInput(forwards={")
            .append(v7)
            .append("}, backwards={")
            .append(v6)
            .append("}, left={")
            .append(v5)
            .append("}, right={")
            .append(v4)
            .append("}, jumping={")
            .append(v3);
         temp2 = -745130999;

         StringBuilder v12 = v11.append(StrDec.d("1xjrGm8zL5L0t78UyA==", temp2)).append(v2).append(", slowDown=");

         return v12.append(v1).append(")").toString();
      }

      
   }
}
