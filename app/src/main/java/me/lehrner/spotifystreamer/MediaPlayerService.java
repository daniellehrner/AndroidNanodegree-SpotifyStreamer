package me.lehrner.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.os.IBinder;
import android.util.Log;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY = "me.lehrner.spotifystreamer.PLAY";
    public static final String ACTION_PAUSE = "me.lehrner.spotifystreamer.PAUSE";
    public static final String KEY_TRACK_URL = "me.lehrner.spotifystreamer.track.url";
    private static final String STATE_IDLE = "idle";
    private static final String STATE_STARTED = "started";
    private static final String STATE_PAUSE = "pause";

    private MediaPlayer mMediaPlayer = null;
    private String mPLayerState = STATE_IDLE;

    public MediaPlayerService() {
        initMediaPlayer();
    }


    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    private void setDataSource(String url) {
        if (!mPLayerState.equals(STATE_IDLE)) {
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
        switch (intent.getAction()) {
            case ACTION_PLAY:
                Log.d("Service.onStartCommand", "Action play");
                if (mMediaPlayer == null) {
                    initMediaPlayer();
                }

                if (mPLayerState.equals(STATE_PAUSE)) {
                    mMediaPlayer.start();
                    mPLayerState = STATE_STARTED;
                }
                else {
                    mMediaPlayer.reset();
                    mPLayerState = STATE_IDLE;
                    setDataSource(intent.getStringExtra(KEY_TRACK_URL));
                }

//                String tempTrackUrl = intent.getStringExtra(KEY_TRACK_URL);
//
//                if (tempTrackUrl == null) {
//                    Log.d("Service.onStartCommand", "tempTrackUrl is null");
//                    mMediaPlayer.start();
//                }
//                else {
//                    if (mPLayerState.equals(STATE_STARTED) && !mMediaPlayer.isPlaying()) {
//                        Log.d("Service.onStartCommand", "Previous track stopped");
//                        mMediaPlayer.reset();
//                        mPLayerState = STATE_IDLE;
//                    }
//
//                    if (mLastTrackUrl != null && mLastTrackUrl.equals(tempTrackUrl)) {
//                        Log.d("Service.onStartCommand", "Resume last song");
//                        mMediaPlayer.start();
//                    }
//                    else {
//                        mLastTrackUrl = tempTrackUrl;
//                        setDataSource(mLastTrackUrl);
//                    }
//                }
                break;
            case ACTION_PAUSE:
                Log.d("Service.onStartCommand", "Action pause");
                mMediaPlayer.pause();
                mPLayerState = STATE_PAUSE;
                break;
            default:
                Log.e("Service.onStartCommand", "Invalid action: " + intent.getAction());
        }

        return START_STICKY;
    }

    // Called when MediaPlayer is ready
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d("Player.onPrepared", "PREPARED");
        mediaPlayer.start();
        mPLayerState = STATE_STARTED;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.e("Service.onError", "What: " + what + ", extra: " + extra);
        mediaPlayer.release();
        mediaPlayer = null;

        initMediaPlayer();

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
