package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import org.json.JSONObject;

public class LocationReportHandler extends AbstractPacketHandler {

	private LocationTracker tracker;

	public LocationReportHandler(LocationTracker tracker) {
		super("nearbyPlayers");
		this.tracker = tracker;
	}

	@Override
	public void handle(JSONObject msg) {
		JSONObject data = msg.getJSONObject("locations");
		for (Object nameObject : data.names()) {
			String name = (String) nameObject;
			Location loc = new Location(data.getJSONObject(name));
			tracker.reportLocation(name, loc);
		}
	}

}
