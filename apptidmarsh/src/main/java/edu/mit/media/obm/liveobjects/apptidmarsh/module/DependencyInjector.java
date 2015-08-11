package edu.mit.media.obm.liveobjects.apptidmarsh.module;

import android.content.Context;

import dagger.ObjectGraph;

/**
 * Created by artimo14 on 8/8/15.
 */
public class DependencyInjector {
    private static DependencyInjector mInstance = null;

    private ObjectGraph mObjectGraph;

    private DependencyInjector() {
        // create object class for dependency injection
        mObjectGraph = ObjectGraph.create(MiddlewareModule.class, ApplicationModule.class);
    }

    public static DependencyInjector getInstance() {
        if (mInstance == null) {
            mInstance = new DependencyInjector();
        }

        return mInstance;
    }

    public static void inject(Object object, Context context) {
        // how can I add SystemModule to the object graph created in the constructor?
        ObjectGraph objectGraph = ObjectGraph.create(new MiddlewareModule(), new ApplicationModule(), new SystemModule(context));
        objectGraph.inject(object);
    }
}
