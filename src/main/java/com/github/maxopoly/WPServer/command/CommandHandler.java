package com.github.maxopoly.WPServer.command;

import com.github.maxopoly.WPServer.command.commands.Reload;
import com.github.maxopoly.WPServer.command.commands.Stop;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.logging.log4j.Logger;

public class CommandHandler {

	private Map<String, Command> commands;
	private Logger logger;

	public CommandHandler(Logger logger) {
		this.commands = new HashMap<String, Command>();
		this.logger = logger;
		registerCommands();
	}

	/**
	 * Registers all native commands
	 */
	private synchronized void registerCommands() {
		registerCommand(new Stop());
		registerCommand(new Reload());
		logger.info("Loaded total of " + commands.values().size() + " commands");
	}

	public synchronized void registerCommand(Command command) {
		commands.put(command.getIdentifier().toLowerCase(), command);
		if (command.getAlternativeIdentifiers() != null) {
			for (String alt : command.getAlternativeIdentifiers()) {
				commands.put(alt.toLowerCase(), command);
			}
		}
	}

	public synchronized void handle(String input) {
		if (input == null || input.equals("")) {
			return;
		}
		String[] args = input.split(" ");
		if (args[0].toLowerCase().equals("help")) {
			help();
			return;
		}
		Command comm = commands.get(args[0]);
		if (comm == null) {
			logger.warn(args[0] + " is not a valid command");
			return;
		}
		if (args.length == 1) {
			args = new String[0];
		} else {
			args = Arrays.copyOfRange(args, 1, args.length);
		}
		if (args.length < comm.minimumArgs()) {
			logger.warn(comm.getIdentifier() + " requires at least " + comm.minimumArgs() + " parameter");
			logger.info("Usage: " + comm.getUsage());
			return;
		}
		if (args.length > comm.maximumArgs()) {
			logger.warn(comm.getIdentifier() + " accepts at maximum " + comm.maximumArgs() + " parameter");
			logger.info("Usage: " + comm.getUsage());
			return;
		}
		comm.execute(args);
	}

	public void help() {
		// turn into hashset to filter out duplicates
		for (Command comm : new HashSet<Command>(commands.values())) {
			logger.info(" - " + comm.getUsage());
		}
	}

}
