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

package net.labhackercd.nhegatu.ui.message;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.squareup.picasso.Transformation;

import net.labhackercd.nhegatu.R;

/**
 * A {@link Transformation} that draws a "Play" button over a image.
 */
public class VideoPlayButtonTransformation implements Transformation {
    private final Bitmap overlay;

    public VideoPlayButtonTransformation(Context context) {
        this.overlay =  BitmapFactory.decodeResource(
                context.getResources(), R.drawable.ic_play_circle_outline_white_36dp);
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap bitmap = Bitmap.createBitmap(
                source.getWidth(), source.getHeight(), source.getConfig());

        Canvas canvas = new Canvas(bitmap);

        // Draw the original image
        canvas.drawBitmap(source, new Matrix(), null);

        // Draw the overlay
        float top = source.getHeight() / 2 - overlay.getHeight() / 2;
        float left = source.getWidth() / 2 - overlay.getWidth() / 2;
        canvas.drawBitmap(overlay, left, top, null);

        // Ensure source is recycled
        source.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        return "playButtonOverlay";
    }
}
