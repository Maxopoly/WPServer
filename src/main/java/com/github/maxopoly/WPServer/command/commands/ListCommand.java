package com.github.maxopoly.WPServer.command.commands;

import com.github.maxopoly.WPServer.ClientConnection;
import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.command.Command;
import java.util.List;

public class ListCommand extends Command {

	public ListCommand() {
		super("list", 0, 0, "listplayers", "listconnections", "online");
	}

	@Override
	public void execute(String[] args) {
		List<ClientConnection> activeConns = Main.getServerManager().getActiveConnections();
		if (activeConns.size() == 0) {
			Main.getLogger().info("Noone is connected right now :(");
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("A total of " + activeConns.size() + " players is connected: ");
		for (ClientConnection conn : activeConns) {
			sb.append(conn.getIdentifier());
			sb.append("   ");
		}
	}

	@Override
	public String getUsage() {
		return "list";
	}

}
