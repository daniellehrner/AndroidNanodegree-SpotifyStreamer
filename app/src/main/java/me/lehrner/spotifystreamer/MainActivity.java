package me.lehrner.spotifystreamer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public final static String ARTIST_NAME = "me.lehrner.spotifystreamer.ARTISTNAME";
    public final static String ARTIST_ID = "me.lehrner.spotifystreamer.ARTISTID";

    private Toast toast;
    private ArtistAdapter artistAdapter;
    private ListView mListView;
    private View mLoadingView;
    private int mShortAnimationDuration;

    private void updateArtistView(String artist) {
        SpotifyArtistSearch spotifySearch = new SpotifyArtistSearch();
        spotifySearch.updateListView(artist, this);
    }

    public void addAllAdapter(ArrayList<SpotifyArtistSearchResult> searchResult) {
        artistAdapter.addAll(searchResult);
    }

    public void showToast(String message) {
        toast.setText(message);
        toast.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("onNewIntent", "Start");

        setIntent(intent);
        handleIntent(getIntent());
    }

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "Start");

        Context context = getApplicationContext();
        toast = Toast.makeText(context, " ", Toast.LENGTH_SHORT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mListView = (ListView) findViewById(R.id.listview_search_result);
        mLoadingView = findViewById(R.id.loading_spinner_artist);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        artistAdapter =
                new ArtistAdapter(
                        this, // The current context (this activity)
                        R.layout.artist_item_layout, // The name of the layout ID.
                        new ArrayList<SpotifyArtistSearchResult>());

        mListView.setAdapter(artistAdapter);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_text);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long rowId) {
                SpotifyArtistSearchResult clickedItem = (SpotifyArtistSearchResult) adapter.getItemAtPosition(position);

                Intent topTracksIntent = new Intent(getApplicationContext(), TopTracks.class);
                topTracksIntent.putExtra(ARTIST_NAME, clickedItem.getArtistName());
                topTracksIntent.putExtra(ARTIST_ID, clickedItem.getArtistId());
                startActivity(topTracksIntent);

//                showToast(clickedItem.getImageMedium());
            }
        });

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Log.d("handleIntent", "Action: " + intent.getAction());


        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            if (!artistAdapter.isEmpty()) {
                artistAdapter.clear();
            }

            mListView.setVisibility(View.GONE);
            mLoadingView.setAlpha(1f);
            mLoadingView.setVisibility(View.VISIBLE);

            String query = intent.getStringExtra(SearchManager.QUERY);
            updateArtistView(query);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}