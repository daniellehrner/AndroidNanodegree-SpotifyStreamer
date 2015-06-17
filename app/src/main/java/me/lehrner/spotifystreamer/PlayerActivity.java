package me.lehrner.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bumptech.glide.Glide;

public class PlayerActivity extends AppCompatActivity {
    private String mArtistName;
    private String mAlbumName;
    private String mTrackName;
    private String mTrackUrl;
    private String mAlbumImageUrl;

    public String getAlbumImageUrl() {
        return mAlbumImageUrl;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public String getTrackUrl() {
        return mTrackUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        Log.d("handleIntent", "Action: " + intent.getAction());

        mArtistName = intent.getStringExtra(TopTracksFragment.ARTIST_NAME);
        mAlbumName = intent.getStringExtra(TopTracksFragment.ALBUM_NAME);
        mTrackName = intent.getStringExtra(TopTracksFragment.TRACK_NAME);
        mTrackUrl = intent.getStringExtra(TopTracksFragment.TRACK_URL);
        mAlbumImageUrl = intent.getStringExtra(TopTracksFragment.ALBUM_IMAGE);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }
}