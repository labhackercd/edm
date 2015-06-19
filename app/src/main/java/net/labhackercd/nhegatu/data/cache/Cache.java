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

package net.labhackercd.nhegatu.data.cache;

import rx.Observable;

public abstract class Cache<K, V> {

    public abstract V get(K key);

    public abstract void put(K key, V value);

    public Observable.Transformer<V, V> refresh(K key) {
        return observable -> observable.doOnNext(value -> put(key, value));
    }

    public Observable.Transformer<V, V> getOrRefresh(K key) {
        return observable -> observable.compose(refresh(key))
                .startWith(getCached(key))
                .first();
    }

    private Observable<V> getCached(K key) {
        return Observable.defer(() -> {
            V cached = get(key);
            return cached != null ? Observable.just(cached) : Observable.empty();
        });
    }
}
