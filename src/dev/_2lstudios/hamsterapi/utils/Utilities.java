package dev._2lstudios.hamsterapi.utils;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Utilities {
	public static Player getRandomPlayer() {
		final Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

		if (!players.isEmpty()) {
			final int i = (int) ((players.size()) * Math.random());

			return players.toArray(new Player[0])[i];
		}

		return null;
	}
}
