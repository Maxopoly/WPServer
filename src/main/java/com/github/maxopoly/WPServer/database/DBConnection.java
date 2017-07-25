package com.github.maxopoly.WPServer.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.logging.log4j.Logger;

public class DBConnection {

	private HikariDataSource datasource;
	private Logger logger;

	public DBConnection(Logger logger, String user, String password, String host, int port, String database,
			int poolSize, long connectionTimeout, long idleTimeout, long maxLifetime) {
		this.logger = logger;
		HikariConfig config = new HikariConfig();
		try {
			Class.forName("org.mariadb.jdbc.MySQLDataSource");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
		config.setConnectionTimeout(connectionTimeout);
		config.setIdleTimeout(idleTimeout);
		config.setMaxLifetime(maxLifetime);
		config.setMaximumPoolSize(poolSize);
		config.setUsername(user);
		if (password != null) {
			config.setPassword(password);
		}
		this.datasource = new HikariDataSource(config);

		try (Connection connection = getConnection(); Statement statement = connection.createStatement();) {
			statement.executeQuery("SELECT 1");
		} catch (SQLException se) {
			logger.error("Unable to initialize Database", se);
			this.datasource = null;
		}
	}

	/**
	 * Gets a single connection from the pool for use. Checks for null database first.
	 * 
	 * @return A new Connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		available();
		return this.datasource.getConnection();
	}

	/**
	 * Closes all connections and this connection pool.
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		available();
		this.datasource.close();
		this.datasource = null;
	}

	/**
	 * Quick test; either ends or throws an exception if data source isn't configured.
	 * 
	 * @throws SQLException
	 */
	public void available() throws SQLException {
		if (this.datasource == null) {
			throw new SQLException("No Datasource Available");
		}
	}

	/**
	 * Available for direct use within this package, use the provided public methods for anything else
	 * 
	 * @return DataSource being used
	 */
	HikariDataSource getHikariDataSource() {
		return datasource;
	}

}
