package me.lehrner.spotifystreamer;

import android.util.Log;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

class SpotifySearch {
    private final SpotifyService spotify;

    SpotifySearch() {
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();
    }

    public void updateListView(String artist, final MainActivity activity) {
        Log.d("updateView", "Start ");

        final ArrayList<SpotifySearchResult> searchResult = new ArrayList<>();

        Log.d("SpotifySearch.do", "Artist = " + artist);

        spotify.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager pager, Response response) {
                for (int i = 0; i < pager.artists.limit; i++) {
                    Artist artist = pager.artists.items.get(i);

                    String artistName = artist.name;
                    String artistId = artist.id;
                    String imageUrlMedium = "", imageUrlBig = "";

                    //String artistImageMedium = artist.images.s;
                    //Log.d("SpotifySearch.do", "Artist = " + artistName + " (" + artistId);

                    int numberImages = artist.images.size();

                    if (numberImages == 4) {
                        imageUrlMedium = artist.images.get(2).url;
                        imageUrlBig = artist.images.get(1).url;
                    }
                    else if (numberImages == 3) {
                        imageUrlMedium = artist.images.get(1).url;
                        imageUrlBig = artist.images.get(0).url;
                    }

                    /*if (!imageUrlMedium.isEmpty() && !imageUrlBig.isEmpty()) {
                        Log.d("SpotifySearch.do", "imageUrlMedium = " + imageUrlMedium);
                        Log.d("SpotifySearch.do", "imageUrlBig = " + imageUrlBig);
                    }*/

                    SpotifySearchResult newArtist = new SpotifySearchResult(artistName, artistId, imageUrlMedium, imageUrlBig);
                    searchResult.add(newArtist);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.artistAdapter.addAll(searchResult);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("SpotifySearch.do", "Error: " + error.toString());
            }
        });
    }
}

