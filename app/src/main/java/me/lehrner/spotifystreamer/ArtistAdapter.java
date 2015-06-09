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

class ArtistAdapter extends ArrayAdapter<SpotifyArtistSearchResult> {
    private final List<SpotifyArtistSearchResult> mArtistList;
    private final Context mContext;
    private final int mLayoutId;

    public ArtistAdapter(Context context, int LayoutId, List<SpotifyArtistSearchResult> artistList) {
        super(context,LayoutId, artistList);
        mArtistList = artistList;
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
        ImageView artistImageView = (ImageView) artistView.findViewById(R.id.artist_image_view);
        TextView artistNameView = (TextView) artistView.findViewById(R.id.artist_name_view);
        SpotifyArtistSearchResult s = mArtistList.get(position);

        Picasso.with(mContext).cancelRequest(artistImageView);

        if (s.getImageMedium().isEmpty()) {
            artistImageView.setImageResource(R.mipmap.ic_mic_black_48dp);
            artistImageView.setContentDescription(mContext.getString(R.string.empty_image));
        }
        else {
            Picasso.with(mContext).load(s.getImageMedium()).placeholder(R.mipmap.ic_mic_black_48dp).into(artistImageView);
            artistImageView.setContentDescription(mContext.getString(R.string.image_of_artist) + s.getArtistName());
        }
        artistNameView.setText(s.getArtistName());


        return artistView;
    }
}
