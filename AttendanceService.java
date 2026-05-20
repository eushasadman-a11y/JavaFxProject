package com.attendance.service;

import com.attendance.config.DatabaseConfig;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AttendanceService {

    public void markAttendance(String studentId) {
        String today = LocalDate.now().toString();
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (isAlreadyMarked(conn, studentId, today)) {
                System.out.println("Attendance already marked for: " + studentId);
                return;
            }

            String fetchSql = "SELECT full_name, department FROM profiles WHERE student_id = ?";
            try (PreparedStatement fetchPs = conn.prepareStatement(fetchSql)) {
                fetchPs.setString(1, studentId);
                try (ResultSet rs = fetchPs.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("full_name");
                        String dept = rs.getString("department");

                        String insertSql = "INSERT INTO attendance(student_id, student_name, department, date, timestamp, status, method) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                            insertPs.setString(1, studentId);
                            insertPs.setString(2, name);
                            insertPs.setString(3, dept != null ? dept : "N/A");
                            insertPs.setString(4, today);
                            insertPs.setString(5, timestamp);
                            insertPs.setString(6, "Present");
                            insertPs.setString(7, "Face ID");
                            insertPs.executeUpdate();
                            System.out.println("Attendance marked: " + name + " at " + timestamp);
                        }
                    } else {
                        System.out.println("No profile found for student_id: " + studentId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Service Error: " + e.getMessage());
        }
    }

    private boolean isAlreadyMarked(Connection conn, String studentId, String date) throws SQLException {
        String sql = "SELECT count(*) FROM attendance WHERE student_id = ? AND date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, date);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
