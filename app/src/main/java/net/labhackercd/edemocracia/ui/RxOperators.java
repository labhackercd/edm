package net.labhackercd.edemocracia.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.app.Activity;

import net.labhackercd.edemocracia.account.AccountUtils;

import java.io.IOException;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class RxOperators {
    /**
     * Transform a request observable in such a way that it'll only emit fresh values
     * if the *fresh* flag is set to `true`. Otherwise, it'll try to emit the cached value,
     * but if there is no cached value it'll emit the fresh value.
     *
     * @param fresh Boolean.
     * @return The transformation.
     */
    public static <T> Observable.Transformer<T, T> fresh(boolean fresh) {
        return observable -> observable.take(fresh ? 2 : 1).last();
    }

    /**
     * Ensure there is an usable account in the AccountManager before making requests:
     *
     *     service.getGroups().compose(requireAccount(activity));
     *
     * @param activity The *current* activity.
     * @return A transformation.
     */
    public static <T> Observable.Transformer<T, T> requireAccount(Activity activity) {
        return observable -> {
            PublishSubject<T> subject = PublishSubject.create();

            // AccountUtils.getAccount calls AccountManager.addAccount,
            // which cannot be called from main thread.
            Schedulers.newThread().createWorker()
                    .schedule(() -> {
                        AccountManager manager = AccountManager.get(activity);
                        Account account;
                        try {
                            account = AccountUtils.getAccount(manager, activity);
                        } catch (IOException | AccountsException e) {
                            throw new RuntimeException(e);
                        }

                        if (account == null)
                            throw new AssertionError("account == null");

                        observable.subscribe(subject);
                    });

            return subject.asObservable();
        };
    }
}
