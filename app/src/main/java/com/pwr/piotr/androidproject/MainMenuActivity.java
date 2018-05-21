package com.pwr.piotr.androidproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

    public class MainMenuActivity extends AppCompatActivity {
        Button scannerButton;
        Button browseButton;
        Button modelsButton;
        Button feedbackButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        scannerButton = (Button) findViewById(R.id.scanerBtn);
        browseButton = (Button) findViewById(R.id.browserBtn);
        modelsButton = (Button) findViewById(R.id.modelBtn);
        feedbackButton = (Button) findViewById(R.id.feedbackBtn);

        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getScannerScreenIntent = new Intent(v.getContext(),ScannerActivity.class);
                startActivity(getScannerScreenIntent);
            }
        });

        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getImageBrowseScreenIntent = new Intent(v.getContext(),ImageBrowserActivity.class);
                startActivity(getImageBrowseScreenIntent);
            }
        });

        modelsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getModelsScreenIntent = new Intent(v.getContext(), ModelActivity.class);
                startActivity(getModelsScreenIntent);
            }
        });

        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getFeedbackScreenIntent = new Intent(v.getContext(),FeedbackActivity.class);
                startActivity(getFeedbackScreenIntent);
            }
        });
    }
}