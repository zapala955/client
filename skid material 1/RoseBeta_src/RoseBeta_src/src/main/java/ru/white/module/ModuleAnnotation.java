package ru.white.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Environment(EnvType.CLIENT)
public @interface ModuleAnnotation {
   boolean allowDisable() default true;

   String desc() default "У модуля нету описания";

   String name();

   Category category();

   int key() default -1;

   boolean hidden() default false;

   boolean autoEnabled() default false;
}
