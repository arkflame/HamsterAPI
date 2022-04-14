package dev._2lstudios.hamsterapi;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class Debug {
    private static ConsoleCommandSender sender;
    
    public static void init(HamsterAPI plugin) {
        sender = plugin.getServer().getConsoleSender();
    }

    public static boolean isEnabled() {
        return sender != null;
    }

    public static void log(String prefix, String message) {
        if (sender != null) {
            sender.sendMessage(
                ChatColor.translateAlternateColorCodes(
                    '&', 
                    "&5[&dHamsterAPI&5] " + prefix + message
                )
            );
        }
    }

    public static void info(String message) {
        log("&9INFO &7", message);
    }

    public static void crit(String message) {
        log("&4CRIT &c", message);
    }

    public static void warn(String message) {
        log("&6WARN &e", message);
    }
}
