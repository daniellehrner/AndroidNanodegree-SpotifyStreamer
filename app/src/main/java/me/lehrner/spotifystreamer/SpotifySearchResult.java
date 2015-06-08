package me.lehrner.spotifystreamer;

class SpotifySearchResult {
    private final String artistName;
    private final String artistId;
    private final String imageMedium;
    private final String imageBig;

    public SpotifySearchResult(String artistName, String artistId,
                        String imageMedium, String imageBig) {
        this.artistName = artistName;
        this.artistId = artistId;
        this.imageMedium = imageMedium;
        this.imageBig = imageBig;
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

    public String getImageBig() {
        return imageBig;
    }

    @Override
    public String toString() {
        return "ArtistName: " + artistName +
                ", ArtistId: " + artistId +
                ", ImageMedium: " + imageMedium +
                ", ImageBig: " + imageBig;
    }
}
