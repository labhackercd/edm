package net.labhackercd.edemocracia.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.common.base.Joiner;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.account.AccountUtils;
import net.labhackercd.edemocracia.account.UserData;
import net.labhackercd.edemocracia.data.ImageLoader;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.db.LocalMessage;
import net.labhackercd.edemocracia.ui.group.GroupListFragment;
import net.labhackercd.edemocracia.ui.message.MessageListFragment;
import net.labhackercd.edemocracia.ui.preference.PreferenceFragment;
import net.labhackercd.edemocracia.ui.thread.ThreadListFragment;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    @Inject UserData userData;
    @Inject ImageLoader imageLoader;
    private ActionBarDrawerToggle drawerToggle;
    private LocalBroadcastReceiver broadcastReceiver;

    @InjectView(R.id.drawer) DrawerLayout drawer;
    @InjectView(R.id.drawer_list) RecyclerView drawerList;
    @InjectView(R.id.profile_name_text) TextView userNameView;
    @InjectView(R.id.profile_email_text) TextView userEmailView;
    @InjectView(R.id.profile_image) ImageView userImageView;

    private static final java.lang.String STATE_SELECTED_POSITION = "selectedNavItem";

    private static final int REQUEST_GOOGLE_CREDENTIAL_AUTHORIZATION = 45189;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        // Setup the drawer list.
        drawerList.setLayoutManager(new LinearLayoutManager(
                drawerList.getContext(), LinearLayoutManager.VERTICAL, false));

        // TODO Use Home button to navigate up, just like GMail does.

        // Insert the default fragment
        final FragmentManager fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.container, new GroupListFragment());
            transaction.commit();
        }

        // TODO Dedup this.
        Intent intent = getIntent();

        String type = intent.getType();
        String action = intent.getAction();

        if (MimeTypes.MESSAGE.equals(type)) {
            LocalMessage message = intent.getParcelableExtra(EXTRA_MESSAGE);
            replaceMainFragment(MessageListFragment.newInstance(message));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        drawerToggle = new ActionBarDrawerToggle(
                this, drawer, R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                /* TODO USER_HAS_LEARNED_DRAWER
                if (!userHasLearnedDrawer) {
                    userHasLearnedDrawer = true;
                    saveSharedStateSetting(activity, PREFERENCE_USER_HAS_LEARNED_DRAWER, true);
                }
                 */
                invalidateOptionsMenu();
            }
        };

        drawer.post(drawerToggle::syncState);
        drawer.setDrawerListener(drawerToggle);

        /* TODO USER_HAS_LEARNED_DRAWER
        if (!userHasLearnedDrawer && !savedInstanceState)
            drawer.openDrawer();
         */

        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter();
        adapter.setSelectionListener(new NavigationDrawerAdapter.SelectionListener() {
            @Override
            public void onItemSelected(View view, int position) {
                // TODO This really shouldn't be tied to the gravity only.
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        if (savedInstanceState != null) {
            int selected = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            if (adapter.getSelectedItemPosition() != selected)
                adapter.setSelectedItem(selected);
        }

        drawerList.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (broadcastReceiver == null)
            broadcastReceiver = new LocalBroadcastReceiver();
        broadcastReceiver.register(LocalBroadcastManager.getInstance(this));

        User user = AccountUtils.getUser(userData, this);

        userNameView.setText(getUserDisplayName(user));
        userEmailView.setText(user.getEmailAddress());

        imageLoader.userPortrait(user.getPortraitId())
                .fit()
                .centerCrop()
                .into(userImageView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (broadcastReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION,
                ((NavigationDrawerAdapter) drawerList.getAdapter()).getSelectedItemPosition());
    }

    private String getUserDisplayName(User user) {
        String userName = Joiner.on(' ').join(
                user.getFirstName(), user.getMiddleName(), user.getLastName());
        if (TextUtils.isEmpty(userName.trim()))
            userName = user.getScreenName();
        return userName;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If the DrawerToggle can handle this item, we just let it.
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    private void replaceMainFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /** App notifications will be place here for now. */

    public static void notifyMessageSubmissionFailure(
            Context context, LocalMessage message, Throwable error) {
        Intent intent = createIntent(context, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.message_submission_failed))
                .setSmallIcon(R.drawable.ic_videocam_white_36dp)
                .setContentIntent(pendingIntent);

        getSupportNotificationManager(context).notify(0, builder.build());
    }

    public static void notifyUserRecoverableAuthException(
            Context context, LocalMessage message, UserRecoverableAuthException exception) {
        Intent intent = PreferenceFragment.newRecoverIntent(context, exception);

        // TODO What I want here is:
        // [.. Everything the user would do to navigate to the message ...]
        //     > Message > PreferenceActivity > AUTHORIZE
        // Once the authorization is done the user should be redirected to the message.
        // I really don't know if this is the way to do that, but what can I do?
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(createIntent(context, message));
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder
                .getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.message_submission_failed))
                .setSmallIcon(R.drawable.ic_videocam_white_36dp)
                .setContentIntent(pendingIntent);

        getSupportNotificationManager(context).notify(1, builder.build());
    }

    private static NotificationManager getSupportNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /** Local broadcasts. Mainly used for changing the content of the MainActivity. */

    /** Create an intent to view the group list. */
    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_DEFAULT);
        return intent;
    }

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


    /** Create an Intent to view a LocalMessage. */
    private static Intent createIntent(Context context, LocalMessage message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setType(MimeTypes.MESSAGE);
        intent.putExtra(EXTRA_MESSAGE, message);
        return intent;
    }

    private static final String EXTRA_GROUP = extraName("group");
    private static final String EXTRA_THREAD = extraName("thread");
    private static final String EXTRA_MESSAGE = extraName("message");
    private static final String EXTRA_CATEGORY = extraName("category");

    private static String extraName(String key) {
        return Joiner.on('.').join(MainActivity.class.getCanonicalName(), key);
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getType();
            String action = intent.getAction();
            Timber.d("Received intent for %s, %s.", type, action);
            if (MimeTypes.GROUP.equals(type)) {
                Group group = (Group) intent.getSerializableExtra(EXTRA_GROUP);
                replaceMainFragment(ThreadListFragment.newInstance(group));
            } else if (MimeTypes.CATEGORY.equals(type)) {
                Category category = (Category) intent.getSerializableExtra(EXTRA_CATEGORY);
                replaceMainFragment(ThreadListFragment.newInstance(category));
            } else if (MimeTypes.THREAD.equals(type)) {
                Thread thread = (Thread) intent.getSerializableExtra(EXTRA_THREAD);
                replaceMainFragment(MessageListFragment.newInstance(thread));
            } else if (MimeTypes.MESSAGE.equals(type)) {
                Timber.d("Displaying message...");
                LocalMessage message = intent.getParcelableExtra(EXTRA_MESSAGE);
                replaceMainFragment(MessageListFragment.newInstance(message));
            } else if (Intent.ACTION_DEFAULT.equals(action)) {
                // TODO Only if it's not the current fragment already?
                Timber.d("Default action.");
                replaceMainFragment(new GroupListFragment());
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
                    this, IntentFilter.create(Intent.ACTION_VIEW, MimeTypes.MESSAGE));
            manager.registerReceiver(
                    this, new IntentFilter(Intent.ACTION_DEFAULT));
        }
    }
}