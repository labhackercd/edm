package net.labhackercd.edemocracia.data;

import net.labhackercd.edemocracia.data.MainRepository.Request;

import java.util.LinkedHashMap;

import rx.Observable;
import rx.functions.Func1;

/**
 * Poor man's in-memory cache.
 */
public class RequestCache {
    private LinkedHashMap<Object, Object> cache = new LinkedHashMap<>();

    public <T> Observable<T> cached(MainRepository.Request<T> request) {
        T cached = safeGet(request);
        if (cached != null)
            return Observable.just(cached);
        else
            return register(request);
    }

    public <T> Observable<T> register(MainRepository.Request<T> request) {
        return request.observable()
                .doOnNext(value -> cache.put(request.request(), value));
    }

    public <T> Func1<Request<T>, Observable<T>> skipIf(boolean skip) {
        return request -> skip ? register(request) : cached(request);
    }

    @SuppressWarnings("unchecked")
    private <T> T safeGet(MainRepository.Request<T> request) {
        try {
            return (T) cache.get(request.request());
        } catch (ClassCastException e) {
            return null;
        }
    }
}
