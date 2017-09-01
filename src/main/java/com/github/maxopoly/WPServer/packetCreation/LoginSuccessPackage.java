package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import com.github.maxopoly.WPCommon.util.WPStatics;
import org.json.JSONObject;

public class LoginSuccessPackage extends AbstractJsonPacket {

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.LoginSuccess;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("version", WPStatics.protocolVersion);
	}

}
