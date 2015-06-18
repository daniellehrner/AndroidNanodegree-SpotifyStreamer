package me.lehrner.spotifystreamer;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


public class PlayerActivityFragment extends Fragment {
    private static final String TAG_PLAY = "play";
    private static final String TAG_PAUSE = "pause";

    private PlayerActivity mActivity;
    private TextView mArtistNameView, mAlbumNameView, mTrackNameView;
    private ImageView mPlayerImage;
    private SpotifyTrackSearchResult mTrack;
    private ImageButton mPlayButton;
    private MediaPlayerService mPlayerService;
    private Intent mPlayerServiceIntent;

    public PlayerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_player, container, false);

        mArtistNameView = (TextView) mRootView.findViewById(R.id.player_artist_name);
        mAlbumNameView = (TextView) mRootView.findViewById(R.id.player_album_name);
        mTrackNameView = (TextView) mRootView.findViewById(R.id.player_track_name);
        mPlayerImage = (ImageView) mRootView.findViewById(R.id.player_image_view);

        mPlayButton = (ImageButton) mRootView.findViewById(R.id.player_image_play);
        mPlayButton.setTag(TAG_PLAY);

        mPlayerService = new MediaPlayerService();
        mPlayerServiceIntent = new Intent(mActivity, MediaPlayerService.class);

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setTrack(String artistName, SpotifyTrackSearchResult track) {
        mTrack = track;

        String albumName = track.getAlbumName();

        mArtistNameView.setText(artistName);
        mAlbumNameView.setText(albumName);
        mTrackNameView.setText(track.getTrackName());

        String albumImageUrl = track.getImageUrlBig();

        if (!albumImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(albumImageUrl)
                    .placeholder(R.drawable.vinyl)
                    .into(mPlayerImage);

            mPlayerImage.setContentDescription(getString(R.string.image_of_artist) + albumName);
        }

        playTrack(track.getTrackUrl());
    }

    public void playPauseTrack() {
        String buttonTag = (String) mPlayButton.getTag();

        switch (buttonTag) {
            case TAG_PLAY:
                mPlayButton.setImageResource(R.drawable.ic_pause_black_48dp);
                mPlayButton.setTag(TAG_PAUSE);
                mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_PAUSE);
                break;
            case TAG_PAUSE:
                mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                mPlayButton.setTag(TAG_PLAY);
                mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_PLAY);
                mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_TRACK_URL, mTrack.getTrackUrl());
                break;
            default:
                Log.e("switchPlayButtonSymbol", "Invalid button tag: " + buttonTag);
                mActivity.finish();
        }

        mActivity.startService(mPlayerServiceIntent);
    }

    private void playTrack(String trackUrl) {
        if (!mPlayerService.isPlaying()) {
            switchPlayButtonSymbol();
        }

        Log.d("playTrack", "Starting track " + trackUrl);

        mPlayerServiceIntent.setAction(MediaPlayerService.ACTION_PLAY);
        mPlayerServiceIntent.putExtra(MediaPlayerService.KEY_TRACK_URL, trackUrl);

        mActivity.startService(mPlayerServiceIntent);
    }


    private void switchPlayButtonSymbol() {
        String buttonTag = (String) mPlayButton.getTag();

        switch (buttonTag) {
            case TAG_PLAY:
                mPlayButton.setImageResource(R.drawable.ic_pause_black_48dp);
                mPlayButton.setTag(TAG_PAUSE);
                break;
            case TAG_PAUSE:
                mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                mPlayButton.setTag(TAG_PLAY);
                break;
            default:
                Log.e("switchPlayButtonSymbol", "Invalid button tag: " + buttonTag);
                mActivity.finish();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (PlayerActivity) activity;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mPlayerService != null && mPlayerServiceIntent != null) {
            mActivity.stopService(mPlayerServiceIntent);
            mPlayerService = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        outState.putParcelableArrayList(KEY_TRACK_LIST, mTracks);
//        outState.putParcelable(KEY_LIST_VIEW, mListView.onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }
}
