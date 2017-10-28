package com.github.maxopoly.WPServer.model;

import com.github.maxopoly.WPCommon.model.permission.PermissionLevel;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevelManagement;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.database.AuthDAO;
import com.github.maxopoly.WPServer.packetCreation.PermissionUpdatePacket;
import java.util.Map;

public class AuthorizedUserManagement {

	private AuthDAO db;
	private Map<String, PermissionLevel> userUUIDS;

	public AuthorizedUserManagement(AuthDAO dao) {
		this.db = dao;
		userUUIDS = db.loadAll();
		if (Main.getServerManager() != null) {
			for (ClientConnection conn : Main.getServerManager().getActiveConnections()) {
				conn.updatePermissionLevel(getPermLevel(conn.getUUID()));
				conn.sendData(new PermissionUpdatePacket(conn.getPermissionLevel()));
			}
		}
	}

	public PermissionLevel getPermLevel(String uuidWithoutDash) {
		PermissionLevel level = userUUIDS.get(uuidWithoutDash);
		if (level == null) {
			// default perm set
			level = PermissionLevelManagement.getDefaultPermissionLevel();
		}
		return level;
	}

}
