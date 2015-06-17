package me.lehrner.spotifystreamer;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class PlayerActivityFragment extends Fragment {
    private PlayerActivity mActivity;
    private TextView mArtistNameView, mAlbumNameView, mTrackNameView;
    private ImageView mPlayerImage;

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

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String albumName = mActivity.getAlbumName();
        String albumImageUrl = mActivity.getAlbumImageUrl();

        mArtistNameView.setText(mActivity.getArtistName());
        mAlbumNameView.setText(albumName);
        mTrackNameView.setText(mActivity.getTrackName());

        if (!albumImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(albumImageUrl)
                    .placeholder(R.drawable.vinyl)
                    .into(mPlayerImage);

            mPlayerImage.setContentDescription(getString(R.string.image_of_artist) + albumName);
        }
        else {
            mPlayerImage.setImageResource(R.drawable.vinyl);
            mPlayerImage.setContentDescription(getString(R.string.empty_image));
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (PlayerActivity) activity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        outState.putParcelableArrayList(KEY_TRACK_LIST, mTracks);
//        outState.putParcelable(KEY_LIST_VIEW, mListView.onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }
}
