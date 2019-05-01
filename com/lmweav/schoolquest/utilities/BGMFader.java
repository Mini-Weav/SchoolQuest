package com.lmweav.schoolquest.utilities;

import android.media.MediaPlayer;

import java.util.Timer;
import java.util.TimerTask;

/*
 * School Quest: BGMFader
 * This class is a utility that fades the volume in/out when starting/stopping a media player.
 *
 * @author Luke Weaver
 * @version 1.0.5
 * @since 2019-04-21
 */
public class BGMFader {

    private static int iVolume;

    private final static int INT_VOLUME_MAX = 100;
    private final static int INT_VOLUME_MIN = 0;
    private final static float FLOAT_VOLUME_MAX = 1;
    private final static float FLOAT_VOLUME_MIN = 0;

    /*---------------------------------------------------------------------------------------------
    | Methods
    ----------------------------------------------------------------------------------------------*/

    public static void start(final MediaPlayer mediaPlayer, int fadeDuration) {
        if (fadeDuration > 0) { iVolume = INT_VOLUME_MIN; }
        else { iVolume = INT_VOLUME_MAX; }

        updateVolume(mediaPlayer, 0);

        if (!mediaPlayer.isPlaying()) { mediaPlayer.start(); }

        if (fadeDuration > 0) {
            final Timer timer = new Timer(true);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateVolume(mediaPlayer, 1);
                    if (iVolume == INT_VOLUME_MAX) {
                        timer.cancel();
                        timer.purge();
                    }
                }
            };

            int delay = fadeDuration / INT_VOLUME_MAX;
            if (delay == 0) { delay = 1; }

            timer.schedule(timerTask, delay, delay);
        }
    }


    public static void stop(final MediaPlayer mediaPlayer, int fadeDuration,
                            final Runnable... endRunnable) {
        if (fadeDuration > 0) { iVolume = INT_VOLUME_MAX; }
        else { iVolume = INT_VOLUME_MIN; }

        updateVolume(mediaPlayer, 0);

        if (fadeDuration > 0) {
            final Timer timer = new Timer(true);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    updateVolume(mediaPlayer,-1);
                    if (iVolume == INT_VOLUME_MIN) {
                        mediaPlayer.stop();
                        timer.cancel();
                        timer.purge();
                        if (endRunnable.length != 0) { endRunnable[0].run(); }
                    }
                }
            };

            int delay = fadeDuration / INT_VOLUME_MAX;
            if (delay == 0) { delay = 1; }

            timer.schedule(timerTask, delay, delay);
        }
    }

    private static void updateVolume(MediaPlayer mediaPlayer, int change) {
        iVolume = iVolume + change;

        if (iVolume < INT_VOLUME_MIN) { iVolume = INT_VOLUME_MIN; }
        else if (iVolume > INT_VOLUME_MAX) { iVolume = INT_VOLUME_MAX; }

        float fVolume = 1 - ((float) Math.log(INT_VOLUME_MAX - iVolume) /
                (float) Math.log(INT_VOLUME_MAX));

        if (fVolume < FLOAT_VOLUME_MIN) { fVolume = FLOAT_VOLUME_MIN; }
        else if (fVolume > FLOAT_VOLUME_MAX) { fVolume = FLOAT_VOLUME_MAX; }

        mediaPlayer.setVolume(fVolume, fVolume);
    }
}
