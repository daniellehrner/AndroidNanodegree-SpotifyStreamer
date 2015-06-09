package me.lehrner.spotifystreamer;

class SpotifyArtistSearchResult {
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
}
