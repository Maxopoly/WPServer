package com.github.maxopoly.WPServer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.logging.log4j.Logger;

public class AuthDAO {

	private static final String createAuthTable = "create table if not exists authUsers(uuid varchar(36) not null, int rank not null default 0, primary key (uuid));";

	private static final String getRank = "select rank from authUsers where uuid = ?;";
	private static final String updateRank = "update authUsers set rank = ? where uuid = ?;";
	private static final String insertRank = "insert into authUsers (uuid,rank) values(?,?);";

	private DBConnection connection;
	private Logger logger;

	public AuthDAO(DBConnection connection, Logger logger) {
		this.connection = connection;
		this.logger = logger;
		createTables();
	}

	/**
	 * Retrives the rank of a player
	 * 
	 * @param uuid
	 *            UUID of the players whose rank should be retrieved
	 * @return Integer bigger or equal 0 if a rank was found, -1 if no rank was found
	 */
	public int getRank(UUID uuid) {
		try (Connection conn = connection.getConnection(); PreparedStatement prep = conn.prepareStatement(getRank)) {
			prep.setString(1, uuid.toString());
			try (ResultSet rs = prep.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				} else {
					return -1;
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to retrieve rank for " + uuid.toString(), e);
			return -1;
		}
	}

	/**
	 * Sets/updates the rank of a player
	 * 
	 * @param uuid
	 *            UUID of the player whose rank should be changed
	 * @param rank
	 *            New rank
	 */
	public void setRank(UUID uuid, int rank) {
		int currentRank = getRank(uuid);
		if (currentRank == -1) {
			// none so far
			insertRank(uuid, rank);
		} else {
			// has existing rank, so update it
			updateRank(uuid, rank);
		}
	}

	private void insertRank(UUID uuid, int rank) {
		try (Connection conn = connection.getConnection(); PreparedStatement prep = conn.prepareStatement(insertRank)) {
			prep.setString(1, uuid.toString());
			prep.setInt(2, rank);
			prep.execute();
		} catch (SQLException e) {
			logger.error("Failed to insert rank for " + uuid.toString(), e);
		}
	}

	private void updateRank(UUID uuid, int rank) {
		try (Connection conn = connection.getConnection(); PreparedStatement prep = conn.prepareStatement(updateRank)) {
			prep.setInt(1, rank);
			prep.setString(2, uuid.toString());
			prep.execute();
		} catch (SQLException e) {
			logger.error("Failed to update rank for " + uuid.toString(), e);
		}
	}

	private boolean createTables() {
		try (Connection conn = connection.getConnection();
				PreparedStatement prep = conn.prepareStatement(createAuthTable)) {
			prep.execute();
		} catch (SQLException e) {
			logger.error("Failed to create auth table", e);
			return false;
		}
		return true;
	}

}
