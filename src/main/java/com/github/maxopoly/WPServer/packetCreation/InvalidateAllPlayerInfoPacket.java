package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import org.json.JSONObject;

public class InvalidateAllPlayerInfoPacket extends AbstractJsonPacket {

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.InvalidateAllPlayerInfo;
	}

	@Override
	public void setupJSON(JSONObject json) {
		// no content
	}

}
