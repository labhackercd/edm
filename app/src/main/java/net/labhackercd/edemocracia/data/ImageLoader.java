package net.labhackercd.edemocracia.data;

import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import net.labhackercd.edemocracia.data.api.Portal;

import rx.Observable;

public class ImageLoader {
    private final Cache cache;
    private final Portal portal;
    private final Picasso picasso;
    private final MainRepository repository;

    public ImageLoader(Portal portal, Picasso picasso, MainRepository repository, Cache cache) {
        this.portal = portal;
        this.picasso = picasso;
        this.repository = repository;
        this.cache = cache;
    }

    public RequestCreator group(long groupId) {
        String url = String.format("%s/documents/%d/0/icone", portal.url(), groupId);
        return picasso.load(Uri.parse(url));
    }

    public RequestCreator userPortrait(long portraitId) {
        String url = userPortraitUrl(portraitId);
        return picasso.load(Uri.parse(url));
    }

    // TODO Some caching control. With the current implementation images could end up being
    // cached forever!
    // TODO Isn't it just sad that we cant create a Picasso Request from an observable without
    // the calling code even knowing about it? That would reduce A LOT of code duplication.
    public Observable<RequestCreator> userPortrait2(long userId) {
        return repository.getUser(userId)
                .transform(r -> r.asObservable()
                        .compose(cache.cache(r.key())))
                .asObservable()
                .map(user -> picasso.load(Uri.parse(userPortraitUrl(user.getPortraitId()))));
    }

    private String userPortraitUrl(long portraitId) {
        return String.format("%s/image/user_male_portrait?img_id=%d", portal.url(), portraitId);
    }
}
