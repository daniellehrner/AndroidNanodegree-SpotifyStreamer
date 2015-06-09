package me.lehrner.spotifystreamer;

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

    private Toast toast;
    private ArtistAdapter artistAdapter;

    private void updateArtistView(String artist) {
        SpotifySearch spotifySearch = new SpotifySearch();
        spotifySearch.updateListView(artist, this);
    }

    public void addAllAdapter(ArrayList<SpotifySearchResult> searchResult) {
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

        artistAdapter =
                new ArtistAdapter(
                        this, // The current context (this activity)
                        R.layout.artist_item_layout, // The name of the layout ID.
                        new ArrayList<SpotifySearchResult>());

        ListView listviewSearchResult = (ListView) findViewById(R.id.listview_search_result);
        listviewSearchResult.setAdapter(artistAdapter);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_text);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        listviewSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long rowId) {
                SpotifySearchResult clickedItem = (SpotifySearchResult) adapter.getItemAtPosition(position);

                showToast(clickedItem.getImageMedium());
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
            String query = intent.getStringExtra(SearchManager.QUERY);
            updateArtistView(query);
        }
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