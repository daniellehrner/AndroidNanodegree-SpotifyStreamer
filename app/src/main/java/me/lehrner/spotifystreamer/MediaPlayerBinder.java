package me.lehrner.spotifystreamer;

import android.os.Binder;

class MediaPlayerBinder extends Binder {
    private MediaPlayerService mService = null;

    MediaPlayerBinder(MediaPlayerService service) {
        mService = service;
    }

    public MediaPlayerService getService() {
        return mService;
    }

    public void clear() {
        mService = null;
    }
}
