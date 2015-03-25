package net.labhackercd.edemocracia.data.provider;

import android.net.Uri;

public class EDMContract {
    /** TODO: Columns? */

    public static final String CONTENT_AUTHORITY = "net.labhackercd.edemocracia";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_GROUPS = "groups";
    private static final String PATH_THREADS = "threads";
    private static final String PATH_CATEGORIES = "categories";

    public static final class Group {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUPS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.edemocracia.group";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.edemocracia.group";
    }

    public static final class Category {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.edemocracia.category";
    }

    public static final class Thread {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_THREADS).build();

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.edemocraciaa.thread";
    }

    public static final class Message {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_THREADS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.edemocracia.message";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.edemocraciaa.message";
    }
}
