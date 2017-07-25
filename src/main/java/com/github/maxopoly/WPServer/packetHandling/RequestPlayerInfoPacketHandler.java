package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.packetCreation.PlayerInfoPacket;
import org.json.JSONObject;

public class RequestPlayerInfoPacketHandler extends AbstractPacketHandler {

	private ClientConnection connection;

	public RequestPlayerInfoPacketHandler(ClientConnection conn) {
		super("requestPlayerInfo");
		this.connection = conn;
	}

	@Override
	public void handle(JSONObject msg) {
		String name = msg.getString("name");
		Player player = Main.getPlayerInfoManagement().getPlayer(name);
		if (player != null) {
			connection.sendData(new PlayerInfoPacket(player).getMessage());
		}
	}
}
