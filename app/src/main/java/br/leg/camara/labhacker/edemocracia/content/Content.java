package br.leg.camara.labhacker.edemocracia.content;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

public abstract class Content {
    public static String getAuthority(Class cls) {
        return cls.getName();
    }

    public static Uri getContentUri(Class cls) {
        return new Uri.Builder()
                .authority(getAuthority(cls))
                .scheme(ContentResolver.SCHEME_CONTENT)
                .build();
    }

    public static Uri withAppendedId(Class cls, long id) {
        return ContentUris.withAppendedId(getContentUri(cls), id);
    }

    public abstract long getId();
}
