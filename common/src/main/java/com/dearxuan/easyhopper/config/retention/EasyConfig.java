package com.dearxuan.easyhopper.config.retention;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EasyConfig {

    Value value() default @Value;

    String tooltip() default "<modid>.<name>.tooltip";

    boolean allowInGame() default true;
}