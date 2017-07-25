package com.github.maxopoly.WPServer.model;

import com.github.maxopoly.WPCommon.model.Faction;
import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPServer.database.AltDAO;
import java.util.Map;

public class PlayerInfoManagement {

	private AltDAO db;

	private Map<String, Player> players;
	private Map<String, Faction> factions;

	public PlayerInfoManagement(AltDAO dao) {
		this.db = dao;
		loadAllFromDB();
	}

	private void loadAllFromDB() {
		Map[] maps = db.loadAll();
		factions = maps[1];
		players = maps[0];
	}

	public Player getPlayer(String accName) {
		return players.get(accName.toLowerCase());
	}

}
