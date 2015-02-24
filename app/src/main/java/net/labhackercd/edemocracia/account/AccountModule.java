package net.labhackercd.edemocracia.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static net.labhackercd.edemocracia.account.AccountConstants.ACCOUNT_TYPE;

@Module(complete = false, library = true)
@SuppressWarnings("UnusedDeclaration")
public class AccountModule {
    @Provides @Singleton
    Credentials.Provider provideCredentialsProvider(final Application application) {
        final AccountManager accountManager = AccountManager.get(application);
        return new Credentials.Provider() {
            @Override
            public Credentials getCredentials() {
                final Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
                final Account account = accounts.length > 0 ? accounts[0] : null;
                return account == null ? null : new Credentials() {
                    @Override
                    public String getEmailAddress() {
                        return account.name;
                    }

                    @Override
                    public String getPassword() {
                        return accountManager.getPassword(account);
                    }
                };
            }
        };
    }
}