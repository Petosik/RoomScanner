package com.pwr.piotr.androidproject.assistant;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * Created by piotr on 5/10/2018.
 */

public class EdgeDetector {
    private static final String TAG = "Timer";
    private Mat imgGray, blurImage, edge, dx, dy, hierarchy, result;
    private List<MatOfPoint> contoursList, filteredContoursList;
    private Size blurSize;
    private Scalar contursScalar, color;
    private Point offset;
    private Comparator<MatOfPoint> comparator;
    int iterations;

    public EdgeDetector() {
        imgGray = new Mat();
        blurImage = new Mat();
        edge = new Mat();
        dx = new Mat();
        dy = new Mat();
        hierarchy = new Mat();
        //result = new Mat(new Size(),CvType.CV_8UC3);
        contoursList = new ArrayList<MatOfPoint>();
        filteredContoursList = new ArrayList<MatOfPoint>();
        blurSize = new Size(3, 3);
        contursScalar = new Scalar(0, 255, 0);
        offset = new Point();
        color = new Scalar(255, 255, 255);
        comparator = new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint contour1, MatOfPoint contour2) {
                MatOfPoint2f contour1_2f = new MatOfPoint2f(contour1.toArray());
                MatOfPoint2f contour2_2f = new MatOfPoint2f(contour2.toArray());
                double contourArea1 = Imgproc.arcLength(contour1_2f, false);
                double contourArea2 = Imgproc.arcLength(contour2_2f, false);

                contour1_2f.release();
                contour2_2f.release();

                return Double.compare(contourArea2, contourArea1);
            }
        };
    }


    public Mat detectMat(Mat imageMat, int noise, int contours) {

        long start = System.nanoTime();

        contoursList.clear();

        Imgproc.cvtColor(imageMat, imgGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(imgGray, blurImage, blurSize);

        Imgproc.Scharr(blurImage, dx, CvType.CV_16S, 1, 0);
        Imgproc.Scharr(blurImage, dy, CvType.CV_16S, 0, 1);
        Imgproc.Canny(dx, dy, edge, noise, noise * 3);

        Imgproc.findContours(edge, contoursList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        result = new Mat(imgGray.rows(), imgGray.cols(), CvType.CV_8UC3);

        long beforeSort = System.nanoTime() - start;

        Collections.sort(contoursList, comparator);
        iterations = (int)(contoursList.size() * ((double) contours / 100));

        long afterSort = System.nanoTime() - start;
        Log.i(TAG, "Sorting: " + (afterSort - beforeSort));

        for (int i = 0; i < iterations; i++) {
            Imgproc.drawContours(result, contoursList, i, color, 2);
        }

        Log.i(TAG, "Drawing: " + (System.nanoTime() - start - afterSort));

        return result;
    }


    public Mat detectBitmap(Bitmap imageBitmap, int noise, int contours) {
        Mat matrix = new Mat();
        Bitmap bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, matrix);

        return detectMat(matrix, noise, contours);
    }

    public void release() {
        this.imgGray.release();
        this.blurImage.release();
        this.edge.release();
    }

}
