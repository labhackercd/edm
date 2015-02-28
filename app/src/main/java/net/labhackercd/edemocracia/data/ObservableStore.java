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
    private final Map<V, PublishSubject<T>> requests = new LinkedHashMap<>();

    public Observable<T> get(final V key) {
        PublishSubject<T> request = requests.get(key);
        if (request != null) {
            // There's an in-flight network request for this section already. Join it.
            return request.asObservable();
        }

        request = PublishSubject.create();
        requests.put(key, request);

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
                // Do nothing.
            }
        });

        fetchRx(key).subscribeOn(Schedulers.io()).subscribe(request);

        return request.asObservable();
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
