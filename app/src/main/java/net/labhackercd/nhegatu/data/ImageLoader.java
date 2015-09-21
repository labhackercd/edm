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

import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import net.labhackercd.nhegatu.data.api.TypedService;
import rx.Observable;

public class ImageLoader {
    private final Portal portal;
    private final Picasso picasso;
    private final TypedService service;

    public ImageLoader(Portal portal, TypedService service, Picasso picasso) {
        this.portal = portal;
        this.picasso = picasso;
        this.service = service;
    }

    public RequestCreator group(long groupId) {
        String url = String.format("%s/documents/%d/0/icone", portal.getUrl(), groupId);
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
        return service.getUser(userId)
                // TODO cache
                .map(user -> {
                    long portraitId = user.getPortraitId();
                    return portraitId <= 0 ? null : picasso.load(Uri.parse(userPortraitUrl(portraitId)));
                })
                .filter(r -> r != null);
    }

    private String userPortraitUrl(long portraitId) {
        return String.format("%s/image/user_male_portrait?img_id=%d", portal.getUrl(), portraitId);
    }
}
