package com.antware.thirty;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {

    private static final float MUSIC_VOLUME = 0.5f;

    MediaPlayer player;
    private static int currentPos = 0;

    public IBinder onBind(Intent arg0) {

        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.music);
        player.setLooping(true); // Set looping
        player.setVolume(MUSIC_VOLUME,MUSIC_VOLUME);

    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        player.seekTo(currentPos);
        player.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        currentPos = player.getCurrentPosition();
        player.stop();
        player.release();
    }

}