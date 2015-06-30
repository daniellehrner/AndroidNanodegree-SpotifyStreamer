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
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PlayerActivityFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {
    private static final String KEY_TRACKS = "me.lehrner.spotifystreamer.tracks";
    private static final String KEY_ARTIST = "me.lehrner.spotifystreamer.artist";
    private static final String KEY_TAG = "me.lehrner.spotifystreamer.tag";
    private static final String KEY_DURATION = "me.lehrner.spotifystreamer.duration";
    private static final String KEY_CURRENT_POSITION = "me.lehrner.spotifystreamer.position";
    private static final String KEY_TRACK_ID = "me.lehrner.spotifystreamer.track.id";

    private static final int DELAY = 200, PERIOD = 200;

    private PlayerActivity mActivity;
    private TextView mArtistNameView, mAlbumNameView, mTrackNameView, mPlayerTimeStartView, mPlayerTimeEndView;
    private ImageView mPlayerImage;
    private ArrayList<SpotifyTrackSearchResult> mTracks;
    private ImageButton mPlayButton;
    private MediaPlayerService mPlayerService;
    private Intent mPlayerServiceIntent;
    private View mRootView;
    private boolean mBound = false, mPlayAfterConnected = false, mSeekbarUserTouch = false;
    private String mArtistName;
    private int mTrackDuration = 0, mCurrentPosition = 0, mLastCurrentPosition = 0,
            mLastTrackDuration = 0, mTrackId = 0, mProgressByUser = 0;
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

        mPlayerServiceIntent = new Intent(mActivity, MediaPlayerService.class);

        boolean newTrack = false, autoStart = false;

        if (savedInstanceState != null) {
            mTracks = savedInstanceState.getParcelableArrayList(KEY_TRACKS);
            mArtistName = savedInstanceState.getString(KEY_ARTIST);
            mTrackId = savedInstanceState.getInt(KEY_TRACK_ID);

            mTrackDuration = savedInstanceState.getInt(KEY_DURATION);
            mPlayerTimeEndView.setText(timeIntToString(mTrackDuration));
            mSeekBar.setMax(mTrackDuration);

            mCurrentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
            mPlayerTimeStartView.setText(timeIntToString(mCurrentPosition));
        }
        else {
            mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_START);
            mActivity.startService(mPlayerServiceIntent);
            mTracks = mActivity.getTracks();
            mArtistName = mActivity.getArtistName();
            mTrackId = mActivity.getTrackId();

            newTrack = true;
            autoStart = true;
        }

        setTrack(mTrackId, newTrack);

        if (autoStart) {
            if (mBound) {
                playTrack(mTrackId);
            }
            // if service is not bound yet, set boolean to start track after connection is established
            else {
                mPlayAfterConnected = true;
            }
        }
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
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mProgressByUser = progress;
            mPlayerTimeStartView.setText(timeIntToString(progress));
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        mSeekbarUserTouch = true;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        mSeekbarUserTouch = false;
        Intent notificationImageIntent = new Intent(mActivity, MediaPlayerService.class);
        notificationImageIntent.setAction(MediaPlayerService.ACTION_SET_PROGRESS);
        notificationImageIntent.putExtra(MediaPlayerService.KEY_PROGRESS, mProgressByUser);

        mActivity.startService(notificationImageIntent);
    }

    private void startTimer() {
        createTimerTask();

        mTimer = new Timer();
        // periodically check progress and duration of track every 200 ms
        mTimer.scheduleAtFixedRate(mUpdateTrackStatus, DELAY, PERIOD);
    }

    private void stopTimer() {
        mTimer.cancel();
        mTimer.purge();
        mUpdateTrackStatus.cancel();
    }

    private void createTimerTask() {
        mUpdateTrackStatus = new TimerTask() {
            public void run() {
                if (!mBound) {
                    return;
                }

                final int trackId = mPlayerService.getTrackId();

                if (trackId != mTrackId) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTrack(trackId, true);
                        }
                    });
                }

                if (mPlayerService.isIdle()) {
                    mPlayButton.setClickable(false);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSeekBar.setEnabled(false);
                        }
                    });
                    return;
                }
                else {
                    mPlayButton.setClickable(true);
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSeekBar.setEnabled(true);
                        }
                    });
                }

                if (!mPlayerService.isCompleted()) {
                    if (mPlayerService.isPlaying() && mPlayButton.getTag().equals(getString(R.string.TAG_PLAY))) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changePlayButton(getString(R.string.TAG_PAUSE));
                            }
                        });
                    }
                    else if (!mPlayerService.isPlaying() && mPlayButton.getTag().equals(getString(R.string.TAG_PAUSE))) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changePlayButton(getString(R.string.TAG_PLAY));
                            }
                        });
                    }

                    int duration = mPlayerService.getDuration();
                    mTrackDuration = (duration > 0) ? duration : 0;

                    if (mTrackDuration == 0) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPlayerTimeEndView.setText("");
                            }
                        });
                    }
                    else if (mLastTrackDuration != mTrackDuration) {

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPlayerTimeEndView.setText(timeIntToString(mTrackDuration));
                                mSeekBar.setMax(mTrackDuration);
                            }
                        });

                        mLastTrackDuration = mTrackDuration;
                        Log.d("Player.Timer", "Setting duration to: " + mTrackDuration);
                    }

                    mCurrentPosition = mPlayerService.getCurrentPosition();

                    if ((mCurrentPosition < 0) ||(mCurrentPosition > mTrackDuration)) {
                        mCurrentPosition = 0;
                    }

                    // only update time if it has changed and user isn't using the seekbar
                    if ((mLastCurrentPosition != mCurrentPosition) && !mSeekbarUserTouch) {

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPlayerTimeStartView.setText(timeIntToString(mCurrentPosition));
                                mSeekBar.setProgress(mCurrentPosition);
                            }
                        });

                        mLastCurrentPosition = mCurrentPosition;
                    }

                }
                else {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changePlayButton(getString(R.string.TAG_PLAY));
                        }
                    });
                }
            }
        };
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerBinder mBinder = (MediaPlayerBinder) service;
            mPlayerService = mBinder.getService();
            mBound = true;

            Log.d("onServiceConnected", "Bound");

            if (mPlayAfterConnected) {
                Log.d("onServiceConnected", "Play");
                playTrack(mTrackId);
                mPlayAfterConnected = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d("onServiceConnected", "Unbound");
            mBound = false;
        }
    };

    private void setTrack(int id, boolean isNewTrack) {
        mTrackId = id;
        SpotifyTrackSearchResult track = mTracks.get(mTrackId);
        String albumName = track.getAlbumName();

        mArtistNameView.setText(mArtistName);
        mAlbumNameView.setText(albumName);
        mTrackNameView.setText(track.getTrackName());

        String albumImageUrl = track.getImageUrlBig();

        if (!albumImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(albumImageUrl)
                    .placeholder(R.drawable.vinyl)
                    .into(mPlayerImage);

            mPlayerImage.setContentDescription(getString(R.string.image_of_artist) + albumName);
        }

        if (isNewTrack) {
            mPlayerTimeStartView.setText("0:00");
            mPlayerTimeEndView.setText("");
            mTrackDuration = mLastCurrentPosition = mCurrentPosition = mLastTrackDuration = 0;
            mSeekBar.setProgress(mCurrentPosition);
        }
        else {
            mPlayerTimeEndView.setText(timeIntToString(mTrackDuration));
            mSeekBar.setMax(mTrackDuration);

            if (mBound && mPlayerService.isPlaying()) {
                mSeekBar.setProgress(mCurrentPosition);
                mPlayerTimeStartView.setText(timeIntToString(mCurrentPosition));
            }
            // track has stopped while the activity was closed
            else {
                mSeekBar.setProgress(mTrackDuration);
                mPlayerTimeStartView.setText(timeIntToString(mTrackDuration));
            }
        }
    }

    public void playPauseTrack(String buttonTag) {
        if (buttonTag.equals(getString(R.string.TAG_PLAY))) {
            Log.d("playPauseTrack", "Button play");
            sendMediaPlayerIntent(MediaPlayerService.ACTION_PLAY);
        }
        else if (buttonTag.equals(getString(R.string.TAG_PAUSE))) {
            Log.d("playPauseTrack", "Button pause");
            sendMediaPlayerIntent(MediaPlayerService.ACTION_PAUSE);
        }
        else {
            Log.e("playPauseTrack", "Invalid button tag: " + buttonTag);
            mActivity.finish();
        }
    }

    private void playTrack(int trackId) {
        Log.d("playTrack", "Starting track with id" + trackId);

        mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_PLAY);
        mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_TRACK_ID, trackId);
        mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_PLAYLIST, mTracks);
        mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_ARTIST, mArtistName);

        mActivity.startService(mPlayerServiceIntent);

        mPlayerServiceIntent.removeExtra(MediaPlayerService.KEY_TRACK_ID);
    }

    public void previous() {
        if (mBound) {
            sendMediaPlayerIntent(MediaPlayerService.ACTION_PREVIOUS);
        }
    }

    public void next() {
        if (mBound) {
            sendMediaPlayerIntent(MediaPlayerService.ACTION_NEXT);
        }
    }

    private void sendMediaPlayerIntent(String action) {
        Intent intent = new Intent(mActivity, MediaPlayerService.class);
        intent.setAction(action);
        mActivity.startService(intent);
    }

    private void changePlayButton(String tag) {
        String buttonTag = (String) mPlayButton.getTag();

        if (tag.equals(buttonTag)) {
            return;
        }

        if (tag.equals(getString(R.string.TAG_PLAY))) {
            mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
            mPlayButton.setTag(getString(R.string.TAG_PLAY));
        }
        else if (tag.equals(getString(R.string.TAG_PAUSE))) {
            mPlayButton.setImageResource(R.drawable.ic_pause_black_48dp);
            mPlayButton.setTag(getString(R.string.TAG_PAUSE));
        }
        else {
            Log.e("changePlayButton", "Invalid button tag: " + buttonTag);
            mActivity.finish();
        }
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

        outState.putParcelableArrayList(KEY_TRACKS, mTracks);
        outState.putString(KEY_ARTIST, mArtistName);
        outState.putString(KEY_TAG, (String) mPlayButton.getTag());
        outState.putInt(KEY_DURATION, mTrackDuration);
        outState.putInt(KEY_CURRENT_POSITION, mCurrentPosition);
        outState.putInt(KEY_TRACK_ID, mTrackId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = SpotifyStreamerApplication.getRefWatcher(mActivity);
        refWatcher.watch(this);
    }
}
