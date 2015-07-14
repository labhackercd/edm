/*
 * This file is part of Nhegatu, the e-Demoracia Client for Android.
 *
 * Nhegatu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nhegatu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nhegatu.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.labhackercd.nhegatu.data.provider;

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
                "vnd.android.cursor.item/vnd.edemocracia.thread";
    }

    public static final class Message {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_THREADS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.edemocracia.message";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.edemocracia.message";
    }
}
