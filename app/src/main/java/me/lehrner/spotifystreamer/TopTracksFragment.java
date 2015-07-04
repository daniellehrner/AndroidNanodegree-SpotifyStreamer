package me.lehrner.spotifystreamer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;

public class TopTracksFragment extends Fragment {
    private static final String KEY_TRACK_LIST = "me.lehrner.spotifystreamer.tracks";
    private static final String KEY_LIST_VIEW = "me.lehrner.spotifystreamer.track.listview";
    private final static String KEY_ARTIST_ID = "me.lehrner.spotifystreamer.track.artistId";
    private final static String KEY_ARTIST_NAME = "me.lehrner.spotifystreamer.topTracks.ARTIST_NAME";

    public final static String ARRAY_ID = "me.lehrner.spotifystreamer.ARRAY_ID";
    public final static String ARTIST_ID = "me.lehrner.spotifystreamer.ARTIST_ID";
    public final static String ARTIST_NAME = "me.lehrner.spotifystreamer.ARTIST_NAME";
    public final static String TRACK_ARRAY = "me.lehrner.spotifystreamer.TRACK_ARRAY";

    private ListView mListView;
    private ArrayList<SpotifyTrackSearchResult> mTracks;
    private View mLoadingView, mRootView;
    private int mShortAnimationDuration;
    private Toast toast;
    private TrackAdapter mTrackAdapter;
    private Activity mActivity;
    private Context mContext;
    private String mArtistId, mQuery, mArtistName;

    public TopTracksFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        return mRootView;
    }

    @SuppressLint("ShowToast")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLoadingView = mRootView.findViewById(R.id.loading_spinner);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mContext = getActivity();
        toast = Toast.makeText(mContext, " ", Toast.LENGTH_SHORT);

        mTrackAdapter = new TrackAdapter(
                mContext,
                new ArrayList<SpotifyTrackSearchResult>());

        mListView = (ListView) mRootView.findViewById(R.id.listview_track_search_result);
        mListView.setAdapter(mTrackAdapter);

        if (savedInstanceState != null) {
            Log.d("TrackOnActivityCreated", "is a saved instance");

            mTracks = savedInstanceState.getParcelableArrayList(KEY_TRACK_LIST);
            mListView.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_VIEW));
            mArtistId = savedInstanceState.getString(KEY_ARTIST_ID);
            mArtistName = savedInstanceState.getString(KEY_ARTIST_NAME);
            mQuery = savedInstanceState.getString(MainActivity.KEY_QUERY);

            addAllAdapter(mTracks);
            fadeListViewIn();

        }
        else {
            Log.d("TrackOnActivityCreated", "is not  a saved instance");
            Intent intent = mActivity.getIntent();

            if ((intent != null) && (intent.getExtras() != null)) {
                handleIntent(intent);
            }
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long rowId) {
                SpotifyTrackSearchResult clickedItem = (SpotifyTrackSearchResult) adapter.getItemAtPosition(position);

                int arrayPos = mTracks.indexOf(clickedItem);

                if (arrayPos == -1) {
                    Log.e("TopTracks.click", "clicked item not found: " + clickedItem.toString());
                    mActivity.finish();
                }

                Intent playerIntent = new Intent(mContext, PlayerActivity.class);
                playerIntent.putExtra(ARTIST_NAME, mArtistName);
                playerIntent.putExtra(ARRAY_ID, arrayPos);
                playerIntent.putParcelableArrayListExtra(TRACK_ARRAY, mTracks);
                playerIntent.putExtra(ARTIST_ID, mArtistId);
                playerIntent.putExtra(MainActivity.KEY_QUERY, mQuery);
                startActivity(playerIntent);
            }
        });
    }

    public void updateTopTracks(String artistId, String artistName, Activity activity) {
        showLoadingView();

        mArtistId = artistId;
        mArtistName = artistName;
        mTrackAdapter.clear();

        mTrackAdapter = new TrackAdapter(mActivity, new ArrayList<SpotifyTrackSearchResult>());
        mListView.setAdapter(mTrackAdapter);

        SpotifyTrackSearch spotifySearch = new SpotifyTrackSearch();
        spotifySearch.updateListView(mArtistId, activity, this);

        setSubTitle(mArtistName);
    }

    public void setSubTitle(String subtitle) {
        try {
            //noinspection ConstantConditions
            ((AppCompatActivity) mActivity).getSupportActionBar().setSubtitle(subtitle);
        }
        catch (NullPointerException e) {
            Log.e("updateTopTracks", "Can't set subtitle");
        }
    }

    public void getSearchResult(ArrayList<SpotifyTrackSearchResult> searchResult) {
        if (searchResult.isEmpty()) {
            showToast(getString(R.string.no_track_found));

            if (mActivity.getClass().getSimpleName().equals("TopTracks")) {
                mActivity.finish();
            }
            else {
                hideListView();
            }
        }
        else {
            addAllAdapter(searchResult);
            fadeListViewIn();
        }
    }

    public void handleSearchError() {
        showToast(getString(R.string.connection_error));

        if (mActivity.getClass().getSimpleName().equals("MainActivity")) {
            hideListView();
        }
        else if (mActivity.getClass().getSimpleName().equals("TopTracks")) {
            mActivity.finish();
        }
        fadeListViewIn();
    }

    private void handleIntent(Intent intent) {
        updateTopTracks(intent.getStringExtra(MainActivityFragment.ARTIST_ID),
                intent.getStringExtra(MainActivityFragment.ARTIST_NAME),
                mActivity);

        mQuery = intent.getStringExtra(MainActivity.KEY_QUERY);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;
    }

    public void showToast(String message) {
        toast.setText(message);
        toast.show();
    }

    private void addAllAdapter(ArrayList<SpotifyTrackSearchResult> searchResult) {
        Log.d("TopTracks.addAllAdapter", "Start");
        mTracks = searchResult;
        mTrackAdapter.addAll(mTracks);
    }

    private void fadeListViewIn() {
        Log.d("TopTracks.fadeListView", "Start");
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

    private void showLoadingView() {
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.VISIBLE);
        }
    }

    public void hideListView() {
        if (mListView != null) {
            mListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_TRACK_LIST, mTracks);
        outState.putParcelable(KEY_LIST_VIEW, mListView.onSaveInstanceState());
        outState.putString(KEY_ARTIST_ID, mArtistId);
        outState.putString(KEY_ARTIST_NAME, mArtistName);
        outState.putString(MainActivity.KEY_QUERY, mQuery);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = SpotifyStreamerApplication.getRefWatcher(mActivity);
        refWatcher.watch(this);
    }
}
