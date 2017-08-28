package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.WPMappingTile;
import com.github.maxopoly.WPCommon.packetHandling.handlers.MapDataPacketHandler;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.MapDataSyncSession;

public class ServerSideMapDataPacketHandler extends MapDataPacketHandler {

	private ClientConnection conn;

	public ServerSideMapDataPacketHandler(ClientConnection conn) {
		super(Main.getLogger());
		this.conn = conn;
	}

	@Override
	public void handle(WPMappingTile tile, int sessionID) {
		MapDataSyncSession session = conn.getMapDataSyncSession();
		if (session.getID() != sessionID) {
			return;
		}
		session.addReceivedTile(tile);
	}

}
