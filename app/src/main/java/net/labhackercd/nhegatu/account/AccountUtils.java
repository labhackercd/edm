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

package net.labhackercd.nhegatu.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;

import net.labhackercd.nhegatu.data.MainRepository;
import net.labhackercd.nhegatu.data.api.model.User;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

import static net.labhackercd.nhegatu.account.AccountConstants.ACCOUNT_TYPE;

public class AccountUtils {
    public static Account getAccount(final Context context) {
        Account[] accounts = AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0] : null;
    }

    public static Account getAccount(final AccountManager manager, final Activity activity)
            throws IOException, AccountsException {
        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null.");

        if (activity.isFinishing())
            throw new OperationCanceledException();

        Account account = getAccount(activity);

        if (account == null) {
            try {
                manager.addAccount(ACCOUNT_TYPE, null, null, null, activity, null, null).getResult();
            } catch (Exception e) {
                Timber.w(e, "Exception while adding account.");
                if (e instanceof OperationCanceledException)
                    activity.finish();
                else
                    throw e;
            }

            // XXX What?
            account = getAccount(manager, activity);
        }

        return account;
    }

    private static Map<Activity, PublishSubject<Account>> accountRequests = new WeakHashMap<>();

    public static Observable<Account> getOrRequestAccount(Activity activity) {
        // FIXME This is messy because as usual, I have no idea what I'm doing.
        Account account = AccountUtils.getAccount(activity);

        if (account != null)
            return Observable.just(account);

        PublishSubject<Account> request = accountRequests.get(activity);

        if (request != null)
            return request.asObservable();

        request = PublishSubject.create();
        accountRequests.put(activity, request);

        request.subscribe(new Observer<Account>() {
            @Override
            public void onCompleted() {
                onTerminated();
            }

            @Override
            public void onError(Throwable throwable) {
                onTerminated();
            }

            private void onTerminated() {
                accountRequests.remove(activity);
            }

            @Override
            public void onNext(Account account) {
            }
        });

        Observable.<Account>create(f -> {
            Account acc;
            AccountManager manager = AccountManager.get(activity);

            try {
                acc = AccountUtils.getAccount(manager, activity);
            } catch (IOException | AccountsException e) {
                if (!f.isUnsubscribed())
                    f.onError(e);
                return;
            }

            if (!f.isUnsubscribed()) {
                if (acc == null) {
                    f.onError(new AssertionError("account == null"));
                } else {
                    f.onNext(acc);
                    f.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.newThread()).subscribe(request);

        return request.asObservable();
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
        return observable -> getOrRequestAccount(activity).flatMap(account -> observable);
    }

    public static Observable<User> getCurrentUser(MainRepository repository, Activity activity) {
        // TODO FIXME XXX cacheSkipIf with some expiration time or more granular control! PLZ!
        return getOrRequestAccount(activity)
                .flatMap(account -> repository.getUser()
                        .transform(r -> r.asObservable()
                                .compose(UserDataCache.with(activity, account).cacheSkipIf(r.key(), false)))
                        .asObservable()
                        .subscribeOn(Schedulers.io()));
    }
}