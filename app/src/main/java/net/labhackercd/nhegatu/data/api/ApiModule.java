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

package net.labhackercd.nhegatu.data.api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

import com.liferay.mobile.android.auth.basic.BasicAuthentication;

import net.labhackercd.nhegatu.account.AccountUtils;
import net.labhackercd.nhegatu.data.api.client.EDMService;
import net.labhackercd.nhegatu.data.api.client.EDMServiceImpl;
import net.labhackercd.nhegatu.data.api.client.Endpoint;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import net.labhackercd.nhegatu.data.api.error.EDMErrorHandler;

@Module(complete = false, library = true)
@SuppressWarnings("UnusedDeclaration")
public class ApiModule {
    private static final String PRODUCTION_PORTAL_URL = "https://edemocracia.camara.gov.br";
    private static final String PRODUCTION_API_URL = "/api/secure/jsonws";

    @Provides @Singleton
    Portal providePortal() {
        return () -> PRODUCTION_PORTAL_URL;
    }

    @Provides @Singleton
    Endpoint provideEndpoint(Portal portal) {
        return () -> portal.url() + PRODUCTION_API_URL;
    }

    @Provides @Singleton
    EDMService provideEDMServiceImpl(final Application application, Endpoint endpoint) {
        return new EDMServiceImpl.Builder()
                .setEndpoint(endpoint)
                .setErrorHandler(new EDMErrorHandler())
                .setAuthentication(request -> {
                    Account account = AccountUtils.getAccount(application);
                    if (account != null) {
                        AccountManager manager = AccountManager.get(application);
                        String email = account.name;
                        String password = manager.getPassword(account);
                        new BasicAuthentication(email, password).authenticate(request);
                    }
                })
                .build();
    }
}