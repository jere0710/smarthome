package com.github.jhaucke.smarthome.basic.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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
			c = DriverManager.getConnection("jdbc:sqlite:../smarthome.db");
		} catch (SQLException | ClassNotFoundException e) {
			logger.error(e.getMessage());
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
			logger.error(e.getMessage());
		}
		logger.info("power record created successfully");
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
		logger.info("last 5 minutes selected");
		return last5Minutes;
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
			logger.error(e.getMessage());
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
			logger.error(e.getMessage());
		}
		logger.info("state of actuator successfully updated");
	}
}
