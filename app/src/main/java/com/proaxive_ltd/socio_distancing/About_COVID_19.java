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

public class About_COVID_19 extends AppCompatActivity {
    private String GameID = "3642249";
    private boolean testMode = true;
    private String Interstitial_PACEMENT_ID = "ca-app-pub-3940256099942544/1033173712";
    private AdView adView;
    private InterstitialAd interstitialAd;
    TextView about_covid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about__c_o_v_i_d_19);
        about_covid = (TextView)findViewById(R.id.textView);
        about_covid.setMovementMethod(new ScrollingMovementMethod());
        adView = findViewById(R.id.covid_banner);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3412000284180833/3328587605");
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(new AdRequest.Builder().build()); //Interstitial ads
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded(){
                interstitialAd.show();
            }
        });
        adView.loadAd(adRequest);//banner ad

    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }
}
