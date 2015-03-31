package net.labhackercd.edemocracia.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;

import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.api.model.User;

import java.io.IOException;

import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static net.labhackercd.edemocracia.account.AccountConstants.ACCOUNT_TYPE;

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

    public static Observable<Account> getOrRequestAccount(Activity activity) {
        return Observable.<Account>create(f -> {
            Account account;
            AccountManager manager = AccountManager.get(activity);
            try {
                account = AccountUtils.getAccount(manager, activity);
            } catch (IOException | AccountsException e) {
                if (!f.isUnsubscribed())
                    f.onError(e);
                return;
            }

            if (!f.isUnsubscribed()) {
                if (account == null)
                    f.onError(new AssertionError("account == null"));
                else
                    f.onNext(account);
            }
        }).subscribeOn(Schedulers.newThread());
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
        return getOrRequestAccount(activity)
                .flatMap(account -> repository.getUser()
                        .transform(r -> r.asObservable()
                                .compose(UserDataCache.with(activity, account).cache(r.key())))
                        .asObservable()
                        .subscribeOn(Schedulers.io()))
                .asObservable();
    }
}