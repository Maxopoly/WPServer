package com.github.maxopoly.WPServer;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPServer.packetCreation.PlayerLocationUpdate;
import java.util.List;

public class LocationSendingRunnable implements Runnable {

	@Override
	public void run() {
		List<ClientConnection> conns = Main.getServerManager().getActiveConnections();
		LocationTracker tracker = LocationTracker.getInstance();
		List<String> pendingUpdates = tracker.pullAndClearRecentlyUpdatedPlayers();
		if (pendingUpdates.size() == 0) {
			return;
		}
		for (ClientConnection conn : conns) {
			PlayerLocationUpdate updatePacket = new PlayerLocationUpdate(pendingUpdates, conn.getPermissionLevel());
			if (updatePacket.hasPermissionForContent()) {
				conn.sendData(updatePacket);
			}
		}
	}
}
