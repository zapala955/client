package ru.white.module.impl.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.StringVisitable.StyledVisitor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;
import org.joml.Vector4d;
import ru.white.module.impl.player.UseWorldTracker;
import ru.white.event.EventTarget;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Uq;
import ru.white.utils.misc.Zq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.render.et;
import ru.white.utils.render.js;
import ru.white.utils.render.vs;
import ru.white.module.impl.misc.NameProtect;
import ru.white.module.impl.misc.ReportHelper;
import ru.white.utils.ItemUtil;
import ru.white.utils.system.Hu;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventItemUse;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Name Tag",
   category = Category.RENDER,
   desc = "Отображает ники, хп и броню сущностей"
)
@Environment(EnvType.CLIENT)
public class NameTag extends Module {
   public BooleanSetting player = new BooleanSetting(this, "Отображения игроков", true);
   private final List<Entity> entities;
   public BooleanSetting ignoreNaked = new BooleanSetting(this, "Игнорировать голых", false).setVisible(() -> {

      return this.player.getValue();
   });
   public BooleanSetting mobs;
   public BooleanSetting handItems = new BooleanSetting(this, "Предметы в руках", false).setVisible(() -> {
      int temp2 = v1 + 1;

      return this.player.getValue();
   });
   public BooleanSetting items;

   @EventTarget
   public void onTick(EventRenderModel e) {
      this.entities.clear();
      if (mc.field_1687 != null) {
         for (Entity v3 : mc.field_1687.method_18112()) {
            if (v3 instanceof PlayerEntity) {
               PlayerEntity v4 = (PlayerEntity)v3;
               if (this.player.getValue()
                  && (v4.method_5797() == null || !v4.method_5797().getString().startsWith("Ghost_"))
                  && (!this.ignoreNaked.getValue() || hasArmor(v4))) {
                  this.entities.add(v3);
               }
            } else if (v3 instanceof LivingEntity) {

               if (this.mobs.getValue()) {
                  this.entities.add(v3);
               }
            } else if (v3 instanceof ItemEntity && this.items.getValue()) {
               this.entities.add(v3);
            }
         }
      }
   }

