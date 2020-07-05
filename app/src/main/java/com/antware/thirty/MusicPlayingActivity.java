package com.antware.thirty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

/**
 * Super class for all Activities, handling music logic.
 * @author Anton J Olsson
 */
public class MusicPlayingActivity extends AppCompatActivity {

    protected static final int MESSAGE_PLAY_MUSIC = 0;

    public static final String KEY_PLAY_MUSIC = "playMusic";
    private static final float SMALL_SCREEN_DP_LIMIT = 411;
    private static final int LARGE_TEXT_SIZE_LARGE_SCREEN = 24;
    private static final int SMALL_TEXT_SIZE_LARGE_SCREEN = 18;
    private static final int LARGE_TEXT_SIZE_SMALL_SCREEN = 18;
    private static final int SMALL_TEXT_SIZE_SMALL_SCREEN = 14;
    private int largeTextSize, smallTextSize;

    TextView musicControlView;

    protected Intent musicIntent;
    protected boolean playMusic = true;
    protected boolean serviceBound = false;
    protected MusicService musicService;

    protected final ServiceConnection serviceConnection = new ServiceConnection(){

        /**
         * Resumes music when service is connected, if playMusic is true.
         * @param name The concrete component name of the service that has been connected
         * @param binder The IBinder of the Service's communication channel, which you can now make calls on
         */
        public void onServiceConnected(ComponentName name, IBinder binder) {
            musicService = ((MusicService.ServiceBinder) binder).getService();
            if (playMusic)
                musicService.resumeMusic();
        }

        /**
         * Sets musicService to null when service is disconnected.
         * @param name The concrete component name of the service whose connection has been lost
         */
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    /**
     * Restores instance state if not null, and possibly also resumes music playback.
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTextSizes();
        if (savedInstanceState != null){
            playMusic = savedInstanceState.getBoolean(KEY_PLAY_MUSIC);
        }
    }

    /**
     * Sets size of the music controls, depending on screen size.
     */
    private void setTextSizes() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        boolean smallScreen = Math.min(dpHeight, dpWidth) < SMALL_SCREEN_DP_LIMIT;
        largeTextSize = smallScreen ? LARGE_TEXT_SIZE_SMALL_SCREEN : LARGE_TEXT_SIZE_LARGE_SCREEN;
        smallTextSize = smallScreen ? SMALL_TEXT_SIZE_SMALL_SCREEN : SMALL_TEXT_SIZE_LARGE_SCREEN;
    }

    /**
     * Initializes the music control view and adds a click listener.
     */
    protected void initMusicControlView() {
        musicControlView = findViewById(R.id.playView);
        setMusicControlText();
        musicControlView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMusic();
            }
        });
    }

    /**
     * Method to possibly resume the music playback on app restart.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (musicService != null && playMusic)
            musicService.resumeMusic();
    }

    /**
     * Saves the music playback state.
     * @param outState the saved instance state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_PLAY_MUSIC, playMusic);
    }

    /**
     * Initializes the music playback by starting a Service.
     */
    protected void initMusic() {
        bindMusicService();
        musicIntent = new Intent();
        musicIntent.setClass(this, MusicService.class);
        musicIntent.putExtra(KEY_PLAY_MUSIC, playMusic);
        startService(musicIntent);
    }

    /**
     * Unbinds the music service.
     */
    protected void unbindMusicService()
    {
        if (serviceBound)
        {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    /**
     * Binds the music service.
     */
    protected void bindMusicService(){
        bindService(new Intent(this, MusicService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
        serviceBound = true;
    }

    /**
     * If music is playing and pausing is due to switching activities, delays pausing so that new
     * activity can cancel the pausing, preventing any stuttering. If pausing is due to exiting the
     * game, music will be paused/stopped as well.
     */
    @Override
    protected void onPause() {
        super.onPause();
        musicService.pauseDelayed();
    }

    /**
     * Unbinds the music service when activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindMusicService();
    }

    /**
     * If music is playing, pauses the music - and vice versa.
     */
    protected void toggleMusic() {
        if (playMusic) {
            if (musicService != null)
                musicService.pauseMusic();
            playMusic = false;
        }
        else {
            if (musicService != null)
                musicService.resumeMusic();
            playMusic = true;
        }
        setMusicControlText();
    }

    /**
     * Resizes the font size of music control symbols to keep their sizes approx. the same.
     */
    private void setMusicControlText() {
        if (!playMusic) {
            musicControlView.setText(R.string.play);
            musicControlView.setTextSize(largeTextSize);
        }
        else {
            musicControlView.setText(R.string.pause);
            musicControlView.setTextSize(smallTextSize);
        }
    }
}