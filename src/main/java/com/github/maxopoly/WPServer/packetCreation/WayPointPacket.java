package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.WPWayPoint;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class WayPointPacket extends AbstractJsonPacket {

	private Set<WPWayPoint> points;

	public WayPointPacket(Set<WPWayPoint> points) {
		this.points = points;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.WaypointInformation;
	}

	@Override
	public void setupJSON(JSONObject json) {
		JSONArray array = new JSONArray();
		for (WPWayPoint point : points) {
			array.put(point.serialize());
		}
		json.put("points", array);
	}

}
