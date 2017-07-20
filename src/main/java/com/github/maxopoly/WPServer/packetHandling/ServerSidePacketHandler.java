package com.github.maxopoly.WPServer.packetHandling;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.packetHandling.PacketForwarder;
import com.github.maxopoly.WPServer.ClientConnection;
import org.apache.logging.log4j.Logger;

public class ServerSidePacketHandler extends PacketForwarder {

	private ClientConnection conn;

	public ServerSidePacketHandler(ClientConnection conn, Logger logger) {
		super(logger);
		this.conn = conn;
		// needs to be here, because it needs the connection object, fuck the pattern
		registerPacketHandler(new RequestPlayerInfoHandler(conn));
	}

	@Override
	protected void registerHandler() {
		registerPacketHandler(new LocationReportHandler(LocationTracker.getInstance()));
		registerPacketHandler(new SnitchHitHandler(LocationTracker.getInstance()));
	}

}
