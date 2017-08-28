package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.model.WPMappingTile;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class MapDataRequestPacket extends AbstractJsonPacket {

	private int id;
	private List<WPMappingTile> tiles;
	private int filesToSendToClient;

	public MapDataRequestPacket(List<WPMappingTile> tiles, int filesToSendToClient, int id) {
		this.tiles = tiles;
		this.id = id;
		this.filesToSendToClient = filesToSendToClient;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.PlayerMapDataRequest;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("id", id);
		json.put("returnTiles", filesToSendToClient);
		JSONArray coords = new JSONArray();
		for (WPMappingTile tile : tiles) {
			coords.put(tile.getCoords().serialize());
		}
		json.put("coords", coords);
	}

}
