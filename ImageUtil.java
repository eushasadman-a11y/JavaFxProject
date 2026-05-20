package com.attendance.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC4;

public class ImageUtil {

    public static Image matToFxImage(Mat mat) {
        if (mat == null || mat.empty()) {
            return null;
        }
        
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();
        
        WritableImage wx = new WritableImage(width, height);
        PixelWriter pw = wx.getPixelWriter();

        try (UByteIndexer indexer = mat.createIndexer()) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (channels == 3) {
                        int b = indexer.get(y, x, 0) & 0xFF;
                        int g = indexer.get(y, x, 1) & 0xFF;
                        int r = indexer.get(y, x, 2) & 0xFF;
                        pw.setArgb(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
                    } 
                    else if (channels == 4) { 
                        int b = indexer.get(y, x, 0) & 0xFF;
                        int g = indexer.get(y, x, 1) & 0xFF;
                        int r = indexer.get(y, x, 2) & 0xFF;
                        int a = indexer.get(y, x, 3) & 0xFF;
                        pw.setArgb(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                    }
                    else if (channels == 1) {
                        int gray = indexer.get(y, x) & 0xFF;
                        pw.setArgb(x, y, (0xFF << 24) | (gray << 16) | (gray << 8) | gray);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("matToFxImage Error: " + e.getMessage());
        }
        
        return wx;
    }

    public static Mat fxImageToMat(Image img) {
        if (img == null) {
            return null;
        }
        
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();

        Mat mat = new Mat(height, width, CV_8UC4);
        PixelReader pr = img.getPixelReader();
        
        try (UByteIndexer indexer = mat.createIndexer()) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = pr.getArgb(x, y);

                    indexer.put(y, x, 0, (argb >> 0) & 0xFF);  
                    indexer.put(y, x, 1, (argb >> 8) & 0xFF);  
                    indexer.put(y, x, 2, (argb >> 16) & 0xFF); 
                    indexer.put(y, x, 3, (argb >> 24) & 0xFF); 
                }
            }
        } catch (Exception e) {
            System.err.println("fxImageToMat Error: " + e.getMessage());
        }
        
        return mat;
    }
}