package com.attendance;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.attendance.config.DatabaseConfig;
import com.attendance.controller.ProfilesController;
import com.attendance.controller.AttendanceController;
import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {
    private static Stage primaryStage;
    private static BorderPane rootLayout;
    private static Object currentController;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        DatabaseConfig.initializeDatabase();
        primaryStage.setOnCloseRequest(event -> {
            stopCurrentCamera();
            Platform.exit();
            System.exit(0);
        });
        showLogin();
    }

    public static void showLogin() {
        try {
            URL xmlUrl = MainApp.class.getResource("/fxml/login.fxml");
            if (xmlUrl == null) { System.err.println("login.fxml not found!"); return; }
            Parent root = FXMLLoader.load(xmlUrl);
            Scene scene = new Scene(root, 450, 400);
            applyStyleSheet(scene);
            primaryStage.setTitle("Login - AI Attendance System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Login View Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showDashboard() {
        try {
            URL xmlUrl = MainApp.class.getResource("/fxml/dashboard.fxml");
            if (xmlUrl == null) { System.err.println("dashboard.fxml not found!"); return; }

            FXMLLoader loader = new FXMLLoader(xmlUrl);
            Parent root = loader.load();
            currentController = loader.getController();

            if (root instanceof BorderPane bp) {
                rootLayout = bp;
            } else {
                System.err.println("WARN: dashboard root is not a BorderPane!");
            }

            Scene scene = new Scene(root, 1200, 800);
            applyStyleSheet(scene);
            primaryStage.setTitle("AI Attendance System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(750);
            primaryStage.centerOnScreen();
            primaryStage.show();

            loadCenterView("/fxml/dashboard_content.fxml");

        } catch (IOException e) {
            System.err.println("Dashboard Load Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadCenterView(String fxmlPath) {
        try {
            stopCurrentCamera();

            if (rootLayout == null) {
                System.err.println("loadCenterView called before rootLayout is ready: " + fxmlPath);
                return;
            }

            URL xmlUrl = resolveUrl(fxmlPath);
            if (xmlUrl == null) { System.err.println("FXML not found: " + fxmlPath); return; }

            FXMLLoader loader = new FXMLLoader(xmlUrl);
            Parent view = loader.load();
            currentController = loader.getController();
            rootLayout.setCenter(view);

        } catch (IOException e) {
            System.err.println("View Load Error [" + fxmlPath + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static URL resolveUrl(String fxmlPath) {
        URL url = MainApp.class.getResource(fxmlPath);
        if (url == null) {
            String stripped = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
            url = Thread.currentThread().getContextClassLoader().getResource(stripped);
        }
        return url;
    }

    private static void stopCurrentCamera() {
        if (currentController instanceof ProfilesController pc) {
            pc.stopControllerCamera();
        } else if (currentController instanceof AttendanceController ac) {
            ac.stopScanner();
        }
    }

    private static void applyStyleSheet(Scene scene) {
        URL cssUrl = MainApp.class.getResource("/css/Style.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
    }

    public static void main(String[] args) { launch(args); }
}
