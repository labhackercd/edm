package net.labhackercd.edemocracia.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.liferay.mobile.android.v62.user.UserService;

import org.json.JSONArray;

import java.util.List;

import javax.inject.Inject;

import net.labhackercd.edemocracia.R;
import net.labhackercd.edemocracia.activity.ComposeActivity;
import net.labhackercd.edemocracia.content.Message;
import net.labhackercd.edemocracia.content.Thread;
import net.labhackercd.edemocracia.content.User;
import net.labhackercd.edemocracia.fragment.simplerecyclerview.SimpleRecyclerViewFragment;
import net.labhackercd.edemocracia.liferay.exception.PrincipalException;
import net.labhackercd.edemocracia.liferay.session.EDMBatchSession;
import net.labhackercd.edemocracia.liferay.session.EDMSession;
import net.labhackercd.edemocracia.util.JSONReader;

public class MessageListFragment extends SimpleRecyclerViewFragment<Message> {

    @Inject EDMSession session;

    private Thread thread;

    private static final String ARG_THREAD = "thread";

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
        return new MessageListAdapter(getActivity(), items);
    }

    @Override
    protected List<Message> blockingFetchItems() throws Exception {
        EDMBatchSession batchSession = new EDMBatchSession(session);

        JSONArray jsonMessages = new MBMessageService(session)
                .getThreadMessages(
                        thread.getGroupId(), thread.getCategoryId(),
                        thread.getThreadId(), 0, -1, -1);

        if (jsonMessages == null) {
            jsonMessages = new JSONArray();
        }

        // Fetch users associated with each message
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

        List<Message> messages = JSONReader.fromJSON(jsonMessages, Message.JSON_READER);
        for (Message i : messages) {
            if (i.getUserId() == currentUser.getUserId()) {
                i.setUser(currentUser);
            }
        }

        return messages;
    }
}
