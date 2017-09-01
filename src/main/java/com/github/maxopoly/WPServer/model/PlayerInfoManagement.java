package com.github.maxopoly.WPServer.model;

import com.github.maxopoly.WPCommon.model.Faction;
import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.database.AltDAO;
import com.github.maxopoly.WPServer.packetCreation.InvalidateAllPlayerInfoPacket;
import java.util.Map;

public class PlayerInfoManagement {

	private AltDAO db;

	private Map<String, Player> players;
	private Map<String, Faction> factions;

	public PlayerInfoManagement(AltDAO dao) {
		this.db = dao;
		loadAllFromDB();
		if (Main.getServerManager() != null) {
			for (ClientConnection conn : Main.getServerManager().getActiveConnections()) {
				conn.sendData(new InvalidateAllPlayerInfoPacket());
			}
		}
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
