package ru.white.manager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.module.impl.player.AutoAccept;
import ru.white.module.impl.player.AutoLeave;
import ru.white.module.impl.player.AutoTool;
import ru.white.module.impl.player.ClickHelper;
import ru.white.module.impl.player.ElytraHelper;
import ru.white.module.impl.player.FreeCamera;
import ru.white.module.impl.player.FreeLook;
import ru.white.module.impl.movement.InventoryMove;
import ru.white.module.impl.player.LockSlot;
import ru.white.module.impl.player.NoDelay;
import ru.white.module.impl.player.NoPush;
import ru.white.module.impl.player.PvpSafe;
import ru.white.module.impl.player.UseWorldTracker;
import ru.white.module.impl.player.AuctionHelper;
import ru.white.event.EventTarget;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.combat.TriggerBot;
import ru.white.module.impl.combat.AimAssist;
import ru.white.module.impl.combat.AntiBot;
import ru.white.module.impl.combat.AttackAura;
import ru.white.module.impl.combat.AutoSwap;
import ru.white.module.impl.combat.AutoTotem;
import ru.white.module.impl.combat.Criticals;
import ru.white.module.impl.combat.CrystalAura;
import ru.white.module.impl.combat.HitBoxes;
import ru.white.module.impl.combat.MaceHelper;
import ru.white.module.impl.combat.NoFriendDamage;
import ru.white.module.impl.combat.NoSlotChange;
import ru.white.module.impl.combat.AimBot;
import ru.white.module.impl.combat.TargetPearl;
import ru.white.module.impl.combat.UseTracker;
import ru.white.module.impl.render.ClickGui;
import ru.white.module.impl.render.Arrows;
import ru.white.module.impl.render.InterFace;
import ru.white.module.impl.movement.Speed;
import ru.white.module.impl.movement.AirStuck;
import ru.white.module.impl.movement.NoSlow;
import ru.white.module.impl.movement.NoWeb;
import ru.white.module.impl.movement.Spider;
import ru.white.module.impl.movement.Sprint;
import ru.white.module.impl.render.Emotions;
import ru.white.module.impl.render.FOVChanger;
import ru.white.module.impl.render.GlassHands;
import ru.white.module.impl.render.NameTag;
import ru.white.module.impl.render.Particles;
import ru.white.module.impl.render.EchoForm;
import ru.white.module.impl.render.KillEffect;
import ru.white.module.impl.render.Trails;
import ru.white.module.impl.render.EntityEsp;
import ru.white.module.impl.render.NoRender;
import ru.white.module.impl.render.FireFlies;
import ru.white.module.impl.render.Optimization;
import ru.white.module.impl.render.Trajectories;
import ru.white.module.impl.render.ij;
import ru.white.module.impl.render.Rains;
import ru.white.module.impl.render.Gamma;
import ru.white.module.impl.render.GazeThreads;
import ru.white.module.impl.render.WorldCubes;
import ru.white.module.impl.render.WorldTweaks;
import ru.white.module.impl.render.GlassBlock;
import ru.white.module.impl.render.ScanWorld;
import ru.white.module.impl.render.Hands;
import ru.white.module.impl.render.ShaderESP;
import ru.white.module.impl.render.HealthAlert;
import ru.white.module.impl.render.ShaderSky;
import ru.white.module.impl.render.Atmosphere;
import ru.white.module.impl.render.HoldMyItems;
import ru.white.module.impl.render.SoulChain;
import ru.white.module.impl.render.BladeStorm;
import ru.white.module.impl.render.HorizonLines;
import ru.white.module.impl.render.BlockESP;
import ru.white.module.impl.render.ItemPhysics;
import ru.white.module.impl.render.SoulSwarm;
import ru.white.module.impl.render.ChinaHat;
import ru.white.module.impl.render.JumpCircle;
import ru.white.module.impl.render.Svetoch;
import ru.white.module.impl.render.ColorGrade;
import ru.white.module.impl.render.CrossHair;
import ru.white.module.impl.render.SwingAnimation;
import ru.white.module.impl.render.JumpCube;
import ru.white.module.impl.render.TargetEsp;
import ru.white.module.impl.render.DiscordRPC;
import ru.white.module.impl.render.TotemGhost;
import ru.white.module.impl.misc.AutoStorage;
import ru.white.module.impl.misc.ChestStealer;
import ru.white.module.impl.misc.DanjHelper;
import ru.white.module.impl.misc.FakePlayer;
import ru.white.module.impl.misc.GlassFarmer;
import ru.white.module.impl.misc.ItemScroller;
import ru.white.module.impl.misc.NameProtect;
import ru.white.module.impl.misc.PotionFarmer;
import ru.white.module.impl.misc.AppleFarmer;
import ru.white.module.impl.misc.ReportHelper;
import ru.white.module.impl.misc.SPJoiner;
import ru.white.module.impl.misc.AuraCrafter;
import ru.white.module.impl.misc.UnHook;
import ru.white.module.impl.misc.WardenHelper;
import ru.white.module.impl.misc.AutoClanUpgrade;
import ru.white.module.impl.misc.AutoDuel;
import ru.white.module.impl.misc.AutoInvest;
import ru.white.module.impl.misc.AutoMsg;
import ru.white.module.impl.misc.AutoPotion;
import ru.white.module.impl.misc.AutoResell;
import ru.white.event.impl.EventPostMotion;

