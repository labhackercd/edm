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

package net.labhackercd.nhegatu;

import android.app.Application;

import net.labhackercd.nhegatu.account.AccountManager;
import net.labhackercd.nhegatu.data.DataModule;
import net.labhackercd.nhegatu.service.ServiceModule;
import net.labhackercd.nhegatu.ui.UiModule;

import javax.inject.Singleton;

import dagger.Provides;
import net.labhackercd.nhegatu.upload.UploadModule;

@dagger.Module(
        includes = {
                UiModule.class,
                DataModule.class,
                ServiceModule.class,
                UploadModule.class
        }
)
@SuppressWarnings("UnusedDeclaration")
public class EDMRootModule {
    private final EDMApplication application;

    EDMRootModule(EDMApplication application) {
        this.application = application;
    }

    @Provides @Singleton
    Application provideApplication() {
        return application;
    }

    @Provides @Singleton
    AccountManager provideAccountManager(Application application) {
        return AccountManager.get(application.getApplicationContext());
    }
}
