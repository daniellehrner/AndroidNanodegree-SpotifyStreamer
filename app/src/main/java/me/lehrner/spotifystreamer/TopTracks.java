package me.lehrner.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bumptech.glide.Glide;

public class TopTracks extends AppCompatActivity {
    private static final String FRAGMENT = "me.lehrner.spotifystreamer.TopTracksFragment";
    public final static String KEY_ARTIST_NAME = "me.lehrner.spotifystreamer.topTracks.ARTISTNAME";
    public final static String KEY_ARTIST_ID = "me.lehrner.spotifystreamer.topTracks.ARTISTID";

    private TopTracksFragment mFragment;
    private String mArtistName;
    private String mArtistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TopTracks.onCreate", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mFragment = (TopTracksFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT);

            mArtistId = savedInstanceState.getString(KEY_ARTIST_ID);
            mArtistName = savedInstanceState.getString(KEY_ARTIST_NAME);
        }
        else {
            mFragment = (TopTracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
            handleIntent(getIntent());
        }

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            Log.d("TopTracks.onCreate", "subtitle: " + mArtistName);
            actionBar.setSubtitle(mArtistName);
        }
        else {
            Log.e("TopTracks.onCreate", "Can't set actionbar subtitle");
        }
    }

    public String getArtistName () {
        return mArtistName;
    }

    public String getArtistId() {
        return mArtistId;
    }

    private void handleIntent(Intent intent) {
        Log.d("handleIntent", "Action: " + intent.getAction());

        mArtistId = intent.getStringExtra(MainActivityFragment.ARTIST_ID);
        mArtistName = intent.getStringExtra(MainActivityFragment.ARTIST_NAME);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("TopTracks.saveInstance", "Start");
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, FRAGMENT, mFragment);
        outState.putString(KEY_ARTIST_NAME, mArtistName);
        outState.putString(KEY_ARTIST_ID, mArtistId);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d("TopTracks.onPause", "Start");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("TopTracks.onResume", "Start");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("TopTracks.onDestroy", "Start");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d("TopTracks.onStop", "Start");
    }

    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d("TopTracks.RestoreInstan", "Start: " + savedInstanceState.toString());
    }
}
