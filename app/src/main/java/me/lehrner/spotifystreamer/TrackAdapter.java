package me.lehrner.spotifystreamer;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

class TrackAdapter extends ArrayAdapter<SpotifyTrackSearchResult> {
    private final List<SpotifyTrackSearchResult> mTrackList;
    private final Context mContext;
    private final int mLayoutId;

    public TrackAdapter(Context context, int LayoutId, List<SpotifyTrackSearchResult> artistList) {
        super(context,LayoutId, artistList);
        mTrackList = artistList;
        mContext = context;
        mLayoutId = LayoutId;
    }

    public View getView(int position, View artistView, ViewGroup parent) {

        // First let's verify the artistView is not null
        if (artistView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            artistView = inflater.inflate(mLayoutId, parent, false);
        }
        // Now we can fill the layout with the right values
        ImageView trackImageView = (ImageView) artistView.findViewById(R.id.top_track_image_view);
        TextView trackSongView = (TextView) artistView.findViewById(R.id.top_track_song_view);
        TextView trackAlbumView = (TextView) artistView.findViewById(R.id.top_track_album_view);


        SpotifyTrackSearchResult t = mTrackList.get(position);

        Picasso.with(mContext).cancelRequest(trackImageView);

        if (t.getImageUrlBig().isEmpty()) {
            trackImageView.setImageResource(R.mipmap.ic_mic_black_48dp);
            trackImageView.setContentDescription(mContext.getString(R.string.empty_image));
        }
        else {
            Picasso.with(mContext).load(t.getImageUrlMedium()).placeholder(R.mipmap.ic_mic_black_48dp).into(trackImageView);
            trackImageView.setContentDescription(mContext.getString(R.string.image_of_album) + t.getAlbumName());
        }
        trackSongView.setText(t.getTrackName());
        trackAlbumView.setText(t.getAlbumName());

        return artistView;
    }
}

