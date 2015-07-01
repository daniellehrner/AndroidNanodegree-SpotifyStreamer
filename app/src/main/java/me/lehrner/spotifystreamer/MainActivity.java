package me.lehrner.spotifystreamer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v7.widget.SearchView;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {
    private static final String FRAGMENT = "me.lehrner.spotifystreamer.MainActivityFragment";
    public static final String KEY_QUERY = "me.lehrner.spotifystreamer.key.query";
    private MainActivityFragment mFragment;

    public String getQuery() {
        return mQuery;
    }

    private String mQuery;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("Main.onNewIntent", "Start");

        setIntent(intent);
        handleIntent(getIntent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Main.onCreate", "Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mFragment = (MainActivityFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT);
            setIntent(new Intent(Intent.ACTION_MAIN));
            mQuery = savedInstanceState.getString(KEY_QUERY);
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_text);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleIntent(getIntent());
    }


    private void handleIntent(Intent intent) {
        Log.d("handleIntent", "Action: " + intent.getAction());

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQuery = intent.getStringExtra(SearchManager.QUERY);
            mFragment.updateArtistView(mQuery);
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
            case R.id.action_clear_history:
                clearSearchSuggestions();
                mFragment.showToast("Cleared search suggestions");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearSearchSuggestions() {
        Log.d("clearSearchSuggestions", "clear search history");
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                ArtistSuggestionProvider.AUTHORITY, ArtistSuggestionProvider.MODE);
        suggestions.clearHistory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, FRAGMENT, mFragment);
        outState.putString(KEY_QUERY, mQuery);
    }

    @Override
    public void onAttachFragment (Fragment fragment) {
       String fragmentName = fragment.getClass().getName();

        if (fragmentName.equals(FRAGMENT)) {
            mFragment = (MainActivityFragment) fragment;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }
}