package com.github.maxopoly.WPServer.command.commands;

import com.github.maxopoly.WPServer.Main;
import com.github.maxopoly.WPServer.command.Command;

public class Reload extends Command {

	public Reload() {
		super("reload", 0, 0, "reloaddb");
	}

	@Override
	public void execute(String[] args) {
		Main.reloadFromDB();
	}

	@Override
	public String getUsage() {
		return "reload";
	}

}
