package br.leg.camara.labhacker.edemocracia;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity
        implements GroupListFragment.OnGroupSelectedListener,
                   ThreadListFragment.OnThreadSelectedListener,
                   MessageListFragment.OnMessageSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            GroupListFragment groupListFragment = new GroupListFragment();
            transaction.add(R.id.mainFrame, groupListFragment);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGroupSelected(Uri groupUri) {
        Log.v(getClass().getSimpleName(), "Group selected: " + groupUri.toString());

        ThreadListFragment fragment = ThreadListFragment.newInstance(groupUri);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFrame, fragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onThreadSelected(Uri companyUri, Uri groupUri, Uri threadUri) {
        Log.v(getClass().getSimpleName(), "Thread selected: " + threadUri.toString());

        MessageListFragment fragment = MessageListFragment.newInstance(companyUri, groupUri, threadUri);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFrame, fragment);

        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onMessageSelect(Uri messageUri) {
        Log.v(getClass().getSimpleName(), "Message selected: " + messageUri.toString());
    }
}