package com.github.maxopoly.WPServer.database;

import com.github.maxopoly.WPCommon.model.permission.PermissionLevel;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevelManagement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Logger;

public class AuthDAO {

	private static final String createAuthTable = "create table if not exists authUsers(uuid varchar(36) not null, name varchar(32), "
			+ "permissions int not null, primary key (uuid));";

	private static final String getAllUsers = "select * from authUsers";

	private DBConnection connection;
	private Logger logger;

	public AuthDAO(DBConnection connection, Logger logger) {
		this.connection = connection;
		this.logger = logger;
		createTables();
	}

	private boolean createTables() {
		try (Connection conn = connection.getConnection(); PreparedStatement prep = conn.prepareStatement(createAuthTable)) {
			prep.execute();
		} catch (SQLException e) {
			logger.error("Failed to create auth table", e);
			return false;
		}
		return true;
	}

	public Map<String, PermissionLevel> loadAll() {
		Map<String, PermissionLevel> perms = new HashMap<String, PermissionLevel>();
		try (Connection conn = connection.getConnection();
				PreparedStatement prep = conn.prepareStatement(getAllUsers);
				ResultSet rs = prep.executeQuery()) {
			while (rs.next()) {
				String uuid = rs.getString(1);
				int permLevel = rs.getInt(3);
				perms.put(uuid, PermissionLevelManagement.getPermissionLevel(permLevel));
			}
		} catch (SQLException e) {
			logger.error("Failed to load all authorized users", e);
			return null;
		}
		logger.info("Loaded " + perms.size() + " authorized users from db");
		return perms;
	}

}
