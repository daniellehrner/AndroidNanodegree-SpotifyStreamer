package me.lehrner.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bumptech.glide.Glide;

public class TopTracks extends AppCompatActivity {
    private static final String FRAGMENT = "me.lehrner.spotifystreamer.TopTracksFragment";

    private TopTracksFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TopTracks.onCreate", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mFragment = (TopTracksFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT);
        }
        else {
            mFragment = (TopTracksFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_tracks);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("TopTracks.saveInstance", "Start");
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, FRAGMENT, mFragment);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }
}
