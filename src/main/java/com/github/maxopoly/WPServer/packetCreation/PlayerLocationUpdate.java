package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;
import java.util.List;
import org.json.JSONArray;

public class PlayerLocationUpdate extends AbstractJsonPacket {

	public PlayerLocationUpdate(List<String> names) {
		super("playerLocations");
		JSONArray locs = new JSONArray();
		LocationTracker tracker = LocationTracker.getInstance();
		for (String name : names) {
			locs.put(tracker.getLastKnownLocation(name).serialize());
		}
		msg.put("locs", locs);
	}

}
