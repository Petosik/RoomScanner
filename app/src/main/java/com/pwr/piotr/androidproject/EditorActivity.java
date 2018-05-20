package com.pwr.piotr.androidproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.pwr.piotr.androidproject.assistant.EdgeDetector;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class EditorActivity extends AppCompatActivity {
    private ImageView imageView;
    EdgeDetector edgeDetector;
    Point touchPoint;
    List<MatOfPoint> chosenContours;
    List<MatOfPoint> whiteContoursToBeDisplayed;
    Mat originalImageMat;
    final Scalar neonColorHex = new Scalar(57, 255, 20);
    final Scalar whiteColorHex = new Scalar(255, 255, 255);
    final Scalar blackColorHex = new Scalar(0, 0, 0);
    Mat resultImageMat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        chosenContours = new ArrayList<MatOfPoint>();
        edgeDetector = new EdgeDetector();
        originalImageMat = new Mat();
        resultImageMat = new Mat();

        byte[] byteArray = getIntent().getByteArrayExtra("capImg");
        final Bitmap imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        Bitmap bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, originalImageMat);
        //resultImageMat = originalImageMat.clone();

        final ImageView imageView = (ImageView) findViewById(R.id.editingView);
        imageView.setImageBitmap(imageBitmap);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    touchPoint = new Point(event.getX(), event.getY());
                }
                return false;
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MatOfPoint chosenContour = edgeDetector.findContourBasedOnPoint(touchPoint, 20, originalImageMat);
                whiteContoursToBeDisplayed = edgeDetector.findContours(originalImageMat);

                if (chosenContour != null) {
                    boolean isAlreadyChosen = false;
                    for (MatOfPoint contour : chosenContours) {
                        if (contour.toArray().length == chosenContour.toArray().length) {
                            boolean isPointTheSame = false;
                            for (int i = 0; i < contour.toArray().length; i++) {
                                isPointTheSame = contour.toArray()[i].x == chosenContour.toArray()[i].x && contour.toArray()[i].y == chosenContour.toArray()[i].y;
                                if (!isPointTheSame)
                                    break;
                            }
                            isAlreadyChosen = isPointTheSame;
                        }
                    }

                    if (isAlreadyChosen) {
                        int index = chosenContours.indexOf(chosenContour);
                        // TODO: whiteContoursToBeDisplayed musi byc
                        for (int i = index - 1; i < index; i++) {
                            Imgproc.drawContours(resultImageMat, chosenContours, i, whiteColorHex, 2);
                        }
                        chosenContours.remove(chosenContour); // TODO: Nie usuwa sie bo to inny obiekt
                    } else {
                        chosenContours.add(chosenContour);
                        whiteContoursToBeDisplayed.remove(chosenContour); // TODO: Puscic display z whiteContoursToBeDisplayed, usunac biale zastapic zielonym
                                                                            // TODO: Też się nie usuwa bo inny obiekt
                        for (int i = chosenContours.size() - 1; i < chosenContours.size(); i++) {
                            Imgproc.drawContours(resultImageMat, chosenContours, i, whiteColorHex, 2); // TODO:
                        }

                        // TODO: DOKONCZYYYC!!!!!!!
                        Utils.matToBitmap(resultImageMat, imageBitmap);
                        imageView.setImageBitmap(imageBitmap);
                    }
                }
            }
        });
    }
}