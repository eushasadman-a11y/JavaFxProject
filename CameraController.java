package com.attendance.controller;

import com.attendance.MainApp;
import com.attendance.service.FaceRecognitionService;
import com.attendance.util.CameraUtil;
import com.attendance.util.ImageUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.bytedeco.opencv.opencv_core.Mat;

public class CameraController {
    @FXML private ImageView cameraView;
    @FXML private Label lblStatus; 

    private final CameraUtil cameraUtil = new CameraUtil();
    private FaceRecognitionService aiService;
    private volatile boolean isProcessing = false;

    @FXML
    public void initialize() {
        try {
            this.aiService = new FaceRecognitionService();
            setStatus("AI Service Initialized. Press Start Camera.", "green");
        } catch (Exception e) {
            setStatus("AI Service Init Failed: " + e.getMessage(), "red");
        }
    }

    @FXML
    private void onStart(ActionEvent event) {
        boolean started = cameraUtil.startCamera(cameraView, (Mat frame) -> {
            if (frame == null || frame.empty()) {
                return;
            }

            if (aiService != null && !isProcessing) {
                isProcessing = true;
                Mat frameClone = frame.clone();
                new Thread(() -> {
                    try {
                        var ids = aiService.recognizeMultiple(frameClone);
                        if (!ids.isEmpty()) {
                            Platform.runLater(() -> setStatus("Face(s) detected: " + ids, "#27ae60"));
                        }
                    } catch (Exception e) {
                        System.err.println("Recognition Thread Error: " + e.getMessage());
                    } finally {
                        frameClone.release();
                        isProcessing = false;
                    }
                }).start();
            }
        });

        if (started) {
            setStatus("Camera started. Detecting faces...", "#27ae60");
        } else {
            setStatus("Failed to open camera!", "red");
        }
    }

    @FXML
    private void onStop(ActionEvent event) {
        cameraUtil.stopCamera();
        if (cameraView != null) {
            cameraView.setImage(null);
        }
        setStatus("Camera stopped.", "#7f8c8d");
    }

    @FXML
    private void onTrain(ActionEvent event) {
        if (aiService != null) {
            setStatus("Training model, please wait...", "orange");
            new Thread(() -> {
                aiService.trainAll();
                Platform.runLater(() -> setStatus("Training complete!", "#27ae60"));
            }).start();
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        onStop(null);
        if (aiService != null) {
            aiService.shutdown();
        }
        MainApp.loadCenterView("/fxml/dashboard_content.fxml");
    }

    private void setStatus(String msg, String color) {
        Platform.runLater(() -> {
            if (lblStatus != null) {
                lblStatus.setText(msg);
                lblStatus.setStyle("-fx-text-fill: " + color + ";");
            }
        });
    }
}
