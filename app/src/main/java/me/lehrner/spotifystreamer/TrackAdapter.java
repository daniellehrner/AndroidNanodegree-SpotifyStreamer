package me.lehrner.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

class TrackAdapter extends ArrayAdapter<SpotifyTrackSearchResult> {
    private final List<SpotifyTrackSearchResult> mTrackList;
    private final Context mContext;
    private final int mLayoutId;

    public TrackAdapter(Context context, List<SpotifyTrackSearchResult> artistList) {
        super(context, R.layout.top_tracks_item_layout, artistList);
        mTrackList = artistList;
        mContext = context;
        mLayoutId = R.layout.top_tracks_item_layout;
    }

    @Override
    public void clear() {
        super.clear();

        if (mTrackList != null) {
            mTrackList.clear();
        }

        addAll(new ArrayList<SpotifyTrackSearchResult>());
    }

    public View getView(int position, View trackView, ViewGroup parent) {

        ViewHolder viewHolder;

        // First let's verify the trackView is not null
        if (trackView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            trackView = inflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder(trackView);
            trackView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) trackView.getTag();
        }

        SpotifyTrackSearchResult t = mTrackList.get(position);

        Glide.clear(viewHolder.trackImageView);

        if (t.getImageUrlBig().isEmpty()) {
            viewHolder.trackImageView.setImageResource(R.drawable.ic_mic_black_48dp);
            viewHolder.trackImageView.setContentDescription(mContext.getString(R.string.empty_image));
        }
        else {
            Glide.with(mContext)
                    .load(t.getImageUrlMedium())
                    .placeholder(R.drawable.ic_mic_black_48dp)
                    .into(viewHolder.trackImageView);

            viewHolder.trackImageView.setContentDescription(mContext.getString(R.string.image_of_album) + t.getAlbumName());
        }

        viewHolder.trackSongView.setText(t.getTrackName());
        viewHolder.trackAlbumView.setText(t.getAlbumName());

        return trackView;
    }

    public static class ViewHolder {
        public final ImageView trackImageView;
        public final TextView trackSongView;
        public final TextView trackAlbumView;

        public ViewHolder (View view) {
            trackImageView = (ImageView) view.findViewById(R.id.top_track_image_view);
            trackSongView = (TextView) view.findViewById(R.id.top_track_song_view);
            trackAlbumView = (TextView) view.findViewById(R.id.top_track_album_view);
        }
    }
}
