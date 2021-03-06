package me.lehrner.spotifystreamer;

import android.app.Activity;

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

    public void updateListView(String artistId, final Activity activity, final TopTracksFragment fragment) {
        Logfn.d("Start");

        final ArrayList<SpotifyTrackSearchResult> searchResult = new ArrayList<>();

        Logfn.d("ArtistId = " + artistId);

        spotify.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
            @Override
            public void success(Tracks pager, Response response) {
                if (pager == null || pager.tracks == null) {
                    fragment.showToast(activity.getString(R.string.no_track_found));

                    if (pager == null)
                        Logfn.e("pager is null");
                    else
                        Logfn.e("pager.tracks is null");
                    return;
                }

                for (int i = 0; i < pager.tracks.size(); ++i) {
                    Track track = pager.tracks.get(i);

                    String trackName = track.name;
                    String trackUrl = track.preview_url;
                    String albumName = "", imageUrlMedium = "", imageUrlBig = "";

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
                            Logfn.e("track.album.images is null");
                        }
                    }
                    else {
                        Logfn.e("track.album is null");
                    }

                    SpotifyTrackSearchResult newTrack = new SpotifyTrackSearchResult(trackName,
                            trackUrl, albumName, imageUrlMedium, imageUrlBig);
                    searchResult.add(newTrack);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.getSearchResult(searchResult);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Logfn.e("Error: " + error.toString());

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.handleSearchError();
                    }
                });
            }
        });
    }
}
