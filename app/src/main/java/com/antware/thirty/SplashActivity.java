package com.antware.thirty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

// Class for displaying logo/splash screen
public class SplashActivity extends MusicPlayingActivity {

    private final static int SPLASH_DUR = 4000; // Display duration time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Make it possible to rotate screen during activity without affecting music playback or activity duration
        if (savedInstanceState == null){
            initMusic();
            final Intent intent = new Intent(this, GameActivity.class);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_DUR);
        }
        else bindMusicService();
    }
}