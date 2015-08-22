package me.lehrner.spotifystreamer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
    public static final String ACTION_NOTIFICATION = "me.lehrner.spotifystreamer.NOTIFICATION";

    public static final String KEY_TRACK_ID = "me.lehrner.spotifystreamer.track.id";
    public static final String KEY_PLAYLIST = "me.lehrner.spotifystreamer.playlist";
    public static final String KEY_NOTIFICATION_IMAGE = "me.lehrner.spotifystreamer.notification.image";
    public static final String KEY_ARTIST = "me.lehrner.spotifystreamer.artist";
    public static final String KEY_ARTIST_ID = "me.lehrner.spotifystreamer.artist.id";
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
    private String mArtist, mArtistId, mQuery;
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
            Logfn.d("Pause");
            mMediaPlayer.pause();
            mPLayerState = PlayerState.PAUSE;
        }
    }

    private void previous() {
        if (mTrackId == 0) {
            mTrackId = mListSize - 1;
        }
        else {
            --mTrackId;
        }

        play(mTrackId);
    }

    private void next() {
        if (mTrackId == mListSize -1) {
            mTrackId = 0;
        }
        else {
            ++mTrackId;
        }

        play(mTrackId);
    }

    private void play() {
        if (mPLayerState == PlayerState.PAUSE) {
            mMediaPlayer.start();
            mPLayerState = PlayerState.STARTED;
            Logfn.d("Started");
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
        Logfn.d("Idle");
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
            Logfn.d("Illegal state: " + mPLayerState);
            stopSelf();
        }

        Logfn.d("URL: " + url);

        try {
            mMediaPlayer.setDataSource(url);
        }
        catch (Exception e) {
            Logfn.e("Can't set data source: " + e.toString());
            stopSelf();
        }

        Logfn.d("prepareAsync");
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
                String artistId = intent.getStringExtra(KEY_ARTIST_ID);
                String query = intent.getStringExtra(MainActivity.KEY_QUERY);

                if (artist != null) {
                    mArtist = artist;
                }

                if (playlist != null) {
                    mPlayList = playlist;
                    mListSize = mPlayList.size();
                }

                if (artistId != null) {
                    mArtistId = artistId;
                }

                if (query != null) {
                    mQuery = query;
                }

                if (trackId == NO_TRACK) {
                    play();
                }
                else {
                    play(trackId);
                    updateNotificationTrack = true;
                }

                setNotificationIntent();
                break;
            case ACTION_START:
                Logfn.d("Service started");
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                createNotificationRemoteViews();
                createNotificationBuilder();
                mBinder = new MediaPlayerBinder(this);
                break;
            case ACTION_PREVIOUS:
                Logfn.d("Previous");
                updateNotificationPlayPause(ACTION_PLAY);
                updateNotificationTrack = true;
                mNotificationBuilder.setOngoing(true);
                previous();
                setNotificationIntent();
                break;
            case ACTION_NEXT:
                Logfn.d("Next");
                updateNotificationPlayPause(ACTION_PLAY);
                updateNotificationTrack = true;
                mNotificationBuilder.setOngoing(true);
                next();
                setNotificationIntent();
                break;
            case ACTION_PAUSE:
                if (mMediaPlayer.isPlaying()) {
                    Logfn.d("Pause");
                    updateNotificationPlayPause(ACTION_PAUSE);
                    mNotificationBuilder.setOngoing(false);
                    pause();
                }
                break;
            case ACTION_SET_IMAGE:
                Logfn.d("Set image");
                setNotificationImage((Bitmap) intent.getParcelableExtra(KEY_NOTIFICATION_IMAGE));
                break;
            case ACTION_SET_PROGRESS:
                Logfn.d("Set progress");
                setProgressAndPlay(intent.getIntExtra(KEY_PROGRESS, 0));
                break;
            default:
                Logfn.e("Invalid action: " + intent.getAction());
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
                Logfn.d("Nothing to do in state idle");
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

    private void setNotificationIntent() {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());

        if (getResources().getBoolean(R.bool.two_pane)) {
            // intent for main activity
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.setAction(ACTION_NOTIFICATION);
            notificationIntent.putExtra(SearchManager.QUERY, mQuery);
            notificationIntent.putExtra(MainActivityFragment.ARTIST_ID, mArtistId);
            notificationIntent.putExtra(MainActivityFragment.ARTIST_NAME, mArtist);
            notificationIntent.putExtra(TopTracksFragment.ARRAY_ID, mTrackId);
            notificationIntent.putExtra(TopTracksFragment.TRACK_ARRAY, mPlayList);
            notificationIntent.putExtra(TopTracksFragment.ARTIST_NAME, mArtist);
            stackBuilder.addNextIntent(notificationIntent);
        }
        else {
            // intent for main activity
            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
            notificationIntent.setAction(Intent.ACTION_SEARCH);
            notificationIntent.putExtra(SearchManager.QUERY, mQuery);
            stackBuilder.addNextIntent(notificationIntent);

            // intent for top tracks
            notificationIntent = new Intent(getApplicationContext(), TopTracks.class);
            notificationIntent.putExtra(MainActivityFragment.ARTIST_ID, mArtistId);
            notificationIntent.putExtra(MainActivityFragment.ARTIST_NAME, mArtist);
            stackBuilder.addNextIntent(notificationIntent);

            // intent for player
            notificationIntent = new Intent(getApplicationContext(), PlayerActivity.class);
            notificationIntent.setAction(ACTION_NOTIFICATION);
            notificationIntent.putExtra(TopTracksFragment.ARRAY_ID, mTrackId);
            notificationIntent.putExtra(TopTracksFragment.TRACK_ARRAY, mPlayList);
            notificationIntent.putExtra(TopTracksFragment.ARTIST_NAME, mArtist);
            stackBuilder.addNextIntent(notificationIntent);
        }

        PendingIntent pendingNotificationIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        mRemoteViews.setOnClickPendingIntent(R.id.notification_trackname, pendingNotificationIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.notification_artist, pendingNotificationIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.notification_album, pendingNotificationIntent);
        mRemoteViews.setOnClickPendingIntent(R.id.notification_content, pendingNotificationIntent);

        mNotificationBuilder.setContent(mRemoteViews);
        showNotification();
    }

    private void createNotificationBuilder() {
        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContent(mRemoteViews);
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
                Logfn.e("Invalid action: " + action);
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
        Logfn.d("Started, duration: " + mDuration);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Logfn.e("What: " + what + ", extra: " + extra);
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

//        intent = new Intent(this, PlayerActivity.class)
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        Logfn.d("song completed");

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
        Logfn.d("end service");
        super.onDestroy();

        if(mNotForeground) {
            Logfn.d("cancel notification");
            mNotificationManager.cancel(mNotificationId);
        }
        else {
            Logfn.d("stop foreground");
            stopForeground(true);
        }

        mBinder.clear();
    }
}
