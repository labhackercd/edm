package net.labhackercd.edemocracia.job;

import android.app.Application;

import com.path.android.jobqueue.BaseJob;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.di.DependencyInjector;
import com.path.android.jobqueue.log.CustomLogger;

import net.labhackercd.edemocracia.BuildConfig;
import net.labhackercd.edemocracia.EDMApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module(
        injects = AddMessageJob.class,
        complete = false,
        library = true
)
@SuppressWarnings("UnusedDeclaration")
public class JobModule {
    @Provides
    @Singleton
    JobManager provideJobManager(final Application application) {
        Configuration configuration = new Configuration.Builder(application)
                .customLogger(new CustomLogger() {
                    @Override
                    public boolean isDebugEnabled() {
                        return BuildConfig.DEBUG;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Timber.d(text, args);
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Timber.e(t, text, args);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Timber.e(text, args);
                    }
                })
                .minConsumerCount(0)
                .maxConsumerCount(3)
                .loadFactor(3)
                .consumerKeepAlive(3)
                .injector(new DependencyInjector() {
                    @Override
                    public void inject(BaseJob job) {
                        EDMApplication.get(application).inject(job);
                    }
                })
                .build();
        return new JobManager(application, configuration);
    }
}
