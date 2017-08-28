package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import org.json.JSONObject;

public class CrashPacket extends AbstractJsonPacket {

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.Crash;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("get", "fucked");
	}

}