   @EventTarget(
      priority = 500
   )
   public void onTick(EventReceivePacket e) {
      DrawContext v2 = e.getDrawContext();
      float v3 = e.getPartialTicks();
      et.capture();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         for (Entity v5 : this.entities) {
            Vector4d v6 = zq.getVector4D(v5, v3);
            if (!zq.cantSee(v6)) {
               float v7 = (float)mc.field_1773.method_19418().method_71156().method_1022(v5.method_5829().method_1005());
               if (!(v7 < 1.0F)) {
                  float v8 = (float)zq.centerX(v6);
                  float v9 = (float)v6.y - 12.0F;
                  String v10 = "";
                  boolean v11 = false;
                  if (v5 instanceof PlayerEntity v12) {
                     v11 = Client.get().friendManager().isFriend(v12.method_5477().getString());
                     String v15 = Client.get().moduleManager().get(lm.class).isEnabled()
                           && Client.get().moduleManager().get(lm.class).friends.getValue()
                           && Client.get().friendManager().isFriend(v12.method_5820())
                        ? "Friend"
                        : toColoredString(v12.method_5476()).replace("⚡", "");
                     String result = v15.replace(mc.field_1724.method_5477().getString(), "Rose Solution");
                     String temp1 = String.valueOf(Formatting.field_1061);
                     String v16 = String.valueOf(v5.method_5767() ? "null " : (int)this.getHealth(v12));
                     String v17 = temp1;
                     String v18 = result;
                     v10 = v18 + " " + v17 + v16 + "IMinecraft";
                  } else if (v5 instanceof LivingEntity v13) {
                     String v32 = v13.method_5864().method_5897().getString();
                     String v34 = String.valueOf(Formatting.field_1080);
                     String temp2 = String.valueOf(Formatting.field_1068);
                     int temp3 = (int)this.getHealth(v13);
                     String v19 = String.valueOf(Formatting.field_1080);
                     int v20 = temp3;
                     String v21 = temp2;
                     String v22 = v34;
                     String v23 = v32;
                     v10 = v23 + v22 + " / " + v21 + v20 + v19 + "IMinecraft";
                  } else if (v5 instanceof ItemEntity) {
                     ItemEntity v14 = (ItemEntity)v5;
                     ItemStack v31 = v14.method_6983();
                     String v33 = v31.method_63015().getString();
                     String v35 = String.valueOf(Formatting.field_1080);
                     String v36 = String.valueOf(Formatting.field_1068);
                     int v24 = v14.method_6983().method_7947();

                     String v25 = v36;
                     String v26 = v35;
                     String v27 = v33;
                     v10 = v27 + v26 + " x" + v25 + v24;
                  }

                  this.renderTag(v2, v5, v10, v8, v9, v11, v3);
                  if (v5 instanceof PlayerEntity) {
                     PlayerEntity v29 = (PlayerEntity)v5;
                     if (this.player.getValue() && this.handItems.getValue()) {
                        this.renderHandItems(v2, v29, v8, (float)v6.w);
                     }
                  }

                  if (v5 instanceof PlayerEntity) {
                     PlayerEntity v30 = (PlayerEntity)v5;
                     if (this.player.getValue()) {
                        pi.get().render(v2, v30, v8, v9 - 3.0F);
                     }
                  }
               }
            }
         }
      }
   }

   private float getHealth(LivingEntity entity) {
      float v2 = entity.method_6032() + entity.method_6067();
      if (entity instanceof PlayerEntity v3 && mc.field_1687 != null) {
         ScoreboardObjective v4 = mc.field_1687.method_8428().method_1189(ScoreboardDisplaySlot.field_45158);
         if (v4 != null) {
            MutableText v5 = ReadableScoreboardScore.method_55398(
               mc.field_1687.method_8428().method_55430(v3, v4), v4.method_55380(StyledNumberFormat.field_47566)
            );

            try {
               v2 = Float.parseFloat(up.removeFormatting(v5.getString()));
            } catch (Exception v8) {
               
            }
         }
      }

      return iq.isCopyTime() ? entity.method_6032() : MathHelper.method_15363(v2, 0.0F, entity.method_6063() + entity.method_6067());
   }

   private void renderHandItems(DrawContext context, PlayerEntity entity, float centerX, float feetY) {
      ItemStack v5 = entity.method_6047();
      ItemStack v6 = entity.method_6079();
      ArrayList result = new ArrayList();

      ArrayList v7 = result;
      if (!v5.method_7960()) {
         v7.add(v5);
      }

      if (!v6.method_7960()) {
         v7.add(v6);
      }

      if (!v7.isEmpty()) {
         float v8 = 6.0F;
         float v9 = 8.0F;
         float v10 = 4.0F;
         float v11 = 2.0F;
         float v12 = 13.0F;
         float v13 = 2.0F;
         float v14 = 2.0F;
         float v15 = (float)mc.method_22683().method_4495();
         float v16 = v14 / v15;
         int v17 = Client.get().friendManager().isFriend(entity.method_5477().getString()) ? up.getColor(0, 255, 0) : up.background();
         if (nm.isReported(entity)) {
            v17 = nm.getReportColor(v17);
            
         }

         float v18 = feetY + 2.0F;

         for (ItemStack v20 : v7) {
            String v21 = toColoredString(v20.method_63015());
            float v22 = hu.sf_regular.getWidth(v21, v8);
            float v23 = v10 * 2.0F + v9 + v11 + v22;
            float v24 = centerX - v23 / 2.0F;
            vs.ws.blur(v24, v18 - 0.75F, v23, v12, 1.0F, 4.0F, up.replAlpha(v17, hh.getInstance().alphaHUD.getValue() * 0.5F));
            Matrix3x2fStack v25 = context.method_51448();
            v25.pushMatrix();
            v25.translate((v24 + v10 + v9 / 2.0F) * v16, (v18 + v12 / 2.0F - 1.0F) * v16);
            v25.scale(0.5F, 0.5F);
            js.drawItemWithContext(context, v20, -8.0F, -8.0F, 1.0F, 1.0F);
            v25.popMatrix();
            float v26 = v24 + v10 + v9 + v11;
            float v27 = v18 + v12 / 2.0F - 4.8F;
            hu.sf_regular.draw(v21, v26, v27, v8, up.WHITE);
            v18 += v12 + v13;
         }
      }
   }

   public static boolean hasArmor(PlayerEntity p) {
      if (p.method_6118(EquipmentSlot.field_6169).method_7960()) {
         boolean result = p.method_6118(EquipmentSlot.field_6174).method_7960();

         if (result && p.method_6118(EquipmentSlot.field_6172).method_7960()) {

            if (p.method_6118(EquipmentSlot.field_6166).method_7960()) {
               return false;
            }
         }
      }

      return true;
   }

   private static String toColoredString(Text text) {
      StringBuilder v1 = new StringBuilder();
      StyledVisitor temp1 = (style, str) -> {
         TextColor v3x = style.method_10973();
         if (v3x != null) {
            StringBuilder result = v1.append("§#");
            Object[] temp2 = new Object[1];

            temp2[0] = v3x.method_27716();
            result.append(String.format("%06X", temp2));
         }

         v1.append(str);
         return Optional.empty();
      };

      text.method_27658(temp1, Style.field_24360);
      return v1.toString();
   }

   public NameTag() {
      this.mobs = new BooleanSetting(this, "Отображения мобов", true);
      this.items = new BooleanSetting(this, "Отображения предметов", true);
      this.entities = new ArrayList<>();
   }

   @EventTarget
   public void onWorldLoad(EventSync e) {

      this.entities.clear();
   }

   private void renderTag(DrawContext context, Entity entity, String name, float x, float y, boolean isFriend, float tickDelta) {
      EventItemUse v8 = new EventItemUse(name);
      v8.hook();
      name = v8.getText();
      float v9 = 6.0F;
      float v10 = 4.0F;
      float v11 = 4.0F;
      ArrayList v12 = new ArrayList();
      if (entity instanceof LivingEntity v13) {
         for (EquipmentSlot v15 : EquipmentSlot.field_54086) {
            ItemStack v16 = v13.method_6118(v15);
            if (!v16.method_7960()) {
               v12.add(v16);
            }
         }
      }

      float v38 = 10.0F;
      float v39 = 8.0F;
      float v40 = v12.isEmpty() ? 0.0F : v11 + (float)(v12.size() - 1) * v38 + v39;
      float v41 = hu.sf_regular.getWidth(name, v9);
      float v17 = v41 + v40;
      float v18 = v17 + v10 * 2.0F;
      float v19 = 15.0F;
      float v20 = x - v18 / 2.0F;
      float v21 = y - 3.0F;
      int v22 = isFriend ? up.getColor(0, 255, 0) : up.background();
      if (nm.isReported(entity)) {
         v22 = nm.getReportColor(v22);
      }

      vs.ws.blur(v20, v21, v18, v19, 1.0F, 4.0F, up.replAlpha(v22, hh.getInstance().alphaHUD.getValue() * 0.5F));
      Client.get().render2D().flushAll();
      float v23 = v20 + v10;
      float v24 = v21 + v19 / 2.0F - hu.sf_regular.getHeight(v9) / 2.0F - 0.1F;

      hu.sf_regular.draw(name, v23, v24, v9, up.WHITE);
      if (!v12.isEmpty()) {
         float v25 = 2.0F;
         float v26 = (float)mc.method_22683().method_4495();
         float v27 = v25 / v26;
         float v28 = v23 + v41 + v11;
         float v29 = v21 + v19 / 2.0F - 0.5F;
         float v30 = 0.0F;

         for (ItemStack v32 : v12) {
            Matrix3x2fStack v33 = context.method_51448();
            v33.pushMatrix();

            v33.translate((v28 + v30 + v39 / 2.0F) * v27, v29 * v27);

            v33.scale(0.5F, 0.5F);
            js.drawItemWithContext(context, v32, -8.0F, -8.0F, 1.0F, 1.0F);
            v33.popMatrix();
            v30 += v38;
         }
      }
   }

   public static NameTag get() {
      return uq.get(NameTag.class);
   }

   
}
