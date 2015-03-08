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

import com.google.common.base.Joiner;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.ui.group.GroupListFragment;
import net.labhackercd.edemocracia.ui.message.MessageListFragment;
import net.labhackercd.edemocracia.ui.thread.ThreadListFragment;
import net.labhackercd.edemocracia.youtube.Constants;

public class MainActivity extends BaseActivity {

    private LocalBroadcastReceiver broadcastReceiver;

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

        LocalBroadcastManager broadcastManager =
                LocalBroadcastManager.getInstance(this);

        if (broadcastReceiver == null)
            broadcastReceiver = new LocalBroadcastReceiver();
        broadcastReceiver.register(broadcastManager);
    }

    private void replaceMainFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /** Receive broadcasts from the video upload component. */
    private class UploadBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }

    /** Local broadcasts. Mainly used for changing the content of the MainActivity. */

    /** Create an Intent to view a Group. */
    public static Intent createIntent(Context context, Group group) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType(MimeTypes.GROUP);
        intent.putExtra(EXTRA_GROUP, group);
        return intent;
    }

    /** Create an Intent to view a Category. */
    public static Intent createIntent(Context context, Category category) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType(MimeTypes.CATEGORY);
        intent.putExtra(EXTRA_CATEGORY, category);
        return intent;
    }

    /** Create an Intent to view a Thread. */
    public static Intent createIntent(Context context, Thread thread) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType(MimeTypes.THREAD);
        intent.putExtra(EXTRA_THREAD, thread);
        return intent;
    }

    private static final String EXTRA_GROUP = extraName("group");
    private static final String EXTRA_THREAD = extraName("thread");
    private static final String EXTRA_CATEGORY = extraName("category");

    private static String extraName(String key) {
        return Joiner.on('.').join(MainActivity.class.getCanonicalName(), key);
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getType();
            String action = intent.getAction();
            if (type.equals(MimeTypes.GROUP)) {
                Group group = (Group) intent.getSerializableExtra(EXTRA_GROUP);
                replaceMainFragment(ThreadListFragment.newInstance(group));
            } else if (type.equals(MimeTypes.CATEGORY)) {
                Category category = (Category) intent.getSerializableExtra(EXTRA_CATEGORY);
                replaceMainFragment(ThreadListFragment.newInstance(category));
            } else if (type.equals(MimeTypes.THREAD)) {
                Thread thread = (Thread) intent.getSerializableExtra(EXTRA_THREAD);
                replaceMainFragment(MessageListFragment.newInstance(thread));
            } else if (action.equals(Constants.REQUEST_AUTHORIZATION_INTENT)) {
                Intent toRun = intent.getParcelableExtra(
                        Constants.REQUEST_AUTHORIZATION_INTENT_PARAM);
                startActivityForResult(toRun, VideoPickerActivity.REQUEST_AUTHORIZATION);
            } else {
                throw new IllegalArgumentException("Unexpected intent: " + intent.toString());
            }
        }

        protected void register(LocalBroadcastManager manager) {
            manager.registerReceiver(
                    this, IntentFilter.create(Intent.ACTION_VIEW, MimeTypes.GROUP));
            manager.registerReceiver(
                    this, IntentFilter.create(Intent.ACTION_VIEW, MimeTypes.CATEGORY));
            manager.registerReceiver(
                    this, IntentFilter.create(Intent.ACTION_VIEW, MimeTypes.THREAD));
            manager.registerReceiver(
                    this, new IntentFilter(Constants.REQUEST_AUTHORIZATION_INTENT));
        }
    }
}