package com.proaxive_ltd.socio_distancing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class About_App extends AppCompatActivity {

    private AdView adview;
    private InterstitialAd interstitialAd;
    TextView about_app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about__app);
        about_app = (TextView) findViewById(R.id.textView);
        about_app.setMovementMethod(new ScrollingMovementMethod());
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        adview = (AdView)findViewById(R.id.about_app_banner);
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3412000284180833/2431558463");
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded(){
                interstitialAd.show();
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);
    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }
}
