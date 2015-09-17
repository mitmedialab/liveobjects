package edu.mit.media.obm.liveobjects.driver.wifi.module;

import android.content.Context;

import dagger.ObjectGraph;

/**
 * Created by artimo14 on 8/8/15.
 */
public class DependencyInjector {
    private static Context applicationContext = null;

    public static void setApplicationContext(Context applicationContext) {
        DependencyInjector.applicationContext = applicationContext;
    }

    public static void inject(Object dependent) {
        if (applicationContext == null) {
            throw new IllegalStateException("Must set application context before any injection");
        }

        injectModules(dependent, getDefaultModules(applicationContext));
    }

    public static void inject(Object dependent, Object... modules) {
        injectModules(dependent, modules);
    }

    private static Object[] getDefaultModules(Context context) {
        return new Object[] {
                new NetworkWifiModule(context)
        };
    }

    private static void injectModules(Object dependent, Object[] modules) {
        ObjectGraph objectGraph = ObjectGraph.create(modules);
        objectGraph.inject(dependent);
    }
}
