package com.github.maxopoly.WPServer;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPServer.packetCreation.PlayerLocationUpdate;
import java.util.List;

public class LocationSendingRunnable implements Runnable {

	@Override
	public void run() {
		List<ClientConnection> conns = ServerManager.getInstance().getActiveConnections();
		LocationTracker tracker = LocationTracker.getInstance();
		List<String> pendingUpdates = tracker.pullAndClearRecentlyUpdatedPlayers();
		if (pendingUpdates.size() == 0) {
			return;
		}
		PlayerLocationUpdate updatePacket = new PlayerLocationUpdate(pendingUpdates);
		for (ClientConnection conn : conns) {
			conn.sendData(updatePacket.getMessage());
		}
	}
}
