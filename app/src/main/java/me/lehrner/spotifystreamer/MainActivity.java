package me.lehrner.spotifystreamer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.SearchView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private String getArtist(String artist) {
        Log.d("getArtist", "Artist: " + artist);

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, artist, duration);
        toast.show();

        return artist;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("onNewIntent", "Start");
        setIntent(intent);
        handleIntent(getIntent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("onCreate", "Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_text);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Log.d("handleIntent", "Action: " + intent.getAction());

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
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