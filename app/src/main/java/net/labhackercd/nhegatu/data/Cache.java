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

import rx.Observable;

public abstract class Cache {
    /**
     * Transform an observable that emits a single item into an observable that emits
     * one or two items.
     *
     * One item (the original one) if there is nothing cached, and two items (the cached one,
     * and then the original one) if there is a cache entry for the given key.
     *
     * Users can use the @{link Cache.skip} method to either skip or enforce the cache,
     * making the Observable emit only one item again.
     *
     * @param key A unique key identifying this Observable's response in the cache.
     * @return The transformer.
     */
    public <T> Observable.Transformer<T, T> cache(Object key) {
        return observable -> {
            T cached = get(key);
            return observable
                    .doOnNext(value -> put(key, value))
                    .startWith(cached == null ? Observable.empty() : Observable.just(cached));
        };
    }

    public <T> Observable.Transformer<T, T> getCached(Object key) {
        return cacheSkipIf(key, false);
    }

    /**
     * Convenience method that applies both {@link Cache.cache} and {@link Cache.skipIf} to
     * an observable.
     */
    public <T> Observable.Transformer<T, T> cacheSkipIf(Object key, boolean skip) {
        return observable -> observable.compose(cache(key)).compose(Cache.skipIf(skip));
    }

    /**
     * Turn an observable transformed by @{Cache.cache} into an observable that emits only
     * a single item. If *skip* is false, the transformed observable will emit either the cached
     * value (if there is one) or the original value. If skip is true, it'll always emit the
     * original value.
     */
    public static <T> Observable.Transformer<T, T> skipIf(boolean skip) {
        return observable -> observable.take(skip ? 2 : 1).last();
    }

    protected abstract <T> T get(Object key);

    protected abstract <T> void put(Object key, T value);
}
