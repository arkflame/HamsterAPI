package dev._2lstudios.hamsterapi.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayerManager;
import dev._2lstudios.hamsterapi.tasks.PacketInjectorQueue;

public class PlayerJoinListener implements Listener {
    private final HamsterPlayerManager hamsterPlayerManager;
    private final PacketInjectorQueue packetInjectorQueue;

    public PlayerJoinListener(final PacketInjectorQueue injectTask) {
        this.hamsterPlayerManager = HamsterAPI.getInstance().getHamsterPlayerManager();
        this.packetInjectorQueue = injectTask;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        packetInjectorQueue.queue(hamsterPlayerManager.get(player));
    }
}