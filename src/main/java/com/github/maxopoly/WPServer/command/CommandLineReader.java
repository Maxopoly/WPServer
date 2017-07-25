package com.github.maxopoly.WPServer.command;

import java.io.Console;
import org.apache.logging.log4j.Logger;

public class CommandLineReader {

	private Logger logger;
	private CommandHandler cmdHandler;

	public CommandLineReader(Logger logger) {
		this.logger = logger;
		this.cmdHandler = new CommandHandler(logger);
	}

	public void start() {
		Console c = System.console();
		if (c == null) {
			logger.error("No open console was found, assuming we are running as daemon and continue anyway");
			return;
		}
		while (true) {
			String msg = c.readLine("");
			cmdHandler.handle(msg);
		}
	}
}
