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

import android.accounts.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import net.labhackercd.nhegatu.ui.SignInActivity;
import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.io.IOException;

import static android.accounts.AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE;
import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static android.accounts.AccountManager.KEY_INTENT;
import static net.labhackercd.nhegatu.ui.SignInActivity.PARAM_AUTHTOKEN_TYPE;
import static net.labhackercd.nhegatu.ui.SignInActivity.PARAM_EMAIL;

public class Authenticator extends AbstractAccountAuthenticator {

    // TODO Change this to match BuildConfig.APPLICATION_ID.
    public static final String ACCOUNT_TYPE = "net.labhackercd.edemocracia.Account";

    private final Context context;
    private final AccountManager manager;

    public Authenticator(Context context) {
        super(context);
        this.context = context;
        this.manager = AccountManager.get(context);
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                             String authTokenType, String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        final Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                                     Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                               String authTokenType, Bundle options) throws NetworkErrorException {
        final Bundle bundle = new Bundle();

        if (!ACCOUNT_TYPE.equals(authTokenType))
            return bundle;

        String password = manager.getPassword(account);
        if (TextUtils.isEmpty(password)) {
            bundle.putParcelable(KEY_INTENT, createLoginIntent(response));
        }

        return bundle;
    }

    private Parcelable createLoginIntent(AccountAuthenticatorResponse response) {
        final Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(PARAM_AUTHTOKEN_TYPE, ACCOUNT_TYPE);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        return intent;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                              String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                    String authTokenType, Bundle options)
            throws NetworkErrorException {
        final Intent intent = new Intent(context, SignInActivity.class);
        intent.putExtra(PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        if (!TextUtils.isEmpty(account.name))
            intent.putExtra(PARAM_EMAIL, account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_INTENT, intent);
        return bundle;
    }

    public Account getAccount() {
        Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0] : null;
    }

    public String getPassword(Account account) {
        return manager.getPassword(account);
    }

    /**
     * Return an Observable that will emit an account when possible. It defaults to operate on a newThread scheduler.
     */
    public Observable<Account> addAccount(Activity activity) {
        // TODO FIXME This whole thing is flawed. There is no lock anywhere!!! IT'S MADNESS!!!!!!
        AccountManagerFuture<Bundle> addAccount = AccountManager.get(activity)
                .addAccount(Authenticator.ACCOUNT_TYPE, null, null, null, activity, null, null);
        return Observable.<Account>create(f -> {
            Bundle result = null;
            boolean error = false;
            try {
                result = addAccount.getResult();
            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                if (!f.isUnsubscribed())
                    f.onError(e);
                error = true;
            } finally {
                if (!f.isUnsubscribed()) {
                    if (result != null)
                        f.onNext(getAccount());
                    if (!error)
                        f.onCompleted();
                }
            }
        }).doOnUnsubscribe(() -> {
            // XXX Is it alright to *interrupt* the task if it's already running?
            addAccount.cancel(true);
        }).subscribeOn(Schedulers.newThread());
    }
}
