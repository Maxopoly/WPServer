package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.model.LoggedPlayerLocation;
import com.github.maxopoly.WPCommon.model.permission.Permission;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevel;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class PlayerLocationUpdate extends AbstractJsonPacket {

	private List<String> names;
	private PermissionLevel permLevel;

	public PlayerLocationUpdate(List<String> names, PermissionLevel permLevel) {
		this.names = names;
		this.permLevel = permLevel;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.PlayerLocationPush;
	}

	public boolean hasPermissionForContent() {
		return permLevel.hasPermission(Permission.RADAR_LOCATION_READ)
				|| permLevel.hasPermission(Permission.SNITCH_LOCATION_READ);
	}

	@Override
	public void setupJSON(JSONObject json) {
		JSONArray locs = new JSONArray();
		LocationTracker tracker = LocationTracker.getInstance();
		for (String name : names) {
			LoggedPlayerLocation lastLoc = tracker.getLastKnownLocation(name);
			if (permLevel.hasPermission(lastLoc.getType().getMatchingReadPermission())) {
				locs.put(tracker.getLastKnownLocation(name).serialize());
			}
		}
		json.put("locs", locs);
	}
}
