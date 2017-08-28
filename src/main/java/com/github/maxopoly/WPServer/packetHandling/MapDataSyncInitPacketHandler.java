package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.WPMappingTile;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class MapDataSyncInitPacketHandler implements JSONPacketHandler {

	private ClientConnection conn;

	public MapDataSyncInitPacketHandler(ClientConnection conn) {
		this.conn = conn;
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.MapDataSyncInit;
	}

	@Override
	public void handle(JSONObject json) {
		int id = json.getInt("id");
		JSONArray data = json.getJSONArray("tileData");
		conn.resetMapDataSync(id);
		List<WPMappingTile> tiles = new LinkedList<WPMappingTile>();
		for (int i = 0; i < data.length(); i++) {
			WPMappingTile tile = new WPMappingTile(data.getJSONObject(i));
			tiles.add(tile);
		}
		Main.getLogger().info("Beginning map data sync with " + conn.getIdentifier());
		conn.getMapDataSyncSession().calculateTileDiff(tiles);
	}

}
