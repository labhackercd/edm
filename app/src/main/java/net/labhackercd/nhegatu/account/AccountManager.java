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
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import net.labhackercd.nhegatu.data.api.model.User;
import rx.Observable;

import java.io.IOException;

/**
 * Thin wrapper around {@link android.accounts.AccountManager}.
 */
public class AccountManager {
    private static final String DATA_USER = AccountManager.class.getName().concat(".user");

    private final android.accounts.AccountManager manager;

    private AccountManager(android.accounts.AccountManager manager) {
        this.manager = manager;
    }

    public static AccountManager get(Context context) {
        return new AccountManager(android.accounts.AccountManager.get(context));
    }

    // Or should it be called getAccount?
    public static Account createAccount(User user) {
        return new Account(user.getEmailAddress(), Authenticator.ACCOUNT_TYPE);
    }

    public User getUser(Account account) {
        String data = manager.getUserData(account, DATA_USER);
        return GsonUserData.fromUserData(data);
    }

    public Account getAccount() {
        Account[] accounts = manager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0] : null;
    }

    public Observable<Account> addAccount(Activity activity) {
        return Observable.<Account>defer(() -> {
            Bundle result;
            try {
                result = manager
                        .addAccount(Authenticator.ACCOUNT_TYPE, null, null, null, activity, null, null)
                        .getResult();
            } catch (AuthenticatorException | OperationCanceledException | IOException e) {
                return Observable.error(e);
            }

            String accountName = result.getString(android.accounts.AccountManager.KEY_ACCOUNT_NAME);
            String accountType = result.getString(android.accounts.AccountManager.KEY_ACCOUNT_TYPE);

            return Observable.from(manager.getAccountsByType(accountType))
                    .takeFirst(account -> account.name.equals(accountName));
        });
    }

    @Nullable
    public Account addAccountExplicitly(User user, String password) {
        Account account = createAccount(user);
        Bundle userData = new Bundle();
        userData.putString(DATA_USER, GsonUserData.toUserData(user));
        return manager.addAccountExplicitly(account, password, userData) ? account : null;
    }

    public void setPassword(Account account, String password) {
        manager.setPassword(account, password);
    }
}
