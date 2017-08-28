package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class PlayerLocationUpdate extends AbstractJsonPacket {

	private List<String> names;

	public PlayerLocationUpdate(List<String> names) {
		this.names = names;
	}

	@Override
	public PacketIndex getPacket() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setupJSON(JSONObject json) {
		JSONArray locs = new JSONArray();
		LocationTracker tracker = LocationTracker.getInstance();
		for (String name : names) {
			locs.put(tracker.getLastKnownLocation(name).serialize());
		}
		json.put("locs", locs);
	}

}
