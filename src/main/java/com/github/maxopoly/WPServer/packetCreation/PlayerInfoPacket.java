package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;

public class PlayerInfoPacket extends AbstractJsonPacket {

	public PlayerInfoPacket(Player player) {
		super("playerinfo");
		msg.put("player", player.serialize());
	}

}
