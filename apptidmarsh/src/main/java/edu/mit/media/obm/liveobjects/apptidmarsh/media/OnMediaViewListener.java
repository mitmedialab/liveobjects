package edu.mit.media.obm.liveobjects.apptidmarsh.media;

import android.media.MediaPlayer;
import android.media.SoundPool;

/**
 * This interface is a wrapper of the different callbacks from the MediaPlayer
 * that an activity has to handle.
 *
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface OnMediaViewListener extends MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, SoundPool.OnLoadCompleteListener {
    @Override
    void onCompletion(MediaPlayer mp);

    @Override
    boolean onError(MediaPlayer mp, int what, int extra);

    @Override
    boolean onInfo(MediaPlayer mp, int what, int extra);

    @Override
    void onBufferingUpdate(MediaPlayer mp, int percent);

    @Override
    void onLoadComplete(SoundPool soundPool, int sampleId, int status);
}
