package com.github.maxopoly.WPServer.command.commands;

import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.ServerManager;
import com.github.maxopoly.WPServer.command.Command;
import com.github.maxopoly.WPServer.packetCreation.CrashPacket;

public class CrashCommand extends Command {

	public CrashCommand() {
		super("crash", 1, 1, "kill");
	}

	@Override
	public void execute(String[] args) {
		ServerManager manager = Main.getServerManager();
		String nameToMatch = args[0].toLowerCase();
		for (ClientConnection conn : manager.getActiveConnections()) {
			if (nameToMatch.equals(conn.getIdentifier().toLowerCase())) {
				conn.sendData(new CrashPacket());
				Main.getLogger().info("Crashing " + conn.getIdentifier());
				return;
			}
		}
		Main.getLogger().info("No player connected with the name " + args[0]);
	}

	@Override
	public String getUsage() {
		return "crash <playerName>";
	}

}
