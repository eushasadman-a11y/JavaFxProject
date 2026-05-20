package com.attendance.controller;

import com.attendance.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class DashboardController {
    @FXML
    public void initialize() { }

    @FXML public void showDashboardContent(ActionEvent event) { MainApp.loadCenterView("/fxml/dashboard_content.fxml"); }
    @FXML public void onOpenCamera(ActionEvent event)         { MainApp.loadCenterView("/fxml/camera_view.fxml"); }
    @FXML public void showAttendance(ActionEvent event)       { MainApp.loadCenterView("/fxml/attendance_view.fxml"); }
    @FXML public void showProfiles(ActionEvent event)         { MainApp.loadCenterView("/fxml/profiles_view.fxml"); }
    @FXML public void showDepartments(ActionEvent event)      { MainApp.loadCenterView("/fxml/departments_view.fxml"); }
}
