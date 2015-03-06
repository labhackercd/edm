package net.labhackercd.edemocracia.data;

import net.labhackercd.edemocracia.data.rx.RxSupport;

import java.util.LinkedHashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public abstract class ObservableStore<V, T> {
    private final Map<V, T> cache = new LinkedHashMap<>();
    private final Map<V, PublishSubject<T>> requests = new LinkedHashMap<>();

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
            return request.asObservable();
        }

        request = PublishSubject.create();
        requests.put(key, request);

        Observable<T> result = request.asObservable();

        request.subscribe(new Observer<T>() {
            @Override
            public void onCompleted() {
                onTerminated();
            }

            @Override
            public void onError(Throwable e) {
                onTerminated();
            }

            private void onTerminated() {
                requests.remove(key);
            }

            @Override
            public void onNext(T t) {
                cache.put(key, t);
            }
        });

        fetchRx(key).subscribeOn(Schedulers.io()).subscribe(request);

        return result;
    }

    @SuppressWarnings("unchecked")
    private Observable<T> fetchRx(final V key) {
        return RxSupport.createRequestObservable(new RxSupport.Invoker() {
            @Override
            public void invoke(Callback callback) {
                try {
                    callback.next(fetch(key));
                } catch (Throwable t) {
                    callback.error(t);
                }
            }
        });
    }

    protected abstract T fetch(V request);
}
