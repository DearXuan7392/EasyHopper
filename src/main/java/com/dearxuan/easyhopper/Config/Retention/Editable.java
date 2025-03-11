package com.dearxuan.easyhopper.Config.Retention;

public @interface Editable {

    boolean editable() default true;

    boolean setToDefault() default false;
}
