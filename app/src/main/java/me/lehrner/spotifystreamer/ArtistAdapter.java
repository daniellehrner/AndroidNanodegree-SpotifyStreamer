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

class ArtistAdapter extends ArrayAdapter<SpotifySearchResult> {
    private final List<SpotifySearchResult> mArtistList;
    private final Context mContext;
    private final int mLayoutId;

    public ArtistAdapter(Context context, int LayoutId, List<SpotifySearchResult> artistList) {
        super(context,LayoutId, artistList);
        mArtistList = artistList;
        mContext = context;
        mLayoutId = LayoutId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mLayoutId, parent, false);
        }
        // Now we can fill the layout with the right values
        ImageView artistImageView = (ImageView) convertView.findViewById(R.id.artist_image_view);
        TextView artistNameView = (TextView) convertView.findViewById(R.id.artist_name_view);
        SpotifySearchResult s = mArtistList.get(position);

        Picasso.with(mContext).cancelRequest(artistImageView);

        if (s.getImageMedium().isEmpty()) {
            artistImageView.setImageResource(R.mipmap.ic_mic_black_48dp);
            artistImageView.setContentDescription(mContext.getString(R.string.empty_image));
        }
        else {
            Picasso.with(mContext).load(s.getImageMedium()).into(artistImageView);
            artistImageView.setContentDescription(mContext.getString(R.string.album_of) + s.getArtistName());
        }
        artistNameView.setText(s.getArtistName());


        return convertView;
    }
}
