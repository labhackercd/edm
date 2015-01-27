package br.leg.camara.labhacker.edemocracia;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import br.leg.camara.labhacker.edemocracia.content.*;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.tasks.AddMessageFailureEvent;
import br.leg.camara.labhacker.edemocracia.tasks.AddMessageSuccessEvent;
import br.leg.camara.labhacker.edemocracia.tasks.AddMessageTaskQueue;

public class MainActivity extends Activity
        implements GroupListFragment.OnGroupSelectedListener,
                   ThreadListFragment.OnThreadSelectedListener,
                   MessageListFragment.OnMessageSelectedListener {

    // NOTE: Injection starts queue processing!
    @Inject AddMessageTaskQueue addMessageTaskQueue;

    @Inject Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((EDMApplication) getApplication()).inject(this);

        Log.i(getClass().getSimpleName(), addMessageTaskQueue + ":" + bus);

        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            GroupListFragment groupListFragment = new GroupListFragment();
            transaction.add(R.id.container, groupListFragment);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bus.register(this);
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

    @Subscribe
    @SuppressWarnings("UnusedDeclaration") // Used by the event bus.
    public void onAddMessageSuccess(AddMessageSuccessEvent event) {
        Toast.makeText(this, "Message submitted", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    @SuppressWarnings("UnusedDeclaration") // Used by the event bus.
    public void onAddMessageFailure(AddMessageFailureEvent event) {
        // TODO FIXME Should we add the message to the queue again? Or start
        // the queue service again? What should we do!?
        //Toast.makeText(this, "Failed to submit message", Toast.LENGTH_SHORT).show();
    }
}