@Environment(EnvType.CLIENT)
public final class ModuleManager extends LinkedHashMap<Class<? extends Module>, Module> {
   public void addSorted(Module... modules) {
      Arrays.stream(modules).forEach(module -> {
         this.put((Class<? extends Module>)module.getClass(), module);

         
      });
   }

   public <T extends Module> T get(String name) {
      Object result = this.values().stream().filter(module -> {
         String var10000x = module.getName();

         return var10000x.equalsIgnoreCase(name);
      }).map(module -> {

         return (Module)module;
      }).findFirst().orElse(null);

      return (T)result;
   }

   public Module getModule(String name) {
      Module result = this.values().stream().filter(module -> {

         String var10000x = module.getName();

         return var10000x.equalsIgnoreCase(name);
      }).findFirst().orElse(null);

      return result;
   }

   @EventTarget
   public void onKeyboardPress(EventPostMotion event) {
      this.values().stream().filter(module -> {
         int var10000x = module.getKey();
         int temp1 = event.getKey();

         return var10000x == temp1;
      }).forEach(Module::toggle);
      
   }

   public <T extends Module> T get(Class<T> clazz) {
      Module result = this.values().stream().filter(module -> {

         return clazz.isAssignableFrom(module.getClass());
      }).map(clazz::cast).findFirst().orElse(null);

      return (T)result;
   }

   @Override
   public Collection<Module> values() {
      return super.values().stream().sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
   }

   public void unregister(Module... modules) {
      Stream result = Arrays.stream(modules);

      result.forEach(module -> {
         int v2x = -1577891272;

         this.remove(module.getClass());
      });
   }

   public List<Module> get(Category category) {
      List result = this.values().stream().filter(module -> {
         boolean var10000x;
         if (module.getCategory() == category) {
            var10000x = true;
            
         } else {
            var10000x = false;
         }

         return var10000x;
      }).collect(Collectors.toList());

      return result;
   }

   public void init() {
      Module[] temp1 = new Module[101];
      temp1[0] = new fg();
      temp1[1] = new hg();
      temp1[2] = new ig();
      temp1[3] = new dg();
      temp1[4] = new pg();
      temp1[5] = new ng();
      temp1[6] = new og();
      temp1[7] = new lg();
      temp1[8] = new jg();
      temp1[9] = new kg();
      temp1[10] = new eg();
      temp1[11] = new TriggerBot();
      temp1[12] = new ug();
      temp1[13] = new rg();
      temp1[14] = new mg();
      temp1[15] = new xh();
      temp1[16] = new uh();
      temp1[17] = new wh();
      temp1[18] = new vh();
      temp1[19] = new Speed();
      temp1[20] = new th();
      temp1[21] = new ClickGui();
      temp1[22] = new ek();
      temp1[23] = new dh();
      temp1[24] = new yk();
      temp1[25] = new cj();
      temp1[26] = new NameTag();
      temp1[27] = new dk();
      temp1[28] = new xk();
      temp1[29] = new Emotions();
      temp1[30] = new uj();

      temp1[31] = new GlassHands();
      temp1[32] = new nj();
      temp1[33] = new ok();
      temp1[34] = new pk();
      temp1[35] = new ml();
      temp1[36] = new qi();
      temp1[37] = new jj();
      temp1[38] = new Particles();
      temp1[39] = new pj();
      temp1[40] = new ui();
      temp1[41] = new vi();
      temp1[42] = new vj();
      temp1[43] = new yj();
      temp1[44] = new xi();
      temp1[45] = new bl();
      temp1[46] = new rj();
      temp1[47] = new ll();
      temp1[48] = new hl();
      temp1[49] = new kj();
      temp1[50] = new uk();
      temp1[51] = new ri();
      temp1[52] = new wi();
      temp1[53] = new ej();
      temp1[54] = new vk();
      temp1[55] = new nk();
      temp1[56] = new ij();
      temp1[57] = new ik();
      temp1[58] = new zk();
      temp1[59] = new ak();
      temp1[60] = new qk();
      temp1[61] = new aj();
      temp1[62] = new oj();
      temp1[63] = new qj();
      temp1[64] = new Fov();
      temp1[65] = new hh();
      temp1[66] = new nm();
      temp1[67] = new zi();
      temp1[68] = new ai();
      temp1[69] = new ni();
      temp1[70] = new mi();
      temp1[71] = new ei();
      temp1[72] = new pi();
      temp1[73] = new ki();
      temp1[74] = new fi();
      temp1[75] = new bi();
      temp1[76] = new ci();
      temp1[77] = new yh();
      temp1[78] = new li();
      temp1[79] = new ii();
      temp1[80] = new ji();
      temp1[81] = new oi();
      temp1[82] = new nl();
      temp1[83] = new pm();
      temp1[84] = new lm();
      temp1[85] = new im();
      temp1[86] = new mm();
      temp1[87] = new om();
      temp1[88] = new gm();
      temp1[89] = new cm();
      temp1[90] = new sl();

      temp1[v4] = new am();

      temp1[92] = new qm();
      temp1[93] = new pl();
      temp1[94] = new yl();
      temp1[95] = new km();
      temp1[96] = new tl();
      temp1[97] = new xl();
      temp1[98] = new em();
      temp1[99] = new ul();
      temp1[100] = new vl();
      this.addSorted(temp1);
      this.values().stream().filter(Module::isAutoEnabled).forEach(module -> {
         int v1x = 897899546;
         int var10003x = v1x * (v1x + 1) % 2;

         if (var10003x != 0) {
            }

         module.setEnabled(true, false);
      });
      Client.eventHandler().subscribe(this);
   }

   
}
