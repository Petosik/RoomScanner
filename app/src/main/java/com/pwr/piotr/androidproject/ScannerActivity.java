package com.pwr.piotr.androidproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.graphics.Point;

import com.pwr.piotr.androidproject.assistant.EdgeDetector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;

public class ScannerActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "ScannerActivity";
    private static final int CAMERA_PEMISSION_CODE = 1;
    JavaCameraView javaCameraView;
    Mat mRgba, edge;
    Button captureButton;
    SeekBar scharSeekBar, contoursSeekBar;
    Display display;
    EdgeDetector edgeDetector;

    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PEMISSION_CODE);
                this.recreate();

            }
        }

        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setContentView(R.layout.activity_scanner);

        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

        captureButton = (Button) findViewById(R.id.button_capture);
        scharSeekBar = (SeekBar) findViewById(R.id.scharrSeekBar);
        contoursSeekBar = (SeekBar) findViewById(R.id.contoursSeekBar);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap bm = Bitmap.createBitmap(edge.cols(), edge.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(edge, bm);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                Intent getEditScreenIntent = new Intent(view.getContext(), EditorActivity.class);
                getEditScreenIntent.putExtra("capImg", byteArray);
                startActivity(getEditScreenIntent);
            }
        });


        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scharSeekBar.getLayoutParams().width = size.y - 400;
        scharSeekBar.setProgress(50);
        contoursSeekBar.getLayoutParams().width = size.y - 400;
        contoursSeekBar.setProgress(50);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (javaCameraView != null) {
            if (OpenCVLoader.initDebug()) {
                Log.i(TAG, "OpenCV successfully loaded");
                mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } else {
                Log.i(TAG, "OpenCV not loaded");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallBack);
            }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8SC4);
        edgeDetector = new EdgeDetector();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        edgeDetector.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        int edgeThreshScharr = scharSeekBar.getProgress();
        int contours = contoursSeekBar.getProgress();
        edge = edgeDetector.detectMat(mRgba, edgeThreshScharr, contours);

        return edge;
    }
}