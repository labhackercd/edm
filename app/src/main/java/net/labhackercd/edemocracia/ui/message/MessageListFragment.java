package net.labhackercd.edemocracia.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import javax.inject.Inject;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.data.api.EDMService;
import net.labhackercd.edemocracia.data.api.model.Message;
import net.labhackercd.edemocracia.data.api.model.Thread;
import net.labhackercd.edemocracia.ui.SimpleRecyclerViewFragment;

public class MessageListFragment extends SimpleRecyclerViewFragment<Message> {

    private static final String ARG_THREAD = "thread";

    @Inject EDMService service;

    private Thread thread;

    public static MessageListFragment newInstance(Thread thread) {
        MessageListFragment fragment = new MessageListFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_THREAD, thread);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();

        if (args != null) {
            thread = (Thread) args.getSerializable(ARG_THREAD);
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
        return new MessageListAdapter(getActivity(), items);
    }

    @Override
    protected List<Message> blockingFetchItems() throws Exception {
        List<Message> messages = service.getThreadMessages(
                thread.getGroupId(), thread.getCategoryId(), thread.getThreadId());

        /*
        TODO Fetch authors of the messages

        EDMBatchSession batchSession = new EDMBatchSession(session);

        UserService userService = new UserService(batchSession);
        for (int i = 0; i < jsonMessages.length(); i++) {
            long userId = jsonMessages.getJSONObject(i).getLong("userId");
            userService.getUserById(userId);
        }

        try {
            JSONArray jsonUsers = batchSession.invoke();

            // Associate users with their respective messages
            for (int i = 0; i < jsonUsers.length(); i++) {
                jsonMessages.getJSONObject(i).put("user", jsonUsers.getJSONObject(i));
            }
        } catch (PrincipalException e) {
            // Ignore
        }


        User currentUser = session.getUser();

        for (Message i : messages) {
            if (i.getUserId() == currentUser.getUserId()) {
                i.setUser(currentUser);
            }
        }
        */

        return messages;
    }

    @Override
    protected Object getRefreshTaskTag() {
        return new Pair<Class, Long>(getClass(), thread.getRootMessageId());
    }
}
