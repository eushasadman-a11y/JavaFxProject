package com.attendance.ai;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class FaceRecognizer {
    private final FaceDetector detector;
    private final LBPHFaceRecognizer recognizer;
    private volatile boolean isTrained = false;

    private static final String DATASET_PATH = System.getProperty("user.home") + File.separator + ".attendance_ai" + File.separator + "dataset" + File.separator;

    public FaceRecognizer() {
        this.detector = new FaceDetector();
        this.recognizer = LBPHFaceRecognizer.create(1, 8, 8, 8, 80);
        trainModel();
    }

    public synchronized void trainModel() {
        File root = new File(DATASET_PATH);
        if (!root.exists()) {
            root.mkdirs();
            return;
        }

        try (MatVector images = new MatVector()) {
            List<Integer> labelsList = new ArrayList<>();
            File[] studentDirs = root.listFiles();
            
            if (studentDirs == null) {
                return;
            }

            for (File dir : studentDirs) {
                if (!dir.isDirectory()) {
                    continue;
                }
                
                int label = Integer.parseInt(dir.getName());
                File[] files = dir.listFiles();
                if (files == null) {
                    continue;
                }

                for (File f : files) {
                    if (f.getName().endsWith(".png") || f.getName().endsWith(".jpg")) {
                        Mat img = opencv_imgcodecs.imread(f.getAbsolutePath(), opencv_imgcodecs.IMREAD_GRAYSCALE);
                        if (img != null && !img.empty()) {
                            resize(img, img, new Size(100, 100));
                            images.push_back(img);
                            labelsList.add(label);
                        }
                    }
                }
            }

            if (images.size() > 0) {
                try (Mat labelsMat = new Mat(labelsList.size(), 1, CV_32SC1)) {
                    for (int i = 0; i < labelsList.size(); i++) {
                        labelsMat.ptr(i).putInt(labelsList.get(i));
                    }
                    recognizer.train(images, labelsMat);
                    isTrained = true;
                    System.out.println("AI Engine Trained with " + images.size() + " images.");
                }
            }
        } catch (Exception e) {
            System.err.println("Training Error: " + e.getMessage());
        }
    }

    public synchronized String recognizeFace(Mat frame) {
        if (!isTrained || frame == null || frame.empty()) {
            return "Unknown";
        }

        try (Mat gray = new Mat()) {
            cvtColor(frame, gray, COLOR_BGR2GRAY);
            equalizeHist(gray, gray);

            Rect faceRect = detector.detectLargestFace(gray);
            if (faceRect != null) {
                try (Mat faceROI = new Mat(gray, faceRect)) {
                    resize(faceROI, faceROI, new Size(100, 100));
                    
                    int[] label = {-1};
                    double[] conf = {0.0};
                    
                    recognizer.predict(faceROI, label, conf);
                    if (label[0] != -1 && conf[0] < 70) {
                        return String.valueOf(label[0]);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Recognition Error: " + e.getMessage());
        }
        return "Unknown";
    }
}