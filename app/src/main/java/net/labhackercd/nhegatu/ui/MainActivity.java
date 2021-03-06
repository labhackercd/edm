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

package net.labhackercd.nhegatu.ui;

import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.common.base.Joiner;

import net.labhackercd.nhegatu.R;
import net.labhackercd.nhegatu.account.AccountUtils;
import net.labhackercd.nhegatu.account.UserDataCache;
import net.labhackercd.nhegatu.data.ImageLoader;
import net.labhackercd.nhegatu.data.MainRepository;
import net.labhackercd.nhegatu.data.api.model.Category;
import net.labhackercd.nhegatu.data.api.model.Group;
import net.labhackercd.nhegatu.data.api.model.Thread;
import net.labhackercd.nhegatu.data.api.model.User;
import net.labhackercd.nhegatu.data.db.LocalMessage;
import net.labhackercd.nhegatu.data.model.Message;
import net.labhackercd.nhegatu.data.provider.EDMContract;
import net.labhackercd.nhegatu.ui.group.GroupListFragment;
import net.labhackercd.nhegatu.ui.message.MessageListFragment;
import net.labhackercd.nhegatu.ui.preference.PreferenceActivity;
import net.labhackercd.nhegatu.ui.preference.PreferenceFragment;
import net.labhackercd.nhegatu.ui.thread.ThreadListFragment;
import com.google.common.collect.Lists;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import java.util.List;

// TODO Use Home button to navigate up, just like GMail does.
public class MainActivity extends BaseActivity {

    // TODO Change to android.R.id.content
    private static final int CONTENT_RESOURCE_ID = R.id.container;

    @Inject ImageLoader imageLoader;
    @Inject MainRepository repository;
    private ActionBarDrawerToggle drawerToggle;
    private LocalBroadcastReceiver broadcastReceiver;

    @InjectView(R.id.drawer) DrawerLayout drawer;
    @InjectView(R.id.drawer_list) RecyclerView drawerList;
    @InjectView(R.id.profile_name_text) TextView userNameView;
    @InjectView(R.id.profile_email_text) TextView userEmailView;
    @InjectView(R.id.profile_image) ImageView userImageView;
    private Subscription fillDrawerSubscription;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Setup the drawer list.
        drawerList.setLayoutManager(new LinearLayoutManager(
                drawerList.getContext(), LinearLayoutManager.VERTICAL, false));

