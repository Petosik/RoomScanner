package com.pwr.piotr.androidproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.pwr.piotr.androidproject.assistant.EdgeDetector;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EditorActivity extends AppCompatActivity {
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
    private ImageView imageView;
    EdgeDetector edgeDetector;
    Point touchPoint;
    List<MatOfPoint> chosenContours;
    List<MatOfPoint> whiteContoursToBeDisplayed;
    List<MatOfPoint> originalImageContours;
    Mat originalImageMat;
    final Scalar neonColorHex = new Scalar(57, 255, 20);
    final Scalar whiteColorHex = new Scalar(255, 255, 255);
    final Scalar blackColorHex = new Scalar(0, 0, 0);
    Mat resultImageMat;
    Button cuttingButton;
    Button resettingButton;
    Button savingButton;


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

        cuttingButton = (Button) findViewById(R.id.cutBtn);
        savingButton = (Button) findViewById(R.id.saveBtn);
        resettingButton = (Button) findViewById(R.id.resetBtn);
        resettingButton.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

        imageView = (ImageView) findViewById(R.id.editingView);
        imageView.setImageBitmap(imageBitmap);

        Bitmap bmp32 = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, originalImageMat);
        originalImageContours = edgeDetector.findContours(originalImageMat);
        List<Mat> whiteTmp = new ArrayList<Mat>();
        whiteContoursToBeDisplayed = new ArrayList<MatOfPoint>();
        cloneContours(originalImageContours, whiteContoursToBeDisplayed);

        resultImageMat = new Mat(originalImageMat.rows(), originalImageMat.cols(), CvType.CV_8UC3, blackColorHex);

        for (int i = 0; i < originalImageContours.size(); i++) {
            Imgproc.drawContours(resultImageMat, originalImageContours, i, whiteColorHex, 2);
        }

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
                List<MatOfPoint> allContoursToBeDisplayed = new ArrayList<MatOfPoint>(whiteContoursToBeDisplayed);
                allContoursToBeDisplayed.addAll(chosenContours);

                MatOfPoint chosenContour = edgeDetector.findContourBasedOnPoint(touchPoint, 20, allContoursToBeDisplayed);

                if (chosenContour != null) {
                    boolean isAlreadyChosen = edgeDetector.doesListContainContour(chosenContours, chosenContour);
                    if (isAlreadyChosen) {
                        whiteContoursToBeDisplayed.add(chosenContour);
                        chosenContours.remove(edgeDetector.getContourFromList(chosenContour, chosenContours));
                    } else {
                        chosenContours.add(chosenContour);
                        whiteContoursToBeDisplayed.remove(edgeDetector.getContourFromList(chosenContour, whiteContoursToBeDisplayed));
                    }
                    drawContoursOnMatch(isAlreadyChosen, chosenContour);
                }
            }
        });

        cuttingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!chosenContours.isEmpty()) {
                    whiteContoursToBeDisplayed = new ArrayList<MatOfPoint>(chosenContours);
                    chosenContours.clear();
                    drawContoursOnCut();
                    resettingButton.setVisibility(View.VISIBLE);
                    savingButton.setVisibility(View.VISIBLE);
                }
            }
        });

        resettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cloneContours(originalImageContours, whiteContoursToBeDisplayed);
                chosenContours.clear();
                drawContoursOnReset();
                resettingButton.setVisibility(View.INVISIBLE);
                savingButton.setVisibility(View.INVISIBLE);
            }
        });

        savingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmapToBeSaved = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                String filename = "edited_" + System.currentTimeMillis() + ".jpg";
                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File myDir = new File(root);
                myDir.mkdirs();

                File file = new File(myDir, filename);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmapToBeSaved.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("TAG", e.getMessage());
                }
            }
        });
    }

    private void drawContoursToImage() {
        Bitmap resultImageBitmap = Bitmap.createBitmap(resultImageMat.cols(), resultImageMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultImageMat, resultImageBitmap);
        imageView.setImageBitmap(resultImageBitmap);

        if (chosenContours.isEmpty()) {
            cuttingButton.setVisibility(View.GONE);
        } else {
            cuttingButton.setVisibility(View.VISIBLE);
        }
    }

    private void drawContoursOnReset() {
        resultImageMat.setTo(blackColorHex);
        for (int i = 0; i < whiteContoursToBeDisplayed.size(); i++) {
            Imgproc.drawContours(resultImageMat, whiteContoursToBeDisplayed, i, whiteColorHex, 2);
        }
        drawContoursToImage();
    }

    private void drawContoursOnCut() {
        resultImageMat.setTo(blackColorHex);
        for (int i = 0; i < whiteContoursToBeDisplayed.size(); i++) {
            Imgproc.drawContours(resultImageMat, whiteContoursToBeDisplayed, i, whiteColorHex, 2);
        }
        drawContoursToImage();
    }

    private void drawContoursOnMatch(boolean isAlreadyChosen, MatOfPoint chosenContour) {
        if (isAlreadyChosen) {
            List<MatOfPoint> chosenContoursTmp = new ArrayList<MatOfPoint>();
            chosenContoursTmp.add(chosenContour);
            for (int i = 0; i < chosenContoursTmp.size(); i++) {
                Imgproc.drawContours(resultImageMat, chosenContours, i, blackColorHex, 2);
                Imgproc.drawContours(resultImageMat, chosenContours, i, whiteColorHex, 2);
            }
        } else {
            for (int i = 0; i < chosenContours.size(); i++) {
                Imgproc.drawContours(resultImageMat, chosenContours, i, blackColorHex, 2);
                Imgproc.drawContours(resultImageMat, chosenContours, i, neonColorHex, 2);
            }
        }
        drawContoursToImage();
    }

    private void cloneContours(List<MatOfPoint> from, List<MatOfPoint> to) {
        for (Mat contour : from) {
            to.add(from.indexOf(contour), new MatOfPoint(contour.clone()));
        }
    }
}