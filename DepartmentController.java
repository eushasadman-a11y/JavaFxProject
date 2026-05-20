package com.attendance.controller;

import com.attendance.config.DatabaseConfig;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class DepartmentController {

    @FXML private TextField deptNameField, deptHeadField, deptLocField, searchDept;
    @FXML private TableView<Department> deptTable;
    @FXML private TableColumn<Department, String> colName, colHead, colLoc;

    private final ObservableList<Department> deptList = FXCollections.observableArrayList();
    private FilteredList<Department> filteredList;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colHead.setCellValueFactory(new PropertyValueFactory<>("head"));
        colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));

        filteredList = new FilteredList<>(deptList, p -> true);
        deptTable.setItems(filteredList);

        searchDept.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal == null ? "" : newVal.toLowerCase().trim();
            filteredList.setPredicate(dept -> {
                if (lower.isEmpty()) return true;
                return dept.getName().toLowerCase().contains(lower)
                    || dept.getHead().toLowerCase().contains(lower)
                    || dept.getLocation().toLowerCase().contains(lower);
            });
        });

        loadDepartments();
    }

    private void loadDepartments() {
        deptList.clear();
        String query = "SELECT name, head, location FROM departments ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                deptList.add(new Department(
                    rs.getString("name"),
                    rs.getString("head"),
                    rs.getString("location")
                ));
            }
        } catch (Exception e) {
            System.err.println("Load Error: " + e.getMessage());
        }
    }

    @FXML
    private void onSaveDept() {
        String name = deptNameField.getText().trim();
        String head = deptHeadField.getText().trim();
        String loc = deptLocField.getText().trim();

        if (name.isEmpty()) { showAlert("Error", "Name is required!"); return; }

        String sql = "INSERT INTO departments (name, head, location) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, head.isEmpty() ? "Unknown" : head);
            pstmt.setString(3, loc.isEmpty() ? "N/A" : loc);
            pstmt.executeUpdate();
            loadDepartments();
            onClearDept();
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }

    @FXML private void onClearDept() {
        deptNameField.clear(); deptHeadField.clear(); deptLocField.clear();
        searchDept.clear();
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
