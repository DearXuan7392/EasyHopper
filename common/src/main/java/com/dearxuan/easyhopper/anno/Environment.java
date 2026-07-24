package com.dearxuan.easyhopper.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks annotated code as only available in a specific runtime environment.
 * <p>
 * When applied to a {@link ElementType#TYPE type}, all members of that type
 * are implicitly constrained to the same environment. When applied to a
 * {@link ElementType#METHOD method} or {@link ElementType#FIELD field},
 * only that specific member is constrained.
 * <p>
 * The annotation processor ({@code EnvironmentProcessor}) verifies at compile
 * time that code annotated with {@link EnvType#CLIENT} does not reference
 * code annotated with {@link EnvType#SERVER}, and vice versa.
 * {@link EnvType#BOTH} is compatible with all environments.
 * <p>
 * This annotation has no effect at runtime.
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface Environment {

    /**
     * The environment in which the annotated code is allowed to execute.
     */
    EnvType value();
}