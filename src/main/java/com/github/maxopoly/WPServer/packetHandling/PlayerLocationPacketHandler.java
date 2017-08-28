package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.model.LoggedPlayerLocation;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import org.json.JSONArray;
import org.json.JSONObject;

public class PlayerLocationPacketHandler implements JSONPacketHandler {

	private LocationTracker tracker;

	public PlayerLocationPacketHandler(LocationTracker tracker) {
		this.tracker = tracker;
	}

	@Override
	public void handle(JSONObject msg) {
		JSONArray arr = msg.getJSONArray("locs");
		for (int i = 0; i < arr.length(); i++) {
			LoggedPlayerLocation playerLoc = new LoggedPlayerLocation(arr.getJSONObject(i));
			tracker.reportLocation(playerLoc);
		}
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.PlayerLocationPull;
	}

}
