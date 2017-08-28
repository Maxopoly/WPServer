package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.packetHandling.handlers.MapDataCompletionPacketHandler;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.MapDataSyncSession;

public class ServerSideMapDataCompletePacketHandler extends MapDataCompletionPacketHandler {

	private ClientConnection conn;

	public ServerSideMapDataCompletePacketHandler(ClientConnection conn) {
		this.conn = conn;
	}

	@Override
	public void handle(int id) {
		MapDataSyncSession session = conn.getMapDataSyncSession();
		if (session.getID() != id) {
			return;
		}
		session.mergeAndSendMissingTiles();
	}

}
