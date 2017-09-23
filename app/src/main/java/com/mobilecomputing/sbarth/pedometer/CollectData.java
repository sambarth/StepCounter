package com.mobilecomputing.sbarth.pedometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.concurrent.LinkedBlockingQueue;


import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;


public class CollectData extends Service implements SensorEventListener {

    private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    private boolean running = false;
    private boolean threadStarted = false;
    private boolean listenersStarted = false;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyroscope;
    private File file;
    private boolean fileCreated = false;
    private FileWriter fw;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        running = true;

        // create a file in the public Documents directory where the csv will live
        if (!fileCreated) {
            if (isExternalStorageWritable()) {
                file = getDocumentStorageDir();
                fileCreated = true;
                try {
                    fw = new FileWriter(file);
                }
                catch (IOException e){
                    Log.e("Pedometer", e.getMessage());
                }
            }
            else {
                Log.e("Pedometer", "External storage not available");
            }

        }

        if (!threadStarted) {
            threadStarted = true;
            new Thread(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    while (running) {
                        writeLine();
                    }
                }
            }).start();
        }

        Toast.makeText(this, "started thread", Toast.LENGTH_SHORT).show();

        if (!listenersStarted) {

            senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

            senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);

            senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            senSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_FASTEST);

            listenersStarted = true;
        }

        Toast.makeText(this, "listeners setup", Toast.LENGTH_SHORT).show();

        return super.onStartCommand(intent,flags,startId);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                getAccelerometer(event);
                break;
            case Sensor.TYPE_GYROSCOPE:
                getGyroscope(event);
                break;
            default:
                break;

        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        running = false;
        try {
            fw.close();
        }
        catch (IOException e) {
            Log.e("Pedometer", e.getMessage());
        }
        scanFile(this, file, null);
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
    }

    private void getGyroscope(SensorEvent event) {
        String type = "G";
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        long timestamp = event.timestamp;

        String data = "";
        data += timestamp + "," + type + "," + x + "," + y + "," + z + '\n';

        try  {
            queue.put(data);
        } catch (Exception e) {
            Log.e("Pedometer", "gyroscope add to queue failed", e);
        }
    }

    private void getAccelerometer(SensorEvent event) {
        String type = "A";
        float[] values = event.values;
        float x = values[0];
        float y = values[1];
        float z = values[2];
        long timestamp = event.timestamp;

        String data = "";
        data += timestamp + "," + type + "," + x + "," + y + "," + z + '\n';

        try  {
            queue.put(data);
        } catch (Exception e) {
            Log.e("Pedometer", "accelerometer add to queue failed", e);
        }

    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getDocumentStorageDir() {
        file = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PedometerAppData.csv");
        return file;

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void writeLine() {
        String data = "";
        // take the accelerometer/gyroscope data off the queue
        try {
            data = queue.take();
        } catch (Exception e) {
            Log.e("Pedometer", e.getMessage());
        }
        try {
            fw.write(data);
        } catch (IOException e) {
            Log.e(file.getAbsolutePath(), e.getMessage());
        }

    }

    public void scanFile(Context ctxt, File f, String mimeType) {
        MediaScannerConnection.scanFile(ctxt, new String[] {f.getAbsolutePath()}, new String[] {mimeType}, null);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
