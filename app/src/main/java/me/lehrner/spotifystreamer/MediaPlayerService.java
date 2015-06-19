package me.lehrner.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class MediaPlayerService extends Service implements  MediaPlayer.OnPreparedListener,
                                                            MediaPlayer.OnErrorListener,
                                                            MediaPlayer.OnCompletionListener {
    public static final String ACTION_PLAY = "me.lehrner.spotifystreamer.PLAY";
    public static final String ACTION_START = "me.lehrner.spotifystreamer.START";
    public static final String KEY_TRACK_URL = "me.lehrner.spotifystreamer.track.url";
    private static final int STATE_IDLE = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_COMPLETED = 3;

    private MediaPlayer mMediaPlayer = null;
    private int mPLayerState = STATE_IDLE;
    private final IBinder mBinder = new MediaPlayerBinder();
    private int mStartId;

    public class MediaPlayerBinder extends Binder {
         MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public MediaPlayerService() {
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    public boolean isCompleted() {
        return (mPLayerState == STATE_COMPLETED);
    }

    public boolean isIdle() {
        return mPLayerState == STATE_IDLE;
    }

    public boolean isPlaying() {
        return (mMediaPlayer != null) && mMediaPlayer.isPlaying();
    }

    public void pause() {
        if (mPLayerState == STATE_STARTED) {
            Log.d("Player.pause", "Pause");
            mMediaPlayer.pause();
            mPLayerState = STATE_PAUSE;
        }
    }

    public int getDuration() {
        return ((mMediaPlayer != null) && (mPLayerState != STATE_IDLE)) ? mMediaPlayer.getDuration() : 0;
    }

    public int getCurrentPosition() {
        return ((mMediaPlayer != null) && (mPLayerState != STATE_IDLE)) ? mMediaPlayer.getCurrentPosition() : 0;
    }

    private void setDataSource(String url) {
        if (mPLayerState !=STATE_IDLE) {
            Log.d("Service.setDataSource", "Illegal state: " + mPLayerState);
            stopSelf();
        }

        Log.d("Player.setDataSource", "URL: " + url);

        try {
            mMediaPlayer.setDataSource(url);
        }
        catch (Exception e) {
            Log.e("Player.setDataSource", "Can't set data source: " + e.toString());
            stopSelf();
        }

        Log.d("Player.setDataSource", "prepareAsync");
        mMediaPlayer.prepareAsync();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;

        if (mMediaPlayer == null) {
            initMediaPlayer();
        }

        switch (intent.getAction()) {
            case ACTION_PLAY:
                Log.d("Service.onStartCommand", "Action play");
                if (mPLayerState == STATE_PAUSE) {
                    mMediaPlayer.start();
                    mPLayerState = STATE_STARTED;
                    Log.d("Player.onStartCommand", "Started");
                }
                else {
                    mMediaPlayer.reset();
                    mPLayerState = STATE_IDLE;
                    Log.d("Player.onStartCommand", "Idle");
                    setDataSource(intent.getStringExtra(KEY_TRACK_URL));
                }
                break;
            case ACTION_START:
                Log.d("Service.onStartCommand", "Service started");
                break;
            default:
                Log.e("Service.onStartCommand", "Invalid action: " + intent.getAction());
        }

        return START_NOT_STICKY;
    }

    // Called when MediaPlayer is ready
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        mPLayerState = STATE_STARTED;
        Log.d("Player.onPrepared", "Started");
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.e("Service.onError", "What: " + what + ", extra: " + extra);
        mediaPlayer.release();
        //noinspection UnusedAssignment
        mediaPlayer = null;

        initMediaPlayer();

        return true;
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d("Service.onCompletion", "song completed");

        mPLayerState = STATE_COMPLETED;

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        stopSelfResult(mStartId);
    }

    @Override
    public void onDestroy() {
        Log.d("Service.onDestroy", "end service");
        super.onDestroy();
    }
}
