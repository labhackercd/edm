package net.labhackercd.edemocracia;

import android.app.Application;

import net.labhackercd.edemocracia.account.AccountModule;
import net.labhackercd.edemocracia.data.DataModule;
import net.labhackercd.edemocracia.task.TaskModule;
import net.labhackercd.edemocracia.ui.UiModule;

import javax.inject.Singleton;

import dagger.Provides;

@dagger.Module(
        includes = {
                UiModule.class,
                TaskModule.class,
                DataModule.class,
                AccountModule.class
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
