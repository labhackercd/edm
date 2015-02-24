package net.labhackercd.edemocracia.ui;

import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.ui.group.GroupListFragment;
import net.labhackercd.edemocracia.ui.message.ComposeActivity;
import net.labhackercd.edemocracia.ui.message.MessageListFragment;
import net.labhackercd.edemocracia.ui.thread.ThreadListFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                MainActivity.class,
                ComposeActivity.class,
                GroupListFragment.class,
                ThreadListFragment.class,
                MessageListFragment.class,
                SimpleRecyclerViewFragment.class
        },
        library = true,
        complete = false
)
@SuppressWarnings("UnusedDeclaration")
public class UiModule {
    private final User user;

    public UiModule(User user) {
        this.user = user;
    }

    @Provides @Singleton
    User provideUser() {
        return user;
    }
}
