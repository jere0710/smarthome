package com.github.jhaucke.smarthome.watchdogservice.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
		} catch (SQLException | ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
		logger.info("Opened database successfully");
	}

	public List<Integer> selectTheLast5Minutes() {
		List<Integer> last5Minutes = new ArrayList<Integer>();
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT Power FROM PowerData WHERE Timestamp > datetime('now', '-5 minutes');");
			while (rs.next()) {
				last5Minutes.add(rs.getInt("Power"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
		logger.info("Operation done successfully");
		return last5Minutes;
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
