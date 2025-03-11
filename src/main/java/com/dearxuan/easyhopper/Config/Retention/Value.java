package com.dearxuan.easyhopper.Config.Retention;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    float min() default 0;
    float max() default 255;
}
