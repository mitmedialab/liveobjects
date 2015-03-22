package edu.mit.media.obm.liveobjects.app.media;

import android.media.MediaPlayer;

/**
 * This interface is a wrapper of the different callbacks from the MediaPlayer
 * that an activity has to handle.
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public interface OnMediaViewListener extends MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener{
    @Override
    void onCompletion(MediaPlayer mp);

    @Override
    boolean onError(MediaPlayer mp, int what, int extra);

    @Override
    boolean onInfo(MediaPlayer mp, int what, int extra);


}
