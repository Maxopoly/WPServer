package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.WPItem;
import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.model.ChestManagement;
import com.github.maxopoly.WPServer.packetCreation.ItemLocationPacket;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class ItemLocationRequestPacketHandler extends AbstractPacketHandler {

	private ClientConnection connection;

	public ItemLocationRequestPacketHandler(ClientConnection connection) {
		super("itemLocationRequest");
		this.connection = connection;
	}

	@Override
	public void handle(JSONObject msg) {
		WPItem item = new WPItem(msg.getString("item"));
		Map<Chest, List<WPItem>> chests = ChestManagement.getInstance().getChestsForSimilarItems(item);
		connection.sendData(new ItemLocationPacket(chests, item));
	}
}
