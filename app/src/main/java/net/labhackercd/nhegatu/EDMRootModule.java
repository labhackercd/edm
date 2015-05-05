package net.labhackercd.nhegatu;

import android.app.Application;

import net.labhackercd.nhegatu.account.AccountModule;
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
                AccountModule.class,
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
}
