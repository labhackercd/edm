package net.labhackercd.edemocracia.ui;

import net.labhackercd.edemocracia.ui.group.GroupListFragment;
import net.labhackercd.edemocracia.ui.message.ComposeActivity;
import net.labhackercd.edemocracia.ui.message.MessageListFragment;
import net.labhackercd.edemocracia.ui.thread.ThreadListFragment;

import dagger.Module;

@Module(
        injects = {
                MainActivity.class,
                ComposeActivity.class,
                GroupListFragment.class,
                ThreadListFragment.class,
                SplashScreenActivity.class,
                MessageListFragment.class
        },
        library = true,
        complete = false
)
@SuppressWarnings("UnusedDeclaration")
public class UiModule {
}