package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import org.json.JSONObject;

public class InitAuthPacketHandler implements JSONPacketHandler {

	private ClientConnection conn;

	public InitAuthPacketHandler(ClientConnection conn) {
		this.conn = conn;
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.InitAuth;
	}

	@Override
	public void handle(JSONObject json) {
		if (!conn.authPlayer(json)) {
			conn.close();
		} else {
			Main.getLogger().info("Successfully established connection with " + conn.getIdentifier());
		}
	}
}
