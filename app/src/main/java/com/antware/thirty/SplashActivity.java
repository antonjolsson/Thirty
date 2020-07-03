package com.antware.thirty;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

// Class for displaying logo/splash screen
public class SplashActivity extends MusicPlayingActivity {

    private final static int SPLASH_DUR = 3000; // Display duration time
    private static final long ANIMATION_DUR = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        animateLogo();
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

    private void animateLogo() {
        ImageView logoView = findViewById(R.id.logoView);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoView, "scaleX", 0.f, 1.f);
        scaleX.setDuration(ANIMATION_DUR);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoView, "scaleY", 0.f, 1.f);
        scaleY.setDuration(ANIMATION_DUR);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(logoView, "rotation", 0.f, 360.f);
        rotation.setDuration(ANIMATION_DUR);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, rotation);
        set.start();
    }
}