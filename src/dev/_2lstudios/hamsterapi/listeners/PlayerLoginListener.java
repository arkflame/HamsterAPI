package dev._2lstudios.hamsterapi.listeners;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayerManager;

public class PlayerLoginListener implements Listener {
    private final Logger logger;
    private final HamsterPlayerManager hamsterPlayerManager;

    public PlayerLoginListener(final HamsterAPI hamsterAPI) {
        this.logger = hamsterAPI.getLogger();
        this.hamsterPlayerManager = hamsterAPI.getHamsterPlayerManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        final HamsterPlayer hamsterPlayer = hamsterPlayerManager.get(player);

        hamsterPlayer.trySetupInject(logger);
    }
}