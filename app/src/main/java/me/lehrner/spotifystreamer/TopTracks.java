package me.lehrner.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.bumptech.glide.Glide;

public class TopTracks extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String FRAGMENT = "me.lehrner.spotifystreamer.TopTracksFragment";

    private TopTracksFragment mFragment;

    public void onItemClick(AdapterView<?> adapter, View v, int position, long rowId) {
        SpotifyTrackSearchResult clickedItem = (SpotifyTrackSearchResult) adapter.getItemAtPosition(position);

        int trackId = mFragment.getTracks().indexOf(clickedItem);

        if (trackId == -1) {
            Log.e("TopTracks.click", "clicked item not found: " + clickedItem.toString());
            finish();
        }

        Intent playerIntent = new Intent(this, PlayerActivity.class);
        playerIntent.putExtra(TopTracksFragment.ARTIST_NAME, mFragment.getArtistName());
        playerIntent.putExtra(TopTracksFragment.ARRAY_ID, trackId);
        playerIntent.putParcelableArrayListExtra(TopTracksFragment.TRACK_ARRAY, mFragment.getTracks());
        playerIntent.putExtra(TopTracksFragment.ARTIST_ID, mFragment.getArtistId());
        playerIntent.putExtra(MainActivity.KEY_QUERY, mFragment.getQuery());
        startActivity(playerIntent);
    }

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
