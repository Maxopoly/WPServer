package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.packetCreation.PlayerInfoPacket;
import org.json.JSONObject;

public class RequestPlayerInfoPacketHandler implements JSONPacketHandler {

	private ClientConnection connection;

	public RequestPlayerInfoPacketHandler(ClientConnection conn) {
		this.connection = conn;
	}

	@Override
	public void handle(JSONObject msg) {
		String name = msg.getString("name");
		Player player = Main.getPlayerInfoManagement().getPlayer(name);
		if (player != null) {
			connection.sendData(new PlayerInfoPacket(name, player, connection.getPermissionLevel()));
		}
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.PlayerInfoRequest;
	}
}
