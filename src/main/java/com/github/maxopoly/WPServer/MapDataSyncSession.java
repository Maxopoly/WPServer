package com.github.maxopoly.WPServer;

import com.github.maxopoly.WPCommon.model.CoordPair;
import com.github.maxopoly.WPCommon.model.WPMappingTile;
import com.github.maxopoly.WPCommon.packetHandling.packets.MapDataCompletionPacket;
import com.github.maxopoly.WPCommon.packetHandling.packets.MapDataPacket;
import com.github.maxopoly.WPCommon.util.MapDataFileHandler;
import com.github.maxopoly.WPServer.packetCreation.MapDataRequestPacket;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapDataSyncSession extends MapDataFileHandler {

	private static Map<CoordPair, WPMappingTile> cachedTiles;
	private int sessionId;
	private ClientConnection conn;
	private List<CoordPair> unchangedTiles;
	private List<WPMappingTile> receivedTiles;

	public MapDataSyncSession(ClientConnection conn, int id) {
		super(Main.getLogger());
		this.sessionId = id;
		this.conn = conn;
		this.unchangedTiles = new LinkedList<CoordPair>();
		this.receivedTiles = new LinkedList<WPMappingTile>();
		if (cachedTiles == null) {
			cachedTiles = loadCachedTileHashes();
		}
	}

	public void calculateTileDiff(List<WPMappingTile> tiles) {
		List<WPMappingTile> toRequest = new LinkedList<WPMappingTile>();
		int replyCount = cachedTiles.values().size();
		for (WPMappingTile tile : tiles) {
			WPMappingTile cachedTile = cachedTiles.get(tile.getCoords());
			if (cachedTile == null
					|| (cachedTile.getTimeStamp() < tile.getTimeStamp() && cachedTile.getHash() != tile.getHash())) {
				toRequest.add(tile);
			} else {
				if (cachedTile.getHash() == tile.getHash()) {
					unchangedTiles.add(tile.getCoords());
				}
				replyCount--;
			}
		}
		Main.getLogger().info(
				"Requesting " + toRequest.size() + "/" + tiles.size() + " tiles from " + conn.getIdentifier());
		if (conn.isActive()) {
			conn.sendData(new MapDataRequestPacket(toRequest, replyCount, sessionId));
		}
	}

	public void addReceivedTile(WPMappingTile tile) {
		receivedTiles.add(tile);
	}

	public static void saveCachedHashes() {
		new MapDataSyncSession(null, 0).saveCachedTileHashes(cachedTiles);
	}

	public void mergeAndSendMissingTiles() {
		Main.getLogger().info(
				"Beginning tile merge based on data from " + conn.getIdentifier() + ". Total tiles received: "
						+ receivedTiles.size());
		Set<WPMappingTile> toSendBack = new HashSet<WPMappingTile>(cachedTiles.values());
		for (CoordPair pair : unchangedTiles) {
			toSendBack.remove(new WPMappingTile(pair));
		}
		Iterator<WPMappingTile> iter = receivedTiles.iterator();
		while (iter.hasNext()) {
			WPMappingTile tile = iter.next();
			WPMappingTile cachedTile;
			synchronized (cachedTiles) {
				cachedTile = cachedTiles.get(tile.getCoords());
				if (cachedTile == null) {
					cachedTile = new WPMappingTile(tile.getTimeStamp(), tile.getCoords().getX(), tile.getCoords()
							.getZ(), tile.getHash());
					cachedTiles.put(cachedTile.getCoords(), cachedTile);
					saveTile(tile);
					iter.remove();
					System.gc();
					continue;
				}
			}
			synchronized (cachedTile) {
				if (tile.getTimeStamp() > cachedTile.getTimeStamp() && tile.getHash() != cachedTile.getHash()) {
					WPMappingTile tileWithData = loadMapTile(cachedTile.getCoords());
					WPMappingTile mergedData = tileWithData.merge(tile);
					toSendBack.add(mergedData);
					saveTile(mergedData);
					cachedTile.updateTimeStampAndHash(mergedData.getTimeStamp(), mergedData.getHash());
				}
			}
			iter.remove();
			System.gc();
		}
		for (WPMappingTile tile : toSendBack) {
			if (tile.getDataDump() == null) {
				tile = loadMapTile(tile.getCoords());
				if (tile == null) {
					continue;
				}
			}
			if (conn.isActive()) {
				conn.sendData(new MapDataPacket(tile, sessionId));
			}
			System.gc();
		}
		conn.sendData(new MapDataCompletionPacket(sessionId));
		Main.getLogger().info(
				"Completed map sync with " + conn.getIdentifier() + ". Updated " + toSendBack.size()
						+ " tiles client side and " + receivedTiles.size() + " tiles server side");
	}

	public int getID() {
		return sessionId;
	}

	@Override
	public File getBaseDirectory() {
		return new File(System.getProperty("user.dir"));
	}

	@Override
	public String getMapDataPath() {
		return "mapData";
	}

}
