package me.lehrner.spotifystreamer;

public class SpotifySearchResult {
    private String artistName;
    private String artistId;
    private String imageSmall;
    private String imageMedium;
    private String imageBig;

    public SpotifySearchResult(String artistName, String artistId, String imageSmall,
                        String imageMedium, String imageBig) {
        this.artistName = artistName;
        this.artistId = artistId;
        this.imageSmall = imageSmall;
        this.imageMedium = imageMedium;
        this.imageBig = imageBig;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getImageSmall() {
        return imageSmall;
    }

    public String getImageMedium() {
        return imageMedium;
    }

    public String getImageBig() {
        return imageBig;
    }

    @Override
    public String toString() {
        String text =   "ArtistName: " + artistName +
                ", ArtistId: " + artistId +
                ", ImageSmall: " + imageSmall +
                ", ImageMedium: " + imageMedium +
                ", ImageBig: " + imageBig;

        return text;
    }
}
