package ru.white.module.impl.misc;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.white.event.EventTarget;
import ru.white.module.setting.SliderSetting;
import ru.white.utils.misc.Of;
import ru.white.module.setting.BooleanSetting;
import ru.white.utils.misc.Tf;
import ru.white.utils.misc.Uq;
import ru.white.module.Category;
import ru.white.module.Module;
import ru.white.module.ModuleAnnotation;
import ru.white.utils.misc.Up;
import ru.white.auth.StrDec;
import ru.white.utils.render.vs;
import ru.white.utils.ChatUtil;
import ru.white.utils.system.Hu;
import ru.white.event.Event;
import ru.white.event.impl.EventReceivePacket;
import ru.white.event.impl.EventPostMotion;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventStep;
import ru.white.event.impl.EventTrace;
import ru.white.event.impl.EventRenderModel;

@ModuleAnnotation(
   name = "Report Helper",
   desc = "Очередь репортов и подсветка игроков",
   category = Category.OTHER
)
@Environment(EnvType.CLIENT)
public class ReportHelper extends Module {
   private String f987;
   private final Set<String> f975;
   private long f981;
   private static final long AUTO_LIST_INTERVAL_MS = 60000L;
   private long f979;
   private static final Pattern NICK_PATTERN = Pattern.compile("(?i)\\b[A-Za-z0-9_]{3,16}\\b");
   private boolean f984;
   public final tf reportColor;
   public final SettingVisibility reportBind = new SettingVisibility(this, "Бинд репорта");
   private final BufferAllocator f989;
   private final Deque<String> f973;
   private static final RenderPipeline CRYSTAL_PIPELINE = RenderPipelines.method_67887(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.field_56860})
         .withLocation(Identifier.method_60655("client", "report_crystal"))
         .withVertexFormat(VertexFormats.field_1576, DrawMode.field_27379)
         .withCull(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withBlend(BlendFunction.TRANSLUCENT)
         .build()
   );
   public final SliderSetting delaySeconds;
   private long f980;
   private static final Pattern WAIT_SECONDS_PATTERN = Pattern.compile(
      "(?iu)подождать:?\\s*(\\d+)\\s*сек"
   );
   public final BooleanSetting hud;
   private String f978;
   private static final Pattern REPORT_TITLE_PATTERN = Pattern.compile(
      "(?iu)(?:жалоба\\s+на|репорт\\s+на|report\\s+(?:on|for)|complaint\\s+(?:on|for))\\s+([A-Za-z0-9_]{3,16})"
   );
   private String f977;
   private final Set<String> f974;
   private long f982;
   private static final RenderLayer CRYSTAL_LAYER = RenderLayer.method_75940(
      "report_crystal",
      RenderSetup.method_75927(CRYSTAL_PIPELINE).method_75929(4096).method_75938()
   );
   private long f986;
   private static final int MAX_AUTO_LIST_PAGES = 10;
   private static final long AUTO_LIST_PAGE_TIMEOUT_MS = 2500L;
   private int f985;
   private String f988;
   public final BooleanSetting autoCheckList;
   private final Set<String> f976;
   private long f983;

   private long m1289(String lower) {
      Matcher result = WAIT_SECONDS_PATTERN.matcher(lower);

      Matcher v2 = result;
      return v2.find()
         ? Math.max(1L, Long.parseLong(v2.group(1))) * (-887399478518983185L + 887399478518984185L) + -1052165273536581961L + 1052165273536582461L
         : -8612626139719596807L + 8612626139719601807L;
   }

   public static boolean shouldHighlight(Entity entity) {
      try {
         nm result = get();

         nm v1 = result;

         if (v1 != null) {

            if (v1.isEnabled() && v1.m1292(entity)) {
               return true;
            }
         }

         return false;
      } catch (Exception v5) {
         return false;
      }
   }

   private void m1270() {
      if (this.f977 != null && !this.f977.isBlank()) {
         this.m1271(this.f977);
      } else {

         zp.addChatMessage("Репорт Хелпер: Сначала ударь игрока.");
      }
   }

   private String m1300(String value) {
      String v2 = Formatting.method_539(value == null ? "" : value);

      return v2 == null ? "" : v2.replace((char)(167), (char)(32)).trim();
   }

   @Override
   protected void onDisable() {
      this.f973.clear();

      this.f974.clear();
      this.f978 = null;
      this.f984 = false;
   }

   private boolean m1277() {
      if (mc.field_1755 == null) {
         return false;
      } else {
         String v1 = this.m1300(mc.field_1755.method_25440().getString()).toLowerCase(Locale.ROOT);
         return v1.contains("репорт") || v1.contains("жалоб") || v1.contains("report");
      }
   }

   private boolean m1287(String lower) {

      return lower.contains("игрок находится в афк") || lower.contains("afk");
   }

   private void m1275() {
      if (mc.field_1755 != null && mc.field_1724 != null) {
         if (mc.field_1755 instanceof GenericContainerScreen && mc.field_1724.field_7512 instanceof GenericContainerScreenHandler v1) {
            long v14 = System.currentTimeMillis();
            if (v14 - this.f980 < 6443642800367677454L + -6443642800367676954L) {
               
            } else {
               this.f980 = v14;
               boolean v4 = false;
               int v6 = v1.method_17388() * (9);

               for (int v7 = 0; v7 < Math.min(v6, v1.field_7761.size()); v7++) {
                  Slot v8 = (Slot)v1.field_7761.get(v7);

                  ItemStack v9 = v8.method_7677();
                  if (!v9.method_7960()) {
                     String v10 = this.m1278(v9);
                     if (v10 != null) {
                        v4 = true;

                        this.f976.add(this.m1299(v10));
                        this.f975.add(this.m1299(v10));
                     }

                     if (v5 == -1 && this.m1279(v9)) {
                        v5 = v7;
                     }
                  }
               }

               if (v4 && this.f978 != null && this.m1293(this.f978)) {
                  this.f978 = null;
               }

               if (this.f984) {
                  boolean v15 = this.m1277() || v4 || v5 != -1;
                  if (!v15) {
                     this.f984 = false;
                     return;
                  }

                  String v16 = this.m1276(v1, v6);
                  if (v16.equals(this.f987)) {
                     if (v14 - this.f986 > 7350819321218450033L + -7350819321218447533L) {
                        this.m1281(v1);
                     }

                     return;
                  }

                  if (v5 != -1 && this.f985 < 10 && mc.field_1761 != null) {
                     this.f987 = v16;
                     this.f985++;
                     this.f986 = v14;
                     this.f983 = v14;
                     mc.field_1761.method_2906(v1.field_7763, v5, 0, SlotActionType.field_7790, mc.field_1724);
                     return;
                  }

                  this.m1281(v1);
               }
            }
         }
      }
   }

   public static int getReportColor(int fallback) {
      try {
         nm v1 = get();
         if (v1 != null) {
            boolean result = v1.isEnabled();

            if (result) {
               return v1.reportColor.getValue();
            }
         }

         return fallback;
      } catch (Exception v5) {
         return fallback;
      }
   }

   private boolean m1279(ItemStack stack) {
      if (this.m1280(stack.method_7964().getString())) {
         return true;
      } else {
         LoreComponent v2 = (LoreComponent)stack.method_58694(DataComponentTypes.field_49632);
         if (v2 == null) {
            return false;
         } else {
            for (Text v4 : v2.comp_2400()) {
               if (this.m1280(v4.getString())) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   private void m1281(GenericContainerScreenHandler handler) {
      this.f984 = false;
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         ClientPlayNetworkHandler result = mc.field_1724.field_3944;
         CloseHandledScreenC2SPacket temp1 = new CloseHandledScreenC2SPacket(handler.field_7763);

         result.method_52787(temp1);
      }

      mc.method_1507(null);
   }

   public static boolean isReported(Entity entity) {
      if (entity instanceof PlayerEntity v1) {
         Text result = v1.method_5477();

         return isReportedName(result.getString());
      } else {
         return false;
      }
   }

   private boolean m1292(Entity entity) {
      if (entity instanceof PlayerEntity v2) {
         boolean result = this.m1294(v2.method_5477().getString());

         if (result) {

            return true;
         }
      }

      return false;
   }

   private void m1268(VertexConsumer EventTarget, Matrix4f mat, Vec3d cam, double cx, double cy, double cz, float spin, int cr, int StringSetting, int cb) {
      float v14 = 0.18F;
      float v15 = 0.3F;
      float v16 = (float)(cx - cam.field_1352);
      float v17 = (float)(cy - cam.field_1351);
      float v18 = (float)(cz - cam.field_1350);
      float[][] v19 = new float[4][2];

      for (int v20 = 0; v20 < 4; v20++) {
         float v21 = spin + (float)v20 * (float) (Math.PI / 2);
         v19[v20][0] = v16 + (float)Math.cos((double)v21) * v14;
         v19[v20][1] = v18 + (float)Math.sin((double)v21) * v14;
      }

      for (int v26 = 0; v26 < 4; v26++) {
         int v27 = (v26 + 1) % 4;
         float v28;
          else {
            v28 = 0.78F;
         }

         float v22 = v28;
         float v23 = v26 % 2 == 0 ? 0.68F : 0.52F;
         float temp4 = v17 + v15;
         float var10006 = v19[v26][0];
         float var10008 = v19[v26][1];
         float var10009 = v19[v27][0];
         float[] var10011 = v19[v27];

         this.m1269(
            EventTarget,
            mat,
            v16,
            temp4,
            v18,
            var10006,
            v17,
            var10008,
            var10009,
            v17,
            var10011[1],
            (int)((float)cr * v22),
            (int)((float)StringSetting * v22),
            (int)((float)cb * v22),
            210
         );
         this.m1269(
            EventTarget,
            mat,
            v16,
            v17 - v15,
            v18,
            v19[v27][0],
            v17,
            v19[v27][1],
            v19[v26][0],
            v17,
            v19[v26][1],
            (int)((float)cr * v23),
            (int)((float)StringSetting * v23),
            (int)((float)cb * v23),
            210
         );
      }
   }

   @EventTarget
   public void onAttack(Event event) {
      if (event.getTarget() instanceof PlayerEntity result) {

         PlayerEntity v2 = result;
         if (v2 != mc.field_1724) {
            this.f977 = v2.method_5477().getString();
         }
      }
   }

   private boolean m1298(String name) {
      if (mc.method_1548() != null) {
         boolean result = this.m1299(mc.method_1548().method_1676()).equals(this.m1299(name));

         if (result) {

            return true;
         }
      }

      return false;
   }

   private boolean m1301(String word) {
      String v2 = word.toLowerCase(Locale.ROOT);
      if (!v2.equals("report") && !v2.equals("complaint")) {

         if (!v2.equals("status") && !v2.equals("date")) {

            if (!v2.equals("reason") && !v2.equals("minecraft") && !v2.equals("list") && !v2.equals("next") && !v2.equals("page")) {
               return false;
            }
         }
      }

      return true;
   }

   @EventTarget
   public void onTick(EventRenderModel event) {
      this.m1274();
      this.m1275();
      this.m1272();
   }

   private boolean m1288(String lower) {
      if (!lower.contains("игрок не найден")) {

         if (!lower.contains("player not found")) {
            return false;
         }
      }

      return true;
   }

   private boolean m1284(String lower) {
      if (!lower.contains("жалоба успешно отправлена")) {
         if (lower.contains("репорт на")) {

            if (lower.contains("был принят")) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public static int getReportColorRgb(int fallback) {
      int result = getReportColor(fallback);

      return result & 16777215;
   }

   @EventTarget
   public void onRender3D(EventTrace e) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         float v2 = mc.method_61966().method_60637(true);
         Vec3d v3 = mc.field_1773.method_19418().method_71156();
         MatrixStack v4 = e.getMatrixStack();
         Matrix4f v5 = v4.method_23760().method_23761();
         Immediate v6 = VertexConsumerProvider.method_22991(this.f989);
         int v7 = this.reportColor.getValue();
         int v8 = v7 >> 16 & 255;
         int v9 = v7 >> 8 & 255;
         int v10 = v7 & 255;
         long v11 = System.currentTimeMillis();
         boolean v13 = false;

         for (PlayerEntity v15 : mc.field_1687.method_18456()) {
            if (v15 != mc.field_1724 && this.m1294(v15.method_5477().getString()) && !(mc.field_1724.method_5858(v15) > 16384.0)) {
               Vec3d v16 = v15.method_30950(v2);
               long v17 = (long)(v15.method_5477().getString().hashCode() & 65535) * (-3998098034238083020L + 3998098034238083027L);
               float v19 = (float)((v11 + v17) % (-2384919235705450543L + 2384919235705454543L)) / 4000.0F * (float) (Math.PI * 2);
               double v20 = Math.sin((double)(v11 + v17) / 900.0) * 0.1;
               double v22 = v16.field_1352;
               double result = v16.field_1351 + (double)v15.method_17682() + 0.85 + v20;

               double v24 = result;
               double v26 = v16.field_1350;
               this.m1268(v6.method_73477(CRYSTAL_LAYER), v5, v3, v22, v24, v26, v19, v8, v9, v10);
               v13 = true;
            }
         }

         if (v13) {
            v6.method_22993();
         }
      }
   }

   private String m1296() {

      long v1 = Math.max(0L, this.f981 - System.currentTimeMillis());
      String result;
      if (v1 <= 0L) {
         result = "Готово";
      } else {
         long v8 = (v1 + -5349521745541967010L + 5349521745541968009L) / (-3214949191552099093L + 3214949191552100093L);

         long v3 = v8;
         result = "Кд: " + v3 + "с";
      }

      return result;
   }

   private boolean m1285(String lower) {

      String temp1 = "необходимо подождать";

      return lower.contains(temp1) || lower.contains("cooldown");
   }

   private void m1291(String name) {
      String v2 = this.m1299(name);
      if (!v2.isEmpty() && !this.m1298(v2)) {
         this.f975.add(v2);

         this.f976.add(v2);
      }
   }

   private boolean m1280(String raw) {
      String v2 = this.m1300(raw).toLowerCase(Locale.ROOT);

      return v2.contains("впер") || v2.contains("следующ") || v2.contains("next");
   }

   private void m1273(String name) {
      String v2 = this.m1299(name);
      if (!v2.isEmpty() && !this.m1298(v2) && !this.m1293(v2)) {
         int result = v3 * (v3 + 1);

         if (!this.f974.contains(v2)) {
            this.f973.addFirst(name);
            this.f974.add(v2);
            return;
         }
      }
   }

   public ReportHelper() {
      this.reportColor = new tf(this, "Цвет подсветки", -43691);
      this.delaySeconds = new SliderSetting(this, "Задержка репорта", 31.0F, 31.0F, 90.0F, 1.0F);
      this.hud = new BooleanSetting(this, "Худ", true);
      this.autoCheckList = new BooleanSetting(this, "Проверять список", false);
      this.f973 = new ArrayDeque<>();
      this.f974 = new LinkedHashSet<>();
      this.f975 = new LinkedHashSet<>();
      this.f976 = new LinkedHashSet<>();
      this.f987 = "";
      this.f989 = new BufferAllocator(16384);
   }

   public static int getColorForEntity(Entity entity, int fallback) {
      int result;
      if (isReported(entity)) {

         result = getReportColor(fallback);
      } else {
         result = fallback;
      }

      return result;
   }

   public static nm get() {
      nm result = uq.get(nm.class);

      return result;
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {
      this.f984 = false;
   }

   private void m1274() {
      if (this.autoCheckList.getValue() && mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         if (mc.field_1755 == null && this.f978 == null) {
            long v1 = System.currentTimeMillis();
            if (this.f984 && v1 - this.f983 > -903171350580671135L + 903171350580676135L) {
               this.f984 = false;
            }

            if (v1 - this.f982 >= -5738942158559009811L + 5738942158559069811L) {
               int temp2 = v3 * (v3 + 1) % 2;

               if (temp2 != 0) {
                  }

               this.f982 = v1;
               this.f983 = v1;
               this.f984 = true;
               this.f985 = 0;
               this.f986 = 0L;
               this.f987 = "";
               this.m1297("report list");
            }
         }
      }
   }

   private String m1299(String name) {
      if (name == null) {
         return "";
      } else {

         Matcher v2 = NICK_PATTERN.matcher(this.m1300(name));
         boolean result = v2.find();
         int v6 = v4 * (v4 + 1);

         return result ? v2.group().toLowerCase(Locale.ROOT) : "";
      }
   }

   private String m1276(GenericContainerScreenHandler handler, int containerSlots) {
      StringBuilder result = new StringBuilder;

      result./* $VF: Unable to resugar constructor */<init>();
      StringBuilder v3 = result;

      for (int v4 = 0; v4 < Math.min(containerSlots, handler.field_7761.size()); v4++) {
         ItemStack v5 = ((Slot)handler.field_7761.get(v4)).method_7677();
         if (!v5.method_7960()) {
            result = v3.append(v4).append((char)(58));

            String v10 = v5.method_7964().getString();

            result.append(v10).append((char)(59));
         }
      }

      return v3.toString();
   }

   private void m1272() {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null && !this.f973.isEmpty()) {
         long v1 = System.currentTimeMillis();
         if (this.f978 != null) {
            if (v1 - this.f979 <= -1765180573807720491L + 1765180573807732491L) {
               return;
            }

            this.m1273(this.f978);
            this.f978 = null;
         }

         if (v1 >= this.f981) {
            String v3 = this.f973.pollFirst();
            if (v3 != null) {
               this.f974.remove(this.m1299(v3));
               if (!this.m1293(v3) && !this.m1298(v3)) {
                  this.m1297("report " + v3 + " чит");
                  this.f978 = v3;

                  this.f979 = v1;
               }
            }
         }
      }
   }

   private int m1295() {
      LinkedHashSet v1 = new LinkedHashSet<>(this.f975);
      v1.addAll(this.f976);
      v1.addAll(this.f974);
      if (this.f978 != null) {
         v1.add(this.m1299(this.f978));

         
      }

      return v1.size();
   }

   private boolean m1293(String name) {
      String v2 = this.m1299(name);
      boolean result = this.f975.contains(v2);

      if (!result && !this.f976.contains(v2)) {
         result = false;
         
      } else {
         result = true;
      }

      return result;
   }

   private long m1290() {
      long result = 8932460394576603083L + -8932460394576572083L;

      result = Math.max(result, this.delaySeconds.getValue().longValue() * (-8521172705424445204L + 8521172705424446204L));

      return result;
   }

   public static boolean isAutoListActive() {
      try {
         nm v0 = get();
         if (v0 != null) {
            int result = v1 * (v1 + 1);

            if (v0.isEnabled() && v0.f984) {
               return true;
            }
         }

         return false;
      } catch (Exception v4) {
         return false;
      }
   }

   public static boolean hasHighlightedTargets() {
      try {
         nm v0 = get();
         if (v0 != null && v0.isEnabled()) {

            if (v0.m1295() > 0) {
               return true;
            }
         }

         return false;
      } catch (Exception v2) {
         return false;
      }
   }

   @EventTarget
   public void onPacket(EventStep event) {
      if (!event.isSend()) {
         Packet v2 = event.getPacket();
         if (v2 instanceof GameMessageS2CPacket) {
            int result = v4 * (v4 + 1);

            GameMessageS2CPacket v3 = (GameMessageS2CPacket)v2;
            this.m1283(v3.comp_763().getString());
         }
      }
   }

   public static int getShaderColor(int fallback, Collection<Entity> targets) {
      return hasHighlightedTargets() ? getReportColor(fallback) : fallback;
   }

   private void m1271(String name) {
      String v2 = this.m1299(name);
      if (!v2.isEmpty() && !this.m1298(v2)) {

         if (!this.m1293(v2) && !this.f974.contains(v2)) {
            this.f973.addLast(name);
            this.f974.add(v2);
            this.f988 = null;
            StringBuilder v6 = new StringBuilder;

            v6./* $VF: Unable to resugar constructor */<init>();
            zp.addChatMessage(
               v6.append("Репорт Хелпер: Добавлен в очередь ")
                  .append((Object)name)
                  .toString()
            );
            return;
         }
      }
   }

   private void m1283(String raw) {
      String v2 = this.m1300(raw);

      if (!v2.isEmpty()) {
         String v3 = v2.toLowerCase(Locale.ROOT);
         if (this.f978 != null) {
            if (this.m1285(v3)) {
               this.m1273(this.f978);
               this.f978 = null;
               this.f981 = System.currentTimeMillis() + this.m1289(v3);
            } else if (this.m1287(v3)) {
               this.f988 = this.f978;
               this.f978 = null;
            } else if (this.m1288(v3)) {
               this.f988 = this.f978;
               this.f978 = null;
            } else if (this.m1286(v3)) {
               this.m1291(this.f978);
               this.f978 = null;
            } else if (this.m1284(v3)) {
               this.m1291(this.f978);
               this.f978 = null;
               this.f981 = System.currentTimeMillis() + this.m1290();
            }
         }

         String v4 = this.m1282(v2);
         if (v4 != null) {
            this.m1291(v4);
         }
      }
   }

   private void m1297(String command) {
      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         ClientPlayNetworkHandler result = mc.field_1724.field_3944;
         String temp1;
         if (command.startsWith("/")) {

            temp1 = command.substring(1);
            
         } else {
            temp1 = command;
         }

         result.method_45730(temp1);
      }
   }

   private String m1282(String raw) {
      String result = this.m1300(raw);

      String v2 = result;
      if (v2.isEmpty()) {
         return null;
      } else {
         Matcher v3 = REPORT_TITLE_PATTERN.matcher(v2);
         if (v3.find()) {
            return v3.group(1);
         } else {
            String v4 = v2.toLowerCase(Locale.ROOT);
            if (!v4.contains("жалоб") && !v4.contains("репорт") && !v4.contains("report")) {
               return null;
            } else {
               Matcher v5 = NICK_PATTERN.matcher(v2);

               while (v5.find()) {
                  String v6 = v5.group();

                  if (!this.m1301(v6)) {
                     return v6;
                  }
               }

               return null;
            }
         }
      }
   }

   @EventTarget
   public void onKey(EventPostMotion event) {
      int temp2 = v2 + 1;

      if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
         Integer result = this.reportBind.getValue();

         if (result != -1 && event.getKey() == this.reportBind.getValue()) {
            this.m1270();
         }
      }
   }

   private boolean m1294(String name) {
      String v2 = this.m1299(name);
      return this.m1293(v2) || this.f974.contains(v2) || this.f978 != null && this.m1299(this.f978).equals(v2);
   }

   private void m1269(
      VertexConsumer EventTarget, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a
   ) {
      vc.method_22918(mat, x1, y1, z1).method_1336(r, g, b, a);
      vc.method_22918(mat, x2, y2, z2).method_1336(r, g, b, a);

      VertexConsumer result = vc.method_22918(mat, x3, y3, z3);

      result.method_1336(r, g, b, a);
   }

   private boolean m1286(String lower) {

      return lower.contains("уже отправляли") || lower.contains("already");
   }

   public static boolean isReportedName(String name) {
      try {
         nm v1 = get();
         if (v1 != null && v1.isEnabled()) {
            boolean result = v1.m1294(name);
            int v7 = v2 * (v2 + 1);

            if (result) {
               return true;
            }
         }

         return false;
      } catch (Exception v5) {

         return false;
      }
   }

   private String m1278(ItemStack stack) {
      String v2 = this.m1282(stack.method_7964().getString());
      if (v2 != null) {
         return v2;
      } else {
         LoreComponent v3 = (LoreComponent)stack.method_58694(DataComponentTypes.field_49632);
         if (v3 == null) {
            return null;
         } else {

            for (Text v5 : v3.comp_2400()) {
               String v6 = this.m1282(v5.getString());
               if (v6 != null) {
                  return v6;
               }
            }

            return null;
         }
      }
   }

   @EventTarget(
      priority = -400
   )
   public void onDisplay(EventReceivePacket event) {
      if (this.hud.getValue() && mc.field_1724 != null && !mc.field_1690.field_1842) {
         float v2 = 2.0F;
         float v3 = (float)mc.method_22683().method_4495();
         float v4 = v2 / v3;
         float v5 = (float)mc.method_22683().method_4486() / v4;
         String v6 = "Репорт Хелпер";
         String result;
         if (this.f977 == null) {
            result = "Цель: Нет";
         } else {
            String v15 = this.f977;
            result = "Цель: " + v15;
         }

         String v7 = result;
         int v16 = this.f973.size();
         String v8 = "Очередь: " + v16;
         if (this.f978 == null) {
            result = this.m1296();
         } else {
            String v17 = this.f978;
            result = "Отправлен: " + v17;
         }

         String v9 = result;
         if (this.f988 != null && this.f978 == null && this.f973.isEmpty()) {
            String v18 = this.f988;
            v9 = "Ошибка: " + v18;
         }

         int v19 = this.m1295();
         String v10 = "Помечено: " + v19;
         float v11 = Math.max(118.0F, Math.max(hu.sf_medium.getWidth(v7, 5.5F), hu.sf_medium.getWidth(v9, 5.5F)) + 16.0F);
         float v12 = v5 - v11 - 8.0F;
         float v13 = 42.0F;
         float v14 = 55.0F;
         vs.ws.blur(v12, v13, v11, v14, 1.0F, 5.0F, up.getRectColor(1.0F));
         hu.sf_medium.draw(v6, v12 + 6.0F, v13 + 5.0F, 6.5F, this.reportColor.getValue());
         hu.sf_medium.draw(v7, v12 + 6.0F, v13 + 18.0F, 5.5F, up.WHITE);
         String temp1 = v8 + " | " + v10;

         hu.sf_medium.draw(temp1, v12 + 6.0F, v13 + 29.0F, 5.5F, up.WHITE);
         hu.sf_medium.draw(v9, v12 + 6.0F, v13 + 40.0F, 5.5F, up.WHITE);
      }
   }

   
}
