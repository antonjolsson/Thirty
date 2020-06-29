package com.antware.thirty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MusicPlayingActivity extends AppCompatActivity {

    protected static final int MESSAGE_PLAY_MUSIC = 0;

    public static final String KEY_PLAY_MUSIC = "playMusic";
    private static final float LARGE_TEXT_SIZE = 24;
    private static final float SMALL_TEXT_SIZE = 18;

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
        if (savedInstanceState != null){
            playMusic = savedInstanceState.getBoolean(KEY_PLAY_MUSIC);
        }
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
        if(serviceBound)
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

    private void setMusicControlText() {
        if (!playMusic) {
            musicControlView.setText(R.string.play);
            musicControlView.setTextSize(LARGE_TEXT_SIZE);
        }
        else {
            musicControlView.setText(R.string.pause);
            musicControlView.setTextSize(SMALL_TEXT_SIZE);
        }
    }
}