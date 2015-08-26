package com.github.jhaucke.smarthome.datacollectorservice.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to access the database.
 */
public class SQLiteJDBC {

	private Connection c = null;
	private final Logger logger;

	/**
	 * Constructor for {@link SQLiteJDBC}.
	 */
	public SQLiteJDBC() {
		super();
		logger = LoggerFactory.getLogger(SQLiteJDBC.class);

		openDatabase();
	}

	private void openDatabase() {
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:../test.db");
			createTables();
		} catch (SQLException | ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
		logger.info("Opened database successfully");
	}

	private void createTables() {
		try {
			Statement stmt = c.createStatement();
			String createPowerDataSql = "CREATE TABLE PowerData ( "
					+ "ID			INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, "
					+ "Timestamp	NUMERIC NOT NULL DEFAULT CURRENT_TIMESTAMP UNIQUE, "
					+ "Power		INTEGER NOT NULL " + ")";
			stmt.executeUpdate(createPowerDataSql);
			stmt.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}

	public void insertPowerData(int power) {
		try {
			Statement stmt = c.createStatement();
			String sql = "INSERT INTO POWERDATA (POWER) VALUES (" + power + ");";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
		logger.info("Records created successfully");
	}
}
