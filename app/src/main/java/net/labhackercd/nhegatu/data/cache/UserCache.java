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

package net.labhackercd.nhegatu.data.cache;

import android.accounts.Account;

import android.accounts.AccountManager;
import android.content.Context;

import com.google.gson.*;
import net.labhackercd.nhegatu.data.api.model.User;

import java.lang.reflect.Type;

/** Caches stuff into an {@link Account}'s *user data* section. */
public class UserCache extends Cache<Account, User> {

    private static final String DATA_FIELD = UserCache.class.getName();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(User.class, new User.GsonDeserializer())
            .create();

    private final AccountManager manager;

    public UserCache(Context context) {
        this.manager = AccountManager.get(context);
    }

    @Override
    public User get(Account account) {
        String json = manager.getUserData(account, DATA_FIELD);
        return json == null ? null : gson.fromJson(json, User.class);
    }

    @Override
    public void put(Account account, User user) {
        manager.setUserData(account, DATA_FIELD, gson.toJson(user));
    }
}
