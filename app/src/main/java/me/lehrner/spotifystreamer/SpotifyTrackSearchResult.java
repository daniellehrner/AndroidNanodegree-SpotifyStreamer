package me.lehrner.spotifystreamer;

class SpotifyTrackSearchResult {
    private final String trackName;
    private final String trackId;
    private final String albumName;
    private final String imageUrlMedium;
    private final String imageUrlBig;

    public SpotifyTrackSearchResult(String trackName, String trackId,
                                    String albumName, String imageUrlMedium,
                                    String imageUrlBig) {
        this.trackName = trackName;
        this.trackId = trackId;
        this.albumName = albumName;
        this.imageUrlMedium = imageUrlMedium;
        this.imageUrlBig = imageUrlBig;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getTrackId() {
        return trackId;
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
                ", TrackId: " + trackId +
                ", AlbumName: " + albumName +
                ", ImageUrlBig: " + imageUrlBig;
    }
}
