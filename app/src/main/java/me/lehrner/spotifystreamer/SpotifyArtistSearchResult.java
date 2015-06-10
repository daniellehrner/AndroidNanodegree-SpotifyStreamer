package me.lehrner.spotifystreamer;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

class SpotifyArtistSearchResult implements Parcelable {
    private static final String KEY_ARTIST_NAME = "me.lehrner.spotifystreamer.artistName";
    private static final String KEY_ARTIST_ID = "me.lehrner.spotifystreamer.artistId";
    private static final String KEY_IMAGE_MEDIUM = "me.lehrner.spotifystreamer.imageMedium";

    private final String artistName;
    private final String artistId;
    private final String imageMedium;

    public SpotifyArtistSearchResult(String artistName, String artistId,
                                     String imageMedium) {
        this.artistName = artistName;
        this.artistId = artistId;
        this.imageMedium = imageMedium;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getImageMedium() {
        return imageMedium;
    }

    @Override
    public String toString() {
        return "ArtistName: " + artistName +
                ", ArtistId: " + artistId +
                ", ImageMedium: " + imageMedium;
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
        bundle.putString(KEY_ARTIST_NAME, artistName);
        bundle.putString(KEY_ARTIST_ID, artistId);
        bundle.putString(KEY_IMAGE_MEDIUM, imageMedium);

        // write the key value pairs to the parcel
        dest.writeBundle(bundle);
    }

    /**
     * Creator required for class implementing the parcelable interface.
     */
    public static final Parcelable.Creator<SpotifyArtistSearchResult> CREATOR = new Creator<SpotifyArtistSearchResult>() {

        @Override
        public SpotifyArtistSearchResult createFromParcel(Parcel source) {
            // read the bundle containing key value pairs from the parcel
            Bundle bundle = source.readBundle();

            // instantiate a person using values from the bundle
            return new SpotifyArtistSearchResult(
                    bundle.getString(KEY_ARTIST_NAME),
                    bundle.getString(KEY_ARTIST_ID),
                    bundle.getString(KEY_IMAGE_MEDIUM));
        }

        @Override
        public SpotifyArtistSearchResult[] newArray(int size) {
            return new SpotifyArtistSearchResult[size];
        }

    };
}
