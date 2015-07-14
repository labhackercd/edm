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

package net.labhackercd.nhegatu.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import com.squareup.picasso.*;
import com.squareup.picasso.Request;

import java.io.IOException;

/**
 * A {@linkplain RequestHandler request handler} that loads thumbnails for content.
 */
public class ContentThumbnailRequestHandler extends RequestHandler {
    private final ContentResolver contentResolver;

    public ContentThumbnailRequestHandler(Context context) {
        contentResolver = context.getContentResolver();
    }

    @Override
    public boolean canHandleRequest(com.squareup.picasso.Request data) {
        return ContentResolver.SCHEME_CONTENT.equals(data.uri.getScheme());
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        String path = getPath(request.uri);
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }

    private String getPath(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }
}
