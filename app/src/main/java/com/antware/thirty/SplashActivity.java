package com.antware.thirty;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

/**
 * Class for displaying the logo/splash screen.
 * @author Anton J Olsson
 */
public class SplashActivity extends MusicPlayingActivity {

    private final static int SPLASH_DUR = 3000; // Display duration time
    private static final long ANIMATION_DUR = 1000;

    Handler startActivityHandler;
    Runnable startActivityRunnable;

    /**
     * Displays the animated logo for 3 seconds before starting GameActivity and music playback.
     * The delay is implemented by a Handler.
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        animateLogo();
        if (savedInstanceState == null){
            initMusic();
            final Intent intent = new Intent(this, GameActivity.class);
            startActivityHandler = new Handler();
            startActivityRunnable = new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                    finish();
                }
            };
            startActivityHandler.postDelayed(startActivityRunnable, SPLASH_DUR);
        }
        else bindMusicService();
    }

    /**
     * Animates the appearance of the game logo by enlarging and rotating it.
     */
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

    /**
     * Pauses the music and resets the position when back button is pressed. Just as in GameActivity,
     * this must be executed to make the music start from the beginning on app restart.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivityHandler.removeCallbacks(startActivityRunnable);
        musicService.pauseMusic();
        musicService.setPlaybackPos(0);
    }
}