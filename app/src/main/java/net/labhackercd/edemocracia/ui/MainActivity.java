package net.labhackercd.edemocracia.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.ui.group.GroupListFragment;
import net.labhackercd.edemocracia.ui.message.MessageListFragment;
import net.labhackercd.edemocracia.ui.thread.ThreadListFragment;
import net.labhackercd.edemocracia.youtube.Constants;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity {
    @Inject EventBus eventBus;

    private UploadBroadcastReceiver uploadBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        final FragmentManager fragmentManager = getSupportFragmentManager();

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                final boolean showBackButton = fragmentManager.getBackStackEntryCount() > 0;
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(showBackButton);
                    actionBar.setDisplayShowHomeEnabled(!showBackButton);
                }
            }
        });

        if (savedInstanceState == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.container, new GroupListFragment());
            transaction.commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Pop back stack (go to previous fragment) until we get to home,
        // then close the application.
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        } else {
            return super.onSupportNavigateUp();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        eventBus.register(this);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        if (uploadBroadcastReceiver == null) {
            uploadBroadcastReceiver = new UploadBroadcastReceiver();
        }
        broadcastManager.registerReceiver(
                uploadBroadcastReceiver, new IntentFilter(Constants.REQUEST_AUTHORIZATION_INTENT));

    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ShowCategoryEvent event) {
        Category category = event.getCategory();
        replaceMainFragment(ThreadListFragment.newInstance(category));
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ShowGroupEvent event) {
        Group group = event.getGroup();
        replaceMainFragment(ThreadListFragment.newInstance(group));
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ShowThreadEvent event) {
        Thread thread = event.getThread();
        replaceMainFragment(MessageListFragment.newInstance(thread));
    }

    protected void replaceMainFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private class UploadBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.REQUEST_AUTHORIZATION_INTENT)) {
                Intent toRun = intent
                        .getParcelableExtra(Constants.REQUEST_AUTHORIZATION_INTENT_PARAM);
                startActivityForResult(toRun, VideoPickerActivity.REQUEST_AUTHORIZATION);
            }
        }
    }

    public static class ShowThreadEvent {
        private final Thread thread;

        public ShowThreadEvent(Thread thread) {
            this.thread = thread;
        }

        public Thread getThread() {
            return thread;
        }
    }

    public static class ShowGroupEvent {
        private final Group group;

        public ShowGroupEvent(Group group) {
            this.group = group;
        }

        public Group getGroup() { return group; }
    }

    public static class ShowCategoryEvent {
        private final Category category;

        public ShowCategoryEvent(Category category) {
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }
    }
}