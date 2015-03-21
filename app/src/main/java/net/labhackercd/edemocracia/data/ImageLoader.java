package net.labhackercd.edemocracia.data;

import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import net.labhackercd.edemocracia.data.api.Portal;

public class ImageLoader {
    private final Portal portal;
    private final Picasso picasso;

    public ImageLoader(Portal portal, Picasso picasso) {
        this.portal = portal;
        this.picasso = picasso;
    }

    public RequestCreator group(long groupId) {
        String url = String.format("%s/documents/%d/0/icone", portal.url(), groupId);
        return picasso.load(Uri.parse(url));
    }
}
