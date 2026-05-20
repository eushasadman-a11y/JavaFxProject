package com.attendance.controller;

import com.attendance.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.sql.*;
import com.attendance.config.DatabaseConfig;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }

    @FXML
    private void handleLogin() {
        String user = (usernameField.getText() != null) ? usernameField.getText().trim() : "";
        String pass = (passwordField.getText() != null) ? passwordField.getText() : "";

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both fields!");
            return;
        }

        if (authenticate(user, pass)) {
            System.out.println("Login Successful: " + user);
            MainApp.showDashboard(); 
        } else {
            errorLabel.setText("Invalid username or password!");
            passwordField.clear();
        }
    }

    private boolean authenticate(String user, String pass) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); 
            }
        } catch (SQLException e) {
            System.err.println("Auth Error: " + e.getMessage());
            return false;
        }
    }
}