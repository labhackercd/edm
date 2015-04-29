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
