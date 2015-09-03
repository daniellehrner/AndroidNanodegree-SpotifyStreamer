package me.lehrner.spotifystreamer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment {
    private static final String KEY_ARTIST_LIST = "me.lehrner.spotifystreamer.artists";
    private static final String KEY_LIST_VIEW = "me.lehrner.spotifystreamer.artist.listview";
    public final static String ARTIST_NAME = "me.lehrner.spotifystreamer.ARTIST_NAME";
    public final static String ARTIST_ID = "me.lehrner.spotifystreamer.ARTIST_ID";
    private final static String KEY_LAST_ARTIST = "me.lehrner.spotifystreamer.lastArtist";
    public final static String KEY_LIST_POSITION = "me.lehrner.spotifystreamer.position";

    private ArtistAdapter mAdapter;
    private ArrayList<SpotifyArtistSearchResult> mArtists;
    private ListView mListView;
    private SearchView mSearchView;
    private View mLoadingView, mRootView;
    private int mShortAnimationDuration, mPosition;
    private Toast toast;
    private Context mContext;
    private MainActivity mActivity;
    private String mLastArtist, mCurrentArtist;
    private boolean mIsNotificationIntent = false;

    public MainActivityFragment() {
    }

    public int getPosition() {
        return mPosition;
    }

    public void setNotificationIntent(boolean b) {
        mIsNotificationIntent = b;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_search, container, false);
        return mRootView;
    }

    @Override
    public void onResume() {
        Logfn.d("Start");
        super.onResume();

        // avoid @string/same_artist toast after resume
        mLastArtist = null;
    }

    @SuppressLint("ShowToast")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Logfn.d("Start");
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

                if (mActivity.isTwoPane()) {
                    mActivity.getTopTracksFragment().updateTopTracks(clickedItem.getArtistId(),
                            clickedItem.getArtistName(),
                            mActivity);

                    mPosition = position;
                }
                else {
                    Intent topTracksIntent = new Intent(mContext, TopTracks.class);
                    topTracksIntent.putExtra(ARTIST_NAME, clickedItem.getArtistName());
                    topTracksIntent.putExtra(ARTIST_ID, clickedItem.getArtistId());
                    topTracksIntent.putExtra(MainActivity.KEY_QUERY, mActivity.getQuery());
                    startActivity(topTracksIntent);
                }
            }
        });

        ArrayList<SpotifyArtistSearchResult> artistsListTemp;

        if(savedInstanceState != null) {
            // read the artist list from the saved state
            artistsListTemp = savedInstanceState.getParcelableArrayList(KEY_ARTIST_LIST);
            mListView.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_VIEW));
            mLastArtist = savedInstanceState.getString(KEY_LAST_ARTIST);

            // not needed on phones
            if (mActivity.isTwoPane()) {
                mPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
                mListView.smoothScrollToPosition(mPosition);
            }
        }
        else {
            artistsListTemp = new ArrayList<>();
        }

        addAllAdapter(artistsListTemp);
    }

    public void handleConnectionError() {
        showToast(mActivity.getString(R.string.connection_error));
        fadeListViewIn();
    }

    public void handleSearchResult(ArrayList<SpotifyArtistSearchResult> searchResult) {
        if (searchResult.isEmpty()) {
            showToast(getString(R.string.no_artist_found));
        }
        else {
            addAllAdapter(searchResult);
            saveLastSearchQuery();
            if (mActivity.isTwoPane()) {
                mActivity.getTopTracksFragment().hideListView();
            }
        }
        fadeListViewIn();

        if (mActivity.isTwoPane() && mIsNotificationIntent) {
            Logfn.d("set artist selection to " + mPosition);
            mListView.setItemChecked(mPosition, true);
            mActivity.getTopTracksFragment().getSearchResult(mActivity.getTracks());

            mIsNotificationIntent = false;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
    }

    private void addAllAdapter(ArrayList<SpotifyArtistSearchResult> searchResult) {
        mArtists = searchResult;
        mAdapter.addAll(searchResult);
    }

    private void saveLastSearchQuery() {
        // save last artist only when search was successful
        mLastArtist = mCurrentArtist;

        Logfn.d("saving: " + mLastArtist);
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(mContext,
                ArtistSuggestionProvider.AUTHORITY, ArtistSuggestionProvider.MODE);
        suggestions.saveRecentQuery(mLastArtist.trim().toLowerCase(), null);
    }

    public void updateArtistView(String artist) {
        Logfn.d("Start");
        if (mLastArtist != null && mLastArtist.equals(artist)) {
            showToast(getString(R.string.same_artist));
            return;
        }

        if (!mSearchView.getQuery().equals(artist)) {
            mSearchView.setQuery(artist, false);
            mSearchView.clearFocus();
        }

        // save artist temporarily
        mCurrentArtist = artist;

        fadeListViewOut();

        if (!mAdapter.isEmpty()) {
            mAdapter.clear();
            mListView.setAdapter(mAdapter);
            mArtists.clear();
        }

        if (mActivity.isTwoPane()) {
            mActivity.getTopTracksFragment().setSubTitle(" ");
        }

        SpotifyArtistSearch spotifySearch = new SpotifyArtistSearch();
        spotifySearch.updateListView(artist, mActivity, this);
    }

    public void showToast(String message) {
        toast.setText(message);
        toast.show();
    }

    private void fadeListViewIn() {
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
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_ARTIST_LIST, mArtists);
        outState.putParcelable(KEY_LIST_VIEW, mListView.onSaveInstanceState());

        if (mLastArtist != null) {
            outState.putString(KEY_LAST_ARTIST, mLastArtist);
        }
        else {
            outState.putString(KEY_LAST_ARTIST, " ");
        }

        if (mActivity.isTwoPane()) {
            outState.putInt(KEY_LIST_POSITION, mPosition);
        }
    }

    public void setArtistPosition(int artistPosition) {
        mPosition = artistPosition;
    }
}
