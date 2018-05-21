package com.pwr.piotr.androidproject;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends AppCompatActivity {
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;
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
        whiteContoursToBeDisplayed = edgeDetector.findContours(originalImageMat);
        resultImageMat = new Mat(originalImageMat.rows(), originalImageMat.cols(), CvType.CV_8UC3, blackColorHex);

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
                    drawContours();
                }
            }
        });

        cuttingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!chosenContours.isEmpty()) {
                    whiteContoursToBeDisplayed = new ArrayList<MatOfPoint>(chosenContours);
                    chosenContours.clear();
                    drawContours();
                    resettingButton.setVisibility(View.VISIBLE);
                    savingButton.setVisibility(View.VISIBLE);
                }
            }
        });

        resettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteContoursToBeDisplayed = edgeDetector.findContours(originalImageMat);
                chosenContours.clear();
                drawContours();
                resettingButton.setVisibility(View.INVISIBLE);
                savingButton.setVisibility(View.INVISIBLE);
            }
        });

        savingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    } else {
                        ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.CAMERA}, WRITE_EXTERNAL_STORAGE_CODE);
                        ((Activity) getApplicationContext()).recreate();
                    }
                }


                Bitmap bitmapToBeSaved = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                FileOutputStream out = null;
                String filename = "edited_" + System.currentTimeMillis() + ".jpeg";

                File sd = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                File dest = new File(sd, filename);

                try {
                    out = new FileOutputStream(dest);
                    bitmapToBeSaved.compress(Bitmap.CompressFormat.JPEG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("TAG", e.getMessage());
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                            Log.d("TAG", "OK!!");
                        }
                    } catch (IOException e) {
                        Log.d("TAG", e.getMessage() + "Error");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void drawContours() {
        resultImageMat.setTo(blackColorHex);
        for (int i = 0; i < whiteContoursToBeDisplayed.size(); i++) {
            Imgproc.drawContours(resultImageMat, whiteContoursToBeDisplayed, i, whiteColorHex, 2);
        }
        for (int i = 0; i < chosenContours.size(); i++) {
            Imgproc.drawContours(resultImageMat, chosenContours, i, neonColorHex, 2);
        }
        Bitmap resultImageBitmap = Bitmap.createBitmap(resultImageMat.cols(), resultImageMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultImageMat, resultImageBitmap);
        imageView.setImageBitmap(resultImageBitmap);

        if (chosenContours.isEmpty()) {
            cuttingButton.setVisibility(View.GONE);
        } else {
            cuttingButton.setVisibility(View.VISIBLE);
        }
    }
}