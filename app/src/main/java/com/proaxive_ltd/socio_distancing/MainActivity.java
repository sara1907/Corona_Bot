package com.proaxive_ltd.socio_distancing;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static Net net;
    private static Mat frame;
    private static JavaCameraView javaCameraView;
    private static boolean isDistanceFromYou = true;
    private static boolean mute = false;
    public static final int REQUEST_PERMISSION = 300;
    private AdView adview;
    // request code for permission requests to the os for image
    //public static final int REQUEST_IMAGE = 100;
    private static final String[] classNames = {"background",
            "aeroplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair",
            "cow", "diningtable", "dog", "horse",
            "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"};
    MediaPlayer mp = null;
    music_play mup;
    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(MainActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView(); //to display on camera visuals on screen
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                }

            }
            super.onManagerConnected(status);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      /*  if (OpenCVLoader.initDebug()) {
           // Log.d("Declaration", "OpenCv is Working");
        } else {
            Log.d("Declaration", "OpenCv is not Working");
        }*/
        javaCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);
        javaCameraView.setVisibility(SurfaceView.VISIBLE); //setting visibility
        //adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adview = (AdView)findViewById(R.id.main_act_banner);
        AdRequest adRequest = new AdRequest.Builder().build();
       // adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adview.loadAd(adRequest);
        //javaCameraView.setMaxFrameSize(480, 720);
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }

        // request permission to write data (aka images) to the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }

        // request permission to read data (aka images) from the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        }
        javaCameraView.setCvCameraViewListener(MainActivity.this); //sets the camera listener
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mup = new music_play();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menucont, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.about_app) {
            Intent i = new Intent(getApplicationContext(), About_App.class);
            startActivity(i);
        }
        else if ( id == R.id.from_you)
        {
            isDistanceFromYou = true;
        }
        else if( id == R.id.between_person)
        {
            isDistanceFromYou = false;
        }
        else if(id == R.id.volume_up){
            View it = (View)findViewById(R.id.volume_up);
            it.setVisibility(View.INVISIBLE);
            mute = true;
            View it1 = (View)findViewById(R.id.volume_down);
            it1.setVisibility(View.VISIBLE);

        }
        else if(id == R.id.volume_down){
            View it = (View)findViewById(R.id.volume_down);
            it.setVisibility(View.INVISIBLE);
            mute = false;
            View it1 = (View)findViewById(R.id.volume_up);
            it1.setVisibility(View.VISIBLE);
        }

        return true;
    }

    public float getFocalLength() {
        CameraManager cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        float[] foc = {};
        try {
            String[] cameraIds = cm.getCameraIdList();
            foc = new float[cameraIds.length];
            for (String str : cameraIds) {
               // Log.i("all_the_camera", str);
                CameraCharacteristics cc = cm.getCameraCharacteristics(str);
                foc = cc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
               /* for (float f : foc)
                    Log.i("all_the_camera_focal", String.valueOf(f));*/
                break;
            }

        } catch (Exception ob) {
        }
        return foc[0];
    }

    public float getDistanceFromCamera(float focal_length, float width, float pixel_width) {
        float dis = 2 * (focal_length * width) / pixel_width;
        return dis / 10;
    }

    public double getDistanceFromRectangles(int left1, int right1, int left2, int right2, int top1, int top2) {
        double dist = 0.0;
        dist = Math.sqrt((right1 - left2) * (right1 - left2) + (top2 - top1) * (top2 - top1));
        return 0.0002645833 * (dist);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        //String proto = getPackageResourcePath(R.raw.);
        //String proto = "src/main/assets/MobileNetSSDdeploy.prototxt.txt";
        String proto = getPath("MobileNetSSDdeploy.prototxt.txt", this);
        // String caffemodel = "src/main/assets/MobileNetSSDdeploy.caffemodel";
        String caffemodel = getPath("MobileNetSSDdeploy.caffemodel", this);
        net = Dnn.readNetFromCaffe(proto, caffemodel);
        frame = new Mat(height, width, CvType.CV_8UC4); //creates a image contantiner of rgba scheme for rgb its 8Uc3
        //mrgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        frame.release();
    }

    public float calcLinearDistance(int x1, int x2, int y1, int y2) {
        double res = Math.sqrt(Math.pow(Math.abs(x2 - x1), 2) + Math.pow(Math.abs(y2 - y1), 2));
        return (float) res;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final int IN_WIDTH = 300;
        final int IN_HEIGHT = 300;
       // final float WH_RATIO = (float) IN_WIDTH / IN_HEIGHT;
        final double IN_SCALE_FACTOR = 0.007843;
        final double MEAN_VAL = 127.5;
        final double THRESHOLD = 0.2;

        // Get a new frame
        frame = inputFrame.rgba();
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

        // Forward image through network.
        Mat blob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR,
                new Size(IN_WIDTH, IN_HEIGHT),
                new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), /*swapRB*/false, /*crop*/false);
        net.setInput(blob);
        Mat detections = net.forward();

        int cols = frame.cols();
        int rows = frame.rows();

        detections = detections.reshape(1, (int) detections.total() / 7);
        mp = MediaPlayer.create(getApplicationContext(), R.raw.alarm_sound);
        for (int i = 0; i < detections.rows(); ++i) {
            double confidence = detections.get(i, 2)[0];
            if (confidence > THRESHOLD) {
                int classId = (int) detections.get(i, 1)[0];
                if (classId == 15) {
                    int left = (int) (detections.get(i, 3)[0] * cols);
                    int top = (int) (detections.get(i, 4)[0] * rows);
                    int right = (int) (detections.get(i, 5)[0] * cols);
                    int bottom = (int) (detections.get(i, 6)[0] * rows);
                    int left1 = (int) (detections.get(i + 1, 3)[0] * cols);
                    int top1 = (int) (detections.get(i + 1, 4)[0] * rows);
                    int right1 = (int) (detections.get(i + 1, 5)[0] * cols);
                    //int bottom1 = (int) (detections.get(i + 1, 6)[0] * rows);
                    double dis = getDistanceFromRectangles(left, left1, right, right1, top, top1);
                    //Log.i("Distancesbetweenrect", String.valueOf(dis));
                    // Draw rectangle around detected object.
                    Imgproc.rectangle(frame, new Point(left, top), new Point(right, bottom),
                            new Scalar(0, 255, 0));
                    //Log.i("classid", String.valueOf(classId));
                    String label = classNames[classId] + ": " + confidence;
                    int[] baseLine = new int[1];
                    Size labelSize = Imgproc.getTextSize(label, Core.FONT_HERSHEY_COMPLEX, 1, 1, baseLine);

                    // Draw background for label.
                    Imgproc.rectangle(frame, new Point(left, top - labelSize.height),
                            new Point(left + labelSize.width, top + baseLine[0]),
                            new Scalar(255, 255, 255), Core.FILLED);
                    // Write class name and confidence.
                    float fin_dis = getDistanceFromCamera(getFocalLength(), (float) 406.4, (float) 0.39 * calcLinearDistance(left, right, top, top));
                    if (fin_dis < 1.33 && !mute) {
                        mp.start();
                    } else if (mute) {
                        mp.stop();
                    }
                    //Log.i("Distance Calculated", classNames[classId] + ":" + String.valueOf(fin_dis));
                    if(isDistanceFromYou) {
                        Imgproc.putText(frame, classNames[classId] + ":  " + String.valueOf(fin_dis), new Point(left, top),
                                Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(0, 0, 0));
                    }
                    else
                    {
                        Imgproc.putText(frame, "Between Two People" + ":  " + String.valueOf(dis), new Point(left, top),
                                Core.FONT_HERSHEY_COMPLEX, 1, new Scalar(0, 0, 0));
                    }

                }
            }
        }

        //framet = frame.t();
        // Core.flip(frame,frame,Core.ROTATE_90_COUNTERCLOCKWISE);
        //Imgproc.resize(frame, frame, frame.size());
        return frame;
    }

    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();

        BufferedInputStream inputStream = null;

        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();

            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            //Log.i("TAG", "Failed to upload a file");
        }
        return "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }

    }


    private class music_play extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
           mp = MediaPlayer.create(getApplicationContext(), R.raw.alarm_sound);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mp.start();
            return (null);
        }

        @Override
        protected void onPostExecute(Void v) {
            mp.stop();
            super.onPostExecute(v);
        }
    }
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(),"This application needs read, write, and camera permissions to run. Application now closing.",Toast.LENGTH_LONG);
                System.exit(0);
            }
        }
    }
}