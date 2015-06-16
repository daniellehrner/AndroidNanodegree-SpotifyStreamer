package me.lehrner.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class PlayerActivity extends AppCompatActivity {
    private String mArtistName, mAlbumName, mTrackName, mTrackUrl, mAlbumImage;

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
        mAlbumImage = intent.getStringExtra(TopTracksFragment.ALBUM_IMAGE);
    }
}