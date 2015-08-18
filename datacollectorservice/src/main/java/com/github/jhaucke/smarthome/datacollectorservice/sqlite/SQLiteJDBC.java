package com.github.jhaucke.smarthome.datacollectorservice.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class SQLiteJDBC {

	private static SQLiteJDBC instance = null;
	private Connection c = null;

	/**
	 * A private Constructor prevents any other class from instantiating.
	 */
	private SQLiteJDBC() {
	}

	/**
	 * Method to get the instance of {@link SQLiteJDBC}.
	 */
	public static SQLiteJDBC getInstance() {
		if (instance == null) {
			instance = new SQLiteJDBC();
		}
		return instance;
	}

	private void openDatabase() {
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			c.setAutoCommit(false);
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}

	public void insertPowerData(int power) {
		Statement stmt = null;
		try {
			openDatabase();

			stmt = c.createStatement();
			String sql = "INSERT INTO POWERDATA (POWER) VALUES (" + power + ");";
			stmt.executeUpdate(sql);

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Records created successfully");
	}
}
