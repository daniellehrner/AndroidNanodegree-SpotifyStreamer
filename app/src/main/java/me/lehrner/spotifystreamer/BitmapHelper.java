package me.lehrner.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;

class BitmapHelper extends AsyncTask<String, Integer, Long> {
    private Bitmap mBitmap = null;
    private final Context mContext;

    BitmapHelper(Context context) {
        mContext = context;
    }

    protected Long doInBackground(String... urls) {
        for (String url : urls) {
            try {
                final float IMAGE_SIZE_DP = 64.0f;
                final float scale = mContext.getResources().getDisplayMetrics().density;
                final int imageSizePx = (int) (IMAGE_SIZE_DP * scale + 0.5f);

                Logfn.d("Loading image " + url + " with size: " + imageSizePx);

                mBitmap = Glide
                        .with(mContext)
                        .load(url)
                        .asBitmap()
                        .into(imageSizePx, imageSizePx)
                        .get();
            } catch (final Exception e) {
                Logfn.e(e.getMessage());
            }
        }
        return 0l;
    }

    protected void onPostExecute(Long notUsed) {
        if (mBitmap != null) {
            Logfn.d("Image loaded");

            Intent notificationImageIntent = new Intent(mContext, MediaPlayerService.class);
            notificationImageIntent.setAction(MediaPlayerService.ACTION_SET_IMAGE);
            notificationImageIntent.putExtra(MediaPlayerService.KEY_NOTIFICATION_IMAGE, mBitmap);

            mContext.startService(notificationImageIntent);
        }
        else {
            Logfn.e("Image not loaded");
        }
    }
}
