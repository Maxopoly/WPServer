package com.github.maxopoly.WPServer;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevelManagement;
import com.github.maxopoly.WPCommon.packetHandling.incoming.BinaryDataForwarder;
import com.github.maxopoly.WPCommon.packetHandling.incoming.IncomingDataHandler;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketForwarder;
import com.github.maxopoly.WPCommon.util.AES_CFB8_Encrypter;
import com.github.maxopoly.WPServer.packetHandling.ChestContentPacketHandler;
import com.github.maxopoly.WPServer.packetHandling.ChestDeletionPacketHandler;
import com.github.maxopoly.WPServer.packetHandling.InitAuthPacketHandler;
import com.github.maxopoly.WPServer.packetHandling.ItemLocationRequestPacketHandler;
import com.github.maxopoly.WPServer.packetHandling.MapDataSyncInitPacketHandler;
import com.github.maxopoly.WPServer.packetHandling.PlayerLocationPacketHandler;
import com.github.maxopoly.WPServer.packetHandling.RequestPlayerInfoPacketHandler;
import com.github.maxopoly.WPServer.packetHandling.ServerSideMapDataCompletePacketHandler;
import com.github.maxopoly.WPServer.packetHandling.ServerSideMapDataPacketHandler;
import java.io.DataInputStream;
import org.apache.logging.log4j.Logger;

public class ServerSidePacketHandler extends IncomingDataHandler {

	public ServerSidePacketHandler(Logger logger, DataInputStream input, AES_CFB8_Encrypter encrypter,
			Runnable failureCallback, ClientConnection conn) {
		// init at default perm level
		super(logger, input, encrypter, failureCallback, PermissionLevelManagement.getPermissionLevel(4));
		JSONPacketForwarder jsonHandler = new JSONPacketForwarder(logger);
		jsonHandler.registerHandler(new RequestPlayerInfoPacketHandler(conn));
		jsonHandler.registerHandler(new ChestContentPacketHandler());
		jsonHandler.registerHandler(new PlayerLocationPacketHandler(LocationTracker.getInstance()));
		jsonHandler.registerHandler(new ItemLocationRequestPacketHandler(conn));
		jsonHandler.registerHandler(new MapDataSyncInitPacketHandler(conn));
		jsonHandler.registerHandler(new InitAuthPacketHandler(conn));
		jsonHandler.registerHandler(new ServerSideMapDataCompletePacketHandler(conn));
		jsonHandler.registerHandler(new ChestDeletionPacketHandler());
		BinaryDataForwarder binaryHandler = new BinaryDataForwarder(logger);
		binaryHandler.registerHandler(new ServerSideMapDataPacketHandler(conn));
		registerHandler(binaryHandler);
		registerHandler(jsonHandler);
	}

}
