package dev._2lstudios.hamsterapi.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dev._2lstudios.hamsterapi.tasks.InjectTask;

public class PlayerJoinListener implements Listener {
    private final InjectTask injectTask;

    public PlayerJoinListener(final InjectTask injectTask) {
        this.injectTask = injectTask;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        injectTask.inject(event.getPlayer());
    }
}