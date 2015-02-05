package net.labhackercd.edemocracia.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import javax.inject.Inject;

import net.labhackercd.edemocracia.application.EDMApplication;
import net.labhackercd.edemocracia.fragment.ThreadListFragment;
import net.labhackercd.edemocracia.fragment.GroupListFragment;
import net.labhackercd.edemocracia.fragment.MessageListFragment;
import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.content.*;
import net.labhackercd.edemocracia.content.Thread;
import net.labhackercd.edemocracia.task.AddMessageTask;
import net.labhackercd.edemocracia.task.AddMessageTaskQueue;
import net.labhackercd.edemocracia.task.VideoUploadTask;
import net.labhackercd.edemocracia.task.VideoUploadTaskQueue;
import net.labhackercd.edemocracia.ytdl.Constants;

import de.greenrobot.event.EventBus;

public class MainActivity extends ActionBarActivity {

    // NOTE: Injection starts queue processing!
    @Inject AddMessageTaskQueue addMessageTaskQueue;
    @Inject VideoUploadTaskQueue videoUploadTaskQueue;

    @Inject EventBus eventBus;

    private UploadBroadcastReceiver uploadBroadcastReceiver;

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
            transaction.add(R.id.container, new GroupListFragment());
            transaction.commit();
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

    public void addAddMessageTask(AddMessageTask task) {
        addMessageTaskQueue.add(task);
    }

    public void addVideoUploadTask(VideoUploadTask task) {
        videoUploadTaskQueue.add(task);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(AddMessageTask.Success event) {
        Toast.makeText(this, "Message submitted", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(AddMessageTask.Failure event) {
        // TODO FIXME Should we add the message to the queue again?
        // Or start the queue service again? What should we do!?
        Toast.makeText(this, "Failed to submit message", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ShowForumEvent event) {
        Forum forum = event.getForum();
        replaceMainFragment(ThreadListFragment.newInstance(forum));
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
}