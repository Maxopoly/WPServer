package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import com.github.maxopoly.WPServer.model.ChestManagement;
import org.json.JSONObject;

public class ChestContentPacketHandler extends AbstractPacketHandler {

	public ChestContentPacketHandler() {
		super("chestContent");
	}

	@Override
	public void handle(JSONObject msg) {
		ChestManagement.getInstance().updateContent(new Chest(msg.getJSONObject("chest")));
	}
}
