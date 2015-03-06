package net.labhackercd.edemocracia.data.rx;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Utilities for supporting RxJava Observables.
 */
public final class RxSupport {
    public interface Invoker {
        void invoke(Callback callback);

        interface Callback {
            void next(Object o);
            void error(Throwable t);
        }
    }

    public static Observable createRequestObservable(final Invoker invoker) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override public void call(final Subscriber<? super Object> subscriber) {
                invoker.invoke(new Invoker.Callback() {
                    @Override public void next(Object o) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(o);
                            subscriber.onCompleted();
                        }
                    }

                    @Override public void error(Throwable t) {
                        if (!subscriber.isUnsubscribed())
                            subscriber.onError(t);
                    }
                });
            }
        });
    }
}