        // Only handle intent if the Activity is actually being created!
        if (savedInstanceState == null) {
            Intent intent = getIntent();

            if (intent == null
                    || Intent.ACTION_MAIN.equals(intent.getAction())
                    || intent.getAction() == null && intent.getType() == null) {
                intent = createIntent(this);
            }

            if (!(Intent.ACTION_VIEW.equals(intent.getAction()) && handleViewIntent(intent, false))) {
                throw new UnsupportedOperationException(String.format("Unable to handle intent: %s", intent));
            }
        }
    }

    private boolean handleViewIntent(Intent intent) {
        return handleViewIntent(intent, true);
    }

    private boolean handleViewIntent(Intent intent, boolean addToBackStack) {
        Uri data = intent.getData();
        if (data != null && data.getScheme() != null && data.getScheme().startsWith("http")) {
            List<String> segments = data.getPathSegments();
            if (segments.contains("message")) {
                long messageId;
                try {
                    messageId = Long.valueOf(segments.get(segments.indexOf("message") + 1));
                } catch (Exception e) {
                    Timber.e(e, "Failed to extract message id from url");
                    return false;
                }
                replaceContent(MessageListFragment.newInstance(messageId), addToBackStack);
                return true;
            }
        }

        String type = intent.getType();

        if (type != null) {
            switch (type) {
                case EDMContract.Group.CONTENT_TYPE:
                    replaceContent(new GroupListFragment(), addToBackStack);
                    return true;
                case EDMContract.Group.CONTENT_ITEM_TYPE:
                case EDMContract.Category.CONTENT_ITEM_TYPE:
                    replaceContent(createThreadListFragment(this, intent), addToBackStack);
                    return true;
                case EDMContract.Thread.CONTENT_ITEM_TYPE:
                case EDMContract.Message.CONTENT_ITEM_TYPE:
                    replaceContent(createMessageListFragment(this, intent), addToBackStack);
                    return true;
            }
        }

        return false;
    }

    private static Fragment createThreadListFragment(Context context, Intent intent) {
        Group group = (Group) intent.getParcelableExtra(EXTRA_GROUP);
        if (group != null) {
            return ThreadListFragment.newInstance(group);
        } else {
            Category category = (Category) intent.getParcelableExtra(EXTRA_CATEGORY);
            if (category != null) {
                return ThreadListFragment.newInstance(category);
            } else {
                Uri uri = intent.getData();
                if (uri != null) {
                    return ThreadListFragment.newInstance(context, uri);
                } else {
                    throw new IllegalStateException("Unable to create fragment from intent: " + intent);
                }
            }
        }
    }

    private static Fragment createMessageListFragment(Context context, Intent intent) {
        Thread thread = (Thread) intent.getParcelableExtra(EXTRA_THREAD);
        if (thread != null) {
            return MessageListFragment.newInstance(thread);
        } else {
            LocalMessage localMessage = intent.getParcelableExtra(EXTRA_MESSAGE);
            if (localMessage != null) {
                return MessageListFragment.newInstance(localMessage);
            } else {
                // TODO
                throw new UnsupportedOperationException();
            }
        }
    }

    private int replaceContent(Fragment fragment, boolean addToBackStack) {
        // TODO Also allow the user to build a *custom* back stack.
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(CONTENT_RESOURCE_ID, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        return transaction.commit();
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

        NavDrawerAdapter adapter = new NavDrawerAdapter(drawer, getDrawerItems(drawerList.getContext()));
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

        if (fillDrawerSubscription == null || fillDrawerSubscription.isUnsubscribed()) {
            fillDrawerSubscription = AccountUtils.getOrRequestAccount(this)
                    .flatMap(account -> repository.getUser()
                            .transform(r -> r.asObservable()
                                    .compose(UserDataCache.with(this, account).getCached("currentUser")))
                            .asObservable()
                            .map(UserInfo::create)
                            .startWith(new UserInfo(account.name)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::fillDrawerUserInfo, (error) -> {
                        // TODO Proper error handling!
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void fillDrawerUserInfo(UserInfo info) {
        userNameView.setText(info.name);
        userEmailView.setText(info.email);
        imageLoader.userPortrait(info.portraitId)
                .fit().centerCrop()
                .into(userImageView);
    }

    private static class UserInfo {
        public final String name;
        public final String email;
        public final long portraitId;

        private UserInfo(String email) {
            this(null, email, 0);
        }

        private UserInfo(String name, String email, long portraitId) {
            this.name = name;
            this.email = email;
            this.portraitId = portraitId;
        }

        private static UserInfo create(User user) {
            return new UserInfo(getUserDisplayName(user), user.getEmailAddress(), user.getPortraitId());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (broadcastReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);

        if (fillDrawerSubscription != null) {
            fillDrawerSubscription.unsubscribe();
            fillDrawerSubscription = null;
        }
    }

    public static String getUserDisplayName(User user) {
        String userName = Joiner.on(' ').join(
                user.getFirstName(), user.getMiddleName(), user.getLastName());
        if (TextUtils.isEmpty(userName.trim()))
            userName = user.getScreenName();
        return userName;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /** App notifications will be place here for now. */

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

    private static final String EXTRA_GROUP = extraName("group");
    private static final String EXTRA_THREAD = extraName("thread");
    private static final String EXTRA_MESSAGE = extraName("message");
    private static final String EXTRA_CATEGORY = extraName("category");

    private static String extraName(String key) {
        return MainActivity.class.getCanonicalName().concat(".extra.").concat(key);
    }

    /** Create an intent to view the group list. */
    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(EDMContract.Group.CONTENT_TYPE);
        return intent;
    }

    /** Create an Intent to view a Group. */
    public static Intent createIntent(Context context, Group group) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(EDMContract.Group.CONTENT_ITEM_TYPE);
        intent.putExtra(EXTRA_GROUP, (Parcelable) group);
        return intent;
    }

    /** Create an Intent to view a Category. */
    public static Intent createIntent(Context context, Category category) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(EDMContract.Category.CONTENT_ITEM_TYPE);
        intent.putExtra(EXTRA_CATEGORY, (Parcelable) category);
        return intent;
    }

    /** Create an Intent to view a Thread. */
    public static Intent createIntent(Context context, Thread thread) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(EDMContract.Thread.CONTENT_ITEM_TYPE);
        intent.putExtra(EXTRA_THREAD, (Parcelable) thread);
        return intent;
    }


    /** Create an Intent to view a LocalMessage. */
    public static Intent createIntent(Context context, LocalMessage message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(EDMContract.Message.CONTENT_ITEM_TYPE);
        intent.putExtra(EXTRA_MESSAGE, (Parcelable) message);
        return intent;
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_VIEW.equals(intent.getAction()) && handleViewIntent(intent))
                Timber.d("Intent handled: %s", intent);
            else
                Timber.w("Unhandled intent: %s", intent);
        }

        protected void register(LocalBroadcastManager manager) {
            manager.registerReceiver(
                    this, IntentFilter.create(Intent.ACTION_VIEW, EDMContract.Group.CONTENT_TYPE));
            manager.registerReceiver(
                    this, IntentFilter.create(Intent.ACTION_VIEW, EDMContract.Group.CONTENT_ITEM_TYPE));
            manager.registerReceiver(
                    this, IntentFilter.create(Intent.ACTION_VIEW, EDMContract.Category.CONTENT_ITEM_TYPE));
            manager.registerReceiver(
                    this, IntentFilter.create(Intent.ACTION_VIEW, EDMContract.Thread.CONTENT_ITEM_TYPE));
        }
    }

    private static List<NavDrawerItem> getDrawerItems(Context context) {
        return Lists.newArrayList(
                createDrawerItem(
                        context.getString(R.string.title_group_list),
                        R.drawable.ic_forum_black_24dp,
                        MainActivity::onClickHome),
                // TODO Make "back" and "up" consistent in the Preference Screen
                createDrawerItem(
                        context.getString(R.string.title_preferences),
                        R.drawable.ic_settings_black_24dp,
                        MainActivity::onClickPreferences)
                /*
                TODO About screen.
                createDrawerItem(
                        context.getString(R.string.title_about),
                        R.drawable.ic_info_black_24dp,
                        MainActivity::onClickAbout)
                        */
        );
    }

    private static NavDrawerItem createDrawerItem(final String title, final int icon,
                                                  final NavDrawerItem.OnClickListener onClickListener) {
        return new NavDrawerItem() {
            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public int getIcon() {
                return icon;
            }

            @Override
            public OnClickListener getOnClickListener() {
                return onClickListener;
            }
        };
    }

    private static void onClickHome(DrawerLayout drawer, View view) {
        Context context = view.getContext();
        Intent intent = MainActivity.createIntent(context);
        if (LocalBroadcastManager.getInstance(context).sendBroadcast(intent))
            drawer.closeDrawers();
    }

    private static void onClickPreferences(DrawerLayout drawer, View view) {
        Context context = view.getContext();
        Intent intent = new Intent(context, PreferenceActivity.class);
        context.startActivity(intent);
    }

    private static void onClickAbout(DrawerLayout drawer, View view) {
        Timber.d("%s", view.getAnimation());
        // TODO Show the about screen.
    }
}