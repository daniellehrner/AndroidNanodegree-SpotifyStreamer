package me.lehrner.spotifystreamer;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

class SpotifyTrackSearchResult implements Parcelable {
    private static final String KEY_TRACK_NAME = "me.lehrner.spotifystreamer.trackName";
    private static final String KEY_TRACK_URL = "me.lehrner.spotifystreamer.trackUrl";
    private static final String KEY_ALBUM_NAME = "me.lehrner.spotifystreamer.albumName";
    private static final String KEY_IMAGE_TRACK_MEDIUM = "me.lehrner.spotifystreamer.imageTrackMedium";
    private static final String KEY_IMAGE_TRACK_BIG = "me.lehrner.spotifystreamer.imageTrackBig";

    private final String trackName;
    private final String trackUrl;
    private final String albumName;
    private final String imageUrlMedium;
    private final String imageUrlBig;

    public SpotifyTrackSearchResult(String trackName, String trackUrl,
                                    String albumName, String imageUrlMedium,
                                    String imageUrlBig) {

        this.trackName = trackName;
        this.trackUrl = trackUrl;
        this.albumName = albumName;
        this.imageUrlMedium = imageUrlMedium;
        this.imageUrlBig = imageUrlBig;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getImageUrlMedium() {
        return imageUrlMedium;
    }

    public String getImageUrlBig() {
        return imageUrlBig;
    }

    @Override
    public String toString() {
        return "TrackName: " + trackName +
                ", TrackUrl: " + trackUrl +
                ", AlbumName: " + albumName +
                ", ImageUrlMedium: " + imageUrlMedium +
                ", ImageUrlBig: " + imageUrlBig;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // create a bundle for the key value pairs
        Bundle bundle = new Bundle();

        // insert the key value pairs to the bundle
        bundle.putString(KEY_TRACK_NAME, trackName);
        bundle.putString(KEY_TRACK_URL, trackUrl);
        bundle.putString(KEY_ALBUM_NAME, albumName);
        bundle.putString(KEY_IMAGE_TRACK_MEDIUM, imageUrlMedium);
        bundle.putString(KEY_IMAGE_TRACK_BIG, imageUrlBig);

        // write the key value pairs to the parcel
        dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator<SpotifyTrackSearchResult> CREATOR = new Creator<SpotifyTrackSearchResult>() {

        @Override
        public SpotifyTrackSearchResult createFromParcel(Parcel source) {
            // read the bundle containing key value pairs from the parcel
            Bundle bundle = source.readBundle();

            // instantiate a person using values from the bundle
            return new SpotifyTrackSearchResult(
                    bundle.getString(KEY_TRACK_NAME),
                    bundle.getString(KEY_TRACK_URL),
                    bundle.getString(KEY_ALBUM_NAME),
                    bundle.getString(KEY_IMAGE_TRACK_MEDIUM),
                    bundle.getString(KEY_IMAGE_TRACK_BIG));
        }

        @Override
        public SpotifyTrackSearchResult[] newArray(int size) {
            return new SpotifyTrackSearchResult[size];
        }

    };
}
