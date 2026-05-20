package com.attendance.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final String URL = "jdbc:sqlite:attendance.db";

    static {
        try { Class.forName("org.sqlite.JDBC"); }
        catch (ClassNotFoundException e) { throw new RuntimeException("SQLite JDBC Driver not found!", e); }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT DEFAULT 'Admin'
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS profiles (
                    student_id TEXT PRIMARY KEY,
                    full_name TEXT NOT NULL,
                    email TEXT,
                    department TEXT,
                    role TEXT,
                    phone TEXT,
                    photo_path TEXT,
                    reg_date DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS attendance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    student_id TEXT,
                    student_name TEXT,
                    department TEXT,
                    date TEXT,
                    timestamp TEXT,
                    status TEXT,
                    method TEXT DEFAULT 'Face ID',
                    FOREIGN KEY (student_id) REFERENCES profiles(student_id)
                )
            """);

            try { stmt.execute("ALTER TABLE attendance ADD COLUMN department TEXT"); } catch (Exception ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN method TEXT DEFAULT 'Face ID'"); } catch (Exception ignored) {}

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS departments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE,
                    head TEXT DEFAULT 'Unknown',
                    location TEXT DEFAULT 'N/A'
                )
            """);

            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('admin', 'admin123', 'Admin')");
            System.out.println("Database Initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database Init Error: " + e.getMessage());
        }
    }
}
