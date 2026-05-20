package com.attendance.controller;

import com.attendance.service.FaceRecognitionService;
import com.attendance.config.DatabaseConfig;
import com.attendance.model.AttendanceRecord;
import com.attendance.service.AttendanceService;
import com.attendance.util.CameraUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.ResourceBundle;

public class AttendanceController implements Initializable {

    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> colStudent, colDept, colDate, colTime, colStatus;
    @FXML private ImageView cameraView;
    @FXML private Label lblScannerStatus;
    @FXML private Button btnScanner;

    private final ObservableList<AttendanceRecord> attendanceList = FXCollections.observableArrayList();
    private FaceRecognitionService faceService;
    private final AttendanceService attendanceService = new AttendanceService();
    private final CameraUtil cameraUtil = new CameraUtil();
    private boolean isScannerRunning = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("department"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        attendanceTable.setItems(attendanceList);
        loadAttendanceData();
    }

    @FXML
    public void toggleScanner(ActionEvent event) {
        if (!isScannerRunning) {
            startScanner();
        } else {
            stopScanner();
        }
    }

    private void startScanner() {
        if (faceService == null) {
            try {
                faceService = new FaceRecognitionService();
            } catch (Exception e) {
                lblScannerStatus.setText("AI Service failed to load: " + e.getMessage());
                lblScannerStatus.setStyle("-fx-text-fill: red;");
                return;
            }
        }

        boolean success = cameraUtil.startCamera(cameraView, frame -> {
            if (frame != null && !frame.empty()) {
                List<Integer> intLabels = faceService.recognizeMultiple(frame);
                processDetections(intLabels);
            }
        });

        if (success) {
            isScannerRunning = true;
            btnScanner.setText("Stop Scanner");
            lblScannerStatus.setText("Scanner is Online — point at face...");
            lblScannerStatus.setStyle("-fx-text-fill: green;");
        } else {
            lblScannerStatus.setText("Failed to open Camera!");
            lblScannerStatus.setStyle("-fx-text-fill: red;");
        }
    }

    private void processDetections(List<Integer> intLabels) {
        if (intLabels == null || intLabels.isEmpty()) {
            return;
        }

        for (Integer intLabel : intLabels) {
            if (intLabel == null || intLabel == -1) {
                Platform.runLater(() -> {
                    lblScannerStatus.setText("Scanning... Unknown face detected.");
                    lblScannerStatus.setStyle("-fx-text-fill: orange;");
                });
            } else {
                String studentId = faceService.getLabelToStudentId(intLabel);
                if (studentId == null) {
                    Platform.runLater(() -> {
                        lblScannerStatus.setText("Face matched but ID not found in database.");
                        lblScannerStatus.setStyle("-fx-text-fill: orange;");
                    });
                    continue;
                }

                String displayName = getStudentName(studentId);
                attendanceService.markAttendance(studentId);

                Platform.runLater(() -> {
                    lblScannerStatus.setText("Recognised: " + displayName + " (ID: " + studentId + ")");
                    lblScannerStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    loadAttendanceData();
                });
            }
        }
    }

    private String getStudentName(String studentId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT full_name FROM profiles WHERE student_id = ?")) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("full_name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Name lookup error: " + e.getMessage());
        }
        return studentId;
    }

    public void stopScanner() {
        isScannerRunning = false;
        cameraUtil.stopCamera();
        if (btnScanner != null) {
            btnScanner.setText("Start Scanner");
        }
        if (lblScannerStatus != null) {
            lblScannerStatus.setText("Scanner is Offline");
            lblScannerStatus.setStyle("-fx-text-fill: black;");
        }
        Platform.runLater(() -> { if (cameraView != null) {
            cameraView.setImage(null);
        } });

        if (faceService != null) {
            faceService.shutdown();
            faceService = null;
        }
    }

    @FXML
    public void loadAttendanceData() {
        ObservableList<AttendanceRecord> tempList = FXCollections.observableArrayList();
        String sql = "SELECT p.full_name, COALESCE(p.department,'N/A') as department, " +
                     "a.date, a.timestamp, a.status " +
                     "FROM attendance a " +
                     "JOIN profiles p ON a.student_id = p.student_id " +
                     "WHERE a.date = date('now', 'localtime') ORDER BY a.id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tempList.add(new AttendanceRecord(
                    rs.getString("full_name"),
                    rs.getString("department"),
                    rs.getString("date"),
                    rs.getString("timestamp"),
                    rs.getString("status")
                ));
            }
            Platform.runLater(() -> attendanceList.setAll(tempList));
        } catch (SQLException e) {
            System.err.println("Attendance Load Error: " + e.getMessage());
        }
    }
}
