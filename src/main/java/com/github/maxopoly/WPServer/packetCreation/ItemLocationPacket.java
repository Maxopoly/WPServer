package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.WPItem;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONObject;

public class ItemLocationPacket extends AbstractJsonPacket {

	public ItemLocationPacket(Map<Chest, List<WPItem>> chests, WPItem item) {
		super("itemLocation");
		msg.put("item", item.serialize());
		JSONArray array = new JSONArray();
		for (Entry<Chest, List<WPItem>> entry : chests.entrySet()) {
			JSONObject obj = new JSONObject();
			obj.put("loc", entry.getKey().getLocation().serialize());
			JSONArray content = new JSONArray();
			for (WPItem contentItem : entry.getValue()) {
				content.put(contentItem.serialize());
			}
			obj.put("items", content);
			array.put(obj);
		}
		msg.put("chests", array);
	}

}
