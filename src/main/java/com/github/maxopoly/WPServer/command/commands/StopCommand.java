package com.github.maxopoly.WPServer.command.commands;

import com.github.maxopoly.WPServer.command.Command;
import com.github.maxopoly.WPServer.model.ChestManagement;

public class StopCommand extends Command {

	public StopCommand() {
		super("exit", 0, 0, "stop", "end", "close");
	}

	@Override
	public void execute(String[] args) {
		ChestManagement.getInstance().saveToFile();
		System.exit(0);
	}

	@Override
	public String getUsage() {
		return "stop";
	}

}
