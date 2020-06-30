package com.antware.thirty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends MusicPlayingActivity {

    private final static int SPLASH_DUR = 4000; // 4000

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMusic();
        setContentView(R.layout.activity_splash);

        final Intent intent = new Intent(this, GameActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, SPLASH_DUR);
    }
}