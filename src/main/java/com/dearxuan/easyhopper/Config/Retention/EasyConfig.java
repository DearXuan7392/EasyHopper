package com.dearxuan.easyhopper.Config.Retention;

import com.dearxuan.easyhopper.Config.ModMenu.ModEnv;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EasyConfig {

    Value value() default @Value;

    ModEnv env() default ModEnv.Null;

    String tooltip() default "<name>.tooltip";

    String promptKey() default "";
}