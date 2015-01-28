package edu.mit.media.obm.liveobjects.middleware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Valerio Panzica La Manna on 1/27/15.
 */
public class LiveObjectsList implements Iterable<LiveObject>{

    private Collection<LiveObject> liveObjectsList;

    protected LiveObjectsList() {
        liveObjectsList = new ArrayList<>();

    }

    protected void add(LiveObject liveObject) {
        liveObjectsList.add(liveObject);
    }

    public boolean isEmpty() {
        return liveObjectsList.isEmpty();
    }
    @Override
    public Iterator<LiveObject> iterator() {
        return liveObjectsList.iterator();
    }
}
