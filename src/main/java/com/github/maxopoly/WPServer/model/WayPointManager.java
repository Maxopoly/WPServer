package com.github.maxopoly.WPServer.model;

import com.github.maxopoly.WPCommon.model.WPWayPoint;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.database.WayPointDAO;
import com.github.maxopoly.WPServer.packetCreation.WayPointPacket;
import java.util.HashSet;
import java.util.Set;

public class WayPointManager {

	private Set<WPWayPoint> points;
	private WayPointDAO dao;

	public WayPointManager(WayPointDAO dao) {
		this.dao = dao;
		points = dao.loadAll();
		if (Main.getServerManager() != null) {
			for (ClientConnection conn : Main.getServerManager().getActiveConnections()) {
				conn.sendData(new WayPointPacket(conn.getPermissionLevel(), getAllPoints()));
			}
		}
	}

	public synchronized Set<WPWayPoint> getAllPoints() {
		// never hand out original
		return new HashSet<WPWayPoint>(points);
	}
}
