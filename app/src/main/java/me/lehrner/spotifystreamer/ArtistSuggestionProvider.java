package me.lehrner.spotifystreamer;

import android.content.SearchRecentSuggestionsProvider;

public class ArtistSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "me.lehrner.ArtistSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public ArtistSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}