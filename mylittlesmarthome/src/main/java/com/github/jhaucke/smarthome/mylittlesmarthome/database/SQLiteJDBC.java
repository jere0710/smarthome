package com.github.jhaucke.smarthome.mylittlesmarthome.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
			Properties config = new Properties();
			config.put("journal_mode", "WAL");
			c = DriverManager.getConnection("jdbc:sqlite:../smarthome.db", config);
		} catch (ClassNotFoundException e) {
			logger.error("message: " + e.getMessage() + " cause: " + e.getCause());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error("state: " + e.getSQLState() + " code: " + e.getErrorCode() + " message: " + e.getMessage()
					+ " cause: " + e.getCause());
		}

		logger.info("opened database successfully");
	}

	public void insertPowerData(int power) {
		try {
			Statement stmt = c.createStatement();
			String sql = "INSERT INTO PowerData (Power) VALUES (" + power + ");";
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			logger.error("state: " + e.getSQLState() + " code: " + e.getErrorCode() + " message: " + e.getMessage()
					+ " cause: " + e.getCause());
		}
		logger.info("power record created successfully");
	}

	public List<Integer> selectTheLast2Minutes() {
		List<Integer> last2Minutes = new ArrayList<Integer>();
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT Power FROM PowerData WHERE Timestamp > datetime('now', '-2 minutes');");
			while (rs.next()) {
				last2Minutes.add(rs.getInt("Power"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			logger.error("state: " + e.getSQLState() + " code: " + e.getErrorCode() + " message: " + e.getMessage()
					+ " cause: " + e.getCause());
		}
		logger.info("last 2 minutes selected");
		return last2Minutes;
	}

	public Integer selectStateOfActuator(int actuatorId) {

		Integer actuatorStateId = null;
		try {
			Statement stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ID_ActuatorState FROM Actuator WHERE ID = " + actuatorId + ";");
			while (rs.next()) {
				actuatorStateId = rs.getInt("ID_ActuatorState");
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			logger.error("state: " + e.getSQLState() + " code: " + e.getErrorCode() + " message: " + e.getMessage()
					+ " cause: " + e.getCause());
		}
		logger.info("current state of actuator selected");
		return actuatorStateId;
	}

	public void updateStateOfActuator(int actuatorId, int actuatorStateId) {
		try {
			Statement stmt = c.createStatement();
			String sql = "UPDATE Actuator SET ID_ActuatorState = " + actuatorStateId + " WHERE ID = " + actuatorId
					+ ";";
			stmt.executeUpdate(sql);
			// AutoCommit
			stmt.close();
		} catch (SQLException e) {
			logger.error("state: " + e.getSQLState() + " code: " + e.getErrorCode() + " message: " + e.getMessage()
					+ " cause: " + e.getCause());
		}
		logger.info("state of actuator successfully updated");
	}
}
