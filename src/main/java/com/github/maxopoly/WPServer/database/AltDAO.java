package com.github.maxopoly.WPServer.database;

import com.github.maxopoly.WPCommon.model.Faction;
import com.github.maxopoly.WPCommon.model.MCAccount;
import com.github.maxopoly.WPCommon.model.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.Logger;

public class AltDAO {

	// singleton
	private static AltDAO instance;

	public static AltDAO getInstance() {
		if (instance == null) {
			instance = new AltDAO();
		}
		return instance;
	}

	private static final String createAccountTable = "create table if not exists accounts(id int not null auto_increment, name varchar(16) not null, "
			+ "altGroupId int not null, primary key (id), unique key uniqueName (name));";
	private static final String createFactionTable = "create table if not exists factions(id int not null auto_increment, name varchar(255) not null, "
			+ "standing int not null default '0', primary key (id), unique key uniqueName (name));";
	private static final String createMainTable = "create table if not exists mains(id int not null, altGroupId int not null, standing int not null "
			+ "default '0', faction_id int, primary key (altGroupId), constraint mainKey foreign key (id) references accounts(id) on delete cascade "
			+ "on update no action, constraint factionKey foreign key (faction_id) references factions(id) on delete set null  on update no action);";

	private static final String getAlts = "select a2.id, a2.name,a2.altGroupId from accounts a1 inner join accounts a2 on a1.altGroupId = a2.altGroupId where a1.name = ?;";
	private static final String getMainInfo = "select m.id, m.standing, m.faction_id, f.name, f.standing from mains m inner join factions f on m.faction_id=f.id where altGroupId = ?;";

	private DBConnection connection;
	private Logger logger;

	private AltDAO() {
		createTables();
	}

	private boolean createTables() {
		try (Connection conn = connection.getConnection();
				PreparedStatement prep = conn.prepareStatement(createFactionTable)) {
			prep.execute();
		} catch (SQLException e) {
			logger.error("Failed to create faction table", e);
			return false;

		}
		try (Connection conn = connection.getConnection();
				PreparedStatement prep = conn.prepareStatement(createAccountTable)) {
			prep.execute();
		} catch (SQLException e) {
			logger.error("Failed to create account table", e);
			return false;
		}
		try (Connection conn = connection.getConnection(); PreparedStatement prep = conn.prepareStatement(createMainTable)) {
			prep.execute();
		} catch (SQLException e) {
			logger.error("Failed to create main table", e);
			return false;

		}
		return true;
	}

	public Player getPlayerInfo(String altName) {
		List<MCAccount> alts = new LinkedList<MCAccount>();
		List<String> altNames = new LinkedList<String>();
		int altGroup = -1;
		try (Connection conn = connection.getConnection(); PreparedStatement prep = conn.prepareStatement(getAlts)) {
			prep.setString(1, altName);
			try (ResultSet rs = prep.executeQuery()) {
				while (rs.next()) {
					int id = rs.getInt(1);
					String name = rs.getString(2);
					altGroup = rs.getInt(3);
					MCAccount acc = new MCAccount(name);
					alts.add(acc);
					altNames.add(name);
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to retrieve alts for " + altName, e);
			return null;
		}
		if (altGroup == -1) {
			logger.error("Failed to retrieve alt group for " + altName);
			return null;
		}
		try (Connection conn = connection.getConnection(); PreparedStatement prep = conn.prepareStatement(getMainInfo)) {
			prep.setInt(1, altGroup);
			try (ResultSet rs = prep.executeQuery()) {
				if (rs.next()) {
					int mainID = rs.getInt(1);
					int standing = rs.getInt(2);
					int factionID = rs.getInt(3);
					String factionName = rs.getString(4);
					int factionStanding = rs.getInt(5);
					MCAccount main = null;
					for (MCAccount curr : alts) {
						if (curr.getID() == mainID) {
							main = curr;
						}
					}
					if (main == null) {
						logger.error("Failed to find main for " + altName + ". Inconsistent db scheme?");
						return null;
					}
					Faction f = new Faction(factionName, factionStanding);
					return new Player(f, altNames, main.getName(), standing);
				} else {
					logger.error("Failed to find main for " + altName + ". Inconsistent db scheme?");
					return null;
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to retrieve alts for " + altName, e);
			return null;
		}
	}
}
