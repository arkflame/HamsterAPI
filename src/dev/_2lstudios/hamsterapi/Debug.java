package dev._2lstudios.hamsterapi;

import java.util.logging.Logger;

import org.bukkit.ChatColor;

public class Debug {
    private static Logger logger;
    private static String prefix;
    
    public static void init(Logger logger) {
        Debug.logger = logger;

        prefix = ChatColor.translateAlternateColorCodes('&', "&5[&dDebug&5] &r");
    }

    public static void log(String message) {
        if (logger != null) {
            logger.info(Debug.prefix + message);
        }
    }
}
