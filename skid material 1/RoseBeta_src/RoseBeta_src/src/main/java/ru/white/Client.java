package ru.white;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import lombok.Generated;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import ru.white.event.EventManager;
import ru.white.utils.ClientUtil;
import ru.white.ui.DraggableWidget;
import ru.white.manager.ModuleManager;
import ru.white.auth.Guard;
import ru.white.auth.StrDec;
import ru.white.manager.CommandManager;
import ru.white.manager.Theme;
import ru.white.manager.GuiManager;
import ru.white.manager.rotation.RotationManager;
import ru.white.module.impl.render.ClickGui;
import ru.white.screen.DropDownMenu;
import ru.white.screen.TestGui;
import ru.white.screen.ClickGuiScreen;
import ru.white.utils.render.us;
import ru.white.manager.InventoryPresetManager;
import ru.white.manager.DiscordRPC;
import ru.white.manager.DiscordManager;
import ru.white.manager.ConfigManager;
import ru.white.manager.FriendManager;
import ru.white.utils.system.Bu;

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
   private TestGui testGuiScreen;
   private ConfigManager configManager;
   private if moduleManager;
   private vd componentManager;
   private DropDownMenu dropDownClickGuiScreen;
   private ko clickGuiScreen;
   private DiscordManager discordManager;
   private static final EventManager eventHandler = tc.threadSafe();
   private FriendManager friendManager;
   private final DiscordRPC rpc = new DiscordRPC();
   public static Client get;
   private us render2D;
   private InventoryPresetManager inventoryPresetManager;
   private q commandManager;
   public static String build = "5.0";
   private hb guiManager;

   public void start() {
      eventHandler.registerLambdaFactory("", (lookupInMethod, klass) -> {

         Object[] var10002x = new Object[]{klass, null};

         var10002x[1] = MethodHandles.lookup();
         return (Lookup)lookupInMethod.invoke(null, var10002x);
      });

      bu.register();
   }

   @Generated
   public us render2D() {

      return this.render2D;
   }

   @Generated
   public DiscordRPC rpc() {

      return this.rpc;
   }

   @Generated
   public if moduleManager() {

      return this.moduleManager;
   }

   @Generated
   public ConfigManager configManager() {
      int temp2 = v1 + 1;

      return this.configManager;
   }

   public Screen getClickGuiScreen() {
      ClickGui result;
      if (this.moduleManager != null) {
         result = this.moduleManager.get(ClickGui.class);
      } else {
         result = null;
         int temp1 = v2 * (v2 + 1) % 2;

         if (temp1 != 0) {
            }
      }

      ClickGui v1 = result;
      if (v1 != null && v1.guiType.is("Test")) {
         if (this.testGuiScreen == null) {
            this.testGuiScreen = new TestGui();
         }

         return this.testGuiScreen;
      } else if (v1 != null && v1.guiType.is("Drop Down")) {
         if (this.dropDownClickGuiScreen == null) {
            this.dropDownClickGuiScreen = new DropDownMenu();
         }

         return this.dropDownClickGuiScreen;
      } else {
         return this.clickGuiScreen;
      }
   }

   @Generated
   public ko clickGuiScreen() {
      return this.clickGuiScreen;
   }

   @Generated
   public hb guiManager() {

      return this.guiManager;
   }

   public void onInitializeClient() {
      Guard.init();
      String v1 = build;
      System.out.println("[Rose] Initializing client v" + v1);
      get = this;
      bb.init();
      this.start();
      this.discordManager = new DiscordManager();
      this.discordManager.init();
      this.configManager = new ConfigManager();
      this.configManager.setup();
      this.friendManager = new FriendManager();
      this.friendManager.init();
      this.inventoryPresetManager = new InventoryPresetManager();
      this.inventoryPresetManager.init();
      this.moduleManager = new if();
      this.moduleManager.init();

      this.componentManager = new vd();

      this.componentManager.init();
      this.commandManager = new q();
      this.commandManager.init();
      this.configManager.init();
      this.guiManager = new hb();
      this.guiManager.init();
      this.render2D = new us();
      this.clickGuiScreen = new ko();
      this.dropDownClickGuiScreen = new DropDownMenu();
      this.testGuiScreen = new TestGui();
      Theme v5 = this.guiManager.getCurrentTheme();

      ko.selectedTheme = v5;
      ko.preSelectedTheme = this.guiManager.getCurrentTheme();
      br.playWelcomeSound();
      Runtime.getRuntime().addShutdownHook(new Thread(this::unload));
   }

   @Generated
   public DiscordManager discordManager() {

      return this.discordManager;
   }

   public DiscordManager getDiscordManager() {

      return this.discordManager;
   }

   @Generated
   public FriendManager friendManager() {
      return this.friendManager;
   }

   @Generated
   public DropDownMenu dropDownClickGuiScreen() {

      return this.dropDownClickGuiScreen;
   }

   public static Client get() {

      return get;
   }

   public void unload() {

      if (this.configManager != null) {
         this.configManager.autoSave();
      }
   }

   @Generated
   public vd componentManager() {

      return this.componentManager;
   }

   @Generated
   public TestGui testGuiScreen() {

      return this.testGuiScreen;
   }

   @Generated
   public static EventManager eventHandler() {

      return eventHandler;
   }

   @Generated
   public InventoryPresetManager inventoryPresetManager() {
      int temp2 = v1 + 1;

      return this.inventoryPresetManager;
   }

   @Generated
   public q commandManager() {

      temp1 %= 2;

      if (temp1 != 0) {
         }

      return this.commandManager;
   }

   
}
