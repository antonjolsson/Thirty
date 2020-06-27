package com.antware.thirty;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class MusicService extends Service implements MediaPlayer.OnErrorListener{

    private static final float MUSIC_VOLUME = 0.5f;
    private final IBinder mBinder = new ServiceBinder();

    MediaPlayer player;
    private int currentPos = 0;

    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class ServiceBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void pauseMusic()
    {
        if(player.isPlaying())
        {
            player.pause();
            currentPos = player.getCurrentPosition();

        }
    }

    public void resumeMusic()
    {
        if(!player.isPlaying())
        {
            player.seekTo(currentPos);
            player.start();
        }
    }

    public void stopMusic()
    {
        player.stop();
        player.release();
        player = null;
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

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
}