package edu.mit.media.obm.liveobjects.apptidmarsh;

import android.app.Application;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import edu.mit.media.obm.liveobjects.apptidmarsh.module.DependencyInjector;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class TidmarshApplication extends Application {
    @Inject Bus mBus;

    @Override
    public void onCreate() {
        super.onCreate();
        DependencyInjector.inject(this, this);
    }

    public final Bus getBus() {
        return mBus;
    }
}
