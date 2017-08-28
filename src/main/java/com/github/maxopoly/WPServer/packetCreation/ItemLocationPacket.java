package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.WPItem;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONObject;

public class ItemLocationPacket extends AbstractJsonPacket {

	private Map<Chest, List<WPItem>> chests;
	private WPItem item;

	public ItemLocationPacket(Map<Chest, List<WPItem>> chests, WPItem item) {
		this.chests = chests;
		this.item = item;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.ItemLocationReply;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("item", item.serialize());
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
		json.put("chests", array);
	}

}
