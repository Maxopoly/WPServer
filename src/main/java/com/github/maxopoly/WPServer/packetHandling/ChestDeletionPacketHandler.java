package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import com.github.maxopoly.WPServer.model.ChestManagement;
import org.json.JSONObject;

public class ChestDeletionPacketHandler implements JSONPacketHandler {

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.DeleteChest;
	}

	@Override
	public void handle(JSONObject json) {
		Location loc = new Location(json.getJSONObject("loc"));
		ChestManagement.getInstance().deleteChest(loc);
	}
}
