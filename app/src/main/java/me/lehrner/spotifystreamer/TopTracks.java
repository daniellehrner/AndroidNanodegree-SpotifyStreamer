package me.lehrner.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


public class TopTracks extends AppCompatActivity {
    private static final String FRAGMENT = "me.lehrner.spotifystreamer.TopTracksFragment";
    private TopTracksFragment mFragment;

    private String mArtistName;
    private String mArtistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TopTracks.onCreate", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setSubtitle(mArtistName);
        }
        else {
            Log.e("TopTracks.onCreate", "Can't set actionbar subtitle");
        }

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mFragment = (TopTracksFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT);
        }
        else {
            mFragment = (TopTracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
        }

        handleIntent(getIntent());
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
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, FRAGMENT, mFragment);
    }
}
