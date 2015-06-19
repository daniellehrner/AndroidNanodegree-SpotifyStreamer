package me.lehrner.spotifystreamer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class PlayerActivityFragment extends Fragment {
    private static final String KEY_TRACK = "me.lehrner.spotifystreamer.tracks";
    private static final String KEY_ARTIST = "me.lehrner.spotifystreamer.artist";
    private static final String KEY_TAG = "me.lehrner.spotifystreamer.tag";
    private static final String KEY_DURATION = "me.lehrner.spotifystreamer.duration";
    private static final String KEY_CURRENT_POSITION = "me.lehrner.spotifystreamer.position";

    private static final String TAG_PLAY = "play";
    private static final String TAG_PAUSE = "pause";

    private static final int DELAY = 200, PERIOD = 200;

    private PlayerActivity mActivity;
    private TextView mArtistNameView, mAlbumNameView, mTrackNameView, mPlayerTimeStartView, mPlayerTimeEndView;
    private ImageView mPlayerImage;
    private SpotifyTrackSearchResult mTrack;
    private ImageButton mPlayButton;
    private MediaPlayerService mPlayerService;
    private Intent mPlayerServiceIntent;
    private View mRootView;
    private boolean mBound = false, mPlayAfterConnected = false;
    private String mArtistName, mTag;
    private int mTrackDuration = 0, mcurrentPosition = 0, mLastCurrentPosition = 0, mLastTrackDuration = 0;
    private SeekBar mSeekBar;
    private Timer mTimer;
    private TimerTask mUpdateTrackStatus;

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_player, container, false);
        return mRootView;
    }

    private String timeIntToString(int timeMs) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeMs),
                TimeUnit.MILLISECONDS.toSeconds(timeMs) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMs))
        );
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mArtistNameView = (TextView) mRootView.findViewById(R.id.player_artist_name);
        mAlbumNameView = (TextView) mRootView.findViewById(R.id.player_album_name);
        mTrackNameView = (TextView) mRootView.findViewById(R.id.player_track_name);
        mPlayerImage = (ImageView) mRootView.findViewById(R.id.player_image_view);
        mPlayButton = (ImageButton) mRootView.findViewById(R.id.player_image_play);
        mPlayerTimeStartView = (TextView) mRootView.findViewById(R.id.player_time_start);
        mPlayerTimeEndView = (TextView) mRootView.findViewById(R.id.player_time_end);
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.player_time_progress);
        mSeekBar.setIndeterminate(false);

        mPlayerServiceIntent = new Intent(mActivity, MediaPlayerService.class);

        if (savedInstanceState != null) {
            mTrack = savedInstanceState.getParcelable(KEY_TRACK);
            mArtistName = savedInstanceState.getString(KEY_ARTIST);
            changePlayButton(savedInstanceState.getString(KEY_TAG));

            mTrackDuration = savedInstanceState.getInt(KEY_DURATION);
            mPlayerTimeEndView.setText(timeIntToString(mTrackDuration));
            mSeekBar.setMax(mTrackDuration);

            mcurrentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
            mPlayerTimeStartView.setText(timeIntToString(mcurrentPosition));

        }
        else {
            mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_START);
            mActivity.startService(mPlayerServiceIntent);
        }

        setTrack();
    }

    @Override
    public void onStart() {
        Log.d("PlayerFragment.onStart", "Start");
        super.onStart();

        mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_START);
        mActivity.startService(mPlayerServiceIntent);

        mPlayerServiceIntent = new Intent(mActivity, MediaPlayerService.class);
        mActivity.bindService(mPlayerServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

        startTimer();
    }

    private void startTimer() {
        createTimerTask();

        // periodically check progress and duration of track every
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(mUpdateTrackStatus, DELAY, PERIOD);
    }

    private void stopTimer() {
        mTimer.cancel();
        mTimer.purge();
        mUpdateTrackStatus.cancel();
    }

     private int convertProgress(int time) {
         int result = 0;

         if (mTrackDuration > 0) {
             result = time * 100 / mTrackDuration;
         }

         return result;
     }

    private void createTimerTask() {
        mUpdateTrackStatus = new TimerTask() {
            public void run() {
                if (mPlayerService != null) {
                    if (mPlayerService.getState().equals(MediaPlayerService.STATE_STARTED) ||
                            mPlayerService.getState().equals(MediaPlayerService.STATE_PAUSE) ) {

                        int duration = mPlayerService.getDuration();
                        if (duration > 0) {
                            mTrackDuration = duration;
                        }
                        else {
                            mTrackDuration = 0;
                        }

                        if (mLastTrackDuration != mTrackDuration) {

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPlayerTimeEndView.setText(timeIntToString(mTrackDuration));
//                                    mSeekBar.setMax(mTrackDuration);
//                                    mSeekBar.setProgress(0);
                                }
                            });

                            mLastTrackDuration = mTrackDuration;
                            Log.d("Player.Timer", "Setting duration to :" + mTrackDuration);
                        }

                        mcurrentPosition = mPlayerService.getCurrentPosition();

                        // only update time if it has changed
                        if (mLastCurrentPosition != mcurrentPosition) {

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPlayerTimeStartView.setText(timeIntToString(mcurrentPosition));
                                    mSeekBar.setProgress(convertProgress(mTrackDuration));
                                }
                            });

                            mLastCurrentPosition = mcurrentPosition;
