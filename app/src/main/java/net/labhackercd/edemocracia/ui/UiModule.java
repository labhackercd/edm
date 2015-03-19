package net.labhackercd.edemocracia.ui;

import android.app.Application;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.ui.group.GroupListFragment;
import net.labhackercd.edemocracia.ui.message.ComposeActivity;
import net.labhackercd.edemocracia.ui.message.MessageListFragment;
import net.labhackercd.edemocracia.ui.preference.PreferenceActivity;
import net.labhackercd.edemocracia.ui.thread.ThreadListFragment;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;

import java.io.InputStream;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                MainActivity.class,
                ComposeActivity.class,
                GroupListFragment.class,
                PreferenceActivity.class,
                ThreadListFragment.class,
                MessageListFragment.class,
                SplashScreenActivity.class
        },
        library = true,
        complete = false
)
@SuppressWarnings("UnusedDeclaration")
public class UiModule {
    @Provides @Singleton
    TextProcessor provideTextProcessor(Application application) {
        InputStream input = null;
        try {
            input = application.getResources().openRawResource(R.raw.kefirbb);
            return BBProcessorFactory.getInstance().create(input);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable e) {
                    // Ignore.
                }
            }
        }
    }
}