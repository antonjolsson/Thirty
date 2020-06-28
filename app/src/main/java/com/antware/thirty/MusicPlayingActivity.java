package com.antware.thirty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class MusicPlayingActivity extends AppCompatActivity {

    public static final String KEY_PLAY_MUSIC = "playMusic";

    protected Intent musicIntent;
    protected boolean playMusic = true;
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_PLAY_MUSIC, playMusic);
    }

    protected void initMusic() {
        bindMusicService();
        musicIntent = new Intent();
        musicIntent.setClass(this, MusicService.class);
        if (playMusic)
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
}