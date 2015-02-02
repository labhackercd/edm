package net.labhackercd.edemocracia.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import net.labhackercd.edemocracia.application.EDMApplication;
import net.labhackercd.edemocracia.fragment.ThreadListFragment;
import net.labhackercd.edemocracia.fragment.GroupListFragment;
import net.labhackercd.edemocracia.fragment.MessageListFragment;
import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.content.*;
import net.labhackercd.edemocracia.content.Thread;
import net.labhackercd.edemocracia.task.AddMessageFailureEvent;
import net.labhackercd.edemocracia.task.AddMessageSuccessEvent;
import net.labhackercd.edemocracia.task.AddMessageTask;
import net.labhackercd.edemocracia.task.AddMessageTaskQueue;
import net.labhackercd.edemocracia.task.VideoUploadTask;
import net.labhackercd.edemocracia.task.VideoUploadTaskQueue;
import net.labhackercd.edemocracia.ytdl.Constants;

public class MainActivity extends ActionBarActivity {

    public static final String SHOW_GROUP_INTENT = "net.labhackercd.edemocracia.MainActivity.ShowGroup";
    public static final String SHOW_GROUP_INTENT_PARAM = "net.labhackercd.edemocracia.MainActivity.ShowGroup.group";

    public static final String SHOW_THREAD_INTENT = "net.labhackercd.edemocracia.MainActivity.ShowThread";
    public static final String SHOW_THREAD_INTENT_PARAM = "net.labhackercd.edemocracia.MainActivity.ShowThread.thread";

    // NOTE: Injection starts queue processing!
    @Inject AddMessageTaskQueue addMessageTaskQueue;
    @Inject VideoUploadTaskQueue videoUploadTaskQueue;

    @Inject Bus bus;

    private UploadBroadcastReceiver uploadBroadcastReceiver;
    private ShowGroupBroadcastReceiver showGroupBroadcastReceiver;
    private ShowThreadBroadcastReceiver showThreadBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((EDMApplication) getApplication()).inject(this);

        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            GroupListFragment groupListFragment = new GroupListFragment();
            transaction.add(R.id.container, groupListFragment);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        bus.register(this);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        if (uploadBroadcastReceiver == null) {
            uploadBroadcastReceiver = new UploadBroadcastReceiver();
        }
        broadcastManager.registerReceiver(
                uploadBroadcastReceiver, new IntentFilter(Constants.REQUEST_AUTHORIZATION_INTENT));

        if (showGroupBroadcastReceiver == null) {
            showGroupBroadcastReceiver = new ShowGroupBroadcastReceiver();
        }
        broadcastManager.registerReceiver(
                showGroupBroadcastReceiver, new IntentFilter(SHOW_GROUP_INTENT));

        if (showThreadBroadcastReceiver == null) {
            showThreadBroadcastReceiver = new ShowThreadBroadcastReceiver();
        }
        broadcastManager.registerReceiver(
                showThreadBroadcastReceiver, new IntentFilter(SHOW_THREAD_INTENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        bus.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void onForumSelected(Forum forum) {
        ThreadListFragment fragment = ThreadListFragment.newInstance(forum);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    public void onThreadSelected(Thread thread) {
        MessageListFragment fragment = MessageListFragment.newInstance(thread);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Subscribe
    @SuppressWarnings("UnusedDeclaration") // Used by the event bus
    public void onAddMessageSuccess(AddMessageSuccessEvent event) {
        Toast.makeText(this, "Message submitted", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    @SuppressWarnings("UnusedDeclaration") // Used by the event bus
    public void onAddMessageFailure(AddMessageFailureEvent event) {
        // TODO FIXME Should we add the message to the queue again?
        // Or start the queue service again? What should we do!?
        Toast.makeText(this, "Failed to submit message", Toast.LENGTH_SHORT).show();
    }

    public void addAddMessageTask(AddMessageTask task) {
        addMessageTaskQueue.add(task);
    }

    public void addVideoUploadTask(VideoUploadTask task) {
        videoUploadTaskQueue.add(task);
    }

    public static Intent getIntent(Context context, Forum group) {
        Intent intent = new Intent(SHOW_GROUP_INTENT);
        intent.putExtra(SHOW_GROUP_INTENT_PARAM, group);
        return intent;
    }

    public static Intent getIntent(Context context, Thread thread) {
        Intent intent = new Intent(SHOW_THREAD_INTENT);
        intent.putExtra(SHOW_THREAD_INTENT_PARAM, thread);
        return intent;
    }

    private class ShowGroupBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onForumSelected((Forum) intent.getParcelableExtra(SHOW_GROUP_INTENT_PARAM));
        }
    }

    private class ShowThreadBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onThreadSelected((Thread) intent.getParcelableExtra(SHOW_THREAD_INTENT_PARAM));
        }
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
}