package com.dearxuan.easyhopper.Config.ModMenu;

import org.slf4j.LoggerFactory;

public class Logger {

    private final org.slf4j.Logger LOGGER;

    public Logger(String ModName) {
        this.LOGGER = LoggerFactory.getLogger(ModName);
    }

    public void info(String s) {
        LOGGER.info(s);
    }

    public void error(String s) {
        LOGGER.error(s);
    }

    public void error(Exception e) {
        LOGGER.error("---------------错误开始---------------");
        LOGGER.error(e.toString());
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            LOGGER.error(String.valueOf(element));
        }
        LOGGER.error("---------------错误结束---------------");
    }
}
