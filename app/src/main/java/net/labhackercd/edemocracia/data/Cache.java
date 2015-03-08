package net.labhackercd.edemocracia.data;

import rx.Observable;

public abstract class Cache {
    public <T> Observable.Transformer<T, T> cache(Object key) {
        return observable -> {
            T cached = get(key);
            return observable
                    .doOnNext(value -> put(key, value))
                    .startWith(cached == null ? Observable.empty() : Observable.just(cached));
        };
    }

    public static <T> Observable.Transformer<T, T> skipIf(boolean skip) {
        return observable -> observable.take(skip ? 2 : 1).last();
    }

    protected abstract <T> T get(Object key);

    protected abstract <T> void put(Object key, T value);
}
