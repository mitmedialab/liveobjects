package edu.mit.media.obm.liveobjects.driver.wifi.module;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import edu.mit.media.obm.liveobjects.driver.wifi.event.ConnectedToNetworkDeviceEvent;
import edu.mit.media.obm.liveobjects.middleware.common.MiddlewareInterface;

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

        Object[] modules = getDefaultModules(applicationContext);
        inject(dependent, modules);
    }

    public static void inject(Object dependent, Object... modules) {
        ObjectGraph objectGraph = ObjectGraph.create(modules);
        objectGraph.inject(dependent);
    }

    private static Object[] getDefaultModules(Context context) {
        return new Object[] {
                new NetworkWifiModule(context)
        };
    }
}
