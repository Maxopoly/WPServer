package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import com.github.maxopoly.WPServer.model.ChestManagement;
import org.json.JSONObject;

public class ChestContentPacketHandler implements JSONPacketHandler {

	@Override
	public void handle(JSONObject msg) {
		ChestManagement.getInstance().updateContent(new Chest(msg.getJSONObject("chest")));
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.ChestContent;
	}
}
