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

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Toast toast;
    public ArrayAdapter<SpotifySearchResult> artistAdapter;

    private void getArtist(String artist) {
        Log.d("getArtist", "Artist: " + artist);

        SpotifySearch spotifySearch = new SpotifySearch();

        try {
            spotifySearch.updateListView(artist, this);
        }
        catch (IllegalStateException e) {
            showToast(getString(R.string.connection_error));
            Log.e("getArtist", "spotifySearch.execute: " + e.toString());
        }
    }

    private void showToast(String message) {
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
                new ArrayAdapter<>(
                        this, // The current context (this activity)
                        R.layout.list_item_layout, // The name of the layout ID.
                        R.id.list_item_search_textview, // The ID of the textview to populate.
                        new ArrayList<SpotifySearchResult>());

        ListView listView = (ListView) findViewById(R.id.listview_search_result);
        listView.setAdapter(artistAdapter);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_text);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Log.d("handleIntent", "Action: " + intent.getAction());

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            if (!artistAdapter.isEmpty()) {
                artistAdapter.clear();
            }
            String query = intent.getStringExtra(SearchManager.QUERY);
            getArtist(query);

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