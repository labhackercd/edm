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

package net.labhackercd.nhegatu.data.rx;

import rx.Observable;
import rx.Subscriber;

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
