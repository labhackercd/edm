package net.labhackercd.edemocracia.data;

import net.labhackercd.edemocracia.data.api.ServiceError;
import net.labhackercd.edemocracia.data.rx.RxSupport;

import java.util.LinkedHashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public abstract class ObservableStore<V, T> {
    private final Map<V, T> cache = new LinkedHashMap<>();
    private final Map<V, PublishSubject<T>> requests = new LinkedHashMap<V, PublishSubject<T>>();

    /**
     * Returns an Observable<T> that can emit one or two item.
     *
     * It'll emit only one (fresh) item when there is no cached value.
     *
     * It'll emit two (cached, fresh) items when there is a cached item.
     *
     * Subscribers can then choose to take only the cached item or ask for the fresh one:
     *
     *     public Observable<T> getData(V request, boolean fresh) {
     *          return store.get(request).take(fresh ? 2 : 1).last();
     *     }
     */
    public Observable<T> get(final V key) {
        Observable<T> cached;
        if (cache.containsKey(key)) {
            cached = Observable.just(cache.get(key));
        } else {
            cached = Observable.empty();
        }
        return Observable.concat(cached, Observable.defer(() -> fresh(key)));
    }

    public Observable<T> fresh(final V key) {
        PublishSubject<T> request = requests.get(key);
        if (request != null) {
            // There's an in-flight network request for this section already. Join it.
            return request.asObservable();
        }

        request = PublishSubject.create();
        requests.put(key, request);

        // We have to lift the subscription before subscribing it to the cache.
        // Don't ask me why, tho. This is the way it is or it won't be.
        Observable<T> observable = request.asObservable();

        request.subscribe(new Observer<T>() {
            @Override
            public void onCompleted() {
                onEnd();
            }

            @Override
            public void onError(Throwable e) {
                onEnd();
            }

            private void onEnd() {
                requests.remove(key);
            }

            @Override
            public void onNext(T t) {
                cache.put(key, t);
            }
        });

        fetchRx(key).subscribeOn(Schedulers.io()).subscribe(request);

        return observable;
    }

    @SuppressWarnings("unchecked")
    private Observable<T> fetchRx(final V key) {
        return RxSupport.createRequestObservable(new RxSupport.Invoker() {
            @Override
            public void invoke(Callback callback) {
                try {
                    T result = fetch(key);
                    callback.next(result);
                } catch (ServiceError error) {
                    callback.error(error);
                }
            }
        });
    }

    protected abstract T fetch(V request);
}
