package me.lehrner.spotifystreamer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;


public class MediaPlayerService extends Service implements  MediaPlayer.OnPreparedListener,
                                                            MediaPlayer.OnErrorListener,
                                                            MediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY = "me.lehrner.spotifystreamer.PLAY";
    public static final String ACTION_PREVIOUS = "me.lehrner.spotifystreamer.PREVIOUS";
    public static final String ACTION_NEXT = "me.lehrner.spotifystreamer.NEXT";
    public static final String ACTION_PAUSE = "me.lehrner.spotifystreamer.PAUSE";
    public static final String ACTION_START = "me.lehrner.spotifystreamer.START";
    public static final String ACTION_SET_IMAGE = "me.lehrner.spotifystreamer.SET_IMAGE";
    public static final String ACTION_SET_PROGRESS = "me.lehrner.spotifystreamer.SET_PROGRESS";

    public static final String KEY_TRACK_ID = "me.lehrner.spotifystreamer.track.id";
    public static final String KEY_PLAYLIST = "me.lehrner.spotifystreamer.playlist";
    public static final String KEY_NOTIFICATION_IMAGE = "me.lehrner.spotifystreamer.notification.image";
    public static final String KEY_ARTIST = "me.lehrner.spotifystreamer.artist";
    public static final String KEY_PROGRESS = "me.lehrner.spotifystreamer.progress";

    private static final int NO_TRACK = -1;
    private static final int mNotificationId = 34589;

    private MediaPlayer mMediaPlayer = null;
    private MediaPlayerBinder mBinder;
    private int mStartId = 0, mTrackId = 0, mListSize = 0, mDuration = 0, mUserProgress = 0;
    private ArrayList<SpotifyTrackSearchResult> mPlayList;
    private PlayerState mPLayerState = PlayerState.IDLE;
    private NotificationCompat.Builder mNotificationBuilder;
    private RemoteViews mRemoteViews;
    private String mArtist;
    private boolean mNotForeground = true;
    private NotificationManager mNotificationManager;

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

        mDuration = 0;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isCompleted() {
        return (mPLayerState == PlayerState.COMPLETED);
    }

    public boolean isIdle() {
        return mPLayerState == PlayerState.IDLE;
    }

    public boolean isPlaying() {
        return (mMediaPlayer != null) && mMediaPlayer.isPlaying();
    }

    private void pause() {
        if (mPLayerState == PlayerState.STARTED) {
            Log.d("Player.pause", "Pause");
            mMediaPlayer.pause();
            mPLayerState = PlayerState.PAUSE;
        }
    }

    private void previous() {
        if (mTrackId == 0) {
            mTrackId = mListSize - 1;
        }
        else {
            mTrackId--;
        }

        play(mTrackId);
    }

    private void next() {
        if (mTrackId == mListSize -1) {
            mTrackId = 0;
        }
        else {
            mTrackId++;
        }

        play(mTrackId);
    }

    private void play() {
        if (mPLayerState == PlayerState.PAUSE) {
            mMediaPlayer.start();
            mPLayerState = PlayerState.STARTED;
            Log.d("Player.onStartCommand", "Started");
        }
        else {
            play(mTrackId);
        }
    }

    private void play(int playListId) {
        if (mMediaPlayer == null) {
            initMediaPlayer();
        }
        else {
            mMediaPlayer.reset();
            mDuration = 0;
        }

        mPLayerState = PlayerState.IDLE;
        Log.d("Player.play", "Idle");
        setDataSource(mPlayList.get(playListId).getTrackUrl());
        mTrackId = playListId;
    }

    public int getDuration() {
        return mDuration;
    }

    public int getCurrentPosition() {
        return ((mMediaPlayer != null) && (mPLayerState != PlayerState.IDLE)) ? mMediaPlayer.getCurrentPosition() : 0;
    }

    public int getTrackId() {
        return mTrackId;
    }

    private void setDataSource(String url) {
        if (mPLayerState != PlayerState.IDLE) {
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

        boolean updateNotificationTrack = false;

        switch (intent.getAction()) {
            case ACTION_PLAY:
                mNotificationBuilder.setOngoing(true);

                updateNotificationPlayPause(ACTION_PLAY);
                int trackId = intent.getIntExtra(KEY_TRACK_ID, NO_TRACK);
                ArrayList<SpotifyTrackSearchResult> playlist = intent.getParcelableArrayListExtra(KEY_PLAYLIST);
                String artist = intent.getStringExtra(KEY_ARTIST);

                if (artist != null) {
                    mArtist = artist;
                }

                if (playlist != null) {
                    mPlayList = playlist;
                    mListSize = mPlayList.size();
                }

                if (trackId == NO_TRACK) {
                    play();
                }
                else {
                    play(trackId);
                    updateNotificationTrack = true;
                }
                break;
            case ACTION_START:
                Log.d("Service.onStartCommand", "Service started");
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                createNotificationRemoteViews();
                createNotificationBuilder();
                mBinder = new MediaPlayerBinder(this);
                break;
            case ACTION_PREVIOUS:
                Log.d("Service.onStartCommand", "Previous");
                updateNotificationPlayPause(ACTION_PLAY);
                updateNotificationTrack = true;
                mNotificationBuilder.setOngoing(true);
                previous();
                break;
            case ACTION_NEXT:
                Log.d("Service.onStartCommand", "Next");
                updateNotificationPlayPause(ACTION_PLAY);
                updateNotificationTrack = true;
                mNotificationBuilder.setOngoing(true);
                next();
                break;
            case ACTION_PAUSE:
                if (mMediaPlayer.isPlaying()) {
                    Log.d("Service.onStartCommand", "Pause");
                    updateNotificationPlayPause(ACTION_PAUSE);
                    mNotificationBuilder.setOngoing(false);
                    pause();
                }
                break;
            case ACTION_SET_IMAGE:
                Log.d("Service.onStartCommand", "Set image");
                setNotificationImage((Bitmap) intent.getParcelableExtra(KEY_NOTIFICATION_IMAGE));
                break;
            case ACTION_SET_PROGRESS:
                Log.d("Service.onStartCommand", "Set progress");
                setProgressAndPlay(intent.getIntExtra(KEY_PROGRESS, 0));
                break;
            default:
                Log.e("Service.onStartCommand", "Invalid action: " + intent.getAction());
        }

        if (updateNotificationTrack) {
            SpotifyTrackSearchResult track = mPlayList.get(mTrackId);
            updateNotificationTrack(mArtist, track.getTrackName(), track.getImageUrlMedium());
        }

        showNotification();

        return START_NOT_STICKY;
    }

    private void setProgressAndPlay(int progress) {
        if (mMediaPlayer == null) {
            initMediaPlayer();
        }

        switch(mPLayerState) {
            case STARTED:
                mMediaPlayer.seekTo(progress);
                break;
            case PAUSE:
                mMediaPlayer.seekTo(progress);
                play();
                break;
            case COMPLETED:
                mUserProgress = progress;
                play();
                break;
            default:
                Log.d("setProgressAndPlay", "Nothing to do in state idle");
        }
    }

    private void setNotificationImage(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        mRemoteViews.setImageViewBitmap(R.id.notification_album, bitmap);
        mNotificationBuilder.setContent(mRemoteViews);

        showNotification();
    }

    private void createNotificationBuilder() {

        //the intent that is started when the notification is clicked (works)
        Intent notificationIntent = new Intent(this, PlayerActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pendingNotificationIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContent(mRemoteViews);

//        notification.contentIntent = pendingNotificationIntent;
//        notification.flags |= Notification.FLAG_NO_CLEAR;

        //this is the intent that is supposed to be called when the
        //button is clicked
//        Intent switchIntent = new Intent(this, MediaPlayerService.class);
//        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0,
//                switchIntent, 0);

//        notificationView.setOnClickPendingIntent(R.id.closeOnFlash,
//                pendingSwitchIntent);


    }

    private void updateNotificationTrack(String artist, String track, String albumUrl) {
        mRemoteViews.setTextViewText(R.id.notification_trackname, track);
        mRemoteViews.setTextViewText(R.id.notification_artist, artist);

        BitmapHelper bitmapHelper = new BitmapHelper(this);
        bitmapHelper.execute(albumUrl);

        mNotificationBuilder.setContent(mRemoteViews);
    }

    private void updateNotificationPlayPause(String action) {
        switch (action) {
            case ACTION_PLAY:
                mRemoteViews.setViewVisibility(R.id.notification_play, View.GONE);
                mRemoteViews.setViewVisibility(R.id.notification_pause, View.VISIBLE);
                break;
            case ACTION_PAUSE:
                mRemoteViews.setOnClickPendingIntent(R.id.notification_play, getPlayPendingIntent());

                mRemoteViews.setViewVisibility(R.id.notification_pause, View.GONE);
                mRemoteViews.setViewVisibility(R.id.notification_play, View.VISIBLE);
                break;
            default:
                Log.e("NotificationPlayPause", "Invalid action: " + action);
                return;
        }

        mNotificationBuilder.setContent(mRemoteViews);
        showNotification();
    }

    private void showNotification() {
        if((mMediaPlayer == null) || (mNotificationBuilder == null)) {
            return;
        }

        if (mNotForeground) {
            startForeground(mNotificationId, mNotificationBuilder.build());
            mNotForeground = false;
        }
        else {
            mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
        }
    }

    // Called when MediaPlayer is ready
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (mUserProgress != 0) {
            mediaPlayer.seekTo(mUserProgress);
            mUserProgress = 0;
        }

        mediaPlayer.start();
        mPLayerState = PlayerState.STARTED;
        updateNotificationPlayPause(ACTION_PLAY);
        mDuration = mediaPlayer.getDuration();
        Log.d("Player.onPrepared", "Started, duration: " + mDuration);
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

    private PendingIntent getPlayPendingIntent() {
        return getPlayPendingIntent(NO_TRACK);
    }

    private PendingIntent getPlayPendingIntent(int trackId) {
        Intent intent = new Intent(this, MediaPlayerService.class);

        intent.setAction(ACTION_PLAY);
        intent.putExtra(KEY_TRACK_ID, trackId);
        intent.putExtra(KEY_PLAYLIST, mPlayList);
        intent.putExtra(KEY_ARTIST, mArtist);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createNotificationRemoteViews() {
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.notification);

        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(ACTION_PREVIOUS);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.notification_previous, pendingIntent);

        intent.setAction(ACTION_NEXT);
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.notification_next, pendingIntent);

        intent.setAction(ACTION_PAUSE);
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.notification_pause, pendingIntent);

        mRemoteViews.setOnClickPendingIntent(R.id.notification_play, getPlayPendingIntent());
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d("Service.onCompletion", "song completed");

        mPLayerState = PlayerState.COMPLETED;

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        mRemoteViews.setOnClickPendingIntent(R.id.notification_play, getPlayPendingIntent(mTrackId));
        mNotificationBuilder.setOngoing(false);
        updateNotificationPlayPause(ACTION_PAUSE);

        stopSelfResult(mStartId);
    }

    @Override
    public void onDestroy() {
        Log.d("Service.onDestroy", "end service");
        super.onDestroy();

        if(mNotForeground) {
            Log.d("Service.onDestroy", "cancel notification");
            mNotificationManager.cancel(mNotificationId);
        }
        else {
            Log.d("Service.onDestroy", "stop foreground");
            stopForeground(true);
        }

        mBinder.clear();

//        RefWatcher refWatcher = SpotifyStreamerApplication.getRefWatcher(getApplication());
//        refWatcher.watch(this);
    }
}
