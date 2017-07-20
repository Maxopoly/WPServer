package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;
import java.util.List;
import org.json.JSONObject;

public class PlayerLocationUpdate extends AbstractJsonPacket {

	public PlayerLocationUpdate(List<String> names) {
		super("playerLocations");
		JSONObject locs = new JSONObject();
		LocationTracker tracker = LocationTracker.getInstance();
		for (String name : names) {
			locs.put(name, tracker.getLastKnownLocation(name).serialize());
		}
		msg.put("locations", locs);
	}

}
