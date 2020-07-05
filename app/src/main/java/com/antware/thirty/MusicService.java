package com.antware.thirty;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

/**
 * Service for music playback.
 * @author Anton J Olsson
 */

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


    /**
     * Return the communication channel to the service.
     * @param arg0 The Intent that was used to bind to this service, as given to Context.bindService.
     * Note that any extras that were included with the Intent at that point will not be seen here
     * @return Return an IBinder through which clients can call on to the service
     */
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    /**
     * Pauses the playback with a delay so that it can be resumed again with no stutter on e.g. a
     * configuration change.
     */
    public void pauseDelayed() {
        pauseHandler.postDelayed(pauseRunnable, PAUSE_DELAY);
    }

    /**
     * Sets the playback position.
     * @param position the new playbackback position
     */
    public void setPlaybackPos(int position) {
        currentPos = position;
        player.seekTo(position);
    }

    public class ServiceBinder extends Binder {
        /**
         * Returns this instance.
         * @return this instance
         */
        MusicService getService() {
            return MusicService.this;
        }
    }

    /**
     * Pauses the music, if it's playing.
     */
    public void pauseMusic()
    {
        if (player.isPlaying()) {
            player.pause();
            currentPos = player.getCurrentPosition();
        }
    }

    /**
     * Resumes the music, if not playing. Removes a Runnable to pause the music, if present
     * (see pauseDelayed).
     */
    public void resumeMusic()
    {
        pauseHandler.removeCallbacks(pauseRunnable);
        if (!player.isPlaying()) {
            player.seekTo(currentPos);
            player.start();
        }
    }

    /**
     * Starts a MediaPlayer.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.music);
        player.setLooping(true);
        player.setVolume(MUSIC_VOLUME,MUSIC_VOLUME);

    }

    /**
     * Called by the system every time a client explicitly starts the service by calling Context.startService(Intent),
     * providing the arguments it supplied and a unique integer token representing the start request.
     * Also starts the MediaPlayer at current playback position, if playBack is true.
     * @param intent The Intent supplied to Context.startService(Intent), as given
     * @param flags Additional data about this start request
     * @param startId A unique integer representing this specific request to start
     * @return
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        player.seekTo(currentPos);
        boolean playMusic = intent.getBooleanExtra("playMusic", false);
        if (playMusic) player.start();
        return START_STICKY;
    }

    /**
     * Saves playback position before stopping and releasing the MediaPlayer.
     */
    @Override
    public void onDestroy() {
        currentPos = player.getCurrentPosition();
        player.stop();
        player.release();
    }

    /**
     * Called to indicate an error.
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }
}