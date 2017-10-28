package com.github.maxopoly.WPServer;

import com.github.maxopoly.WPCommon.util.WPStatics;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;

public class ServerManager {

	private List<ClientConnection> activeConnections;
	private Logger logger;
	private KeyPair keyPair;

	public Logger getLogger() {
		return logger;
	}

	public ServerManager(Logger logger) {
		this.logger = logger;
		activeConnections = new LinkedList<ClientConnection>();
		genServerKeys();
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(new LocationSendingRunnable(), 500, 500, TimeUnit.MILLISECONDS);
	}

	@SuppressWarnings("resource")
	// it's fine that we never close the server socket, it's supposed to run until the application exits
	public void startServer() {
		int port = WPStatics.port;
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
			// infinite loop to accept connections
			try {
				Socket client = server.accept();
				ClientConnection conn = new ClientConnection(client, logger, keyPair);
				synchronized (activeConnections) {
					activeConnections.add(conn);
				}
				new Thread(conn).start();
			} catch (IOException e) {
				logger.error("Error occured while accepting client connection", e);
			}
		}
	}

	public List<ClientConnection> getActiveConnections() {
		// filter out dead ones
		synchronized (activeConnections) {
			Iterator<ClientConnection> iter = activeConnections.iterator();
			List<ClientConnection> conns = new LinkedList<ClientConnection>();
			while (iter.hasNext()) {
				ClientConnection conn = iter.next();
				if (!conn.isActive()) {
					iter.remove();
					continue;
				}
				if (conn.isInitialized()) {
					conns.add(conn);
				}
			}
			return conns;
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
