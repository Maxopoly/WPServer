package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.packetCreation.PlayerInfoPacket;
import java.util.Arrays;
import org.json.JSONObject;

public class RequestPlayerInfoHandler extends AbstractPacketHandler {

	private ClientConnection connection;

	public RequestPlayerInfoHandler(ClientConnection conn) {
		super("requestPlayerInfo");
		this.connection = conn;
	}

	@Override
	public void handle(JSONObject msg) {
		String name = msg.getString("name");
		if (name.equals("Frensin")) {
			// TODO get this from the db
			Player p = new Player(null, Arrays.asList("Frensin"), "Frensin", 10);
			connection.sendData(new PlayerInfoPacket(p).getMessage());
		}
	}
}
