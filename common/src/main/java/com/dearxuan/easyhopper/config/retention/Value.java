package com.dearxuan.easyhopper.config.retention;

import com.dearxuan.easyhopper.anno.Environment;
import com.dearxuan.easyhopper.anno.EnvType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Environment(EnvType.BOTH)
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    float min() default 0;
    float max() default 255;
}
