package net.labhackercd.edemocracia.account;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.google.gson.Gson;

import net.labhackercd.edemocracia.data.api.model.User;

public class UserData {
    private static final String KEY_USER = "user";

    private final Gson gson;

    public UserData(Gson gson) {
        this.gson = gson;
    }

    public void setUser(AccountManager manager, Account account, User user) {
        String json = gson.toJson(user);
        manager.setUserData(account, KEY_USER, json);
    }

    public User getUser(AccountManager manager, Account account) {
        String json = manager.getUserData(account, KEY_USER);
        return gson.fromJson(json, User.class);
    }
}
