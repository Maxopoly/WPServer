package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.WPWayPoint;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevel;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class WayPointPacket extends AbstractJsonPacket {

	private Set<WPWayPoint> points;
	private PermissionLevel permLevel;

	public WayPointPacket(PermissionLevel permLevel, Set<WPWayPoint> points) {
		this.points = points;
		this.permLevel = permLevel;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.WaypointInformation;
	}

	@Override
	public void setupJSON(JSONObject json) {
		JSONArray array = new JSONArray();
		for (WPWayPoint point : points) {
			if (permLevel.hasPermission(point.getGroup().getRequiredPermission())) {
				array.put(point.serialize());
			}
		}
		json.put("points", array);
	}
}
