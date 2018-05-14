package com.pwr.piotr.androidproject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackActivity extends Activity {

    Button feedbackSendButton;
    TextView feedbackMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackSendButton = (Button) findViewById(R.id.feedbackSendBtn);
        feedbackMessage = (TextView) findViewById(R.id.feedbackMessage);

        feedbackSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for version " + String.valueOf(getCurrentAppVersionCode()));
                emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackMessage.getText().toString());
                emailIntent.setData(Uri.parse("mailto:feedbackroomscanner@o2.pl"));
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(FeedbackActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int getCurrentAppVersionCode() {
        int versionCode = -1;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }
}
