package net.labhackercd.edemocracia.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;

import java.io.IOException;

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
                Timber.d(e, "Error while retrieving account.");
                if (e instanceof OperationCanceledException)
                    activity.finish();
                throw e;
            }

            // XXX What?
            account = getAccount(manager, activity);
        }

        return account;
    }
}