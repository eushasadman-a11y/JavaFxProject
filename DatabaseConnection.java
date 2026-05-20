package com.attendance.util;

import com.attendance.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver missing! Add it to your dependencies.");
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DatabaseConfig.getConnection();
            
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Retrieved connection is null or already closed.");
            }
            
            return conn;
            
        } catch (SQLException e) {
            throw new SQLException("CRITICAL: Could not connect to the Attendance database. " + e.getMessage(), e);
        } catch (Exception e) {
            throw new SQLException("Unexpected error during database access: " + e.getMessage(), e);
        }
    }
}