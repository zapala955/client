package ru.white.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import ru.white.event.EventTarget;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.utils.ChatUtil;
import ru.white.event.impl.EventSync;
import ru.white.event.impl.EventRenderModel;

@Environment(EnvType.CLIENT)
public final class InventoryPresetManager {
   private long f45;
   private boolean f40;
   private int f41;
   private int f44;
   private final Map<String, InventoryPreset> f36;
   private final ArrayDeque<va.wa> f37;
   private int f42;
   private static final EquipmentSlot[] ARMOR = new EquipmentSlot[]{
      EquipmentSlot.field_6169, EquipmentSlot.field_6174, EquipmentSlot.field_6172, EquipmentSlot.field_6166
   };
   private final Random f38;
   private static final Path CURRENT_FILE = va.DIRECTORY.resolve("current.txt");
   private static final Path DIRECTORY = Path.of("rose/client1_21_11/inventory-presets");
   private String f39;
   private int f43;
   private final MinecraftClient f35 = MinecraftClient.method_1551();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Pattern EDGE_DECORATION = Pattern.compile("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$");
   private static final Pattern DECORATIVE_BRACKETS = Pattern.compile("^\\s*[\\[({<【〖].*?[\\])}>】〗]\\s*");

   public void cancelArrange(boolean notify) {
      if (this.f40 && notify) {
         zp.addChatMessage("Раскладка пресета остановлена");
      }

      this.m100();
      this.f40 = false;
      this.f37.clear();
      this.f41 = -1;
      this.f42 = -1;

      this.f44 = 0;
      this.f45 = 0L;
   }

   private int m94() {
      for (int v1 = 0; v1 < 9; v1++) {
         ItemStack result = this.currentStack(v1);

         if (result.method_7960()) {

            return v1;
         }
      }

      return 0;
   }

   private void m97(int slot, va.za expectedCursor, va.za expectedSlot) {

      this.m98(slot, 0, expectedCursor, expectedSlot);
   }

   private void m98(int slot, int button, va.za expectedCursor, va.za expectedSlot) {
      ArrayDeque result = this.f37;
      va.wa temp1 = new va.wa(slot, button, SlotActionType.field_7790, expectedCursor, expectedSlot, va.za.EMPTY);

      result.add(temp1);
   }

   public static int logicalToScreenSlot(int logical) {
      if (logical >= 0 && logical < 9) {
         return 36 + logical;
      } else {

         if (logical >= 9 && logical < 36) {
            return logical;
         } else if (logical >= 36 && logical < 40) {
            return 5 + (logical - (36));
         } else {
            return logical == 40 ? 45 : -1;
         }
      }
   }

   public Optional<InventoryPreset> activePreset() {
      InventoryPreset result;
      if (this.f39 == null) {
         result = null;
      } else {

         result = this.f36.get(key(this.f39));

         
      }

      return Optional.ofNullable(result);
   }

   private int m102(ItemStack cursor) {
      if (this.f42 >= 0) {
         int result = this.f42;

         int temp1 = this.f35.field_1724.field_7512.field_7761.size();

         if (result < temp1 && this.m103(this.f42, cursor)) {
            return this.f42;
         }
      }

      for (int v2 = 0; v2 < 36; v2++) {
         int v3 = logicalToScreenSlot(v2);
         if (this.m103(v3, cursor)) {
            return v3;
         }
      }

      int v7 = logicalToScreenSlot(40);
      return this.m103(v7, cursor) ? v7 : -1;
   }

