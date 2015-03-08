package net.labhackercd.edemocracia.data;

import rx.Observable;
import rx.functions.Func1;

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
        Request<T> self = this;
        return new Request<T>() {
            @Override
            public Object key() {
                return self.key();
            }

            @Override
            public Observable<T> asObservable() {
                return transformation.call(self);
            }
        };
    }
}
