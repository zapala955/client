package ru.white.utils.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Vector2f;
import ru.white.module.Module;
import ru.white.auth.StrDec;
import ru.white.utils.animation.Animation;
import ru.white.module.setting.Setting;

@Environment(EnvType.CLIENT)
public class Vf extends Setting<Vector2f> {
   public final Vector2f targetPosition = new Vector2f();
   public boolean active;
   public Animation animationX;
   public final Vector2f size;
   public Animation animationY;
   public final Vector2f position = new Vector2f();
   public boolean structure;
   public boolean lockX;

   public Vf(Module parent, String name, Vector2f value, boolean active) {
      super(parent, name, value);
      this.size = new Vector2f();
      this.active = true;
      this.structure = false;
      this.lockX = false;
      this.animationX = new Animation();
      this.animationY = new Animation();
      this.position.set(value);
      this.targetPosition.set(value);
      this.active = active;
   }

   public Vf(Module parent, String name, Vector2f value, boolean active, boolean structure) {
      super(parent, name, value);
      this.size = new Vector2f();
      this.active = true;
      this.structure = false;
      this.lockX = false;
      this.animationX = new Animation();
      this.animationY = new Animation();
      this.position.set(value);
      this.targetPosition.set(value);
      this.active = active && !structure;
      this.structure = structure;
   }

   public Vector2f getValue() {

      return this.position;
   }

   public vf onSetVisible(Runnable action) {
      return (vf)super.onSetVisible(action);
   }

   public Setting<?> set(Vector2f value) {

      this.position.set(value);
      this.targetPosition.set(value);
      return super.set(value);
   }

   public Vf(Module parent, String name) {
      super(parent, name, new Vector2f());
      this.size = new Vector2f();
      this.active = true;
      this.structure = false;
      this.lockX = false;
      this.animationX = new Animation();
      this.animationY = new Animation();
   }

   public Vf(Module parent, String name, Vector2f value) {
      super(parent, name, value);
      this.size = new Vector2f();
      this.active = true;
      this.structure = false;
      this.lockX = false;
      this.animationX = new Animation();
      this.animationY = new Animation();
      this.position.set(value);
      this.targetPosition.set(value);
   }

   public vf onAction(Runnable action) {

      return (vf)super.onAction(action);
   }

   
}
