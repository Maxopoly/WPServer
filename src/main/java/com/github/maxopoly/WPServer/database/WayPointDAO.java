package com.github.maxopoly.WPServer.database;

import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.WPWayPoint;
import com.github.maxopoly.WPCommon.model.WPWayPointGroup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.Logger;

public class WayPointDAO {

	private static final String createWayPointTable = "create table if not exists waypoints(name varchar(128) not null, "
			+ "x int not null, y int not null, z int not null, color int not null, groupName varchar(128) not null);";

	private static final String loadAllWayPoints = "select name, x, y, z, color, groupName from waypoints;";

	private DBConnection connection;
	private Logger logger;

	public WayPointDAO(DBConnection connection, Logger logger) {
		this.connection = connection;
		this.logger = logger;
		createTables();
	}

	public Set<WPWayPoint> loadAll() {
		Set<WPWayPoint> points = new HashSet<WPWayPoint>();
		try (Connection conn = connection.getConnection();
				PreparedStatement prep = conn.prepareStatement(loadAllWayPoints);
				ResultSet rs = prep.executeQuery()) {
			while (rs.next()) {
				String name = rs.getString(1);
				int x = rs.getInt(2);
				int y = rs.getInt(3);
				int z = rs.getInt(4);
				int color = rs.getInt(5);
				String group = rs.getString(6);
				WPWayPointGroup enumGroup;
				try {
					enumGroup = WPWayPointGroup.valueOf(group.toUpperCase());
				} catch (IllegalArgumentException e) {
					logger.warn("Skipping loading of way point " + name + ", because it's group " + group
							+ " was unknown");
					continue;
				}
				points.add(new WPWayPoint(new Location(x, y, z), name, color, enumGroup));
			}
		} catch (SQLException e) {
			logger.error("Failed to load all waypoints", e);
			return null;
		}
		logger.info("Loaded " + points.size() + " waypoints from db");
		return points;
	}

	private boolean createTables() {
		try (Connection conn = connection.getConnection();
				PreparedStatement prep = conn.prepareStatement(createWayPointTable)) {
			prep.execute();
		} catch (SQLException e) {
			logger.error("Failed to create waypoint table", e);
			return false;
		}
		return true;
	}

}
