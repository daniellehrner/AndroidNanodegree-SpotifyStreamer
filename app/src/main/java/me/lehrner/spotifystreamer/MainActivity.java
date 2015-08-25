package me.lehrner.spotifystreamer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements  PlayerActivityFragment.OnTrackSelectedListener,
                                                                AdapterView.OnItemClickListener  {
    private static final String MAIN_FRAGMENT_NAME = "MainActivityFragment";
    private static final String TOP_TRACKS_FRAGMENT_NAME = "TopTracksFragment";
    public static final String KEY_QUERY = "me.lehrner.spotifystreamer.key.query";
    private static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";
    private MainActivityFragment mMainFragment;
    private TopTracksFragment mTopTracksFragment;
    private String mQuery, mArtistId, mArtistName;
    private int mTrackId, mArrayId;
    private boolean mIsNotificationIntent = false, mTwoPane;
    private ShareActionProvider mShareActionProvider;
    private ArrayList<SpotifyTrackSearchResult> mTracks;

    public boolean isTwoPane() {
        return mTwoPane;
    }

    public boolean isNotificationIntent() {
        return mIsNotificationIntent;
    }

    public TopTracksFragment getTopTracksFragment() {
        return mTopTracksFragment;
    }

    public boolean getIsNotificationIntent() {
        return mIsNotificationIntent;
    }

    public String getArtistName() {
        return mIsNotificationIntent ? mArtistName : mTopTracksFragment.getArtistName();
    }

    public ArrayList<SpotifyTrackSearchResult> getTracks() {
        return mIsNotificationIntent ? mTracks : mTopTracksFragment.getTracks();
    }

    public int getTrackId() {
        return mTrackId;
    }

    public String getArtistId() {
        return mIsNotificationIntent ? mArtistId : mTopTracksFragment.getArtistId();
    }

    public int getArrayId() {
        return mArrayId;
    }

    public int getArtistPosition() {
        return mMainFragment.getPosition();
    }

    public String getQuery() {
        return mQuery;
    }

    public void setShareIntentUrl(String url) {
        if (mShareActionProvider != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, url);

            mShareActionProvider.setShareIntent(intent);
        }
    }

    public void onItemClick(AdapterView<?> adapter, View v, int position, long rowId) {
        SpotifyTrackSearchResult clickedItem = (SpotifyTrackSearchResult) adapter.getItemAtPosition(position);

        mTrackId = getTracks().indexOf(clickedItem);

        FragmentManager fragmentManager = getSupportFragmentManager();
        PlayerActivityFragment newFragment = new PlayerActivityFragment();

        if (getTrackId() == -1) {
            Logfn.e("clicked item not found: " + clickedItem.toString());
            finish();
        }

        newFragment.show(fragmentManager, "dialog");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logfn.d("Start");

        setIntent(intent);
        handleIntent(getIntent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logfn.d("Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mMainFragment = (MainActivityFragment) getSupportFragmentManager().getFragment(savedInstanceState, MAIN_FRAGMENT_NAME);
            setIntent(new Intent(Intent.ACTION_MAIN));
            mQuery = savedInstanceState.getString(KEY_QUERY);

            if (mIsNotificationIntent) {
                mArtistName = savedInstanceState.getString(TopTracksFragment.ARTIST_NAME);
                mArtistId = savedInstanceState.getString(TopTracksFragment.ARTIST_ID);
                mArrayId = savedInstanceState.getInt(TopTracksFragment.ARRAY_ID);
                mTracks = savedInstanceState.getParcelableArrayList(TopTracksFragment.TRACK_ARRAY);
            }
        }

        mTwoPane = getResources().getBoolean(R.bool.two_pane);

        if (mTwoPane) {
            Logfn.d("Two-pane mode");

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_tracks_container, new TopTracksFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();

            }
        }
        else {
            Logfn.d("Single-pane mode");
        }

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_text);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        handleIntent(getIntent());
        if (mTwoPane) {
            try {
                //noinspection ConstantConditions
                getSupportActionBar().setSubtitle(" ");
            }
            catch (NullPointerException e) {
                Logfn.e("Can't set subtitle");
            }
        }
    }


    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Logfn.d("Action: " + intent.getAction());

        mIsNotificationIntent = false;

        if (action.equals(MediaPlayerService.ACTION_NOTIFICATION)) {
            Logfn.d("Intent from Notification");
            mIsNotificationIntent = true;

            mQuery = intent.getStringExtra(SearchManager.QUERY);

            if (mTwoPane) {
                mMainFragment.updateArtistView(mQuery);

                mArrayId = intent.getIntExtra(TopTracksFragment.ARRAY_ID, -1);
                mTracks =  intent.getParcelableArrayListExtra(TopTracksFragment.TRACK_ARRAY);
                mArtistName = intent.getStringExtra(TopTracksFragment.ARTIST_NAME);
                mArtistId = intent.getStringExtra(TopTracksFragment.ARTIST_ID);
                mMainFragment.setArtistPosition(intent.getIntExtra(MainActivityFragment.KEY_LIST_POSITION, -1));
            }
        }
        else if (action.equals(Intent.ACTION_SEARCH)) {
            mQuery = intent.getStringExtra(SearchManager.QUERY);
            mMainFragment.updateArtistView(mQuery);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        if (isTwoPane()) {
            getMenuInflater().inflate(R.menu.menu_player, menu);
            MenuItem item = menu.findItem(R.id.menu_track_share);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_clear_history:
                clearSearchSuggestions();
                mMainFragment.showToast("Cleared search suggestions");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearSearchSuggestions() {
        Logfn.d("clear search history");
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                ArtistSuggestionProvider.AUTHORITY, ArtistSuggestionProvider.MODE);
        suggestions.clearHistory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        getSupportFragmentManager().putFragment(outState, MAIN_FRAGMENT_NAME, mMainFragment);
        outState.putString(KEY_QUERY, mQuery);

        if (mIsNotificationIntent) {
            outState.putString(TopTracksFragment.ARTIST_NAME, mArtistName);
            outState.putString(TopTracksFragment.ARTIST_ID, mArtistId);
            outState.putInt(TopTracksFragment.ARRAY_ID, mArrayId);
            outState.putParcelableArrayList(TopTracksFragment.TRACK_ARRAY, mTracks);
        }
    }

    @Override
    public void onAttachFragment (Fragment fragment) {
       String fragmentName = fragment.getClass().getSimpleName();

        if (fragmentName.equals(MAIN_FRAGMENT_NAME)) {
            mMainFragment = (MainActivityFragment) fragment;
        }
        else if (fragmentName.equals(TOP_TRACKS_FRAGMENT_NAME)) {
            mTopTracksFragment = (TopTracksFragment) fragment;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }
}