package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.content.Context;
import android.widget.MediaController;

import dagger.Module;
import dagger.Provides;
import edu.mit.media.obm.liveobjects.apptidmarsh.media.VideoViewFragment;

/**
 * Created by artimo14 on 8/1/15.
 */
@Module(injects = VideoViewFragment.class)
public class VideoViewFragmentModule {
    Context mContext;

    public VideoViewFragmentModule(Context context) {
        mContext = context;
    }

    @Provides
    MediaController provideMediaController() {
        return new MediaController(mContext);
    }
}
