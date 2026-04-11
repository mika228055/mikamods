package net.mika.mikamods.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerUtil {
    public static final Logger LOGGER = LogManager.getLogger("loader");

    public static void info(String msg) { LOGGER.info(msg); }
    public static void warn(String msg) { LOGGER.warn(msg); }
    public static void error(String msg) { LOGGER.error(msg); }
}
