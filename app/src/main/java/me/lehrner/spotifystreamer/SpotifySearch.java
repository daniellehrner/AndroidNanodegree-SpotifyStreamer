package me.lehrner.spotifystreamer;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class SpotifySearch extends AsyncTask<String, Void, ArrayList<SpotifySearchResult>> {
    protected ArrayList<SpotifySearchResult> doInBackground(String... artists) {
        Log.d("SpotifySearch.do", "Start ");

        ArrayList<SpotifySearchResult> searchResult = new ArrayList<>();

        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();

        for (String artist : artists) {
            Log.d("SpotifySearch.do", "Artist = " + artist);
            ArtistsPager results = spotify.searchArtists(artist);
        }

        return searchResult;
    }

    protected void onPostExecute(ArrayList<SpotifySearchResult> result) {
        Log.d("SpotifySearch.Post", "Start ");
    }
}

