package com.attendance.controller;

import com.attendance.config.DatabaseConfig;
import com.attendance.model.StudentSummary;
import com.attendance.model.RecentAttendance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardContentController {

    @FXML private Label lblTotalStudents, lblPresentToday, lblDateTime;
    @FXML private TableView<StudentSummary> studentTable;
    @FXML private TableColumn<StudentSummary, String> colId;
    @FXML private TableColumn<StudentSummary, String> colName;
    @FXML private TableView<RecentAttendance> recentAttendanceTable;
    @FXML private TableColumn<RecentAttendance, String> colRecentName;
    @FXML private TableColumn<RecentAttendance, String> colRecentTime;

    @FXML
    public void initialize() {
        lblDateTime.setText(LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRecentName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRecentTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        loadDashboardData();
    }

    public void loadDashboardData() {
        ObservableList<StudentSummary> students = FXCollections.observableArrayList();
        ObservableList<RecentAttendance> recentList = FXCollections.observableArrayList();

        String studentQuery = "SELECT student_id, full_name FROM profiles ORDER BY reg_date DESC";
        String presentQuery = "SELECT COUNT(*) FROM attendance WHERE date = date('now', 'localtime')";
        String recentQuery  = "SELECT student_name, timestamp FROM attendance ORDER BY id DESC LIMIT 5";

        try (Connection conn = DatabaseConfig.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(studentQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new StudentSummary(
                        rs.getString("student_id"),
                        rs.getString("full_name")
                    ));
                }
                studentTable.setItems(students);
                lblTotalStudents.setText(String.valueOf(students.size()));
            }

            try (PreparedStatement ps = conn.prepareStatement(presentQuery);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lblPresentToday.setText(String.valueOf(rs.getInt(1)));
            }

            try (PreparedStatement ps = conn.prepareStatement(recentQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recentList.add(new RecentAttendance(
                        rs.getString("student_name"),
                        rs.getString("timestamp")
                    ));
                }
                recentAttendanceTable.setItems(recentList);
            }

        } catch (Exception e) {
            System.err.println("Dashboard Data Error: " + e.getMessage());
        }
    }
}
