package me.lehrner.spotifystreamer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PlayerActivityFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener {
    private static final String KEY_TRACKS = "me.lehrner.spotifystreamer.tracks";
    private static final String KEY_ARTIST = "me.lehrner.spotifystreamer.artist";
    private static final String KEY_TAG = "me.lehrner.spotifystreamer.tag";
    private static final String KEY_DURATION = "me.lehrner.spotifystreamer.duration";
    private static final String KEY_CURRENT_POSITION = "me.lehrner.spotifystreamer.position";
    private static final String KEY_TRACK_ID = "me.lehrner.spotifystreamer.track.id";

    private static final int DELAY = 200, PERIOD = 200;

    private Activity mActivity;
    private TextView mArtistNameView, mAlbumNameView, mTrackNameView,
            mPlayerTimeStartView, mPlayerTimeEndView;
    private ImageView mPlayerImage;
    private ArrayList<SpotifyTrackSearchResult> mTracks;
    private ImageButton mPlayButton;
    private MediaPlayerService mPlayerService;
    private Intent mPlayerServiceIntent;
    private View mRootView;
    private boolean mBound = false, mPlayAfterConnected = false,
            mSeekBarUserTouch = false, mNewTrack = false, mAutoStart = false;
    private String mArtistName;
    private int mTrackDuration = 0, mCurrentPosition = 0, mLastCurrentPosition = 0,
            mLastTrackDuration = 0, mTrackId = 0, mProgressByUser = 0;
    private SeekBar mSeekBar;
    private Timer mTimer;
    private TimerTask mUpdateTrackStatus;
    private OnTrackSelectedListener mTrackListener;
    private OnMainActivityControlListener mMainActivityControlListener;

    public PlayerActivityFragment() {
    }

    public interface OnTrackSelectedListener {
        boolean isNotificationIntent();
        boolean isTwoPane();
        String getArtistName();
        ArrayList<SpotifyTrackSearchResult> getTracks();
        int getTrackId();
        String getArtistId();
        String getQuery();
        void setShareIntentUrl(String url);
    }

    public interface OnMainActivityControlListener {
        int getArtistPosition();
        void setNotificationIntent(boolean b);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logfn.d("Start");
        mRootView = inflater.inflate(R.layout.fragment_player, container, false);
        return mRootView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Logfn.d("Start");
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private String timeIntToString(int timeMs) {
        return String.format("%d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(timeMs),
            TimeUnit.MILLISECONDS.toSeconds(timeMs) -
            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMs))
        );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logfn.d("Start");
        super.onCreate(savedInstanceState);

        mPlayerServiceIntent = new Intent(mActivity, MediaPlayerService.class);

        if (savedInstanceState != null) {
            Logfn.d("savedInstanceState not null");
            mTracks = savedInstanceState.getParcelableArrayList(KEY_TRACKS);
            mArtistName = savedInstanceState.getString(KEY_ARTIST);
            mTrackId = savedInstanceState.getInt(KEY_TRACK_ID);

            mTrackDuration = savedInstanceState.getInt(KEY_DURATION);
            mCurrentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION);
        }
        else {
            Logfn.d("savedInstanceState null");
            mTracks = mTrackListener.getTracks();
            mArtistName = mTrackListener.getArtistName();
            mTrackId = mTrackListener.getTrackId();

            mNewTrack = true;

            if (mTrackListener.isNotificationIntent()) {
                mAutoStart = false;
                if (mMainActivityControlListener != null) {
                    mMainActivityControlListener.setNotificationIntent(false);
                }
            }
            else {
                mAutoStart = true;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Logfn.d("Start");
        super.onActivityCreated(savedInstanceState);

        mArtistNameView = (TextView) mRootView.findViewById(R.id.player_artist_name);
        mAlbumNameView = (TextView) mRootView.findViewById(R.id.player_album_name);
        mTrackNameView = (TextView) mRootView.findViewById(R.id.player_track_name);
        mPlayerImage = (ImageView) mRootView.findViewById(R.id.player_image_view);
        mPlayButton = (ImageButton) mRootView.findViewById(R.id.player_image_play);
        mPlayerTimeStartView = (TextView) mRootView.findViewById(R.id.player_time_start);
        mPlayerTimeEndView = (TextView) mRootView.findViewById(R.id.player_time_end);
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.player_time_progress);

        if (savedInstanceState != null) {
            mSeekBar.setMax(mTrackDuration);
            mPlayerTimeStartView.setText(timeIntToString(mCurrentPosition));
            mPlayerTimeEndView.setText(timeIntToString(mTrackDuration));
        }

        setTrack(mTrackId, mNewTrack);

        if (mAutoStart) {
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
        Logfn.d("Start");
        super.onStart();

        startBindService();
        mSeekBar.setOnSeekBarChangeListener(this);
        setButtonOnClick();
    }

    private void setButtonOnClick() {
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playPauseTrack((String) v.getTag());
            }
        });

        final View.OnClickListener prevNextListener = new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.player_image_next:
                        Logfn.d("Button next");
                        next();
                        break;
                    case R.id.player_image_previous:
                        Logfn.d("Button previous");
                        previous();
                        break;
                    default:
                        Logfn.e("Invalid id: " + v.getId());
                }
            }
        };

        mRootView.findViewById(R.id.player_image_previous).setOnClickListener(prevNextListener);
        mRootView.findViewById(R.id.player_image_next).setOnClickListener(prevNextListener);
    }

    private void startBindService() {
        mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_START);
        mActivity.startService(mPlayerServiceIntent);

        mPlayerServiceIntent = new Intent(mActivity, MediaPlayerService.class);
        mActivity.bindService(mPlayerServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mProgressByUser = progress;
            mPlayerTimeStartView.setText(timeIntToString(progress));
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        mSeekBarUserTouch = true;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        mSeekBarUserTouch = false;
        Intent notificationImageIntent = new Intent(mActivity, MediaPlayerService.class);
        notificationImageIntent.setAction(MediaPlayerService.ACTION_SET_PROGRESS);
        notificationImageIntent.putExtra(MediaPlayerService.KEY_PROGRESS, mProgressByUser);

        mActivity.startService(notificationImageIntent);
    }

    private void startTimer() {
        Logfn.d("Start");
        stopTimer();
        createTimerTask();

        mTimer = new Timer();
        // periodically check progress and duration of track every 200 ms
        mTimer.scheduleAtFixedRate(mUpdateTrackStatus, DELAY, PERIOD);
    }

    private void stopTimer() {
        if (mTimer != null) {
            Logfn.d("Stop");
            mTimer.cancel();
            mTimer.purge();
        }

        if (mUpdateTrackStatus != null) {
            mUpdateTrackStatus.cancel();
        }
    }

    private void createTimerTask() {
        mUpdateTrackStatus = new TimerTask() {
            public void run() {
                if (!mBound) {
                    return;
                }

                if (mPlayerService == null) {
                    Logfn.e("mPlayerService is null");
                    return;
                }

                final int playListSize = mPlayerService.getPlayListSize();

                if (playListSize < 0) {
                    Logfn.d("playlist not initialized");
                }

                final int trackId = mPlayerService.getTrackId();

                if (!mPlayerService.isIdle() && (trackId != mTrackId)) {
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
                        Logfn.d("Setting duration to: " + mTrackDuration);
                    }

                    mCurrentPosition = mPlayerService.getCurrentPosition();

                    if ((mCurrentPosition < 0) ||(mCurrentPosition > mTrackDuration)) {
                        mCurrentPosition = 0;
                    }

                    // only update time if it has changed and user isn't using the seek bar
                    if ((mLastCurrentPosition != mCurrentPosition) && !mSeekBarUserTouch) {

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
                            mPlayerTimeStartView.setText(timeIntToString(mTrackDuration));
                            mSeekBar.setProgress(mTrackDuration);
                        }
                    });
                }
            }
        };
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerBinder mBinder = (MediaPlayerBinder) service;
            mPlayerService = mBinder.getService();
            mBound = true;

            Logfn.d("Bound");
            startTimer();

            if (mPlayAfterConnected) {
                Logfn.d("Play");
                playTrack(mTrackId);
                mPlayAfterConnected = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logfn.d("Unbound");
            mBound = false;
            mPlayerService = null;
            stopTimer();
        }
    };

    private void setTrack(int id, boolean isNewTrack) {
        mTrackId = id;
        SpotifyTrackSearchResult track = mTracks.get(mTrackId);
        String albumName = track.getAlbumName();

        mTrackListener.setShareIntentUrl(track.getTrackUrl());

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

    private void playPauseTrack(String buttonTag) {
        if (buttonTag.equals(getString(R.string.TAG_PLAY))) {
            Logfn.d("Button play");
            sendMediaPlayerIntent(MediaPlayerService.ACTION_PLAY);
        }
        else if (buttonTag.equals(getString(R.string.TAG_PAUSE))) {
            Logfn.d("Button pause");
            sendMediaPlayerIntent(MediaPlayerService.ACTION_PAUSE);
        }
        else {
            Logfn.e("Invalid button tag: " + buttonTag);
            mActivity.finish();
        }
    }

    private void playTrack(int trackId) {
        Logfn.d("Starting track with id " + trackId);

        mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_PLAY);
        mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_TRACK_ID, trackId);
        mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_PLAYLIST, mTracks);
        mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_ARTIST, mArtistName);
        mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_ARTIST_ID, mTrackListener.getArtistId());
        mPlayerServiceIntent.putExtra(MainActivity.KEY_QUERY, mTrackListener.getQuery());

        if (mTrackListener.isTwoPane()) {
            mPlayerServiceIntent.putExtra(MainActivityFragment.KEY_LIST_POSITION, mMainActivityControlListener.getArtistPosition());
        }

        mActivity.startService(mPlayerServiceIntent);

        // remove extra in case play is called again
        mPlayerServiceIntent.removeExtra(MediaPlayerService.KEY_TRACK_ID);
    }

    private void previous() {
        if (mBound) {
            sendMediaPlayerIntent(MediaPlayerService.ACTION_PREVIOUS);
        }
    }

    private void next() {
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
            Logfn.e("Invalid button tag: " + buttonTag);
            mActivity.finish();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        Logfn.d("Start");
        super.onAttach(activity);

        mActivity = activity;

        try {
            mTrackListener = (OnTrackSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTrackSelectedListener");
        }

        if (mTrackListener.isTwoPane()) {
            try {
                mMainActivityControlListener = (OnMainActivityControlListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnMainActivityControlListener");
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBound) {
            mActivity.unbindService(mConnection);
            Logfn.d("Unbound from Service");
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
}
