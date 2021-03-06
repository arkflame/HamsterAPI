package dev._2lstudios.hamsterapi.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayerManager;

public class PlayerQuitListener implements Listener {
    private final HamsterPlayerManager hamsterPlayerManager;

    public PlayerQuitListener(final HamsterPlayerManager hamsterPlayerManager) {
        this.hamsterPlayerManager = hamsterPlayerManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        hamsterPlayerManager.remove(player);
    }
}