package com.dearxuan.easyhopper.platform;

public interface IPlatformHelper {

    /**
     * 获取当前平台的名称
     *
     * @return 当前平台的名称.
     */
    String getPlatformName();

    /**
     * 检查具有给定 ID 的模组是否已加载.
     *
     * @param modId 要检查是否已加载的模组 ID.
     * @return 如果模组已加载则返回 true, 否则返回 false.
     */
    boolean isModLoaded(String modId);

    /**
     * 检查游戏当前是否处于开发环境.
     *
     * @return 如果处于开发环境则返回 true, 否则返回 false.
     */
    boolean isDevelopmentEnvironment();

    /**
     * 获取环境类型的名称字符串.
     *
     * @return 环境类型的名称.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
