package com.mobilecomputing.sbarth.pedometer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static android.R.attr.permission;
import static android.R.attr.value;

public class MainActivity extends AppCompatActivity {

    private boolean collecting = false;

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startCollecting() {
        Intent intent = new Intent(this, CollectData.class);
        startService(intent);
    }

    public void stopCollecting() {
        Intent intent = new Intent(this, CollectData.class);
        stopService(intent);
    }

    public void onClick(View view) {
        Toast.makeText(this, "clicked!", Toast.LENGTH_SHORT).show();

        Button button = (Button) findViewById(R.id.button);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else {
            if (collecting) {
                stopCollecting();
                button.setText("Start Collecting");
                collecting = !collecting;
            } else {
                startCollecting();
                button.setText("Stop Collecting");
                collecting = !collecting;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCollecting();

                } else {
                    Log.e("Pedometer", "data collection cannot be started because required permissions were denied.");

                }

            }

        }
    }

}
