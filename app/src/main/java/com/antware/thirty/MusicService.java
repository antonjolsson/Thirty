package com.antware.thirty;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class MusicService extends Service implements MediaPlayer.OnErrorListener{

    private static final float MUSIC_VOLUME = 0.2f;
    private static final long PAUSE_DELAY = 400;
    private final IBinder mBinder = new ServiceBinder();

    MediaPlayer player;
    private int currentPos = 0; // Current playback position in ms

    Handler pauseHandler = new Handler();
    Runnable pauseRunnable = new Runnable() {
        @Override
        public void run() {
            pauseMusic();
        }
    };

    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    // See explanation in MusicPlayingActivity
    public void pauseDelayed() {
        pauseHandler.postDelayed(pauseRunnable, PAUSE_DELAY);
    }

    public class ServiceBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void pauseMusic()
    {
        if (player.isPlaying()) {
            player.pause();
            currentPos = player.getCurrentPosition();
        }
    }

    public void resumeMusic()
    {
        pauseHandler.removeCallbacks(pauseRunnable);
        if (!player.isPlaying()) {
            player.seekTo(currentPos);
            player.start();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.music);
        player.setLooping(true);
        player.setVolume(MUSIC_VOLUME,MUSIC_VOLUME);

    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        player.seekTo(currentPos);
        boolean playMusic = intent.getBooleanExtra("playMusic", false);
        if (playMusic) player.start();
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