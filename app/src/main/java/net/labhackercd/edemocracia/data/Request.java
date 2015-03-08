package net.labhackercd.edemocracia.data;

import rx.Observable;
import rx.functions.Func1;

/**
 * A Request is basically an Observable which knows it's request key.
 *
 * Since we can't subclass Observable in RxJava, though, we have to call Request.asObservable
 * when we want to use the Request as an Observable.
 *
 * The great thing about this Request wrapper is it's @{link Request.transform} method.
 * It can be used to apply @{link Observable.Transformation}s to the source Observable in a
 * context where we know the key used to generate that Observable. Like caching:
 *
 *     Request<List<Item>> request = service.getItemList();
 *     Observable<List<Item>> items = request.transform(r ->
 *                 r.asObservable()
 *                 .compose(cache.cacheSkipIf(cache.key(), false)))
 *             .asObservable();
 *
 * @param <T>
 */
public abstract class Request<T>  {
    public abstract Object key();
    public abstract Observable<T> asObservable();

    public static <T> Request<T> create(Object key, Observable<T> observable) {
        return new Request<T>() {
            @Override
            public Object key() {
                return key;
            }

            @Override
            public Observable<T> asObservable() {
                return observable;
            }
        };
    }

    public Request<T> transform(Func1<Request<T>, Observable<T>> transformation) {
        return Request.create(key(), transformation.call(this));
    }
}
