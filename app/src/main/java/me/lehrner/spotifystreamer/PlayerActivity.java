package me.lehrner.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity implements PlayerActivityFragment.OnTrackSelectedListener {
    private static final String FRAGMENT = "me.lehrner.spotifystreamer.PlayerActivityFragment";
    private static final String PLAYER_FRAGMENT_TAG = "PLAYER_FRAGMENT_TAG";

    private static final String KEY_TRACKS = "me.lehrner.spotifystreamer.tracks";
    private static final String KEY_ARTIST = "me.lehrner.spotifystreamer.artist";
    private static final String KEY_ARRAY_ID = "me.lehrner.spotifystreamer.tag";
    private static final String KEY_ARTIST_ID = "me.lehrner.spotifystreamer.id";

    private String mArtistName, mArtistId, mQuery;
    private int mArrayId;
    private ArrayList<SpotifyTrackSearchResult> mTracks;
    private PlayerActivityFragment mFragment;
    private boolean mIsNotificationIntent = false;

    @SuppressWarnings("unused")
    public void buttonPrevNext(View view) {
        switch (view.getId()) {
            case R.id.player_image_next:
                Log.d("buttonPrevNext", "Button next");
                mFragment.next();
                break;
            case R.id.player_image_previous:
                Log.d("buttonPrevNext", "Button previous");
                mFragment.previous();
                break;
            default:
                Log.e("buttonPrevNext", "Invalid id: " + view.getId());
        }
    }

    @SuppressWarnings("unused")
    public void buttonPlay(View view) {
        mFragment.playPauseTrack((String) view.getTag());
    }

    public String getArtistName() {
        return mArtistName;
    }

    public boolean getIsNotificationIntent() {
        return mIsNotificationIntent;
    }

    public ArrayList<SpotifyTrackSearchResult> getTracks() {
        return mTracks;
    }

    public int getTrackId() {
        return mArrayId;
    }

    public String getArtistId() {
        return mArtistId;
    }

    public String getQuery() {
        return mQuery;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mFragment = (PlayerActivityFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT);
            mTracks = savedInstanceState.getParcelableArrayList(KEY_TRACKS);
            mArrayId = savedInstanceState.getInt(KEY_ARRAY_ID);
            mArtistName = savedInstanceState.getString(KEY_ARTIST);
            mArtistId = savedInstanceState.getString(KEY_ARTIST_ID);
            mQuery = savedInstanceState.getString(MainActivity.KEY_QUERY);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            PlayerActivityFragment newFragment = new PlayerActivityFragment();
            boolean twoPane = getResources().getBoolean(R.bool.two_pane);

            if (twoPane) {
                newFragment.show(fragmentManager, "dialog");
            }
            else {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(R.id.player_placeholder, newFragment, PLAYER_FRAGMENT_TAG)
                        .addToBackStack(null)
                        .commit();
            }

            handleIntent(getIntent());
        }
    }

    private void handleIntent(Intent intent) {
        Log.d("Player.handleIntent", "Action: " + intent.getAction());

        if (intent.getAction() != null && intent.getAction().equals(MediaPlayerService.ACTION_NOTIFICATION)) {
            Log.d("Player.handleIntent", "Intent from Notification");
            mIsNotificationIntent = true;
        }
        else {
            mIsNotificationIntent = false;
        }

        mArrayId = intent.getIntExtra(TopTracksFragment.ARRAY_ID, -1);

        mTracks =  intent.getParcelableArrayListExtra(TopTracksFragment.TRACK_ARRAY);
        mArtistName = intent.getStringExtra(TopTracksFragment.ARTIST_NAME);
        mArtistId = intent.getStringExtra(TopTracksFragment.ARTIST_ID);
        mQuery = intent.getStringExtra(MainActivity.KEY_QUERY);

        try {
            if (mTracks != null) {
                Log.d("Player.handleIntent", "Size tracks array: " + mTracks.size());
            } else {
                throw new Exception("mTracks is null");
            }

            if (mArrayId == -1) {
                throw new Exception("mArrayId is invalid");
            }
        }
        catch (Exception e) {
            Log.e("Player.handleIntent", e.getMessage());
            finish();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onAttachFragment (Fragment fragment) {
        mFragment = (PlayerActivityFragment) fragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("TopTracks.saveInstance", "Start");
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, FRAGMENT, mFragment);
        outState.putParcelableArrayList(KEY_TRACKS, mTracks);
        outState.putString(KEY_ARTIST, mArtistName);
        outState.putInt(KEY_ARRAY_ID, mArrayId);
        outState.putString(KEY_ARTIST_ID, mArtistId);
        outState.putString(MainActivity.KEY_QUERY, mQuery);
    }
}