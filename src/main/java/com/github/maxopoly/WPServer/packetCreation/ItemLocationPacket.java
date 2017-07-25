package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ItemLocationPacket extends AbstractJsonPacket {

	public ItemLocationPacket(List<Chest> chests, int itemID) {
		super("itemLocation");
		msg.put("id", itemID);
		JSONArray array = new JSONArray();
		for (Chest chest : chests) {
			JSONObject obj = new JSONObject();
			obj.put("loc", chest.getLocation().serialize());
			obj.put("amount", chest.getAmount(itemID));
			array.put(obj);
		}
		msg.put("chests", array);
	}

}
