package ru.white.module;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.NotImplementedException;
import ru.white.event.EventManager;
import ru.white.utils.ClientUtil;
import ru.white.auth.StrDec;
import ru.white.Client;
import ru.white.module.impl.render.InterFace;
import ru.white.utils.animation.satoshi.Animation;
import ru.white.utils.animation.satoshi.ep;
import ru.white.module.setting.Setting;
import ru.white.utils.IMinecraft;

@Environment(EnvType.CLIENT)
public abstract class Module implements IMinecraft {
   private boolean f200;
   public Animation animation12;
   public final ru.white.utils.animation.Animation toggleAnimation;
   public Animation animation16;
   public final ru.white.utils.animation.Animation animtoP8;
   private Category category;
   public final ru.white.utils.animation.Animation expandedAnimation;
   public Animation animation2;
   public Animation animation3;
   private ModuleAnnotation f194;
   private boolean f197;
   public Animation animation14;
   public Animation animation1;
   private final List<Setting<?>> settings = new ObjectArrayList();
   private boolean f198;
   public final ru.white.utils.animation.Animation hoverModule;
   private boolean enabled;
   public final ru.white.utils.animation.Animation animation_new_1;
   public Animation animation15;
   private String name;
   public Animation animation;
   public final ru.white.utils.animation.Animation animation_new = new ru.white.utils.animation.Animation();
   private String f196;
   public final ru.white.utils.animation.Animation animtoP7;
   private String f195;
   private boolean f199;
   private int key;

   public void setEnabled(boolean enabled, boolean notification) {
      if (this.enabled != enabled && (this.f198 || enabled)) {
         this.enabled = enabled;
         if (enabled) {
            this.m216();
            if (mc.field_1724 != null && notification) {
               String v3 = "tone_enable";
               if (hh.getInstance().typeNotify.is("Второй")) {
                  v3 = "notify_enable";
               } else {

                  if (hh.getInstance().typeNotify.is("Третий")) {
                     v3 = "akrien_enable";
                  } else if (hh.getInstance().typeNotify.is("Четвертый")) {
                     v3 = "enable3";
                     
                  } else if (hh.getInstance().typeNotify.is("Пятый")) {
                     v3 = "enable4";
                  }
               }

               br.playSound_wav(v3, hh.getInstance().volume.getValue());
            }
         } else {
            this.m217();
            if (mc.field_1724 != null && notification) {
               String v6 = "tone_disable";
               if (hh.getInstance().typeNotify.is("Второй")) {
                  v6 = "notify_disable";
               } else if (hh.getInstance().typeNotify.is("Третий")) {
                  v6 = "akrien_disable";
               } else if (hh.getInstance().typeNotify.is("Четвертый")) {
                  v6 = "disable3";
               } else if (hh.getInstance().typeNotify.is("Пятый")) {
                  v6 = "disable4";
               }

               br.playSound_wav(v6, hh.getInstance().volume.getValue());
            }
         }
      }
   }

   @Generated
   public void setAutoEnabled(boolean autoEnabled) {
      this.f197 = autoEnabled;
   }

   @Generated
   public void setBigName(String bigName) {
      this.f195 = bigName;
   }

   @Generated
   public boolean isAllowDisable() {
      int temp3 = v2 + 1;

      return this.f198;
   }

   @Generated
   public List<Setting<?>> getSettings() {

      return this.settings;
   }

   @Generated
   public void setAnimation1(Animation animation1) {
      this.animation1 = animation1;

      
   }

   @Generated
   public void setAnimation3(Animation animation3) {
      this.animation3 = animation3;
      
   }

   @Generated
   public void setDesc(String desc) {

      this.f196 = desc;
   }

   @Generated
   public void setAnimation(Animation animation) {
      this.animation = animation;
   }

   @Generated
   public ModuleAnnotation getModuleInfo() {
      return this.f194;
   }

   @Generated
   public Animation getAnimation() {

      return this.animation;
   }

   @Generated
   protected boolean canEqual(Object other) {
      boolean result = other instanceof Module;

      return result;
   }

   private void m215() {
      EventManager result = Client.eventHandler();

      result.unsubscribe(this);
   }

   protected void onEnable() {
   }

