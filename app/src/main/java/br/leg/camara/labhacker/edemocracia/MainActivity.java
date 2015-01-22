package br.leg.camara.labhacker.edemocracia;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import br.leg.camara.labhacker.edemocracia.content.*;
import br.leg.camara.labhacker.edemocracia.content.Thread;

public class MainActivity extends Activity
        implements GroupListFragment.OnGroupSelectedListener,
                   ThreadListFragment.OnThreadSelectedListener,
                   MessageListFragment.OnMessageSelectedListener {

    public static final String VIDEO_ATTACHMENT_FRAGMENT = "videoAttachmentFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if (savedInstanceState == null) {

            VideoAttachmentFragment frag = new VideoAttachmentFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(frag, VIDEO_ATTACHMENT_FRAGMENT);
            transaction.commit();

            transaction = getFragmentManager().beginTransaction();
            GroupListFragment groupListFragment = new GroupListFragment();
            transaction.add(R.id.container, groupListFragment);
            transaction.commit();
        }
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

    @Override
    public void onGroupSelected(Group group) {
        ThreadListFragment fragment = ThreadListFragment.newInstance(group);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onThreadSelected(Thread thread) {
        MessageListFragment fragment = MessageListFragment.newInstance(thread);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onMessageSelect(Message message) {
        Log.v(getClass().getSimpleName(), "Message selected: " + message);
    }
}