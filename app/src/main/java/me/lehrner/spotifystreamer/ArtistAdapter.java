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

class ArtistAdapter extends ArrayAdapter<SpotifyArtistSearchResult> {
    private final List<SpotifyArtistSearchResult> mArtistList;
    private final Context mContext;
    private final int mLayoutId;

    public ArtistAdapter(Context context, List<SpotifyArtistSearchResult> artistList) {
        super(context, R.layout.artist_item_layout, artistList);
        mArtistList = artistList;
        mContext = context;
        mLayoutId = R.layout.artist_item_layout;
    }

    @Override
    public void clear() {
        if (mArtistList != null) {
            mArtistList.clear();
        }
        super.clear();
        addAll(new ArrayList<SpotifyArtistSearchResult>());
    }

    @Override
    public int getCount() {
        return mArtistList.size();
    }
    @Override
    public SpotifyArtistSearchResult getItem(int pos) {
        return mArtistList.get(pos);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View artistView, ViewGroup parent) {

        ViewHolder viewHolder;

        // First let's verify the artistView is not null
        if (artistView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            artistView = inflater.inflate(mLayoutId, parent, false);

            viewHolder = new ViewHolder(artistView);
            artistView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) artistView.getTag();
        }

        SpotifyArtistSearchResult s = mArtistList.get(position);

        Glide.clear(viewHolder.artistImageView);

        if (s.getImageMedium().isEmpty()) {
            viewHolder.artistImageView.setImageResource(R.drawable.ic_mic_black_48dp);
            viewHolder.artistImageView.setContentDescription(mContext.getString(R.string.empty_image));
        }
        else {
            Glide.with(mContext)
                    .load(s.getImageMedium())
                    .placeholder(R.drawable.ic_mic_black_48dp)
                    .into(viewHolder.artistImageView);

            viewHolder.artistImageView.setContentDescription(mContext.getString(R.string.image_of_artist) + s.getArtistName());
        }

        viewHolder.artistNameView.setText(s.getArtistName());

        return artistView;
    }

    public static class ViewHolder {
        public final ImageView artistImageView;
        public final TextView artistNameView;

        public ViewHolder (View view) {
            artistImageView = (ImageView) view.findViewById(R.id.artist_image_view);
            artistNameView = (TextView) view.findViewById(R.id.artist_name_view);
        }
    }
}