   private void m216() {
      if (mc.field_1724 != null) {
         this.onEnable();
         
      }

      this.m214();
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Module v2)) {
         return false;
      } else if (!v2.canEqual(this)) {
         return false;
      } else if (this.isEnabled() != v2.isEnabled()) {
         return false;
      } else if (this.isAutoEnabled() != v2.isAutoEnabled()) {
         return false;
      } else if (this.isAllowDisable() != v2.isAllowDisable()) {
         return false;
      } else if (this.isHidden() != v2.isHidden()) {
         return false;
      } else if (this.getKey() != v2.getKey()) {
         return false;
      } else if (this.isExpanded() != v2.isExpanded()) {
         return false;
      } else {
         List v3 = this.getSettings();
         List v4 = v2.getSettings();
         if (v3 == null ? v4 == null : v3.equals(v4)) {
            ModuleAnnotation v5 = this.getModuleInfo();
            ModuleAnnotation v6 = v2.getModuleInfo();
            if (v5 == null ? v6 == null : v5.equals(v6)) {
               String v7 = this.getName();
               String v8 = v2.getName();
               if (v7 == null ? v8 == null : v7.equals(v8)) {
                  String v9 = this.getBigName();
                  String v10 = v2.getBigName();
                  if (v9 == null ? v10 == null : v9.equals(v10)) {
                     String v11 = this.getDesc();
                     String v12 = v2.getDesc();
                     if (v11 == null ? v12 == null : v11.equals(v12)) {
                        Category v13 = this.getCategory();
                        Category v14 = v2.getCategory();
                        if (v13 == null) {
                           if (v14 != null) {

                              return false;
                           }
                        } else if (!v13.equals(v14)) {
                           return false;
                        }

                        ru.white.utils.animation.Animation v15 = this.getAnimation_new();
                        ru.white.utils.animation.Animation v16 = v2.getAnimation_new();
                        if (v15 == null ? v16 == null : v15.equals(v16)) {
                           ru.white.utils.animation.Animation v17 = this.getAnimation_new_1();
                           ru.white.utils.animation.Animation v18 = v2.getAnimation_new_1();
                           if (v17 == null ? v18 == null : v17.equals(v18)) {
                              ru.white.utils.animation.Animation v19 = this.getToggleAnimation();
                              ru.white.utils.animation.Animation v20 = v2.getToggleAnimation();
                              if (v19 == null ? v20 == null : v19.equals(v20)) {
                                 ru.white.utils.animation.Animation v21 = this.getHoverModule();
                                 ru.white.utils.animation.Animation v22 = v2.getHoverModule();
                                 if (v21 == null ? v22 == null : v21.equals(v22)) {
                                    ru.white.utils.animation.Animation v23 = this.getExpandedAnimation();
                                    ru.white.utils.animation.Animation v24 = v2.getExpandedAnimation();
                                    if (v23 == null) {

                                       if (v24 != null) {
                                          return false;
                                       }
                                    } else if (!v23.equals(v24)) {
                                       return false;
                                    }

                                    ru.white.utils.animation.Animation v25 = this.getAnimtoP7();
                                    ru.white.utils.animation.Animation v26 = v2.getAnimtoP7();
                                    if (v25 == null ? v26 == null : v25.equals(v26)) {
                                       ru.white.utils.animation.Animation v27 = this.getAnimtoP8();
                                       ru.white.utils.animation.Animation v28 = v2.getAnimtoP8();
                                       if (v27 == null ? v28 == null : v27.equals(v28)) {
                                          Animation v29 = this.getAnimation();
                                          Animation v30 = v2.getAnimation();
                                          if (v29 == null ? v30 == null : v29.equals(v30)) {
                                             Animation v31 = this.getAnimation1();
                                             Animation v32 = v2.getAnimation1();
                                             if (v31 == null ? v32 == null : v31.equals(v32)) {
                                                Animation v33 = this.getAnimation2();
                                                Animation v34 = v2.getAnimation2();
                                                if (v33 == null ? v34 == null : v33.equals(v34)) {
                                                   Animation v35 = this.getAnimation3();
                                                   Animation v36 = v2.getAnimation3();
                                                   if (v35 == null ? v36 == null : v35.equals(v36)) {
                                                      Animation v37 = this.getAnimation12();
                                                      Animation v38 = v2.getAnimation12();
                                                      if (v37 == null ? v38 == null : v37.equals(v38)) {
                                                         Animation v39 = this.getAnimation14();
                                                         Animation v40 = v2.getAnimation14();
                                                         if (v39 == null ? v40 == null : v39.equals(v40)) {
                                                            Animation v41 = this.getAnimation15();
                                                            Animation v42 = v2.getAnimation15();
                                                            if (v41 == null ? v42 == null : v41.equals(v42)) {
                                                               Animation v43 = this.getAnimation16();
                                                               Animation v44 = v2.getAnimation16();
                                                               return v43 == null ? v44 == null : v43.equals(v44);
                                                            } else {
                                                               return false;
                                                            }
                                                         } else {
                                                            return false;
                                                         }
                                                      } else {
                                                         return false;
                                                      }
                                                   } else {
                                                      return false;
                                                   }
                                                } else {
                                                   return false;
                                                }
                                             } else {
                                                return false;
                                             }
                                          } else {
                                             return false;
                                          }
                                       } else {
                                          return false;
                                       }
                                    } else {
                                       return false;
                                    }
                                 } else {
                                    return false;
                                 }
                              } else {
                                 return false;
                              }
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public void setEnabled(boolean enabled) {
      this.setEnabled(enabled, true);
      
   }

   @Generated
   public void setAnimation16(Animation animation16) {
      this.animation16 = animation16;
      
   }

   @Generated
   public void setModuleInfo(ModuleAnnotation moduleInfo) {

      this.f194 = moduleInfo;
   }

   @Generated
   public String getBigName() {
      return this.f195;
   }

   @Generated
   public Animation getAnimation3() {

      return this.animation3;
   }

   @Generated
   public ru.white.utils.animation.Animation getAnimtoP8() {

      return this.animtoP8;
   }

   @Generated
   public void setKey(int key) {

      this.key = key;
   }

   @Generated
   public void setHidden(boolean hidden) {

      this.f199 = hidden;
   }

   protected void onDisable() {
   }

   @Generated
   public Animation getAnimation16() {

      return this.animation16;
   }

   @Generated
   public void setAnimation2(Animation animation2) {

      this.animation2 = animation2;
   }

   private void m214() {
      EventManager result = Client.eventHandler();

      result.subscribe(this);
   }

   @Generated
   public Animation getAnimation1() {
      return this.animation1;
   }

   @Generated
   public Animation getAnimation15() {
      return this.animation15;
   }

   @Generated
   public Animation getAnimation2() {
      int temp3 = v2 + 1;

      return this.animation2;
   }

   @Generated
   public String getDesc() {

      return this.f196;
   }

   @Generated
   public void setCategory(Category category) {
      this.category = category;
   }

   @Generated
   public boolean isAutoEnabled() {

      return this.f197;
   }

   @Generated
   public void setAnimation12(Animation animation12) {
      this.animation12 = animation12;
      
   }

   @Generated
   public Animation getAnimation12() {

      return this.animation12;
   }

   @Generated
   public boolean isHidden() {

      int v4 = v1 * (v1 + 1);

      return this.f199;
   }

   @Generated
   public Category getCategory() {

      return this.category;
   }

   @Generated
   public 

      return this.key;
   }

   @Generated
   @Override
   public int hashCode() {
      v2 = v2 * (59) + (this.isEnabled() ? 79 : 97);
      v2 = v2 * (59) + (this.isAutoEnabled() ? 79 : 97);
      v2 = v2 * (59) + (this.isAllowDisable() ? 79 : 97);
      v2 = v2 * (59) + (this.isHidden() ? 79 : 97);
      v2 = v2 * (59) + this.getKey();
      v2 = v2 * (59) + (this.isExpanded() ? 79 : 97);
      List v3 = this.getSettings();
      v2 = v2 * (59) + (v3 == null ? 43 : v3.hashCode());
      ModuleAnnotation v4 = this.getModuleInfo();
      v2 = v2 * (59) + (v4 == null ? 43 : v4.hashCode());
      String v5 = this.getName();
      v2 = v2 * (59) + (v5 == null ? 43 : v5.hashCode());
      String v6 = this.getBigName();
      v2 = v2 * (59) + (v6 == null ? 43 : v6.hashCode());
      String v7 = this.getDesc();
      v2 = v2 * (59) + (v7 == null ? 43 : v7.hashCode());
      Category v8 = this.getCategory();
      v2 = v2 * (59) + (v8 == null ? 43 : v8.hashCode());
      ru.white.utils.animation.Animation v9 = this.getAnimation_new();
      v2 = v2 * (59) + (v9 == null ? 43 : v9.hashCode());
      ru.white.utils.animation.Animation v10 = this.getAnimation_new_1();
      v2 = v2 * (59) + (v10 == null ? 43 : v10.hashCode());
      ru.white.utils.animation.Animation v11 = this.getToggleAnimation();
      v2 = v2 * (59) + (v11 == null ? 43 : v11.hashCode());
      ru.white.utils.animation.Animation v12 = this.getHoverModule();
      v2 = v2 * (59) + (v12 == null ? 43 : v12.hashCode());
      ru.white.utils.animation.Animation v13 = this.getExpandedAnimation();
      v2 = v2 * (59) + (v13 == null ? 43 : v13.hashCode());
      ru.white.utils.animation.Animation v14 = this.getAnimtoP7();
      int result = v2 * (59);

      v2 = result + (v14 == null ? 43 : v14.hashCode());
      ru.white.utils.animation.Animation v15 = this.getAnimtoP8();
      v2 = v2 * (59) + (v15 == null ? 43 : v15.hashCode());
      Animation v16 = this.getAnimation();
      v2 = v2 * (59) + (v16 == null ? 43 : v16.hashCode());
      Animation v17 = this.getAnimation1();
      v2 = v2 * (59) + (v17 == null ? 43 : v17.hashCode());
      Animation v18 = this.getAnimation2();
      v2 = v2 * (59) + (v18 == null ? 43 : v18.hashCode());
      Animation v19 = this.getAnimation3();
      v2 = v2 * (59) + (v19 == null ? 43 : v19.hashCode());
      Animation v20 = this.getAnimation12();
      v2 = v2 * (59) + (v20 == null ? 43 : v20.hashCode());
      Animation v21 = this.getAnimation14();
      v2 = v2 * (59) + (v21 == null ? 43 : v21.hashCode());
      Animation v22 = this.getAnimation15();
      v2 = v2 * (59) + (v22 == null ? 43 : v22.hashCode());
      Animation v23 = this.getAnimation16();
      return v2 * (59) + (v23 == null ? 43 : v23.hashCode());
   }

   @Generated
   public Animation getAnimation14() {
      return this.animation14;
   }

   @Generated
   public ru.white.utils.animation.Animation getAnimation_new() {
      return this.animation_new;
   }

   @Generated
   public void setName(String name) {
      this.name = name;
      int result = v2 * (v2 + 1) % 2;

      if (result != 0) {
         }
   }

   @Generated
   public ru.white.utils.animation.Animation getExpandedAnimation() {

      return this.expandedAnimation;
   }

   public void setExpanded(boolean expanded) {

      this.f200 = expanded;
   }

   private void m217() {

      if (mc.field_1724 != null) {
         this.onDisable();
      }

      this.m215();
   }

   @Generated
   public boolean isEnabled() {

      return this.enabled;
   }

   public Module() {
      this.animation_new_1 = new ru.white.utils.animation.Animation();
      this.toggleAnimation = new ru.white.utils.animation.Animation();
      this.hoverModule = new ru.white.utils.animation.Animation();
      this.expandedAnimation = new ru.white.utils.animation.Animation();
      this.animtoP7 = new ru.white.utils.animation.Animation();
      this.animtoP8 = new ru.white.utils.animation.Animation();
      this.animation = new ep(300, 1.0);
      this.animation1 = new ep(300, 1.0);
      this.animation2 = new ep(300, 1.0);
      this.animation3 = new ep(300, 1.0);
      this.animation12 = new ep(300, 1.0);
      this.animation14 = new ep(300, 1.0);
      this.animation15 = new ep(300, 1.0);
      this.animation16 = new ep(300, 1.0);
      Class v1 = this.getClass();
      ModuleAnnotation v2 = v1.getAnnotation(hf.class);
      if (v2 == null) {
         String v3 = v1.getSimpleName();
         throw new NotImplementedException("@ModuleInfo annotation not found on " + v3);
      } else {
         this.f194 = v2;
         this.name = v2.name().trim().replaceAll(" ", "");
         this.f195 = v2.name();
         this.f196 = v2.desc();
         this.category = v2.category();
         this.f197 = v2.autoEnabled();
         this.f198 = v2.allowDisable();
         this.f199 = v2.hidden();
         this.key = v2.key();
         this.setup();
      }
   }

   @Generated
   public ru.white.utils.animation.Animation getToggleAnimation() {
      return this.toggleAnimation;
   }

   @Generated
   public ru.white.utils.animation.Animation getAnimation_new_1() {

      return this.animation_new_1;
   }

   @Generated
   public ru.white.utils.animation.Animation getAnimtoP7() {
      return this.animtoP7;
   }

   public void setup() {
   }

   @Generated
   public ru.white.utils.animation.Animation getHoverModule() {
      return this.hoverModule;
   }

   @Generated
   public void setAnimation14(Animation animation14) {
      this.animation14 = animation14;
   }

   @Generated
   @Override
   public String toString() {
      String result = String.valueOf(this.getSettings());
      String temp1 = String.valueOf(this.getModuleInfo());
      String temp2 = this.getName();
      String temp3 = this.getBigName();
      String temp4 = this.getDesc();
      String temp5 = String.valueOf(this.getCategory());
      boolean var10006 = this.isEnabled();
      boolean var10007 = this.isAutoEnabled();
      boolean var10008 = this.isAllowDisable();
      boolean var10009 = this.isHidden();
      int var10010 = this.getKey();
      String var10011 = String.valueOf(this.getAnimation_new());
      String var10012 = String.valueOf(this.getAnimation_new_1());
      String var10013 = String.valueOf(this.getToggleAnimation());
      String var10014 = String.valueOf(this.getHoverModule());
      String var10015 = String.valueOf(this.getExpandedAnimation());
      String var10016 = String.valueOf(this.getAnimtoP7());
      String var10017 = String.valueOf(this.getAnimtoP8());
      String var10018 = String.valueOf(this.getAnimation());
      String var10019 = String.valueOf(this.getAnimation1());
      String var10020 = String.valueOf(this.getAnimation2());
      String var10021 = String.valueOf(this.getAnimation3());
      String var10022 = String.valueOf(this.getAnimation12());
      String var10023 = String.valueOf(this.getAnimation14());
      String var10024 = String.valueOf(this.getAnimation15());
      String var10025 = String.valueOf(this.getAnimation16());
      boolean v1 = this.isExpanded();
      String v2 = var10025;
      String v3 = var10024;
      String v4 = var10023;
      String v5 = var10022;
      String v6 = var10021;
      String v7 = var10020;
      String v8 = var10019;
      String v9 = var10018;
      String v10 = var10017;
      String v11 = var10016;
      String v12 = var10015;
      String v13 = var10014;
      String v14 = var10013;
      String v15 = var10012;
      String v16 = var10011;
      int v17 = var10010;
      boolean v18 = var10009;
      boolean v19 = var10008;
      boolean v20 = var10007;
      boolean v21 = var10006;
      String v22 = temp5;
      String v23 = temp4;
      String v24 = temp3;
      String v25 = temp2;
      String v26 = temp1;
      String v27 = result;
      StringBuilder v31 = new StringBuilder()
         .append("Module(settings=")
         .append((Object)v27)
         .append(", moduleInfo=")
         .append((Object)v26)
         .append(", name=")
         .append((Object)v25)
         .append(", bigName=")
         .append((Object)v24)
         .append(", desc=")
         .append((Object)v23)
         .append(", category=")
         .append((Object)v22)
         .append(", enabled=")
         .append(v21);

      StringBuilder v32 = v31.append(StrDec.d("6lssdQdhhfuN8zt//Rk=", v34))
         .append(v20)
         .append(", allowDisable=")
         .append(v19)
         .append(", hidden=")
         .append(v18)
         .append(", key=")
         .append(v17)
         .append(", animation_new=")
         .append((Object)v16)
         .append(", animation_new_1=")
         .append((Object)v15)
         .append(", toggleAnimation=")
         .append((Object)v14)
         .append(", hoverModule=")
         .append((Object)v13)
         .append(", expandedAnimation=")
         .append((Object)v12)
         .append(", animtoP7=")
         .append((Object)v11)
         .append(", animtoP8=")
         .append((Object)v10)
         .append(", animation=")
         .append((Object)v9)
         .append(", animation1=")
         .append((Object)v8)
         .append(", animation2=")
         .append((Object)v7)
         .append(", animation3=")
         .append((Object)v6);

      StringBuilder v33 = v32.append(", animation12=")
         .append((Object)v5)
         .append(", animation14=")
         .append((Object)v4)
         .append(", animation15=")
         .append((Object)v3);

      return v33.append(", animation16=")
         .append((Object)v2)
         .append(", expanded=")
         .append(v1)
         .append(")")
         .toString();
   }

   @Generated
   public String getName() {

      return this.name;
   }

   public boolean isExpanded() {
      return this.f200;
   }

   @Generated
   public void setAnimation15(Animation animation15) {
      this.animation15 = animation15;
   }

   public void toggle() {

      boolean v4;
      if (!this.enabled) {

         v4 = true;
      } else {
         v4 = false;
      }

      this.setEnabled(v4);
   }

   @Generated
   public void setAllowDisable(boolean allowDisable) {
      this.f198 = allowDisable;

      
   }

   
}
