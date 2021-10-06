package dev._2lstudios.hamsterapi.listeners;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayerManager;

public class PlayerJoinListener implements Listener {
    private final Logger logger;
    private final HamsterPlayerManager hamsterPlayerManager;

    public PlayerJoinListener(final HamsterAPI hamsterAPI) {
        this.logger = hamsterAPI.getLogger();
        this.hamsterPlayerManager = hamsterAPI.getHamsterPlayerManager();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final HamsterPlayer hamsterPlayer = hamsterPlayerManager.add(player);

        if (!hamsterPlayer.tryInject()) {
            logger.warning("Failed to inject player " + player.getName()
                    + " please contact 2LStudios for support about HamsterAPI as this can lead to vulnerabilities.");
        }
    }
}