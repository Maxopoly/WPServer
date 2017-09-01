package com.github.maxopoly.WPServer.command.commands;

import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.command.Command;
import com.github.maxopoly.WPServer.packetCreation.InvalidateAllPlayerInfoPacket;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super("reload", 0, 0, "reloaddb");
	}

	@Override
	public void execute(String[] args) {
		Main.reloadFromDB();
		InvalidateAllPlayerInfoPacket packet = new InvalidateAllPlayerInfoPacket();
		for (ClientConnection conn : Main.getServerManager().getActiveConnections()) {
			conn.sendData(packet);
		}
	}

	@Override
	public String getUsage() {
		return "reload";
	}

}
