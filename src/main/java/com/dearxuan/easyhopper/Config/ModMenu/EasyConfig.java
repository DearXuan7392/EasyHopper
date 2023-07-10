package com.dearxuan.easyhopper.Config.ModMenu;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EasyConfig {

    String min() default "0";

    String max() default "256";

    ModEnv env() default ModEnv.ServerOnly;
}