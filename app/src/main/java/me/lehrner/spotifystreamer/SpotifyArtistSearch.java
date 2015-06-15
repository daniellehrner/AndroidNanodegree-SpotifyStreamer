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

class SpotifyArtistSearch {
    private final SpotifyService spotify;

    SpotifyArtistSearch() {
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();
    }

    public void updateListView(final String artist, final MainActivity activity, final MainActivityFragment fragment) {
        Log.d("updateView", "Start ");

        final ArrayList<SpotifyArtistSearchResult> searchResult = new ArrayList<>();

        Log.d("SpotifyArtistSearch.do", "Artist = " + artist);

        spotify.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager pager, Response response) {
                if (pager == null || pager.artists == null || pager.artists.items == null) {
                    fragment.showToast(fragment.getString(R.string.no_artist_found));

                    if (pager == null)
                        Log.e("artistUpdateListView", "pager is null");
                    else if (pager.artists == null)
                        Log.e("artistUpdateListView", "pager.artists is null");
                    else
                        Log.e("artistUpdateListView", "pager.artists.items is null");
                    return;
                }

                for (int i = 0; i < pager.artists.items.size(); i++) {
                    Artist artist = pager.artists.items.get(i);

                    String artistName = artist.name;
                    String artistId = artist.id;
                    String imageUrlMedium = "";

                    //String artistImageMedium = artist.images.s;
                    //Log.d("SpotifyArtistSearch.do", "Artist = " + artistName + " (" + artistId);

                    if (artist.images != null) {
                        int numberImages = artist.images.size();

                        if (numberImages == 4) {
                            imageUrlMedium = artist.images.get(2).url;
                        } else if (numberImages == 3) {
                            imageUrlMedium = artist.images.get(1).url;
                        }
                    }
                    else {
                        Log.e("artistUpdateListView", "artists.images is null");
                    }

                    SpotifyArtistSearchResult newArtist = new SpotifyArtistSearchResult(artistName, artistId, imageUrlMedium);
                    searchResult.add(newArtist);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (searchResult.isEmpty()) {
                            fragment.showToast(fragment.getString(R.string.no_artist_found));
                        }
                        else {
                            fragment.addAllAdapter(searchResult);
                            fragment.saveLastSearchQuery();
                        }
                        fragment.fadeListViewIn();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("SpotifyArtistSearch.do", "Error: " + error.toString());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.fadeListViewIn();
                    }
                });
                fragment.showToast(activity.getString(R.string.connection_error));
            }
        });
    }
}

