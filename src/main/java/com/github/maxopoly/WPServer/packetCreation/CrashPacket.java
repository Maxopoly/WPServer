package com.github.maxopoly.WPServer.packetCreation;

import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;

public class CrashPacket extends AbstractJsonPacket {

	public CrashPacket() {
		super("crash");
	}

}