   private void load(Path path) {
      try (BufferedReader v2 = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
         JsonObject v3 = JsonParser.parseReader(v2).getAsJsonObject();
         String v4 = v3.has("name") ? v3.get("name").getAsString() : path.getFileName().toString();
         long v5 = v3.has("createdAt") ? v3.get("createdAt").getAsLong() : 0L;
         ArrayList v7 = new ArrayList();
         JsonArray v8 = v3.getAsJsonArray("slots");
         if (v8 != null) {
            for (JsonElement v10 : v8) {
               JsonObject v11 = v10.getAsJsonObject();
               String v12 = v11.has("item") ? v11.get("item").getAsString() : "";
               int v13 = v11.has("count") ? v11.get("count").getAsInt() : 0;
               String v14 = v11.has("displayName") ? v11.get("displayName").getAsString() : "";
               String v15 = v11.has("potionSignature")
                  ? v11.get("potionSignature").getAsString()
                  : "";
               if (v15.isBlank() && ta.ua.canHavePotionContents(v12) && !v14.isBlank()) {
                  v15 = "legacy-name:" + v14;
               }

               boolean result = v11.has("potionData");

               String v16 = result ? v11.get("potionData").toString() : "";
               Identifier v17 = Identifier.method_12829(v12);
               v7.add(v17 != null && Registries.field_41178.method_10250(v17) ? new ta.ua(v12, v13, v14, v15, v16) : ta.ua.EMPTY);
            }
         }

         InventoryPreset v24 = new InventoryPreset(v4, v5, v7);
         this.f36.put(key(v4), v24);
      } catch (Exception v23) {
         v23.printStackTrace();
      }
   }

   public boolean isArranging() {

      return this.f40;
   }

   private boolean m101() {
      if (this.f35.field_1724 != null && !this.f35.field_1724.field_7512.method_34255().method_7960()) {
         ItemStack v1 = this.f35.field_1724.field_7512.method_34255();
         int v2 = this.m102(v1);
         if (v2 < 0) {
            return false;
         } else {
            this.f37
               .add(
                  new va.wa(
                     v2,
                     0,
                     SlotActionType.field_7790,
                     va.za.m87(v1),
                     va.za.m87(this.f35.field_1724.field_7512.method_7611(v2).method_7677()),
                     va.za.EMPTY
                  )
               );

            this.f42 = -1;
            return true;
         }
      } else {
         return true;
      }
   }

