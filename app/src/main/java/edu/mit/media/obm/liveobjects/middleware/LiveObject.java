package edu.mit.media.obm.liveobjects.middleware;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class representing a Live Object
 *
 * Created by Valerio Panzica La Manna on 1/27/15.
 */
public class LiveObject implements Parcelable{

    private String name;

    public LiveObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void put(ContentType contentType, String name, Object object) {
        //TODO to implement

    }

    public Object get(ContentType contentType, String name) {
        //TODO to implement
        return null;

    }



    // implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }
    // implement Parcelable
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    public static final Creator<LiveObject> CREATOR = new Creator<LiveObject>() {
        @Override
        public LiveObject createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public LiveObject[] newArray(int size) {
            return new LiveObject[0];
        }
    };

    private LiveObject(Parcel source) {
        name = source.readString();
    }

    @Override
    public String toString() {
        return name;
    }
}
