package com.attendance.service;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import java.io.*;
import java.util.*;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

public final class FaceRecognitionService {

    private static final String BASE_PATH  = System.getProperty("user.home") + "/.attendance_ai/";
    private static final String MODEL_PATH = BASE_PATH + "model/trained_model.xml";
    private static final String FACES_PATH = "data/faces/";

    private final Map<String, Integer> studentIdToLabel = new LinkedHashMap<>();
    private final Map<Integer, String> labelToStudentId = new HashMap<>();

    private final List<Mat>     images = new ArrayList<>();
    private final List<Integer> labels = new ArrayList<>();

    private final LBPHFaceRecognizer recognizer;
    private final CascadeClassifier  faceCascade;
    private boolean modelIsTrained = false;

    public FaceRecognitionService() {
        this.recognizer = LBPHFaceRecognizer.create(1, 8, 8, 8, 75);
        this.faceCascade = loadCascade();
        new File(BASE_PATH + "model").mkdirs();
        loadDataset();
        if (!images.isEmpty()) {
            trainAll();
        } else {
            loadModel();
        }
    }

    private CascadeClassifier loadCascade() {
        String[] resourcePaths = {
            "/data/haarcascade_frontalface_default.xml",
            "/haarcascade/haarcascade_frontalface_default.xml",
            "/haarcascade_frontalface_default.xml",
            "haarcascade_frontalface_default.xml",
            "data/haarcascade_frontalface_default.xml"
        };
        try {
            InputStream is = null;
            for (String path : resourcePaths) {
                is = getClass().getResourceAsStream(path);
                if (is != null) { System.out.println("AI: Cascade found: " + path); break; }
            }
            if (is == null) {
                System.err.println("CRITICAL: haarcascade_frontalface_default.xml not found!");
                return null;
            }
            File tempFile = File.createTempFile("face_detector", ".xml");
            tempFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buf = new byte[8192]; int n;
                while ((n = is.read(buf)) != -1) out.write(buf, 0, n);
            }
            CascadeClassifier cls = new CascadeClassifier(tempFile.getAbsolutePath());
            if (cls.empty()) { System.err.println("Classifier empty!"); return null; }
            System.out.println("AI: Face Detector ready.");
            return cls;
        } catch (Exception e) {
            System.err.println("AI Cascade Init Failed: " + e.getMessage());
            return null;
        }
    }

    public List<Mat> detectFaces(Mat frame) {
        List<Mat> faceList = new ArrayList<>();
        if (frame == null || frame.empty() || faceCascade == null) return faceList;
        Mat gray = new Mat();
        try {
            cvtColor(frame, gray, COLOR_BGR2GRAY);
            equalizeHist(gray, gray);
            RectVector faces = new RectVector();
            faceCascade.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(30, 30), new Size(500, 500));
            for (int i = 0; i < faces.size(); i++) {
                Rect rect = faces.get(i);
                Mat face = new Mat(gray, rect);
                Mat resized = new Mat();
                resize(face, resized, new Size(160, 160));
                faceList.add(resized);
                face.release();
            }
        } catch (Exception e) {
            System.err.println("Detection Error: " + e.getMessage());
        } finally {
            gray.release();
        }
        return faceList;
    }

    public void trainAll() {
        if (images.isEmpty()) { System.out.println("AI: No images to train."); return; }
        try (MatVector imagesVector = new MatVector(images.size());
             Mat labelsMat = new Mat(images.size(), 1, CV_32SC1)) {
            for (int i = 0; i < images.size(); i++) {
                imagesVector.put(i, images.get(i));
                labelsMat.ptr(i).putInt(labels.get(i));
            }
            recognizer.train(imagesVector, labelsMat);
            modelIsTrained = true;
            saveModel();
            System.out.println("AI: Model trained with " + images.size() + " image(s).");
        } catch (Exception e) {
            System.err.println("Training Error: " + e.getMessage());
        }
    }

    public List<Integer> recognizeMultiple(Mat frame) {
        Set<Integer> uniqueResults = new HashSet<>();
        if (!modelIsTrained) return new ArrayList<>(uniqueResults);
        List<Mat> faces = detectFaces(frame);
        for (Mat face : faces) {
            int[] label = {0};
            double[] confidence = {0.0};
            try {
                recognizer.predict(face, label, confidence);
                if (label[0] != -1 && confidence[0] < 70) {
                    uniqueResults.add(label[0]);
                } else {
                    uniqueResults.add(-1);
                }
            } catch (Exception e) {
                System.err.println("Predict Error: " + e.getMessage());
            } finally {
                face.release();
            }
        }
        return new ArrayList<>(uniqueResults);
    }

    public String getLabelToStudentId(int label) {
        return labelToStudentId.get(label);
    }

    private void saveModel() {
        try { recognizer.save(MODEL_PATH); System.out.println("AI: Model saved."); }
        catch (Exception e) { System.err.println("Model Save Error: " + e.getMessage()); }
    }

    private void loadModel() {
        File f = new File(MODEL_PATH);
        if (f.exists() && f.length() > 0) {
            try { recognizer.read(MODEL_PATH); modelIsTrained = true; System.out.println("AI: Model loaded."); }
            catch (Exception e) { System.err.println("Model Load Error: " + e.getMessage()); }
        }
    }

    private void loadDataset() {
        File facesDir = new File(FACES_PATH);
        if (!facesDir.exists()) { System.out.println("AI: data/faces/ not found."); return; }

        File[] files = facesDir.listFiles((d, n) -> n.endsWith(".jpg") || n.endsWith(".png"));
        if (files == null || files.length == 0) { System.out.println("AI: No face images found."); return; }

        int nextLabel = 1;
        for (File f : files) {
            try {
                String nameNoExt = f.getName().replaceAll("\\.[^.]+$", "");
                int label;
                if (studentIdToLabel.containsKey(nameNoExt)) {
                    label = studentIdToLabel.get(nameNoExt);
                } else {
                    label = nextLabel++;
                    studentIdToLabel.put(nameNoExt, label);
                    labelToStudentId.put(label, nameNoExt);
                }

                Mat img = imread(f.getAbsolutePath(), IMREAD_GRAYSCALE);
                if (!img.empty()) {
                    Mat resized = new Mat();
                    resize(img, resized, new Size(160, 160));
                    images.add(resized);
                    labels.add(label);
                    img.release();
                    System.out.println("AI: Loaded face for id=" + nameNoExt + " label=" + label);
                }
            } catch (Exception e) {
                System.err.println("Dataset Load Error (" + f.getName() + "): " + e.getMessage());
            }
        }
        System.out.println("AI: Dataset loaded with " + images.size() + " face(s).");
    }

    public void shutdown() {
        images.forEach(Mat::release);
        images.clear();
        if (faceCascade != null) faceCascade.close();
    }
}
