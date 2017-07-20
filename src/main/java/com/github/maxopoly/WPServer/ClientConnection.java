package com.github.maxopoly.WPServer;

import com.github.maxopoly.WPCommon.packetHandling.PacketForwarder;
import com.github.maxopoly.WPCommon.util.AES_CFB8_Encrypter;
import com.github.maxopoly.WPCommon.util.ConnectionUtils;
import com.github.maxopoly.WPCommon.util.PKCSEncrypter;
import com.github.maxopoly.WPCommon.util.VarInt;
import com.github.maxopoly.WPServer.packetHandling.ServerSidePacketHandler;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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
	private PacketForwarder forwarder;
	private KeyPair keyPair;
	private AES_CFB8_Encrypter encrypter;
	private byte[] sharedSecret;

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
		System.out.println("d");
		this.forwarder = new ServerSidePacketHandler(this, logger);
		active = true;
		if (!(figureOutEncryption() && authPlayer())) {
			System.out.println("e");
			close();
			return;
		}
		System.out.println("f");
		logger.info("Successfully established connection with " + socket.getInetAddress().toString());
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public void run() {
		while (active) {
			JSONObject packet = readPacket();
			if (packet == null) {
				break;
			}
			forwarder.handlePacket(packet);
		}
	}

	private JSONObject readPacket() {
		try {
			int packetLength = VarInt.readVarInt(input, encrypter);
			byte[] dataArray = new byte[packetLength];
			input.readFully(dataArray);
			byte[] decryptedData = encrypter.decrypt(dataArray);
			String msg = new String(decryptedData, StandardCharsets.UTF_8);
			return new JSONObject(msg);
		} catch (IOException e) {
			close();
			logger.error("IOError while reading packets, killing connection", e);
			return null;
		}
	}

	public void sendData(JSONObject json) {
		if (!active) {
			return;
		}
		synchronized (output) {
			String jsonString = json.toString();
			try {
				byte[] data = jsonString.getBytes(StandardCharsets.UTF_8);
				VarInt.writeVarInt(output, data.length);
				byte[] encrypted = encrypter.encrypt(data);
				output.write(encrypted);
			} catch (IOException e) {
				close();
				logger.error("Failed to send json reply", e);
			}
		}
	}

	public void close() {
		active = false;
		try {
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
				logger.error("Encrypted secret had wrong length, expected 128, but got " + encryptedSecretLength);
				return false;
			}
			sharedSecret = new byte[encryptedSecretLength];
			input.readFully(sharedSecret);
			int encryptedConfKeyLength = VarInt.readVarInt(input);
			if (encryptedConfKeyLength != 128) {
				logger.error("Encrypted confirmation key had wrong length, expected 128, but got " + encryptedConfKeyLength);
				return false;
			}
			receivedConfirmationKey = new byte[encryptedConfKeyLength];
			input.readFully(receivedConfirmationKey);
		} catch (IOException e) {
			logger.error("Failed to send pub key", e);
			return false;
		}
		sharedSecret = PKCSEncrypter.decrypt(sharedSecret, keyPair.getPrivate());
		receivedConfirmationKey = PKCSEncrypter.decrypt(receivedConfirmationKey, keyPair.getPrivate());
		for (int i = 0; i < confirmationkey.length; i++) {
			if (confirmationkey[i] != receivedConfirmationKey[i]) {
				logger.error("Received wrong confirmation key back");
				return false;
			}
		}
		encrypter = new AES_CFB8_Encrypter(sharedSecret, sharedSecret);
		logger.info("Successfully exchanged encryption details");
		return true;
	}

	private boolean authPlayer() {
		JSONObject playerInfo = readPacket();
		String name = playerInfo.getString("name");
		String uuid = playerInfo.getString("uuid");
		logger.info(socket.getInetAddress().getHostAddress() + " is trying to auth as " + name);
		String hash;
		try {
			hash = ConnectionUtils.generateKeyHash(uuid, sharedSecret, keyPair.getPublic().getEncoded());
		} catch (IOException e) {
			logger.error("Failed to gen key hash", e);
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
			logger.error("Failed to auth uuid, user submitted " + uuid + ", but mojang said " + mojangUUID);
			return false;
		}
		if (!mojangName.equals(name)) {
			logger.error("Failed to auth user name, user submitted " + name + ", but mojang said " + mojangName);
			return false;
		}
		logger.info("Successfuly authenticated " + name + " with uuid " + uuid);
		return true;
	}
}
