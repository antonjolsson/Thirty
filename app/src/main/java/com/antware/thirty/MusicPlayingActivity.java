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

// Super class for all activities, handling music logic
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
    protected boolean playMusic = false;
    protected boolean serviceBound = false;
    protected MusicService musicService;

    protected final ServiceConnection serviceConnection = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder binder) {
            musicService = ((MusicService.ServiceBinder) binder).getService();
            if (playMusic)
                musicService.resumeMusic();
        }

        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTextSizes();
        if (savedInstanceState != null){
            playMusic = savedInstanceState.getBoolean(KEY_PLAY_MUSIC);
        }
    }

    // Set size of music controls
    private void setTextSizes() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        boolean smallScreen = Math.min(dpHeight, dpWidth) < SMALL_SCREEN_DP_LIMIT;
        largeTextSize = smallScreen ? LARGE_TEXT_SIZE_SMALL_SCREEN : LARGE_TEXT_SIZE_LARGE_SCREEN;
        smallTextSize = smallScreen ? SMALL_TEXT_SIZE_SMALL_SCREEN : SMALL_TEXT_SIZE_LARGE_SCREEN;
    }

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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_PLAY_MUSIC, playMusic);
    }

    protected void initMusic() {
        bindMusicService();
        musicIntent = new Intent();
        musicIntent.setClass(this, MusicService.class);
        musicIntent.putExtra(KEY_PLAY_MUSIC, playMusic);
        startService(musicIntent);
    }

    protected void unbindMusicService()
    {
        if (serviceBound)
        {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    protected void bindMusicService(){
        bindService(new Intent(this, MusicService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
        serviceBound = true;
    }

    // If music is playing and pausing is due to switching activities, delay pausing so that new
    // activity can cancel the pausing, preventing any stuttering. If pausing is due to exiting the
    // game, music will be paused/stopped as well.
    @Override
    protected void onPause() {
        super.onPause();
        musicService.pauseDelayed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindMusicService();
    }

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

    // Resize font size of music control symbols to keep their sizes approx. the same
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