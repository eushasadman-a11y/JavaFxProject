package com.attendance.ai;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FaceTrainer {
    private final LBPHFaceRecognizer faceRecognizer;

    public FaceTrainer() {
        this.faceRecognizer = LBPHFaceRecognizer.create(1, 8, 8, 8, 80.0);
    }

    public void train(String dataPath, String savePath) {
        File root = new File(dataPath);
        if (!root.exists() || !root.isDirectory()) {
            System.err.println("Training data directory not found: " + dataPath);
            return;
        }
        List<Mat> faceImages = new ArrayList<>();
        List<Integer> faceLabels = new ArrayList<>();
        File[] studentDirs = root.listFiles(File::isDirectory);
        if (studentDirs == null || studentDirs.length == 0) {
            System.err.println("No student data found in dataset!");
            return;
        }

        for (File dir : studentDirs) {
            try {
                int label = Integer.parseInt(dir.getName());
                File[] files = dir.listFiles((d, name) -> 
                    name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png")
                );

                if (files != null) {
                    for (File f : files) {
                        Mat img = imread(f.getAbsolutePath(), IMREAD_GRAYSCALE);
                        if (img.empty()) {
                            continue;
                        }
                        resize(img, img, new Size(160, 160));
                        equalizeHist(img, img);

                        faceImages.add(img);
                        faceLabels.add(label);
                    }
                }
            } catch (NumberFormatException e) {
                System.err.println("Skipping non-numeric folder: " + dir.getName());
            }
        }

        if (!faceImages.isEmpty()) {
            System.out.println("Training started with " + faceImages.size() + " images...");
            
            try (MatVector images = new MatVector(faceImages.size());
                 Mat labels = new Mat(faceImages.size(), 1, CV_32SC1)) {

                for (int i = 0; i < faceImages.size(); i++) {
                    images.put(i, faceImages.get(i));
                    labels.ptr(i).putInt(faceLabels.get(i));
                }
                faceRecognizer.train(images, labels);
                
                File saveFile = new File(savePath);
                if (saveFile.getParentFile() != null && !saveFile.getParentFile().exists()) {
                    saveFile.getParentFile().mkdirs();
                }
                
                faceRecognizer.save(savePath); 
                System.out.println("AI Model saved successfully at: " + savePath);

            } finally {
                for (Mat m : faceImages) {
                    m.release();
                }
            }
        } else {
            System.err.println("Training failed: No valid images processed.");
        }
    }
}