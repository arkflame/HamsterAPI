package dev._2lstudios.hamsterapi.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import dev._2lstudios.hamsterapi.HamsterAPI;

public class PlayerQuitListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final HamsterAPI hamsterAPI = HamsterAPI.getInstance();

        hamsterAPI.getHamsterPlayerManager().remove(event.getPlayer());
    }
}