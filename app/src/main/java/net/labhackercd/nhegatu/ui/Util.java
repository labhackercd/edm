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

package net.labhackercd.nhegatu.ui;

import net.labhackercd.nhegatu.data.cache.Cache;
import rx.Observable;

public class Util {
    private Util() {
        // Private constructor to prevent subclassing.
    }

    public static <T> Observable.Transformer<T, T> applyCache(Cache<Object, Object> cache, Object key) {
        return applyCache(cache, key, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> applyCache(Cache<Object, Object> cache, Object key, boolean fresh) {
        return observable -> {
            Cache<Object, T> typedCache = (Cache<Object, T>) cache;
            if (fresh) {
                return observable.compose(typedCache.refresh(key));
            } else {
                return observable.compose(typedCache.getOrRefresh(key));
            }
        };
    }
}
