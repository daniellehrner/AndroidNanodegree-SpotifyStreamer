package me.lehrner.spotifystreamer;

import android.content.Context;
import android.content.Intent;

public class MusicIntentReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // pause music if audio suddenly becomes
        // noisy (e.g. unplugging head phones)
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {

            Logfn.d("Audio becoming noisy");
            Intent pauseIntent = new Intent(context, MediaPlayerService.class);
            pauseIntent.setAction(MediaPlayerService.ACTION_PAUSE);

            context.startService(pauseIntent);
        }
    }
}
