package com.pwr.piotr.androidproject.assistant;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piotr on 5/10/2018.
 */

public class EdgeDetector {
    private Mat imgGray, blurImage, edge, dx, dy, hierarchy, result;
    private List<MatOfPoint> contoursList;
    private Size blurSize;
    private Scalar contursScalar, color;
    private Point offset;

    public EdgeDetector() {
        imgGray = new Mat();
        blurImage = new Mat();
        edge = new Mat();
        dx = new Mat();
        dy = new Mat();
        hierarchy = new Mat();
        contoursList = new ArrayList<MatOfPoint>();
        blurSize = new Size(3, 3);
        contursScalar = new Scalar(0, 255, 0);
        offset = new Point();
        color = new Scalar(255, 255, 255);
    }


    public Mat detectMat(Mat imageMat, int noise, int contours) {

        Imgproc.cvtColor(imageMat, imgGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(imgGray, blurImage, blurSize);

        Imgproc.Scharr(blurImage, dx, CvType.CV_16S, 1, 0);
        Imgproc.Scharr(blurImage, dy, CvType.CV_16S, 0, 1);
        Imgproc.Canny(dx, dy, edge, noise, noise * 3);

        Imgproc.findContours(edge, contoursList, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        result = new Mat(imgGray.rows(),imgGray.cols(),CvType.CV_8UC3);

        for (int i = 0; i < contoursList.size(); i++) {
            Imgproc.drawContours(result, contoursList, i, color, 2, 8, hierarchy, 0, offset);
        }
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
