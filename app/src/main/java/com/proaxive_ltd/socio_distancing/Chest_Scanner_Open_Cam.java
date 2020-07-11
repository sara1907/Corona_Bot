package com.proaxive_ltd.socio_distancing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;

public class Chest_Scanner_Open_Cam extends AppCompatActivity {

    private static Uri imageUri;
    public static final String Model_file = "Covid_Model19_Final1.tflite";
    public static final int REQUEST_IMAGE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chest__scanner__open__cam);
        openCameraIntent();

    }

    private void openCameraIntent(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Chest Scan");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Camera Picture");
        // tell camera where to store the resulting picture
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        Toast.makeText(this, "Kindly take a picture of a Chest X-Ray (Radiograph)!!", Toast.LENGTH_LONG).show();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        // start camera, and wait for it to finish
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        // if the camera activity is finished, obtained the uri, crop it to make it square, and send it to 'Classify' activity
        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            try {
                Toast.makeText(this, "Kindly take a picture of a Chest X-Ray (Radiograph)!!", Toast.LENGTH_LONG).show();
                Uri source_uri = imageUri;
                Uri dest_uri = Uri.fromFile(new File(getCacheDir(), "cropped"));
                // need to crop it to square image as CNN's always required square input
                Crop.of(source_uri, dest_uri).asSquare().start(Chest_Scanner_Open_Cam.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
            else if(requestCode == REQUEST_IMAGE && resultCode == RESULT_CANCELED) {
                try {
                    Intent i = new Intent(Chest_Scanner_Open_Cam.this, MainActivity.class);
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        // if cropping acitivty is finished, get the resulting cropped image uri and send it to 'Classify' activity
        else if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK){
            imageUri = Crop.getOutput(data);
            Intent i = new Intent(Chest_Scanner_Open_Cam.this, Classifier.class);
            // put image data in extras to send
            i.putExtra("resID_uri", imageUri);
            // put filename in extras
            i.putExtra("chosen", Model_file);
            // put model type in extras
            // send other required data
            startActivity(i);
        }
        else if(requestCode == Crop.REQUEST_CROP && resultCode == RESULT_CANCELED){
            imageUri = Crop.getOutput(data);
            Intent i = new Intent(Chest_Scanner_Open_Cam.this, MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(Chest_Scanner_Open_Cam.this, MainActivity.class);
        startActivity(i);
        super.onBackPressed();
    }
}
