package net.labhackercd.edemocracia.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.squareup.picasso.Transformation;

/**
 * Created by baufaker on 11/02/15.
 */
public class OverlayTransformation implements Transformation {

    private final Bitmap button;

    public OverlayTransformation (Bitmap button) {
        this.button = button;

    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap bmOverlay = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        Canvas canvas = new Canvas(bmOverlay);

        canvas.drawBitmap(source, new Matrix(), null);
        canvas.drawBitmap(button, (source.getWidth()/2 - button.getWidth()/2), (source.getHeight()/2 - button.getHeight()/2), null);
        source.recycle();
        return bmOverlay;
    }

    @Override
    public String key() {
        return "test";
    }
}
