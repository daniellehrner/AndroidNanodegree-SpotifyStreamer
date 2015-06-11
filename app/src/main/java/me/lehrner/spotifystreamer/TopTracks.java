package me.lehrner.spotifystreamer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class TopTracks extends AppCompatActivity {
    private String mArtistId;
    private String mArtistName;
    private ListView mListView;
    private View mLoadingView;
    private int mShortAnimationDuration;

    private Toast toast;
    private TrackAdapter trackAdapter;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TopTracks.onCreate", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        mListView = (ListView) findViewById(R.id.listview_track_search_result);
        mLoadingView = findViewById(R.id.loading_spinner);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        handleIntent(getIntent());

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setSubtitle(mArtistName);
        }
        else {
            Log.e("TopTracks.onCreate", "Can't set actionbar subtitle");
        }

        Context context = getApplicationContext();
        toast = Toast.makeText(context, " ", Toast.LENGTH_SHORT);

        trackAdapter =
                new TrackAdapter(
                        this, // The current context (this activity)
                        R.layout.top_tracks_item_layout, // The name of the layout ID.
                        new ArrayList<SpotifyTrackSearchResult>());

        mListView.setAdapter(trackAdapter);

        SpotifyTrackSearch spotifySearch = new SpotifyTrackSearch();
        spotifySearch.updateListView(mArtistId, this);
    }

    public void showToast(String message) {
        toast.setText(message);
        toast.show();
    }

    private void handleIntent(Intent intent) {
        Log.d("handleIntent", "Action: " + intent.getAction());

        mArtistId = intent.getStringExtra(MainActivityFragment.ARTIST_ID);
        mArtistName = intent.getStringExtra(MainActivityFragment.ARTIST_NAME);
    }

    public void addAllAdapter(ArrayList<SpotifyTrackSearchResult> searchResult) {
        trackAdapter.addAll(searchResult);
    }

    public void fadeListViewIn() {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        mListView.setAlpha(0f);
        mListView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        mListView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        mLoadingView.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoadingView.setVisibility(View.GONE);
                    }
                });
    }
}