//                            Log.d("Player.Timer", "Setting current position to :" + mcurrentPosition);
                        }
                    }
                }
            }
        };
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
            mPlayerService = binder.getService();
            mBound = true;

            Log.d("onServiceConnected", "Bound");

            if (mPlayAfterConnected) {
                Log.d("onServiceConnected", "Play");
                playTrack(mTrack.getTrackUrl());
                mPlayAfterConnected = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("onServiceConnected", "Unbound");
            mBound = false;
        }
    };

    private void setTrack() {
        setTrack(mActivity.getArtistName(), mActivity.getTrack());
    }

    public void setTrack(String artistName, SpotifyTrackSearchResult track) {
        mTrack = track;
        mArtistName = artistName;

        String albumName = mTrack.getAlbumName();

        mArtistNameView.setText(artistName);
        mAlbumNameView.setText(albumName);
        mTrackNameView.setText(mTrack.getTrackName());

        String albumImageUrl = mTrack.getImageUrlBig();

        if (!albumImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(albumImageUrl)
                    .placeholder(R.drawable.vinyl)
                    .into(mPlayerImage);

            mPlayerImage.setContentDescription(getString(R.string.image_of_artist) + albumName);
        }

        if (mBound) {
            playTrack(mTrack.getTrackUrl());
        }
        // if service is not bound yet, set boolean to start track after connection is established
        else {
            mPlayAfterConnected = true;
        }
        changePlayButton(TAG_PLAY);
        mPlayerTimeStartView.setText("0:00");
        mPlayerTimeEndView.setText("");
    }

    public void playPauseTrack() {
        String buttonTag = (String) mPlayButton.getTag();

        switch (buttonTag) {
            case TAG_PLAY:
                mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_PAUSE);
                changePlayButton(TAG_PAUSE);
                break;
            case TAG_PAUSE:
                changePlayButton(TAG_PLAY);
                playTrack(mTrack.getTrackUrl());
                break;
            default:
                Log.e("switchPlayButtonSymbol", "Invalid button tag: " + buttonTag);
                mActivity.finish();
        }


    }

    private void playTrack(String trackUrl) {
            if (!mPlayerService.isPlaying()) {
            changePlayButton(TAG_PLAY);
        }

        Log.d("playTrack", "Starting track " + trackUrl);

        mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_PLAY);
        mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_TRACK_URL, trackUrl);

        mActivity.startService(mPlayerServiceIntent);
    }


    private void changePlayButton(String tag) {
        String buttonTag = (String) mPlayButton.getTag();

        switch (tag) {
            case TAG_PLAY:
                mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                mPlayButton.setTag(TAG_PLAY);
                break;
            case TAG_PAUSE:
                mPlayButton.setImageResource(R.drawable.ic_pause_black_48dp);
                mPlayButton.setTag(TAG_PAUSE);
                break;
            default:
                Log.e("switchPlayButtonSymbol", "Invalid button tag: " + buttonTag);
                mActivity.finish();
        }

        mTag = tag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (PlayerActivity) activity;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBound) {
            mActivity.unbindService(mConnection);
            mBound = false;
            Log.d("Player.onStop", "Unbound from Service");
        }

        stopTimer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_TRACK, mTrack);
        outState.putString(KEY_ARTIST, mArtistName);
        outState.putString(KEY_TAG, mTag);
        outState.putInt(KEY_DURATION, mTrackDuration);
        outState.putInt(KEY_CURRENT_POSITION, mcurrentPosition);
    }

}
