package me.lehrner.spotifystreamer;

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
        Logfn.d("Start");

        final ArrayList<SpotifyArtistSearchResult> searchResult = new ArrayList<>();

        Logfn.d("Artist = " + artist);

        spotify.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager pager, Response response) {
                if (pager == null || pager.artists == null || pager.artists.items == null) {
                    fragment.showToast(fragment.getString(R.string.no_artist_found));

                    if (pager == null)
                        Logfn.e("pager is null");
                    else if (pager.artists == null)
                        Logfn.e("pager.artists is null");
                    else
                        Logfn.e("pager.artists.items is null");
                    return;
                }

                for (int i = 0; i < pager.artists.items.size(); ++i) {
                    Artist artist = pager.artists.items.get(i);

                    String artistName = artist.name;
                    String artistId = artist.id;
                    String imageUrlMedium = "";

                    if (artist.images != null) {
                        int numberImages = artist.images.size();

                        if (numberImages == 4) {
                            imageUrlMedium = artist.images.get(2).url;
                        } else if (numberImages == 3) {
                            imageUrlMedium = artist.images.get(1).url;
                        }
                    }
                    else {
                        Logfn.e("artists.images is null");
                    }

                    SpotifyArtistSearchResult newArtist = new SpotifyArtistSearchResult(artistName, artistId, imageUrlMedium);
                    searchResult.add(newArtist);
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.handleSearchResult(searchResult);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Logfn.e("Error: " + error.toString());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.handleConnectionError();
                    }
                });
            }
        });
    }
}

