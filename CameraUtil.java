package com.attendance.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import static org.bytedeco.opencv.global.opencv_videoio.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class CameraUtil {
    private ScheduledExecutorService timer;
    private VideoCapture capture;
    private volatile boolean running = false;
    private static volatile Image latestFrame;

    public boolean startCamera(ImageView view, Consumer<Mat> frameConsumer) {
        if (running) return true;

        this.capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            this.capture = new VideoCapture(0, CAP_DSHOW);
            if (!capture.isOpened()) {
                System.err.println("Error: Camera could not be opened!");
                return false;
            }
        }

        running = true;
        timer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "camera-thread");
            t.setDaemon(true);
            return t;
        });

        timer.scheduleAtFixedRate(() -> {
            if (!running || capture == null || !capture.isOpened()) {
                return;
            }

            Mat frame = new Mat();
            try {
                if (capture.read(frame) && !frame.empty()) {
                    Image fxImage = ImageUtil.matToFxImage(frame);
                    latestFrame = fxImage;

                    Platform.runLater(() -> {
                        if (view != null && running) {
                            view.setImage(fxImage);
                        }
                    });

                    if (frameConsumer != null) {
                        frameConsumer.accept(frame);
                    }
                }
            } catch (Exception e) {
                System.err.println("Camera Frame Error: " + e.getMessage());
            } finally {
                if (frame != null) frame.release();
            }
        }, 0, 33, TimeUnit.MILLISECONDS);

        return true;
    }

    public void stopCamera() {
        running = false;

        if (timer != null) {
            timer.shutdownNow();
            try {
                timer.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            timer = null;
        }

        if (capture != null) {
            if (capture.isOpened()) capture.release();
            capture = null;
        }

        latestFrame = null;
        System.out.println("Camera released.");
    }

    public Mat grabFrame() {
        if (capture != null && capture.isOpened()) {
            Mat frame = new Mat();
            if (capture.read(frame) && !frame.empty()) return frame;
            frame.release();
        }
        return null;
    }

    public Image matToImage(Mat frame) {
        return (frame == null || frame.empty()) ? null : ImageUtil.matToFxImage(frame);
    }

    public static Image getLatestFrame() { return latestFrame; }

    public boolean isRunning() { return running; }
}
