package com.dearxuan.easyhopper.anno;

/**
 * Represents the runtime environment in which code is allowed to execute.
 * Used in conjunction with {@link Environment} to enforce environment separation
 * at compile time via annotation processing.
 */
public enum EnvType {

    /**
     * Code that is safe to execute in both client and server environments.
     * No restrictions are applied.
     */
    BOTH,

    /**
     * Code that is only available on the physical client.
     * Referencing this from server-side code will cause a compile error.
     */
    CLIENT,

    /**
     * Code that is only available on the dedicated server.
     * Referencing this from client-side code will cause a compile error.
     */
    SERVER
}