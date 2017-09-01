package com.github.maxopoly.WPServer;

import com.github.maxopoly.WPServer.command.CommandLineReader;
import com.github.maxopoly.WPServer.database.AltDAO;
import com.github.maxopoly.WPServer.database.AuthDAO;
import com.github.maxopoly.WPServer.database.DBConnection;
import com.github.maxopoly.WPServer.database.WayPointDAO;
import com.github.maxopoly.WPServer.model.AuthorizedUserManagement;
import com.github.maxopoly.WPServer.model.ChestManagement;
import com.github.maxopoly.WPServer.model.PlayerInfoManagement;
import com.github.maxopoly.WPServer.model.WayPointManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

	private static PlayerInfoManagement accountCache;
	private static Logger logger;
	private static ServerManager serverManager;
	private static AuthorizedUserManagement userManagement;
	private static WayPointManager wayPointManager;
	private static DBConnection dbConnection;

	public static void main(String[] args) {
		logger = LogManager.getLogger("Main");
		try {
			dbConnection = new DBConnection(logger, args[0], args[1], args[2], Integer.parseInt(args[3]), args[4], 5,
					10000, 600000, 1800000);
		} catch (NumberFormatException e) {
			logger.error("Given port " + args[3] + " is not a number");
			System.exit(1);
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("Not enough args supplied to init db");
			System.exit(1);
		}
		reloadFromDB();
		serverManager = new ServerManager(logger);
		startCmdReading();
		// force loading from save file
		ChestManagement.getInstance();
		// force creation of hash cache
		new MapDataSyncSession(null, 0);
		serverManager.startServer();
	}

	public static void reloadFromDB() {
		AltDAO altDao = new AltDAO(dbConnection, logger);
		accountCache = new PlayerInfoManagement(altDao);
		AuthDAO authDao = new AuthDAO(dbConnection, logger);
		userManagement = new AuthorizedUserManagement(authDao);
		WayPointDAO wayPointDao = new WayPointDAO(dbConnection, logger);
		wayPointManager = new WayPointManager(wayPointDao);
	}

	private static void startCmdReading() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				new CommandLineReader(logger).start();
			}
		}).start();
	}

	public static PlayerInfoManagement getPlayerInfoManagement() {
		return accountCache;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static ServerManager getServerManager() {
		return serverManager;
	}

	public static WayPointManager getWayPointManager() {
		return wayPointManager;
	}

	public static AuthorizedUserManagement getAuthorizedUserManagement() {
		return userManagement;
	}

}
