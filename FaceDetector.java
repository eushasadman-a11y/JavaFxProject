package com.attendance.ai;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import static org.bytedeco.opencv.global.opencv_objdetect.*;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FaceDetector {
    private CascadeClassifier faceCascade;

    public FaceDetector() {
        try {
            InputStream is = getClass().getResourceAsStream("/data/haarcascade_frontalface_default.xml");
            if (is == null) {
                is = getClass().getResourceAsStream("/haarcascade_frontalface_default.xml");
            }

            if (is == null) {
                System.err.println("Error: haarcascade file not found!");
                return;
            }

            File tempFile = File.createTempFile("haarcascade", ".xml");
            tempFile.deleteOnExit();
            Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            this.faceCascade = new CascadeClassifier(tempFile.getAbsolutePath());
            System.out.println("FaceDetector: XML Loaded Successfully.");
        } catch (Exception e) {
            System.err.println("FaceDetector Error: " + e.getMessage());
        }
    }

    public Rect detectLargestFace(Mat grayFrame) {
        if (faceCascade == null || grayFrame == null || grayFrame.empty()) {
            return null;
        }

        RectVector faces = new RectVector();
        faceCascade.detectMultiScale(grayFrame, faces);
        
        Rect largestFace = null;
        long maxArea = 0;
        
        for (long i = 0; i < faces.size(); i++) {
            Rect rect = faces.get(i);
            long area = (long) rect.width() * rect.height();
            if (area > maxArea) {
                maxArea = area;
                largestFace = rect;
            }
        }
        return largestFace;
    }
}