   private boolean m90() {
      InventoryPreset v1 = this.activePreset().orElse(null);
      if (v1 == null) {
         return false;
      } else {
         for (int v2 = 36; v2 < 40; v2++) {
            if (this.m91(v1, v2)) {
               return true;
            }
         }

         if (this.m91(v1, 40)) {
            return true;
         } else {

            while (v6 < 9) {
               if (this.m91(v1, v6)) {
                  return true;
               }

               v6++;
            }

            for (int v7 = 9; v7 < 36; v7++) {
               if (this.m91(v1, v7)) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public void searchAuction(va.xa missing) {

      if (this.f35.field_1724 != null && missing != null) {
         String v2 = cleanSearchName(missing.entry().displayName());
         if (v2.isBlank()) {
            ItemStack v3 = missing.entry().toStack();
            String result;
            if (v3.method_7960()) {
               result = missing.entry().itemId();
            } else {
               result = v3.method_7964().getString();
               
            }

            v2 = result;
            
         }

         v2 = cleanSearchName(v2);
         if (!v2.isBlank()) {
            this.f35.field_1724.field_3944.method_45730("ah search " + v2);
         }
      }
   }

   public InventoryPresetManager() {
      this.f36 = new LinkedHashMap<>();
      this.f37 = new ArrayDeque<>();
      this.f38 = new Random();
      this.f41 = -1;
      this.f42 = -1;
   }

   private void m105() {
      this.f36.clear();

      try {
         Files.createDirectories(DIRECTORY);

         try (Stream v1 = Files.list(DIRECTORY)) {
            v1.filter(path -> {
               String result = path.getFileName().toString();
               int v1x = 846050826;
               int temp2 = v1x * (v1x + 1);

               return result.endsWith(".json");
            }).forEach(this::load);
         }

         if (Files.exists(CURRENT_FILE)) {
            String v9 = Files.readString(CURRENT_FILE, StandardCharsets.UTF_8).trim();
            if (this.f36.containsKey(key(v9))) {
               this.f39 = this.f36.get(key(v9)).name();
            }
         }
      } catch (IOException v8) {
         v8.printStackTrace();
      }
   }

   public static int screenToLogicalSlot(int screenSlot) {
      if (screenSlot >= 36 && screenSlot <= 44) {
         return screenSlot - (36);
      } else if (screenSlot >= 9 && screenSlot <= 35) {
         return screenSlot;
      } else {
         if (screenSlot >= 5) {

            if (screenSlot <= 8) {

               return 36 + (screenSlot - 5);
            }
         }

         return screenSlot == 45 ? 40 : -1;
      }
   }

   private boolean m92(InventoryPreset preset, ta.ua wanted, int target, ItemStack targetStack) {
      int v5 = logicalToScreenSlot(target);
      if (v5 < 0) {
         return false;
      } else if (wanted.sameItem(targetStack)) {
         int v16 = this.m96(preset, wanted, target, false);
         if (v16 < 0) {
            return false;
         } else {
            ItemStack v17 = this.currentStack(v16);

            int v18 = logicalToScreenSlot(v16);
            if (v18 < 0) {
               return false;
            } else if (!ItemStack.method_31577(v17, targetStack)) {
               return this.m93(v16, target, v17, targetStack);
            } else {
               int v9 = wanted.count() - targetStack.method_7947();
               if (v17.method_7947() > v9) {
                  return this.m95(v18, v5, v17, targetStack, v9);
               } else {
                  this.f42 = v18;
                  va.za v10 = va.za.m87(v17);
                  va.za v11 = va.za.m87(targetStack);
                  this.m97(v18, va.za.EMPTY, v10);
                  this.m97(v5, v10, v11);
                  int v12 = Math.max(0, v17.method_7947() + targetStack.method_7947() - targetStack.method_7914());
                  if (v12 > 0) {
                     this.m97(v18, v10.m88(v12), va.za.EMPTY);
                  }

                  return true;
               }
            }
         }
      } else {
         int v6 = this.m96(preset, wanted, target, true);
         if (v6 < 0) {
            v6 = this.m96(preset, wanted, target, false);
         }

         if (v6 < 0) {
            return false;
         } else {

            int v7 = logicalToScreenSlot(v6);
            if (v7 < 0) {
               return false;
            } else {
               ItemStack v8 = this.currentStack(v6);
               if (targetStack.method_7960() && v8.method_7947() > wanted.count()) {
                  return this.m95(v7, v5, v8, targetStack, wanted.count());
               } else {

                  return this.m93(v6, target, v8, targetStack);
               }
            }
         }
      }
   }

   private static String m108(String value) {
      if (value == null) {

         int v5 = v2 * (v2 + 1) % 2;

         if (v5 != 0) {
            }

         return "";
      } else {
         String v1 = value.replaceAll("[\\\\/:*?\"<>|]", "").replaceAll("\\s+", " ").trim();
         return v1.length() > 32 ? v1.substring(0, 32).trim() : v1;
      }
   }

   @EventTarget
   public void onWorldLoad(EventSync event) {

      this.cancelArrange(false);
   }

   public boolean startArrange() {
      if (this.activePreset().isEmpty() || this.f35.field_1724 == null || !(this.f35.field_1755 instanceof InventoryScreen)) {
         return false;
      } else if (!this.f35.field_1724.field_7512.method_34255().method_7960()) {

         zp.addChatMessage(
            "Сначала положите предмет с курсора в слот"
         );
         return false;
      } else {
         this.f37.clear();
         this.f40 = true;
         this.f41 = this.f35.field_1724.field_7512.field_7763;
         this.f42 = -1;
         this.f44 = 0;
         this.f45 = 0L;
         return true;
      }
   }

   public void init() {
      this.m105();

      Client.eventHandler().subscribe(this);
   }

   private void m99(int slot, int hotbarSlot, va.za expectedSlot, va.za expectedHotbar) {
      ArrayDeque result = this.f37;
      va.wa temp1 = new va.wa;

      temp1./* $VF: Unable to resugar constructor */<init>(slot, hotbarSlot, SlotActionType.field_7791, va.za.EMPTY, expectedSlot, expectedHotbar);
      result.add(temp1);
   }

   public boolean isComplete() {
      InventoryPreset v1 = this.activePreset().orElse(null);

      if (v1 != null && this.f35.field_1724 != null) {

         while (true) {

            if (v2 >= 41) {
               return true;
            }

            ta.ua v3 = v1.slot(v2);
            if (!v3.isEmpty() && !v3.matches(this.currentStack(v2))) {
               return false;
            }

            v2++;
         }
      } else {
         return false;
      }
   }

   @EventTarget
   public void onTick(EventRenderModel event) {
      if (this.activePreset().isPresent() && !this.f40 && this.f35.field_1724 != null && this.f35.field_1724.field_7512.method_34255().method_7960()) {

         if (this.isComplete()) {
            this.f39 = null;
            this.m106();
         }
      }

      if (this.f40) {
         if (this.f35.field_1724 != null
            && this.f35.field_1761 != null
            && this.f35.field_1755 instanceof InventoryScreen
            && this.f35.field_1724.field_7512.field_7763 == this.f41) {
            if (System.currentTimeMillis() >= this.f45) {
               if (this.f37.isEmpty() && !this.f35.field_1724.field_7512.method_34255().method_7960()) {
                  if (!this.m101()) {
                     long v8 = System.currentTimeMillis();
                     long temp2 = 3681916521940427263L + -3681916521940427113L;

                     this.f45 = v8 + temp2;
                  }
               } else if (!this.f37.isEmpty() || this.m90()) {
                  va.wa v2 = this.f37.peekFirst();
                  if (v2 != null) {
                     if (v2.slot() >= 0 && v2.slot() < this.f35.field_1724.field_7512.field_7761.size()) {
                        if (!v2.m85(
                           this.f35.field_1724.field_7512.method_34255(),
                           this.f35.field_1724.field_7512.method_7611(v2.slot()).method_7677(),
                           v2.actionType() == SlotActionType.field_7791
                              ? this.f35.field_1724.field_7512.method_7611(logicalToScreenSlot(v2.button())).method_7677()
                              : ItemStack.field_8037
                        )) {
                           this.f37.clear();

                           if (!this.f35.field_1724.field_7512.method_34255().method_7960()) {
                              this.m101();
                           }

                           this.f45 = System.currentTimeMillis() + -4393997076116518886L + 4393997076116519036L;
                        } else {
                           this.f37.pollFirst();
                           this.f35
                              .field_1761
                              .method_2906(this.f35.field_1724.field_7512.field_7763, v2.slot(), v2.button(), v2.actionType(), this.f35.field_1724);
                           this.f44 = 0;
                           long v3 = v2.actionType() != SlotActionType.field_7791 && v2.button() != 1
                              ? 6192089671712661032L + -6192089671712660967L
                              : -2358377244758308013L + 2358377244758308048L;
                           this.f45 = System.currentTimeMillis() + v3 + (long)this.f38.nextInt(31);
                           if (this.f37.isEmpty() && this.f35.field_1724.field_7512.method_34255().method_7960()) {
                              this.f42 = -1;
                           }
                        }
                     } else {
                        this.f37.clear();
                        this.f45 = System.currentTimeMillis() + -4343528220692371904L + 4343528220692372054L;
                     }
                  }
               } else if (this.isComplete()) {
                  this.f40 = false;
                  zp.addChatMessage("Пресет полностью разложен");
                  this.deactivate();
               } else {
                  if (++this.f44 >= 20) {
                     this.f40 = false;
                     zp.addChatMessage(
                        "Нет безопасного перемещения: проверьте количество предметов и свободный слот"
                     );
                  } else {
                     this.f45 = System.currentTimeMillis() + -2484637941088408798L + 2484637941088408898L;
                  }
               }
            }
         } else {
            this.cancelArrange(true);
         }
      }
   }

   public void scrollMissing(int direction, 

      this.f43 = Math.max(0, Math.min(Math.max(0, total - visible), this.f43 + direction));
   }

   public void delete(InventoryPreset preset) {
      if (preset != null) {
         this.f36.remove(key(preset.name()));
         if (this.f39 != null && this.f39.equalsIgnoreCase(preset.name())) {
            this.deactivate();
         }

         try {
            Files.deleteIfExists(this.m107(preset.name()));
         } catch (IOException v5) {
            v5.printStackTrace();
         }
      }
   }

   public int missingScroll(int total, int visible) {
      this.f43 = Math.max(0, Math.min(this.f43, Math.max(0, total - visible)));

      return this.f43;
   }

   private boolean m103(int screenSlot, ItemStack cursor) {
      if (screenSlot >= 0) {

         if (screenSlot < this.f35.field_1724.field_7512.field_7761.size()) {
            Slot v3 = this.f35.field_1724.field_7512.method_7611(screenSlot);
            if (!v3.method_7680(cursor)) {
               return false;
            }

            ItemStack v4 = v3.method_7677();
            return v4.method_7960() || ItemStack.method_31577(v4, cursor) && v4.method_7947() + cursor.method_7947() <= v4.method_7914();
         }
      }

      return false;
   }

   public String activeName() {

      int temp2 = v1 + 1;

      return this.f39;
   }

   private void save(InventoryPreset preset) {
      JsonObject v2 = new JsonObject();
      v2.addProperty("name", preset.name());

      v2.addProperty("createdAt", preset.createdAt());
      JsonArray v3 = new JsonArray();

      for (ta.ua v5 : preset.slots()) {
         JsonObject v6 = new JsonObject();
         v6.addProperty("item", v5.itemId());
         v6.addProperty("count", v5.count());
         if (!v5.displayName().isBlank()) {

            v6.addProperty("displayName", v5.displayName());
         }

         if (!v5.potionSignature().isBlank()) {
            v6.addProperty("potionSignature", v5.potionSignature());
         }

         if (!v5.potionData().isBlank()) {
            v6.add("potionData", JsonParser.parseString(v5.potionData()));
         }

         v3.add(v6);
      }

      v2.add("slots", v3);

      try {
         Files.createDirectories(DIRECTORY);

         try (BufferedWriter v12 = Files.newBufferedWriter(
               this.m107(preset.name()), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            )) {
            GSON.toJson(v2, v12);
         }
      } catch (IOException v11) {
         v11.printStackTrace();
      }
   }

   public InventoryPresetManager.ya stateFor(int logicalSlot) {
      InventoryPreset v2 = this.activePreset().orElse(null);
      if (v2 == null) {
         return va.ya.NONE;
      } else {
         ta.ua result = v2.slot(logicalSlot);

         ta.ua v3 = result;

         if (v3.isEmpty()) {
            return va.ya.CORRECT;
         } else if (v3.matches(this.currentStack(logicalSlot))) {
            return va.ya.CORRECT;
         } else {
            return this.m104(v2).containsKey(v3.matchKey()) ? va.ya.MISSING : va.ya.WRONG_SLOT;
         }
      }
   }

   public void activate(InventoryPreset preset) {
      if (preset == null) {
         
      } else {
         int temp1 = v2 * (v2 + 1) % 2;

         if (temp1 != 0) {
            }

         this.cancelArrange(false);
         this.f39 = preset.name();
         this.f43 = 0;
         this.m106();
      }
   }

   public void deactivate() {
      this.cancelArrange(false);
      this.f39 = null;
      this.f43 = 0;
      int temp3 = v2 * (v2 + 1) % 2;

      if (temp3 != 0) {
         }

      this.m106();
   }

   public List<InventoryPreset> presets() {
      Stream result = this.f36.values().stream();
      Comparator temp1 = Comparator.comparingLong(InventoryPreset::createdAt);

      return result.sorted(temp1.reversed()).toList();
   }

   public static String cleanSearchName(String source) {
      if (source == null) {
         return "";
      } else {
         String v1 = Formatting.method_539(source);
         if (v1 == null) {
            return "";
         } else {
            v1 = DECORATIVE_BRACKETS.matcher(v1).replaceFirst("");
            v1 = v1.replaceAll("\\s+", " ").trim();
            String[] v2 = v1.split(" ");

            if (v2.length >= 3 && v2[0].equalsIgnoreCase(v2[v2.length - 1])) {
               v1 = String.join(" ", Arrays.copyOfRange(v2, 1, v2.length - 1));
            }

            v1 = EDGE_DECORATION.matcher(v1.trim()).replaceAll("").trim();
            return v1.replaceAll("\\s+", " ");
         }
      }
   }

   private void m100() {
      if (this.f35.field_1724 != null) {

         if (this.f35.field_1761 != null && this.f35.field_1755 instanceof InventoryScreen && !this.f35.field_1724.field_7512.method_34255().method_7960()) {
            ItemStack v5 = this.f35.field_1724.field_7512.method_34255();

            int v1 = this.m102(v5);
            if (v1 >= 0) {

               this.f35.field_1761.method_2906(this.f35.field_1724.field_7512.field_7763, v1, 0, SlotActionType.field_7790, this.f35.field_1724);
            }

            return;
         }
      }
   }

   public Map<String, va.xa> missingItems() {
      InventoryPreset v1 = this.activePreset().orElse(null);
      if (v1 == null) {
         return Map.of();
      } else {
         Map v2 = this.m104(v1);
         LinkedHashMap result = new LinkedHashMap;

         result./* $VF: Unable to resugar constructor */<init>();
         LinkedHashMap v3 = result;

         for (Entry v5 : v2.entrySet()) {
            ta.ua v6 = v1.slots().stream().filter(slot -> {
               String var10000x = slot.matchKey();
               Object temp1 = v5.getKey();

               return var10000x.equals(temp1);
            }).findFirst().orElse(ta.ua.EMPTY);
            v3.put((String)v5.getKey(), new va.xa(v6, (Integer)v5.getValue()));
         }

         return v3;
      }
   }

   private boolean m95(int sourceScreen, int targetScreen, ItemStack sourceStack, ItemStack targetStack, int amount) {
      if (amount > 0 && !sourceStack.method_7960() && sourceStack.method_7947() > amount) {
         if (!targetStack.method_7960() && !ItemStack.method_31577(sourceStack, targetStack)) {
            return false;
         } else if (targetStack.method_7947() + amount > sourceStack.method_7914()) {
            return false;
         } else {

            this.f42 = sourceScreen;
            va.za v6 = va.za.m87(sourceStack);
            this.m97(sourceScreen, va.za.EMPTY, v6);

            for (int v7 = 0; v7 < amount; v7++) {
               va.za v8 = v6.m88(sourceStack.method_7947() - v7);
               int v9 = targetStack.method_7947() + v7;
               va.za v10 = v9 == 0 ? va.za.EMPTY : v6.m88(v9);
               this.m98(targetScreen, 1, v8, v10);
            }

            int v14 = sourceStack.method_7947() - amount;
            if (v14 > 0) {

               this.m97(sourceScreen, v6.m88(v14), va.za.EMPTY);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   private boolean m91(InventoryPreset preset, int target) {
      ta.ua v3 = preset.slot(target);

      ItemStack v4 = this.currentStack(target);
      if (!v3.isEmpty() && !v3.matches(v4)) {

         if (this.m92(preset, v3, target, v4)) {
            return true;
         }
      }

      return false;
   }

   public ItemStack currentStack(int logicalSlot) {
      if (this.f35.field_1724 == null) {
         return ItemStack.field_8037;
      } else if (logicalSlot >= 0 && logicalSlot < 36) {
         return this.f35.field_1724.method_31548().method_5438(logicalSlot);
      } else if (logicalSlot >= 36 && logicalSlot < 40) {

         return this.f35.field_1724.method_6118(ARMOR[logicalSlot - (36)]);
      } else {
         return logicalSlot == 40 ? this.f35.field_1724.method_6079() : ItemStack.field_8037;
      }
   }

   private Path m107(String name) {
      Path result = DIRECTORY;
      String temp1 = m108(name);

      temp1 = temp1.toLowerCase(Locale.ROOT);

      String v2 = temp1.replace((char)v7, (char)(95));
      return result.resolve(v2 + ".json");
   }

   private void m106() {
      try {
         Files.createDirectories(DIRECTORY);
         Path result = CURRENT_FILE;
         String temp1 = this.f39 == null ? "" : this.f39;
         Charset temp2 = StandardCharsets.UTF_8;

         OpenOption[] v5 = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

         Files.writeString(result, temp1, temp2, v5);
      } catch (IOException v4) {
         v4.printStackTrace();
      }
   }

   private int m96(InventoryPreset preset, ta.ua wanted, int target, boolean requireEnoughCount) {
      for (int v5 = 0; v5 < 41; v5++) {

         if (v5 != target) {

            ItemStack v6 = this.currentStack(v5);
            if (wanted.sameItem(v6) && (!requireEnoughCount || v6.method_7947() >= wanted.count())) {
               ta.ua v7 = preset.slot(v5);
               if (v7.isEmpty() || !v7.matches(v6)) {
                  return v5;
               }
            }
         }
      }

      return -1;
   }

   private boolean m93(int source, int target, ItemStack sourceStack, ItemStack targetStack) {
      int v5 = logicalToScreenSlot(source);
      int v6 = logicalToScreenSlot(target);
      if (v5 >= 0 && v6 >= 0) {
         this.f42 = -1;
         va.za v7 = va.za.m87(sourceStack);
         va.za v8 = va.za.m87(targetStack);
         if (target >= 0) {

            if (target < 9) {
               this.m99(v5, target, v7, v8);
               return true;
            }
         }

         if (source >= 0 && source < 9) {
            this.m99(v6, source, v8, v7);
            return true;
         } else {
            int v9 = this.m94();
            if (v9 < 0) {

               return false;
            } else {
               int v10 = logicalToScreenSlot(v9);
               va.za v11 = va.za.m87(this.currentStack(v9));
               this.m99(v5, v9, v7, v11);
               this.m99(v6, v9, v8, v7);
               this.m99(v5, v9, v11, v8);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public InventoryPreset saveCurrent(String requestedName) {
      if (this.f35.field_1724 == null) {
         return null;
      } else {
         String v2 = m108(requestedName);
         if (v2.isBlank()) {
            return null;
         } else {
            ArrayList v3 = new ArrayList(41);

            for (int v4 = 0; v4 < 36; v4++) {
               v3.add(ta.ua.fromStack(this.f35.field_1724.method_31548().method_5438(v4)));
            }

            for (EquipmentSlot v7 : ARMOR) {
               v3.add(ta.ua.fromStack(this.f35.field_1724.method_6118(v7)));
            }

            v3.add(ta.ua.fromStack(this.f35.field_1724.method_6079()));
            InventoryPreset v10 = new InventoryPreset(v2, System.currentTimeMillis(), v3);
            this.f36.put(key(v2), v10);

            this.save(v10);
            return v10;
         }
      }
   }

   private static String key(String name) {

      return name == null ? "" : name.toLowerCase(Locale.ROOT);
   }

   private Map<String, Integer> m104(InventoryPreset preset) {
      LinkedHashMap v2 = new LinkedHashMap();
      HashMap v3 = new HashMap();
      ArrayList v4 = new ArrayList();

      for (ta.ua v6 : preset.slots()) {
         if (!v6.isEmpty()) {
            v2.merge(v6.matchKey(), v6.count(), Integer::sum);
            
         }
      }

      for (int v12 = 0; v12 < 41; v12++) {
         ItemStack v15 = this.currentStack(v12);

         if (!v15.method_7960()) {
            v4.stream().filter(variant -> {
               int v2x = -324530811;
               int temp2 = v2x * (v2x + 1) % 2;

               if (temp2 != 0) {
                  }

               return variant.sameItem(v15);
            }).findFirst().ifPresent(variant -> {
               String var10001x = variant.matchKey();
               Integer temp2 = v15.method_7947();
               BiFunction temp3 = Integer::sum;

               v3.merge(var10001x, temp2, temp3);
            });
         }
      }

      if (this.f35.field_1724 != null) {
         ItemStack v13 = this.f35.field_1724.field_7512.method_34255();
         if (!v13.method_7960()) {
            v4.stream().filter(variant -> {

               return variant.sameItem(v13);
            }).findFirst().ifPresent(variant -> {
               String var10001x = variant.matchKey();
               Integer temp2 = v13.method_7947();

               v3.merge(var10001x, temp2, Integer::sum);
            });
         }
      }

      LinkedHashMap v14 = new LinkedHashMap();

      for (Object result : v2.entrySet()) {

         Entry v7 = (Entry)result;
         int v8 = (Integer)v7.getValue() - v3.getOrDefault(v7.getKey(), 0);
         if (v8 > 0) {
            v14.put((String)v7.getKey(), v8);
         }
      }

      return v14;
   }

   @Environment(EnvType.CLIENT)
   private static record wa(int slot, int button, SlotActionType actionType, va.za expectedCursor, va.za expectedSlot, va.za expectedHotbar) {
      private final int f25;
      private final InventoryPresetManager.za f30;
      private final SlotActionType f27;
      private final InventoryPresetManager.za f28;
      private final InventoryPresetManager.za f29;
      private final int f26;

      private wa(int slot, int button, SlotActionType actionType, va.za expectedCursor, va.za expectedSlot, va.za expectedHotbar) {
         this.f25 = slot;
         this.f26 = button;
         this.f27 = actionType;
         this.f28 = expectedCursor;
         this.f29 = expectedSlot;
         this.f30 = expectedHotbar;
      }

      public InventoryPresetManager.za expectedHotbar() {
         return this.f30;
      }

      public InventoryPresetManager.za expectedCursor() {

         return this.f28;
      }

      public int slot() {
         return this.f25;
      }

      private boolean m85(ItemStack cursor, ItemStack slotStack, ItemStack hotbarStack) {
         boolean result;
         if (!this.f28.m89(cursor) || !this.f29.m89(slotStack) || this.f27 == SlotActionType.field_7791 && !this.f30.m89(hotbarStack)) {
            result = false;

            
         } else {
            result = true;
         }

         return result;
      }

      public SlotActionType actionType() {

         return this.f27;
      }

      public 

         return this.f26;
      }

      @Override
      public final boolean equals(Object o) {

         return ObjectMethods.bootstrap<"equals",va.wa,"slot;button;actionType;expectedCursor;expectedSlot;expectedHotbar",va.wa::f25,va.wa::f26,va.wa::f27,va.wa::f28,va.wa::f29,va.wa::f30>(
            this, o
         );
      }

      public InventoryPresetManager.za expectedSlot() {

         return this.f29;
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static record xa(ta.ua entry, int count) {
      private final InventoryPreset.ua f31;

      public xa(ta.ua entry, int count) {
         this.f31 = entry;
         this.count = count;
      }

      @Override
      public final 

         return ObjectMethods.bootstrap<"hashCode",va.xa,"entry;count",va.xa::f31,va.xa::count>(this);
      }

      public ItemStack stack() {
         return this.f31.toStack();
      }

      public InventoryPreset.ua entry() {
         return this.f31;
      }

      @Override
      public final String toString() {

         return ObjectMethods.bootstrap<"toString",va.xa,"entry;count",va.xa::f31,va.xa::count>(this);
      }

      
   }

   @Environment(EnvType.CLIENT)
   public static enum ya {
      NONE,
      CORRECT,
      WRONG_SLOT,
      MISSING;
   }

   @Environment(EnvType.CLIENT)
   private static record za(String itemId, int count, String components, boolean empty) {
      private static final InventoryPresetManager.za EMPTY = new va.za("", 0, "", true);
      private final String f32;
      private final String f33;
      private final boolean f34;

      private static va.za m87(ItemStack stack) {
         if (stack != null && !stack.method_7960()) {
            va.za result = new va.za;

            result./* $VF: Unable to resugar constructor */<init>(
               Registries.field_41178.method_10221(stack.method_7909()).toString(), stack.method_7947(), stack.method_57353().toString(), false
            );
            return result;
         } else {
            return EMPTY;
         }
      }

      private InventoryPresetManager.za m88(int newCount) {
         va.za result;
         if (!this.f34 && newCount > 0) {
            result = new va.za;

            result./* $VF: Unable to resugar constructor */<init>(this.f32, newCount, this.f33, false);
         } else {
            result = EMPTY;
         }

         return result;
      }

      @Override
      public final int hashCode() {
         int result = ObjectMethods.bootstrap<"hashCode",va.za,"itemId;count;components;empty",va.za::f32,va.za::count,va.za::f33,va.za::f34>(this);

         return result;
      }

      public String components() {

         return this.f33;
      }

      @Override
      public final String toString() {
         String result = ObjectMethods.bootstrap<"toString",va.za,"itemId;count;components;empty",va.za::f32,va.za::count,va.za::f33,va.za::f34>(this);

         return result;
      }

      public boolean empty() {

         return this.f34;
      }

      public 

         return this.count;
      }

      private boolean m89(ItemStack stack) {
         if (this.f34) {
            return stack == null || stack.method_7960();
         } else if (stack != null && !stack.method_7960() && stack.method_7947() == this.count) {

            if (this.f32.equals(Registries.field_41178.method_10221(stack.method_7909()).toString())) {

               if (this.f33.equals(stack.method_57353().toString())) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      public String itemId() {

         return this.f32;
      }

      private za(String itemId, int count, String components, boolean empty) {
         this.f32 = itemId;
         this.count = count;
         this.f33 = components;
         this.f34 = empty;
      }

      
   }
}
