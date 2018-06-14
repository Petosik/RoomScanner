package com.pwr.piotr.androidproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.pwr.piotr.androidproject.assistant.EdgeDetector;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageBrowserActivity extends AppCompatActivity {

    Display display;
    SeekBar scharSeekBar, contoursSeekBar;
    Button proceedButton;
    EdgeDetector edgeDetector;
    Bitmap originalImgBtm;

    ImageView imageView;

    private final static int RESULT_LOAD_IMAGE = 1;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_browser);

        final Intent imageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        imageIntent.setType("image/*");
        startActivityForResult(imageIntent, RESULT_LOAD_IMAGE);

        proceedButton = (Button) findViewById(R.id.proceedBtn);
        scharSeekBar = (SeekBar) findViewById(R.id.scharrSeekBarBrowsing);
        contoursSeekBar = (SeekBar) findViewById(R.id.contoursSeekBarBrowsing);
        imageView = (ImageView) findViewById(R.id.browserImgView);

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                Intent getEditScreenIntent = new Intent(view.getContext(), EditorActivity.class);
                getEditScreenIntent.putExtra("capImg", byteArray);
                startActivity(getEditScreenIntent);
            }
        });

        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (originalImgBtm != null) {
                    int edgeThreshScharr = scharSeekBar.getProgress();
                    int contours = contoursSeekBar.getProgress();

                    applyDetection(edgeThreshScharr, contours);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };

        scharSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        contoursSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            finish();
        } else {
            switch (requestCode) {
                case RESULT_LOAD_IMAGE:
                    Uri selectedImage = data.getData();
                    try {
                        display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        scharSeekBar.getLayoutParams().width = size.y - 400;
                        scharSeekBar.setProgress(100);
                        contoursSeekBar.getLayoutParams().width = size.y - 400;
                        contoursSeekBar.setProgress(50);

                        originalImgBtm = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        int edgeThreshScharr = scharSeekBar.getProgress();
                        int contours = contoursSeekBar.getProgress();

                        applyDetection(edgeThreshScharr, contours);
                    } catch (IOException e) {
                        Log.i("TAG", "Some exception " + e);
                    }
                    break;
            }
        }
    }

    private void applyDetection(int noise, int contours) {
        edgeDetector = new EdgeDetector();
        Mat result;

        result = edgeDetector.detectBitmap(originalImgBtm, noise, contours);
        Bitmap bmpResult = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
        ;

        Utils.matToBitmap(result, bmpResult);
        imageView.setImageBitmap(bmpResult);
    }
}