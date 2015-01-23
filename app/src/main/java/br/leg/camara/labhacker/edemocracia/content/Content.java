package br.leg.camara.labhacker.edemocracia.content;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

public abstract class Content implements Parcelable {

    public abstract long getId();

    /**
     * Parcelable interface implementation.
     *
     * We made content parcelable to simplify the passing around during fragment instantiation.
     * This is probably stupid, I know, but I couldn't figure any other alternative.
     *
     * To make it even hacker and easier to implement, I used Gson to serialize objects into the
     * Parcel. This is extremely clever, you see. It probably has some performance issues, but
     * that shouldn't be a problem for our use case. Our models are small and we only use parcels
     * for fragment instantiation.
     *
     * So the only real drawback of Gson is the deserialization process. We have to tell Gson
     * which class we want to get from the string we're deserializing, so we have to save the
     * class into the Parcel and read it later. I didn't know how to do this without taking the
     * risk of dealing with a ClassNotFoundException while looking up for the class by it's name.
     * Since Parcelable.Creator.createFromParcel doesn't throw anything, I can't let the exception
     * propagate, so I just return null. I don't really know if this is gonna be a issue, but it
     * probably will, so I'm leaving a note for now.
     *
     * Anyway, maybe in the future if I grok the Bundles concept we can come up with something
     * better here. Or we could implement decent *parcelablization*.
     */
    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(getClass().getName());
        out.writeString(new Gson().toJson(this));
    }

    public static final Parcelable.Creator<Content> CREATOR = new Parcelable.Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel in) {
            Class<?> cls;
            try {
                cls = Class.forName(in.readString(), true, Content.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return (Content) new Gson().fromJson(in.readString(), cls);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
        }
    };
}
