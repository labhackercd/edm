package net.labhackercd.edemocracia.account;

import android.accounts.Account;

import android.accounts.AccountManager;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.labhackercd.edemocracia.data.Cache;

/** Caches stuff into an {@link Account}'s *user data* section. */
public class UserDataCache extends Cache {
    private final Account account;
    private final AccountManager manager;

    private static final Gson gson = new GsonBuilder().create();

    public UserDataCache(Context context, Account account) {
        this.manager = AccountManager.get(context);
        this.account = account;
    }

    @Override
    protected <T> T get(Object key) {
        return null;
    }

    @Override
    protected <T> void put(Object key, T value) {
    }

    public static UserDataCache with(Context context, Account account) {
        return new UserDataCache(context.getApplicationContext(), account);
    }
}
