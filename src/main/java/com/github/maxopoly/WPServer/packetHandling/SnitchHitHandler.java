package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;

import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.model.SnitchHitAction;
import org.json.JSONObject;

public class SnitchHitHandler extends AbstractPacketHandler {

	private LocationTracker tracker;

	public SnitchHitHandler(LocationTracker tracker) {
		super("snitchhit");
		this.tracker = tracker;
	}

	@Override
	public void handle(JSONObject msg) {
		String name = msg.getString("name");
		Location loc = new Location(msg.getJSONObject("location"));
		SnitchHitAction action = SnitchHitAction.valueOf(msg.getString("action"));
		tracker.reportSnitch(name, loc);
	}

}
