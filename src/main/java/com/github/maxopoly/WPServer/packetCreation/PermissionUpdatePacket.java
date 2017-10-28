package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.permission.PermissionLevel;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import org.json.JSONObject;

public class PermissionUpdatePacket extends AbstractJsonPacket {

	private PermissionLevel permLevel;

	public PermissionUpdatePacket(PermissionLevel permLevel) {
		this.permLevel = permLevel;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.UpdatePermissions;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("level", permLevel.getID());
	}

}
