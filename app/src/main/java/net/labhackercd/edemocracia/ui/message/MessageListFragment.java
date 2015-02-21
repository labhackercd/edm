package net.labhackercd.edemocracia.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.model.Message;
import net.labhackercd.edemocracia.data.model.Thread;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;
import net.labhackercd.edemocracia.data.api.GroupService;

public class MessageListFragment extends SimpleRecyclerViewFragment<Message> {
    private Thread thread;
    private static final String ARG_THREAD = "thread";

    @Inject Picasso picasso;
    @Inject GroupService groupService;

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
        inflater.inflate(R.menu.show_thread_menu, menu);
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
        Intent intent = new Intent(getActivity(), ComposeActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(ComposeActivity.PARENT_EXTRA, thread);
        startActivity(intent);
        return true;
    }

    @Override
    protected RecyclerView.Adapter createAdapter(List<Message> items) {
        return new MessageListAdapter(getActivity(), picasso, items);
    }

    @Override
    protected List<Message> blockingFetchItems() throws Exception {
        return groupService.getThreadMessages(
                thread.getGroupId(), thread.getCategoryId(), thread.getThreadId(), 0, -1, -1);
    }
}
