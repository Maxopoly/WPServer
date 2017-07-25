package com.github.maxopoly.WPServer.database;

import com.github.maxopoly.WPCommon.model.Faction;
import com.github.maxopoly.WPCommon.model.MCAccount;
import com.github.maxopoly.WPCommon.model.Player;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;

public class AltDAO {

	private static final String createAccountTable = "create table if not exists accounts(id int not null auto_increment, name varchar(16) not null, "
			+ "altGroupId int not null, primary key (id), unique key uniqueName (name));";
	private static final String createFactionTable = "create table if not exists factions(id int not null auto_increment, name varchar(255) not null, "
			+ "standing int not null default '0', primary key (id), unique key uniqueName (name));";
	private static final String createMainTable = "create table if not exists mains(id int not null, altGroupId int not null, standing int not null "
			+ "default '0', faction_id int, primary key (altGroupId), constraint mainKey foreign key (id) references accounts(id) on delete cascade "
			+ "on update no action, constraint factionKey foreign key (faction_id) references factions(id) on delete set null  on update no action);";

	private static final String getAlts = "select a2.id, a2.name,a2.altGroupId from accounts a1 inner join accounts a2 on a1.altGroupId = a2.altGroupId where a1.name = ?;";
	private static final String getMainInfo = "select m.id, m.standing, m.faction_id, f.name, f.standing from mains m inner join factions f on m.faction_id=f.id where altGroupId = ?;";

	private static final String getAllFactions = "select id,name,standing from factions";
	private static final String getAllMains = "select m.id, m.altGroupId, m.standing, m.faction_id, a.name from mains m inner join accounts a on m.id = a.id;";
	private static final String getAllALts = "select a.id, a.altGroupId, a.name from accounts a left join mains m on a.id = m.id where m.id is null;  ";

	private DBConnection connection;
	private Logger logger;

	public AltDAO(DBConnection connection, Logger logger) {
		this.logger = logger;
		this.connection = connection;
		if (!createTables()) {
			logger.error("Failed to init alt DB, shutting down");
			System.exit(1);
		}
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
					return new Player(f, alts, main, standing);
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

	public Map[] loadAll() {
		Map[] result = new Map[2];
		Map<String, Player> players = new HashMap<String, Player>();
		Map<String, Faction> factions = new HashMap<String, Faction>();
		Map<Integer, Faction> factionsById = new HashMap<Integer, Faction>();
		Map<Integer, Player> playersById = new HashMap<Integer, Player>();
		result[0] = players;
		result[1] = factions;
		try (Connection conn = connection.getConnection();
				PreparedStatement prep = conn.prepareStatement(getAllFactions);
				ResultSet rs = prep.executeQuery()) {
			while (rs.next()) {
				int factionID = rs.getInt(1);
				String factionName = rs.getString(2);
				int factionStanding = rs.getInt(3);
				Faction f = new Faction(factionID, factionName, factionStanding);
				factions.put(f.getName().toLowerCase(), f);
				factionsById.put(f.getID(), f);
			}
		} catch (SQLException e) {
			logger.error("Failed to retrieve all factions", e);
			return null;
		}
		logger.info("Loaded " + factions.size() + " factions from DB");
		try (Connection conn = connection.getConnection();
				PreparedStatement prep = conn.prepareStatement(getAllMains);
				ResultSet rs = prep.executeQuery()) {
			while (rs.next()) {
				int accID = rs.getInt(1);
				int altID = rs.getInt(2);
				int standing = rs.getInt(3);
				int factionID = rs.getInt(4);
				String name = rs.getString(5);
				List<MCAccount> accounts = new LinkedList<MCAccount>();
				MCAccount main = new MCAccount(accID, name);
				accounts.add(main);
				Faction fac = factionsById.get(factionID);
				Player player = new Player(fac, accounts, main, standing, altID);
				playersById.put(altID, player);
				players.put(name.toLowerCase(), player);
			}
		} catch (SQLException e) {
			logger.error("Failed to retrieve all factions", e);
			return null;
		}
		logger.info("Loaded " + players.size() + " unique players from DB");
		try (Connection conn = connection.getConnection();
				PreparedStatement prep = conn.prepareStatement(getAllALts);
				ResultSet rs = prep.executeQuery()) {
			while (rs.next()) {
				int accID = rs.getInt(1);
				int altID = rs.getInt(2);
				String name = rs.getString(3);
				MCAccount alt = new MCAccount(accID, name);
				Player main = playersById.get(altID);
				main.addAlt(alt);
				players.put(name.toLowerCase(), main);
			}
		} catch (SQLException e) {
			logger.error("Failed to retrieve all factions", e);
			return null;
		}
		logger.info("Loaded " + players.size() + " total MC accounts from DB");
		return result;
	}
}
