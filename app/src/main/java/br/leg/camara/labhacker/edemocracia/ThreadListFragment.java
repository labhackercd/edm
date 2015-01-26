package br.leg.camara.labhacker.edemocracia;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.liferay.mobile.android.v62.mbmessage.MBMessageService;
import com.liferay.mobile.android.v62.mbthread.MBThreadService;

import org.json.JSONArray;

import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.content.Message;
import br.leg.camara.labhacker.edemocracia.util.EDMBatchSession;
import br.leg.camara.labhacker.edemocracia.util.EDMSession;
import br.leg.camara.labhacker.edemocracia.content.Thread;
import br.leg.camara.labhacker.edemocracia.util.JSONReader;


public class ThreadListFragment extends ContentListFragment<Thread> {

    public static String ARG_PARENT = "parent";

    private Group group;
    private OnThreadSelectedListener listener;

    public static ThreadListFragment newInstance(Group group) {
        ThreadListFragment fragment = new ThreadListFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_PARENT, group);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Bundle args = getArguments();

        if (args != null) {
            group = args.getParcelable(ARG_PARENT);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnThreadSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnThreadSelectedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (listener != null) {
            listener.onThreadSelected((Thread) getListAdapter().getItem(position));
        }
    }

    @Override
    protected List<Thread> fetchItems() throws Exception {
        EDMSession session = EDMSession.get(getActivity().getApplicationContext());

        assert session != null;

        MBThreadService service = new MBThreadService(session);

        JSONArray jsonThreads = service.getGroupThreads(group.getGroupId(), -1, 0, -1, -1);

        List<Thread> threads = JSONReader.fromJSON(jsonThreads, Thread.JSON_READER);

        EDMBatchSession batchSession = new EDMBatchSession(session);

        MBMessageService messageService = new MBMessageService(batchSession);

        for (Thread thread : threads) {
            messageService.getMessage(thread.getRootMessageId());
        }

        List<Message> messages = JSONReader.fromJSON(batchSession.invoke(), Message.JSON_READER);

        for (Message message : messages) {
            threads.get(messages.indexOf(message)).setRootMessage(message);
        }

        return threads;
    }

    public interface OnThreadSelectedListener {
        public void onThreadSelected(Thread thread);
    }
}
