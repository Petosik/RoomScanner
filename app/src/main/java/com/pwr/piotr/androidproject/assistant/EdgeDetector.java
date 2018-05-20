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
        contoursList.clear();

        Imgproc.cvtColor(imageMat, imgGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(imgGray, blurImage, blurSize);

        Imgproc.Scharr(blurImage, dx, CvType.CV_16S, 1, 0);
        Imgproc.Scharr(blurImage, dy, CvType.CV_16S, 0, 1);
        Imgproc.Canny(dx, dy, edge, noise, noise * 3);

        Imgproc.findContours(edge, contoursList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        result = new Mat(imgGray.rows(), imgGray.cols(), CvType.CV_8UC3);

        Collections.sort(contoursList, comparator);
        iterations = (int) (contoursList.size() * ((double) contours / 100));

        for (int i = 0; i < iterations; i++) {
            Imgproc.drawContours(result, contoursList, i, color, 2);
        }

        return result;
    }

    private Mat bitmapToMat(Bitmap bitmap) {
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);

        return mat;
    }

    public Mat detectBitmap(Bitmap imageBitmap, int noise, int contours) {
        Mat matrix = bitmapToMat(imageBitmap);
        return detectMat(matrix, noise, contours);
    }

    public MatOfPoint findContourBasedOnPoint(Point point, double precision, Mat imageMatrix) {
        contoursList.clear();
        Mat tmpMat = new Mat();
        Imgproc.cvtColor(imageMatrix, tmpMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.findContours(tmpMat, contoursList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contoursList) {
            for (Point contourPoint : contour.toArray()) {
                if (contourPoint.x >= point.x - precision && contourPoint.x < point.x + precision && contourPoint.y >= point.y - precision && contourPoint.y < point.y + precision) {
                    return contour;
                }
            }
        }

        return null;
    }

    public List<MatOfPoint> findContours(Mat mat) {
        contoursList.clear();
        Mat tmpMat = new Mat();
        Imgproc.cvtColor(mat, tmpMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.findContours(tmpMat, contoursList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        return contoursList;
    }

    public void release() {
        this.imgGray.release();
        this.blurImage.release();
        this.edge.release();
    }
}