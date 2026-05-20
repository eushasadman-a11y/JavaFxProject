package com.attendance.controller;

import com.attendance.config.DatabaseConfig;
import com.attendance.model.Profile;
import com.attendance.util.CameraUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgcodecs;

import java.io.File;
import java.sql.*;

public class ProfilesController {

    @FXML private TableView<Profile> profileTable;
    @FXML private TableColumn<Profile, String> colStudentId, colFullName, colEmail, colDept, colRole, colPhone;
    @FXML private TextField pfStudentId, pfFullName, pfEmail, pfDept, pfPhone;
    @FXML private TextField searchProfile; 
    @FXML private ComboBox<String> pfRole;
    @FXML private ImageView imgProfilePreview;

    private final ObservableList<Profile> profileList = FXCollections.observableArrayList();
    private FilteredList<Profile> filteredList;
    private final CameraUtil cameraUtil = new CameraUtil();
    private Mat lastCapturedMat;
    private boolean cameraStarted = false;

    @FXML
    public void initialize() {
        if (pfRole != null) {
            pfRole.setItems(FXCollections.observableArrayList("Student", "Teacher", "Staff"));
        }

        setupTableColumns();

        filteredList = new FilteredList<>(profileList, p -> true);
        profileTable.setItems(filteredList);

        if (searchProfile != null) {
            searchProfile.textProperty().addListener((obs, oldVal, newVal) -> {
                String lower = newVal == null ? "" : newVal.toLowerCase().trim();
                filteredList.setPredicate(profile -> {
                    if (lower.isEmpty()) {
                        return true;
                    }
                    return profile.getStudentId().toLowerCase().contains(lower)
                        || profile.getFullName().toLowerCase().contains(lower)
                        || (profile.getDept() != null && profile.getDept().toLowerCase().contains(lower));
                });
            });
        }

        profileTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillFormFromProfile(newVal);
                stopControllerCamera();
                loadProfileImage(newVal.getStudentId());
            }
        });

        loadProfiles();
        startCameraStream();
    }

    private void startCameraStream() {
        if (cameraStarted) return;
        cameraStarted = true;
        cameraUtil.startCamera(imgProfilePreview, (Mat frame) -> {
            if (frame != null && !frame.empty()) {
                if (this.lastCapturedMat != null) this.lastCapturedMat.release();
                this.lastCapturedMat = frame.clone();
            }
        });
    }

    private void setupTableColumns() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDept.setCellValueFactory(new PropertyValueFactory<>("dept"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }

    private void fillFormFromProfile(Profile p) {
        pfStudentId.setText(p.getStudentId() != null ? p.getStudentId() : "");
        pfFullName.setText(p.getFullName() != null ? p.getFullName() : "");
        pfEmail.setText(p.getEmail() != null ? p.getEmail() : "");
        pfDept.setText(p.getDept() != null ? p.getDept() : "");
        pfPhone.setText(p.getPhone() != null ? p.getPhone() : "");
        pfRole.setValue(p.getRole());
    }

    @FXML
    private void onSaveProfile() {
        String studentId = pfStudentId.getText().trim();
        String name = pfFullName.getText().trim();
        String role = pfRole.getValue();

        if (studentId.isEmpty() || name.isEmpty() || role == null) {
            showAlert("Required fields (ID, Name, Role) are missing!");
            return;
        }

        File dir = new File("data/faces");
        if (!dir.exists()) dir.mkdirs();

        String photoPath = "data/faces/" + studentId + ".jpg";

        if (lastCapturedMat != null && !lastCapturedMat.empty()) {
            if (opencv_imgcodecs.imwrite(photoPath, lastCapturedMat)) {
                saveToDatabase(studentId, name, role, photoPath);
            } else {
                showAlert("Could not save image file!");
            }
        } else {
            saveToDatabase(studentId, name, role, null);
        }
    }

    private void saveToDatabase(String studentId, String name, String role, String photoPath) {
        // Check if update or insert
        String checkSql = "SELECT COUNT(*) FROM profiles WHERE student_id = ?";
        try (Connection conn = DatabaseConfig.getConnection()) {
            boolean exists;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, studentId);
                ResultSet rs = ps.executeQuery();
                exists = rs.next() && rs.getInt(1) > 0;
            }

            String query = exists
                ? "UPDATE profiles SET full_name=?, email=?, department=?, role=?, phone=?, photo_path=? WHERE student_id=?"
                : "INSERT INTO profiles (full_name, email, department, role, phone, photo_path, student_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, name);
                pstmt.setString(2, pfEmail.getText().trim());
                pstmt.setString(3, pfDept.getText().trim());
                pstmt.setString(4, role);
                pstmt.setString(5, pfPhone.getText().trim());
                pstmt.setString(6, photoPath);
                pstmt.setString(7, studentId);
                pstmt.executeUpdate();
            }

            Platform.runLater(() -> {
                loadProfiles();
                onClearProfile();
                showAlert(exists ? "Profile updated for " + name : "Profile saved for " + name);
            });

        } catch (SQLException e) {
            showAlert("Database Error: " + e.getMessage());
        }
    }

    private void loadProfileImage(String studentId) {
        File file = new File("data/faces/" + studentId + ".jpg");
        if (file.exists()) {
            imgProfilePreview.setImage(new Image(file.toURI().toString()));
        } else {
            imgProfilePreview.setImage(null);
        }
    }

    @FXML
    public void loadProfiles() {
        profileList.clear();
        String query = "SELECT * FROM profiles ORDER BY reg_date DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                profileList.add(new Profile(
                    rs.getString("student_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("department"),
                    rs.getString("role"),
                    rs.getString("phone")));
            }
        } catch (Exception e) {
            System.err.println("Table Load Error: " + e.getMessage());
        }
    }

    public void stopControllerCamera() {
        if (cameraUtil != null) cameraUtil.stopCamera();
        if (lastCapturedMat != null) { lastCapturedMat.release(); lastCapturedMat = null; }
        cameraStarted = false;
    }

    @FXML
    private void onClearProfile() {
        pfStudentId.clear(); pfFullName.clear(); pfEmail.clear();
        pfDept.clear(); pfPhone.clear();
        pfRole.setValue(null);
        imgProfilePreview.setImage(null);
        profileTable.getSelectionModel().clearSelection();
        if (searchProfile != null) searchProfile.clear();
        if (!cameraUtil.isRunning()) startCameraStream();
    }

    private void showAlert(String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }
}
