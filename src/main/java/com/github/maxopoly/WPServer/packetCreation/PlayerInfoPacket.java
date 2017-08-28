package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import org.json.JSONObject;

public class PlayerInfoPacket extends AbstractJsonPacket {

	private Player player;

	public PlayerInfoPacket(Player player) {
		this.player = player;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.PlayerInfoReply;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("player", player.serialize());
	}

}
