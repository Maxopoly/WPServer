package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.MCAccount;
import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPCommon.model.permission.Permission;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevel;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.Arrays;
import org.json.JSONObject;

public class PlayerInfoPacket extends AbstractJsonPacket {

	private Player player;
	private PermissionLevel permLevel;
	private String requestedPlayerName;

	public PlayerInfoPacket(String requestedPlayerName, Player player, PermissionLevel permLevel) {
		this.player = player;
		this.permLevel = permLevel;
		this.requestedPlayerName = requestedPlayerName;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.PlayerInfoReply;
	}

	@Override
	public void setupJSON(JSONObject json) {
		if (!permLevel.hasPermission(Permission.ALT_LOOKUP)) {
			// clean out alts
			player = new Player(player.getFaction(), Arrays.asList(new MCAccount[] { new MCAccount(requestedPlayerName) }),
					new MCAccount(requestedPlayerName), player.getStanding(), player.isPOS());
		}
		json.put("player", player.serialize());
	}
}
