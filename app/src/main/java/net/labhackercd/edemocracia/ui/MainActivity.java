package net.labhackercd.edemocracia.ui;

import android.app.NotificationManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
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

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.common.base.Joiner;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.account.AccountUtils;
import net.labhackercd.edemocracia.account.UserDataCache;
import net.labhackercd.edemocracia.data.ImageLoader;
import net.labhackercd.edemocracia.data.MainRepository;
import net.labhackercd.edemocracia.data.api.model.Category;
import net.labhackercd.edemocracia.data.api.model.Group;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.data.api.model.User;
import net.labhackercd.edemocracia.data.db.LocalMessage;
import net.labhackercd.edemocracia.data.model.Message;
import net.labhackercd.edemocracia.data.provider.EDMContract;
import net.labhackercd.edemocracia.ui.group.GroupListFragment;
import net.labhackercd.edemocracia.ui.message.MessageListFragment;
import net.labhackercd.edemocracia.ui.preference.PreferenceFragment;
import net.labhackercd.edemocracia.ui.thread.ThreadListFragment;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

// TODO Use Home button to navigate up, just like GMail does.
public class MainActivity extends BaseActivity {

    private static final String STATE_SELECTED_POSITION = "selectedNavItem";

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

            if (!(Intent.ACTION_VIEW.equals(intent.getAction()) && handleViewIntent(intent))) {
                throw new UnsupportedOperationException(String.format("Unable to handle intent: %s", intent));
            }
        }
    }

    private boolean handleViewIntent(Intent intent) {
        String type = intent.getType();

        if (type == null)
            return false;

        switch (type) {
            case EDMContract.Group.CONTENT_TYPE:
                replaceContent(new GroupListFragment());
                return true;
            case EDMContract.Group.CONTENT_ITEM_TYPE:
            case EDMContract.Category.CONTENT_ITEM_TYPE:
                replaceContent(createThreadListFragment(this, intent));
                return true;
            case EDMContract.Thread.CONTENT_ITEM_TYPE:
            case EDMContract.Message.CONTENT_ITEM_TYPE:
                replaceContent(createMessageListFragment(this, intent));
                return true;
            default:
                return false;
        }
    }

    private static Fragment createThreadListFragment(Context context, Intent intent) {
        Group group = (Group) intent.getSerializableExtra(EXTRA_GROUP);
        if (group != null) {
            return ThreadListFragment.newInstance(group);
        } else {
            Category category = (Category) intent.getSerializableExtra(EXTRA_CATEGORY);
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
        Thread thread = (Thread) intent.getSerializableExtra(EXTRA_THREAD);
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

    private int replaceContent(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(CONTENT_RESOURCE_ID, fragment);
        // TODO Parametrize the addition to the back stack.
        // TODO Also allow the user to build a *custom* back stack.
        transaction.addToBackStack(null);
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

        if (fillDrawerSubscription == null || fillDrawerSubscription.isUnsubscribed()) {
            fillDrawerSubscription = AccountUtils.getOrRequestAccount(this)
                    .flatMap(account -> repository.getUser()
                            .asObservable()
                            .compose(UserDataCache.with(this, account).cache(null))
                            .map(UserInfo::create)
                            .startWith(new UserInfo(account.name))
                            .subscribeOn(Schedulers.io()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::fillDrawerUserInfo);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION,
                ((NavigationDrawerAdapter) drawerList.getAdapter()).getSelectedItemPosition());
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
        intent.putExtra(EXTRA_GROUP, group);
        return intent;
    }

    /** Create an Intent to view a Category. */
    public static Intent createIntent(Context context, Category category) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(EDMContract.Category.CONTENT_ITEM_TYPE);
        intent.putExtra(EXTRA_CATEGORY, category);
        return intent;
    }

    /** Create an Intent to view a Thread. */
    public static Intent createIntent(Context context, Thread thread) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(EDMContract.Thread.CONTENT_ITEM_TYPE);
        intent.putExtra(EXTRA_THREAD, thread);
        return intent;
    }


    /** Create an Intent to view a LocalMessage. */
    private static Intent createIntent(Context context, LocalMessage message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        //intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType(EDMContract.Message.CONTENT_ITEM_TYPE);
        intent.putExtra(EXTRA_MESSAGE, (Message) message);
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
}