package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.liferay.mobile.android.v62.mbmessage.MBMessageService;

import org.json.JSONArray;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Message;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.util.EDMSession;
import br.leg.camara.labhacker.edemocracia.util.JSONReader;

public class MessageListFragment extends ContentListFragment<Message> {

    public static String ARG_THREAD = "thread";

    private Thread thread;

    private OnMessageSelectedListener listener;

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnMessageSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnMessageSelectedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
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

        FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();

        transaction.replace(R.id.container, fragment);

        transaction.addToBackStack(null);

        transaction.commit();

        return true;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (listener != null) {
            listener.onMessageSelect((Message) getListAdapter().getItem(position));
        }
    }

    @Override
    protected List<Message> fetchItems() throws Exception {
        EDMSession session = EDMSession.get(getActivity().getApplicationContext());

        JSONArray messages = new MBMessageService(session).getThreadMessages(
                thread.getGroupId(), 0, thread.getThreadId(), 0, -1, -1);

        return JSONReader.fromJSON(messages, Message.JSON_READER);
    }

    public interface OnMessageSelectedListener {
        public void onMessageSelect(Message message);
    }
}
