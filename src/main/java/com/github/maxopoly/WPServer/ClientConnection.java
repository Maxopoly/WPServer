package com.github.maxopoly.WPServer;

import com.github.maxopoly.WPCommon.model.permission.PermissionLevel;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevelManagement;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.IPacket;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.OutgoingDataHandler;
import com.github.maxopoly.WPCommon.util.AES_CFB8_Encrypter;
import com.github.maxopoly.WPCommon.util.ConnectionUtils;
import com.github.maxopoly.WPCommon.util.PKCSEncrypter;
import com.github.maxopoly.WPCommon.util.VarInt;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.SecureRandom;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class ClientConnection implements Runnable {

	private static String sessionServerIP = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s";

	private DataInputStream input;
	private DataOutputStream output;
	private Socket socket;
	private Logger logger;
	private boolean active;
	private ServerSidePacketHandler packetHandler;
	private OutgoingDataHandler outgoingDataHandler;
	private KeyPair keyPair;
	private AES_CFB8_Encrypter encrypter;
	private byte[] sharedSecret;
	private boolean initialized;
	private String identifier;
	private PermissionLevel permLevel;
	private MapDataSyncSession mapSyncSession;
	private String uuid;

	public ClientConnection(Socket socket, Logger logger, KeyPair keyPair) {
		logger.info("Connection attempt by " + socket.getInetAddress().getHostAddress());
		this.socket = socket;
		this.keyPair = keyPair;
		this.logger = logger;
		try {
			this.input = new DataInputStream(socket.getInputStream());
			this.output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			close();
			logger.error("Failed to init connection with client", e);
			return;
		}
	}

	public boolean isInitialized() {
		return initialized;
	}

	private void setup() {
		active = true;
		identifier = socket.getInetAddress().getHostAddress();
		if (!figureOutEncryption()) {
			close();
			return;
		}
		Runnable closingCallBack = new Runnable() {

			@Override
			public void run() {
				close();
				logger.error("IOError, killing connection with " + identifier);
			}
		};
		outgoingDataHandler = new OutgoingDataHandler(output, encrypter, closingCallBack);
		packetHandler = new ServerSidePacketHandler(logger, input, encrypter, closingCallBack, this);
		packetHandler.updatePermissionLevel(PermissionLevelManagement.getAuthOnlyPermissionLevel());
	}

	public boolean isActive() {
		return active;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void run() {
		if (!initialized) {
			setup();
		}
		if (active) {
			packetHandler.startHandling();
		}
	}

	public void sendData(IPacket packet) {
		outgoingDataHandler.queuePacket(packet);
	}

	public void close() {
		active = false;
		logger.info("Closing connection with " + identifier);
		try {
			if (packetHandler != null) {
				packetHandler.stopHandling();
			}
			if (outgoingDataHandler != null) {
				outgoingDataHandler.stop();
			}
			socket.close();
		} catch (IOException e) {
			// fine
		}
	}

	private boolean figureOutEncryption() {
		byte[] pubKeyEncoded = keyPair.getPublic().getEncoded();
		byte[] confirmationkey = new byte[16];
		byte[] receivedConfirmationKey;
		new SecureRandom().nextBytes(confirmationkey);
		try {
			VarInt.writeVarInt(output, pubKeyEncoded.length);
			output.write(pubKeyEncoded);
			output.write(confirmationkey);
			int encryptedSecretLength = VarInt.readVarInt(input);
			if (encryptedSecretLength > 128) {
				logger.error("Encrypted secret had wrong length, expected 128, but got " + encryptedSecretLength
						+ " from " + identifier);
				return false;
			}
			sharedSecret = new byte[encryptedSecretLength];
			input.readFully(sharedSecret);
			int encryptedConfKeyLength = VarInt.readVarInt(input);
			if (encryptedConfKeyLength != 128) {
				logger.error("Encrypted confirmation key had wrong length, expected 128, but got "
						+ encryptedConfKeyLength + " from " + identifier);
				return false;
			}
			receivedConfirmationKey = new byte[encryptedConfKeyLength];
			input.readFully(receivedConfirmationKey);
		} catch (IOException e) {
			logger.error("Failed to send pub key to " + identifier, e);
			return false;
		}
		sharedSecret = PKCSEncrypter.decrypt(sharedSecret, keyPair.getPrivate());
		receivedConfirmationKey = PKCSEncrypter.decrypt(receivedConfirmationKey, keyPair.getPrivate());
		for (int i = 0; i < confirmationkey.length; i++) {
			if (confirmationkey[i] != receivedConfirmationKey[i]) {
				logger.error("Received wrong confirmation key back from " + identifier);
				return false;
			}
		}
		encrypter = new AES_CFB8_Encrypter(sharedSecret, sharedSecret);
		logger.info("Successfully exchanged encryption details with " + identifier);
		return true;
	}

	public boolean authPlayer(JSONObject playerInfo) {
		String name = playerInfo.getString("name");
		identifier = name;
		String uuid = playerInfo.getString("uuid");
		this.uuid = uuid;
		this.permLevel = Main.getAuthorizedUserManagement().getPermLevel(uuid);
		String tag = playerInfo.getString("tag");
		logger.info(socket.getInetAddress().getHostAddress() + " is trying to auth as " + name + " with tag " + tag);
		String hash;
		try {
			hash = ConnectionUtils.generateKeyHash(uuid, sharedSecret, keyPair.getPublic().getEncoded());
		} catch (IOException e) {
			logger.error("Failed to gen key hash for " + identifier, e);
			return false;
		}
		String mojangUrl = String.format(sessionServerIP, name, hash);
		String playerInfoFromMojang;
		try {
			playerInfoFromMojang = ConnectionUtils.sendGet(mojangUrl);
		} catch (Exception e) {
			logger.error("Failed to get player info from yggdrassil", e);
			return false;
		}
		JSONObject playerInfoMojangJson = new JSONObject(playerInfoFromMojang);
		String mojangUUID = playerInfoMojangJson.getString("id");
		String mojangName = playerInfoMojangJson.getString("name");
		if (!mojangUUID.equals(uuid)) {
			logger.error("Failed to auth uuid, " + identifier + " submitted " + uuid + ", but mojang said "
					+ mojangUUID);
			return false;
		}
		if (!mojangName.equals(name)) {
			logger.error("Failed to auth user name, " + identifier + " submitted " + name + ", but mojang said "
					+ mojangName);
			return false;
		}
		logger.info("Successfuly authenticated " + identifier + " with uuid " + uuid);
		updatePermissionLevel(Main.getAuthorizedUserManagement().getPermLevel(uuid));
		initialized = true;
		return true;
	}

	public MapDataSyncSession getMapDataSyncSession() {
		return mapSyncSession;
	}

	public PermissionLevel getPermissionLevel() {
		return permLevel;
	}

	public void updatePermissionLevel(PermissionLevel permLevel) {
		this.permLevel = permLevel;
		packetHandler.updatePermissionLevel(permLevel);
	}

	public void resetMapDataSync(int id) {
		mapSyncSession = new MapDataSyncSession(this, id);
	}

	public String getUUID() {
		return uuid;
	}
}
