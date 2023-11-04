package dev._2lstudios.hamsterapi.hamsterplayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class HamsterPlayerManager {
    final Map<UUID, HamsterPlayer> hamsterPlayers = new HashMap<>();

    public HamsterPlayer add(final Player player) {
        final HamsterPlayer hamsterPlayer = new HamsterPlayer(player);

        hamsterPlayers.put(player.getUniqueId(), hamsterPlayer);

        return hamsterPlayer;
    }

    public void remove(final Player player) {
        hamsterPlayers.remove(player.getUniqueId());
    }

    public HamsterPlayer get(final Player player) {
        return hamsterPlayers.get(player.getUniqueId());
    }
}
