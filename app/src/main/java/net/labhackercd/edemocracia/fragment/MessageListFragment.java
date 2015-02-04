package net.labhackercd.edemocracia.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.liferay.mobile.android.v62.mbmessage.MBMessageService;

import org.json.JSONArray;

import java.util.List;

import javax.inject.Inject;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.content.Message;
import net.labhackercd.edemocracia.content.Thread;
import net.labhackercd.edemocracia.liferay.session.EDMSession;
import net.labhackercd.edemocracia.util.JSONReader;

public class MessageListFragment extends SimpleRecyclerViewFragment<Message> {
    public static String ARG_THREAD = "thread";

    @Inject EDMSession session;

    private Thread thread;

    public static MessageListFragment newInstance(Thread thread) {
        MessageListFragment fragment = new MessageListFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_THREAD, thread);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();

        if (args != null) {
            thread = args.getParcelable(ARG_THREAD);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.threadlike_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reply:
                return onReplySelected();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean onReplySelected() {
        ComposeFragment fragment = ComposeFragment.newInstance(thread);

        FragmentTransaction transaction = getActivity()
                .getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.container, fragment);

        transaction.addToBackStack(null);

        transaction.commit();

        return true;
    }

    @Override
    protected RecyclerView.Adapter createAdapter(List<Message> items) {
        return new MessageListAdapter(getActivity(), items);
    }

    @Override
    protected List<Message> blockingFetchItems() throws Exception {
        JSONArray messages = new MBMessageService(session)
                .getThreadMessages(thread.getGroupId(), thread.getCategoryId(), thread.getThreadId(), 0, -1, -1);

        return JSONReader.fromJSON(messages, Message.JSON_READER);
    }
}
