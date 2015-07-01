package me.lehrner.spotifystreamer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment {
    private static final String KEY_ARTIST_LIST = "me.lehrner.spotifystreamer.artists";
    private static final String KEY_LIST_VIEW = "me.lehrner.spotifystreamer.artist.listview";
    public final static String ARTIST_NAME = "me.lehrner.spotifystreamer.ARTIST_NAME";
    public final static String ARTIST_ID = "me.lehrner.spotifystreamer.ARTIST_ID";
    private final static String KEY_LAST_ARTIST = "me.lehrner.spotifystreamer.lastArtist";

    private ArtistAdapter mAdapter;
    private ArrayList<SpotifyArtistSearchResult> mArtists;
    private ListView mListView;
    private SearchView mSearchView;
    private View mLoadingView, mRootView;
    private int mShortAnimationDuration;
    private Toast toast;
    private Context mContext;
    private MainActivity mActivity;
    private String mLastArtist;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_search, container, false);
        return mRootView;
    }

    @SuppressLint("ShowToast")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("Main.onActivityCreated", "Start");
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();
        toast = Toast.makeText(mContext, " ", Toast.LENGTH_SHORT);

        mListView = (ListView) mRootView.findViewById(R.id.listview_search_result);
        mSearchView = (SearchView) mRootView.findViewById(R.id.search_text);
        mLoadingView = mRootView.findViewById(R.id.loading_spinner_artist);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mAdapter = new ArtistAdapter(
                        mContext,
                        new ArrayList<SpotifyArtistSearchResult>());

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long rowId) {
                SpotifyArtistSearchResult clickedItem = (SpotifyArtistSearchResult) adapter.getItemAtPosition(position);

                Intent topTracksIntent = new Intent(mContext, TopTracks.class);
                topTracksIntent.putExtra(ARTIST_NAME, clickedItem.getArtistName());
                topTracksIntent.putExtra(ARTIST_ID, clickedItem.getArtistId());
                topTracksIntent.putExtra(MainActivity.KEY_QUERY, mActivity.getQuery());
                startActivity(topTracksIntent);
            }
        });

        ArrayList<SpotifyArtistSearchResult> artistsListTemp;

        if(savedInstanceState != null) {
            // read the artist list from the saved state
            artistsListTemp = savedInstanceState.getParcelableArrayList(KEY_ARTIST_LIST);
            mListView.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_VIEW));
            mLastArtist = savedInstanceState.getString(KEY_LAST_ARTIST);
        }
        else {
            artistsListTemp = new ArrayList<>();
        }

        addAllAdapter(artistsListTemp);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
    }

    public void addAllAdapter(ArrayList<SpotifyArtistSearchResult> searchResult) {
        mArtists = searchResult;
        mAdapter.addAll(searchResult);
    }

    public void saveLastSearchQuery () {
        Log.d("saveLastSearchQuery", "saving: " + mLastArtist);
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(mContext,
                ArtistSuggestionProvider.AUTHORITY, ArtistSuggestionProvider.MODE);
        suggestions.saveRecentQuery(mLastArtist, null);
    }

    public void updateArtistView(String artist) {
        Log.d("updateArtistView", "Start");
        if (mLastArtist != null && mLastArtist.equals(artist)) {
            showToast(getString(R.string.same_artist));
            return;
        }

        if (mSearchView.getQuery().length() == 0) {
            mSearchView.setQuery(artist, false);
            mSearchView.clearFocus();
        }

        mLastArtist = artist;

        fadeListViewOut();

        if (!mAdapter.isEmpty()) {
            mAdapter.clear();
            mListView.setAdapter(mAdapter);
            mArtists.clear();
        }

        SpotifyArtistSearch spotifySearch = new SpotifyArtistSearch();
        spotifySearch.updateListView(artist, mActivity, this);
    }

    public void showToast(String message) {
        toast.setText(message);
        toast.show();
    }

    public void fadeListViewIn() {
        if (mListView != null) {
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
        }

        if (mLoadingView != null) {
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

    private void fadeListViewOut() {
        if (mListView != null)
            mListView.setVisibility(View.GONE);

        if (mLoadingView != null) {
            mLoadingView.setAlpha(1f);
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_ARTIST_LIST, mArtists);
        outState.putParcelable(KEY_LIST_VIEW, mListView.onSaveInstanceState());

        if (mLastArtist != null) {
            outState.putString(KEY_LAST_ARTIST, mLastArtist);
        }
        else {
            outState.putString(KEY_LAST_ARTIST, " ");
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = SpotifyStreamerApplication.getRefWatcher(mActivity);
        refWatcher.watch(this);
    }
}
