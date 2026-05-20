package com.attendance.util;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.opencv_objdetect.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FaceRecognitionUtil {
    private CascadeClassifier faceCascade;
    private LBPHFaceRecognizer faceRecognizer;
    private static final String MODEL_PATH = "data/trained_model.xml";

    public FaceRecognitionUtil() {
        try {
            InputStream is = getClass().getResourceAsStream("/com/attendance/data/haarcascade_frontalface_default.xml");
            if (is == null) {
                throw new RuntimeException("Cascade XML not found in resources!");
            }
            
            File tempFile = File.createTempFile("haarcascade", ".xml");
            tempFile.deleteOnExit(); 
            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.faceCascade = new CascadeClassifier(tempFile.getAbsolutePath());

            this.faceRecognizer = LBPHFaceRecognizer.create();
            
            File modelFile = new File(MODEL_PATH);
            if (modelFile.exists()) {
                this.faceRecognizer.read(MODEL_PATH);
                System.out.println("AI Model Loaded: " + MODEL_PATH);
            } else {
                System.out.println("No trained model found. System will identify all as Unknown.");
            }

        } catch (Exception e) {
            System.err.println("FaceRecognitionUtil Init Error: " + e.getMessage());
        }
    }

    public String identifyFace(Mat frame) {
        if (faceCascade == null || frame == null || frame.empty()) {
            return "Unknown";
        }

        try (Mat grayFrame = new Mat()) {
            cvtColor(frame, grayFrame, COLOR_BGR2GRAY);
            equalizeHist(grayFrame, grayFrame);

            try (RectVector faces = new RectVector()) {
                faceCascade.detectMultiScale(grayFrame, faces);

                if (faces.size() > 0) {
                    Rect faceRect = faces.get(0);
                    
                    try (Mat faceROI = new Mat(grayFrame, faceRect);
                         Mat resizedFace = new Mat()) {
                        
                        resize(faceROI, resizedFace, new Size(100, 100)); 

                        int[] label = {-1};
                        double[] confidence = {0.0};
                        
                        faceRecognizer.predict(resizedFace, label, confidence);

                        if (label[0] != -1 && confidence[0] < 75) {
                            return String.valueOf(label[0]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Identification Error: " + e.getMessage());
        }
        return "Unknown";
    }
}