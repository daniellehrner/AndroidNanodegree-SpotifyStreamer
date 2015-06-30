package me.lehrner.spotifystreamer;


import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class SpotifyStreamerApplication extends Application {

    public static RefWatcher getRefWatcher(Context context) {
        SpotifyStreamerApplication application = (SpotifyStreamerApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        refWatcher = LeakCanary.install(this);
    }
}
