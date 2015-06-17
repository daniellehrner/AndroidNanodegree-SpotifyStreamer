package me.lehrner.spotifystreamer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class TopTracksFragment extends Fragment {
    private static final String KEY_TRACK_LIST = "me.lehrner.spotifystreamer.tracks";
    private static final String KEY_LIST_VIEW = "me.lehrner.spotifystreamer.track.listview";
    private final static String KEY_ARTIST_ID = "me.lehrner.spotifystreamer.track.artistId";

    public final static String ARTIST_NAME = "me.lehrner.spotifystreamer.ARTISTNAME";
    public final static String ALBUM_NAME = "me.lehrner.spotifystreamer.ALBUMNAME";
    public final static String TRACK_NAME = "me.lehrner.spotifystreamer.TRACKNAME";
    public final static String TRACK_URL = "me.lehrner.spotifystreamer.TRACKURL";
    public final static String ALBUM_IMAGE = "me.lehrner.spotifystreamer.ALBUMIMAGE";

    private ListView mListView;
    private ArrayList<SpotifyTrackSearchResult> mTracks;
    private View mLoadingView, mRootView;
    private int mShortAnimationDuration;
    private Toast toast;
    private TrackAdapter mTrackAdapter;
    private TopTracks mActivity;
    private Context mContext;
    private String mArtistId;

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

        mTrackAdapter =
                new TrackAdapter(
                        mContext, // The current context (this activity)
                        R.layout.top_tracks_item_layout, // The name of the layout ID.
                        new ArrayList<SpotifyTrackSearchResult>());

        mListView = (ListView) mRootView.findViewById(R.id.listview_track_search_result);

        mListView.setAdapter(mTrackAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long rowId) {
                SpotifyTrackSearchResult clickedItem = (SpotifyTrackSearchResult) adapter.getItemAtPosition(position);

                Intent playerIntent = new Intent(mContext, PlayerActivity.class);
                playerIntent.putExtra(ARTIST_NAME, mActivity.getArtistName());
                playerIntent.putExtra(ALBUM_NAME, clickedItem.getAlbumName());
                playerIntent.putExtra(TRACK_NAME, clickedItem.getTrackName());
                playerIntent.putExtra(TRACK_URL, clickedItem.getTrackUrl());
                playerIntent.putExtra(ALBUM_IMAGE, clickedItem.getImageUrlBig());
                startActivity(playerIntent);
            }
        });

        if (savedInstanceState != null) {
            Log.d("TrackOnActivityCreated", "is a saved instance");

            mListView.onRestoreInstanceState(savedInstanceState.getParcelable(KEY_LIST_VIEW));
            mTracks = savedInstanceState.getParcelableArrayList(KEY_TRACK_LIST);
            mArtistId = savedInstanceState.getString(KEY_ARTIST_ID);

            addAllAdapter(mTracks);
            fadeListViewIn();

        }
        else {
            Log.d("TrackOnActivityCreated", "is not  a saved instance");
            mArtistId = mActivity.getArtistId();
            SpotifyTrackSearch spotifySearch = new SpotifyTrackSearch();
            spotifySearch.updateListView(mArtistId, mActivity, this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (TopTracks) activity;
    }

    public void showToast(String message) {
        toast.setText(message);
        toast.show();
    }

    public void addAllAdapter(ArrayList<SpotifyTrackSearchResult> searchResult) {
        mTracks = searchResult;
        mTrackAdapter.addAll(mTracks);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_TRACK_LIST, mTracks);
        outState.putParcelable(KEY_LIST_VIEW, mListView.onSaveInstanceState());
        outState.putString(KEY_ARTIST_ID, mArtistId);
    }
}
