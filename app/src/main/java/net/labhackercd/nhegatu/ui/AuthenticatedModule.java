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

package net.labhackercd.nhegatu.ui;

import android.accounts.Account;
import com.liferay.mobile.android.auth.basic.BasicAuthentication;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import net.labhackercd.nhegatu.EDMRootModule;
import net.labhackercd.nhegatu.account.Authenticator;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.MainRepository;
import net.labhackercd.nhegatu.data.Portal;
import net.labhackercd.nhegatu.data.api.client.EDMService;
import net.labhackercd.nhegatu.data.cache.Cache;
import net.labhackercd.nhegatu.ui.group.GroupListFragment;
import net.labhackercd.nhegatu.ui.message.ComposeActivity;
import net.labhackercd.nhegatu.ui.message.MessageListFragment;
import net.labhackercd.nhegatu.ui.preference.PreferenceActivity;
import net.labhackercd.nhegatu.ui.preference.PreferenceFragment;
import net.labhackercd.nhegatu.ui.thread.CategoryThreadListFragment;
import net.labhackercd.nhegatu.ui.thread.GroupThreadListFragment;
import net.labhackercd.nhegatu.ui.thread.ThreadListFragment;

import javax.inject.Singleton;

@Module(
    complete = false,
    injects = {
        ComposeActivity.class,
        GroupListFragment.class,
        PreferenceFragment.class,
        PreferenceActivity.class,
        ThreadListFragment.class,
        MessageListFragment.class,
        GroupThreadListFragment.class,
        CategoryThreadListFragment.class
    }
)
@SuppressWarnings("UnusedDeclaration")
public class AuthenticatedModule {
    private final Account account;

    AuthenticatedModule(Account account) {
    this.account = account;
    }

    @Provides @Singleton
    Account provideAccount() {
    return account;
    }

    @Provides @Singleton
    MainRepository provideMainRepository(EDMService service, Authenticator authenticator) {
    EDMService authenticatedService = service.newBuilder()
        .setAuthentication(request -> {
        String email = account.name;
        String password = authenticator.getPassword(account);
        new BasicAuthentication(email, password).authenticate(request);
        })
        .build();
    return new MainRepository(authenticatedService);
    }

    @Provides @Singleton
    ImageLoader provideImageLoader(Portal portal, Picasso picasso, MainRepository repository, Cache cache) {
    return new ImageLoader(portal, picasso, repository, cache);
    }
}
