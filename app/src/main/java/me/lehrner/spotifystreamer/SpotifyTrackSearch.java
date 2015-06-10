package me.lehrner.spotifystreamer;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

class SpotifyTrackSearch {
    private final SpotifyService spotify;
    private final Map<String, Object> options;

    SpotifyTrackSearch() {
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();

        options = new HashMap<>();
        options.put("country", Locale.getDefault().getCountry());
    }

    public void updateListView(String artistId, final TopTracks activity) {
        Log.d("TrackUpdateView", "Start ");

        final ArrayList<SpotifyTrackSearchResult> searchResult = new ArrayList<>();

        Log.d("TrackUpdateView", "ArtistId = " + artistId);

        spotify.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(Tracks pager, Response response) {
                if (pager == null || pager.tracks == null) {
                    activity.showToast(activity.getString(R.string.no_track_found));

                    if (pager == null)
                        Log.e("TrackUpdateView", "pager is null");
                    else
                        Log.e("TrackUpdateView", "pager.tracks is null");
                    return;
                }

                for (int i = 0; i < pager.tracks.size(); i++) {
                    Track track = pager.tracks.get(i);

                    String trackName = track.name;
                    String trackId = track.id;
                    String albumName = "", imageUrlMedium = "", imageUrlBig = "";

                    //String artistImageMedium = artist.images.s;
                    //Log.d("SpotifyArtistSearch.do", "Artist = " + artistName + " (" + artistId);

                    if (track.album != null) {
                        albumName = track.album.name;

                        if (track.album.images != null) {
                            int numberImages = track.album.images.size();

                            if (numberImages == 4) {
                                imageUrlBig = track.album.images.get(1).url;
                                imageUrlMedium = track.album.images.get(2).url;
                            } else if (numberImages == 3) {
                                imageUrlBig = track.album.images.get(0).url;
                                imageUrlMedium = track.album.images.get(1).url;
                            }
                        }
                        else {
                            Log.e("TrackUpdateView", "track.album.images is null");
                        }
                    }
                    else {
                        Log.e("TrackUpdateView", "track.album is null");
                    }

                    SpotifyTrackSearchResult newTrack = new SpotifyTrackSearchResult(trackName,
                            trackId, albumName, imageUrlMedium, imageUrlBig);
                    searchResult.add(newTrack);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (searchResult.isEmpty()) {
                            activity.showToast(activity.getString(R.string.no_track_found));
                            activity.finish();
                        } else {
                            activity.addAllAdapter(searchResult);
                            activity.fadeListViewIn();
                        }
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("SpotifyArtistSearch.do", "Error: " + error.toString());

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.fadeListViewIn();
                    }
                });
                activity.showToast(activity.getString(R.string.connection_error));
            }
        });
    }
}
