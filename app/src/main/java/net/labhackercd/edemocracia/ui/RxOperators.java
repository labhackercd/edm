package net.labhackercd.edemocracia.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.app.Activity;

import net.labhackercd.edemocracia.account.AccountUtils;
import net.labhackercd.edemocracia.data.api.EDMErrorHandler;

import java.io.IOException;

import rx.Observable;
import rx.functions.Func1;

public class RxOperators {
    /**
     * Register some convenience error handler for AuthorizationErrors.
     *
     * If the transformed observable emits an AuthorizationError, the *Sign In* screen
     * will be displayed. Then, if the user logs in successfully, the source Observable
     * will be retried. If not, god knows what happen.
     *
     * @param activity The activity that will be used to call AccountManager.addAccount().
     * @return The transformation.
     */
    public static <T> Observable.Transformer<T, T> signInOnAuthorizationError(Activity activity) {
        return new RetryOnAuthErrorTransformer<>(activity);
    }

    private static class RetryOnAuthErrorTransformer<R> implements Observable.Transformer<R, R> {
        private final Activity activity;

        public RetryOnAuthErrorTransformer(Activity activity) {
            this.activity = activity;
        }

        @Override
        public Observable<R> call(Observable<R> observable) {
            /*
            I couldn't make it work with this.
            return observable.retryWhen(notification ->
                [...]
            );
            */
            return observable.retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                @Override
                public Observable<?> call(Observable<? extends Throwable> errorNotifications) {
                    return errorNotifications
                            .flatMap(error -> {
                                if (EDMErrorHandler.isAuthorizationError(error))
                                    return requestAccount(activity);
                                else
                                    return Observable.error(error);
                            });
                }
            });
        }

        private Observable<Account> requestAccount(final Activity activity) {
            return Observable.create(subscriber -> {
                AccountManager manager = AccountManager.get(activity);
                try {
                    Account account = AccountUtils.getAccount(manager, activity);
                    subscriber.onNext(account);
                    subscriber.onCompleted();
                } catch (IOException | AccountsException e) {
                    subscriber.onError(e);
                }
            });
        }
    }
}
