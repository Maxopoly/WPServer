package com.github.maxopoly.WPServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerManager {

	private static ServerManager instance;

	private static final int port = 23452;

	private List<ClientConnection> activeConnections = Collections.synchronizedList(new LinkedList<ClientConnection>());
	private Logger logger;
	private KeyPair keyPair;

	public static ServerManager getInstance() {
		if (instance == null) {
			instance = new ServerManager();
		}
		return instance;
	}

	private ServerManager() {
		logger = LogManager.getLogger("Main");
		genServerKeys();
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(new LocationSendingRunnable(), 1, 1, TimeUnit.SECONDS);
	}

	@SuppressWarnings("resource")
	// it's fine that we never close the server socket, it's supposed to run until the application exits
	public void startServer() {
		logger.info("Starting server on port " + port);
		ServerSocket server;
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		while (true) {
			// infinite loop to accept conditions
			try {
				Socket client = server.accept();
				ClientConnection conn = new ClientConnection(client, logger, keyPair);
				if (conn.isActive()) {
					activeConnections.add(conn);
					new Thread(conn).start();
				}
			} catch (IOException e) {
				logger.error("Error occured while accepting client connection", e);
			}
		}
	}

	public List<ClientConnection> getActiveConnections() {
		// filter out dead ones
		synchronized (activeConnections) {
			Iterator<ClientConnection> iter = activeConnections.iterator();
			while (iter.hasNext()) {
				if (!iter.next().isActive()) {
					iter.remove();
				}
			}
			return new LinkedList<ClientConnection>(activeConnections);
		}
	}

	private void genServerKeys() {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to gen keys", e);
			return;
		}
		keyGen.initialize(1024, new SecureRandom());
		keyPair = keyGen.generateKeyPair();
	}
}
