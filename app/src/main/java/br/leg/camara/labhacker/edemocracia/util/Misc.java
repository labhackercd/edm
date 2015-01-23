package br.leg.camara.labhacker.edemocracia.util;

import android.net.Uri;
import android.widget.VideoView;

import java.lang.reflect.Field;

public class Misc {
    public static Uri getVideoViewVideoURI(VideoView view) {
        try {
            Field uriField = VideoView.class.getDeclaredField("mUri");
            uriField.setAccessible(true);
            return (Uri) uriField.get(view);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
