package com.pwr.piotr.androidproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;


public class EditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        byte[] byteArray = getIntent().getByteArrayExtra("capImg");
        Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        ImageView iv = (ImageView) findViewById(R.id.editingView);
        iv.setImageBitmap(bm);
    }
}
